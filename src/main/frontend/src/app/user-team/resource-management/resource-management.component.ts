import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
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
import { ValidationService } from 'src/app/services/validation.service';
import { TeamService } from 'src/app/services/team.service';

@Component({
  selector: 'app-resource-management',
  templateUrl: './resource-management.component.html',
  styleUrls: ['./resource-management.component.css']
})
export class ResourceManagementComponent implements OnInit {

  @ViewChild("alert_message")
  alertTemplate: TemplateRef<any>;

  data: string;
  currentUser: User;
  feature = "Resource Management";
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
  isMyDepartmentProject:boolean = false;
  isPendingProject:boolean = false;
  allProjectTable:boolean = false;
  isAllPendingProjectAllowed: boolean = false;
  isHOD:boolean = false;

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
  allTeamListCopy: any[] = [];

  employeeRole: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin'];

  constructor(
    private departmentService: DepartmentService,
    private validationService: ValidationService,
    private employeeService: EmployeeService,
    private modalService: BsModalService,
    private teamService: TeamService,
    private resourceManagementService: ResourceManagementService,
    private authenticationService: AuthenticationService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }


  ngOnInit(): void {

     // Dynamic Subfeature Flags 
     let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
     featureMap.subFeatures?.forEach(sub => {
       this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
     });
     console.log(this.feature, this.userMapping);

    this.sectionViewInit();
  }

  sectionViewInit(){
    if(this.currentUser.employeeRole == 'HOD' || this.currentUser.employeeRole == 'SuperAdmin'){
      this.isHOD = true;
    }else{
      this.isHOD = false;
    }
    if(this.currentUser.employeeRole == '' || this.currentUser.employeeRole == undefined || this.currentUser.employeeRole == null){
      this.isHOD = false;
    }

    if (this.userMapping.view_all_rmg_projects) {
      this.showViewProjects();
    } else if (this.userMapping.view_my_department_rmg_projects) {
      this.showDepartmentWiseProject();
    } else if (this.userMapping.view_pending_for_approval_rmg_projects) {
      this.showPendingForApprovalProject();
    }
  }

  showViewProjects(){
    this.isProjectTable = true;
    this.allProjectTable = true;
    
    this.isHideButton = false;
    this.isEditProject = false;
    this.isMyDepartmentProject = false;
    this.isPendingProject = false;
    this.alreadyCreatedTeam();
  }

  showEditProjectForm(project: any){
    this.isEditProject = true;

    this.allProjectTable = true;
    this.isHideButton = true;
    
    this.allTeamList = [];
    this.projectObj = Object.assign({}, project);
    this.getAllDepartmentList(project);
    this.getTeamListByProjectName(project);
  }

  closeEditProject(){
    this.isHideButton = false;
  }

  showDepartmentWiseProject(){
    this.allProjectList = [];
    this.isMyDepartmentProject = true;
    this.allProjectTable = true;

    this.isHideButton = false;
    this.isEditProject = false;
    this.isProjectTable = false;
    this.isPendingProject = false;
    this.alreadyCreatedTeam();
  }

  showPendingForApprovalProject(){
    this.allProjectList = [];
    this.isPendingProject = true;
    this.allProjectTable = true;

    this.isHideButton = false;
    this.isEditProject = false;
    this.isProjectTable = false;
    this.isMyDepartmentProject = false;
    this.alreadyCreatedTeam();
  }

  getAllProjects(){
    // fetch('https://poportal.apmosys.com/PoPortal/project/fixedCost/getAllProjects').then(res => res.json()).then(data => {
    fetch('http://192.168.21.175:8080/PoPortal/project/fixedCost/getAllProjects').then(res => res.json()).then(data => {
      let _projectList = data;

      if((_projectList != null || _projectList != undefined) && (this.teamCreatedProjectList != null || this.teamCreatedProjectList != undefined)){
        _projectList.forEach((proj) => {
          this.teamCreatedProjectList.forEach((projTeam) => {
            if(proj.name == projTeam.name){
              proj.isTeamCreated = true;
            }
          })
        });
      }

      //sort according to dateTime
      _projectList = _projectList.sort((a, b) => (new Date(a.createdOn).getTime() < new Date(b.createdOn).getTime())? 1 : -1);

      //View My Department project
      if(this.isMyDepartmentProject == true){
        this.allProjectList = _projectList?.filter((proj) => proj.department?.includes(this.currentUser.departmentName));
        
      }
      //View Pending for approval projects
      else if(this.isPendingProject == true){
        this.resourceManagementService.getPendingForApprovalProject().pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus == "Success") {
            const pendingProject = response.serviceResponse;

            if(this.userMapping.allow_all_rmg_pending_projects){
              this.isAllPendingProjectAllowed = this.userMapping.allow_all_rmg_pending_projects;
            }

            if(this.isAllPendingProjectAllowed){
              this.allProjectList = pendingProject;
            }else{
              this.allProjectList = pendingProject.filter((proj) => proj.department?.includes(this.currentUser.departmentName));
            }
            console.log( this.allProjectList, " :  this.allProjectList");
          } else {
            this.allProjectList = [];
            console.error(response.serviceResponse);
          }
        });

      }else{
        this.allProjectList = _projectList;
        console.log(this.allProjectList, " all projects");
      }
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

  checkTeamName(template: TemplateRef<any>, team:any, teamIndex:any) {
    if (this.projectObj.isTeamCreated == "true" || this.projectObj.isTeamCreated == true) {
      let teamObj = new Team();
      teamObj.teamId = team.teamId;
      teamObj.teamName = team.teamName;
      teamObj.projectName = this.projectObj.name;

      this.teamService.checkTeamName(teamObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Fail") {
          this.allTeamList.forEach((presentTeam, index) => {
            if(index == teamIndex){
              presentTeam.teamName = '';
            }
          });
          this.openAlertMod(template, response.serviceResponse);
        }else{
          this.allTeamListCopy.forEach((teamCopy) => {
            if(teamCopy.teamName == team.teamName && (team.teamName != null && team.teamName != '' && team.teamName != undefined)){
    
              this.allTeamList.forEach((presentTeam, index) => {
                if(index == teamIndex){
                  presentTeam.teamName = '';
                }
              });
              this.openAlertMod(template, "Team Name already exists!!");
            }
          });
        }
      });
    }else{

      this.allTeamListCopy.forEach((teamCopy) => {
        if(teamCopy.teamName == team.teamName && (team.teamName != null && team.teamName != '' && team.teamName != undefined)){

          this.allTeamList.forEach((presentTeam, index) => {
            if(index == teamIndex){
              presentTeam.teamName = '';
            }
          });
          this.openAlertMod(template, "Team Name already exists!!");
        }
      });
    }
  }

  validateProjectObj(projectObj, template: TemplateRef<any>){
    if(projectObj.teamList.length == 0){
      this.alertMessage = "Please add a Team !!"
      this.openAlertMod(template, this.alertMessage);
      return false;
    }

    if(projectObj.teamList.length != 0){
      let flag = true;
      projectObj.teamList.forEach((projObj, index) => {
        projObj.teamName = projObj.teamName?.trim();
        if (!this.validationService.validateNullUndefinedEmptyString(projObj.teamName)) {
          this.alertMessage = `Please enter Team Name - ${index + 1}!!`
          flag = false;
          return;
        }
        if (!this.validationService.validateTeamName(projObj.teamName)) {
          this.alertMessage = "Please enter valid Team Name !!"
          this.openAlertMod(template, this.alertMessage);
          return false;
        }

        if (projObj.departmentList == undefined || projObj.departmentList.length == 0 || projObj.departmentList == null) {
          this.alertMessage = `Please select Team's department - ${index + 1}!!`
          flag = false;
          return;
        }

        if(projObj.teamMemberList == undefined || projObj.teamMemberList.length == 0 || projObj.teamMemberList == null){
          this.alertMessage = `Please add Team Member(s) - ${index + 1}!!`
          flag = false;
          return;
        }

        if(projObj.teamMemberList.length != 0){
          let memberFlag = true;
          projObj.teamMemberList.forEach((member) => {
            if (!this.validationService.validateNullUndefinedEmptyString(member.name)) {
              this.alertMessage = `Please select Team Member- ${index + 1}!!`
              flag = false;
              return;
            }

            if(member.isTeamLead == null && (member.isTeamLead != true || member.isTeamLead != 'true')){
              if(member.employeeRole == undefined || member.employeeRole.length == 0 || member.employeeRole == null){
                this.alertMessage = `Please select member(s) Employee Role - ${index + 1}!!`
                flag = false;
                console.log(projObj.teamMemberList, " : projObj.teamMemberList");
                
                return;
              }
            }
          });
          if (!memberFlag) {
            this.openAlertMod(template, this.alertMessage);
            return false;
          }else{
            return true;
          }
        }
      });
      if (!flag) {
        this.openAlertMod(template, this.alertMessage);
        return false;
      }else{
        return true;
      }
    }
  }

  createDraftProjectInfo(template: TemplateRef<any>){
    
    this.projectObj.teamList = this.allTeamList;
    this.projectObj.createdBy = this.currentUser.empId;
    
    let inputValidated: boolean = this.validateProjectObj(this.projectObj, template)
    if (!inputValidated) return;

    if (this.isHOD == true) {
      this.projectObj.isHOD = true;
      console.log(this.projectObj, " : this.projectObj");
      this.resourceManagementService.createDraftProjectInfo(this.projectObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.showViewProjects();
          this.openAlertMod(template, response.serviceResponse);
        } else {
          console.error(response.serviceResponse);
        }
      });
    }else{
      this.projectObj.isHOD = false;
      console.log(this.projectObj, " : this.projectObj");
      this.resourceManagementService.createDraftProjectInfo(this.projectObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.showViewProjects();
          this.openAlertMod(template,response.serviceResponse);

          this.resourceManagementService.sendProjectApproval(this.projectObj).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus == "Success") {
              this.cancelRequest();
              this.openAlertMod(template,response.serviceResponse);
            } else {
              console.error(response.serviceResponse);
            }
          });
        } else {
          console.error(response.serviceResponse);
        }
      });
    }
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
          this.allTeamListCopy = this.projectObj.teamList;
        }

      } else {
        console.error(response.serviceResponse);

        //Project Team List
        if (this.projectObj.teamList == undefined || this.projectObj.teamList.length == 0) {
          this.addInputTeamField();
        } else {
          this.allTeamList = this.projectObj.teamList;
          this.allTeamListCopy = this.projectObj.teamList;
        }
      }
    });
  }

  onApproveProject(project:any) {
    this.resourceManagementService.approvePendingProject(project).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(this.alertTemplate,response.serviceResponse);
        this.showPendingForApprovalProject();
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  onRejectProject(project:any) {
    this.resourceManagementService.rejectPendingProject(project).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(this.alertTemplate,response.serviceResponse);
        this.showPendingForApprovalProject();
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  // Manage team & teamMemberList

  addInputTeamField(){
    let newTeamObj = new Team();
    this.allTeamList.push(newTeamObj);
    this.allTeamListCopy = JSON.parse(JSON.stringify(this.allTeamList));
    console.log(this.allTeamList, " : this.allTeamList");
  }

  removeInputTeamField(teamObj) {
    this.allTeamList.forEach((value, index) => {
      if (value == teamObj) {
        this.updatedTeamList.push(value);
        this.allTeamList.splice(index, 1);
        this.allTeamListCopy.splice(index, 1);
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
    const currentTeam = this.currentTeam;
    this.allTeamList?.forEach((team) => {
      if (team.teamName == currentTeam.teamName) {
        team.teamMemberList.splice(teamMember,1);
        console.log(team.teamMemberList, " : team.teamMemberList");
        
        this.teamObj.allTeamMemberList.splice(teamMember, 1);
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
        let teamLeadObj = team.teamMemberList?.filter(x => x.isTeamLead == true || x.isTeamLead == "true");
        if(teamLeadObj){
          this.teamObj.teamLeadId = teamLeadObj[0]?.empId;
        }
        console.log(teamLeadObj, " :teamLeadObj");
        console.log(this.teamObj.teamLeadId, " : this.teamObj.teamLeadId");

        this.teamObj.allTeamMemberList = team.teamMemberList?.filter(x => x.isTeamLead != true);
        if((this.teamObj.allTeamMemberList != undefined || this.teamObj.allTeamMemberList != null) && this.teamObj.allTeamMemberList.length != 0){
          this.isUpdation = true;
        }else{
          this.isUpdation = false;
        }
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

    if (Object.keys(this.previewTeamList[0]).length === 0) {
      this.previewTeamList = [];
    }
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
