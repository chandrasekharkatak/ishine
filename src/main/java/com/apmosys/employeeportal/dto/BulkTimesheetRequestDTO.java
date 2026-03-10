package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Map;

import com.apmosys.employeeportal.dto.TimesheetDTO_new.TimesheetDocumentDataDTO;

import lombok.Data;

@Data
public class BulkTimesheetRequestDTO {
//	private Map<Long, List<Long>> timesheetProjectMap;
	private List<Long> timesheetIds; // optional for approve
    private String status;           // APPROVED / REJECTED
    private Long updatedBy;
    private Long rmId;
    private String rejectMode; 
    private Long rejectionReasonId;
    private String rejectRemark;
    private List<ProjectRejectionDTO> projectRejections;
    private boolean confirmNightShift;
    private List<TimesheetDocumentDataDTO> documentDetails;
}
