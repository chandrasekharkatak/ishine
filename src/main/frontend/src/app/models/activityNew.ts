export class ActivityNew {
  clientId: number | null;
  clientLocationId: number | null;
  teamId: number | null;
  activityId: number | null;
  description: string;
  completionTime: number | null;
  
  // Additional fields for dropdowns
  clientLocationList?: any[];
  projectList?: any[];
  projectActivities?: any[];
}