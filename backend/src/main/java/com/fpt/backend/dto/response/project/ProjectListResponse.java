package com.fpt.backend.dto.response.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectListResponse {
    private List<ProjectListItemResponse> items;
    private long totalElements;
    private int totalPages;
}
