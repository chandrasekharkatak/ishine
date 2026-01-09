import { ProjectEntry } from "./projectEntry";

export class LocationEntry {
  locationId: number | null;
  logInTime?: string;
  logOutTime?: string;
  projects: ProjectEntry[];
}