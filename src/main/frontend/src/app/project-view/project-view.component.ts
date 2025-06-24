import { Location } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { Router } from '@angular/router';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';

import { Breadcrumb } from 'src/app/models/breadcrumd';
import { Project } from 'src/app/models/project';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { ProjectService } from 'src/app/services/project.service';
import { UtilityService } from 'src/app/services/utility.service';
import { DefaultProjectUpdate } from '../models/defaultProjectUpdate';
import { EmployeeInformation } from '../models/employeeInformation';
import { GetProjectDetailsForBulkDefaultUpdate } from '../models/getProjectDetailsForBulkDefaultUpdate';
import { SetDefaultProjectObj } from '../models/setDefaultProjectObj';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { ResourceManagementService } from '../services/resource-management.service';


class FilterData {
  title: any;
  columns: any;
  queryList: any;
}
@Component({
  selector: 'app-project-view',
  templateUrl: './project-view.component.html',
  styleUrls: ['./project-view.component.css']
})
export class ProjectViewComponent implements OnInit {

  currentBreadcrumbList: any[] = [];
  projectList: any[] = [];
  projectObj: Project = new Project();
  teamMemberList: any[] = [];
  selectedProjectId: any;
  bulkEmployeeList: EmployeeInformation[] = [];
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  page = 1;
  filters: any = {};
  isSearchEnabled: boolean = false;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  currentUser: User;
  lastDate: any;
  selectedOtherProjectId: any;
  modalRef3: BsModalRef = new BsModalRef();
  modalRef4: BsModalRef = new BsModalRef();
  modalRef5: BsModalRef = new BsModalRef();
  setDefaultProjectObj: SetDefaultProjectObj = new SetDefaultProjectObj();
  employeeRole: any[] = ['Employee', 'TeamLead', 'Manager', 'HOD', 'HR', 'SuperAdmin', 'RMG'];
  searchTerm: any
  searchTermTeam: any;
  resourceRequirementListBulk: any;
  defaultProjectUpdate: DefaultProjectUpdate = new DefaultProjectUpdate();
  filteredOtherProjectList: any[] = [];
  otherProjectList: any[] = [];
  filteredTeamsForDefaultBulk: any;
  isBulkDelete: boolean = false;
  isBulkUpdateMode: boolean = true;
  filteredProjectsForDefaultBulkBench: any;
  defaultProjectUpdateBulk: DefaultProjectUpdate = new DefaultProjectUpdate();
  filteredProjectsForDefaultBulkOther: any;
  bulkProjectType: any;
  benchProjectListBulk: any;
  otherProjectListBulk: any;
  projectListBulk: GetProjectDetailsForBulkDefaultUpdate = new GetProjectDetailsForBulkDefaultUpdate();
  teamListBulk: any;
  @ViewChild("alert_message_without_reload")
  alertTemplateWithoutReload: TemplateRef<any>;
  openAlertMod3(template: TemplateRef<any>, message: any) {
    this.modalRef3 = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }
  employeesColumns: any[] = ['blank', 'spoc', 'teamLeadName', 'teamName', 'employeeName', 'billableType', 'startDate', 'employeeRole'];
  constructor(
    private breadcrumbService: BreadcrumbService,
    private modalService: BsModalService,
    private employee360Service: Employee360Service,
    private router: Router,
    private projectService: ProjectService,
    private employeeService: EmployeeService,
    public utilityService: UtilityService,
    private location: Location,
    private resourceManagementService: ResourceManagementService,
    private authenticationService: AuthenticationService

  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    const storedData = localStorage.getItem('projectId');
    const parsedData = storedData || null;

    // console.log("storedData ", storedData);
    // console.log("parsedData ", parsedData);

    if (parsedData !== null && parsedData !== undefined) {
      this.selectedProjectId = parsedData;
    } else {
      this.selectedProjectId = history.state.data;
    }
    this.getProjectInfo();
  }

  handlePageChange(event) {
    this.page = event;
  }

  getProjectInfo(): void {
    this.projectObj.projectViewId = this.selectedProjectId;
    console.log("projectObj ", this.projectObj);

    if (this.projectObj.projectViewId.startsWith('po')) {
      this.projectObj.projectViewId = this.projectObj.projectViewId.substring(2);
      console.log("projectId ", this.projectObj.projectViewId);
      this.projectObj.projectViewId = Number(this.projectObj.projectViewId);
      console.log("projectId ", this.projectObj.projectViewId);
      this.employee360Service.getPoProjectInfo(this.projectObj).subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.projectList = response.serviceResponse;
            this.projectObj = this.projectList[0];
            this.getTeamInfo(this.projectObj);
          } else {
            console.warn("Failed to fetch project info");
          }
        },
        error: (error) => {
          console.error("Error fetching project info:", error);
        }
      });
    } else {
      this.employee360Service.getProjectInfo(this.projectObj).subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.projectList = response.serviceResponse;
            this.projectObj = this.projectList[0];
            this.getTeamInfo(this.projectObj);
          } else {
            console.warn("Failed to fetch project info");
          }
        },
        error: (error) => {
          console.error("Error fetching project info:", error);
        }
      });
    }
  }

  getTeamInfo(project): void {
    this.employee360Service.getTeamInfo(project).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === "Success") {
          this.teamMemberList = response.serviceResponse.teamDetails;

          console.log("getTeamInfo ", this.teamMemberList);

          if (Array.isArray(this.teamMemberList)) {
            this.teamMemberList.forEach((team) => {
              if (Array.isArray(team.teamMemberDetails)) {
                team.teamMemberDetails.forEach((employee) => {
                  employee.emp360 = employee.empId;
                });
              }
            });
          } else {
            console.warn("teamMemberList is not an array:", this.teamMemberList);
          }

          console.log("Formatted Team Data: ", this.teamMemberList);
        } else {
          console.warn("Failed to fetch team info");
        }
      },
      error: (error) => {
        console.error("Error fetching team info:", error);
      }
    });
  }

  viewProjectInfo(projectObj: any) {
    let breadcrumbObject = new Breadcrumb();
    breadcrumbObject.title = "Project View - " + projectObj.projectName;
    breadcrumbObject.url = "/user-team/resource-management";
    breadcrumbObject.object = projectObj;
    this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    this.router.navigate([breadcrumbObject.url], { queryParams: {} });
  }

  editProjectInfo(projectObj: any) {
    let breadcrumbObject = new Breadcrumb();
    breadcrumbObject.title = "Project Edit - " + projectObj.projectName;
    breadcrumbObject.url = "/user-team/resource-management";
    breadcrumbObject.object = projectObj;
    this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    this.router.navigate([breadcrumbObject.url], { queryParams: {} });
  }

  deleteResourceModal(template: TemplateRef<any>, projObj, member) {

    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.projectObj = projObj;
    this.projectObj.empId = member.empId;
    if (member.lastDate != null || member.lastDate != '') {
      this.lastDate = member.lastDate;
    }
    console.log("hdgh", this.lastDate, projObj);
  }

  deleteResourceFromProject(template: TemplateRef<any>) {
    // this.cancelRequest();

    let projectObj = new Project();
    projectObj.teamId = this.projectObj.teamId;
    projectObj.empId = this.projectObj.empId;
    projectObj.endDate = this.lastDate;

    if (projectObj.endDate == null || projectObj.endDate == '' || projectObj.endDate == undefined) {
      this.openAlertMod(template, "Select End Date");
      return;
    }
    console.log("kmnjdbjhvb", projectObj);
    this.projectService.updateProjectResourceAsInActive(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getProjectInfo();
      }
    })
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  onSearch(searchData) {
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

  cancelRequest() {
    this.modalRef.hide();
  }
   cancelRequest5() {
    this.modalRef5.hide();
  }

  goBack(): void {
    this.location.back();
  }
  selectedMembers: any[] = [];
  employeeSelectionHistory: any[] = [];
  globalSelectAll: boolean = false;
  onSelectionChange(team: any, object: any) {
    if (!this.selectedMembers) {
      this.selectedMembers = [];
    }

    const existingTeamIndex = this.selectedMembers.findIndex(item => item.team.teamId === team.teamId);

    if (object.selected) {
     console.log("test",object);
      if (existingTeamIndex === -1) {
        this.selectedMembers.push({ team, object: [object] });
      } else {

        const alreadyAdded = this.selectedMembers[existingTeamIndex].object
          .some((m: any) => m.empId === object.empId);
        if (!alreadyAdded) {
          this.selectedMembers[existingTeamIndex].object.push(object);
        }
      }


      const historyExists = this.employeeSelectionHistory.some(
        (entry) => entry.teamId === team.teamId && entry.empId === object.empId
      );
      if (!historyExists) {
        const deselectedMember = {
          teamId: team.teamId,
          empId: object.empId,
          endDate: object.lastDate,
          employeeTeamMapId:object.employeeTeamMapId
        };
        this.employeeSelectionHistory.push(deselectedMember);
      }
    } else {

      if (existingTeamIndex !== -1) {
        const memberIndex = this.selectedMembers[existingTeamIndex].object
          .findIndex((m: any) => m.empId === object.empId);

        if (memberIndex !== -1) {

          this.selectedMembers[existingTeamIndex].object.splice(memberIndex, 1);


          if (this.selectedMembers[existingTeamIndex].object.length === 0) {
            this.selectedMembers.splice(existingTeamIndex, 1);
          }
        }
      }


      const historyIndex = this.employeeSelectionHistory.findIndex(
        (entry) => entry.teamId === team.teamId && entry.empId === object.empId
      );
      if (historyIndex !== -1) {
        this.employeeSelectionHistory.splice(historyIndex, 1);
      }
    }

    console.log("Selected Members:", this.selectedMembers);
    console.log("Employee Selection History:", this.employeeSelectionHistory);
  }






  isTeamFullySelected(team: any): boolean {
    return team.teamMemberDetails.every((member: any) => member.selected);
  }

  toggleTeamSelection(team: any, event: any) {
    const isChecked = event.target.checked;

    team.teamMemberDetails.forEach((member: any) => {
      member.selected = isChecked;
      this.onSelectionChange(team, member);


      if (isChecked) {

        const exists = this.employeeSelectionHistory.some(
          (entry) => entry.teamId === team.teamId && entry.empId === member.empId
        );
        if (!exists) {
          const deselectedMember = {
            teamId: team.teamId,
            empId: member.empId,
            endDate: member.lastDate
          };
          this.employeeSelectionHistory.push(deselectedMember);
        }
      }
    });


    if (!isChecked) {
      this.employeeSelectionHistory = this.employeeSelectionHistory.filter(
        (entry) => entry.teamId !== team.teamId
      );
    }
  }


  isMemberSelected(team: any, member: any): boolean {
    return !!member.selected;
  }

  areAllTeamsSelected(): boolean {
    if (!Array.isArray(this.teamMemberList)) {
      return false;
    }

    const allTeamsHaveMembers = this.teamMemberList.every(team => Array.isArray(team.teamMemberDetails) && team.teamMemberDetails.length > 0);

    const allMembersSelected = this.teamMemberList.every(team =>
      Array.isArray(team.teamMemberDetails) &&
      team.teamMemberDetails.every((member: any) => member.selected)
    );

    return this.teamMemberList.length > 0 && allTeamsHaveMembers && allMembersSelected;
  }



  toggleAllTeams(event: any): void {
    const isChecked = event.target.checked;

    this.teamMemberList.forEach(team => {
      team.teamMemberDetails.forEach(member => {
        if (member.selected !== isChecked) {
          member.selected = isChecked;
          this.onSelectionChange(team, member); // Reuse your main logic
        }
      });
    });

    // Clean up or fill employeeSelectionHistory
    if (!isChecked) {
      this.employeeSelectionHistory = [];
      this.selectedMembers = [];
    }
  }

  activeProjects: any;
  EmployessIds: any;
  currentProjectDetails: any
  deleteResourceModalBulk(template1: TemplateRef<any>, template2: TemplateRef<any>, template: TemplateRef<any>, project) {
    console.log("Projects", Project);
    const empIds: number[] = this.teamMemberList.reduce((acc: number[], team: any) => {
      const members = Array.isArray(team.teamMemberDetails) ? team.teamMemberDetails : [];

      members.forEach(member => {
        if (
          Array.isArray(member.otherActiveProjects) &&
          member.otherActiveProjects.length === 0 &&
          member.isDefaultProject === 1
        ) {
          acc.push(member.empId);
        }
      });

      return acc;
    }, []);


    const empIdsHavingActiveProjects: number[] = this.teamMemberList.reduce((acc: number[], team: any) => {
      const members = Array.isArray(team.teamMemberDetails) ? team.teamMemberDetails : [];

      members.forEach(member => {
        if (
          Array.isArray(member.otherActiveProjects) &&
          member.otherActiveProjects.length !== 0 &&
          member.isDefaultProject === 1
        ) {
          acc.push(member.empId);
        }
      });

      return acc;
    }, []);

    this.activeProjects = empIdsHavingActiveProjects;
    this.EmployessIds = empIds;
    const selectedEmpIds = this.employeeSelectionHistory.map(item => item.empId);
      console.log("this member to be deleted",this.employeeSelectionHistory);
    this.EmployessIds = this.EmployessIds.filter(empId =>
      selectedEmpIds.includes(empId)
    );
    
    this.activeProjects = this.activeProjects.filter(empId =>
      selectedEmpIds.includes(empId)
    );
    console.log("this member to be deleted", this.activeProjects, this.EmployessIds);
   if (this.EmployessIds.length !== 0 && this.activeProjects.length !== 0) {
      this.getEmployeeInformationBulk(this.EmployessIds);
      this.modalRef4 = this.modalService.show(template1, { class: 'modal-xl' });
      this.setDefaultProjectObj.empIds = this.activeProjects;
      this.setDefaultProjectObj.projectId = project.projectId;
      this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
      this.modalRef = this.modalService.show(template2, { class: 'modal-xl' });

    } else if (this.EmployessIds.length !== 0 && this.activeProjects.length === 0) {
      this.getEmployeeInformationBulk(this.EmployessIds);
      this.modalRef4 = this.modalService.show(template1, { class: 'modal-xl' });
    } else if (this.EmployessIds.length === 0 && this.activeProjects.length !== 0) {
      this.setDefaultProjectObj.empIds = this.activeProjects;
      this.setDefaultProjectObj.projectId = project.projectId;
      this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
      this.modalRef = this.modalService.show(template2, { class: 'modal-xl' });
    } else {
      this.modalRef5 = this.modalService.show(template, { class: 'modal-sm' });
    }

    // console.log("tesmp", this.projectdetails1);
    this.getProjectDetailsForBulkDefaultUpdate();
    // this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    let lastDate1 = null;
    this.employeeSelectionHistory.forEach(employee => {
      lastDate1 = employee.endDate ? moment(employee.endDate).format('YYYY-MM-DD') : moment().format('YYYY-MM-DD');
    });
    this.lastDate = lastDate1;
  }
  deleteResourceFromProjectBulk(template: TemplateRef<any>) {
    this.projectService.updateProjectResourcesAsInActiveBulk(this.employeeSelectionHistory)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.openAlertMod(template, response.serviceResponse);


          this.selectedMembers = [];
          this.employeeSelectionHistory = [];


          this.teamMemberList.forEach(team => {
            team.teamMemberDetails.forEach(member => {
              member.selected = false;
            });
          });


          console.log("After deletion:", this.selectedMembers, this.employeeSelectionHistory);


          this.getProjectInfo();
        }
        window.location.reload();
      });
  }




  getEmployeeInformationBulk(empIds) {
    this.resourceManagementService.getEmployeeInformationBulk(empIds).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.bulkEmployeeList = response.serviceResponse;
      } else {
           if (this.EmployessIds.length !== 0) {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }
      }
    });
  }

  setProjectMappingAndDefaultProject(template: TemplateRef<any>, emp) {
    console.log("empId", emp, emp.empId);
    this.setDefaultProjectObj.empId = [emp.empId];
    this.setDefaultProjectObj.createdBy = this.currentUser.empId;
    this.setDefaultProjectObj.projectId = emp.projectId;
    this.setDefaultProjectObj.teamId = emp.teamId;
    this.setDefaultProjectObj.employeeRole = emp.employeeRole;
    this.setDefaultProjectObj.resourceOverViewId = emp.resourceOverViewId;
    const today = new Date();
    this.lastDate = today.toISOString().split('T')[0];
    if (this.setDefaultProjectObj.teamId !== null) {
      this.resourceManagementService.setProjectMappingAndDefaultProject(this.setDefaultProjectObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.bulkEmployeeList = response.serviceResponse;
          this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
          this.EmployessIds = this.EmployessIds.filter(id => id !== emp.empId);
          this.getEmployeeInformationBulk(this.EmployessIds);
          console.log("empId", this.activeProjects.length, this.EmployessIds.length);
          if (this.activeProjects.length === 0 && this.EmployessIds.length === 0) {
            this.modalRef4.hide();
            this.modalRef5 = this.modalService.show(template, { class: 'modal-sm' });
          }
          if(this.EmployessIds.length === 0 && this.activeProjects.length !== 0  ){
            this.modalRef4.hide();
            this.openAlertMod3(this.alertTemplateWithoutReload, "Please update the default project of employees who are currently mapped to other active projects.");
          }
        } else {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }
      });
    }

  }


  setProjectMappingAndDefaultProjectBulk(setDefaultProjectObj, template: TemplateRef<any>) {

    setDefaultProjectObj.empId = this.EmployessIds;
    const today = new Date();
    this.lastDate = today.toISOString().split('T')[0];
    if (setDefaultProjectObj.teamId !== null) {
      this.resourceManagementService.setProjectMappingAndDefaultProject(setDefaultProjectObj).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.bulkEmployeeList = response.serviceResponse;
          this.EmployessIds = [];
          setDefaultProjectObj = [];
          // this.getEmployeeInformationBulk(this.EmployessIds);
          if (this.activeProjects.length === 0 && this.EmployessIds.length === 0) {
            this.modalRef4.hide();
            this.modalRef5 = this.modalService.show(template, { class: 'modal-sm' });
          } 
          if(this.EmployessIds.length === 0 && this.activeProjects.length !== 0  ){
            this.modalRef4.hide();
            this.openAlertMod3(this.alertTemplateWithoutReload, "Please update the default project of employees who are currently mapped to other active projects.");
          }
        } else {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }
      });
    }

  }
  bulkEmployeeListActiveList: any[] = [];
  getEmployeeInformationForDefaultProject(setDefaultProjectObj: any) {

    this.resourceManagementService.getEmployeeInformationForDefaultProject(setDefaultProjectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.bulkEmployeeListActiveList = response.serviceResponse;
        console.error("Unable to fetch Employee List!", this.bulkEmployeeListActiveList);
        // this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
      
       } else {
           if (this.activeProjects.length !== 0) {
          this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Employee List");
          console.error("Unable to fetch Employee List!");
        }
       }
    });
  }


  getProjectDetailsForBulkDefaultUpdate() {
    this.resourceManagementService.getProjectDetailsForBulkDefaultUpdate().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.projectListBulk = response.serviceResponse;
        this.benchProjectListBulk = this.projectListBulk.benchProjectList;
        this.otherProjectListBulk = this.projectListBulk.otherProjectList;
        this.filteredProjectsForDefaultBulkBench = this.benchProjectListBulk;
        this.filteredProjectsForDefaultBulkOther = this.otherProjectListBulk;
      } else {
        this.openAlertMod3(this.alertTemplateWithoutReload, "Unable to fetch Project List");
        console.error("Unable to fetch Project List!");
      }
    });
  }



  getTeamListForSelectedProject() {
    let selectedProjectId = this.setDefaultProjectObj.projectId;

    if (!selectedProjectId || !this.bulkProjectType)
      this.teamListBulk = [];

    const projectList = this.bulkProjectType === 'bench' ? this.benchProjectListBulk : this.otherProjectListBulk;
    this.teamListBulk = projectList.find(p => p.projectId === selectedProjectId);
    this.filteredTeamsForDefaultBulk = this.teamListBulk.teamList;
    this.resourceRequirementListBulk = this.teamListBulk.resourceRequirement;
  }
  getTeamListForSelectedProject1(emp) {
    let selectedProjectId = emp.projectId;

    if (!selectedProjectId || !this.bulkProjectType)
      this.teamListBulk = [];

    const projectList = emp.projectType === 'bench' ? this.benchProjectListBulk : this.otherProjectListBulk;
    this.teamListBulk = projectList.find(p => p.projectId === selectedProjectId);
    this.filteredTeamsForDefaultBulk = this.teamListBulk.teamList;
    this.resourceRequirementListBulk = this.teamListBulk.resourceRequirement;
  }


  filterTeamsForDefaultBulk() {
    const lowerSearch = this.searchTermTeam.toLowerCase();
    this.filteredTeamsForDefaultBulk = this.teamListBulk.teamList.filter(team =>
      team.teamName.toLowerCase().includes(lowerSearch)
    );
  }

  filterProjectsForDefaultBulkOther() {
    const lowerSearch = this.searchTerm.toLowerCase();
    this.filteredProjectsForDefaultBulkOther = this.otherProjectListBulk.filter(project =>
      project.projectName.toLowerCase().includes(lowerSearch)
    );
  }

  filterProjectsForDefaultBulkBench() {
    const lowerSearch = this.searchTerm.toLowerCase();
    this.filteredProjectsForDefaultBulkBench = this.benchProjectListBulk.filter(project =>
      project.projectName.toLowerCase().includes(lowerSearch)
    );
  }


  setDefaultProjectUpdateForActiveProject(details: any, projectId, template: TemplateRef<any>) {
    console.log("test id", details.empId, details);
    this.defaultProjectUpdate.empIds = [details.empId];
    this.defaultProjectUpdate.projectId = projectId;
    this.defaultProjectUpdate.createdBy = this.currentUser.empId;
    const today = new Date();
    this.lastDate = today.toISOString().split('T')[0];
    this.resourceManagementService.setDefaultProjectUpdateBillable(this.defaultProjectUpdate).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
        this.activeProjects = this.activeProjects.filter(id => id !== details.empId);
        this.setDefaultProjectObj.empIds = this.activeProjects;
        this.setDefaultProjectObj.projectId = this.currentProjectDetails;
        this.getEmployeeInformationForDefaultProject(this.setDefaultProjectObj);
        if (this.EmployessIds.length === 0 && this.activeProjects.length === 0) {
          this.modalRef.hide();
          this.modalRef5 = this.modalService.show(template, { class: 'modal-sm' });
        }
         if(this.EmployessIds.length !== 0 && this.activeProjects.length === 0  ){
            this.modalRef.hide();
            this.openAlertMod3(this.alertTemplateWithoutReload, "Please update the default project of employees who are not mapped to other active projects.");
          }
      } else {
        this.openAlertMod3(this.alertTemplateWithoutReload, response.serviceResponse);
        console.error("Error setting the default project!");
      }
    });
  }


  filterOtherProjects() {
    const lowerSearch = this.searchTerm.toLowerCase();
    this.filteredOtherProjectList = this.otherProjectList.filter(project =>
      project.projectName.toLowerCase().includes(lowerSearch)
    );
  }
}
