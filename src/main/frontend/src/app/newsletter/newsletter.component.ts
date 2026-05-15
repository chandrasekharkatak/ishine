import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { LocationStrategy } from '@angular/common';
import { NewsletterService } from '../services/newsletter.service';
import { Newsletter } from '../models/newsletter';
import { first } from 'rxjs/operators';
import * as moment from 'moment';
import { AppComponent } from '../app.component';
import { saveAs } from "file-saver";
import { Sort } from '@angular/material/sort';
import { Document } from '../models/document';
import { Query } from '../models/query';
import { UtilityService } from '../services/utility.service';
import { Feature } from '../models/feature';
import { ActivatedRoute, Router } from '@angular/router';


class FilterData {
  title: any;
  columns: any;
  queryList: any;


}

@Component({
  standalone: false,
  selector: 'app-newsletter',
  templateUrl: './newsletter.component.html',
  styleUrls: ['./newsletter.component.css']
})
export class NewsletterComponent implements OnInit {
  currentUser: User;

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  @ViewChild('preview_document')
  previewDocument: TemplateRef<any>;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  type : any;
  newsletters:any[] = [];
  allReadNewsletters:any[] = [];
  newsletterObj:Newsletter = new Newsletter();
  fileType : any =[];
  storedDataList:any[] = [];

  feature = 'Newsletter';

  src:any;
  fileName:any
  isTable : boolean = false;
  modalRef:NgbModalRef;
  alertMessage: any;
  typeNames:any;

  queryList: any[] = [];
  filterData: any = new FilterData();
  filters:any = {};
  isSearchEnabled:boolean = false;
  newsletterColumns:any[] = ['blank','displayName','name','createdOn'];
  newsletterCol:any[] = ['displayName','createdOn'];

  newsletterModalConfiguration = {
    backdrop: true,
    ignoreBackdropClick: true,
    keyboard: false,
    class : 'modal-xl'
  }

  userMapping: any = {};

  constructor(
    private authenticationService: AuthenticationService,
    private modalService: NgbModal,
    private locationStrategy: LocationStrategy,
    private newsletterService : NewsletterService,
    private utilityService: UtilityService,
    private router : Router
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

   async ngOnInit(): Promise<void> {

    // this.getAllNewsletters();
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
          featureMap.subFeatures?.forEach(sub => {
            this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
        });
    this.getAllTypeForDoc(this.type);
    this.preventBackButton();
    this.showTable(this.type, this.alertTemplate);
  }

  ngAfterViewInit(): void {
    this.openPreviewNewsletterModal(this.alertTemplate);
  }

  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }

  showTable(selectedType,template:TemplateRef<any>){
    //console.log(selectedType," type")
    this.isTable = true;
    this.fileType.forEach(type => {
      type.isActive = (type === selectedType);
      this.type = selectedType;
      this.typeNames = selectedType.typeName;
    });
    this.getAllNewsletters(selectedType,template);
  }
  //  getAllNewsletters(){
  //   this.newsletters = [];
  //   this.sortColumn=[];
  //   this.sortColumnType=[];
  //   this.sortDirection='';
  //   this.newsletterService.getAllNewsletters().pipe(first()).subscribe((response:any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.newsletters =  response.serviceResponse;
  //       this.newsletters.forEach(doc => {
  //         doc.createdOn = (doc.createdOn)? moment(doc.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
  //       });
  //       //console.log("Newsletters List : ", this.newsletters);
  //       this.getAllReadNewsletter();
  //     } else {
  //       console.error(response.serviceResponse);
  //     }
  //   });
  // }

  getAllNewsletters(selectedType, template:TemplateRef<any>){
    //console.log(" newsletter by typeId  ",selectedType);
    let doc = new Document();
    doc.typeId= selectedType.typeId;
    doc.typeName = selectedType.typeName;
    this.newsletters = [];
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    //console.log(" Before call backend " ,doc);
    this.newsletterService.getAllNewslettersByTypeId(doc).pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.newsletters =  response.serviceResponse;
        this.newsletters.forEach(doc => {
          doc.createdOn = (doc.createdOn)? moment(doc.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
         doc.emp360CreatedBy = doc.createdBy;
        });
        //console.log("Newsletters List : ", this.newsletters);
        this.getAllReadNewsletter();
      } else {
        this.openAlertMod(template,response.serviceResponse);
      }
    });
  }
  marksasRead(message: TemplateRef<any>){

    let doc = new Document();
    doc.documentId = this.newsletterObj.documentId;
    let newsletter = new Newsletter();
    newsletter.documentId = doc.documentId;
    newsletter.empId = this.currentUser.empId;
    this.newsletterService.getAllReadNewslettersByEmpId(newsletter).pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(message, "Document marked as read successfully");
        this.getAllNewsletters(this.type, message);
      } else {
        this.openAlertMod(message, "Error marking document as read");
      }
    }
    );
  }


  getAllReadNewsletter(){
    this.allReadNewsletters = [];
    let newsletterObj = new Newsletter();
    newsletterObj.empId = this.currentUser.empId;

    this.newsletterService.getAllReadNewslettersByEmpId(newsletterObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allReadNewsletters = response.serviceResponse;
       //console.log("allReadNewsletters : ", response.serviceResponse);

       this.allReadNewsletters.forEach((readNewsletter:Newsletter) => {
          let fileObj = this.newsletters.find((newsletter:Newsletter) => readNewsletter.documentId == newsletter.documentId);
          if(fileObj) fileObj.isRead = true;
        });
      }else{
        console.error(response.serviceResponse);
      }
    });
  }

  onReadNewsletter(template:TemplateRef<any>){
    this.cancelRequest();

    let newsletterObj = new Newsletter();
    newsletterObj.documentId = this.newsletterObj.documentId;
    newsletterObj.empId = this.currentUser.empId;

    this.newsletterService.onReadNewsletter(newsletterObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let dtoResponse = response.serviceResponse;
        this.currentUser.newsletterReadCheck = dtoResponse.newsletterReadCheck;

        //Update newsletterObj with latest data
        this.newsletterObj = dtoResponse.newsletter;

        //console.log( this.currentUser.newsletterReadCheck , " :  this.currentUser.newsletterReadCheck");
        this.authenticationService.setcurrentUserSubject(this.currentUser);
        this.openPreviewNewsletterModal(this.alertTemplate);
      }

      //this.getAllNewsletters(this.type,template)
    });
  }

  loadingDocument = false;
  previewPolicyDocument(template: TemplateRef<any>,doc: any) {
    this.loadingDocument = true;
    this.src = null;
    this.fileName = doc.displayName;

    this.newsletterObj = doc;
//console.log(" doc.documentId   ",doc.documentId);
    this.newsletterService.downloadDocument(doc.documentId).pipe(first()).subscribe((response:any) => {
      const blob = new Blob([response], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;

      this.src =  a.href;

      if(this.src != null){
        this.openPreviewDocument(template);
      }
      this.loadingDocument = false;
    },
    (error) => {

      console.error('Error fetching document:', error);


      this.loadingDocument = false;
  }
    );
  }

  downloadFile(doc: any) {
    this.newsletterService.downloadDocument(doc.documentId).subscribe(blob => saveAs(blob,doc.fileName));
  }


  //Modal
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  openPreviewDocument(template: TemplateRef<any>){
    this.modalRef = this.modalService.open(template,this.newsletterModalConfiguration);
  }

  cancelRequest() {
    this.modalRef?.close();
  }

  openPreviewNewsletterModal(template:TemplateRef<any>){
    if(this.currentUser.newsletterReadCheck != null){
      this.previewPolicyDocument(this.previewDocument,this.currentUser.newsletterReadCheck);
    }else{
      this.getAllNewsletters(this.type,template);
    }
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

  toggleSearch(){
    this.sortColumn=[];
    this.sortColumnType=[];
    this.sortDirection='';
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  onSearch(searchData){
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }
// added by anurag

getAllTypeForDoc(template:TemplateRef<any>){
  this.newsletterService.getAllTypeName().pipe(first()).subscribe((response:any)=>{
    if(response.serviceStatus == "Success"){
      this.fileType = response.serviceResponse;
      this.type = this.fileType[0]
      this.fileType.forEach(type =>{
        if(this.fileType !== undefined){
          type.isActive = (type == this.type);
        }
      })
      this.getAllNewsletters(this.type, template);
      //console.log(" fileType   ",this.fileType);
    }
  })
}

// custom filter modal

openFilterModal(template: TemplateRef<any> , colums : any[], title: any){
  //console.log("Document columns   ",colums);
  this.queryList = [];
  this.filterData.title = title;
  this.filterData.columns = colums;


  this.queryList = [
    { column: "Document Name" , operator: "" , value: "", conjunction: "" }
  ];
  this. storedDataList.forEach((data) => {
    if(data.filterName == title){
      data.queryList.forEach((queryObj) => {
        if (queryObj.column == 'Created On') {
          queryObj.value = (queryObj.value) ? moment(queryObj.value).format('DD-MM-YYYY HH:mm:ss') : '';
        }
      });
      this.queryList = data.queryList;
    }
  });

  this.filterData.queryList = JSON.stringify(this.queryList);
  //console.log(" filteredData     ",this.filterData);
  this.modalRef = this.modalService.open(template, {modalDialogClass: 'modal-xl'});


}

getCustomDocumentList(queryObjList: any, template: TemplateRef<any>){
  this.newsletters = [];

  let queryObj = new Query();
  queryObj.queryList = queryObjList;

  //console.log("QueryList    ::     ",queryObj);

  if(queryObjList.length == 0){
    this.getAllNewsletters(this.type,template);
  }else{
    this.newsletterService.customQueryForDocument(queryObj).pipe(first()).subscribe((response : any)=>{
      if(response.serviceStatus == "Success"){
        this.newsletters = response.serviceResponse;

        if(this.newsletters.length == 0){
          this.openAlertMod(this.alertTemplate," No Data Found")
        }
      }else{
        this.openAlertMod(template,response.serviceResponse);
      }
    });
  }

}


onFilterSubmit(emittedArray: any, template: TemplateRef<any>){

  if (emittedArray[0].length != 0) {
    //console.log("queryList : ", emittedArray[0]);
    this.queryList = JSON.parse(JSON.stringify(emittedArray[0]));
    this.cancelRequest();

    emittedArray[1].forEach((object) => {
      if (Object.keys(object).length !== 0) {
        if (this.storedDataList.find((x) => x.filterName == object.filterName)) {
          this.storedDataList = this.storedDataList.map(arr1 => emittedArray[1].find(arr2 => arr2.filterName === arr1.filterName) || arr1);
        } else {
          this.storedDataList.push(object);
        }
      }
    });
    emittedArray[0].forEach((query)=>{
      if (query.column == 'Created On') {
        query.value = (query.value) ? moment(query.value, "DD-MM-YYYY").format('YYYY-MM-DD HH:mm:ss') : '';
      }

      if(query.column == 'Created By'){
        query.value = query.value.split("-")[1];
      }
    });

    if(this.filterData.title == 'Filter All Document'){
      this.getCustomDocumentList(emittedArray[0], template);
    }

}else{
  let clearedFilter = this.storedDataList.find((filter) => filter.filterName == emittedArray[1]);
      this.storedDataList.splice(clearedFilter);
}

if (emittedArray[1] == 'Filter All Document') {
  this.showTable(this.type,template);
}


}






}
