import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class FilterStateService {

  public projectReportFilters: any = null;
  public deptIdListByUser: any[] = [];
  public deptIdList: any[] = [];
  public selectedProjectStatus: any = null;
  public myDept: boolean = false;

  constructor() { }

  public clearProjectReportFilters(): void {
    this.projectReportFilters = null;
  }
}