package com.apmosys.employeeportal.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class LockStatusDTO {
	
	private Boolean isLocked;
	private Integer lockedTrainingId;
	private String lockedTrainingName;
	private String lockReason;
	private Boolean canSkip;
	private Boolean deadlineCrossed;
	private Integer currentCycleNumber;
}
