import { DatePipe } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as Highcharts from 'highcharts';
import * as moment from 'moment';
import { Biomax, biomaxFilter } from 'src/app/models/biomax';
import { Breadcrumb } from 'src/app/models/breadcrumd';
import { Team } from 'src/app/models/team';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { Employee360Service } from 'src/app/services/employee360.service';

import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

@Component({
  selector: 'app-employee360-biomax',
  templateUrl: './employee360-biomax.component.html',
  styleUrls: ['./employee360-biomax.component.css']
})

export class Employee360BiomaxComponent implements OnInit{
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  employeementId:any[]=[]
  biomaxFilter=new biomaxFilter();
  currentDate1: Date = new Date(); // Current date
  biomaxList:Biomax[]=[];
  biomaxList1:Biomax[]=[];
  currentBreadcrumbList: any[] = [];
  team=new Team();
  employeeIdList:any[]=[];
  filters: any = {};
   employeeIdString = '';
  teamList:Team[]=[];
  
  defaultview:boolean=true;
  chartdata={
    workinghours:0,
    lessthenworkinghours:0,
    hovertime:0
  }
  biomaxListColumns: any[] = ['blank', 'logDate', 'beginTime', 'endTime', 'shiftName'];
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  isSearchEnabled:boolean =false;
  chartOptions: Highcharts.Options = {
    chart: {
      type: 'pie'
    },
    title: {
      text: 'Work Hours Distribution'
    }, credits: {
      enabled: false
    },
    series: [
      {
        type: 'pie',
        name: 'Work Hours',
        data: []  
      }
    ]
  };
   date = new Date();

filter={
  officestartTimePicker:'',
  officeendTimePicker:'',
  employeeId:'',
  viewfilter:'Weekly'
}
filter1={
  officestartTimePicker:'',
  officeendTimePicker:'',
  employeeId:'',
  viewfilter:'Weekly'
}


employeeData:any;
constructor(private datePipe: DatePipe,
  private employee360:Employee360Service,
  private breadcrumbService: BreadcrumbService){
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
 
}

ngOnInit(): void {

  this.filter1.officeendTimePicker=this.filter.officeendTimePicker;
  this.filter1.officestartTimePicker=this.filter.officestartTimePicker;
  const storedData = localStorage.getItem('employee360Data');
  const parsedData = storedData ? JSON.parse(storedData) : null;
  if(parsedData != null || parsedData != undefined ){
    this.employeeData =  parsedData;
  }else{
    this.employeeData = history.state.data;
  }
  let empCOde=this.employeeData.employeementId;

  let resultString = empCOde.replace("-", "");
  this.biomaxFilter.empId.push(resultString);

let findbreadcrumbObject = this.currentBreadcrumbList.findIndex(x => x.title == "Biomax");
            if (findbreadcrumbObject >= 0) {
              this.currentBreadcrumbList.splice(findbreadcrumbObject + 1);
              this.breadcrumbService.setBreadcrumbSubject(this.currentBreadcrumbList);
            } else {
              let breadcrumbObject = new Breadcrumb();
              breadcrumbObject.title = "Biomax";
              breadcrumbObject.url = "/employee-360/biomax";
              this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
            }

  this.viewFilterdata();
 
}

onSearch(searchData) {
  this.filters = searchData;
}
toggleSearch() {
  this.sortColumn = [];
  this.sortColumnType = [];
  this.sortDirection = '';
  this.isSearchEnabled = !this.isSearchEnabled;
  if (!this.isSearchEnabled) {
    this.filters = {};
  }
}


 sortData(sort: Sort) {

    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }



   viewFilterdata() {
  // Initialize filter1 object
 

  // Create a Date object for manipulation
  var  currentDate = new Date();

  // Adjust the date based on the selected viewfilter
  if (this.filter.viewfilter === "Weekly") {
    currentDate.setDate(currentDate.getDate() - 7);  // Subtract 7 days for Weekly
   
  } 
  else if (this.filter.viewfilter === "Monthly") {
    currentDate.setMonth(currentDate.getMonth() - 1); // Subtract 1 month for Monthly
  } 
  else if (this.filter.viewfilter === "Yearly") {
    currentDate.setFullYear(currentDate.getFullYear() - 1); // Subtract 1 year for Yearly
  }

  this.filter1.officestartTimePicker = this.formatDate(""+currentDate);
  this.filter1.officeendTimePicker = this.formatDate(""+new Date());
  this.biomaxFilter.startDate=this.filter1.officestartTimePicker;
  this.biomaxFilter.endDate=this.filter1.officeendTimePicker;
  console.log(this.biomaxFilter);
  this.getBiomatrixFilter();

}

formatDate(dateString: string): string {
  const date = new Date(dateString); // Convert the string to a Date object
  return this.datePipe.transform(date, 'yyyy-MM-dd 00:00:00.000')!;
}
 fromDateFilter = (d: Date): boolean => {
  const DAY_IN_MS = 24 * 60 * 60 * 1000;
  let BACKDATED_LEAVE_PERIOD = 30;
  let FUTUREDATED_LEAVE_PERIOD = 180;

  // Calculate minDate and maxDate
  let minDate = new Date(d.getTime() - (BACKDATED_LEAVE_PERIOD * DAY_IN_MS));
  let maxDate = new Date(d.getTime() + (FUTUREDATED_LEAVE_PERIOD * DAY_IN_MS));
  const currentDate = new Date();
  const formattedDate = moment(d).startOf('day');
  const formattedMinDate = moment(minDate).startOf('day');
  const formattedMaxDate = moment(maxDate).startOf('day');
  return formattedDate.isBetween(formattedMinDate, formattedMaxDate, 'day', '[]');
}
searchBioMax(){
  this.biomaxFilter.startDate=this.formatDate(this.filter.officestartTimePicker);
  this.biomaxFilter.endDate=this.formatDate(this.filter.officeendTimePicker);
  this.getBiomatrixFilter();
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
async getBiomatrixFilter() {
  let workinghours2 = 0;
  let lessthenworkinghours = 0;
  let hovertime = 0;

  try {
    // Await the result of the method assuming it returns a Promise
    const response: any = await this.employee360.getEmployeeDetailsForBiomax(this.biomaxFilter).toPromise(); // Convert Observable to Promise if needed

    this.biomaxList = response.serviceResponse;
   
    // Process each item in the biomaxList
    this.biomaxList.forEach((filter5) => {
      let workinghours: number = parseInt(filter5.totalDuration);
      if (workinghours !== 0) {
        workinghours2 += 9; // Baseline working hours
        if (workinghours > 9) {
          hovertime += (workinghours - 9); // Overtime calculation
        }
        if (workinghours < 9) {
          lessthenworkinghours += workinghours; // Less than working hours calculation
        }
      }
    });

    // After processing, update chart data if biomaxList has items
    if (this.biomaxList.length > 0) {
      this.chartdata.hovertime = hovertime;
      this.chartdata.workinghours = workinghours2;
      this.chartdata.lessthenworkinghours = lessthenworkinghours;
      this.updateChartData(this.chartdata);
    }
  } catch (error) {
    console.error("Error fetching employee details:", error);
  }
}


private updateChartData(data: any): void {
  this.chartOptions = {
    chart: {
      type: 'pie'
    },
    title: {
      text: 'Work Hours Distribution'
    },
    credits: {
      enabled: false
    },
    colors: ['#FF5733', '#33FF57', '#3357FF'],
    series: [
      {
        type: 'pie',
        name: 'Work Hours',
        data: [
          { name: 'Working Hours', y: data.workinghours },
          { name: 'Less Than Working Hours', y: data.lessthenworkinghours },
          { name: 'Overtime', y: data.hovertime }
        ]
      }
    ]
  };

  // Update chart with new options
  Highcharts.chart('biomaxfiterContainer', this.chartOptions);
}  
  
ngAfterViewInit(): void {
  // Initialize the chart if not already initialized
  if (this.chartdata) {
    Highcharts.chart('biomaxfiterContainer', this.chartOptions);
  }
}
teamData(id:any){
  
  let breadcrumbObject = new Breadcrumb();
             
  breadcrumbObject.title = "Team";
              breadcrumbObject.url = "/employee-360/biomax";
              this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
  let findbreadcrumbObject = this.currentBreadcrumbList.findIndex(x => x.title == "Team");
            if (findbreadcrumbObject >= 0) {
              this.currentBreadcrumbList.splice(findbreadcrumbObject + 1);
              this.breadcrumbService.setBreadcrumbSubject(this.currentBreadcrumbList);
            } else {
              breadcrumbObject.title = "Team";
              breadcrumbObject.url = "/employee-360/biomax";
              this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
            }
  this.defaultview=false;
 this.team.teamId=id;
 alert(this.team.teamId);
this.getTeamFromEmployeeMapping();
//this.getBiomatrixFilter();

  
}
goBack(){
  this.defaultview=true;
//this.getBiomatrixFilter();
}
async getTeamFromEmployeeMapping(){
  this.biomaxFilter.empId=[];
  this.employee360.getTeamTImeSheet(this.team).subscribe((response: any) => {
    //this.teamList = response.serviceResponse;
    let employeeIdListTeam;
    response.serviceResponse.forEach((x)=>{
    
      this.biomaxFilter.empId.push(x.employeementId);
    })
    console.log(this.biomaxFilter);
    this.getBiomatrixFilter();
  });
 
}
}
