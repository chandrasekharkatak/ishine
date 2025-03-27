import { Component, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { Breadcrumb } from 'src/app/models/breadcrumd';
import { LMSResponse } from 'src/app/models/lmsResponse';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { Employee360Service } from 'src/app/services/employee360.service';

@Component({
  selector: 'app-lms',
  templateUrl: './lms.component.html',
  styleUrls: ['./lms.component.css']
})
export class LMSComponent implements OnInit {
  currentBreadcrumbList: any[] = [];
  isSearchEnabled:boolean = false;
  filters:any = {};
  displayData: boolean;
  CoursesStatus=false;
  QuizzesStatus=false;
  SurveysStatus=false;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  courseColumns=['blank','Name','Completion_Progress'];
  lmsList:LMSResponse;




  constructor(
    private breadcrumbService: BreadcrumbService,
private employee360Service:Employee360Service

  ) { }

  ngOnInit(): void {
     let findbreadcrumbObject = this.currentBreadcrumbList.findIndex(x => x.title == "LMS");
                if (findbreadcrumbObject >= 0) {
                  this.currentBreadcrumbList.splice(findbreadcrumbObject + 1);
                  this.breadcrumbService.setBreadcrumbSubject(this.currentBreadcrumbList);
                } else {
                  let breadcrumbObject = new Breadcrumb();
                  breadcrumbObject.title = "LMS";
                  breadcrumbObject.url = "/employee-360/lms";
                  this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
                }
                this.getLmsRecored();
  }

  getLmsRecored(){
    this.employee360Service.getLmsData("shivtosh.pal@apmosys.com").subscribe((response:any)=>{
      this.lmsList=response;
    })
    this.CoursesStatus=true
  }
  display(data:any){
    this.CoursesStatus=false;
    this.QuizzesStatus=false;
    this.SurveysStatus=false;
    if(data=="Courses"){
      this.CoursesStatus=true;
    }
    if(data=="Quizzes"){
      this.QuizzesStatus=true;
    }
    if(data=="Surveys"){
      this.SurveysStatus=true;
    }
  }
  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  onSearch(searchData){
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

  page = 1;
    handlePageChange(event) {
      this.page = event;
    }
  
    sortData(sort: Sort){
      //console.log(sort);
      if(sort.active){
        let sortParams:any[] = sort.active?.split("|");
        this.sortColumn = sortParams[0];
        this.sortColumnType = sortParams[1];
        this.sortDirection = sort.direction;
      }
    }


}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);

}
