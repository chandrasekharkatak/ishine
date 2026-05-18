package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "migrated_doc_tracking")
public class MigratedDoc {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doc_id", unique = true)
    private Long docId;

    @Column(name = "timesheet_id")
    private Long timesheetId;

    @Column(name = "doc_name")
    private String docName;

    @Column(name = "project_id")
    private Integer projectId;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    public MigratedDoc() {}

    public MigratedDoc(Long docId, Long timesheetId, String docName, Integer projectId) {
        this.docId = docId;
        this.timesheetId = timesheetId;
        this.docName = docName;
        this.projectId = projectId;
        this.createdOn = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }
    public Long getTimesheetId() { return timesheetId; }
    public void setTimesheetId(Long timesheetId) { this.timesheetId = timesheetId; }
    public String getDocName() { return docName; }
    public void setDocName(String docName) { this.docName = docName; }
    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }
    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }
}
