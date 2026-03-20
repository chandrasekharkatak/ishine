import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
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
import { UtilityService } from 'src/app/services/utility.service';
import { ValidationService } from 'src/app/services/validation.service';
import { HrHodMangerApiForPerformnace } from 'src/app/models/hrHodMangerApiForPerformnace';

@Component({
  standalone: false,
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
  static:any[] = [];
  feature= "Performance Config";
  isQuaterForm: boolean = false;
  isQuaterCreation: boolean = false;
  isQuaterUpdation: boolean = false;
  isSearchEnabled: boolean = false;
  filters: any = {};
  isSearchEnabledReview: boolean = false;
  existingQuarters: any[] = [];
  items:any=10;
  skip:any = 0;
  quarterCycleDataForExcel: any[];
  allEmployee: any[] = [];
  quarterCyclesList: any[]=[];
  allEmployee1: any[] = [];
  sortColumn: any;
  sortColumnType: any;
  sortDirection = 'asc';
  eligibleEmployees: any[] = [];
  modalRef!: NgbModalRef;
  clickedFinancialYear:any;
  clickedTemplate : TemplateRef<any>;
  clickerQuarterId: any;
  page = 1;
  quarterCycle = new QuarterCycle();
  quarterCycleUpdate = new QuarterCycle();
  employeesArray: any[] = [];
  errorMsg = '';
  totalItems = 0;
  itemsPerPage = 20;
  previewPage = 0;
  quarterCycleColumns: any[] = ['blank', 'financialYear', 'quarterCycle', 'createdByName', 'createdOn', 'updatedByName', 'updatedOn'];
  previewColumns: any[] = ['EmploymentId', 'financialYear', 'quarterCycle', 'createdByName', 'createdOn', 'updatedByName', 'updatedOn'];
  log:Log;
  tabName:any = 'Configurations';
  excelName: string;
  constructor(
    private validationService: ValidationService,
    private modalService: NgbModal,
    private datePipe: DatePipe,
    private authenticationService: AuthenticationService,
    private exportExcelService: ExportExcelService,
    private locationStrategy: LocationStrategy,
    private departmentService: DepartmentService,
    private domainService: DomainService,
    private performanceService:PerformanceService,
    private logService:LogService,
    private utilityService: UtilityService,
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

//for adding a drop down of financial years
  financialYears: string[] = [];
  selectedFinancialYear!: string;

  async ngOnInit(): Promise<void> {


    this.showQuaterTable();
    this.getAllDepartmentList();
    this.getReviewLabelForEveryDepartment();
    this.generateFinancialYears();
    this.setDefaultYears();
    this.logService.updateLogInfo(this.log);
    this.loadEmployeeData();
   let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
   featureMap.subFeatures?.forEach(sub => {
     this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
   });

  }

  generateFinancialYears(): void {
  const currentYear = new Date().getFullYear() ;
  const startYear = currentYear-1;
  const numberOfYears = 5;     // how many future years to be shown on dropdown 
  this.financialYears.push(`${startYear}-${startYear + 1}`);
  for (let i = 1; i < numberOfYears; i++) {
    const year = startYear + i;
    this.financialYears.push(`${year}-${year + 1}`);
  }
}
  
  cycleFrequency :string[] = ['Monthly','Quarterly', 'Half-Yearly' , 'Yearly'];
  selectedCycleFrequency!: string;

  // qaurterCycle  :string [] =['Q1(APR-JUN)', 'Q2(JUL-SEP)', 'Q3(OCT-DEC)', 'Q4(JAN-MAR)'];
  

  qaurterCycle : {range : string , abbreviation : string} [] = [
    { range: 'APR-JUN', abbreviation: 'Q1(APR-JUN)' },
    { range: 'JUL-SEP', abbreviation: 'Q2(JUL-SEP)' },
    { range: 'OCT-DEC', abbreviation: 'Q3(OCT-DEC)' },
    { range: 'JAN-MAR', abbreviation: 'Q4(JAN-MAR)' }
  ] 
  selectedQaurterCycle!: string;
;

  selectedHalfYearCycle!: string;
  selectedYearlyCycle : string  = 'APR-MAR';




  setDefaultYears(): void {
    const selectYear = this.selectedFinancialYear;
    if(selectYear==null){
      this.selectedFinancialYear = this.financialYears[0];
    }
  }

  // setDropDownData(): void {
  //  if(this.selectedFinancialYear!=null && this.selectedCycleFrequency==='Qaurterly'){
  //   this.quarterCycle.fromMonth = 
  //  }
  // }


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
    
  
    
    if(this.selectedCycleFrequency=='Monthly'){
      
    
    if (!this.validationService.validateNullUndefinedEmptyString(quarterCycle.fromMonth)) {
      this.alertMessage = "Please Select valid quarter!!"
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
  else
  {
    if(this.selectedCycleFrequency=='Quarterly'){
    if (!this.validationService.validateNullUndefinedEmptyString(this.selectedQaurterCycle)){
      this.alertMessage = "Please Select valid quarter!!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
  }
    else if (this.selectedCycleFrequency == 'Half-Yearly') {
      if (!this.validationService.validateNullUndefinedEmptyString(this.selectedHalfYearCycle)) {
        this.alertMessage = "Please Select valid half-year cycle!!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }
    else if (this.selectedCycleFrequency== 'Yearly') {
      if (!this.validationService.validateNullUndefinedEmptyString(this.selectedYearlyCycle)) {
        this.alertMessage = "Please Select valid year!!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }
    return true;
  }
}

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
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
    // const currentDate = new Date();
    // const currentYear = currentDate.getFullYear();
    // const currentMonth = currentDate.getMonth() + 1;

    // if (currentMonth >= 4) {
    //   // If it's April 2025 or later, set to previous financial year (2024-2025)
    //   this.quarterCycle.fromYear = currentYear - 1;
    //   this.quarterCycle.toYear = currentYear;
    // } else {
    //   // If it's Jan, Feb, or March 2025, financial year is still 2023-2024
    //   this.quarterCycle.fromYear = currentYear - 2;
    //   this.quarterCycle.toYear = currentYear - 1;
    // }
    this.quarterCycle.financialYear = this.selectedFinancialYear;
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

    
    console.log(this.selectedQaurterCycle);
    if(this.selectedCycleFrequency.toLowerCase()== 'quarterly'){
      // console.log()
       this.quarterCycle.quarterCycle = this.selectedQaurterCycle;
      //  this.quarterCycle.fromMonth = this.selectedQaurterCycle;
    }
    else if (this.selectedCycleFrequency.toLowerCase() == 'half-yearly') {
      this.quarterCycle.quarterCycle = this.selectedHalfYearCycle.slice(
        this.selectedHalfYearCycle.indexOf('(') + 1,
        this.selectedHalfYearCycle.indexOf(')')

      );
    }
    else if (this.selectedCycleFrequency.toLowerCase() == 'yearly') {
      this.quarterCycle.quarterCycle = this.selectedYearlyCycle;
    }
    else{
      
      this.quarterCycle.quarterCycle = `${this.quarterCycle.fromMonth}-${this.quarterCycle.toMonth}`;
    }

    let inputValidated: boolean = this.validateQuarterCycleObj(this.quarterCycle, template)

    if (!inputValidated) return;

    this.quarterCycle.createdBy = this.currentUser.empId;
    this.quarterCycle.financialYear = this.selectedFinancialYear;
    this.quarterCycle.isActive = true;
    this.quarterCycle.isEnable = false;
    this.quarterCycle.cycleType = this.selectedCycleFrequency;
    console.log(this.selectedCycleFrequency);
    console.log(this.quarterCycle.cycleType);

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
    // const financialYear = `${this.quarterCycle.fromYear}-${this.quarterCycle.toYear}`;
    const financialYear = this.selectedFinancialYear;
    this.performanceService.getQuartersByYear(financialYear).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.existingQuarters = response.serviceResponse;
        console.log(this.existingQuarters);

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
          quarterCycleObj.emp360CreatedBy = quarterCycleObj.createdBy;
          quarterCycleObj.emp360CreatedBy = quarterCycleObj.updatedBy;

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
          "Cycle": x.quarterCycle,
          "Created By": x.createdByName,
          "Created on": (x.createdOn) ? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null,
          "Updated By": x.updatedByName,
          "Updated on": (x.updatedOn) ? moment(x.updatedOn).format(AppComponent.DATETIME_FORMAT) : null
        })
      )
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
    });
  }

  // exportToExcel(id:any): void {
  //   const tableId = id; // Replace with your actual table ID
  //   this.excelName = "QuarterCycle.xlsx";
  //   this.tabName= 'Quarter Cycle Table';

  //   this.exportExcelService.exportTableFormat(tableId,this.excelName,this.tabName);
  // }

  fileName = 'eligibleEmp.xlsx';
  eligiblePreviewDataForExcel: any[] = [];

  exportToExcelPreviewData() {
    console.log(this.quarterCycle.quarterId);
    this.performanceService.exportExcelForEligiblePreview(this.clickedFinancialYear , this.currentUser.empId).pipe(first()).subscribe((response: any) => {
      if(response.serviceStatus == "Success"){
        this.eligiblePreviewDataForExcel = response.serviceResponse;
      }

      const excelArray = this.eligiblePreviewDataForExcel.map(
        x => ({
          "EmploymentId": x.employmentId,
          "Date of Joining": (x.dateOfJoining) ? moment(x.dateOfJoining).format(AppComponent.DATETIME_FORMAT) : null,
          "Email": x.email,
          "Employment Status": x.employmentstatus,
          "Name": x.name,
          "Department Name" : x.departmentName,
          "Manager Name" : x.managerName,
          "Reporting Manager Name" : x.reportingManagerName,
          "HOD Name" : x.hodName,
          "finalRating": x.finalRating
    })
  )
  this.exportExcelService.exportTableDataToExcel(excelArray, this.fileName)
})
  }
  




  getMonthIndex(monthShort: string): number {
    const monthIndex = {
      'JAN': 1, 'FEB': 2, 'MAR': 3, 'APR': 4, 'MAY': 5, 'JUN': 6,
      'JUL': 7, 'AUG': 8, 'SEP': 9, 'OCT': 10, 'NOV': 11, 'DEC': 12
    };
    return monthIndex[monthShort] || 0;
  }
  isAllSelected = false;  // Track if all departments are selected

// Toggle Select/Deselect All
toggleSelectAll() {
  if (this.isAllSelected) {
    // Deselect all if already selected
    this.reviewObj.deptId = [];
    this.isAllSelected = false;
  } else {
    // Select all departments
    this.reviewObj.deptId = this.allDeptList.map(dept => dept.deptId);
    this.isAllSelected = true;
  }
}

// Handle selection change
// onDepartmentChange() {
//   const selectedDepartments = this.reviewObj.deptId;
//   this.isAllSelected = selectedDepartments.length === this.allDeptList.length;
// }
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
      this.openAlertMod(template, `The selected month ${this.quarterCycle.fromMonth} already exists in a cycle ${existingQuarter}.`);

    }
  }

  // checkMonthExistencee(type: 'fromMonth' | 'toMonth', template: TemplateRef<any>) {
  //   const selectedMonth = this.quarterCycle[type];
  //   if (!selectedMonth) return;

  //   const selectedMonthIndex = this.getMonthIndex(selectedMonth);
  //   let existingQuarter = '';

  //   const isOverlap = this.existingQuarters.some((quarter: any) => {
  //     const [existingFrom, existingTo] = quarter.quarterCycle.split('-');
  //     const existingFromIndex = this.getMonthIndex(existingFrom);
  //     const existingToIndex = this.getMonthIndex(existingTo);

  //     const overlaps = selectedMonthIndex >= existingFromIndex && selectedMonthIndex <= existingToIndex;

  //     if (overlaps) {
  //       existingQuarter = quarter.quarterCycle;
  //     }

  //     return overlaps;
  //   });

  //   if (isOverlap) {
  //     setTimeout(() => {
  //       this.quarterCycle[type] = '';
  //     });

  //     this.openAlertMod(template, `The selected month ${selectedMonth} already exists in the quarter cycle ${existingQuarter}.`);
  //   }
  // }
  
  
checkQuaterlyExistance( template: TemplateRef<any>){
console.log(this.selectedQaurterCycle);
const fromMonth = this.selectedQaurterCycle.split('-')[0];
const toMonth = this.selectedQaurterCycle.split('-')[1];
console.log(fromMonth);
console.log(toMonth);
this.validateSelectedQaurterCycle(fromMonth, toMonth, template);
}


  checkHalfYearlyExistance( template: TemplateRef<any>){
    console.log('hallfff callled');
    console.log('half year cycle',this.selectedHalfYearCycle);
    let fromMonth !:string;
    let toMonth !:string;
    if(this.selectedHalfYearCycle == 'H1 (APR-SEP)'){
      fromMonth = 'APR';
      toMonth = 'SEP';

    }
      else{
        fromMonth = 'OCT';
        toMonth = 'MAR';
      }
     
      console.log('fromMonth' , fromMonth);
      console.log('toMonth',toMonth);       

      this.validateSelectedQaurterCycle(fromMonth, toMonth, template);

    


  }

  checkYearlyExistance( template: TemplateRef<any>){
    console.log('yearly callled');
    console.log('yearly cycle',this.selectedYearlyCycle);
    const fromMonth = 'APR';
    const toMonth = 'MAR';

    this.validateSelectedQaurterCycle(fromMonth, toMonth, template);

  }

  validateSelectedQaurterCycle(fromMonth: string, toMonth: string, template: TemplateRef<any>) {
    const selectedFromMonth = fromMonth;
    const selectedToMonth = toMonth;
    const selectedYear=this.selectedFinancialYear;
    if (!selectedFromMonth || !selectedToMonth) return;

     const fromMonthIndex = this.getMonthIndex(fromMonth);
    const toMonthIndex = this.getMonthIndex(toMonth);

    let overlappingCycle = '';

    const isOverlap = this.existingQuarters.some((quarter: any) => {
      const [existingFrom, existingTo] = quarter.quarterCycle.split('-');
      const existingFromIndex = this.getMonthIndex(existingFrom);
      const existingToIndex = this.getMonthIndex(existingTo);
      const existingYear = quarter.financialYear
   
      if (this.isQuaterUpdation && quarter.quarterId === this.quarterCycle.quarterId) {
        return false;
    }

      // Check if new range overlaps with any existing cycle
      const overlaps =(
        (fromMonthIndex >= existingFromIndex && fromMonthIndex <= existingToIndex  ) ||
        (toMonthIndex >= existingFromIndex && toMonthIndex <= existingToIndex) ||
        (fromMonthIndex <= existingFromIndex && toMonthIndex >= existingToIndex)
        
      ) && existingYear === selectedYear;
      if (overlaps) {
        overlappingCycle = quarter.quarterCycle;
      }

      return overlaps;
  });

  if (isOverlap) {
      this.selectedQaurterCycle ='';
    this.openAlertMod(template, `The selected range ${fromMonth}-${toMonth} overlaps with an existing cycle ${overlappingCycle}.`);

    };

    console.log('overlapping cycle --- - - - - ',overlappingCycle);




}

  

  checkMonthExistencee(type: 'fromMonth' | 'toMonth', template: TemplateRef<any>) {
   
      this.validateSelectedQaurterCycle(this.quarterCycle.fromMonth, this.quarterCycle.toMonth, template);

    // const selectedFromMonth = this.quarterCycle.fromMonth;
    // const selectedToMonth = this.quarterCycle.toMonth;
    // const selectedYear = this.quarterCycle.financialYear;

    // if (!selectedFromMonth || !selectedToMonth) return;

    // const selectedFromIndex = this.getMonthIndex(selectedFromMonth);
    // const selectedToIndex = this.getMonthIndex(selectedToMonth);

    // let overlappingCycle = '';
     

    // const isOverlap = this.existingQuarters.some((quarter: any) => {
    //   const [existingFrom, existingTo] = quarter.quarterCycle.split('-');
    //   const existingFromIndex = this.getMonthIndex(existingFrom);
    //   const existingToIndex = this.getMonthIndex(existingTo);
   
    //   if (this.isQuaterUpdation && quarter.quarterId === this.quarterCycle.quarterId) {
    //     return false;
    // }

    //   // Check if new range overlaps with any existing cycle
    //   const overlaps =
    //     (selectedFromIndex >= existingFromIndex && selectedFromIndex <= existingToIndex) ||
    //     (selectedToIndex >= existingFromIndex && selectedToIndex <= existingToIndex) ||
    //     (selectedFromIndex <= existingFromIndex && selectedToIndex >= existingToIndex);

    //   if (overlaps) {
    //     overlappingCycle = quarter.quarterCycle;
    //   }

    //   return overlaps;
    // });

    // if (isOverlap) {
    //   setTimeout(() => {
    //     this.quarterCycle[type] = '';
    //   });

    //   this.openAlertMod(template, `The selected range ${selectedFromMonth}-${selectedToMonth} overlaps with an existing cycle ${overlappingCycle}.`);
    // }
}




// getQuarterTable( quarterId:any ,financialYear :any , template : TemplateRef<any>)
// {
//   this.employeesArray = [];
//   this.errorMsg = '';
//   this.previewPage = 0;
//   this.totalItems = 0;

//   const obj = new QuarterCycle();
//   obj.financialYear = financialYear;
//   this.clickedFinancialYear = obj.financialYear;
//   obj.quarterId = quarterId;
//   this.clickerQuarterId = quarterId;
//   console.log('year is : ',financialYear);
//   // console.log('quarterId',quarterId);
//   this.clickedTemplate = template;

//   console.log('currentUser -- ',this.currentUser.empId)

//   this.performanceService
//     .getAllEmployeePerformanceForQuarter(quarterId,financialYear,this.currentUser.empId , this.previewPage , this.itemsPerPage)
//     .pipe(first()) 
//     .subscribe(
//       (res: any) => {
//         if (res.serviceStatus === 'Success') {
//           const response = res.serviceResponse;
//           this.employeesArray = response.data;
//           this.totalItems = response.totalCount;          

//           console.log(this.employeesArray);
//            this.modalRef = this.modalService.show(template, { class: 'modal-lg' });

//         } else {
//           this.errorMsg = res.serviceResponse;
//         }
        
//       },
//       () => {
//         this.errorMsg = 'Failed to load employee data';
//       }
//     );
// }

// pageChanged(event) {
//   this.itemsPerPage = event.pageSize; 
//   this.previewPage = event.pageIndex;
//   this.getQuarterTable(this.clickerQuarterId,this.clickedFinancialYear , this.clickedTemplate)
// }

// trying
 getQuarterTable(quarterId: any, financialYear: any, template: TemplateRef<any>) {
  this.employeesArray = [];
  this.errorMsg = '';
  this.previewPage = 0;
  this.totalItems = 0;
  this.clickedFinancialYear = financialYear;
  this.clickerQuarterId = quarterId;
  this.clickedTemplate = template;

  this.loadEmployeeData();
  // this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
  this.modalRef = this.modalService.open(template, { 
    size: 'xl',           // Extra large size
    centered: true,       // Center the modal
    scrollable: true,     // Enable scrolling
    backdrop: 'static',   // Prevent closing on backdrop click (optional)
    windowClass: 'custom-wide-modal' // Custom class for additional styling
  });
}
loadEmployeeData() {
  console.log('Loading page:', this.previewPage);
  
  this.performanceService
    .getAllEmployeePerformanceForQuarter(
      this.clickerQuarterId,
      this.clickedFinancialYear,
      this.currentUser.empId,
      this.previewPage,
      this.itemsPerPage
    )
    .pipe(first())
    .subscribe(
      (res: any) => {
        if (res.serviceStatus === 'Success') {
          const response = res.serviceResponse;
          this.employeesArray = response.data;
          this.totalItems = response.totalCount;
          console.log('Loaded employees:', this.employeesArray);
        } else {
          this.errorMsg = res.serviceResponse;
        }
      },
      () => {
        this.errorMsg = 'Failed to load employee data';
      }
    );
}

pageChangedPreview(event)
{
  this.itemsPerPage = event.pageSize;
  this.previewPage = event.pageIndex;
  this.loadEmployeeData();
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
    this.modalRef?.close();
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
    console.log('isEnable',this.quarterCycle.isEnable);
     const alreadyEnabled = this.quarterCyclesList?.some(
    (cycle: any) => cycle.isEnable === true
  );

  if (alreadyEnabled) {
    this.openAlertMod(template, 'Only one cycle can be enabled at a time');
    return;
  }
    obj.quarterId = quarterId;
    this.performanceService.isEnable(obj).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
       
        this.openAlertMod(template, 'Cycle Enabled');
        this.showQuaterTable();
      }
    });
  }

  quarterIdToBeDeleted: any;
  DeleteConfirm(quarterId: any, template: TemplateRef<any>) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.quarterIdToBeDeleted = quarterId;

  }

  Delete(template: TemplateRef<any>) {
    const obj = new QuarterCycle();
    obj.isActive = false;
    obj.quarterId = this.quarterIdToBeDeleted;


    this.performanceService.deleteQuarterCycle(obj).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, 'Cycle Deleted');
        this.showQuaterTable();
      }
    });
  }


  getByQuarterById(quarterId: any) {
    // this.fetchExistingQuarters();
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
        this.openAlertMod(template, 'Cycle Disabled');
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

  validateReviewTypes(template: TemplateRef<any>,reviewObj:review,allSpecializationList:any[] ){

    if(!this.validationService.validateNullUndefinedEmptyString(reviewObj.quarterId)){
      this.alertMessage = "Please select Cycle !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }


    let flag = true;
    allSpecializationList.forEach((spec:any, index) => {
      if(!this.validationService.validateNullUndefinedEmptyString(spec.reviewLabel)){
         this.alertMessage = `Please enter Review Label ${index+1} !!`;
         this.openAlertMod(template, this.alertMessage);
         flag = false;
         return;
      }
      if(!this.validationService.validateNullUndefinedEmptyString(spec.reviewFieldType)){
        this.alertMessage = `Please select Review Field Type ${index+1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return;
      }
      if(!this.validationService.validateNullUndefinedEmptyString(spec.condition)){
        this.alertMessage = `Please enter Limit ${index+1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        return;
     }

    });
    return flag;
  }

  reviewObj1:any[] = [];
  //pagination
  createReview(reviewObj:any,template: TemplateRef<any>) {


    if(reviewObj.deptId == null || reviewObj.deptId == '' ||reviewObj.quarterId == null || reviewObj.quarterId=='' ){
      this.openAlertMod(template, 'Please Select Department');
      return;
    }
    let inputValidated: boolean = this.validateReviewTypes(template, reviewObj, this.allSpecializationList);
    if (!inputValidated) return;



    const reviewLabels = this.allSpecializationList.map(spec => spec.reviewLabel);
    const duplicateLabels = reviewLabels.filter((label, index) => reviewLabels.indexOf(label) !== index);



    if (duplicateLabels.length > 0) {
      this.openAlertMod(template, 'Review Labels must be unique.');
      return;
    }
    for (let spec of this.allSpecializationList) {
      const cond = Number(spec.condition);
      if (cond > 5) {
        this.openAlertMod(template, 'Rating limit must be 5 or less (maximum 5).');
        return;
      }
      if (cond <= 0) {
        this.openAlertMod(template, 'Limit value must be greater than 0.');
        return;
      }
      if (spec.condition == null || spec.condition === '') {
        this.openAlertMod(template, 'Limit cannot be empty.');
        return;
      }
    }
    // if()
    this.reviewObj.allSpecializationList = this.allSpecializationList;
    this.reviewObj.createdBy = this.currentUser.empId;

    this.performanceService.addReviewType(this.reviewObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.reviewObj = new review();
        this.openAlertMod(template, response.serviceResponse);
        this.showReviewTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
        console.error("API Response", response.serviceResponse);
      }
    });
  }


  updateReview(template: TemplateRef<any>) {
    let inputValidated: boolean = this.validateReviewTypes(template, this.reviewObj, this.allSpecializationList);
    if (!inputValidated) return;


    if(this.reviewObj.deptId == null || this.reviewObj.deptId == '' || this.reviewObj.quarterId == null || this.reviewObj.quarterId=='' ){
      this.openAlertMod(template, 'Please Select Department');
      return;
    }
    for (let spec of this.allSpecializationList) {
      const cond = Number(spec.condition);
      if (cond > 5) {
        this.openAlertMod(template, 'Rating limit must be 5 or less (maximum 5).');
        return;
      }
      if (cond <= 0) {
        this.openAlertMod(template, 'Limit value must be greater than 0.');
        return;
      }
      if (spec.condition == null || spec.condition === '') {
        this.openAlertMod(template, 'Limit cannot be empty.');
        return;
      }
    }

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
          this.openAlertMod(template, response.serviceResponse);
          this.reviewObj = new review();
          this.showReviewTable();
        } else {
          this.openAlertMod(template, response.serviceResponse);
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
        this.reviewTypelist.forEach((employee) => {
          employee.emp360CreatedBy = employee.createdBy;
         employee.emp360UpdatedBy = employee.updatedBy;
        });
      } else {
        console.error("API Response", response.serviceResponse);
        // this.openAlertMod(template, 'ReviewType  Not Fetched ');
      }

    })
  }


  rewadardDeleteId:any;
  openConfirmDeleteModal(template: TemplateRef<any>, rewardID: any) {
    this.rewadardDeleteId=rewardID.reviewTypeId
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
  }
  deleteReviewForm(template: TemplateRef<any>) {

    this.performanceService.deleteReviewType(this.rewadardDeleteId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        // this.reviewTypelist = response.serviceResponse;
        this.openAlertMod(template,  response.serviceResponse);
        this.showReviewTable();
      }
      else {
        this.openAlertMod(template,  response.serviceResponse);
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
    const inputValue = event.target.value;
    if ((k >= 65 && k <= 90) || (k >= 97 && k <= 122)) {
        return true;
    }
    if (k === 32 && inputValue.length > 0) {
        return true;
    }
    return false;
}

  // fieldRestrictNumber(event) {
  //   const k = event.charCode;
  //   if ((k >= 65 && k <= 90) || (k >= 97 && k <= 122) || (k === 32)) {
  //     return true;
  //   }


  //   return false;
  // }



  getReviewLabelForEveryDepartment(){
    this.performanceService.getReviewLabelForEveryDepartment().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.reviewObj1 = response.serviceResponse;
        console.log('Review Label changed:', this.reviewObj1 );
      }
    });
  }
  onReviewLabelChange(reviewLabel:any,template: TemplateRef<any>) {

    const re = this.reviewObj1.filter(res =>
      res.reviewLabel == reviewLabel &&
      (Array.isArray(this.reviewObj.deptId) ? this.reviewObj.deptId.includes(res.departmentId) : res.departmentId === this.reviewObj.deptId) &&
      res.quarterId == this.reviewObj.quarterId

    );

    if (re.length > 0) {
      this.openAlertMod(template, 'Review Labels Already Exist');
      return;
    }
  }




}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);

}




