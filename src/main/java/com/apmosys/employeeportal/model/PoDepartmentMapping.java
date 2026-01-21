package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Entity
@Getter
@Setter
//@Audited
@ToString
public class PoDepartmentMapping {
	
	 @Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long poDepartmentMapId;
		private Integer poId;
		private Long deptId;
		private Boolean active;

}
