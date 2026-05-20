package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.ToString;

@Data
@Entity
@ToString
@Audited
@Table(
	    name = "project_hierarchy_mapping",
	    uniqueConstraints = {
	        @UniqueConstraint(columnNames = {"parent_project_id", "child_project_id"})
	    }
	)
public class ProjectHierarchyMapping {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@Column(name = "parent_project_id")
	private Integer parentProjectId;
	
	@Column(name = "child_project_id")
	private Integer childProjectId;
	
	@Column(name = "active")
	private boolean active;
	
	@Column(name = "created_by")
    private Long createdBy;
    
	@CreationTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;
    
    @PrePersist
    public void onCreate() {
        if (this.updatedBy != null) {
            this.updatedOn = LocalDateTime.now();
        } else {
            this.updatedOn = null;
        }
    }

    @PreUpdate
    public void onUpdate() {
        if (this.updatedBy != null) {
            this.updatedOn = LocalDateTime.now();
        } else {
            this.updatedOn = null;
        }
    }
}
