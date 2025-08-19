import { Component, Input, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { Feature } from 'src/app/models/feature';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';
import { ExportExcelService } from 'src/app/services/export-excel.service';

@Component({
  selector: 'app-view-employee',
  templateUrl: './view-employee.component.html',
  styleUrls: ['./view-employee.component.css']
})
export class ViewEmployeeComponent implements OnInit {

  @Input()
  allEmployeeData: any[] = [];
  allEmployeeDataException: any[] = [];
  @Input()
  catagory: any;
  sortColumn: any;
  sortColumnType: any;
  sortDirection = 'asc';
  allProjectTable: boolean = false;
  tableColumns :any[]= ['blank','blank','employeementIdAccToET','name','department','billableType','billable','projectName','clientName','apmosysRM','clientRM','poNo','poProjectType','poStartDate','poEndDate','projectManagerName','teamName','employeeRole','status'];
  tableColumnsInternal :any[]= ['blank','blank','employeementIdAccToET','name','department','billableType','billable','projectName','clientName','projectManagerName','teamName','employeeRole','status'];
  tableColumnsNotMapped : any[] = ['blank','employmentIdAcToET','name','deptName','billableType','managerName','jobRoleName'] ;
  tableColumnsWithoutBillability : any[] = ['blank','employmentIdAcToET','name','departmentName','managerName','jobRoleName'];
  exceptionTableColumns: any[] = ['blank','blank','employmentId','employeeName','department','billableType','projectName','clientName','apmosysRM','clientRM','poNumber','poProjectType','poStartDate','poEndDate'];
  tableColumnsBench :any[]= ['blank', 'blank', 'employeementId', 'name', 'department', 'billableType', 'billable', 'onBenchDate', 'daysOnBench', 'projectName', 'clientName', 'projectManagerName', 'teamName', 'employeeRole', 'status', 'apmosysRM', 'clientRM', 'poNo', 'poProjectType', 'poStartDate', 'poEndDate'];
  projectInfoColumns: string[] = ['blank', 'blank', 'projectName', 'apmosysRM', 'clientRM', 'poStartDate', 'poEndDate', 'poNo', 'clientName', 'projectManagerName', 'teamName', 'employeeName', 'jobRole', 'deptName', 'mobileNo', 'email', 'billable', 'billableType', 'effectiveStartDate'];
  filters: any = {};
  page = 1;
  isSearchEnabled: boolean = false;
  excelName : any;
  filtersBench: any = {};
  filteredEmployeeData: any[] = [];
  expandedEmployees: Set<number> = new Set();
  expandedProjects = new Set<number>();
  feature = "Resource Management";
  userMapping: any = {};
  currentUser: User;

  constructor(private exportExcelService :ExportExcelService,
    private breadcrumbService: BreadcrumbService,
    private authenticationService: AuthenticationService,
  ) {this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
      featureMap.subFeatures?.forEach(sub => {
        this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    // this.exportToExcel();
    console.log(this.catagory,"catagory")
    console.log(this.allEmployeeData,"allEmployeeData")

    if(this.catagory !== 'Unfilled Timesheet Projects') {
      this.allEmployeeData = this.processEmployeeDataForSearch(this.allEmployeeData);
    }
    
    this.filteredEmployeeData = [...this.allEmployeeData];
  }

processEmployeeDataForSearch(employees: any[]): any[] {
  return employees.map(employee => {
    if (!employee.hasOwnProperty('projectManagerName')) {
      if (employee.rmgprojects && employee.rmgprojects.length > 0) {
        employee.projectManagerName = employee.rmgprojects
          .flatMap(project => project.projectManagers || [])
          .map(manager => manager.projectManagerName)
          .filter(name => name)
          .join(', ');
      } else {
        employee.projectManagerName = '';
      }
    }
    return employee; 
  });
}

  console(projectManagerId: any) {
    console.log(projectManagerId, "projectManagerId");
  }

  toggleEmployeeExpansion(employeeIndex: number): void {
    const globalIndex = this.getGlobalEmployeeIndex(employeeIndex);
    if (this.expandedEmployees.has(globalIndex)) {
      this.expandedEmployees.delete(globalIndex);
    } else {
      this.expandedEmployees.add(globalIndex);
    }
  }

  isEmployeeExpanded(employeeIndex: number): boolean {
    const globalIndex = this.getGlobalEmployeeIndex(employeeIndex);
    return this.expandedEmployees.has(globalIndex);
  }

  getGlobalEmployeeIndex(localIndex: number): number {
    return (this.page - 1) * 5 + localIndex;
  }

  hasMultipleSubRows(employee: any): boolean {
    if (!employee.rmgprojects) return false;
    const totalSubRows = employee.rmgprojects.reduce((acc: number, project: any) => {
      return acc + (project.rmgTeam?.length || 0);
    }, 0);
    return totalSubRows > 1;
  }

toggleProjectExpansion(projectIndex: number): void {
  const globalIndex = this.getGlobalProjectIndex(projectIndex);
  if (this.expandedProjects.has(globalIndex)) {
    this.expandedProjects.delete(globalIndex);
  } else {
    this.expandedProjects.add(globalIndex);
  }
}

isProjectExpanded(projectIndex: number): boolean {
  const globalIndex = this.getGlobalProjectIndex(projectIndex);
  return this.expandedProjects.has(globalIndex);
}

// getGlobalProjectIndex(localIndex: number): number {
//   return (this.page - 1) * 5 + localIndex;
// }

hasMultipleSubRows1(project: any): boolean {
  if (!project.teamDetails) return false;
  
  const totalEmployees = project.teamDetails.reduce((acc: number, team: any) => {
    return acc + (team.mappedEmployeeDetails?.length || 0);
  }, 0);
  
  return totalEmployees > 1;
}

getHierarchicalSrNo1(pIndex: number, tIndex: number, eIndex: number): string {
  const project = this.allEmployeeData[(this.page - 1) * 5 + pIndex];
  const totalTeams = project.teamDetails?.length || 0;
  const totalEmployees = project.teamDetails?.[tIndex]?.mappedEmployeeDetails?.length || 0;
  const globalIndex = this.getGlobalProjectIndex(pIndex) + 1;

  if (totalTeams > 1 || totalEmployees > 1) {
    return `${globalIndex}.${tIndex + 1}.${eIndex + 1}`;
  } else {
    return `${globalIndex}`;
  }
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
    this.page = 1;
    this.filters = searchData;
    // console.log("Updated Filter : ", this.filters);
  }

  handlePageChange(event) {
    this.page = event;
  }
  getLength(data: any[]): number {
    // console.log(data);
    if (!data) return 0;
    else return data.length;
  }
  getRowspanForProject(employee: any): number {
    if (!employee.rmgprojects) return 0;
    return employee.rmgprojects.reduce((acc: number, team: any) => {
      return acc + (team.rmgTeam?.length || 0);
    }, 0);
  }

  //    getHierarchicalSrNo(eIndex: number, pIndex: number, tIndex: number): string {
  //   const globalProjectIndex = this.getGlobalProjectIndex(eIndex) + 1;
  //   return `${globalProjectIndex}.${pIndex + 1}.${tIndex + 1}`;
  // }
  getHierarchicalSrNo(eIndex: number, pIndex: number, tIndex: number): string {
    const employee = this.allEmployeeData[(this.page - 1) * 5 + eIndex];
    const totalProjects = employee.rmgprojects?.length || 0;
    const totalTeams = employee.rmgprojects?.[pIndex]?.rmgTeam?.length || 0;

    const globalIndex = this.getGlobalProjectIndex(eIndex) + 1;

    if (totalProjects > 1 || totalTeams > 1) {
      return `${globalIndex}.${pIndex + 1}.${tIndex + 1}`;
    } else {
      return `${globalIndex}`;
    }
  }

  getGlobalProjectIndex(localPIndex: number): number {
    const itemsPerPage = 5; // Match with HTML
    return (this.page - 1) * itemsPerPage + localPIndex;
  }
  getGlobalProjectIndex1(localPIndex: number): number {
    const itemsPerPage = 10; 
    return (this.page - 1) * itemsPerPage + localPIndex;
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  nameListToString(listData: any[]): string {
    if (!listData || listData.length === 0) {
      return '';
    }
    return listData.map(item => item.projectManagerName).join(', ');
  }

  exportToExcel(): void {
    this.excelName = 'Employee_Details_Report.xlsx';

    const dataForTable = []
    this.allEmployeeData.forEach(employee => {
      employee.rmgprojects.forEach(empProject => {
        empProject.rmgTeam.forEach(data => {
          dataForTable.push({
            "Employment Id": "A-" + employee.employeementId,
            "Employee Name": employee.name,
            "Department": employee.department,
            "Billable Type": employee.billableType,
            "Is Billable": employee.billable,
            "Project Name": empProject.projectName,
            "Client Name": empProject.clientName,
            "Apmosys RM": empProject.apmosysRM,
            "Client RM": empProject.clientRM,
            "PO No.": empProject.poNo,
            "PO Project Type": empProject.poProjectType,
            "PO Start Date": empProject.poStartDate,
            "PO End Date": empProject.poEndDate,
            "Manager Name": "NA",
            "Team Name": data.teamName,
            "Employee Role": data.employeeRole,
            "Status": data.status
          })
        });
      });
    });
    console.log(dataForTable, "dataForTable")
    console.log(this.excelName, "this.excelName")
    this.exportExcelService.exportTableDataToExcel(dataForTable, this.excelName)
  }

  get filteredEmployeeList() {
    if (!this.filters || Object.keys(this.filters).length === 0) {
      return this.allEmployeeData;
    }

    const filterText = (val: any) => val?.toString().toLowerCase() || '';

    return this.allEmployeeData.map(employee => {
      const filteredProjects = employee.rmgprojects.map(project => {
        const filteredTeams = project.rmgTeam.filter(team =>
          Object.keys(this.filters).every(key => {
            const searchValue = this.filters[key]?.toLowerCase() || '';

            return (
              filterText(employee[key]).includes(searchValue) ||
              filterText(project[key]).includes(searchValue) ||
              filterText(team[key]).includes(searchValue)
            );
          })
        );

        return {
          ...project,
          rmgTeam: filteredTeams
        };
      }).filter(project => project.rmgTeam.length > 0);

      return {
        ...employee,
        rmgprojects: filteredProjects
      };
    }).filter(employee => employee.rmgprojects.length > 0);
  }


  // exportExceptionToExcel1(): void {
  //   const excelName = 'Exception_Employee_Details_Report.xlsx';

  //   const dataForTable = this.allEmployeeData.map((employee: any) => {
  //     return {
  //       "Employment Id": "A-" + employee.employmentId,
  //       "Employee Name": employee.employeeName,
  //       "Department": employee.department,
  //       "Billable Type": employee.billableType,
  //       "Project Name": employee.projectName,
  //       "Client Name": employee.clientName,
  //       "Apmosys RM": employee.apmosysRM,
  //       "Client RM": employee.clientRM,
  //       "PO No.": employee.poNumber,
  //       "PO Project Type": employee.poProjectType,
  //       "PO Start Date": employee.poStartDate,
  //       "PO End Date": employee.poEndDate
  //     };
  //   });

  //   this.exportExcelService.exportTableDataToExcel(dataForTable, excelName);
  // }

exportToExcelBench(): void {
  const excelName = 'Exception_Employee_Details_Report.xlsx';

  const dataForTable: any[] = [];

  this.allEmployeeData.forEach((employee: any) => {
    employee.rmgprojects?.forEach((project: any) => {
      project.rmgTeam?.forEach((team: any) => {
        dataForTable.push({
          "Employment Id": employee.employeementId,
          "Employee Name": employee.name,
          "Department": employee.department,
          "Billable Type": employee.billableType,
          "Billable": employee.billable,
          "On Bench Date": employee.onBenchDate,
          "Days On Bench": employee.daysOnBench,
          "Project Name": project.projectName,
          "Client Name": project.clientName,
          "Apmosys RM": project.apmosysRM,
          "Client RM": project.clientRM,
          "PO No.": project.poNo,
          "PO Project Type": project.poProjectType,
          "PO Start Date": project.poStartDate,
          "PO End Date": project.poEndDate,
          "Project Managers": this.nameListToString(project.projectManagers),
          "Team Name": team.teamName,
          "Employee Role": team.employeeRole,
          "Status": team.status
        });
      });
    });
  });

  this.exportExcelService.exportTableDataToExcel(dataForTable, excelName);
}

onBenchSearch(searchData: any) {
  this.page = 1;
  this.filtersBench = searchData;
}

get filteredBenchEmployeeList() {
  if (!this.filtersBench || Object.keys(this.filtersBench).length === 0) {
    return this.allEmployeeData;
  }

  return this.allEmployeeData.filter(employee => {
    return Object.keys(this.filtersBench).every(key => {
      const searchValue = this.filtersBench[key]?.toLowerCase?.().trim() || '';

      if (employee[key] && employee[key].toString().toLowerCase().includes(searchValue)) {
        return true;
      }

      if (employee.rmgprojects) {
        return employee.rmgprojects.some(project => {
          if (project[key] && project[key].toString().toLowerCase().includes(searchValue)) {
            return true;
          }

          if (project.rmgTeam) {
            return project.rmgTeam.some(team => {
              return team[key] && team[key].toString().toLowerCase().includes(searchValue);
            });
          }

          return false;
        });
      }

      return false;
    });
  });
}

onSearchException(searchParams: any): void {
  const searchKeys = Object.keys(searchParams);
  
  this.filteredEmployeeData = this.allEmployeeData.filter(employee => {
    return searchKeys.every(key => {
      const topLevelMatch = employee[key]?.toString().toLowerCase().includes(searchParams[key].toLowerCase());
      
      const nestedMatch = employee.rmgProjects?.some(project =>
        project[key]?.toString().toLowerCase().includes(searchParams[key].toLowerCase())
      );

      return topLevelMatch || nestedMatch;
    });
  });
}

onSearchProjects(searchParams: any): void {
  const searchKeys = Object.keys(searchParams).filter(key => searchParams[key]);
  if (searchKeys.length === 0) {
    this.filteredEmployeeData = [...this.allEmployeeData];
    this.page = 1; 
    return;
  }

  this.filteredEmployeeData = this.allEmployeeData.filter(project => {
    return searchKeys.every(key => {
      const searchValue = searchParams[key].toString().toLowerCase();
      const topLevelMatch = project[key]?.toString().toLowerCase().includes(searchValue);

      const pmMatch = project.projectManagers?.some(manager =>
        manager[key]?.toString().toLowerCase().includes(searchValue)
      );
      const teamDetailsMatch = project.teamDetails?.some(team => {
        const teamMatch = team[key]?.toString().toLowerCase().includes(searchValue);
        const employeeMatch = team.mappedEmployeeDetails?.some(employee =>
          employee[key]?.toString().toLowerCase().includes(searchValue)
        );
        return teamMatch || employeeMatch;
      });

      return topLevelMatch || pmMatch || teamDetailsMatch;
    });
  });

  this.page = 1;
  console.log(this.filteredEmployeeData, "filteredEmployeeData");
}

exportExceptionToExcel1(): void {
  const excelName = 'Exception_Employee_Details_Report.xlsx';

  const dataForTable: any[] = [];

  this.allEmployeeData.forEach((employee: any) => {
    if (employee.rmgProjects?.length) {
      employee.rmgProjects.forEach((project: any) => {
        dataForTable.push({
          "Employment Id": "A-" + employee.employmentId,
          "Employee Name": employee.employeeName,
          "Department": employee.department,
          "Billable Type": employee.billableType,
          "Project Name": project.projectName,
          "Client Name": project.clientName,
          "Apmosys RM": project.apmosysRM,
          "Client RM": project.clientRM,
          "PO No.": project.poNo,
          "PO Project Type": project.poProjectType,
          "PO Start Date": project.poStartDate,
          "PO End Date": project.poEndDate
        });
      });
    } else {
      // Optional: Handle employees with no rmgProjects
      dataForTable.push({
        "Employment Id": "A-" + employee.employmentId,
        "Employee Name": employee.employeeName,
        "Department": employee.department,
        "Billable Type": employee.billableType,
        "Project Name": '',
        "Client Name": '',
        "Apmosys RM": '',
        "Client RM": '',
        "PO No.": '',
        "PO Project Type": '',
        "PO Start Date": '',
        "PO End Date": ''
      });
    }
  });

  this.exportExcelService.exportTableDataToExcel(dataForTable, excelName);
}

exportNotMappedToExcel(): void {
  const excelName = 'Not_Mapped_Employee_Report.xlsx';

  const dataForTable = this.allEmployeeData.map((employee: any, index: number) => {
    return {
      "Sr No.": index + 1,
      "Employment Id": "A-" + employee.employeementId,
      "Employee Name": employee.name,
      "Department": employee.deptName,
      "Billable Type": employee.billableType,
      "Manager Name": employee.managerName,
      "Employee Role": employee.jobRoleName
    };
  });

  this.exportExcelService.exportTableDataToExcel(dataForTable, excelName);
}
exportWithoutBillabilityToExcel(): void {
  const excelName = 'Null_Billability_Employee_Report.xlsx';

  const dataForTable = this.allEmployeeData.map((employee: any, index: number) => {
    return {
      "Sr No.": index + 1,
      "Employment Id": "A-" + employee.employeementId,
      "Employee Name": employee.name,
      "Department": employee.departmentName,
      "Manager Name": employee.managerName,
      "Employee Role": employee.jobRoleName
    };
  });

  this.exportExcelService.exportTableDataToExcel(dataForTable, excelName);
}


  
}
