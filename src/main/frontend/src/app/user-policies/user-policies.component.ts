import { AfterViewInit, Component, OnInit,TemplateRef, ViewChild } from '@angular/core';
import { User } from 'src/app/models/user';
import { PoliciesService } from '../services/policies.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { saveAs } from "file-saver";
import { Sort } from '@angular/material/sort';
import { UploadPolicy } from 'src/app/models/UploadPolicy';

import { Feature } from 'src/app/models/feature';
import { LocationStrategy } from '@angular/common';
import * as moment from 'moment';
import { AppComponent } from '../app.component';
import { UtilityService } from '../services/utility.service';
import { ActivatedRoute, Router } from '@angular/router';



@Component({
  standalone: false,
  selector: 'app-user-policies',
  templateUrl: './user-policies.component.html',
  styleUrls: ['./user-policies.component.css']
})
export class UserPoliciesComponent implements OnInit, AfterViewInit {
  currentUser: User;

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  @ViewChild('preview_document')
  previewDocument: TemplateRef<any>;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  currentDoc :any;

  src:any;
  fileName:any

  policyModalConfiguration = {
    backdrop: true,
    ignoreBackdropClick: true,
    keyboard: false,
    class : 'modal-xl'
  }

  readEnambleModalConfig = {
    backdrop: true,
    ignoreBackdropClick: true,
    keyboard: false,
    class : 'modal-sm'
  }
  userMapping: any = {};
  feature = 'HR Policies';



  constructor(private policiesService : PoliciesService,
    private authenticationService: AuthenticationService,
    private modalService: NgbModal,
    private locationStrategy: LocationStrategy,
        private utilityService: UtilityService,
        private router : Router
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);

    //console.log(this.currentUser, " : current USer");


  }
  document:any[] = [];
  data:string;
  modalRef:NgbModalRef;
  fileObj:UploadPolicy = new UploadPolicy();
  alertMessage: any;
  allReadPoliciesList:any[] = [];

  filters:any = {};
  isSearchEnabled:boolean = false;
  policyColumns:any[] = ['blank','policyName','createdByName','createdOn'];
  isDocumentScrolledToBottom:boolean = false;

  async ngOnInit(): Promise<void> {

    // this.getAllNewsletters();
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
          featureMap.subFeatures?.forEach(sub => {
            this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
        });

    this.getAllDocuments();
    this.preventBackButton();
  }

  ngAfterViewInit(): void {
    this.openPreviewPolicyModal();
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
           doc.emp360CreatedBy = doc.createdBy;
        });
        this.getAllReadPolicies();
        //console.log("DocumentList xyz: ", this.document);
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
       //console.log("this.alll : ", response.serviceResponse);
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

  onReadPolicy(template: TemplateRef<any>){
    this.cancelRequest();

    let fileObj = new UploadPolicy();
    fileObj.policyID = this.fileObj.policyID;
    fileObj.empId = this.currentUser.empId;

    this.policiesService.onReadPolicy(fileObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        // this.openAlertMod(template, response.serviceResponse);
        // this.getAllDocuments();
        let dtoResponse = response.serviceResponse;
        this.currentUser.policyReadConsent = dtoResponse.policyReadConsent;
        if (this.currentDoc && this.currentDoc.policyID === fileObj.policyID) {
          this.currentDoc.isRead = true;
        }

        this.isDocumentScrolledToBottom = true;
        //console.log( this.currentUser.policyReadConsent , " :  this.currentUser.policyReadConsent");
        this.authenticationService.setcurrentUserSubject(this.currentUser);
        this.openPreviewPolicyModal();
      }else{
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  previewPolicyDocument(template: TemplateRef<any>,doc: any,event?: MouseEvent) {
    // disable button on click
    if(event) (event.target as HTMLButtonElement).disabled = true;


    this.src = null;
    this.fileName = doc.policyName;
    this.isDocumentScrolledToBottom = false;

    this.currentDoc = doc;

   this.policiesService.downloadDocument( doc.policyID).pipe(first()).subscribe((response:any) => {
      const blob = new Blob([response], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;

      this.src =  a.href;

      if(this.src != null){
        if(event) (event.target as HTMLButtonElement).disabled = false;
        this.openPreviewDocument(template);
      }
    });
  }

  //Modal
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
    this.alertMessage = message;
  }

  openPreviewDocument(template: TemplateRef<any>) {
    this.modalRef = this.modalService.open(template, this.policyModalConfiguration);
    setTimeout(() => {
      if (this.currentDoc.readEnabled == 'false') {
        this.isDocumentScrolledToBottom = true;
      } else if (this.currentDoc.readEnabled == 'true' && !this.currentDoc.isRead) {
        let scrollElement = document.querySelector('.ng2-pdf-viewer-container');
        if (scrollElement) {
          scrollElement.addEventListener("scroll", (event: any) => {
            if (event.target.offsetHeight + event.target.scrollTop >= event.target.scrollHeight) {
              this.isDocumentScrolledToBottom = true;
            } else {
              this.isDocumentScrolledToBottom = false;
            }
          });
        }
      } else {
        this.isDocumentScrolledToBottom = true;
      }
    }, 100)
  }

  cancelRequest() {
    this.modalRef?.close();
  }

  openReadEnabledMod(template: TemplateRef<any>, fileObj:UploadPolicy) {
    this.cancelRequest();
    this.modalRef = this.modalService.open(template, this.readEnambleModalConfig);
    this.fileObj = fileObj;
  }

  openPreviewPolicyModal(){
    if(this.currentUser.policyReadConsent != null){
      //console.log("this.currentUser.policyReadConsent ", this.currentUser.policyReadConsent, " ---");

      this.previewPolicyDocument(this.previewDocument,this.currentUser.policyReadConsent);
    }else{
      this.getAllDocuments();
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
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  onSearch(searchData){
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

  goToGrievanceComponent(category: string, subCategory: string)  {
    this.router.navigate(['/grievance'], {
    queryParams: {
      category: category,
      subcategory: subCategory
    }
  });
}

}
function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);

}
