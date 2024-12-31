import { Component, OnInit, EventEmitter, Input, Output, ViewChild } from '@angular/core';
import * as moment from 'moment';
import { first } from 'rxjs/operators';
import { CalendarComponent } from 'src/app/helpers/calendar/calendar.component';
import { Timesheet } from 'src/app/models/timesheet';
import { Employee360Service } from 'src/app/services/employee360.service';
import { TimesheetService } from 'src/app/services/timesheet.service';


@Component({
  selector: 'app-employee360-timesheet',
  templateUrl: './employee360-timesheet.component.html',
  styleUrls: ['./employee360-timesheet.component.css']
})
export class Employee360TimesheetComponent implements OnInit {

  @ViewChild("thisMonthCal")
  private thisMonthCalendar: CalendarComponent;
  @ViewChild("lastMonthCal")
  private lastMonthCalendar: CalendarComponent;

  activeButton: any;
  selectedFrequency: string = 'All';
  selectedOption: number = 1;
  activeCompOffButton: string;
  timesheetDetails: any;
  userMapping: any;
  projectName:any;
  teamName:any;
  empId:any;
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
    private employee360Service : Employee360Service,
    private timesheetService : TimesheetService
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
    if(this.activeButton !=='Calendar'){
      this.get360TimesheetDetails();
    }else{
      this.getTimesheetsForHomePageByEmpId('Last 7 Days');
    }
  }
  setFrequency(frequency: string) {
    this.selectedFrequency = frequency;
    console.log('Selected Frequency:', this.selectedFrequency);
}

  // getTimesheetsForHomePageByEmpId(arg0: string) {
  //   console.log('Hello')
  //   }
  getTimesheetsForHomePageByEmpId(dateRange: any) {
    this.timesheetDetails = [];
    const TOTAL_WORKING_HOURS_IN_DAY = 8;
    const currentDate = new Date();
    const dateFormat = 'YYYY-MM-DD';
    let fromDate: any;
    let toDate: any;
    let totaltimesheetDaysCount = 0;
    let filledTimesheetDetails = []

    let timesheetObj = new Timesheet();
    timesheetObj.empId = sessionStorage.getItem('employeeId');
    timesheetObj.empId = 21899;
    console.log(sessionStorage.getItem('employeeId'));
    console.log("timesheetObj.empId => " , timesheetObj.empId);
    

    if (dateRange == 'Last 7 Days') {
      totaltimesheetDaysCount = 7;
      const DAY_IN_MS = 24 * 60 * 60 * 1000;
      fromDate = new Date(currentDate.getTime() - (1 * DAY_IN_MS));
      toDate = new Date(currentDate.getTime() - (7 * DAY_IN_MS));

      //console.log(`Last 7 Days : ${moment(fromDate).format(dateFormat)} -- ${moment(toDate).format(dateFormat)}`);
      timesheetObj.startDate = moment(toDate).format(dateFormat);
      timesheetObj.endDate = moment(fromDate).format(dateFormat);
    } else if (dateRange == 'This Month') {
      totaltimesheetDaysCount = moment(`${currentDate.getFullYear()}-${currentDate.getMonth()}`, "YYYY-MM").daysInMonth()
      fromDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
      toDate = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0);

      //console.log(`This Month : ${moment(fromDate).format(dateFormat)} -- ${moment(toDate).format(dateFormat)}`);
      timesheetObj.startDate = moment(fromDate).format(dateFormat);
      timesheetObj.endDate = moment(toDate).format(dateFormat);
    } else if (dateRange == 'Last Month') {
      totaltimesheetDaysCount = moment(`${currentDate.getFullYear()}-${currentDate.getMonth() - 1}`, "YYYY-MM").daysInMonth()

      fromDate = new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1);
      toDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), 0);

      //console.log(`Last Month : ${moment(fromDate).format(dateFormat)} -- ${moment(toDate).format(dateFormat)}`);
      timesheetObj.startDate = moment(fromDate).format(dateFormat);
      timesheetObj.endDate = moment(toDate).format(dateFormat);
    }

    this.timesheetService.getTimesheetsForHomePageByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        filledTimesheetDetails = response.serviceResponse;
        //console.log("filledTimesheetDetails : ", filledTimesheetDetails);7, claimIds
      } else {
        console.error(response.serviceResponse);
      }

      let pendingCount = 0;
      let approvedCount = 0;
      let rejectedCount = 0;
      let notFilledCount = 0;

      for (let date = moment(timesheetObj.startDate); date.isSameOrBefore(timesheetObj.endDate); date.add(1, 'days')) {
        let newTimesheetObj = new Timesheet();
        newTimesheetObj.date = moment(date).format(dateFormat);

        if (newTimesheetObj.date) {
          let checkedTimesheet = filledTimesheetDetails.find(timesheet => timesheet.date == newTimesheetObj.date);

          if (checkedTimesheet) {
            newTimesheetObj = checkedTimesheet;
            newTimesheetObj.totalWorkingHoursPercentage = (newTimesheetObj.totalWorkingHours / TOTAL_WORKING_HOURS_IN_DAY) * 100 + "%";

            // For Chart Data
            if (newTimesheetObj.status == "Pending") {pendingCount++; }
            else if (newTimesheetObj.status == "Approved") {approvedCount++;}
            else if (newTimesheetObj.status == "Rejected") rejectedCount++;
          } else {
            newTimesheetObj.totalWorkingHoursPercentage = "0%";
            newTimesheetObj.status = "Not Filled";
            newTimesheetObj.dayType = "Not Filled";
            newTimesheetObj.weekDayName = this.getWeekDay(newTimesheetObj.date);
            notFilledCount++;
          }
        }
        this.timesheetDetails.push(newTimesheetObj);
      }

      //console.log("timesheetDetails : ", this.timesheetDetails);
      this.timesheetDetails.sort(this.dateCompare);

      let timesheetChartData = [{
        name: "Pending",
        y: pendingCount
      },
      {
        name: "Approved",
        y: approvedCount
      },
      {
        name: "Not Filled",
        y: notFilledCount
      },
      {
        name: "Rejected",
        y: rejectedCount
      }];

      // this.renderTimesheetChart(`${dateRange} Timesheet`, 'totalEODChart', timesheetChartData, 'Timesheet(s)');
      if (dateRange == 'This Month')
        this.thisMonthCalendar.addTimesheetDetails();
      else if (dateRange == 'Last Month')
        this.lastMonthCalendar.addTimesheetDetails();
    });
  }

  dateCompare(a, b) {
    const dateFormat = 'YYYY-MM-DD';
    return (moment(new Date(a.date)).format(dateFormat) < moment(new Date(b.date)).format(dateFormat)) ? -1 : 1;
  }

  getWeekDay(date: any): string {
    let weekDay = '';
    let day = new Date(date).getDay();

    switch (day) {
      case 0: {
        weekDay = 'Sunday';
        break;
      }
      case 1: {
        weekDay = 'Monday';
        break;
      }
      case 2: {
        weekDay = 'Tuesday';
        break;
      }
      case 3: {
        weekDay = 'Wednesday';
        break;
      }
      case 4: {
        weekDay = 'Thursday';
        break;
      }
      case 5: {
        weekDay = 'Friday';
        break;
      }
      case 6: {
        weekDay = 'Saturday';
        break;
      }

      default: {
        weekDay = '';
        break;
      }
    }
    return weekDay;
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
    console.log("save clicked ");
    
    const selectedEmpIds = this.data
      .filter(item => item.selected) 
      .map(item => item .empId); 
    console.log('Selected Employee IDs:', selectedEmpIds);
  }

  findByProject(arg0: any){
    console.log("project clicked =>  " + arg0 );
    
    this.projectName = arg0;
    this.byClick();
  }
  findByTeam(arg0: any){
    console.log("team clicked => " + arg0);
    
    this.teamName = arg0;
    this.byClick();
  }
  findByEmp(arg0: any){
    this.empId = arg0;
    this.byClick();
  }

  byClick() {
    let obj = {
      projectName : this.projectName,
      teamName: this.teamName,
      // empId:this.empId

    }
    }

    action(arg : any){
      console.log()
    }

    get360TimesheetDetails() {
      console.log(this.activeButton);
      this.employee360Service.get360TimesheetDetails(this.activeButton).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus === "Success") {
              console.log("=> serviceResponse", response.serviceResponse);
              this.data = response.serviceResponse;
              // this.data = null;
  
              // Transform the data from an object to an array
              this.data = Object.values(this.data); // Convert to array
              console.log("=> transformed data", this.data);
          }
      });
  }
  onChangeOption() {
    throw new Error('Method not implemented.');
    }

}
