package com.apmosys.employeeportal.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class KresponseRemark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String remark;
    private String createdBy;
    private java.util.Date createdDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kresponse_id", nullable = false)
    private Kresponse kresponse;
    
    private String employeeRole;
    
}