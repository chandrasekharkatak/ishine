import { Component, Input, Output, EventEmitter, OnInit, TemplateRef, ViewChild, Optional } from '@angular/core';
import { NgbModal, NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { SurveyOption } from 'src/app/models/sureyOption';
import { SurveyQuestion } from 'src/app/models/surveyQuestion';
import { Survey } from 'src/app/models/survey';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { SurveyService } from 'src/app/services/survey.service';
import { ValidationService } from 'src/app/services/validation.service';

export interface QuizViewConfig {
  mode: 'preview' | 'attend';
  quizId?: number;
  trainingId?: number;
  cycleNumber?: number;
  contentId?: number;
  isAlreadySubmitted?: boolean;
  questions?: any[]; // For preview mode from builder
  quizTitle?: string;
  cutoffQuestions?: number;
  totalQuestions?: number;
}

@Component({
  selector: 'app-quiz-view-modal',
  templateUrl: './quiz-view-modal.component.html',
  styleUrls: ['./quiz-view-modal.component.css'],
  standalone: false
})
export class QuizViewModalComponent implements OnInit {
  @Input() config!: QuizViewConfig;
  @Input() showHeader: boolean = true;
  @Input() showSidebar: boolean = true;
  @Input() showCloseButton: boolean = true;
  @Output() quizSubmitted = new EventEmitter<any>();
  @Output() questionStateChanged = new EventEmitter<{ index: number; answered: boolean[] }>();
  
  @ViewChild('alertTemplate') alertTemplate!: TemplateRef<any>;
  
  // UI State
  activeIndex = 0;
  isLoading = false;
  alertMessage = '';
  
  // Quiz Data
  quizId: number;
  quizTitle = '';
  cutoffQuestions = 0;
  totalQuestions = 0;
  questions: SurveyQuestion[] = [];
  
  // For attend mode
  correctAnswers: { [key: string]: string } = {};
  formSubmitted = false;
  marksObtained = 0;
  passStatus = '';
  correctAnswersCount = 0;
  answeredQuestions: boolean[] = [];
  canMoveNext = false;
  showResultsSummary = false;
  isResultsBannerCollapsed = false;

  toggleReview(): void {
    this.showResultsSummary = !this.showResultsSummary;
  }

  toggleResultsBanner(): void {
    this.isResultsBannerCollapsed = !this.isResultsBannerCollapsed;
  }
  
  // User
  currentUser: User;
  
  readonly OPTION_LETTERS = ['A', 'B', 'C', 'D', 'E', 'F'];

  constructor(
    private modalService: NgbModal,
    private surveyService: SurveyService,
    private auth: AuthenticationService,
    private validationService: ValidationService,
    @Optional() public activeModal: NgbActiveModal,
  ) {
    this.currentUser = this.auth.currentUserValue;
  }

  ngOnInit(): void {
    if (this.config.mode === 'preview' && this.config.questions) {
      // Preview mode - use passed questions
      this.questions = this.config.questions;
      this.quizTitle = this.config.quizTitle || 'Quiz Preview';
      this.cutoffQuestions = this.config.cutoffQuestions || 0;
      this.totalQuestions = this.config.totalQuestions || this.questions.length;
      this.isLoading = false;
    } else if (this.config.mode === 'attend' && (this.config.quizId || this.config.trainingId)) {
      // Attend mode - fetch from API
      this.loadQuizForAttendance();
    }
  }

  loadQuizForAttendance(): void {
    this.isLoading = true;
    
    if (this.config.isAlreadySubmitted) {
      this.loadSubmittedQuiz();
    } else {
      this.loadFreshQuiz();
    }
  }

  loadFreshQuiz(): void {
    this.surveyService.getQuizQuestionByTrainingId(this.config.trainingId).subscribe({
      next: (response: any) => {
        this.questions = response.serviceResponse.allSurveyQuestionList;
        this.quizId = response.serviceResponse.quizId;
        
        this.questions.forEach((survey: SurveyQuestion) => {
          survey.optionsList = typeof survey.options === 'string' 
            ? JSON.parse(survey.options) 
            : survey.options;
          survey.required = typeof survey.required === 'string' 
            ? JSON.parse(survey.required) 
            : survey.required;
          survey.response = survey.optionType === 'checkbox' ? [] : '';
        });
        
        this.totalQuestions = this.questions.length;
        this.answeredQuestions = new Array(this.questions.length).fill(false);
        this.activeIndex = 0;
        this.isLoading = false;
        
        setTimeout(() => {
          this.checkCurrentQuestionAnswered();
        }, 100);
      },
      error: (error: any) => {
        this.showAlert(error.error?.serviceStatus || 'Failed to load quiz');
        this.isLoading = false;
      }
    });
  }

  loadSubmittedQuiz(): void {
    const surveyObj = new Survey();
    surveyObj.trainingId = this.config.trainingId;
    surveyObj.empId = this.currentUser.empId;
    surveyObj.isQuizResponse = true;
    surveyObj.isAttendingQuiz = false;

    this.surveyService.getSurveyResponseByEmpIdAndSurveyId(surveyObj).subscribe({
      next: (response: any) => {
        this.questions = response.serviceResponse.allSurveyQuestionList;
        this.quizId = response.serviceResponse.quizId;
        this.correctAnswers = response.serviceResponse.correctAnswers || {};
        this.totalQuestions = response.serviceResponse.totalQuestions || 0;
        this.correctAnswersCount = 0;
        
        this.questions.forEach((survey: SurveyQuestion) => {
          survey.optionsList = typeof survey.options === 'string' 
            ? JSON.parse(survey.options) 
            : survey.options;
          survey.required = typeof survey.required === 'string' 
            ? JSON.parse(survey.required) 
            : survey.required;
            
          if (survey.response === survey.correctAnswer) {
            this.correctAnswersCount++;
          }
          
          if (survey.cuttOffQuestions) {
            this.cutoffQuestions = survey.cuttOffQuestions;
          }
        });
        
        this.formSubmitted = true;
        this.isLoading = false;
        this.onFormChange();
      },
      error: (error: any) => {
        this.showAlert(error.error?.serviceStatus || 'Failed to load results');
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (!this.canMoveNext) {
      this.showAlert(`Please provide response for Question ${this.activeIndex + 1}`);
      return;
    }

    const form = document.getElementById('quizForm') as HTMLFormElement;
    if (!form) return;

    const surveyObj = new Survey();
    surveyObj.empId = this.currentUser.empId;
    surveyObj.type = 'quiz';
    surveyObj.createdBy = this.currentUser.empId;
    surveyObj.surveyId = this.quizId;
    surveyObj.trainingId = this.config.trainingId;
    surveyObj.cycleNumber = this.config.cycleNumber;
    surveyObj.contentId = this.config.contentId;
    surveyObj.surveyQuestionList = [];
    surveyObj.isQuizResponse = true;
    surveyObj.isAttendingQuiz = false;

    // Capture current question's state one last time
    this.onFormChange();


    // Validate
    for (let i = 0; i < this.questions.length; i++) {
      const q = this.questions[i];
      
      if (q.optionType === 'radio' && !q.response) {
        this.showAlert(`Please select an option for Question ${i + 1}`);
        return;
      } else if (q.optionType === 'checkbox' && (!q.response || (Array.isArray(q.response) && q.response.length === 0))) {
        this.showAlert(`Please select at least one option for Question ${i + 1}`);
        return;
      } else if (q.optionType === 'text' && !this.validationService.validateNullUndefinedEmptyString(q.response)) {
        this.showAlert(`Please provide response for Question ${i + 1}`);
        return;
      }
    }

    // Prepare submission
    this.questions.forEach(q => {
      const sq = new SurveyQuestion();
      sq.surveyQuestionId = q.surveyQuestionId;
      sq.required = q.required;
      sq.response = Array.isArray(q.response) ? q.response.join(', ') : q.response;
      surveyObj.surveyQuestionList.push(sq);
    });

    this.surveyService.setSurveyResponseByEmpId(surveyObj).subscribe({
      next: (response: any) => {
        const results = response.serviceResponse;
        this.correctAnswers = results.correctAnswers || {};
        this.correctAnswersCount = results.correctAnswersCount || 0;
        this.passStatus = results.passStatus || 'fail';
        this.cutoffQuestions = results.cutOffQuestion || 0;
        this.totalQuestions = results.totalQuestions || 0;
        this.formSubmitted = true;
        this.quizSubmitted.emit(this.passStatus === 'pass');
        
        // Update questions with correct answers for display
        this.questions.forEach(q => {
          q.correctAnswer = this.correctAnswers[q.question];
        });
      },
      error: (error: any) => {
        this.showAlert(error.error?.serviceStatus || 'Submission failed');
      }
    });
  }

  onFormChange(): void {
    if (this.formSubmitted || this.config.mode === 'preview') {
      this.canMoveNext = true;
      return;
    }

    const form = document.getElementById('quizForm') as HTMLFormElement;
    if (!form) return;

    const currentQ = this.questions[this.activeIndex];
    if (!currentQ) return;

    let isAnswered = false;
    if (currentQ.optionType === 'checkbox') {
      const selected = Array.from(form.querySelectorAll('input[type="checkbox"]:checked')) as HTMLInputElement[];
      currentQ.response = selected.map(i => i.value).join(', ');
      isAnswered = selected.length > 0;
    } else if (currentQ.optionType === 'radio') {
      const selected = form.querySelector('input[type="radio"]:checked') as HTMLInputElement;
      currentQ.response = selected ? selected.value : '';
      isAnswered = !!selected;
    } else {
      const input = form.elements[`question-${this.activeIndex + 1}`] as any;
      currentQ.response = input?.value || '';
      isAnswered = !!currentQ.response;
    }

    this.canMoveNext = isAnswered;
    this.answeredQuestions[this.activeIndex] = isAnswered;
    this.questionStateChanged.emit({ index: this.activeIndex, answered: this.answeredQuestions });
  }

  checkCurrentQuestionAnswered(): void {
    setTimeout(() => this.onFormChange(), 50);
  }

  canNavigateTo(index: number): boolean {
    if (this.formSubmitted || this.config.mode === 'preview') return true;
    if (index === this.activeIndex) return true;
    
    // Allow going to any question that is already answered
    if (this.answeredQuestions[index]) return true;
    
    // Also allow going to the FIRST unanswered question
    const firstUnanswered = this.answeredQuestions.findIndex(a => !a);
    return index <= firstUnanswered;
  }

  goToQuestion(index: number): void {
    if (this.formSubmitted || this.config.mode === 'preview') {
      this.activeIndex = index;
      if (this.config.mode === 'preview') {
        this.canMoveNext = true;
      } else {
        this.checkCurrentQuestionAnswered();
      }
      return;
    }
    
    // In attend mode, only allow going to answered or next unanswered
    const firstUnanswered = this.answeredQuestions.findIndex(a => !a);
    if (firstUnanswered === -1 || index <= firstUnanswered) {
      this.activeIndex = index;
      this.checkCurrentQuestionAnswered();
    }
  }

  nextQuestion(): void {
    if (this.activeIndex < this.questions.length - 1) {
      if (this.canMoveNext || this.formSubmitted || this.config.mode === 'preview') {
        this.activeIndex++;
        this.canMoveNext = this.formSubmitted || this.config.mode === 'preview';
        this.checkCurrentQuestionAnswered();
      }
    }
  }

  prevQuestion(): void {
    if (this.activeIndex > 0) {
      this.activeIndex--;
      this.canMoveNext = true; // Always allow next when going back
      this.checkCurrentQuestionAnswered();
    }
  }

  getOptionLetter(index: number): string {
    return this.OPTION_LETTERS[index] || (index + 1).toString();
  }

  showAlert(message: string): void {
    this.alertMessage = message;
    this.modalService.open(this.alertTemplate, { size: 'sm' });
  }

  close(): void {
    this.quizSubmitted.emit({
      submitted: this.formSubmitted,
      passStatus: this.passStatus
    });
    
    // Actually close the modal if we are in one
    if (this.activeModal) {
      this.activeModal.dismiss('close');
    }
  }

  getQuestionDisplay(q: SurveyQuestion, index: number): string {
    if (!q || !q.question) return `Question ${index + 1}`;
    return q.question.length > 60 ? q.question.substring(0, 60) + '…' : q.question;
  }

  isCorrectAnswer(q: SurveyQuestion, option: SurveyOption): boolean {
    const isPreview = this.config && this.config.mode === 'preview';
    if ((this.formSubmitted && this.correctAnswers) || isPreview) {
      const correct = isPreview ? q.correctAnswer : this.correctAnswers[q.question];
      if (!correct) return false;
      
      if (q.optionType === 'checkbox') {
        const correctOptions = typeof correct === 'string' 
          ? correct.split(',').map(s => s.trim()) 
          : [];
        return correctOptions.includes(option.optionValue);
      } else {
        return correct === option.optionValue;
      }
    }
    return false;
  }

  isUserAnswer(q: SurveyQuestion, option: SurveyOption): boolean {
    if (q.optionType === 'checkbox') {
      const userAnswers = Array.isArray(q.response) 
        ? q.response 
        : q.response ? q.response.split(',').map(s => s.trim()) : [];
      return userAnswers.includes(option.optionValue);
    } else {
      return q.response === option.optionValue;
    }
  }

  getOptionClass(q: SurveyQuestion, option: SurveyOption): string {
    if (!this.formSubmitted) return '';
    
    const isCorrect = this.isCorrectAnswer(q, option);
    const isUser = this.isUserAnswer(q, option);
    
    if (isUser && isCorrect) return 'correct-user';
    if (isUser && !isCorrect) return 'incorrect-user';
    if (!isUser && isCorrect) return 'correct-answer';
    return '';
  }

  getOptionBgColor(q: SurveyQuestion, option: SurveyOption): string {
    if (!this.formSubmitted) return 'transparent';
    
    const isCorrect = this.isCorrectAnswer(q, option);
    const isUser = this.isUserAnswer(q, option);
    
    if (isUser && isCorrect) return '#f0fdf4'; // success-light
    if (isUser && !isCorrect) return '#fef2f2'; // danger-light
    if (!isUser && isCorrect) return '#f0fdf4'; // success-light
    return 'transparent';
  }

  getOptionTextColor(q: SurveyQuestion, option: SurveyOption): string {
    if (!this.formSubmitted) return '#374151';
    
    const isCorrect = this.isCorrectAnswer(q, option);
    const isUser = this.isUserAnswer(q, option);
    
    if (isUser && isCorrect) return '#15803d';
    if (isUser && !isCorrect) return '#b91c1c';
    if (!isUser && isCorrect) return '#15803d';
    return '#374151';
  }

  getOptionDotColor(q: SurveyQuestion, option: SurveyOption): string {
    if (!this.formSubmitted) return '#cbd5e1';
    
    const isCorrect = this.isCorrectAnswer(q, option);
    const isUser = this.isUserAnswer(q, option);
    
    if (isUser && isCorrect) return '#16a34a';
    if (isUser && !isCorrect) return '#dc2626';
    if (!isUser && isCorrect) return '#16a34a';
    return '#cbd5e1';
  }
}