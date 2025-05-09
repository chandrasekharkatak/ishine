package com.apmosys.employeeportal.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Kresponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Float response;
    private Long kpiId;
    private Long empId;
    private Long quarterId;
    private String description;
//  private String review;
    
    private Boolean isFixed;

    private Long progress;
    
    @OneToMany(mappedBy = "kresponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KresponseRemark> remarks = new ArrayList<>();
    
    public void addRemark(KresponseRemark remark) {
        remarks.add(remark);
        remark.setKresponse(this);
    }
    
    public void removeRemark(KresponseRemark remark) {
        remarks.remove(remark);
        remark.setKresponse(null);
    }
}