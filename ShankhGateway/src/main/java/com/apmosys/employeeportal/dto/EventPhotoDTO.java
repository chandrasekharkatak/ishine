package com.apmosys.employeeportal.dto;

import java.util.Arrays;
import java.util.List;

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
	private String eventCaption;
	private Integer photoOrder;
	private String isExternalLink;
	private String externalLink;
	
	private Long updatedBy;
	private Long createdBy;
	private String createdByName;
	private String createdOn;
	
	private List<EventPhotoDTO> eventPhotoList;
	
	@Override
	public String toString() {
		return "EventPhotoDTO [eventPhotoId=" + eventPhotoId + ", eventName=" + eventName + ", imageName=" + imageName
				+ ", imageBytes=" + Arrays.toString(imageBytes) + ", eventCaption=" + eventCaption + ", photoOrder="
				+ photoOrder + ", updatedBy=" + updatedBy + ", createdBy=" + createdBy + ", createdByName="
				+ createdByName + ", createdOn=" + createdOn + "]";
	}
	
	
	
}
