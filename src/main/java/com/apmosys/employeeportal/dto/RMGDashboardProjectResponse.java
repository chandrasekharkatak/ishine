package com.apmosys.employeeportal.dto;

import org.springframework.data.domain.Slice;

import lombok.Data;

@Data
public class RMGDashboardProjectResponse {

    private Slice<ProjectFetchDTO> projectList;
    private Long totalCount;
    private Long pageNumber;
    private Long pageSize;
    private Long totalElements;
    private Long totalPages;
    private boolean hasNext;

}
