import { ScrollStrategy } from '@angular/cdk/overlay';
import { DatePipe } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { Router } from '@angular/router';
import { saveAs } from 'file-saver';
import * as moment from 'moment';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { CalendarComponent } from 'src/app/helpers/calendar/calendar.component';
import { Breadcrumb } from 'src/app/models/breadcrumd';
import { Employee } from 'src/app/models/employee';
import { Timesheet } from 'src/app/models/timesheet';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { EncryptionService } from 'src/app/services/EncryptionService';
import { TimesheetService } from 'src/app/services/timesheet.service';
import { UtilityService } from 'src/app/services/utility.service';
import { SortPipe } from 'src/app/sort.pipe';
import * as XLSX from 'xlsx';

@Component({
  standalone: false,
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
  projectName: any;
  teamName: any = 0;
  empId: any = 0;
  empIdd: any = 0;
  projectId: any = 0;
  currentUser: any;
  time;
  managerId: any = 0;
  endDate: any;
  startDate: any;
  scrollStrategy: ScrollStrategy;
  data: Timesheet[] = [];
  formattedStartDate: any = "null";
  formattedEndDate: any = "null";
  allSelected: any;
  dateTimeRange: any = null;
  todayDate: Date = new Date();
  breadcrumbs: any;
  mainRowSpam: any;
  result: any[] = [];
  timesheetIds: any[] = [];
  responseCount: any = 0;

  filters: any = {};
  isSearchEnabled: boolean = false;


  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  //Bulk approve-reject
  bulkList: any = [];
  isSelectAll: boolean = false;
  isSelect: boolean = false;
  allTeamTimesheetRequests: Timesheet[] = [];
  timesheetID:any = [];
  Employee360=new Employee();
  projectClicked:boolean=false;
  teamClicked:boolean=false;
  actionButton:boolean=false;

  timesheetColumns:any[]=['blank','employmentId','name','date','dayType','projectName','teamName','completionTime','activity','officeInTime','officeOutTime','totalTime','nightShift','status','createdOn'];
  
  
  constructor(
    private employeeService: EmployeeService,
    private utilityService: UtilityService,
    private employee360Service : Employee360Service,
    private timesheetService : TimesheetService,
    private datePipe: DatePipe,
    private modalService: NgbModal,
    private router: Router,
    private breadcrumbService: BreadcrumbService,
    private encryptionService: EncryptionService,
  ) {
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
  }
  modalRef:NgbModalRef;
  isProjectTeamClicked: boolean = false;
  header: String = "";
  currentBreadcrumbList: any[] = [];

  ngOnInit(): void {
    let findbreadcrumbObject = this.currentBreadcrumbList.findIndex(x => x.title == "Timesheet");
    if (findbreadcrumbObject >= 0) {
      this.currentBreadcrumbList.splice(findbreadcrumbObject + 1);
      this.breadcrumbService.setBreadcrumbSubject(this.currentBreadcrumbList);
    } else {
      let breadcrumbObject = new Breadcrumb();
      breadcrumbObject.title = "Timesheet";
      breadcrumbObject.url = "/employee-360/timesheet";
      this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    }

    this.activeButton = 'Pending';

    const encryptedUser = sessionStorage.getItem('currentUser');

if (encryptedUser) {
  const decryptedString = this.encryptionService.decrypt(encryptedUser);
  if (decryptedString) {
    try {
      this.currentUser = JSON.parse(decryptedString);
    } catch (error) {
      console.error('Failed to parse decrypted session user:', decryptedString, error);
      this.currentUser = null;
    }
  } else {
    console.warn('Decryption returned empty string.');
    this.currentUser = null;
  }
} else {
  console.warn('No currentUser found in sessionStorage');
  this.currentUser = null;
} 
    // this.currentUser = sessionStorage.getItem('currentUser');
    if (this.currentUser) {
      const currentUserData = JSON.parse(this.currentUser);
      this.managerId = currentUserData.empId;
      console.log(this.managerId);
    }
    // this.empId=sessionStorage.getItem('empId');
    let encryptedEmployeeData = localStorage.getItem('employee360Data');
    let employeeData = null;
    if (encryptedEmployeeData) {
  const decryptedString = this.encryptionService.decrypt(encryptedEmployeeData);
  if (decryptedString) {
    try {
      employeeData = JSON.parse(decryptedString);
    } catch (error) {
      console.error('Failed to parse decrypted session user:', decryptedString, error);
      employeeData = null;
    }
  } else {
    console.warn('Decryption returned empty string.');
    employeeData = null;
  }
} else {
  console.warn('No currentUser found in sessionStorage');
  employeeData = null;
} 
    this.Employee360 = employeeData;
    let employeeObject = employeeData;
    let empId = employeeObject.empId;

    this.startDate = null;
    this.endDate = null;
    this.formattedStartDate = null;
    this.formattedEndDate = null;
    this.get360TimesheetDetails(this.activeButton, empId, this.projectId, this.teamName, this.formattedStartDate, this.formattedEndDate);
    // this.getAllEmployeeFor360View();
    console.log(this.Employee360);
  }

  setActiveButton(button: string): void {
    this.activeButton = button;
    // this.empIdd=240065;
    if (button !== 'Pending') {
      this.allSelected = false;
    }
    if(this.activeButton !=='Calendar'){
      this.empId=sessionStorage.getItem('empId');
      this.get360TimesheetDetails(this.activeButton,this.empId,this.projectId,this.teamName,this.formattedStartDate,this.formattedEndDate);
    }else{
      this.empId=sessionStorage.getItem('empId');
      this.getTimesheetsForHomePageByEmpId('Last 7 Days');
    }
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
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Time Sheet');

  const excelBuffer: any = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
  const data: Blob = new Blob([excelBuffer], { type: 'application/octet-stream' });

  saveAs(data, exportToExcelTeamfile);
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
    timesheetObj.empId = sessionStorage.getItem('empId');
    // timesheetObj.empId = 21899;
    // 240065
    console.log(sessionStorage.getItem('employeeId'));
    console.log("timesheetObj.empId => ", timesheetObj.empId);


    if (dateRange == 'Last 7 Days') {
      totaltimesheetDaysCount = 7;
      const DAY_IN_MS = 24 * 60 * 60 * 1000;
      fromDate = new Date(currentDate.getTime() - (1 * DAY_IN_MS));
      toDate = new Date(currentDate.getTime() - (7 * DAY_IN_MS));
      timesheetObj.startDate = moment(toDate).format(dateFormat);
      timesheetObj.endDate = moment(fromDate).format(dateFormat);
    } else if (dateRange == 'This Month') {
      totaltimesheetDaysCount = moment(`${currentDate.getFullYear()}-${currentDate.getMonth()}`, "YYYY-MM").daysInMonth()
      fromDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1);
      toDate = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0);
      timesheetObj.startDate = moment(fromDate).format(dateFormat);
      timesheetObj.endDate = moment(toDate).format(dateFormat);
    } else if (dateRange == 'Last Month') {
      totaltimesheetDaysCount = moment(`${currentDate.getFullYear()}-${currentDate.getMonth() - 1}`, "YYYY-MM").daysInMonth()

      fromDate = new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1);
      toDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), 0);
      timesheetObj.startDate = moment(fromDate).format(dateFormat);
      timesheetObj.endDate = moment(toDate).format(dateFormat);
    }

    this.timesheetService.getTimesheetsForHomePageByEmpId(timesheetObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        filledTimesheetDetails = response.serviceResponse;
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
            if (newTimesheetObj.status == "Pending") { pendingCount++; }
            else if (newTimesheetObj.status == "Approved") { approvedCount++; }
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

  bulkClicked(status: string) {
    console.log("status+++" + status);
    this.updateStatus(status);
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  resetSearch() {
    this.isSearchEnabled = false;
    this.filters = {};
  }

  onSearch(searchData) {
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

  sortData(sort: any) {
    //console.log(sort);
    const sortDirection = sort.direction.desc; // 'asc' or 'desc'

    // if(sort.active){
    //   let sortParams:any[] = sort.active?.split("|");
    //   this.sortColumn = sortParams[0];
    //   this.sortColumnType = sortParams[1];
    //   this.sortDirection = sort.direction;
    // }
    if (sortDirection === 'desc') {
      this.result.sort((a, b) => {
        return b.date - a.date; // Adjust to your column logic
      });
    } else if (sortDirection === 'asc') {
      this.result.sort((a, b) => {
        return a.date - b.date; // Adjust to your column logic
      });
    }
  }

  update(status: string, empId: number, date: string) {
    for (const emp of this.result) {
      if (emp.empId == empId && emp.date == date) {
        this.timesheetIds.push(emp.timesheetId);
      }
    }
    this.updateStatus(status);
  }

  toggleRowSelection(empId: number, date: string, selected: boolean) {
    // this.bulkList.push(empId);

    for (const emp of this.result) {
      if (emp.empId === empId && emp.date === date) {
        emp.selected = selected;

        if (selected) {
          this.timesheetIds.push(emp.timesheetId);
          if (!this.bulkList.includes(emp.date)) {
            this.bulkList.push(emp.date);
          }
        } else {
          this.timesheetIds = this.timesheetIds.filter(id => id !== emp.timesheetId);
          this.bulkList = this.bulkList.filter(d => d !== emp.date);
        }
      }
    }

    console.log("this.bulkList", this.bulkList);

    console.log("this.result", this.result);
    console.log("timesheetID", this.timesheetIds);

    this.allSelected = false;
    const allSelect = this.result.every(emp => emp.selected === true);
    if (allSelect) {
      this.allSelected = true;
    }

    const allDeselect = this.result.every(emp => emp.selected === false);
    if (allDeselect) {
      this.allSelected = false;
    }
  }


  toggleSelectAll() {
    if (this.allSelected === true) {
      for (const emp of this.result) {
        if (emp.selected === false) {
          emp.selected = true;
          if (!this.bulkList.includes(emp.date)) { this.bulkList.push(emp.date) }
          this.timesheetIds.push(emp.timesheetId);
        }
      }
    } else {
      this.result.forEach(emp => {
        emp.selected = false;
      });
      this.timesheetIds = [];
      this.bulkList = [];
    }

  }

  findByProject(projectId: number, projectName: string) {
    alert(projectId);
    this.isProjectTeamClicked = true;
    this.projectClicked = true;
    this.allSelected = false;
    this.header = projectName + " : Member Details";

    this.projectId = projectId;
    this.get360TimesheetDetails(this.activeButton, this.empIdd, projectId, this.teamName, this.formattedStartDate, this.formattedEndDate);
  }

  findByTeam(teamName: any, teamId: any) {
    alert(teamName);
    this.isProjectTeamClicked = true;
    this.teamClicked = true;
    this.allSelected = false;
    this.teamName = teamName;
    this.header = teamName + " : Member Details";
    console.log("team clicked => " + teamName);
    this.get360TimesheetDetails(this.activeButton, this.empIdd, this.projectId, teamId, this.formattedStartDate, this.formattedEndDate);
  }


  loading: boolean = false;

  updateStatus(status: string) {
    if (this.loading) return;
    this.loading = true;
    console.log("this.timesheet", this.timesheetIds);
    console.log("allSelected+++++++" + this.allSelected);

    this.employee360Service.updateStatus(status, this.timesheetIds, this.managerId).pipe(first()).subscribe(
      (response: any) => {
        this.loading = false;

        if (response.serviceStatus === "Success") {
          this.alertMessage = response.serviceResponse;
          this.showModal = true;
          if (this.modalRef) {
            this.modalRef.close();
            this.modalRef = null;
          }

          this.modalRef = this.modalService.open(this.alertModal, {
            keyboard: false,
            modalDialogClass : 'modal-sm',
          });
        } else {
          this.alertMessage = response.serviceResponse;
          this.showModal = true;
        }
      },
      (error) => {
        this.loading = false;
        console.error("Error occurred:", error);
        this.alertMessage = "Error occurred while updating status.";
        this.showModal = true;
      }
    );
    this.get360TimesheetDetails(this.activeButton, this.empId, this.projectId, this.teamName, this.formattedStartDate, this.formattedEndDate);


  }

  cancelRequest() {
    this.showModal = false;
    if (this.modalRef) {
      this.modalRef.close();
      this.modalRef = null;
    }
  }

  goBack() {
    this.isProjectTeamClicked = false;
    this.projectClicked = false;
    this.teamClicked = false;
    this.projectId = 0;
    this.teamName = "null";
    this.allSelected = false;

let encryptedEmployeeData = localStorage.getItem('employee360Data');
    let employeeData = null;
    if (encryptedEmployeeData) {
  const decryptedString = this.encryptionService.decrypt(encryptedEmployeeData);
  if (decryptedString) {
    try {
      employeeData = JSON.parse(decryptedString);
    } catch (error) {
      console.error('Failed to parse decrypted session user:', decryptedString, error);
      employeeData = null;
    }
  } else {
    console.warn('Decryption returned empty string.');
    employeeData = null;
  }
} else {
  console.warn('No currentUser found in sessionStorage');
  employeeData = null;
} 


    let employeeObject = employeeData;
    let emp_Id = employeeObject.empId;
    this.get360TimesheetDetails(this.activeButton, emp_Id, this.projectId, this.teamName, this.formattedStartDate, this.formattedEndDate);
  }

  onChangeOption(arg: any) {

    if (arg == 1) {
      // Handle "All"
      this.startDate = null;
      this.endDate = null;
      this.resetDateRange();
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

      // this.startDate = new Date(this.endDate.getFullYear(), this.endDate.getMonth(), 1);

      this.startDate.setDate(this.startDate.getMonth());
      this.setDate();
    } else if (arg == 4) {
      // Handle "Date range"
      // start and end date will be handled by the owl-datepicker input fields
    }
    console.log("startDate", this.startDate);
    console.log("endDate", this.endDate);

  }

  resetDateRange() {
    this.dateTimeRange = null;
    this.startDate = null;
    this.endDate = null;
    this.setDate();
  }

  getDateRange() {
    if (this.dateTimeRange && this.dateTimeRange.length === 2) {
      const fromDate = this.dateTimeRange[0];
      const toDate = this.dateTimeRange[1];

      console.log('From Date:', fromDate);
      console.log('To Date:', toDate);
      this.startDate = fromDate;
      this.endDate = toDate;

      // logic to filter data based on the selected range
      this.setDate();
    }
  }

  setDate() {
    if (this.startDate && this.endDate) {
      this.formattedStartDate = this.datePipe.transform(this.startDate, 'dd-MM-yyyy');
      this.formattedEndDate = this.datePipe.transform(this.endDate, 'dd-MM-yyyy');
      if (this.isProjectTeamClicked) {
        this.get360TimesheetDetails(this.activeButton, this.empIdd, this.projectId, this.teamName, this.formattedStartDate, this.formattedEndDate);
      }
      else { this.get360TimesheetDetails(this.activeButton, this.empId, this.projectId, this.teamName, this.formattedStartDate, this.formattedEndDate); }
    } else {
      this.get360TimesheetDetails(this.activeButton, this.empId, this.projectId, this.teamName, this.startDate, this.endDate);

    }
  }


  navigateToEmployee360() {
    this.removeActiveTab();
    this.router.navigate(['/employee-360/profile']);
    this.setActiveTab();
  }

  removeActiveTab() {
    const tab = document.getElementById('Employee360Tab').querySelector('.nav-link.active');
    tab?.classList.remove('active');
  }

  setActiveTab() {
    const tab = document.getElementById('Employee360Tab').querySelector('.nav-link');
    tab.classList.add('active');
    let activeRouteLink = tab.getAttribute('routerLink');
  }

  transformData(originalData: any) {
    let transformedData = [];

    Object.values(originalData).forEach((employee: any) => {
      let isFirstActivity = true;
      // this.managerId=21865;
      if (this.managerId == employee.managerId) {
        this.actionButton = true;
      } else { this.actionButton = false; }
      const totalActivitiesCount = employee.timeSheetlist.length;
      employee.timeSheetlist.forEach(items => {
        let showProject = true;
        transformedData.push({
          empId: employee.empId,
          employmentId: "A-" + employee.employmentId,
          name: employee.name,
          date: employee.date,
          officeInTime: employee.officeInTime,
          officeOutTime: employee.officeOutTime,
          totalTime: employee.totalTime,
          nightShift: employee.nightShift,
          status: employee.status,
          createdOn: employee.createdOn,
          dayType: employee.dayType,
          completionTime: items.completionTime,
          activity: items.activity,
          projectName: items.projectName,
          teamName: items.teamName,
          teamId: items.teamId,
          projectId: items.projectId,
          activityId: items.activityId,
          timesheetId: items.timesheetId,
          empActivitiesCountForDay: totalActivitiesCount,
          showProject: showProject,
          showEmpId: isFirstActivity,
          selected: false,
        });
        isFirstActivity = false;
        showProject = false;
      });
    });

    return transformedData;
  }

//   get360TimesheetDetails(activeButton:string,empId:number,projectId:number,teamName:string,managerId:number,formattedStartDate:string,formattedEndDate:string) {
//     console.log(this.activeButton);
//     this.employee360Service.get360TimesheetDetails(activeButton,empId,projectId,teamName,managerId,formattedStartDate,formattedEndDate).pipe(first()).subscribe((response: any) => {
//         if (response.serviceStatus === "Success") {
//             console.log("=> serviceResponse", response.serviceResponse);
//             this.data = response.serviceResponse;
//             this.data = Object.values(this.data);
//             this.responseCount=this.data.length; 
//             this.result = this.transformData(this.data);
//             this.result.forEach((employee) => {
//               let matchingEmployee = this.employeesFor360.find(emp => emp.empId == employee.empId);
//               console.log("matchingEmployee ", matchingEmployee);
//               employee.emp360 = matchingEmployee ? matchingEmployee : {};
//           });
//             console.log("this.result =>", this.result )
//         }
//     });

//     this.currentUser=sessionStorage.getItem('currentUser');
//     if (this.currentUser) {
//       const currentUserData = JSON.parse(this.currentUser);
//       this.managerId = currentUserData.empId;
//       console.log(this.managerId); 
//     }
// }

//   getAllEmployeeFor360View(): void {
//         this.employeesFor360 = [];
//         this.employeeService.getAllEmployeesFor360View().subscribe({
//             next: (response: any) => {
//                 if (response.serviceStatus == "Success") {
//                     this.employeesFor360 = response.serviceResponse;
    
//                     this.employeesFor360.forEach(employeeObj => {
//                         employeeObj.employeementId = this.utilityService.appendEmployeementid(employeeObj.isConsultant, employeeObj.employeementId);
//                         employeeObj.dateOfJoining = employeeObj.dateOfJoining ? moment(employeeObj.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
//                         employeeObj.dateOfRelieving = employeeObj.dateOfRelieving ? moment(employeeObj.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
//                         employeeObj.updatedOn = employeeObj.updatedOn ? moment(employeeObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
//                         employeeObj.createdOn = employeeObj.createdOn ? moment(employeeObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
    
//                         if (employeeObj.isConsultant == 'true')
//                             employeeObj.employeeType = 'Consultant';
//                         else if (employeeObj.isApprenticeship == 'true')
//                             employeeObj.employeeType = 'Apprentice';
//                         else
//                             employeeObj.employeeType = 'Regular';
//                     });
    
//                     this.employeesFor360 = new SortPipe().transform(this.employeesFor360, ['name', 'string', 'asc']);
//                 } else {
//                     alert(response.serviceResponse);
//                 }
//             },
//             error: (error) => {
//                 console.error("Error fetching employees:", error);
//             }
//         });
//     }

async get360TimesheetDetails(
  activeButton: string, 
  empId: number, 
  projectId: number, 
  teamName: string, 
  // managerId: number, 
  formattedStartDate: string, 
  formattedEndDate: string
): Promise<void> {
  console.log(this.activeButton);

  try {

   
    const response: any = await this.employee360Service.get360TimesheetDetails(
      activeButton, 
      empId, 
      projectId, 
      teamName, 
      // managerId, 
      formattedStartDate, 
      formattedEndDate
    ).toPromise();  

    if (response.serviceStatus === "Success") {
      console.log("=> serviceResponse", response.serviceResponse);
      this.data = response.serviceResponse;
      this.data = Object.values(this.data);
      this.responseCount = this.data.length;
      this.result = this.transformData(this.data);
      this.result.forEach((employee) => {
        employee.emp360 = employee.empId;
      });
      this.result.sort((a, b) => {
          
        if (a.date > b.date) {
          return -1; 
        } else if (a.date < b.date) {
          return 1; 
        }
        return 0;
      });

      console.log("this.result =>", this.result);
    }

    const encryptedUser = sessionStorage.getItem('currentUser');

if (encryptedUser) {
  const decryptedString = this.encryptionService.decrypt(encryptedUser);
  if (decryptedString) {
    try {
      this.currentUser = JSON.parse(decryptedString);
    } catch (error) {
      console.error('Failed to parse decrypted session user:', decryptedString, error);
      this.currentUser = null;
    }
  } else {
    console.warn('Decryption returned empty string.');
    this.currentUser = null;
  }
} else {
  console.warn('No currentUser found in sessionStorage');
  this.currentUser = null;
} 


    // this.currentUser = sessionStorage.getItem('currentUser');
    if (this.currentUser) {
      const currentUserData = JSON.parse(this.currentUser);
      this.managerId = currentUserData.empId;
      console.log(this.managerId);
    }

  } catch (error) {
    console.error("Error fetching data:", error);
  }
}

    clearBreadcrumbs(){
      window.location.reload()
    }

    
}

