package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
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
	    
	    @Column(name = "po_id")
	    private Long poId;
	    
	    @Column(name = "client_role_id")
	    private Long clientRoleId;
	    
	    @Column(name = "role")
	    private String role;
	    
	    @Column(name = "experience")
	    private String experience;
	    
	    @Column(name = "department")
	    private String department;
	    
	    @Column(name = "count")
	    private Long count;
	    
	    @Column(name = "active")
	    private Boolean active;
	    
	    
	   


}
