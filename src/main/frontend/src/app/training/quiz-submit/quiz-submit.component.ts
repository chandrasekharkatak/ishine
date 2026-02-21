import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  Input,
  OnInit,
  Output,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs';
import { SurveyOption } from 'src/app/models/sureyOption';
import { SurveyQuestion } from 'src/app/models/surveyQuestion';
import { SurveyService } from 'src/app/services/survey.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';
import { Survey } from 'src/app/models/survey';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-quiz-submit',
  templateUrl: './quiz-submit.component.html',
  styleUrl: './quiz-submit.component.css',
  standalone: false
})
export class QuizSubmit implements OnInit {
  @Input() trainingId: number;
  @Input() cycleNumber: number;
  @Input() contentId: number;
  @Input() isAlreadySubmitted: boolean = false;

  @ViewChild('alert_message') alertMessageTemplate: TemplateRef<any>;

  quizId: number;
  modalRef: NgbModalRef;
  isQuizLoaded: boolean = false;
  alertMessage: string = '';
  allSurveyQuestionList: SurveyQuestion[] = [];
  correctAnswers: { [key: string]: string } = {};
  formSubmitted: boolean = false;
  marksObtained: number = 0;
  passStatus: string = '';
  correctAnswersCount: number = 0;
  cuttOffQuestions: number = 0;
  totalQuestions: number = 0;
  
  @Output() quizSubmitted = new EventEmitter<boolean>();
  @Output() closeTrainingView = new EventEmitter<any>();
  
  currentUser: User;

  constructor(private surveyService: SurveyService, private modalService: NgbModal, 
    private authenticationService: AuthenticationService, private validationService: ValidationService) {}

  ngOnInit() {
    this.currentUser = this.authenticationService.currentUserValue;
    if (this.isAlreadySubmitted) {
      this.getSubmittedQuizData();
    } else {
      this.getQuizForm();
    }
  }

  getQuizForm() {
    this.surveyService.getQuizQuestionByTrainingId(this.trainingId).pipe(first()).subscribe((response: any) => {
      if(response.serviceStatus == "Success") {
        this.allSurveyQuestionList = response.serviceResponse.allSurveyQuestionList;
        this.quizId = response.serviceResponse.quizId;

        this.allSurveyQuestionList.forEach((survey: SurveyQuestion) => {
          survey.optionsList = typeof survey.options === 'string' ? JSON.parse(survey.options) : survey.options;
          survey.required = typeof survey.required === 'string' ? JSON.parse(survey.required) : survey.required;
          survey.response = survey.optionType === 'checkbox' ? [] : '';
        });

        const surveyQuestionsTemplate: string = this.createInitialTemplate();
        const formStart = `<form id="surveyForm">`;
        const formEnd = `</form>`;
        const surveyTemplate = formStart + surveyQuestionsTemplate + formEnd;
        this.isQuizLoaded = false;

        setTimeout(() => {
          const surveyContainer = document.getElementById('surveyContainer');
          if (surveyContainer) {
            const oldDynamicSection = surveyContainer.querySelector('.dynamic-questions');
            if (oldDynamicSection) oldDynamicSection.remove();

            const wrapper = document.createElement('div');
            wrapper.classList.add('dynamic-questions');
            wrapper.innerHTML = surveyTemplate;

            const buttonRow = surveyContainer.querySelector('.row.mt-3');
            if (buttonRow) {
              surveyContainer.insertBefore(wrapper, buttonRow);
            } else {
              surveyContainer.appendChild(wrapper);
            }

            this.isQuizLoaded = true;
          }
        }, 500);

      } else {  
        this.showAlertMessage(response.serviceResponse || "something went wrong");
        return;
      }
    });
  }

  getSubmittedQuizData() {
    let surveyObj = new Survey();
    surveyObj.trainingId = this.trainingId;
    surveyObj.empId = this.currentUser.empId;
    surveyObj.isQuizResponse = true;
    surveyObj.isAttendingQuiz = false;

    this.surveyService.getSurveyResponseByEmpIdAndSurveyId(surveyObj).pipe(first()).subscribe((response: any) => {
      if(response.serviceStatus == "Success") {
        this.allSurveyQuestionList = response.serviceResponse.allSurveyQuestionList;
        this.quizId = response.serviceResponse.quizId;
        this.correctAnswers = response.serviceResponse.correctAnswers;
        this.totalQuestions = response.serviceResponse.totalQuestions || 0;
        
        let marksObtained = null;
        this.allSurveyQuestionList.forEach((survey: SurveyQuestion) => {
          survey.optionsList = typeof survey.options === 'string' ? JSON.parse(survey.options) : survey.options;
          survey.required = typeof survey.required === 'string' ? JSON.parse(survey.required) : survey.required;
          if (survey.marksObtained && marksObtained == null) {
            marksObtained = survey.marksObtained;
          }
          if (survey.passStatus && !this.passStatus) {
            this.passStatus = survey.passStatus;
          }
          if (survey.response === survey.correctAnswer) {
            this.correctAnswersCount++;
          }
          if(survey.cuttOffQuestions){
            this.cuttOffQuestions = survey.cuttOffQuestions;
          }
        });
        
        this.marksObtained = marksObtained;
        
        const resultsHeader = this.createResultsHeader();
        const surveyQuestionsTemplate: string = this.createResultsTemplate();
        const formStart = `<form id="surveyForm">`;
        const formEnd = `</form>`;
        const surveyTemplate = resultsHeader + formStart + surveyQuestionsTemplate + formEnd;
        this.isQuizLoaded = false;

        setTimeout(() => {
          const surveyContainer = document.getElementById('surveyContainer');
          if (surveyContainer) {
            const oldDynamicSection = surveyContainer.querySelector('.dynamic-questions');
            if (oldDynamicSection) oldDynamicSection.remove();
            
            const oldHeaderSection = surveyContainer.querySelector('.quiz-results-header');
            if (oldHeaderSection) oldHeaderSection.remove();

            const wrapper = document.createElement('div');
            wrapper.classList.add('dynamic-questions');
            wrapper.innerHTML = surveyTemplate;

            const buttonRow = surveyContainer.querySelector('.row.mt-3') as HTMLElement;
            if (buttonRow) {
              buttonRow.style.display = 'none';
              surveyContainer.insertBefore(wrapper, buttonRow);
            } else {
              surveyContainer.appendChild(wrapper);
            }

            this.isQuizLoaded = true;
          }
        }, 500);
      } else {
        this.showAlertMessage(response.serviceResponse || "something went wrong");
      }
    });
  }

  onSubmit(template: TemplateRef<any>) {
    const form: any = document.getElementById('surveyForm');

    let surveyObj = new Survey();
    surveyObj.empId = this.currentUser.empId;
    surveyObj.type = "quiz";
    surveyObj.createdBy = this.currentUser.empId;
    surveyObj.surveyId = this.quizId;
    surveyObj.trainingId = this.trainingId;
    surveyObj.cycleNumber = this.cycleNumber;
    surveyObj.contentId = this.contentId;
    surveyObj.surveyQuestionList = [];
    surveyObj.isQuizResponse = true;
    surveyObj.isAttendingQuiz = false;

    this.allSurveyQuestionList.forEach((question, index) => {
      if (question.optionType === 'checkbox') {
        let response = [];
        const elements = form.elements[`question-${index + 1}`];
        if (elements) {
          if (elements.forEach) {
            elements.forEach((checkboxOption: any) => {
              if (checkboxOption.checked) response.push(checkboxOption.value);
            });
          } else if (elements.checked) {
            response.push(elements.value);
          }
        }
        question.response = response;
      } else {
        const element = form.elements[`question-${index + 1}`];
        question.response = element ? element.value : '';
      }
    });

    this.allSurveyQuestionList.forEach((question) => {
      let surveyQuestion = new SurveyQuestion();
      surveyQuestion.surveyQuestionId = question.surveyQuestionId;
      surveyQuestion.required = question.required;
      
      if (Array.isArray(question.response)) {
        surveyQuestion.response = question.response.join(", ");
      } else {
        surveyQuestion.response = question.response;
      }
      
      surveyObj.surveyQuestionList.push(surveyQuestion);
    });

    let inputValidated: boolean = this.validateSurveyResponse(surveyObj, template);
    if (!inputValidated) return;
    
    this.surveyService.setSurveyResponseByEmpId(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        const quizResults = response.serviceResponse;
        this.correctAnswers = quizResults.correctAnswers || {};
        this.correctAnswersCount = quizResults.correctAnswersCount || 0;
        this.passStatus = quizResults.passStatus || 'fail';
        this.cuttOffQuestions = quizResults.cutOffQuestion || 0;
        this.totalQuestions = quizResults.totalQuestions || 0;
        
        this.formSubmitted = true;
        setTimeout(() => {
          this.rebuildTemplate();
        }, 100);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }

  createResultsHeader(): string {
    const badgeClass = this.passStatus?.toLowerCase() === 'pass' ? 'success' : 'danger';
    
    return `
      <div class="quiz-results-header mb-2 p-1 border rounded bg-light">
        <div class="row align-items-center">
          <div class="col-md-3">
            <h4 class="mb-0">Quiz Results</h4>
          </div>
          <div class="col-md-9 text-end">
            <div class="d-inline-block me-4">
              <span class="fw-bold">Score:</span>
              <span class="ms-2 badge bg-primary fs-6">${this.correctAnswersCount}/${this.totalQuestions}</span>
            </div>
            <div class="d-inline-block me-4">
              <span class="fw-bold">Status:</span>
              <span class="ms-2 badge bg-${badgeClass} fs-6">${this.passStatus || 'N/A'}</span>
            </div>
            <div class="d-inline-block me-4">
              <span class="fw-bold">Correct Answers:</span>
              <span class="ms-2 badge bg-info fs-6">${this.correctAnswersCount}</span>
            </div>
            <div class="d-inline-block">
              <span class="fw-bold">Cut off Questions:</span>
              <span class="ms-2 badge bg-warning fs-6">${this.cuttOffQuestions}</span>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  createInitialTemplate(): string {
    let surveyTemplate = ``;

    this.allSurveyQuestionList.forEach((question: SurveyQuestion, qIndex) => {
      let finalQuestionTemplate = ``;
      const questionStartTemplate = `<div class="row"><div class="form-group">`;
      const questionEndTemplate = `</div></div>`;
      const questionRequiredTemplate = `<span class="text-danger">*</span>`;
      const isQuestionRequired = question.required === true ? questionRequiredTemplate : '';

      const questionTemplate = `
        <h5 class="mb-0">
          <i class="fa-solid fa-q question-icon"></i>.&nbsp;
          ${question.question ?? ''}${isQuestionRequired}
        </h5>
        <small class="text-secondary">${question.description ?? ''}</small>
      `;

      finalQuestionTemplate = questionStartTemplate + questionTemplate;

      if (question.optionType === 'text') {
        finalQuestionTemplate += `<textarea class="form-control" rows="1" name="question-${qIndex + 1}"></textarea>`;
      } else if (question.optionType === 'checkbox') {
        let optionTemplate = '';
        question.optionsList.forEach((option: SurveyOption, opIndex) => {
          optionTemplate += `
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="q-${qIndex + 1}-check-option-${opIndex + 1}"
                    value="${option.optionValue}" name="question-${qIndex + 1}">
              <label class="form-check-label" for="q-${qIndex + 1}-check-option-${opIndex + 1}">
                ${option.optionValue}
              </label>
            </div>
          `;
        });
        finalQuestionTemplate += optionTemplate;
      } else if (question.optionType === 'radio') {
        let optionTemplate = '';
        question.optionsList.forEach((option: SurveyOption, index) => {
          optionTemplate += `
            <div class="form-check">
              <input class="form-check-input" type="radio" id="q-${qIndex + 1}-radio-option-${index + 1}"
                    value="${option.optionValue}" name="question-${qIndex + 1}">
              <label class="form-check-label" for="q-${qIndex + 1}-radio-option-${index + 1}">
                ${option.optionValue}
              </label>
            </div>
          `;
        });
        finalQuestionTemplate += optionTemplate;
      }

      finalQuestionTemplate += questionEndTemplate;
      surveyTemplate += finalQuestionTemplate;
    });

    return surveyTemplate;
  }

  rebuildTemplate() {
    const surveyContainer = document.getElementById('surveyContainer');
    if (!surveyContainer) return;

    const oldDynamicSection = surveyContainer.querySelector('.dynamic-questions');
    if (oldDynamicSection) {
      oldDynamicSection.remove();
    }

    const surveyQuestionsTemplate: string = this.createResultsTemplate();
    const resultsHeader = this.createResultsHeader();
    const formStart = `<form id="surveyForm">`;
    const formEnd = `</form>`;
    const surveyTemplate = resultsHeader + formStart + surveyQuestionsTemplate + formEnd;

    const wrapper = document.createElement('div');
    wrapper.classList.add('dynamic-questions');
    wrapper.innerHTML = surveyTemplate;

    const buttonRow = surveyContainer.querySelector('.row.mt-3') as HTMLElement;
    if (buttonRow) {
      buttonRow.style.display = 'none';
      surveyContainer.insertBefore(wrapper, buttonRow);
    } else {
      surveyContainer.appendChild(wrapper);
    }
  }

  createResultsTemplate(): string {
    let surveyTemplate = ``;

    this.allSurveyQuestionList.forEach((question: SurveyQuestion, qIndex) => {
      let finalQuestionTemplate = ``;
      const questionStartTemplate = `<div class="row"><div class="form-group">`;
      const questionEndTemplate = `</div></div>`;
      const questionRequiredTemplate = `<span class="text-danger">*</span>`;
      const isQuestionRequired = question.required === true ? questionRequiredTemplate : '';

      const questionTemplate = `
        <h5 class="mb-0">
          <i class="fa-solid fa-q question-icon"></i>.&nbsp;
          ${question.question ?? ''}${isQuestionRequired}
        </h5>
        <small class="text-secondary">${question.description ?? ''}</small>
      `;

      finalQuestionTemplate = questionStartTemplate + questionTemplate;

      const correctAnswer = this.correctAnswers[question.question] || '';
      const optionType = question.optionType || 'radio';

      if (optionType === 'text') {
        const userAnswer = question.response || '';
        finalQuestionTemplate += `<textarea class="form-control" rows="1" name="question-${qIndex + 1}" disabled>${userAnswer}</textarea>`;
        
        if (correctAnswer) {
          const isCorrect = userAnswer.toString().trim() === correctAnswer.toString().trim();
          finalQuestionTemplate += `
            <div class="mt-2">
              <span class="text-info">Correct answer: ${correctAnswer}</span>
              <span class="ms-2 ${isCorrect ? 'text-success' : 'text-danger'}">
                ${isCorrect ? '✓ Correct' : '✗ Incorrect'}
              </span>
            </div>
          `;
        }
      } else if (optionType === 'radio') {
        let optionTemplate = '';
        const userAnswer = question.response || '';
        
        question.optionsList.forEach((option: SurveyOption, index) => {
          const isUserSelected = userAnswer === option.optionValue;
          const isCorrectOption = correctAnswer === option.optionValue;
          let optionClass = '';
          
          if (isUserSelected && isCorrectOption) optionClass = 'text-success fw-bold';
          else if (isUserSelected && !isCorrectOption) optionClass = 'text-danger';
          else if (!isUserSelected && isCorrectOption) optionClass = 'text-success';
          
          optionTemplate += `
            <div class="form-check">
              <input class="form-check-input" type="radio" id="q-${qIndex + 1}-radio-option-${index + 1}"
                    value="${option.optionValue}" name="question-${qIndex + 1}" 
                    ${isUserSelected ? 'checked' : ''} disabled>
              <label class="form-check-label ${optionClass}" for="q-${qIndex + 1}-radio-option-${index + 1}">
                ${option.optionValue}
                ${isCorrectOption ? ' ✓ (Correct Answer)' : ''}
              </label>
            </div>
          `;
        });
        
        finalQuestionTemplate += optionTemplate;
      } else if (optionType === 'checkbox') {
        let optionTemplate = '';
        const userAnswers = Array.isArray(question.response) ? question.response : 
                           (question.response ? question.response.split(',').map((s: string) => s.trim()) : []);
        const correctAnswers = correctAnswer.split(',').map(a => a.trim());
        
        question.optionsList.forEach((option: SurveyOption, opIndex) => {
          const isUserSelected = userAnswers.includes(option.optionValue);
          const isCorrectOption = correctAnswers.includes(option.optionValue);
          let optionClass = '';
          
          if (isUserSelected && isCorrectOption) optionClass = 'text-success fw-bold';
          else if (isUserSelected && !isCorrectOption) optionClass = 'text-danger';
          else if (!isUserSelected && isCorrectOption) optionClass = 'text-success';
          
          optionTemplate += `
            <div class="form-check">
              <input class="form-check-input" type="checkbox" id="q-${qIndex + 1}-check-option-${opIndex + 1}"
                    value="${option.optionValue}" name="question-${qIndex + 1}" 
                    ${isUserSelected ? 'checked' : ''} disabled>
              <label class="form-check-label ${optionClass}" for="q-${qIndex + 1}-check-option-${opIndex + 1}">
                ${option.optionValue}
                ${isCorrectOption ? ' ✓ (Correct Answer)' : ''}
              </label>
            </div>
          `;
        });
        
        finalQuestionTemplate += optionTemplate;
      }

      finalQuestionTemplate += questionEndTemplate;
      surveyTemplate += finalQuestionTemplate;
    });

    return surveyTemplate;
  }

  showAlertMessage(message: string) {
    this.alertMessage = message;
    this.modalRef = this.modalService.open(this.alertMessageTemplate, {size: 'modal-sm' });
  }

  openAlertMod(template: TemplateRef<any>, message: string) {
    this.alertMessage = message;
    this.modalRef = this.modalService.open(template, {size: 'modal-sm' });
  }

  validateSurveyResponse(surveyObj: Survey, template: TemplateRef<any>) {
    for (let index = 0; index < surveyObj.surveyQuestionList.length; index++) {
      let question = surveyObj.surveyQuestionList[index];
      if (question.required && !this.validationService.validateNullUndefinedEmptyString(question.response)) {
        this.alertMessage = `Please provide response for Question ${index + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        return false;
      }
    }
    return true;
  }

  closeAlertMessage() {
    this.modalRef.close();
  }

  closeSuccessMessage() {
    this.modalRef.close();
    this.quizSubmitted.emit(true);
  }

  closeQuizView() {
    this.resetView();
    this.closeTrainingView.emit();
  }

  closeQuizSubmitView() {
    this.resetView();
    this.quizSubmitted.emit(true);
  }

  resetView(){
    this.isQuizLoaded = false;
    this.alertMessage = '';
    this.allSurveyQuestionList = [];
    this.correctAnswers = {};
    this.formSubmitted = false;
    this.marksObtained = 0;
    this.passStatus = '';
    this.correctAnswersCount = 0;
    this.cuttOffQuestions = 0;
    this.totalQuestions = 0;
  }
}