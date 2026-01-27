package com.apmosys.employeeportal.model;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
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
	    
	  
	    
	    private String yearWiseRateCartStartDate;
	    private String yearWiseRateCartEndDate;
	    private String lineItemStartDate;
	    private String lineItemEndDate;
	    
	    @Column(name = "active")
	    private boolean active;
	    
	    
	   

   

}