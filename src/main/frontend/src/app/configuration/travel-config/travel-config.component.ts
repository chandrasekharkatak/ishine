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

  items:any=10;
  page:any=1;
  isSearchEnabledReview: boolean = false;
  isCategoryTable: boolean = true;
  createCategoryForm:boolean =false;
  filters: any = {};
  sortColumn: any;
  sortColumnType: any;
  sortDirection = 'asc';

  travelReason = {
    travelReasonName: '',
    description: ''
  };
  currentEmployeeInfo: Employee = new Employee();
  domainSpecializationList: any[];

  modeType: string = '';
  description: string = '';
  currentUser: any;
  alertMessage: any;
  travelReasonlist: any[] = []; 
  travelModelist: any[] = [];
  selectedTravelReasons: string[] = [];
  handlePageChange(event) {
    this.page = event;
  }
  reviewColumns: any[] = ['blank', 'reviewLabel', 'reviewFieldType', 'condition', 'quarterCycle', 'departmentName', 'employeeName', 'createdOn', 'updatedByName', 'updatedOn']

  constructor(private modalService: BsModalService,private travelDesk: TravelDeskService,
    private employeeService : EmployeeService,
    private authenticationService: AuthenticationService
  ) {this.authenticationService.currentUser.subscribe(x => this.currentUser = x) }

  ngOnInit(): void {
    this.onGetEmployeeInfo();
    this.onGetTravelReason();
    this.onGetTravelMode();
  }

  modalRef: BsModalRef = new BsModalRef();
  modalRef2: BsModalRef = new BsModalRef();
  openAlertMod1(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
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

  classCategoryForm:boolean=false;
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

  async submitTravelReason(template: TemplateRef<any>) {
    try {
      const response: any = await this.travelDesk.saveTravelReason(this.travelReason).toPromise();
      console.log('Travel Reason saved:', response);
      if (response.serviceStatus === "Success") {
        this.openAlertMod(template, "Travel Reason submitted successfully!");
        this.modalRef.hide();
      }
      else {
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

  async onGetTravelReason(){

    const response: any = await this.travelDesk.getTravelReason().toPromise();
    if (response.serviceStatus == "Success") {
      this.travelReasonlist = response.serviceResponse;
    
      console.log("travelReasonlist   ::::::: : ", this.travelReasonlist);

    } else {
      console.error(response.serviceResponse);
    }
  }


  async submitTravelMode(template: TemplateRef<any>) {
    if (!this.selectedTravelReasons || this.selectedTravelReasons.length === 0) {
      this.openAlertMod(template, "Please select at least one travel reason.");
      return;
    }
  
    try {
      const reason = this.selectedTravelReasons ;
      //for (const reason of this.selectedTravelReasons) {
        const travelModePayload = {
          travelReason: reason, 
          modeType: this.modeType,
          description: this.description,
          createdBy: this.currentEmployeeInfo?.empId || 0
        };
  
        const response: any = await this.travelDesk.saveTravelMode(travelModePayload).toPromise();
  
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

  async onGetTravelMode(){

    const response: any = await this.travelDesk.getTravelMode().toPromise();
    if (response.serviceStatus == "Success") {
      this.travelModelist = response.serviceResponse;
    
      console.log("travelModelist   ::::::: : ", this.travelModelist);

    } else {
      console.error(response.serviceResponse);
    }
  }


}

