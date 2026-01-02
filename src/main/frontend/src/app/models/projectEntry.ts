import { ActivityNew } from "./activityNew";

export class ProjectEntry {
  projectId: number | null;
  inTime: string;
  outTime: string;
  totalWorkingHours: string;
  activities: ActivityNew[];
}