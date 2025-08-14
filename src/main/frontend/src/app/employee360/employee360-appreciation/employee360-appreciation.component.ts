import { Component, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';
import { Appreciation } from 'src/app/models/appreciation';
import { Breadcrumb } from 'src/app/models/breadcrumd';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { HelpService } from 'src/app/services/help.service';
import { UtilityService } from 'src/app/services/utility.service';
import { SortPipe } from 'src/app/sort.pipe';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
@Component({
  selector: 'app-employee360-appreciation',
  templateUrl: './employee360-appreciation.component.html',
  styleUrls: ['./employee360-appreciation.component.css']
})
export class Employee360AppreciationComponent implements OnInit {
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  currentUser: User;
  isSearchEnabled: boolean = false;
  filters: any = {};
  employee: any[] = [];
  employeeData: any[] = [];
  myAppreciationList: any[] = [];
  teamAppreciationList: any[] = [];
  accortoselectedList: any[] = [];
  formattedDateRanges: string[] = [];
  isTeamAppreciationView: boolean = false;
  isDateRangeDisabled: boolean = false;
  selectedDateRange: string | null = null;
  selectedRange: string = '';
  page = 1;
  currentEmpId:any;
  currentEId:any;
  employeeList: any[] = [];
  matchedEmployees: any[] = [];
  matchedEmployee: any;
  currentBreadcrumbList: any[] = [];
  appreciationColumns: any[] = ['blank','appreciationEventName', 'appreciateType','appreciationByName', 'appreciationDate','comment'];
  appreciationTeamColumns: any[] = ['blank','appreciationEventName','appreciateType','appreciationToName', 'appreciationByName', 'appreciationDate', 'comment'];
  items = 10;
  employeesFor360: any[] = [];

  isTeam:boolean = true;


  constructor(
    private authenticationService: AuthenticationService,
    private employeeService: EmployeeService,
    private breadcrumbService: BreadcrumbService,
    private helpService: HelpService,
    private utilityService: UtilityService
  ) {
    const empData = sessionStorage.getItem('AllEmployees');
    if (empData) {
      this.employeeList = JSON.parse(empData);
      console.log(this.employeeList);
    }
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
    // this.getDateRanges();
    // this.getEmployeeInfo();
  }

  async ngOnInit(): Promise<void> {
   
    let findbreadcrumbObject = this.currentBreadcrumbList.findIndex(x => x.title == "Appreciation");
    if (findbreadcrumbObject >= 0) {
      this.currentBreadcrumbList.splice(findbreadcrumbObject + 1);
      this.removeActiveTab();
      this.breadcrumbService.setBreadcrumbSubject(this.currentBreadcrumbList);
    } else {
      let breadcrumbObject = new Breadcrumb();
      breadcrumbObject.title = "Appreciation";
      breadcrumbObject.url = "/employee-360/appreciation";
      this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    }

    let employeeData = sessionStorage.getItem('employee360Data');
    let employeeObject = JSON.parse(employeeData);
     this.currentEId = employeeObject.empId;
     this.currentEmpId = Number(employeeObject.employeementId.replace(/\D/g, ''));
    // this.calculateFinancialYear();
    this.appreciation.startDate = null;
    this.appreciation.endDate = null;
    this.getEmployeeAppreciationDetails();
    // this.getTeamAppreciationDetails();
   
  }
  exportToExcel(id:any): void {
   let exportToExcelTeamfile=id+".xlsx";
    const table = document.getElementById(''+id); // Get table by ID
    if (!table) {
      console.error('Table not found');
      return;
    }
  
    const worksheet: XLSX.WorkSheet = XLSX.utils.table_to_sheet(table); // Convert table to worksheet
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Project Data');
  
    const excelBuffer: any = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
    const data: Blob = new Blob([excelBuffer], { type: 'application/octet-stream' });
  
    saveAs(data, exportToExcelTeamfile);
  }
  ngAfterViewInit() {
    this.setActiveTab();
  }
  ngOnDestroy() {
    this.removeActiveTab();
  }
  refresh() {
    window.location.reload();
  }
  setActiveTab() {
    const tab = document.getElementById('Employee360Tab/appreciation').querySelector('.nav-link');
    tab.classList.add('active');
  }

  removeActiveTab() {
    const tab = document.getElementById('Employee360Tab').querySelector('.nav-link.active');
    tab?.classList.remove('active');
  }

  getTeamAppreciationList(){
    this.isTeam = false;
    // this.accortoselectedList = [];
    // this.accortoselectedList = this.teamAppreciationList;
    this.getTeamAppreciationDetails();

  }
  getTeamOrEmployeeDetails(){
    this.getTeamAppreciationDetails();
    this.getEmployeeAppreciationDetails();
  }

  getEmployeeAppreciationList(){
    this.isTeam = true;
    this.accortoselectedList = [];
    this.getEmployeeAppreciationDetails();
  }
 
  // getDateRanges() {
  //   this.employeeService.getDateRangesForDropdown(this.currentEmpId).subscribe(
  //     (data: any) => {
  //       this.formattedDateRanges = data.map((range: any) => {
  //         console.log(range.fromDate, "==", range.toDate);
  //         const fromYear = new Date(range.fromDate).getFullYear() - 1;
  //         const toYear = new Date(range.toDate).getFullYear();
  //         // if(toYear==fromYear){
  //         //   return `${fromYear}`;
  //         // }else{
  //         //   return `${fromYear}-${toYear}`;
  //         // }
  //         return `${fromYear}-${toYear}`;

  //       });
  //     },
  //     (error) => {
  //       console.error('Error fetching date ranges', error);
  //     }
  //   );
  // }

  financialYear: string = '';
  appreciation: Appreciation = new Appreciation();

  // calculateFinancialYear() {
  //   const today = new Date();
  //   const currentYear = today.getFullYear();
  //   const currentMonth = today.getMonth() + 1;
  //   let startYear, endYear;
  //   if (currentMonth >= 4) {
  //     startYear = currentYear;
  //     endYear = currentYear + 1;
  //   } else {
  //     startYear = currentYear - 1;
  //     endYear = currentYear;
  //   }
  //   this.financialYear = `${startYear}-${endYear}`;
  //   this.appreciation.startDate = `${startYear}-04-01`;
  //   this.appreciation.endDate = `${endYear}-03-31`;
  // }
  

  accToDateRangeSelect(){
    this.getEmployeeAppreciationDetails();
    this.getTeamAppreciationDetails();
  }

  getEmployeeAppreciationDetails() {
    this.appreciation.employeementId = this.currentEId;
    this.helpService.getMyAppreciationDetails(this.appreciation).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.accortoselectedList = response.serviceResponse;
        this.accortoselectedList.forEach(appObj => {
          appObj.emp360AppreciationBy =  appObj.appreciationByByEmpId;
          // appObj.appreciationDate = (appObj.appreciationDate)
          //   ? moment(appObj.appreciationDate).format(AppComponent.DATETIME_FORMAT)
          //   : null;
        });
      } else {
        console.error(response.serviceResponse);
      }
    });
  }


  getTeamAppreciationDetails(){
    this.appreciation.empId = this.currentEId;
    this.helpService.getTeamAppreciationDetails(this.appreciation).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamAppreciationList= response.serviceResponse;
        this.teamAppreciationList.forEach(appObj => {
          console.log("appObj.appreciationByByEmpId ", appObj.appreciationBy);
          appObj.emp360AppreciationBy = appObj.appreciationByByEmpId;
          appObj.emp360AppreciationTo = appObj.appreciationToByEmpId;
          // appObj.appreciationDate = (appObj.appreciationDate) 
          //     ? moment(appObj.appreciationDate).format(AppComponent.DATETIME_FORMAT) 
          //     : null;
        });
      } else {
        console.error(response.serviceResponse);
      }
    });
  }





  // getEmployeeInfo(fromDate?: string, toDate?: string): void {
  //   const requestPayload = {
  //     empId: this.currentEmpId,
  //     fromDate: fromDate || null,
  //     toDate: toDate || null
  //   };

  //   this.employeeService.getEmployeeAppreciationByEmpId(requestPayload).subscribe(
  //     (response: any) => {
  //       this.employee = response.appreciationDto;
  //       console.log('Employee appreciation data:', this.employee);
  //       console.log(" employeesFor360 details", this.employeesFor360);
  //       this.employee.forEach(y => {
  //         let matchingEmployee = this.employeesFor360.find(emp => emp.empId === y.empId);
  //         console.log("matchingEmployee ", matchingEmployee);
  //         y.emp360 = matchingEmployee ? matchingEmployee : {};
  //       });
  //       console.log('employee ** -- ', this.employee);
  //     },
  //     (error) => {
  //       console.error('Error fetching employee data:', error);
  //     }
  //   );
  // }

  // onSelectRange(event: Event): void {
  //   const target = event.target as HTMLSelectElement;
  //   this.selectedRange = target.value;

  //   if (this.selectedRange) {
  //     const selectedRange = this.selectedRange.split('-');
  //     const fromDate = selectedRange[0] + '-01-01';
  //     const toDate = selectedRange[1] + '-12-31';
  //     this.getEmployeeInfo(fromDate, toDate);
  //   } else {
  //     this.getEmployeeInfo();
  //   }
  // }

  // getTeamAppreciationData() {
  //   const requestPayload = {
  //     empId: this.currentEmpId
  //   };

  //   this.isTeamAppreciationView = !this.isTeamAppreciationView;
  //   // this.selectedDateRange = '';
  //   // this.isDateRangeDisabled = this.isTeamAppreciationView;

  //   if (this.isTeamAppreciationView) {
  //     this.employeeService.getTeamAppreciationByEmpId(requestPayload).subscribe(
  //       (response: any) => {
  //         this.employee = response.appreciationDto;

  //         this.getMatchingEmployees();
  //         console.log('Team appreciation data:', this.employee);
  //       },
  //       (error) => {
  //         console.error('Error fetching team data:', error);
  //       }
  //     );
  //   } else {
  //     this.getEmployeeInfo();
  //   }
  // }

  // getMatchingEmployees(): void {
  //   this.employee.forEach((empObj: any) => {
  //     this.employeeList.forEach((listObj: any) => {
  //       if (empObj.empId === listObj.empId) {
  //         this.matchedEmployees.push(listObj);
  //       }
  //     });
  //     console.log('Matched Employees:', this.matchedEmployees);
  //   },
  //     (error) => {
  //       console.error('Error fetching employee data:', error);
  //     });
  // }

  // getMatchedEmployee(event: any) {
  //   return this.matchedEmployees.find(employee => employee.empId === event.empId);
  // }

  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }
  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  onSearch(searchData) {
    this.filters = searchData;
  }

  formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
