package com.apmosys.employeeportal.dto.TimesheetDTO_new;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalDocumentDTO_new {


    private Long finalDocId;

    private String fileUrl;
    private String docName;

    private Integer mimeTypeId;

    private Long createdBy;
    private LocalDateTime createdOn;

    private Long updatedBy;
    private LocalDateTime updatedOn;


}
