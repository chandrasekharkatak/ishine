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

  feature="Holiday";
  currentUser:User;
  userMapping:any = {};

  holidayList:any[] = [];

  constructor(
    private holidayService : HolidayService,
    private authenticationService : AuthenticationService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

  ngOnInit(): void {
    this.getAllHolidayByEmpWorkLocation()
  }

  getAllHolidayByEmpWorkLocation(){
    this.holidayList = [];
    
    let employeeObj = new Employee();
    employeeObj.empId = this.currentUser.empId;
    this.holidayService.getAllHolidayByEmpWorkLocation(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayList = response.serviceResponse;
        console.log("holidayList : ", this.holidayList);
      } else {
        console.error(response.serviceResponse);
      }
    });
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

