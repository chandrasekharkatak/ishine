import { Component, EventEmitter, Input, OnInit, Output, ViewEncapsulation } from '@angular/core';
import { Router } from '@angular/router';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { FilterStateService } from 'src/app/services/filter-state.service';

@Component({
  selector: '[app-column-filter-bar]',
  templateUrl: './column-filter-bar.component.html',
  styleUrls: ['./column-filter-bar.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ColumnFilterBarComponent implements OnInit {


  @Input()
  columnList:any[];
  @Input() searchOnEnter:boolean = false;
  displayColumns:any[] = [];
  currentBreadcrumbList: any[] = [];
  projectManagement: any;

  @Output()
  onSearch:EventEmitter<any> = new EventEmitter();

  constructor(
    private filterStateService: FilterStateService,
    private breadcrumbService: BreadcrumbService,
    private router: Router
  ) {
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
   }

  ngOnInit(): void {
    this.setSearchColumns();

    if ((this.currentBreadcrumbList != undefined && this.currentBreadcrumbList != null) 
    && this.router.url.includes('resource-management')
    && this.currentBreadcrumbList[this.currentBreadcrumbList.length - 1]?.title.includes("Project")) {
      this.onSearch.emit({'name' : this.currentBreadcrumbList[this.currentBreadcrumbList.length - 1]?.object?.projectName}); 
    }

    const savedFilters = this.filterStateService.projectReportFilters;
    if (savedFilters) {
      this.displayColumns.forEach(col => {
        if (savedFilters[col.column]) {
          col.value = savedFilters[col.column];
        }
      });
      this.search(); 
    }
  }

  setSearchColumns(){
    this.displayColumns = [];

    if ((this.currentBreadcrumbList != undefined && this.currentBreadcrumbList != null)
     && this.router.url.includes('resource-management')
     && this.currentBreadcrumbList[this.currentBreadcrumbList.length - 1]?.title.includes("Project")) {
      this.projectManagement = true;
    }
    
    if(this.columnList && this.columnList.length != 0){
      this.columnList.forEach(columnName => {
          let isBlank:boolean = false;
          if(columnName == 'blank') isBlank=true; 

          if(this.projectManagement == true && columnName == 'name'){
            this.displayColumns.push({column : columnName, value: this.currentBreadcrumbList[this.currentBreadcrumbList.length - 1]?.object?.projectName, isBlank: isBlank});
          }else{
            this.displayColumns.push({column : columnName, value: '', isBlank: isBlank});
          }
      });
    }else{
      console.error("column List is empty.")
    }
  }

  search(){
    let searchData = {};

    this.displayColumns.forEach(data => {
      if(data.value?.trim()){
        searchData[data.column] = data.value?.trim();
      }
    });

    //console.log("updated Search Data : ", searchData);
    this.onSearch.emit(searchData);    
  }
}
