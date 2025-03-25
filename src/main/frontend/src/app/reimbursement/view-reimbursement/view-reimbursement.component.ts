import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Employee } from 'src/app/models/employee';
import { MyTravelDesk } from 'src/app/models/travelDesk';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';
import { ReimbursementComponent } from '../reimbursement.component';
import { ReimbursementService } from 'src/app/services/reimbursement.service';
import { MyReimbursement } from 'src/app/models/reimbursement';

@Component({
  selector: 'app-view-reimbursement',
  templateUrl: './view-reimbursement.component.html',
  styleUrls: ['./view-reimbursement.component.css']
})
export class ViewReimbursementComponent implements OnInit {

 
   @ViewChild("alert_message")
   alertTemplate: TemplateRef<any>;
   @ViewChild('editTravelRequestModal') modalElement: any;
   travelDeskInfo: MyTravelDesk;
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
 
   constructor(
     private modalService: BsModalService,
     private sanitizer: DomSanitizer,
     private employeeService : EmployeeService,
     private authenticationService: AuthenticationService,
     private reimbursementService:ReimbursementService,
   ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x) }
 
   ngOnInit(): void {
     this.onGetEmployeeInfo();
     this.onGetReimbursementInfo();
   }
 
   async onGetReimbursementInfo() {
     if (this.isValidForm()) {

      this.reimbursementInfo = new MyReimbursement();
       let reimbursementData = new MyReimbursement();
 
       console.log('currentEmployeeInfo ::::::::::::::::',this.currentEmployeeInfo);
       reimbursementData.empId = this.currentUser.empId;  
 
       console.log('reimbursementData Data  ::::::::::::::::', reimbursementData);
 
       const response: any = await this.reimbursementService.fetchReimbursementData(reimbursementData).toPromise();
       
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
   //pagination
 
   page = 1;
   handlePageChange(event) {
       this.page = event;
   }
   //alertMessage: any;
   modalRef: BsModalRef = new BsModalRef();
   openAlertMod(template: TemplateRef<any>, message: any) {
     this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
     this.alertMessage = message;
   }
   openEditModal(template: TemplateRef<any> ,row :any) {
 
     this.selectedReimbursementRequest = { ...row };
 
     this.selectedReimbursementRequest.fromDate = this.formatDate(this.selectedReimbursementRequest.fromDate);
     this.selectedReimbursementRequest.toDate = this.formatDate(this.selectedReimbursementRequest.toDate);
     this.selectedReimbursementRequest.appliedOn = this.formatDate(this.selectedReimbursementRequest.appliedOn);
 
    //  console.log('editpain asichi re ::::::::::::::::::::',this.selectedTravelRequest);

    console.log('editpain asichi re ::::::::::::::::::::',this.selectedReimbursementRequest);
 
     this.openAlertMod(template, "");
 
   }
 
   formatDate(date: string | Date | null): string | null {
     if (!date) return null; 
     const d = new Date(date);
   
     if (isNaN(d.getTime())) {
       console.error("Invalid date:", date);
       return null;
     }
     
     const month = ('0' + (d.getMonth() + 1)).slice(-2);
     const day = ('0' + d.getDate()).slice(-2);
     const year = d.getFullYear();
     
     return `${year}-${month}-${day}`;
   }
 
   
 
   async updateRequest() {

    console.log("Update pain asichi  ::::::::::::::::");

       this.reimbursementInfo = new MyReimbursement();       
       let newreimbursementData = new MyReimbursement();
       newreimbursementData.requestId=this.selectedReimbursementRequest.requestId;
       newreimbursementData.empId = this.selectedReimbursementRequest.empId;
       newreimbursementData.name = this.selectedReimbursementRequest.name;
       newreimbursementData.email =this.selectedReimbursementRequest.email;
       newreimbursementData.departmentName=this.selectedReimbursementRequest.department;
       newreimbursementData.designationName=this.selectedReimbursementRequest.designationName;
       newreimbursementData.mobileNo=this.selectedReimbursementRequest.mobileNo;
       newreimbursementData.managerName=this.selectedReimbursementRequest.managerName;
       newreimbursementData.travelMode= this.selectedReimbursementRequest.travelMode ;
       newreimbursementData.expenditureType= this.selectedReimbursementRequest.expenditureType ;
       newreimbursementData.distance= this.selectedReimbursementRequest.distance ;
       newreimbursementData.currency= this.selectedReimbursementRequest.currency ;
       newreimbursementData.amount= this.selectedReimbursementRequest.amount ;
       newreimbursementData.appliedBy= this.selectedReimbursementRequest.appliedBy ;
       newreimbursementData.appliedOn= this.selectedReimbursementRequest.appliedOn ;
       newreimbursementData.fromDate =this.selectedReimbursementRequest.fromDate;
       newreimbursementData.toDate = this.selectedReimbursementRequest.toDate;
       newreimbursementData.purpose = this.selectedReimbursementRequest.purpose;
       newreimbursementData.status= this.selectedReimbursementRequest.status ;
      //  newreimbursementData.supportingDocument = this.selectedReimbursementRequest.supportingDocument;
 
 
       console.log('selectedReimbursementRequest Data ::::::::::::::::', this.selectedReimbursementRequest);
        const index = this.reimbursementRequests.findIndex(request => request.requestId === this.selectedReimbursementRequest.requestId);
 
     if (index !== -1) {
       // Update the travel request at the found index with the new data
       this.reimbursementRequests[index] = { ...this.selectedReimbursementRequest };
 
       try {
        console.log('Try Bhitare Data ::::::::::::::::');

         const response: any = await this.reimbursementService.updateReimbursementData(newreimbursementData).toPromise();
   
         if (response.serviceStatus === "Success") {
           alert("Success! Your data was updated successfully.");
           console.log('Updated Travel Request:', this.selectedReimbursementRequest);
           this.modalRef.hide();
         } else {
           console.error('Error updating reimbursement request:', response.serviceResponse);
           alert('There was an issue updating the data.');
         }
       } catch (error) {
         console.error('Error during API call:', error);
         alert('An error occurred while updating the data. Please try again later.');
       }
  
     }
   }
 
   async deleteReimbursement(row : any) {

     if (this.isValidForm()) {
      this.reimbursementInfo = new MyReimbursement(); 
       let reimbursementData = new MyReimbursement();
 
       this.selectedReimbursementdataforDelete  = { ...row }; 
 
       console.log('delete pain asichi reee :::::::::::::::::',this.selectedReimbursementdataforDelete)
 
       console.log('currentEmployeeInfo ::::::::::::::::',this.currentEmployeeInfo);
       reimbursementData.requestId = this.selectedReimbursementdataforDelete.requestId;  
 
      // console.log('Form data for delete :::::::::::::::::::::', this.travelRequests.requestId);
 
       const response: any = await this.reimbursementService.revokeReimbursement(reimbursementData).toPromise();
       
       if (response.serviceStatus === "Success") {
         alert("Success! Your Data is deleted successfully.");
         this.onGetReimbursementInfo();
         
       } else {
         console.error('Error fetching data:', response.serviceResponse);
       }
     }
   }
 
 
 
  async onGetEmployeeInfo(){
     this.domainSpecializationList = [];
     this.currentEmployeeInfo = new Employee();
     let currentEmp = new Employee();
     currentEmp.empId = this.currentUser.empId;
     currentEmp.isDraft = false;
     console.log("currentEmp :::::::::::::::::::::::: ", currentEmp);
     
     const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
     if (response.serviceStatus == "Success") {
       this.currentEmployeeInfo = response.serviceResponse;
     
       //console.log("currentEmployeeInfo : ", this.currentEmployeeInfo);
       //this.loadProfileImage(this.currentEmployeeInfo.imageBytes)
 
     } else {
       console.error(response.serviceResponse);
     }
     setTimeout(()=>{
       this.currentEmployeeInfo.documentList && this.currentEmployeeInfo.documentList.forEach((doc, index) => {
         if (doc.documentBytes) {
           let preview = document.getElementById(`docPreview${index + 1}`);
             let objectURL = 'data:image/*;base64,' + doc.documentBytes;
             let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
             preview.setAttribute('src', src);
         }
       });
     }, 500);
   }
 
 
   closeModal() {
     this.modalRef.hide();
   }
 

}
