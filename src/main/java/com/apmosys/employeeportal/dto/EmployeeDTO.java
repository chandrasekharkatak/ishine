package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class EmployeeDTO {
	
	private Long empId;
	private Long draftEmpId;
	private String name;
	private Integer managerId;
	private String dateOfJoining;
	private String dateOfBirth;
	private String email;
	private String gender;
	private String bloodGroup;
	private String maritalStatus;
	private String fatherName;
	private String placeOfBirth;
	private String motherTongue;
	private String passportNumber;
	private Long aadhar;
	private String panNumber;
	private Long mobileNo;
	private Long landline;
	private String address;
	private String city;
	private String state;
	private String country;
	private Integer pincode;
	private Long officialMobileNo;
	private String permanentAddress;
	private String emergencyContactPerson;
	private String relation;
	private Long emergencyContactMobile;
	private String employmentstatus;
	private Short noticePeriod;
	private Long jobRoleId;
	private String bankName;
	private String bankAccountNo;
	private String bankIFSCCode;
	private String pfAccountNumber;
	private String previousPfAccountNumber;
	private String uan;
	private String esicNumber;
	private String graduation;
	private Short yearOfGrad;
	private String postGraduation;
	private String hobbies;	
	private String aboutMe;
	private String viewsOnOrganisation;	
	private Short yearOfPostGrad;
	private String password;
	private Integer otp;
	private String newPassword;
	private byte[] imageBytes;
	private Long userTypeId;
	private String managerName;
	private String jobRoleName;
	private Long departmentId;
	private String departmentName;
	private Integer createdBy;
	private String createdOn;
	
	
	

}
