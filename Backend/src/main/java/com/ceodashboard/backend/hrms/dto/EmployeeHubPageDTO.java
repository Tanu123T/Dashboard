package com.ceodashboard.backend.hrms.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EmployeeHubPageDTO {
    private List<EmployeeHubItemDTO> content;
    private long totalElements;
    private int  totalPages;
    private int  page;
    private int  size;
}
