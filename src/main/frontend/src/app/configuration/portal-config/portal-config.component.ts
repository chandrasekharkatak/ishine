import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Portal } from 'src/app/models/portal';
// import { first } from 'rxjs/operators';
import { PortalService } from 'src/app/services/portal.service';
import { ValidationService } from 'src/app/services/validation.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';
import { Feature } from 'src/app/models/feature';
import { Employee } from 'src/app/models/employee';
import { Sort } from '@angular/material/sort';
import { first } from 'rxjs/operators';
import { EmployeeService } from 'src/app/services/employee.service';
import { Query } from 'src/app/models/query';
import { enableAppreciation } from 'src/app/models/enableAppreciation';
import * as moment from 'moment';	



class FilterData{
  title:any;
  columns:any;
  queryList:any;
}



@Component({
  selector: 'app-portal-config',
  templateUrl: './portal-config.component.html',
  styleUrls: ['./portal-config.component.css']
})
export class PortalConfigComponent implements OnInit {

  feature="Portal Config";
  currentUser:User;
  userMapping:any = {};
  allEmployeeList: any;
  allAppreciationEvent:any;
  data:any;
  appByCategory:any;

  //flag	
  portalConfig: boolean = false;	
  appreciationConfig: boolean = false;	
  viewEventConfig: boolean = false;	
  isTable : boolean = false;	
  isCreation : boolean = false;	
  viewAppreciationForm : boolean = false;	
  isAppreciationTable : boolean = false;	
  isUpdation : boolean = false;	
  fromDate:any;	
  toDate:any;

  alertMessage:any;
  @ViewChild('alert_message') alertTemplate:TemplateRef<any>;
  modalRef: BsModalRef = new BsModalRef();

  portalObj:Portal = new Portal();

  portalConfigList:any[] = [];
 // appreciationColumns:any[] = ['Employee Id','Full Name','Email Id','Employment Status','Date of Joining','Department'];
 appreciationColumns:any[] = ['Department'];
  queryList:any[] = [];
  filterData:any = new FilterData(); 
  display=null;
  appreciationObj: enableAppreciation = new enableAppreciation();
  all:any;
  enableAppreciationList:any[] = [];

  constructor(
    private portalService:PortalService,
    private validationService:ValidationService,
    private modalService: BsModalService,
    private employeeService: EmployeeService,
    private authenticationService: AuthenticationService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log(this.feature, " : ", this.userMapping);
    this.getAllEvent();
    this.sectionViewInit();
  }

  reset(){
    this.appreciationObj = new enableAppreciation();
    this.appreciationObj.fromDate=null;
    this.appreciationObj.toDate=null;
    this.appreciationObj.appreciationEventName=null;
    this.appreciationObj.enableAppreciationFor = "";
    this.appreciationObj.appreciationEventName="";	
    this.appreciationObj.appreciateType="";
   
   }

  sectionViewInit() {
    if(this.userMapping.update_portal_global_configuration){
      this.getAllPortalConfig();
    }
    else if(this.userMapping.appreciation_configuration){
       this.enableAppreciationOnclick();
    }
    else if(this.userMapping.appreciation_configuration){	
      this.viewAppreciationEventOnClick();	
    }
    else if(this.userMapping.view_employees_appreciation) {   
       this.viewAllAppreciation();
    }
  }
  getAllEvent(){
   this.allAppreciationEvent=[];
    this.portalService.getAllEvent().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allAppreciationEvent = response.serviceResponse; 
        // this.allEmployeeList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
        console.log("allEventList : ", this.allAppreciationEvent)
      } else {
       this.openAlertMod(this.alertTemplate, response.serviceResponse)
        // console.log("allEventList : ", this.allAppreciationEvent);

      }
    });

  }

  onDateChange(template:TemplateRef<any>){
    if (this.appreciationObj.toDate < this.appreciationObj.fromDate) {
      if (!this.validationService.validateNullUndefinedEmptyString(this.appreciationObj.fromDate)) {
        this.alertMessage = "Please enter from Date !!"
        this.openAlertMod(template, this.alertMessage);
        this.appreciationObj.toDate = ''
        return false;
      }
      if (!this.validationService.validateNullUndefinedEmptyString(this.appreciationObj.toDate)) {
        this.alertMessage = "Please enter To Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
      console.log("end date is small");
      // this.appreciationObj.toDate = ''
      this.alertMessage = "From Date should be less Than To Date !!"
      this.openAlertMod(template, this.alertMessage);
    this.appreciationObj.fromDate = ''
      
    } else {
          this.allEmployeeList = [];
    }
  }

  onToDateChange(template:TemplateRef<any>){
    if(this.appreciationObj.toDate > this.appreciationObj.fromDate){
      if (!this.validationService.validateNullUndefinedEmptyString(this.appreciationObj.fromDate)) {
        this.alertMessage = "Please enter From Date !!"
        this.openAlertMod(template, this.alertMessage);
        // this.appreciationObj.fromDate = ''
        return false;
      }
      if (!this.validationService.validateNullUndefinedEmptyString(this.appreciationObj.toDate)) {
        this.alertMessage = "Please enter To Date !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

     
    }else {
      this.alertMessage = "To Date should be Greater Than From Date !!"
      this.openAlertMod(template, this.alertMessage);
      this.appreciationObj.toDate = ''
      this.allEmployeeList = []
    }
  }

  OnCheckEventName(template:TemplateRef<any>){
    console.log("here in checkPoint event name")

    this.portalService.OnCheckEventName(this.appreciationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.openAlertMod(template, response.serviceResponse);
        this.appreciationObj.appreciationEventName = '';
      }
    });

  }
  
  getAllPortalConfig(){
    this.appreciationObj.fromDate =''
    this.appreciationObj.toDate =''
    this.portalConfig = true;
    this.appreciationConfig =false;
     this.isTable = false;
     this.viewAppreciationForm = false;
     this.isAppreciationTable = false;
     this.viewEventConfig= false;	
     this.isTable = false;	
     this.isUpdation = false;

    this.getAllPortalConfigData();
  }
  getAllPortalConfigData() {

    this.portalService.getPortalConfig().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.portalObj = Object.assign({}, response.serviceResponse);
        this.portalConfigList = response.serviceResponse;

        console.log(this.portalConfigList,  "   :  portalConfigList");

        for(let portal of this.portalConfigList){
          if(portal.configName == 'Probation Period'){
            this.portalObj.probationPeriod = portal.configPeriod;
            this.portalObj.probationMailTrigger = portal.mailTrigger;
          }
          if(portal.configName == 'Notice Period'){
            this.portalObj.noticePeriod = portal.configPeriod;
            this.portalObj.noticeMailTrigger = portal.mailTrigger;
          }
          if(portal.configName == 'OTRS Link'){
            this.portalObj.otrsLink = portal.configValue;
          }
          if(portal.configName == 'SNIPIT Link'){
            this.portalObj.snipitLink = portal.configValue;
          }
          if(portal.configName == 'DSR Download Path'){
            this.portalObj.dsrDownloadPath = portal.configValue;
          }
          if(portal.configName == 'DSR Day'){
            this.portalObj.dsrGenerateDay = portal.configValue;
          }
        }
      } else {
        console.error(response.serviceResponse);
      }
    });
  }
  enableAppreciationOnclick(){
    this.appreciationObj.fromDate =''
    this.appreciationObj.toDate =''
    this.appreciationObj.appreciationEventName = ''
    this.appreciationObj.enableAppreciationFor = ''
    this.portalConfig = false;
    this.appreciationConfig=true;
    this.viewAppreciationForm = false;
    this.isTable = false;
    this.fromDate = null;
    this.toDate = null;
    this.isAppreciationTable = false;
    this.isCreation = true;	
    this.viewEventConfig= false;	
    this.isUpdation = false;	
    this.reset();

  }
  getAllEmployees(template:TemplateRef<any>){
    this.portalService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse; 
        this.allEmployeeList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
        console.log("allEmployeeList : ", this.allEmployeeList)
      } else {
        this.openAlertMod(template ,response.serviceResponse)
      }
    });
  }
  viewAllAppreciation(){
    this.appreciationObj.fromDate =''
    this.appreciationObj.toDate =''
    this.data = ''
    this.portalConfig = false;
    this.appreciationConfig=false;
    this.viewAppreciationForm = true;
    this.isAppreciationTable = false;
    this.isTable = false;
    this.viewEventConfig= false;	
    this.isCreation = false;	
    this.isUpdation = false;

  }
  
  changeEvent(template: TemplateRef<any>, columns:any[], title:any,value:string){
    if (value == "all") {
      this.isTable = true;
      this.allEmployeeList = [];
    }
    else if (value == "custom") {
      this.isTable = true;
      this.openFilterModal(template, columns, title);
    }
   this.getAllEmployees(template);
  }

  updatePortalGlobalConfiguration(portalObj,template: TemplateRef<any>){

    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.probationPeriod)) {
      this.alertMessage = "Please enter Probation Period !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.probationMailTrigger)) {
      this.alertMessage = "Please enter Probation Period Mail Trigger !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.noticePeriod)) {
      this.alertMessage = "Please enter Notice Period !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.noticeMailTrigger)) {
      this.alertMessage = "Please enter Notice Period Mail Trigger !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.otrsLink)) {
      this.alertMessage = "Please enter OTRS Link !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }else if(this.validationService.validateUrl(portalObj.otrsLink) == false){
      this.alertMessage = "Please valid OTRS Link !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.snipitLink)) {
      this.alertMessage = "Please enter SNIPIT Link !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }else if(this.validationService.validateUrl(portalObj.snipitLink) == false){
      this.alertMessage = "Please valid SNIPIT Link !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.dsrDownloadPath)) {
      this.alertMessage = "Please enter DSR folder path !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(portalObj.dsrGenerateDay)) {
      this.alertMessage = "Please enter DSR Generation Day !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }else if(!this.validationService.validateMonthDays(portalObj.dsrGenerateDay)){
      this.alertMessage = "Please enter valid day !!"
      this.openAlertMod(template, this.alertMessage);
      return;
    }

    let tempArray = this.portalConfigList;    

    tempArray.forEach((portalConfig,index)=> {
      
      if(index == 0)
      {       
        this.portalConfigList[0].configPeriod = this.portalObj.probationPeriod;
        this.portalConfigList[0].mailTrigger = this.portalObj.probationMailTrigger;
      }
      else if(index == 1)
      {
        this.portalConfigList[1].configPeriod = this.portalObj.noticePeriod;
        this.portalConfigList[1].mailTrigger = this.portalObj.noticeMailTrigger;
      }else if(index == 2)
      {
        this.portalConfigList[2].configValue = this.portalObj.otrsLink;
      }else if(index == 3)
      {
        this.portalConfigList[3].configValue = this.portalObj.snipitLink;
      }else if(index == 4)
      {
        this.portalConfigList[4].configValue = this.portalObj.dsrDownloadPath;
      }else if(index == 5)
      {
        this.portalConfigList[5].configValue = this.portalObj.dsrGenerateDay;
      }
      
    })
        this.portalObj.allPortalConfigData = this.portalConfigList;

         this.portalService.updatePortalConfig(portalObj).pipe(first()).subscribe((response: any) => {
           if (response.serviceStatus == "Success") {
             this.openAlertMod(template, response.serviceResponse);
           }else{
             this.openAlertMod(template, response.serviceResponse);
           }
         });
  }


  generatePerviousMonthDSR(template: TemplateRef<any>){
    this.portalService.generatePerviousMonthDSR().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }
  
  /* Filter */
  openFilterModal(template: TemplateRef<any>, columns:any[], title:any) {
    console.log("columns : ", columns);
    
    this.filterData.title  = title;
    this.filterData.columns = columns;
    this.filterData.queryList = JSON.stringify(this.queryList);

    console.log("filterData : ", this.filterData);
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  onFilterSubmit(queryList:any , template:TemplateRef<any>){
    console.log("queryList : ", queryList);
    this.queryList = queryList;
    this.cancelRequest();
    
    if(this.filterData.title == 'Filter Appreciation'){
      this.getCustomEmployeeList(queryList,template);
    }
  }

  
  getCustomEmployeeList(queryObjList:any , template : TemplateRef<any>) {
    this.allEmployeeList = [];
    let queryObj = new Query();
    queryObj.queryList = queryObjList;
    if(queryObjList == ''){
      this.getAllEmployees(template);

    }else {
      this.employeeService.customQueryForEmployeeReport(queryObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allEmployeeList = response.serviceResponse;
          console.log("response" +response);

          this.allEmployeeList = this.allEmployeeList.filter((value, index, self) =>
          index === self.findIndex((t) => (
            t.employeementId === value.employeementId
          ))
        )

          if(this.allEmployeeList.length != 0){
            this.openAlertMod(template, "Employee Record found")
          }else{
            this.openAlertMod(template, "No Data found")
          }
          this.allEmployeeList.forEach(employee => {
            employee.employeementId = "A-".concat(employee.employeementId);
          });
          console.log("allEmployeeList : ", this.allEmployeeList)
        } else {
          this.openAlertMod(template,response.serviceResponse)
        }
      });
    }
  }
  validateAppreciation(appreciationObj: enableAppreciation, template: TemplateRef<any>) {

    if (!this.validationService.validateNullUndefinedEmptyString(appreciationObj.fromDate)) {
      this.alertMessage = "Please Select From Date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if (!this.validationService.validateNullUndefinedEmptyString(appreciationObj.toDate)) {
      this.alertMessage = "Please select To Date !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(appreciationObj.appreciationEventName)) {
      this.alertMessage = "Event Name field should not be empty!!!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else  if(!this.validationService.validateAlphaWithSpaceInbetween(appreciationObj.appreciationEventName)) {
      this.alertMessage = "Please Enter Valid Event Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  	
  fromDateFilter = (d: Date)=>{	
    const dateFormat = 'YYYY-MM-DD';	
    const currentDate = new Date();	
    const DAY_IN_MS = 24 * 60 * 60 * 1000;	
    const BACKDATED_LEAVE_PERIOD = 0;	
    const FUTUREDATED_LEAVE_PERIOD = 365;	
    const time=d?.getTime();	
    let minDate = new Date(currentDate.getTime() - (BACKDATED_LEAVE_PERIOD * DAY_IN_MS));	
    let maxDate = new Date(currentDate.getTime() + (FUTUREDATED_LEAVE_PERIOD * DAY_IN_MS));	
      
    return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)));	
    
}	
toDateFilter = (d: Date)=>{	
  const dateFormat = 'YYYY-MM-DD';	
  const currentDate = new Date();	
  const DAY_IN_MS = 24 * 60 * 60 * 1000;	
  const BACKDATED_LEAVE_PERIOD = 0;	
  const FUTUREDATED_LEAVE_PERIOD = 365;	
  const time=d?.getTime();	
  let minDate = new Date(currentDate.getTime() - (BACKDATED_LEAVE_PERIOD * DAY_IN_MS));	
  let maxDate = new Date(currentDate.getTime() + (FUTUREDATED_LEAVE_PERIOD * DAY_IN_MS));	
    
  return ((moment(d).format(dateFormat) >= moment(minDate).format(dateFormat) && moment(d).format(dateFormat) <= moment(maxDate).format(dateFormat)));	
  
}

  enableAppreciation(template: TemplateRef<any>){
    const dateFormat = 'YYYY-MM-DD';
    let inputValidated: boolean = this.validateAppreciation(this.appreciationObj, template)
    if (!inputValidated) return;
    this.enableAppreciationList = [];

    this.enableAppreciationList = this.allEmployeeList.map(employee => {
      return {
        // employeementId : employee.employeementId,
        empId : employee.empId,
        isAppreciationEnable : true
      }
    });

    console.log("enableAppreciationList : ", this.enableAppreciationList);
    this.appreciationObj.fromDate = moment(this.appreciationObj.fromDate).format(dateFormat)
    this.appreciationObj.toDate = moment(this.appreciationObj.toDate).format(dateFormat)

    this.appreciationObj.appreciationEventName = this.appreciationObj.appreciationEventName;
    this.appreciationObj.enableAppreciationList = this.enableAppreciationList;
    console.log("enableAppreciation : ", this.appreciationObj)
    
    this.portalService.enableAppreciation(this.appreciationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.all = response.serviceResponse;
        console.log("appreciation : ", this.all)
        this.enableAppreciationList = []
        this.allEmployeeList = []
        this.openAlertMod(template, response.serviceResponse); 
        this.reset();       
      } else {
        console.error(response.serviceResponse)
      }
    });

  }
  viewAppreciationsOnSubmit(){
    this.portalConfig = false;
    this.appreciationConfig=false;
    this.viewAppreciationForm = false;
    // this.isAppreciationTable = true;
    this.isTable = false;
   this.viewAppreciations();
   this.isAppreciationTable = true;	
    this.viewEventConfig= false;	
    this.isCreation = false;	
    this.isUpdation = false;	


  }

  viewAppreciations(){
    this.appByCategory = [];	
    this.portalService.viewAppreciations(this.appreciationObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.appByCategory = response.serviceResponse;
        console.log("appreciation : ", this.appByCategory)
        this.isAppreciationTable = true;

        // this.openAlertMod(template, response.serviceResponse); 
        this.reset();       
      } else {
        console.error(response.serviceResponse)
      }
    });

  }
  viewAppreciationEventOnClick(){	
    this.portalConfig = false;	
    this.appreciationConfig=false;	
    this.viewAppreciationForm = false;	
    this.isAppreciationTable = false;	
    this.isTable = false;	
    this.viewEventConfig= true;	
    this.isCreation = false;	
    this.isUpdation = false;	
    this.getAllEvent();

  }	
  validateEnableAppreciationObj(appreciationObj: enableAppreciation, template: TemplateRef<any>) {	
    if (!this.validationService.validateNullUndefinedEmptyString(appreciationObj.fromDate)) {	
      this.alertMessage = "Please enter From Date !!"	
      this.openAlertMod(template, this.alertMessage);	
      return false;	
    }	
    if (!this.validationService.validateAlphaWithSpace(appreciationObj.appreciationEventName)) {	
      this.alertMessage = "Please enter Valid Event Name!!"	
      this.openAlertMod(template, this.alertMessage);	
      return false;	
    }	
    if (!this.validationService.validateNullUndefinedEmptyString(appreciationObj.toDate)) {	
      this.alertMessage = "Please select To Date !!"	
      this.openAlertMod(template, this.alertMessage);	
      return false;	
    }	
    if (!this.validationService.validateNullUndefinedEmptyString(appreciationObj.enableAppreciationFor)) {	
      this.alertMessage = "Please select Employees  !!"	
      this.openAlertMod(template, this.alertMessage);	
      return false;	
    }	
    return true;	
  }	
  onUpdateAppreciationEvent(template: TemplateRef<any>){	
    const dateFormat = 'YYYY-MM-DD';
    let inputValidated: boolean = this.validateEnableAppreciationObj(this.appreciationObj, template)	
    if (!inputValidated) return;	

    this.enableAppreciationList = this.allEmployeeList.map(employee => {	
      return {	
        empId : employee.empId,	
        isAppreciationEnable : true	
      }	
    });	


    this.appreciationObj.fromDate = moment(this.appreciationObj.fromDate).format(dateFormat)
    this.appreciationObj.toDate = moment(this.appreciationObj.toDate).format(dateFormat)
    this.appreciationObj.appreciationEventName = this.appreciationObj.appreciationEventName;
    this.appreciationObj.enableAppreciationList = this.enableAppreciationList;    console.log("updateAppreciation : ", this.appreciationObj)

  
    this.appreciationObj.updatedBy = this.currentUser.empId;;	
    console.log("Update dept : ", this.appreciationObj);	
    this.portalService.updateAppreciationEvent(this.appreciationObj).pipe(first()).subscribe((response: any) => {	
      if (response.serviceStatus == "Success") {	
        this.openAlertMod(template, response.serviceResponse);	
        this.reset();	
        //this.showTable();	
      } else {	
        this.openAlertMod(template, response.serviceResponse);	
        this.reset();	
      }	
    });	
  }
  showUpdateForm(appreciationEvent: enableAppreciation) {	
    this.portalConfig = false;	
    this.appreciationConfig=true;	
    this.viewAppreciationForm = false;	
    this.isAppreciationTable = false;	
    this.isTable = false;	
    this.viewEventConfig= false;	
    this.isCreation = false;	
    this.isUpdation = true;	
    this.appreciationObj = Object.assign({}, appreciationEvent)	
  }
  openDeleteAppreciationEvent(template: TemplateRef<any>, appreciationEvent: any) {	
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });	
    this.appreciationObj = appreciationEvent;	
    console.log(this.appreciationObj);	
  }	
  onDeleteAppreciationEvent(template: TemplateRef<any>) {	
    this.cancelRequest();	
    this.portalService.deleteAppreciationEvent(this.appreciationObj).pipe(first()).subscribe((response: any) => {	
      if (response.serviceStatus == "Success") {	
        this.openAlertMod(template, response.serviceResponse);	
        // this.showTable();	
        this.getAllEvent();	
        // this.viewAppreciationEventOnClick();	
      } else {	
        this.openAlertMod(template, response.serviceResponse);	
        this.getAllEvent();	
        // this.viewAppreciationEventOnClick();	
      }	
    });	
  }	

  //modal

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }
  sortData(sort:Sort){	
    console.log(sort);	
    	
    const data=this.allEmployeeList;	
   	
    if(!sort.active || sort.direction==='')	
    {	
      this.allEmployeeList=data;	
      return;	
    }	
    else {	
      this.allEmployeeList=data.sort(	
        (a,b)=>{	
          const isAsc =sort.direction==='asc';	
          switch(sort.active){	
            // case 'i':	
            // return compare(a.index , b.index , isAsc)	
            case 'empId':	
              return compare(a.empId.toLowerCase() , b.empId.toLowerCase() , isAsc)	
              case 'name':	
                return compare(a.name.toLowerCase() , b.name.toLowerCase() , isAsc)	
                case 'email':	
                  return compare(a.email.toLowerCase() , b.email.toLowerCase() , isAsc)	
                  case 'employmentstatus':	
                  return compare(a.employmentstatus.toLowerCase() , b.employmentstatus.toLowerCase() , isAsc)
                  case 'dateOfJoining':	
                  return compare(a.dateOfJoining.toLowerCase() , b.dateOfJoining.toLowerCase() , isAsc)
                default:	
                 return 0;	
          }	
        }	
      )	
    }	
    	
    	
  }	

  sortViewAppreciationData(sort:Sort){	
    console.log(sort);	
    	
    const data=this.appByCategory;	
   	
    if(!sort.active || sort.direction==='')	
    {	
      this.appByCategory=data;	
      return;	
    }	
    else {	
      this.appByCategory=data.sort(	
        (a,b)=>{	
          const isAsc =sort.direction==='asc';	
          switch(sort.active){	
            // case 'i':	
            // return compare(a.index , b.index , isAsc)	
            case 'appreciateType':	
              return compare(a.appreciateType.toLowerCase() , b.appreciateType.toLowerCase() , isAsc)	
              case 'appreciationToName':	
                return compare(a.appreciationToName.toLowerCase() , b.appreciationToName.toLowerCase() , isAsc)	
                case 'appreciationByName':	
                  return compare(a.appreciationByName.toLowerCase() , b.appreciationByName.toLowerCase() , isAsc)	
                  case 'appreciationDate':	
                  return compare(a.appreciationDate.toLowerCase() , b.appreciationDate.toLowerCase() , isAsc)
                  case 'managerName':	
                  return compare(a.managerName.toLowerCase() , b.managerName.toLowerCase() , isAsc)
                  case 'reason':	
                  return compare(a.reason.toLowerCase() , b.reason.toLowerCase() , isAsc)
                default:	
                 return 0;	
          }	
        }	
      )	
    }	
    	
    	
  }	

}

function compare(a: number | string, b: number | string, isAsc: boolean) {	
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);

}
