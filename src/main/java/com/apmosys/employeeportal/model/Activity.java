package com.apmosys.employeeportal.model;



import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name="Activities")
public class Activity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long activityId;
	
	private Long teamId;
	
	private String activity;
	
	private Float eta;
	
	@Embedded
	public CommonProperties commonProperty = new CommonProperties();
	
	

}
