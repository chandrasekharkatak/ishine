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
  standalone: false,
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

  currentQuestionIndex: number = 0;
  canMoveNext: boolean = false;
  answeredQuestions: boolean[] = [];

  @Output() quizSubmitted = new EventEmitter<boolean>();
  @Output() closeTrainingView = new EventEmitter<any>();
  @Output() questionStateChanged = new EventEmitter<{ index: number; answered: boolean[] }>();

  currentUser: User;

  constructor(
    private surveyService: SurveyService,
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private validationService: ValidationService,
  ) {}

  ngOnInit() {
    this.currentUser = this.authenticationService.currentUserValue;
    if (this.isAlreadySubmitted) {
      this.getSubmittedQuizData();
    } else {
      this.getQuizForm();
    }
  }

  getQuizForm() {
    this.isQuizLoaded = false;

    this.surveyService.getQuizQuestionByTrainingId(this.trainingId).subscribe({
      next: (response: any) => {
        this.allSurveyQuestionList =
          response.serviceResponse.allSurveyQuestionList;
        this.quizId = response.serviceResponse.quizId;

        this.allSurveyQuestionList.forEach((survey: SurveyQuestion) => {
          survey.optionsList =
            typeof survey.options === 'string'
              ? JSON.parse(survey.options)
              : survey.options;
          survey.required =
            typeof survey.required === 'string'
              ? JSON.parse(survey.required)
              : survey.required;
          survey.response = survey.optionType === 'checkbox' ? [] : '';
        });

        // Initialize answeredQuestions array
        this.answeredQuestions = new Array(this.allSurveyQuestionList.length).fill(false);
        this.currentQuestionIndex = 0;
        this.canMoveNext = false;

        const surveyQuestionsTemplate = this.createInitialTemplate();
        const surveyTemplate = `<form id="surveyForm">${surveyQuestionsTemplate}</form>`;

        // show container first
        this.isQuizLoaded = true;

        setTimeout(() => {
          const container = document.querySelector(
            '#surveyContainer .quiz-content',
          );

          if (!container) return;

          container.innerHTML = '';

          const wrapper = document.createElement('div');
          wrapper.classList.add('dynamic-questions');
          wrapper.innerHTML = surveyTemplate;

          container.appendChild(wrapper);

          // Check if the first question is required, to disable next button initially
          this.onQuizFormChange();
        }, 0);
      },

      error: (error: any) => {
        this.isQuizLoaded = true;
        this.showAlertMessage(
          error.error?.serviceStatus || 'something went wrong',
        );
      },
    });
  }

  getSubmittedQuizData() {
    this.isQuizLoaded = false;

    let surveyObj = new Survey();
    surveyObj.trainingId = this.trainingId;
    surveyObj.empId = this.currentUser.empId;
    surveyObj.isQuizResponse = true;
    surveyObj.isAttendingQuiz = false;

    this.surveyService
      .getSurveyResponseByEmpIdAndSurveyId(surveyObj)
      .subscribe({
        next: (response: any) => {
          this.allSurveyQuestionList =
            response.serviceResponse.allSurveyQuestionList;
          this.quizId = response.serviceResponse.quizId;
          this.correctAnswers = response.serviceResponse.correctAnswers;
          this.totalQuestions = response.serviceResponse.totalQuestions || 0;

          let marksObtained = null;

          this.allSurveyQuestionList.forEach((survey: SurveyQuestion) => {
            survey.optionsList =
              typeof survey.options === 'string'
                ? JSON.parse(survey.options)
                : survey.options;
            survey.required =
              typeof survey.required === 'string'
                ? JSON.parse(survey.required)
                : survey.required;

            if (survey.marksObtained && marksObtained == null) {
              marksObtained = survey.marksObtained;
            }

            if (survey.passStatus && !this.passStatus) {
              this.passStatus = survey.passStatus;
            }

            if (survey.response === survey.correctAnswer) {
              this.correctAnswersCount++;
            }

            if (survey.cuttOffQuestions) {
              this.cuttOffQuestions = survey.cuttOffQuestions;
            }
          });

          this.marksObtained = marksObtained;

          // const resultsHeader = this.createResultsHeader();
          const surveyQuestionsTemplate = this.createResultsTemplate();

          const surveyTemplate = `<form id="surveyForm">${surveyQuestionsTemplate}</form>`;

          this.isQuizLoaded = true;

          setTimeout(() => {
            const container = document.querySelector(
              '#surveyContainer .quiz-content',
            );

            if (!container) return;

            container.innerHTML = '';

            const wrapper = document.createElement('div');
            wrapper.classList.add('dynamic-questions');
            wrapper.innerHTML = surveyTemplate;

            container.appendChild(wrapper);
          }, 0);
        },

        error: (error: any) => {
          this.isQuizLoaded = true;
          this.showAlertMessage(
            error.error?.serviceStatus || 'something went wrong',
          );
        },
      });
  }

  onSubmit(template: TemplateRef<any>) {
    // If they were supposed to answer the last question and haven't, stop them
    if (!this.canMoveNext) {
      this.alertMessage = `Please provide response for Question ${this.currentQuestionIndex + 1} !!`;
      this.openAlertMod(template, this.alertMessage);
      return;
    }

    const form: any = document.getElementById('surveyForm');

    let surveyObj = new Survey();
    surveyObj.empId = this.currentUser.empId;
    surveyObj.type = 'quiz';
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
        surveyQuestion.response = question.response.join(', ');
      } else {
        surveyQuestion.response = question.response;
      }

      surveyObj.surveyQuestionList.push(surveyQuestion);
    });

    let inputValidated: boolean = this.validateSurveyResponse(
      surveyObj,
      template,
    );
    if (!inputValidated) return;

    this.surveyService.setSurveyResponseByEmpId(surveyObj).subscribe({
      next: (response: any) => {
        const quizResults = response.serviceResponse;
        this.correctAnswers = quizResults.correctAnswers || {};
        this.correctAnswersCount = quizResults.correctAnswersCount || 0;
        this.passStatus = quizResults.passStatus || 'fail';
        this.cuttOffQuestions = quizResults.cutOffQuestion || 0;
        this.totalQuestions = quizResults.totalQuestions || 0;

        this.formSubmitted = true;
        this.quizSubmitted.emit(true);
        setTimeout(() => {
          this.rebuildTemplate();
        }, 100);
      },
      error: (error: any) => {
        this.openAlertMod(
          template,
          error.error?.serviceStatus || 'something went wrong',
        );
      },
    });
  }

  createInitialTemplate(): string {
    let surveyTemplate = ``;

    this.allSurveyQuestionList.forEach((question: SurveyQuestion, qIndex) => {
      let finalQuestionTemplate = ``;
      const displayStyle = qIndex === 0 ? 'block' : 'none';
      const questionStartTemplate = `<div class="question-block" id="question-block-${qIndex}" style="display: ${displayStyle};"><div class="row"><div class="form-group">`;
      const questionEndTemplate = `</div></div></div>`;
      const questionRequiredTemplate = `<span class="text-danger">*</span>`;
      const isQuestionRequired =
        question.required === true ? questionRequiredTemplate : '';

      const questionTemplate = `
      <h5 class="mb-0">
        <span class="question-number">Q${qIndex + 1}.</span>&nbsp;
        <span class="question-text">${question.question ?? ''}${isQuestionRequired}</span>
      </h5>
      ${question.description ? `<small class="text-secondary d-block mt-1 mb-2">${question.description}</small>` : ''}
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

  nextQuestion() {
    if (this.currentQuestionIndex < this.allSurveyQuestionList.length - 1) {
      this.currentQuestionIndex++;
      this.updateQuestionVisibility();
      this.onQuizFormChange();
    }
  }

  prevQuestion() {
    if (this.currentQuestionIndex > 0) {
      this.currentQuestionIndex--;
      this.updateQuestionVisibility();
      this.onQuizFormChange();
    }
  }

  jumpToQuestion(index: number) {
    // Only allow jumping to questions that have been answered or the next unanswered one
    const firstUnanswered = this.answeredQuestions.findIndex(a => !a);
    const targetAllowed = firstUnanswered === -1 || index <= firstUnanswered;
    if (!targetAllowed) return;
    this.currentQuestionIndex = index;
    this.updateQuestionVisibility();
    this.onQuizFormChange();
  }

  updateQuestionVisibility() {
    for (let i = 0; i < this.allSurveyQuestionList.length; i++) {
        const el = document.getElementById(`question-block-${i}`);
        if (el) {
            el.style.display = i === this.currentQuestionIndex ? 'block' : 'none';
        }
    }
  }

  onQuizFormChange() {
    if (this.isAlreadySubmitted || this.formSubmitted) return;
    const form: any = document.getElementById('surveyForm');
    if (!form) return;
    
    const elements = form.elements[`question-${this.currentQuestionIndex + 1}`];
    let isAnswered = false;
    
    if (elements) {
        if (this.allSurveyQuestionList[this.currentQuestionIndex].optionType === 'checkbox' || 
            this.allSurveyQuestionList[this.currentQuestionIndex].optionType === 'radio') {
            if (elements.length !== undefined && !elements.nodeName) { // it's a NodeList/RadioNodeList
                for (let i = 0; i < elements.length; i++) {
                    if (elements[i].checked) isAnswered = true;
                }
            } else {
                isAnswered = elements.checked;
            }
        } else {
            isAnswered = elements.value?.trim()?.length > 0;
        }
    }
    
    if (this.allSurveyQuestionList[this.currentQuestionIndex].required) {
        this.canMoveNext = isAnswered;
    } else {
        this.canMoveNext = true;
    }

    // Update answered tracking
    this.answeredQuestions[this.currentQuestionIndex] = isAnswered;
    this.questionStateChanged.emit({ index: this.currentQuestionIndex, answered: [...this.answeredQuestions] });
  }

  rebuildTemplate() {
    const container = document.querySelector('#surveyContainer .quiz-content');

    if (!container) return;

    const surveyQuestionsTemplate = this.createResultsTemplate();
    // const resultsHeader = this.createResultsHeader();

    const surveyTemplate = `<form id="surveyForm">${surveyQuestionsTemplate}</form>`;

    container.innerHTML = '';

    const wrapper = document.createElement('div');
    wrapper.classList.add('dynamic-questions');
    wrapper.innerHTML = surveyTemplate;

    container.appendChild(wrapper);
  }

  createResultsTemplate(): string {
    let surveyTemplate = ``;

    this.allSurveyQuestionList.forEach((question: SurveyQuestion, qIndex) => {
      let finalQuestionTemplate = ``;
      const questionStartTemplate = `<div class="row"><div class="form-group">`;
      const questionEndTemplate = `</div></div>`;
      const questionRequiredTemplate = `<span class="text-danger">*</span>`;
      const isQuestionRequired =
        question.required === true ? questionRequiredTemplate : '';

      const questionTemplate = `
        <h5 class="mb-0">
          <span class="question-number">Q${qIndex + 1}.</span>&nbsp;
          <span class="question-text">${question.question ?? ''}${isQuestionRequired}</span>
        </h5>
        ${question.description ? `<small class="text-secondary d-block mt-1 mb-2">${question.description}</small>` : ''}
      `;

      finalQuestionTemplate = questionStartTemplate + questionTemplate;

      const correctAnswer = this.correctAnswers[question.question] || '';
      const optionType = question.optionType || 'radio';

      if (optionType === 'text') {
        const userAnswer = question.response || '';
        finalQuestionTemplate += `<textarea class="form-control" rows="1" name="question-${qIndex + 1}" disabled>${userAnswer}</textarea>`;

        if (correctAnswer) {
          const isCorrect =
            userAnswer.toString().trim() === correctAnswer.toString().trim();
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

          let dotColor = '#cbd5e1'; // neutral grey
          let textColor = '#374151';
          let icon = '';
          let bgColor = 'transparent';

          if (isUserSelected && isCorrectOption) {
            dotColor = '#16a34a'; textColor = '#15803d'; icon = '<i class="fas fa-check-circle" style="color:#16a34a;margin-right:4px;"></i>'; bgColor = '#f0fdf4';
          } else if (isUserSelected && !isCorrectOption) {
            dotColor = '#dc2626'; textColor = '#b91c1c'; icon = '<i class="fas fa-times-circle" style="color:#dc2626;margin-right:4px;"></i>'; bgColor = '#fef2f2';
          } else if (!isUserSelected && isCorrectOption) {
            dotColor = '#16a34a'; textColor = '#15803d'; icon = '<i class="fas fa-check-circle" style="color:#16a34a;margin-right:4px;"></i>'; bgColor = '#f0fdf4';
          }

          optionTemplate += `
            <div style="display:flex;align-items:center;gap:10px;padding:7px 12px;margin:5px 0;border-radius:8px;background:${bgColor};">
              <span style="width:12px;height:12px;border-radius:50%;border:2px solid ${dotColor};display:inline-block;flex-shrink:0;"></span>
              <span style="color:${textColor};font-size:0.9rem;">${icon}${option.optionValue}</span>
              ${isCorrectOption ? '<span style="color:#16a34a;font-size:0.78rem;margin-left:auto;font-weight:500;">✓ Correct Answer</span>' : ''}
            </div>
          `;
        });

        finalQuestionTemplate += optionTemplate;
      } else if (optionType === 'checkbox') {
        let optionTemplate = '';
        const userAnswers = Array.isArray(question.response)
          ? question.response
          : question.response
            ? question.response.split(',').map((s: string) => s.trim())
            : [];
        const correctAnswers = correctAnswer.split(',').map((a) => a.trim());

        question.optionsList.forEach((option: SurveyOption, opIndex) => {
          const isUserSelected = userAnswers.includes(option.optionValue);
          const isCorrectOption = correctAnswers.includes(option.optionValue);

          let dotColor = '#cbd5e1';
          let textColor = '#374151';
          let icon = '';
          let bgColor = 'transparent';

          if (isUserSelected && isCorrectOption) {
            dotColor = '#16a34a'; textColor = '#15803d'; icon = '<i class="fas fa-check-circle" style="color:#16a34a;margin-right:4px;"></i>'; bgColor = '#f0fdf4';
          } else if (isUserSelected && !isCorrectOption) {
            dotColor = '#dc2626'; textColor = '#b91c1c'; icon = '<i class="fas fa-times-circle" style="color:#dc2626;margin-right:4px;"></i>'; bgColor = '#fef2f2';
          } else if (!isUserSelected && isCorrectOption) {
            dotColor = '#16a34a'; textColor = '#15803d'; icon = '<i class="fas fa-check-circle" style="color:#16a34a;margin-right:4px;"></i>'; bgColor = '#f0fdf4';
          }

          optionTemplate += `
            <div style="display:flex;align-items:center;gap:10px;padding:7px 12px;margin:5px 0;border-radius:8px;background:${bgColor};">
              <span style="width:12px;height:12px;border-radius:3px;border:2px solid ${dotColor};display:inline-block;flex-shrink:0;"></span>
              <span style="color:${textColor};font-size:0.9rem;">${icon}${option.optionValue}</span>
              ${isCorrectOption ? '<span style="color:#16a34a;font-size:0.78rem;margin-left:auto;font-weight:500;">✓ Correct Answer</span>' : ''}
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
    this.modalRef = this.modalService.open(this.alertMessageTemplate, {
      size: 'modal-sm',
    });
  }

  openAlertMod(template: TemplateRef<any>, message: string) {
    this.alertMessage = message;
    this.modalRef = this.modalService.open(template, { size: 'modal-sm' });
  }

  validateSurveyResponse(surveyObj: Survey, template: TemplateRef<any>) {
    for (let index = 0; index < surveyObj.surveyQuestionList.length; index++) {
      let question = surveyObj.surveyQuestionList[index];
      let originalQuestion = this.allSurveyQuestionList[index];

      // Check for radio buttons specifically
      if (originalQuestion.optionType === 'radio') {
        if (
          question.response === null ||
          question.response === undefined ||
          question.response === ''
        ) {
          this.alertMessage = `Please select an option for Question ${index + 1} !!`;
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
      }
      // Check for checkboxes
      else if (originalQuestion.optionType === 'checkbox') {
        if (
          !question.response ||
          (Array.isArray(question.response) && question.response.length === 0)
        ) {
          this.alertMessage = `Please select at least one option for Question ${index + 1} !!`;
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
      }
      // Check for text inputs
      else {
        if (
          !this.validationService.validateNullUndefinedEmptyString(
            question.response,
          )
        ) {
          this.alertMessage = `Please provide response for Question ${index + 1} !!`;
          this.openAlertMod(template, this.alertMessage);
          return false;
        }
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

  resetView() {
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
    this.currentQuestionIndex = 0;
    this.canMoveNext = false;
  }
}
