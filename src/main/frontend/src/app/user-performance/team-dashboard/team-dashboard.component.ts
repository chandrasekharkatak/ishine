import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';
import { AssignGoalsGroupComponent } from '../assign-goals-group/assign-goals-group.component';
import { Sort } from '@angular/material/sort';
import { User } from 'src/app/models/user';
import { TeamViewService } from 'src/app/services/team-view.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { Employee } from 'src/app/models/employee';
import { TeamDashboardService } from 'src/app/services/team-dashboard.service';

@Component({
  selector: 'app-team-dashboard',
  templateUrl: './team-dashboard.component.html',
  styleUrls: ['./team-dashboard.component.css']
})
export class TeamDashboardComponent implements OnInit {
  currentUser: User;
  viewTeamMemberList: any[] = [];
  teamMemberColumns: any[] = ['blank', 'name', 'department', 'totalGoals', 'goalsCompleted'];

  isSearchEnabled: boolean = false;
  filters: any = {};
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  hodId: number;
  page = 1;
  loading = false;

  constructor(
    private router: Router,
    private http: HttpClient,
    private modalService: BsModalService,
    private teamViewService: TeamViewService,
    private authenticationService: AuthenticationService,
    private teamDashboardService: TeamDashboardService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  employees: any[] = [];
  goals: any[] = [];
  modalRef?: BsModalRef;

  ngOnInit() {
    if (this.currentUser && this.currentUser.hodId) {
      this.hodId = this.currentUser.hodId;
      console.log('HOD ID:', this.hodId);
      this.getEmployeesInDepartment();
    } else {
      console.error('Current user or HOD ID is undefined');
    }
  }

  getEmployeesInDepartment() {
    this.loading = true;
    this.teamDashboardService.findEmployeesInSameDepartmentAsCurrentUser(this.hodId).subscribe(
      (response: any) => {
        console.log('Raw response:', JSON.stringify(response))
        if (response && response.serviceStatus && 
            response.serviceStatus.toUpperCase() === 'SUCCESS') {
          // Map the response to match the expected structure
          this.viewTeamMemberList = response.serviceResponse.map(employee => {
            return {
              id: employee.empId,
              name: employee.name,
              department: this.currentUser.departmentName || 'N/A',
              employeementId: employee.employeementId,
              totalGoals: employee.noOfGoals || 0,
              goalsCompleted: employee.goalsCompleted || 0
            };
          });
          console.log('Employees fetched successfully:', this.viewTeamMemberList);
        } else {
          console.error('Error in response:', response);
          this.viewTeamMemberList = [];
        }
        this.loading = false;
      },
      (error) => {
        console.error('Error fetching department members:', error);
        this.viewTeamMemberList = [];
        this.loading = false;
      }
    );
  }

  openAssignGoalsGroupModal() {
    this.modalRef = this.modalService.show(AssignGoalsGroupComponent, {
      class: 'modal-lg',
      initialState: {
        selectedTeam: 'Team 1'
      }
    });
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
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

  onSearch(searchData) {
    this.filters = searchData;
  }

  getEmployeePerformance(viewTeamMember) {
    // Store employee data in session storage or service for access in performance dashboard
    sessionStorage.setItem('selectedEmployee', JSON.stringify(viewTeamMember));
    this.router.navigate(['/user-performance/performance-dashboard']);
  }
}