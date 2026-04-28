package com.ceodashboard.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectsPageResponseDTO {
    private ProjectSummaryDTO summary;
    private List<ProjectDTO> projects;
}
