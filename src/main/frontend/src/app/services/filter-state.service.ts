import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class FilterStateService {

  public projectReportFilters: any = null;
  public deptIdListByUser: any[] = [];
  public deptIdList: any[] = [];
  public selectedStatusTab: any = null;
  public selectedProjectStatus: any = null;
  public projectPageSize:any = 10;
  public myDept: boolean = false;
  public isNewRmgDashboard:boolean = true;

  constructor() { }

  public clearProjectReportFilters(): void {
    this.projectReportFilters = null;
  }
}