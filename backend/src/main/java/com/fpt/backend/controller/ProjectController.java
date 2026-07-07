package com.fpt.backend.controller;

import com.fpt.backend.service.ProjectService;
import com.fpt.backend.service.ProjectService.ProjectListResponse;
import com.fpt.backend.util.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
public class ProjectController {
        private final ProjectService projectService;

        @GetMapping("/list")
        public ResponseEntity<BaseResponse<ProjectListResponse>> getProjects(
                        @RequestParam(defaultValue = "") String search,
                        @RequestParam(defaultValue = "") String status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "id") String sortBy,
                        @RequestParam(defaultValue = "desc") String sortDirection) {
                ProjectListResponse projects = projectService.getProjects(
                                search,
                                status,
                                page,
                                sortBy,
                                sortDirection);

                return ResponseEntity.ok()
                                .cacheControl(CacheControl.noStore())
                                .body(new BaseResponse<>(projects));
        }
}
