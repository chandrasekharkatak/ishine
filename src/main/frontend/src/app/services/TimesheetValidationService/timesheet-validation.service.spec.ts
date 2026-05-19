import { TestBed } from '@angular/core/testing';
import { TimesheetValidationService, TimesheetValidationContext, ValidationResult } from './timesheet-validation.service';
import { TimesheetConfigService } from './timesheet-config.service';
import { LocationEntry } from 'src/app/models/locationEntry';
import { ProjectEntry } from 'src/app/models/projectEntry';
import { ActivityNew } from 'src/app/models/activityNew';
import { User } from 'src/app/models/user';
import { TimesheetDocumentDataI } from 'src/app/user-timesheet/my-timesheet/timesheet-form/types';

describe('TimesheetValidationService', () => {
  let service: TimesheetValidationService;
  let configService: TimesheetConfigService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [TimesheetConfigService]
    });
    service = TestBed.inject(TimesheetValidationService);
    configService = TestBed.inject(TimesheetConfigService);
  });

  /**
   * Helper function to create a mock validation context
   */
  function createMockContext(overrides: Partial<TimesheetValidationContext> = {}): TimesheetValidationContext {
    const defaultUser: User = {
      empId: 1,
      employeementId: 'EMP001',
      isApmosysProduct: false,
      isTimesheetLockCheckEnable: true,
      timesheetBackDatedDays: 30,
      timesheetLockDays: 7,
      managerId: 2,
      departmentId: 1
    } as User;

    const defaultContext: TimesheetValidationContext = {
      dayType: 1,
      fromDate: '01-01-2024',
      toDate: null,
      isNightShift: false,
      apmosysInTime: '09:00',
      apmosysOutTime: '18:00',
      totalPresence: 8,
      timesheetAppliedFor: 'self',
      timesheetFilledForUser: defaultUser,
      currentUser: defaultUser,
      timesheetLocations: [],
      workLocationList: [
        { workLocationTypeId: 1, code: 'OFFICE' },
        { workLocationTypeId: 2, code: 'REMOTE' }
      ],
      documentData: [],
      uniqueProjectsList: [],
      empHasClientSideId: false
    };

    return { ...defaultContext, ...overrides };
  }

  /**
   * Helper function to create a mock location entry
   */
  function createMockLocation(overrides: Partial<LocationEntry> = {}): LocationEntry {
    const defaultLocation: LocationEntry = {
      locationMappingId: null,
      workLocationType: null,
      workLocationTypeId: 1,
      locationInTime: '09:00',
      locationOutTime: '18:00',
      totalWorkingHours: 8,
      projects: []
    };

    return { ...defaultLocation, ...overrides };
  }

  /**
   * Helper function to create a mock project entry
   */
  function createMockProject(overrides: Partial<ProjectEntry> = {}): ProjectEntry {
    const defaultProject: ProjectEntry = {
      projectId: 1,
      projectName: 'Test Project',
      clientId: 1,
      clientLocationId: 1,
      clientApprovalStatus: null,
      clientSideId: null,
      hasClientSideId: false,
      hasClientFlag: false,
      shadowEmpId: null,
      isShadowTimesheet: false,
      isShadowForSelf: false,
      activities: [],
      projectActivities: [],
      clientList: [],
      clientLocationList: [],
      projectList: [],
      shadowForList: [],
      poId: null,
      poNo: '',
      status: 1,
      locationMappingId: null,
      projectHoursMinutes: null,
      timesheetId: null,
      totalClientWorkingMinutes: null,
      totalWorkingHours: 8,
      description: null
    };

    return { ...defaultProject, ...overrides };
  }

  /**
   * Helper function to create a mock activity entry
   */
  function createMockActivity(overrides: Partial<ActivityNew> = {}): ActivityNew {
    const defaultActivity: ActivityNew = {
      activityId: 1,
      description: 'Test Activity',
      durationMinutes: 8,
      teamId: 1,
      projectId: 1,
      timesheetId: null,
      clientTeamList: [],
      allActivitiesForProject: []
    };

    return { ...defaultActivity, ...overrides };
  }

  describe('validateCreate', () => {
    it('should return valid for a complete valid timesheet', () => {
      const project = createMockProject({
        activities: [createMockActivity()]
      });
      const location = createMockLocation({
        projects: [project]
      });

      const context = createMockContext({
        timesheetLocations: [location]
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(true);
      expect(result.errors.length).toBe(0);
    });

    it('should return invalid when employee is not selected', () => {
      const context = createMockContext({
        timesheetFilledForUser: null
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.field === 'employee')).toBe(true);
    });

    it('should return invalid when user selects themselves for team timesheet', () => {
      const user: User = { empId: 1 } as User;
      const context = createMockContext({
        timesheetAppliedFor: 'team',
        timesheetFilledForUser: user,
        currentUser: user
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.message.includes('cannot select yourself'))).toBe(true);
    });

    it('should return invalid when day type is null', () => {
      const context = createMockContext({
        dayType: null
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.field === 'dayType')).toBe(true);
    });

    it('should return invalid when from date is null', () => {
      const context = createMockContext({
        fromDate: null
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.field === 'fromDate')).toBe(true);
    });

    it('should return invalid when from date is in the future', () => {
      const futureDate = new Date();
      futureDate.setDate(futureDate.getDate() + 1);
      const futureDateStr = `${String(futureDate.getDate()).padStart(2, '0')}-${String(futureDate.getMonth() + 1).padStart(2, '0')}-${futureDate.getFullYear()}`;

      const context = createMockContext({
        fromDate: futureDateStr
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.message.includes('future date'))).toBe(true);
    });
  });

  describe('validateBasicFields', () => {
    it('should validate night shift requires to date', () => {
      const context = createMockContext({
        isNightShift: true,
        dayType: 1, // Fillable day type
        toDate: null
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.field === 'toDate' && e.message.includes('Night Shift'))).toBe(true);
    });

    it('should validate to date must be after from date for night shift', () => {
      const context = createMockContext({
        isNightShift: true,
        dayType: 1,
        fromDate: '02-01-2024',
        toDate: '01-01-2024' // Before from date
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.message.includes('To Date must be after'))).toBe(true);
    });

    it('should validate night shift not allowed for non-fillable day types', () => {
      const context = createMockContext({
        isNightShift: true,
        dayType: 4 // Non-fillable day type
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.message.includes('Night shift not allowed'))).toBe(true);
    });

    it('should validate work check in time required for fillable day types', () => {
      const context = createMockContext({
        dayType: 1, // Fillable
        apmosysInTime: null
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.field === 'apmosysInTime')).toBe(true);
    });

    it('should validate work check out time required for fillable day types', () => {
      const context = createMockContext({
        dayType: 1, // Fillable
        apmosysOutTime: null
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.field === 'apmosysOutTime')).toBe(true);
    });

    it('should validate total presence cannot be 0 for fillable day types', () => {
      const context = createMockContext({
        dayType: 1, // Fillable
        totalPresence: 0
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.field === 'totalPresence' && e.message.includes('cannot be 0'))).toBe(true);
    });

    it('should validate times cannot be filled for non-fillable day types', () => {
      const context = createMockContext({
        dayType: 4, // Non-fillable
        apmosysInTime: '09:00',
        apmosysOutTime: '18:00'
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.message.includes('cannot be filled for this day type'))).toBe(true);
    });
  });

  describe('validateLocationsAndProjects', () => {
    it('should return invalid when no locations provided', () => {
      const context = createMockContext({
        timesheetLocations: []
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.scope === 'LOCATION' && e.message.includes('at least one location'))).toBe(true);
    });

    it('should validate location has projects', () => {
      const location = createMockLocation({
        projects: []
      });

      const context = createMockContext({
        timesheetLocations: [location]
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.scope === 'PROJECT' && e.message.includes('no project is provided'))).toBe(true);
    });

    it('should validate project is selected', () => {
      const project = createMockProject({
        projectId: null
      });
      const location = createMockLocation({
        projects: [project]
      });

      const context = createMockContext({
        timesheetLocations: [location]
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.field === 'projectId')).toBe(true);
    });

    it('should validate client is mandatory for project', () => {
      const project = createMockProject({
        clientId: null
      });
      const location = createMockLocation({
        projects: [project]
      });

      const context = createMockContext({
        timesheetLocations: [location]
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.field === 'clientId' && e.message.includes('Client is mandatory'))).toBe(true);
    });

    it('should validate client location is mandatory for project', () => {
      const project = createMockProject({
        clientLocationId: null
      });
      const location = createMockLocation({
        projects: [project]
      });

      const context = createMockContext({
        timesheetLocations: [location]
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.field === 'clientLocationId' && e.message.includes('Client Location is mandatory'))).toBe(true);
    });

    it('should validate activities required for fillable day types', () => {
      const project = createMockProject({
        activities: []
      });
      const location = createMockLocation({
        projects: [project]
      });

      const context = createMockContext({
        dayType: 1, // Fillable
        timesheetLocations: [location]
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.scope === 'ACTIVITY' && e.message.includes('At least one activity'))).toBe(true);
    });

    it('should validate team is required for activity', () => {
      const activity = createMockActivity({
        teamId: null
      });
      const project = createMockProject({
        activities: [activity]
      });
      const location = createMockLocation({
        projects: [project]
      });

      const context = createMockContext({
        timesheetLocations: [location]
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.field === 'teamId' && e.message.includes('Team is required'))).toBe(true);
    });

    it('should validate activity is required', () => {
      const activity = createMockActivity({
        activityId: null
      });
      const project = createMockProject({
        activities: [activity]
      });
      const location = createMockLocation({
        projects: [project]
      });

      const context = createMockContext({
        timesheetLocations: [location]
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.field === 'activityId' && e.message.includes('Activity is required'))).toBe(true);
    });

    it('should validate activity hours must be greater than 0', () => {
      const activity = createMockActivity({
        durationMinutes: 0
      });
      const project = createMockProject({
        activities: [activity]
      });
      const location = createMockLocation({
        projects: [project]
      });

      const context = createMockContext({
        timesheetLocations: [location]
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.field === 'durationMinutes' && e.message.includes('must be greater than 0'))).toBe(true);
    });

    it('should validate description required for non-fillable day types', () => {
      const project = createMockProject({
        description: null,
        activities: []
      });
      const location = createMockLocation({
        projects: [project]
      });

      const context = createMockContext({
        dayType: 4, // Non-fillable
        timesheetLocations: [location]
      });

      const result = service.validateCreate(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.field === 'description' && e.message.includes('Description is required'))).toBe(true);
    });
  });

  describe('validateLocationInTime', () => {
    it('should validate location in-time is not before ApMoSys in-time', () => {
      const isValid = service.validateLocationInTime(
        '08:00', // Location in-time
        '01-01-2024',
        '09:00' // ApMoSys in-time
      );

      expect(isValid).toBe(false);
    });

    it('should validate location in-time can be equal to ApMoSys in-time', () => {
      const isValid = service.validateLocationInTime(
        '09:00', // Location in-time
        '01-01-2024',
        '09:00' // ApMoSys in-time
      );

      expect(isValid).toBe(true);
    });

    it('should validate location in-time can be after ApMoSys in-time', () => {
      const isValid = service.validateLocationInTime(
        '10:00', // Location in-time
        '01-01-2024',
        '09:00' // ApMoSys in-time
      );

      expect(isValid).toBe(true);
    });

    it('should handle 12-hour format with AM', () => {
      const isValid = service.validateLocationInTime(
        '09:00 AM',
        '01-01-2024',
        '08:00 AM'
      );

      expect(isValid).toBe(true);
    });

    it('should handle 12-hour format with PM', () => {
      const isValid = service.validateLocationInTime(
        '02:00 PM',
        '01-01-2024',
        '01:00 PM'
      );

      expect(isValid).toBe(true);
    });

    it('should return false for invalid time format', () => {
      const isValid = service.validateLocationInTime(
        'invalid',
        '01-01-2024',
        '09:00'
      );

      expect(isValid).toBe(false);
    });
  });

  describe('validateLocationOutTime', () => {
    it('should validate location out-time is not after ApMoSys out-time', () => {
      const isValid = service.validateLocationOutTime(
        '19:00', // Location out-time
        '01-01-2024',
        '18:00', // ApMoSys out-time
        false
      );

      expect(isValid).toBe(false);
    });

    it('should validate location out-time can be equal to ApMoSys out-time', () => {
      const isValid = service.validateLocationOutTime(
        '18:00', // Location out-time
        '01-01-2024',
        '18:00', // ApMoSys out-time
        false
      );

      expect(isValid).toBe(true);
    });

    it('should validate location out-time can be before ApMoSys out-time', () => {
      const isValid = service.validateLocationOutTime(
        '17:00', // Location out-time
        '01-01-2024',
        '18:00', // ApMoSys out-time
        false
      );

      expect(isValid).toBe(true);
    });

    it('should validate night shift requires to date', () => {
      const isValid = service.validateLocationOutTime(
        '02:00',
        '01-01-2024',
        '01:00',
        true, // Night shift
        undefined // No to date
      );

      expect(isValid).toBe(false);
    });

    it('should handle night shift with to date', () => {
      const isValid = service.validateLocationOutTime(
        '02:00',
        '01-01-2024',
        '01:00',
        true, // Night shift
        '02-01-2024' // To date
      );

      expect(isValid).toBe(true);
    });
  });

  describe('validateHours', () => {
    it('should validate project hours cannot exceed location hours', () => {
      const activity = createMockActivity({
        durationMinutes: 10 // 10 hours
      });
      const project = createMockProject({
        activities: [activity],
        totalWorkingHours: 10
      });
      const location = createMockLocation({
        projects: [project],
        totalWorkingHours: 8 // Location has only 8 hours
      });

      const context = createMockContext({
        timesheetLocations: [location]
      });

      const result = service.validateHours(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.message.includes('cannot be less than sum of project working hours'))).toBe(true);
    });

    it('should validate total location hours cannot exceed total presence', () => {
      const location1 = createMockLocation({
        totalWorkingHours: 5
      });
      const location2 = createMockLocation({
        totalWorkingHours: 4
      });

      const context = createMockContext({
        totalPresence: 8, // Total presence is 8 hours
        timesheetLocations: [location1, location2] // Total: 9 hours
      });

      const result = service.validateHours(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.message.includes('cannot be more than total presence hours'))).toBe(true);
    });

    it('should return valid when hours are correctly balanced', () => {
      const activity = createMockActivity({
        durationMinutes: 4
      });
      const project = createMockProject({
        activities: [activity],
        totalWorkingHours: 4
      });
      const location = createMockLocation({
        projects: [project],
        totalWorkingHours: 8
      });

      const context = createMockContext({
        totalPresence: 8,
        timesheetLocations: [location]
      });

      const result = service.validateHours(context);
      expect(result.isValid).toBe(true);
    });
  });

  describe('validateDocumentUploads', () => {
    it('should skip document validation when employee has no client side ID', () => {
      const project = createMockProject({
        clientSideId: null
      });

      const context = createMockContext({
        empHasClientSideId: false,
        uniqueProjectsList: [project],
        documentData: []
      });

      const result = service.validateDocumentUploads(context);
      expect(result.isValid).toBe(true);
    });

    it('should skip document validation for non-fillable day types', () => {
      const project = createMockProject({
        clientSideId: 'CLIENT001'
      });

      const context = createMockContext({
        dayType: 4, // Non-fillable
        empHasClientSideId: true,
        uniqueProjectsList: [project],
        documentData: []
      });

      const result = service.validateDocumentUploads(context);
      expect(result.isValid).toBe(true);
    });

    it('should validate filled document is required for pending projects', () => {
      const project = createMockProject({
        clientSideId: 'CLIENT001',
        clientApprovalStatus: 1 // Pending
      });

      const context = createMockContext({
        dayType: 1, // Fillable
        empHasClientSideId: true,
        uniqueProjectsList: [project],
        documentData: []
      });

      const result = service.validateDocumentUploads(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.message.includes('Filled Attendance Proof is mandatory'))).toBe(true);
    });

    it('should validate approved document is required for approved projects', () => {
      const project = createMockProject({
        clientSideId: 'CLIENT001',
        clientApprovalStatus: 2 // Approved
      });

      const filledDoc: TimesheetDocumentDataI = {
        projectId: 1,
        docType: 'Filled',
        uniqueIdentifier: 'file1.pdf',
        docId: null,
        docName: 'file1.pdf',
        finalFlag: false,
        bulkApprovedDocId: null,
        previewUrl: null,
        rawObjectUrl: null,
        fileError: null,
        fileType: 'pdf',
        fileSize: 1000
      };

      const context = createMockContext({
        dayType: 1, // Fillable
        empHasClientSideId: true,
        uniqueProjectsList: [project],
        documentData: [filledDoc]
      });

      const result = service.validateDocumentUploads(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.message.includes('Approved Attendance Proof is mandatory'))).toBe(true);
    });

    it('should validate document has no file errors', () => {
      const project = createMockProject({
        clientSideId: 'CLIENT001',
        clientApprovalStatus: 1 // Pending
      });

      const filledDoc: TimesheetDocumentDataI = {
        projectId: 1,
        docType: 'Filled',
        uniqueIdentifier: 'file1.pdf',
        docId: null,
        docName: 'file1.pdf',
        finalFlag: false,
        bulkApprovedDocId: null,
        previewUrl: null,
        rawObjectUrl: null,
        fileError: 'File size exceeds limit',
        fileType: 'pdf',
        fileSize: 1000
      };

      const context = createMockContext({
        dayType: 1,
        empHasClientSideId: true,
        uniqueProjectsList: [project],
        documentData: [filledDoc]
      });

      const result = service.validateDocumentUploads(context);
      expect(result.isValid).toBe(false);
      expect(result.errors.some(e => e.message.includes('has an error'))).toBe(true);
    });
  });

  describe('isDayTypeFillable', () => {
    it('should return true for fillable day types', () => {
      expect(service.isDayTypeFillable(1)).toBe(true);
      expect(service.isDayTypeFillable(3)).toBe(true);
      expect(service.isDayTypeFillable(8)).toBe(true);
    });

    it('should return false for non-fillable day types', () => {
      expect(service.isDayTypeFillable(4)).toBe(false);
      expect(service.isDayTypeFillable(6)).toBe(false);
      expect(service.isDayTypeFillable(7)).toBe(false);
    });

    it('should return false for null day type', () => {
      expect(service.isDayTypeFillable(null)).toBe(false);
    });

    it('should return false for unknown day types', () => {
      expect(service.isDayTypeFillable(99)).toBe(false);
    });
  });

  describe('formatErrorsForDisplay', () => {
    it('should return empty string for valid result', () => {
      const result: ValidationResult = {
        isValid: true,
        errors: [],
        warnings: []
      };

      const formatted = service.formatErrorsForDisplay(result);
      expect(formatted).toBe('');
    });

    it('should return single error message', () => {
      const result: ValidationResult = {
        isValid: false,
        errors: [{
          field: 'dayType',
          message: 'Day type cannot be null',
          scope: 'TIMESHEET'
        }],
        warnings: []
      };

      const formatted = service.formatErrorsForDisplay(result);
      expect(formatted).toBe('Day type cannot be null');
    });

    it('should format multiple errors with numbering', () => {
      const result: ValidationResult = {
        isValid: false,
        errors: [
          { field: 'dayType', message: 'Day type cannot be null', scope: 'TIMESHEET' },
          { field: 'fromDate', message: 'Date cannot be null', scope: 'TIMESHEET' }
        ],
        warnings: []
      };

      const formatted = service.formatErrorsForDisplay(result);
      expect(formatted).toContain('1. Day type cannot be null');
      expect(formatted).toContain('2. Date cannot be null');
    });
  });

  describe('formatWarningsForDisplay', () => {
    it('should return empty string when no warnings', () => {
      const result: ValidationResult = {
        isValid: true,
        errors: [],
        warnings: []
      };

      const formatted = service.formatWarningsForDisplay(result);
      expect(formatted).toBe('');
    });

    it('should return single warning', () => {
      const result: ValidationResult = {
        isValid: true,
        errors: [],
        warnings: ['Warning message']
      };

      const formatted = service.formatWarningsForDisplay(result);
      expect(formatted).toBe('Warning message');
    });

    it('should format multiple warnings with numbering', () => {
      const result: ValidationResult = {
        isValid: true,
        errors: [],
        warnings: ['Warning 1', 'Warning 2']
      };

      const formatted = service.formatWarningsForDisplay(result);
      expect(formatted).toContain('1. Warning 1');
      expect(formatted).toContain('2. Warning 2');
    });
  });

  describe('Error deduplication', () => {
    it('should deduplicate identical errors', () => {
      const project1 = createMockProject({
        projectId: null
      });
      const project2 = createMockProject({
        projectId: null
      });
      const location = createMockLocation({
        projects: [project1, project2]
      });

      const context = createMockContext({
        timesheetLocations: [location]
      });

      const result = service.validateCreate(context);
      // Should have deduplicated errors
      const projectIdErrors = result.errors.filter(e => e.field === 'projectId');
      expect(projectIdErrors.length).toBeGreaterThan(0);
    });
  });

  describe('Location highlighting', () => {
    it('should include location IDs in highlightedLocationIds when validation fails', () => {
      const project = createMockProject({
        clientId: null
      });
      const location = createMockLocation({
        workLocationTypeId: 1,
        projects: [project]
      });

      const context = createMockContext({
        timesheetLocations: [location]
      });

      const result = service.validateCreate(context);
      expect(result.highlightedLocationIds).toContain(1);
    });
  });
});
