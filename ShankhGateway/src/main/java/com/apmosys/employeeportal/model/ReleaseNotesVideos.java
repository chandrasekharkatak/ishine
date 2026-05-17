package com.apmosys.employeeportal.model;

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
@Table(name="ReleaseNotesVideos")
public class ReleaseNotesVideos {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer videoId;
	
	private Integer notificationId;
	
	private String name;
	
	
	
}


