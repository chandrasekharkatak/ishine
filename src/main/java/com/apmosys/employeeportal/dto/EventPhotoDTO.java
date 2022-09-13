package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
public class EventPhotoDTO {

	private Long eventPhotoId;
	private String eventName;
	private String imageName;
	private byte[] imageBytes;
	
	private Long updatedBy;
	private Long createdBy;
	private String createdByName;
	private String createdOn;
	@Override
	public String toString() {
		return "EventPhotoDTO [eventPhotoId=" + eventPhotoId + ", eventName=" + eventName + ", imageName=" + imageName
				+ ", updatedBy=" + updatedBy + ", createdBy=" + createdBy + ", createdByName=" + createdByName
				+ ", createdOn=" + createdOn + "]";
	}
	
	
}
