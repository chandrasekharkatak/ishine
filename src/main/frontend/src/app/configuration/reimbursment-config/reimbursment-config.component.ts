import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import Swal from 'sweetalert2';
import { Employee } from 'src/app/models/employee';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';

@Component({
  standalone: false,
  selector: 'app-reimbursment-config',
  templateUrl: './reimbursment-config.component.html',
  styleUrls: ['./reimbursment-config.component.css']
})
export class ReimbursmentConfigComponent implements OnInit {

  items: any = 10;
  page: any = 1;
  isSearchEnabledReview: boolean = false;
  isCategoryTable: boolean = true;
  filters: any = {};
  sortColumn: any;
  sortColumnType: any;
  sortDirection = 'asc';
  expenditureType: { id?: number; expenditureTypeName: string; description: string; createdBy: any } = {
    expenditureTypeName: '',
    description: '',
    createdBy: ''
  };

  foodType: { foodTypeId?: number; foodTypeName: string; description: string; createdBy: any } = {
    foodTypeName: '',
    description: '',
    createdBy: ''
  };
  vehicleType: { vehicleTypeId?: number; vehicleTypeName: string; description: string; createdBy: any } = {
    vehicleTypeName: '',
    description: '',
    createdBy: ''
  };
  selectedExpenditure: string = '';
  requiresVehicleType: any = null;
  editingTravelModeId: number | null = null;
  vehicleTypeList:any[] = [];
  expenditureTypeList:any[] = [];
  foodTypeList:any[] = [];
  travelModeList:any[] = [];

  currentEmployeeInfo: Employee = new Employee();
  domainSpecializationList: any[];
  currentUser:User;
  modeType: any;
  description: any;
  istravelModeTab: boolean = false;
  isCityTab: boolean = false;
  isClassTab: boolean = false;
  isCategoryTableTab: boolean = true;
  /** Reimbursement → Approval Matrix tab (approval rules UI; backend later). */
  isRuleSetTab = false;
  foodCategoryTable = false;
  foodAllowanceTypeTab = false;
  handlePageChange(event) {
    this.page = event;
  }
  reviewColumns: any[] = ['blank', 'reviewLabel', 'reviewFieldType', 'condition', 'quarterCycle', 'departmentName', 'employeeName', 'createdOn', 'updatedByName', 'updatedOn']

  @ViewChild('expenditureTypeModal') private expenditureTypeModalTpl: TemplateRef<any>;
  @ViewChild('travelModeModal') private travelModeModalTpl: TemplateRef<any>;
  @ViewChild('vehicleTypeModal') private vehicleTypeModalTpl: TemplateRef<any>;
  @ViewChild('foodTypeModal') private foodTypeModalTpl: TemplateRef<any>;

  /** Single active form modal (add/edit masters). */
  private formModalRef: NgbModalRef;

  constructor(private modalService: NgbModal, private travelDesk: TravelDeskService,private reimbursementService:ReimbursementService,
    private employeeService: EmployeeService,
    private authenticationService: AuthenticationService
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x) }

  ngOnInit(): void {
    this.onGetEmployeeInfo();
    this.onGetExpenditureType();
    this.onGetTravelMode();
    this.onGetVehicleType();
    this.onGetFoodType();
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

  openExpenditureTypeModal(): void {
    this.openFormModal(this.expenditureTypeModalTpl);
  }

  openTravelModeModal(): void {
    this.openFormModal(this.travelModeModalTpl);
  }

  openVehicleTypeModal(): void {
    this.openFormModal(this.vehicleTypeModalTpl);
  }

  openFoodTypeModal(): void {
    this.openFormModal(this.foodTypeModalTpl);
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

  private swalWarningWithList(title: string, lines: string[]): void {
    const listItems = lines.map(l => `<li class="mb-1 text-left">${this.escapeHtml(l)}</li>`).join('');
    Swal.fire({
      icon: 'warning',
      html: `<p class="text-left mb-2">${this.escapeHtml(title)}</p><ul class="pl-4 text-left mb-0">${listItems}</ul>`,
      confirmButtonText: 'OK',
      width: 560
    });
  }

  showQuaterTable(){
    this.isCategoryTable=true;
    this.isClass=false;
    this.istravelMode=false;
    this.istravelModeTab = false;
    this.isCategoryTableTab = true;
    this.isCityTab = false;
    this.isClassTab = false;
    this.foodCategoryTable =false;
    this.foodAllowanceTypeTab = false;
    this.isRuleSetTab = false;
  }
  createCategory(){
    this.resetForm();
    this.openExpenditureTypeModal();
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
  istravelMode:Boolean=false;
  subCategory(){
   this.istravelMode=true;
   this.isClass=false;
   this.isCategoryTable=false;
    this.istravelModeTab = true;
    this.isCategoryTableTab = false;
    this.isCityTab = false;
    this.isClassTab = false;
    this.foodCategoryTable =false;
    this.foodAllowanceTypeTab = false;
    this.isRuleSetTab = false;
  }

  isClass:boolean=false;
  classCategory(){
    this.isClass=true;
    this.istravelMode=false;
    this.isCategoryTable=false;
    this.istravelModeTab = false;
    this.isCategoryTableTab = false;
    this.isCityTab = false;
    this.isClassTab = true;
    this.foodCategoryTable =false;
    this.foodAllowanceTypeTab = false;
    this.isRuleSetTab = false;
  }

  foodTypeTable(){
    this.isClass=false;
    this.istravelMode=false;
    this.isCategoryTable=false;
    this.foodCategoryTable =true;
    this.istravelModeTab = false;
    this.isCategoryTableTab = false;
    this.foodAllowanceTypeTab = true;
    this.isClassTab = false;
    this.isRuleSetTab = false;
  }

  showRuleSetPanel(): void {
    this.isRuleSetTab = true;
    this.isCategoryTable = false;
    this.isClass = false;
    this.istravelMode = false;
    this.foodCategoryTable = false;
    this.istravelModeTab = false;
    this.isCategoryTableTab = false;
    this.isClassTab = false;
    this.foodAllowanceTypeTab = false;
  }

  subClassCategory(){
    this.resetForm();
    this.openVehicleTypeModal();
  }

  travelCategory(){
    this.resetForm();
    this.openTravelModeModal();
  }

  private swalMessage(icon: 'success' | 'error' | 'warning' | 'info', title: string): void {
    Swal.fire({
      icon,
      title,
      confirmButtonText: 'OK'
    });
  }

  async submitExpenditureType() {
    try {
      this.expenditureType.createdBy = this.currentEmployeeInfo.empId;
      const newReason = this.expenditureType.expenditureTypeName.trim().toLowerCase();
      const duplicate = this.expenditureTypeList.some((reason: any) =>
        reason.expenditureTypeName.trim().toLowerCase() === newReason
        && reason.id !== this.expenditureType.id
      );
      if (duplicate) {
        this.swalMessage('warning', 'Expenditure type name already exists!');
        return;
      }
      const response: any = await this.reimbursementService.saveExpenditureType(this.expenditureType).toPromise();
      if (response.serviceStatus === 'Success') {
        const wasEdit = !!this.expenditureType.id;
        this.closeFormModal();
        this.swalMessage('success', wasEdit ? 'Expenditure type updated.' : 'Expenditure type saved.');
        this.resetForm();
        await this.onGetExpenditureType();
        this.showQuaterTable();
      } else {
        this.swalMessage('error', response.serviceError || 'Submission failed.');
      }
    } catch (error) {
      console.error('Error submitting expenditure type:', error);
      this.swalMessage('error', 'Error occurred while saving the expenditure type.');
    }
  }

  editExpenditureType(row: any) {
    this.expenditureType = {
      id: row.id,
      expenditureTypeName: row.expenditureTypeName,
      description: row.description || '',
      createdBy: this.currentEmployeeInfo?.empId
    };
    this.openExpenditureTypeModal();
  }

  async deleteExpenditureTypeRow(row: any) {
    const ok = await this.swalConfirmDelete(
      `Delete expenditure type <strong>${this.escapeHtml(row.expenditureTypeName)}</strong>?`
    );
    if (!ok) {
      return;
    }
    try {
      const resp: any = await this.reimbursementService.deleteExpenditureType(row.id).toPromise();
      if (resp.serviceStatus === 'Success') {
        this.swalMessage('success', 'Expenditure type deleted.');
        await this.onGetExpenditureType();
      } else {
        const rawList = resp.serviceResponse;
        const modes: string[] = Array.isArray(rawList)
          ? rawList.map((x: any) => (typeof x === 'string' ? x : String(x)))
          : [];
        if (modes.length) {
          this.swalWarningWithList(resp.serviceError || 'Cannot delete this expenditure type.', modes);
        } else {
          this.swalMessage('warning', resp.serviceError || 'Cannot delete this expenditure type.');
        }
      }
    } catch (error) {
      console.error(error);
      this.swalMessage('error', 'Delete failed.');
    }
  }

  async onGetExpenditureType() {
    const response: any = await this.reimbursementService.onGetExpenditureType().toPromise();
    if (response.serviceStatus == 'Success') {
      this.expenditureTypeList = response.serviceResponse || [];
    } else {
      this.expenditureTypeList = [];
    }
  }

  async submitTravelMode() {
    if (!this.selectedExpenditure || this.selectedExpenditure.length === 0) {
      this.swalMessage('warning', 'Please select an expenditure type.');
      return;
    }
    if (!this.modeType || !this.modeType.trim()) {
      this.swalMessage('warning', 'Travel mode name is required.');
      return;
    }
    try {
      const reason = this.selectedExpenditure;
      const newLower = this.modeType.trim().toLowerCase();
      const duplicate = this.travelModeList.some((m: any) =>
        m.expenditureType === reason
        && m.modeType.trim().toLowerCase() === newLower
        && m.travelModeId !== this.editingTravelModeId
      );
      if (duplicate) {
        this.swalMessage('warning', 'This mode already exists for the selected expenditure type.');
        return;
      }
      const travelModePayload = {
        travelModeId: this.editingTravelModeId,
        expenditureType: reason,
        modeType: this.modeType.trim(),
        description: this.description,
        requiresVehicleType: (this.requiresVehicleType === true || this.requiresVehicleType === 'Yes') ? 'Yes' : 'No',
        createdBy: this.currentEmployeeInfo?.empId || 0
      };
      const response: any = await this.reimbursementService.saveTravelMode(travelModePayload).toPromise();
      if (response.serviceStatus !== 'Success') {
        this.swalMessage('error', response.serviceError || 'Failed to save travel mode.');
        return;
      }
      const wasEditTm = !!this.editingTravelModeId;
      this.closeFormModal();
      this.swalMessage('success', wasEditTm ? 'Travel mode updated.' : 'Travel mode saved.');
      this.resetForm();
      await this.onGetTravelMode();
      this.subCategory();
    } catch (error) {
      console.error('API error:', error);
      this.swalMessage('error', 'Unexpected error occurred.');
    }
  }

  editTravelMode(review: any) {
    this.editingTravelModeId = review.travelModeId;
    this.selectedExpenditure = review.expenditureType;
    this.modeType = review.modeType;
    this.description = review.description || '';
    const rv = review.requiresVehicleType;
    this.requiresVehicleType = (rv === 'Yes' || rv === true || rv === 'true');
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
      const resp: any = await this.reimbursementService.deleteReimbursementTravelMode(row.travelModeId).toPromise();
      if (resp.serviceStatus === 'Success') {
        this.swalMessage('success', 'Travel mode deleted.');
        await this.onGetTravelMode();
      } else {
        this.swalMessage('warning', resp.serviceError || 'Delete failed.');
      }
    } catch (error) {
      console.error(error);
      this.swalMessage('error', 'Delete failed.');
    }
  }

  async onGetTravelMode() {
    const response: any = await this.reimbursementService.getTravelMode().toPromise();
    if (response.serviceStatus == 'Success') {
      this.travelModeList = response.serviceResponse || [];
    } else {
      this.travelModeList = [];
    }
  }

  async submitVehicleType() {
    try {
      this.vehicleType.createdBy = this.currentEmployeeInfo.empId;
      const newReason = this.vehicleType.vehicleTypeName.trim().toLowerCase();
      const duplicate = this.vehicleTypeList.some((r: any) =>
        r.vehicleTypeName.trim().toLowerCase() === newReason
        && r.id !== this.vehicleType.vehicleTypeId
      );
      if (duplicate) {
        this.swalMessage('warning', 'Vehicle type name already exists!');
        return;
      }
      const response: any = await this.reimbursementService.saveVehicleType(this.vehicleType).toPromise();
      if (response.serviceStatus === 'Success') {
        const wasEditV = !!this.vehicleType.vehicleTypeId;
        this.closeFormModal();
        this.swalMessage('success', wasEditV ? 'Vehicle type updated.' : 'Vehicle type saved.');
        this.resetForm();
        this.classCategory();
        await this.onGetVehicleType();
      } else {
        this.swalMessage('error', response.serviceError || 'Submission failed.');
      }
    } catch (error) {
      console.error('Error submitting vehicle type:', error);
      this.swalMessage('error', 'Error occurred while saving the vehicle type.');
    }
  }

  editVehicleType(row: any) {
    this.vehicleType = {
      vehicleTypeId: row.id,
      vehicleTypeName: row.vehicleTypeName,
      description: row.description || '',
      createdBy: this.currentEmployeeInfo?.empId
    };
    this.openVehicleTypeModal();
  }

  async deleteVehicleTypeRow(row: any) {
    const ok = await this.swalConfirmDelete(
      `Delete vehicle type <strong>${this.escapeHtml(row.vehicleTypeName)}</strong>?`
    );
    if (!ok) {
      return;
    }
    try {
      const resp: any = await this.reimbursementService.deleteVehicleType(row.id).toPromise();
      if (resp.serviceStatus === 'Success') {
        this.swalMessage('success', 'Vehicle type deleted.');
        await this.onGetVehicleType();
      } else {
        this.swalMessage('warning', resp.serviceError || 'Delete failed.');
      }
    } catch (error) {
      console.error(error);
      this.swalMessage('error', 'Delete failed.');
    }
  }

  async onGetVehicleType() {
    const response: any = await this.reimbursementService.onGetVehicleType().toPromise();
    if (response.serviceStatus == 'Success') {
      this.vehicleTypeList = response.serviceResponse || [];
    } else {
      this.vehicleTypeList = [];
    }
  }

  async saveFoodType() {
    try {
      this.foodType.createdBy = this.currentEmployeeInfo.empId;
      const newReason = this.foodType.foodTypeName.trim().toLowerCase();
      const duplicate = this.foodTypeList.some((r: any) =>
        r.foodTypeName.trim().toLowerCase() === newReason
        && r.id !== this.foodType.foodTypeId
      );
      if (duplicate) {
        this.swalMessage('warning', 'Food type name already exists!');
        return;
      }
      const payload = {
        foodTypeId: this.foodType.foodTypeId,
        foodTypeName: this.foodType.foodTypeName,
        description: this.foodType.description,
        createdBy: this.foodType.createdBy
      };
      const response: any = await this.reimbursementService.saveFoodType(payload).toPromise();
      if (response.serviceStatus === 'Success') {
        const wasEditF = !!this.foodType.foodTypeId;
        this.closeFormModal();
        this.swalMessage('success', wasEditF ? 'Food allowance type updated.' : 'Food allowance type saved.');
        this.resetForm();
        await this.onGetFoodType();
        this.foodTypeTable();
      } else {
        this.swalMessage('error', response.serviceError || 'Submission failed.');
      }
    } catch (error) {
      console.error('Error submitting food type:', error);
      this.swalMessage('error', 'Error occurred while saving the food type.');
    }
  }

  editFoodType(row: any) {
    this.foodType = {
      foodTypeId: row.id,
      foodTypeName: row.foodTypeName,
      description: row.description || '',
      createdBy: this.currentEmployeeInfo?.empId
    };
    this.openFoodTypeModal();
  }

  async deleteFoodTypeRow(row: any) {
    const ok = await this.swalConfirmDelete(
      `Delete food allowance type <strong>${this.escapeHtml(row.foodTypeName)}</strong>?`
    );
    if (!ok) {
      return;
    }
    try {
      const resp: any = await this.reimbursementService.deleteFoodType(row.id).toPromise();
      if (resp.serviceStatus === 'Success') {
        this.swalMessage('success', 'Food allowance type deleted.');
        await this.onGetFoodType();
      } else {
        this.swalMessage('warning', resp.serviceError || 'Delete failed.');
      }
    } catch (error) {
      console.error(error);
      this.swalMessage('error', 'Delete failed.');
    }
  }

  async onGetFoodType() {
    const response: any = await this.reimbursementService.onGetFoodType().toPromise();
    if (response.serviceStatus == 'Success') {
      this.foodTypeList = response.serviceResponse || [];
    } else {
      this.foodTypeList = [];
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

resetForm() {
  this.expenditureType = {
    expenditureTypeName: '',
    description: '',
    createdBy: ''
  };
  this.foodType = {
    foodTypeName: '',
    description: '',
    createdBy: ''
  };
  this.vehicleType = {
    vehicleTypeName: '',
    description: '',
    createdBy: ''
  };
  this.modeType = '';
  this.description = '';
  this.selectedExpenditure = '';
  this.requiresVehicleType = null;
  this.editingTravelModeId = null;
}

}
