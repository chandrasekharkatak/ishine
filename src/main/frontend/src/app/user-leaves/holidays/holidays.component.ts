import { LocationStrategy } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Employee } from 'src/app/models/employee';
import { Holiday } from 'src/app/models/holiday';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { HolidayService } from 'src/app/services/holiday.service';

@Component({
  standalone: false,
  selector: 'app-holidays',
  templateUrl: './holidays.component.html',
  styleUrls: ['./holidays.component.css']
})
export class HolidaysComponent implements OnInit {

  data:string;
  feature="Holiday";
  currentUser:User;
  userMapping:any = {};

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  holidayList:any[] = [];

  selectedYearholidayList:any[] = [];	
  holidayList1:any[] = [];	
  years:any[]=[];
  currentYear:any;

  filters:any = {};
  isSearchEnabled:boolean = false;
  holidayColumns:any[] = ['blank','occasion','dayOfTheWeek','dateOfHoliday','state'];

  constructor(
    private holidayService : HolidayService,
    private authenticationService : AuthenticationService,
    private locationStrategy: LocationStrategy
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

  ngOnInit(): void {
    this.getAllHolidayByEmpWorkLocation()
    this.preventBackButton();
    this.dynamicYearForDropdown();	
  }
  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }
  dynamicYearForDropdown(){	
    let currentYear = new Date().getFullYear();	
    this.years = [];	
    this.years.push(currentYear);	
    for (var i = 1; i < 2; i++) {	
      this.years.push(currentYear - i);	
    }	
    //console.log(this.years, "dynamic year");	
    	
  }
  getAllHolidayByEmpWorkLocation(){
    this.holidayList = [];
    this.holidayList1 = [];	
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    this.holidayService.getAllHolidayByEmpWorkLocation(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayList = response.serviceResponse;
        this.holidayList.forEach(holiday => {
          holiday.dateOfHoliday = (holiday.dateOfHoliday)? moment(holiday.dateOfHoliday).format(AppComponent.DATE_FORMAT) : null;
        });

        this.holidayList1 = this.holidayList;	

        this.currentYear = new Date().getFullYear();	
        // sessionStorage.setItem('currentyear');
        this.holidayList = this.holidayList.filter((holiday:Holiday)=> moment(holiday.dateOfHoliday, "DD-MM-YYYY").year() == this.currentYear);


        //console.log("this.holidayList : ", this.holidayList);
        //console.log("this.holidayList1 : ", this.holidayList1);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getFilterHolidayList(value:any){	
    this.selectedYearholidayList = [];	
    let searchYear = parseInt(value);	
    //console.log(searchYear,"searchYear")	
    //console.log(this.holidayList,"this.holidayListthis.holidayList")	
    this.holidayList1.forEach(holiday =>{	
      const year = moment(holiday.dateOfHoliday, "DD-MM-YYYY").year();	
      if(searchYear === year){	
        this.selectedYearholidayList.push(holiday);	
      }      	
    });	
    this.holidayList =  this.selectedYearholidayList;	
    //console.log(this.selectedYearholidayList,"this.holidayListholidayList")	
  }

  //pagination 

  page = 1;
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

  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  onSearch(searchData){
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

}


function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}

