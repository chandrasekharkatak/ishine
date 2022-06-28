import { Component, OnDestroy, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Subscription } from 'rxjs';
import { first } from 'rxjs/operators';
import { Department } from 'src/app/models/department';
import { Feature } from 'src/app/models/feature';
import { Holiday } from 'src/app/models/holiday';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { HolidayService } from 'src/app/services/holiday.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-dept-config',
  templateUrl: './dept-config.component.html',
  styleUrls: ['./dept-config.component.css']
})
export class DeptConfigComponent implements OnInit {

  //flags 
  isCreation:boolean = false;
  isUpdation: boolean = false;
  isForm: boolean = false;
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

  holidayList:any[] = [];
  mappedHolidayList:any[] = [];
  deptHolidayList:any[] = [];

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private departmentService: DepartmentService,
    private employeeService:EmployeeService,
    private authenticationService : AuthenticationService,
    private holidayService : HolidayService,) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.getAllEmployeeList(); // for HOD List

    // Dynamic Subfeature Flags 
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    this.sectionViewInit();
  }

  sectionViewInit(){
    if(this.userMapping.view_all_department || this.userMapping.update_department || this.userMapping.delete_department){
      //for dept table data 
      this.showTable();
    }
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
    this.getAllHolidays();
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
    this.getHolidayListByDeptId(this.deptObj.deptId);
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
    
    this.deptObj.createdBy = this.currentUser.empId;
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
    
    this.deptObj.updatedBy = this.currentUser.empId;;
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
    department.deptId = this.deptObj.deptId;

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

  getAllEmployeeList(){
    this.hodList = [];

    this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.hodList = response.serviceResponse;
        console.log("hodList : ", this.hodList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  getAllHolidays(){
    this.holidayList = [];
    
    this.holidayService.getAllHolidays().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayList = response.serviceResponse;
        console.log("holidayList : ", this.holidayList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getHolidayListByDeptId(deptId:any){
    this.mappedHolidayList = [];
    let holidayObj:Holiday = new Holiday();
    holidayObj.deptId = deptId;

    this.holidayService.getHolidayListByDeptId(holidayObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.mappedHolidayList = response.serviceResponse;
      } else {
        this.mappedHolidayList = [];
      }
      console.log("mappedHolidayList : ", this.mappedHolidayList);
      this.getActiveHolidays();
    });
  }

  getActiveHolidays(){
    this.deptHolidayList = [];

     this.deptHolidayList = this.holidayList.map(holiday => {
      let mapHoliday = this.mappedHolidayList.find(_holiday => _holiday.holidayId == holiday.holidayId);
      if(mapHoliday){
        holiday.isActive = true;
        holiday.departmentHolidayMapId = mapHoliday.departmentHolidayMapId;
      }else{
        holiday.isActive = false;
        holiday.departmentHolidayMapId = null;
      }
      return holiday;
    });
    console.log("deptHolidayList : ", this.deptHolidayList);
  }

  openUpdateConfimationModal(template: TemplateRef<any>, ){
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
 }

 onUpdateDeptHolidayMapping(template: TemplateRef<any>){
  let updatedHolidays = this.deptHolidayList.filter(holiday => {
    if(holiday.departmentHolidayMapId === null && holiday.isActive == false){
      // to send only manipulated data ...
    }else{
      return holiday;
    }
  });

  let updateDeptObj = new Department();
  updateDeptObj.deptId = this.deptObj.deptId;
  updateDeptObj.holidays = updatedHolidays;

  console.log("Update dept holiday Mapping :",updateDeptObj);
  

  // this.departmentService.updateDepartment(updateDeptObj).pipe(first()).subscribe((response: any) => {
  //   if (response.serviceStatus == "Success") {
  //     this.openAlertMod(template, response.serviceResponse);
  //     this.showTable();
  //   } else {
  //     this.openAlertMod(template, response.serviceResponse);
  //   }
  // });
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
