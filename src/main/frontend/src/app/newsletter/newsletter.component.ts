import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { LocationStrategy } from '@angular/common';
import { NewsletterService } from '../services/newsletter.service';
import { Newsletter } from '../models/newsletter';
import { first } from 'rxjs/operators';
import * as moment from 'moment';
import { AppComponent } from '../app.component';
import { saveAs } from "file-saver";
import { Sort } from '@angular/material/sort';

@Component({
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
 
  newsletters:any[] = [];
  allReadNewsletters:any[] = [];
  newsletterObj:Newsletter = new Newsletter();

  src:any;
  fileName:any

  modalRef: BsModalRef = new BsModalRef();
  alertMessage: any;

  filters:any = {};
  isSearchEnabled:boolean = false;
  newsletterColumns:any[] = ['blank','displayName','createdByName','createdOn'];

  newsletterModalConfiguration = {
    backdrop: true,
    ignoreBackdropClick: true,
    keyboard: false,
    class : 'modal-xl'
  }

  constructor(
    private authenticationService: AuthenticationService,
    private modalService: BsModalService,
    private locationStrategy: LocationStrategy,
    private newsletterService : NewsletterService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

   ngOnInit(): void {

    this.getAllNewsletters();
    this.preventBackButton();
  }

  ngAfterViewInit(): void {
    this.openPreviewNewsletterModal();
  }

  preventBackButton(){
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(()=>{
      history.pushState(null, null, location.href);
    })
  }

   getAllNewsletters(){
    this.newsletters = [];
    
    this.newsletterService.getAllNewsletters().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.newsletters =  response.serviceResponse;
        this.newsletters.forEach(doc => {
          doc.createdOn = (doc.createdOn)? moment(doc.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        console.log("Newsletters List : ", this.newsletters);
        this.getAllReadNewsletter();
      } else {
        console.error(response.serviceResponse);
      }
    });
  }


  getAllReadNewsletter(){
    this.allReadNewsletters = [];
    let newsletterObj = new Newsletter();
    newsletterObj.empId = this.currentUser.empId;

    this.newsletterService.getAllReadNewslettersByEmpId(newsletterObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allReadNewsletters = response.serviceResponse;
       console.log("allReadNewsletters : ", response.serviceResponse);

       this.allReadNewsletters.forEach((readNewsletter:Newsletter) => {
          let fileObj = this.newsletters.find((newsletter:Newsletter) => readNewsletter.documentId == newsletter.documentId);
          if(fileObj) fileObj.isRead = true;
        }); 
      }else{
        console.error(response.serviceResponse);
      }
    });
  }

  onReadNewsletter(){
    this.cancelRequest();

    let newsletterObj = new Newsletter();
    newsletterObj.documentId = this.newsletterObj.documentId;
    newsletterObj.empId = this.currentUser.empId;

    this.newsletterService.onReadNewsletter(newsletterObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        let dtoResponse = response.serviceResponse;
        this.currentUser.newsletterReadCheck = dtoResponse.newsletterReadCheck;

        console.log( this.currentUser.newsletterReadCheck , " :  this.currentUser.newsletterReadCheck");
        this.authenticationService.setcurrentUserSubject(this.currentUser);
        this.openPreviewNewsletterModal();
      }

      this.getAllNewsletters()
    });
  }

  
  previewPolicyDocument(template: TemplateRef<any>,doc: any) {   
    this.src = null;
    this.fileName = doc.displayName;

    this.newsletterObj = doc;

    this.newsletterService.downloadDocument(doc.documentId).pipe(first()).subscribe((response:any) => {
      const blob = new Blob([response], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;

      this.src =  a.href;

      if(this.src != null){
        this.openPreviewDocument(template);
      }
    });
  }

  downloadFile(doc: any) {
    this.newsletterService.downloadDocument(doc.documentId).subscribe(blob => saveAs(blob,doc.fileName));
  }


  //Modal
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openPreviewDocument(template: TemplateRef<any>){
    this.modalRef = this.modalService.show(template,this.newsletterModalConfiguration);
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  openPreviewNewsletterModal(){ 
    if(this.currentUser.newsletterReadCheck != null){
      this.previewPolicyDocument(this.previewDocument,this.currentUser.newsletterReadCheck);
    }else{
      this.getAllNewsletters();
    }
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
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  onSearch(searchData){
    this.filters = searchData;
    console.log("Updated Filter : ", this.filters);
  }

}
