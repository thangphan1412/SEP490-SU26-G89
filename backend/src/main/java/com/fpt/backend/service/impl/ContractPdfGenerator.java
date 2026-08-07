package com.fpt.backend.service.impl;

import com.fpt.backend.entity.Contracts;
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
import java.util.regex.Pattern;

@Component
public class ContractPdfGenerator {
    private static final String DOCUMENT_TITLE = "HỢP ĐỒNG ĐIỆN TỬ";
    private static final Pattern PAGE_BREAK_PATTERN = Pattern.compile(
            "^<!--\\s*pagebreak\\s*-->$",
            Pattern.CASE_INSENSITIVE
    );
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] generate(
            Contracts contract,
            ContractDocumentRenderer.RenderedDocument renderedDocument
    ) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDFont regularFont = loadUnicodeFont(document, false);
            PDFont boldFont = loadUnicodeFont(document, true);
            configureDocumentInformation(document, contract);

            try (DocumentWriter writer = new DocumentWriter(
                    document,
                    regularFont,
                    boldFont,
                    contract
            )) {
                writer.writeTitle();
                writer.writeMetadata("Số hợp đồng", contract.getContractNumber());
                writer.writeMetadata("Tên hợp đồng", contract.getContractTitle());
                writer.writeMetadata(
                        "Dự án",
                        contract.getProject() == null
                                ? null
                                : contract.getProject().getProjectName()
                );
                writer.writeMetadata(
                        "Thời hạn",
                        formatDate(contract.getEffectiveDate())
                                + " - " + formatDate(contract.getExpirationDate())
                );
                writer.writeMetadata("Trạng thái", contract.getContractStatus());
                writer.writeSeparator();
                writer.writeContractContent(renderedDocument.content());
                writer.writeSignatureSummary(renderedDocument);
            }

            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate contract PDF", exception);
        }
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
        String windowsFont = bold ? "arialbd.ttf" : "arial.ttf";
        String linuxFont = bold ? "DejaVuSans-Bold.ttf" : "DejaVuSans.ttf";
        List<Path> paths = new ArrayList<>();
        String windowsDirectory = System.getenv("WINDIR");

        if (windowsDirectory != null && !windowsDirectory.isBlank()) {
            paths.add(Path.of(windowsDirectory, "Fonts", windowsFont));
        }

        paths.add(Path.of("C:\\Windows\\Fonts\\" + windowsFont));
        paths.add(Path.of("/usr/share/fonts/truetype/dejavu/" + linuxFont));
        return paths;
    }

    private void configureDocumentInformation(
            PDDocument document,
            Contracts contract
    ) {
        PDDocumentInformation information = document.getDocumentInformation();
        information.setTitle(safeValue(contract.getContractTitle()));
        information.setSubject("Completed electronic contract");
        information.setAuthor(safeValue(contract.getContractCreateBy()));
        information.setCreator("E-CONTRACT Management System");
    }

    private String formatDate(java.time.LocalDate value) {
        return value == null ? "Chưa cập nhật" : DATE_FORMATTER.format(value);
    }

    private String safeValue(String value) {
        return value == null || value.isBlank() ? "Chưa cập nhật" : value.trim();
    }

    private static final class DocumentWriter implements AutoCloseable {
        private static final PDRectangle PAGE_SIZE = PDRectangle.A4;
        private static final float MARGIN = 52f;
        private static final float HEADER_HEIGHT = 48f;
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

        private void writeTitle() throws IOException {
            ensureSpace(40f);
            for (String line : wrap(DOCUMENT_TITLE, boldFont, 18f)) {
                float textWidth = textWidth(line, boldFont, 18f);
                drawText(
                        line,
                        boldFont,
                        18f,
                        Math.max(MARGIN, (PAGE_SIZE.getWidth() - textWidth) / 2f),
                        cursorY
                );
                cursorY -= 24f;
            }
            cursorY -= 8f;
        }

        private void writeMetadata(String label, String value) throws IOException {
            writeWrappedText(
                    label + ": " + safe(value),
                    regularFont,
                    10.5f,
                    15f,
                    1f
            );
        }

        private void writeSeparator() throws IOException {
            ensureSpace(22f);
            cursorY -= 5f;
            stream.moveTo(MARGIN, cursorY);
            stream.lineTo(PAGE_SIZE.getWidth() - MARGIN, cursorY);
            stream.setLineWidth(0.6f);
            stream.stroke();
            cursorY -= 16f;
        }

        private void writeContractContent(String content) throws IOException {
            String normalized = content == null ? "" : content.replace("\r", "");
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

        private void writeSignatureSummary(
                ContractDocumentRenderer.RenderedDocument renderedDocument
        ) throws IOException {
            ensureSpace(150f);
            cursorY -= 14f;
            writeCenteredText(
                    "XÁC NHẬN CHỮ KÝ ĐIỆN TỬ",
                    boldFont,
                    12f,
                    18f,
                    14f
            );

            float columnGap = 24f;
            float columnWidth = (BODY_WIDTH - columnGap) / 2f;
            float leftX = MARGIN;
            float rightX = MARGIN + columnWidth + columnGap;
            float topY = cursorY;

            drawText("ĐẠI DIỆN BÊN A", boldFont, 11f, leftX, topY);
            drawText("ĐẠI DIỆN BÊN B", boldFont, 11f, rightX, topY);
            drawText(
                    safe(renderedDocument.directorSignerName()),
                    regularFont,
                    10.5f,
                    leftX,
                    topY - 22f
            );
            drawText(
                    safe(renderedDocument.partnerSignerName()),
                    regularFont,
                    10.5f,
                    rightX,
                    topY - 22f
            );
            drawText(
                    signatureTime(renderedDocument.directorSignedAt()),
                    regularFont,
                    9.5f,
                    leftX,
                    topY - 42f
            );
            drawText(
                    signatureTime(renderedDocument.partnerSignedAt()),
                    regularFont,
                    9.5f,
                    rightX,
                    topY - 42f
            );
            drawText(
                    renderedDocument.directorSignerName() == null
                            ? "Chưa ký"
                            : "Đã ký điện tử",
                    boldFont,
                    10f,
                    leftX,
                    topY - 64f
            );
            drawText(
                    renderedDocument.partnerSignerName() == null
                            ? "Chưa ký"
                            : "Đã ký điện tử",
                    boldFont,
                    10f,
                    rightX,
                    topY - 64f
            );
            cursorY = topY - 92f;
        }

        private void writeCenteredText(
                String text,
                PDFont font,
                float fontSize,
                float leading,
                float afterSpacing
        ) throws IOException {
            List<String> lines = wrap(text, font, fontSize);
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
            List<String> lines = wrap(text, font, fontSize);
            for (String line : lines) {
                ensureSpace(leading + afterSpacing);
                drawText(line, font, fontSize, MARGIN, cursorY);
                cursorY -= leading;
            }
            cursorY -= afterSpacing;
        }

        private List<String> wrap(
                String text,
                PDFont font,
                float fontSize
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
                if (textWidth(candidate, font, fontSize) <= BODY_WIDTH) {
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
            if (cursorY - requiredHeight
                    < MARGIN + FOOTER_HEIGHT) {
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
            drawHeaderAndFooter();
            cursorY = PAGE_SIZE.getHeight() - MARGIN - HEADER_HEIGHT;
        }

        private void drawHeaderAndFooter() throws IOException {
            drawText(
                    "E-CONTRACT",
                    boldFont,
                    9.5f,
                    MARGIN,
                    PAGE_SIZE.getHeight() - 30f
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
            String normalized = line.toUpperCase();
            return normalized.startsWith("ĐIỀU ")
                    || normalized.startsWith("BÊN A:")
                    || normalized.startsWith("BÊN B:")
                    || normalized.startsWith("ĐẠI DIỆN");
        }

        private boolean isDocumentTitle(String line) {
            String normalized = line.toUpperCase();
            return normalized.startsWith("HỢP ĐỒNG")
                    && line.equals(normalized);
        }

        private String signatureTime(LocalDateTime value) {
            return value == null
                    ? "Chưa có thời gian ký"
                    : "Ký lúc " + DATE_TIME_FORMATTER.format(value);
        }

        private String safe(String value) {
            return value == null || value.isBlank()
                    ? "Chưa cập nhật"
                    : value.trim();
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
