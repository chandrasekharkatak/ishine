import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { EncryptionService } from './EncryptionService';
import { EmployeeTimesheetDTO } from '../models/EmployeeTimesheetDTO';
import { Timesheet } from '../models/timesheet';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TimesheetNewService {

  private baseUrl:any = environment.baseUrl;
  
    constructor(private http: HttpClient, private encryptionService : EncryptionService) { }

    addTimesheetWithClientUpdated(employeeTimesheetDTO:EmployeeTimesheetDTO , selectedFile:any,selectedFile2:any) {
        // const encryptedDto = this.encryptionService.encrypt(JSON.stringify(employeeTimesheetDTO))
        const formData = new FormData();
        formData.append('dto', JSON.stringify(employeeTimesheetDTO));
        formData.append('filledDoc', selectedFile);
        formData.append('approvedDoc', selectedFile2);
        return this.http.post(`${this.baseUrl}` + `api/v2/timesheet/addTimesheetWithClientNew`, formData);
      }

        getAllProjectsByEmpId(empId: number) {
        return this.http.post(`${this.baseUrl}` + `api/v2/timesheet/getAllProjectsByEmpId`, empId);
        }

        getActiveProjectsByEmpId(empId: any): Observable<any> {
        return this.http.post(`${this.baseUrl}`+`api/v2/timesheet/getActiveProjectsByEmpId?empId=${empId}`, null);
        }

        getAllDayTypes(){
        return this.http.get(`${this.baseUrl}` + `api/v2/master/day-type/getAllActiveDayType`);
        }  
        getAllMyTimesheetsByEmpId(timesheetObj: Timesheet) {
        return this.http.post(`${this.baseUrl}` + `api/v2/timesheet/getAllMyTimesheetsByEmpId`, timesheetObj);
        }

        getAllWorkLocation(){
        return this.http.get(`${this.baseUrl}` + `api/v2/master/work-location-type/getAllActiveLocationTypes`);
        }
        getAllDSRApprovalStatus(){
          return this.http.get(`${this.baseUrl}` + `api/v2/master/client-status/getAllActiveStatusForClient`);
        }
        getActiveProjectsAndClientSideIdByEmpId(empId:number){
          return this.http.post(`${this.baseUrl}` + `api/v2/timesheet/getActiveProjectsAndClientSideIdByEmpId`,  empId);
        }

}
  
