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
import { EmployeeService } from 'src/app/services/employee.service';


@Component({
  selector: 'app-team-dashboard',
  templateUrl: './team-dashboard.component.html',
  styleUrls: ['./team-dashboard.component.css']
})
export class TeamDashboardComponent implements OnInit {
  currentUser:User;
  teamMemberColumns:any[] = ['blank','name','department','totalGoals','goalsCompleted'];

  isSearchEnabled:boolean = false;
  filters:any = {};
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  viewTeamMemberList = []; 
  selectedEmployees = []; 
  goalTemplates = []; 
  selectedGoalTemplate: string = ''; 
  isSelectAll = false; 
  

  constructor(
    private router: Router,
    private http: HttpClient,
    private modalService: BsModalService,
    private teamViewService : TeamViewService,
    private authenticationService : AuthenticationService,
    private employeeService:EmployeeService,

  ) {    
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  employees: any [] = [{  }];
  goals: any[] = [];
  modalRef?: BsModalRef;

  ngOnInit() {
    this.fetchTeamMembers(); 
    this.fetchGoalTemplates();
  }

  fetchTeamMembers() {
    
  }

  fetchGoalTemplates() {
    
  }

  selectAll(event: any) {
    this.isSelectAll = event.target.checked;
    this.viewTeamMemberList.forEach((member) => {
      member.isSelected = this.isSelectAll;
    });
    this.updateSelectedEmployees();
  }

  updateSelectedEmployees() {
    this.selectedEmployees = this.viewTeamMemberList.filter((member) => member.isSelected);
  }

  openBulkAssignModal(template: any) {
    this.updateSelectedEmployees();
    if (this.selectedEmployees.length > 0) {
      this.modalRef = this.modalService.show(template);
    }
  }

  getAllTeamPerformance(){
    this.viewTeamMemberList = [];
    let employeeObj = new Employee();
    // this.teamViewService.getAllTeamPerformance(employeeObj).subscribe((response : any) => {
    //   if (response.serviceStatus == 'success') {
    //     this.viewTeamMemberList = response.responseData;
    //   }
    //   else{
    //     console.error(response.serviceResponse);
    //   }
    // });
  }
  

  openAssignGoalsGroupModal() {
    this.modalRef = this.modalService.show(AssignGoalsGroupComponent, {
      class: 'modal-lg',
      initialState: {
        selectedTeam: 'Team 1' 
      }
    });
  }

  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
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
      
    }


    getEmployeePerformance(viewTeamMember: Employee) {
          this.employeeService.setEmployee(viewTeamMember);
      
          this.router.navigate(['/view-performance']);
        }


  assignGoalsToEmployees(modalRef: BsModalRef) {
   
  }
}
