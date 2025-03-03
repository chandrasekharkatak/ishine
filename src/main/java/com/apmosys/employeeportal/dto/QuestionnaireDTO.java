package com.apmosys.employeeportal.dto;

import lombok.*;
import javax.persistence.*;


@Getter
@Setter
public class QuestionnaireDTO {

	private Long goalId;
	private Long questionId;
    private String questionTitle;
    private String questiondescription;
    private String createdBy;
    private Long quarterId;
	
}
