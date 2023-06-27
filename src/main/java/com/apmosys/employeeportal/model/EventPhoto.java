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
@Table(name="EventPhotos")
public class EventPhoto  {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long eventPhotoId;
	
	private String eventName;
	
	private String imageName;
	
	private String eventCaption;
	
	private Integer photoOrder;
	
	private String isExternalLink;
	
	private String externalLink;
	
	@Embedded
	public CommonProperties commonProperty = new CommonProperties();

}
