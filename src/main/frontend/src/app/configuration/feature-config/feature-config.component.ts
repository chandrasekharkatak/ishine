import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Feature } from 'src/app/models/feature';
import { SubFeature } from 'src/app/models/subFeature';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-feature-config',
  templateUrl: './feature-config.component.html',
  styleUrls: ['./feature-config.component.css']
})
export class FeatureConfigComponent implements OnInit {

   //flags 
   isCreation:boolean = true;
   isUpdation: boolean = false;
   isForm: boolean = true;
   isTable: boolean = false;
 
   //modal 
   alertMessage:any;
   modalRef: BsModalRef = new BsModalRef();

   featureObj:Feature = new Feature();
   subFeatureObj:SubFeature = new SubFeature();
   subFeatureList:any = [this.subFeatureObj];

   allFeatureList:any;
   

  constructor(private validationService:ValidationService,private modalService: BsModalService,) { }

  ngOnInit(): void {
  }

  addInputField() {
    let newSubObj = new SubFeature();                                        
    this.subFeatureList.push(newSubObj);
  }

  removeInputField(subFeature) {
    this.subFeatureList.forEach((value, index) => {
      if (value == subFeature) this.subFeatureList.splice(index, 1);
    });
  }

  showCreateForm(){
    this.isForm = true;
    this.isTable = false;
    this.isCreation = true;
    this.isUpdation = false;

    this.reset();
  }

  showTable(){
    this.isTable = true;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;

    this.getAllFeatureList();
  }

  reset(){
    this.featureObj = new Feature();
    this.subFeatureObj = new SubFeature();

    this.allFeatureList = [];
  }

  showUpdateForm(feature:Feature){
    this.isForm = true;
    this.isTable = false;
    this.isUpdation = true;
    this.isCreation = false;

    this.featureObj = Object.assign({}, feature)
  }

  //CRUD
  onCreateFeature(template: TemplateRef<any>){
    console.log("feature : ", this.featureObj);
    console.log("subfeatures : ", this.subFeatureList);
    
  }

  onUpdateFeature(template: TemplateRef<any>){
    console.log("onUpdateFeature"); 
  }

  getAllFeatureList(){
    console.log("getAllFeatureList"); 
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
