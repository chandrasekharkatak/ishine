import { HttpClient } from "@angular/common/http";
import { EventEmitter, Injectable, OnInit } from "@angular/core";
import * as moment from "moment";
import { BsModalRef, BsModalService } from "ngx-bootstrap/modal";
import { first } from "rxjs/operators";
import { certification } from "../models/certification";
import { Employee } from "../models/employee";
import { PreviousEmployer } from "../models/previousEmployer";
import { User } from "../models/user";
import { AuthenticationService } from "./authentication.service";
import { EmployeeService } from "./employee.service";
import { ValidationService } from "./validation.service";
import { environment } from "src/environments/environment";
import { Router } from "@angular/router";
import { UtilityService } from "./utility.service";
import { EncryptionService } from "./EncryptionService";

@Injectable({ providedIn: 'root' })
export class UpdateUserInfoService {
    private baseUrl: any = environment.baseUrl;

    //modal 
    alertMessage: any;
    modalRef: BsModalRef = new BsModalRef();
    employeeData: any;

    //Obj 
    currentUser: User;
    private userInfoObj: Employee = new Employee();
    updateduserInfoObj: EventEmitter<Employee> = new EventEmitter<Employee>();


    constructor(
        private http: HttpClient,
        private authenticationService: AuthenticationService,
        private modalService: BsModalService,
        private employeeService: EmployeeService,
        private router: Router,
        private utilityService:UtilityService,
        private encryptionService:EncryptionService
    ) {
        this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
        this.getEmployeeInfo();
        console.log("hhyyyy  ", this.router.url);
    }

    getEmployeeInfo() {

        console.log("#################YES@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@  ", this.router.url);

        this.userInfoObj = new Employee();
        let currentEmp = new Employee();

        if (this.router.url.startsWith('/employee-360/profile')) {

            let encryptedEmployeeData = localStorage.getItem('employee360Data');
            let employeeData = null;
            if (encryptedEmployeeData) {
                const decryptedString = this.encryptionService.decrypt(encryptedEmployeeData);
                if (decryptedString) {
                    try {
                        employeeData = JSON.parse(decryptedString);
                    } catch (error) {
                        console.error('Failed to parse decrypted session user:', decryptedString, error);
                        employeeData = null;
                    }
                } else {
                    console.warn('Decryption returned empty string.');
                    employeeData = null;
                }
            } else {
                console.warn('No currentUser found in sessionStorage');
                employeeData = null;
            }
            const storedData = employeeData;
            const parsedData = storedData ? storedData : null;
            if (parsedData != null || parsedData != undefined) {
                this.employeeData = parsedData;
            }
            currentEmp.empId = this.employeeData.empId;
        } else {
            console.log("hhyyyy  ", this.router.url);
            currentEmp.empId = this.currentUser.empId;
            console.log("currentEmp : ", currentEmp);
        }




        this.employeeService.getEmployeeByEmpId(currentEmp).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
                this.userInfoObj = response.serviceResponse;
                if (this.userInfoObj.certifications) {
                    this.userInfoObj.certifications.forEach((certification: certification) => certification.employeeCertificateId = null);
                }
                if (this.userInfoObj.previousEmploymentList) {
                    this.userInfoObj.previousEmploymentList && this.userInfoObj.previousEmploymentList.forEach((previousEmployer: PreviousEmployer) => previousEmployer.previousEmploymentId = null);
                }

                //console.log("userInfoObj : ", this.userInfoObj);
                this.updateduserInfoObj.emit(this.userInfoObj);
            } else {
                console.error(response.serviceResponse);
            }
        });
    }

    getUserInfoObj() {
      //  this.getEmployeeInfo();
        return Object.assign({}, this.userInfoObj);
    }


    getUserInfoObjwithEmpId(){
          this.getEmployeeInfo();
         return Object.assign({}, this.userInfoObj);
    }

    setUserInfoObj(userInfoObj: Employee) {
        this.userInfoObj = userInfoObj;
        this.updateduserInfoObj.emit(this.userInfoObj);
    }

    async saveEmployeeInfo(): Promise<any> {
        let response: any;

        this.userInfoObj.isDraft = true;
        //console.log("saveEmployeeInfo : ", this.userInfoObj);

        if (this.userInfoObj.updateApplicationStatus == "In-Progress") {

            response = await this.employeeService.updateDraftEmployee(this.userInfoObj).toPromise();
        } else {
            this.userInfoObj.updateApplicationStatus = "In-Progress";
            response = await this.employeeService.createDraftEmployee(this.userInfoObj).toPromise();
        }

        return response;
    }

    async updateEmployeeInfo(): Promise<any> {


        if (this.router.url.startsWith('/employee-360/profile')) {
            
             let encryptedEmployeeData = localStorage.getItem('employee360Data');
            let employeeData = null;
            if (encryptedEmployeeData) {
                const decryptedString = this.encryptionService.decrypt(encryptedEmployeeData);
                if (decryptedString) {
                    try {
                        employeeData = JSON.parse(decryptedString);
                    } catch (error) {
                        console.error('Failed to parse decrypted session user:', decryptedString, error);
                        employeeData = null;
                    }
                } else {
                    console.warn('Decryption returned empty string.');
                    employeeData = null;
                }
            } else {
                console.warn('No currentUser found in sessionStorage');
                employeeData = null;
            }
            const storedData = employeeData;
            const parsedData = storedData ? storedData : null;
            if(parsedData != null || parsedData != undefined ){
              this.employeeData =  parsedData;
            }

            this.userInfoObj.updatedBy = this.employeeData.empId;
            this.userInfoObj.isDraft = true;
            this.userInfoObj.updateApplicationStatus = "Pending For Approval";
            this.userInfoObj.isApmosysProduct = this.employeeData.isApmosysProduct;
           
        } else {
        this.userInfoObj.updatedBy = this.currentUser.empId;
        this.userInfoObj.isDraft = true;
        this.userInfoObj.updateApplicationStatus = "Pending For Approval";
        this.userInfoObj.isApmosysProduct = this.currentUser.isApmosysProduct;
        }




       
        //console.log("updateEmployeeInfo : ", this.userInfoObj);
        return await this.employeeService.updateDraftStatusById(this.userInfoObj).toPromise();
    }

    async getDraftByEmpId(): Promise<Employee> {
        let draftObj: Employee;

        let currentEmp = new Employee();


        if (this.router.url.startsWith('/employee-360/profile')) {
             let encryptedEmployeeData = localStorage.getItem('employee360Data');
            let employeeData = null;
            if (encryptedEmployeeData) {
                const decryptedString = this.encryptionService.decrypt(encryptedEmployeeData);
                if (decryptedString) {
                    try {
                        employeeData = JSON.parse(decryptedString);
                    } catch (error) {
                        console.error('Failed to parse decrypted session user:', decryptedString, error);
                        employeeData = null;
                    }
                } else {
                    console.warn('Decryption returned empty string.');
                    employeeData = null;
                }
            } else {
                console.warn('No currentUser found in sessionStorage');
                employeeData = null;
            }
            const storedData = employeeData;
            const parsedData = storedData ?storedData : null;
            if(parsedData != null || parsedData != undefined ){
              this.employeeData =  parsedData;
            }
            console.log("Employeement Id", this.employeeData)

            let employementid =  Number(this.utilityService.getEmployeeIdSubstring2(this.employeeData));
            // let employementid =  Number(this.employeeData.employeementid);
            currentEmp.employeementId = employementid;
            console.log("Employeement Id", currentEmp.employeementId)
            currentEmp.isDraft = true;
        } else{
            
           
        currentEmp.employeementId = this.currentUser.employeementId;
        currentEmp.isApmosysProduct = this.currentUser.isApmosysProduct;
        currentEmp.isDraft = true;
        }

       


        

        const response: any = await this.employeeService.getDraftEmployeeByEmploymentId(currentEmp).toPromise();
        if (response.serviceStatus == "Success") {
            draftObj = response.serviceResponse;
            //console.log("draftObj : ", draftObj);
        } else {
            console.error(response.serviceResponse)
        }

        return draftObj;
    }

}