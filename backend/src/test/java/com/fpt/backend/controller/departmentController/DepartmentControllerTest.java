// package com.fpt.backend.controller.departmentController;

// import com.fpt.backend.dto.request.department.DepartmentRequestDTO;
// import com.fpt.backend.dto.response.department.DepartmentResponseDTO;
// import com.fpt.backend.exception.AppExceptionHandler;
// import com.fpt.backend.exception.NotFoundException;
// import com.fpt.backend.service.interfaces.department.IDepartmentService;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.test.web.servlet.setup.MockMvcBuilders;
// import org.springframework.web.server.ResponseStatusException;

// import java.util.UUID;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.verifyNoInteractions;
// import static org.mockito.Mockito.when;
// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static
// org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static
// org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @ExtendWith(MockitoExtension.class)
// class DepartmentControllerTest {

// @Mock
// private IDepartmentService departmentService;

// private MockMvc mockMvc;

// @BeforeEach
// void setUp() {
// DepartmentController departmentController =
// new DepartmentController(departmentService);

// mockMvc = MockMvcBuilders
// .standaloneSetup(departmentController)
// .setControllerAdvice(new AppExceptionHandler())
// .build();
// }

// @Test
// void getDepartmentById_returnsDepartment() throws Exception {
// UUID departmentId = UUID.randomUUID();
// DepartmentResponseDTO response = DepartmentResponseDTO.builder()
// .id(departmentId)
// .departmentName("Human Resources")
// .departmentCode("HR")
// .departmentStatus("Active")
// .build();
// when(departmentService.getDepartmentById(departmentId))
// .thenReturn(response);

// mockMvc.perform(get("/api/v1/departments/{id}", departmentId))
// .andExpect(status().isOk())
// .andExpect(jsonPath("$.status").value(200))
// .andExpect(jsonPath("$.data.id").value(departmentId.toString()))
// .andExpect(jsonPath("$.data.departmentCode").value("HR"));
// }

// @Test
// void getDepartmentById_returnsBadRequestForInvalidUuid() throws Exception {
// mockMvc.perform(get("/api/v1/departments/{id}", ":id"))
// .andExpect(status().isBadRequest())
// .andExpect(jsonPath("$.status").value(400))
// .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));

// verifyNoInteractions(departmentService);
// }

// @Test
// void getDepartmentById_returnsNotFoundWhenDepartmentDoesNotExist() throws
// Exception {
// UUID departmentId = UUID.randomUUID();
// when(departmentService.getDepartmentById(departmentId))
// .thenThrow(new NotFoundException("Department not found"));

// mockMvc.perform(get("/api/v1/departments/{id}", departmentId))
// .andExpect(status().isNotFound())
// .andExpect(jsonPath("$.status").value(404))
// .andExpect(jsonPath("$.message").value("Department not found"));
// }

// @Test
// void createDepartment_returnsCreatedForValidRequest() throws Exception {
// UUID departmentId = UUID.randomUUID();
// DepartmentResponseDTO response = DepartmentResponseDTO.builder()
// .id(departmentId)
// .departmentName("Human Resources")
// .departmentCode("HR")
// .departmentStatus("Active")
// .build();
// when(departmentService.createDepartment(any(DepartmentRequestDTO.class)))
// .thenReturn(response);

// mockMvc.perform(post("/api/v1/departments")
// .contentType(MediaType.APPLICATION_JSON)
// .content("""
// {
// "departmentName": "Human Resources",
// "departmentCode": "HR",
// "departmentStatus": "Active"
// }
// """))
// .andExpect(status().isCreated())
// .andExpect(jsonPath("$.status").value(201))
// .andExpect(jsonPath("$.data.id").value(departmentId.toString()));
// }

// @Test
// void createDepartment_returnsValidationErrorsForInvalidRequest() throws
// Exception {
// mockMvc.perform(post("/api/v1/departments")
// .contentType(MediaType.APPLICATION_JSON)
// .content("""
// {
// "departmentName": " ",
// "departmentCode": "1-INVALID",
// "departmentStatus": "Archived"
// }
// """))
// .andExpect(status().isBadRequest())
// .andExpect(jsonPath("$.status").value(400))
// .andExpect(jsonPath("$.data.departmentName").exists())
// .andExpect(jsonPath("$.data.departmentCode").exists())
// .andExpect(jsonPath("$.data.departmentStatus").exists());

// verifyNoInteractions(departmentService);
// }

// @Test
// void createDepartment_returnsConflictForDuplicateCode() throws Exception {
// when(departmentService.createDepartment(any(DepartmentRequestDTO.class)))
// .thenThrow(new ResponseStatusException(
// HttpStatus.CONFLICT,
// "Department code is already in use"
// ));

// mockMvc.perform(post("/api/v1/departments")
// .contentType(MediaType.APPLICATION_JSON)
// .content("""
// {
// "departmentName": "Human Resources",
// "departmentCode": "HR",
// "departmentStatus": "Active"
// }
// """))
// .andExpect(status().isConflict())
// .andExpect(jsonPath("$.status").value(409))
// .andExpect(jsonPath("$.message").value("Department code is already in use"));
// }
// }
