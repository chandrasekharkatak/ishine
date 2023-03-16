import { AfterViewInit, Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { HelpService } from '../services/help.service';
import * as moment from 'moment';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { saveAs } from "file-saver";
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ActivatedRoute, Params } from '@angular/router';
import { Help } from '../models/help';

@Component({
  selector: 'app-helpdesk',
  templateUrl: './helpdesk.component.html',
  styleUrls: ['./helpdesk.component.css']
})
export class HelpdeskComponent implements OnInit, AfterViewInit {

  @ViewChild("preview_document")
  previewDocument: TemplateRef<any>;

  modalRef: BsModalRef = new BsModalRef();
  alertMessage: any;

  document:any[] = [];

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  data: any;
  src:any;
  fileName:any
  currentDocument:any

  filters:any = {};
  isSearchEnabled:boolean = false;
  documentsColumns:any[] = ['blank','fileName','helpDocumentName','createdByName','createdOn'];


  constructor(
    private helpService: HelpService,
    private sanitizer: DomSanitizer,
    private modalService: BsModalService,
    private route: ActivatedRoute,
  ) { }


  ngAfterViewInit(): void {
     this.sectionViewInit();
  }


  ngOnInit(): void {
    this.getAllHelpDocument();

    this.route.params.subscribe((params:Params) => {
      this.currentDocument = params['id'];
    });

  }

  sectionViewInit(){
    if(this.currentDocument != undefined && this.currentDocument != null){
      let helpObj = new Help();
      helpObj.helpDocId = this.currentDocument;
      this.previewHelpDocument(this.previewDocument, helpObj);
    }
  }

  getAllHelpDocument(){
    this.data='';
    this.document = [];
    this.helpService.getAllHelpDocument().pipe(first()).subscribe((response:any) => {
      if (response.serviceStatus == "Success") {
        this.document =  response.serviceResponse;
        this.document.forEach(doc => {
          doc.createdOn = (doc.createdOn)? moment(doc.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });
        console.log("DocumentList : ", this.document);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  downloadFile(doc: any) {
    this.helpService.downloadHelpDocument(doc.helpDocId).subscribe(blob => saveAs(blob,doc.fileName));
  }

  previewHelpDocument(template: TemplateRef<any>,doc: any) {
    this.src = null;
    this.fileName = "HelpDocument";

    this.helpService.downloadHelpDocument(doc.helpDocId).pipe(first()).subscribe((response:any) => {
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

  // Modal

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openPreviewDocument(template: TemplateRef<any>){
    this.modalRef = this.modalService.show(template, { class: 'modal-xl' });
  }

  cancelRequest() {
    this.modalRef.hide();
  }
}
