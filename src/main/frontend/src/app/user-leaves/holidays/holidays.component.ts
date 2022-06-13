import { Component, OnInit } from '@angular/core';
import { first } from 'rxjs/operators';
import { HolidayService } from 'src/app/services/holiday.service';

@Component({
  selector: 'app-holidays',
  templateUrl: './holidays.component.html',
  styleUrls: ['./holidays.component.css']
})
export class HolidaysComponent implements OnInit {

  holidayList:any[] = [];

  constructor(
    private holidayService : HolidayService,
  ) { }

  ngOnInit(): void {
    this.getAllHolidays()
  }

  getAllHolidays(){
    this.holidayList = [];

    this.holidayService.getAllHolidays().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.holidayList = response.serviceResponse;
        console.log("leaveTypes : ", this.holidayList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

}
