import { Component, EventEmitter, Input, OnInit, Output, ViewEncapsulation } from '@angular/core';

@Component({
  selector: '[app-column-filter-bar]',
  templateUrl: './column-filter-bar.component.html',
  styleUrls: ['./column-filter-bar.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class ColumnFilterBarComponent implements OnInit {


  @Input()
  columnList:any[];
  displayColumns:any[] = [];

  @Output()
  onSearch:EventEmitter<any> = new EventEmitter();

  constructor() { }

  ngOnInit(): void {
    this.setSearchColumns();
  }

  setSearchColumns(){
    this.displayColumns = [];
    
    if(this.columnList && this.columnList.length != 0){
      this.columnList.forEach(columnName => {
          let isBlank:boolean = false;
          if(columnName == 'blank') isBlank=true; 
          this.displayColumns.push({column : columnName, value: '', isBlank: isBlank});
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
