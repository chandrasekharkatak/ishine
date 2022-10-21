import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Employee } from '../models/employee';
import { Holiday } from '../models/holiday';

@Injectable({
  providedIn: 'root'
})
export class HolidayService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

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

  getAllHolidays() {
    return this.http.get(`${this.baseUrl}` + `api/getAllHolidays`);
  }

  getAllHolidayByEmpWorkLocation(employeeObj: Employee) {
    return this.http.post(`${this.baseUrl}` + `api/getAllHolidayByEmpWorkLocation`, employeeObj);
  }

  checkOccasionIfAlreadyExist(holidayObj: Holiday){
    return this.http.post(`${this.baseUrl}` + `api/checkOccasionIfAlreadyExist`, holidayObj);
  }

}
