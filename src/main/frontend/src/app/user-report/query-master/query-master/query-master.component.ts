import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Feature } from 'src/app/models/feature';
import { Query } from 'src/app/models/query';
import { QueryTable } from 'src/app/models/queryTable';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { QueryTableService } from 'src/app/services/query-table.service';
import { UtilityService } from 'src/app/services/utility.service';

@Component({
  selector: 'app-query-master',
  templateUrl: './query-master.component.html',
  styleUrls: ['./query-master.component.css']
})
export class QueryMasterComponent implements OnInit {

  @ViewChild('alert_message') alertTemplate: TemplateRef<any>;
  modalRef: BsModalRef = new BsModalRef();
  alertMessage: any;
  feature = 'Query Master';
  currentUser: User;
  userMapping: any = {};

  queryObj = new QueryTable();

  isNonPublishedTable : boolean = false;
  isPublishedTable : boolean = false;
  isCreateQueryForm : boolean = false;
  isUpdateQueryForm : boolean = false;

  nonPublishedQueryData: any[] = [];
  nonPublishedList : any;
  PublishedQueryData: any[] = [];
  PublishedList : any;
  filters:any = {};
  previewQueryResult =[];
  headers: string[] = [];

  constructor(
    private authenticationService: AuthenticationService,
    private modalService: BsModalService,
    private queryService : QueryTableService,
    private utilityService : UtilityService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }
   name:string;

  ngOnInit(): void {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.name = sub.subFeatureName;
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    console.log("this.name ",this.name)
    console.log(this.userMapping);
    this.sectionViewInit();
  }

sectionViewInit(){
  if (this.userMapping.non_published) {
    this.showNonPublishedQueryTable();
  } else if (this.userMapping.published) {
    this.showPublishedTable();
  } 
}

reset(){
  this.queryObj.queryName = '';
  this.queryObj.query = '';
}

showCreateQueryForm(){
  this.reset();
  this.isCreateQueryForm = true ;
  this.isNonPublishedTable = false;
  this.isPublishedTable = false;
}

showUpdateForm(query){
  this.isUpdateQueryForm = true;
  this.isNonPublishedTable = false;
  this.isPublishedTable = false;
  this.getQueryDetailsByQueryId(query);
}

  showNonPublishedQueryTable(){
    this.reset();
    this.isNonPublishedTable = true;
    this.isPublishedTable = false;
    this.isCreateQueryForm = false;
    this.isUpdateQueryForm = false;
    this.filters = {};
    this.getNonPublishedQuery();
  }

  showPublishedTable(){
    this.reset();
    this.isPublishedTable = true;
    this.isUpdateQueryForm = false;
    this.isNonPublishedTable=false;
    this.isCreateQueryForm = false;
    this.getPublishedQuery();
  }

  createQuery(query,template : TemplateRef<any>){
    console.log(query,"hii");
  
    let queryObj = new QueryTable();
    queryObj.queryName = query.queryName;
    queryObj.query = query.query;
    queryObj.createdBy = this.currentUser.empId;


    this.queryService.createQuery(queryObj).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.openAlertMod(template,response.serviceResponse);
        this.showNonPublishedQueryTable();
      }else{
        this.openAlertMod(template,response.serviceResponse);
      }
    })
  }

  updateQuery(query , template:TemplateRef<any>){
    let queryobj = new QueryTable();
    queryobj.queryName = query.queryName;
    queryobj.query = query.query;
    queryobj.queryId = query.queryId;
    queryobj.updatedBy = this.currentUser.empId;
    queryobj.updateByName = this.currentUser.name;
    this.queryService.updateQuery(queryobj).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.openAlertMod(template , response.serviceResponse);
        this.showNonPublishedQueryTable();
      }else{
        this.openAlertMod(template , response.serviceResponse)
      }
    })
  }

  deleteQuery(query,template: TemplateRef<any>){
    console.log(" delete method call ",query);
    let queryobj = new QueryTable();

    queryobj.queryId = query.queryId
    queryobj.queryName = query.queryName;
    queryobj.query = query.query;
    
    this.queryService.deleteQuery(queryobj).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.openAlertMod(template , response.serviceResponse);
        this.getNonPublishedQuery();
      }else{
        this.openAlertMod(template , response.serviceResponse)
      }
    })

  }

  getNonPublishedQuery(){
    this.nonPublishedQueryData = []
    this.queryService.getNonPublishedQuery().pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.nonPublishedQueryData = response.serviceResponse;
        this.nonPublishedQueryData.forEach((data)=>{
          data.createdOn = moment(data.createdOn).format(AppComponent.DATE_FORMAT);
        })
        this.nonPublishedList = this.nonPublishedQueryData;
        console.log("   nonPublishedQueryData   ",this.nonPublishedList)

      }else{
        console.log(response.serviceResponse)
      }
    })
  }

  getPublishedQuery(){
    console.log("published data will fetched here !!")
    this.queryService.getPublishedQuery().pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.PublishedQueryData = response.serviceResponse;
        this.PublishedQueryData.forEach((data)=>{
          data.updatedOn = moment(data.updatedOn).format(AppComponent.DATE_FORMAT);
          data.createdOn = moment(data.createdOn).format(AppComponent.DATE_FORMAT);
        });
        this.PublishedList = this.PublishedQueryData;
      }else{
        console.log(response.serviceResponse);
      }
    })
  }

  getQueryDataForPreview(template:TemplateRef<any>){

    let query = new QueryTable();
    query.query = this.queryObj.query;
    this.queryService.getQueryDataForPreview(query).pipe(first()).subscribe((response: any) => {
      
        if(response.serviceStatus == "Success"){
        this.headers = response.serviceResponse[0]; 
        console.log("headers ",this.headers)
        this.previewQueryResult = response.serviceResponse.slice(1);
        console.log("preview Query data ", this.previewQueryResult);
      } else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse);
      }
    });
  }

  getQueryDetailsByQueryId(query){
    console.log("query details   ",query)
    let queryobj = new QueryTable();
    queryobj.queryId = query.queryId;
    this.queryService.getQueryDetailsByQueryId(queryobj).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.queryObj = response.serviceResponse;
      }else{
        this.openAlertMod(this.alertMessage,response.serviceResponse);
      }
    })

  }

  publishQuery(query,template : TemplateRef<any>){

    console.log(query)
    let queryobj = new QueryTable();
    queryobj.queryId = query.queryId;
    queryobj.query = query.query;
    queryobj.queryName = query.queryName;
    queryobj.updatedBy = this.currentUser.empId;
    this.queryService.publishQuery(queryobj).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.openAlertMod(template,response.serviceResponse);
        this.getNonPublishedQuery();
      }else{
        this.openAlertMod(template,response.serviceResponse);
      }
    })

  }




  openPreviewModal(template:TemplateRef<any> , query){
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
    this.queryObj = query; 
    this.getQueryDataForPreview(template);
  }


  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest(){
    this.modalRef.hide();
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  pageNo = 1;
  handlePageChanges(event) {
    this.page = event;
  }


}
