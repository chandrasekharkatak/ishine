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
import { Feature } from 'src/app/models/feature';
import { LogService } from 'src/app/services/log.service';
import { Log } from 'src/app/models/log';

interface Goal {
  goalStatus: string;
  goalProgress: any;
  goalId: number;
  goalTitle: string;
  description: string;
  checkpoints: any[];
  managerRemark: string;
  employeeRemark: string;
  assignedBy: string;
  quarter: string;
  expectedCompletionDate: string;
  createdDate: string;
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
  feature="view_performance";
  userMapping:any = {};
  log:Log;
  activeTab: string = 'kra-kpi';
  quarterCyclesList: any;
  selectedQuarter:any;
  selectedQuarter1:any;

  kraKpiMetrics: any[] = [];
  questionnaireQuestions: any[] = [];
  currentQuestionnaireId: any;

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
  selectedEmployee: any ;
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
    private goalService:GoalService,
    private logService:LogService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.logService.updateLogInfo(this.log);

    this.subscription = this.employeeService.employee$.subscribe(emp => {
      this.selectedEmployee = emp;
    });
    this.onGetEmployeeInfo();
    this.fetchQuarters();
    this.fetchGoals();
    this.loadPerformanceStats();
    this.setActiveTab('kra-kpi');

    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
  }

  fetchQuarters(): void {
    this.performanceService.getAllQuarterCycles().subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.quarterCyclesList = response.serviceResponse;
          this.selectedQuarter = this.quarterCyclesList[0].quarterId;
          
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
    if (!empId) return;
    const quarter = Number(this.selectedQuarter);
    this.goalService.getGoalsByEmployeeAndQuarter(empId, quarter).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.goals = response.serviceResponse; 
          console.log('list of goals',this.goals);
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
      next: (response: any) => {
        this.stats = response.serviceResponse;
        console.log('STATS::: ',this.stats);
      },
      error: (err) => {
        console.error('Error fetching performance stats:', err);
      },
    });
  }

  

  async onGetEmployeeInfo(){
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.selectedEmployee.id;

    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;
      console.log(this.currentEmployeeInfo);
    } else {
      console.error(response.serviceResponse);
    }
  }


  setActiveTab(tab: string) {
    this.activeTab = tab;

  }

  openModal(template: TemplateRef<any>, goal: any): void {
    this.selectedGoal = goal;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }


 

  // loadReviewData(): void {
  //   const empId = this.currentUser?.empId;
  //   const quarter = this.selectedQuarter1;
  //   if (!empId) return;
  //   this.performanceService.getKraKpiReview(empId, quarter).subscribe({
  //     next: (data) => {
  //       if (data.serviceStatus === 'Success') {
  //         this.kraKpiMetrics = data.serviceResponse;
  //       }
  //     },
  //     error: err => console.error('Error fetching KRA/KPI metrics:', err)
  //   });

  //   this.performanceService.getQuestionnaireReview(empId, quarter).subscribe({
  //     next: (data) => {
  //       if (data.serviceStatus === 'Success') {
  //         this.questionnaireQuestions = data.serviceResponse;
  //       }
  //     },
  //     error: err => console.error('Error fetching questionnaire:', err)
  //   });
  // }

  loadQuestionnaireQuestions(): void {
    const quarterId = this.selectedQuarter1;
    const departmentId = this.currentEmployeeInfo.departmentId;

    this.questionnaireQuestions = [];
    this.currentQuestionnaireId = null;    

    this.http.get(`http://localhost:8081/api/questionnaires/getQuestionnaireByQuarterAndDepartment/${quarterId}/${departmentId}`)
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === 'Success') {
            this.questionnaireQuestions = response.serviceResponse[0].questions;
            this.currentQuestionnaireId = response.serviceResponse[0].questionId;
            // console.log('Questionnaire response:',this.questionnaireQuestions);
            console.log('length of questionnaire: ', this.questionnaireQuestions.length);
  
           
          } else {
            console.error('Failed to load questionnaire questions:', response.serviceMessage);
          }
        },
        error: (error) => {
          console.error('Error fetching questionnaire questions:', error);
        }
      });
  }

  saveQuestionnaireResponses(): void {
    console.log("Response ======> "+ JSON.stringify(this.questionnaireQuestions));
  
    const response = {
      questions: this.questionnaireQuestions,
    };

    const empId = this.currentEmployeeInfo.empId;
    const quarterId = this.selectedQuarter1;
    
  
    this.performanceService.submitQuestionnaireResponses(response,empId,quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          alert('Questionnaire responses submitted successfully!');
        } else {
          alert('Failed to submit responses: ' + response.serviceMessage);
        }
      },
      error: (error) => {
        console.error('Error submitting questionnaire responses:', error);
        alert('An error occurred while submitting responses. Please try again.');
      }
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
  
    this.goalService.updateGoal(this.selectedGoal.goalId, payload).subscribe(
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