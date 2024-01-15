import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Employee } from '../models/employee';
import { Holiday } from '../models/holiday';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class HolidayService {

  private baseUrl:any = environment.baseUrl;

  constructor(private http: HttpClient) { }

  addHoliday(holidayObj:Holiday) {
    return this.http.post(`${this.baseUrl}` + `api/addHoliday`, holidayObj);
  }

  updateHoliday(holidayObj: Holiday) {
    return this.http.post(`${this.baseUrl}` + `api/updateHoliday`, holidayObj);
  }

  deleteHoliday(holidayObj: Holiday) {
    return this.http.post(`${this.baseUrl}` + `api/deleteHoliday`, holidayObj);
  }

  getAllHolidays(holidayObj : Holiday) {
    return this.http.post(`${this.baseUrl}` + `api/getAllHolidays`, holidayObj);
  }

  getAllHolidayByEmpWorkLocation(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/getAllHolidayByEmpWorkLocation`, employeeObj);
  }

  checkOccasionIfAlreadyExist(holidayObj: Holiday){
    return this.http.post(`${this.baseUrl}` + `api/checkOccasionIfAlreadyExist`, holidayObj);
  }

  reconsileHolidayTimesheet(holidayObj: Holiday){
    return this.http.post(`${this.baseUrl}` + `api/reconsileHolidayTimesheet`, holidayObj);
  }

}
