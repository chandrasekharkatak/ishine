package com.apmosys.employeeportal.model;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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
@Table(uniqueConstraints = @UniqueConstraint(name = "unique_po", columnNames = { "po_id" }))
public class ProjectPoDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "po_id")
    private Long poId;
	
	@Column(name = "project_id")
    private Integer projectId;
	
	@Column(name = "po_project_id")
    private Long poProjectId;
	
	@Column(name = "po_no")
    private String poNo;
	
	@Column(name = "po_start_date")
    private String poStartDate;
	
	@Column(name = "po_end_date")
    private String poEndDate;
   
	@Column(name = "prev_po")
    private Long prevPO;
	
	@Column(name = "next_po")
    private Long nextPO;
	
	@Column(name = "apmosys_rm")
    private String apmosysRM;
	
	@Column(name = "client_rm")
    private String clientRm;
	
	@Column(name = "apmosys_rm_email")
    private String apmosysRmEmail;
   
	@Column(name = "active")
    private boolean active;
   
	@Column(name = "msg")
    private String msg;
	
	@Column(name = "is_renewable")
	private boolean isRenewable;
	
	@Column(name = "client_location_id")
    private Long clientLocationId;
	
	@Column(name = "client_address_id")
	private Long clientAddressId;
	
	@Column(name = "created_by")
    private Long createdBy;
    
   
    @CreationTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "created_on",updatable = false)
    private Timestamp createdOn;
    
    private String poCreatedOn;
    
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    private String poUpdatedOn;
    
    
    
    @UpdateTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "updated_on")
    private Timestamp updatedOn;
    
 
    


}
