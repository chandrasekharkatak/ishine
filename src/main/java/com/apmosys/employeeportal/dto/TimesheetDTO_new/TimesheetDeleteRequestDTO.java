package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for delete operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimesheetDeleteRequestDTO {
    private Long timesheetId;
    private Long projectId;
    private Long activityId; // Optional - only for activity deletion
}

