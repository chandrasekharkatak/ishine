import { Component, OnInit, TemplateRef } from '@angular/core';
import { DepartmentService } from 'src/app/services/department.service';
import { first, map, startWith } from 'rxjs/operators';
import { Employee } from 'src/app/models/employee';
import { EmployeeService } from 'src/app/services/employee.service';
import { ProjectService } from 'src/app/services/project.service';
import { Project } from 'src/app/models/project';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { ValidationService } from 'src/app/services/validation.service';
import { Department } from 'src/app/models/department';

@Component({
  selector: 'app-project-config',
  templateUrl: './project-config.component.html',
  styleUrls: ['./project-config.component.css']
})
export class ProjectConfigComponent implements OnInit {

  employeeObj: Employee = new Employee();
  projectObj: Project = new Project();

  isCreateForm:boolean = false;
  isTable:boolean = false;
  isCreation:boolean = false;
  isUpdation:boolean = false;

  allDeptList: any[] = [];
  managerList: any[] = [];
  allClientList: any[] = [];
  allProjects: any[] = [];
  allClientLocationList: any[] = [];
  updatedClientLocationList: any[] = [];
  clientLocationList: any[] = [];
  filteredClientList: any[] = [];

  alertMessage:any;
  modalRef: BsModalRef = new BsModalRef();

  allStates: any[] = [
    "Andaman & Nicobar Islands",
    "Andhra Pradesh",
    "Arunachal Pradesh",
    "Assam",
    "Bihar",
    "Chandigarh",
    "Chhattisgarh",
    "Dadra and Nagar Haveli and  Daman & Diu",
    "Delhi",
    "Goa",
    "Gujarat",
    "Haryana",
    "Himachal Pradesh",
    "Jammu & Kashmir",
    "Jharkhand",
    "Karnataka",
    "Kerala",
    "Ladakh",
    "Lakshadweep",
    "Madhya Pradesh",
    "Maharashtra",
    "Manipur",
    "Meghalaya",
    "Mizoram",
    "Nagaland",
    "Odisha",
    "Puducherry",
    "Punjab",
    "Rajasthan",
    "Sikkim",
    "Tamil Nadu",
    "Telangana",
    "Tripura",
    "Uttar Pradesh",
    "Uttarakhand",
    "West Bengal",
  ];

  data:any;

  constructor(
    private departmentService: DepartmentService,
    private employeeService: EmployeeService,
    private projectService: ProjectService,
    private modalService: BsModalService,
    public validationService: ValidationService,
  ) { }

  ngOnInit(): void {
    this.sectionViewInit();
  }

  sectionViewInit(){
    this.showTable();

    //client Location
    // if (this.projectObj.clientLocation == undefined || this.projectObj.clientLocation == 0) {
    //   this.addInputClientLocationField();
    // } else {
    //   this.allClientLocationList = this.projectObj.clientLocation;
    // }
  }

  showCreateForm(){
    this.isCreateForm = true;
    this.isCreation = true;

    this.isTable = false;
    this.projectObj = new Project();
    this.getAllDepartmentList();
    this.getManagerList();
    this.getAllClientList();
  }

  showUpdateForm(project:Project){
    this.isUpdation = true;

    this.isCreateForm = true;
    this.isCreation = false;
    this.isTable = false;
    this.getAllDepartmentList();
    this.getManagerList();
    this.getAllClientList();

    this.allClientLocationList = [];
    this.filteredClientList = [];
    this.projectObj.departmentName = null;

    this.projectObj.projectId = project.projectId;
    this.projectService.getProjectByProjectId(this.projectObj).pipe(first()).subscribe((response: any) => {
      if(response.serviceStatus == "Success") {
        this.projectObj = Object.assign({}, response.serviceResponse)[0];
        this.getClientLocationList(this.projectObj.clientId);
        if(this.projectObj.syncProject == "true"){
          this.projectObj.syncProject = true;
        }else{
          this.projectObj.syncProject = false;
        }

        console.log(this.projectObj, " this.projectObj");

        //client Location
        // if (this.projectObj.allClientLocationList == undefined || this.projectObj.allClientLocationList == 0) {
        //   this.addInputClientLocationField();
        // } else {
        //   this.allClientLocationList = this.projectObj.allClientLocationList;
        // }
        // console.log(this.projectObj.allClientLocationList, " : this.projectObj.allClientLocationList");
        this.projectObj.departmentName = this.projectObj.departmentList;
        
        
        
      }else {
        console.log(response.serviceResponse);
      }
    });
  }

  showTable(){
    this.isTable = true;
    this.page = 1;
    
    this.isCreateForm = false;
    this.isCreation = false;
    this.getAllProjects();
  }

  openDeleteProject(project: any,template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.projectObj = new Project();
    this.projectObj.projectId = project.projectId;
  }

  toggleSync(event){
    if(event.target.checked){
      this.projectObj.syncProject = true;
    }else{
      this.projectObj.syncProject = false;
    }
  }

  // Manage ClientLocation
  // addInputClientLocationField() {
  //   let newProjectObj = new Project();
  //   this.allClientLocationList.push(newProjectObj);
  //   console.log(this.allClientLocationList, " : this.allClientLocation");
    
  // }

  // removeInputClientLocationField(clientLocationObj) {
  //   this.allClientLocationList.forEach((value, index) => {
  //     if (value == clientLocationObj) {
  //       this.updatedClientLocationList.push(value);
  //       this.allClientLocationList.splice(index, 1);
  //     }
  //   });
  //   console.log(this.updatedClientLocationList, " :this.updatedCLientLocationList");
  // }

  getAllDepartmentList() {
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getManagerList() {
    this.managerList = [];

    this.employeeObj.role = "Manager";	
    this.employeeObj.employeementId = this.employeeObj.employeementId?.substring(2)
    this.employeeService.getAllEmployeesByRole(this.employeeObj).pipe(first()).subscribe((response: any) => {	
      if (response.serviceStatus == "Success") {	
        this.managerList = response.serviceResponse;
        console.log("managerList : ", this.managerList);
      } else {	
        console.error(response.serviceResponse)	
      }	
    });	
  }

  getAllClientList() {
    this.allClientList = [];

    this.projectService.getAllClients().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allClientList = response.serviceResponse;

        //remove duplicate clients
        this.filteredClientList = this.allClientList.filter((value, index, self) =>
          index === self.findIndex((t) => (
            t.clientId === value.clientId
          ))
        )

        console.log(this.filteredClientList, " : this.filteredClientList");
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getClientLocationList(clientId: any){
    this.clientLocationList = [];
    this.allClientLocationList = [];
    // this.addInputClientLocationField();

    const key = "clientLocationId";
    this.clientLocationList = [...new Map(this.allClientList.map((project: Project) => [project[key], project])).values()].filter((project: Project) => {
      if (project.clientId == clientId) {
        return { clientLocationId: project.clientLocationId, clientLocation: project.clientLocation }
      }
    });
    console.log("clientLocationList :", this.clientLocationList);
  }

  getAllProjects(){
    this.data = ''
    this.allProjects = [];

    this.projectService.getAllProjects().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allProjects = response.serviceResponse;

        //remove duplicate clients
        this.allProjects = this.allProjects.filter((value, index, self) =>
          index === self.findIndex((t) => (
            t.projectId === value.projectId
          ))
        )

        console.log(this.allProjects, " : this.allProjects");
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  validateProjectObj(projectObj: Project, template: TemplateRef<any>){
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.projectName)) {
      this.alertMessage = "Please enter Project name !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.clientId)) {
      this.alertMessage = "Please select a client !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.departmentName)) {
      this.alertMessage = "Please select department !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.projectManagerId)) {
      this.alertMessage = "Please select project manager !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectObj.state)) {
      this.alertMessage = "Please select state !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    return true;
  }

  checkProjectName(template:TemplateRef<any>){
    let projectObj = new Project();
    projectObj.projectName = this.projectObj.projectName?.trim();

    if(projectObj.projectName.length >= 5){
      if (!this.validationService.validateProjectName(projectObj.projectName)) {
        this.alertMessage = "Please enter valid Project Name !!"
        this.projectObj.projectName = ''
        this.openAlertMod(template, this.alertMessage);
        return false;
      }

    }else{
      this.alertMessage = "Please enter more than 4 letters in Project Name !!"
      this.projectObj.projectName = ''
      this.openAlertMod(template, this.alertMessage);
      return false;
    }
    console.log("---_________  ",this.projectObj);
    this.projectService.checkProjectName(projectObj).pipe(first()).subscribe((response :any)=>{
      if(response.serviceStatus == "Fail"){
        this.openAlertMod(template, response.serviceResponse);
      }
    })

  }

  createProject(template: TemplateRef<any>){
    let inputValidated: boolean = this.validateProjectObj(this.projectObj, template)
    if (!inputValidated) return;

    this.projectObj.departmentList = this.projectObj.departmentName;
    // this.projectObj.clientLocation = this.allClientLocationList.map((location) => {
    //   return location['clientLocationId'];
    // });
    this.projectObj.departmentName = null;
    this.projectObj.projectName = this.projectObj.projectName?.trim();
    
    this.projectService.createProject(this.projectObj).pipe(first()).subscribe((response: any) => {
      if(response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      }else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  updateProject(template: TemplateRef<any>){
    let inputValidated: boolean = this.validateProjectObj(this.projectObj, template)
    if (!inputValidated) return;

    this.projectObj.departmentList = this.projectObj.departmentName;
    this.projectObj.departmentName = null;

    this.projectService.updateProject(this.projectObj).pipe(first()).subscribe((response: any) => {
      if(response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      }else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  deleteProject(template: TemplateRef<any>){
    this.cancelRequest();
    this.projectService.deleteProject(this.projectObj).pipe(first()).subscribe((response: any) => {
      if(response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.showTable();
      }else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  exportToExcel(){

  }

  //pagination
  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  //modal
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

}
