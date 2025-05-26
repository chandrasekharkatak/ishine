import { Component, Input, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';

@Component({
  selector: 'app-view-employee',
  templateUrl: './view-employee.component.html',
  styleUrls: ['./view-employee.component.css']
})
export class ViewEmployeeComponent implements OnInit {
  @Input()
  allEmployeeData: any[] = [];
  sortColumn: any;
  sortColumnType: any;
  sortDirection = 'asc';
  allProjectTable: boolean = false;
  tableColumns = [];
  filters: any = {};
  page = 1;
  isSearchEnabled: boolean = false;
  
  constructor() { }

  ngOnInit(): void {
  }

   sortData(sort: Sort) {
      //console.log(sort);
      if (sort.active) {
        let sortParams: any[] = sort.active?.split("|");
        this.sortColumn = sortParams[0];
        this.sortColumnType = sortParams[1];
        this.sortDirection = sort.direction;
      }
    }

     onSearch(searchData) {
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

   handlePageChange(event) {
    this.page = event;
  }
  getLength(data:any[]):number{
    // console.log(data);
  if(!data) return 0;
  else return data.length;
  }
   getRowspanForProject(employee: any): number {
    if (!employee.rmgprojects) return 0;
    return employee.rmgprojects.reduce((acc: number, team: any) => {
      return acc + (team.rmgTeam?.length || 0);
    }, 0);
  }

     getHierarchicalSrNo(eIndex: number, pIndex: number, tIndex: number): string {
    const globalProjectIndex = this.getGlobalProjectIndex(eIndex) + 1;
    return `${globalProjectIndex}.${pIndex + 1}.${tIndex + 1}`;
  }
    getGlobalProjectIndex(localPIndex: number): number {
    const itemsPerPage = 5; // Match with HTML
    return (this.page - 1) * itemsPerPage + localPIndex;
  }
}
