package com.ceodashboard.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberWorkDTO {
    private String name;
    private String role;
    private Integer totalTasks;
    private Integer completedTasks;
    private Integer inProgressTasks;
    private Integer todoTasks;
    private Double donePercentage;
    private Double inProgressPercentage;
    private Double todoPercentage;
    private Double estimatedHours;
    private Double actualHours;
}
