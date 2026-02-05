package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString
@Audited
@Table(indexes = {@Index(name = "idx_prm_po_id", columnList = "po_id") })
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
	    
	  
	    
	    private LocalDateTime yearWiseRateCartStartDate;
	    private LocalDateTime yearWiseRateCartEndDate;
	    private LocalDateTime lineItemStartDate;
	    private LocalDateTime lineItemEndDate;
	    
	    @Column(name = "active")
	    private boolean active;
	    
	    
	   

   

}