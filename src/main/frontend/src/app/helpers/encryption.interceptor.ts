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

@Injectable()
export class EncryptionInterceptor implements HttpInterceptor {
  private readonly KEY1 = CryptoJS.enc.Utf8.parse('msoe837%)ks&!6eb');
  private readonly KEY2 = CryptoJS.enc.Utf8.parse('p10Cu&@m3idh9so5');

  private readonly SECURE_ENDPOINTS: string[] = [
    '/api/authenticateUser',
    '/api/authenticateUserWithOTP',
    '/api/checkUserSession',
    '/api/logoutUser',
    '/api/checkEmailWhenForgotPassword',
    '/api/checkOTPWhenForgotPassword',
    '/api/resendOTP',
    '/api/getAllEmployees',
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
       '/api/getClientAndProjectReportDataList' ,
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
       '/api/sendTimesheetDetailsToShankh'

  ];

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let encryptedReq = req;

    // Encrypt request body
    if (this.isSecureEndpoint(req.url) && req.body) {
      const salt = Date.now();
      const encryptedData = this.encrypt(JSON.stringify(req.body), salt);

      encryptedReq = req.clone({
        body: { encryptedData } // salt is inside the encrypted data now
      });
    }

    return next.handle(encryptedReq).pipe(
      map(event => {
        if (event instanceof HttpResponse && event.body && this.isSecureEndpoint(req.url)) {
          try {
            const body = typeof event.body === 'string' ? JSON.parse(event.body) : event.body;
            const encrypted = body.encryptedData;
            if (!encrypted) {
              console.warn('Missing encryptedData in response');
              return event;
            }

            const decrypted = this.decrypt(encrypted);
            console.log('Decrypted raw string:', decrypted);

            // Extract plaintext + salt
            const lastPipeIndex = decrypted.lastIndexOf('|');
            if (lastPipeIndex < 0) throw new Error('Invalid decrypted format');

            const cleanDecrypted = decrypted.substring(0, lastPipeIndex);
            const salt = parseInt(decrypted.substring(lastPipeIndex + 1), 10);
            const currentTs = Date.now();

            // Validate salt
            if (salt > currentTs) throw new Error(`Future timestamp detected`);
            if (currentTs - salt > 10000) throw new Error(`Response expired (${currentTs - salt}ms old)`);

            let parsedBody: any;
            try {
              parsedBody = JSON.parse(cleanDecrypted);
            } catch {
              parsedBody = cleanDecrypted;
            }

            return event.clone({ body: parsedBody });
          } catch (e) {
            console.error('Response decryption failed:', e);
            return event;
          }
        }
        return event;
      })
    );
  }

  private isSecureEndpoint(url: string): boolean {
    const cleanedUrl = new URL(url, window.location.origin).pathname;
    return this.SECURE_ENDPOINTS.includes(cleanedUrl);
  }

  private encrypt(plainText: string, salt: number): string {
    const saltedPlainText = plainText + '|' + salt;
    const encrypted = CryptoJS.AES.encrypt(
      CryptoJS.enc.Utf8.parse(saltedPlainText),
      this.KEY1,
      {
        keySize: 128 / 8,
        iv: this.KEY2,
        mode: CryptoJS.mode.CBC,
        padding: CryptoJS.pad.Pkcs7
      }
    );
    return encrypted.toString();
  }

  private decrypt(cipherText: string): string {
    const decrypted = CryptoJS.AES.decrypt(cipherText, this.KEY1, {
      keySize: 128 / 8,
      iv: this.KEY2,
      mode: CryptoJS.mode.CBC,
      padding: CryptoJS.pad.Pkcs7
    });
    return decrypted.toString(CryptoJS.enc.Utf8);
  }
}
