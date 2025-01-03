import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SharedCommunicationService {

  private employeeListSubject = new BehaviorSubject<any[]>([]);
  employeeList$ = this.employeeListSubject.asObservable();

  constructor() { }

  updateEmployeeList(employeeList: any[]) {
    this.employeeListSubject.next(employeeList);
  }
}
