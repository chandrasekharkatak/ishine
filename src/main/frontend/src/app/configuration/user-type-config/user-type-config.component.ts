import { DatePipe } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { CrudMapping } from 'src/app/models/crudMapping';
import { UserType } from 'src/app/models/userType';
import { UsertypeService } from 'src/app/services/usertype.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-user-type-config',
  templateUrl: './user-type-config.component.html',
  styleUrls: ['./user-type-config.component.css']
})
export class UserTypeConfigComponent implements OnInit {

   //flags 
   isCreation:boolean = true;
   isUpdation: boolean = false;
   isForm: boolean = true;
   isTable: boolean = false;
 
   //modal 
   alertMessage:any;
   modalRef: BsModalRef = new BsModalRef();

   userTypeObj: UserType = new UserType();
   crudMappingObj: CrudMapping = new CrudMapping();
   allUserTypes:any;
   allCrudMappings:any;

  constructor(private validationService:ValidationService,private modalService: BsModalService,
    private usertypeService:UsertypeService,  private datePipe: DatePipe,) { }

  ngOnInit(): void {
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

    this.getAllUserTypes();
    this.getAllCRUDMapping();
  }

  reset(){
    this.userTypeObj = new UserType();
    this.crudMappingObj = new CrudMapping();
    this.allUserTypes = [];
    this.allCrudMappings = [];
  }

  showUpdateForm(usertype:UserType){
    this.isForm = true;
    this.isTable = false;
    this.isUpdation = true;
    this.isCreation = false;

    let userCrudMapping = this.allCrudMappings.filter(curdMap => {
      if(usertype.userTypeId == curdMap.userTypeId){
        curdMap.moduleVisibilty =(JSON.parse(curdMap.moduleVisibilty) != null) ? JSON.parse(curdMap.moduleVisibilty) : false;
        curdMap.creation =(JSON.parse(curdMap.creation) != null) ? JSON.parse(curdMap.creation) : false;
        curdMap.edit =(JSON.parse(curdMap.edit) != null) ? JSON.parse(curdMap.edit) : false;
        curdMap.deletion =(JSON.parse(curdMap.deletion) != null) ? JSON.parse(curdMap.deletion) : false;
        curdMap.view =(JSON.parse(curdMap.view) != null) ? JSON.parse(curdMap.view) : false;
        return curdMap
      } 
    });

    this.userTypeObj.userType = usertype.userType;
    this.userTypeObj.userCrudMapping = userCrudMapping;

    console.log("userTypeObj : ", this.userTypeObj);
    
  }

  onCreateUsertype(template: TemplateRef<any>){
    
    if(!this.validationService.validateNullUndefinedEmptyString(this.userTypeObj.userType)){
      this.alertMessage = "Please enter user type !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    this.userTypeObj.createdBy = 1;
    console.log("userTypeObj :", this.userTypeObj);
    this.usertypeService.createUserType(this.userTypeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onUpdateUsertypeCrudMapping(userMap:CrudMapping ,template: TemplateRef<any>){
    userMap.updatedBy = 1;
    console.log("update userMap :", userMap);
    this.usertypeService.updateCrudMappingsByMapId(userMap).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllUserTypes(){
    this.allUserTypes = [];

    this.usertypeService.getAllUserTypes().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allUserTypes = response.serviceResponse;
        console.log("allUserTypes : ", this.allUserTypes)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  getAllCRUDMapping(){
    this.allCrudMappings = [];

    this.usertypeService.getAllUserTypeCrudMappings().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allCrudMappings = response.serviceResponse;
        console.log("allCrudMappings : ", this.allCrudMappings)
      } else {
        alert(response.serviceResponse)
      }
    });
  }


  openDeleteUserType(template: TemplateRef<any>, usertype: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.userTypeObj = usertype;
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

}
