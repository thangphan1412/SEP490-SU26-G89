package com.fpt.backend.controller.ControllerContractTest;

import com.fpt.backend.controller.contractController.ContractController;
import com.fpt.backend.dto.request.contract.ContractRequest;
import com.fpt.backend.dto.response.contract.ContractResponse;
import com.fpt.backend.exception.AppExceptionHandler;
import com.fpt.backend.exception.BadHttpException;
import com.fpt.backend.exception.GlobalException;
import com.fpt.backend.service.interfaces.contract.ContractService;
import com.fpt.backend.util.BaseResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class ContractControllerTest {

    @Mock
    private ContractService contractService;

    @InjectMocks
    private ContractController contractController;

    private MockMvc mockMvc;


    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .standaloneSetup(contractController)
                .setControllerAdvice(
                        new GlobalException(),
                        new AppExceptionHandler()
                )
                .build();
    }private static ContractRequest validRequest() {

        return requestWith(
                null,
                "HD-001",
                "Test Contract",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 11, 1),
                UUID.randomUUID()
        );
    }


    private static ContractRequest requestWith(
            UUID projectId,
            String contractNumber,
            String contractTitle,
            LocalDate effectiveDate,
            LocalDate expirationDate,
            UUID contractTypeId
    ) {

        return new ContractRequest(
                projectId,                  // 1. projectId
                null,                       // 2. phaseId
                null,                       // 3. taskId
                contractTypeId,              // 4. contractTypeId
                null,                       // 5. contractTemplateId
                null,                       // 6. contractTemplateVersionId
                contractNumber,              // 7. contractNumber
                contractTitle,               // 8. contractTitle
                null,                       // 9. contractStatus
                effectiveDate,               // 10. effectiveDate
                expirationDate,              // 11. expirationDate
                null,                       // 12. contractCreatedBy
                null,                       // 13. contractCreatedAt
                "Contract content",          // 14. contractContent
                null,                       // 15. contractLayoutJson
                false,                       // 16. saveAsTemplateVersion
                null,                       // 17. templateVersionName
                null,                       // 18. templateVersionNote
                null,                       // 19. previousContractId
                null,                       // 20. actorName
                null,                       // 21. actorRole
                Map.of(),                   // 22. attributeValues
                List.of()                   // 23. workflowAssignees
        );
    }



    /////////////
    @Test
    void createContract_shouldReturn201_whenAllPreconditionsAreSatisfied() {

        // ============================================================
        // ARRANGE
        // ============================================================

        // User đã authenticated
        // Server đang hoạt động
        // ProjectId được cung cấp
        UUID projectId = UUID.randomUUID();

        UUID contractTypeId = UUID.randomUUID();

        ContractRequest request = requestWith(
                projectId,
                "HD-001",
                "Test Contract",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 11, 1),
                contractTypeId
        );

        System.out.println(contractTypeId);
        ContractResponse expectedResponse =
                mock(ContractResponse.class);

        when(contractService.createContract(request))
                .thenReturn(expectedResponse);


        // ============================================================
        // ACT
        // ============================================================

        ResponseEntity<BaseResponse<ContractResponse>> response =
                contractController.createContract(request);


        // ============================================================
        // ASSERT
        // ============================================================

        // Kiểm tra HTTP status = 201 CREATED
        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        // Kiểm tra response body không null
        assertNotNull(response.getBody());

        // Kiểm tra status trong response
        assertEquals(
                HttpStatus.CREATED.value(),
                response.getBody().getStatus()
        );

        // Kiểm tra message
        assertEquals(
                "Created",
                response.getBody().getMessage()
        );

        // Kiểm tra data trả về đúng
        assertSame(
                expectedResponse,
                response.getBody().getData()
        );

        // Kiểm tra projectId được truyền đúng
        assertEquals(
                projectId,
                request.projectId()
        );

        // Kiểm tra contractTypeId được truyền đúng
        assertEquals(
                contractTypeId,
                request.contractTypeId()
        );

        // Kiểm tra các giá trị input
        assertEquals(
                "HD-001",
                request.contractNumber()
        );

        assertEquals(
                "Test Contract",
                request.contractTitle()
        );

        assertEquals(
                LocalDate.of(2026, 10, 1),
                request.effectiveDate()
        );

        assertEquals(
                LocalDate.of(2026, 11, 1),
                request.expirationDate()
        );

        // Kiểm tra service được gọi đúng một lần
        verify(contractService, times(1))
                .createContract(request);
    }
    /// contractnumber null

    @Test
    void createContract_shouldReturn201_whenContractNumberIsNull() {

        // ============================================================
        // ARRANGE
        // ============================================================

        UUID projectId = UUID.randomUUID();
        UUID contractTypeId = UUID.randomUUID();

        ContractRequest request = requestWith(
                projectId,
                null, // contractNumber = null
                "Test Contract",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 11, 1),
                contractTypeId
        );
        System.out.println(request.contractNumber());
        ContractResponse expectedResponse =
                mock(ContractResponse.class);

        when(contractService.createContract(request))
                .thenReturn(expectedResponse);

        // ============================================================
        // ACT
        // ============================================================

        ResponseEntity<BaseResponse<ContractResponse>> response =
                contractController.createContract(request);

        // ============================================================
        // LẤY MESSAGE THỰC TẾ TỪ BE
        // ============================================================

        assertNotNull(response.getBody());

        String message = response.getBody().getMessage();

        System.out.println("================================");
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("BE Message: " + message);
        System.out.println("BE Data: " + response.getBody().getData());
        System.out.println("================================");

        // ============================================================
        // ASSERT
        // ============================================================

        // HTTP status
        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        // Status trong BaseResponse
        assertEquals(
                HttpStatus.CREATED.value(),
                response.getBody().getStatus()
        );

        // Message BE không null
        assertNotNull(message);

        // ContractNumber = null
        assertNull(request.contractNumber());

        // Kiểm tra dữ liệu
        assertEquals(
                projectId,
                request.projectId()
        );

        assertEquals(
                contractTypeId,
                request.contractTypeId()
        );

        assertEquals(
                "Test Contract",
                request.contractTitle()
        );

        // Data trả về đúng
        assertSame(
                expectedResponse,
                response.getBody().getData()
        );

        // Service được gọi đúng một lần
        verify(contractService, times(1))
                .createContract(request);
    }
    /// contractnumber null

    @Test
    void createContract_shouldReturn201_whenContractTitleIsNull() {

        // ============================================================
        // ARRANGE
        // ============================================================

        UUID projectId = UUID.randomUUID();
        UUID contractTypeId = UUID.randomUUID();

        ContractRequest request = requestWith(
                projectId,
                "HD-001", // contractNumber = null
                null, // contractTitle = null
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 11, 1),
                contractTypeId
        );
        System.out.println(request.contractNumber());
        System.out.println(request.contractTitle());
        ContractResponse expectedResponse =
                mock(ContractResponse.class);

        when(contractService.createContract(request))
                .thenReturn(expectedResponse);

        // ============================================================
        // ACT
        // ============================================================

        ResponseEntity<BaseResponse<ContractResponse>> response =
                contractController.createContract(request);

        // ============================================================
        // LẤY MESSAGE THỰC TẾ TỪ BE
        // ============================================================

        assertNotNull(response.getBody());

        String message = response.getBody().getMessage();

        System.out.println("================================");
        System.out.println("HTTP Status: " + response.getStatusCode());
        System.out.println("BE Message: " + message);
        System.out.println("BE Data: " + response.getBody().getData());
        System.out.println("================================");

        // ============================================================
        // ASSERT
        // ============================================================

        // HTTP status
        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );

        // Status trong BaseResponse
        assertEquals(
                HttpStatus.CREATED.value(),
                response.getBody().getStatus()
        );

        // Message BE không null
        assertNotNull(message);

        // ContractTitle = null
        assertNull(request.contractTitle());

        // Kiểm tra dữ liệu
        assertEquals(
                projectId,
                request.projectId()
        );

        assertEquals(
                contractTypeId,
                request.contractTypeId()
        );



        // Data trả về đúng
        assertSame(
                expectedResponse,
                response.getBody().getData()
        );

        // Service được gọi đúng một lần
        verify(contractService, times(1))
                .createContract(request);
    }
}