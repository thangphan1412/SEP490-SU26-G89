package com.fpt.backend.service.impl.contract;

import com.fpt.backend.dto.request.contract.ContractTemplateLayout;
import com.fpt.backend.entity.ContractWorkflowStepInstance;
import com.fpt.backend.entity.Contracts;
import com.fpt.backend.entity.Users;
import com.fpt.backend.enums.ContractWorkflowActionType;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ContractFullDocumentTemplateTest {
    private final ContractTemplateLayoutMapper layoutMapper =
            new ContractTemplateLayoutMapper(new ObjectMapper());
    private final ContractDocumentRenderer renderer =
            new ContractDocumentRenderer();
    private final ContractPdfGenerator pdfGenerator =
            new ContractPdfGenerator(layoutMapper);

    @Test
    void fullDocumentModeExportsOnlyTheVersionContent() throws Exception {
        Contracts contract = contract("""
                CUSTOM DOCUMENT HEADER
                Contract: {{contract_title}}
                Number: {{contract_number}}

                ĐẠI DIỆN BÊN A
                {{director_signature}}

                ĐẠI DIỆN BÊN B
                {{partner_signature}}
                """);
        ContractTemplateLayout layout = layoutMapper.normalize(
                1,
                List.of(),
                null,
                ContractTemplateLayoutMapper.FULL_DOCUMENT_MODE
        );
        contract.setContractLayoutJson(layoutMapper.toJson(layout));

        ContractDocumentRenderer.RenderedDocument rendered = renderer.render(
                contract,
                List.of(),
                Map.of()
        );
        byte[] pdf = pdfGenerator.generate(contract, rendered);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            String extracted = new PDFTextStripper().getText(document);
            assertThat(extracted)
                    .contains("CUSTOM DOCUMENT HEADER")
                    .contains("Contract: Service Agreement")
                    .contains("Number: CON-2026-0001")
                    .contains("ĐẠI DIỆN BÊN A")
                    .contains("ĐẠI DIỆN BÊN B")
                    .contains("Chưa ký")
                    .doesNotContain("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM")
                    .doesNotContain("CÁC ĐIỀU KHOẢN HỢP ĐỒNG");
        }
    }

    @Test
    void fullDocumentModeKeepsStandaloneDynamicFields() throws Exception {
        Contracts contract = contract("{{contract_title}}");
        ContractTemplateLayout layout = layoutMapper.normalize(
                1,
                List.of(),
                null,
                ContractTemplateLayoutMapper.FULL_DOCUMENT_MODE
        );
        contract.setContractLayoutJson(layoutMapper.toJson(layout));

        byte[] pdf = pdfGenerator.generate(
                contract,
                renderer.render(contract, List.of(), Map.of())
        );

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(new PDFTextStripper().getText(document))
                    .contains("Service Agreement")
                    .doesNotContain("Nội dung điều khoản chưa được cập nhật");
        }
    }

    @Test
    void layoutsWithoutDocumentModeRemainLegacy() {
        ContractTemplateLayout legacyLayout = layoutMapper.normalize(
                1,
                List.of(),
                null
        );

        assertThat(legacyLayout.documentMode())
                .isEqualTo(ContractTemplateLayoutMapper.LEGACY_DOCUMENT_MODE);
        assertThat(layoutMapper.isFullDocument(layoutMapper.toJson(legacyLayout)))
                .isFalse();
        assertThat(layoutMapper.isFullDocument("{\"pageCount\":1,\"fields\":[]}"))
                .isFalse();
    }

    @Test
    void partyFieldsUseTheUsersAssignedToSigningSteps() {
        Users partyA = Users.builder()
                .firstName("An")
                .lastName("Nguyen")
                .email("an@example.com")
                .numberPhone("0901000001")
                .build();
        Users partyB = Users.builder()
                .firstName("Binh")
                .lastName("Tran")
                .email("binh@example.com")
                .numberPhone("0901000002")
                .build();
        Contracts contract = contract("""
                {{party_a_name}} | {{party_a_email}} | {{party_a_phone}}
                {{party_b_name}} | {{party_b_email}} | {{party_b_phone}}
                """);
        contract.setWorkflowStepInstances(List.of(
                signingStep(1, "CEO", partyA),
                signingStep(2, "ExternalPartners", partyB)
        ));

        ContractDocumentRenderer.RenderedDocument rendered = renderer.render(
                contract,
                List.of(),
                Map.of()
        );

        assertThat(rendered.content())
                .contains("An Nguyen | an@example.com | 0901000001")
                .contains("Binh Tran | binh@example.com | 0901000002");
    }

    private Contracts contract(String content) {
        Contracts contract = new Contracts();
        contract.setContractNumber("CON-2026-0001");
        contract.setContractTitle("Service Agreement");
        contract.setContractStatus("NEW");
        contract.setContractCreateBy("Template Tester");
        contract.setContractCreatedAt(LocalDateTime.of(2026, 9, 11, 9, 30));
        contract.setEffectiveDate(LocalDate.of(2026, 9, 11));
        contract.setExpirationDate(LocalDate.of(2027, 9, 11));
        contract.setContractContent(content);
        return contract;
    }

    private ContractWorkflowStepInstance signingStep(
            int order,
            String requiredRole,
            Users assignedUser
    ) {
        return ContractWorkflowStepInstance.builder()
                .stepOrder(order)
                .stepName("Sign " + order)
                .actionType(ContractWorkflowActionType.SIGN)
                .requiredRoleCode(requiredRole)
                .assignedUser(assignedUser)
                .build();
    }
}
