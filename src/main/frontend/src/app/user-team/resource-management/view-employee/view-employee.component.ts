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
  tableColumns: any[] = ['blank', 'employeementId', 'name', 'department', 'billableType', 'billable', 'projectName', 'clientName', 'apmosysRM', 'clientRM', 'poNo', 'poProjectType', 'poStartDate', 'poEndDate', 'projectManagerName', 'teamName', 'employeeRole', 'status'];
  tableColumnsNotMapped: any[] = ['blank', 'employeementId', 'name', 'departmentName', 'managerName', 'jobRoleName'];
  exceptionTableColumns: any[] = ['blank', 'employmentId', 'employeeName', 'department', 'billableType', 'projectName', 'clientName', 'apmosysRM', 'clientRM', 'poNumber', 'poProjectType', 'poStartDate', 'poEndDate'];
  filters: any = {};
  page = 1;
  isSearchEnabled: boolean = false;
  excelName: any;
  constructor(private exportExcelService: ExportExcelService,
    private breadcrumbService: BreadcrumbService
  ) { }

  ngOnInit(): void {
    // this.exportToExcel();
    console.log(this.catagory, "catagory")
    console.log(this.allEmployeeData, "allEmployeeData")
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

  getHierarchicalSrNo(eIndex: number, pIndex: number, tIndex: number): string {
    const globalProjectIndex = this.getGlobalProjectIndex(eIndex) + 1;
    return `${globalProjectIndex}.${pIndex + 1}.${tIndex + 1}`;
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
  filteredEmployeeData: any;

  onSearchException(searchObj: any) {
    const searchText = (text: string) => text?.toString().toLowerCase() || '';
    this.filteredEmployeeData = [...this.allEmployeeData];
    this.filteredEmployeeData = this.allEmployeeData.filter(employee => {
      // Check top-level employee fields
      const matchesEmployee = Object.keys(searchObj).some(key => {
        const value = searchText(searchObj[key]);
        if (!value) return false;

        return (
          searchText(employee[key]).includes(value)
        );
      });

      // If not matched in employee level, check inside projects
      const matchingProjects = employee.rmgProjects?.filter(project =>
        Object.keys(searchObj).some(key => {
          const value = searchText(searchObj[key]);
          if (!value) return false;

          return (
            searchText(project[key]).includes(value)
          );
        })
      );

      // If projects match, keep only those matching projects
      if (matchingProjects?.length) {
        employee.rmgProjects = matchingProjects;
        return true;
      }

      // Return true if employee matches directly
      return matchesEmployee;
    });
  }




}
