import { Component, OnInit, TemplateRef } from '@angular/core';
import { Employee } from 'src/app/models/employee';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { TeamViewService } from 'src/app/services/team-view.service';
import { first } from 'rxjs/operators';
import { User } from 'src/app/models/user';
import { Feature } from 'src/app/models/feature';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { Sort } from '@angular/material/sort';
import { LocationStrategy } from '@angular/common';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { EmployeeService } from 'src/app/services/employee.service';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';

@Component({
  selector: 'app-team-member',
  templateUrl: './team-member.component.html',
  styleUrls: ['./team-member.component.css']
})
export class TeamMemberComponent implements OnInit {

    data:string;
  feature="Team Members";
  currentUser:User;
  userMapping:any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  //excel
  excelName = '';
  elementName = '';

  viewTeamMemberList: any[] = []; 

  filters:any = {};
  isSearchEnabled:boolean = false;
  teamMemberColumns:any[] = ['blank','employeementId','name','email','jobRoleName','mobileNo'];

  filteredEmployeeAuditHistory:any[] = [];
  employeeAuditHistory:any[] = [];
  isFullJourneyAccordianBody: boolean = false;
  isLifeCycleAccordianBody: boolean = false;
  isKycUpdateAccordianBody: boolean = false;
  isEmployeeInfoAccordianBody: boolean = false;
  isTeamProjectAccordianBody: boolean = false;
  lifeCycleChangeList:any[] = [];
  teamProjectChangeList:any[] = [];
  kycUpdateList:any[] = [];
  employeeInfoChangeList:any[] = [];
  allApplicationList : any[] =[]
  modalRef: BsModalRef = new BsModalRef();
  alertMessage: any;
  auditFilter:any = {};
  isAuditSearchEnabled:boolean = false;
  employeeAuditColumns:any[] = ['blank', 'date', 'field', 'value', 'bucketName', 'updatedByName'];
  isTable: boolean = false;


  constructor(
    private authenticationService : AuthenticationService,
    private teamViewService : TeamViewService,
    private exportExcelService: ExportExcelService,
    private employeeService : EmployeeService,
    private locationStrategy: LocationStrategy,
    private modalService: BsModalService,
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);
    
    this.getAllTeamMemberView();
    this.preventBackButton();
  }
  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }

  getAllTeamMemberView(){
    this.viewTeamMemberList = []

    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    this.teamViewService.getAllTeamMemberView(employeeObj).pipe(first()).subscribe((response : any) => {
      if (response.serviceStatus == "Success") {
        this.viewTeamMemberList = response.serviceResponse;
        for(let x of this.viewTeamMemberList){
          x.employeementId="A-".concat(x.employeementId)
        }
        //console.log("viewTeamMemberList : ", this.viewTeamMemberList);
      } else {
        console.error(response.serviceResponse);
      }
    });

  }

  //excel

  exportToExcel(): void {

    this.excelName = "TeamMemberSheet.xlsx";

      const onlySpecificDataArr = this.viewTeamMemberList.map(
        x => ({
          "Employee Id": x.employeementId,
          "Name": x.name,
          "Email": x.email,
          "Job Role": x.jobRoleName,
          "Mobile No": x.mobileNo
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
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

  resetAuditSearchFilter(event, title?:any){
    this.isAuditSearchEnabled = false;
    this.auditFilter = {};

    if(title == 'Full Journey'){
      this.isFullJourneyAccordianBody = true;
    }else{
      this.isFullJourneyAccordianBody = false;
    }
    if(title == 'Life Cycle Change'){
      this.isLifeCycleAccordianBody = true;
    }else{
      this.isLifeCycleAccordianBody = false;
    }
    if(title == 'Kyc Update'){
      this.isKycUpdateAccordianBody = true;
    }else{
      this.isKycUpdateAccordianBody = false;
    }
    if(title == 'Employee Info Change'){
      this.isEmployeeInfoAccordianBody = true;
    }else{
      this.isEmployeeInfoAccordianBody = false;
    }
    if(title == 'Team/Project Change'){
      this.isTeamProjectAccordianBody = true;
    }else{
      this.isTeamProjectAccordianBody = false;
    }
  }


  getEmployeeAuditInfo(employee:any, auditTemplate: TemplateRef<any>, template: TemplateRef<any>){
    this.filteredEmployeeAuditHistory = [];
    this.employeeAuditHistory = [];
    this.filters = {};
    this.isEmployeeInfoAccordianBody = false;
    this.isTeamProjectAccordianBody = false;
    this.isKycUpdateAccordianBody  = false;
    this.isLifeCycleAccordianBody = false;
    this.isFullJourneyAccordianBody = false;

    let employeeObj = new Employee();
    employeeObj.empId = employee.empId;

    this.employeeService.getEmployeeAuditInfo(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeAuditHistory = response.serviceResponse;

        Object.keys(this.employeeAuditHistory).forEach(key => {
          let obj = this.employeeAuditHistory[key];
          Object.keys(obj).forEach(innerKey => {
            if(obj[innerKey] !== null){
              let newObj = {
                "date": key,
                "field": innerKey,
                "value": obj[innerKey],
                "bucketName": obj["bucketName"],
                "updatedByName": obj["updatedByName"] ? obj["updatedByName"] : null,
                "createdByName": obj["createdByName"] ? obj["createdByName"] : null,
                "color": '#FFFFFF'
              };

              if(newObj.field !== 'bucketName' && newObj.field !== 'designationId' && newObj.field !== 'jobRoleId'
               && newObj.field !== 'createdBy' && newObj.field !== 'updatedBy' &&  newObj.field !== 'departmentId'
               &&  newObj.field !== 'updatedByName' &&  newObj.field !== 'createdOn' &&  newObj.field !== 'createdByName'
               &&  newObj.field !== 'updatedOn' &&  newObj.field !== 'empId' &&  newObj.field !== 'teamId' &&  newObj.field !== 'employeeTeamMapId'
               &&  newObj.field !== 'teamLeadId' && newObj.field !== 'reportingManagerId' && newObj.field !== 'managerId'){

                if(newObj.field == 'employmentstatus'){
                  newObj.bucketName = 'Lifecycle Changes';
                }

                if(newObj.bucketName == 'Employment Info Changes'){
                  newObj.color = '#9FE2BF';
                }else if(newObj.bucketName == 'Lifecycle Changes'){
                  newObj.color = '#40E0D0';
                }else if(newObj.bucketName == 'KYC Update'){
                  newObj.color = '#CCCCFF';
                }else if(newObj.bucketName == 'Team/Project Changes'){
                  newObj.color = '#F1948A';
                }

                if(newObj.field == 'active'){
                  newObj.value = newObj.value == '1' ? 'Yes' : 'No';
                }

                if(newObj.field == 'startDate'){
                  newObj.value = (newObj.value)? moment(newObj.value).format(AppComponent.DATETIME_FORMAT) : null;
                }

                newObj.date = ( newObj.date)? moment( newObj.date).format(AppComponent.DATETIME_FORMAT) : null;

                if(newObj.field == 'dateOfRelieving' || newObj.field == 'dateOfResign'){
                  newObj.value = (newObj.value)? moment(newObj.value).format(AppComponent.DATE_FORMAT) : null;
                }

                const fieldConversion = newObj.field.replace(/([A-Z])/g, " $1");
                const finalField = fieldConversion.charAt(0).toUpperCase() + fieldConversion.slice(1);

                newObj.field = finalField;

                this.filteredEmployeeAuditHistory.push(newObj);
              }
            }
          });
        });

        this.filteredEmployeeAuditHistory.sort((a, b) => (b.date > a.date) ? 1 : -1);

        this.filteredEmployeeAuditHistory.forEach((object) => {
          let date;
          if(object.bucketName == 'Team/Project Changes'){
            if(object.field == 'Active' && object.value == 'No'){
              date = object.date;
            }
            let updateField = this.filteredEmployeeAuditHistory.find(x => x.date == date && x.field == 'Start Date');
              if(updateField){
                updateField.field = 'End Date';
              }
          }
        });

        this.lifeCycleChangeList = this.filteredEmployeeAuditHistory.filter(x => x.bucketName == 'Lifecycle Changes');
        this.teamProjectChangeList = this.filteredEmployeeAuditHistory.filter(x => x.bucketName == 'Team/Project Changes');
        this.kycUpdateList = this.filteredEmployeeAuditHistory.filter(x => x.bucketName == 'KYC Update');
        this.employeeInfoChangeList = this.filteredEmployeeAuditHistory.filter(x => x.bucketName == 'Employment Info Changes');

        this.modalRef =  this.modalService.show(auditTemplate, { class: 'modal-lg' });

        console.log(this.filteredEmployeeAuditHistory , " : this.filteredEmployeeAuditHistory ");
      } else {
        console.log(response.serviceResponse, " audit response");
      }
    });
  }


  cancelRequest() {
    this.modalRef.hide();
  }
  pageNo =1;
  handleAuditPageChanges(event) {
    this.pageNo = event;
  }

  toggleAuditSearch(){
    this.isAuditSearchEnabled = !this.isAuditSearchEnabled;
  }

  onAuditSearch(searchData){
    this.auditFilter = searchData;
    console.log("Audit Updated Filter : ", this.filters);
  }




}

function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}
