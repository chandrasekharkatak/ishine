import { Component, OnInit, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { Feature } from 'src/app/models/feature';
import { Project } from 'src/app/models/project';
import { first } from 'rxjs/operators';
import { User } from 'src/app/models/user';
import { DepartmentService } from 'src/app/services/department.service';
import { Team } from 'src/app/models/team';
import { Employee } from 'src/app/models/employee';
import { EmployeeService } from 'src/app/services/employee.service';
import { Department } from 'src/app/models/department';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { TeamMember } from 'src/app/models/teamMember';
import { ResourceManagementService } from 'src/app/services/resource-management.service';
import { AuthenticationService } from 'src/app/services/authentication.service';

@Component({
  selector: 'app-resource-management',
  templateUrl: './resource-management.component.html',
  styleUrls: ['./resource-management.component.css']
})
export class ResourceManagementComponent implements OnInit {

  data: string;
  currentUser: User;
  feature = "Team Config";
  userMapping: any = {};

  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  projectObj:Project = new Project();
  teamObj:Team = new Team();
  employeeObj: Employee = new Employee();
  newteamMember:TeamMember = new TeamMember();

  isProjectTable:boolean = false;
  isEditProject:boolean = false;
  isUpdation:boolean = false;
  isHideButton:boolean = false;

  currentTeam:any;

  allProjectList:any[] = [];
  allDeptList:any[] = [];
  filteredDeptList:any[] = [];
  teamLeadsList:any[] = [];
  employeeListByDept: Employee[] = [];
  allTeamMembers: any[] = [];
  allTeamList: any[] = [];
  updatedTeamList: any[] = [];
  teamCreatedProjectList: any[] = [];
  previewTeamList: any[] = [];

  employeeRole: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin'];

  constructor(
    private departmentService: DepartmentService,
    private employeeService: EmployeeService,
    private modalService: BsModalService,
    private resourceManagementService: ResourceManagementService,
    private authenticationService: AuthenticationService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }


  ngOnInit(): void {

     // Dynamic Subfeature Flags 
    //  let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    //  featureMap.subFeatures?.forEach(sub => {
    //    this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    //  });
    //  console.log(this.feature, this.userMapping);

    this.sectionViewInit();
  }

  sectionViewInit(){
    this.showViewProjects();
  }

  showViewProjects(){
    this.isProjectTable = true;
    
    this.isEditProject = false;
    this.alreadyCreatedTeam();
  }

  showEditProjectForm(project: any){
    this.isEditProject = true;

    this.isProjectTable = true;
    this.isHideButton = true;
    
    this.allTeamList = [];
    this.projectObj = Object.assign({}, project);
    this.getAllDepartmentList(project);
    this.getTeamListByProjectName(project);
  }

  closeEditProject(){
    this.isHideButton = false;
  }

  getAllProjects(){
    fetch('https://poportal.apmosys.com/PoPortal/project/fixedCost/getAllProjects').then(res => res.json()).then(data => {
      const _projectList = data;

      if((_projectList != null || _projectList != undefined) && (this.teamCreatedProjectList != null || this.teamCreatedProjectList != undefined)){
        _projectList.forEach((proj) => {
          this.teamCreatedProjectList.forEach((projTeam) => {
            if(proj.name == projTeam.name){
              proj.isTeamCreated = true;
            }
          })
        });
      }

      this.allProjectList = _projectList;
      console.log(this.allProjectList, " : this.allProjectList");
    });
  }

  alreadyCreatedTeam(){
    this.resourceManagementService.alreadyCreatedTeam().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.teamCreatedProjectList = response.serviceResponse;
        this.getAllProjects();
        console.log(this.teamCreatedProjectList, " this.teamCreatedProjectList");
      } else {
        this.getAllProjects();
        console.error(response.serviceResponse);
      }
    });
  }

  getAllDepartmentList(project:any) {
    this.allDeptList = [];

    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
        this.filteredDeptList = this.allDeptList.filter(x => project.department.includes(x.name));
        console.log("allDeptList : ", this.allDeptList)
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  // Team

  getAllEmployeesByRole(departmentList:any) {
    this.teamLeadsList = [];
    let employeeList = [];

    this.employeeObj.role = "TeamLead";
    this.employeeService.getAllEmployeesByRole(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        employeeList = response.serviceResponse;
        console.log("employeeList By Role : ", employeeList);
        console.log("Department Selected : ", departmentList);

        let filterDepartmentList = this.employeeListByDept.map(y => y.departmentId);
        this.teamLeadsList = employeeList.filter(x => departmentList?.includes(x.departmentId));
        this.teamLeadsList = this.teamLeadsList.sort((a, b) => a.name.localeCompare(b.name));
        console.log("teamLeadsList : ", this.teamLeadsList)
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllEmployeesByDepartmentIds(departmentList:any) {
    this.employeeListByDept = [];

    let empObj = new Employee();
    empObj.departmentList = departmentList?.map(deptId => {
       let dept =  new Department();
       dept.deptId = deptId;
       return dept;
    });

    console.log("empObj.departmentList : ", empObj.departmentList);
    
    this.employeeService.getAllEmployeesByDepartmentIds(empObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.employeeListByDept = response.serviceResponse;
        this.employeeListByDept = this.employeeListByDept.sort((a, b) => a.name.localeCompare(b.name));
        console.log("employeeList By Department : ", this.employeeListByDept);
        this.updateEmployeeListAccordingToTeamMembers();
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  updateEmployeeListAccordingToTeamMembers() {
    console.log("Existing Team Member : ", this.teamObj.allTeamMemberList);
    console.log("Selected Team Member : ", this.allTeamMembers);
    this.employeeListByDept.forEach((employee, index) => {
      const existingEmployee = this.teamObj.allTeamMemberList?.find(member => member.empId == employee.empId);
      if (existingEmployee) {
        employee.isSelected = true;
      }else{
        const existingEmployee = this.allTeamMembers.find(member => member.empId == employee.empId);
        if (existingEmployee) {
          employee.isSelected = true;
        }
      }
    });
  }

  setTeamLead(teamLeadId){
    let teamLead:any = [];
    teamLead = this.teamLeadsList?.find(x => x.empId == teamLeadId);
    if(teamLead){
      this.allTeamList?.forEach((team:any) => {
        if(team.teamName == this.currentTeam.teamName){
          if(team.teamMemberList){
            teamLead.isTeamLead = true;
            team.teamMemberList = team.teamMemberList.filter(x => x.isTeamLead != true);
            console.log(team.teamMemberList, " : team memeber list");
            team.teamMemberList = [...team.teamMemberList, ...[teamLead]];
          }else{
            teamLead.isTeamLead = true;
            team.teamMemberList = [teamLead];

            console.log(team.teamMemberList, " : team memeber list ===");
          }
        }
      });
    }
  }

  addTeamMemberMapping(){
    this.allTeamList?.forEach((team:any) => {
      if(team.teamName == this.currentTeam.teamName){
        team.createdBy = this.currentUser.empId;
        if(team.teamMemberList){
          if(this.allTeamMembers.length){
            team.teamMemberList = [...team.teamMemberList,...this.allTeamMembers];
          }else{
            team.teamMemberList = [...team.teamMemberList];
          }
        }else{
          team.teamMemberList = (this.allTeamMembers.length !== 0) ? this.allTeamMembers : null;
        }
      }
    });
    this.cancelRequest();
  }

  createDraftProjectInfo(template: TemplateRef<any>){
    this.projectObj.teamList = this.allTeamList;
    
    console.log(this.projectObj, " : this.projectObj");
    this.resourceManagementService.createDraftProjectInfo(this.projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template,response.serviceResponse);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getTeamListByProjectName(project:any){
    this.previewTeamList = [];
    this.resourceManagementService.getTeamListByProjectName(project).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectObj.teamList = response.serviceResponse;
        this.projectObj.teamList.forEach((obj) =>{
          obj.departmentList = obj.departmentList?.map(x=>+x);
        });
        console.log(this.projectObj.teamList, " this.projectObj.teamList");
        this.previewTeamList = this.projectObj.teamList;

        //Project Team List
        if (this.projectObj.teamList == undefined || this.projectObj.teamList.length == 0) {
          this.addInputTeamField();
        } else {
          this.allTeamList = this.projectObj.teamList;
        }

      } else {
        console.error(response.serviceResponse);

        //Project Team List
        if (this.projectObj.teamList == undefined || this.projectObj.teamList.length == 0) {
          this.addInputTeamField();
        } else {
          this.allTeamList = this.projectObj.teamList;
        }
      }
    });
  }

  // Manage team & teamMemberList

  addInputTeamField(){
    let newTeamObj = new Team();
    this.allTeamList.push(newTeamObj);
    console.log(this.allTeamList, " : this.allTeamList");
  }

  removeInputTeamField(teamObj) {
    this.allTeamList.forEach((value, index) => {
      if (value == teamObj) {
        this.updatedTeamList.push(value);
        this.allTeamList.splice(index, 1);
      }
    });
  }

  addTeamMember(){
    const newTeamMember = this.employeeListByDept.find(employee => employee.empId == this.newteamMember.empId);
    if(newTeamMember){
      newTeamMember.employeeRole = this.newteamMember.employeeRole;
      this.allTeamMembers.push(newTeamMember);
    }
    this.newteamMember = new TeamMember();
  }

  removeTeamMember(teamMember) {

    this.teamObj.allTeamMemberList?.forEach((value, index) => {
      if (value == teamMember) {
        if (this.teamObj.updatedTeamMemberList[0]) {
          this.teamObj.updatedTeamMemberList.push(value);
        } else {
          this.teamObj.updatedTeamMemberList = [];
          this.teamObj.updatedTeamMemberList.push(value);
        }
        this.teamObj.allTeamMemberList.splice(index, 1);
        let existingEmployee = this.employeeListByDept.find(employee => employee.empId == teamMember.empId);
        if (existingEmployee) existingEmployee.isSelected = false;
      }
    });
  }

  removeInputTeamMemberField(teamMember) {
    this.allTeamMembers.forEach((value, index) => {
      if (value == teamMember){
        this.allTeamMembers.splice(index, 1);
        let existingEmployee = this.employeeListByDept.find(employee => employee.empId == teamMember.empId);
        if (existingEmployee) existingEmployee.isSelected = false;
      }
    });
    console.log("Updated Members : ", this.allTeamMembers);
  }

  // Modals

  openTeamMemberModal(template: TemplateRef<any>, currentTeam){
    this.allTeamMembers = [];
    this.getAllEmployeesByDepartmentIds(currentTeam.departmentList);
    this.getAllEmployeesByRole(currentTeam.departmentList);
    this.allTeamList?.forEach((team:any) => {
      if(team.teamName == currentTeam.teamName){
        this.isUpdation = true;
        let teamLeadObj = team.teamMemberList?.filter(x => x.isTeamLead == true || x.isTeamLead == "true");
        if(teamLeadObj){
          this.teamObj.teamLeadId = teamLeadObj[0]?.empId;
        }
        this.teamObj.allTeamMemberList = team.teamMemberList?.filter(x => x.isTeamLead != true);
      }
    });
    console.log(this.teamObj.allTeamMemberList, " allTeamMemberList");
    
    this.currentTeam = currentTeam;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  previewTeamModal(template: TemplateRef<any>, teamObj:any){
    this.previewTeamList = [];
    this.allTeamList?.forEach((team:any) => {
      if(team.teamName == teamObj.teamName){
        this.previewTeamList = [team];
      }
    });
    console.log(this.previewTeamList, " : this.previewTeamList");
    
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  openProjectPreviewModal(template: TemplateRef<any>, project:any){
    this.previewTeamList = [];
    this.projectObj = Object.assign({}, project);
    this.getTeamListByProjectName(project);

    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  //pagination 

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortProject(sort: Sort) {
    console.log(sort);
    const data = this.allProjectList;
    if (!sort.active || sort.direction === '') {
      this.allProjectList = data;
      return;
    } else {
      this.allProjectList = data.sort(
        (a, b) => {
          const isAsc = sort.direction === 'asc';
          switch (sort.active) {
            
            default:
              return 0;
          }
        }
      )
    }
  }

}
