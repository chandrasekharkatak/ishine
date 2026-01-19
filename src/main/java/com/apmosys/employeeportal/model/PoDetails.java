package com.apmosys.employeeportal.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class PoDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long poId;
    private Integer projectId;
    private String poNo;
    private Long lastModifiedBy;
    private LocalDateTime lastModifiedOn;
    private String apmosysRM;
    private String clientRm;
    private String apmosysRmEmail;
    private String poStartDate;
    private String poEndDate;
    private boolean activeFlag;
    private String prevPO;
    private String nextPO;
    private String msg;
    // private Long clientLocationId;

}
