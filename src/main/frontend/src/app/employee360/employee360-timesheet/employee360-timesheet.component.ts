import { Component, OnInit } from '@angular/core';
import { first } from 'rxjs/operators';
import { Timesheet } from 'src/app/models/timesheet';
import { Employee360Service } from 'src/app/services/employee360.service';

@Component({
  selector: 'app-employee360-timesheet',
  templateUrl: './employee360-timesheet.component.html',
  styleUrls: ['./employee360-timesheet.component.css']
})
export class Employee360TimesheetComponent implements OnInit {


  activeButton: any;
  activeCompOffButton: string;
  timesheetDetails: any;
  userMapping: any;
  time

  data  :Timesheet [] = [];
//   data: any[] = [
//     {
//         id: 1,
//         empId: 'E001',
//         name: 'John Doe',
//         date: new Date('2023-10-01'), // Example date
//         dateType: 'Comp Off',
//         activity: 'Worked on project A',
//         project: 'Project A',
//         teamName: 'Team Alpha',
//         inTime: new Date('2023-10-01T09:00:00'), // Example in time
//         outTime: new Date('2023-10-01T17:00:00'), // Example out time
//         totalWorkingHrs: 8,
//         shift: 'Day',
//         status: 'Approved',
//         appliedOn: new Date('2023-09-30') // Example applied on date
//     }
//     // Add more data as needed
// ];
allSelected: any;

  constructor(
    private employee360Service : Employee360Service
  ) {
    
   }

  ngOnInit(): void {
    this.activeButton = 'Pending';
    this.get360TimesheetDetails();
  }

  setActiveButton(button: string): void {
    this.activeButton = button;
    // if (button === 'CompOff') {
    //   this.activeCompOffButton = 'Requests';
    // }
  }

  getTimesheetsForHomePageByEmpId(arg0: string) {
    console.log('Hello')
    }

  onToggle(item: any) {
      console.log('Toggled:', item);
  }

  toggleSelectAll() {
    this.data.forEach(item => {
      item.selected = this.allSelected; 
    });
  }

  updateSelection() {
    this.allSelected = this.data.every(item => item.selected); 
  }

  saveSelected() {
    const selectedEmpIds = this.data
      .filter(item => item.selected) 
      .map(item => item .empId); 
    console.log('Selected Employee IDs:', selectedEmpIds);
  }

  filterByProject(arg0: any) {
    throw new Error('Method not implemented.');
    }



    get360TimesheetDetails() {
      console.log(this.activeButton);
      this.employee360Service.get360TimesheetDetails(this.activeButton).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus === "Success") {
              console.log("=> serviceResponse", response.serviceResponse);
              this.data = response.serviceResponse;
  
              // Transform the data from an object to an array
              this.data = Object.values(this.data); // Convert to array
              console.log("=> transformed data", this.data);
          }
      });
  }

}
