import { LocationStrategy } from '@angular/common';
import { Component, OnInit,EventEmitter, Input, Output } from '@angular/core';
import { Query } from 'src/app/models/query';
import { LeaveService } from 'src/app/services/leave.service';
import { first } from 'rxjs/operators';
import { Feature } from 'src/app/models/feature';
import { Rewards } from 'src/app/models/rewards';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { RewardsServiceService } from 'src/app/services/rewards-service.service';


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
  selector: 'app-rewards-config',
  templateUrl: './rewards-config.component.html',
  styleUrls: ['./rewards-config.component.css']
})
export class RewardsConfigComponent implements OnInit {

  feature = "Rewards Config";
  currentUser: User;
  userMapping: any = {};
  rewardSub : boolean = false;
  rewardsObj= new Rewards();
  allCategoryist: any[];
  employeeColumns: any[] = [
    'Employee Id', 'Full Name', 'Department', 'Designation', 'Job Role', 'Manager', 'Team Name', 
    'Project Name','Client Name', 'Employment Status', 'Date Of Joining','Gender', 
     'Probation Period', 'Notice Period','Experience',
  ];
  // rewardsService: any;
  rewardType: string = '';
  rewardTypes: string[] = [];
  selectedRewardType: string | null = null;
  columnList:any[]=[]
  operatorList:Operator[]=[{name:"Equal",symbol:"="},{name:"Contains",symbol:"like"},{name:"Less than",symbol:"<"},
  {name:"Greator Than",symbol:">"},{name:"Less or Equal",symbol:"<="},{name:"Greator or equal",symbol:">="},
  {name:" Not Equal",symbol:"!="}];
  conjunctionList:Operator[]=[{name:"AND",symbol:"AND"},{name:"OR",symbol:"OR"}];
  
  queryList:Query[]=[new Query()];
  invalidForm: boolean;
  valueOptionList = [];
  keyword = "name";
  storedFilterData:storedData[] = [new storedData()];

  @Input() data: any;
  @Output() filterSubmitted:EventEmitter<any> =  new EventEmitter<any>(); 

  constructor(
    private rewardsService: RewardsServiceService,
    private authenticationService: AuthenticationService,
    private leaveService : LeaveService,
    private locationStrategy : LocationStrategy,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

  ngOnInit(): void {
    
    this.showRewardSub();
    this.columnList = this.employeeColumns;
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    this.preventBackButton();
    this.columnList = this.data.columns;
    if (this.data.queryList.length > 5) {
      this.queryList = JSON.parse(this.data.queryList);
    }
    
  }

  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }

  showRewardSub(){
    this.rewardSub = true;
    this.getAllCategoryName();
  }

  getAllCategoryName(){
    this.allCategoryist = [];
    this.rewardsService.getAllRewardsCategory().pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.allCategoryist = response.serviceResponse;
        //console.log("this.allTypeList   ::   ",this.allTypeList);
      }
    })
  }

  addRewardType() {
    if (this.rewardType) {
      this.rewardTypes.push(this.rewardType);
      this.rewardType = ''; // Clear the input field
    }
  }

  removeRewardType(index: number) {
    this.rewardTypes.splice(index, 1);
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

        //console.log(this.storedFilterData, " : this.storedFilterData");
        
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


}

