package com.fpt.backend.service.impl;

import com.fpt.backend.entity.ContractStatusHistory;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Projects;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ContractDocumentRendererTest {
    private static final String DIRECTOR_SIGNATURE_TEXT =
            "Đã ký điện tử bởi Tran Thi Director";

    private final ContractDocumentRenderer renderer =
            new ContractDocumentRenderer();

    @Test
    void rendersContractFieldsManualValuesAndSignerNames() {
        Contracts contract = createContract();
        List<ContractStatusHistory> history = List.of(
                signature("SIGN_PARTNER", "Nguyen Van Partner", 12),
                signature("SIGN_DIRECTOR", "Tran Thi Director", 11)
        );

        ContractDocumentRenderer.RenderedDocument rendered = renderer.render(
                contract,
                history,
                Map.of("contract_value", "150000000")
        );

        assertThat(rendered.content())
                .contains("CON-2026-0001")
                .contains("Hop dong dich vu")
                .contains("01/08/2026")
                .contains("31/07/2027")
                .contains("150.000.000 VNĐ")
                .contains("Tran Thi Director")
                .contains("Nguyen Van Partner")
                .doesNotContain("{{");
        assertThat(rendered.fullySigned()).isTrue();
    }

    @Test
    void hidesSignerIdentityUntilTheSigningEventExists() {
        ContractDocumentRenderer.RenderedDocument rendered = renderer.render(
                createContract(),
                List.of(),
                Map.of("contract_value", "150000000")
        );

        assertThat(rendered.content()).contains("Chưa ký");
        assertThat(rendered.directorSignerName()).isNull();
        assertThat(rendered.partnerSignerName()).isNull();
        assertThat(rendered.fullySigned()).isFalse();
    }

    @Test
    void createsReadableVietnamesePdf() throws Exception {
        Contracts contract = createContract();
        ContractDocumentRenderer.RenderedDocument rendered = renderer.render(
                contract,
                List.of(
                        signature("SIGN_PARTNER", "Nguyen Van Partner", 12),
                        signature("SIGN_DIRECTOR", "Tran Thi Director", 11)
                ),
                Map.of("contract_value", "150000000")
        );
        byte[] pdf = new ContractPdfGenerator().generate(contract, rendered);
        Files.createDirectories(Path.of("target"));
        Files.write(Path.of("target", "contract-pdf-preview.pdf"), pdf);

        assertThat(new String(pdf, 0, 4, StandardCharsets.US_ASCII))
                .isEqualTo("%PDF");
        try (PDDocument document = Loader.loadPDF(pdf)) {
            String extracted = new PDFTextStripper().getText(document);
            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(1);
            assertThat(extracted)
                    .contains("HỢP ĐỒNG ĐIỆN TỬ")
                    .contains("CON-2026-0001")
                    .contains("Tran Thi Director")
                    .contains("Nguyen Van Partner")
                    .doesNotContain("{{");
        }
    }

    @Test
    void keepsExplicitTemplatePagesAndRendersSignaturesOnEveryPage()
            throws Exception {
        Contracts contract = createContract();
        contract.setContractContent("""
                PAGE ONE
                Director: {{director_name}}
                {{director_signature}}

                <!-- pagebreak -->

                PAGE TWO
                Director: {{director_name}}
                {{director_signature}}
                Partner: {{partner_name}}
                {{partner_signature}}
                """);
        ContractDocumentRenderer.RenderedDocument rendered = renderer.render(
                contract,
                List.of(
                        signature("SIGN_PARTNER", "Nguyen Van Partner", 12),
                        signature("SIGN_DIRECTOR", "Tran Thi Director", 11)
                ),
                Map.of()
        );

        assertThat(rendered.content())
                .contains("<!-- pagebreak -->")
                .contains("Nguyen Van Partner");
        assertThat(countDirectorSignatures(rendered.content())).isEqualTo(2);

        byte[] pdf = new ContractPdfGenerator().generate(contract, rendered);
        try (PDDocument document = Loader.loadPDF(pdf)) {
            String extracted = new PDFTextStripper().getText(document);
            assertThat(document.getNumberOfPages()).isGreaterThanOrEqualTo(2);
            assertThat(extracted)
                    .contains("PAGE ONE")
                    .contains("PAGE TWO")
                    .doesNotContain("pagebreak");
            assertThat(countDirectorSignatures(extracted)).isEqualTo(2);
        }
    }

    private Contracts createContract() {
        Projects project = new Projects();
        project.setProjectName("Du an hop dong");

        Contracts contract = new Contracts();
        contract.setContractNumber("CON-2026-0001");
        contract.setContractTitle("Hop dong dich vu");
        contract.setContractStatus("ACTIVE");
        contract.setEffectiveDate(LocalDate.of(2026, 8, 1));
        contract.setExpirationDate(LocalDate.of(2027, 7, 31));
        contract.setContractCreateBy("Contract Seed Employee");
        contract.setProject(project);
        contract.setContractContent("""
                HỢP ĐỒNG CUNG CẤP DỊCH VỤ

                Số hợp đồng: {{contract_number}}
                Tên hợp đồng: {{contract_title}}
                Ngày hiệu lực: {{effective_date}}
                Ngày hết hạn: {{expiration_date}}
                Giá trị hợp đồng: {{contract_value}}

                ĐẠI DIỆN BÊN A
                {{director_name}}
                {{director_signature}}

                ĐẠI DIỆN BÊN B
                {{partner_name}}
                {{partner_signature}}
                """);
        return contract;
    }

    private ContractStatusHistory signature(
            String action,
            String actorName,
            int hour
    ) {
        ContractStatusHistory history = new ContractStatusHistory();
        history.setAction(action);
        history.setActorName(actorName);
        history.setSignerAgeVerified(true);
        history.setChangedAt(LocalDateTime.of(2026, 8, 2, hour, 0));
        return history;
    }

    private static int countDirectorSignatures(String content) {
        return content.split(Pattern.quote(DIRECTOR_SIGNATURE_TEXT), -1).length - 1;
    }
}
