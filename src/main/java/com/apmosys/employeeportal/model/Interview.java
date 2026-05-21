package com.apmosys.employeeportal.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "interview_tracker")
@Getter
@Setter
@ToString
@Audited
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String date;
    private String time;
    private String client;
    private String role;
    private String project;
    private Long departmentId;
    private Long employeeId;
    private String mode;
    private String interviewStatus;
    private String selectionStatus;
    private String onboardingStatus;
    private String interviewStatusChangeDate;
    private String selectionStatusChangeDate;
    private String onboardingStatusChangeDate;

    @Column(length = 500)
    private String interviewRemarks;

    @Column(length = 500)
    private String selectionRemarks;

    @Column(length = 500)
    private String onboardingRemarks;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String jd;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

    private String interviewerName;
    private Long scheduledById;

    private String resumeFileName;
    private String resumeFilePath;

    @Embedded
    public CommonProperties commonProperty = new CommonProperties();
}
