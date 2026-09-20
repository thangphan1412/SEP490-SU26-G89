package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractTemplateBlockRequest;
import com.fpt.backend.dto.request.contract.ContractTemplateLayout;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.exception.BadHttpException;
import lombok.RequiredArgsConstructor;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Component
@RequiredArgsConstructor
public class ContractPdfGenerator {
    private static final Pattern PAGE_BREAK_PATTERN = Pattern.compile(
            "^<!--\\s*pagebreak\\s*-->$",
            Pattern.CASE_INSENSITIVE);
    private final ContractTemplateLayoutMapper layoutMapper;

    public byte[] generate(
            Contracts contract,
            ContractDocumentRenderer.RenderedDocument renderedDocument) {
        if (contract.getContractTemplateVersion() != null
                && !layoutMapper.isFullDocument(contract.getContractLayoutJson())) {
            throw new BadHttpException(
                    "This contract uses an old clause-only template version. "
                            + "Create a full-document version before generating a new PDF");
        }

        try (PDDocument document = new PDDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDFont regularFont = loadUnicodeFont(document, false);
            PDFont boldFont = loadUnicodeFont(document, true);
            configureDocumentInformation(document, contract);

            try (DocumentWriter writer = new DocumentWriter(
                    document,
                    regularFont,
                    boldFont)) {
                writer.writeContractContent(renderedDocument.content());
            }

            document.save(output);
            /// ma hoa o day sang byte[] calculate 256
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
                "A Unicode TrueType font is required to export Vietnamese contract PDFs");
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
            Contracts contract) {
        PDDocumentInformation information = document.getDocumentInformation();
        information.setTitle(safeValue(contract.getContractTitle()));
        information.setSubject("Contract document");
        information.setAuthor(safeValue(contract.getContractCreateBy()));
        information.setCreator("E-CONTRACT Management System");
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String safeValue(String value) {
        return hasText(value) ? value.trim() : "Chưa cập nhật";
    }

    private static final class DocumentWriter implements AutoCloseable {
        private static final PDRectangle PAGE_SIZE = PDRectangle.A4;
        private static final float MARGIN = 52f;
        private static final float BODY_WIDTH = PAGE_SIZE.getWidth() - (MARGIN * 2);

        private final PDDocument document;
        private final PDFont regularFont;
        private final PDFont boldFont;
        private PDPageContentStream stream;
        private float cursorY;

        private DocumentWriter(
                PDDocument document,
                PDFont regularFont,
                PDFont boldFont) throws IOException {
            this.document = document;
            this.regularFont = regularFont;
            this.boldFont = boldFont;
            newPage();
        }

        private void writeContractContent(String renderedContent)
                throws IOException {
            String normalized = renderedContent == null
                    ? ""
                    : renderedContent.replace("\r", "");
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

        private void writeCenteredText(
                String text,
                PDFont font,
                float fontSize,
                float leading,
                float afterSpacing) throws IOException {
            List<String> lines = wrap(text, font, fontSize, BODY_WIDTH);
            ensureSpace((lines.size() * leading) + afterSpacing);
            for (String line : lines) {
                float width = textWidth(line, font, fontSize);
                drawText(
                        line,
                        font,
                        fontSize,
                        Math.max(MARGIN, (PAGE_SIZE.getWidth() - width) / 2f),
                        cursorY);
                cursorY -= leading;
            }
            cursorY -= afterSpacing;
        }

        private void writeWrappedText(
                String text,
                PDFont font,
                float fontSize,
                float leading,
                float afterSpacing) throws IOException {
            List<String> lines = wrap(text, font, fontSize, BODY_WIDTH);
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
                float fontSize,
                float maxWidth) throws IOException {
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
            if (cursorY - requiredHeight < MARGIN) {
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
            cursorY = PAGE_SIZE.getHeight() - MARGIN;
        }

        private void drawText(
                String text,
                PDFont font,
                float fontSize,
                float x,
                float y) throws IOException {
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
