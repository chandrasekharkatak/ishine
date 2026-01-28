import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { EncryptionService } from './EncryptionService';
import { EmployeeTimesheetDTO } from '../models/EmployeeTimesheetDTO';
import { Timesheet } from '../models/timesheet';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class TimesheetNewService {
  private baseUrl: any = environment.baseUrl;

  constructor(
    private http: HttpClient,
    private encryptionService: EncryptionService
  ) {}

  addTimesheetWithClientUpdated(
    employeeTimesheetDTO: EmployeeTimesheetDTO,
    selectedFile: any,
    selectedFile2: any
  ) {
    // const encryptedDto = this.encryptionService.encrypt(JSON.stringify(employeeTimesheetDTO))
    const formData = new FormData();
    formData.append('dto', JSON.stringify(employeeTimesheetDTO));
    formData.append('filledDoc', selectedFile);
    formData.append('approvedDoc', selectedFile2);
    return this.http.post(
      `${this.baseUrl}` + `api/v2/timesheet/addTimesheetWithClientNew`,
      formData
    );
  }

  getAllProjectsByEmpId(empId: number) {
    return this.http.post(
      `${this.baseUrl}` + `api/v2/timesheet/getAllProjectsByEmpId`,
      empId
    );
  }

  getActiveProjectsByEmpId(empId: any): Observable<any> {
    return this.http.post(
      `${this.baseUrl}` +
        `api/v2/timesheet/getActiveProjectsByEmpId?empId=${empId}`,
      null
    );
  }

  getAllDayTypes() {
    return this.http.get(
      `${this.baseUrl}` + `api/v2/master/day-type/getAllActiveDayType`
    );
  }
  getAllMyTimesheetsByEmpId(timesheetObj: Timesheet) {
    return this.http.post(
      `${this.baseUrl}` + `api/v2/timesheet/getAllMyTimesheetsByEmpId`,
      timesheetObj
    );
  }

  getAllWorkLocation() {
    return this.http.get(
      `${this.baseUrl}` +
        `api/v2/master/work-location-type/getAllActiveLocationTypes`
    );
  }
  getAllDSRApprovalStatus() {
    return this.http.get(
      `${this.baseUrl}` +
        `api/v2/master/client-status/getAllActiveStatusForClient`
    );
  }
  getActiveProjectsAndClientSideIdByEmpId(empId: number) {
    return this.http.post(
      `${this.baseUrl}` +
        `api/v2/timesheet/getActiveProjectsAndClientSideIdByEmpId`,
      empId
    );
  }

  getAlreadyFilledTimesheetDatesByEmpId(empId: number) {
    return this.http.post(
      `${this.baseUrl}` +
        `api/v2/timesheet/getAlreadyFilledTimesheetDatesByEmpId`,
      empId
    );
  }

  createTimesheet(
    employeeTimesheetDTO: EmployeeTimesheetDTO,
    selectedFile: File[]
  ) {
    const formData = new FormData();
    formData.append('dto', this.encryptionService.encrypt(JSON.stringify(employeeTimesheetDTO)));
    selectedFile.forEach((file) => {
      formData.append('documents', file, file.name);
    });
    return this.http.post(
      `${this.baseUrl}` + `api/v2/timesheet/create`,
      formData
    );
  }

  getTimesheetDashboardCountForEmployee(
    month: any,
    year: any,
    empId: any,
    isClientDashboard: any,
    selectedBillableTypes: any,
    selectedEmployeeStatus: any,
    clientSideFilter: string
  ) {
    const payload = {
      month: month,
      year: year,
      empId: empId,
      isClientDashboard: isClientDashboard,
      selectedBillableTypes: selectedBillableTypes,
      selectedEmployeeStatus: selectedEmployeeStatus,
      clientSideFilter: clientSideFilter
    };
    return this.http.post(
      `${this.baseUrl}` + `api/v2/timesheet/getTimesheetDashboardCountForEmployee`,
      payload
    );
  }
  bulkApproveTimesheetsByIds1(payload: {
    timesheetIds: number[];
    status: string;
    updatedBy: number;
    rejectReason?: string | null;
  }) {
    return this.http.post<any>(
      `${this.baseUrl}api/bulkApproveTimesheetRequest1`,
      payload
    );
  }

  bulkRejectTimesheetsByIds1(payload: {
    timesheetIds: number[];
    status: string;
    updatedBy: number;
    rejectReason: string;
  }) {
    return this.http.post<any>(
      `${this.baseUrl}api/bulkRejectTimesheetRequest1`,
      payload
    );
  }

  approveRejectProjects(payload: {
    timesheetId: number;
    locationMappingId: number;
    projectIds: number[];
    status: string;
    updatedBy: number;
    rejectReason?: string;
  }) {
    return this.http.post<any>(
      `${this.baseUrl}api/approveRejectProjectTimesheet`,
      payload
    );
  }






}
