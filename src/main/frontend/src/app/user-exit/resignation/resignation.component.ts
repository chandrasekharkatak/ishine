import { Component, OnInit, SecurityContext, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { DomSanitizer } from '@angular/platform-browser';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Employee } from 'src/app/models/employee';
import { EmployeeExit } from 'src/app/models/employeeExit';
import { Feature } from 'src/app/models/feature';
import { Log } from 'src/app/models/log';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExitService } from 'src/app/services/exit.service';
import { LogService } from 'src/app/services/log.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-resignation',
  templateUrl: './resignation.component.html',
  styleUrls: ['./resignation.component.css']
})
export class ResignationComponent implements OnInit {

  modalRef: BsModalRef = new BsModalRef();
  alertMessage: any;

  feature="Resignation";
  userMapping:any = {};
  log:Log;

  currentUser:User;
  employeeExitObj: EmployeeExit = new EmployeeExit();
  employeeInfoObj: Employee = new Employee();

  applicationToBeApproved: any;
  applicationToBeRejected: any;
  allProjectCount: any;
  viewedApplication: any;

  isViewEmployeeInfo: boolean = false;
  isViewProject: boolean = false;
  isViewRating:boolean = false;
  isViewBilling:boolean = false;

  allResignationApplicationList:any[] = [];
  projectList:any[] = [];

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  filters:any = {};
  isSearchEnabled:boolean = false;
  resignationColumns:any[] = ['blank','employmentId','name','resignationStatus','statusUpdatedByName','createdOn', 'statusUpdatedOn'];
  projectColumns:any[] = ['blank', 'projectName', 'teamName', 'active', 'startDate', 'endDate'];

  constructor(
    private modalService: BsModalService,
    private exitService : ExitService,
    private authenticationService : AuthenticationService,
    public validationService: ValidationService,
    private sanitizer: DomSanitizer,
    private logService:LogService,
    private employeeService:EmployeeService,
  ) {this.authenticationService.currentUser.subscribe(x => this.currentUser = x)}

  ngOnInit(): void {
    this.getAllResignationApplication();
    
     // Dynamic Subfeature Flags 
     let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
     featureMap.subFeatures?.forEach(sub => {
       this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
     });
     console.log(this.feature, this.userMapping);
  }

  viewEmployeeInfo(resignation: any){
    this.isViewEmployeeInfo = true;
    this.viewedApplication = resignation;
    this.resetSearch();
    this.onGetEmployeeInfo(resignation);
  }

  goToApplicationPage(){
    this.isViewProject = false;
    this.resetSearch();
    this.getAllResignationApplication();
  }

  getAllResignationApplication(){
    this.isViewEmployeeInfo = false;

    this.exitService.getAllResignationApplication().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allResignationApplicationList = response.serviceResponse;

        this.allResignationApplicationList.forEach((object) => {
          object.employmentId = "A-".concat(object.employmentId);
          object.createdOn = (object.createdOn)? moment(object.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
          object.statusUpdatedOn = (object.statusUpdatedOn)? moment(object.statusUpdatedOn).format(AppComponent.DATETIME_FORMAT) : null;
        });

        this.allResignationApplicationList = this.allResignationApplicationList.sort((a, b) => (new Date(a.createdOn).getTime() < new Date(b.createdOn).getTime()) ? 1 : -1);
      }else{
        console.error(response.serviceResponse);
      }
    });
  }

  async onGetEmployeeInfo(resignation: any){

    if(resignation.employmentId.startsWith('A-')){
      resignation.employmentId  = resignation.employmentId.substring(2);
    }

    this.employeeInfoObj = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = resignation.empId;

    console.log(currentEmp.empId, " : currentEmp.empId");
    
    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.employeeInfoObj = response.serviceResponse;

      this.openProjectView();

      if(resignation.resignationStatus != 'Approved'){
        this.employeeInfoObj.dateOfResign = resignation.createdOn;
      }
      this.employeeInfoObj.resignationStatus = resignation.resignationStatus;
    
      console.log("currentEmployeeInfo : ", this.employeeInfoObj);
      this.loadProfileImage(this.employeeInfoObj.imageBytes);
    } else {
      console.error(response.serviceResponse);
    }
  }

  loadProfileImage(imageByte:any){
    let imageElement = document.getElementById('user-avatar');
    if(imageByte){
      let objectURL = 'data:image/*;base64,' + imageByte;
        let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
        imageElement.setAttribute("src", src);
    }else{
      imageElement.setAttribute("src", "assets/Images/default-user-image.jpeg");
    }
  }

  approveResignationApplication(template: TemplateRef<any>){
    this.cancelRequest();

    let resignation: EmployeeExit = this.applicationToBeApproved;

    resignation.statusUpdatedBy = this.currentUser.empId;
    if(resignation.employmentId.startsWith('A-')){
      resignation.employmentId  = resignation.employmentId.substring(2);
    }

    this.exitService.approveResignationApplication(resignation).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getAllResignationApplication();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  rejectResignationApplication(template: TemplateRef<any>){
    this.cancelRequest();

    if (!this.validationService.validateAlphabetAtLeastTwoCharacter(this.employeeExitObj.rejectReason)) {
      this.alertMessage = "Please enter valid resignation reject reason!!"
      this.openAlertMod(template, this.alertMessage);
      return;
  }

    this.applicationToBeRejected.statusUpdatedBy = this.currentUser.empId;
    this.applicationToBeRejected.rejectReason = this.employeeExitObj.rejectReason;
    if(this.applicationToBeRejected.employmentId.startsWith('A-')){
      this.applicationToBeRejected.employmentId  = this.applicationToBeRejected.employmentId.substring(2);
    }

    this.exitService.rejectResignationApplication(this.applicationToBeRejected).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getAllResignationApplication();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  revokeResignationApplication(template: TemplateRef<any>){
    this.cancelRequest();

    let employeeExitObj = new EmployeeExit();
    employeeExitObj.empId = this.employeeInfoObj.empId;
    employeeExitObj.statusUpdatedBy = this.currentUser.empId;
    employeeExitObj.employeeResignationId = this.viewedApplication.employeeResignationId;

    this.exitService.revokeResignationApplication(employeeExitObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getAllResignationApplication();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  openProjectView(){
    this.isViewProject = !this.isViewProject;

    if(this.isViewProject){
      this.getAllProjectByEmpId();
    }
  }

  getAllProjectByEmpId(){
    let employeeExitObj = new EmployeeExit();
    employeeExitObj.empId = this.employeeInfoObj.empId;

    this.exitService.getAllProjectByEmpId(employeeExitObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectList = response.serviceResponse;

        const key = 'projectName';
        this.allProjectCount = [...new Map(this.projectList.map(item => [item[key], item])).values()].length;

        this.projectList.forEach((object) => {
          object.startDate = (object.startDate)? moment(object.startDate).format(AppComponent.DATETIME_FORMAT) : null;
          object.endDate = (object.endDate)? moment(object.endDate).format(AppComponent.DATETIME_FORMAT) : null;
          object.active  = (object.active  == '1') ? 'Yes' : 'No';
        });

        console.log(" this.projectList : ", this.projectList);
      }else{
        console.error(response.serviceResponse);
      }
    });
  }

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

  resetSearch(){
    this.isSearchEnabled = false;
    this.filters = {};
  }

  onSearch(searchData){
    this.filters = searchData;
    console.log("Updated Filter : ", this.filters);
  }

  // Modal

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openRejectResignationModal(template: TemplateRef<any>, resignationObj:any){
    this.employeeExitObj = new EmployeeExit();
    this.modalRef = this.modalService.show(template);
    this.applicationToBeRejected = resignationObj;
  }

  openApproveResignationModal(template: TemplateRef<any>, resignationObj:any){
    this.modalRef = this.modalService.show(template);
    this.applicationToBeApproved = resignationObj;
  }

  openPreviewDocument(template: TemplateRef<any>){
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  cancelRequest() {
    this.modalRef.hide();
  }

}
