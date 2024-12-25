import { Component, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';

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
  formattedDateRanges: string[] = []; 
  isTeamAppreciationView: boolean = false;
  isDateRangeDisabled: boolean = true;
  selectedDateRange: string | null = null;
  selectedRange: string = '';
  page = 1;
  currentEmpId: number = Number(sessionStorage.getItem('employeeId'));

  constructor(
    private authenticationService: AuthenticationService,
    private employeeService: EmployeeService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.getDateRanges();
    this.getEmployeeInfo();
   }

  ngOnInit(): void {
    
  }
  getDateRanges() {
    this.employeeService.getDateRangesForDropdown(this.currentEmpId).subscribe(
      (data: any) => {
        this.formattedDateRanges = data.map((range: any) => {
          const fromYear = new Date(range.fromDate).getFullYear();
          const toYear = new Date(range.toDate).getFullYear();
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
      empId: this.currentEmpId
    };

    this.isTeamAppreciationView = !this.isTeamAppreciationView;
    this.selectedDateRange = '';
    this.isDateRangeDisabled = this.isTeamAppreciationView;

    if (this.isTeamAppreciationView) {
      this.employeeService.getTeamAppreciationByEmpId(requestPayload).subscribe(
        (response: any) => {
          this.employee = response.appreciationDto;
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
