package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractTemplateBlockRequest;
import com.fpt.backend.dto.request.contract.ContractTemplateLayout;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.ProjectMember;
import com.fpt.backend.entity.UserRole;
import com.fpt.backend.entity.Users;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Component
public class ContractPdfGenerator {
    private static final String NATIONAL_HEADER =
            "CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM";
    private static final String NATIONAL_MOTTO =
            "Độc lập - Tự do - Hạnh phúc";
    private static final Pattern PAGE_BREAK_PATTERN = Pattern.compile(
            "^<!--\\s*pagebreak\\s*-->$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern STANDALONE_FORM_FIELD_PATTERN = Pattern.compile(
            "^\\{\\{\\s*(project_name|director_name|director_signature|"
                    + "partner_name|partner_signature)\\s*}}$",
            Pattern.CASE_INSENSITIVE
    );
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int SIGNATURE_IMAGE_MAX_BYTES = 5 * 1024 * 1024;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile(
            "\\{\\{\\s*([a-zA-Z][a-zA-Z0-9_]*)\\s*}}"
    );
    private static final Set<String> PARTY_A_ROLES = Set.of(
            "CEO",
            "DIRECTOR"
    );
    private static final Set<String> PARTY_B_ROLES = Set.of(
            "PARTNER",
            "EXTERNAL",
            "EXTERNAL_PARTNER"
    );
    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] generate(
            Contracts contract,
            ContractDocumentRenderer.RenderedDocument renderedDocument
    ) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDFont regularFont = loadUnicodeFont(document, false);
            PDFont boldFont = loadUnicodeFont(document, true);
            configureDocumentInformation(document, contract);

            PartyInformation partyA = resolveParty(
                    contract,
                    renderedDocument.directorSignerName(),
                    PARTY_A_ROLES
            );
            PartyInformation partyB = resolveParty(
                    contract,
                    renderedDocument.partnerSignerName(),
                    PARTY_B_ROLES
            );

            try (DocumentWriter writer = new DocumentWriter(
                    document,
                    regularFont,
                    boldFont,
                    contract
            )) {
                for (ContractTemplateBlockRequest block : resolveBlocks(contract)) {
                    if (Boolean.FALSE.equals(block.enabled())) {
                        continue;
                    }
                    String heading = renderBlockText(
                            block.heading(),
                            renderedDocument.placeholderValues()
                    );
                    String content = renderBlockText(
                            block.content(),
                            renderedDocument.placeholderValues()
                    );
                    switch (block.type()) {
                        case "NATIONAL_HEADER" -> writer.writeNationalHeader(
                                heading,
                                content
                        );
                        case "CONTRACT_HEADING" -> writer.writeContractHeading(
                                heading,
                                content
                        );
                        case "LEGAL_INTRODUCTION" ->
                                writer.writeLegalIntroduction(content);
                        case "PARTY_A" -> writer.writeParty(heading, partyA);
                        case "PARTY_B" -> writer.writeParty(heading, partyB);
                        case "CLAUSE_HEADING" -> writer.writeSectionTitle(heading);
                        case "CONTENT" -> writer.writeContractContent(
                                contract.getContractContent(),
                                renderedDocument.content()
                        );
                        case "SIGNATURE_SECTION" -> writer.writeSignatureSection(
                                renderedDocument,
                                heading,
                                renderBlockText(
                                        block.leftLabel(),
                                        renderedDocument.placeholderValues()
                                ),
                                renderBlockText(
                                        block.rightLabel(),
                                        renderedDocument.placeholderValues()
                                )
                        );
                        default -> {
                            // Layout validation prevents unsupported block types.
                        }
                    }
                }
            }

            document.save(output);
            /// ma hoa o day sang byte[] calculate 256
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate contract PDF", exception);
        }
    }

    private List<ContractTemplateBlockRequest> resolveBlocks(Contracts contract) {
        String layoutJson = contract.getContractLayoutJson();
        if (layoutJson == null || layoutJson.isBlank()) {
            return ContractTemplateLayoutMapper.defaultBlocks();
        }
        try {
            ContractTemplateLayout layout = objectMapper.readValue(
                    layoutJson,
                    ContractTemplateLayout.class
            );
            return layout == null || layout.blocks() == null
                    || layout.blocks().isEmpty()
                    ? ContractTemplateLayoutMapper.defaultBlocks()
                    : layout.blocks();
        } catch (JacksonException exception) {
            return ContractTemplateLayoutMapper.defaultBlocks();
        }
    }

    private String renderBlockText(
            String template,
            Map<String, String> values
    ) {
        if (template == null) {
            return null;
        }
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder rendered = new StringBuilder();
        while (matcher.find()) {
            String replacement = values.getOrDefault(
                    matcher.group(1).toLowerCase(Locale.ROOT),
                    "Chưa cập nhật"
            );
            matcher.appendReplacement(
                    rendered,
                    Matcher.quoteReplacement(replacement)
            );
        }
        matcher.appendTail(rendered);
        return rendered.toString();
    }

    private PartyInformation resolveParty(
            Contracts contract,
            String signerName,
            Set<String> acceptedRoles
    ) {
        List<ProjectMember> members = contract.getProject() == null
                || contract.getProject().getProjectMembers() == null
                ? List.of()
                : contract.getProject().getProjectMembers();

        Users user = members.stream()
                .map(ProjectMember::getUser)
                .filter(Objects::nonNull)
                .filter(candidate -> sameName(userDisplayName(candidate), signerName))
                .findFirst()
                .orElseGet(() -> members.stream()
                        .map(ProjectMember::getUser)
                        .filter(Objects::nonNull)
                        .filter(candidate -> rolesFor(candidate).stream()
                                .anyMatch(acceptedRoles::contains))
                        .findFirst()
                        .orElse(null));

        String resolvedName = hasText(signerName)
                ? signerName.trim()
                : userDisplayName(user);
        if (user == null) {
            return new PartyInformation(
                    resolvedName,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        String departmentName = user.getDepartment() == null
                ? null
                : user.getDepartment().getDepartmentName();
        String companyName = user.getDepartment() == null
                || user.getDepartment().getCompany() == null
                ? null
                : user.getDepartment().getCompany().getCompanyName();

        return new PartyInformation(
                resolvedName,
                rolesFor(user).stream().findFirst().orElse(null),
                user.getEmail(),
                user.getNumberPhone(),
                user.getDob(),
                departmentName,
                companyName
        );
    }

    private PDFont loadUnicodeFont(PDDocument document, boolean bold)
            throws IOException {
        for (Path path : fontCandidates(bold)) {
            if (Files.isRegularFile(path)) {
                return PDType0Font.load(document, path.toFile());
            }
        }

        throw new IOException(
                "A Unicode TrueType font is required to export Vietnamese contract PDFs"
        );
    }

    private List<Path> fontCandidates(boolean bold) {
        String windowsTimesFont = bold ? "timesbd.ttf" : "times.ttf";
        String windowsArialFont = bold ? "arialbd.ttf" : "arial.ttf";
        String liberationFont = bold
                ? "LiberationSerif-Bold.ttf"
                : "LiberationSerif-Regular.ttf";
        String dejavuFont = bold ? "DejaVuSerif-Bold.ttf" : "DejaVuSerif.ttf";
        List<Path> paths = new ArrayList<>();
        String windowsDirectory = System.getenv("WINDIR");

        if (hasText(windowsDirectory)) {
            paths.add(Path.of(windowsDirectory, "Fonts", windowsTimesFont));
            paths.add(Path.of(windowsDirectory, "Fonts", windowsArialFont));
        }

        paths.add(Path.of("C:\\Windows\\Fonts\\" + windowsTimesFont));
        paths.add(Path.of("C:\\Windows\\Fonts\\" + windowsArialFont));
        paths.add(Path.of("/usr/share/fonts/truetype/liberation2/" + liberationFont));
        paths.add(Path.of("/usr/share/fonts/truetype/dejavu/" + dejavuFont));
        return paths;
    }

    private void configureDocumentInformation(
            PDDocument document,
            Contracts contract
    ) {
        PDDocumentInformation information = document.getDocumentInformation();
        information.setTitle(safeValue(contract.getContractTitle()));
        information.setSubject("Contract document");
        information.setAuthor(safeValue(contract.getContractCreateBy()));
        information.setCreator("E-CONTRACT Management System");
    }

    private static String userDisplayName(Users user) {
        if (user == null) {
            return null;
        }

        String firstName = user.getFirstName() == null
                ? ""
                : user.getFirstName().trim();
        String lastName = user.getLastName() == null
                ? ""
                : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return hasText(fullName) ? fullName : user.getEmail();
    }

    private static boolean sameName(String first, String second) {
        return hasText(first)
                && hasText(second)
                && first.trim().equalsIgnoreCase(second.trim());
    }

    private static String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "";
        }
        String normalized = role.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        String compact = normalized.replace("_", "");
        return switch (compact) {
            case "ADMIN", "ADMINISTRATOR" -> "ADMIN";
            case "MANAGER", "HEADOFDEPARTMENT", "DEPARTMENTHEAD" ->
                    "HEAD_OF_DEPARTMENT";
            case "PARTNER", "EXTERNAL", "EXTERNALPARTNER",
                    "EXTERNALPARTNERS", "EXTERNALPARNER", "EXTERNALPARNERS" ->
                    "EXTERNAL_PARTNER";
            default -> normalized;
        };
    }

    private static Set<String> rolesFor(Users user) {
        LinkedHashSet<String> roles = new LinkedHashSet<>();
        if (user == null || user.getUserRoles() == null) {
            return roles;
        }
        user.getUserRoles().stream()
                .filter(Objects::nonNull)
                .map(UserRole::getRole)
                .filter(Objects::nonNull)
                .forEach(role -> {
                    addRole(roles, role.getRoleCode());
                    addRole(roles, role.getRoleName());
                });
        return roles;
    }

    private static void addRole(Set<String> roles, String value) {
        String normalized = normalizeRole(value);
        if (!normalized.isEmpty()) {
            roles.add(normalized);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String safeValue(String value) {
        return hasText(value) ? value.trim() : "Chưa cập nhật";
    }

    private record PartyInformation(
            String fullName,
            String role,
            String email,
            String phoneNumber,
            String dateOfBirth,
            String departmentName,
            String companyName
    ) {
    }

    private static final class DocumentWriter implements AutoCloseable {
        private static final PDRectangle PAGE_SIZE = PDRectangle.A4;
        private static final float MARGIN = 52f;
        private static final float FOOTER_HEIGHT = 32f;
        private static final float BODY_WIDTH = PAGE_SIZE.getWidth() - (MARGIN * 2);

        private final PDDocument document;
        private final PDFont regularFont;
        private final PDFont boldFont;
        private final Contracts contract;
        private PDPageContentStream stream;
        private float cursorY;
        private int pageNumber;

        private DocumentWriter(
                PDDocument document,
                PDFont regularFont,
                PDFont boldFont,
                Contracts contract
        ) throws IOException {
            this.document = document;
            this.regularFont = regularFont;
            this.boldFont = boldFont;
            this.contract = contract;
            newPage();
        }

        private void writeNationalHeader() throws IOException {
            writeCenteredText(NATIONAL_HEADER, boldFont, 12.5f, 18f, 0f);
            writeCenteredText(NATIONAL_MOTTO, boldFont, 11.5f, 17f, 5f);
            ensureSpace(12f);
            float lineWidth = 160f;
            float lineX = (PAGE_SIZE.getWidth() - lineWidth) / 2f;
            drawLine(lineX, cursorY, lineX + lineWidth, cursorY, 0.7f);
            cursorY -= 22f;
        }

        private void writeNationalHeader(String heading, String motto)
                throws IOException {
            writeCenteredText(
                    hasText(heading) ? heading : NATIONAL_HEADER,
                    boldFont,
                    12.5f,
                    18f,
                    0f
            );
            writeCenteredText(
                    hasText(motto) ? motto : NATIONAL_MOTTO,
                    boldFont,
                    11.5f,
                    17f,
                    5f
            );
            ensureSpace(12f);
            float lineWidth = 160f;
            float lineX = (PAGE_SIZE.getWidth() - lineWidth) / 2f;
            drawLine(lineX, cursorY, lineX + lineWidth, cursorY, 0.7f);
            cursorY -= 22f;
        }

        private void writeContractHeading() throws IOException {
            String title = safe(contract.getContractTitle())
                    .toUpperCase(Locale.forLanguageTag("vi-VN"));
            writeCenteredText(title, boldFont, 16f, 22f, 4f);
            writeCenteredText(
                    "Số: " + safe(contract.getContractNumber()),
                    regularFont,
                    11f,
                    16f,
                    16f
            );
        }

        private void writeContractHeading(String heading, String subtitle)
                throws IOException {
            String title = (hasText(heading)
                    ? heading
                    : safe(contract.getContractTitle()))
                    .toUpperCase(Locale.forLanguageTag("vi-VN"));
            writeCenteredText(title, boldFont, 16f, 22f, 4f);
            if (hasText(subtitle)) {
                writeCenteredText(subtitle, regularFont, 11f, 16f, 16f);
            }
        }

        private void writeLegalIntroduction() throws IOException {
            writeWrappedText(
                    "- Căn cứ các quy định pháp luật hiện hành;",
                    regularFont,
                    11f,
                    16f,
                    2f
            );
            writeWrappedText(
                    "- Căn cứ nhu cầu và sự thỏa thuận của các bên.",
                    regularFont,
                    11f,
                    16f,
                    8f
            );
            writeWrappedText(
                    "Hôm nay, " + formatContractDate() + ", các bên gồm:",
                    regularFont,
                    11f,
                    16f,
                    8f
            );
        }

        private void writeLegalIntroduction(String content) throws IOException {
            if (!hasText(content)) {
                return;
            }
            for (String line : content.replace("\r", "").split("\n", -1)) {
                if (line.isBlank()) {
                    cursorY -= 8f;
                } else {
                    writeWrappedText(line.strip(), regularFont, 11f, 16f, 3f);
                }
            }
            cursorY -= 5f;
        }

        private String formatContractDate() {
            LocalDate date = contract.getContractCreatedAt() == null
                    ? LocalDate.now()
                    : contract.getContractCreatedAt().toLocalDate();
            return "ngày " + date.getDayOfMonth()
                    + " tháng " + date.getMonthValue()
                    + " năm " + date.getYear();
        }

        private void writeParty(String heading, PartyInformation party)
                throws IOException {
            ensureSpace(130f);
            writeWrappedText(heading, boldFont, 12f, 17f, 3f);
            writePartyField("Họ và tên", party.fullName());
            writePartyField("Chức vụ/Vai trò", party.role());
            writePartyField("Email", party.email());
            writePartyField("Số điện thoại", party.phoneNumber());
            writePartyField("Ngày sinh", party.dateOfBirth());
            writePartyField("Phòng ban", party.departmentName());
            writePartyField("Đơn vị", party.companyName());
            cursorY -= 8f;
        }

        private void writePartyField(String label, String value)
                throws IOException {
            writeWrappedText(
                    label + ": " + safe(value),
                    regularFont,
                    11f,
                    15.5f,
                    0f
            );
        }

        private void writeSectionTitle() throws IOException {
            ensureSpace(38f);
            cursorY -= 8f;
            writeCenteredText(
                    "CÁC ĐIỀU KHOẢN HỢP ĐỒNG",
                    boldFont,
                    12f,
                    18f,
                    9f
            );
        }

        private void writeSectionTitle(String heading) throws IOException {
            if (!hasText(heading)) {
                return;
            }
            ensureSpace(38f);
            cursorY -= 8f;
            writeCenteredText(heading, boldFont, 12f, 18f, 9f);
        }

        private void writeContractContent(
                String templateContent,
                String renderedContent
        ) throws IOException {
            String normalized = prepareContractContent(
                    templateContent,
                    renderedContent
            );
            for (String rawLine : normalized.split("\n", -1)) {
                String line = rawLine.strip();
                if (PAGE_BREAK_PATTERN.matcher(line).matches()) {
                    newPage();
                    continue;
                }

                if (line.isEmpty()) {
                    ensureSpace(10f);
                    cursorY -= 9f;
                    continue;
                }

                if (isDocumentTitle(line)) {
                    writeCenteredText(line, boldFont, 14f, 20f, 8f);
                } else if (isHeading(line)) {
                    writeWrappedText(line, boldFont, 11.5f, 17f, 5f);
                } else {
                    writeWrappedText(line, regularFont, 11f, 16f, 2f);
                }
            }
        }

        private String prepareContractContent(
                String templateContent,
                String renderedContent
        ) {
            String normalizedTemplate = templateContent == null
                    ? ""
                    : templateContent.replace("\r", "");
            String normalizedRendered = renderedContent == null
                    ? ""
                    : renderedContent.replace("\r", "");
            String[] templateLines = normalizedTemplate.split("\n", -1);
            String[] renderedLines = normalizedRendered.split("\n", -1);
            List<String> visibleLines = new ArrayList<>();
            boolean placeholderOnlyContent = containsOnlyStandaloneFormFields(
                    templateLines
            );

            for (int index = 0; index < renderedLines.length; index++) {
                boolean standaloneFormField = placeholderOnlyContent
                        && index < templateLines.length
                        && STANDALONE_FORM_FIELD_PATTERN.matcher(
                                templateLines[index].strip()
                        ).matches();
                if (!standaloneFormField) {
                    visibleLines.add(renderedLines[index]);
                }
            }

            String content = withoutEmbeddedSignatureBlock(
                    String.join("\n", visibleLines)
            );
            return content.isBlank()
                    ? "Nội dung điều khoản chưa được cập nhật."
                    : content;
        }

        private boolean containsOnlyStandaloneFormFields(
                String[] templateLines
        ) {
            boolean hasFormField = false;
            for (String templateLine : templateLines) {
                String line = templateLine.strip();
                if (line.isEmpty() || PAGE_BREAK_PATTERN.matcher(line).matches()) {
                    continue;
                }
                if (!STANDALONE_FORM_FIELD_PATTERN.matcher(line).matches()) {
                    return false;
                }
                hasFormField = true;
            }
            return hasFormField;
        }

        private String withoutEmbeddedSignatureBlock(String content) {
            String normalized = content == null ? "" : content.replace("\r", "");
            String[] lines = normalized.split("\n", -1);
            int partyAIndex = -1;
            int partyBIndex = -1;

            for (int index = 0; index < lines.length; index++) {
                String heading = lines[index]
                        .strip()
                        .toUpperCase(Locale.forLanguageTag("vi-VN"));
                if ("ĐẠI DIỆN BÊN A".equals(heading)) {
                    partyAIndex = index;
                } else if (partyAIndex >= 0
                        && "ĐẠI DIỆN BÊN B".equals(heading)) {
                    partyBIndex = index;
                    break;
                }
            }

            if (partyAIndex < 0 || partyBIndex <= partyAIndex) {
                return normalized;
            }

            return String.join(
                    "\n",
                    java.util.Arrays.copyOfRange(lines, 0, partyAIndex)
            ).stripTrailing();
        }

        private void writeSignatureSection(
                ContractDocumentRenderer.RenderedDocument renderedDocument,
                String sectionHeading,
                String leftLabel,
                String rightLabel
        ) throws IOException {
            ensureSpace(175f);
            cursorY -= 16f;
            writeCenteredText(
                    hasText(sectionHeading) ? sectionHeading : "ĐẠI DIỆN CÁC BÊN",
                    boldFont,
                    12f,
                    18f,
                    16f
            );

            float columnGap = 30f;
            float columnWidth = (BODY_WIDTH - columnGap) / 2f;
            float rightX = MARGIN + columnWidth + columnGap;
            float topY = cursorY;

            writeSignatureColumn(
                    hasText(leftLabel) ? leftLabel : "BÊN A",
                    renderedDocument.directorSignerName(),
                    renderedDocument.directorSignedAt(),
                    renderedDocument.directorSignatureImagePath(),
                    renderedDocument.directorDocumentHash(),
                    MARGIN,
                    columnWidth,
                    topY
            );
            writeSignatureColumn(
                    hasText(rightLabel) ? rightLabel : "BÊN B",
                    renderedDocument.partnerSignerName(),
                    renderedDocument.partnerSignedAt(),
                    renderedDocument.partnerSignatureImagePath(),
                    renderedDocument.partnerDocumentHash(),
                    rightX,
                    columnWidth,
                    topY
            );
            cursorY = topY - 138f;
        }

        private void writeSignatureColumn(
                String heading,
                String signerName,
                LocalDateTime signedAt,
                String signatureImagePath,
                String documentHash,
                float x,
                float width,
                float topY
        ) throws IOException {
            drawCenteredWithin(heading, boldFont, 11.5f, x, width, topY, 15f);

            if (signedAt == null || !hasText(signerName)) {
                drawCenteredWithin(
                        "Ký và ghi rõ họ tên",
                        regularFont,
                        10f,
                        x,
                        width,
                        topY - 22f,
                        14f
                );
                return;
            } else {
                drawCenteredWithin(
                        "ĐÃ KÝ ĐIỆN TỬ",
                        boldFont,
                        10f,
                        x,
                        width,
                        topY - 28f,
                        14f
                );
                drawCenteredWithin(
                        "Ký lúc " + DATE_TIME_FORMATTER.format(signedAt),
                        regularFont,
                        9.5f,
                        x,
                        width,
                        topY - 50f,
                        13f
                );
                drawSignatureImage(
                        signatureImagePath,
                        x,
                        width,
                        topY - 102f
                );
            }

            drawCenteredWithin(
                    signerName,
                    boldFont,
                    10.5f,
                    x,
                    width,
                    topY - 112f,
                    14f
            );
            if (hasText(documentHash)) {
                drawCenteredWithin(
                        "SHA-256: " + documentHash.substring(
                                0,
                                Math.min(12, documentHash.length())
                        ) + "...",
                        regularFont,
                        7.5f,
                        x,
                        width,
                        topY - 130f,
                        10f
                );
            }
        }

        private void drawSignatureImage(
                String imagePath,
                float x,
                float columnWidth,
                float bottomY
        ) {
            if (!hasText(imagePath)) {
                return;
            }
            try {
                URLConnection connection = URI.create(imagePath)
                        .toURL()
                        .openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(5000);
                byte[] bytes;
                try (InputStream input = connection.getInputStream()) {
                    bytes = input.readNBytes(SIGNATURE_IMAGE_MAX_BYTES + 1);
                }
                if (bytes.length > SIGNATURE_IMAGE_MAX_BYTES) {
                    return;
                }
                PDImageXObject image = PDImageXObject.createFromByteArray(
                        document,
                        bytes,
                        "contract-signature"
                );
                float maxWidth = Math.min(110f, columnWidth - 20f);
                float maxHeight = 42f;
                float scale = Math.min(
                        maxWidth / image.getWidth(),
                        maxHeight / image.getHeight()
                );
                float imageWidth = image.getWidth() * scale;
                float imageHeight = image.getHeight() * scale;
                stream.drawImage(
                        image,
                        x + ((columnWidth - imageWidth) / 2f),
                        bottomY,
                        imageWidth,
                        imageHeight
                );
            } catch (Exception ignored) {
                // Signer name, timestamp and hash remain as the fallback proof.
            }
        }

        private void writeCenteredText(
                String text,
                PDFont font,
                float fontSize,
                float leading,
                float afterSpacing
        ) throws IOException {
            List<String> lines = wrap(text, font, fontSize, BODY_WIDTH);
            ensureSpace((lines.size() * leading) + afterSpacing);
            for (String line : lines) {
                float width = textWidth(line, font, fontSize);
                drawText(
                        line,
                        font,
                        fontSize,
                        Math.max(MARGIN, (PAGE_SIZE.getWidth() - width) / 2f),
                        cursorY
                );
                cursorY -= leading;
            }
            cursorY -= afterSpacing;
        }

        private void writeWrappedText(
                String text,
                PDFont font,
                float fontSize,
                float leading,
                float afterSpacing
        ) throws IOException {
            List<String> lines = wrap(text, font, fontSize, BODY_WIDTH);
            for (String line : lines) {
                ensureSpace(leading + afterSpacing);
                drawText(line, font, fontSize, MARGIN, cursorY);
                cursorY -= leading;
            }
            cursorY -= afterSpacing;
        }

        private void drawCenteredWithin(
                String text,
                PDFont font,
                float fontSize,
                float x,
                float width,
                float y,
                float leading
        ) throws IOException {
            float currentY = y;
            for (String line : wrap(text, font, fontSize, width)) {
                float lineWidth = textWidth(line, font, fontSize);
                drawText(
                        line,
                        font,
                        fontSize,
                        x + Math.max(0f, (width - lineWidth) / 2f),
                        currentY
                );
                currentY -= leading;
            }
        }

        private List<String> wrap(
                String text,
                PDFont font,
                float fontSize,
                float maxWidth
        ) throws IOException {
            String normalized = safe(text).replace('\t', ' ').trim();
            if (normalized.isEmpty()) {
                return List.of("");
            }

            List<String> lines = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            for (String word : normalized.split("\\s+")) {
                String candidate = current.isEmpty()
                        ? word
                        : current + " " + word;
                if (textWidth(candidate, font, fontSize) <= maxWidth) {
                    current.setLength(0);
                    current.append(candidate);
                } else {
                    if (!current.isEmpty()) {
                        lines.add(current.toString());
                        current.setLength(0);
                    }
                    current.append(word);
                }
            }
            if (!current.isEmpty()) {
                lines.add(current.toString());
            }
            return lines;
        }

        private float textWidth(String text, PDFont font, float fontSize)
                throws IOException {
            return font.getStringWidth(safe(text)) / 1000f * fontSize;
        }

        private void ensureSpace(float requiredHeight) throws IOException {
            if (cursorY - requiredHeight < MARGIN + FOOTER_HEIGHT) {
                newPage();
            }
        }

        private void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }

            PDPage page = new PDPage(PAGE_SIZE);
            document.addPage(page);
            stream = new PDPageContentStream(document, page);
            pageNumber++;
            drawFooter();
            cursorY = PAGE_SIZE.getHeight() - MARGIN;
        }

        private void drawFooter() throws IOException {
            drawLine(
                    MARGIN,
                    40f,
                    PAGE_SIZE.getWidth() - MARGIN,
                    40f,
                    0.4f
            );
            drawText(
                    safe(contract.getContractNumber()),
                    regularFont,
                    8.5f,
                    MARGIN,
                    24f
            );
            String pageLabel = "Trang " + pageNumber;
            float pageLabelWidth = textWidth(pageLabel, regularFont, 8.5f);
            drawText(
                    pageLabel,
                    regularFont,
                    8.5f,
                    PAGE_SIZE.getWidth() - MARGIN - pageLabelWidth,
                    24f
            );
        }

        private void drawLine(
                float startX,
                float startY,
                float endX,
                float endY,
                float width
        ) throws IOException {
            stream.moveTo(startX, startY);
            stream.lineTo(endX, endY);
            stream.setLineWidth(width);
            stream.stroke();
        }

        private void drawText(
                String text,
                PDFont font,
                float fontSize,
                float x,
                float y
        ) throws IOException {
            stream.beginText();
            stream.setFont(font, fontSize);
            stream.newLineAtOffset(x, y);
            stream.showText(safe(text).replace("\n", " "));
            stream.endText();
        }

        private boolean isHeading(String line) {
            String normalized = line.toUpperCase(Locale.forLanguageTag("vi-VN"));
            return normalized.startsWith("ĐIỀU ")
                    || normalized.startsWith("BÊN A:")
                    || normalized.startsWith("BÊN B:")
                    || normalized.startsWith("ĐẠI DIỆN");
        }

        private boolean isDocumentTitle(String line) {
            String normalized = line.toUpperCase(Locale.forLanguageTag("vi-VN"));
            return normalized.startsWith("HỢP ĐỒNG")
                    && line.equals(normalized);
        }

        private String safe(String value) {
            return hasText(value) ? value.trim() : "Chưa cập nhật";
        }

        @Override
        public void close() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }
    }
}
