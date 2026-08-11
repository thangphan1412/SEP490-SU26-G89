package com.fpt.backend.service.impl.contract;

import com.fpt.backend.entity.Company;
import com.fpt.backend.entity.ContractStatusHistory;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Departments;
import com.fpt.backend.entity.ProjectMember;
import com.fpt.backend.entity.Projects;
import com.fpt.backend.entity.Users;
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
                .contains("Hợp đồng cung cấp dịch vụ")
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
                    .contains("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM")
                    .contains("Độc lập - Tự do - Hạnh phúc")
                    .contains("HỢP ĐỒNG CUNG CẤP DỊCH VỤ")
                    .contains("CON-2026-0001")
                    .contains("Tran Thi Director")
                    .contains("Nguyen Van Partner")
                    .doesNotContain("{{");
        }
    }

    @Test
    void createsUnsignedPdfWithPartyInformationAndBlankSigningArea()
            throws Exception {
        Contracts contract = createContract();
        contract.setContractStatus("NEW");
        ContractDocumentRenderer.RenderedDocument rendered = renderer.render(
                contract,
                List.of(),
                Map.of("contract_value", "150000000")
        );

        byte[] pdf = new ContractPdfGenerator().generate(contract, rendered);
        Files.createDirectories(Path.of("target"));
        Files.write(Path.of("target", "contract-pdf-unsigned-preview.pdf"), pdf);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String extracted = new PDFTextStripper().getText(document);
            assertThat(extracted)
                    .contains("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM")
                    .contains("BÊN A")
                    .contains("director@seed.local")
                    .contains("BÊN B")
                    .contains("partner@seed.local")
                    .contains("Ký và ghi rõ họ tên")
                    .doesNotContain("Chưa ký")
                    .doesNotContain("ĐÃ KÝ ĐIỆN TỬ")
                    .doesNotContain("Đã ký điện tử bởi");
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
        Company company = new Company();
        company.setCompanyName("Công ty E-CONTRACT");

        Departments directorDepartment = new Departments();
        directorDepartment.setDepartmentName("Ban Giám đốc");
        directorDepartment.setCompany(company);

        Departments partnerDepartment = new Departments();
        partnerDepartment.setDepartmentName("Đơn vị đối tác");
        partnerDepartment.setCompany(company);

        Users director = new Users();
        director.setFirstName("Tran Thi");
        director.setLastName("Director");
        director.setEmail("director@seed.local");
        director.setNumberPhone("0901000001");
        director.setDob("01/01/1980");
        director.setRole("DIRECTOR");
        director.setDepartment(directorDepartment);

        Users partner = new Users();
        partner.setFirstName("Nguyen Van");
        partner.setLastName("Partner");
        partner.setEmail("partner@seed.local");
        partner.setNumberPhone("0901000002");
        partner.setDob("02/02/1985");
        partner.setRole("PARTNER");
        partner.setDepartment(partnerDepartment);

        Projects project = new Projects();
        project.setProjectName("Du an hop dong");
        project.setProjectMembers(List.of(
                ProjectMember.builder().project(project).user(director).build(),
                ProjectMember.builder().project(project).user(partner).build()
        ));

        Contracts contract = new Contracts();
        contract.setContractNumber("CON-2026-0001");
        contract.setContractTitle("Hợp đồng cung cấp dịch vụ");
        contract.setContractStatus("ACTIVE");
        contract.setEffectiveDate(LocalDate.of(2026, 8, 1));
        contract.setExpirationDate(LocalDate.of(2027, 7, 31));
        contract.setContractCreateBy("Contract Seed Employee");
        contract.setProject(project);
        contract.setContractContent("""
                ĐIỀU 1. THÔNG TIN HỢP ĐỒNG
                Số hợp đồng: {{contract_number}}
                Tên hợp đồng: {{contract_title}}
                Ngày hiệu lực: {{effective_date}}
                Ngày hết hạn: {{expiration_date}}
                Giá trị hợp đồng: {{contract_value}}

                ĐIỀU 2. TRÁCH NHIỆM CÁC BÊN
                Các bên có trách nhiệm thực hiện đúng những điều khoản đã thỏa thuận.

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
