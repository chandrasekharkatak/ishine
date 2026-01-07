import { ProjectEntry } from "./projectEntry";

export class LocationEntry {
  locationId: number | null;
  locationName?: string;
  clientLocationId?: number | null;
  logInTime?: string;
  logOutTime?: string;
  totalClientWorkingHours?: string;
  projects: ProjectEntry[];
  availableProjects?: any[]; // Projects available for this location
}