export class ActivityNew {
  clientTeamList?: any[]; // Teams for this project
  teamId?: number | null;
  activityId: number | null;
  description: string;
  durationMinutes: number | null;
  allActivitiesForProject?: any[];
  // projectActivities?: any[];
  timesheetId?: number;
  projectId?: number;
}