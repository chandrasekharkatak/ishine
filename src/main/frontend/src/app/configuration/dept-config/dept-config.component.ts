import { Component, OnDestroy, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Subscription } from 'rxjs';
import { first } from 'rxjs/operators';
import { Department } from 'src/app/models/department';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { Holiday } from 'src/app/models/holiday';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { HolidayService } from 'src/app/services/holiday.service';
import { ValidationService } from 'src/app/services/validation.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { Sort } from '@angular/material/sort';
import { LocationStrategy } from '@angular/common';
import { AppComponent } from 'src/app/app.component';
import * as moment from 'moment';
import { ColFilterPipe } from 'src/app/col-filter.pipe';


@Component({
  selector: 'app-dept-config',
  templateUrl: './dept-config.component.html',
  styleUrls: ['./dept-config.component.css']
})
export class DeptConfigComponent implements OnInit {

  //flags
  isCreation: boolean = false;
  isUpdation: boolean = false;
  isForm: boolean = false;
  isTable: boolean = false;
  data:string;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  //modal
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  //obj
  deptObj: Department = new Department();
  employeeObj: Employee = new Employee();
  allDeptList: any;
  filterAllDeptList: any;
  hodList: any = [];
  hodListFilter: any = [];
  oldDepartment: any;
  newDepartment: any;
  onDeleteDepartmentResponse: any;

  //excel
  departmentDataForExcel: any[];
  name = 'Department.xlsx';

  feature = "Department Config";
  currentUser: User;
  userMapping: any = {};


  filters:any = {};
  isSearchEnabled:boolean = false;
  departmentColumns:any[] = ['blank','name','hodName','createdByName','createdOn','updatedOn','updatedByName'];

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private departmentService: DepartmentService,
    private employeeService: EmployeeService,
    private authenticationService: AuthenticationService,
    private holidayService: HolidayService,
    private exportExcelService: ExportExcelService,
    private locationStrategy:LocationStrategy) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.getHODList(); // for HOD List

    // Dynamic Subfeature Flags
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    this.sectionViewInit();
    this.preventBackButton();
  }
  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit() {
    // if(this.userMapping.create_department){
    //   this.showCreateForm();
    // }
    // else if (this.userMapping.view_all_department || this.userMapping.update_department || this.userMapping.delete_department) {
    //   //for dept table data
    //   this.showTable();
    // }
    if (this.userMapping.view_all_department || this.userMapping.update_department || this.userMapping.delete_department) {
      //for dept table data
      this.showTable();
    }
  }

  showCreateForm() {
    this.isForm = true;
    this.isCreation = true;

    this.isTable = false;
    this.isUpdation = false;

    this.reset();
  }

  showTable() {
    this.isTable = true;

    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;

    this.getAllDepartmentList();
  }

  reset() {
    this.deptObj = new Department();
    //Deafult values for dropdown
    this.deptObj.hodId = '';

    this.allDeptList = [];
  }

  showUpdateForm(department: Department) {
    this.isForm = true;
    this.isTable = false;
    this.isUpdation = true;
    this.isCreation = false;

    this.deptObj = Object.assign({}, department)
  }

  validateDepartmentObj(deptObj: Department, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(deptObj.name)) {
      this.alertMessage = "Please enter Department Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } else if (!this.validationService.validateDepartmentName(deptObj.name)) {
      this.alertMessage = "Please enter Valid Department Name!!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }


    if (!this.validationService.validateNullUndefinedEmptyString(deptObj.hodId)) {
      this.alertMessage = "Please select Head of Department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  // CRUD
  onCreateDepartment(template: TemplateRef<any>) {
    this.deptObj.name = this.deptObj.name.trim();
    let inputValidated: boolean = this.validateDepartmentObj(this.deptObj, template)
    if (!inputValidated) return;

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

  onUpdateDepartment(template: TemplateRef<any>) {
    this.deptObj.name = this.deptObj.name.trim();
    let inputValidated: boolean = this.validateDepartmentObj(this.deptObj, template)
    if (!inputValidated) return;

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

  onDeleteDepartment(template: TemplateRef<any>, alertTemplate: TemplateRef<any>) {
    this.cancelRequest();
    this.onDeleteDepartmentResponse = null;
    this.filterAllDeptList = this.allDeptList.filter(x => x.deptId !== this.deptObj.deptId);

    //! Need to check this
    let department: Department = new Department();
    department.deptId = this.deptObj.deptId;
    this.oldDepartment = this.deptObj.deptId;

    this.departmentService.deleteDepartment(department).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(alertTemplate, response.serviceResponse);
        this.showTable();
        this.page=1;
      } else if(response.serviceStatus == "Fail") {
        this.deptObj.newDeptId = '';
        this.onDeleteDepartmentResponse = response.serviceResponse;
        this.modalRef = this.modalService.show(template);
      }else{
        this.openAlertMod(alertTemplate, response.serviceResponse);
      }
    });
  }

  validateChangeDepartmentMappingObj(deptObj: Department, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(deptObj.newDeptId)) {
      this.alertMessage = "Please select a Department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  onChangeDepartmentJobRoleMapping(template: TemplateRef<any>) {
    this.cancelRequest();
    let inputValidated: boolean = this.validateChangeDepartmentMappingObj(this.deptObj, template)
    if (!inputValidated) return;

    let department: Department = new Department();
    department.deptId = this.deptObj.newDeptId;
    department.oldDeptId = this.oldDepartment;
    department.isDeptUsedInIshine = this.onDeleteDepartmentResponse.isDeptUsedInIshine;
    department.isDeptUsedInPoPortal = this.onDeleteDepartmentResponse.isDeptUsedInPoPortal;

    console.log(department, " : department");

    this.departmentService.changeDepartmentJobRoleMapping(department).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        department.deptId = this.oldDepartment;
        this.departmentService.deleteDepartment(department).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            this.openAlertMod(template, response.serviceResponse);
            this.showTable();
            this.page=1;
          } else {
            this.openAlertMod(template, response.serviceResponse);
          }
        });
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllDepartmentList() {
    this.data=''
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
        this.allDeptList.forEach(dept => {
          dept.createdOn = (dept.createdOn)? moment(dept.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          dept.updatedOn = (dept.updatedOn)? moment(dept.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        console.log("allDeptList : ", this.allDeptList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  // getAllEmployeeList() {
  //   this.hodList = [];

  //   this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.hodList = response.serviceResponse;
  //       console.log("hodList : ", this.hodList)
  //       this.hodListFilter =  this.hodList.filter(x => x.jobRoleName.includes("-HOD"));
  //     } else {
  //       alert(response.serviceResponse)
  //     }
  //   });
  // }

  getHODList() {
    this.hodList = [];
    let employeeList = [];

    this.employeeObj.role = "HOD";
    this.employeeService.getAllEmployeesByRole(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        employeeList = response.serviceResponse;
        console.log("employeeList By Role : ", employeeList)
        this.hodList = employeeList;
        console.log("managerList : ", this.hodList)
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  checkDepartmentName(deptName:any, template: TemplateRef<any>){

    let deptObj = new Department();
    deptObj.name = deptName;
    deptObj.deptId = this.deptObj.deptId;

    this.departmentService.checkDepartmentName(deptObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.deptObj.name = '';
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  openUpdateConfimationModal(template: TemplateRef<any>,) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  // download excel
  exportToExcel(): void {

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.departmentDataForExcel = response.serviceResponse;
        console.log("response.serviceResponse: ",response.serviceResponse);
      }

      const onlySpecificDataArr = this.departmentDataForExcel.map(
        x => ({
          "Department Name": x.name,
          "Head of Department": x.hodName,
          "Created by": x.createdByName,
          "Created on": (x.createdOn)? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null,
          "Updaeted by": x.updatedByName ??' - ',
          "Updated On": (x.updatedOn)? moment(x.updatedOn).format(AppComponent.DATETIME_FORMAT) : ' - ',
        })
      )
      //console.log("Excel Array: ",onlySpecificDataArr);
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
    });
  }


  // Modals
  openDeleteDepartment(template: TemplateRef<any>, department: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.deptObj = department;
    console.log(this.deptObj);
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  //pagination

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort){
    console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  onSearch(searchData){
    this.filters = searchData;
    console.log("Updated Filter : ", this.filters);
  }
}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);

}

