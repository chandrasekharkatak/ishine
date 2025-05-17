import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Employee } from 'src/app/models/employee';
import { MyReimbursement } from 'src/app/models/reimbursement';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';

@Component({
  selector: 'app-total-reimbursementrequest',
  templateUrl: './total-reimbursementrequest.component.html',
  styleUrls: ['./total-reimbursementrequest.component.css']
})
export class TotalReimbursementrequestComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  @ViewChild('editTravelRequestModal') modalElement: any;
  //travelDeskInfo: MyTravelDesk;
  reimbursementInfo: MyReimbursement;
  currentEmployeeInfo: Employee = new Employee();
  travelRequests: any = [];
  reimbursementRequests: any = [];
  selectedReimbursementRequest: any = [];
  selectedTravelRequest: any = [];
  selectedReimbursementdataforDelete: any = [];
  domainSpecializationList: any[];
  currentUser: any;
  alertMessage: any;
  level: number;
  constructor(
    private modalService: BsModalService,
     private sanitizer: DomSanitizer,
     private employeeService : EmployeeService,
     private authenticationService: AuthenticationService,
     private reimbursementService:ReimbursementService,
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x)}

  ngOnInit(): void {
    this.onGetReimbursementInfo();

  }

  async onGetReimbursementInfo() {
    if (this.isValidForm()) {

     this.reimbursementInfo = new MyReimbursement();
      let reimbursementData = new MyReimbursement();

      console.log('currentEmployeeInfo ::::::::::::::::',this.currentEmployeeInfo);
      reimbursementData.empId = this.currentUser.empId;  

      console.log('reimbursementData Data  ::::::::::::::::', reimbursementData);

      const response: any = await this.reimbursementService.fetchReimbursementDataforApproval(reimbursementData).toPromise();
      
      if (response.serviceStatus === "Success") {
        this.reimbursementRequests = response.serviceResponse;  
        console.log('Fetched Reimbursement Requests:', this.reimbursementRequests);
        this.selectedReimbursementRequest = this.reimbursementRequests;
      } else {
        console.error('Error fetching data:', response.serviceResponse);
      }
    }
  }

  isValidForm() {
    return true; 
  }

  shouldShowAction(row: any): boolean {
    console.log('row data :::::::::::::',row);
    if (row.level === 1) {
      return row.status === 'Pending';
    } else if (row.level === 2) {
      return row.status === 'Approved' && row.level2approverStatus === 'Pending';
    } else if (row.level === 3) {
      return (
        row.status === 'Approved' &&
        row.level2approverStatus === 'Approved' &&
        row.level3approverStatus === 'Pending'
      );
    }
    return false;
  }
  //pagination

  page = 1;
  handlePageChange(event) {
      this.page = event;
  }
  //alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
    //this.onGetReimbursementInfo();
    location.reload();
  }


openEditModal(template: TemplateRef<any> ,row :any) {

  this.selectedReimbursementRequest = { ...row };

  console.log('editpain asichi re ::::::::::::::::::::',this.selectedReimbursementRequest);

  this.openAlertMod(template, "njvhv");

}

closeModal() {
  this.modalRef.hide();
  this.onGetReimbursementInfo();
}

}
