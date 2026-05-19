package com.apmosys.employeeportal.dto;

import java.util.List;
import java.util.Map;

import com.apmosys.employeeportal.mongodb.modal.ProjectInsightQuestionDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionMappedStatusDto {
	private List<ProjectInsightQuestionDetails> questions;
	private Map<String,Integer> statusMap;
	private Map<String,Integer> historyMap;
}