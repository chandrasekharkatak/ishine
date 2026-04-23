import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/internal/operators/first';
import { Employee } from 'src/app/models/employee';
import { MyReimbursement } from 'src/app/models/reimbursement';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';
import { ActivatedRoute, Router } from '@angular/router';



@Component({
  standalone: false,
  selector: 'app-my-reimbursement',
  templateUrl: './my-reimbursement.component.html',
  styleUrls: ['./my-reimbursement.component.css']
})
export class MyReimbursementComponent implements OnInit {
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  selectedReason: any;
  isTravel: boolean = false;
  alertMessage: any;
  modalRef:NgbModalRef;


  reimbursementInfo: MyReimbursement = new MyReimbursement();

  currentEmployeeInfo: Employee = new Employee();
  fromDate: string;
  toDate: string;
  currencyType: any;
  currentUser: any;
  amount: any;
  travelMode: any;
  distance: any;
  fromDateInput: any;
  toDateInput: any;
  purpose: any;
  fileInput: any;
  expenditureType: any;

expenditureTypeList:any[] = [];
foodTypeList:any[] = [];
travelModeList:any[] = [];
vehicleTypeList:any[] = [];

  reimbursementObj: any = {
    currencyType: '',
    currentUser: '',
    amount: 0,
    distance: '',
    travelMode: '',
    travelClass: '',
    expenditureType: '',
    fromDate: null,
    toDate: null,
    dateOfFood: null,
    purpose: '',
    fromLocation: '',
    toLocation: '',
    supportingDocument: null,
    kilometers: null,
    foodAllowanceType: null,
    vehicleType: ''
  };



  constructor(private empService: EmployeeService,
    private authenticationService: AuthenticationService,
    private reimbursementService: ReimbursementService,
    private modalService: NgbModal,
    private router : Router
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {

    const today = new Date();
    this.toDate = today.toISOString().split('T')[0];
    today.setDate(today.getDate() - 60);
    this.fromDate = today.toISOString().split('T')[0];
    this.onGetEmployeeInfo();
    this.setNextMonthDateRange();
    this.onGetExpenditureType();
    this.onGetTravelMode();
    this.onGetVehicleType();
    this.onGetFoodType();
  }

  onReasonSelect() {
    this.reimbursementObj.amount = 0;
    this.reimbursementObj.travelMode =null;
    this.reimbursementObj.vehicleType = null;
    console.log(this.reimbursementObj.expenditureType);
    if (this.reimbursementObj.expenditureType === 'Travel') {
      this.isTravel = true;
      console.log(this.isTravel);
    }
    else {
      this.isTravel = false;
    }
  }

  async onGetEmployeeInfo() {
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;
    currentEmp.isDraft = false;
    console.log("currentUser  :::::::::: ", this.currentUser);

    const response: any = await this.empService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;

      console.log("currentEmployeeInfo : ", this.currentEmployeeInfo);

    } else {
      console.error(response.serviceResponse);
    }





  }

  onKilometersChange() {
    if (this.reimbursementObj.vehicleType === 'Car' && this.reimbursementObj.distance > 0) {
      this.reimbursementObj.amount = this.reimbursementObj.distance * 12;
    } else if (this.reimbursementObj.vehicleType === 'Bike' && this.reimbursementObj.distance > 0) {
      this.reimbursementObj.amount = this.reimbursementObj.distance * 6;

    } else {
      this.reimbursementObj.amount = 0;
    }
  }

  async resetForm(template: TemplateRef<any>) {
    this.reimbursementObj = {
      currencyType: '',
      currentUser: '',
      amount: 0,
      distance: '',
      travelMode: '',
      travelClass: '',
      expenditureType: '',
      fromDate: null,
      toDate: null,
      dateOfFood: null,
      purpose: '',
      fromLocation: '',
      toLocation: '',
      supportingDocument: '',
      kilometers: null,
      foodAllowanceType: null,
      file: ''
    };
    const fileInput: HTMLInputElement | null = document.querySelector('input[type="file"]');
    if (fileInput) {
      fileInput.value = ''; // Clear the file input value
    }
    this.alertMessage = `Your form data has been successfully reset  !!!!!!`;
    this.openAlertMod(template, this.alertMessage);
  }

  async submitForm(template: TemplateRef<any>) {

    // Logic to handle form submission
    if (this.isValidForm()) {
      console.log("console", this.fileUploads);

      if (!this.reimbursementObj.expenditureType) {
        this.openAlertMod(template, "Please select an Expenditure Type.");
        return;
      }

      // Travel related validations
      if (this.reimbursementObj.expenditureType === 'Travel') {
        if (!this.reimbursementObj.travelMode) {
          this.openAlertMod(template, "Please select a Travel Mode.");
          return;
        }

        if (this.reimbursementObj.travelMode === 'Personal Vehicle') {
          if (!this.reimbursementObj.vehicleType) {
            this.openAlertMod(template, "Please select a Vehicle Type.");
            return;
          }
          if (!this.reimbursementObj.distance || this.reimbursementObj.distance <= 0) {
            this.openAlertMod(template, "Please enter valid Distance in KM.");
            return;
          }
        }
      }

      // Food related validations
      if (this.reimbursementObj.expenditureType === 'Food') {
        if (!this.reimbursementObj.foodAllowanceType) {
          this.openAlertMod(template, "Please select Food Allowance Type.");
          return;
        }
        if (!this.reimbursementObj.dateOfFood) {
          this.openAlertMod(template, "Please select the Fooding Date.");
          return;
        }
      }

      // Common fields
      if (!this.reimbursementObj.amount || this.reimbursementObj.amount <= 0) {
        this.openAlertMod(template, "Please enter a valid Total Amount .");
        return;
      }

      if (this.reimbursementObj.expenditureType !== 'Food') {
        if (!this.reimbursementObj.fromDate) {
          this.openAlertMod(template, "Please select From Date.");
          return;
        }
        if (!this.reimbursementObj.toDate) {
          this.openAlertMod(template, "Please select To Date.");
          return;
        }
      }

      if (!this.reimbursementObj.purpose || this.reimbursementObj.purpose.trim() === '') {
        this.openAlertMod(template, "Please enter the Purpose.");
        return;
      }

      if (!this.fileUploads || this.fileUploads.length === 0 || !this.fileUploads.some(f => f.file)) {
        this.openAlertMod(template, "Please upload a valid document.");
        return;
      }
      // First upload the file
      // const fileFormData = new FormData();
      // fileFormData.append('file', this.reimbursementObj.supportingDocument);
      // fileFormData.append("displayName", this.reimbursementObj.supportingDocument.name);
      // fileFormData.append("uploadedBy", this.currentEmployeeInfo.empId);
      try {
        let reimbursementData = new MyReimbursement();

        const uploadedDocs: { fileName: string, docId: string }[] = [];
        let uploadResponse: any;

        for (const fileObj of this.fileUploads) {
          if (fileObj.file) {
            try {
              const fileFormData = new FormData();
              fileFormData.append('file', fileObj.file);
              fileFormData.append("displayName", fileObj.file.name);
              fileFormData.append("uploadedBy", this.currentEmployeeInfo.empId);

              uploadResponse = await this.reimbursementService.uploadFileReimbursement(fileFormData)
                .pipe(first())
                .toPromise();

              if (uploadResponse.serviceStatus !== "Success") {
                this.openAlertMod(template, `File upload failed: ${uploadResponse.serviceResponse}`);
                return;
              }

              uploadedDocs.push({
                fileName: fileObj.file.name,
                docId: uploadResponse.serviceResponse.documentId
              });

              // Push each document ID into the docIds array
              reimbursementData.docIds.push(uploadResponse.serviceResponse.documentId);

            } catch (error) {
              console.error("Upload failed for file", fileObj.file.name, error);
              this.openAlertMod(template, `File upload failed: ${error.message || error}`);
              return;
            }
          }
        }
        //   const uploadResponse: any = await this.reimbursementService.uploadFileReimbursement(this.fileUploads).pipe(first()).toPromise();
        //   if (uploadResponse.serviceStatus === "Fail") {
        //     this.openAlertMod(template, `Error found: ${uploadResponse.serviceResponse}`);
        //     return;
        //   } else if (uploadResponse.serviceStatus !== "Success") {
        //     this.openAlertMod(template, uploadResponse.serviceResponse || "Unexpected file upload response.");
        //     return;
        //   }
        this.reimbursementInfo = new MyReimbursement();


        reimbursementData.empId = this.currentEmployeeInfo.empId;
        reimbursementData.name = this.currentEmployeeInfo.name;
        reimbursementData.email = this.currentEmployeeInfo.email;
        reimbursementData.departmentName = this.currentEmployeeInfo.departmentName;
        reimbursementData.designationName = this.currentEmployeeInfo.designationName;
        reimbursementData.mobileNo = this.currentEmployeeInfo.mobileNo;
        //reimbursementData.managerName=this.currentEmployeeInfo.managerName;
        reimbursementData.amount = this.reimbursementObj.amount;
        reimbursementData.travelMode = this.reimbursementObj.travelMode;
        reimbursementData.distance = this.reimbursementObj.distance;
        reimbursementData.fromDate = this.reimbursementObj.fromDate;
        reimbursementData.levelOneApprover = this.currentUser.hodId,
          reimbursementData.managerName = this.currentUser.hodName,
          reimbursementData.toDate = this.reimbursementObj.toDate;
        reimbursementData.purpose = this.reimbursementObj.purpose;
        reimbursementData.fileData = this.reimbursementObj.fileData;
        reimbursementData.selectedCurrency = this.reimbursementObj.currencyType;
        reimbursementData.expenditureType = this.reimbursementObj.expenditureType;
        reimbursementData.vehicleType = this.reimbursementObj.vehicleType;
        reimbursementData.foodAllowanceType = this.reimbursementObj.foodAllowanceType;
        reimbursementData.dateOfFood = this.reimbursementObj.dateOfFood;
        reimbursementData.multipleDocIds = this.reimbursementObj.docIds;
        reimbursementData.reportingManagerId = this.currentUser.managerId ;
        reimbursementData.reportingManagerName = this.currentUser.managerName ;

        console.log('reimbursementData Data::::::::::::::::::::::::::::', reimbursementData);
        console.log('currentEmployeeInfo Data::::::::::::::::::::::::::::', this.currentEmployeeInfo);


        this.onGetEmployeeInfo();

        const response: any = await this.reimbursementService.saveReimbursementData(reimbursementData).toPromise();
        if (response.serviceStatus == "Success") {

          this.resetAfterSubmit();
          this.fileUploads=[];
          this.addInputSpecializationField();


          this.openAlertMod(template, "Success! Your request is processed successfully!");

          //  alert("Success! Your request was processed successfully.");
          //window.location.reload();

        } else {
          console.error(response.serviceResponse);
        }
      } catch (error) {
        console.error("Error during submit:", error);
        this.openAlertMod(template, "An unexpected error occurred while submitting the request.");
      }
    }
  }

  isValidForm() {
    return true;
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }


  cancelRequest() {
    this.modalRef?.close();
    // location.reload();
  }

  cancelRequest2() {
    this.modalRef?.close();
  }

  cancelRequest1() {
    this.modalRef?.close();
    this.resetAfterSubmit();
    // location.reload();
  }

  onFileChange(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.reimbursementObj.supportingDocument = file;
    }
  }




  async resetAfterSubmit() {
    this.reimbursementObj = {
      currencyType: '',
      currentUser: '',
      amount: 0,
      distance: '',
      travelMode: '',
      travelClass: '',
      expenditureType: '',
      fromDate: null,
      toDate: null,
      dateOfFood: null,
      purpose: '',
      fromLocation: '',
      toLocation: '',
      supportingDocument: '',
      kilometers: null,
      foodAllowanceType: null,
      file: ''
    };
    const fileInput: HTMLInputElement | null = document.querySelector('input[type="file"]');
    if (fileInput) {
      fileInput.value = ''; // Clear the file input value
    }

  }


  // fromDate: string = '2023-01-01'; // example minimum
  // toDate: string = '2025-12-31';   // example maximum
  invalidFromDate: boolean = false;

  onFromDateChange(value: string) {
    if (!value) return;

    const selected = new Date(value);
    const day = selected.getDate();

    if (day < 1 || day > 15) {
      this.invalidFromDate = true;
      this.reimbursementObj.fromDate = '';
    } else {
      this.invalidFromDate = false;
    }
  }

  fileUploads: any[] = [{}];

  onFileChange1(event: any, index: number, template: TemplateRef<any>) {
    const file = event.target.files[0];
    if (!file) {

      this.fileUploads[index].file = null;
      return;
    }
    if (file) {
      const maxSizeInBytes = 1 * 1024 * 1024;

      if (file.size > maxSizeInBytes) {
        this.openAlertMod(template, "File size should be less than or equal to 1MB.");
        event.target.value = '';
        return;
      }

      this.fileUploads[index].file = file;
      this.fileUploads[index].uploadedBy = this.currentEmployeeInfo.empId;
      // this.reimbursementObj.supportingDocument = file;
    }
  }


  addInputSpecializationField() {
    if (this.fileUploads.length < 6) {
      this.fileUploads.push({});
    }
  }

  removeInputSpecializationField(index: number) {
    this.fileUploads.splice(index, 1);
  }

  setNextMonthDateRange() {
    const today = new Date();
    let nextMonth = today.getMonth() + 1;
    let year = today.getFullYear();

    if (nextMonth > 11) {
      nextMonth = 0; // January
      year++;
    }

    const minDate = new Date(year, nextMonth, 1);   // 1st of next month
    const maxDate = new Date(year, nextMonth, 15);  // 15th of next month

    this.fromDate = this.formatDate(minDate);
    this.toDate = this.formatDate(maxDate);
  }

  formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }



async onGetExpenditureType() {

  const response: any = await this.reimbursementService.onGetExpenditureType().toPromise();
  if (response.serviceStatus == "Success") {
  this.expenditureTypeList = response.serviceResponse;

  console.log("expenditureTypeList ::::::: : ", this.expenditureTypeList);

  } else {
  console.error(response.serviceResponse);
  }
  }

  async onGetTravelMode() {

  const response: any = await this.reimbursementService.getTravelMode().toPromise();
  if (response.serviceStatus == "Success") {
  this.travelModeList = response.serviceResponse;

  console.log("travelModelist ::::::: : ", this.travelModeList);

  } else {
  console.error(response.serviceResponse);
  }
  }


  async onGetVehicleType() {

  const response: any = await this.reimbursementService.onGetVehicleType().toPromise();
  if (response.serviceStatus == "Success") {
  this.vehicleTypeList = response.serviceResponse;

  console.log("vehicleTypeList ::::::: : ", this.vehicleTypeList);

  } else {
  console.error(response.serviceResponse);
  }
  }


  async onGetFoodType() {

  const response: any = await this.reimbursementService.onGetFoodType().toPromise();
  if (response.serviceStatus == "Success") {
  this.foodTypeList = response.serviceResponse;

  console.log("foodTypeList ::::::: : ", this.foodTypeList);

  } else {
  console.error(response.serviceResponse);
  }
  }

    goToGrievanceComponent(category: string, subCategory: string)  {
    this.router.navigate(['/grievance'], {
    queryParams: {
      category: category,
      subcategory: subCategory
    }
  });
}



}








