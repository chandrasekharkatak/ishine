import { ActivityNew } from "./activityNew";

export class ProjectEntry {
  projectId: number | null;
  projectName?: string;
  hasClientSideId?: boolean;
  hasClientFlag?: boolean;
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
  timesheetId?: number;
  poNo?: string;
  poId?: number;
  status?: 1 | 2 | 3; // Values: 1=Pending, 2=Approved, 3=Rejected
  locationMappingId?: number;
  totalClientWorkingMinutes?: number;
  projectHoursMinutes?: number;
  description?: string | null;
  
  // Client Details (fetched when project is selected via getClientDetailsByProjectIdAndEmpId API)
  clientDetails?: {
    clientId: number;
    clientName: string;
    clientLocations: Array<{
      clientLocationId: number;
      clientLocation: string;
    }>;
    project: {
      projectId: number;
      projectName: string;
      teams: Array<{
        teamId: number;
        teamName: string;
      }>;
    };
  };
}