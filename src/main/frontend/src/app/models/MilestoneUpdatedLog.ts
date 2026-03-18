export interface MilestoneUpdatedLog {
  milestoneId?: number;
  poId?: number;
  projectId?: number;

  milestoneName?: string;
  milestoneStartDate?: Date;  // ISO format or 'yyyy-MM-dd'
  milestoneEndDate?: Date;

  description?: string;
  remarks?: string;
  milestoneStatus?: string;

  lineItemId?: number;
  lineItemName?: string;
  projectName?: string;
  poNumber?: string;

  extendedDate?: Date;  // Can also use string if needed
  extendedStartDate?: Date;

  updatedBy?: number;
  updatedByName?:string

  milestoneExtensionReasonId?: number;
  milestoneExtensionReasonText?: string;
}
