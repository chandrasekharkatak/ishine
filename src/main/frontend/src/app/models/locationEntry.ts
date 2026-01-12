import { ProjectEntry } from "./projectEntry";

export class LocationEntry {
  locationId: number | null;
  logInTime?: string;
  logOutTime?: string;
  totalWorkingHours: number | null;
  projects: ProjectEntry[];
}