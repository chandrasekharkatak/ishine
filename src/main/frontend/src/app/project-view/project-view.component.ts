import { Component, OnInit, TemplateRef } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { Router } from '@angular/router';
import * as moment from 'moment';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { AppComponent } from 'src/app/app.component';
import { Breadcrumb } from 'src/app/models/breadcrumd';
import { Project } from 'src/app/models/project';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee360Service } from 'src/app/services/employee360.service';
import { ProjectService } from 'src/app/services/project.service';
import { UtilityService } from 'src/app/services/utility.service';
import { SortPipe } from 'src/app/sort.pipe';
import { Location } from '@angular/common';


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
  employeesFor360: any[] = [];
  selectedProjectId: any;

  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  page = 1;
  filters:any = {};
  isSearchEnabled:boolean = false;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  lastDate: any;
  employeesColumns: any[] = ['blank', 'teamName', 'employeeName', 'billableType', 'startDate', 'employeeRole'];
  constructor(
    private breadcrumbService: BreadcrumbService,
    private modalService: BsModalService,
    private employee360Service : Employee360Service,
    private router:Router,
    private projectService: ProjectService,
    private employeeService: EmployeeService,
    public utilityService: UtilityService,
    private location: Location
  ) { }
  
  ngOnInit(): void {
    const storedData = localStorage.getItem('projectId');
    const parsedData = storedData ? JSON.parse(storedData) : null;

    // console.log("storedData ", storedData);
    // console.log("parsedData ", parsedData);

    if (parsedData !== null && parsedData !== undefined) {
        this.selectedProjectId = parsedData;
    } else {
        this.selectedProjectId = history.state.data;
    }

    this.getAllEmployeeFor360View();
  }

  getAllEmployeeFor360View(): void {
    this.employeesFor360 = [];
    this.employeeService.getAllEmployeesFor360View().subscribe({
        next: (response: any) => {
            if (response.serviceStatus == "Success") {
                this.employeesFor360 = response.serviceResponse;

                this.employeesFor360.forEach(employeeObj => {
                    employeeObj.employeementId = this.utilityService.appendEmployeementid(employeeObj.isConsultant, employeeObj.employeementId);
                    employeeObj.dateOfJoining = employeeObj.dateOfJoining ? moment(employeeObj.dateOfJoining).format(AppComponent.DATE_FORMAT) : null;
                    employeeObj.dateOfRelieving = employeeObj.dateOfRelieving ? moment(employeeObj.dateOfRelieving).format(AppComponent.DATE_FORMAT) : null;
                    employeeObj.updatedOn = employeeObj.updatedOn ? moment(employeeObj.updatedOn).format(AppComponent.DATETIME_FORMAT) : null;
                    employeeObj.createdOn = employeeObj.createdOn ? moment(employeeObj.createdOn).format(AppComponent.DATETIME_FORMAT) : null;

                    if (employeeObj.isConsultant == 'true')
                        employeeObj.employeeType = 'Consultant';
                    else if (employeeObj.isApprenticeship == 'true')
                        employeeObj.employeeType = 'Apprentice';
                    else
                        employeeObj.employeeType = 'Regular';
                });

                this.employeesFor360 = new SortPipe().transform(this.employeesFor360, ['name', 'string', 'asc']);
                this.getProjectInfo(); 
            } else {
                alert(response.serviceResponse);
            }
        },
        error: (error) => {
            console.error("Error fetching employees:", error);
        }
    });
}


handlePageChange(event) {
  this.page = event;
}
  getProjectInfo(): void {
    this.projectObj.projectId = this.selectedProjectId;
    // console.log("projectObj ", this.projectObj);

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

  getTeamInfo(project): void {
    this.employee360Service.getTeamInfo(project).subscribe({
        next: (response: any) => {
            if (response.serviceStatus === "Success") {
                this.teamMemberList = response.serviceResponse;
                // console.log("getTeamInfo ", this.teamMemberList);

                const groupedData = {};

                this.teamMemberList.forEach((member) => {
                    const teamKey = member.teamId;

                    if (!groupedData[teamKey]) {
                        groupedData[teamKey] = {
                            teamId: member.teamId,
                            teamName: member.teamName,
                            employees: [],
                            projectId: member.projectId,
                            projectName: member.projectName
                        };
                    }

                    groupedData[teamKey].employees.push({
                        empId: member.empId,
                        employeeName: member.employeeName,
                        employeeRole: member.employeeRole ? member.employeeRole.split(',').filter(role => role.trim() !== '').join(', ') : "",
                        startDate: member.startDate ? moment(member.startDate).format(AppComponent.DATETIME_FORMAT) : null,
                        billableType: member.billableType,
                        active: member.active,
                        emp360: {}
                    });

                    groupedData[teamKey].employees = groupedData[teamKey].employees || [];
                });

                this.teamMemberList = Object.values(groupedData);

                this.teamMemberList.forEach((team) => {
                    team.employees.forEach((employee) => {
                        // console.log("employee.empId ", employee.empId);
                        let matchingEmployee = this.employeesFor360.find(emp => emp.empId === employee.empId);
                        // console.log("matchingEmployee ", matchingEmployee);
                        employee.emp360 = matchingEmployee ? matchingEmployee : {};
                    });
                });
                // console.log("Formatted Team Data: ", this.teamMemberList);
            } else {
                console.warn("Failed to fetch team info");
            }
        },
        error: (error) => {
            console.error("Error fetching team info:", error);
        }
    });
  }
  
  viewProjectInfo(projectObj :any){
    let breadcrumbObject = new Breadcrumb();
    breadcrumbObject.title = "Project View - "+projectObj.projectName;
    breadcrumbObject.url = "/user-team/resource-management";
    breadcrumbObject.object = projectObj;
    this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    this.router.navigate([breadcrumbObject.url], { queryParams: { }});
  }

  editProjectInfo(projectObj :any){
    let breadcrumbObject = new Breadcrumb();
    breadcrumbObject.title = "Project Edit - "+projectObj.projectName;
    breadcrumbObject.url = "/user-team/resource-management";
    breadcrumbObject.object = projectObj;
    this.breadcrumbService.addObjectToAddInBreadcrumb(breadcrumbObject);
    this.router.navigate([breadcrumbObject.url], { queryParams: { }});
  }

  deleteResourceModal(template: TemplateRef<any>, projObj,member) {
    this.modalRef = this.modalService.show(template, { class: 'modal-md' });
    this.projectObj = projObj;
    this.projectObj.empId = member.empId;
    // console.log(" member ",member.empId," ",member.employeeName);
  }

  deleteResourceFromProject(template: TemplateRef<any>) {
    this.cancelRequest();
    // console.log("Pri ",this.projectObj);
    let projectObj = new Project();
    projectObj.teamId = this.projectObj.teamId;
    projectObj.empId = this.projectObj.empId;
    projectObj.endDate = this.lastDate;

    // console.log("team details ", projectObj)
    this.projectService.updateProjectResourceAsInActive(projectObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.getProjectInfo();
      }
    })
  }

  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  sortData(sort: Sort){
    //console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }
  
  onSearch(searchData){
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  goBack(): void {
    this.location.back();
  }

}
