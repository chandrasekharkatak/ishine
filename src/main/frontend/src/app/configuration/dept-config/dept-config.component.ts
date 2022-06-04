import { Component, OnDestroy, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Subscription } from 'rxjs';
import { first } from 'rxjs/operators';
import { Department } from 'src/app/models/department';
import { Feature } from 'src/app/models/feature';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-dept-config',
  templateUrl: './dept-config.component.html',
  styleUrls: ['./dept-config.component.css']
})
export class DeptConfigComponent implements OnInit, OnDestroy {

  private activatedSubscriptions:Subscription;
  //flags 
  isCreation:boolean = true;
  isUpdation: boolean = false;
  isForm: boolean = true;
  isTable: boolean = false;

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  //obj
  deptObj:Department = new Department();
  allDeptList:any;
  hodList:any = [];

  feature="Department";
  currentUser:User;
  userMapping:any = {};

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private departmentService: DepartmentService,
    private employeeService:EmployeeService,
    private authenticationService : AuthenticationService) {
      this.activatedSubscriptions = this.employeeService.updatedEmployeeList.subscribe(allEmployees => {
        this.hodList = allEmployees
    });
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    //Deafult values for dropdown
    this.deptObj.hodId = '';

    // Dynamic Subfeature Flags 
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);
  }

  ngOnDestroy(): void {
    this.activatedSubscriptions.unsubscribe();
  }

  showCreateForm(){
    this.isForm = true;
    this.isTable = false;
    this.isCreation = true;
    this.isUpdation = false;

    this.reset();
  }

  showTable(){
    this.isTable = true;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;

    this.getAllDepartmentList();
  }

  reset(){
    this.deptObj = new Department();
    //Deafult values for dropdown
    this.deptObj.hodId = '';

    this.allDeptList = [];
  }

  showUpdateForm(department:Department){
    this.isForm = true;
    this.isTable = false;
    this.isUpdation = true;
    this.isCreation = false;

    this.deptObj = Object.assign({}, department)
  }

  validateDepartmentObj(deptObj:Department, template: TemplateRef<any>){

    if(!this.validationService.validateNullUndefinedEmptyString(deptObj.name)){
      this.alertMessage = "Please enter Department Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    

    if(!this.validationService.validateNullUndefinedEmptyString(deptObj.hodId)){
      this.alertMessage = "Please select Head of Department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  // CRUD 
  onCreateDepartment(template: TemplateRef<any>){
    let inputValidated:boolean  = this.validateDepartmentObj(this.deptObj, template)
    if(!inputValidated) return;
    
    this.deptObj.createdBy = 1;
    console.log("Create Dept : ", this.deptObj);
    this.departmentService.createDepartment(this.deptObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }

    });
  }

  onUpdateDepartment(template: TemplateRef<any>){
    let inputValidated:boolean  = this.validateDepartmentObj(this.deptObj, template)
    if(!inputValidated) return;
    
    this.deptObj.updatedBy = 1;
    console.log("Update dept : ", this.deptObj);
    this.departmentService.updateDepartment(this.deptObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDeleteDepartment(template: TemplateRef<any>){
    this.cancelRequest();
  
    //! Need to check this 
    let department:Department = new Department();
    department.dept_id = this.deptObj.dept_id;

    this.departmentService.deleteDepartment(department).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllDepartmentList(){
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
        console.log("allDeptList : ", this.allDeptList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  // Modals
  openDeleteDepartment(template: TemplateRef<any>, department: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.deptObj = department;
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}
