package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
@Table(uniqueConstraints = @UniqueConstraint(name = "unique_po",columnNames = {"po_id"}))
public class ProjectPoDetails {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column(name = "po_id")
    private Long poId;
    private Integer projectId;
    private String poNo;
    private Long updatedBy;
    
    @UpdateTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column
    private Timestamp updatedOn;
    
    private String apmosysRM;
    private String clientRm;
    private String apmosysRmEmail;
    private String poStartDate;
    private String poEndDate;
    private Boolean active;
    private Long prevPO;
    private Long nextPO;
    private String msg;
    private Long clientLocationId;
    private Long createdBy;
    
    
    @CreationTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(updatable = false)
    private Timestamp createdOn;
    


}
