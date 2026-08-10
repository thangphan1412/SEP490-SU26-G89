package com.fpt.backend.service.impl;

import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.ProjectMember;
import com.fpt.backend.entity.Users;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

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
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Set<String> PARTY_A_ROLES = Set.of(
            "CEO",
            "DIRECTOR"
    );
    private static final Set<String> PARTY_B_ROLES = Set.of(
            "PARTNER",
            "EXTERNAL",
            "EXTERNAL_PARTNER"
    );

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
                writer.writeNationalHeader();
                writer.writeContractHeading();
                writer.writeParty("BÊN A", partyA);
                writer.writeParty("BÊN B", partyB);
                writer.writeSectionTitle();
                writer.writeContractContent(renderedDocument.content());
                writer.writeSignatureSection(renderedDocument, partyA, partyB);
            }

            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate contract PDF", exception);
        }
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
                        .filter(candidate -> acceptedRoles.contains(
                                normalizeRole(candidate.getRole())
                        ))
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
                user.getRole(),
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
        return role == null
                ? ""
                : role.trim()
                        .toUpperCase(Locale.ROOT)
                        .replace('-', '_')
                        .replace(' ', '_');
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

        private void writeContractContent(String content) throws IOException {
            String normalized = withoutEmbeddedSignatureBlock(content);
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

            if (partyAIndex < lines.length / 2 || partyBIndex <= partyAIndex) {
                return normalized;
            }

            return String.join(
                    "\n",
                    java.util.Arrays.copyOfRange(lines, 0, partyAIndex)
            ).stripTrailing();
        }

        private void writeSignatureSection(
                ContractDocumentRenderer.RenderedDocument renderedDocument,
                PartyInformation partyA,
                PartyInformation partyB
        ) throws IOException {
            ensureSpace(175f);
            cursorY -= 16f;
            writeCenteredText("ĐẠI DIỆN CÁC BÊN", boldFont, 12f, 18f, 16f);

            float columnGap = 30f;
            float columnWidth = (BODY_WIDTH - columnGap) / 2f;
            float rightX = MARGIN + columnWidth + columnGap;
            float topY = cursorY;

            writeSignatureColumn(
                    "BÊN A",
                    partyA,
                    renderedDocument.directorSignerName(),
                    renderedDocument.directorSignedAt(),
                    MARGIN,
                    columnWidth,
                    topY
            );
            writeSignatureColumn(
                    "BÊN B",
                    partyB,
                    renderedDocument.partnerSignerName(),
                    renderedDocument.partnerSignedAt(),
                    rightX,
                    columnWidth,
                    topY
            );
            cursorY = topY - 138f;
        }

        private void writeSignatureColumn(
                String heading,
                PartyInformation party,
                String signerName,
                LocalDateTime signedAt,
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
            }

            String printedName = hasText(signerName)
                    ? signerName
                    : party.fullName();
            drawCenteredWithin(
                    safe(printedName),
                    boldFont,
                    10.5f,
                    x,
                    width,
                    topY - 112f,
                    14f
            );
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
