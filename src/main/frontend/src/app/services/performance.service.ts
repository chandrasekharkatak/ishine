import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { QuarterCycle } from '../models/quarterCycle';

@Injectable({
  providedIn: 'root'
})
export class PerformanceService {

  private baseUrl:any = environment.baseUrl;
  
  constructor(private http: HttpClient) { }


  getAllEmployee() {
    return this.http.get(`${this.baseUrl}` + `api/getAllEmployees`);
  }

  getAllEmployeesForPerformance(details:any){
    return this.http.post(`${this.baseUrl}` + `api/getAllEmployeesForPerformance`, details);
  }

  createQuarterCycle(quarterCycle: QuarterCycle){
    return this.http.post(`${this.baseUrl}` + `api/createQuarterCycle`, quarterCycle);
  }

  updateQuarterCycle(quarterCycle: QuarterCycle){
    return this.http.post(`${this.baseUrl}` + `api/updateQuarterCycle`, quarterCycle);
  }

  getQuartersByYear(financialYear:any){
    return this.http.get(`${this.baseUrl}`+ `api/getQuartersByYear/` +financialYear);
  }

  getAllQuarterCycles(){
    return this.http.get(`${this.baseUrl}`+ `api/getAllQuarterCycles`);
  }

  isEnable(quarterCycle:QuarterCycle){
     return this.http.post(`${this.baseUrl}`+`api/isEnable`, quarterCycle);
  }

  addReviewType(reviewObj:any){
    return this.http.post(`${this.baseUrl}` + `api/addReviewType`,reviewObj);
  }

  getReviewType() {
    return this.http.get(`${this.baseUrl}` + `api/getReviewType`);
  }

  deleteReviewType(reviewObj:any){
    return this.http.post(`${this.baseUrl}` + `api/deleteReviewType`,reviewObj);
  }

  deleteQuarterCycle(quarterCycle:QuarterCycle){
    return this.http.post(`${this.baseUrl}`+`api/isDelete`, quarterCycle);
  }

  getQuarterCycleById(quarterId:any){
    return this.http.get(`${this.baseUrl}` + `api/getQuarterCycleById/`+quarterId);

  }








getReviewTypeById(reviewObj: any) {
  return this.http.post(`${this.baseUrl}` + `api/getReviewTypeById`, reviewObj);
}

updateReviewType(reviewObjs:any): Observable<any> {
  return this.http.post(`${this.baseUrl}` + `api/updateReviewType`, reviewObjs);

}

submitEmployeePerformanceHOD(performance: any){
  return this.http.post(`${this.baseUrl}` + `api/submitEmployeePerformanceHOD`,performance);
}


hrAndHodEmpoyeePerformanceView(performance: any){
  return this.http.post(`${this.baseUrl}` + `api/hrAndHODEmployeePerformanceView`,performance);
}

submitEmployeePerformanceHR(performance:any){
  return this.http.post(`${this.baseUrl}` + `api/submitEmployeePerformanceHR`,performance);
}

updateEmployeePerformanceHOD(performance:any){
  return this.http.post(`${this.baseUrl}` + `api/updateEmployeePerformanceHOD`,performance);
}



getALLdepartmentByEmployee() {
  return this.http.get(`${this.baseUrl}`+ `api/getAllDepartmentbyEmployeecont`);
}

getReviewLabelForEveryDepartment() {
  return this.http.get(`${this.baseUrl}` + `api/getReviewLabelForEveryDepartment`);
}

}
