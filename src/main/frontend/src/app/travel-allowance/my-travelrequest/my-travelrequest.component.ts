import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { ClipboardService } from 'ngx-clipboard';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Activity } from 'src/app/models/activity';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { Holiday } from 'src/app/models/holiday';
import { Leave } from 'src/app/models/leave';
import { Timesheet } from 'src/app/models/timesheet';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { HolidayService } from 'src/app/services/holiday.service';
import { LeaveService } from 'src/app/services/leave.service';
import { TeamViewService } from 'src/app/services/team-view.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { ValidationService } from 'src/app/services/validation.service';
import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { ImageService } from 'src/app/services/image.service';
import { DomainService } from 'src/app/services/domain.service';
import { Domain } from 'src/app/models/domain';
import { TravelDeskService } from 'src/app/services/travel-desk.service';
import { MyTravelDesk } from 'src/app/models/travelDesk';



// @Component({
//   selector: 'app-travel-allowance',
//   templateUrl: './travel-allowance.component.html',
//   styleUrls: ['./travel-allowance.component.css']
// })
@Component({
  selector: 'app-my-travelrequest',
  templateUrl: './my-travelrequest.component.html',
  styleUrls: ['./my-travelrequest.component.css']
})
// export class TravelAllowanceComponent implements OnInit {
  export class MyTravelrequestComponent implements OnInit {
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;


  isUpdateProfile:boolean = false;
  travelDeskInfo:MyTravelDesk = new MyTravelDesk();
  currentUser:any;
  currentEmployeeInfo:Employee = new Employee();


  feature="Profile";
  userMapping:any = {};

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();


  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;



  //excel
  excelName = '';
  allMyTimesheetsDataForExcel: any[] = [];


  travelDeskObj: any = {
    associatedTravelRequest: '',
    travelMode: '',
    travelClass: '',
    fromDate: null,
    toDate: null,
    purposeOfTravel: '',
    fromLocation:'',
    toLocation:'',
    supportingDocument: null, // File
  };
  locationStrategy: any;
  domainSpecializationList: any[];
  todayDate: string;



  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private employeeService : EmployeeService,
    private holidayService: HolidayService,
    private imageService : ImageService,
    private domainService:DomainService,
    private sanitizer: DomSanitizer,
    private travelDesk : TravelDeskService,
    
    
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {

    const today = new Date();
    this.todayDate = today.toISOString().split('T')[0];

    this.onGetEmployeeInfo();

    // Dynamic Subfeature Flags
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    
    this.preventBackButton();

    
    // this.setStartDateMinMax();
  }
  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }




  disableMannualDateInput() {
    return false;
  }



  async onGetEmployeeInfo(){
    this.domainSpecializationList = [];
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;
    currentEmp.isDraft = false;
    //console.log("currentEmp : ", currentEmp);
    
    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;

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

  cancelRequest() {
    this.modalRef.hide();
  }

  async submitForm(template: TemplateRef<any>) {
    if (this.isValidForm()) {

      console.log('1st ::::::::::::::::::::::',this.travelDeskObj.associatedTravelRequest);


        if (!this.travelDeskObj.associatedTravelRequest) {
            this.alertMessage = `Please select an Associated Travel Request.`;
            this.openAlertMod(template, this.alertMessage);
            return;
        }

        if (!this.travelDeskObj.travelMode) {
            
            this.alertMessage = `Please select a Travel Mode.`;
            this.openAlertMod(template, this.alertMessage);
            return;
        }

        if (!this.travelDeskObj.travelClass) {
          this.alertMessage = `Please select a Travel Class.`;
          this.openAlertMod(template, this.alertMessage);
          return;
        }

        if (!this.travelDeskObj.fromLocation) {
          this.alertMessage = `Please Enter from location.`;
          this.openAlertMod(template, this.alertMessage);
          return;
        }

        if (!this.travelDeskObj.toLocation) {
          this.alertMessage = `Please Enter to location.`;
          this.openAlertMod(template, this.alertMessage);
          return;
        }

        if (!this.travelDeskObj.fromDate) {
          this.alertMessage = `Please Select from date.`;
          this.openAlertMod(template, this.alertMessage);
          return;
        }

        if (!this.travelDeskObj.toDate) {
          this.alertMessage = `Please Select to date.`;
          this.openAlertMod(template, this.alertMessage);
          return;
        }

        if (!this.travelDeskObj.purposeOfTravel) {
            this.alertMessage = `Please enter the Purpose of Travel.`;
          this.openAlertMod(template, this.alertMessage);
          return;
        }

        this.travelDeskInfo = new MyTravelDesk();
        let travelData = new MyTravelDesk();
        travelData.employeeId = this.currentEmployeeInfo.empId;
        travelData.fullName = this.currentEmployeeInfo.name;
        travelData.email = this.currentEmployeeInfo.email;
        travelData.departmentName = this.currentEmployeeInfo.departmentName;
        travelData.designationName = this.currentEmployeeInfo.designationName;
        travelData.mobileNo = this.currentEmployeeInfo.mobileNo;
        travelData.managerName = this.currentUser.hodName;
        travelData.associatedTravelRequest = this.travelDeskObj.associatedTravelRequest;
        travelData.travelMode = this.travelDeskObj.travelMode;
        travelData.travelClass = this.travelDeskObj.travelClass;
        travelData.fromDate = this.travelDeskObj.fromDate;
        travelData.toDate = this.travelDeskObj.toDate;
        travelData.fromLocation = this.travelDeskObj.fromLocation;
        travelData.toLocation = this.travelDeskObj.toLocation;
        travelData.purposeOfTravel = this.travelDeskObj.purposeOfTravel;
        travelData.supportingDocument = this.travelDeskObj.supportingDocument;
        travelData.levelOneApprover = this.currentUser.hodId;
        travelData.hodName = this.currentUser.hodName;

        console.log('Form Data:', travelData);

        try {
            this.onGetEmployeeInfo();

            const response: any = await this.travelDesk.saveTravelData(travelData).toPromise();
            if (response.serviceStatus == "Success") {
                this.alertMessage = `Success! Your request was processed successfully!`;
                this.openAlertMod(template, this.alertMessage);
            } else {
                console.error(response.serviceResponse);
            }
        } catch (error) {
            console.error('Error submitting form:', error);
        }
    }
}

   // Modals
   openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  onPaste(e) {
    e.preventDefault();
    return false;
  }



  preventScroll(event: WheelEvent): void {
    event.preventDefault();
  }
  


  resetForm(template: TemplateRef<any>) {
    this.travelDeskObj = {
      associatedTravelRequest: '',
      travelMode: '',
      travelClass: '',
      fromLocation: '',
      toLocation: '',
      fromDate: '',
      toDate: '',
      purposeOfTravel: '',
      supportingDocument: null 
  };
  this.alertMessage = `Your form data has been successfully reset  !!!!!!`;
  this.openAlertMod(template, this.alertMessage);
    
}


isValidForm() {
return true;
}

onFileChange(event: any) {
  const file = event.target.files[0];
  if (file) {
    this.travelDeskObj.supportingDocument = file;
  }
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



}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}

