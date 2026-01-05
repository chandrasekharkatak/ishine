import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
import { MatCalendarCellClassFunction } from '@angular/material/datepicker';
import { Sort } from '@angular/material/sort';
import { Router, ActivatedRoute } from '@angular/router';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
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
import { LogService } from 'src/app/services/log.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  standalone: false,
  selector: 'app-biomax-approval',
  templateUrl: './biomax-approval.component.html',
  styleUrls: ['./biomax-approval.component.css']
})
export class BiomaxApprovalComponent implements OnInit {

  data:string;
  biomax=new biomaxRequest();

  biomaxList:any[]=[];
  biomaxListEmployeeId:any[]=[];
  biomaxListEmployeeIdPedning:any[]=[];
  biomaxListEmployeeIdApproved:any[]=[];
  biomaxRequestType:biomaxRequestIssue[]=[];
  biomaxListReportingManager:any[]=[];
  biomaxFilterData=new biomaxRequest();
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
  modalRef:NgbModalRef;

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


  filters:any = {};
  isSearchEnabled:boolean = false;
  compOffReqColumns:any[] = ['blank','compOffReasons','fromDate','noOfDays','description','status'];
  holidayDates:any[] = [];
  isWeekOffsExcluded:boolean = false;
  weekOffExcludedDepartmentList:any[] = [];
  teamMemberList:any[] = [];
  applicableDates: any[];
  // dateList: any[];

  constructor(
    private validationService:ValidationService,
    private modalService: NgbModal,
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
   // this.biomax.biomaxTitle="Request For BioMax";
    this.biomax.reportingManagerId=this.currentUser.managerId;
    this.getAllHolidays();
    this.getAllBiomaxRequestForEmployee();
    this.sectionViewInit();
    this.preventBackButton();
    this.getBioMaxRequestType();
    console.log("Leave",this.leaveObj);
  }
  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
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
  disableMannualDateInput1(){
    return false;
  }
  showCreateForm(){
    this.isForm = true;
    this.isCreation = true;
    this.isUpdation = false;

    this.isCompOffRequestsTable = false;
    this.isApprovalRequest=false;
    this.isApprovedRequest=false;

  }

  formatDate(timestamp: string | number): Date {
    return new Date(timestamp);
  }

  fromDateFilterDatePicker = (date: Date): boolean => {
    return this.applicableDates.some(d => this.formatDate(d).toDateString() === date.toDateString());
  };



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



    // Modals
    openAlertMod(template: TemplateRef<any>, message: any) {
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
      this.alertMessage = message;
    }

    cancelRequest() {
      this.modalRef?.close();
    }
    CronJobs(){
      this.biomaxseviceService.getBioMaxRequestTypeCronJon().pipe(first()).subscribe((ressponse:any)=>{
        console.log(ressponse);
      })
    }


    getAllBiomaxRequestForEmployee(){
      this.biomaxListEmployeeId = [];

      this.biomaxseviceService.getByEmployeeId(this.currentUser.empId).pipe(first()).subscribe((response: any) => {
       if(response.serviceStatus=="true"){
        this.biomaxListEmployeeId=response.serviceResponse;
        this.applicableDates = this.biomaxListEmployeeId[0].applicableDates;
        console.log(this.applicableDates ,":applicableDates ");
        this.biomaxListEmployeeIdPedning= this.biomaxListEmployeeId.filter((p)=>p.biomaxStatus==="Pending")
     this.biomaxListEmployeeIdApproved=this.biomaxListEmployeeId.filter((p)=>p.biomaxStatus==="Approved" || p.biomaxStatus==="Rejected")

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
      this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
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
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.approvalType=type;
    this.getById(id);

    }
    ProjectClone(){
      this.biomaxseviceService.getBioMaxRequestTpoprojectclone().pipe(first()).subscribe((response:any)=>{
        console.log(response);
      })
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
          this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
          this.alertMessage = response.serviceMessage;
          this.ngOnInit();
        }else{
          this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
          this.alertMessage = response.serviceMessage;


        }
      })

    }


    biomaxrequestDate:Date;
    // Allow Only Past 1 month Days for Comp-off Application
    fromDateFilter = (d: Date)=>{
        const dateFormat = 'YYYY-MM-DD';
        const currentDate = new Date();
        const DAY_IN_MS = 24 * 60 * 60 * 1000;
        let BACKDATED_LEAVE_PERIOD = 30;
        let FUTUREDATED_LEAVE_PERIOD = 180;
        const time=d?.getTime();

        if(this.currentUser.leaveBackdatedLockDays){
          BACKDATED_LEAVE_PERIOD = this.currentUser.leaveBackdatedLockDays;
        }
        if(this.currentUser.leaveFuturedatedLockDays){
          FUTUREDATED_LEAVE_PERIOD = this.currentUser.leaveFuturedatedLockDays;
        }

        let minDate = new Date(currentDate.getTime() - (BACKDATED_LEAVE_PERIOD * DAY_IN_MS));
        let maxDate = new Date(currentDate.getTime() + (FUTUREDATED_LEAVE_PERIOD * DAY_IN_MS));


        if(this.leaveObj.leaveAppliedFor == 'self'){
          if(this.leaveObj.leaveTypeCode == 'ML'){
            return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.biomaxListEmployeeIdPedning.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
          }else
            if(this.weekOffExcludedDepartmentList.find(deptId => deptId == this.currentUser.departmentId)){
              return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.biomaxListEmployeeIdPedning.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
            }else{
              return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.holidayDates.find(x=>x.getTime()==time) && !this.biomaxListEmployeeIdPedning.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
            }
        }else{
          let teamMember = this.teamMemberList.find(employee => employee.empId == this.leaveObj.empId)
          if(this.weekOffExcludedDepartmentList.find(deptId => deptId == teamMember.departmentId)){
            return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.biomaxListEmployeeIdPedning.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
          }else{
            return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.holidayDates.find(x=>x.getTime()==time) && !this.biomaxListEmployeeIdPedning.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
          }
        }


      }
    tobiomaxrequestDate:Date;
    // Allow Only Past 1 month Days for Comp-off Application
    tofromDateFilter = (d: Date)=>{
      const dateFormat = 'YYYY-MM-DD';
      const currentDate = new Date();
      const DAY_IN_MS = 24 * 60 * 60 * 1000;
      let BACKDATED_LEAVE_PERIOD = 30;
      let FUTUREDATED_LEAVE_PERIOD = 180;
      const time=d?.getTime();

      if(this.currentUser.leaveBackdatedLockDays){
        BACKDATED_LEAVE_PERIOD = this.currentUser.leaveBackdatedLockDays;
      }
      if(this.currentUser.leaveFuturedatedLockDays){
        FUTUREDATED_LEAVE_PERIOD = this.currentUser.leaveFuturedatedLockDays;
      }

      let minDate = new Date(currentDate.getTime() - (BACKDATED_LEAVE_PERIOD * DAY_IN_MS));
      let maxDate = new Date(currentDate.getTime() + (FUTUREDATED_LEAVE_PERIOD * DAY_IN_MS));


      if(this.leaveObj.leaveAppliedFor == 'self'){
        if(this.leaveObj.leaveTypeCode == 'ML'){
          return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.biomaxListEmployeeIdPedning.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
        }else
          if(this.weekOffExcludedDepartmentList.find(deptId => deptId == this.currentUser.departmentId)){
            return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.biomaxListEmployeeIdPedning.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
          }else{
            return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.holidayDates.find(x=>x.getTime()==time) && !this.biomaxListEmployeeIdPedning.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
          }
      }else{
        let teamMember = this.teamMemberList.find(employee => employee.empId == this.leaveObj.empId)
        if(this.weekOffExcludedDepartmentList.find(deptId => deptId == teamMember.departmentId)){
          return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.biomaxListEmployeeIdPedning.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
        }else{
          return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)) && !this.holidayDates.find(x=>x.getTime()==time) && !this.biomaxListEmployeeIdPedning.find(leaveApplication => moment(d).format(dateFormat) >= moment(leaveApplication.fromDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(leaveApplication.toDate).format(dateFormat)));
        }
      }


    }

 getAllHolidays(){
    this.holidayList = [];
    this.holidayDates = [];
    let holidayObj = new Holiday();
    holidayObj.state=this.currentUser.workLocation;
    this.holidayService.getAllHolidays(holidayObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayList = response.serviceResponse;
        this.holidayDates = this.holidayList.map(holiday => new Date(this.datePipe.transform(holiday.dateOfHoliday, 'MM/dd/yyyy')));

        console.log("holidayList : ", this.holidayList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

      holidayHighlight: MatCalendarCellClassFunction<Date> = (cellDate, view) => {
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






    resetToDate(){
      this.compOffObj.toDate = ''
      this.compOffObj.noOfDays = ''
    }

    onApplyBioMaxRequest(template: TemplateRef<any>){
      this.biomax.empId=this.currentUser.empId;
      if(!this.validationService.validateNullUndefinedEmptyString(this.biomax.biomaxTitle)){
        this.alertMessage = "Please Select Request Type !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      if(!this.validationService.validateNullUndefinedEmptyString(this.biomax.biomaxrequestDate)){
        this.alertMessage = "Please fill from Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      if(!this.validationService.validateNullUndefinedEmptyString(this.biomax.tobiomaxrequestDate)){
        this.alertMessage = "Please fill to  Date !!"
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
      this.biomax.tobiomaxrequestDate = this.datePipe.transform(this.biomax.tobiomaxrequestDate, dateFormat);

     this.biomaxseviceService.createBiomaxRequest(this.biomax).pipe(first()).subscribe((response:any)=>{
      console.log(response);
      if(response.serviceStatus=="success"){
        this.alertMessage = "Please fill the Date !!"
        this.openAlertMod(template, response.serviceMessage);
      this.ngOnInit();
      this.isCompOffRequestsTable=true;
      this.isForm=false;
      }
     });


    }
    getBioMaxRequestType(){
      this.biomaxseviceService.getBioMaxRequestType().pipe(first()).subscribe((response:any)=>{
        this.biomaxRequestType=response.serviceResponse;

      })
     }



    deletebiomaxRequest(template: TemplateRef<any>) {
      this.cancelRequest();

  this.biomaxseviceService.deletebiomaxRequest(this.biomax.biomaxreequestId).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
           this.openAlertMod(template, response.serviceMessage);
          this.ngOnInit();
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
