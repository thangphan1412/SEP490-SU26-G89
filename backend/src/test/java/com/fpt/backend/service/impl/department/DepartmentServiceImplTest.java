// package com.fpt.backend.service.impl.department;

// import com.fpt.backend.dto.request.department.DepartmentRequestDTO;
// import com.fpt.backend.dto.response.department.DepartmentResponseDTO;
// import com.fpt.backend.entity.Departments;
// import com.fpt.backend.exception.BadHttpException;
// import com.fpt.backend.exception.NotFoundException;
// import com.fpt.backend.repository.department.DepartmentRepository;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.ArgumentCaptor;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.http.HttpStatus;
// import org.springframework.web.server.ResponseStatusException;

// import java.util.List;
// import java.util.Optional;
// import java.util.UUID;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class)
// class DepartmentServiceImplTest {

// @Mock
// private DepartmentRepository departmentRepository;

// @InjectMocks
// private DepartmentServiceImpl departmentService;

// @Test
// void getDepartmentById_returnsDepartmentWhenItExists() {
// UUID departmentId = UUID.randomUUID();
// Departments department = createDepartmentEntity(
// departmentId,
// "Human Resources",
// "HR",
// "Active"
// );
// when(departmentRepository.findById(departmentId))
// .thenReturn(Optional.of(department));

// DepartmentResponseDTO result =
// departmentService.getDepartmentById(departmentId);

// assertEquals(departmentId, result.getId());
// assertEquals("Human Resources", result.getDepartmentName());
// assertEquals("HR", result.getDepartmentCode());
// assertEquals("Active", result.getDepartmentStatus());
// }

// @Test
// void getDepartmentById_throwsNotFoundWhenDepartmentDoesNotExist() {
// UUID departmentId = UUID.randomUUID();
// when(departmentRepository.findById(departmentId))
// .thenReturn(Optional.empty());

// NotFoundException exception = assertThrows(
// NotFoundException.class,
// () -> departmentService.getDepartmentById(departmentId)
// );

// assertEquals("Department not found", exception.getMessage());
// }

// @Test
// void createDepartment_normalizesAndSavesValidRequest() {
// UUID departmentId = UUID.randomUUID();
// DepartmentRequestDTO request = new DepartmentRequestDTO(
// " Human Resources ",
// " hr_team ",
// "active"
// );
// when(departmentRepository.existsByDepartmentCodeIgnoreCase("HR_TEAM"))
// .thenReturn(false);
// when(departmentRepository.save(any(Departments.class)))
// .thenAnswer(invocation -> {
// Departments department = invocation.getArgument(0);
// department.setId(departmentId);
// return department;
// });

// DepartmentResponseDTO result = departmentService.createDepartment(request);

// ArgumentCaptor<Departments> departmentCaptor =
// ArgumentCaptor.forClass(Departments.class);
// verify(departmentRepository).save(departmentCaptor.capture());
// Departments savedDepartment = departmentCaptor.getValue();

// assertEquals("Human Resources", savedDepartment.getDepartmentName());
// assertEquals("HR_TEAM", savedDepartment.getDepartmentCode());
// assertEquals("Active", savedDepartment.getDepartmentStatus());
// assertNotNull(savedDepartment.getDepartmentCreatedAt());
// assertNull(savedDepartment.getUpdatedAt());
// assertEquals(departmentId, result.getId());
// }

// @Test
// void createDepartment_throwsConflictWhenCodeAlreadyExists() {
// DepartmentRequestDTO request = new DepartmentRequestDTO(
// "Human Resources",
// "HR",
// "Active"
// );
// when(departmentRepository.existsByDepartmentCodeIgnoreCase("HR"))
// .thenReturn(true);

// ResponseStatusException exception = assertThrows(
// ResponseStatusException.class,
// () -> departmentService.createDepartment(request)
// );

// assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
// verify(departmentRepository, never()).save(any(Departments.class));
// }

// @Test
// void createDepartment_rejectsInvalidDepartmentCode() {
// DepartmentRequestDTO request = new DepartmentRequestDTO(
// "Human Resources",
// "1-INVALID",
// "Active"
// );

// BadHttpException exception = assertThrows(
// BadHttpException.class,
// () -> departmentService.createDepartment(request)
// );

// assertEquals(
// "Department code must start with a letter and contain 2-50 uppercase letters,
// numbers, or underscores",
// exception.getMessage()
// );
// verify(departmentRepository, never()).save(any(Departments.class));
// }

// @Test
// void updateDepartment_updatesAndNormalizesValidRequest() {
// UUID departmentId = UUID.randomUUID();
// Departments existingDepartment = createDepartmentEntity(
// departmentId,
// "Information Technology",
// "IT",
// "Active"
// );
// DepartmentRequestDTO request = new DepartmentRequestDTO(
// " Technology Operations ",
// " tech_ops ",
// "inactive"
// );
// when(departmentRepository.findById(departmentId))
// .thenReturn(Optional.of(existingDepartment));
// when(departmentRepository.existsByDepartmentCodeIgnoreCaseAndIdNot(
// "TECH_OPS",
// departmentId
// )).thenReturn(false);
// when(departmentRepository.save(existingDepartment))
// .thenReturn(existingDepartment);

// DepartmentResponseDTO result = departmentService.updateDepartment(
// departmentId,
// request
// );

// assertEquals("Technology Operations",
// existingDepartment.getDepartmentName());
// assertEquals("TECH_OPS", existingDepartment.getDepartmentCode());
// assertEquals("Inactive", existingDepartment.getDepartmentStatus());
// assertNotNull(existingDepartment.getUpdatedAt());
// assertEquals("Technology Operations", result.getDepartmentName());
// }

// @Test
// void searchDepartments_rejectsUnsupportedStatusFilter() {
// BadHttpException exception = assertThrows(
// BadHttpException.class,
// () -> departmentService.searchDepartments("", "Archived")
// );

// assertEquals(
// "Department status filter must be Active or Inactive",
// exception.getMessage()
// );
// verify(departmentRepository, never()).searchAndFilter(any(), any());
// }

// @Test
// void searchDepartments_returnsEmptyListWhenRepositoryHasNoData() {
// when(departmentRepository.searchAndFilter("", ""))
// .thenReturn(List.of());

// List<DepartmentResponseDTO> result =
// departmentService.searchDepartments("", "");

// assertNotNull(result);
// assertEquals(0, result.size());
// }

// private Departments createDepartmentEntity(
// UUID id,
// String name,
// String code,
// String status
// ) {
// Departments department = Departments.builder()
// .departmentName(name)
// .departmentCode(code)
// .departmentStatus(status)
// .company(null)
// .build();
// department.setId(id);
// return department;
// }
// }
