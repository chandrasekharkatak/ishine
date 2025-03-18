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
import { Subscription } from 'rxjs';

interface Goal {
  statusPercentage: any;
  id: number;
  name: string;
  description: string;
  checkpoints: any[];
  managerRemark: string;
  employeeRemark: string;
  assignedBy: string;
  employeeName: string;
  dueDate: string;
  assignedDate: string;
}

interface Stats {
  goalsCompleted: number;
  goalsRemaining: number;
  kraKpiScore: string;
  questionnaireScore: string;
}

interface AppraisalSummary {
  finalRating: number;
  finalRemarks: string;
  appraisalScore: number;
}

@Component({
  selector: 'app-view-performance',
  templateUrl: './view-performance.component.html',
  styleUrls: ['./view-performance.component.css']
})
export class ViewPerformanceComponent implements OnInit {

  currentUser:User;
  activeTab: string = 'kra-kpi';
  selectedQuarter: string = 'Q1';
  quarters = [];

  kraKpiMetrics: any[] = [];
  questionnaireQuestions: any[] = [];

  kraKpiReviewForm: FormGroup;
  questionnaireReviewForm: FormGroup;

  stats: Stats ={
    goalsCompleted: 0,
    goalsRemaining: 0,
    kraKpiScore: '',
    questionnaireScore: '',
  };

  goals: Goal[] = [];
  summary?: AppraisalSummary;

  currentEmployeeInfo:Employee = new Employee();
  selectedEmployee!: Employee | null ;
  selectedGoal?: Goal;
  subscription!: Subscription;
  modalRef?: BsModalRef;
  errorMessage: string;

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
    this.subscription = this.employeeService.employee$.subscribe(emp => {
      this.selectedEmployee = emp;
    });
    this.onGetEmployeeInfo();
    this.fetchQuarters();
    this.fetchGoals();
    this.loadPerformanceStats();
    this.setActiveTab('kra-kpi');
  }

  fetchQuarters(): void {
    this.performanceService.getAvailableQuarters().subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'SUCCESS') {
          this.quarters = response.serviceResponse;
          this.selectedQuarter = this.quarters[0] || '';
          this.onQuarterChange();
        } else {
          this.errorMessage = response.serviceMessage || 'Failed to load quarters.';
        }
      },
      error: (error) => {
        this.errorMessage = error.message || 'Error fetching quarters.';
      }
    });
  }

  onQuarterChange(): void {
    if (!this.selectedQuarter) return;

    this.loadPerformanceStats();
    this.loadReviewData();
    this.fetchGoals();
  }

  loadAppraisalSummary(): void {
    const empId = this.currentEmployeeInfo.empId;
    if (!empId ) return;

    this.performanceService.getAppraisalSummary(empId).subscribe({
      next: (data) => {
        this.summary = data;
      },
      error: (err) => {
        console.error('Error fetching Appraisal Summary:', err);
      },
    });
  }


  fetchGoals(): void {
    this.errorMessage = ''; 
    const empId = this.currentEmployeeInfo.empId;
    const quarter = this.selectedQuarter;

    if (!empId) return;

    this.goalService.getGoalsByEmployeeAndQuarter(empId, quarter).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'SUCCESS') {
          this.goals = response.serviceResponse; 
        } else {
          this.errorMessage = response.serviceMessage || 'No goals found for this employee.';
        }
      },
      error: (error) => {
        this.errorMessage = error.message || 'Failed to fetch employee goals.';
      },
    });
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
    currentEmp.empId = this.selectedEmployee.empId;

    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;

    } else {
      console.error(response.serviceResponse);
    }
  }


  setActiveTab(tab: string) {
    this.activeTab = tab;
    this.loadReviewData();
  }

  openModal(template: TemplateRef<any>, goal: any): void {
    this.selectedGoal = goal;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }


 

  loadReviewData(): void {
    const empId = this.currentUser?.empId;
    const quarter = this.selectedQuarter;
    if (!empId) return;
    this.performanceService.getKraKpiReview(empId, quarter).subscribe({
      next: (data) => {
        if (data.serviceStatus === 'SUCCESS') {
          this.kraKpiMetrics = data.serviceResponse;
        }
      },
      error: err => console.error('Error fetching KRA/KPI metrics:', err)
    });

    this.performanceService.getQuestionnaireReview(empId, quarter).subscribe({
      next: (data) => {
        if (data.serviceStatus === 'SUCCESS') {
          this.questionnaireQuestions = data.serviceResponse;
        }
      },
      error: err => console.error('Error fetching questionnaire:', err)
    });
  }

  
  

  addCheckpoint() {
    this.selectedGoal.checkpoints.push('');
  }
  
  removeCheckpoint(index: number) {
    this.selectedGoal.checkpoints.splice(index, 1);
  }
  
  saveUpdates() {
    const payload = {
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

  submitReview(): void {
    let reviewData;

    if (this.activeTab === 'kra-kpi') {
      reviewData = this.kraKpiMetrics.map((metric) => ({
        name: metric.name,
        rating: metric.rating,
      }));
    } else if (this.activeTab === 'questionnaire') {
      reviewData = this.questionnaireQuestions.map((question) => ({
        text: question.text,
        rating: question.rating,
      }));
    }

    const payload = {
      quarter: this.selectedQuarter,
      type: this.activeTab === 'kra-kpi' ? 'KRA/KPI' : 'Questionnaire',
      reviews: reviewData,
    };

    this.performanceService.submitReview(payload).subscribe({
      next: () => alert('Review submitted successfully!'),
      error: (err) => console.error('Error submitting review:', err),
    });
  }
  
}