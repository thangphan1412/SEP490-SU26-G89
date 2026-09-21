package com.fpt.backend.controller.ControllerContractTest;




import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.backend.controller.contractController.ContractTemplateController;
import com.fpt.backend.dto.request.contract.ContractTemplateRequest;
import com.fpt.backend.dto.response.contract.ContractTemplateResponse;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.NotFoundException;
import com.fpt.backend.service.interfaces.contract.ContractTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContractTemplateControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ContractTemplateService contractTemplateService;

    private ObjectMapper objectMapper;

    private UUID contractTypeId;

    @BeforeEach
    void setUp() {

        ContractTemplateController controller =
                new ContractTemplateController(
                        contractTemplateService
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        objectMapper = new ObjectMapper();

        contractTypeId = UUID.randomUUID();
    }

    // =========================================================
    // Helper
    // =========================================================

    private String validJson() {

        return """
                {
                    "contractTypeId": "%s",
                    "contractTemplateName": "Contract Template Test",
                    "status": "Active",
                    "createdBy": "%s",
                    "contractTemplateDescription": "Test description"
                }
                """.formatted(
                contractTypeId,
                UUID.randomUUID()
        );
    }

    private ContractTemplateResponse mockResponse() {

        return mock(ContractTemplateResponse.class);
    }

    // =========================================================
    // CT01: All valid fields
    // =========================================================

    @Test
    void createContractTemplate_CT01_shouldReturn201_whenAllFieldsAreValid()
            throws Exception {

        when(contractTemplateService.createContractTemplate(any()))
                .thenReturn(mockResponse());

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.message").value("Created"));

        verify(contractTemplateService, times(1))
                .createContractTemplate(any());
    }

    // =========================================================
    // CT02: ContractTypeId = null
    // =========================================================

    @Test
    void createContractTemplate_CT02_shouldHandleNullContractTypeId()
            throws Exception {

        String json = """
                {
                    "contractTypeId": null,
                    "contractTemplateName": "Contract Template Test",
                    "status": "Active",
                    "createdBy": "admin",
                    "contractTemplateDescription": "Test"
                }
                """;

        when(contractTemplateService.createContractTemplate(any()))
                .thenThrow(new BadHttpException(
                        "Contract type is required"
                ));

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is5xxServerError());

        verify(contractTemplateService, times(1))
                .createContractTemplate(any());
    }

    // =========================================================
    // CT03: ContractTypeId = empty
    // =========================================================

    @Test
    void createContractTemplate_CT03_shouldRejectEmptyContractTypeId()
            throws Exception {

        String json = """
                {
                    "contractTypeId": "",
                    "contractTemplateName": "Contract Template Test",
                    "status": "Active",
                    "createdBy": "admin",
                    "contractTemplateDescription": "Test"
                }
                """;

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(contractTemplateService);
    }

    // =========================================================
    // CT04: ContractTypeId = invalid UUID
    // =========================================================

    @Test
    void createContractTemplate_CT04_shouldRejectInvalidContractTypeId()
            throws Exception {

        String json = """
                {
                    "contractTypeId": "00000000-000000000-1",
                    "contractTemplateName": "Contract Template Test",
                    "status": "Active",
                    "createdBy": "admin",
                    "contractTemplateDescription": "Test"
                }
                """;

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(contractTemplateService);
    }

    // =========================================================
    // CT05: ContractTypeId = non-existing UUID
    // =========================================================

    @Test
    void createContractTemplate_CT05_shouldHandleNonExistingContractTypeId()
            throws Exception {

        String json = """
                {
                    "contractTypeId": "00000000-0000-0000-0000-000000000001",
                    "contractTemplateName": "Contract Template Test",
                    "status": "Active",
                    "createdBy": "admin",
                    "contractTemplateDescription": "Test"
                }
                """;

        when(contractTemplateService.createContractTemplate(any()))
                .thenThrow(new NotFoundException(
                        "Contract type not found"
                ));

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is5xxServerError());

        verify(contractTemplateService, times(1))
                .createContractTemplate(any());
    }

    // =========================================================
    // CT06-08: ContractTemplateName
    // =========================================================

    static Stream<Arguments> invalidTemplateNames() {

        return Stream.of(
                Arguments.of("CT06", null),
                Arguments.of("CT07", ""),
                Arguments.of("CT08", " ")
        );
    }

    @ParameterizedTest(name = "{0}: TemplateName = {1}")
    @MethodSource("invalidTemplateNames")
    void createContractTemplate_shouldHandleInvalidTemplateName(
            String testCase,
            String templateName
    ) throws Exception {

        String nameJson = templateName == null
                ? "null"
                : objectMapper.writeValueAsString(templateName);

        String json = """
                {
                    "contractTypeId": "%s",
                    "contractTemplateName": %s,
                    "status": "Active",
                    "createdBy": "admin",
                    "contractTemplateDescription": "Test"
                }
                """.formatted(contractTypeId, nameJson);

        when(contractTemplateService.createContractTemplate(any()))
                .thenThrow(new BadHttpException(
                        "Contract template name is required"
                ));

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is5xxServerError());

        verify(contractTemplateService, times(1))
                .createContractTemplate(any());
    }

    // =========================================================
    // CT09: TemplateName valid
    // =========================================================

    @Test
    void createContractTemplate_CT09_shouldReturn201_whenTemplateNameIsValid()
            throws Exception {

        String json = """
                {
                    "contractTypeId": "%s",
                    "contractTemplateName": "Contract Template Test",
                    "status": "Active",
                    "createdBy": "admin",
                    "contractTemplateDescription": "Test"
                }
                """.formatted(contractTypeId);

        when(contractTemplateService.createContractTemplate(any()))
                .thenReturn(mockResponse());

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(contractTemplateService, times(1))
                .createContractTemplate(any());
    }

    // =========================================================
    // CT10-11: Status
    // =========================================================

    static Stream<Arguments> validStatuses() {

        return Stream.of(
                Arguments.of("CT10", "Active"),
                Arguments.of("CT11", "UnActive")
        );
    }

    @ParameterizedTest(name = "{0}: Status = {1}")
    @MethodSource("validStatuses")
    void createContractTemplate_shouldHandleStatus(
            String testCase,
            String status
    ) throws Exception {

        String json = """
                {
                    "contractTypeId": "%s",
                    "contractTemplateName": "Contract Template Test",
                    "status": "%s",
                    "createdBy": "admin",
                    "contractTemplateDescription": "Test"
                }
                """.formatted(contractTypeId, status);

        when(contractTemplateService.createContractTemplate(any()))
                .thenReturn(mockResponse());

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(contractTemplateService, times(1))
                .createContractTemplate(any());
    }

    // =========================================================
    // CT12: CreateBy = null
    // =========================================================

    @Test
    void createContractTemplate_CT12_shouldReturn201_whenCreateByIsNull()
            throws Exception {

        String json = """
                {
                    "contractTypeId": "%s",
                    "contractTemplateName": "Contract Template Test",
                    "status": "Active",
                    "createdBy": null,
                    "contractTemplateDescription": "Test"
                }
                """.formatted(contractTypeId);

        when(contractTemplateService.createContractTemplate(any()))
                .thenReturn(mockResponse());

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(contractTemplateService, times(1))
                .createContractTemplate(any());
    }

    // =========================================================
    // CT13: CreateBy = empty
    // =========================================================

    @Test
    void createContractTemplate_CT13_shouldReturn201_whenCreateByIsEmpty()
            throws Exception {

        String json = """
                {
                    "contractTypeId": "%s",
                    "contractTemplateName": "Contract Template Test",
                    "status": "Active",
                    "createdBy": "",
                    "contractTemplateDescription": "Test"
                }
                """.formatted(contractTypeId);

        when(contractTemplateService.createContractTemplate(any()))
                .thenReturn(mockResponse());

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(contractTemplateService, times(1))
                .createContractTemplate(any());
    }

    // =========================================================
    // CT14: Description = null
    // =========================================================

    @Test
    void createContractTemplate_CT14_shouldReturn201_whenDescriptionIsNull()
            throws Exception {

        String json = """
                {
                    "contractTypeId": "%s",
                    "contractTemplateName": "Contract Template Test",
                    "status": "Active",
                    "createdBy": "admin",
                    "contractTemplateDescription": null
                }
                """.formatted(contractTypeId);

        when(contractTemplateService.createContractTemplate(any()))
                .thenReturn(mockResponse());

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(contractTemplateService, times(1))
                .createContractTemplate(any());
    }

    // =========================================================
    // CT15: Description = empty
    // =========================================================

    @Test
    void createContractTemplate_CT15_shouldReturn201_whenDescriptionIsEmpty()
            throws Exception {

        String json = """
                {
                    "contractTypeId": "%s",
                    "contractTemplateName": "Contract Template Test",
                    "status": "Active",
                    "createdBy": "admin",
                    "contractTemplateDescription": ""
                }
                """.formatted(contractTypeId);

        when(contractTemplateService.createContractTemplate(any()))
                .thenReturn(mockResponse());

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        verify(contractTemplateService, times(1))
                .createContractTemplate(any());
    }

    // =========================================================
    // CT16: Duplicate template name
    // =========================================================

    @Test
    void createContractTemplate_CT16_shouldHandleDuplicateName()
            throws Exception {

        when(contractTemplateService.createContractTemplate(any()))
                .thenThrow(new BadHttpException(
                        "A contract template with this name already exists in the selected type"
                ));

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().is5xxServerError());

        verify(contractTemplateService, times(1))
                .createContractTemplate(any());
    }

    // =========================================================
    // CT17: Wrong content type
    // =========================================================

    @Test
    void createContractTemplate_CT17_shouldReturn415_whenContentTypeIsWrong()
            throws Exception {

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(validJson()))
                .andExpect(status().isUnsupportedMediaType());

        verifyNoInteractions(contractTemplateService);
    }

    // =========================================================
    // CT18: Malformed JSON
    // =========================================================

    @Test
    void createContractTemplate_CT18_shouldReturn400_whenJsonIsMalformed()
            throws Exception {

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(contractTemplateService);
    }

    // =========================================================
    // CT19: Request body null
    // =========================================================

    @Test
    void createContractTemplate_CT19_shouldHandleNullRequest()
            throws Exception {

        when(contractTemplateService.createContractTemplate(any()))
                .thenThrow(new BadHttpException(
                        "Contract template information is required"
                ));

        mockMvc.perform(post("/api/v1/contract-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().is5xxServerError());

        verify(contractTemplateService, times(1))
                .createContractTemplate(isNull());
    }
}