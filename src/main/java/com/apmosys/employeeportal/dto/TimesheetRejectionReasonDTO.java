package com.apmosys.employeeportal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TimesheetRejectionReasonDTO {
    

    private String projectName;
    private String remarks;
    private String rejectionReason;

    public TimesheetRejectionReasonDTO(String projectName,String rejectionReason, String remarks
                                  ) {
        this.projectName = projectName;
        this.remarks = remarks;
        this.rejectionReason = rejectionReason;
    }

 
   
}
