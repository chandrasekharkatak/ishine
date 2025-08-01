import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Employee360Service } from '../services/employee360.service';
import { Sort } from '@angular/material/sort';

@Component({
  selector: 'app-team-employee-timesheet-view',
  templateUrl: './team-employee-timesheet-view.component.html',
  styleUrls: ['./team-employee-timesheet-view.component.css']
})
export class TeamEmployeeTimesheetViewComponent implements OnInit {

  projectId: any;
  teamMemberList: any[] = [];
  page = 1;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  filters: any = {};
  employeesColumns: any[] = ['blank', 'spoc', 'teamLeadName', 'teamName', 'employeeName', 'billableType', 'startDate', 'employeeRole'];

  constructor(private route: ActivatedRoute,
    private employee360Service: Employee360Service) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.projectId = params['projectId'];
      if (this.projectId) {
        this.getTeamInfo(this.projectId);
      } else {
        console.warn("projectId is missing in query params.");
      }
      console.log('Received projectId from query param:', this.projectId);
    });
  }

  getTeamInfo(project:any): void {
    this.employee360Service.getTeamInfo(project).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === "Success") {
          this.teamMemberList = response.serviceResponse.teamDetails;

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

  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

}
