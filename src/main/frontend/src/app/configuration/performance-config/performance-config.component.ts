import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { DepartmentService } from './../../services/department.service';

import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';

import { QuarterCycle } from 'src/app/models/quarterCycle';

import { Feature } from 'src/app/models/feature';
import { Log } from 'src/app/models/log';
import { review } from 'src/app/models/reviewType';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DomainService } from 'src/app/services/domain.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { LogService } from 'src/app/services/log.service';
import { PerformanceService } from 'src/app/services/performance.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-performance-config',
  templateUrl: './performance-config.component.html',
  styleUrls: ['./performance-config.component.css']
})
export class PerformanceConfigComponent implements OnInit {

  alertMessage: any;

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  isQuaterTable: boolean = true;
  isReviewTable: boolean = false;
  isReviewUpdation: boolean = false;
  isCreateReview: boolean = false;
  isReviewForm: boolean = false;
  reviewObj: review = new review();
  allSpecializationList: any[] = [];
  allDeptList: any[] = [];
  currentUser: User;
  userMapping: any = {};
  reviewTypelist: any[] = [];
  reviewColumns: any[] = ['blank', 'reviewLabel', 'reviewFieldType', 'condition', 'quarterCycle', 'departmentName', 'employeeName', 'createdOn', 'updatedByName', 'updatedOn']

  feature: "Performance Config";
  isQuaterForm: boolean = false;
  isQuaterCreation: boolean = false;
  isQuaterUpdation: boolean = false;
  isSearchEnabled: boolean = false;
  filters: any = {};
  isSearchEnabledReview: boolean = false;
  existingQuarters: any[] = [];
  items:any=10;
  quarterCycleDataForExcel: any[];

  quarterCyclesList: any;

  sortColumn: any;
  sortColumnType: any;
  sortDirection = 'asc';

  modalRef: BsModalRef = new BsModalRef();


  page = 1;
  quarterCycle = new QuarterCycle();
  quarterCycleUpdate = new QuarterCycle();

  quarterCycleColumns: any[] = ['blank', 'financialYear', 'quarterCycle', 'createdByName', 'createdOn', 'updatedByName', 'updatedOn'];

  log:Log;
  tabName:any = 'Configurations';
  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private datePipe: DatePipe,
    private authenticationService: AuthenticationService,
    private exportExcelService: ExportExcelService,
    private locationStrategy: LocationStrategy,
    private departmentService: DepartmentService,
    private domainService: DomainService,
    private performanceService:PerformanceService,
    private logService:LogService
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    this.showQuaterTable();
    this.getAllDepartmentList();
    this.logService.updateLogInfo(this.log);
    
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
   

    
    
  }

  months: { full: string, short: string }[] = [
    { full: 'January', short: 'JAN' }, { full: 'February', short: 'FEB' }, { full: 'March', short: 'MAR' },
    { full: 'April', short: 'APR' }, { full: 'May', short: 'MAY' }, { full: 'June', short: 'JUN' },
    { full: 'July', short: 'JUL' }, { full: 'August', short: 'AUG' }, { full: 'September', short: 'SEP' },
    { full: 'October', short: 'OCT' }, { full: 'November', short: 'NOV' }, { full: 'December', short: 'DEC' }
  ];

  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }



  validateQuarterCycleObj(quarterCycle: QuarterCycle, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(quarterCycle.fromMonth)) {
      this.alertMessage = "Please Select fromMonth!!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(quarterCycle.toMonth)) {
      this.alertMessage = "Please Select toMonth!!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  showQuaterTable() {
    this.isQuaterTable = true;
    this.isReviewForm = false;
    this.isReviewTable = false;
    this.isCreateReview = false;
    this.isQuaterCreation = false;
    this.isQuaterUpdation = false;
    this.isQuaterForm = false;
    this.getAllQuarterCycles();

  }

  setFinancialYear() {
    const currentDate = new Date();
    const currentYear = currentDate.getFullYear();
    const currentMonth = currentDate.getMonth() + 1; // Months are 0-based in JS

    if (currentMonth >= 4) {
      this.quarterCycle.fromYear = currentYear;
      this.quarterCycle.toYear = currentYear + 1;


    } else {
      this.quarterCycle.fromYear = currentYear - 1;
      this.quarterCycle.toYear = currentYear;


    }
  }

  showReviewTable() {
    this.isQuaterTable = false;
    this.isQuaterForm = false;
    this.isReviewForm = false;
    this.isReviewTable = true;
    this.isCreateReview = false;
    this.isReviewUpdation = false;
    this.isReviewForm = false;
    this.isReviewTable = true;
    this.isCreateReview = false;
    this.reviewObj = new review();
    this.getReviewType();
   
  }

  createQuarterCycle(template: TemplateRef<any>) {

    let inputValidated: boolean = this.validateQuarterCycleObj(this.quarterCycle, template)
   
    if (!inputValidated) return;
    this.quarterCycle.createdBy = this.currentUser.empId;
    this.quarterCycle.financialYear = `${this.quarterCycle.fromYear}-${this.quarterCycle.toYear}`;
    this.quarterCycle.quarterCycle = `${this.quarterCycle.fromMonth}-${this.quarterCycle.toMonth}`;
    this.quarterCycle.isActive = true;
    this.quarterCycle.isEnable = false;
   
    this.performanceService.createQuarterCycle(this.quarterCycle).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showQuaterTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });

  }

  updateQuarterCycle(template: TemplateRef<any>) {

    let inputValidated: boolean = this.validateQuarterCycleObj(this.quarterCycle, template)
    
    if (!inputValidated) return;
    

    this.quarterCycle.updatedBy = this.currentUser.empId;
    this.quarterCycle.financialYear = `${this.quarterCycle.fromYear}-${this.quarterCycle.toYear}`;
    this.quarterCycle.quarterCycle = `${this.quarterCycle.fromMonth}-${this.quarterCycle.toMonth}`;
  
    this.performanceService.updateQuarterCycle(this.quarterCycle).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showQuaterTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });

  }


  fetchExistingQuarters() {
    const financialYear = `${this.quarterCycle.fromYear}-${this.quarterCycle.toYear}`;

   
    this.performanceService.getQuartersByYear(financialYear).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.existingQuarters = response.serviceResponse;
        
      }
    });
  }

  getAllQuarterCycles() {
    this.performanceService.getAllQuarterCycles().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.quarterCyclesList = response.serviceResponse;
        this.quarterCyclesList.forEach(quarterCycleObj => {
          quarterCycleObj.updatedOn = (quarterCycleObj.updatedOn) ? moment(quarterCycleObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
          quarterCycleObj.createdOn = (quarterCycleObj.createdOn) ? moment(quarterCycleObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
      } else {
        alert(response.serviceResponse);
      }
    });
  }

  name = 'QuarterCycle.xlsx';
  exportToExcel(): void {
    this.performanceService.getAllQuarterCycles().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.quarterCycleDataForExcel = response.serviceResponse;
      }
      const onlySpecificDataArr = this.quarterCycleDataForExcel.map(
        x => ({
          "Financial Year": x.financialYear,
          "Quarter Cycle": x.quarterCycle,
          "Created By": x.createdByName,
          "Created on": (x.createdOn) ? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null,
          "Updated By": x.updatedByName,
          "Updated on": (x.updatedOn) ? moment(x.updatedOn).format(AppComponent.DATETIME_FORMAT) : null
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
    });
  }




  getMonthIndex(monthShort: string): number {
    const monthIndex = {
      'JAN': 1, 'FEB': 2, 'MAR': 3, 'APR': 4, 'MAY': 5, 'JUN': 6,
      'JUL': 7, 'AUG': 8, 'SEP': 9, 'OCT': 10, 'NOV': 11, 'DEC': 12
    };
    return monthIndex[monthShort] || 0;
  }

  checkMonthExistence(template: TemplateRef<any>) {
    if (!this.quarterCycle.fromMonth) return;

    const selectedMonthIndex = this.getMonthIndex(this.quarterCycle.fromMonth);
    let existingQuarter = '';

    const isOverlap = this.existingQuarters.some((quarter: any) => {
      const [existingFrom, existingTo] = quarter.quarterCycle.split('-');
      const existingFromIndex = this.getMonthIndex(existingFrom);
      const existingToIndex = this.getMonthIndex(existingTo);

      const overlaps = selectedMonthIndex >= existingFromIndex && selectedMonthIndex <= existingToIndex;

      if (overlaps) {
        existingQuarter = quarter.quarterCycle;
      }

      return overlaps;
    });

    if (isOverlap) {
      this.quarterCycle.fromMonth = '';
      this.openAlertMod(template, `The selected month ${this.quarterCycle.fromMonth} already exists in a quarter cycle ${existingQuarter}.`);

    }
  }

  checkMonthExistencee(type: 'fromMonth' | 'toMonth', template: TemplateRef<any>) {
    const selectedMonth = this.quarterCycle[type];
    if (!selectedMonth) return;

    const selectedMonthIndex = this.getMonthIndex(selectedMonth);
    let existingQuarter = '';

    const isOverlap = this.existingQuarters.some((quarter: any) => {
      const [existingFrom, existingTo] = quarter.quarterCycle.split('-');
      const existingFromIndex = this.getMonthIndex(existingFrom);
      const existingToIndex = this.getMonthIndex(existingTo);

      const overlaps = selectedMonthIndex >= existingFromIndex && selectedMonthIndex <= existingToIndex;

      if (overlaps) {
        existingQuarter = quarter.quarterCycle;
      }

      return overlaps;
    });

    if (isOverlap) {
      setTimeout(() => {
        this.quarterCycle[type] = '';
      });

      this.openAlertMod(template, `The selected month ${selectedMonth} already exists in the quarter cycle ${existingQuarter}.`);
    }
  }


  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  
  toggleSearchReviewType() {
    this.isSearchEnabledReview = !this.isSearchEnabledReview;
    if (!this.isSearchEnabledReview) {
      this.filters = {};
    }
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  showCreateReviewForm() {
    this.isQuaterTable = false;
    this.isReviewTable = false;
    this.isReviewForm = true;
    this.isCreateReview = true;
    this.getAllQuarterCycles();
    
    this.allSpecializationList = [];

    if (this.reviewObj.allSpecializationList == undefined || this.reviewObj.allSpecializationList.length == 0) {
      this.addInputSpecializationField();
    } else {
      this.allSpecializationList = this.reviewObj.allSpecializationList;
    }
  }

  Enable(quarterId: any, template: TemplateRef<any>) {
    const obj = new QuarterCycle();
    obj.isEnable = true;
    obj.quarterId = quarterId;
    this.performanceService.isEnable(obj).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, 'Quarter Cycle Enabled');
        this.showQuaterTable();
      }
    });
  }

  quarterIdToBeDeleted: any;
  DeleteConfirm(quarterId: any, template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.quarterIdToBeDeleted = quarterId;

  }

  Delete(template: TemplateRef<any>) {
    const obj = new QuarterCycle();
    obj.isActive = false;
    obj.quarterId = this.quarterIdToBeDeleted;


    this.performanceService.deleteQuarterCycle(obj).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, 'Quarter Cycle Deleted');
        this.showQuaterTable();
      }
    });
  }


  getByQuarterById(quarterId: any) {
    this.isQuaterTable = false;
    this.isQuaterForm = true;

    this.isQuaterCreation = false;

    this.isQuaterUpdation = true;
    this.performanceService.getQuarterCycleById(quarterId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {

        this.quarterCycle = response.serviceResponse;
        const [fromYear, toYear] = this.quarterCycle.financialYear.split("-");
        const [fromMonth, toMonth] = this.quarterCycle.quarterCycle.split("-");
        this.quarterCycle.fromYear = fromYear;
        this.quarterCycle.toYear = toYear;
        this.quarterCycle.fromMonth = fromMonth;
        this.quarterCycle.toMonth = toMonth;

        this.fetchExistingQuarters();

       
      } else {
        console.error(response.serviceResponse)
      }
    });


  }




  Disable(quarterId: any, template: TemplateRef<any>) {
    const obj = new QuarterCycle();
    obj.isEnable = false;
    obj.quarterId = quarterId;

    this.performanceService.isEnable(obj).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, 'Quarter Cycle Disabled');
        this.showQuaterTable();
      }
    });
  }




  showUpdateReviewForm(review: any) {
    this.isReviewUpdation = true;
    this.isQuaterTable = false;
    this.isReviewTable = false;
    this.isReviewForm = false;
    this.isCreateReview = false;
    this.allSpecializationList = [];

    this.reviewObj.reviewTypeId = review.reviewTypeId;
    
    this.reviewObj.deptId = [review.deptId];
    this.performanceService.getReviewTypeById(this.reviewObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.reviewObj = response.serviceResponse;      
        this.reviewObj.departmentName = review.departmentName;
        this.reviewObj.quarterCycle = review.quarterCycle;
        this.allSpecializationList.push(this.reviewObj);
      } else {
        console.error(response.serviceResponse);
      }
    });

  }



  addInputSpecializationField(template?: TemplateRef<any>) {

    let reviewObj = new review();
    this.allSpecializationList.push(reviewObj);
    // this.allSpecializationList.push("nsjd");
  }
  showQuaterCreateForm() {
    this.setFinancialYear();
    this.fetchExistingQuarters();
    this.isQuaterTable = false;
    this.isQuaterForm = true;
    this.isQuaterCreation = true;
    this.isQuaterUpdation = false;
    this.quarterCycle.fromMonth = '';
    this.quarterCycle.toMonth = '';
  }



  getAllDepartmentList() {
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
        

      } else {
        console.error(response.serviceResponse)
      }
    });
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

  removeInputSpecializationField(spec: any) {
    this.allSpecializationList.forEach((value, index) => {
      if (value == spec) {
        this.allSpecializationList.splice(index, 1);
      }
    });
    //console.log(this.allSpecializationList, " :this.allSpecializationList");
  }

  onSearch(searchData) {
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }


  //pagination
  createReview(template: TemplateRef<any>) {

    this.reviewObj.allSpecializationList = this.allSpecializationList;
    this.reviewObj.createdBy = this.currentUser.empId;
    this.reviewObj.deptId = this.reviewObj.deptId;
    this.reviewObj.quarterId = this.reviewObj.quarterId;
    console.log(this.reviewObj);
    this.performanceService.addReviewType(this.reviewObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.reviewObj = new review();
        this.openAlertMod(template, 'ReviewType Created Successfully');
        this.showReviewTable();
      } else {
        this.openAlertMod(template, 'ReviewType  Not Created ');
        console.error("API Response", response.serviceResponse);
      }
    });
  }


  updateReview(template: TemplateRef<any>) {

    let obj = {
      reviewTypeId: this.reviewObj.reviewTypeId,
      deptId: [this.reviewObj.deptId],
      quarterId: this.reviewObj.quarterId,
      updatedBy: this.currentUser.empId,
      allSpecializationList: [
        {
          reviewLabel: this.reviewObj.reviewLabel,
          reviewFieldType: this.reviewObj.reviewFieldType,
          condition: this.reviewObj.condition,
        }
      ]
    };

    console.log("Review Object to send:", obj);


    // Send the object to the backend API
    this.performanceService.updateReviewType(obj).subscribe(
      (response: any) => {
        console.log("API response received:", response);

        if (response.serviceStatus === "Success") {
          this.openAlertMod(template, 'Review Updated Successfully');
          this.reviewObj = new review();
          this.showReviewTable();
        } else {
          this.openAlertMod(template, 'ReviewType  Not Updated ');
          console.error("API response error:", response.serviceResponse);

        }
      },
      (error) => {
        console.log("API call failed with error:", error);
        // You can handle error here, like displaying a user-friendly error message
      }
    );
  }

  getReviewType() {
    this.performanceService.getReviewType().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.reviewTypelist = response.serviceResponse;
        this.reviewTypelist = response.serviceResponse.sort((a, b) => {
          return new Date(b.createdOn).getTime() - new Date(a.createdOn).getTime();
        });
      } else {
        console.error("API Response", response.serviceResponse);
        // this.openAlertMod(template, 'ReviewType  Not Fetched ');
      }


      console.log("vhgscvshdgc" + response.serviceResponse.quaterCycle);
    })
  }

  deleteReviewForm(obj: any, template: TemplateRef<any>) {

    this.performanceService.deleteReviewType(obj.reviewTypeId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        // this.reviewTypelist = response.serviceResponse;
        this.openAlertMod(template, 'ReviewType Deleted Successfully');
        this.showReviewTable();
      }
      else {
        this.openAlertMod(template, 'ReviewTypem Not Deleted ');
        console.log("API Response" + response.serviceResponse);
      }
    })
  }
  handlePageChange(event) {
    this.page = event;
  }



  fieldRestictCharacters(event) {
    const k = event.charCode;
    if (k >= 48 && k <= 57) {
      return true;
    }
    return false;
  }

  fieldRestrictNumber(event) {
    const k = event.charCode;
    if ((k >= 65 && k <= 90) || (k >= 97 && k <= 122) || (k === 32)) {
      return true;
    }

    return false;
  }

}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);

}



