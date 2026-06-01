import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Query } from '../models/query';
import { first, map, switchMap } from 'rxjs/operators';
import * as moment from 'moment';
import { AppComponent } from '../app.component';
import { SortPipe } from '../sort.pipe';
import { of } from 'rxjs/internal/observable/of';
import { Observable } from 'rxjs';
import { EmployeeIdUtilService } from './employee-id-util.service';

@Injectable({
  providedIn: 'root'
})
export class UtilityService {

  private baseUrl:any = environment.baseUrl;
  private employee360ViewUser: boolean = false;
  employeesFor360: any[] = [];
  allEmployeeList360: any[] = [];


  constructor(private http: HttpClient, private employeeIdUtil: EmployeeIdUtilService) { }

  appendEmployeementid(isConsultant: any, emp: any, isApmosysProduct?: any): string {
    const numeric = this.employeeIdUtil.extractNumericId(String(emp)) ?? String(emp);
    return this.employeeIdUtil.generateEmploymentId(numeric, isApmosysProduct, isConsultant) ?? `A-${numeric}`;
  }

  getFormattedEmployeeId(empObj: any): string {
    return this.employeeIdUtil.formatEmploymentIdForDisplay(empObj) ?? '';
  }

  applyEmployeeDisplayFields(emp: any, options?: { regularLabel?: string }): void {
    this.employeeIdUtil.applyEmployeeDisplayFields(emp);
    if (options?.regularLabel && emp?.employeeType === 'Regular') {
      emp.employeeType = options.regularLabel;
    }
  }

  applyEmployeeDisplayFieldsToList(list: any[], options?: { regularLabel?: string }): void {
    if (list == null || !Array.isArray(list)) {
      return;
    }
    list.forEach((emp) => this.applyEmployeeDisplayFields(emp, options));
  }

  resolveEmployeeTypeLabel(emp: any): string {
    return this.employeeIdUtil.formatEmployeeTypeLabel(emp);
  }

  /** Leave balance / legacy APIs that expect ApMoSys Product or Other. */
  resolveEmployeeTypeForLeaveApi(emp: any): string {
    const type = this.employeeIdUtil.resolveEmployeeType(
      emp?.isApmosysProduct, emp?.isConsultant, emp?.isApprenticeship);
    if (type === 'Apmosys Product Consultant') {
      return 'ApMoSys Product Consultant';
    }
    if (type === 'Apmosys Product') {
      return 'ApMoSys Product';
    }
    return 'Other';
  }

  formatNaEmployeementPlaceholder(emp: any): string {
    const formatted = this.getFormattedEmployeeId(emp);
    return formatted ? `NA (${formatted})` : 'NA';
  }

  hasEmploymentIdPrefix(value: string): boolean {
    return /^(A-|CS-|AP-|APCS-)/i.test(String(value ?? '').trim());
  }

  stripEmploymentIdPrefix(id: any): number | string | null {
    return this.employeeIdUtil.toApiEmploymentId(id);
  }

  formatEmploymentIdForExport(emp: any): string {
    return this.employeeIdUtil.formatEmploymentIdForDisplay(emp)
      ?? String(emp?.employeementId ?? emp?.employmentIdAcToET ?? '');
  }

  substringEmployeementid(isConsultant, emp): string {
    const numeric = this.employeeIdUtil.toApiEmploymentId(emp);
    if (numeric != null) {
      return String(numeric);
    }
    if(isConsultant == "true"){
      if (emp.startsWith("A-")) {
        return emp.substring(2); 
      } else {
        console.log("Invalid apprenticeship ID format");
        return emp; 
      }
      // return emp.substring(5);
      // return emp.substring(2);
    }
    else {
      console.error("invalid data found");
      //return emp;
    }
  }

  getEmployeeIdSubstring(empObj: any): string {  

    console.log("yess",empObj)

    return this.substringEmployeementid(
      empObj.isConsultant,
      empObj.isApprenticeship
    );
  }

  substringEmploymentId2(isConsultant: string, emp: string): string {
    const numeric = this.employeeIdUtil.toApiEmploymentId(emp);
    if (numeric != null) {
      return String(numeric);
    }
    return emp;
  }

  getEmployeeIdSubstring2(empObj: any): string {  

    console.log("yess",empObj)

    return this.substringEmploymentId2(
      empObj.isConsultant,
      empObj.employeementId
    );
  }
  

  transformData(response: any): any {
    const groupedData: any = {};
  
    response.serviceResponse.forEach((record: any) => {
      const monthYear = record.leaveDate;
      const leaveType = record.leaveType;
      const status = record.status;
      const count = parseInt(record.total_leave_records, 10);
  
      if (!groupedData[monthYear]) {
        groupedData[monthYear] = {};
      }
      if (!groupedData[monthYear][leaveType]) {
        groupedData[monthYear][leaveType] = { Approved: 0, Pending: 0, Rejected: 0, Revoked: 0 };
      }
  
      groupedData[monthYear][leaveType][status] += count;
    });
  
    return groupedData;
  }

  getCustomQueryData(query: Query) {
    return this.http.post(`${this.baseUrl}` + `api/getCustomQueryData`, query);
  }
  // Get CustomQuery Filtered Data
  getFilteredQueryData(payload: any):Observable<any> {
    return this.http.post(`${this.baseUrl}` + `api/getFilteredQueryData`, payload);
  }

  // ===== Custom Query: server-side =====
  getCustomQueryDataPaged(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}` + `api/getCustomQueryDataPaged`, payload);
  }

  getFilteredQueryDataPaged(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}` + `api/getFilteredQueryDataPaged`, payload);
  }

  getCustomQueryDistinctValues(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}` + `api/getCustomQueryDistinctValues`, payload);
  }

  setEmployee360ViewAccess(hasAccess: boolean): void {
    this.employee360ViewUser = hasAccess;
  }

  getEmployee360ViewAccess(): boolean {
    return this.employee360ViewUser;
  }

  getEmployeeDetailsFor360View() {
    return this.getAllEmployeesFor360View().pipe(
      first(),
      switchMap((response: any) => {

        if (response.serviceStatus == "Success") {
          console.log(response.serviceResponse);
          let allEmployeeList360 = response.serviceResponse;
          
          // Perform necessary formatting and data transformations
          allEmployeeList360.forEach(employeeObj => {
            this.applyEmployeeDisplayFields(employeeObj);
            employeeObj.dateOfJoining = employeeObj.dateOfJoining ? moment(employeeObj.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
            employeeObj.dateOfRelieving = employeeObj.dateOfRelieving ? moment(employeeObj.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
            employeeObj.updatedOn = employeeObj.updatedOn ? moment(employeeObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
            employeeObj.createdOn = employeeObj.createdOn ? moment(employeeObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          });
  
          // Return an observable that waits for the next data load
          const sortedEmployees = new SortPipe().transform(allEmployeeList360, ['name', 'string', 'asc']);
          return of(sortedEmployees);  // Using 'of' to wrap the sorted result in an observable
        } else {
          alert(response.serviceResponse);
          return of([]);  // Return an observable of an empty array if serviceStatus is not "Success"
        }
      })
    ).toPromise();
  }
  
  getEmployeeDetailsFor360ViewNewImple(empId:any) {
    return this.getAllEmployeesFor360Viewnew(empId).pipe(
      first(),
      switchMap((response: any) => {

        if (response.serviceStatus == "Success") {
          console.log(response.serviceResponse[0]);
          let employeeObj = response.serviceResponse[0];
          
          // Perform necessary formatting and data transformations
         
            this.applyEmployeeDisplayFields(employeeObj);
            employeeObj.dateOfJoining = employeeObj.dateOfJoining ? moment(employeeObj.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
            employeeObj.dateOfRelieving = employeeObj.dateOfRelieving ? moment(employeeObj.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
            employeeObj.updatedOn = employeeObj.updatedOn ? moment(employeeObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
            employeeObj.createdOn = employeeObj.createdOn ? moment(employeeObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          return of(employeeObj);  // Using 'of' to wrap the sorted result in an observable
        } else {
          alert(response.serviceResponse);
          return of([]);  // Return an observable of an empty array if serviceStatus is not "Success"
        }
      })
    ).toPromise();
  }
  
  getAllEmployeesFor360View() {
    return this.http.get(`${this.baseUrl}` + `api/getAllEmployeesFor360View`);
  }
  getAllEmployeesFor360Viewnew(empId:any) {
    return this.http.get(`${this.baseUrl}` + `api/getAllEmployeesFor360View/`+empId);
  }

  getQueriesByEmployeeID(employeeID:any) {
    return this.http.get(`${this.baseUrl}` + `api/query/getByEmpID/`+employeeID);
  }

  saveQuery(payload: any): Observable<any> {
    return this.http.post(`${this.baseUrl}` + `api/query/create`,payload);
  }

  updateQuery(queryID:any, payload: any): Observable<any> {
    return this.http.put(`${this.baseUrl}` + `api/query/update/`+queryID,payload);
  }

  getJobRoles(): Observable<any> {
    return this.http.get(`${this.baseUrl}` + `api/getAllJobRole`);
  }



}