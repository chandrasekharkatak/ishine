import { Component, EventEmitter, OnInit, Output, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Employee } from 'src/app/models/employee';
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
  uploadedFiles:any[] = [];

  constructor(
    private updateUserInfoService: UpdateUserInfoService,
    private modalService: BsModalService,
  ) { }

  ngOnInit(): void {
    this.currentEmployeeInfo = this.updateUserInfoService.getUserInfoObj();
  }

  onSave(template: TemplateRef<any>){
    if (this.uploadedFiles.length == 0) {
      this.alertMessage = "Kindly Select Documents to Upload !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    this.loadInfoPreview.emit();
  }

  onImageSelect(event:any){
    const imgObj = {file : event.target.files[0], inputName : event.target.id};

    let selectedFile = this.uploadedFiles.find(file => file.inputName == imgObj.inputName);
    if(selectedFile){
      selectedFile.file = imgObj.file; 
    }else{
      this.uploadedFiles.push(imgObj);
    }
    
    console.log("Uploaded Files : ", this.uploadedFiles);
    
  }


  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }


}
