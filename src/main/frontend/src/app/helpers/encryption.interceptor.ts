import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import * as CryptoJS from 'crypto-js';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { v4 as uuidv4 } from 'uuid';

@Injectable()
export class EncryptionInterceptor implements HttpInterceptor {
  private readonly KEY1 = CryptoJS.enc.Utf8.parse('msoe837%)ks&!6eb');
  private readonly KEY2 = CryptoJS.enc.Utf8.parse('p10Cu&@m3idh9so5');
  private readonly CUSTOM_HEADER = 'X-TRACE-MAP';
   private readonly SESSION_STORAGE_KEY = 'TRACE_MAP_STORAGE';

  private readonly SECURE_ENDPOINTS: string[] = [
    '/api/authenticateUser',
   '/api/authenticateUserWithOTP', 
   '/api/checkUserSession', 
   '/api/logoutUser', 
   '/api/checkEmailWhenForgotPassword', 
   '/api/checkOTPWhenForgotPassword', 
   '/api/resendOTP', 
   '/api/getAllEmployees', 
   '/api/getAllEmployeesByRole',
   '/api/getEmployeeAppreciationByEmpId', 
   '/api/getTeamAppreciationByEmpId', 
   '/api/getEmployeeByEmpId', 
   '/api/getAllEmployeesForPerformance', 
   '/api/getAllEmployeesBirthDayToday', 
   '/api/empdetails',
   '/api/getAllManagers', 
   '/api/getEmployeeProfileCompletion', 
   '/api/updateTimesheetLockCheck', 
   '/api/getEmployeeBasicInfo', 
   '/api/getAllEmployeeInfo', 
   '/api/unlockAllTimesheet', 
   '/api/getProjectsByDepartmentName', 
   '/api/getTeamMemberByTeamName', 
   '/api/getManagerList', 
   '/api/setManagerToNewManager', 
   '/api/isEmployeeOnBench', 
   '/api/mapLeavesAndCompOffToNewManager', 
   '/api/getReporteesListByManagerId', 
   '/api/getReporteesListByReportingManagerId', 
   '/api/setReportingManagerToNewManager', 
   '/api/updateDefaultProject', 
   '/api/getExpiredPo', 
   '/api/sendExpiredPoEmail', 
   '/api/getRewardsAndAppreciationCount', 
   '/api/getAllEmployeesWorkAnniversaryToday', 
   '/api/getProjectsAccToDepartmentAndProjectType', 
   '/api/getAllEmployeesBasedOnUserLogined', 
   '/api/fetchInactivePOCounts', 
   '/api/fetchInactivePOListOfEmployee', 
   '/api/fetchactivePOCounts', 
   '/api/fetchActivePOListOfEmployee', 
   '/api/getAllClients', 
   '/api/getAllProjects', 
   '/api/createProject', 
   '/api/getProjectByProjectId', 
   '/api/updateProject', 
   '/api/deleteProject', 
   '/api/syncPoProjectAndTeam', 
   '/api/getSyncableProject', 
   '/api/checkProjectName', 
   '/api/getAllMyProjectByEmpId', 
   '/api/poProjectTimesheetSync', 
   '/api/getEmployeeProjectReport', 
   '/api/getResourceRequirementFromPoPortal', 
   '/api/getProjectWithCliendSideID', 
   '/api/getCompletedFixedCostProjects', 
   '/api/getFixedCostCount', 
   '/api/getClientAndProjectReport', 
   '/api/getClientAndProjectReportDataList', 
   '/api/createDraftProjectInfo', 
   '/api/getTeamListByProjectName', 
   '/api/alreadyCreatedTeam', 
   '/api/getPendingForApprovalProject', 
   '/api/approvePendingProject', 
   '/api/rejectPendingProject', 
   '/api/sendProjectApproval', 
   '/api/sendProjectInfoToPoPortal', 
   '/api/bulkSyncProject', 
   '/api/approveProject',
    '/api/getInternalProject', 
    '/api/getExistingProjectsAndTeamsByEmployee', 
    '/api/updateProjectResourceAsInActive', 
    '/api/deleteTeamByTeamId', 
    '/api/getProjectInfo', 
    '/api/getPoProjectInfo', 
    '/api/getTeamInfo', 
    '/api/syncPoProjectDetailsByProjectId', 
    '/api/getPoProjectDetailsForPoProjects', 
    '/api/sendEmailNotificationToBDTeam', 
    '/api/getEmployeeByNameAndEmpld', 
    '/api/getAllExpiredTNMProject', 
    '/api/combinedPOINTERNALCountList', 
    '/api/combinedPOINTERNALDataList', 
    '/api/deleteTeamsByIdsBulk', 
    '/api/updateProjectResourcesAsInActiveBulk', 
    '/api/updateProjectStartAndEndDate', 
    '/api/completionDateOfProject', 
    '/api/rbacInternalProjects', 
    '/api/rbacUnfilledTimesheetsProjects', 
    '/api/rbacShankhProjects', 
    '/api/rbacAllShankhInternalProjects', 
    '/api/employeesMappedProjectsDepartmentWise', 
    '/api/rbacBothShankhInternal', 
    '/api/projectLessEmployees', 
    '/api/getEmployessWithoutBillable', 
    '/api/exceptionEmployeeReport', 
    '/api/projectLessEmployeesDepartmentWise', 
    '/api/totalEmployeeCount', 
    '/api/totalEmployeeCountInDepartments', 
    '/api/getResourceRequirementByPoProjectId', 
    '/api/getEmployeeInformation', 
    '/api/poCrudOperationsInIshine', 
    '/api/poDump', 
    '/api/fillDepartmentforAllProjectsInIshine', 
    '/api/getPreviousDefaultProjectDetails', 
    '/api/setDefaultProjectUpdateBillable', 
    '/api/getProjectDetailsForBulkDefaultUpdate', 
    '/api/getEmployeeInformationBulk', 
    '/api/setProjectMappingAndDefaultProject', 
    '/api/getEmployeeInformationForDefaultProject', 
    '/api/getAllResourceRequirementForProject', 
    '/api/getBenchEmployeeMoreThan30Days', 
    '/api/getProjectTimesheetSummary', 
    '/api/getProjectStatusByPoProjectId', 
    '/api/getDeptsByRole', 
    '/api/getDeptsByUser', 
    '/api/deleteTempProjects', 
    '/api/updateHasClientSideId', 
    '/api/getActiveProjectList', 
    '/api/liftAndShiftTeams', 
    '/api/fetchHasClientSideId', 
    '/api/getProjectStructure', 
    '/api/handleTeamsAsPerLinkedPo', 
    '/api/sendTimesheetDetailsToShankh'];

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.isSecureEndpoint(req.url)) {
      return next.handle(req);
    }

    const traceId = uuidv4();

    // --- Get existing traceMap from sessionStorage ---
    const storedTraceMap: Record<string, string> = JSON.parse(
      sessionStorage.getItem(this.SESSION_STORAGE_KEY) || '{}'
    );

    // --- Add current request ---
    storedTraceMap[req.url] = traceId;
    sessionStorage.setItem(this.SESSION_STORAGE_KEY, JSON.stringify(storedTraceMap));

    const encryptedBody = req.body ? this.encrypt(JSON.stringify(req.body), traceId) : null;
    const encryptedHeader = this.encrypt(JSON.stringify({ [req.url]: traceId }), null);

    const encryptedReq = req.clone({
      body: encryptedBody ? { encryptedData: encryptedBody } : null,
      headers: req.headers.set(this.CUSTOM_HEADER, JSON.stringify({ encryptedData: encryptedHeader }))
    });

    return next.handle(encryptedReq).pipe(
  map(event => {
    if (!(event instanceof HttpResponse)) return event; // only process HttpResponse

    try {
      if (!event.body) throw new Error('Empty response body');

      // --- 1. Verify header trace map ---
      const headerMapStr = event.headers.get(this.CUSTOM_HEADER);
      if (!headerMapStr) throw new Error(`Missing custom header for trace verification: ${req.url}`);

      let headerJson: any;
      try {
        headerJson = JSON.parse(headerMapStr);
      } catch {
        throw new Error('Invalid X-TRACE-MAP header format');
      }

      const headerEncrypted = headerJson.encryptedData;
      if (!headerEncrypted) throw new Error('Missing encryptedData in header');

      const decryptedMap: Record<string, string> = JSON.parse(this.decrypt(headerEncrypted));
      const storedMap: Record<string, string> = JSON.parse(sessionStorage.getItem(this.SESSION_STORAGE_KEY) || '{}');

      if (!this.isObjectEqual(decryptedMap, { [req.url]: storedMap[req.url] })) {
        delete storedMap[req.url];
        sessionStorage.setItem(this.SESSION_STORAGE_KEY, JSON.stringify(storedMap));
        throw new Error(`Trace map mismatch! Potential tampering detected: ${req.url}`);
      }

      const expectedTraceId = decryptedMap[req.url];
      if (!expectedTraceId) throw new Error(`Trace map does not contain entry for API: ${req.url}`);

      // --- 2. Decrypt response body ---
      let responseBodyObj: any = event.body;
      if (typeof responseBodyObj === 'string') {
        try {
          responseBodyObj = JSON.parse(responseBodyObj);
        } catch {
          throw new Error('Response is not valid JSON');
        }
      }

      const responseEncrypted = responseBodyObj.encryptedData;
      if (!responseEncrypted) throw new Error('Missing encryptedData in response');

      const decryptedResponse = this.decrypt(responseEncrypted);
      console.log("decryptedResponse", decryptedResponse);
      const lastPipe = decryptedResponse.lastIndexOf('|');
      if (lastPipe < 0) throw new Error('Invalid response format, missing traceId');

      const responseBody = decryptedResponse.substring(0, lastPipe);
      const responseTraceId = decryptedResponse.substring(lastPipe + 1);

      if (responseTraceId !== expectedTraceId) throw new Error('TraceId mismatch! Potential tampering detected.');

      let parsedBody: any;
      try {
        parsedBody = JSON.parse(responseBody);
      } catch {
        parsedBody = responseBody;
      }
       // --- Remove verified traceId ---
      delete storedMap[req.url];
      sessionStorage.setItem(this.SESSION_STORAGE_KEY, JSON.stringify(storedMap));

      return event.clone({ body: parsedBody });

    } catch (error) {
      // --- Any error stops the response ---
      sessionStorage.removeItem(this.SESSION_STORAGE_KEY); // clear all session data if needed
      throw error;
    }
  })
);
  }

  private isSecureEndpoint(url: string): boolean {
    const path = new URL(url, window.location.origin).pathname;
    return this.SECURE_ENDPOINTS.some(ep => ep === path);
  }

  private encrypt(plainText: string, traceId: string | null): string {
    const salted = traceId ? `${plainText}|${traceId}` : plainText;
    return CryptoJS.AES.encrypt(
      CryptoJS.enc.Utf8.parse(salted),
      this.KEY1,
      { iv: this.KEY2, mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7 }
    ).toString();
  }

  private decrypt(cipherText: string): string {
    return CryptoJS.AES.decrypt(cipherText, this.KEY1, {
      iv: this.KEY2,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7
    }).toString(CryptoJS.enc.Utf8);
  }

  private isObjectEqual(obj1: Record<string, string>, obj2: Record<string, string>): boolean {
    const keys1 = Object.keys(obj1);
    const keys2 = Object.keys(obj2);
    if (keys1.length !== keys2.length) return false;
    return keys1.every(key => obj2.hasOwnProperty(key) && obj1[key] === obj2[key]);
  }
}
