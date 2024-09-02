import { Component, OnInit, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { User } from 'src/app/models/user';
import { AttendanceReconciliationService } from 'src/app/services/attendance-reconciliation.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';




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

  
  maxTodayDate:any;

  AttendancereConciliation: any[] = ['empId', 'empName', 'emploginTime', 'emplogoutTime', 'empTotalWorkingHours','empDeviation'];

  filters:any = {};

  constructor(  private modalService: BsModalService, private exportExcelService: ExportExcelService,private attendanceReconciliationService:AttendanceReconciliationService,
    private authenticationService : AuthenticationService
  ) {
    this.maxTodayDate = new Date().toISOString().split('T')[0];
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x)
  
   }

  ngOnInit(): void {
   this.date = moment().format("YYYY-MM-DD");
   console.log("ckeck date =======",this.date)
    this.getBioMatricData();
  }


  searchQuery:string='';
  filterDataforsearch(): void {
    const query = this.searchQuery;
    if(query){
      this.filteredData = this.attendanceReconciliationList.filter(attendance =>
        (attendance[0] && attendance[0].toString().toLowerCase().includes(query)) ||
        (attendance[1] && attendance[1].toString().toLowerCase().includes(query)) ||
        (attendance[2] && attendance[2].toString().toLowerCase().includes(query)) ||
        (attendance[3] && attendance[3].toString().toLowerCase().includes(query)) ||
        (attendance[4] && attendance[4].toString().toLowerCase().includes(query)) ||
        (attendance[5] && attendance[5].toString().toLowerCase().includes(query))
      );
      this.attendanceReconciliationList = [...this.filteredData];
    } else{
      this.attendanceReconciliationList =[...this.attendanceReconciliationOriginaldata];
    }
    
  }


onSearch(searchData){
  this.filters = searchData;

  this.filteredData = this.attendanceReconciliationList.filter(attendance =>
    attendance.employeeId.toLowerCase().includes(searchData) ||
    attendance.fullName.toLowerCase().includes(searchData) ||
    attendance.logIn.toLowerCase().includes(searchData) ||
    attendance.logOut.toLowerCase().includes(searchData) ||
    attendance.totalWorkingHours.toLowerCase().includes(searchData)
  );

  this.attendanceReconciliationList = [...this.filteredData];

  this.page = 1;
  // this.getBioMatricData();
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




getBioMatricData(){

  console.log("selected date ",this.date);

  this.attendanceReconciliationService.getBiomatricData(this.date).subscribe((response: any) => {
     this.attendanceReconciliationList=response.serviceResponse;
    this.attendanceReconciliationOriginaldata =[... this.attendanceReconciliationList];
     this.modalRef.hide();

  });
}




getviewMoreData(template: TemplateRef<any>, id:any ,name:string,logoutime:string){


  console.log("id--",id);
  console.log("name-",name);
  console.log("logoutime--",logoutime);



  this.name=name;
  // this.logout=logoutime;
  this.attendanceReconciliationService.getviewMoreData(id,this.date).subscribe((response: any) => {
     this.viewMoreList=response.serviceResponse;
     console.log("data--- ",this.viewMoreList);

     this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  });


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




 // Excel Export 
 exportToExcel(): void {

    this.excelName = 'attendanceReconciliation.xlsx';
    const onlySpecificDataArr = this.attendanceReconciliationList.map(
      x => ({
        "Employee Id": x.empId,
        "Employee Name": x.empName,
        "Log IN": x.emploginTime,
        "Log Out": x.emplogoutTime,
        "Total Working Hours": x.empTotalWorkingHours,
        "Total Deviation Time": x.empDeviation
       
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)


}



// Excel Export 
exportToExcelviewMore(): void {

  this.excelName = 'ViewMoreData.xlsx';
  const onlySpecificDataArr = this.attendanceReconciliationList.map(
    x => ({
      "Emp Id": x.empId,
      "Employee Name": this.name,
      "Log IN": x.emploginTime,
      "Device Name": x.empDeviceName, 
    })
  )
  this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.excelName)


}




}
