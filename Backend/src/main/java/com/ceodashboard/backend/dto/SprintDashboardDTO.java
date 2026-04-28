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
public class SprintDashboardDTO {
    private SprintSummaryDTO summary;
    private List<TeamMemberDTO> team;
    private List<SprintListItemDTO> sprints;
}
