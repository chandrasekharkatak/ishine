package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper DTO for complete timesheet request/response.
 * Matches the JSON contract structure:
 * {
 *   "employeeTimesheet": { ... },
 *   "filledDocument": { ... },
 *   "finalDocument": { ... }
 * }
 * 
 * @author System
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class employeeTimesheetMappingDTO_new {
    
    /**
     * Employee timesheet data with nested projectTimesheets
     */
    private EmployeeTimesheetDTO employeeTimesheet;
    
    /**
     * Filled document (timesheet document)
     */
    private TimesheetDocumentDTO_new filledDocument;
    
    /**
     * Final document (approved document)
     */
    private FinalDocumentDTO_new finalDocument;
}
