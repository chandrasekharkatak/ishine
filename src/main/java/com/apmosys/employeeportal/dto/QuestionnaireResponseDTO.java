package com.apmosys.employeeportal.dto;

import lombok.*;
import javax.persistence.*;

@Getter
@Setter
public class QuestionnaireResponseDTO {

	
	private Long empId;
	
	private Long questionId;

	private String questionTitle;
	
	private Long responseId;
	
	private Integer quarter;
	
	private String response;
	
	private String remarks;
	
	private Float score;
}
