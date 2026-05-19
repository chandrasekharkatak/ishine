package com.apmosys.employeeportal.dto;

import java.util.Collections;
import java.util.List;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.GetReporteesTimesheetReqDTO;

import lombok.Builder;
import lombok.Getter;

/**
 * Type-safe response DTO for paginated timesheet requests.
 * Replaces the generic HashMap response with strongly-typed fields,
 * providing compile-time safety and better IDE support.
 */
@Getter
@Builder
public class PaginatedTimesheetResponse {

    private final List<GetReporteesTimesheetReqDTO> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;

    /**
     * Creates an empty paginated response.
     *
     * @param page the requested page number
     * @param size the requested page size
     * @return an empty PaginatedTimesheetResponse
     */
    public static PaginatedTimesheetResponse empty(int page, int size) {
        return PaginatedTimesheetResponse.builder()
                .content(Collections.emptyList())
                .page(page)
                .size(size)
                .totalElements(0)
                .totalPages(0)
                .hasNext(false)
                .build();
    }

    /**
     * Creates a paginated response with content.
     *
     * @param content the list of timesheet DTOs
     * @param page the current page number
     * @param size the page size
     * @param totalElements the total number of elements
     * @param totalPages the total number of pages
     * @param hasNext whether there is a next page
     * @return a new PaginatedTimesheetResponse
     */
    public static PaginatedTimesheetResponse of(
            List<GetReporteesTimesheetReqDTO> content,
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext) {
        return PaginatedTimesheetResponse.builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(hasNext)
                .build();
    }
}
