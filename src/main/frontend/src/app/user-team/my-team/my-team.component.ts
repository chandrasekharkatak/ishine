import { Component, OnInit, TemplateRef} from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { Leave } from 'src/app/models/leave';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { TeamViewService } from 'src/app/services/team-view.service';
import { LeaveService } from 'src/app/services/leave.service';
import { User } from 'src/app/models/user';
import { Feature } from 'src/app/models/feature';

@Component({
  selector: 'app-my-team',
  templateUrl: './my-team.component.html',
  styleUrls: ['./my-team.component.css']
})
export class MyTeamComponent implements OnInit {

  feature="My Team";
  currentUser:User;
  userMapping:any = {};

  // modal
  alertMessage:any
  modalRef: BsModalRef = new BsModalRef();

  // flags
  isViewTeam: boolean = true;
  isTeamLeaveHistory: boolean = false;
  isTeamRequest: boolean = false;
  isLeaveRequest: boolean = true;
  isCompOffRequest: boolean = false;

  // Obj
  employeeObj:Employee = new Employee();
  teamViewList:any[] = [];
  teamViewLeaveHistoryList:any[] = [];
  leaveApplicationList:any[] = [];
  allCompOffApplications:any[] = [];

  constructor(
    private authenticationService : AuthenticationService,
    private modalService: BsModalService,
    private teamViewService : TeamViewService,
    private leaveService : LeaveService
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x); 
  }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, this.userMapping);

    this.getAllTeamView();
    this.getAllTeamLeaveHistoryView();
    this.getAllMyTeamsPendingLeaveApplicationsByManagerId();
    this.getPendingCompOffRequestsByManagerId();
  }

  viewTeam(){
    this.isViewTeam = true;
    this.isTeamLeaveHistory = false;
    this.isTeamRequest = false;
  }

  viewTeamLeaveHistory(){
    this.isTeamLeaveHistory = true;
    this.isViewTeam = false;
    this.isTeamRequest = false;
  }

  viewTeamRequest(){
    this.isTeamRequest = true;
    this.isTeamLeaveHistory = false;
    this.isViewTeam = false;
  }

  viewTeamLeaveRequest(){
    this.isLeaveRequest = true;
    this.isCompOffRequest = false;
  }

  viewTeamCompOffRequest(){
    this.isLeaveRequest = false;
    this.isCompOffRequest = true;
  }

  viewGenericRequest(){
    this.isLeaveRequest = false;
    this.isCompOffRequest = false;
  }

  getAllTeamView(){
    this.teamViewList = []

    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    this.teamViewService.getAllTeamView(employeeObj).pipe(first()).subscribe((response : any) => {
      if (response.serviceStatus == "Success") {
        this.teamViewList = response.serviceResponse;
        console.log("teamViewList : ", this.teamViewList);
      } else {
        console.error(response.serviceResponse);
      }
    });

  }

  getAllTeamLeaveHistoryView(){
    this.teamViewLeaveHistoryList = []

    let leaveObj = new Leave();
    leaveObj.empId = this.currentUser.empId;
    this.teamViewService.getAllTeamLeaveHistoryView(leaveObj).pipe(first()).subscribe((response : any) => {
      if (response.serviceStatus == "Success") {
        this.teamViewLeaveHistoryList = response.serviceResponse;
        console.log("teamViewLeaveHistory : ", this.teamViewLeaveHistoryList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }


  getAllMyTeamsPendingLeaveApplicationsByManagerId(){
    this.leaveApplicationList = []

    let leaveObj = new Leave();
    leaveObj.managerId = this.currentUser.empId;
    this.leaveService.getAllMyTeamsPendingLeaveApplicationsByManagerId(leaveObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.leaveApplicationList = response.serviceResponse;
        console.log("leaveApplicationList : ", this.leaveApplicationList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  onUpdateLeaveStatus(template: TemplateRef<any>, leaveApplication, updatedLeaveStatusId){
    // 1 = pending , 2 = Approved , 3= Rejected
    leaveApplication.leaveStatusId = updatedLeaveStatusId;
    console.log("leaveApplication : ", leaveApplication);
    
    this.leaveService.updateLeaveStatus(leaveApplication).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getAllMyTeamsPendingLeaveApplicationsByManagerId()
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

    //comOff Applications
    getPendingCompOffRequestsByManagerId(){
      this.allCompOffApplications = []
  
      let compOff = new Leave();
      compOff.managerId = this.currentUser.empId;
      this.leaveService.getPendingCompOffRequestsByManagerId(compOff).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allCompOffApplications = response.serviceResponse;
          console.log("allCompOffApplications : ", this.allCompOffApplications);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }
  
    onUpdateCompOffStatus(template: TemplateRef<any>, compOffObj, updatedCompOffStatusId){
      // 1 = pending , 2 = Approved , 3= Rejected
      compOffObj.leaveStatusId = updatedCompOffStatusId;
      console.log("Update Comp off : ", compOffObj);
      this.leaveService.updateCompOffById(compOffObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.openAlertMod(template, response.serviceResponse);
          this.getPendingCompOffRequestsByManagerId();
        } else {
          this.openAlertMod(template, response.serviceResponse);
        }
      });
    }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}
