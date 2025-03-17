import { Component, Input, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BsModalRef } from 'ngx-bootstrap/modal';

@Component({
  selector: 'app-goals-template',
  templateUrl: './goals-template.component.html',
  styleUrls: ['./goals-template.component.css']
})
export class GoalsTemplateComponent implements OnInit {
  @Input() selectedEmployees: any[] = [];

  goals: any[] = [{
    name: 'Goal 1',
    assignedDate: '04-03-2025',
    description: 'Description of Goal 1',
  
  }];
  newGoals: any[] = []; 
  showNewGoalForm: boolean = false;

  newGoal: any = {
    name: '',
    assignedDate: '',
    completionDate: '',
    description: '',
    selected: false
  };

  constructor(private http: HttpClient, public modalRef: BsModalRef) {}

  ngOnInit() {
    this.fetchGoals(); 
  }

  fetchGoals() {
    
  }

  addGoal() {
    if (this.newGoal.name && this.newGoal.assignedDate && this.newGoal.completionDate && this.newGoal.description) {
      this.newGoals.push({ ...this.newGoal });
      this.newGoal = { name: '', assignedDate: '', completionDate: '', description: '', selected: false };
      this.showNewGoalForm = false;
    }
  }

  toggleNewGoalForm() {
    this.showNewGoalForm = !this.showNewGoalForm;
  }

  assignGoals() {
    const selectedGoals = [
      ...this.goals.filter(goal => goal.selected), 
      ...this.newGoals 
    ];

    const requestBody = {
      employees: this.selectedEmployees,
      assignedGoals: selectedGoals
    };

    
  }
}

