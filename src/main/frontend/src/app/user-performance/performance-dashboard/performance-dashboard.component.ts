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

interface QuestionDTO {
response: any;
  id: number;
  questionText: string;
}

interface kpiList{
  response:any;
  id:number;
  description: string;
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
  kpiList:kpiList[] = [];
  selectedgoalProgress: any;
  kraKpiReviewForm: FormGroup;
  questionnaireReviewForm: FormGroup;
  stats: Stats = {
    goalsCompleted: 0,
    goalsRemaining: 0,
    kraKpiScore: '',
    questionnaireScore: '',
  };
  minRating = 3;
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
          this.selectedQuarter1 = this.quarterCyclesList[0].quarterId;
          // console.log('list of quarters:', this.quarterCyclesList);
          
          this.onQuarterChange();
          this.quarterChange2();
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
    this.loadAppraisalSummary();
    
  }




  loadAppraisalSummary(): void { 
    const empId = this.currentEmployeeInfo.empId;
    console.log('emp id: ',this.currentEmployeeInfo.empId);
    if (!empId) return;

    this.performanceService.getAppraisalSummary(empId).subscribe({
      next: (response) => {
        this.summary = response.serviceResponse[0];
        // console.log('appraisal summary:', this.summary);
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
          // console.log('list of goals',this.goals);
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
        // console.log('STATS::: ',this.stats);
      },
      error: (err) => {
        console.error('Error fetching performance stats:', err);
      },
    });
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
    
  }

  openModal(template: TemplateRef<any>, goal: any): void {
    this.selectedGoal = goal;
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }


quarterChange2(): void{
  this.loadKpiList();
  this.loadQuestionnaireQuestions();
}

loadQuestionnaireQuestions(): void {
  
  const quarterId = this.selectedQuarter1;
  const departmentId = this.currentEmployeeInfo.departmentId;

  this.questionnaireQuestions = [];
  this.currentQuestionnaireId = null;
  
  if (!quarterId || !departmentId) return;

    this.performanceService.getQuestionnares(departmentId, quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.questionnaireQuestions = response.serviceResponse[0].questions;
          console.log('Questionnaire response:',this.questionnaireQuestions);

        } else {
          console.error('Failed to load questionnaire questions:', response.serviceMessage);
        }
      },
      error: (error) => {
        console.error('Error fetching questionnaire questions:', error);
      }
    });
}


loadKpiList(): void {
  const quarterId = this.selectedQuarter1;
  const departmentId = this.currentEmployeeInfo.departmentId;
  
  if (!quarterId || !departmentId) return;
  
    this.performanceService.getKraKpi(departmentId, quarterId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.kpiList = response.serviceResponse[0].kpis;

        } else {
          console.error('Failed to load questionnaire questions:', response.serviceMessage);
        }
      },
      error: (error) => {
        console.error('Error fetching questionnaire questions:', error);
      }
    });

}



saveKpiResponses(): void {

  console.log("Response ======> "+ JSON.stringify(this.kpiList));

  const response = this.kpiList;
  const empId = this.currentEmployeeInfo.empId;
  const quarterId = this.selectedQuarter1;

  this.performanceService.submitKpiResponses(response,empId,quarterId).subscribe({
    next: (response: any) => {
      if (response.serviceStatus === 'Success') {
        alert('KPI responses submitted successfully!');
      } else {
        alert('Failed to submit responses: ' + response.serviceMessage);
      }
    },
    error: (error) => {
      console.error('Error submitting KPI responses:', error);
      alert('An error occurred while submitting responses. Please try again.');
    }
  });
}

saveQuestionnaireResponses(): void {
 

  const empId = this.currentEmployeeInfo.empId;
  const quarterId = this.selectedQuarter1;


  this.performanceService.submitQuestionnaireResponses(this.questionnaireQuestions,empId,quarterId).subscribe({
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
      goalProgress: this.selectedgoalProgress,
      employeeRemark: this.selectedGoal.employeeRemark,
    };
  
    this.goalService.updateGoal(this.selectedGoal.goalId, payload).subscribe(
      (response) => {
        if (response.serviceStatus === 'Success') {
          alert('Updates saved successfully!');
          this.modalRef.hide();
        }
        alert('Updates saved successfully!');
        this.modalRef.hide();
      },
      (error) => {
        console.error('Error saving updates:', error);
      }
    );
  }
}