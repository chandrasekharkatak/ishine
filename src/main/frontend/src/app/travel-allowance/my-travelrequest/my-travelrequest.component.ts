import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { DomSanitizer } from '@angular/platform-browser';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/internal/operators/first';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { MyTravelDesk } from 'src/app/models/travelDesk';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DomainService } from 'src/app/services/domain.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';
import { ValidationService } from 'src/app/services/validation.service';


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
  file:any;
  fileName :any;
  feature="Profile";
  userMapping:any = {};
  cityOptions: string[] = [];

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
    hotelCategory:'',
    tlSubCategory:'',
    docIds: [],

  };
  locationStrategy: any;
  domainSpecializationList: any[];
  todayDate: string;



  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private employeeService : EmployeeService,
    private sanitizer: DomSanitizer,
    private travelDesk : TravelDeskService,
    private domainService:DomainService,
    
    
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {

    const today = new Date();
    today.setDate(today.getDate() + 7);
    this.todayDate = today.toISOString().split('T')[0];

    this.onGetEmployeeInfo();
    
   
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    
    this.preventBackButton();

    //this.updateCityOptions(cityCategory);

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
    location.reload();
  }

  cancelRequest1() {
    this.modalRef.hide();
  }


async submitForm(template: TemplateRef<any>) {
  if (this.isValidForm()) {
    // All validations
    if (!this.travelDeskObj.associatedTravelRequest) {
      this.openAlertMod(template, "Please select an Associated Travel Request.");
      return;
    }
    if (this.travelDeskObj.associatedTravelRequest !== 'Hotel') {
    if (!this.travelDeskObj.travelMode) {
      this.openAlertMod(template, "Please select a Travel Mode.");
      return;
    }
    if (!this.travelDeskObj.travelClass) {
      this.openAlertMod(template, "Please select a Travel Class.");
      return;
    }
    if (!this.travelDeskObj.fromLocation) {
      this.openAlertMod(template, "Please Enter from location.");
      return;
    }
    if (!this.travelDeskObj.toLocation) {
      this.openAlertMod(template, "Please Enter to location.");
      return;
    }
  }
    if (!this.travelDeskObj.fromDate) {
      this.openAlertMod(template, "Please Select from date.");
      return;
    }
    if (!this.travelDeskObj.toDate) {
      this.openAlertMod(template, "Please Select to date.");
      return;
    }
    if (!this.travelDeskObj.purposeOfTravel) {
      this.openAlertMod(template, "Please enter the Purpose of Travel.");
      return;
    }
    if (!this.fileUploads || this.fileUploads.length === 0 || !this.fileUploads.some(f => f.file)) {
      this.openAlertMod(template, "Please enter a valid document.");
      return;
    }

    // First upload the file
    // const fileFormData = new FormData();
    // fileFormData.append('file', this.travelDeskObj.supportingDocument);
    // fileFormData.append("displayName", this.travelDeskObj.supportingDocument.name);
    // fileFormData.append("uploadedBy", this.currentEmployeeInfo.empId);

    try {
      this.travelDeskObj.docIds = [];
      
     // let reimbursementData = new MyReimbursement();

      const uploadedDocs: { fileName: string, docId: string }[] = [];
      let uploadResponse: any;

      
      for (const fileObj of this.fileUploads) {
        if (fileObj.file) {
          try {
            const fileFormData = new FormData();
            fileFormData.append('file', fileObj.file);
            fileFormData.append("displayName", fileObj.file.name);
            fileFormData.append("uploadedBy", this.currentEmployeeInfo.empId);
      
            uploadResponse = await this.travelDesk.uploadFile(fileFormData)
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
            this.travelDeskObj.docIds.push(uploadResponse.serviceResponse.documentId);
      
          } catch (error) {
            console.error("Upload failed for file", fileObj.file.name, error);
            this.openAlertMod(template, `File upload failed: ${error.message || error}`);
            return;
          }
        }
      }
      // const uploadResponse: any = await this.travelDesk.uploadFile(fileFormData).pipe(first()).toPromise();

      // if (uploadResponse.serviceStatus === "Fail") {
      //   this.openAlertMod(template, `Error found: ${uploadResponse.serviceResponse}`);
      //   return;
      // } else if (uploadResponse.serviceStatus !== "Success") {
      //   this.openAlertMod(template, uploadResponse.serviceResponse || "Unexpected file upload response.");
      //   return;
      // }

      // If file uploaded successfully, proceed with travel form data
      const travelData: any = {
        employeeId: this.currentEmployeeInfo.empId,
        fullName: this.currentEmployeeInfo.name,
        email: this.currentEmployeeInfo.email,
        departmentName: this.currentEmployeeInfo.departmentName,
        designationName: this.currentEmployeeInfo.designationName,
        mobileNo: this.currentEmployeeInfo.mobileNo,
        managerName: this.currentUser.hodName,
        associatedTravelRequest: this.travelDeskObj.associatedTravelRequest,
        travelMode: this.travelDeskObj.travelMode,
        travelClass: this.travelDeskObj.travelClass,
        fromDate: this.travelDeskObj.fromDate,
        toDate: this.travelDeskObj.toDate,
        fromLocation: this.travelDeskObj.fromLocation,
        toLocation: this.travelDeskObj.toLocation,
        purposeOfTravel: this.travelDeskObj.purposeOfTravel,
        levelOneApprover: this.currentUser.hodId,
        hodName: this.currentUser.hodName,
        hotelCategory:this.travelDeskObj.hotelCategory,
        cityCategory:this.travelDeskObj.tlSubCategory,
        city : this.travelDeskObj.selectedCity,
        docId: this.travelDeskObj.docIds
      };

      console.log('travel data         :::::::::::::',travelData);
      // const formData = new FormData();
      // formData.append('file', this.travelDeskObj.supportingDocument);
      // formData.append('travelData', new Blob([JSON.stringify(travelData)], { type: "application/json" }));

      const saveResponse: any = await this.travelDesk.saveTravelData(travelData).toPromise();

      if (saveResponse.serviceStatus === "Success") {
        this.openAlertMod(template, "Success! Your request was processed successfully!");
        //location.reload();
      } else {
        this.openAlertMod(template, saveResponse.serviceResponse || "Failed to save travel data.");
      }
    } catch (error) {
      console.error("Error during submit:", error);
      this.openAlertMod(template, "An unexpected error occurred while submitting the request.");
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

onSubCategoryChange(subCategory: string) {
  switch (subCategory) {
    case 'Metros':
      this.cityOptions = [
        'Mumbai', 'Delhi', 'Chennai', 'Kolkata', 'Bengaluru', 'Hyderabad'
      ];
      break;
    case 'Category A':
      this.cityOptions = [
        'Ahmedabad', 'Baroda', 'Indore', 'Pune', 'Nagpur', 'Jaipur',
        'Srinagar', 'Amritsar', 'Chandigarh', 'Guwahati', 'Patna', 'Lucknow',
        'Bhubaneswar', 'Panaji', 'Trivendrum', 'Kochi', 'Gangtok',
        'Udaipur', 'Agra', 'Coimbatore'
      ];
      break;
    case 'Category B':
      this.cityOptions = [
        'Surat', 'Rajkot', 'Bhopal', 'Nasik', 'Ludhiana', 'Jammu',
        'Varanasi', 'Ghaziabad', 'Kanpur', 'Ranchi', 'Jamshedpur',
        'Siliguri', 'Cuttack', 'Vizag', 'Vijaywada', 'Madurai',
        'Mangalore', 'Jodhpur', 'Mohali'
      ];
      break;
    default:
      this.cityOptions = [];
  }
}

// onFileChange(event: any, template: TemplateRef<any>) {
//   const file = event.target.files[0];
//   if (file) {
//     this.travelDeskObj.supportingDocument = file;
//   }
//     if (!this.travelDeskObj.supportingDocument) {
//       this.openAlertMod(template, "Please upload a file.");
//       return;
//     }
  
//     const formData = new FormData();
//     formData.append('file', this.travelDeskObj.supportingDocument);
  
//     this.domainService.employeeBulkUpload(formData).pipe(first()).subscribe(
//       (response: any) => {
//         if (response.serviceStatus === "Success") {
//           this.openAlertMod(template, response.serviceResponse);
//         } else if (response.serviceStatus === "Fail") {
//           this.openAlertMod(template, `Error found: ${response.serviceResponse}`);
//           event.target.value = '';  
//         } else {
//           this.openAlertMod(template, response.serviceResponse);
//         }
//       },
//       (error) => {
//         this.openAlertMod(template, "An error occurred while processing the upload.");
//         console.error(error);
//       }
//     );
// }
 
  //pagination
  onFileChange(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.travelDeskObj.supportingDocument = file;
    }
  }
  
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

fileUploads: any[] = [{}]; 

      onFileChange1(event: any, index: number,template: TemplateRef<any>) {
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

      
}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}

