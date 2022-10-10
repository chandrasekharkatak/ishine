import { Component, EventEmitter, OnInit, Output, SecurityContext, TemplateRef } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Document } from 'src/app/models/document';
import { Employee } from 'src/app/models/employee';
import { ImageService } from 'src/app/services/image.service';
import { UpdateUserInfoService } from 'src/app/services/updateUserInfo.service';

@Component({
  selector: 'app-document-upload',
  templateUrl: './document-upload.component.html',
  styleUrls: ['./document-upload.component.css']
})
export class DocumentUploadComponent implements OnInit {

  @Output() loadInfoPreview: EventEmitter<any> = new EventEmitter<any>();

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  currentEmployeeInfo:Employee = new Employee();
  documentList:Document[] = [];
  files:any[] = [];

  fileType:any;



  constructor(
    private updateUserInfoService: UpdateUserInfoService,
    private modalService: BsModalService,
    private imageService : ImageService,
    private sanitizer: DomSanitizer,
  ) {

  }

  ngOnInit(): void {
    this.currentEmployeeInfo = this.updateUserInfoService.getUserInfoObj();
    console.log("currentEmployeeInfo in Document Upload => ", this.currentEmployeeInfo);
    
    if(this.currentEmployeeInfo){
      this.setDocumentList()
    }
  }

  async setDocumentList() {
    this.documentList = [];
    let employeeObj = new Employee();

    if(this.currentEmployeeInfo.draftEmpId){
      employeeObj.empId = this.currentEmployeeInfo.draftEmpId;
      employeeObj.isDraft = true;
    }else{
      employeeObj.empId = this.currentEmployeeInfo.empId;
      employeeObj.isDraft = false;
    }

    let response: any = await this.imageService.getEmployeeDocuments(employeeObj).toPromise();
    if (response.serviceStatus == 'Success') {
      this.documentList = response.serviceResponse;
    } else {
      console.log(response.serviceResponse);
    }

    if (employeeObj.isDraft && this.documentList.length == 0) {
      // Draft does not have any documents saved, hence fetching documents for employee
      employeeObj.empId = this.updateUserInfoService.currentUser.empId;
      employeeObj.isDraft = false;

      response = await this.imageService.getEmployeeDocuments(employeeObj).toPromise();
      if (response.serviceStatus == 'Success') {
        this.documentList = response.serviceResponse;
      } else {
        console.log(response.serviceResponse);
      }
    }

    console.log("documentList : ", this.documentList);

    if (!this.documentList.find(doc => doc.documentType == 'Aadhar Card')) this.addAdharCard();
    if (!this.documentList.find(doc => doc.documentType == 'Pan Card')) this.addPanCard();
    if (this.currentEmployeeInfo.pursuing == 'No' && !this.documentList.find(doc => doc.documentType == 'Recent Passing Certificate')) this.addPassingCertificate();
    if (this.currentEmployeeInfo.experience == 'Exprienced' && !this.documentList.find(doc => doc.documentType == 'Experience Letter')) this.addExperienceLetter();


    if (this.currentEmployeeInfo.certifications) {
      this.currentEmployeeInfo.certifications.forEach((certification, index) => {
        if (!this.documentList.find(doc => doc.documentType == `Certification ${index + 1}`)) this.addCertification(`Certification ${index + 1}`);
      });
    }

    this.documentList.forEach((doc, index) => {
      if (doc.documentBytes) {
        doc.uploadStatus = "Completed";
        setTimeout(() => {
          let preview = document.getElementById(`docPreview${index + 1}`);
          let objectURL = 'data:image/*;base64,' + doc.documentBytes;
          let src: string = this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.sanitizer.bypassSecurityTrustResourceUrl(objectURL));
          preview.setAttribute('src', src);
        }, 500);
      }
    });
  }

  addAdharCard(){
    this.documentList.push(new Document('Aadhar Card'));
  }

  addPanCard(){
    this.documentList.push(new Document('Pan Card'));
  }

  addPassingCertificate(){
    this.documentList.push(new Document('Recent Passing Certificate'));
  }

  addExperienceLetter(){
    this.documentList.push(new Document('Experience Letter'));
  }

  addCertification(certificationName:string){
    this.documentList.push(new Document(certificationName)); 
  }

  onSave(template: TemplateRef<any>){
    console.log("Selected Files : ", this.files);
    console.log("documentList : ", this.documentList);

    this.currentEmployeeInfo.documentList = this.documentList;
    this.updateUserInfoService.setUserInfoObj(this.currentEmployeeInfo);
    
    if (this.documentList.filter(doc => doc.documentName != null).length == 0) {
      this.alertMessage = "Kindly Select Images !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    this.documentList = this.documentList.filter(doc => doc.documentName != null);
    
    let employeeObj = new Employee();
    employeeObj.employeementId = this.currentEmployeeInfo.employeementId;
    employeeObj.empId = this.currentEmployeeInfo.draftEmpId;
    employeeObj.documentList = this.documentList;
    employeeObj.isDraft = true;

    console.log("Save Documents : ", employeeObj);
    this.imageService.saveEmployeeDocuments(employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == 'Success') {
        this.openAlertMod(template, response.serviceResponse);
        this.loadInfoPreview.emit();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onImageSelect(event:any, index:any, documentObj:Document, template: TemplateRef<any>){
    const extensionRE = /(?:\.([^.]+))?$/;

    const image = event.target.files[0];
    const imageName = documentObj.documentType.replaceAll(" ", "-")+"_"+moment(new Date()).format("DD-MM-YYYY-hh-mm-ss")+"."+extensionRE.exec(image.name)[1];
    const inputName = event.target.id;

    const imgObj = {image : image, imageName: imageName, inputName : inputName};

    let selectedFile = this.files.find(file => file.inputName == imgObj.inputName);
    if(selectedFile){
      selectedFile.file = imgObj.image; 
    }else{
      this.files.push(imgObj);
    }

    let doc = this.documentList.find(doc => doc.documentType == documentObj.documentType);
    if(doc) {
      doc.uploadStatus = "Completed";
      doc.documentName = imageName;
    }
    

    const formData = new FormData();
    formData.append(`image`, image, imageName);
    formData.append("uploadedBy", this.currentEmployeeInfo.draftEmpId);
    formData.append("employeementId", this.currentEmployeeInfo.employeementId);
    formData.append("empId", this.currentEmployeeInfo.draftEmpId);

    this.imageService.uploadEmployeeDocument(formData).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == 'Success') {
        let preview = document.getElementById(`docPreview${index + 1}`);
        if (image) {
          preview?.setAttribute('src', URL.createObjectURL(image));
        }
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


  // Modals
  openDocumentUploadMod(template: TemplateRef<any>, fileType:any) {
    console.log("fileType : ", fileType);
    this.fileType = fileType;
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }


}
