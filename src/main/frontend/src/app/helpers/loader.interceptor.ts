import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpResponse,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { LoaderService } from '../services/loader.service';



@Injectable()
export class LoaderInterceptor implements HttpInterceptor {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  URL_whiteList = [

    `${this.baseUrl}` + `api/authenticateUser`,
    `${this.baseUrl}` + `api/authenticateUserWithOTP`,

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
    `${this.baseUrl}`+`api/findEmployeeWorkingHistory`,
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
  ]

  constructor(private loaderService: LoaderService) { }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {


    this.URL_whiteList.forEach((element) => {


      if (element === request.url) {
        
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
