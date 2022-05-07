package com.apmosys.employeeportal.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
public class Employee {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long emp_id;

	private String name;

	
	private LocalDate dateOfBirth;
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

	private LocalDate dateOfJoining;
	private String employmentstatus;
	private short noticePeriod;


	@Column(columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP" , insertable = false ,updatable = false)
	private Timestamp createdOn;
	
	//@Column(columnDefinition="TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP")
	private Timestamp updatedOn;
	
	private int createdBy;	
	private int updatedBy;	
	private int managerId;
	
	public Long getEmp_id() {
		return emp_id;
	}
	public void setEmp_id(Long emp_id) {
		this.emp_id = emp_id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(LocalDate dateOfBirth) {
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
	public Timestamp getCreatedOn() {
		return createdOn;
	}
	public void setCreatedOn(Timestamp createdOn) {
		this.createdOn = createdOn;
	}
	public int getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}
	public Timestamp getUpdatedOn() {
		return updatedOn;
	}
	public void setUpdatedOn(Timestamp updatedOn) {
		this.updatedOn = updatedOn;
	}
	public int getUpdatedBy() {
		return updatedBy;
	}
	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}
	public int getManagerId() {
		return managerId;
	}
	public void setManagerId(int managerId) {
		this.managerId = managerId;
	}
	public LocalDate getDateOfJoining() {
		return dateOfJoining;
	}
	public void setDateOfJoining(LocalDate dateOfJoining) {
		this.dateOfJoining = dateOfJoining;
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
	@Override
	public String toString() {
		return "Employee [emp_id=" + emp_id + ", name=" + name + ", dateOfBirth=" + dateOfBirth + ", email=" + email
				+ ", gender=" + gender + ", bloodGroup=" + bloodGroup + ", maritalStatus=" + maritalStatus
				+ ", fatherName=" + fatherName + ", placeOfBirth=" + placeOfBirth + ", motherTongue=" + motherTongue
				+ ", passportNumber=" + passportNumber + ", aadhar=" + aadhar + ", panNumber=" + panNumber
				+ ", mobileNo=" + mobileNo + ", landline=" + landline + ", address=" + address + ", city=" + city
				+ ", state=" + state + ", country=" + country + ", pincode=" + pincode + ", officialMobileNo="
				+ officialMobileNo + ", permanentAddress=" + permanentAddress + ", emergencyContactPerson="
				+ emergencyContactPerson + ", relation=" + relation + ", emergencyContactMobile="
				+ emergencyContactMobile + ", dateOfJoining=" + dateOfJoining + ", employmentstatus=" + employmentstatus
				+ ", noticePeriod=" + noticePeriod + ", createdOn=" + createdOn + ", updatedOn=" + updatedOn
				+ ", createdBy=" + createdBy + ", updatedBy=" + updatedBy + ", managerId=" + managerId + "]";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

	
	
}
