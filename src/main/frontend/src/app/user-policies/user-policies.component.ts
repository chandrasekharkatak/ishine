import { AfterViewInit, Component, OnInit,TemplateRef, ViewChild } from '@angular/core';
import { User } from 'src/app/models/user';
import { PoliciesService } from '../services/policies.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { saveAs } from "file-saver";
import { Sort } from '@angular/material/sort';
import { UploadPolicy } from 'src/app/models/UploadPolicy';

import { Feature } from 'src/app/models/feature';
import { LocationStrategy } from '@angular/common';
import * as moment from 'moment';
import { AppComponent } from '../app.component';






@Component({
  selector: 'app-user-policies',
  templateUrl: './user-policies.component.html',
  styleUrls: ['./user-policies.component.css']
})
export class UserPoliciesComponent implements OnInit, AfterViewInit {
  currentUser: User;

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;


  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  constructor(private policiesService : PoliciesService,
    private authenticationService: AuthenticationService,
    private modalService: BsModalService,
    private locationStrategy: LocationStrategy
  ) { 
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);

  }
  document:any[] = [];
  data:string;
  modalRef: BsModalRef = new BsModalRef();
  fileObj:UploadPolicy = new UploadPolicy();  
  alertMessage: any;
  allReadPoliciesList:any[] = [];

  filters:any = {};
  isSearchEnabled:boolean = false;
  policyColumns:any[] = ['blank','fileName','policyName','createdByName','createdOn'];
 

  ngOnInit(): void {

    this.getAllDocuments();
    this.preventBackButton();
  }

  ngAfterViewInit(): void {
    if(this.currentUser.isAllPolicyMarkAsRead == 'false'){
      this.openAlertMod(this.alertTemplate, "Please read all the policies and mark them as read to proceed further.")
    }
  }

  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }
  getAllDocuments(){
    this.data='';
    this.document = [];
    this.policiesService.getAllDocument().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.document =  response.serviceResponse;
        this.document.forEach(doc => {
          doc.createdOn = (doc.createdOn)? moment(doc.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        this.getAllReadPolicies();
        console.log("DocumentList xyz: ", this.document);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }
  getAllReadPolicies(){
    this.allReadPoliciesList = [];
    let fileObj = new UploadPolicy();
    fileObj.empId = this.currentUser.empId;

    this.policiesService.getReadPoliciesByEmpId(fileObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allReadPoliciesList = response.serviceResponse;
       console.log("this.alll : ", response.serviceResponse);
       this.allReadPoliciesList.forEach((readPolicies:UploadPolicy) => {
          let fileObj = this.document.find((policy:UploadPolicy) => readPolicies.policyID == policy.policyID);
          if(fileObj) fileObj.isRead = true;
        }); 
      }else{
        console.error(response.serviceResponse);
      }
    });

  }
  
  downloadFile(doc: any) {
    this.policiesService.downloadDocument( doc.policyID).subscribe(blob => saveAs(blob,doc.fileName));
  }
  cancelRequest() {
    this.modalRef.hide();
  }
  openReadEnabledMod(template: TemplateRef<any>, fileObj:UploadPolicy) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.fileObj = fileObj;
  }
  onReadPolicy(template: TemplateRef<any>){
    this.cancelRequest();
    let fileObj = new UploadPolicy();
    fileObj.policyID = this.fileObj.policyID;
    fileObj.empId = this.currentUser.empId;
    console.log("Activate Survey : ", fileObj);
    this.policiesService.onReadPolicy(fileObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getAllDocuments();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort){	
    console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;      
    }
  }
  
  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
  }

  onSearch(searchData){
    this.filters = searchData;
    console.log("Updated Filter : ", this.filters);
  }

}
function compare(a: number | string, b: number | string, isAsc: boolean) {	
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);

}