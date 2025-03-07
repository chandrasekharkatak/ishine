import { Component, Input } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { GoalsTemplateComponent } from '../goals-template/goals-template.component';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';


@Component({
  selector: 'app-assign-goals-group',
  templateUrl: './assign-goals-group.component.html',
  styleUrls: ['./assign-goals-group.component.css']
})
export class AssignGoalsGroupComponent {
  @Input() selectedTeam: string = '';
  employees: any[] = [{
    id: 1,
    name: 'Employee 1',
    assigned: false,
    goalsCount : 1
  }, {
    id: 2,
    name: 'Employee 2',
    assigned: false, 
    goalsCount : 2
  }, {
    id: 3,
    name: 'Employee 3',
    assigned: false, 
    goalsCount : 1
  }];
  teams: any[] = [{
    id: 1,
    name: 'Team 1'
  }, {
    id: 2,
    name: 'Team 2'
  }, {
    id: 3,
    name: 'Team 3'
  }];

  constructor(private http: HttpClient, public modalRef: BsModalRef,private modalService: BsModalService) {}

  ngOnInit() {
    // this.fetchTeams();
    // this.fetchEmployees();
  }

  // fetchTeams() {
  //   this.http.get('/api/teams').subscribe((data: any) => {
  //     this.teams = data;
  //   });
  // }

  // fetchEmployees() {
  //   this.http.get(`/api/employees?team=${this.selectedTeam}`).subscribe((data: any) => {
  //     this.employees = data;
  //   });
  // }

  assignToGroup(employee: any) {
    employee.assigned = true;
  }

  proceedToGoalsTemplate() {
    this.modalRef.hide();
    this.modalRef = this.modalService.show(GoalsTemplateComponent, {
      class: 'modal-xl',
      
    });
  }
}
