package com.apmosys.employeeportal.dto;

import java.util.Arrays;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadPolicyDTO {
	
	private Long policyID;
	
	private String policyName;
	
	private String fileName;
	
	private byte[] fileBytes;
	
	private Long updatedBy;
	private Long createdBy;
	private String createdByName;
	private String createdOn;
	private String readEnabled;
	private Long empId;
	private String name;

	
	@Override
	public String toString() {
		return "UploadPolicyDTO [policyID=" + policyID + ", policyName=" + policyName + ", fileName=" + fileName
				+ ", fileBytes=" + Arrays.toString(fileBytes) + ", updatedBy=" + updatedBy + ", createdBy=" + createdBy
				+ ", createdByName=" + createdByName + ", createdOn=" + createdOn + "]";
	}
	

}
