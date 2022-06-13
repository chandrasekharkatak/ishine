import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Holiday } from '../models/holiday';

@Injectable({
  providedIn: 'root'
})
export class HolidayService {

  private baseUrl:any = (window as { [key: string]: any })["__proxyConfigIp"] as string + "/";

  constructor(private http: HttpClient) { }

  addHoliday(holidayObj:Holiday) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/addHoliday`, holidayObj);
  }

  updateHoliday(holidayObj: Holiday) {
    return this.http.post(`${this.baseUrl}` + `employeeportal/api/updateHoliday`, holidayObj);
  }

  getAllHolidays() {
    return this.http.get(`${this.baseUrl}` + `employeeportal/api/getAllHolidays`);
  }
}
