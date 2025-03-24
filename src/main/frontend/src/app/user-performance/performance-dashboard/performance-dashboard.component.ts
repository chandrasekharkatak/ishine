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
import { Feature } from 'src/app/models/feature';
import { LogService } from 'src/app/services/log.service';
import { Log } from 'src/app/models/log';

interface Goal {
  progress: any;
  id: number;
  goalTitle: string;
  description: string;
  checkpoints: any[];
  managerRemark: string;
  employeeRemark: string;
  assignedBy: string;
  employeeName: string;
  expectedCompletionDate: string;
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

interface QuestionDTO {
response: any;
  id: number;
  questionText: string;
  // Add other fields as needed
}

@Component({
  selector: 'app-performance-dashboard',
  templateUrl: './performance-dashboard.component.html',
  styleUrls: ['./performance-dashboard.component.css']
})
export class PerformanceDashboardComponent implements OnInit {
  feature="performance_dashboard";
  userMapping:any = {};
  log:Log;
  responses = [];
  currentUser: User;
  activeTab: string = 'kra-kpi';
  selectedQuarter: number;
  quarterCyclesList: any;
  selectedQuarter1: any;
  kraKpiMetrics: any[] = [];
  questionnaireQuestions: QuestionDTO[] = [];
  selectedgoalProgress: any;
  kraKpiReviewForm: FormGroup;
  questionnaireReviewForm: FormGroup;
  stats: Stats = {
    goalsCompleted: 0,
    goalsRemaining: 0,
    kraKpiScore: '',
    questionnaireScore: '',
  };

  goals: Goal[] = [];
  summary?: AppraisalSummary;

  currentEmployeeInfo: Employee = new Employee();
  selectedGoal?: Goal;
  modalRef?: BsModalRef;
  errorMessage: string;
  currentQuestionnaireId: any;

  constructor(
    private http: HttpClient, 
    private employeeService: EmployeeService,
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

    this.onGetEmployeeInfo();
    this.fetchQuarters();
    this.fetchGoals();
    this.loadPerformanceStats();

    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });


  }
  async onGetEmployeeInfo() {
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;

    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;
      console.log('Employee info:', this.currentEmployeeInfo);
      this.fetchQuarters();
    } else {
      console.error(response.serviceResponse);
    }
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
    
    // Clear existing questions when quarter changes
    this.questionnaireQuestions = [];
    
    this.loadPerformanceStats();
    this.loadReviewData(); // This is commented out in your original code
    this.fetchGoals();
    
    // Only load questionnaire questions if we're on the questionnaire tab
    if (this.activeTab === 'questionnaire') {
      this.loadQuestionnaireQuestions();
    }
  }

  onQuarterChange1(): void {
    if (!this.selectedQuarter) return;
    
    // Clear existing questions when quarter changes
    this.questionnaireQuestions = [];
    
    // this.loadPerformanceStats();
    // this.loadReviewData(); // This is commented out in your original code
    // this.fetchGoals();
    
    // Only load questionnaire questions if we're on the questionnaire tab
    if (this.activeTab === 'questionnaire') {
      this.loadQuestionnaireQuestions();
    }
  }


  loadAppraisalSummary(): void { 
    const empId = this.currentEmployeeInfo.empId;
    if (!empId) return;

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
        console.log('STATS::: ',this.stats)
      },
      error: (err) => {
        console.error('Error fetching performance stats:', err);
      },
    });
  }

  // loadQuestionnaireQuestions(): void {
  //   const quarterId = this.selectedQuarter;
  //   const departmentId = this.currentEmployeeInfo.departmentId;
    
  //   if (!quarterId || !departmentId) return;
    
  //   this.performanceService.getQuestionnaireByQuarterAndDepartment(quarterId, departmentId).subscribe({
  //     next: (response: any) => {
  //       if (response.serviceStatus === 'Success') {
  //         this.questionnaireQuestions = response.serviceResponse;
  //       } else {
  //         console.error('Failed to load questionnaire questions:', response.serviceMessage);
  //       }
  //     },
  //     error: (error) => {
  //       console.error('Error fetching questionnaire questions:', error);
  //     }
  //   });
  // }

  setActiveTab(tab: string) {
    this.activeTab = tab;
    
    // Only load questionnaire data when switching to that tab
    if (tab === 'questionnaire' && this.questionnaireQuestions.length === 0) {
      this.loadQuestionnaireQuestions();
    }
  }

  openModal(template: TemplateRef<any>, goal: any): void {
    this.selectedGoal = goal;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  loadReviewData(): void {
    const empId = this.currentUser?.empId;
    const quarter = this.selectedQuarter1;
    if (!empId) return;
    this.performanceService.getKraKpiReview(empId, quarter).subscribe({
      next: (data) => {
        if (data.serviceStatus === 'Success') {
          this.kraKpiMetrics = data.serviceResponse;
        }
      },
      error: err => console.error('Error fetching KRA/KPI metrics:', err)
    });

    this.performanceService.getQuestionnaireReview(empId, quarter).subscribe({
      next: (data) => {
        if (data.serviceStatus === 'Success') {
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
  
  // Add these methods to your PerformanceDashboardComponent class

initQuestionnaireForm(): void {
  const formGroup = this.fb.group({});
  
  // Add form controls for each question
  this.questionnaireQuestions.forEach(question => {
    formGroup.addControl('question_' + question.id, this.fb.control(''));
    formGroup.addControl('rating_' + question.id, this.fb.control(''));
  });
  
  this.questionnaireReviewForm = formGroup;
}

loadQuestionnaireQuestions(): void {
  // Add a check to prevent redundant calls
  if (this.questionnaireQuestions.length > 0) {
    // Questions are already loaded, just initialize the form
    this.initQuestionnaireForm();
    return;
  }
  
  const quarterId = this.selectedQuarter1;
  const departmentId = this.currentEmployeeInfo.departmentId;
  
  if (!quarterId || !departmentId) return;
  
  // Fix: Updated the API endpoint to match the required format
  this.http.get(`http://localhost:8081/api/questionnaires/getQuestionnaireByQuarterAndDepartment/${quarterId}/${departmentId}`)
    .subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.questionnaireQuestions = response.serviceResponse[0].questions;
          this.currentQuestionnaireId = response.serviceResponse[0].questionId;
          console.log('Questionnaire response:',this.questionnaireQuestions);

          this.initQuestionnaireForm();
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
    questionId: this.currentQuestionnaireId
  };

  

  this.performanceService.submitQuestionnaireResponses(response).subscribe({
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
  saveUpdates() {
    const payload = {
      GoalProgress: this.selectedgoalProgress,
      // checkpoints: this.selectedGoal.checkpoints,
      employeeRemark: this.selectedGoal.employeeRemark,
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