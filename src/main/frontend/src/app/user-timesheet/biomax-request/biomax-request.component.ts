import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
import { MatCalendarCellClassFunction } from '@angular/material/datepicker';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { biomaxRequest } from 'src/app/models/biomaxRequest';
import { biomaxRequestIssue } from 'src/app/models/biomaxRequestIssue';
import { Feature } from 'src/app/models/feature';
import { Holiday } from 'src/app/models/holiday';
import { Leave } from 'src/app/models/leave';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BiomaxseviceService } from 'src/app/services/biomaxsevice.service';
import { HolidayService } from 'src/app/services/holiday.service';
import { LeaveService } from 'src/app/services/leave.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-biomax-request',
  templateUrl: './biomax-request.component.html',
  styleUrls: ['./biomax-request.component.css']
})
export class BiomaxRequestComponent implements OnInit {

  data:string;
  biomax=new biomaxRequest();

  biomaxList:any[]=[];
  biomaxListEmployeeId:any[]=[];
  biomaxListReportingManager:any[]=[];
  biomaxFilterData=new biomaxRequest();
  biomaxRequestIssueData:biomaxRequestIssue[]=[];
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  popupmessage='';
  //flags 
  isCreation:boolean = false;
  isForm: boolean = false;
  isUpdation:boolean = false;
  isCompOffRequestsTable: boolean = false;
  isApprovalRequest:boolean=false;
  isApprovedRequest:boolean=false;
  approvalType='';
  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  //obj
  feature="Comp off";
  currentUser:User;
  userMapping:any = {};

  excelName = '';
  elementName = '';
  
  holidayList:any[]= [];
  compOffObj:Leave = new Leave();
  compOffReasons:any[] = [];
  allCompOffRequests:any[] = [];
  previousCompOffRequests:any[] = [];
  leaveObj:Leave= new Leave();
  biomaxServiceRequest:biomaxRequestIssue[]=[];
  filters:any = {};
  isSearchEnabled:boolean = false;
  compOffReqColumns:any[] = ['blank','compOffReasons','fromDate','noOfDays','description','status'];
  holidayDates:any[] = [];
  isWeekOffsExcluded:boolean = false;
  weekOffExcludedDepartmentList:any[] = [];
  teamMemberList:any[] = [];

  constructor(
    private validationService:ValidationService,
    private modalService: BsModalService,
    private authenticationService : AuthenticationService,
    private leaveService : LeaveService,
    private biomaxseviceService:BiomaxseviceService,
    private datePipe: DatePipe,
    private locationStrategy: LocationStrategy,
    private holidayService:HolidayService
    ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    this.biomax=new biomaxRequest();
    //console.log(this.feature, this.userMapping);
    this.biomax.biomaxTitle="Request For BioMax";
    this.biomax.reportingManagerId=this.currentUser.reportingManagerId;
    
    this.sectionViewInit();
    this.getAllBiomaxRequestForEmployee();
    this.preventBackButton();
    this.getAllHolidays();
    this.getBioMaxRequestType();
  
  }
  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }
  getBioMaxRequestType(){
    this.biomaxseviceService.getBioMaxRequestType().pipe(first()).subscribe((response:any)=>{
      this.biomaxRequestIssueData=response.serviceResponse;
      console.log(response.serviceResponse);

    })
  }

  sectionViewInit(){
    if(this.userMapping.apply_comp_off_req){
      this.showCreateForm();
    }else if(this.userMapping.view_comp_off_req_status || this.userMapping.update_comp_off_req){
      this.showBioMaxRequestTable();
    }
  }
 
  disableMannualDateInput(){
    return false;
  }

  showCreateForm(){
    this.isForm = true;
    this.isCreation = true;
    this.isUpdation = false;	

    this.isCompOffRequestsTable = false;
    this.isApprovalRequest=false;
    this.isApprovedRequest=false;
    this.reset();
  }

  showUpdateForm(compOff:Leave){	
    this.isForm = true;	
    this.isUpdation = true;	
    this.isCreation = false;	
  
    this.isCompOffRequestsTable = false;

    this.compOffObj = Object.assign({}, compOff);
    this.compOffObj.leaveType = 'Compensatory Off'
    this.compOffObj.fromDate = (this.compOffObj.fromDate)? moment(this.compOffObj.fromDate, AppComponent.DATE_FORMAT).format(AppComponent.DB_DATE_FORMAT) : null;
    this.compOffObj.toDate = (this.compOffObj.toDate)? moment(this.compOffObj.toDate, AppComponent.DATE_FORMAT).format(AppComponent.DB_DATE_FORMAT) : null;

    let selectedReason =  this.compOffReasons.find(compOffReson => compOffReson.compOffReasons == this.compOffObj.compOffReasons);
    if(selectedReason) this.compOffObj.reasonId = selectedReason.compOffId;

    //console.log("For Update Comp-Off : ", this.compOffObj, selectedReason);
  }

 
  showBioMaxApprovalRequestTable(){
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.isApprovalRequest = true;
    this.isCompOffRequestsTable=false;
    this.isApprovedRequest=false;
    this.isForm = false;
    this.isCreation = false;
    this.page=1;
    this.data=''
  }
  showBioMaxApprovedRequestTable(){
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.isApprovedRequest = true;
    this.isApprovalRequest = false;
    this.isCompOffRequestsTable=false;
    this.isForm = false;
    this.isCreation = false;
    this.page=1;
    this.data=''
  
    this.getAllBioMaxRequestForReportingManager();
    
  }

  reset(){
    this.compOffObj = new Leave();
    this.compOffObj.leaveType = 'Compensatory Off'
    this.compOffObj.compOffId = '';

    this.allCompOffRequests = [];
  }

    // Modals
    openAlertMod(template: TemplateRef<any>, message: any) {
      this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
      this.alertMessage = message;
    }
  
    cancelRequest() {
      this.modalRef.hide();
      this.isForm=true;
      this.isCreation=true;
      this.isCompOffRequestsTable=false;
      this.isApprovalRequest=false;
      this.isApprovedRequest=false;
      this.reset();
    }

   

    getAllBiomaxRequestForEmployee(){
      this.biomaxListEmployeeId = [];
  
      this.biomaxseviceService.getByEmployeeId(this.currentUser.empId).pipe(first()).subscribe((response: any) => {
       if(response.serviceStatus=="true"){
        this.biomaxListEmployeeId=response.serviceResponse;
        console.log("sdhbsdhvbsjdhvsjhbdvjhbshj",this.biomaxListEmployeeId);
       }
      });
    }
    getAllBioMaxRequestForReportingManager(){
      this.biomaxListReportingManager=[];
      this.biomaxseviceService.getByReportingManagerEmployeeId(this.currentUser.empId).pipe(first()).subscribe((response: any)=>{
        if(response.serviceStatus=="true"){
          this.biomaxListReportingManager=response.serviceResponse;
         }
      });
    }
    openDeletebiomaxRequest(template: TemplateRef<any>,id:any){
      this.popupmessage="Are you Sure to delete the Bio Max Request";
      this.modalRef = this.modalService.show(template, { class: 'modal-sm' });	
      this.biomax.biomaxreequestId=id;
    }
    showBioMaxRequestTable(){
      this.sortColumn=[];
      this.sortColumnType=[];
      this.sortDirection='';
      this.isCompOffRequestsTable = true;
      this.isApprovalRequest=false;
      this.isApprovedRequest=false;
      this.isForm = false;
      this.isCreation = false;
      this.page=1;
      this.data=''
      this.getAllBiomaxRequestForEmployee();
    }
    openApprovedRequest(template: TemplateRef<any>,id:any,type:any){
    this.popupmessage="Are you sure to "+type+" that request ?";
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.approvalType=type;	
    this.getById(id);
   
    }

    getById(id:number){
      this.biomaxseviceService.getById(id).pipe(first()).subscribe((response:any)=>{
        this.biomaxFilterData=response.serviceResponse;
      })
    }
    approvedOrRejected(template: TemplateRef<any>){
      this.cancelRequest();
      this.biomaxFilterData.biomaxStatus=this.approvalType;
      this.biomaxseviceService.updateBiomaxRequest(this.biomaxFilterData.biomaxreequestId,this.biomaxFilterData).pipe(first()).subscribe((response:any)=>{
        if(response.serviceStatus=="success"){
          this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
          this.alertMessage = response.serviceMessage;
         this.showCreateForm();
        }else{
          this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
          this.alertMessage = response.serviceMessage;
        
         
        }
      })
      
    }


    biomaxrequestDate:Date;
    // Allow Only Past 1 month Days for Comp-off Application
    fromDateFilter = (d: Date)=>{
      const dateFormat = 'YYYY-MM-DD';
      biomaxrequestDate:Date;
      const currentDate = new Date();
      const DAY_IN_MS = 24 * 60 * 60 * 1000;
      let BACKDATED_LEAVE_PERIOD = 5;
      let FUTUREDATED_LEAVE_PERIOD = 30;
      const time=d?.getTime();

      // if(this.currentUser.compOffLockDays){
      //   BACKDATED_LEAVE_PERIOD = this.currentUser.compOffLockDays;
      // }

      // const FUTUREDATED_LEAVE_PERIOD = 180;
      let minDate = new Date(currentDate.getTime() - (BACKDATED_LEAVE_PERIOD * DAY_IN_MS));
      let maxDate = new Date(currentDate.getTime()+ (FUTUREDATED_LEAVE_PERIOD * DAY_IN_MS));

      // if (this.isUpdation) {
      //   this.previousCompOffRequests = this.previousCompOffRequests.filter(compOff => this.datePipe.transform(compOff.fromDate, "yyyy-MM-dd") != this.datePipe.transform(this.compOffObj.fromDate, "yyyy-MM-dd"));
      // }

      // if(this.isUpdation == true){
      //   this.biomaxListEmployeeId = this.biomaxListEmployeeId.filter(x => x.biomaxrequestDate != this.leaveObj.toDate);
      // }
      // if(this.leaveObj.leaveAppliedFor == 'self'){
      //       if(this.leaveObj.leaveTypeCode == 'ML'){
      //         return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.biomaxListEmployeeId.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.biomaxrequestDate).format(dateFormat) ));
      //       }else
      //         if(this.weekOffExcludedDepartmentList.find(deptId => deptId == this.currentUser.departmentId)){
      //           return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.biomaxListEmployeeId.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.biomaxrequestDate).format(dateFormat) ) );
      //         }else{
      //           return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.holidayDates.find(x=>x.getTime()==time) && !this.biomaxListEmployeeId.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.biomaxrequestDate).format(dateFormat) ) );
      //         }
      //     }else{
      //       let teamMember = this.teamMemberList.find(employee => employee.empId == this.leaveObj.empId)
      //       if(this.weekOffExcludedDepartmentList.find(deptId => deptId == teamMember.departmentId)){
      //         return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.biomaxListEmployeeId.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.biomaxrequestDate).format(dateFormat) ));
      //       }else{
      //         return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.holidayDates.find(x=>x.getTime()==time) && !this.biomaxListEmployeeId.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.biomaxrequestDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.biomaxrequestDate).format(dateFormat)));
      //       }
      //     }
     return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.previousCompOffRequests.find(compOffApplication => moment(d).format(dateFormat) >= moment(compOffApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(compOffApplication.toDate).format(dateFormat)));
    }


 getAllHolidays(){
    this.holidayList = [];
    this.holidayDates = [];
    let holidayObj = new Holiday();
    holidayObj.state=this.currentUser.workLocation;
    //console.log(" Worklocation ::  ",this.currentUser);
    this.holidayService.getAllHolidays(holidayObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayList = response.serviceResponse;
        this.holidayDates = this.holidayList.map(holiday => new Date(this.datePipe.transform(holiday.dateOfHoliday, 'MM/dd/yyyy')));
      //   this.holidayDates = this.holidayList.map(holiday => ({
      //     date: new Date(this.datePipe.transform(holiday.dateOfHoliday, 'MM/dd/yyyy')),
      //     state: holiday.state
      // }));
      
        //console.log("holidayDates : ", this.holidayDates); 
        console.log("holidayList : ", this.holidayList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

      holidayHighlight: MatCalendarCellClassFunction<Date> = (cellDate, view) => {
        // //console.log("leaveObj  ::  ",this.leaveObj);
        // Only highligh dates inside the month view.
        if (view === 'month') {
          const time = cellDate.getTime()
          
          // Highlight the holidays.
          if(this.leaveObj.leaveAppliedFor == 'self'){
            if(this.leaveObj.leaveTypeCode == 'ML'){
              this.isWeekOffsExcluded=true;
              return '';
            }else{
              if(this.weekOffExcludedDepartmentList.find(deptId => deptId == this.currentUser.departmentId)){
                return '';
              }else{
                return (this.holidayDates.find(x=>x.getTime()==time)) ? 'holiday-date' : '';
              }
            }
           
          }else{
            let teamMember = this.teamMemberList.find(employee => employee.empId == this.leaveObj.empId)
            if(this.weekOffExcludedDepartmentList.find(deptId => deptId == teamMember.departmentId)){
              return '';
            }else{
              return (this.holidayDates.find(x=>x.getTime()==time)) ? 'holiday-date' : '';
            }
          }
        }
        return '';
      }

  
  

    validateLeavetObj(compOffObj:Leave, template: TemplateRef<any>){
      
      if(!this.validationService.validateNullUndefinedEmptyString(compOffObj.reasonId)){
        this.alertMessage = "Please select comp off reason !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      if(!this.validationService.validateNullUndefinedEmptyString(compOffObj.fromDate)){
        this.alertMessage = "Please select date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
  
      if(!this.validationService.validateNullUndefinedEmptyString(compOffObj.description?.trim())){
        this.alertMessage = "Please enter description !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }else if(!this.validationService.validateActivityTimesheetDiscription(compOffObj.description?.trim())){
        this.alertMessage = "Please enter valid description !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

      return true;
    }

    resetToDate(){
      this.compOffObj.toDate = ''
      this.compOffObj.noOfDays = ''
    }

    onApplyBioMaxRequest(template: TemplateRef<any>){
      this.biomax.empId=this.currentUser.empId;
      if(!this.validationService.validateNullUndefinedEmptyString(this.biomax.biomaxrequestDate)){
        this.alertMessage = "Please fill the Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      if(!this.validationService.validateNullUndefinedEmptyString(this.biomax.requestRemark)){
        this.alertMessage = "Please Fill the Description !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      const dateFormat = "yyyy-MM-dd'T'HH:mm:ss"; // LocalDateTime format
      this.biomax.biomaxrequestDate = this.datePipe.transform(this.biomax.biomaxrequestDate, dateFormat);
      
     this.biomaxseviceService.createBiomaxRequest(this.biomax).pipe(first()).subscribe((response:any)=>{
      console.log(response);
      if(response.serviceStatus=="success"){
        this.alertMessage = "Please fill the Date !!"
        this.openAlertMod(template, response.serviceMessage);
      this.ngOnInit();
      }
     });
     
    }


   
    // onUpdateCompOff(template: TemplateRef<any>){
    //   const dateFormat = 'YYYY-MM-DD';
    //   let inputValidated:boolean  = this.validateLeavetObj(this.compOffObj, template)
    //   if(!inputValidated) return;

    //   let compOff = new Leave();
    //   compOff = Object.assign({}, this.compOffObj);
    //   compOff.description = this.compOffObj.description?.trim();
    //   compOff.fromDate = moment(this.compOffObj.fromDate).format(dateFormat);
    //   // compOff.toDate = moment(this.compOffObj.toDate).format(dateFormat);
    //   compOff.updatedBy = this.currentUser.empId;
    //   // compOff.reportingManagerId = this.currentUser.reportingManagerId
  
    //   if(this.currentUser.reportingManagerId != null && this.currentUser.approvalsTo == "Reporting Manager"){
    //     compOff.reportingManagerId = this.currentUser.reportingManagerId;
    //     compOff.managerEmail = this.currentUser.reportingManagerEmail;
    //     compOff.managerId = this.currentUser.reportingManagerId;
    //     compOff.managerName = this.currentUser.reportingManagerName;
    //   }else{
    //     compOff.reportingManagerId = this.currentUser.managerId;
    //     compOff.managerId = this.currentUser.managerId;
    //     compOff.managerEmail = this.currentUser.managerEmail;
    //     compOff.managerName = this.currentUser.managerName;
    //   }


    //   //console.log("Update comp off : ", compOff);
    //   this.leaveService.updateCompOff(compOff).pipe(first()).subscribe((response: any) => {
    //     if (response.serviceStatus == "Success") {
    //       this.openAlertMod(template, response.serviceResponse);
    //       this.showCompOffRequestTable();
    //     } else {
    //       this.openAlertMod(template, response.serviceResponse);
    //     }
    //   });
    // }

    deletebiomaxRequest(template: TemplateRef<any>) {	
      this.cancelRequest();
  this.biomaxseviceService.deletebiomaxRequest(this.biomax.biomaxreequestId).pipe(first()).subscribe((response: any) => {	
        if (response.serviceStatus == "Success") {	
          this.openAlertMod(template, response.serviceMessage);	
          this.showCreateForm();
        } else {	
          this.openAlertMod(template, response.serviceMessage);	
        }	
      });	
    }


    // download excel


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
  }

  onSearch(searchData){
  this.filters = searchData;
  //console.log("Updated Filter : ", this.filters);
  }

}

  
function compare(a: number | string, b: number | string, isAsc: boolean) {	
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);	
}