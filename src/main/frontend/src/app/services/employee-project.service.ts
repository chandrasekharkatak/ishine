import { Injectable } from '@angular/core';
import { TeamService } from './team.service';
import { first, firstValueFrom } from 'rxjs';
import { RmgTeamMember } from '../models/rmgTeamMember';
import * as moment from 'moment';
import { Project } from '../models/project';
import { ProjectService } from './project.service';
import { AuthenticationService } from './authentication.service';
import { User } from '../models/user';
import { ToastService } from './toast.service';
import { EmployeeProjectTimesheetDto } from '../models/employeeProjectTimesheetDto';
import { ResourceManagementService } from './resource-management.service';
import { RmgResourceRequirement } from '../models/rmgResourceRequirement';
import { PoDetails } from '../models/poDetails';
import { ValidationService } from './validation.service';
import { AppModalService } from '../user-team/resource-management/app-modal.service';
import { GlobalRightDrawerService } from './global-right-drawer.service';

export enum ResultType {
  ALERT = 'ALERT',
  PROJECT_START_DATE_ERROR = 'PROJECT_START_DATE_ERROR',
  PROJECT_START_DATE_UPDATE_CONFIRMATION = 'PROJECT_START_DATE_UPDATE_CONFIRMATION',
  EMPLOYEE_PROJECT_TIMESHEET_CONFLICT = 'EMPLOYEE_PROJECT_TIMESHEET_CONFLICT',
  PROJECT_GAP = 'PROJECT_GAP',
  NO_CONFLICT = 'NO_CONFLICT',
  EMPLOYEE_EXISTING_PROJECT_DETAILS = 'EMPLOYEE_EXISTING_PROJECT_DETAILS',
  DELETE_EMPLOYEE_FROM_EXISTING_PROJECT = 'DELETE_EMPLOYEE_FROM_EXISTING_PROJECT',
  EMPLOYEE_MAPPING_BETWEEN_EXISTING_PROJECT = 'EMPLOYEE_MAPPING_BETWEEN_EXISTING_PROJECT',
  EMPLOYEE_DATE_OF_JOINING_LESS_THAN_MEMBER_START_DATE = 'EMPLOYEE_DATE_OF_JOINING_LESS_THAN_MEMBER_START_DATE',
  SUCCESS = 'SUCCESS',
  ERROR = 'ERROR',
  EMPLOYEE_ALREADY_MAPPED_TO_SAME_PROJECT_DIFFERENT_TEAM = 'EMPLOYEE_ALREADY_MAPPED_TO_SAME_PROJECT_DIFFERENT_TEAM',
  OVERLAPPING_ENTRIES_FOUND_IN_THIS_PROJECT = 'OVERLAPPING_ENTRIES_FOUND_IN_THIS_PROJECT',
  EMPLOYEE_ALREADY_ASSIGNED_TO_SAME_PROJECT_TEAM = 'EMPLOYEE_ALREADY_ASSIGNED_TO_SAME_PROJECT_TEAM',
  FUTURE_ASSIGNMENT_CONFLICT_SAME_PROJECT_TEAM = 'FUTURE_ASSIGNMENT_CONFLICT_SAME_PROJECT_TEAM'
}

export interface AppResult<T = any> {
  success: boolean;        // overall outcome
  type: ResultType;        // what kind of result
  data?: T;                // payload (optional)
  message?: string;        // user-friendly message
  error?: any;             // raw error (optional)
}

@Injectable({
  providedIn: 'root'
})
export class EmployeeProjectService {

  currentUser: User;
  startDateUpdateProjectId: any;

  constructor(
    private teamService: TeamService,
    private readonly projectService: ProjectService,
    private readonly resourceManagementService: ResourceManagementService,
    private readonly authenticationService: AuthenticationService,
    private toastService: ToastService,
    private drawerService: GlobalRightDrawerService,
    private readonly validationService: ValidationService,

    private appModalService: AppModalService
  ) {
    this.listenToModalActions();
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  /**
   * Formats any date-like tokens inside backend messages for display.
   * Converts:
   * - yyyy-MM-dd
   * - yyyy-MM-ddTHH:mm:ss
   * into dd/MM/yyyy.
   */
  formatDisplayMessage(message: any): string {
    if (message === null || message === undefined) {
      return '';
    }
    // If backend sends a Date object (rare), format directly.
    if (message instanceof Date) {
      return moment(message).format('DD/MM/YYYY');
    }
    const str = String(message);
    // Replace yyyy-MM-dd and yyyy-MM-ddTHH:mm:ss patterns.
    return str.replace(/\b(\d{4})-(\d{2})-(\d{2})(?:T\d{2}:\d{2}:\d{2})?\b/g, (_m, y, mm, dd) => `${dd}/${mm}/${y}`);
  }

  async validateEmployeeProjectStartDateChange(member: any, projectData: any): Promise<any> {
    let result: AppResult;
    const selectedDate = this.normalizeDate(member.startDate);
    this.startDateUpdateProjectId = projectData.currentProjectId;

    let rmgMember: RmgTeamMember = new RmgTeamMember();
    rmgMember.empId = member.empId;
    rmgMember.projectId = projectData.currentProjectId;
    rmgMember.projectType = projectData.projectType;
    rmgMember.startDate = selectedDate;
    rmgMember.projectIds = projectData.projectIds;
    rmgMember.teamId = member.teamId;
    rmgMember.etmId = member?.etmId;
    // Optional context and end date support (backward compatible)
    rmgMember.endDate = member?.endDate ? this.normalizeDate(member.endDate) : null;
    rmgMember.validationContext = projectData?.validationContext || null;
    rmgMember.validationSource = projectData?.validationSource || member?.validationSource || null;
    try {
      console.debug('[validateEmployeeProjectStartDateChange] request', {
        empId: rmgMember.empId,
        projectId: rmgMember.projectId,
        teamId: rmgMember.teamId,
        startDate: rmgMember.startDate,
        endDate: rmgMember.endDate,
        validationContext: rmgMember.validationContext,
        validationSource: rmgMember.validationSource
      });
      const response: any = await firstValueFrom(this.teamService.validateEmployeeProjectStartDate(rmgMember));
      console.debug('[validateEmployeeProjectStartDateChange] response', response);
      if (response.serviceStatus === 'Success') {
        // Centralized same project/team validations
        if (response.serviceResponse === 'EMPLOYEE_ALREADY_ASSIGNED_TO_SAME_PROJECT_TEAM') {
          const msg = this.formatDisplayMessage(response.serviceResponse1 || 'Employee is already assigned to this project/team.');
          result = this.failureResult(ResultType.EMPLOYEE_ALREADY_ASSIGNED_TO_SAME_PROJECT_TEAM, msg);
          this.handleResult(result);
          return result;
        }
        if (response.serviceResponse === 'FUTURE_ASSIGNMENT_CONFLICT_SAME_PROJECT_TEAM') {
          const msg = this.formatDisplayMessage(
            response?.serviceResponse2?.validationMessage
            || response.serviceResponse1
            || 'Employee already has a future project/team mapping. Please contact RMG for further assistance.'
          );
          result = this.failureResult(ResultType.FUTURE_ASSIGNMENT_CONFLICT_SAME_PROJECT_TEAM, msg);
          this.handleResult(result);
          return result;
        }
        if (['FUTURE_ASSIGNMENT_CONFLICT_REQUIRE_ENDDATE', 'FUTURE_ASSIGNMENT_CONFLICT_ENDDATE_OVERLAP'].includes(response.serviceResponse)) {
          // RMG-only controlled handling: enable end date input and enforce max end date = futureStart - 1
          const futureStart = response?.serviceResponse2?.conflictingStartDate || response?.serviceResponse2;
          if (futureStart) {
            member.memberMaxEndDate = moment(futureStart).subtract(1, 'day').format('YYYY-MM-DD');
          }
          member.isEndDateVisible = true;
          // Use an existing caller-handled type to keep flows compatible (enables endDate field).
          const msg = this.formatDisplayMessage(response?.serviceResponse2?.validationMessage || response?.serviceResponse1 || '');
          member.validationMessage = msg;
          result = this.failureResult(ResultType.EMPLOYEE_MAPPING_BETWEEN_EXISTING_PROJECT, msg, member);
          return result;
        }
        if (response.serviceResponse === 'OPEN_ASSIGNMENT_CONFLICT_SAME_PROJECT_TEAM') {
          const msg = this.formatDisplayMessage(response.serviceResponse1 || 'Employee has an open assignment in this project/team. Please review existing mapping.');
          result = this.alertResult(msg);
          this.handleResult(result);
          return result;
        }
        if (response.serviceResponse === 'TEAM_ID_REQUIRED_FOR_VALIDATION') {
          const msg = this.formatDisplayMessage(response.serviceResponse1 || 'Team Id is required for validation.');
          result = this.alertResult(msg);
          this.handleResult(result);
          return result;
        }
        if (response.serviceResponse === 'FUTURE_TEAM_ONBOARDING_OVERLAP') {
          const msg = this.formatDisplayMessage(
            response?.serviceResponse2?.validationMessage
            || response.serviceResponse1
            || 'Employee already has a future onboarding in this team. Please update existing scheduled onboarding.'
          );
          result = this.alertResult(msg);
          this.handleResult(result);
          return result;
        }
        if (response.serviceResponse == 'EMPLOYEE_DATE_OF_JOINING_LESS_THAN_MEMBER_START_DATE') {
          const responseMsg = this.formatDisplayMessage(response.serviceResponse2 && response.serviceResponse2 != null ? response.serviceResponse2 : 'Member Start Date must not be earlier than the Employee’s Date of Joining!!');
          result = this.failureResult(ResultType.EMPLOYEE_DATE_OF_JOINING_LESS_THAN_MEMBER_START_DATE, responseMsg);
          this.handleResult(result);
          return result;
        }
        else if (response.serviceResponse == 'PROJECT_START_DATE_LESS_THAN_MEMBER_START_DATE') {
          result = this.failureResult(ResultType.PROJECT_START_DATE_ERROR, '');
          this.handleResult(result);
          return result;
        }
        // #current_team_member_resource_template only: TNM overlap with a future allocation (endDate may be null on serviceResponse2).
        else if (response.serviceResponse === 'CURRENT_TNM_PROJECT_OVERLAPPING'
            && projectData?.validationSource === 'CURRENT_TEAM_TEMPLATE') {
          const raw = response.serviceResponse2;
          const entries = Array.isArray(raw) ? raw : (raw != null ? [raw] : []);
          member.tnmOverlapDetail = entries;

          let boundaryStartRaw: any = null;
          let bestNorm: string | null = null;

          for (const entry of entries) {
            if (!entry?.employeeTeamStartDate) {
              continue;
            }
            const eStartNorm = this.normalizeDate(entry.employeeTeamStartDate);
            if (!eStartNorm) {
              continue;
            }
            // Future mapping strictly after selected start (per product rule).
            if (eStartNorm > selectedDate) {
              if (!bestNorm || eStartNorm < bestNorm) {
                bestNorm = eStartNorm;
                boundaryStartRaw = entry.employeeTeamStartDate;
              }
            }
          }

          if (!boundaryStartRaw && entries.length > 0) {
            for (const entry of entries) {
              if (!entry?.employeeTeamStartDate) {
                continue;
              }
              const eStartNorm = this.normalizeDate(entry.employeeTeamStartDate);
              if (!eStartNorm || eStartNorm < selectedDate) {
                continue;
              }
              const hasEnd = this.validationService.validateNullUndefinedEmptyStringTrim(entry.employeeTeamEndDate);
              const eEndNorm = hasEnd ? this.normalizeDate(entry.employeeTeamEndDate) : null;
              if (!hasEnd || (eEndNorm != null && eEndNorm >= selectedDate)) {
                boundaryStartRaw = entry.employeeTeamStartDate;
                break;
              }
            }
          }

          member.isEndDateVisible = true;
          if (boundaryStartRaw) {
            member.memberMaxEndDate = moment(boundaryStartRaw).subtract(1, 'day').format('YYYY-MM-DD');
          }
          const fsDisp = this.formatDisplayMessage(boundaryStartRaw || '');
          const msg = boundaryStartRaw
            ? `Resource already has a future project allocation starting from ${fsDisp}. Please select an end date before that date.`
            : this.formatDisplayMessage(
                response.serviceResponse1
                || 'This resource overlaps another TNM project assignment. Please select an end date for the current assignment.'
              );
          member.validationMessage = msg;
          result = this.failureResult(ResultType.EMPLOYEE_MAPPING_BETWEEN_EXISTING_PROJECT, msg, member);
          return result;
        }
        else if (['CONFLICTING_TIMESHEET_RECORDS_FOUND', 'OTHER_TNM_PROJECT_OVERLAPPING', 'CURRENT_TNM_PROJECT_OVERLAPPING'].includes(response.serviceResponse)) {
          const entries = response.serviceResponse2 || [];
          let newStartDateEarlierThanAnyExistingProject: boolean = false;

          for (let entry of entries) {
            if (!this.validationService.validateNullUndefinedEmptyStringTrim(entry.employeeTeamStartDate)
              || !this.validationService.validateNullUndefinedEmptyStringTrim(entry.employeeTeamEndDate)) {
              continue;
            }

            if (this.normalizeDate(entry.employeeTeamStartDate) >= selectedDate && this.normalizeDate(entry.employeeTeamEndDate) >= selectedDate) {
              member.memberMaxEndDate = moment(entry.employeeTeamStartDate).subtract(1, 'day').format('YYYY-MM-DD');
              newStartDateEarlierThanAnyExistingProject = true;
              break;
            }
          }

          if (newStartDateEarlierThanAnyExistingProject) {
            result = this.failureResult(ResultType.EMPLOYEE_MAPPING_BETWEEN_EXISTING_PROJECT, '', member);
          } else {
            result = this.failureResult(ResultType.EMPLOYEE_PROJECT_TIMESHEET_CONFLICT, '', { entries: entries, isOverlap: response.serviceResponse !== 'CONFLICTING_TIMESHEET_RECORDS_FOUND', rmgMember: rmgMember });
          }
        }
        else if (response.serviceResponse === 'OVERLAPPING_ENTRIES_FOUND_IN_THIS_PROJECT') {
          const entries = response.serviceResponse2 || [];
          for (let entry of entries) {
            if (!this.validationService.validateNullUndefinedEmptyStringTrim(entry.employeeTeamStartDate)
              || !this.validationService.validateNullUndefinedEmptyStringTrim(entry.employeeTeamEndDate)) {
              continue;
            }

            if (this.normalizeDate(entry.employeeTeamStartDate) >= selectedDate && this.normalizeDate(entry.employeeTeamEndDate) >= selectedDate) {
              member.memberMaxEndDate = moment(entry.employeeTeamStartDate).subtract(1, 'day').format('YYYY-MM-DD');
              break;
            }
          }
          result = this.failureResult(ResultType.OVERLAPPING_ENTRIES_FOUND_IN_THIS_PROJECT, '', member);
        }
        else if (response.serviceResponse === 'GAP_EXISTS') {
          result = this.failureResult(ResultType.PROJECT_GAP, '', response.serviceResponse2);
        }
        else if (response.serviceResponse === 'NO_CONFLICT') {
          result = this.failureResult(ResultType.NO_CONFLICT, '');
        }
      } else {
        result = this.alertResult(response.serviceResponse);
      }
    } catch {
      result = this.alertResult('Something went wrong');
    }

    this.handleResult(result);
    return result;
  }

  async updateProjectStartDate(projectId: any, projectNewStartDate: any) {
    let result: AppResult;
    if (!projectNewStartDate || projectNewStartDate == undefined || projectNewStartDate == null) {
      result = this.alertResult("Kindly provide new Project Start Date!!");
      this.handleResult(result);
      return result;
    }
    let projectObj: Project = new Project();
    projectObj.projectId = projectId;
    projectObj.startDate = moment(this.normalizeDate(projectNewStartDate)).format('YYYY-MM-DD');
    projectObj.updatedBy = this.currentUser.empId;

    try {
      const response: any = await firstValueFrom(this.projectService.updateProjectStartDate(projectObj));
      if (response.serviceStatus == "Success") {
        this.appModalService.close('PROJECT_START_DATE_UPDATE_CONFIRMATION');
        this.appModalService.triggerAction({ actionType: 'PROJECT_START_DATE_UPDATED' });
        result = this.alertResult(response.serviceResponse);
      } else {
        result = this.alertResult(response.serviceResponse || "Something went wrong!");
      }
    } catch {
      result = this.alertResult('Something went wrong');
    }

    this.handleResult(result);
    return result;
  }

  async updateTeamMembersStartDateAndEndDate(employeeObj: EmployeeProjectTimesheetDto, existingEmployeeProjectTimesheetEntries: EmployeeProjectTimesheetDto[], rmgTeamMember: RmgTeamMember) {
    let result: AppResult;
    if (!employeeObj.employeeTeamStartDate || employeeObj.employeeTeamStartDate == undefined || employeeObj.employeeTeamStartDate == null) {
      result = this.alertResult(`Kindly provide a valid Start date!!`);
      this.handleResult(result);
      return result;
    }
    if (!employeeObj.removePermanently && (!employeeObj.employeeTeamEndDate || employeeObj.employeeTeamEndDate == undefined || employeeObj.employeeTeamEndDate == null)) {
      result = this.alertResult(`Kindly provide a valid End date!!`);
      this.handleResult(result);
      return result;
    }

    const selectedStartDate = this.normalizeDate(employeeObj?.employeeTeamStartDate);
    const selectedEndDate = this.normalizeDate(employeeObj?.employeeTeamEndDate);
    const projectStartDate = this.normalizeDate(employeeObj?.projectStartDate);
    const isSelectedProjectTNM = employeeObj.projectType && employeeObj?.projectType?.toLowerCase() === 'tnm';
    
    if (selectedStartDate < projectStartDate) {
      result = this.alertResult("Member Start Date must be after Project Start Date!!");
      this.handleResult(result);
      return result;
    }

    if (!employeeObj.removePermanently && (selectedStartDate > selectedEndDate)) {
      result = this.alertResult("Member End Date must be after Member Start Date!!");
      this.handleResult(result);
      return result;
    }

    for (let i = 0; i < existingEmployeeProjectTimesheetEntries?.length; i++) {
      let tempObj = existingEmployeeProjectTimesheetEntries[i];
      if (employeeObj?.etmId === tempObj?.etmId) {
        continue;
      }
      if (!tempObj.employeeTeamStartDate || tempObj.employeeTeamStartDate == undefined || tempObj.employeeTeamStartDate == null
        || !tempObj.employeeTeamEndDate || tempObj.employeeTeamEndDate == undefined || tempObj.employeeTeamEndDate == null) {
        continue;
      }

      const tempStartDate = this.normalizeDate(tempObj?.employeeTeamStartDate);
      const tempEndDate = this.normalizeDate(tempObj?.employeeTeamEndDate);
      const isTempProjectTNM = tempObj.projectType && tempObj?.projectType?.toLowerCase() === 'tnm';
      const isOverlapping = (isTempProjectTNM || isSelectedProjectTNM) && (selectedStartDate <= tempEndDate) && (selectedEndDate >= tempStartDate);
      if (isOverlapping) {
        result = this.alertResult(`Date range overlaps with another Project assignment (${tempObj.projectName})`);
        this.handleResult(result);
        return result;
      }
    }

    employeeObj.employeeTeamStartDate = this.normalizeDate(employeeObj.employeeTeamStartDate);
    employeeObj.employeeTeamEndDate = this.normalizeDate(employeeObj.employeeTeamEndDate);
    employeeObj.updatedBy = this.currentUser?.empId;
    try {
      const response: any = await firstValueFrom(this.teamService.updateTeamMembersStartDateAndEndDate(employeeObj));
      if (response.serviceStatus === "Success") {
        this.appModalService.close('EMPLOYEE_PROJECT_TIMESHEET_CONFLICT');
        this.toastService.success(response.serviceResponse);
      } else {
        this.toastService.error(response.serviceResponse || "Something went wrong, unable to update the Employee details at the moment!!");
      }

      let projectData = {
        currentProjectId: rmgTeamMember?.projectId,
        projectIds: rmgTeamMember.projectIds,
        projectType: rmgTeamMember.projectType
      };

      this.validateEmployeeProjectStartDateChange(rmgTeamMember, projectData);
    } catch (error) {
      result = this.alertResult("Something went wrong!!");
    }

    this.handleResult(result);
    return result;
  }

  async getResourceRequirementByPoId(poId: any): Promise<any> {
    let result: AppResult;
    if (!poId) {
      result = this.alertResult('Kindly Select a PO!!');
      return result;
    }
    let resourceRequirementList: RmgResourceRequirement[] = [];
    try {
      const response: any = await firstValueFrom(this.resourceManagementService.getResourceRequirementByPoId(poId));
      if (response.serviceStatus === "Success") {
        resourceRequirementList = response.serviceResponse || [];

        resourceRequirementList?.forEach(req => {
          req.isExpired =
            (req.poEndDate && moment(req.poEndDate).format('YYYY-MM-DD') < moment(new Date()).format('YYYY-MM-DD')) ||
            (req.requirementEndDate && moment(req.requirementEndDate).format('YYYY-MM-DD') < moment(new Date()).format('YYYY-MM-DD'));
        });

        resourceRequirementList.sort((a, b) =>
          moment(b?.requirementEndDate || 0).diff(moment(a?.requirementEndDate || 0))
        );

      } else {
        this.toastService.error(response.serviceResponse || "Something went wrong, unable to fetch PO Role!!");
      }
    } catch (error) {
      this.toastService.error("Something went wrong!!");
    }

    result = this.successResult(resourceRequirementList);
    this.handleResult(result);
    return result;
  }

  async getPoDetailsByProjectId(projectId: any): Promise<any> {
    let result: AppResult;
    let poDetailsList: PoDetails[] = [];

    try {
      const response: any = await firstValueFrom(this.resourceManagementService.getActivePoDetailsByProjectId(projectId));
      if (response.serviceStatus === "Success") {
        poDetailsList = response.serviceResponse || [];

        poDetailsList.forEach(po => {
          const poStartDate = this.normalizeDate(po.poStartDate);
          const poEndDate = this.normalizeDate(po.poEndDate);
          if (poStartDate && poStartDate != undefined && poStartDate != null && poEndDate && poEndDate != undefined && poEndDate != null) {
            po.poTitle = po.poNo + ' | PO Start Date - ' + poStartDate + ' | PO End Date - ' + poEndDate;
          } else {
            po.poTitle = po.poNo;
          }
          po.isExpired = (po.poEndDate && moment(po.poEndDate).format('YYYY-MM-DD') < moment(new Date()).format('YYYY-MM-DD'));
        });

        poDetailsList.sort((a, b) =>
          moment(b?.poEndDate || 0).diff(moment(a?.poEndDate || 0))
        );
        result = this.successResult(poDetailsList);
      } else {
        result = this.alertResult(response.serviceResponse || "Something went wrong, unable to fetch PO Details List!!");
      }
    } catch (error) {
      result = this.alertResult("Something went wrong, unable to fetch PO Details List!!");
    }

    this.handleResult(result);
    return result;
  }

  async getEmployeeExistingProjectDetails(empId: any, projectId: any, employee: any, enforceCheck: boolean = false, teamId: any): Promise<any> {
    let result: AppResult;
    try {
      const response: any = await firstValueFrom(this.projectService.getEmployeeExistingProjectDetailsByEmpId(empId, projectId, enforceCheck, teamId));
      if (response?.serviceStatus !== "Success") {
        result = this.alertResult(response?.serviceResponse || "Something went wrong!!");
        this.handleResult(result);
        return result;
      }
      if (response?.serviceStatus === "Success" && response?.serviceResponse === "Employee is already mapped to the team!!") {
        result = this.failureResult(ResultType.EMPLOYEE_ALREADY_MAPPED_TO_SAME_PROJECT_DIFFERENT_TEAM, response?.serviceResponse1, response?.serviceResponse);
        this.handleResult(result);
        return result;
      }
      if (response?.serviceStatus === "Success" && response?.serviceResponse === "Employee Existing Project Details Not found!!") {
        result = this.successResult();
        this.handleResult(result);
        this.appModalService.triggerAction({ actionType: 'GET_ETM_MAX_END_DATE', data: empId });
        return result;
      }

      const employeeExistingProjectDetails = this.mapEmployeeProjectDates(response.serviceResponse);
      employeeExistingProjectDetails.forEach((e) => {
        e.currentProjectId = projectId
      });
      result = this.createResult(true, ResultType.EMPLOYEE_EXISTING_PROJECT_DETAILS, { data: { data: employeeExistingProjectDetails, employmentId: employee.employmentId, name: employee.name } });
    } catch (error) {
      result = this.alertResult("Something went wrong!");
    }
    this.handleResult(result);
    return result;
  }

  async deleteEmployeeProjectResourceMapping(employee: any, employeeProjectEndDate: any, employeeProjectEndDateType: any) {
    let result: AppResult;
    if (!employee.removePermanently && (!employeeProjectEndDate || employeeProjectEndDate == undefined || employeeProjectEndDate == null)) {
      result = this.alertResult("Please provide End date!!");
      this.handleResult(result);
      return result;
    }
    if (!employee.removePermanently && this.normalizeDate(employee.startDate) > this.normalizeDate(employeeProjectEndDate)) {
      result = this.alertResult("Member End date cannot be less then Member Start date!!");
      this.handleResult(result);
      return result;
    }
    if (employee.removePermanently) {
      const flag = await this.validateIfAnyApprovedOrPendingTimesheetExist(employee.projectId, employee.empId, employee.etmId);
      if (!flag) {
        return;
      }
    }

    employee.isCustomDate = employeeProjectEndDateType === 'Custom';
    employee.rescEndDate = this.normalizeDate(employeeProjectEndDate);
    employee.updatedBy = this.currentUser.empId;
    employee.rescRemovedBy = this.currentUser.empId;

    try {
      const response: any = await firstValueFrom(this.projectService.updateEmployeeProjectMappingAsInActive(employee));
      if (response.serviceStatus == "Success") {
        this.drawerService?.close();
        this.toastService.success(response.serviceResponse);
        this.appModalService.close('DELETE_EMPLOYEE_FROM_EXISTING_PROJECT');
        this.getEmployeeExistingProjectDetails(employee.empId, employee.currentProjectId, employee, false, employee.teamId);
      } else {
        this.toastService.error(response.serviceResponse);
      }
    } catch (error) {
      this.toastService.error('Something went wrong!!');
    }
  }

  async validateIfAnyApprovedOrPendingTimesheetExist(projectId: any, empId: any, employeeTeamMapId: any): Promise<boolean> {
    const response: any = await firstValueFrom(this.teamService.validateIfAnyApprovedOrPendingTimesheetExist(projectId, empId, employeeTeamMapId));
    if (response.serviceStatus == "Success") {
      if (response.serviceResponse != 0) {
        this.appModalService.open('ALERT', 'ALERT', response.serviceResponse1);
        return false;
      }
    } else {
      this.appModalService.open('ALERT', 'ALERT', response.serviceResponse || "Something went wrong, unable to fetch timesheet filled count. Please try again later!!");
      return false;
    }
    return true;
  }

  // Helpers Start
  mapEmployeeProjectDates(projects: any[]): any[] {
    return this.isValidList(projects) ? projects?.map(project => ({
      ...project,
      startDate: project.startDate ? this.normalizeDate(project.startDate) : null,
      updatedOn: project.updatedOn ? this.normalizeDate(project.updatedOn) : null
    })) : [];
  }

  isValidList(list: any) {
    return Array.isArray(list) && this.validationService.validateNullUndefinedEmptyList(list);
  }

  normalizeDate(dateInput: any) {
    if (!dateInput) {
      return null;
    }
    const date = new Date(dateInput);
    if (isNaN(date.getTime())) {
      return null;
    }
    return moment(dateInput).startOf('day').format('YYYY-MM-DDTHH:mm:ss');
  }

  successResult<T>(data?: T): AppResult<T> {
    return { success: true, type: ResultType.SUCCESS, data };
  }

  failureResult<T>(type: ResultType, message?: string, data?: T): AppResult<T> {
    return { success: false, type, message, data };
  }

  alertResult(message: string): AppResult {
    return { success: false, type: ResultType.ALERT, message };
  }

  errorResult(message: string, error?: any): AppResult {
    return { success: false, type: ResultType.ERROR, message, error };
  }

  createResult<T>(success: boolean, type: ResultType, options?: { data?: T; message?: string; error?: any; }): AppResult<T> {
    return { success, type, ...options };
  }

  private handleResult(result: AppResult) {
    if (!result) {
      return;
    }

    console.log(result);
    switch (result.type) {
      case ResultType.PROJECT_START_DATE_ERROR:
        this.appModalService.open('PROJECT_START_DATE_ERROR', 'PROJECT_START_DATE_ERROR');
        break;

      case ResultType.PROJECT_START_DATE_UPDATE_CONFIRMATION:
        this.appModalService.open('PROJECT_START_DATE_UPDATE_CONFIRMATION', 'PROJECT_START_DATE_UPDATE_CONFIRMATION', result.data);
        break;

      case ResultType.EMPLOYEE_PROJECT_TIMESHEET_CONFLICT:
        this.appModalService.open('EMPLOYEE_PROJECT_TIMESHEET_CONFLICT', 'EMPLOYEE_PROJECT_TIMESHEET_CONFLICT', result.data);
        break;

      case ResultType.PROJECT_GAP:
        this.appModalService.open('PROJECT_GAP', 'PROJECT_GAP', result.data);
        break;

      case ResultType.ALERT:
        this.appModalService.open('ALERT', 'ALERT', this.formatDisplayMessage(result.message));
        break;

      case ResultType.EMPLOYEE_EXISTING_PROJECT_DETAILS:
        this.appModalService.open('EMPLOYEE_EXISTING_PROJECT_DETAILS', 'EMPLOYEE_EXISTING_PROJECT_DETAILS', result.data);
        break;

      case ResultType.EMPLOYEE_DATE_OF_JOINING_LESS_THAN_MEMBER_START_DATE:
        this.appModalService.open('ALERT', 'ALERT', this.formatDisplayMessage(result.message));
        break;

      case ResultType.EMPLOYEE_ALREADY_MAPPED_TO_SAME_PROJECT_DIFFERENT_TEAM:
        this.appModalService.open('ALERT', 'ALERT', this.formatDisplayMessage(result.message));
        break;

      case ResultType.EMPLOYEE_ALREADY_ASSIGNED_TO_SAME_PROJECT_TEAM:
        this.appModalService.open('ALERT', 'ALERT', this.formatDisplayMessage(result.message || 'Employee already has an active assignment in this project/team.'));
        break;

      case ResultType.FUTURE_ASSIGNMENT_CONFLICT_SAME_PROJECT_TEAM:
        this.appModalService.open('ALERT', 'ALERT', this.formatDisplayMessage(result.message || 'Employee already has a future assignment in this project/team. Please contact RMG for further assistance.'));
        break;

      case ResultType.DELETE_EMPLOYEE_FROM_EXISTING_PROJECT:
        break;

    }
  }

  listenToModalActions() {
    this.appModalService.rmgAction$.subscribe(action => {
      console.log(action)
      const data = action.data;
      switch (action.actionType) {
        case 'OPEN_UPDATE_PROJECT_START_DATE_MODAL':
          this.appModalService.open('PROJECT_START_DATE_UPDATE_CONFIRMATION', 'PROJECT_START_DATE_UPDATE_CONFIRMATION', this.startDateUpdateProjectId);
          break;

        case 'UPDATE_PROJECT_START_DATE':
          this.updateProjectStartDate(data.projectId, data.projectNewStartDate);
          break;

        case 'UPDATE_EMPLOYEE_PROJECT_START_AND_END_DATE':
          this.updateTeamMembersStartDateAndEndDate(data.employee, data.existingEmpEntries, data.rmgTeamMember);
          break;

        case 'OPEN_DELETE_EMPLOYEE_FROM_EXISTING_PROJECT_MODAL':
          this.appModalService.open('DELETE_EMPLOYEE_FROM_EXISTING_PROJECT', 'DELETE_EMPLOYEE_FROM_EXISTING_PROJECT', data);
          break;

        case 'DELETE_EMPLOYEE_FROM_EXISTING_PROJECT':
          this.deleteEmployeeProjectResourceMapping(data.employee, data.employeeProjectEndDate, data.employeeProjectEndDateType);
          break;

      }
    });
  }
  // Helpers End
}
