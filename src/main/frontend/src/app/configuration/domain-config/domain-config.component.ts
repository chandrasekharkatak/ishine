import { Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Domain } from 'src/app/models/domain';
import { Employee } from 'src/app/models/employee';
import { Feature } from 'src/app/models/feature';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DomainService } from 'src/app/services/domain.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';
import { ValidationService } from 'src/app/services/validation.service';


@Component({
  selector: 'app-domain-config',
  templateUrl: './domain-config.component.html',
  styleUrls: ['./domain-config.component.css']
})
export class DomainConfigComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  feature = 'Domain Config';

  //modal
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  all:any;
  domainToBeDeleted:any;
  userMapping: any = {};

  currentUser: User;
  employeeObj: Employee = new Employee();
  domainObj: Domain = new Domain();

  isDomain: boolean = false;
  isDomainTable: boolean = false;
  isDomainCreation: boolean = false;
  isDomainUpdation: boolean = false;
  isDomainForm: boolean = false;

  allDomainList:any[] = [];
  specializationList:any[] = [];
  allSpecializationList:any[] = [];


  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  filters:any = {};
  isSearchEnabled:boolean = false;
  domainColumns:any[] = ['blank','domainName','createdByName','createdOn']

  SpecializationInput: any =document.getElementById('input1');
  //@ViewChild('myInput', { static: false }) myInput: ElementRef<HTMLInputElement>;

  //excel
  domainDataForExcel: any[];
  name = 'Domain.xlsx';

  constructor(
    private domainService:DomainService,
    public validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private exportExcelService: ExportExcelService,
  ) {this.authenticationService.currentUser.subscribe(x => this.currentUser = x);}

  ngOnInit(): void {

     // Dynamic Subfeature Flags
     let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
     featureMap.subFeatures?.forEach(sub => {
       this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
     });
     console.log(this.feature, this.userMapping);

    this.showAllDomain()
  }

  showAllDomain(){
    this.isDomain = true;
    this.isDomainTable = true;

    this.isDomainCreation = false;
    this.isDomainUpdation = false;
    this.isDomainForm = false;

    this.getAllDomain(this.alertTemplate);
  }

  showCreateDomainForm(){
    this.isDomainCreation = true;
    this.isDomainForm = true;

    this.isDomain = false;
    this.isDomainTable = false;
    this.isDomainUpdation = false;

    this.domainObj = new Domain();
    this.allSpecializationList = [];

    //Template Activity
    if (this.domainObj.allSpecializationList == undefined || this.domainObj.allSpecializationList.length == 0) {
      this.addInputSpecializationField();
    } else {
      this.allSpecializationList = this.domainObj.allSpecializationList;
    }
  }

  showUpdateDomainForm(domain:any){
    this.isDomainForm = true;
    this.isDomainUpdation = true;

    this.isDomainCreation = false;
    this.isDomain = false;
    this.isDomainTable = false;

    this.domainService.getDomainSpecializationByDomainId(domain).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.domainObj = Object.assign({}, response.serviceResponse);

        this.allSpecializationList = this.domainObj.allSpecializationList;

        console.log(response.serviceResponse, " : response.serviceResponse");
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  ValidateInput(targetValue,SpecializationName)
  {
       if(!this.validationService.validateTeamActivity(SpecializationName))
       {
           targetValue.value = '';
       }
  }

  addInputSpecializationField(template?: TemplateRef<any>, currentSpecializationName?:any)
  {
     if(this.allSpecializationList.length!=0)
     {
      console.log("spec: ", currentSpecializationName);
      if(!this.validationService.validateTeamActivity(currentSpecializationName))
      {
        let selectedSpec = this.allSpecializationList.find(currentSpecialization => currentSpecialization.specializationName == currentSpecializationName);
        if(selectedSpec) selectedSpec.specializationName = '';

        //this.SpecializationInput.value = '';

        this.alertMessage = "Please Enter Valid Specialization Name !!"
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
     }
    let domainObj = new Domain();
    this.allSpecializationList.push(domainObj);
    console.log(this.allSpecializationList, " : this.allSpecializationList");
  }

  removeInputSpecializationField(spec:any){
    this.allSpecializationList.forEach((value, index) => {
      if (value == spec) {
        this.allSpecializationList.splice(index, 1);
      }
    });
    console.log(this.allSpecializationList, " :this.allSpecializationList");
  }

  //Doamin & Specialization  :: start

  getAllDomain(template?: TemplateRef<any>){
    this.domainService.getAllDomain().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDomainList = response.serviceResponse;

        this.allDomainList.forEach((domain) => {
          domain.createdOn = (domain.createdOn)? moment(domain.createdOn).format(AppComponent.DATETIME_FORMAT) : null;
        });

        console.log(this.allDomainList, " : this.allDomainList");
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getDomainSpecialization(){
    this.specializationList = [];

    let domainObj = new Domain();
    domainObj.domainIdList = this.employeeObj.domainList;

    console.log(domainObj, " : domainObj selected");
    this.domainService.getDomainSpecialization(domainObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.specializationList = response.serviceResponse;
        this.specializationList = this.specializationList.sort((a, b) => a.specializationName.localeCompare(b.specializationName));
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  validateDomainObj(domainObj, template: TemplateRef<any>) {
    let flag = true;

    domainObj.domainName = domainObj.domainName?.trim();
    if (!this.validationService.validateNullUndefinedEmptyString(domainObj.domainName)) {
      this.alertMessage = "Please enter Domain Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateTeamActivity(domainObj.domainName)) {
      this.alertMessage = "Please enter Valid Domain Name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }


    this.allSpecializationList.forEach((spec, index) => {
      if(!flag) return;

      spec.specializationName = spec.specializationName?.trim();
      if (!this.validationService.validateNullUndefinedEmptyString(spec.specializationName)) {
        this.alertMessage = `Please enter Specialization - ${index + 1}!!`
        flag = false;
        return;
      }
      if (!this.validationService.validateTeamActivity(spec.specializationName)) {
        this.alertMessage = `Please enter valid Specialization - ${index + 1}!!`
        flag = false;
        return;
      }
    });
    if (!flag) {
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    const uniqueSpecialization = new Set(this.allSpecializationList.map(x => x.specializationName));
    if (uniqueSpecialization.size < this.allSpecializationList.length) {
      this.alertMessage = "Duplicate Specialization Name are not allowed !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    return true;
  }

  createDomain(template: TemplateRef<any>){

    let inputValidated: boolean = this.validateDomainObj(this.domainObj, template)
    if (!inputValidated) return;

    this.domainObj.allSpecializationList = this.allSpecializationList;
    this.domainObj.createdBy = this.currentUser.empId;
    this.domainService.createDomain(this.domainObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showAllDomain();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  updateDomain(template: TemplateRef<any>){
    let inputValidated: boolean = this.validateDomainObj(this.domainObj, template)
    if (!inputValidated) return;

    this.domainObj.allSpecializationList = this.allSpecializationList;
    this.domainObj.updatedBy = this.currentUser.empId;

    this.domainService.updateDomain(this.domainObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showAllDomain();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  deleteDomain(template: TemplateRef<any>) {
    this.domainToBeDeleted.updatedBy = this.currentUser.empId;

    this.domainService.deleteDomain(this.domainToBeDeleted).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showAllDomain();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  checkDomainName(domainName:any, template: TemplateRef<any>){

    let domainObj = new Domain();
    domainObj.domainName = domainName;
    domainObj.domainId = this.domainObj.domainId;
    this.domainService.checkDomainName(this.domainObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Fail") {
        this.domainObj.domainName = '';
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  //Doamin & Specialization  :: end


  /* Modal */

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  openDeleteDomainMod(template: TemplateRef<any> , domain: any){
    this.domainToBeDeleted = domain;
    this.modalRef = this.modalService.show(template);
  }

  //pagination
  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  //Export -- download excel
  exportToExcel(): void {
  this.domainService.getAllDomain().pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus == "Success") {
      this.domainDataForExcel = response.serviceResponse;
    }
    const onlySpecificDataArr = this.domainDataForExcel.map(
      x => ({
        "Domain Name": x.domainName,
        "Created by": x.createdByName,
        "Created on": (x.createdOn)? moment(x.createdOn).format(AppComponent.DATETIME_FORMAT) : null,
      })
    )
    this.exportExcelService.exportTableDataToExcel(onlySpecificDataArr, this.name)
  });
}



  //sort & searching
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
    if(this.isSearchEnabled == true){
      this.filters = searchData;
      console.log("Updated Filter : ", this.filters);
    }
  }
}
