import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-employee360-timesheet',
  templateUrl: './employee360-timesheet.component.html',
  styleUrls: ['./employee360-timesheet.component.css']
})
export class Employee360TimesheetComponent implements OnInit {
filterByProject(arg0: any) {
throw new Error('Method not implemented.');
}

  activeButton: any;
  activeCompOffButton: string;
  timesheetDetails: any;
  userMapping: any;

  data: any[] = [
    {
        id: 1,
        empId: 'E001',
        name: 'John Doe',
        date: new Date('2023-10-01'), // Example date
        dateType: 'Comp Off',
        activity: 'Worked on project A',
        project: 'Project A',
        teamName: 'Team Alpha',
        inTime: new Date('2023-10-01T09:00:00'), // Example in time
        outTime: new Date('2023-10-01T17:00:00'), // Example out time
        totalWorkingHrs: 8,
        shift: 'Day',
        status: 'Approved',
        appliedOn: new Date('2023-09-30') // Example applied on date
    },
    {
      id: 1,
      empId: 'E001',
      name: 'John Doe',
      date: new Date('2023-10-01'), // Example date
      dateType: 'Comp Off',
      activity: 'Worked on project A',
      project: 'Project A',
      teamName: 'Team Alpha',
      inTime: new Date('2023-10-01T09:00:00'), // Example in time
      outTime: new Date('2023-10-01T17:00:00'), // Example out time
      totalWorkingHrs: 8,
      shift: 'Day',
      status: 'Approved',
      appliedOn: new Date('2023-09-30') // Example applied on date
    },
    {
      id: 1,
      empId: 'E001',
      name: 'John Doe',
      date: new Date('2023-10-01'), // Example date
      dateType: 'Comp Off',
      activity: 'Worked on project A',
      project: 'Project A',
      teamName: 'Team Alpha',
      inTime: new Date('2023-10-01T09:00:00'), // Example in time
      outTime: new Date('2023-10-01T17:00:00'), // Example out time
      totalWorkingHrs: 8,
      shift: 'Day',
      status: 'Approved',
      appliedOn: new Date('2023-09-30') // Example applied on date
    },
    {
        id: 2,
        empId: 'E002',
        name: 'Jane Smith',
        date: new Date('2023-10-02'),
        dateType: 'Sick Leave',
        activity: 'Worked on project B',
        project: 'Project B',
        teamName: 'Team Beta',
        inTime: new Date('2023-10-02T09:30:00'),
        outTime: new Date('2023-10-02T17:30:00'),
        totalWorkingHrs: 8,
        shift: 'Day',
        status: 'Pending',
        appliedOn: new Date('2023-10-01')
    },
    {
        id: 3,
        empId: 'E003',
        name: 'Alice Johnson',
        date: new Date('2023-10-03'),
        dateType: 'Annual Leave',
        activity: 'Worked on project C',
        project: 'Project C',
        teamName: 'Team Gamma',
        inTime: new Date('2023-10-03T08:45:00'),
        outTime: new Date('2023-10-03T16:45:00'),
        totalWorkingHrs: 8,
        shift: 'Day',
        status: 'Approved',
        appliedOn: new Date('2023-10-02')
    },
    // Add more data as needed
];
allSelected: any;

  constructor() { }

  ngOnInit(): void {
    this.activeButton = 'Pending';
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

  yourFunction(){
    console.log("exected")
  }


}
