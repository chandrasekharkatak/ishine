import { Component, OnInit, TemplateRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';
import { FormBuilder, FormGroup } from '@angular/forms';
import { User } from 'src/app/models/user';
import { Employee } from 'src/app/models/employee';
import { EmployeeService } from 'src/app/services/employee.service';
import { PerformanceService } from 'src/app/services/performance.service';
import { GoalService } from 'src/app/services/goal.service';
import { Subscription } from 'rxjs';

interface Goal {
  id: number;
  name: string;
  description: string;
  checkpoints: { id: number; remark: string }[];
  statusPercentage: number;
  managerRemark: string;
  employeeRemark: string;
  assignedBy: string;
  employeeName: string;
  dueDate: string;
  assignedDate: string;
}

@Component({
  selector: 'app-view-performance',
  templateUrl: './view-performance.component.html',
  styleUrls: ['./view-performance.component.css']
})
export class ViewPerformanceComponent implements OnInit {

  activeTab: string = 'kra-kpi';
  selectedQuarter: string = 'Q1';
  quarters = [];

  kraKpiMetrics: any[] = [];
  questionnaireQuestions: any[] = [];

  kraKpiReviewForm: FormGroup;
  questionnaireReviewForm: FormGroup;
  selectedGoal: any;

  stats: {
    goalsCompleted: number;
    goalsRemaining: number;
    kraKpiScore: string;
    questionnaireScore: string;
  };

  goals: Goal[] = [];


  currentEmployeeInfo: Employee = new Employee();
  selectedEmployee!: Employee | null ;
  modalRef?: BsModalRef;
  subscription!: Subscription;
  constructor(
    private http: HttpClient, 
    private employeeService:EmployeeService,
    private modalService: BsModalService,
    private fb: FormBuilder,
    private performanceService:PerformanceService,
    private goalService:GoalService
  ) {
  }

  ngOnInit(): void {
  //  this.fetchGoals();
  this.onGetEmployeeInfo();
  this.loadPerformanceStats();
  this.subscription = this.employeeService.employee$.subscribe(emp => {
    this.selectedEmployee = emp;
  });

  }

  fetchGoals(): void {
    
  }

  async onGetEmployeeInfo(){
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.selectedEmployee.empId;

    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;

    } else {
      console.error(response.serviceResponse);
    }
  }

  loadPerformanceStats(): void {
    const empId = this.selectedEmployee?.empId;
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


  ngOnDestroy() {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }
  
}
