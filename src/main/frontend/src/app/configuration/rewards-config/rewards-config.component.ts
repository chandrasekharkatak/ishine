import { LocationStrategy } from '@angular/common';
import { Component, OnInit,EventEmitter, Input, Output, TemplateRef } from '@angular/core';
import { Query } from 'src/app/models/query';
import { LeaveService } from 'src/app/services/leave.service';
import { first } from 'rxjs/operators';
import { Feature } from 'src/app/models/feature';
import { Rewards } from 'src/app/models/rewards';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { RewardsServiceService } from 'src/app/services/rewards-service.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { Sort } from '@angular/material/sort';


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
  rewardSubData : boolean = false;
  rewardsObj= new Rewards();
  allCategoryist: any[];
  employeeColumns: any[] = [
    'Employee Id', 'Full Name', 'Department', 'Designation', 'Job Role', 'Manager', 'Team Name', 
    'Project Name','Client Name', 'Employment Status', 'Date Of Joining','Gender', 
     'Probation Period', 'Notice Period','Experience',
  ];
  
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
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  rewardsList: Rewards[] = [];  // To store rewards data
  allCategoryList: any[] = [];
  isSearchEnabled: boolean = false;
  rewardsColumns = [
    { columnDef: 'rewardName', header: 'Reward Name' },
    { columnDef: 'categoryName', header: 'Category Name' },
    { columnDef: 'rewardTypes', header: 'Reward Types' }
  ];
  rewardsDataForExcel: any[];
  name = 'Rewards.xlsx';
  isTable: boolean = false;
  filters:any = {};
  page = 1;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  allDeptList: any;
  isEditMode: boolean = false;  // Flag to determine create or edit mode
  rewardIdToEdit: number;  

  @Input() data: any;
  @Output() filterSubmitted:EventEmitter<any> =  new EventEmitter<any>(); 

  constructor(
    private rewardsService: RewardsServiceService,
    private authenticationService: AuthenticationService,
    private leaveService : LeaveService,
    private locationStrategy : LocationStrategy,
    private modalService: BsModalService,
    private exportExcelService: ExportExcelService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

  ngOnInit(): void {
    this.fetchAllRewards();
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
    // if (this.isEditMode && this.rewardIdToEdit) {
    //   this.loadRewardConfiguration(this.rewardIdToEdit);
    // }
  }

  toggleRewardForm() {
    this.rewardSub = !this.rewardSub;
  }

  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }

  showRewardSub(){
    this.rewardSubData = true;
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
      if(!this.queryList[i].column || !this.queryList[i].operator || !this.queryList[i].value || 
        (i<this.queryList.length-1 && !this.queryList[i].conjunction)){
        this.invalidForm=true;
        return false;
      }
    }
    return true;
  }

  openForm(mode: string, rewardObj?: Rewards) {
    this.rewardSub = true;  // Ensure the form is visible

    if (mode === 'edit' && rewardObj) {
        this.isEditMode = true;
        this.rewardsObj = { ...rewardObj };  // Pre-fill form with reward details for editing
        this.rewardTypes = rewardObj.rewardTypes || [];
    } else {
        this.isEditMode = false;
        this.rewardsObj = new Rewards();  // Clear form for creating a new reward
        this.rewardTypes = [];
    }
}


  onEdit(rewardObj: Rewards) {
    this.isEditMode = true;
    this.rewardIdToEdit = rewardObj.id;
    this.rewardsObj = { ...rewardObj }; // Shallow copy the object
    this.rewardTypes = rewardObj.rewardTypes || []; // Ensure rewardTypes is an array
    this.queryList = rewardObj.customFilterDTOList || []; // Handle case where this list might be empty
  }
  
  onSubmit(template: TemplateRef<any>) {
    if (this.validateData()) {
      this.rewardsObj.categoryId = parseInt(this.rewardsObj.categoryId, 10);
      this.rewardsObj.rewardName = (<HTMLInputElement>document.querySelector('input[placeholder="Enter Sub Category name"]')).value;
      this.rewardsObj.rewardTypes = this.rewardTypes;
      this.rewardsObj.customFilterDTOList = this.queryList.map(filter => {
        return {
          ...filter,
          value: typeof filter.value === 'object' && filter.value !== null ? filter.value.name : filter.value,
          conjunction: filter.conjunction || "",  
          customQuery: filter.customQuery || ""   
        };
      });
      console.log("Submit Button : ", this.rewardsObj);
      if (this.isEditMode) {
        this.rewardsObj.updatedBy = this.currentUser.empId;
        this.rewardsService.editRewardConfiguration(this.rewardsObj).subscribe((response: any) => {
          if (response.serviceStatus === 'Success') {
            console.log('Reward updated successfully', response);
            this.openAlertMod(template, response.serviceResponse);
          }
        });
      } else {
        this.rewardsObj.createdBy = this.currentUser.empId;
        this.rewardsService.saveRewardConfiguration(this.rewardsObj).subscribe((response: any) => {
          if (response.serviceStatus === 'Success') {
            console.log('Reward created successfully', response);
            this.openAlertMod(template, response.serviceResponse);
          }
        });
      }
    }
  }
  
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  fetchAllRewards() {
    this.rewardsList = [];
    this.rewardsService.showAllRewards().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === "Success") {
        this.rewardsList = response.serviceResponse;
      } else {
        console.error("Error fetching rewards: ", response.serviceError);
      }
    });
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  onSearch(searchData){
    this.filters = searchData;
    console.log("Updated Filter : ", this.filters);
  }

  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort){
    // console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  exportToExcel(): void {
    this.rewardsService.showAllRewards().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.rewardsDataForExcel = response.serviceResponse;
        //console.log("response.serviceResponse: ",response.serviceResponse);
      } 
      const onlySpecificDataArr = this.rewardsDataForExcel.map(
        x => ({
          "Reward Name": x.rewardName,
          "Category Name": x.categoryName || 'N/A',
          "Reward Types": x.rewardTypes.join(', ')
          // "Created by": x.createdByName,
          // "Created on": (x.createdOn)? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null,
          // "Updaeted by": x.updatedByName ??' - ',
          // "Updated On": (x.updatedOn)? moment(x.updatedOn).format(AppComponent.DATETIME_FORMAT) : ' - ',
        })
      )
      //console.log("Excel Array: ",onlySpecificDataArr);
      this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
    });
  }
}