import { Component, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';

@Component({
  selector: 'app-reimbursment-config',
  templateUrl: './reimbursment-config.component.html',
  styleUrls: ['./reimbursment-config.component.css']
})
export class ReimbursmentConfigComponent implements OnInit {

  items:any=10;
  page:any;
  isSearchEnabledReview: boolean = false;
  isCategoryTable: boolean = true;
  createCategoryForm:boolean =false;
  filters: any = {};
  sortColumn: any;
  sortColumnType: any;
  sortDirection = 'asc';
  handlePageChange(event) {
    this.page = event;
  }
  reviewColumns: any[] = ['blank', 'reviewLabel', 'reviewFieldType', 'condition', 'quarterCycle', 'departmentName', 'employeeName', 'createdOn', 'updatedByName', 'updatedOn']

  constructor() { }

  ngOnInit(): void {
  }

  showQuaterTable(){
    this.isCategoryTable=true;
    this.isClass=false;
    this.istravelMode=false;
    this.travelModeForm=false;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
  }
  createCategory(){
    this.isCategoryTable=false;
    this.createCategoryForm=true;
    this.travelModeForm=false;
    this.classCategoryForm=false;
    this.isClass=false;
    this.istravelMode=false;

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
toggleSearchReviewType() {
    this.isSearchEnabledReview = !this.isSearchEnabledReview;
    if (!this.isSearchEnabledReview) {
      this.filters = {};
    }
  }
  istravelMode:Boolean=false;
  subCategory(){
   this.istravelMode=true;
   this.isClass=false;
   this.isCategoryTable=false;
   this.travelModeForm=false;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
  }

  isClass:boolean=false;
  classCategory(){
    this.isClass=true;
    this.istravelMode=false;
    this.isCategoryTable=false;
    this.travelModeForm=false;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
  }

  classCategoryForm:boolean=false;
  travelModeForm:boolean=false;
  subClassCategory(){
    this.travelModeForm=false;
    this.classCategoryForm=true;
    this.createCategoryForm=false;
    this.isClass=false;
    this.istravelMode=false;
    this.isCategoryTable=false;
  }

  travelCategory(){
    this.travelModeForm=true;
    this.classCategoryForm=false;
    this.createCategoryForm=false;
    this.isClass=false;
    this.istravelMode=false;
    this.isCategoryTable=false;
  }

}
