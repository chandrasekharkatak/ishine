import { Component, OnInit, TemplateRef, Input, Output, ViewChild } from '@angular/core';
import * as moment from 'moment';
import { first } from 'rxjs/operators';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { CalendarComponent } from 'src/app/helpers/calendar/calendar.component';
import { Timesheet } from 'src/app/models/timesheet';
import { Employee360Service } from 'src/app/services/employee360.service';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { Modal } from 'bootstrap';
import { OwlDateTimeComponent } from 'ng-pick-datetime';
import { ScrollStrategy } from '@angular/cdk/overlay';
import { DatePipe } from '@angular/common';



import { Router } from '@angular/router';
import { Breadcrumb } from 'src/app/models/breadcrumd';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';

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
  
  @ViewChild("alertTemplate") alertModal: TemplateRef<any>;

  showModal = false;
  alertMessage: string = '';
  activeButton: any;
  selectedFrequency: string = 'All';
  selectedOption: number = 1;
  activeCompOffButton: string;
  timesheetDetails: any;
  userMapping: any;
  projectName:any;
  teamName:any="null";
  empId:any=0;
  empIdd:any;
  projectId:any=0;
  currentUser:any;
  time;
  managerId:any;
  endDate:any ;
  startDate:any ;
  scrollStrategy: ScrollStrategy;
  data  :Timesheet [] = [];
  formattedStartDate:any;
  formattedEndDate: any;
  allSelected: any;
  dateTimeRange: any = null;
  todayDate: Date = new Date();
  breadcrumbs:any;
  mainRowSpam:any ;
  result:any [] = [];
  
  constructor(
    private employee360Service : Employee360Service,
    private timesheetService : TimesheetService,
    private datePipe: DatePipe,
    private modalService: BsModalService,
    private router: Router,
    private breadcrumbService: BreadcrumbService
  ) {
    
   }
  modalRef: BsModalRef = new BsModalRef();
  isProjectTeamClicked:boolean=false;
  header:String="";
  // allSelected: any;
  empIds:[];
  breadcrumbUrl:any;

  // constructor(
  //   private employee360Service : Employee360Service,
  //   private timesheetService : TimesheetService,
  //   private modalService: BsModalService,
  //   private router: Router ) {}

  ngOnInit(): void {
    console.log("timesheet breadcrumb:"+sessionStorage.getItem('breadcrumb'));
    this.breadcrumbUrl=sessionStorage.getItem('breadcrumb');
    let breadcrumbObject = new Breadcrumb();
    breadcrumbObject.title = "Timesheet";
    breadcrumbObject.url = this.breadcrumbUrl+"/timesheet";
    this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);

    this.activeButton = 'Pending';
    this.currentUser=sessionStorage.getItem('currentUser');
    if (this.currentUser) {
      const currentUserData = JSON.parse(this.currentUser);
      this.managerId = currentUserData.empId;
      console.log(this.managerId); 
    }
    // this.empIdd=sessionStorage.getItem('employeeId');
    this.empIdd=240065;
    this.managerId = 21823;
    this.startDate = null;
    this.endDate = null;
    this.formattedStartDate = null;
    this.formattedEndDate = null;
    this.get360TimesheetDetails(this.activeButton,this.empIdd,this.projectId,this.teamName,this.managerId,this.formattedStartDate,this.formattedEndDate);
  }

  setActiveButton(button: string): void {
    this.activeButton = button;
    
    if(this.activeButton !=='Calendar'){
      this.get360TimesheetDetails(this.activeButton,this.empIdd,this.projectId,this.teamName,this.managerId,this.formattedStartDate,this.formattedEndDate);
    }else{
      this.getTimesheetsForHomePageByEmpId('Last 7 Days');
    }
  }

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
    // 240065
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

  updateSelection(empId:any) {
    console.log(empId);
    this.allSelected = this.data.every(item => item.selected); 
  }

  saveSelected(status:String) {
    console.log("save clicked :"+status);
    console.log("=>allSelected: "+this.allSelected);
    
    const selectedEmpIds = this.data
      .filter(item => item.selected) 
      .map(item => item .empId); 
    console.log('Selected Employee IDs:', this.allSelected);
  }

  findByProject(projectId: number){
    this.isProjectTeamClicked=true;
    this.header="Project Member Details";
    console.log("project clicked =>  " + projectId );
    this.get360TimesheetDetails(this.activeButton,this.empId,projectId,this.teamName,this.managerId,this.formattedStartDate,this.formattedEndDate);
  }

  findByTeam(teamName: any){
    this.isProjectTeamClicked=true;
    this.header="Team Member Details";
    console.log("team clicked => " + teamName);
    this.get360TimesheetDetails(this.activeButton,this.empId,this.projectId,teamName,this.managerId,this.formattedStartDate,this.formattedEndDate);
  }
  

  loading: boolean = false; // Add a loading flag

updateStatus(status: string, empId: number, date: string) {
  if (this.loading) return; // Prevent duplicate calls
  this.loading = true; // Set loading to true

  status = "Pending";

  this.employee360Service.updateStatus(status, empId, date).pipe(first()).subscribe(
    (response: any) => {
      this.loading = false; // Reset loading on response

      if (response.serviceStatus === "Success") {
        this.alertMessage = response.serviceResponse;
        this.showModal = true;

        // Properly manage modal service
        if (this.modalRef) {
          this.modalRef.hide();
          this.modalRef = null;
        }

        this.modalRef = this.modalService.show(this.alertModal, {
          keyboard: false,
          class: 'modal-sm',
        });
      } else {
        this.alertMessage = response.serviceResponse;
        this.showModal = true;
      }
    },
    (error) => {
      this.loading = false; // Reset loading on error
      console.error("Error occurred:", error);
      this.alertMessage = "Error occurred while updating status.";
      this.showModal = true;
    }
  );
}


  // cancelRequest() {
  //   // Hide the modal
  //   this.showModal = false;
  // }

  cancelRequest() {
    this.showModal = false; // Hide the modal
    if (this.modalRef) {
      this.modalRef.hide(); // Properly hide the modal
      this.modalRef = null;
    }
  }
  

  goBack(){
    this.isProjectTeamClicked=false;
    this.get360TimesheetDetails(this.activeButton,this.empIdd,this.projectId,this.teamName,this.managerId,this.formattedStartDate,this.formattedEndDate);
  }

  // let transformedTimeSheet = [];

    get360TimesheetDetails(activeButton:string,empId:number,projectId:number,teamName:string,managerId:number,formattedStartDate:string,formattedEndDate:string) {
      console.log(this.activeButton);
      this.employee360Service.get360TimesheetDetails(activeButton,empId,projectId,teamName,managerId,formattedStartDate,formattedEndDate).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus === "Success") {
              console.log("=> serviceResponse", response.serviceResponse);
              this.data = response.serviceResponse;
              this.data = Object.values(this.data); 
              console.log("=> transformed data", this.data);

              const activityCounts: number[] = [];

              // Iterate over employee data
              // for (const employee of this.data) {
              //     // Calculate the total number of activities for each employee
              //     const totalActivities = employee.timeSheet.reduce(
              //         (count: number, timeSheet: any) => count + timeSheet.activities.length,
              //         0
              //     );
              //     activityCounts.push(totalActivities);
              // }

              console.log("=> Activity counts array", activityCounts);
              
              // this.mainRowSpam= activityCounts;
              this.result = this.transformData(response.serviceResponse);
              console.log("this.result =>", this.result )

          }
      });
  }

  
// console.log(transformedTimeSheet);

 


  // selectedOption: number = 1;  // Default option is "All"
  
  // Variables for date range
  // startDate: Date | null = null;
  // endDate: Date | null = null;
  // activeButton: string = 'Pending';

  onChangeOption(arg: any) {
    // this.dateTimeRange = null;
    if (arg == 1) {
      // Handle "All"
      this.startDate = null;
      this.endDate = null;
      this.setDate();
    } else if (arg == 2) {
      // Handle "Weekly"
      this.startDate = new Date();
      this.endDate = new Date();
      this.startDate.setDate(this.startDate.getDate() - 7);
      this.setDate();
    } else if (arg == 3) {
      // Handle "Monthly"
      this.startDate = new Date();
      this.endDate = new Date();
      this.startDate.setDate(this.startDate.getDate() - 30);
      this.setDate();
    } else if (arg == 4) {
      // Handle "Date range"
      // start and end date will be handled by the owl-datepicker input fields
    }
    console.log("startDate" , this.startDate);
    console.log("endDate" , this.endDate);

  }

  resetDateRange() {
    this.dateTimeRange = null;
    this.startDate = null;
    this.endDate = null;
    // Optionally, call your method to fetch data after reset
    this.onChangeOption(1);
  }

  getDateRange() {
    if (this.dateTimeRange && this.dateTimeRange.length === 2) {
      const fromDate = this.dateTimeRange[0];
      const toDate = this.dateTimeRange[1];
  
      console.log('From Date:', fromDate);
      console.log('To Date:', toDate);
      this.startDate = fromDate;
      this.endDate = toDate;
  
      // Your logic to filter data based on the selected range
      this.setDate();
    }
  }

  setDate(){
    if (this.startDate && this.endDate) {
       this.formattedStartDate = this.datePipe.transform(this.startDate, 'dd-MM-yyyy');
      this.formattedEndDate = this.datePipe.transform(this.endDate, 'dd-MM-yyyy');
      this.get360TimesheetDetails(this.activeButton,this.empId,this.projectId,this.teamName,this.managerId,this.formattedStartDate,this.formattedEndDate);
    }else{
      this.get360TimesheetDetails(this.activeButton,this.empId,this.projectId,this.teamName,this.managerId,this.startDate,this.endDate);

    }
  }
  

  navigateToEmployee360(){
    this.removeActiveTab();
    this.router.navigate(['/employee-360/profile']);
    this.setActiveTab();
  }

  removeActiveTab(){
    const tab = document.getElementById('Employee360Tab').querySelector('.nav-link.active');
    tab?.classList.remove('active');
  }

  setActiveTab(){
    const tab = document.getElementById('Employee360Tab').querySelector('.nav-link');
    tab.classList.add('active');
    let activeRouteLink = tab.getAttribute('routerLink');
  }

transformData(originalData: any ) {
  let transformedData = [];

  Object.values(originalData).forEach((employee :any) => {
    let isFirstActivity = true; // Track the first activity for each employee

      // Calculate the total number of activities for the employee
  const totalActivitiesCount = employee.timeSheet.reduce((total, item) => total + item.activities.length, 0);

    employee.timeSheet.forEach(items => {
      let showProject = true;
      items.activities.forEach(activity => {
        transformedData.push({
          empId: employee.empId,
          name: employee.name,
          date: employee.date,  // Maintained date
          officeInTime: employee.officeInTime,
          officeOutTime: employee.officeOutTime,
          totalWorkingHours: employee.totalWorkingHours,
          nightShift: employee.nightShift,
          status: employee.status,
          createdOn: employee.createdOn,

          activities: activity,
          projectName: items.projectName,
          teamName: items.teamName,
          projectId: items.projectId,
          activityId: items.activityId,
          empActivitiesCountForDay: totalActivitiesCount, // Total activities count for the employee
          projectActivityCount: items.activities.length,
          showProject: showProject,  // Show project only for the first activity in the list
          showEmpId: isFirstActivity, // Show employee ID only for the first activity
        });
        
        // Set isFirstActivity to false after the first activity for the employee
        isFirstActivity = false;
        showProject = false;
      });
    });
  });

  return transformedData;
}

}

