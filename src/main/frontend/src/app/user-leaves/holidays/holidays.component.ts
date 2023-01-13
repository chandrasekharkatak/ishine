import { LocationStrategy } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { first } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { Holiday } from 'src/app/models/holiday';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { HolidayService } from 'src/app/services/holiday.service';

@Component({
  selector: 'app-holidays',
  templateUrl: './holidays.component.html',
  styleUrls: ['./holidays.component.css']
})
export class HolidaysComponent implements OnInit {

    data:string;
  feature="Holiday";
  currentUser:User;
  userMapping:any = {};

  holidayList:any[] = [];

  selectedYearholidayList:any[] = [];	
  holidayList1:any[] = [];	
  years:any[]=[];
  currentYear:any;

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
    console.log(this.years, "dynamic year");	
    	
  }
  getAllHolidayByEmpWorkLocation(){
    this.holidayList = [];
    this.holidayList1 = [];	
    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    this.holidayService.getAllHolidayByEmpWorkLocation(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayList = response.serviceResponse;
        this.holidayList1 = this.holidayList;	

        this.currentYear = new Date().getFullYear();	
        // sessionStorage.setItem('currentyear');
        this.holidayList = this.holidayList.filter(x=>new Date (x.dateOfHoliday).getFullYear() == this.currentYear);


        console.log("holidayList : ", this.holidayList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }
  getFilterHolidayList(value:any){	
    this.selectedYearholidayList = [];	
    let searchYear = parseInt(value);	
    console.log(searchYear,"searchYear")	
    console.log(this.holidayList,"this.holidayListthis.holidayList")	
    this.holidayList1.forEach(holiday =>{	
      const year = new Date(holiday.dateOfHoliday).getFullYear();	
      if(searchYear === year){	
        this.selectedYearholidayList.push(holiday);	
      }      	
    });	
    this.holidayList =  this.selectedYearholidayList;	
    console.log(this.selectedYearholidayList,"this.holidayListholidayList")	
  }

  //pagination 

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortHoliday(sort:Sort){
    console.log(sort);
    const data=this.holidayList;
    if(!sort.active || sort.direction===''){
      this.holidayList=data;
      return ;
    }else {
      this.holidayList=data.sort(
        (a,b)=>{
          const isAsc=sort.direction==='asc';
          switch(sort.active){
            case 'occasion':
              return compare(a.occasion.toLowerCase() , b.occasion.toLowerCase() , isAsc)

              case 'dayOfTheWeek':
                return compare(a.dayOfTheWeek.toLowerCase() , b.dayOfTheWeek.toLowerCase() , isAsc)

                case 'dateOfHoliday':
                  return compare(a.dateOfHoliday , b.dateOfHoliday ,isAsc)

                  case 'state':
                    return compare(a.state , b.state , isAsc)

                    default :
                    return 0;
              
          }
        }
      )
    }
    
  }

}


function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}

