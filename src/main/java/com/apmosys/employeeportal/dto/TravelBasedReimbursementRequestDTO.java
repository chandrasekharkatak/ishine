package com.apmosys.employeeportal.dto;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TravelBasedReimbursementRequestDTO {
	
	private Integer serialNo;

    private Integer travelId;

    private String invoiceNo;

    private Timestamp invoiceDate;

    private Double amount;
    
    private String uploadedBy;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = true, updatable = true)
    private Timestamp uploadedOn;

    private String updatedBy;

    private Timestamp updatedOn;
    
//    private  List<MultipartFile> fileName;

}
