package com.fpt.backend.controller.departmentController;

import com.fpt.backend.constant.ApiConstant;
import com.fpt.backend.dto.request.department.DepartmentRequestDTO;
import com.fpt.backend.dto.response.department.DepartmentResponseDTO;
import com.fpt.backend.service.interfaces.department.IDepartmentService;
import com.fpt.backend.util.BaseResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstant.Department.DEPARTMENTS)
public class DepartmentController {

    @Autowired
    private IDepartmentService departmentService;

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
        try {
            DepartmentResponseDTO department = departmentService.getDepartmentById(id);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(BaseResponse.<DepartmentResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Department found")
                            .data(department)
                            .build());
        } catch (Exception exception) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.<DepartmentResponseDTO>builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .message(exception.getMessage())
                            .build());
        }
    }

    @PostMapping
    public ResponseEntity<BaseResponse<DepartmentResponseDTO>> createDepartment(
            @RequestBody DepartmentRequestDTO request
    ) {
        try {
            DepartmentResponseDTO department = departmentService.createDepartment(request);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(BaseResponse.<DepartmentResponseDTO>builder()
                            .status(HttpStatus.CREATED.value())
                            .message("Department created successfully")
                            .data(department)
                            .build());
        } catch (Exception exception) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.<DepartmentResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(exception.getMessage())
                            .build());
        }
    }

    @PutMapping(ApiConstant.Department.BY_ID)
    public ResponseEntity<BaseResponse<DepartmentResponseDTO>> updateDepartment(
            @PathVariable UUID id,
            @RequestBody DepartmentRequestDTO request
    ) {
        try {
            DepartmentResponseDTO department = departmentService.updateDepartment(id, request);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(BaseResponse.<DepartmentResponseDTO>builder()
                            .status(HttpStatus.OK.value())
                            .message("Department updated successfully")
                            .data(department)
                            .build());
        } catch (Exception exception) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(BaseResponse.<DepartmentResponseDTO>builder()
                            .status(HttpStatus.BAD_REQUEST.value())
                            .message(exception.getMessage())
                            .build());
        }
    }
}
