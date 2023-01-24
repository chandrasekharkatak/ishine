import { Component, OnInit } from '@angular/core';
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

  projectObj:Project = new Project();
  teamObj:Team = new Team();
  employeeObj: Employee = new Employee();

  isProjectTable:boolean = false;
  isEditProject:boolean = false;
  isUpdation:boolean = false;

  allProjectList:any[] = [];
  allDeptList:any[] = [];
  filteredDeptList:any[] = [];
  teamLeadsList:any[] = [];
  employeeListByDept: Employee[] = [];
  allTeamMembers: any[] = [];

  constructor(
    private departmentService: DepartmentService,
    private employeeService: EmployeeService,
  ) {}


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
    this.getAllProjects();
  }

  showEditProjectForm(project: any){
    this.isEditProject = true;

    this.isProjectTable = false;
    this.projectObj = Object.assign({}, project);
    this.getAllDepartmentList(project);
  }

  getAllProjects(){
    fetch('https://poportal.apmosys.com/PoPortal/project/fixedCost/getAllProjects').then(res => res.json()).then(data => {
      this.allProjectList = data;
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

  getAllEmployeesByRole() {
    this.teamLeadsList = [];
    let employeeList = [];

    this.employeeObj.role = "TeamLead";
    this.employeeService.getAllEmployeesByRole(this.employeeObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        employeeList = response.serviceResponse;
        console.log("employeeList By Role : ", employeeList);
        console.log("Department Selected : ", this.teamObj.departmentList);

        let filterDepartmentList = this.employeeListByDept.map(y => y.departmentId);
        this.teamLeadsList = employeeList.filter(x => this.teamObj.departmentList?.includes(x.departmentId));
        this.teamLeadsList = this.teamLeadsList.sort((a, b) => a.name.localeCompare(b.name));
        console.log("teamLeadsList : ", this.teamLeadsList)
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllEmployeesByDepartmentIds() {
    this.employeeListByDept = [];

    let empObj = new Employee();
    empObj.departmentList = this.teamObj.departmentList?.map(deptId => {
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
