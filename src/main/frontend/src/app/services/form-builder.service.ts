import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FormBuilderService {
  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  createDynamicForm(formObject: any){
    return this.http.post(`${this.baseUrl}` + `form/createDynamicForm`, formObject);
  }

  updateDynamicForm(formId: any, formObject: any){
    return this.http.put(`${this.baseUrl}` + `form/updateDynamicForm/`+ `${formId}`, formObject);
  }

  getAllDynamicForm(){
    return this.http.get(`${this.baseUrl}` + `form/getAllDynamicForm`);
  }

  getByDynamicFormById(formId:any){
    return this.http.get(`${this.baseUrl}` + `form/getByDynamicFormById/`+ `${formId}`);
  }

  deleteFormById(formId:any){
    return this.http.get(`${this.baseUrl}` + `form/deleteFormById/`+ `${formId}`);
  }

  getAllDynamicFormByDepartmentAndType(deptIds: any){
    const params = new HttpParams().set('allDeptIds', deptIds);
    return this.http.get(`${this.baseUrl}` + `form/getAllDynamicFormByDepartmentAndType`,{params});
  }

  getAllEmployeeList(){
    return this.http.get(`${this.baseUrl}` + `api/getAllEmployee`);
  }

}
