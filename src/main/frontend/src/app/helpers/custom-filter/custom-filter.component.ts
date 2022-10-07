import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { Query } from 'src/app/models/query';

class Operator{
  name:string;
  symbol:string;
}

@Component({
  selector: 'app-custom-filter',
  templateUrl: './custom-filter.component.html',
  styleUrls: ['./custom-filter.component.css']
})
export class CustomFilterComponent implements OnInit {

  columnList:any[]=[]
  operatorList:Operator[]=[{name:"Equal",symbol:"="},{name:"Contains",symbol:"like"},{name:"Less than",symbol:"<"},
  {name:"Greator Than",symbol:">"},{name:"Less or Equal",symbol:"<="},{name:"Greator or equal",symbol:">="},
  {name:" Not Equal",symbol:"!="}];
  conjunctionList:Operator[]=[{name:"AND",symbol:"AND"},{name:"OR",symbol:"OR"}];

  queryList:Query[]=[new Query()];
  invalidForm: boolean;

  @Input() data: any;
  @Output() filterSubmitted:EventEmitter<any> =  new EventEmitter<any>(); 
  constructor() { }

  ngOnInit(): void {
    this.columnList = this.data.columns;
    if (this.data.queryList.length > 5) {
      this.queryList = JSON.parse(this.data.queryList);
    }
  }

  addFilter(i){
    this.queryList.splice(i+1,0,new Query());
  }

  removeFilter(i){
    this.queryList.splice(i,1);
  }
  submit(){
    if(this.validateData()){
      if(this.queryList[0].column==null){
        console.log("this.queryList : ",this.queryList);
        this.filterSubmitted.emit([]);
      }
      else{
        console.log("this.queryList : ",this.queryList);
        this.filterSubmitted.emit(this.queryList);
      }
      
    };
    
  }
  clear(){
    this.queryList=[new Query()];
    this.invalidForm=false;
  }

  validateData(){
    for(let i=1;i<this.queryList.length;i++){
      // if(this.queryList[i].operator=='like'){
      //   this.queryList[i].value='%'+this.queryList[i].value+'%';
      // }
      if(!this.queryList[i].column || !this.queryList[i].operator || !this.queryList[i].value || 
        (i<this.queryList.length-1 && !this.queryList[i].conjunction)){
        this.invalidForm=true;
        return false;
      }
    }
    return true;
  }

}
