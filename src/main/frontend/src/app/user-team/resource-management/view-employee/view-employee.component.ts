import { Component, Input, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';
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
  tableColumns :any[]= ['blank','employeementId','name','department','billableType','billable','projectName','clientName','apmosysRM','clientRM','poNo','poProjectType','poStartDate','poEndDate','projectManagerName','teamName','employeeRole','status'];
  tableColumnsNotMapped : any[] = ['blank','employeementId','name','departmentName','managerName','jobRoleName'] ;
  exceptionTableColumns: any[] = ['blank','employmentId','employeeName','department','billableType','projectName','clientName','apmosysRM','clientRM','poNumber','poProjectType','poStartDate','poEndDate'];
  tableColumnsBench :any[]= ['blank','employeementId','name','department','billableType','billable','onBenchDate','daysOnBench','projectName','clientName','apmosysRM','clientRM','poNo','poProjectType','poStartDate','poEndDate','projectManagerName','teamName','employeeRole','status'];
  filters: any = {};
  page = 1;
  isSearchEnabled: boolean = false;
  excelName : any;
  filtersBench: any = {};
  filteredEmployeeData: any[] = [];

  constructor(private exportExcelService :ExportExcelService,
    private breadcrumbService: BreadcrumbService
  ) { }

  ngOnInit(): void {
    // this.exportToExcel();
    console.log(this.catagory,"catagory")
    console.log(this.allEmployeeData,"allEmployeeData")
    this.filteredEmployeeData = [...this.allEmployeeData];
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


  exportExceptionToExcel1(): void {
    const excelName = 'Exception_Employee_Details_Report.xlsx';

    const dataForTable = this.allEmployeeData.map((employee: any) => {
      return {
        "Employment Id": "A-" + employee.employmentId,
        "Employee Name": employee.employeeName,
        "Department": employee.department,
        "Billable Type": employee.billableType,
        "Project Name": employee.projectName,
        "Client Name": employee.clientName,
        "Apmosys RM": employee.apmosysRM,
        "Client RM": employee.clientRM,
        "PO No.": employee.poNumber,
        "PO Project Type": employee.poProjectType,
        "PO Start Date": employee.poStartDate,
        "PO End Date": employee.poEndDate
      };
    });

    this.exportExcelService.exportTableDataToExcel(dataForTable, excelName);
  }

exportExceptionToExcelBench(): void {
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
  
}
