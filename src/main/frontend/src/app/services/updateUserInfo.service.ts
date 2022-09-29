import { HttpClient } from "@angular/common/http";
import { EventEmitter, Injectable, OnInit } from "@angular/core";
import * as moment from "moment";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { first } from "rxjs/operators";
import { Employee } from "../models/employee";
import { User } from "../models/user";
import { AuthenticationService } from "./authentication.service";
import { EmployeeService } from "./employee.service";
import { ValidationService } from "./validation.service";

@Injectable({providedIn: 'root'})
export class UpdateUserInfoService {
    private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";
  
    //modal 
    alertMessage: any;
    modalRef: BsModalRef = new BsModalRef();

    //Obj 
    currentUser: User;
    private userInfoObj: Employee = new Employee();
    updateduserInfoObj : EventEmitter<Employee> = new EventEmitter<Employee>();


    constructor(
        private http: HttpClient,
        private authenticationService: AuthenticationService,
        private modalService: BsModalService,
        private employeeService: EmployeeService,
    ){ 
        this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
        this.getEmployeeInfo();
    }

    getEmployeeInfo(){
        this.userInfoObj = new Employee();
        let currentEmp = new Employee();
        currentEmp.empId = this.currentUser.empId;
        console.log("currentEmp : ", currentEmp);
        
        this.employeeService.getEmployeeByEmpId(currentEmp).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.userInfoObj = response.serviceResponse;
            console.log("userInfoObj : ", this.userInfoObj);
            this.updateduserInfoObj.emit(this.userInfoObj);
          } else {
            console.error(response.serviceResponse);
          }
        });
    }

    getUserInfoObj(){
        return Object.assign({}, this.userInfoObj) ;
    }

    setUserInfoObj(userInfoObj: Employee){
        this.userInfoObj = userInfoObj;
        this.updateduserInfoObj.emit(this.userInfoObj);
    }

    async saveEmployeeInfo(): Promise<any> {
        let response:any;

        this.userInfoObj.isDraft = true;
        console.log("saveEmployeeInfo : ", this.userInfoObj);

        if (this.userInfoObj.updateApplicationStatus == "In-Progress") {
            
            response = await this.employeeService.updateDraftEmployee(this.userInfoObj).toPromise();
        } else {
            this.userInfoObj.updateApplicationStatus = "In-Progress";
            response = await this.employeeService.createDraftEmployee(this.userInfoObj).toPromise();
        }

        return response;
    }

    async updateEmployeeInfo(): Promise<any> {
        this.userInfoObj.isDraft = true;
        this.userInfoObj.updateApplicationStatus = "Pending For Approval";
        console.log("updateEmployeeInfo : ", this.userInfoObj);
        return await this.employeeService.updateDraftEmployee(this.userInfoObj).toPromise();
    }

    async getDraftByEmpId():Promise<Employee>{
        let draftObj:Employee;

        let currentEmp = new Employee();
        currentEmp.employeementId = this.currentUser.employeementId;
        currentEmp.isDraft = true;

        const response:any = await this.employeeService.getDraftEmployeeByEmploymentId(currentEmp).toPromise();
        if (response.serviceStatus == "Success") {
            draftObj = response.serviceResponse;
            console.log("draftObj : ", draftObj);
        } else {
            console.error(response.serviceResponse)
        }

        return draftObj;
    }

}