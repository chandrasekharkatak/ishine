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
	
	// Additional fields for routing decisions
	private Boolean hasMandatoryTrainingPending; // True if mandatory training exists (even if lock not enabled)
	private Boolean isHardLock; // True if lock enabled AND deadline crossed (blocks all navigation)
}
