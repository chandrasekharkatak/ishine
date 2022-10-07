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

    `${this.baseUrl}` + `employeeportal/api/authenticateUser`,
    `${this.baseUrl}` + `employeeportal/api/authenticateUserWithOTP`,

    `${this.baseUrl}` + `employeeportal/api/createDepartment`,
    `${this.baseUrl}` + `employeeportal/api/updateDepartment`,
    `${this.baseUrl}` + `employeeportal/api/deleteDepartment`,
    `${this.baseUrl}` + `employeeportal/api/getAllDepartments`,
    `${this.baseUrl}` + `employeeportal/api/updateDepartmentHolidayMappings`,

    `${this.baseUrl}` + `employeeportal/api/createEmployee`,
    `${this.baseUrl}` + `employeeportal/api/updateEmployeeByEmpId`,
    `${this.baseUrl}` + `employeeportal/api/deleteEmployeeByEmpId`,
    `${this.baseUrl}` + `employeeportal/api/getAllEmployees`,
    `${this.baseUrl}` + `employeeportal/api/getEmployeeByEmpId`,
    `${this.baseUrl}` + `employeeportal/api/updateEmployeeProfileByEmpId`,
    `${this.baseUrl}` + `employeeportal/api/previewImage`,
    `${this.baseUrl}` + `employeeportal/api/uploadImage`,
    `${this.baseUrl}` + `employeeportal/api/createDraftEmployee`,
    `${this.baseUrl}` + `employeeportal/api/updateDraftEmployeeById`,
    `${this.baseUrl}` + `employeeportal/api/deleteDraftEmployeeById`,
    `${this.baseUrl}` + `employeeportal/api/getAllDraftEmployees`,

    `${this.baseUrl}` + `employeeportal/api/addHoliday`,
    `${this.baseUrl}` + `employeeportal/api/updateHoliday`,
    `${this.baseUrl}` + `employeeportal/api/getAllHolidays`,
    `${this.baseUrl}` + `employeeportal/api/getHolidayListByDeptId`,

    `${this.baseUrl}` + `employeeportal/api/createJobRole`,
    `${this.baseUrl}` + `employeeportal/api/updateJobRole`,
    `${this.baseUrl}` + `employeeportal/api/deleteJobRole`,
    `${this.baseUrl}` + `employeeportal/api/getAllJobRole`,

    `${this.baseUrl}` + `employeeportal/api/applyLeave`,
    `${this.baseUrl}` + `employeeportal/api/updateLeaveStatus`,
    `${this.baseUrl}` + `employeeportal/api/getAllMyLeaveApplicationsByEmpId`,
    `${this.baseUrl}` + `employeeportal/api/getAllMyTeamsPendingLeaveApplicationsByManagerId`,
    `${this.baseUrl}` + `employeeportal/api/getLeaveLogsByEmpId`,
    `${this.baseUrl}` + `employeeportal/api/getMyLeaveBalancesByEmpId`,
    `${this.baseUrl}` + `employeeportal/api/updateLeavesByEmpId`,
    `${this.baseUrl}` + `employeeportal/api/createLeaveType`,
    `${this.baseUrl}` + `employeeportal/api/updateLeaveType`,
    `${this.baseUrl}` + `employeeportal/api/getAllLeaveTypes`,

    `${this.baseUrl}` + `employeeportal/api/getAllCompOffReasons`,
    `${this.baseUrl}` + `employeeportal/api/applyForCompOff`,
    `${this.baseUrl}` + `employeeportal/api/updateCompOffById`,
    `${this.baseUrl}` + `employeeportal/api/getAllCompOffRequestsByEmpId`,
    `${this.baseUrl}` + `employeeportal/api/getPendingCompOffRequestsByManagerId`,

    `${this.baseUrl}` + `employeeportal/api/getSubfeaturesByJobRoleId`,
    `${this.baseUrl}` + `employeeportal/api/getAllSubFeatures`,
    `${this.baseUrl}` + `employeeportal/api/updateRoleFeatureMapping`,

    `${this.baseUrl}` + `employeeportal/api/createUserType`,
    `${this.baseUrl}` + `employeeportal/api/getAllUserTypes`,
    `${this.baseUrl}` + `employeeportal/api/updateCrudMappingsByMapId`,
    `${this.baseUrl}` + `employeeportal/api/getUserTypeCrudMappingsByUserTypeId`,
    `${this.baseUrl}` + `employeeportal/api/getAllUserTypeCrudMappings`,
    `${this.baseUrl}` + `employeeportal/api/changeLeaveTypeMapping`,

    `${this.baseUrl}` + `employeeportal/api/getAllEventPhotos`,
    `${this.baseUrl}` + `employeeportal/api/customQueryForLeaveReport`,
    `${this.baseUrl}` + `employeeportal/api/timesheetReport`,
    `${this.baseUrl}` + `employeeportal/api/leaveReport`,
    `${this.baseUrl}` + `employeeportal/api/customQueryForEmployeeReport`
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
