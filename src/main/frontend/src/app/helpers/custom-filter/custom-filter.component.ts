import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { first } from 'rxjs/operators';
import { Query } from 'src/app/models/query';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { LeaveService } from 'src/app/services/leave.service';

class Operator{
  name:string;
  symbol:string;
}

class storedData{
  filterName:any;
  queryList:any;
  columnData:any;
}

@Component({
  selector: 'app-custom-filter',
  templateUrl: './custom-filter.component.html',
  styleUrls: ['./custom-filter.component.css']
})
export class CustomFilterComponent implements OnInit {

  
  columnList:any[]=[]
  operatorList:Operator[]=[{name:"Equal",symbol:"="},{name:"Contains",symbol:"like"},{name:"Less than",symbol:"<"},
  {name:"Greater Than",symbol:">"},{name:"Less or Equal",symbol:"<="},{name:"Greater or equal",symbol:">="},
  {name:" Not Equal",symbol:"!="}];
  conjunctionList:Operator[]=[{name:"AND",symbol:"AND"},{name:"OR",symbol:"OR"}];
  currentUser:User;
  queryList:Query[]=[new Query()];
  invalidForm: boolean;
  valueOptionList = [];
  keyword = "name";
  storedFilterData:storedData[] = [new storedData()];
  @Input() data: any;
  @Output() filterSubmitted:EventEmitter<any> =  new EventEmitter<any>(); 
  constructor(
     private authenticationService:AuthenticationService,
    private leaveService : LeaveService
  ) {this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    this.columnList = this.data.columns;
    if (this.data.queryList.length > 5) {
      this.queryList = JSON.parse(this.data.queryList);
    }
  }

  addFilter(i){
    this.queryList.splice(i+1,0,new Query());
  }

  selectEvent(value:any){
    //console.log(value, " : value");
  }

  onChangeSearch(a){
    //console.log(a, " : a");
  }

  valueFocus(columnName){
    if(this.queryList.length != 0 && columnName != undefined && columnName != null){
      this.getValueOptionData(columnName);
    }
  }

  getValueOptionData(column:any){
    this.valueOptionList = [];

    let queryObj = new Query();
    queryObj.column = column;
    queryObj.empId= this.currentUser.empId;

    this.leaveService.getValueOptionData(queryObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {        
        this.valueOptionList = response.serviceResponse;
        if(column == 'Employee Id'){
          this.valueOptionList.forEach((x) => {
            x.name = "A-".concat(x.name);
          });
        }
        //console.log(this.valueOptionList , ":this.valueOptionList ");
      } else {
        //console.log(response.serviceResponse, " : response.serviceResponse");
      }
    }); 
  }

  removeFilter(i){
    this.queryList.splice(i,1);
  }
  submit(){
    if(this.validateData()){
      if(this.queryList[0].column==null){
        //console.log("this.queryList : ",this.queryList);
        let arrayToBeEmitted = [[],this.data.title];
        this.filterSubmitted.emit(arrayToBeEmitted);
      }
      else{
        //console.log("this.queryList : ",this.queryList);
        this.queryList.forEach((obj) => {
          if(typeof obj.value === 'object'){
            obj.value = obj.value.name;
          }
        });

        this.storedFilterData.forEach((data) => {
          if(data.filterName == this.data.title){
            data.queryList = this.queryList;
          }else{
            let storedDataObj = new storedData();
            storedDataObj.filterName = this.data.title;
            storedDataObj.queryList = this.queryList;

            this.storedFilterData.push(storedDataObj);
          }
        });

        console.log(this.storedFilterData, " : this.storedFilterData");
        
        let arrayToBeEmitted = [this.queryList,this.storedFilterData];
        this.filterSubmitted.emit(arrayToBeEmitted);
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
  getPlaceholder(column: string): string {
    const dateFields = ['From Date', 'To Date', 'Date','Po Start Date','Po End Date'];
    return dateFields.includes(column) ? 'DD-MM-YYYY' : 'Enter value';
}
}
