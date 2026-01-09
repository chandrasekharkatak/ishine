import { ActivityNew } from "./activityNew";

export class ProjectEntry {
  projectId: number | null;
  projectName?: string;
  hasClientSideId?: boolean;
  shadowEmpId?: number;
  isShadowTimesheet?: boolean;
  isShadowForSelf?: boolean;
  clientSideId?: string | null;
  clientId?: number | null;
  clientLocationId?: number | null;
  clientApprovalStatus: number | null;
  totalWorkingHours?: number | null;
  shadowForList:any[];
  activities: ActivityNew[];
  projectActivities?: any[]; // Activities loaded for the project (shared by all activities)
  clientList?: any[]; // Clients for this project
  clientLocationList?: any[]; // Client locations for this project
  projectList?: any[]; // Teams for this project
}