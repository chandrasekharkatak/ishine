package com.apmosys.employeeportal.dto;

import java.util.List;
import lombok.*;

@Getter
@Setter
public class BulkQuestionnaireAssignRequestDTO {
	 private List<Long> empIds;
	    private Long questionId;
//	    private String expectedCompletionDate;
	    private Long quarterId;
}
