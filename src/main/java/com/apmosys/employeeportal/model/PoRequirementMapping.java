package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.envers.Audited;

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
public class PoRequirementMapping {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long poRequirementMappingId;
	    private Long poId;
	    private Long clientRoleId;
	    private String role;
	    private String experience;
	    private String department;
	    private Boolean active;
	    private Long count;


}
