import { Component, OnDestroy, OnInit, Pipe, TemplateRef, ViewChild } from '@angular/core';
import { Employee } from 'src/app/models/employee';
import { EmployeeService } from 'src/app/services/employee.service';
import { ValidationService } from 'src/app/services/validation.service';
import { first } from 'rxjs/operators';
import { DatePipe } from '@angular/common';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

@Component({
  selector: 'app-employee-config',
  templateUrl: './employee-config.component.html',
  styleUrls: ['./employee-config.component.css']
})
export class EmployeeConfigComponent implements OnInit {

  //flags 
  isCreation:boolean = true;
  isUpdation: boolean = false;
  isForm: boolean = true;
  isTable: boolean = false;

  isDraft: boolean = false;
  isDraftTable: boolean = false;

  //modal 
  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  //Obj 
  employeeObj:Employee = new Employee();
  allEmployeeList:any;
  managerList:any = [];

  constructor(private employeeService:EmployeeService,private validationService:ValidationService, 
    private datePipe: DatePipe,private modalService: BsModalService,) { }
 
  ngOnInit(): void {
    // TODO : Here we should fetch list of Managers
    this.getAllEmployeeList(); //for manager dropdown 

    //Deafult values for dropdown
    this.employeeObj.gender = '';
    this.employeeObj.maritalStatus = '';
    // this.employeeObj.managerId = '';
  }

  ngAfterViewInit() {
    this.setCalenderMaxDate();
  }

  setCalenderMaxDate(){
    const today = this.datePipe.transform(new Date(), 'yyyy-MM-dd');
    let DOB = document.getElementById('DOB');
    let DOJ = document.getElementById('DOJ');
    DOB.setAttribute('max', today);
    DOJ.setAttribute('max', today);
  }

  showCreateForm(){
    this.isForm = true;
    this.isTable = false;
    this.isCreation = true;
    this.isUpdation = false;
    this.isDraft=false;
    this.isDraftTable = false;

    this.reset();
  }

  showTable(){
    this.isTable = true;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraft = false;
    this.isDraftTable = false;

    this.managerList = [];
    this.getAllEmployeeList();
  }

  showDraftTable(){
    this.isDraftTable = true;
    this.isTable = false;
    this.isForm = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraft = false;
    
    this.getAllDraftEmployees();
  }

  reset(){
    this.employeeObj = new Employee();
    //Deafult values for dropdown
    this.employeeObj.gender = '';
    this.employeeObj.maritalStatus = '';
    this.employeeObj.managerId = '';

    this.allEmployeeList = [];
  }

  showUpdateForm(employee:Employee){
    this.isForm = true;
    this.isTable = false;
    this.isUpdation = true;
    this.isCreation = false;
    this.isDraft= false;
    this.isDraftTable = false;

    this.employeeObj = Object.assign({}, employee);
    this.employeeObj.dateOfBirth = this.datePipe.transform(employee.dateOfBirth.replaceAll('/', '-'), 'yyyy-MM-dd');
    this.employeeObj.dateOfJoining = this.datePipe.transform(employee.dateOfJoining.replaceAll('/', '-'), 'yyyy-MM-dd')
  }

  showUpdateDraftForm(employee:Employee){
    this.isDraft= true;
    this.isForm = false;
    this.isTable = false;
    this.isUpdation = false;
    this.isCreation = false;
    this.isDraftTable = false;
    
    this.employeeObj = Object.assign({}, employee);
    this.employeeObj.dateOfBirth = this.datePipe.transform(employee.dateOfBirth, 'yyyy-MM-dd');
    this.employeeObj.dateOfJoining = this.datePipe.transform(employee.dateOfJoining, 'yyyy-MM-dd')
  }

  validateEmployeeObj(employeeObj:Employee, template: TemplateRef<any>){

    // Non-Mandatory fields : passportNumber, landline, emergencyContactPerson, relation, emergencyContactMobile, Educational & Banking info
    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.name)){
      this.alertMessage = "Please enter Full Name !!";
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.email)){
      this.alertMessage = "Please enter email id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateEmail(employeeObj.email)){
      this.alertMessage = "Please enter valid email id !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.gender)){
      this.alertMessage = "Please select gender !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfBirth)){
      this.alertMessage = "Please enter date of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.bloodGroup)){
      this.alertMessage = "Please enter blood group !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.maritalStatus)){
      this.alertMessage = "Please enter select martial status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.fatherName)){
      this.alertMessage = "Please enter father name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.placeOfBirth)){
      this.alertMessage = "Please enter place of birth !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.motherTongue)){
      this.alertMessage = "Please enter mother tongue !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.aadhar)){
      this.alertMessage = "Please enter aadhar card number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.panNumber)){
      this.alertMessage = "Please enter PAN card number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.mobileNo)){
      this.alertMessage = "Please enter mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateMobileNumber(employeeObj.mobileNo)){
      this.alertMessage = "Please enter valid mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } 

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.address)){
      this.alertMessage = "Please enter address !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.state)){
      this.alertMessage = "Please enter state !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.city)){
      this.alertMessage = "Please enter city !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.country)){
      this.alertMessage = "Please enter country !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.pincode)){
      this.alertMessage = "Please enter pincode !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.officialMobileNo)){
      this.alertMessage = "Please enter official mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }else if(!this.validationService.validateMobileNumber(employeeObj.officialMobileNo)){
      this.alertMessage = "Please enter valid official mobile number !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    } 

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.permanentAddress)){
      this.alertMessage = "Please enter permanent address !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.dateOfJoining)){
      this.alertMessage = "Please enter date of joining !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    
    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.employmentstatus)){
      this.alertMessage = "Please enter employment status !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.noticePeriod)){
      this.alertMessage = "Please enter notice period !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(!this.validationService.validateNullUndefinedEmptyString(employeeObj.managerId)){
      this.alertMessage = "Please select manager !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  // CRUD 
  onCreateEmployee(template: TemplateRef<any>){
    this.employeeObj.isDraft = false;
    // transform date formats to dd-MM-yyyy
    this.employeeObj.dateOfBirth = this.datePipe.transform(this.employeeObj.dateOfBirth, 'dd-MM-yyyy');
    this.employeeObj.dateOfJoining = this.datePipe.transform(this.employeeObj.dateOfJoining, 'dd-MM-yyyy')

    let inputValidated:boolean  = this.validateEmployeeObj(this.employeeObj, template)
    if(!inputValidated) return;
    
    this.employeeObj.createdBy = 1;
    console.log("Create Employe : ", this.employeeObj);
    this.employeeService.createEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeService.deleteEmployee(this.employeeObj); // deleting draft once employee is created
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }

    });
  }

  onUpdateEmployee(template: TemplateRef<any>){
    this.employeeObj.isDraft = false;
    // transform date formats to dd-MM-yyyy
    this.employeeObj.dateOfBirth = this.datePipe.transform(this.employeeObj.dateOfBirth, 'dd-MM-yyyy');
    this.employeeObj.dateOfJoining = this.datePipe.transform(this.employeeObj.dateOfJoining, 'dd-MM-yyyy');

    let inputValidated:boolean  = this.validateEmployeeObj(this.employeeObj, template)
    if(!inputValidated) return;
    
    this.employeeObj.updatedBy = 1;
    console.log("Update Employe : ", this.employeeObj);
    this.employeeService.updateEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDeleteEmployee(template: TemplateRef<any>){
    this.cancelRequest();
  
    this.employeeService.deleteEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllEmployeeList(){
    this.allEmployeeList = [];

    this.employeeService.getAllEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        console.log("allEmployeeList : ", this.allEmployeeList)
        this.createEmployeeList(this.allEmployeeList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  createEmployeeList(allEmployeeList:any){
    this.managerList = allEmployeeList.map(employee => {
      let emp = {name : employee.name, empId: employee.empId.toString()};
      return emp;
    });
    console.log("managerList : ", this.managerList);
    this.employeeService.updatedEmployeeList.next(this.managerList);
  }

  /* Employee Draft */
  onSaveDraftEmployee(template: TemplateRef<any>){
    this.employeeObj.isDraft = true;
    // transform date formats to dd-MM-yyyy
    this.employeeObj.dateOfBirth = this.datePipe.transform(this.employeeObj.dateOfBirth, 'dd-MM-yyyy');
    this.employeeObj.dateOfJoining = this.datePipe.transform(this.employeeObj.dateOfJoining, 'dd-MM-yyyy')

    this.employeeObj.createdBy = 1;
    console.log("Create Employe Draft : ", this.employeeObj);
    this.employeeService.createDraftEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showDraftTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onUpdateDraftEmployee(template:TemplateRef<any>){
    this.employeeObj.isDraft = true;
    // transform date formats to dd-MM-yyyy
    this.employeeObj.dateOfBirth = this.datePipe.transform(this.employeeObj.dateOfBirth, 'dd-MM-yyyy');
    this.employeeObj.dateOfJoining = this.datePipe.transform(this.employeeObj.dateOfJoining, 'dd-MM-yyyy')

    this.employeeObj.updatedBy = 1;
    console.log("Update Employe Draft : ", this.employeeObj);
    this.employeeService.updateDraftEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showDraftTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  onDeleteDraftEmployee(template: TemplateRef<any>){
    this.cancelRequest();
  
    this.employeeService.deleteDraftEmployee(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showDraftTable();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  getAllDraftEmployees(){
    this.allEmployeeList = [];

    this.employeeService.getAllDraftEmployees().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        console.log("allDraftEmployeeList : ", this.allEmployeeList)
      } else {
        alert(response.serviceResponse)
      }
    });
  }

  // modals
  openDeleteEmployee(template: TemplateRef<any>, employee: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.employeeObj = employee;
  }

  openDeleteDraftEmployee(template: TemplateRef<any>, employee: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.employeeObj = employee;
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

}
