package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@ToString
public class QueryRequestDTO {
    private String customQuery;
    private List<QueryFilterDTO> customQueryFilters;
    private String selectedColumns;

    // Server-side paging/sorting/search (optional)
    /** 1-based page number (UI-friendly). */
    private Integer page;
    /** Page size. */
    private Integer size;
    /** Column key (must match ^[a-zA-Z0-9_]+$). */
    private String sortColumn;
    /** asc|desc */
    private String sortDirection;
    /** Free-text search across selected columns (optional). */
    private String searchText;

    /** Per-column search (server-side column filter bar). Key must match column name. */
    private Map<String, String> columnSearch;
}
