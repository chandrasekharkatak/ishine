import { DatePipe } from '@angular/common';
import { AfterViewInit, Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import * as moment from 'moment';
import { Biomax } from 'src/app/models/biomax';
import { Employee360Service } from 'src/app/services/employee360.service';

@Component({
  selector: 'app-employee360-biomax',
  templateUrl: './employee360-biomax.component.html',
  styleUrls: ['./employee360-biomax.component.css']
})

export class Employee360BiomaxComponent implements OnInit{
  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;
  currentDate: Date = new Date(); // Current date
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

  
 //this.filter.employeeId=this.employeeData.employeementId;
}

viewFilterdata(){
   
   
  if(this.filter.viewfilter=="Weekly"){
     this.date.setDate(this.date.getDate() - 7);
    console.log(this.date);
  }
  if(this.filter.viewfilter=="Monthly"){
    this.date.setDate(this.date.getDate() - 30);
    
  }
  if(this.filter.viewfilter=="Yearly"){
    this.date.setDate(this.date.getDate() - 365);
    
  }
  
  this.filter1.officeendTimePicker=this.formatDate(""+this.date);
  this.filter1.officestartTimePicker=this.formatDate(""+new Date());
  this.getBiomatrixFilter();

}
formatDate(dateString: string): string {
  const date = new Date(dateString); // Convert the string to a Date object
  return this.datePipe.transform(date, 'yyyy-MM-dd 00:00:00.000')!;
}
fromDateFilter = (d: Date)=>{
  const DAY_IN_MS = 24 * 60 * 60 * 1000;
  let BACKDATED_LEAVE_PERIOD = 30;
  let FUTUREDATED_LEAVE_PERIOD = 180;
  let minDate = new Date(d.getTime() - (BACKDATED_LEAVE_PERIOD * DAY_IN_MS));
  let maxDate = new Date(d.getTime() + (FUTUREDATED_LEAVE_PERIOD * DAY_IN_MS));

  const dateFormat = 'YYYY-MM-DD';
  const currentDate = new Date();
 return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)));
    
}
searchBioMax(){
  this.filter1.officeendTimePicker=this.formatDate(this.filter.officeendTimePicker);
  this.filter1.officestartTimePicker=this.formatDate(this.filter.officestartTimePicker);
  this.getBiomatrixFilter();
}

getBiomatrixFilter(){
  this.employee360.getEmployeeDetailsForBiomax(this.filter1.officestartTimePicker,this.filter1.officeendTimePicker,this.filter1.employeeId).subscribe((response:any)=>{
    this.biomaxList=response.serviceResponse;
   
  })
}
  

}
