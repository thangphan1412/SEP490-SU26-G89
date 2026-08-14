package com.fpt.backend.controller.departmentController;

import com.fpt.backend.constant.ApiConstant;
import com.fpt.backend.dto.request.department.DepartmentRequestDTO;
import com.fpt.backend.dto.response.department.DepartmentResponseDTO;
import com.fpt.backend.service.interfaces.department.IDepartmentService;
import com.fpt.backend.util.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstant.Department.DEPARTMENTS)
@RequiredArgsConstructor
public class DepartmentController {

    private final IDepartmentService departmentService;

    @GetMapping
    public ResponseEntity<BaseResponse<List<DepartmentResponseDTO>>> getAllDepartments() {
        List<DepartmentResponseDTO> departments = departmentService.getAllDepartments();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.<List<DepartmentResponseDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Successfully fetched all departments")
                        .data(departments)
                        .build());
    }

    @GetMapping(ApiConstant.Department.LIST)
    public ResponseEntity<BaseResponse<List<DepartmentResponseDTO>>> searchDepartments(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "") String status
    ) {
        List<DepartmentResponseDTO> departments = departmentService.searchDepartments(search, status);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(BaseResponse.<List<DepartmentResponseDTO>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Successfully searched departments")
                        .data(departments)
                        .build());
    }

    @GetMapping(ApiConstant.Department.BY_ID)
    public ResponseEntity<BaseResponse<DepartmentResponseDTO>> getDepartmentById(
            @PathVariable UUID id
    ) {
        DepartmentResponseDTO department = departmentService.getDepartmentById(id);

        return ResponseEntity.ok(
                BaseResponse.<DepartmentResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Department found")
                        .data(department)
                        .build()
        );
    }

    @PostMapping
    public ResponseEntity<BaseResponse<DepartmentResponseDTO>> createDepartment(
            @Valid @RequestBody DepartmentRequestDTO request
    ) {
        DepartmentResponseDTO department = departmentService.createDepartment(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.<DepartmentResponseDTO>builder()
                        .status(HttpStatus.CREATED.value())
                        .message("Department created successfully")
                        .data(department)
                        .build()
        );
    }

    @PutMapping(ApiConstant.Department.BY_ID)
    public ResponseEntity<BaseResponse<DepartmentResponseDTO>> updateDepartment(
            @PathVariable UUID id,
            @Valid @RequestBody DepartmentRequestDTO request
    ) {
        DepartmentResponseDTO department = departmentService.updateDepartment(id, request);

        return ResponseEntity.ok(
                BaseResponse.<DepartmentResponseDTO>builder()
                        .status(HttpStatus.OK.value())
                        .message("Department updated successfully")
                        .data(department)
                        .build()
        );
    }
}
