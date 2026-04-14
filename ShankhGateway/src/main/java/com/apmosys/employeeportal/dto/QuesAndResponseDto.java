package com.apmosys.employeeportal.dto;

import java.util.Map;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;
import com.apmosys.employeeportal.mongodb.modal.ProjectInsightResponseDetails;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class QuesAndResponseDto {
	private ProjectInsightQuestionDetails question;
	private ProjectInsightResponseDetails response;
	Map<Long, String> empMap;
}
