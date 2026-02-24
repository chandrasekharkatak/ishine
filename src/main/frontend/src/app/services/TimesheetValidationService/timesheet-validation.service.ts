import { Injectable } from '@angular/core';
import { LocationEntry } from 'src/app/models/locationEntry';
import { ProjectEntry } from 'src/app/models/projectEntry';
import { ActivityNew } from 'src/app/models/activityNew';
import { User } from 'src/app/models/user';
import { TimesheetDocumentDataI } from 'src/app/user-timesheet/my-timesheet/timesheet-form/types';
import { TimesheetConfigService } from './timesheet-config.service';

/**
 * Validation Error Interface
 * Represents a single validation error with context information
 */
export interface ValidationError {
  field: string;
  message: string;
  scope?: 'TIMESHEET' | 'LOCATION' | 'PROJECT' | 'ACTIVITY' | 'DOCUMENT';
  locationIndex?: number;
  projectIndex?: number;
  activityIndex?: number;
  locationId?: number; // For highlighting specific locations
}

/**
 * Validation Result Interface
 * Contains validation status, errors, and warnings
 */
export interface ValidationResult {
  isValid: boolean;
  errors: ValidationError[];
  warnings: string[];
  highlightedLocationIds?: number[]; // Location IDs that should be highlighted
}

/**
 * Timesheet Validation Context
 * Contains all data needed for comprehensive validation
 */
export interface TimesheetValidationContext {
  // Basic Information
  dayType: number | null;
  fromDate: string | null; // DD-MM-YYYY format
  toDate: string | null; // DD-MM-YYYY format
  isNightShift: boolean;
  apmosysInTime: string | null;
  apmosysOutTime: string | null;
  totalPresence: number;
  
  // Employee Information
  timesheetAppliedFor: string; // 'self' | 'team'
  timesheetFilledForUser: User | null;
  currentUser: User | null;
  
  // Timesheet Data
  timesheetLocations: LocationEntry[];
  
  // Supporting Data
  workLocationList: any[]; // For location code lookup
  documentData?: TimesheetDocumentDataI[];
  uniqueProjectsList?: ProjectEntry[];
  empHasClientSideId?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class TimesheetValidationService {
  constructor(private configService: TimesheetConfigService) {}

  /**
   * Main validation method for timesheet creation.
   * Validates in order (basic → locations → hours → documents). Fail-fast: returns immediately on first error.
   */
  validateCreate(context: TimesheetValidationContext): ValidationResult {
    // 1. Basic validations (top-level, in order); return on first error
    const basicResult = this.validateBasicFields(context);
    if (basicResult.errors.length > 0) {
      return { ...basicResult, warnings: this.deduplicateWarnings(basicResult.warnings) };
    }

    // 2. Location & project validations (location-level fields first, then projects); return on first error
    const locationResult = this.validateLocationsAndProjects(context);
    if (locationResult.errors.length > 0) {
      return {
        ...locationResult,
        warnings: this.deduplicateWarnings(locationResult.warnings),
        highlightedLocationIds: locationResult.highlightedLocationIds || []
      };
    }

    // 3. Hours validations; return on first error
    const hoursResult = this.validateHours(context);
    if (hoursResult.errors.length > 0) {
      return {
        ...hoursResult,
        warnings: this.deduplicateWarnings(hoursResult.warnings),
        highlightedLocationIds: hoursResult.highlightedLocationIds || []
      };
    }

    // 4. Document upload validations; return on first error
    const documentResult = this.validateDocumentUploads(context);
    if (documentResult.errors.length > 0) {
      return { ...documentResult, warnings: this.deduplicateWarnings(documentResult.warnings) };
    }

    const warnings = this.deduplicateWarnings([
      ...basicResult.warnings,
      ...locationResult.warnings,
      ...hoursResult.warnings,
      ...documentResult.warnings
    ]);
    const highlightedLocationIds = [
      ...(locationResult.highlightedLocationIds || []),
      ...(hoursResult.highlightedLocationIds || [])
    ];
    return {
      isValid: true,
      errors: [],
      warnings,
      highlightedLocationIds: [...new Set(highlightedLocationIds)]
    };
  }

  /**
   * Validates basic timesheet fields in order (top to bottom). Returns on first error (fail-fast).
   */
  private validateBasicFields(context: TimesheetValidationContext): ValidationResult {
    const errors: ValidationError[] = [];
    const warnings: string[] = [];
    const isDayTypeFillable = this.isDayTypeFillable(context.dayType);

    // 1. Employee selection
    if (!context.timesheetFilledForUser?.empId) {
      errors.push({ field: 'employee', message: 'Employee selection is required', scope: 'TIMESHEET' });
      return { isValid: false, errors, warnings };
    }

    // 2. Team timesheet self-selection
    if (
      context.timesheetAppliedFor?.toUpperCase() === 'TEAM' &&
      context.currentUser?.empId === context.timesheetFilledForUser?.empId
    ) {
      errors.push({ field: 'employee', message: 'You cannot select yourself for Team Timesheet', scope: 'TIMESHEET' });
      return { isValid: false, errors, warnings };
    }

    // 3. Day type
    if (!context.dayType) {
      errors.push({ field: 'dayType', message: 'Day type cannot be null', scope: 'TIMESHEET' });
      return { isValid: false, errors, warnings };
    }

    // 4. From date
    if (!context.fromDate) {
      errors.push({ field: 'fromDate', message: 'Date / From Date cannot be null', scope: 'TIMESHEET' });
      return { isValid: false, errors, warnings };
    }
    const fromDateParsed = this.parseDDMMYYYY(context.fromDate);
    if (fromDateParsed && fromDateParsed > new Date()) {
      errors.push({ field: 'fromDate', message: 'Date / From Date cannot be a future date', scope: 'TIMESHEET' });
      return { isValid: false, errors, warnings };
    }

    // 5. Night shift: To Date required
    if (context.isNightShift && isDayTypeFillable && !context.toDate) {
      errors.push({ field: 'toDate', message: 'To Date is required for Night Shift', scope: 'TIMESHEET' });
      return { isValid: false, errors, warnings };
    }

    // 6. Night shift: To Date same or next day
    if (context.isNightShift && isDayTypeFillable && context.toDate && context.fromDate) {
      const toDateParsed = this.parseDDMMYYYY(context.toDate);
      if (fromDateParsed && toDateParsed) {
        const diffDays = Math.round(
          (toDateParsed.getTime() - fromDateParsed.getTime()) / (1000 * 60 * 60 * 24)
        );
        if (diffDays !== 0 && diffDays !== 1) {
          errors.push({
            field: 'toDate',
            message: 'For Night Shift, To Date must be the same as From Date OR exactly one day after From Date',
            scope: 'TIMESHEET'
          });
          return { isValid: false, errors, warnings };
        }
      }
    }

    // 7. Night shift not allowed for non-fillable day type
    if (context.isNightShift && !isDayTypeFillable) {
      errors.push({ field: 'nightShift', message: 'Night shift not allowed for selected day type', scope: 'TIMESHEET' });
      return { isValid: false, errors, warnings };
    }

    // 8. Time validations for fillable day types
    if (isDayTypeFillable) {
      if (!context.apmosysInTime) {
        errors.push({ field: 'apmosysInTime', message: 'Work check in time must be filled', scope: 'TIMESHEET' });
        return { isValid: false, errors, warnings };
      }
      if (!context.apmosysOutTime) {
        errors.push({ field: 'apmosysOutTime', message: 'Work check out time must be filled', scope: 'TIMESHEET' });
        return { isValid: false, errors, warnings };
      }
      if (context.totalPresence === 0) {
        errors.push({ field: 'totalPresence', message: 'Your total presence cannot be 0', scope: 'TIMESHEET' });
        return { isValid: false, errors, warnings };
      }
      // Work check-out must be greater than work check-in
      let shouldValidateOrder = true;
      if (context.isNightShift && context.toDate && context.fromDate) {
        const toDateParsed = this.parseDDMMYYYY(context.toDate);
        if (fromDateParsed && toDateParsed) {
          const diffDays = Math.round(
            (toDateParsed.getTime() - fromDateParsed.getTime()) / (1000 * 60 * 60 * 24)
          );
          if (diffDays !== 0) shouldValidateOrder = false;
        }
      }
      if (shouldValidateOrder) {
        const inTime24 = this.parseTimeTo24Hour(context.apmosysInTime);
        const outTime24 = this.parseTimeTo24Hour(context.apmosysOutTime);
        if (inTime24 && outTime24) {
          const inTotalMinutes = inTime24.hour * 60 + inTime24.minute;
          const outTotalMinutes = outTime24.hour * 60 + outTime24.minute;
          if (outTotalMinutes <= inTotalMinutes) {
            errors.push({
              field: 'apmosysOutTime',
              message: 'Work Check-Out time must be greater than Work Check-In time',
              scope: 'TIMESHEET'
            });
            return { isValid: false, errors, warnings };
          }
        }
      }
    } else {
      if (context.apmosysInTime) {
        errors.push({ field: 'apmosysInTime', message: 'Work check in time cannot be filled for this day type', scope: 'TIMESHEET' });
        return { isValid: false, errors, warnings };
      }
      if (context.apmosysOutTime) {
        errors.push({ field: 'apmosysOutTime', message: 'Work check out time cannot be filled for this day type', scope: 'TIMESHEET' });
        return { isValid: false, errors, warnings };
      }
      if (context.totalPresence > 0) {
        errors.push({ field: 'totalPresence', message: 'Total presence cannot be greater than 0 for this day type', scope: 'TIMESHEET' });
        return { isValid: false, errors, warnings };
      }
    }

    return { isValid: true, errors, warnings };
  }

  /**
   * Validates locations and projects. Order: location-level fields first (location selected, then location times), then projects and their fields in order (projectId → clientId → clientLocationId → clientApprovalStatus → activities/description). Returns on first error (fail-fast).
   */
  private validateLocationsAndProjects(context: TimesheetValidationContext): ValidationResult {
    const errors: ValidationError[] = [];
    const warnings: string[] = [];
    const highlightedLocationIds: number[] = [];
    const isDayTypeFillable = this.isDayTypeFillable(context.dayType);

    const fail = (err: ValidationError, locationId?: number): ValidationResult => {
      errors.push(err);
      if (locationId) highlightedLocationIds.push(locationId);
      return { isValid: false, errors, warnings, highlightedLocationIds };
    };

    if (!context.timesheetLocations || context.timesheetLocations.length === 0) {
      return fail({ field: 'locations', message: 'At least one location is required', scope: 'LOCATION' });
    }

    // Track client approval status per project across all locations (for consistency validation)
    const projectStatusMap: Map<number, { status: number; locationIndex: number; projectIndex: number; locLabel: string }> =
      new Map();
    // Track shadow options per project across all locations (must be same for same project)
    const projectShadowMap: Map<number, { isShadow: boolean; isShadowForSelf: boolean; shadowEmpId: number | null; locLabel: string }> =
      new Map();

    for (let lIndex = 0; lIndex < context.timesheetLocations.length; lIndex++) {
      const location = context.timesheetLocations[lIndex];
      const locationId = location.workLocationTypeId || 0;
      const locLabel = context.workLocationList?.find(l => l.workLocationTypeId === location.workLocationTypeId)?.code || `Location ${lIndex + 1}`;

      // 1. Location must be selected (workLocationTypeId)
      if (!location.workLocationTypeId) {
        return fail({
          field: 'workLocationTypeId',
          message: `Location must be selected for ${locLabel}`,
          scope: 'LOCATION',
          locationIndex: lIndex,
          locationId
        }, locationId);
      }

      // 2. Location times (fillable day types only) – in order: in-time, out-time, out > in
      if (isDayTypeFillable) {
        if (
          location.locationInTime &&
          context.fromDate &&
          context.apmosysInTime &&
          !this.validateLocationInTime(location.locationInTime, context.fromDate, context.apmosysInTime)
        ) {
          return fail({
            field: 'locationInTime',
            message: `Log-In time for ${locLabel} cannot be less than Work Check-In time`,
            scope: 'LOCATION',
            locationIndex: lIndex,
            locationId
          }, locationId);
        }
        if (
          location.locationOutTime &&
          context.fromDate &&
          context.apmosysOutTime &&
          !this.validateLocationOutTime(
            location.locationOutTime,
            context.fromDate,
            context.apmosysOutTime,
            context.isNightShift,
            context.toDate || undefined
          )
        ) {
          return fail({
            field: 'locationOutTime',
            message: `Log-Out time for ${locLabel} cannot exceed Work Check-Out time`,
            scope: 'LOCATION',
            locationIndex: lIndex,
            locationId
          }, locationId);
        }
        if (location.locationInTime && location.locationOutTime) {
          let shouldValidateOrder = true;
          if (context.isNightShift && context.toDate && context.fromDate) {
            const fromDateParsed = this.parseDDMMYYYY(context.fromDate);
            const toDateParsed = this.parseDDMMYYYY(context.toDate);
            if (fromDateParsed && toDateParsed) {
              const diffDays = Math.round(
                (toDateParsed.getTime() - fromDateParsed.getTime()) / (1000 * 60 * 60 * 24)
              );
              if (diffDays !== 0) shouldValidateOrder = false;
            }
          }
          if (shouldValidateOrder) {
            const inTime24 = this.parseTimeTo24Hour(location.locationInTime);
            const outTime24 = this.parseTimeTo24Hour(location.locationOutTime);
            if (inTime24 && outTime24) {
              const inTotalMinutes = inTime24.hour * 60 + inTime24.minute;
              const outTotalMinutes = outTime24.hour * 60 + outTime24.minute;
              if (outTotalMinutes <= inTotalMinutes) {
                return fail({
                  field: 'locationTimeOrder',
                  message: `Log-Out time for ${locLabel} must be greater than Log-In time`,
                  scope: 'LOCATION',
                  locationIndex: lIndex,
                  locationId
                }, locationId);
              }
            }
          }
        }
      }

      // 3. At least one project
      if (!location.projects || location.projects.length === 0) {
        return fail({
          field: 'projects',
          message: `For ${locLabel} no project is provided`,
          scope: 'PROJECT',
          locationIndex: lIndex,
          locationId
        }, locationId);
      }

      // 4. Each project: projectId → clientId → clientLocationId → clientApprovalStatus → activities / description
      for (let pIndex = 0; pIndex < location.projects.length; pIndex++) {
        const project = location.projects[pIndex];

        if (!project.projectId) {
          return fail({
            field: 'projectId',
            message: `Project ${pIndex + 1} is not selected for ${locLabel}`,
            scope: 'PROJECT',
            locationIndex: lIndex,
            projectIndex: pIndex,
            locationId
          }, locationId);
        }
        if (!project.clientId) {
          return fail({
            field: 'clientId',
            message: `Client is mandatory for Project ${pIndex + 1} in ${locLabel}`,
            scope: 'PROJECT',
            locationIndex: lIndex,
            projectIndex: pIndex,
            locationId
          }, locationId);
        }
        if (!project.clientLocationId) {
          return fail({
            field: 'clientLocationId',
            message: `Client Location is mandatory for Project ${pIndex + 1} in ${locLabel}`,
            scope: 'PROJECT',
            locationIndex: lIndex,
            projectIndex: pIndex,
            locationId
          }, locationId);
        }
        if (project.clientSideId && isDayTypeFillable && !project.isShadowForSelf && !project.clientApprovalStatus) {
          return fail({
            field: 'clientApprovalStatus',
            message: `Client DSR Approval Status is mandatory for Project ${pIndex + 1} in ${locLabel}`,
            scope: 'PROJECT',
            locationIndex: lIndex,
            projectIndex: pIndex,
            locationId
          }, locationId);
        }

        // Track client approval status consistency across locations for the same project (skip when Shadow for self)
        if (
          isDayTypeFillable &&
          !project.isShadowForSelf &&
          project.clientSideId &&
          project.projectId != null &&
          project.clientApprovalStatus != null
        ) {
          const projId = Number(project.projectId);
          const status = Number(project.clientApprovalStatus);
          const existing = projectStatusMap.get(projId);
          if (!existing) {
            projectStatusMap.set(projId, { status, locationIndex: lIndex, projectIndex: pIndex, locLabel });
          } else if (existing.status !== status) {
            // Inconsistent client approval status for same project across locations
            return fail({
              field: 'clientApprovalStatus',
              message: `Client DSR Approval Status must be same for Project ${pIndex + 1} across all locations.`,
              scope: 'PROJECT',
              locationIndex: lIndex,
              projectIndex: pIndex,
              locationId
            }, locationId);
          }
        }

        // Shadow consistency: same project must have same shadow choice (isShadowTimesheet, isShadowForSelf, shadowEmpId) across all locations
        if (project.projectId != null && isDayTypeFillable) {
          const projId = Number(project.projectId);
          const isShadow = !!project.isShadowTimesheet;
          const isShadowForSelfVal = !!project.isShadowForSelf;
          const shadowEmpIdVal = project.shadowEmpId != null ? Number(project.shadowEmpId) : null;
          const existingShadow = projectShadowMap.get(projId);
          if (!existingShadow) {
            projectShadowMap.set(projId, { isShadow, isShadowForSelf: isShadowForSelfVal, shadowEmpId: shadowEmpIdVal, locLabel });
          } else {
            const sameShadow =
              existingShadow.isShadow === isShadow &&
              existingShadow.isShadowForSelf === isShadowForSelfVal &&
              (existingShadow.shadowEmpId == null ? shadowEmpIdVal == null : existingShadow.shadowEmpId === shadowEmpIdVal);
            if (!sameShadow) {
              return fail({
                field: 'isShadowTimesheet',
                message: `Shadow option (Shadow timesheet / Shadow for self / Shadow for) must be the same for the same project across all locations.`,
                scope: 'PROJECT',
                locationIndex: lIndex,
                projectIndex: pIndex,
                locationId
              }, locationId);
            }
          }
        }

        if (isDayTypeFillable) {
          if (!project.activities || project.activities.length === 0) {
            return fail({
              field: 'activities',
              message: `At least one activity is required for Project ${pIndex + 1} in ${locLabel}`,
              scope: 'ACTIVITY',
              locationIndex: lIndex,
              projectIndex: pIndex,
              locationId
            }, locationId);
          }
          for (let aIndex = 0; aIndex < project.activities.length; aIndex++) {
            const activity = project.activities[aIndex];
            if (!activity.teamId) {
              return fail({
                field: 'teamId',
                message: `Team is required for Activity ${aIndex + 1} in Project ${pIndex + 1} (${locLabel})`,
                scope: 'ACTIVITY',
                locationIndex: lIndex,
                projectIndex: pIndex,
                activityIndex: aIndex,
                locationId
              }, locationId);
            }
            if (!activity.activityId) {
              return fail({
                field: 'activityId',
                message: `Activity is required for Activity in Project ${pIndex + 1} (${locLabel})`,
                scope: 'ACTIVITY',
                locationIndex: lIndex,
                projectIndex: pIndex,
                activityIndex: aIndex,
                locationId
              }, locationId);
            }
            if (!activity.durationMinutes || activity.durationMinutes <= 0) {
              return fail({
                field: 'durationMinutes',
                message: `Hours must be greater than 0 for Activity ${aIndex + 1} in Project ${pIndex + 1} (${locLabel})`,
                scope: 'ACTIVITY',
                locationIndex: lIndex,
                projectIndex: pIndex,
                activityIndex: aIndex,
                locationId
              }, locationId);
            }
          }
        } else {
          if (!project.description || project.description.trim().length === 0) {
            return fail({
              field: 'description',
              message: `Description is required for Project ${pIndex + 1} in ${locLabel}`,
              scope: 'PROJECT',
              locationIndex: lIndex,
              projectIndex: pIndex,
              locationId
            }, locationId);
          }
        }
      }
    }

    return { isValid: true, errors, warnings, highlightedLocationIds };
  }

  /**
   * Validates hours calculations and relationships
   */
  public validateHours(context: TimesheetValidationContext): ValidationResult {
    const errors: ValidationError[] = [];
    const warnings: string[] = [];
    const highlightedLocationIds: number[] = [];
    const isDayTypeFillable = this.isDayTypeFillable(context.dayType);

    if (!isDayTypeFillable) {
      return { isValid: true, errors, warnings, highlightedLocationIds };
    }

    let totalLocationHours = 0;

    for (let lIndex = 0; lIndex < context.timesheetLocations.length; lIndex++) {
      const location = context.timesheetLocations[lIndex];
      const locationId = location.workLocationTypeId || 0;

      // Calculate project hours for this location
      let projectHoursForLocation = 0;

      if (location.projects) {
        for (const project of location.projects) {
          let projHours = 0;

          if (project.activities) {
            for (const activity of project.activities) {
              projHours += Number(activity.durationMinutes) || 0;
            }
          }

          projectHoursForLocation += projHours;
        }
      }

      // Validation: Project hours cannot exceed location hours (fail-fast)
      if (
        location.totalWorkingHours !== null &&
        location.totalWorkingHours !== undefined &&
        projectHoursForLocation > location.totalWorkingHours
      ) {
        const selectedLocationData = context.workLocationList?.find(
          l => l.workLocationTypeId === location.workLocationTypeId
        );
        errors.push({
          field: 'projectHours',
          message: `Total working hours for location ${selectedLocationData?.code || lIndex + 1} cannot be less than sum of project working hours`,
          scope: 'LOCATION',
          locationIndex: lIndex,
          locationId
        });
        if (locationId) highlightedLocationIds.push(locationId);
        return { isValid: false, errors, warnings, highlightedLocationIds };
      }

      totalLocationHours += location.totalWorkingHours || 0;

      // Validation: Total location hours cannot exceed total presence (fail-fast)
      const presenceWithTolerance = (context.totalPresence || 0) + 0.01;
      if (totalLocationHours > presenceWithTolerance) {
        const selectedLocationData = context.workLocationList?.find(
          l => l.workLocationTypeId === location.workLocationTypeId
        );
        errors.push({
          field: 'totalLocationHours',
          message: `Total working hours for all locations cannot be more than total presence hours`,
          scope: 'LOCATION',
          locationIndex: lIndex,
          locationId
        });
        if (locationId) highlightedLocationIds.push(locationId);
        return { isValid: false, errors, warnings, highlightedLocationIds };
      }
    }

    return { isValid: errors.length === 0, errors, warnings, highlightedLocationIds };
  }

  /**
   * Validates document uploads
   */
  public validateDocumentUploads(context: TimesheetValidationContext): ValidationResult {
    const errors: ValidationError[] = [];
    const warnings: string[] = [];
    const isDayTypeFillable = this.isDayTypeFillable(context.dayType);

    // Document validation only applies when employee has client side ID and day type is fillable
    if (!context.empHasClientSideId || !isDayTypeFillable || !context.documentData || !context.uniqueProjectsList) {
      return { isValid: true, errors, warnings };
    }

    // Helper function to check if file exists
    const containsFile = (fileName: string | null): boolean => {
      if (!fileName || !context.documentData) return false;
      return context.documentData.some(f => f.uniqueIdentifier === fileName);
    };

    for (const project of context.uniqueProjectsList) {
      // Skip when Shadow for self (client approval status and documents not required)
      if (project.isShadowForSelf) continue;
      // Only validate for approved (2) or pending (1) projects
      if (project.clientApprovalStatus !== 1 && project.clientApprovalStatus !== 2) {
        continue;
      }

      const projectDocs = context.documentData.filter(
        d => d.projectId === project.projectId
      );

      // Filled Attendance Proof validation (fail-fast)
      const filledDoc = projectDocs.find(d => d.docType === 'Filled');
      if (!filledDoc || !filledDoc.uniqueIdentifier || !containsFile(filledDoc.uniqueIdentifier)) {
        errors.push({
          field: 'filledDocument',
          message: `Filled Attendance Proof is mandatory for Project ${project.projectName || project.projectId}`,
          scope: 'DOCUMENT',
          projectIndex: project.projectId || undefined
        });
        return { isValid: false, errors, warnings };
      }
      if (filledDoc.fileError) {
        errors.push({
          field: 'filledDocument',
          message: `Filled Attendance Proof has an error for Project ${project.projectName || project.projectId}`,
          scope: 'DOCUMENT',
          projectIndex: project.projectId || undefined
        });
        return { isValid: false, errors, warnings };
      }

      // Approved Attendance Proof validation (only when status is Approved) (fail-fast)
      if (project.clientApprovalStatus === 2) {
        const approvedDoc = projectDocs.find(d => d.docType === 'Approved');
        if (!approvedDoc || !approvedDoc.uniqueIdentifier || !containsFile(approvedDoc.uniqueIdentifier)) {
          errors.push({
            field: 'approvedDocument',
            message: `Approved Attendance Proof is mandatory for Project ${project.projectName || project.projectId}`,
            scope: 'DOCUMENT',
            projectIndex: project.projectId || undefined
          });
          return { isValid: false, errors, warnings };
        }
        if (approvedDoc.fileError) {
          errors.push({
            field: 'approvedDocument',
            message: `Approved Attendance Proof has an error for Project ${project.projectName || project.projectId}`,
            scope: 'DOCUMENT',
            projectIndex: project.projectId || undefined
          });
          return { isValid: false, errors, warnings };
        }
      }
    }

    return { isValid: errors.length === 0, errors, warnings };
  }

  /**
   * Validates location in-time against ApMoSys in-time
   */
  validateLocationInTime(
    locationInTime: string,
    fromDate: string,
    apmosysInTime: string
  ): boolean {
    if (!locationInTime || !fromDate || !apmosysInTime) {
      return false;
    }

    const parseDate = (dateStr: string): Date | null => {
      const parts = dateStr.split('-');
      if (parts.length !== 3) return null;
      const [day, month, year] = parts.map(Number);
      if (!day || !month || !year) return null;
      return new Date(year, month - 1, day);
    };

    const parseTime = (timeStr: string): { hh: number; mm: number } | null => {
      const trimmed = timeStr.trim();
      const parts = trimmed.split(' ');
      let timePart = trimmed;
      let meridian = '';

      if (parts.length === 2) {
        timePart = parts[0];
        meridian = parts[1].toUpperCase();
      }

      const timeComponents = timePart.split(':');
      if (timeComponents.length !== 2) return null;

      let hh = Number(timeComponents[0]);
      const mm = Number(timeComponents[1]);

      if (isNaN(hh) || isNaN(mm)) return null;

      // Handle 12-hour format
      if (meridian === 'PM' && hh !== 12) hh += 12;
      if (meridian === 'AM' && hh === 12) hh = 0;

      return { hh, mm };
    };

    const date = parseDate(fromDate);
    const inTime = parseTime(locationInTime);
    const apmIn = parseTime(apmosysInTime);

    if (!date || !inTime || !apmIn) {
      return false;
    }

    const inDateTime = new Date(
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
      inTime.hh,
      inTime.mm
    );

    const apmosysInDateTime = new Date(
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
      apmIn.hh,
      apmIn.mm
    );

    // Location in-time must be >= ApMoSys in-time
    return inDateTime.getTime() >= apmosysInDateTime.getTime();
  }

  /**
   * Helper: Parse time string (HH:mm or HH:mm AM/PM) to 24-hour components
   */
  private parseTimeTo24Hour(timeStr: string): { hour: number; minute: number } | null {
    if (!timeStr) return null;

    const trimmed = timeStr.trim().toUpperCase();
    const hasAMPM = trimmed.includes('AM') || trimmed.includes('PM');

    if (hasAMPM) {
      // Format: "HH:mm AM/PM"
      const [timePart, meridian] = trimmed.split(' ');
      if (!timePart || !meridian) return null;
      const [hhStr, mmStr] = timePart.split(':');
      const hourRaw = Number(hhStr);
      const minute = Number(mmStr);
      if (isNaN(hourRaw) || isNaN(minute)) return null;
      if (hourRaw < 1 || hourRaw > 12 || minute < 0 || minute > 59) return null;

      let hour = hourRaw;
      if (meridian === 'PM' && hour !== 12) hour += 12;
      if (meridian === 'AM' && hour === 12) hour = 0;
      return { hour, minute };
    } else {
      // Format: "HH:mm" (24-hour)
      const [hhStr, mmStr] = trimmed.split(':');
      const hour = Number(hhStr);
      const minute = Number(mmStr);
      if (isNaN(hour) || isNaN(minute)) return null;
      if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return null;
      return { hour, minute };
    }
  }

  /**
   * Validates location out-time against ApMoSys out-time
   */
  validateLocationOutTime(
    locationOutTime: string,
    fromDate: string,
    apmosysOutTime: string,
    isNightShift: boolean,
    toDate?: string
  ): boolean {
    if (!locationOutTime || !fromDate || !apmosysOutTime) {
      return false;
    }

    if (isNightShift && !toDate) {
      return false;
    }

    const parseDate = (dateStr: string): Date | null => {
      const parts = dateStr.split('-');
      if (parts.length !== 3) return null;
      const [day, month, year] = parts.map(Number);
      if (!day || !month || !year) return null;
      return new Date(year, month - 1, day);
    };

    const parseTime = (timeStr: string): { hh: number; mm: number } | null => {
      const trimmed = timeStr.trim();
      const parts = trimmed.split(' ');
      let timePart = trimmed;
      let meridian = '';

      if (parts.length === 2) {
        timePart = parts[0];
        meridian = parts[1].toUpperCase();
      }

      const timeComponents = timePart.split(':');
      if (timeComponents.length !== 2) return null;

      let hh = Number(timeComponents[0]);
      const mm = Number(timeComponents[1]);

      if (isNaN(hh) || isNaN(mm)) return null;

      // Handle 12-hour format
      if (meridian === 'PM' && hh !== 12) hh += 12;
      if (meridian === 'AM' && hh === 12) hh = 0;

      return { hh, mm };
    };

    const startDate = parseDate(fromDate);
    const endDate = isNightShift && toDate ? parseDate(toDate) : startDate;
    const outTime = parseTime(locationOutTime);
    const apmOut = parseTime(apmosysOutTime);

    if (!startDate || !endDate || !outTime || !apmOut) {
      return false;
    }

    const outDateTime = new Date(
      endDate.getFullYear(),
      endDate.getMonth(),
      endDate.getDate(),
      outTime.hh,
      outTime.mm
    );

    const apmosysOutDateTime = new Date(
      endDate.getFullYear(),
      endDate.getMonth(),
      endDate.getDate(),
      apmOut.hh,
      apmOut.mm
    );

    // Location out-time must be <= ApMoSys out-time
    return outDateTime.getTime() <= apmosysOutDateTime.getTime();
  }

  /**
   * Checks if a day type is fillable (requires time entries)
   * Uses TimesheetConfigService for configuration
   */
  isDayTypeFillable(dayType: number | null): boolean {
    return this.configService.isDayTypeFillable(dayType);
  }

  /**
   * Parses DD-MM-YYYY date string to Date object
   */
  private parseDDMMYYYY(dateStr: string): Date | null {
    if (!dateStr) return null;
    const parts = dateStr.split('-');
    if (parts.length !== 3) return null;
    const [day, month, year] = parts.map(Number);
    if (!day || !month || !year) return null;
    return new Date(year, month - 1, day);
  }

  /**
   * Removes duplicate errors based on field and message
   */
  private deduplicateErrors(errors: ValidationError[]): ValidationError[] {
    const seen = new Set<string>();
    return errors.filter(error => {
      const key = `${error.field}-${error.message}-${error.locationIndex}-${error.projectIndex}-${error.activityIndex}`;
      if (seen.has(key)) {
        return false;
      }
      seen.add(key);
      return true;
    });
  }

  /**
   * Removes duplicate warnings
   */
  private deduplicateWarnings(warnings: string[]): string[] {
    return [...new Set(warnings)];
  }

  /**
   * Formats validation errors for display
   * Returns a single string with all error messages,
   * each on its own line, prefixed with a pointing finger emoji.
   */
  formatErrorsForDisplay(result: ValidationResult): string {
    if (result.isValid) return '';

    return result.errors
      .map(error => `👉 ${error.message}`)
      .join('\n');
  }

  /**
   * Formats validation warnings for display
   */
  formatWarningsForDisplay(result: ValidationResult): string {
    if (result.warnings.length === 0) return '';
    
    if (result.warnings.length === 1) {
      return result.warnings[0];
    }

    return result.warnings
      .map((warning, index) => `${index + 1}. ${warning}`)
      .join('\n');
  }
}

/*
While timesheet creation if user have selected night shift
then workout date is calculated based on date in toDate selected on UI
But toDate we don't store directly
so,during update also we have decide dynamicaly 
based on workin and workout date time whether toDate is same 
as from date or different i.e(fromDate+1) Currently during update we always 
show to date as fromDate+1
*/
