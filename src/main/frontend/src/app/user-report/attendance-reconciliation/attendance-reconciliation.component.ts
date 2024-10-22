import { Component, OnInit, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { User } from 'src/app/models/user';
import { AttendanceReconciliationService } from 'src/app/services/attendance-reconciliation.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { DatePipe } from '@angular/common';

class FilterData {
  title: any;
  columns: any;
  queryList: any;
}
@Component({
  selector: 'app-attendance-reconciliation',
  templateUrl: './attendance-reconciliation.component.html',
  styleUrls: ['./attendance-reconciliation.component.css']
})

export class AttendanceReconciliationComponent implements OnInit {

  leaveReportColumns:any;
  filterData: any = new FilterData();
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  isSearchEnabled:boolean=false;
  isLeaveReportTable:boolean=false;
  punchData:any;
  currentUser:User;
  attendanceReconciliationList: any[] = [];
  attendanceReconciliationOriginaldata: any[] = [];
  filteredData: any[] = [];
  viewMoreList: any[] = [];
  queryList: any[] = [];
  storedDataList: any[] = [];
  excelName: any;
  date:string;
  name:string;
  page = 1;
  formattedDate: string;
  maxTodayDate:any;
  AttendancereConciliation: any[] = ['employeeCode', 'employeeName', 'inTime', 'outTime', 'totalDuration','shiftDuration','shiftName','beginTime','endTime','logDate','earlyBy','lateBy','duration','status',];
  filters:any = {};

  constructor(  private modalService: BsModalService, private exportExcelService: ExportExcelService,private attendanceReconciliationService:AttendanceReconciliationService,
    private authenticationService : AuthenticationService,private datePipe: DatePipe
  ) {
    this.maxTodayDate = new Date().toISOString().split('T')[0];
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x)
    // this.formattedDate = this.formatDate(this.date);
   }

   formatDate(dateString: string): string {
    const date = new Date(dateString);
    return this.datePipe.transform(date, 'dd-MMM-yyyy')!;
  }

  ngOnInit(): void {
   this.date = moment().format("YYYY-MM-DD");

   this.formattedDate = this.formatDate(this.date);
  //  this.date=this.formattedDate;
   console.log("ckeck date =======",this.date);
    this.getBioMatricData(this.formattedDate);
  }

onSearch(searchData){
  this.filters = searchData;
}

 //pagination 	
 handlePageChange(event) {
   this.page = event;
 }

 sortData(sort: Sort){	
  //console.log(sort);
  if(sort.active){
    let sortParams:any[] = sort.active?.split("|");
    this.sortColumn = sortParams[0];
    this.sortColumnType = sortParams[1];
    this.sortDirection = sort.direction;      
  }
}

getBioMatricData(date:string){
  const dateformat= this.formatDate(date);
  this.attendanceReconciliationService.getBiomatricData(dateformat).subscribe((response: any) => {
     this.attendanceReconciliationList=response.serviceResponse;
    this.attendanceReconciliationOriginaldata =[... this.attendanceReconciliationList];
     this.modalRef.hide();
  });
}

getviewMoreData(template: TemplateRef<any>,punchrecords:any,name:any){

  this.name=name;
  this.viewMoreList.push(punchrecords);
  const recordsString = this.viewMoreList[0];
  const recordsArray = recordsString.split(',').filter(record => record); 
  this.punchData = [];

  for (let i = 0; i < recordsArray.length; i += 2) {
    if (i + 1 < recordsArray.length) {
      this.punchData.push({
        in: recordsArray[i],
        out: recordsArray[i + 1]
      });
    }
  }

  console.log("Parsed Punch Data: ", this.punchData);
  this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
}

openFilterModal(template: TemplateRef<any>, columns: any[], title: any) {
  this.filterData.title = title;
  this.modalRef = this.modalService.show(template, { class: '' });

}

toggleSearch(){
  this.sortColumn=[];
  this.sortColumnType=[];
  this.sortDirection='';
  this.isSearchEnabled = !this.isSearchEnabled;
  if(!this.isSearchEnabled){
    this.filters = {};
  }
}

cancelRequest() {
  this.modalRef.hide();
}

 exportToExcel(): void {
  this.excelName = 'attendanceReconciliation.xlsx';

  const convertMinutesToHours = (value: number): string => {
      if (value === null || value === undefined) {
          return 'NA'; // Handle null or undefined value
      }
      const hours = Math.floor(value / 60);
      const mins = value % 60;
      const convertedValue = `${hours} hour${hours !== 1 ? 's' : ''} ${mins} min${mins !== 1 ? 's' : ''}`;
      console.log(`Converted value for ${value} minutes: ${convertedValue}`);  // Debugging log
      return convertedValue;
  };

  const onlySpecificDataArr = this.attendanceReconciliationList.map(
    x => ({
      "Employee Id": x.employeeCode,
      "Employee Name": x.employeeName,
      "Log IN": x.inTime,
      "Log Out": x.outTime,
      "Total Working Hours": convertMinutesToHours(x.totalDuration),
      "Shift Duration": convertMinutesToHours(x.shiftDuration),  
      "Shift Name": x.shiftName, 
      "Begin Time": x.beginTime, 
      "endTime": x.endTime,    
      "Log Date": x.logDate,
      "Early By": convertMinutesToHours(x.earlyBy),                
      "Late By": convertMinutesToHours(x.lateBy),                   
      "Status": x.status
    })
  );

  this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName);
}

exportToExcelviewMore(): void {
  this.excelName = 'ViewMoreData.xlsx';
  const onlySpecificDataArr = this.punchData.map(
    x => ({
      "IN": x.in,
      "Employee Name": this.name,
      "OUT": x.out,
    })
  )
  this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)
}
}