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
}
