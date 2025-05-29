import { Component, OnInit, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Employee } from 'src/app/models/employee';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';

@Component({
  selector: 'app-travel-config',
  templateUrl: './travel-config.component.html',
  styleUrls: ['./travel-config.component.css']
})
export class TravelConfigComponent implements OnInit {

  items: any = 10;
  page: any = 1;
  isSearchEnabledReview: boolean = false;
  isCategoryTable: boolean = true;
  createCategoryForm: boolean = false;
  filters: any = {};
  sortColumn: any;
  sortColumnType: any;
  sortDirection = 'asc';
  isHotelCategory: boolean = false;
  isHotelSubCategory: boolean = false;
  isCity: boolean = false;
  travelReason = {
    travelReasonName: '',
    description: ''
  };
  //hotelCategory = '';
  //description = '';
  hotelCategory1 = {
    hotelCategory: '',
    description: ''
  };
  hotelSubCategoryData = {
    hotelCategory: '',
    hotelSubCategoryName: '',
    description: '',
    cityName: ''
  }
  currentEmployeeInfo: Employee = new Employee();
  domainSpecializationList: any[];

  modeType: string = '';
  description: string = '';
  currentUser: any;
  alertMessage: any;
  travelReasonlist: any[] = [];
  hotelCategorylist: any[] = [];
  travelModelist: any[] = [];
  travelModelistByReason: any[] = [];
  selectedTravelReasons: string = '';
  // selectedTravelReasons: string[] = [];
  selectedTravelModes: string[] = [];
  travelClass: any;
  hotelSubCategorylist: any;
  cityList: any;
  travelClassList: any;
  istravelModeTab: boolean = false;
  isHotelCategoryTab: boolean = false;
  isHotelSubCategoryTab: boolean = false;
  isCityTab: boolean = false;
  isClassTab: boolean = false;
  isCategoryTableTab: boolean = true;

  handlePageChange(event) {
    this.page = event;
  }
  reviewColumns: any[] = ['blank', 'reviewLabel', 'reviewFieldType', 'condition', 'quarterCycle', 'departmentName', 'employeeName', 'createdOn', 'updatedByName', 'updatedOn']

  constructor(private modalService: BsModalService, private travelDesk: TravelDeskService,
    private employeeService: EmployeeService,
    private authenticationService: AuthenticationService
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x) }

  ngOnInit(): void {
    this.onGetEmployeeInfo();
    this.onGetTravelReason();
    this.onGetTravelMode();
    this.onGetHotelCategory();
    this.onGetHotelSubCategory();
    this.onGetCity();
    this.onGetTravelCass();
  }

  modalRef: BsModalRef = new BsModalRef();
  modalRef1: BsModalRef = new BsModalRef();
  openAlertMod1(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  async onGetEmployeeInfo() {
    this.domainSpecializationList = [];
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;
    currentEmp.isDraft = false;
    console.log("currentEmp :::::::::::::::::::::::: ", currentEmp);

    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;
    } else {
      console.error(response.serviceResponse);
    }
  }

  showQuaterTable() {
    this.isCategoryTable = true;
    this.isClass = false;
    this.istravelMode = false;
    this.travelModeForm = false;
    this.classCategoryForm = false;
    this.createCategoryForm = false;
    this.isHotelCategory = false;
    this.isHotelSubCategory = false;
    this.isCity = false;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = false;
    this.isClassTab = false;
    this.isCategoryTableTab = true;
  }
  createCategory() {
    this.isCategoryTable = false;
    this.createCategoryForm = true;
    this.travelModeForm = false;
    this.classCategoryForm = false;
    this.isClass = false;
    this.istravelMode = false;
    //this.createCategoryForm=false;
    this.isHotelCategory = false;
    this.isHotelSubCategory = false;
    this.isCity = false;
    this.hotelCategoryForm = false;
    this.hotelSubCategoryForm = false;
    this.cityForm = false;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = false;
    this.isClassTab = false;
    this.isCategoryTableTab = true;

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

  onSearch(searchData) {
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }
  toggleSearchReviewType() {
    this.isSearchEnabledReview = !this.isSearchEnabledReview;
    if (!this.isSearchEnabledReview) {
      this.filters = {};
    }
  }
  istravelMode: Boolean = false;
  subCategory() {
    this.istravelMode = true;
    this.isClass = false;
    this.isCategoryTable = false;
    this.travelModeForm = false;
    this.classCategoryForm = false;
    this.createCategoryForm = false;
    this.isHotelCategory = false;
    this.isHotelSubCategory = false;
    this.isCity = false;
    this.hotelCategoryForm = false;
    this.hotelSubCategoryForm = false;
    this.cityForm = false;
    this.istravelModeTab = true;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = false;
    this.isClassTab = false;
    this.isCategoryTableTab = false;
  }

  isClass: boolean = false;
  classCategory() {
    this.isClass = true;
    this.istravelMode = false;
    this.isCategoryTable = false;
    this.travelModeForm = false;
    this.classCategoryForm = false;
    this.createCategoryForm = false;
    this.isHotelCategory = false;
    this.isHotelSubCategory = false;
    this.isCity = false;
    this.hotelCategoryForm = false;
    this.hotelSubCategoryForm = false;
    this.cityForm = false;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = false;
    this.isClassTab = true;
    this.isCategoryTableTab = false;
  }

  hotelCategory() {
    this.isClass = false;
    this.istravelMode = false;
    this.isCategoryTable = false;
    this.travelModeForm = false;
    this.classCategoryForm = false;
    this.createCategoryForm = false;
    this.isHotelCategory = false;
    this.isHotelSubCategory = false;
    this.isCity = false;
    this.hotelCategoryForm = true;
    this.hotelSubCategoryForm = false;
    this.cityForm = false;

    this.istravelModeTab = false;
    this.isHotelCategoryTab = true;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = false;
    this.isClassTab = false;
    this.isCategoryTableTab = false;

  }
  hotelCategory3() {
    this.isClass = false;
    this.istravelMode = false;
    this.isCategoryTable = false;
    this.travelModeForm = false;
    this.classCategoryForm = false;
    this.createCategoryForm = false;
    this.isHotelCategory = true;
    this.isHotelSubCategory = false;
    this.isCity = false;
    this.hotelCategoryForm = false;
    this.hotelSubCategoryForm = false;
    this.cityForm = false;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = true;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = false;
    this.isClassTab = false;
    this.isCategoryTableTab = false;
  }

  hotelSubCategory() {
    this.isClass = false;
    this.istravelMode = false;
    this.isCategoryTable = false;
    this.travelModeForm = false;
    this.classCategoryForm = false;
    this.createCategoryForm = false;
    this.isHotelCategory = false;
    this.isHotelSubCategory = false;
    this.isCity = false;
    this.hotelCategoryForm = false;
    this.hotelSubCategoryForm = true;
    this.cityForm = false;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = true;
    this.isCityTab = false;
    this.isClassTab = false;
    this.isCategoryTableTab = false;
  }
  hotelSubCategory3() {
    this.isClass = false;
    this.istravelMode = false;
    this.isCategoryTable = false;
    this.travelModeForm = false;
    this.classCategoryForm = false;
    this.createCategoryForm = false;
    this.isHotelCategory = false;
    this.isHotelSubCategory = true;
    this.isCity = false;
    this.hotelCategoryForm = false;
    this.hotelSubCategoryForm = false;
    this.cityForm = false;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = true;
    this.isCityTab = false;
    this.isClassTab = false;
    this.isCategoryTableTab = false;
  }

  cityCategory() {
    this.isClass = false;
    this.istravelMode = false;
    this.isCategoryTable = false;
    this.travelModeForm = false;
    this.classCategoryForm = false;
    this.createCategoryForm = false;
    this.isHotelCategory = false;
    this.isHotelSubCategory = false;
    this.isCity = true;
    this.hotelCategoryForm = false;
    this.hotelSubCategoryForm = false;
    this.cityForm = false;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = true;
    this.isClassTab = false;
    this.isCategoryTableTab = false;
  }
  cityCategory3() {
    this.isClass = false;
    this.istravelMode = false;
    this.isCategoryTable = false;
    this.travelModeForm = false;
    this.classCategoryForm = false;
    this.createCategoryForm = false;
    this.isHotelCategory = false;
    this.isHotelSubCategory = false;
    this.isCity = false;
    this.hotelCategoryForm = false;
    this.hotelSubCategoryForm = false;
    this.cityForm = true;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = true;
    this.isClassTab = false;
    this.isCategoryTableTab = false;
  }


  classCategoryForm: boolean = false;
  travelModeForm: boolean = false;
  hotelCategoryForm: boolean = false;
  hotelSubCategoryForm: boolean = false;
  cityForm: boolean = false;

  subClassCategory() {
    this.travelModeForm = false;
    this.classCategoryForm = true;
    this.createCategoryForm = false;
    this.isClass = false;
    this.istravelMode = false;
    this.isCategoryTable = false;
    this.isHotelCategory = false;
    this.isHotelSubCategory = false;
    this.isCity = false;
    this.hotelCategoryForm = false;
    this.hotelSubCategoryForm = false;
    this.cityForm = false;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = false;
    this.isClassTab = true;
    this.isCategoryTableTab = false;
  }

  travelCategory() {
    this.travelModeForm = true;
    this.classCategoryForm = false;
    this.createCategoryForm = false;
    this.isClass = false;
    this.istravelMode = false;
    this.isCategoryTable = false;
    this.isHotelCategory = false;
    this.isHotelSubCategory = false;
    this.isCity = false;
    this.istravelModeTab = true;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = false;
    this.isClassTab = false;
    this.isCategoryTableTab = false;
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openValidationMod(template: TemplateRef<any>, message: any) {
    this.modalRef1 = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  // async submitTravelReason(template: TemplateRef<any>) {
  //   try {
  //     const response: any = await this.travelDesk.saveTravelReason(this.travelReason).toPromise();
  //     console.log('Travel Reason saved:', response);
  //     if (response.serviceStatus === "Success") {
  //       this.openAlertMod(template, "Travel Reason submitted successfully!");
  //       this.modalRef.hide();
  //     }
  //     else {
  //       this.openAlertMod(template, "Submission failed. Try again.!");
  //     }

  //   } catch (error) {
  //     console.error('Error submitting reason:', error);
  //     this.openAlertMod(template, "Error occurred while saving the travel reason.");
  //   }
  // }
  async submitTravelReason(template: TemplateRef<any>,template1: TemplateRef<any>) {
    try {
      const newReason = this.travelReason.travelReasonName.trim().toLowerCase();
  
      const duplicate = this.travelReasonlist.some(reason =>
        reason.travelReasonName.trim().toLowerCase() === newReason
      );
  
      if (duplicate) {
        this.openValidationMod(template1, "Travel Reason already exists!");
        return;
      }
  
      const response: any = await this.travelDesk.saveTravelReason(this.travelReason).toPromise();
      console.log('Travel Reason saved:', response);
  
      if (response.serviceStatus === "Success") {
        this.openAlertMod(template, "Travel Reason submitted successfully!");
        this.modalRef.hide();
        await this.onGetTravelReason();
      } else {
        this.openAlertMod(template, "Submission failed. Try again.!");
      }
  
    } catch (error) {
      console.error('Error submitting reason:', error);
      this.openAlertMod(template, "Error occurred while saving the travel reason.");
    }
  }
  



  cancelRequest() {
    this.modalRef.hide();
    location.reload();
  }
  cancelRequest1() {
    this.modalRef1.hide();
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


  async submitTravelMode(template: TemplateRef<any>,template1: TemplateRef<any>) {
    if (!this.selectedTravelReasons || this.selectedTravelReasons.length === 0) {
      this.openAlertMod(template, "Please select at least one travel reason.");
      return;
    }
    if (!this.modeType || this.modeType.length === 0) {
      this.openAlertMod(template, "Please enter Mode type.");
      return;
    }
    if (!this.description || this.description.length === 0) {
      this.openAlertMod(template, "Please enter the description.");
      return;
    }
    try {
      const reason = this.selectedTravelReasons;

      // const duplicate = this.travelModelist?.some((item: any) =>
      //   item.modeType?.trim().toLowerCase() === this.modeType?.trim().toLowerCase()
      // );
      const isDuplicate = this.travelModelist?.some((item: any) =>
        item.travelReasonName?.trim().toLowerCase() === this.selectedTravelReasons?.trim().toLowerCase() &&
        item.modeType?.trim().toLowerCase() === this.modeType?.trim().toLowerCase()
      );
  
      if (isDuplicate) {
        this.openValidationMod(template1, "Mode Type based on the travel reason is already exists!");
        return;
      }

      const travelModePayload = {
        travelReason: reason,
        modeType: this.modeType,
        description: this.description,
        createdBy: this.currentEmployeeInfo?.empId || 0
      };
      console.log("travelModePayload" ,travelModePayload);

      const response: any = await this.travelDesk.saveTravelMode(travelModePayload).toPromise();

      if (response.serviceStatus !== 'Success') {
        console.error(`Error saving reason: ${reason}`, response.serviceError);
        this.openAlertMod(template, `Failed to save travel mode for reason: ${reason}`);
        return;
      }
      this.openAlertMod(template, 'Travel Mode saved successfully!');
    } catch (error) {
      console.error('API error:', error);
      this.openAlertMod(template, 'Unexpected error occurred!');
    }
  }

  async onGetTravelMode() {

    const response: any = await this.travelDesk.getTravelMode().toPromise();
    if (response.serviceStatus == "Success") {
      this.travelModelist = response.serviceResponse;

      console.log("travelModelist   ::::::: : ", this.travelModelist);

    } else {
      console.error(response.serviceResponse);
    }
  }

  async onTravelReasonChange(selectedReason: string) {
    console.log('Selected reason:', selectedReason);
    this.travelModelistByReason = [];
    try {
      const response: any = await this.travelDesk.getTravelModeByReason(selectedReason).toPromise();
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


  async submitTravelClass(template: TemplateRef<any>,template1: TemplateRef<any>) {
    if (!this.selectedTravelReasons || !this.selectedTravelModes || !this.travelClass || !this.description) {
      this.openAlertMod(template, "All fields are required.");
      return;
    }

    const travelClassPayload = {
      travelReason: this.selectedTravelReasons,
      travelMode: this.selectedTravelModes,
      travelClass: this.travelClass,
      description: this.description,
      createdBy: this.currentEmployeeInfo?.empId || 0
    };

    console.log("travelClassPayload",travelClassPayload);

    try {

      const selectedReason = travelClassPayload.travelReason.trim().toLowerCase();
      // const selectedMode = travelClassPayload.travelMode[1].trim().toLowerCase();      
      const selectedClass = this.travelClass?.trim().toLowerCase();
      // const selectedReason = this.selectedTravelReasons[1]?.trim().toLowerCase();
      const selectedMode = this.selectedTravelModes[1]?.trim().toLowerCase();      
      // const selectedClass = this.travelClass?.trim().toLowerCase();



      console.log("selectedReason :::",selectedReason);
      console.log("selectedMode :::",selectedMode);

      console.log("selectedClass :::",selectedClass);

      
      const isDuplicate = this.travelClassList?.some((item: any) =>
        item.travelReason.travelReasonName?.travelReasonName?.trim().toLowerCase() === selectedReason &&
        item.travelMode.modeType?.modeType?.trim().toLowerCase() === selectedMode &&
        item.travelClass?.trim().toLowerCase() === selectedClass
      );
      
      if (isDuplicate) {
        this.openValidationMod(template, "Travel class for this combination already exists!");
        return;
      }
      const response: any = await this.travelDesk.saveTravelClass(travelClassPayload).toPromise();
      if (response.serviceStatus === 'Success') {
        this.openAlertMod(template, 'Travel Class saved successfully!');
      } else {
        alert('Error saving travel class: ' + response.serviceError);
      }
    } catch (error) {
      console.error('API error:', error);
      this.openAlertMod(template, 'Unexpected error occurred!');
    }
  }

  openDeleteModal(template: TemplateRef<any>, documentObj: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });

  }

  onDeleteType(template: TemplateRef<any>) {
    let doc = new Document();

    // doc.typeId = this.documentObj.typeId;
    // doc.typeName = this.documentObj.typeName;
    //     this.newsletterService.deleteType(doc).pipe(first()).subscribe((response : any)=>{
    //       if(response.serviceStatus == "Success"){
    //         this.openAlertMod(template, response.serviceResponse);
    //         this.getAllTypeName();
    //       }else{
    //         this.openAlertMod(template, response.serviceResponse);
    //       }
    //     })
    //console.log("yes delete !!   ",doc.typeId);
  }


  // async submitHotelCategory(template: TemplateRef<any>) {
  //   try {


  //     const hotelCategoryName = {
  //       hotelCategory: this.hotelCategory1.hotelCategory,
  //       description: this.hotelCategory1.description,
  //       createdBy: this.currentEmployeeInfo?.empId || 0
  //     };



  //     const response: any = await this.travelDesk.saveHotelCategory(hotelCategoryName).toPromise();
  //     console.log('hotelCategoryName saved:', response);
  //     if (response.serviceStatus === "Success") {
  //       this.openAlertMod(template, "Hotel Category Name submitted successfully!");
  //       this.modalRef.hide();
  //     }
  //     else {
  //       this.openAlertMod(template, "Submission failed. Try again.!");
  //     }

  //   } catch (error) {
  //     console.error('Error submitting reason:', error);
  //     this.openAlertMod(template, "Error occurred while saving the hotel Category.");
  //   }
  // }

  async submitHotelCategory(template: TemplateRef<any>) {
    try {
      const newCategory = this.hotelCategory1.hotelCategory?.trim().toLowerCase();
  
      const isDuplicate = this.hotelCategorylist?.some((item: any) =>
        item.hotelCategory?.trim().toLowerCase() === newCategory
      );
  
      if (isDuplicate) {
        this.openAlertMod(template, "Hotel Category already exists!");
        return;
      }
  
      const hotelCategoryPayload = {
        hotelCategory: this.hotelCategory1.hotelCategory,
        description: this.hotelCategory1.description,
        createdBy: this.currentEmployeeInfo?.empId || 0
      };
  
      const response: any = await this.travelDesk.saveHotelCategory(hotelCategoryPayload).toPromise();
      console.log('hotelCategory saved:', response);
  
      if (response.serviceStatus === "Success") {
        this.openAlertMod(template, "Hotel Category submitted successfully!");
        this.modalRef.hide();
      } else {
        this.openAlertMod(template, "Submission failed. Try again!");
      }
  
    } catch (error) {
      console.error('Error submitting hotel category:', error);
      this.openAlertMod(template, "Error occurred while saving the hotel category.");
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

  // async submitHotelSubCategory(template: TemplateRef<any>) {

  //   try {
  //     const hotelCategoryValue = this.hotelSubCategoryData.hotelCategory;
  //     console.log(hotelCategoryValue);
  //     const hotelSubCategoryPayload = {
  //       hotelCategory: this.hotelSubCategoryData.hotelCategory, // From dropdown
  //       hotelSubCategoryName: this.hotelSubCategoryData.hotelSubCategoryName,
  //       description: this.hotelSubCategoryData.description,
  //       createdBy: this.currentEmployeeInfo?.empId || 0
  //     };

  //     const response: any = await this.travelDesk.saveHotelSubCategory(hotelSubCategoryPayload).toPromise();

  //     if (response.serviceStatus !== 'Success') {
  //       console.error('Error saving hotel sub-category:', response.serviceError);
  //       this.openAlertMod(template, "Failed to save Hotel Sub-Category.");
  //       return;
  //     }

  //     this.openAlertMod(template, "Hotel Sub-Category saved successfully!");
  //     this.modalRef.hide();
  //   } catch (error) {
  //     console.error("API error:", error);
  //     this.openAlertMod(template, "Unexpected error occurred while saving Hotel Sub-Category.");
  //   }
  // }

  async submitHotelSubCategory(template: TemplateRef<any>) {
    try {
      const selectedCategoryId = this.hotelSubCategoryData.hotelCategory; // This is a number (category ID)
      const enteredSubCategoryName = this.hotelSubCategoryData.hotelSubCategoryName?.trim().toLowerCase();
  
      // ✅ Check for duplicate based on category ID and sub-category name
      const isDuplicate = this.hotelSubCategorylist?.some((item: any) =>
        item.hotelCategory?.id === selectedCategoryId &&
        item.hotelSubCategoryName?.trim().toLowerCase() === enteredSubCategoryName
      );
  
      if (isDuplicate) {
        this.openAlertMod(template, "Hotel Sub-Category already exists for the selected category.");
        return;
      }
  
      const hotelSubCategoryPayload = {
        hotelCategory: selectedCategoryId,
        hotelSubCategoryName: this.hotelSubCategoryData.hotelSubCategoryName,
        description: this.hotelSubCategoryData.description,
        createdBy: this.currentEmployeeInfo?.empId || 0
      };
  
      const response: any = await this.travelDesk.saveHotelSubCategory(hotelSubCategoryPayload).toPromise();
  
      if (response.serviceStatus !== 'Success') {
        console.error('Error saving hotel sub-category:', response.serviceError);
        this.openAlertMod(template, "Failed to save Hotel Sub-Category.");
        return;
      }
  
      this.openAlertMod(template, "Hotel Sub-Category saved successfully!");
      this.modalRef.hide();
  
    } catch (error) {
      console.error("API error:", error);
      this.openAlertMod(template, "Unexpected error occurred while saving Hotel Sub-Category.");
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


  async submitCity(template: TemplateRef<any>) {
    try {
      const payload = {
        hotelCategoryId: this.hotelSubCategoryData.hotelCategory,
        hotelSubCategoryId: this.hotelSubCategoryData.hotelSubCategoryName,
        cityName: this.hotelSubCategoryData.cityName,
        description: this.hotelSubCategoryData.description,
        createdBy: this.currentEmployeeInfo?.empId || 0
      };

      console.log(payload);

      const response: any = await this.travelDesk.saveCity(payload).toPromise();

      if (response.serviceStatus !== 'Success') {
        console.error('Error saving city:', response.serviceError);
        this.openAlertMod(template, "Failed to save City.");
        return;
      }

      this.openAlertMod(template, "City saved successfully!");
      this.modalRef.hide(); // If you're using modal
    } catch (error) {
      console.error("API error:", error);
      this.openAlertMod(template, "Unexpected error occurred while saving City.");
    }
  }



  async onGetCity() {

    const response: any = await this.travelDesk.getCity().toPromise();
    if (response.serviceStatus == "Success") {
      this.cityList = response.serviceResponse;

      console.log("cityList   ::::::: : ", this.cityList);

    } else {
      console.error(response.serviceResponse);
    }
  }


  async onGetTravelCass() {

    const response: any = await this.travelDesk.onGetTravelCass().toPromise();
    if (response.serviceStatus == "Success") {
      this.travelClassList = response.serviceResponse;

      console.log("travelClassList Fetched   :::::::: ", this.travelClassList);

    } else {
      console.error(response.serviceResponse);
    }
  }

  fieldRestrictNumber(event) {
    const k = event.charCode; 
    const inputValue = event.target.value; 
    if ((k >= 65 && k <= 90) || (k >= 97 && k <= 122)) {
        return true; 
    }
    if (k === 32 && inputValue.length > 0) { 
        return true; 
    }
    return false; 
}








}

