import { Component, EventEmitter, OnInit, Output, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
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
  files:any[] = [];

  constructor(
    private updateUserInfoService: UpdateUserInfoService,
    private modalService: BsModalService,
    private imageService : ImageService,
  ) { }

  ngOnInit(): void {
    this.currentEmployeeInfo = this.updateUserInfoService.getUserInfoObj();
  }

  onSave(template: TemplateRef<any>){
    if (this.files.length == 0) {
      this.alertMessage = "Kindly Select Documents to Upload !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    const formData = new FormData();
    this.files.forEach((file) =>{
      formData.append(`image`, file.image, file.imageName);
      formData.append("inputName", file.inputName);  
    });
    formData.append("uploadedBy", this.currentEmployeeInfo.empId);

    console.log("Upload Images : ", formData);
    this.imageService.uploadEmployeeDocument(formData).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == 'Success') {
        this.openAlertMod(template, response.serviceResponse);
        this.loadInfoPreview.emit();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onImageSelect(event:any){
    const image = event.target.files[0];
    const imageName = image.name;
    const inputName = event.target.id;

    const imgObj = {image : image, imageName: imageName, inputName : inputName};

    let selectedFile = this.files.find(file => file.inputName == imgObj.inputName);
    if(selectedFile){
      selectedFile.file = imgObj.image; 
    }else{
      this.files.push(imgObj);
    }
    
    console.log("Selected Files : ", this.files);
    
  }


  // Modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }


}
