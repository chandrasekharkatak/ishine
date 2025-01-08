import { DatePipe } from '@angular/common';
import { AfterViewInit, Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import * as moment from 'moment';
import { Biomax } from 'src/app/models/biomax';
import { Employee360Service } from 'src/app/services/employee360.service';
import * as Highcharts from 'highcharts';
@Component({
  selector: 'app-employee360-biomax',
  templateUrl: './employee360-biomax.component.html',
  styleUrls: ['./employee360-biomax.component.css']
})

export class Employee360BiomaxComponent implements OnInit{
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  currentDate1: Date = new Date(); // Current date
  biomaxList:Biomax[]=[];
  biomaxList1:Biomax[]=[];
   date = new Date();

filter={
  officestartTimePicker:'',
  officeendTimePicker:'',
  employeeId:'740',
  viewfilter:'Weekly'
}
filter1={
  officestartTimePicker:'',
  officeendTimePicker:'',
  employeeId:'012345',
  viewfilter:'Weekly'
}
chartdata={
  workinghours:0,
  lessthenworkinghours:0,
  hovertime:0
}

employeeData:any;
constructor(private datePipe: DatePipe,
  private employee360:Employee360Service,
  private breadcrumbService: BreadcrumbService){
 
}

ngOnInit(): void {
  this.filter1={
    officestartTimePicker:'',
    officeendTimePicker:'',
    employeeId:'740',
    viewfilter:'Weekly'
  }

  this.filter1.officeendTimePicker=this.filter.officeendTimePicker;
  this.filter1.officestartTimePicker=this.filter.officestartTimePicker;
  const storedData = localStorage.getItem('employee360Data');
  const parsedData = storedData ? JSON.parse(storedData) : null;
  if(parsedData != null || parsedData != undefined ){
    this.employeeData =  parsedData;
  }else{
    this.employeeData = history.state.data;
  }
 
  const breadcrumbObject = { title: `Biomax`, url: "/employee-360/biomax" };
  this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
  this.viewFilterdata();
 
 //this.filter.employeeId=this.employeeData.employeementId;
}

viewFilterdata() {
  // Initialize filter1 object
  this.filter1 = {
    officestartTimePicker: '',
    officeendTimePicker: '',
    employeeId: '740',
    viewfilter: 'Weekly'
  };

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

  // Get the current date for comparison
  const currentDate = new Date();

  // Convert both dates (minDate and maxDate) and d to moment objects for easier comparison
  const formattedDate = moment(d).startOf('day');
  const formattedMinDate = moment(minDate).startOf('day');
  const formattedMaxDate = moment(maxDate).startOf('day');

  // Return whether the date is within the specified range
  return formattedDate.isBetween(formattedMinDate, formattedMaxDate, 'day', '[]');
}
searchBioMax(){
  this.filter1.officeendTimePicker=this.formatDate(this.filter.officeendTimePicker);
  this.filter1.officestartTimePicker=this.formatDate(this.filter.officestartTimePicker);
  this.getBiomatrixFilter();
}

getBiomatrixFilter(){


  let workinghours2=0;
  let lessthenworkinghours=0;
  let hovertime=0;
  this.employee360.getEmployeeDetailsForBiomax(this.filter1.officestartTimePicker,this.filter1.officeendTimePicker,this.filter1.employeeId).subscribe((response:any)=>{
    this.biomaxList=response.serviceResponse;
    this.biomaxList.forEach((filter5)=>{
      let workinghours: number = parseInt(filter5.totalDuration);
     if(workinghours!==0){
      workinghours2=workinghours2+9;
      if(workinghours>9){
        hovertime=hovertime+hovertime;
      } if(workinghours<9){
        lessthenworkinghours=lessthenworkinghours+workinghours;
   
      }
      console.log(workinghours);
      this.chartdata.hovertime=hovertime;
      this.chartdata.workinghours=workinghours2;
      this.chartdata.lessthenworkinghours=lessthenworkinghours;
     }
    
    })
  })
  console.log(this.chartdata);
}
  

}
