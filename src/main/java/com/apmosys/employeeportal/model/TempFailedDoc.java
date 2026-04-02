package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


 
@Entity
@Table(name = "temp_failed_doc")
public class TempFailedDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "doc_id")
    private Long docId;

    @Column(name = "doc_name")
    private String docName;

    @Column(name = "timesheet_id")
    private Long timesheetId;

    @Column(name = "type")
    private String type;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    // constructors
    public TempFailedDoc() {}

    public TempFailedDoc(Long docId, String docName, Long timesheetId, String type, String reason) {
        this.docId = docId;
        this.docName = docName;
        this.timesheetId = timesheetId;
        this.type = type;
        this.reason = reason;
        this.createdOn = LocalDateTime.now();
    }

    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getDocId() { return docId; }
    public void setDocId(Long docId) { this.docId = docId; }
    public String getDocName() { return docName; }
    public void setDocName(String docName) { this.docName = docName; }
    public Long getTimesheetId() { return timesheetId; }
    public void setTimesheetId(Long timesheetId) { this.timesheetId = timesheetId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public LocalDateTime getCreatedOn() { return createdOn; }
    public void setCreatedOn(LocalDateTime createdOn) { this.createdOn = createdOn; }
}

