import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class BreadcrumbService {

  private breadcrumbSubject: BehaviorSubject<any>;
  breadCrumbSessionItem: string | null;
  public currentBreadcrumb: Observable<any>;

  private messageSource = new BehaviorSubject('profile');
  currentMessage = this.messageSource.asObservable();

  constructor() { 
    this.breadCrumbSessionItem = sessionStorage.getItem('breadcrumb');
    this.breadcrumbSubject = new BehaviorSubject<any>(JSON.parse(this.breadCrumbSessionItem));
    this.currentBreadcrumb = this.breadcrumbSubject.asObservable();
  }

  setBreadcrumbSubject(breadcrumbList: any) {
    sessionStorage.setItem('breadcrumb', JSON.stringify(breadcrumbList));
    this.breadcrumbSubject.next(breadcrumbList);
  }

  addObjectToAddInBreadcrumb(breadcrumbObject: any) {
    let breadcrumbSessionList: any[] = JSON.parse(sessionStorage.getItem('breadcrumb'));
    breadcrumbSessionList.push(breadcrumbObject);
    this.setBreadcrumbSubject(breadcrumbSessionList);
  }

  changeMessage(message: string) {
    this.messageSource.next(message)
  }
}
