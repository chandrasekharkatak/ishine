import { Component, OnInit, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Designation } from 'src/app/models/designation';
import { Feature } from 'src/app/models/feature';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { DestinationService } from 'src/app/services/destination.service';
import { ValidationService } from 'src/app/services/validation.service';
import { AppComponent } from 'src/app/app.component';
import { ExportExcelService } from 'src/app/services/export-excel.service';

@Component({
  selector: 'app-designation-config',
  templateUrl: './designation-config.component.html',
  styleUrls: ['./designation-config.component.css']
})
export class DesignationConfigComponent implements OnInit {

  designationObj: Designation = new Designation();

  feature = "Designation Config";
  currentUser: User;
  userMapping: any = {};

  //modal
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  isCreation: boolean = false;
  isUpdation: boolean = false;
  isForm: boolean = false;
  isTable: boolean = false;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  designationToBeDeleted:any;

  allDesignationList:any[] = [];
  allDeptList:any[] = [];
  allDesignationListForExcel:any[] = [];
  filteredDesignationListByDept:any[] = [];

  filters:any = {};
  isSearchEnabled:boolean = false;
  designationColumns:any[] = ['blank','designationName','createdByName','createdOn','updatedOn','updatedByName'];

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private departmentService: DepartmentService,
    private destinationService: DestinationService,
    private exportExcelService: ExportExcelService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    // Dynamic Subfeature Flags
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);

    this.sectionViewInit();
  }

  sectionViewInit(){
    this.showTable();
  }

  showTable(){
    this.isTable = true;

    this.isCreation = false;
    this.isUpdation = false;
    this.isForm = false;

    this.getAllDesignation();
  }

  showCreateForm(){
    this.isCreation = true;
    this.isForm = true;

    this.isTable = false;
    this.isUpdation = false;

    this.designationObj = new Designation();
    this.getAllDepartmentList();
  }

  showUpdateForm(designation:any){
    this.isUpdation = true;
    this.isForm = true;

    this.isCreation = false;
    this.isTable = false;
    this.getAllDepartmentList();
    this.destinationService.getDesignationById(designation).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        this.designationObj = Object.assign({}, response.serviceResponse);
        this.designationObj.deptIdList = this.designationObj.deptIdList?.map(x=>+x);
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllDepartmentList() {
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  checkDesignationName(designationName, template: TemplateRef<any>){
    let designationObj = new Designation();
    designationObj.designationName = designationName;
    designationObj.designationId = this.designationObj.designationId;

    this.destinationService.checkDesignationName(designationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.designationObj.designationName = '';
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  validateDesignationObj(designationObj : Designation, template: TemplateRef<any>){
    if (!this.validationService.validateNullUndefinedEmptyString(designationObj.designationName)) {
      this.alertMessage = "Please enter Designation name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if (!this.validationService.validateProjectName(designationObj.designationName)) {
      this.alertMessage = "Please enter valid Designation Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(designationObj.deptIdList)) {
      this.alertMessage = "Please select department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(designationObj.deptIdList.length == 0){
      this.alertMessage = "Please select department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  onCreateDesignation(template: TemplateRef<any>){
    this.designationObj.designationName = this.designationObj.designationName.trim();
    let inputValidated: boolean = this.validateDesignationObj(this.designationObj, template)
    if (!inputValidated) return;

    this.designationObj.createdBy = this.currentUser.empId;

    //console.log(this.designationObj, " : this.designationObj");

    this.destinationService.createDesignation(this.designationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllDesignation(){
    this.destinationService.getAllDesignation().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDesignationList = response.serviceResponse;

        this.allDesignationList.forEach((designation) => {
          designation.updatedOn = (designation.updatedOn)? moment(designation.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
          designation.createdOn = (designation.createdOn)? moment(designation.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  onUpdateDesignation(template: TemplateRef<any>){
    this.designationObj.designationName = this.designationObj.designationName.trim();
    let inputValidated: boolean = this.validateDesignationObj(this.designationObj, template)
    if (!inputValidated) return;

    this.designationObj.updatedBy = this.currentUser.empId;

    //console.log(this.designationObj, " : this.designationObj");

    this.destinationService.updateDesignation(this.designationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  onDeleteDesignation(deleteDesignation: TemplateRef<any>, template: TemplateRef<any>){
    this.cancelRequest();

    this.destinationService.deleteDesignation(this.designationToBeDeleted).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.showTable();
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.designationObj.newDesignationId = null;
        this.filteredDesignationListByDept = response.serviceResponse;
        this.filteredDesignationListByDept = this.filteredDesignationListByDept.filter(x => x.designationId != this.designationToBeDeleted.designationId)

        this.modalRef = this.modalService.show(deleteDesignation);
        this.page = 1;
      }
    });
  }

  onChangeEmployeeDesignationMapping(template: TemplateRef<any>, newDesignationId:any){
    this.cancelRequest();

    this.designationToBeDeleted.newDesignationId = newDesignationId;
    this.destinationService.changeEmployeeDesignationMapping(this.designationToBeDeleted).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.showTable();
        this.openAlertMod(template, response.serviceResponse);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  // Modals
  openDeleteDesignation(template: TemplateRef<any>, designation: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.designationToBeDeleted = designation;
    //console.log(this.designationObj);
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  //Export Excel
  name = 'designation.xlsx';
  exportToExcel(){
    this.destinationService.getAllDesignation().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDesignationListForExcel = response.serviceResponse;

        const onlySpecificDataArr = this.allDesignationListForExcel.map(
          x => ({
            "Designation Name": x.designationName,
            "Created By": x.createdByName,
            "Created On": (x.createdOn)? moment(x.createdOn).format(AppComponent.DATE_FORMAT) : null,
            "Updated By": x.updatedByName,
            "Updated On": (x.updatedOn)? moment(x.updatedOn).format(AppComponent.DATE_FORMAT) : null
          })
        )
        this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
      }
    });
  }

  //pagination

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort){
    //console.log(sort);
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
    //console.log("Updated Filter : ", this.filters);
  }

}
