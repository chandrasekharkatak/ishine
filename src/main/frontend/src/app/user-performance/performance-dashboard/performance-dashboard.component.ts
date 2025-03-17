import { Component, OnInit, TemplateRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';
import { FormBuilder, FormGroup } from '@angular/forms';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { Employee } from 'src/app/models/employee';
import { EmployeeService } from 'src/app/services/employee.service';
import { PerformanceService } from 'src/app/services/performance.service';
import { GoalService } from 'src/app/services/goal.service';

interface Goal {
  id: number;
  name: string;
  description: string;
  checkpoints: { id: number; remark: string }[];
  managerRemark: string;
  employeeRemark: string;
  assignedBy: string;
  employeeName: string;
  dueDate: string;
  assignedDate: string;
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

  stats: {
    goalsCompleted: number;
    goalsRemaining: number;
    kraKpiScore: string;
    questionnaireScore: string;
  };

  goals: Goal[] = [{
    id: 1,
    name: 'Goalname',
    description: 'goalDescription',
    checkpoints: [],
    managerRemark: '',
    employeeRemark: '',
    assignedBy:'',
    assignedDate: '',
    dueDate: '',
    employeeName: '',
  },
  {
    id: 1,
    name: 'Goalname2',
    description: 'goalDescription',
    checkpoints: [],
    managerRemark: '',
    employeeRemark: '',
    assignedBy:'',
    assignedDate: '',
    dueDate: '',
    employeeName: '',
  }];


  currentEmployeeInfo:Employee = new Employee();
  selectedGoal: any;
  modalRef?: BsModalRef;

  constructor(
    private http: HttpClient, 
    private employeeService:EmployeeService,
    private modalService: BsModalService,
    private fb: FormBuilder,
    private authenticationService : AuthenticationService,
    private performanceService:PerformanceService,
    private goalService:GoalService
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
  //  this.fetchGoals();
  this.onGetEmployeeInfo();
  this.loadPerformanceStats();
  }

  fetchGoals(): void {
    // this.goalService.getGoals().subscribe((data) => {
    //   this.goals = data;
    // });
  }

  loadPerformanceStats(): void {
    const empId = this.currentUser?.empId;
    const quarter = this.selectedQuarter;

    if (!empId || !quarter) return;

    this.performanceService.getPerformanceStats(empId, quarter).subscribe({
      next: (data) => {
        this.stats = data;
      },
      error: (err) => {
        console.error('Error fetching performance stats:', err);
      },
    });
  }

  

  async onGetEmployeeInfo(){
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;

    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;

    } else {
      console.error(response.serviceResponse);
    }
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
  
  onQuarterChange(){
    
  }
  

  addCheckpoint() {
    this.selectedGoal.checkpoints.push('');
  }
  
  removeCheckpoint(index: number) {
    this.selectedGoal.checkpoints.splice(index, 1);
  }
  
  saveUpdates() {
    const payload = {
      statusPercentage: this.selectedGoal.statusPercentage,
      checkpoints: this.selectedGoal.checkpoints,
      employeeRemark: this.selectedGoal.employeeRemark,
      managerRemark: this.selectedGoal.managerRemark,
    };
  
    this.goalService.updateGoal(this.selectedGoal.id, payload).subscribe(
      (response) => {
        alert('Updates saved successfully!');
        this.modalRef.hide();
      },
      (error) => {
        console.error('Error saving updates:', error);
      }
    );
  }
  
}