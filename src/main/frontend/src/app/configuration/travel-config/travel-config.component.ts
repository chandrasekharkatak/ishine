import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import Swal from 'sweetalert2';
import { Employee } from 'src/app/models/employee';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  standalone: false,
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
    id: null as number | null,
    travelReasonName: '',
    description: '',
    createdBy: ''
  };
  editingTravelModeId: number | null = null;
  editingTravelClassId: number | null = null;
  editingHotelCategoryId: number | null = null;
  editingHotelSubCategoryId: number | null = null;
  editingCityId: number | null = null;
  //hotelCategory = '';
  //description = '';
  hotelCategory1 = {
    id: null as number | null,
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
  selectedTravelModes: string = '';
  travelClass: any;
  hotelSubCategorylist: any[] = [];
  cityList: any[] = [];
  travelClassList: any[] = [];
  istravelModeTab: boolean = false;
  isHotelCategoryTab: boolean = false;
  isHotelSubCategoryTab: boolean = false;
  isCityTab: boolean = false;
  isClassTab: boolean = false;
  isCategoryTableTab: boolean = true;
  isRuleSetTab = false;

  handlePageChange(event) {
    this.page = event;
  }
  reviewColumns: any[] = ['blank', 'travelReasonName', 'description', 'createdBy', 'createdOn' ]
  reviewColumns1: any[] = ['blank', 'travelReasonName', 'modeType', 'description', 'createdBy', 'createdOn' ]
  reviewColumns2: any[] = ['blank', 'travelClass', 'description', 'createdBy', 'createdOn']
  reviewColumns3: any[] = ['blank', 'hotelCategory', 'description', 'createdBy', 'createdOn']
  reviewColumns4: any[] = ['blank', 'hotelSubCategoryName', 'description', 'createdBy', 'createdOn', 'updatedBy', 'updatedOn']
  reviewColumns5: any[] = ['blank', 'cityName', 'description', 'createdBy', 'createdOn', 'updatedBy', 'updatedOn']

  @ViewChild('travelReasonModal') private travelReasonModalTpl: TemplateRef<any>;
  @ViewChild('travelModeModal') private travelModeModalTpl: TemplateRef<any>;
  @ViewChild('travelClassModal') private travelClassModalTpl: TemplateRef<any>;
  @ViewChild('hotelCategoryModal') private hotelCategoryModalTpl: TemplateRef<any>;
  @ViewChild('hotelSubCategoryModal') private hotelSubCategoryModalTpl: TemplateRef<any>;
  @ViewChild('cityModal') private cityModalTpl: TemplateRef<any>;

  private formModalRef: NgbModalRef;

  constructor(private modalService: NgbModal, private travelDesk: TravelDeskService,
    private employeeService: EmployeeService,
    private authenticationService: AuthenticationService,
    private router : Router
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

  modalRef:NgbModalRef;
  modalRef1:NgbModalRef;
  modalRef2:NgbModalRef;
  openAlertMod1(template: TemplateRef<any>) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-lg' });
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

  showRuleSetPanel(): void {
    this.isRuleSetTab = true;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = false;
    this.isClassTab = false;
    this.isCategoryTableTab = false;
  }

  showQuaterTable() {
    this.isRuleSetTab = false;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = false;
    this.isClassTab = false;
    this.isCategoryTableTab = true;
    this.page = 1;
    this.onGetTravelReason();
  }
  createCategory() {
    this.resetTravelReasonForm();
    this.openTravelReasonModal();
  }

  private escapeHtml(text: string): string {
    if (text == null || text === '') {
      return '';
    }
    const d = document.createElement('div');
    d.textContent = text;
    return d.innerHTML;
  }

  private closeFormModal(): void {
    this.formModalRef?.close();
    this.formModalRef = undefined;
  }

  private openFormModal(tpl: TemplateRef<any> | undefined): void {
    if (!tpl) {
      return;
    }
    this.closeFormModal();
    this.formModalRef = this.modalService.open(tpl, {
      centered: true,
      size: 'lg',
      backdrop: 'static',
      scrollable: true
    });
  }

  openTravelReasonModal(): void {
    this.openFormModal(this.travelReasonModalTpl);
  }

  openTravelModeModal(): void {
    this.openFormModal(this.travelModeModalTpl);
  }

  openTravelClassModal(): void {
    this.openFormModal(this.travelClassModalTpl);
  }

  openHotelCategoryModal(): void {
    this.openFormModal(this.hotelCategoryModalTpl);
  }

  openHotelSubCategoryModal(): void {
    this.openFormModal(this.hotelSubCategoryModalTpl);
  }

  openCityModal(): void {
    this.openFormModal(this.cityModalTpl);
  }

  private swalMessage(icon: 'success' | 'error' | 'warning' | 'info', title: string, text?: string): void {
    Swal.fire({
      icon,
      title,
      text: text || undefined,
      confirmButtonText: 'OK'
    });
  }

  private async swalConfirmDelete(htmlMessage: string): Promise<boolean> {
    const r = await Swal.fire({
      icon: 'warning',
      title: 'Delete?',
      html: htmlMessage,
      showCancelButton: true,
      confirmButtonText: 'Yes, delete',
      cancelButtonText: 'Cancel',
      focusCancel: true,
      reverseButtons: true
    });
    return r.isConfirmed;
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
  subCategory() {
    this.isRuleSetTab = false;
    this.istravelModeTab = true;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = false;
    this.isClassTab = false;
    this.isCategoryTableTab = false;
    this.page = 1;
    this.onGetTravelMode();
  }

  classCategory() {
    this.isRuleSetTab = false;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = false;
    this.isClassTab = true;
    this.isCategoryTableTab = false;
    this.page = 1;
    this.onGetTravelCass();
  }

  hotelCategory() {
    this.hotelCategory1 = { id: null, hotelCategory: '', description: '' };
    this.openHotelCategoryModal();
  }

  hotelCategory3() {
    this.isRuleSetTab = false;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = true;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = false;
    this.isClassTab = false;
    this.isCategoryTableTab = false;
    this.page = 1;
    this.onGetHotelCategory();
  }

  hotelSubCategory() {
    this.editingHotelSubCategoryId = null;
    this.hotelSubCategoryData = { hotelCategory: '', hotelSubCategoryName: '', description: '', cityName: '' };
    this.openHotelSubCategoryModal();
  }

  hotelSubCategory3() {
    this.isRuleSetTab = false;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = true;
    this.isCityTab = false;
    this.isClassTab = false;
    this.isCategoryTableTab = false;
    this.page = 1;
    this.onGetHotelSubCategory();
  }

  cityCategory() {
    this.isRuleSetTab = false;
    this.istravelModeTab = false;
    this.isHotelCategoryTab = false;
    this.isHotelSubCategoryTab = false;
    this.isCityTab = true;
    this.isClassTab = false;
    this.isCategoryTableTab = false;
    this.page = 1;
    this.onGetCity();
  }

  cityCategory3() {
    this.editingCityId = null;
    this.hotelSubCategoryData = { hotelCategory: '', hotelSubCategoryName: '', description: '', cityName: '' };
    this.openCityModal();
  }

  subClassCategory() {
    this.resetTravelClassForm();
    this.openTravelClassModal();
  }

  travelCategory() {
    this.resetTravelModeForm();
    this.openTravelModeModal();
  }

  // async submitTravelReason(template: TemplateRef<any>) {
  //   try {
  //     const response: any = await this.travelDesk.saveTravelReason(this.travelReason).toPromise();
  //     console.log('Travel Reason saved:', response);
  //     if (response.serviceStatus === "Success") {
  //       this.openAlertMod(template, "Travel Reason submitted successfully!");
  //       this.modalRef?.close();
  //     }
  //     else {
  //       this.openAlertMod(template, "Submission failed. Try again.!");
  //     }

  //   } catch (error) {
  //     console.error('Error submitting reason:', error);
  //     this.openAlertMod(template, "Error occurred while saving the travel reason.");
  //   }
  // }
  async submitTravelReason() {
    try {
      const newReason = this.travelReason.travelReasonName.trim().toLowerCase();
      const duplicate = this.travelReasonlist.some(reason =>
        reason.travelReasonName.trim().toLowerCase() === newReason
        && reason.id !== this.travelReason.id
      );
      if (duplicate) {
        this.swalMessage('warning', 'Travel reason already exists!');
        return;
      }
      const payload = {
        id: this.travelReason.id,
        travelReasonName: this.travelReason.travelReasonName,
        description: this.travelReason.description,
        createdBy: this.currentEmployeeInfo?.empId || this.currentUser.empId
      };
      const response: any = await this.travelDesk.saveTravelReason(payload).toPromise();
      if (response.serviceStatus === 'Success') {
        const wasEdit = !!this.travelReason.id;
        this.closeFormModal();
        this.swalMessage('success', wasEdit ? 'Travel reason updated.' : 'Travel reason saved.');
        this.resetTravelReasonForm();
        await this.onGetTravelReason();
        this.showQuaterTable();
      } else {
        this.swalMessage('error', response.serviceError || 'Submission failed.');
      }
    } catch (error) {
      console.error('Error submitting reason:', error);
      this.swalMessage('error', 'Error occurred while saving the travel reason.');
    }
  }

  async onGetTravelReason() {
    const response: any = await this.travelDesk.getTravelReason().toPromise();
    if (response.serviceStatus == "Success") {
      this.travelReasonlist = response.serviceResponse || [];
    } else {
      this.travelReasonlist = [];
      console.error(response.serviceResponse);
    }
  }

  resetTravelReasonForm() {
    this.travelReason = { id: null, travelReasonName: '', description: '', createdBy: '' };
  }

  editTravelReason(row: any) {
    this.travelReason = {
      id: row.id,
      travelReasonName: row.travelReasonName,
      description: row.description || '',
      createdBy: row.createdBy
    };
    this.openTravelReasonModal();
  }

  async deleteTravelReasonRow(row: any) {
    const ok = await this.swalConfirmDelete(
      `Delete travel reason <strong>${this.escapeHtml(row.travelReasonName)}</strong>?`
    );
    if (!ok) {
      return;
    }
    try {
      const response: any = await this.travelDesk.deleteTravelReason(row.id).toPromise();
      if (response.serviceStatus === 'Success') {
        this.swalMessage('success', 'Travel reason deleted.');
        await this.onGetTravelReason();
      } else {
        this.swalMessage('warning', response.serviceError || 'Delete failed.');
      }
    } catch (error) {
      console.error(error);
      this.swalMessage('error', 'Delete failed.');
    }
  }


  async submitTravelMode() {
    if (!this.selectedTravelReasons?.trim()) {
      this.swalMessage('warning', 'Please select a travel reason.');
      return;
    }
    if (!this.modeType?.trim()) {
      this.swalMessage('warning', 'Please enter mode type.');
      return;
    }
    if (!this.description?.trim()) {
      this.swalMessage('warning', 'Please enter description.');
      return;
    }
    try {
      const reason = this.selectedTravelReasons;
      const isDuplicate = this.travelModelist?.some((item: any) =>
        item.travelReasonName?.trim().toLowerCase() === this.selectedTravelReasons?.trim().toLowerCase() &&
        item.modeType?.trim().toLowerCase() === this.modeType?.trim().toLowerCase() &&
        item.travelModeId !== this.editingTravelModeId
      );
      if (isDuplicate) {
        this.swalMessage('warning', 'This mode already exists for the selected travel reason.');
        return;
      }
      const travelModePayload = {
        travelModeId: this.editingTravelModeId,
        travelReason: reason,
        modeType: this.modeType.trim(),
        description: this.description,
        createdBy: this.currentEmployeeInfo?.empId || 0
      };
      const response: any = await this.travelDesk.saveTravelMode(travelModePayload).toPromise();
      if (response.serviceStatus !== 'Success') {
        this.swalMessage('error', response.serviceError || 'Failed to save travel mode.');
        return;
      }
      const wasEdit = !!this.editingTravelModeId;
      this.closeFormModal();
      this.swalMessage('success', wasEdit ? 'Travel mode updated.' : 'Travel mode saved.');
      this.resetTravelModeForm();
      await this.onGetTravelMode();
      this.subCategory();
    } catch (error) {
      console.error('API error:', error);
      this.swalMessage('error', 'Unexpected error occurred.');
    }
  }

  async onGetTravelMode() {
    const response: any = await this.travelDesk.getTravelMode().toPromise();
    if (response.serviceStatus == "Success") {
      this.travelModelist = response.serviceResponse || [];
    } else {
      this.travelModelist = [];
      console.error(response.serviceError || response.serviceResponse);
    }
  }

  resetTravelModeForm() {
    this.editingTravelModeId = null;
    this.selectedTravelReasons = '';
    this.modeType = '';
    this.description = '';
  }

  editTravelMode(row: any) {
    this.editingTravelModeId = row.travelModeId;
    this.selectedTravelReasons = row.travelReasonName || '';
    this.modeType = row.modeType;
    this.description = row.description || '';
    this.openTravelModeModal();
  }

  async deleteTravelModeRow(row: any) {
    const ok = await this.swalConfirmDelete(
      `Delete travel mode <strong>${this.escapeHtml(row.modeType)}</strong>?`
    );
    if (!ok) {
      return;
    }
    try {
      const response: any = await this.travelDesk.deleteTravelMode(row.travelModeId).toPromise();
      if (response.serviceStatus === 'Success') {
        this.swalMessage('success', 'Travel mode deleted.');
        await this.onGetTravelMode();
      } else {
        this.swalMessage('warning', response.serviceError || 'Delete failed.');
      }
    } catch (error) {
      console.error(error);
      this.swalMessage('error', 'Delete failed.');
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


  async submitTravelClass() {
    if (!this.selectedTravelReasons || !this.selectedTravelModes || !this.travelClass || !this.description) {
      this.swalMessage('warning', 'All fields are required.');
      return;
    }
    const travelClassPayload = {
      travelClassId: this.editingTravelClassId,
      travelReason: this.selectedTravelReasons,
      travelMode: this.selectedTravelModes,
      travelClass: this.travelClass,
      description: this.description,
      createdBy: this.currentEmployeeInfo?.empId || 0
    };
    try {
      const response: any = await this.travelDesk.saveTravelClass(travelClassPayload).toPromise();
      if (response.serviceStatus === 'Success') {
        const wasEdit = !!this.editingTravelClassId;
        this.closeFormModal();
        this.swalMessage('success', wasEdit ? 'Travel class updated.' : 'Travel class saved.');
        this.resetTravelClassForm();
        await this.onGetTravelCass();
        this.classCategory();
      } else {
        this.swalMessage('error', response.serviceError || 'Failed to save travel class.');
      }
    } catch (error) {
      console.error('API error:', error);
      this.swalMessage('error', 'Unexpected error occurred.');
    }
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
  //       this.modalRef?.close();
  //     }
  //     else {
  //       this.openAlertMod(template, "Submission failed. Try again.!");
  //     }

  //   } catch (error) {
  //     console.error('Error submitting reason:', error);
  //     this.openAlertMod(template, "Error occurred while saving the hotel Category.");
  //   }
  // }

  async submitHotelCategory() {
    try {
      const newCategory = this.hotelCategory1.hotelCategory?.trim().toLowerCase();
      const isDuplicate = this.hotelCategorylist?.some((item: any) =>
        item.hotelCategory?.trim().toLowerCase() === newCategory &&
        item.id !== this.hotelCategory1.id
      );
      if (isDuplicate) {
        this.swalMessage('warning', 'Hotel category already exists!');
        return;
      }
      const hotelCategoryPayload = {
        id: this.hotelCategory1.id,
        hotelCategory: this.hotelCategory1.hotelCategory,
        description: this.hotelCategory1.description,
        createdBy: this.currentEmployeeInfo?.empId || 0
      };
      const response: any = await this.travelDesk.saveHotelCategory(hotelCategoryPayload).toPromise();
      if (response.serviceStatus === 'Success') {
        const wasEdit = !!hotelCategoryPayload.id;
        this.closeFormModal();
        this.swalMessage('success', wasEdit ? 'Hotel category updated.' : 'Hotel category saved.');
        this.hotelCategory1 = { id: null, hotelCategory: '', description: '' };
        await this.onGetHotelCategory();
        this.hotelCategory3();
      } else {
        this.swalMessage('error', response.serviceError || 'Submission failed.');
      }
    } catch (error) {
      console.error('Error submitting hotel category:', error);
      this.swalMessage('error', 'Error occurred while saving the hotel category.');
    }
  }




  async onGetHotelCategory() {
    const response: any = await this.travelDesk.getHotelCategory().toPromise();
    if (response.serviceStatus == "Success") {
      this.hotelCategorylist = response.serviceResponse || [];
    } else {
      this.hotelCategorylist = [];
      console.error(response.serviceResponse);
    }
  }

  editHotelCategory(row: any) {
    this.hotelCategory1 = { id: row.id, hotelCategory: row.hotelCategory, description: row.description || '' };
    this.openHotelCategoryModal();
  }

  async deleteHotelCategoryRow(row: any) {
    const ok = await this.swalConfirmDelete(
      `Delete hotel category <strong>${this.escapeHtml(row.hotelCategory)}</strong>?`
    );
    if (!ok) {
      return;
    }
    try {
      const response: any = await this.travelDesk.deleteHotelCategory(row.id).toPromise();
      if (response.serviceStatus === 'Success') {
        this.swalMessage('success', 'Hotel category deleted.');
        await this.onGetHotelCategory();
      } else {
        this.swalMessage('warning', response.serviceError || 'Delete failed.');
      }
    } catch (error) {
      console.error(error);
      this.swalMessage('error', 'Delete failed.');
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
  //     this.modalRef?.close();
  //   } catch (error) {
  //     console.error("API error:", error);
  //     this.openAlertMod(template, "Unexpected error occurred while saving Hotel Sub-Category.");
  //   }
  // }

  async submitHotelSubCategory() {
    try {
      const selectedCategoryId = this.hotelSubCategoryData.hotelCategory;
      const enteredSubCategoryName = this.hotelSubCategoryData.hotelSubCategoryName?.trim().toLowerCase();
      const isDuplicate = this.hotelSubCategorylist?.some((item: any) =>
        item.hotelCategory?.id === selectedCategoryId &&
        item.hotelSubCategoryName?.trim().toLowerCase() === enteredSubCategoryName &&
        item.id !== this.editingHotelSubCategoryId
      );
      if (isDuplicate) {
        this.swalMessage('warning', 'Hotel sub-category already exists for the selected category.');
        return;
      }
      const hotelSubCategoryPayload = {
        id: this.editingHotelSubCategoryId,
        hotelCategory: selectedCategoryId,
        hotelSubCategoryName: this.hotelSubCategoryData.hotelSubCategoryName,
        description: this.hotelSubCategoryData.description,
        createdBy: this.currentEmployeeInfo?.empId || 0
      };
      const response: any = await this.travelDesk.saveHotelSubCategory(hotelSubCategoryPayload).toPromise();
      if (response.serviceStatus !== 'Success') {
        this.swalMessage('error', response.serviceError || 'Failed to save hotel sub-category.');
        return;
      }
      const wasEdit = !!this.editingHotelSubCategoryId;
      this.closeFormModal();
      this.swalMessage('success', wasEdit ? 'Hotel sub-category updated.' : 'Hotel sub-category saved.');
      this.editingHotelSubCategoryId = null;
      this.hotelSubCategoryData = { hotelCategory: '', hotelSubCategoryName: '', description: '', cityName: '' };
      await this.onGetHotelSubCategory();
      this.hotelSubCategory3();
    } catch (error) {
      console.error('API error:', error);
      this.swalMessage('error', 'Unexpected error occurred while saving hotel sub-category.');
    }
  }






  async onGetHotelSubCategory() {
    const response: any = await this.travelDesk.getHotelSubCategory().toPromise();
    if (response.serviceStatus == "Success") {
      this.hotelSubCategorylist = response.serviceResponse || [];
    } else {
      this.hotelSubCategorylist = [];
      console.error(response.serviceResponse);
    }
  }

  editHotelSubCategory(row: any) {
    this.editingHotelSubCategoryId = row.id;
    this.hotelSubCategoryData = {
      hotelCategory: row.hotelCategory?.id,
      hotelSubCategoryName: row.hotelSubCategoryName,
      description: row.description || '',
      cityName: ''
    };
    this.openHotelSubCategoryModal();
  }

  async deleteHotelSubCategoryRow(row: any) {
    const ok = await this.swalConfirmDelete(
      `Delete sub-category <strong>${this.escapeHtml(row.hotelSubCategoryName)}</strong>?`
    );
    if (!ok) {
      return;
    }
    try {
      const response: any = await this.travelDesk.deleteHotelSubCategory(row.id).toPromise();
      if (response.serviceStatus === 'Success') {
        this.swalMessage('success', 'Hotel sub-category deleted.');
        await this.onGetHotelSubCategory();
      } else {
        this.swalMessage('warning', response.serviceError || 'Delete failed.');
      }
    } catch (error) {
      console.error(error);
      this.swalMessage('error', 'Delete failed.');
    }
  }

  async submitCity() {
    try {
      const payload = {
        cityId: this.editingCityId,
        hotelCategoryId: this.hotelSubCategoryData.hotelCategory,
        hotelSubCategoryId: this.hotelSubCategoryData.hotelSubCategoryName,
        cityName: this.hotelSubCategoryData.cityName,
        description: this.hotelSubCategoryData.description,
        createdBy: this.currentEmployeeInfo?.empId || 0
      };
      const response: any = await this.travelDesk.saveCity(payload).toPromise();
      if (response.serviceStatus !== 'Success') {
        this.swalMessage('error', response.serviceError || 'Failed to save city.');
        return;
      }
      const wasEdit = !!this.editingCityId;
      this.closeFormModal();
      this.swalMessage('success', wasEdit ? 'City updated.' : 'City saved.');
      this.editingCityId = null;
      this.hotelSubCategoryData = { hotelCategory: '', hotelSubCategoryName: '', description: '', cityName: '' };
      await this.onGetCity();
      this.cityCategory();
    } catch (error) {
      console.error('API error:', error);
      this.swalMessage('error', 'Unexpected error occurred while saving city.');
    }
  }



  async onGetCity() {
    const response: any = await this.travelDesk.getCity().toPromise();
    if (response.serviceStatus == "Success") {
      this.cityList = response.serviceResponse || [];
    } else {
      this.cityList = [];
      console.error(response.serviceResponse);
    }
  }

  editCity(row: any) {
    this.editingCityId = row.cityId;
    this.hotelSubCategoryData = {
      hotelCategory: row.hotelCategory?.id,
      hotelSubCategoryName: row.hotelSubCategory?.id,
      cityName: row.cityName,
      description: row.description || ''
    };
    this.openCityModal();
  }

  async deleteCityRow(row: any) {
    const ok = await this.swalConfirmDelete(
      `Delete city <strong>${this.escapeHtml(row.cityName)}</strong>?`
    );
    if (!ok) {
      return;
    }
    try {
      const response: any = await this.travelDesk.deleteCity(row.cityId).toPromise();
      if (response.serviceStatus === 'Success') {
        this.swalMessage('success', 'City deleted.');
        await this.onGetCity();
      } else {
        this.swalMessage('warning', response.serviceError || 'Delete failed.');
      }
    } catch (error) {
      console.error(error);
      this.swalMessage('error', 'Delete failed.');
    }
  }

  async onGetTravelCass() {
    const response: any = await this.travelDesk.onGetTravelCass().toPromise();
    if (response.serviceStatus == "Success") {
      this.travelClassList = response.serviceResponse || [];
    } else {
      this.travelClassList = [];
      console.error(response.serviceResponse);
    }
  }

  resetTravelClassForm() {
    this.editingTravelClassId = null;
    this.selectedTravelReasons = '';
    this.selectedTravelModes = '';
    this.travelClass = '';
    this.description = '';
  }

  editTravelClass(row: any) {
    this.editingTravelClassId = row.travelClassId;
    this.selectedTravelReasons = row.travelReason?.travelReasonName || '';
    this.selectedTravelModes = String(row.travelMode?.travelModeId || '');
    this.travelClass = row.travelClass;
    this.description = row.description || '';
    if (this.selectedTravelReasons) {
      this.onTravelReasonChange(this.selectedTravelReasons);
    }
    this.openTravelClassModal();
  }

  async deleteTravelClassRow(row: any) {
    const ok = await this.swalConfirmDelete(
      `Delete travel class <strong>${this.escapeHtml(row.travelClass)}</strong>?`
    );
    if (!ok) {
      return;
    }
    try {
      const response: any = await this.travelDesk.deleteTravelClass(row.travelClassId).toPromise();
      if (response.serviceStatus === 'Success') {
        this.swalMessage('success', 'Travel class deleted.');
        await this.onGetTravelCass();
      } else {
        this.swalMessage('warning', response.serviceError || 'Delete failed.');
      }
    } catch (error) {
      console.error(error);
      this.swalMessage('error', 'Delete failed.');
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

