import { Component, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { AppComponent } from 'src/app/app.component';
import { Breadcrumb } from 'src/app/models/breadcrumd';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { UtilityService } from 'src/app/services/utility.service';
import { SortPipe } from 'src/app/sort.pipe';
@Component({
  selector: 'app-employee360-appreciation',
  templateUrl: './employee360-appreciation.component.html',
  styleUrls: ['./employee360-appreciation.component.css']
})
export class Employee360AppreciationComponent implements OnInit {
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  currentUser: User;
  isSearchEnabled:boolean = false;
  filters:any = {};
  employee : any[]=[];
  employeeData : any[]=[];
  formattedDateRanges: string[] = []; 
  isTeamAppreciationView: boolean = false;
  isDateRangeDisabled: boolean = false;
  selectedDateRange: string | null = null;
  selectedRange: string = '';
  page = 1;
  currentEmpId: number = Number(sessionStorage.getItem('empIdA'));
  currentEId:number = Number(sessionStorage.getItem('eId'));
  employeeList: any[] = [];
  matchedEmployees: any[] = [];
  matchedEmployee: any;
  currentBreadcrumbList: any[] = [];
  appreciationColumns: any[] = ['', 'appreciateType', 'appreciationByName', 'appreciationDate','fromDate', 'toDate'];
  items = 10;
  employeesFor360:any[] = [];

  constructor(
    private authenticationService: AuthenticationService,
    private employeeService: EmployeeService,
    private breadcrumbService: BreadcrumbService,
    private utilityService: UtilityService
  ) {
    const empData = sessionStorage.getItem('AllEmployees');
    if (empData) {
      this.employeeList = JSON.parse(empData);
      console.log(this.employeeList);
    }
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
    this.getAllEmployeeFor360View();
    this.getDateRanges();
    this.getEmployeeInfo();
   }

  ngOnInit(): void {
    
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
  }
  ngAfterViewInit(){
    this.setActiveTab();
  }
  ngOnDestroy(){
    this.removeActiveTab();
  }
  setActiveTab(){
    const tab = document.getElementById('Employee360Tab/appreciation').querySelector('.nav-link');
    tab.classList.add('active');
  }

  removeActiveTab(){
    const tab = document.getElementById('Employee360Tab').querySelector('.nav-link.active');
    tab?.classList.remove('active');
  }
  getAllEmployeeFor360View(): void {
    this.employeesFor360 = [];
    this.employeeService.getAllEmployeesFor360View().subscribe({
        next: (response: any) => {
            if (response.serviceStatus == "Success") {
                this.employeesFor360 = response.serviceResponse;

                this.employeesFor360.forEach(employeeObj => {
                    // employeeObj.employeementId = this.utilityService.appendEmployeementid(employeeObj.isConsultant, employeeObj.employeementId);
                    employeeObj.dateOfJoining = employeeObj.dateOfJoining ? moment(employeeObj.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
                    employeeObj.dateOfRelieving = employeeObj.dateOfRelieving ? moment(employeeObj.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
                    employeeObj.updatedOn = employeeObj.updatedOn ? moment(employeeObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
                    employeeObj.createdOn = employeeObj.createdOn ? moment(employeeObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;

                    if (employeeObj.isConsultant == 'true')
                        employeeObj.employeeType = 'Consultant';
                    else if (employeeObj.isApprenticeship == 'true')
                        employeeObj.employeeType = 'Apprentice';
                    else
                        employeeObj.employeeType = 'Regular';
                });
                console.log("inside 360 employeesFor360",this.employeesFor360);

                this.employeesFor360 = new SortPipe().transform(this.employeesFor360, ['name', 'string', 'asc']);
            } else {
                alert(response.serviceResponse);
            }
        },
        error: (error) => {
            console.error("Error fetching employees:", error);
        }
    });
}
  getDateRanges() {
    this.employeeService.getDateRangesForDropdown(this.currentEmpId).subscribe(
      (data: any) => {
        this.formattedDateRanges = data.map((range: any) => {
          console.log(range.fromDate,"==",range.toDate);
          const fromYear = new Date(range.fromDate).getFullYear()-1;
          const toYear = new Date(range.toDate).getFullYear();
          // if(toYear==fromYear){
          //   return `${fromYear}`;
          // }else{
          //   return `${fromYear}-${toYear}`;
          // }
          return `${fromYear}-${toYear}`;
         
        });
      },
      (error) => {
        console.error('Error fetching date ranges', error);
      }
    );
  }

  getEmployeeInfo(fromDate?: string, toDate?: string): void {
    const requestPayload = {
      empId: this.currentEmpId,
      fromDate: fromDate || null,
      toDate: toDate || null
    };

    this.employeeService.getEmployeeAppreciationByEmpId(requestPayload).subscribe(
      (response: any) => {
        this.employee = response.appreciationDto;
        console.log('Employee appreciation data:', this.employee);
        console.log(" employeesFor360 details",this.employeesFor360)
        this.employee.forEach((y) => {
          let matchingEmployee = this.employeesFor360.find(emp => emp.empId == y.employeementId);
          console.log("matchingEmployee ", matchingEmployee);
          y.emp360 = matchingEmployee ? matchingEmployee : {};
      });
      },
      (error) => {
        console.error('Error fetching employee data:', error);
      }
    );
  }

  onSelectRange(event: Event): void {
    const target = event.target as HTMLSelectElement;
    this.selectedRange = target.value;
    
    if (this.selectedRange) {
      const selectedRange = this.selectedRange.split('-');
      const fromDate = selectedRange[0] + '-01-01';
      const toDate = selectedRange[1] + '-12-31'; 
      this.getEmployeeInfo(fromDate, toDate); 
    } else {
      this.getEmployeeInfo();
    }
  }

  getTeamAppreciationData() {
    const requestPayload = {
      empId: this.currentEId
    };

    this.isTeamAppreciationView = !this.isTeamAppreciationView;
    this.selectedDateRange = '';
    this.isDateRangeDisabled = this.isTeamAppreciationView;

    if (this.isTeamAppreciationView) {
      this.employeeService.getTeamAppreciationByEmpId(requestPayload).subscribe(
        (response: any) => {
          this.employee = response.appreciationDto;

          this.getMatchingEmployees();
          console.log('Team appreciation data:', this.employee);
        },
        (error) => {
          console.error('Error fetching team data:', error);
        }
      );
    } else {
      this.getEmployeeInfo();
    }
  }

  getMatchingEmployees(): void {
    this.employee.forEach((empObj: any) => {
      this.employeeList.forEach((listObj: any) => {
        if (empObj.empId === listObj.empId) {
          this.matchedEmployees.push(listObj);
        }
      });
    console.log('Matched Employees:', this.matchedEmployees);
    },
    (error) => {
      console.error('Error fetching employee data:', error);
    });
  }

  getMatchedEmployee(event: any) {
    return this.matchedEmployees.find(employee => employee.empId === event.empId);
  }

  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort){
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }
  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  onSearch(searchData){
    this.filters = searchData;
  }

  formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
