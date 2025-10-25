package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;
import javax.persistence.Id;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "Employee_Excluded_fromLeave")
public class EmployeeexcludedFromLeave {
	
	@Id
	private Long empId;
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime createdOn;
	@JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	private LocalDateTime updatedOn;
	private Boolean isExcluded;
	private Integer createdBy;

	

}
