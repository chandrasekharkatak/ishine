import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SharedService {

  constructor() { }

  private employeeListSubject = new BehaviorSubject<any[]>([]);
  employeeList$ = this.employeeListSubject.asObservable();


  updateEmployeeList(employeeList: any[]) {
    this.employeeListSubject.next(employeeList);
  }
}
