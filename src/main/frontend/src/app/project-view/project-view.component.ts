import { Location } from '@angular/common';
import { Component, OnInit, TemplateRef } from '@angular/core';
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

  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  page = 1;
  filters: any = {};
  isSearchEnabled: boolean = false;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  lastDate: any;
  employeesColumns: any[] = ['blank', 'teamName', 'spoc', 'teamLeadName', 'employeeName', 'billableType', 'startDate', 'employeeRole'];
  constructor(
    private breadcrumbService: BreadcrumbService,
    private modalService: BsModalService,
    private employee360Service: Employee360Service,
    private router: Router,
    private projectService: ProjectService,
    private employeeService: EmployeeService,
    public utilityService: UtilityService,
    private location: Location
  ) { }

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
          // console.log("getTeamInfo ", this.teamMemberList);

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
    this.cancelRequest();

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

  goBack(): void {
    this.location.back();
  }
  selectedMembers: any[] = [];
  employeeSelectionHistory:any[]=[];
  globalSelectAll: boolean = false;
  onSelectionChange(team: any, object: any) {
    if (!this.selectedMembers) {
      this.selectedMembers = [];
    }
  
    const existingTeamIndex = this.selectedMembers.findIndex(item => item.team.teamId === team.teamId);
  
    if (object.selected) {
     
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
          endDate: object.lastDate
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
  
  deleteResourceModalBulk(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    let lastDate1 = null;
    this.employeeSelectionHistory.forEach(employee => {
      lastDate1=employee.endDate ? moment(employee.endDate).format('YYYY-MM-DD') : moment().format('YYYY-MM-DD');
    });
    this.lastDate=lastDate1;
  }
  deleteResourceFromProjectBulk(template: TemplateRef<any>) {
    this.projectService.updateProjectResourcesAsInActiveBulk(this.employeeSelectionHistory)
      .pipe(first())
      .subscribe((response: any) => {
        if (response.serviceStatus === "Success") {
          this.openAlertMod(template, response.serviceResponse);
  
          // ✅ Reset selections
          this.selectedMembers = [];
          this.employeeSelectionHistory = [];
  
          // ✅ Uncheck all members
          this.teamMemberList.forEach(team => {
            team.teamMemberDetails.forEach(member => {
              member.selected = false;
            });
          });
  
          // ✅ Debug/log AFTER reset
          console.log("After deletion:", this.selectedMembers, this.employeeSelectionHistory);
  
          // ✅ Refresh data AFTER cleanup
          this.getProjectInfo();
        }
        window.location.reload();
      });
  }
  
}
