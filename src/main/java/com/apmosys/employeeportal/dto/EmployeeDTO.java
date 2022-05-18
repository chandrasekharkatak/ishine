package com.apmosys.employeeportal.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class EmployeeDTO {
	
	private Long empId;
	private Long draftEmpId;
	private String name;
	private int managerId;
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
	private short noticePeriod;
	private boolean isDraft = false;
	private Long jobRoleId;
	private String bankName;
	private String bankAccountNo;
	private String bankIFSCCode;
	private String pfAccountNumber;
	private String previousPfAccountNumber;
	private String uan;
	private String esicNumber;
	private String graduation;
	private short yearOfGrad;
	private String postGraduation;
	private String hobbies;	
	private String aboutMe;
	private String viewsOnOrganisation;	
	private short yearOfPostGrad;
	
	
	public Long getEmpId() {
		return empId;
	}

	public void setEmpId(Long empId) {
		this.empId = empId;
	}	
	public Long getDraftEmpId() {
		return draftEmpId;
	}

	public void setDraftEmpId(Long draftEmpId) {
		this.draftEmpId = draftEmpId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getManagerId() {
		return managerId;
	}

	public void setManagerId(int managerId) {
		this.managerId = managerId;
	}

	public String getDateOfJoining() {
		return dateOfJoining;
	}

	public void setDateOfJoining(String dateOfJoining) {
		this.dateOfJoining = dateOfJoining;
	}

	
	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getBloodGroup() {
		return bloodGroup;
	}

	public void setBloodGroup(String bloodGroup) {
		this.bloodGroup = bloodGroup;
	}

	public String getMaritalStatus() {
		return maritalStatus;
	}

	public void setMaritalStatus(String maritalStatus) {
		this.maritalStatus = maritalStatus;
	}	
	public String getFatherName() {
		return fatherName;
	}

	public void setFatherName(String fatherName) {
		this.fatherName = fatherName;
	}

	public String getPlaceOfBirth() {
		return placeOfBirth;
	}

	public void setPlaceOfBirth(String placeOfBirth) {
		this.placeOfBirth = placeOfBirth;
	}

	public String getMotherTongue() {
		return motherTongue;
	}

	public void setMotherTongue(String motherTongue) {
		this.motherTongue = motherTongue;
	}

	public String getPassportNumber() {
		return passportNumber;
	}

	public void setPassportNumber(String passportNumber) {
		this.passportNumber = passportNumber;
	}

	public Long getAadhar() {
		return aadhar;
	}

	public void setAadhar(Long aadhar) {
		this.aadhar = aadhar;
	}

	public String getPanNumber() {
		return panNumber;
	}

	public void setPanNumber(String panNumber) {
		this.panNumber = panNumber;
	}

	public Long getMobileNo() {
		return mobileNo;
	}

	public void setMobileNo(Long mobileNo) {
		this.mobileNo = mobileNo;
	}

	public Long getLandline() {
		return landline;
	}

	public void setLandline(Long landline) {
		this.landline = landline;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Integer getPincode() {
		return pincode;
	}

	public void setPincode(Integer pincode) {
		this.pincode = pincode;
	}

	public Long getOfficialMobileNo() {
		return officialMobileNo;
	}

	public void setOfficialMobileNo(Long officialMobileNo) {
		this.officialMobileNo = officialMobileNo;
	}

	public String getPermanentAddress() {
		return permanentAddress;
	}

	public void setPermanentAddress(String permanentAddress) {
		this.permanentAddress = permanentAddress;
	}

	public String getEmergencyContactPerson() {
		return emergencyContactPerson;
	}

	public void setEmergencyContactPerson(String emergencyContactPerson) {
		this.emergencyContactPerson = emergencyContactPerson;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public Long getEmergencyContactMobile() {
		return emergencyContactMobile;
	}

	public void setEmergencyContactMobile(Long emergencyContactMobile) {
		this.emergencyContactMobile = emergencyContactMobile;
	}
	
	public String getEmploymentstatus() {
		return employmentstatus;
	}

	public void setEmploymentstatus(String employmentstatus) {
		this.employmentstatus = employmentstatus;
	}

	public short getNoticePeriod() {
		return noticePeriod;
	}

	public void setNoticePeriod(short noticePeriod) {
		this.noticePeriod = noticePeriod;
	}

	public boolean getDraft() {
		return isDraft;
	}

	public void setDraft(boolean isDraft) {
		this.isDraft = isDraft;
	}
	public Long getJobRoleId() {
		return jobRoleId;
	}

	public void setJobRoleId(Long jobRoleId) {
		this.jobRoleId = jobRoleId;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public String getBankAccountNo() {
		return bankAccountNo;
	}

	public void setBankAccountNo(String bankAccountNo) {
		this.bankAccountNo = bankAccountNo;
	}

	public String getBankIFSCCode() {
		return bankIFSCCode;
	}

	public void setBankIFSCCode(String bankIFSCCode) {
		this.bankIFSCCode = bankIFSCCode;
	}

	public String getPfAccountNumber() {
		return pfAccountNumber;
	}

	public void setPfAccountNumber(String pfAccountNumber) {
		this.pfAccountNumber = pfAccountNumber;
	}

	public String getPreviousPfAccountNumber() {
		return previousPfAccountNumber;
	}

	public void setPreviousPfAccountNumber(String previousPfAccountNumber) {
		this.previousPfAccountNumber = previousPfAccountNumber;
	}	
	public String getUan() {
		return uan;
	}

	public void setUan(String uan) {
		this.uan = uan;
	}

	public String getEsicNumber() {
		return esicNumber;
	}

	public void setEsicNumber(String esicNumber) {
		this.esicNumber = esicNumber;
	}

	public String getGraduation() {
		return graduation;
	}

	public void setGraduation(String graduation) {
		this.graduation = graduation;
	}

	public short getYearOfGrad() {
		return yearOfGrad;
	}

	public void setYearOfGrad(short yearOfGrad) {
		this.yearOfGrad = yearOfGrad;
	}

	public String getPostGraduation() {
		return postGraduation;
	}

	public void setPostGraduation(String postGraduation) {
		this.postGraduation = postGraduation;
	}

	public String getHobbies() {
		return hobbies;
	}

	public void setHobbies(String hobbies) {
		this.hobbies = hobbies;
	}

	public String getAboutMe() {
		return aboutMe;
	}

	public void setAboutMe(String aboutMe) {
		this.aboutMe = aboutMe;
	}

	public String getViewsOnOrganisation() {
		return viewsOnOrganisation;
	}

	public void setViewsOnOrganisation(String viewsOnOrganisation) {
		this.viewsOnOrganisation = viewsOnOrganisation;
	}

	public short getYearOfPostGrad() {
		return yearOfPostGrad;
	}

	public void setYearOfPostGrad(short yearOfPostGrad) {
		this.yearOfPostGrad = yearOfPostGrad;
	}

	public boolean isDraft() {
		return isDraft;
	}

	

	
	
	

	
	

}
