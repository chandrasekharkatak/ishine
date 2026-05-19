import { ProjectEntry } from "./projectEntry";

export class LocationEntry {
  locationMappingId: number | null;
  workLocationType: string | null;
  workLocationTypeId: number | null;
  locationInTime?: string | null;
  locationOutTime?: string | null;
  projects: ProjectEntry[];
  totalWorkingHours?: number | null;
}