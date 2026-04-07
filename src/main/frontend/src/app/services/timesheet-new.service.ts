import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { EncryptionService } from './EncryptionService';
import { EmployeeTimesheetDTO } from '../models/EmployeeTimesheetDTO';
import { Timesheet } from '../models/timesheet';
import { Observable } from 'rxjs';

/**
 * Payload for approving via bulkApproveTimesheetRequest1.
 * Client-approval rules are enforced server-side using DB document rows only; do not send documentDetails.
 */
export interface BulkApproveTimesheetRequestPayload {
  timesheetIds: number[];
  status: string;
  updatedBy: number;
  rmId: number;
  confirmNightShift?: boolean;
}

/** Bulk reject path (rejectMode BULK) for the same endpoint; distinct from approve payload. */
export interface BulkProcessRejectPayload {
  timesheetIds: number[];
  status: string;
  updatedBy: number;
  rmId: number;
  rejectMode: string;
  rejectionReasonId: number[];
  rejectRemark: string;
}

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

  getMyReporteesApprovedTimesheets(timesheetObj: Timesheet) {
    return this.http.post(`${this.baseUrl}` + `api/getMyReporteesApprovedTimesheets`, timesheetObj);
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

  /**
   * Lightweight API: fetch only header-level timesheet data (id, empId, date, dayTypeId, status)
   * Used for date picker constraints instead of full hierarchical timesheet data.
   */
  getTimesheetMetadataByEmpId(timesheetObj: Timesheet) {
    return this.http.post(
      `${this.baseUrl}` + `api/v2/timesheet/getTimesheetMetadataByEmpId`,
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
    if (selectedFile && selectedFile.length > 0) {
      selectedFile.forEach((file) => {
        if (file) {
          formData.append('documents', file, file.name);
        }
      });
    }
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
  bulkApproveTimesheetsByIds1(payload: BulkApproveTimesheetRequestPayload) {
    return this.http.post<any>(
      `${this.baseUrl}api/bulkApproveTimesheetRequest1`,
      payload
    );
  }

  // bulkRejectTimesheetsByIds1(payload: {
  //   timesheetIds: number[];
  //   projectIds: number[];
  //   status: string;
  //   updatedBy: number;
  //   rejectReason: string,
  // rejectRemark: string
  // }) {
  //   return this.http.post<any>(
  //     `${this.baseUrl}api/bulkApproveTimesheetRequest1`,
  //     payload
  //   );
  // }
  bulkRejectTimesheetsByIds1(payload: {
    timesheetIds: number[];
    status: string;
    rmId: number;
    updatedBy: number;
    projectRejections: {
      projectIds: number[];
      rejectionIds: number[];
      rejectRemark: string;
    }[];
  }) {
    return this.http.post<any>(
      `${this.baseUrl}api/bulkApproveTimesheetRequest1`,
      payload
    );
  }

  processBulkTimesheets(payload: BulkProcessRejectPayload) {
    return this.http.post<any>(
      `${this.baseUrl}api/bulkApproveTimesheetRequest1`,
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






  /**
   * Update existing timesheet
   * @param employeeTimesheetDTO - Timesheet data with timesheetId
   * @param selectedFile - Array of new document files to upload
   */
  updateTimesheet(
    employeeTimesheetDTO: EmployeeTimesheetDTO,
    selectedFile: File[]
  ): Observable<any> {
    // Extract timesheetId from DTO (required by backend)
    const timesheetId = employeeTimesheetDTO.timesheetId;

    if (!timesheetId) {
      throw new Error('Timesheet ID is required for update');
    }

    const formData = new FormData();
    formData.append('dto', this.encryptionService.encrypt(JSON.stringify(employeeTimesheetDTO)));

    // Append documents if provided
    if (selectedFile && selectedFile.length > 0) {
      selectedFile.forEach((file) => {
        if (file) {
          formData.append('documents', file, file.name);
        }
      });
    }

    // Use PUT method with timesheetId as query parameter
    return this.http.put(
      `${this.baseUrl}api/v2/timesheet/update`,
      formData
    );
  }

  /**
   * Get timesheet by ID for editing
   * @param timesheetId - ID of timesheet to retrieve
   */
  getTimesheetById(timesheetId: number): Observable<any> {
    return this.http.get(
      `${this.baseUrl}api/v2/timesheet/${timesheetId}`
    );
  }

  /**
   * Get autofill template for last working-day timesheet before a target date.
   * @param payload - { empId: number; date: string (YYYY-MM-DD) }
   */
  getAutofillTimesheet(payload: { empId: number; date: string }): Observable<any> {
    return this.http.post(
      `${this.baseUrl}api/v2/timesheet/autofill`,
      payload
    );
  }

  /**
   * Get document by ID for preview (returns blob)
   * Uses EmployeeTimesheetControllerNew.getDocumentDataByDocId
   * @param docId - Document ID
   * @param approvedDocType - true for Approved (FinalDocumentNew), false for Filled (TimesheetDocumentDetailsNew)
   */
  getDocumentDataByDocId(docId: number, approvedDocType: boolean): Observable<Blob> {
    const url = `${this.baseUrl}api/v2/timesheet/getDocumentDataByDocId?docId=${docId}&approvedDocType=${approvedDocType}`;
    return this.http.get(url, { responseType: 'blob' });
  }

  /**
   * Get document by ID for preview (returns blob) - alternate path for timesheet-form
   * @param docId - Document ID
   * @param approvedDocType - true for Approved, false/omit for Filled
   */
  getDocumentById(docId: number, approvedDocType?: boolean): Observable<Blob> {
    const approved = approvedDocType ?? false;
    return this.getDocumentDataByDocId(docId, approved);
  }
  getMyReporteesTimesheetRequestsCount(payload: any) {
    return this.http.post(
      `${this.baseUrl}api/getTimesheetStatusCountByManager`,
      payload
    );
  }

  getMyReporteesTimesheetRequests(payload:any){
    return this.http.post(
      this.baseUrl + 'api/getMyReporteesTimesheetRequests',
      payload
    );
  }

  checkEditAllowed(payload: { timesheetId: number; date: string }) {
    return this.http.post<boolean>(
    `${this.baseUrl}api/v2/timesheet/checkEditAllowed`,
    payload
  );
  }

  getLastFilledLocationIdForProjectAndEmp(projectId: number, empId: number): Observable<any> {
    
    // const params = new HttpParams()
    //   .set('empId', empId)
    //   .set('projectId', projectId);
    const body = { empId, projectId };

    return this.http.post(
      `${this.baseUrl}api/v2/timesheet/getMyLastFilledLocationIdForProjectAndEmp`, body
     
    );
  }

  fetchDeptBaseProjectAndClientRelatedDataForEmployee(empId: number): Observable<any> {
    
    const params = new HttpParams()
      .set('empId', empId)

    return this.http.get(
      `${this.baseUrl}api/v2/timesheet/fetchDeptBaseProjectAndClientRelatedDataForEmployee`,
      { params }
    );
  }
  getProjectListForDateAndEmpId(payload: any): Observable<any> {
      return this.http.post(`${this.baseUrl}api/v2/timesheet/getProjectListForDateAndEmpId`, payload);
}

getRejectionDetailsWithProjectsByTimesheetId(timesheetId:number):Observable<any> {
  return this.http.post(`${this.baseUrl}api/v2/timesheet/getRejectionDetailsWithProjectsByTimesheetId`, timesheetId);
}
}
