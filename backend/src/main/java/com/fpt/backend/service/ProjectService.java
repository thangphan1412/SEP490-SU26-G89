package com.fpt.backend.service;

import com.fpt.backend.entity.Projects;
import com.fpt.backend.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectService {
    private static final int PAGE_SIZE = 7;

    private static final Set<String> SORT_FIELDS = Set.of(
            "id",
            "projectCode",
            "projectName",
            "projectStatus",
            "projectStartDate",
            "projectEndDate",
            "projectCreatedBy",
            "projectCreatedAt"
    );

    private final ProjectRepository projectRepository;

    public ProjectListResponse getProjects(
            String search,
            String status,
            int page,
            String sortBy,
            String sortDirection
    ) {
        Pageable pageable = createPageable(page, sortBy, sortDirection);

        Page<ProjectListItem> result = projectRepository
                .findProjectList(clean(search), clean(status), pageable)
                .map(this::toListItem);

        return new ProjectListResponse(
                "DATABASE",
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast(),
                projectRepository.findDistinctStatuses()
        );
    }

    private Pageable createPageable(int page, String sortBy, String sortDirection) {
        int validPage = Math.max(page, 0);
        String sortField = sortBy != null && SORT_FIELDS.contains(sortBy) ? sortBy : "id";
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(validPage, PAGE_SIZE, Sort.by(direction, sortField));
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private ProjectListItem toListItem(Projects project) {
        return new ProjectListItem(
                project.getId(),
                project.getProjectCode(),
                project.getProjectName(),
                project.getProjectDescription(),
                project.getProjectStatus(),
                project.getProjectStartDate(),
                project.getProjectEndDate(),
                project.getProjectCreatedBy(),
                project.getProjectCreatedAt()
        );
    }

    public record ProjectListItem(
            int id,
            String projectCode,
            String projectName,
            String projectDescription,
            String projectStatus,
            LocalDate projectStartDate,
            LocalDate projectEndDate,
            String projectCreatedBy,
            String projectCreatedAt
    ) {
    }

    public record ProjectListResponse(
            String source,
            List<ProjectListItem> items,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            List<String> availableStatuses
    ) {
    }
}
