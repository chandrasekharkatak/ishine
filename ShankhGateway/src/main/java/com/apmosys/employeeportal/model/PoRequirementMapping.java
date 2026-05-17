package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString
@Audited
//@Table(indexes = {@Index(name = "idx_prm_po_id", columnList = "po_id") })
public class PoRequirementMapping {
	
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long poRequirementMappingId;
	    
	    @Column(name = "po_id")
	    private Long poId;
	    
	    @Column(name = "client_role_id")
	    private Long clientRoleId;
	    
	    @Column(name = "role_id")
	    private Long roleId;
	    
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
	    
	    @Column(name = "created_by")
	    private Long createdBy;
	    
	    @CreationTimestamp
	    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	    @Column(name = "created_on",updatable = false)
	    private Timestamp createdOn;
	    
	    @Column(name = "updated_by")
	    private Long updatedBy;
	    
	    @UpdateTimestamp
	    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
	    @Column(name = "updated_on")
	    private Timestamp updatedOn;
	   

   

}