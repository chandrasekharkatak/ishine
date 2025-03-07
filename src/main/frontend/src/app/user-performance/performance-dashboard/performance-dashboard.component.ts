import { Component, OnInit, TemplateRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';
import { FormBuilder, FormGroup } from '@angular/forms';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';


interface Goal {
  id: number;
  name: string;
  description: string;
  checkpoints: { id: number; remark: string }[];
  managerRemark: string;
  employeeRemark: string;
}

@Component({
  selector: 'app-performance-dashboard',
  templateUrl: './performance-dashboard.component.html',
  styleUrls: ['./performance-dashboard.component.css']
})
export class PerformanceDashboardComponent implements OnInit {

  currentUser:User;
  activeTab: string = 'kra-kpi';
  selectedQuarter: string = 'Q1';
  quarters = ['Q1', 'Q2', 'Q3', 'Q4'];

  kraKpiMetrics: any[] = [];
  questionnaireQuestions: any[] = [];

  kraKpiReviewForm: FormGroup;
  questionnaireReviewForm: FormGroup;

  goals: Goal[] = [{
    id: 1,
    name: 'Goalname',
    description: 'goalDescription',
    checkpoints: [],
    managerRemark: '',
    employeeRemark: ''
  }];
  selectedGoal: any;
  modalRef?: BsModalRef;

  constructor(
    private http: HttpClient, 
    
    private modalService: BsModalService,
    private fb: FormBuilder,
    private authenticationService : AuthenticationService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
  //  this.fetchGoals();
  }

  fetchGoals(): void {
    // this.goalService.getGoals().subscribe((data) => {
    //   this.goals = data;
    // });
  }


  setActiveTab(tab: string) {
    this.activeTab = tab;
    this.fetchReviewData();
  }

  openModal(template: TemplateRef<any>, goal: any): void {
    this.selectedGoal = goal;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }


  fetchReviewData() {
    if (this.activeTab === 'kra-kpi') {
      this.http.get(`/api/kra-kpi/review?quarter=${this.selectedQuarter}`)
        .subscribe((data: any) => {
          this.kraKpiMetrics = data;
          this.initializeKraKpiForm();
        });
    }

    if (this.activeTab === 'questionnaire') {
      this.http.get(`/api/questionnaire/review?quarter=${this.selectedQuarter}`)
        .subscribe((data: any) => {
          this.questionnaireQuestions = data;
          this.initializeQuestionnaireForm();
        });
    }
  }

  initializeKraKpiForm() {
    let formControls: any = {};
    this.kraKpiMetrics.forEach(metric => {
      formControls[metric.id] = [0];  // Default value
    });
    this.kraKpiReviewForm = this.fb.group(formControls);
  }

  initializeQuestionnaireForm() {
    let formControls: any = {};
    this.questionnaireQuestions.forEach(question => {
      formControls[question.id] = [0];  // Default value
    });
    this.questionnaireReviewForm = this.fb.group(formControls);
  }


  submitKraKpiReview() {
    let payload = { quarter: this.selectedQuarter, scores: this.kraKpiReviewForm.value };
    this.http.post(`/api/kra-kpi/review/submit`, payload)
      .subscribe(response => console.log('KRA/KPI Review submitted:', response));
  }

  submitQuestionnaireReview() {
    let payload = { quarter: this.selectedQuarter, scores: this.questionnaireReviewForm.value };
    this.http.post(`/api/questionnaire/review/submit`, payload)
      .subscribe(response => console.log('Questionnaire Review submitted:', response));
  }
  
}