import { Component, OnInit, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Employee } from 'src/app/models/employee';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { ReimbursementService } from 'src/app/services/reimbursement.service';
import { TravelDeskService } from 'src/app/services/travel-desk.service';

@Component({
  selector: 'app-reimbursment-config',
  templateUrl: './reimbursment-config.component.html',
  styleUrls: ['./reimbursment-config.component.css']
})
export class ReimbursmentConfigComponent implements OnInit {

  items:any=10;
  page:any;
  isSearchEnabledReview: boolean = false;
  isCategoryTable: boolean = true;
  createCategoryForm:boolean =false;
  filters: any = {};
  sortColumn: any;
  sortColumnType: any;
  sortDirection = 'asc';
  expenditureType = {
    expenditureTypeName: '',
    description: '',
    createdBy:''
  };

  
  foodType = {
    foodTypeName: '',
    description: '',
    createdBy:''
  };
  vehicleType = {
    vehicleTypeName :'',
    description: '',
    createdBy:''
  };
  selectedExpenditure: string = '';
  requiresVehicleType: string = '';
  vehicleTypeList:string = '';
  expenditureTypeList:any[] = [];
  foodTypeList:any[] = [];
  travelModeList:any[] = [];

  currentEmployeeInfo: Employee = new Employee();
  domainSpecializationList: any[];
  alertMessage: any;
  currentUser: User;
  modeType: any;
  description: any;
  handlePageChange(event) {
    this.page = event;
  }
  reviewColumns: any[] = ['blank', 'reviewLabel', 'reviewFieldType', 'condition', 'quarterCycle', 'departmentName', 'employeeName', 'createdOn', 'updatedByName', 'updatedOn']

  constructor(private modalService: BsModalService, private travelDesk: TravelDeskService,private reimbursementService:ReimbursementService,
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

  showQuaterTable(){
    this.isCategoryTable=true;
    this.isClass=false;
    this.istravelMode=false;
    this.travelModeForm=false;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
  }
  createCategory(){
    this.isCategoryTable=false;
    this.createCategoryForm=true;
    this.travelModeForm=false;
    this.classCategoryForm=false;
    this.isClass=false;
    this.istravelMode=false;

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
   this.travelModeForm=false;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
  }

  isClass:boolean=false;
  classCategory(){
    this.isClass=true;
    this.istravelMode=false;
    this.isCategoryTable=false;
    this.travelModeForm=false;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
  }

  foodAllowanceType(){
    this.isClass=false;
    this.istravelMode=false;
    this.isCategoryTable=false;
    this.travelModeForm=false;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
    this.foodCategoryForm= true;
  }

  classCategoryForm:boolean=false;
  foodCategoryForm:boolean=false;
  travelModeForm:boolean=false;
  subClassCategory(){
    this.travelModeForm=false;
    this.classCategoryForm=true;
    this.createCategoryForm=false;
    this.isClass=false;
    this.istravelMode=false;
    this.isCategoryTable=false;
  }

  travelCategory(){
    this.travelModeForm=true;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
    this.isClass=false;
    this.istravelMode=false;
    this.isCategoryTable=false;
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
   // location.reload();
  }

  modalRef: BsModalRef = new BsModalRef();
  modalRef2: BsModalRef = new BsModalRef();
  openAlertMod1(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }
  async submitExpenditureType(template: TemplateRef<any>) {
    try {
      this.expenditureType.createdBy = this.currentEmployeeInfo.empId; 
      console.log('created By ',this.expenditureType.createdBy);
      const response: any = await this.reimbursementService.saveExpenditureType(this.expenditureType).toPromise();
      console.log('Expenditure Type saved', response);
      if (response.serviceStatus === "Success") {
        this.openAlertMod(template, "Expenditure submitted successfully!");
        this.modalRef.hide();
      }
      else {
        this.openAlertMod(template, "Submission failed. Try again.!");
      }

    } catch (error) {
      console.error('Error submitting reason:', error);
      this.openAlertMod(template, "Error occurred while saving the Expenditure type.");
    }
  }


  async onGetExpenditureType() {

    const response: any = await this.reimbursementService.onGetExpenditureType().toPromise();
    if (response.serviceStatus == "Success") {
      this.expenditureTypeList = response.serviceResponse;

      console.log("expenditureTypeList   ::::::: : ", this.expenditureTypeList);

    } else {
      console.error(response.serviceResponse);
    }
  }
 

  async submitTravelMode(template: TemplateRef<any>) {
    if (!this.selectedExpenditure || this.selectedExpenditure.length === 0) {
      this.openAlertMod(template, "Please select at least one travel reason.");
      return;
    }

    try {
      const reason = this.selectedExpenditure;
      const travelModePayload = {
        expenditureType: reason,
        modeType: this.modeType,
        description: this.description,
        requiresVehicleType :this.requiresVehicleType,
        createdBy: this.currentEmployeeInfo?.empId || 0
      };

      console.log("travelModePayload :::::::::::::",travelModePayload);

      const response: any = await this.reimbursementService.saveTravelMode(travelModePayload).toPromise();

      if (response.serviceStatus !== 'Success') {
        console.error(`Error saving reason: ${reason}`, response.serviceError);
        this.openAlertMod(template, `Failed to save travel mode for reason: ${reason}`);
        return;
      }
      // }
      this.openAlertMod(template, 'Travel Mode saved successfully!');
    } catch (error) {
      console.error('API error:', error);
      this.openAlertMod(template, 'Unexpected error occurred!');
    }
  }

  async onGetTravelMode() {

    const response: any = await this.reimbursementService.getTravelMode().toPromise();
    if (response.serviceStatus == "Success") {
      this.travelModeList = response.serviceResponse;

      console.log("travelModelist   ::::::: : ", this.travelModeList);

    } else {
      console.error(response.serviceResponse);
    }
  }

  async submitVehicleType(template: TemplateRef<any>) {
    try {
      this.vehicleType.createdBy = this.currentEmployeeInfo.empId; 
      console.log('Vehicle details ',this.vehicleType);
      const response: any = await this.reimbursementService.saveVehicleType(this.vehicleType).toPromise();
      console.log('Vehicle Type saved', response);
      if (response.serviceStatus === "Success") {
        this.openAlertMod(template, "Vehicle submitted successfully!");
        this.modalRef.hide();
      }
      else {
        this.openAlertMod(template, "Submission failed. Try again.!");
      }

    } catch (error) {
      console.error('Error submitting reason:', error);
      this.openAlertMod(template, "Error occurred while saving the Vehicle type.");
    }
  }



  async onGetVehicleType() {

    const response: any = await this.reimbursementService.onGetVehicleType().toPromise();
    if (response.serviceStatus == "Success") {
      this.vehicleTypeList = response.serviceResponse;

      console.log("vehicleTypeList   ::::::: : ", this.vehicleTypeList);

    } else {
      console.error(response.serviceResponse);
    }
  }



  async saveFoodType(template: TemplateRef<any>) {
    try {
      this.foodType.createdBy = this.currentEmployeeInfo.empId; 
      console.log('Food details ',this.foodType);
      const response: any = await this.reimbursementService.saveFoodType(this.foodType).toPromise();
      console.log('Food Type saved', response);
      if (response.serviceStatus === "Success") {
        this.openAlertMod(template, "Food Type submitted successfully!");
        this.modalRef.hide();
      }
      else {
        this.openAlertMod(template, "Submission failed. Try again.!");
      }

    } catch (error) {
      console.error('Error submitting reason:', error);
      this.openAlertMod(template, "Error occurred while saving the Food type.");
    }
  }

  async onGetFoodType() {

    const response: any = await this.reimbursementService.onGetFoodType().toPromise();
    if (response.serviceStatus == "Success") {
      this.foodTypeList = response.serviceResponse;

      console.log("foodTypeList   ::::::: : ", this.foodTypeList);

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
