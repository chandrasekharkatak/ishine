import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { JobRole } from 'src/app/models/jobRole';
import { DepartmentService } from 'src/app/services/department.service';
import { JobRoleService } from 'src/app/services/job-role.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-role-config',
  templateUrl: './role-config.component.html',
  styleUrls: ['./role-config.component.css']
})
export class RoleConfigComponent implements OnInit {

  //flags 
  isCreation:boolean = true;
  isUpdation: boolean = false;
  isForm: boolean = true;
  isTable: boolean = false;

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  //obj 
  jobRoleObj:JobRole = new JobRole();
  allJobRoleList:any;
  allDeptList:any 

  constructor(private validationService:ValidationService,private modalService: BsModalService,
    private jobRoleService: JobRoleService, private departmentService: DepartmentService) { }

  ngOnInit(): void {
    // getting departments 
    this.getAllDepartmentList();

    //Deafult values for dropdown
    this.jobRoleObj.departmentId = '';
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

    this.getAllJobRoleList();
  }

  reset(){
    this.jobRoleObj = new JobRole();
    //Deafult values for dropdown
    this.jobRoleObj.departmentId = '';

    this.allJobRoleList = [];
  }

  showUpdateForm(jobRole:JobRole){
    this.isForm = true;
    this.isTable = false;
    this.isUpdation = true;
    this.isCreation = false;

    this.jobRoleObj = Object.assign({}, jobRole)
  }

  validateJobRoleObj(jobRole:JobRole, template: TemplateRef<any>){

    if(!this.validationService.validateNullUndefinedEmptyString(jobRole.name)){
      this.alertMessage = "Please enter Job Role Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    if(!this.validationService.validateNullUndefinedEmptyString(jobRole.departmentId)){
      this.alertMessage = "Please select Department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }


  // CRUD
  onCreateJobRole(template: TemplateRef<any>){
    let inputValidated:boolean  = this.validateJobRoleObj(this.jobRoleObj, template)
    if(!inputValidated) return;
    
    this.jobRoleObj.createdBy = 1;
    console.log("Create jobRoleObj : ", this.jobRoleObj);
    this.jobRoleService.createJobRole(this.jobRoleObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }

    });
  }

  onUpdateJobRole(template: TemplateRef<any>){
    let inputValidated:boolean  = this.validateJobRoleObj(this.jobRoleObj, template)
    if(!inputValidated) return;
    
    this.jobRoleObj.updatedBy = 1;
    this.jobRoleObj.jobRoleId = this.jobRoleObj.id; //TODO : DTO and MODEL descrepency
    console.log("Update jobRoleObj : ", this.jobRoleObj);
    this.jobRoleService.updateJobRole(this.jobRoleObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDeleteJobRole(template: TemplateRef<any>){
    this.cancelRequest();
  
    this.jobRoleObj.jobRoleId = this.jobRoleObj.id; //TODO : DTO and MODEL descrepency
    this.jobRoleService.deleteJobRole(this.jobRoleObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllJobRoleList(){
    this.allJobRoleList = [];

    this.jobRoleService.getAllJobRole().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allJobRoleList = response.serviceResponse;
        console.log("allJobRoleList : ", this.allJobRoleList)
      } else {
        alert(response.serviceResponse)
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

  //modals
  openDeleteJobRole(template: TemplateRef<any>, jobRole: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.jobRoleObj = jobRole;
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}
