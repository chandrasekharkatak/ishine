package com.apmosys.employeeportal.model;

import java.sql.Timestamp;

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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Data
@Entity
@Getter
@Setter
@Audited
@ToString
@Table(indexes = { @Index(name = "idx_podeptm_po_id", columnList = "poId"),
        @Index(name = "idx_podeptm_dept_id", columnList = "deptId"),
        @Index(name = "idx_podeptm_project_id", columnList = "projectId")
})
public class PoDepartmentMapping {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long poDepartmentMapId;
	private Long poId;
	private Long deptId;
	private boolean active;
	private Integer projectId;
	
    private Long createdBy;
    @CreationTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "created_on",updatable = false)
    private Timestamp createdOn;
    
    private Long updatedBy;
    @UpdateTimestamp
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    @Column(name = "updated_on")
    private Timestamp updatedOn;

}
