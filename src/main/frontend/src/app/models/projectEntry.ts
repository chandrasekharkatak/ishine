import { ActivityNew } from "./activityNew";

export class ProjectEntry {
  projectId: number | null;
  projectName?: string;
  // clientSideId?: string;
  hasClientSideId?: boolean;
  
  // Office times
  inTime: string;
  outTime: string;
  officeInTime?: string;
  officeOutTime?: string;
  selectedInHour?: string;
  selectedInMinute?: string;
  selectedInPeriod?: string;
  selectedOutHour?: string;
  selectedOutMinute?: string;
  selectedOutPeriod?: string;
  
  // Client times
  clientInTime?: string;
  clientOutTime?: string;
  selectedClientInHour?: string;
  selectedClientInMinute?: string;
  selectedClientInPeriod?: string;
  selectedClientOutHour?: string;
  selectedClientOutMinute?: string;
  selectedClientOutPeriod?: string;
  
  totalWorkingHours: string;
  totalClientWorkingHours?: string;
  
  // Shadow settings (per-project)
  shadowEmpId?: number;
  isShadowTimesheet?: boolean;
  isShadowForSelf?: boolean;
  
  // Client, Team, Location (at project level)
  clientSideId?: string | null;
  clientId?: number | null;
  clientLocationId?: number | null;
  teamId?: number | null;
  clientApprovalStatus: string | null;
  
  // Activities
  activities: ActivityNew[];
  availableActivities?: ActivityNew[]; // Loaded activities for dropdown
  projectActivities?: any[]; // Activities loaded for the project (shared by all activities)
  clientList?: any[]; // Clients for this project
  clientLocationList?: any[]; // Client locations for this project
  projectList?: any[]; // Teams for this project
}