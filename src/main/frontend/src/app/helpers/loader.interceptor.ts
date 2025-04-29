import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest,
  HttpResponse
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from 'src/environments/environment';
import { LoaderService } from '../services/loader.service';



@Injectable()
export class LoaderInterceptor implements HttpInterceptor {

  private baseUrl:any = environment.baseUrl;
  startDate:any;

  URL_whiteList = [

    `${this.baseUrl}` + `api/authenticateUser`,
    `${this.baseUrl}` + `api/authenticateUserWithOTP`,
    `${this.baseUrl}`+`api/upload/designationBulkUpload`,
    `${this.baseUrl}`+`api/employeeBulkUpload`,
    `${this.baseUrl}` + `api/createDepartment`,
    `${this.baseUrl}` + `api/updateDepartment`,
    `${this.baseUrl}` + `api/deleteDepartment`,
    `${this.baseUrl}` + `api/getAllDepartments`,
    `${this.baseUrl}` + `api/updateDepartmentHolidayMappings`,

    `${this.baseUrl}` + `api/createEmployee`,
    `${this.baseUrl}` + `api/updateEmployeeByEmpId`,
    `${this.baseUrl}` + `api/deleteEmployeeByEmpId`,
    `${this.baseUrl}` + `api/getAllEmployees`,
    `${this.baseUrl}` + `api/getEmployeeByEmpId`,
    `${this.baseUrl}` + `api/updateEmployeeProfileByEmpId`,
    `${this.baseUrl}` + `api/previewImage`,
    `${this.baseUrl}` + `api/uploadImage`,
    `${this.baseUrl}` + `api/createDraftEmployee`,
    `${this.baseUrl}` + `api/updateDraftEmployeeById`,
    `${this.baseUrl}` + `api/deleteDraftEmployeeById`,
    `${this.baseUrl}` + `api/getAllDraftEmployees`,

    `${this.baseUrl}` + `api/addHoliday`,
    `${this.baseUrl}` + `api/updateHoliday`,
    `${this.baseUrl}` + `api/getAllHolidays`,
    `${this.baseUrl}` + `api/getHolidayListByDeptId`,

    `${this.baseUrl}` + `api/createJobRole`,
    `${this.baseUrl}` + `api/updateJobRole`,
    `${this.baseUrl}` + `api/deleteJobRole`,
    `${this.baseUrl}` + `api/getAllJobRole`,

    `${this.baseUrl}` + `api/applyLeave`,
    `${this.baseUrl}` + `api/updateLeaveStatus`,
    `${this.baseUrl}` + `api/getAllMyLeaveApplicationsByEmpId`,
    `${this.baseUrl}` + `api/getAllMyTeamsPendingLeaveApplicationsByManagerId`,
    `${this.baseUrl}` + `api/getLeaveLogsByEmpId`,
    `${this.baseUrl}` + `api/getMyLeaveBalancesByEmpId`,
    `${this.baseUrl}` + `api/updateLeavesByEmpId`,
    `${this.baseUrl}` + `api/createLeaveType`,
    `${this.baseUrl}` + `api/updateLeaveType`,
    `${this.baseUrl}` + `api/getAllLeaveTypes`,

    `${this.baseUrl}` + `api/getAllCompOffReasons`,
    `${this.baseUrl}` + `api/applyForCompOff`,
    `${this.baseUrl}` + `api/updateCompOffById`,
    `${this.baseUrl}` + `api/getAllCompOffRequestsByEmpId`,
    `${this.baseUrl}` + `api/getPendingCompOffRequestsByManagerId`,

    `${this.baseUrl}` + `api/addTimesheet`,
    `${this.baseUrl}` + `api/updateTimesheet`,
    `${this.baseUrl}` + `api/getAllProjectsByEmpId`,
    `${this.baseUrl}` + `api/getAllActivitiesByProjectIdandEmpId`,
    `${this.baseUrl}` + `api/getAllMyTimesheetsByEmpId`,
    `${this.baseUrl}` + `api/getAllMyTeamTimesheets`,
    `${this.baseUrl}` + `api/getAllMyActivitiesByTimesheetId`,
    `${this.baseUrl}` + `api/getMyReporteesTimesheetRequests`,
    `${this.baseUrl}` + `api/getMyReporteesApprovedTimesheets`,
    `${this.baseUrl}` + `api/updateTimesheetRequestById`,
    `${this.baseUrl}` + `api/timesheetReport`,
    `${this.baseUrl}` + `api/bulkApproveTimesheetRequest`,
    `${this.baseUrl}` + `api/bulkRejectTimesheetRequest`,

    `${this.baseUrl}` + `api/getSubfeaturesByJobRoleId`,
    `${this.baseUrl}` + `api/getAllSubFeatures`,
    `${this.baseUrl}` + `api/updateRoleFeatureMapping`,

    `${this.baseUrl}` + `api/createUserType`,
    `${this.baseUrl}` + `api/getAllUserTypes`,
    `${this.baseUrl}` + `api/updateCrudMappingsByMapId`,
    `${this.baseUrl}` + `api/getUserTypeCrudMappingsByUserTypeId`,
    `${this.baseUrl}` + `api/getAllUserTypeCrudMappings`,
    `${this.baseUrl}` + `api/changeLeaveTypeMapping`,

    `${this.baseUrl}` + `api/customQueryForLeaveReport`,
    `${this.baseUrl}` + `api/timesheetReport`,
    `${this.baseUrl}` + `api/leaveReport`,
    `${this.baseUrl}` + `api/customQueryForEmployeeReport`,

    `${this.baseUrl}` + `api/createSurvey`,
    `${this.baseUrl}` + `api/getAllSurveys`,
    `${this.baseUrl}` + `api/getAllQuestionsBySurveyId`,
    `${this.baseUrl}` + `api/setSurveyResponseByEmpId`,
    `${this.baseUrl}` + `api/getAnsweredSurveysByEmpId`,
    `${this.baseUrl}` + `api/empdetails`,
    `${this.baseUrl}` + `api/getSurveyAllResponsesBySurveyId`,

    `${this.baseUrl}` + `api/generatePerviousMonthDSR`,
    `${this.baseUrl}` + `api/getAllDocument`,
    `${this.baseUrl}` + `api/logoutUser`,
    `${this.baseUrl}` + `api/viewAppreciation`,
    `${this.baseUrl}` + `api/getAllAppreciationEvent`,
    `${this.baseUrl}` + `api/enableAppreciation`,
    `${this.baseUrl}` + `api/OnCheckEventName`,
    `${this.baseUrl}` + `api/deleteAppreciationEvent`,
    `${this.baseUrl}` + `api/updateAppreciationEvent`,
    `${this.baseUrl}` + `api/getHierarchyChartByEmpId`,
    `${this.baseUrl}` + `api/getAllManagers`,
    `${this.baseUrl}` + `api/getAllTeamMemberView`,
    `${this.baseUrl}` + `api/getAllTeamsByProjectId`,
    `${this.baseUrl}` + `api/getAllActivitiesByProjectIdAndTeamId`,
    `${this.baseUrl}` + `api/getAllEmployeesByRole`,
    `${this.baseUrl}` + `api/getAllMyTeamsByEmpId`,
    `${this.baseUrl}` + `api/getAllProjectListByProjectManagerId`,
    `${this.baseUrl}` + `api/getAllProjects`,
    `${this.baseUrl}` + `api/getActivityTemplate`,
    `${this.baseUrl}` + `api/getAllEventPhotos`,
    `${this.baseUrl}` + `api/findEmployeeWorkingHistory`,
    `${this.baseUrl}` + `api/customTimesheetApplicationReport`,

    `${this.baseUrl}` + `api/getMappedActivityPreview`,
    `${this.baseUrl}` + `api/getMappedActivityInUpdateTeam`,

    `${this.baseUrl}` + `api/updateTimesheetLockCheck`,
    `${this.baseUrl}` + `api/revokeApprovedLeaveApplication`,
    `${this.baseUrl}` + `api/updateRevokeLeaveStatus`,
    `${this.baseUrl}` + `api/generateAllEmployeeDSR`,
    `${this.baseUrl}` + `api/getHierarchyByEmpId`,
    `${this.baseUrl}` + `api/alreadyCreatedTeam`,
    `${this.baseUrl}` + `api/createDraftProjectInfo`,
    `${this.baseUrl}` + `api/approvePendingProject`,
    `${this.baseUrl}` + `api/rejectPendingProject`,
    `${this.baseUrl}` + `api/bulkSyncProject`,
    `${this.baseUrl}` + `api/getInternalProject`,
    `${this.baseUrl}` + `api/getDomainSpecialization`,

    `${this.baseUrl}` + `api/bulkApproveLeaveRequest`,
    `${this.baseUrl}` + `api/bulkRejectLeaveRequest`,

    `${this.baseUrl}` + `api/updateTeam`,

    `${this.baseUrl}` + `api/reconsileHolidayTimesheet`,

    `${this.baseUrl}` + `api/updatePendingLeave`,
    `${this.baseUrl}` + `api/deletePendingLeave`,

    `${this.baseUrl}` + `api/approveResignationApplication`,
    `${this.baseUrl}` + `api/rejectResignationApplication`,
    `${this.baseUrl}` + `api/createResignationApplication`,
    `${this.baseUrl}` + `api/revokeResignationApplication`,
    `${this.baseUrl}` + `api/revokeMyResignationApplication`,

    `${this.baseUrl}` + `api/unlockAllTimesheet`,

    `${this.baseUrl}` + `api/changeDepartmentJobRoleMapping`,
    `${this.baseUrl}` + `api/updateOnBoardingCheckList`,

    `${this.baseUrl}` + `api/updateEmployeePassword`,

    `${this.baseUrl}`+`api/updateDefaultFeatureMapping`,
    `${this.baseUrl}`+`api/getDefaultMapping`,

    `${this.baseUrl}`+`api/checkEmailWhenForgotPassword`,
    `${this.baseUrl}`+`api/updateEmployeeForgotPassword`,

    `${this.baseUrl}`+`api/newsletters/uploadNewsletter`,
    `${this.baseUrl}`+`api/newsletters/`,
    `${this.baseUrl}`+`api/newsletters/download/`,
    `${this.baseUrl}`+`api/newsletters/setNewsletterReadResponseByEmpId`,
    `${this.baseUrl}`+`api/newsletters/getAllReadNewslettersByEmpId`,
    `${this.baseUrl}`+`api/getEmployeeAuditInfo`,

    `${this.baseUrl}`+`api/approveDraftEmployeeApplication`,
    `${this.baseUrl}`+`api/uploadHelpDocument`,
    `${this.baseUrl}`+`api/uploadPolicies`,
    `${this.baseUrl}`+`api/bulkCompOffReject`,
    `${this.baseUrl}`+`api/bulkCompOffApprove`,
    `${this.baseUrl}`+`api/getTeamListByProjectName`,

    // added by anurag 
    `${this.baseUrl}`+`api/deleteType`,
    `${this.baseUrl}`+`api/updateType`,
    `${this.baseUrl}`+`api/getAllTypeName`,
    `${this.baseUrl}`+`api/newsletters/getDocumentByType`,
    `${this.baseUrl}`+`api/newsletters/customQueryForDocument`,
    `${this.baseUrl}` + `api/pipGenerateToUser`,
    // query/createQuery
    `${this.baseUrl}` + `api/query/createQuery`,
    `${this.baseUrl}` + `api/query/getNonPublishedQuery`,
    `${this.baseUrl}` + `api/query/getQueryDataForPreview`,
    `${this.baseUrl}` + `api/query/getQueryDetailsByQueryId`,
    `${this.baseUrl}` + `api/query/updateQuery`,
    `${this.baseUrl}` + `api/query/deleteQuery`,
    `${this.baseUrl}` + `api/getEmployeeAuditInfo`,
    `${this.baseUrl}` + `api/customQueryForDepartmentWiseBillableEmployeeReport`,
    `${this.baseUrl}` + `api/getDepartmentWiseBillableData`,
    `${this.baseUrl}` + `api/pipReturnFromUser`,
    `${this.baseUrl}` + `api/setExtendPeriodByPipId`,
    `${this.baseUrl}` + `api/revokeAccount`,
    `${this.baseUrl}` + `api/upload/billableFile`,
    `${this.baseUrl}` + `api/getDepartmentPendingLeaveHistory`,
    `${this.baseUrl}`+`api/getEmployeeAuditInfo`,
    `${this.baseUrl}`+`api/deleteTeamByTeamId`,
    `${this.baseUrl}`+`api/upload/saveExcelDataForManagerMapping`,
    `${this.baseUrl}` + `api/getBioData`,

    //added by priyadarshini
    `${this.baseUrl}` +`api/saveRewardConfiguration`,
    `${this.baseUrl}` + `api/showAllRewards/`,
    `${this.baseUrl}` + `api/fetchEmployeesFromRewardCondition`,
    `${this.baseUrl}` +`api/submitRewardForEmployee`,
    `${this.baseUrl}` + `api/showAllEmployeeRewards`,
    `${this.baseUrl}` + `api/getReviewType`,
    `${this.baseUrl}` + `api/getProjectInfo`,
    `${this.baseUrl}` + `api/getTeamInfo`,
    `${this.baseUrl}` + `api/getAllEmployeesForPerformance`,
    `${this.baseUrl}` + `api/get360TimesheetDetails`,
    `${this.baseUrl}` + `api/submitEmployeePerformanceHOD`,
    `${this.baseUrl}` + `api/submitEmployeePerformanceHR`,
    `${this.baseUrl}` + `api/addReviewType`,
    `${this.baseUrl}` + `api/updateReviewType`,
    `${this.baseUrl}` + `getExistingProjectsAndTeamsByEmployee`,
    `${this.baseUrl}` + `api/getAllEmployeesFor360View`,
    `${this.baseUrl}` + `api/getAll360LeaveApplicationsByEmpId`,
    `${this.baseUrl}` + `api/biomax`,
    `${this.baseUrl}` + `api/saveExcelDataForReward`,
    `${this.baseUrl}` + `api/fetchEmployeesForHomepageByCategoryId`,
    `${this.baseUrl}` + `api/getAllTeamView`, 
    `${this.baseUrl}`+`api/submitRewardForEmployee`,
    `${this.baseUrl}` + `api/showAllEmployeeRewards`,
    `${this.baseUrl}` + `api/getAllEmployeesReportByProjectTypeInConsolidated`,
    `${this.baseUrl}`+`api/getAllEmployeesReportByProjectType`,
     `${this.baseUrl}`+`api/saveAppreciation`,
    `${this.baseUrl}` + `api/deleteCompOff`,
    `${this.baseUrl}` + `api/getAllOrDeptWiseEmployeeTimesheetReport`,
    `${this.baseUrl}` + `api/getAllEmployeesForPerformance`,
    `${this.baseUrl}` + `api/syncPoProjectDetailsByProjectId`,
    `${this.baseUrl}` + `api/sendEmailNotificationToBDTeam`,
    `${this.baseUrl}` + `api/currentStatusForPerformanceTableView`,
    `${this.baseUrl}` + `api/poprojectclone`,
    `${this.baseUrl}` + `api/getEmployeeProjectReport`,
    `${this.baseUrl}` + `api/updateEmployeeReportBillableType`,
    `${this.baseUrl}` + `api/updateBulkBillableEmployeeReport`,
    `${this.baseUrl}` + `api/updateProjectResourcesAsInActiveBulk`,
    `${this.baseUrl}` + `api/deleteTeamsByIdsBulk`
  ]

  constructor(private loaderService: LoaderService) { }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    this.URL_whiteList.forEach((element) => {

      if (request.url == element) {

        request = request.clone({
          setHeaders: {
            loader: 'true'
          }
        });
      }



    })

    if (request.headers.get('loader')) {
      this.loaderService.requestStarted();
      return this.handle(next, request);
    }

    return next.handle(request);

  }

  handle(next, request) {
    return next.handle(request).pipe(tap((event) => {

      if (event instanceof HttpResponse) {
        this.loaderService.requestEnded();
      }
    },
      (error: HttpErrorResponse) => {
        this.loaderService.resetSpinner();
        throw error;
      }

    ))
  }
}