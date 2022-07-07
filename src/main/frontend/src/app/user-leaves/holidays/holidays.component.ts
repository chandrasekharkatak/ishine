import { Component, OnInit } from '@angular/core';
import { first } from 'rxjs/operators';
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
    this.getAllHolidays()
  }

  getAllHolidays(){
    this.holidayList = [];
    
    this.holidayService.getAllHolidays().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayList = response.serviceResponse;
        console.log("holidayList : ", this.holidayList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

}
