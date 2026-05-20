import { Component, OnInit, SecurityContext, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { DomSanitizer } from '@angular/platform-browser';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/internal/operators/first';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { MyTravelDesk } from 'src/app/models/travelDesk';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DomainService } from 'src/app/services/domain.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';
import { ValidationService } from 'src/app/services/validation.service';
import { ActivatedRoute, Router } from '@angular/router';


@Component({
  standalone: false,
  selector: 'app-my-travelrequest',
  templateUrl: './my-travelrequest.component.html',
  styleUrls: ['./my-travelrequest.component.css']
})
// export class TravelAllowanceComponent implements OnInit {
export class MyTravelrequestComponent implements OnInit {
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;


  isUpdateProfile: boolean = false;
  travelDeskInfo: MyTravelDesk = new MyTravelDesk();
  currentUser: any;
  currentEmployeeInfo: Employee = new Employee();
  file: any;
  fileName: any;
  feature = "Profile";
  userMapping: any = {};
  cityOptions: string[] = [];
  travelModelistByReason: any[] = [];
  travelClasslistByReason: any[] = [];
  citylistBySubCategory: any[] = [];
  selectedTravelReasons: string = '';


  //modal
  alertMessage: any;
  modalRef:NgbModalRef;


  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;



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
    fromLocation: '',
    toLocation: '',
    supportingDocument: null,
    hotelCategory: '',
    tlSubCategory: '',
    docIds: [],
    kycDocumentId: '',
    projectId: null as number | null,
    othersProjectName: '',
    othersClientId: null as number | null,
    displayClientName: ''
  };

  mappedProjectsForTravel: { projectId: number; projectName: string; clientName?: string; clientId?: number | null }[] = [];
  projectPickerSource: 'TEAM' | 'DEPARTMENT' | null = null;
  showOthersOption = false;
  clientsForOthers: { clientId: number; clientName: string }[] = [];
  readonly TRAVEL_OTHERS_PROJECT_ID = -1;
  locationStrategy: any;
  domainSpecializationList: any[];
  todayDate: string;
  travelReasonlist: any[] = [];
  hotelSubCategorylist: any;
  hotelCategorylist: any;
  maxTravelRequestDate: any = new Date();


  constructor(
    private validationService: ValidationService,
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private employeeService: EmployeeService,
    private sanitizer: DomSanitizer,
    private travelDesk: TravelDeskService,
    private reimbursementService: ReimbursementService,
    private domainService: DomainService,
    private router : Router


  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.onGetTravelReason();

    const today = new Date();
    const future = new Date();
    future.setDate(today.getDate() + 60);
    today.setDate(today.getDate() + 7);
    this.todayDate = today.toISOString().split('T')[0];
    this.maxTravelRequestDate = future.toISOString().split('T')[0];
    this.onGetEmployeeInfo();


    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });

    this.preventBackButton();
    this.onGetHotelCategory();
    this.onGetHotelSubCategory();

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



  async onGetEmployeeInfo() {
    this.domainSpecializationList = [];
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;
    currentEmp.isDraft = false;


    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;
      void this.loadMappedProjectsForTravel();
    } else {
      console.error(response.serviceResponse);
    }

    setTimeout(() => {
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
    this.modalRef?.close();
    location.reload();
  }

  cancelRequest3() {
    this.modalRef?.close();
  }

  cancelRequest1() {
    this.modalRef?.close();
  }


  async loadMappedProjectsForTravel() {
    this.mappedProjectsForTravel = [];
    this.projectPickerSource = null;
    this.showOthersOption = false;
    const empId = this.currentEmployeeInfo?.empId ?? this.currentUser?.empId;
    if (empId == null) {
      return;
    }
    try {
      const response: any = await this.reimbursementService
        .fetchReimbursementClaimProjectOptions({ empId })
        .pipe(first())
        .toPromise();
      if (response.serviceStatus !== 'Success' || !response.serviceResponse) {
        return;
      }
      const bag = response.serviceResponse;
      this.projectPickerSource = bag.pickerSource === 'DEPARTMENT' ? 'DEPARTMENT' : 'TEAM';
      this.showOthersOption = !!bag.showOthersOption;
      const raw = (bag.projects || []) as any[];
      const seen = new Set<number>();
      const parsed: { projectId: number; projectName: string; clientName?: string; clientId?: number | null }[] = [];
      for (const p of raw) {
        const id = p.projectId != null ? Number(p.projectId) : NaN;
        if (!Number.isFinite(id) || seen.has(id)) {
          continue;
        }
        seen.add(id);
        const cn = p.clientName != null ? String(p.clientName).trim() : '';
        const cid = p.clientId != null && p.clientId !== '' ? Number(p.clientId) : null;
        parsed.push({
          projectId: id,
          projectName: p.projectName != null ? String(p.projectName) : `Project #${id}`,
          clientName: cn !== '' ? cn : undefined,
          clientId: Number.isFinite(cid as number) ? cid : null
        });
      }
      const othersRows = parsed.filter((r) => r.projectId === this.TRAVEL_OTHERS_PROJECT_ID);
      const normalRows = parsed
        .filter((r) => r.projectId !== this.TRAVEL_OTHERS_PROJECT_ID)
        .sort((a, b) => a.projectName.localeCompare(b.projectName));
      // Travel: always offer "Others" (manual project name + client from clients master)
      if (!othersRows.length) {
        othersRows.push({
          projectId: this.TRAVEL_OTHERS_PROJECT_ID,
          projectName: 'Others',
          clientName: '',
          clientId: null
        });
      }
      this.mappedProjectsForTravel = [...othersRows, ...normalRows];
      await this.loadClientsForOthers();
    } catch (e) {
      console.error('loadMappedProjectsForTravel', e);
      this.mappedProjectsForTravel = [];
      this.projectPickerSource = null;
      this.showOthersOption = false;
    }
  }

  async loadClientsForOthers(): Promise<void> {
    if (this.clientsForOthers.length > 0) {
      return;
    }
    try {
      const response: any = await this.reimbursementService
        .fetchReimbursementClientsFromMaster()
        .pipe(first())
        .toPromise();
      if (response.serviceStatus !== 'Success' || !response.serviceResponse) {
        return;
      }
      const raw = response.serviceResponse as any[];
      this.clientsForOthers = raw
        .filter((r) => r.clientId != null)
        .map((r) => ({
          clientId: Number(r.clientId),
          clientName: r.clientName != null ? String(r.clientName) : ''
        }))
        .filter((r) => Number.isFinite(r.clientId))
        .sort((a, b) => a.clientName.localeCompare(b.clientName));
    } catch (e) {
      console.error('loadClientsForOthers', e);
    }
  }

  emptyProjectPickerHint(): string {
    if (this.projectPickerSource === 'DEPARTMENT') {
      return 'No active projects are linked to your department scope. If this looks wrong, contact RMG.';
    }
    if (this.projectPickerSource === 'TEAM') {
      return 'You have no mapped projects. Contact RMG to assign a project before submitting a travel request.';
    }
    return 'Projects could not be loaded. Refresh the page or try again later.';
  }

  clearSelectedProject(): void {
    this.travelDeskObj.projectId = null;
    this.travelDeskObj.othersProjectName = '';
    this.travelDeskObj.othersClientId = null;
    this.travelDeskObj.displayClientName = '';
  }

  clearOthersClient(): void {
    this.travelDeskObj.othersClientId = null;
  }

  isOthersProjectSelected(): boolean {
    return Number(this.travelDeskObj.projectId) === this.TRAVEL_OTHERS_PROJECT_ID;
  }

  showReadonlyClientForProject(): boolean {
    const id = this.travelDeskObj.projectId != null ? Number(this.travelDeskObj.projectId) : NaN;
    return Number.isFinite(id) && id !== this.TRAVEL_OTHERS_PROJECT_ID;
  }

  onTravelProjectSelected(_event?: unknown): void {
    const id = this.travelDeskObj.projectId != null ? Number(this.travelDeskObj.projectId) : NaN;
    if (!Number.isFinite(id)) {
      this.travelDeskObj.displayClientName = '';
      this.travelDeskObj.othersProjectName = '';
      this.travelDeskObj.othersClientId = null;
      return;
    }
    if (id === this.TRAVEL_OTHERS_PROJECT_ID) {
      this.travelDeskObj.displayClientName = '';
      if (!this.clientsForOthers.length) {
        void this.loadClientsForOthers();
      }
      return;
    }
    this.travelDeskObj.othersProjectName = '';
    this.travelDeskObj.othersClientId = null;
    const p = this.mappedProjectsForTravel.find((x) => x.projectId === id);
    this.travelDeskObj.displayClientName = p?.clientName ? String(p.clientName) : '';
  }

  projectSectionHint(): string {
    if (this.isOthersProjectSelected()) {
      return 'Enter the project name and choose a client from the master list.';
    }
    return 'Select a mapped project. Client is filled automatically from the project mapping.';
  }

  private validateProjectAndClient(template: TemplateRef<any>): boolean {
    if (!this.mappedProjectsForTravel.length) {
      this.openAlertMod(template, this.emptyProjectPickerHint());
      return false;
    }
    if (this.travelDeskObj.projectId == null || this.travelDeskObj.projectId === '') {
      this.openAlertMod(template, 'Please select a project.');
      return false;
    }
    const selectedPid = Number(this.travelDeskObj.projectId);
    if (!this.mappedProjectsForTravel.some((p) => p.projectId === selectedPid)) {
      this.openAlertMod(template, 'Please select a valid project from the list.');
      return false;
    }
    if (selectedPid === this.TRAVEL_OTHERS_PROJECT_ID) {
      if (!(this.travelDeskObj.othersProjectName || '').trim()) {
        this.openAlertMod(template, 'Enter the project name for Others.');
        return false;
      }
      if (this.travelDeskObj.othersClientId == null || this.travelDeskObj.othersClientId === '') {
        this.openAlertMod(template, 'Select a client for Others.');
        return false;
      }
    }
    return true;
  }

  async submitForm(template: TemplateRef<any>) {
    if (this.isValidForm()) {
      if (!this.validateProjectAndClient(template)) {
        return;
      }
      if (!this.travelDeskObj.associatedTravelRequest) {
        this.openAlertMod(template, "Please select an Associated Travel Request.");
        return;
      }
      if (this.travelDeskObj.associatedTravelRequest !== 'Hotel & Lodging') {

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
      if(!this.selectedFile ){
        this.openAlertMod(template, "Please select the kyc Document for Travel.");
        return;
      }
      // if (!this.fileUploads || this.fileUploads.length === 0 || !this.fileUploads.some(f => f.file)) {
      //   this.openAlertMod(template, "Please enter a valid document.");
      //   return;
      // }

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


        let uploadResponse11:any
        if (this.selectedFile) {
          try{
            const fileFormData = new FormData();
            fileFormData.append('file', this.selectedFile);
            fileFormData.append("displayName",this.selectedFileName);
            fileFormData.append("uploadedBy", this.currentEmployeeInfo.empId);

            uploadResponse11 = await this.travelDesk.uploadKycDocument(fileFormData)
              .pipe(first())
              .toPromise();

            if (uploadResponse11.serviceStatus !== "Success") {
              this.travelDeskObj.kycDocumentId = uploadResponse11.serviceResponse.documentId;
              console.error("Upload failed for file", this.travelDeskObj.kycDocumentId, uploadResponse11.serviceResponse.documentId);
              this.openAlertMod(template, `File upload failed: ${uploadResponse.serviceResponse}`);
              return;
            }else{
              this.travelDeskObj.kycDocumentId = uploadResponse11.serviceResponse.documentId;
              console.error("Upload failed for file", this.travelDeskObj.kycDocumentId, uploadResponse11.serviceResponse.documentId);
            }
          } catch (error) {

            this.openAlertMod(template, `File upload failed: ${error.message || error}`);
            return;
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





        const selectedPid = Number(this.travelDeskObj.projectId);
        const projectRow = this.mappedProjectsForTravel.find((p) => p.projectId === selectedPid);
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
          hotelCategory: this.travelDeskObj.hotelCategory,
          cityCategory: this.travelDeskObj.tlSubCategory,
          city: this.travelDeskObj.selectedCity,
          docId: this.travelDeskObj.docIds,
          reportingManagerId: this.currentEmployeeInfo.reportingManagerId,
          reportingManagerName: this.currentEmployeeInfo.reportingManagerName,
          kycDocumentId: this.travelDeskObj.kycDocumentId,
          projectId: selectedPid,
          othersProjectName: selectedPid === this.TRAVEL_OTHERS_PROJECT_ID
            ? (this.travelDeskObj.othersProjectName || '').trim() : null,
          othersClientId: selectedPid === this.TRAVEL_OTHERS_PROJECT_ID
            ? Number(this.travelDeskObj.othersClientId) : null,
          clientId: selectedPid !== this.TRAVEL_OTHERS_PROJECT_ID && projectRow?.clientId != null
            ? Number(projectRow.clientId) : null,
          clientName: selectedPid !== this.TRAVEL_OTHERS_PROJECT_ID
            ? (projectRow?.clientName || this.travelDeskObj.displayClientName || '') : null,
          projectName: selectedPid !== this.TRAVEL_OTHERS_PROJECT_ID
            ? (projectRow?.projectName || '') : null
        };

        console.log('currentEmployeeInfo         :::::::::::::', this.currentEmployeeInfo);

        console.log('travel data         :::::::::::::', travelData);
        // const formData = new FormData();
        // formData.append('file', this.travelDeskObj.supportingDocument);
        // formData.append('travelData', new Blob([JSON.stringify(travelData)], { type: "application/json" }));

        const saveResponse: any = await this.travelDesk.saveTravelData(travelData).toPromise();

        if (saveResponse.serviceStatus === "Success") {
          this.openAlertMod(template, saveResponse.serviceResponse);
          this.travelDeskObj = [];
          this.fileUploads = [];
          this.selectedFile = null;
          this.selectedFileName = null;
          this.addInputSpecializationField();
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
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
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
      supportingDocument: null,
      hotelCategory: '',
      tlSubCategory: '',
      docIds: [],
      kycDocumentId: '',
      projectId: null,
      othersProjectName: '',
      othersClientId: null,
      displayClientName: ''
    };
    this.travelModelistByReason = [];
    this.travelClasslistByReason = [];
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
          'Bengaluru', 'Chennai', 'Delhi', 'Hyderabad', 'Kolkata', 'Mumbai'
        ];
        break;
      case 'Tier 1':
        this.cityOptions = [
          'Agra', 'Ahmedabad', 'Amritsar', 'Baroda', 'Bhubaneswar', 'Chandigarh',
          'Coimbatore', 'Gangtok', 'Guwahati', 'Indore', 'Jaipur', 'Lucknow',
          'Nagpur', 'Panaji', 'Patna', 'Pune', 'Srinagar', 'Trivendrum', 'Udaipur'
        ];
        break;
      case 'Tier 2':
        this.cityOptions = [
          'Bhopal', 'Cuttack', 'Ghaziabad', 'Jamshedpur', 'Jodhpur', 'Jammu',
          'Kanpur', 'Ludhiana', 'Madurai', 'Mangalore', 'Mohali', 'Nasik',
          'Rajkot', 'Ranchi', 'Siliguri', 'Surat', 'Varanasi', 'Vijaywada', 'Vizag'
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

  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
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



  async onGetTravelReason() {

    const response: any = await this.travelDesk.getTravelReason().toPromise();
    if (response.serviceStatus == "Success") {
      this.travelReasonlist = response.serviceResponse;

      console.log("travelReasonlist   ::::::: : ", this.travelReasonlist);

    } else {
      console.error(response.serviceResponse);
    }
  }


  onTravelReasonSelected(_event?: unknown): void {
    void this.onTravelReasonChange(this.travelDeskObj.associatedTravelRequest);
  }

  onTravelModeSelected(_event?: unknown): void {
    void this.onModeChange(this.travelDeskObj.travelMode);
  }

  async onTravelReasonChange(selectedTravelReasons: string) {
    this.travelDeskObj.travelMode = '';
    this.travelDeskObj.travelClass = '';
    this.travelModelistByReason = [];
    this.travelClasslistByReason = [];
    if (selectedTravelReasons == "Hotel & Lodging") {
      this.onGetHotelCategory();
      this.onGetHotelSubCategory();
    }
    try {
      const response: any = await this.travelDesk.getTravelModeByReason(selectedTravelReasons).toPromise();
      if (response.serviceStatus === "Success") {
        this.travelModelistByReason = response.serviceResponse;
        console.log("Filtered travel modes:", this.travelModelistByReason);
      } else {
        console.error("Failed to fetch travel modes:", response.serviceResponse);
      }

    } catch (error) {
      console.error("Error fetching travel modes:", error);
    }
  }


  async onModeChange(selectedTravelMode: string) {
    this.travelDeskObj.travelClass = '';
    this.travelClasslistByReason = [];
    const mode = this.travelModelistByReason?.find((m: any) => m.modeType === selectedTravelMode);
    if (!mode?.travelModeId) {
      return;
    }
    try {
      const response: any = await this.travelDesk.getTravelClassByMode(mode.travelModeId).toPromise();
      if (response.serviceStatus === 'Success') {
        this.travelClasslistByReason = response.serviceResponse || [];
      } else {
        console.error('Failed to fetch travel classes:', response.serviceError || response.serviceResponse);
      }
    } catch (error) {
      console.error('Error fetching travel classes:', error);
    }
  }



  async onGetHotelCategory() {

    const response: any = await this.travelDesk.getHotelCategory().toPromise();
    if (response.serviceStatus == "Success") {
      this.hotelCategorylist = response.serviceResponse;

      console.log("hotelCategorylist   ::::::: : ", this.hotelCategorylist);

    } else {
      console.error(response.serviceResponse);
    }
  }



  async onGetHotelSubCategory() {

    const response: any = await this.travelDesk.getHotelSubCategory().toPromise();
    if (response.serviceStatus == "Success") {
      this.hotelSubCategorylist = response.serviceResponse;

      console.log("hotelSubCategorylist   ::::::: : ", this.hotelSubCategorylist);

    } else {
      console.error(response.serviceResponse);
    }
  }

  async onSubCategoryChanges(selectedSubCategory: string) {
    console.log('Selected Mode:', selectedSubCategory);
    this.citylistBySubCategory = [];
    try {
      const response: any = await this.travelDesk.getCityBySubCategory(selectedSubCategory).toPromise();
      if (response.serviceStatus === "Success") {
        this.citylistBySubCategory = response.serviceResponse;
        console.log("citylistBySubCategory :", this.citylistBySubCategory);
      } else {
        console.error("Failed to fetch City:", response.serviceResponse);
      }

    } catch (error) {
      console.error("Error fetching City:", error);
    }
  }


  selectedFileName: string | null = null;
  selectedFile: File | null = null;
  maxFileSizeMB = 1;

  onFileSelected(event: Event, template: TemplateRef<any>): void {
    const input = event.target as HTMLInputElement;

    if (input.files && input.files.length > 0) {
      const file = input.files[0];

      const allowedTypes = [

        'application/pdf',
        'image/jpeg',
        'image/jpg',
        'image/png',
        'image/gif',
        'image/bmp',
        'image/webp',
        'image/tiff',
        'image/tif',
        'image/svg+xml',
        'application/postscript',
      ];

      const allowedExtensions = [
        '.pdf',
        '.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp',
        '.tiff', '.tif', '.svg', '.eps'
      ];


      const fileExtension = '.' + file.name.split('.').pop()?.toLowerCase();

      // Validate file type
      if (!allowedTypes.includes(file.type) && !allowedExtensions.includes(fileExtension)) {
        this.openAlertMod(template, 'Please select only PDF files, images or scanned copies.');
        input.value = '';
        this.selectedFileName = null;
        this.selectedFile = null;
        return;
      }


      if (file.size > this.maxFileSizeMB * 1024 * 1024) {
        this.openAlertMod(template, 'File size should not exceed 1 MB.');
        input.value = '';
        this.selectedFileName = null;
        this.selectedFile = null;
        return;
      }


      if (file.size === 0) {
        this.openAlertMod(template, 'Selected file is empty. Please choose a valid file.');
        input.value = '';
        this.selectedFileName = null;
        this.selectedFile = null;
        return;
      }


      this.selectedFileName = file.name;
      this.selectedFile = file;

      console.log("KYC Document - Valid file selected:", {
        name: this.selectedFileName,
        type: file.type,
        size: `${(file.size / 1024 / 1024).toFixed(2)} MB`,
        extension: fileExtension
      });

    } else {

      this.selectedFileName = null;
      this.selectedFile = null;
    }
  }


  private getFileTypeDescription(file: File): string {
    const extension = '.' + file.name.split('.').pop()?.toLowerCase();

    if (file.type === 'application/pdf' || extension === '.pdf') {
      return 'PDF Document';
    } else if (file.type.startsWith('image/')) {
      return 'Image File';
    } else {
      return 'Document';
    }
  }


   


}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}

