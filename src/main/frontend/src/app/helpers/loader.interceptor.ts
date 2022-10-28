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

    `${this.baseUrl}` + `api/getSubfeaturesByJobRoleId`,
    `${this.baseUrl}` + `api/getAllSubFeatures`,
    `${this.baseUrl}` + `api/updateRoleFeatureMapping`,

    `${this.baseUrl}` + `api/createUserType`,
    `${this.baseUrl}` + `api/getAllUserTypes`,
    `${this.baseUrl}` + `api/updateCrudMappingsByMapId`,
    `${this.baseUrl}` + `api/getUserTypeCrudMappingsByUserTypeId`,
    `${this.baseUrl}` + `api/getAllUserTypeCrudMappings`,
    `${this.baseUrl}` + `api/changeLeaveTypeMapping`,

    `${this.baseUrl}` + `api/getAllEventPhotos`,
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
