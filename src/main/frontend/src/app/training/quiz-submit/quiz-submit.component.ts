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
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: false
})
export class QuizSubmit implements OnInit {
  @Input() trainingId: number;
  @Input() cycleNumber: number;
  @Input() contentId: number;

  @ViewChild('alert_message') alertMessageTemplate: TemplateRef<any>;

  quizId: number;


  modalRef: NgbModalRef;
  isQuizLoaded: boolean = false;
  alertMessage: string = '';

  allSurveyQuestionList: SurveyQuestion[] = [new SurveyQuestion()];

  @Output() quizSubmitted = new EventEmitter<boolean>();

  currentUser: User;

  constructor(private surveyService: SurveyService, private modalService: NgbModal, 
    private authenticationService: AuthenticationService, private validationService: ValidationService) {}

  ngOnInit() {
    this.currentUser = this.authenticationService.currentUserValue;
    this.getQuizForm();
  }

  getQuizForm() {

    // const surveyPayload:any = {
    //   surveyId: this.quizId,
    //   trainingId: this.trainingId,
    //   type: "quiz"
    // }

    this.surveyService.getQuizQuestionByTrainingId(this.trainingId).pipe(first()).subscribe((response: any) => {
      if(response.serviceStatus == "Success") {
        this.allSurveyQuestionList = response.serviceResponse.allSurveyQuestionList;
        this.quizId = response.serviceResponse.quizId;

        this.allSurveyQuestionList.forEach((survey: SurveyQuestion) => {
          survey.optionsList = JSON.parse(survey.options);
          survey.required = JSON.parse(survey.required);
        });

        const surveyQuestionsTemplate: string = this.createTemplate();
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
    })
  }

  createTemplate(): string {
    let surveyTemplate = ``;

    this.allSurveyQuestionList.forEach((question: SurveyQuestion, qIndex) => {
      let finalQuestionTemplate = ``;
      const questionStartTemplate = `<div class="row"><div class="form-group">`;
      const questionEndTemplate = `</div></div>`;
      const questionRequiredTemplate = `<span class="text-danger">*</span>`;
      const isQuestionRequired =
        question.required === true ? questionRequiredTemplate : '';

      // Question header
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
    //   else if (question.optionType == 'dropdown') {
    //     let optionTemplate = `
    //   <select class="form-select" name="question-${qIndex + 1}">
    //     <option value="" disabled selected>Select an Employee</option>
    // `;

    //     this.allEmployeeList.forEach((emp: any) => {
    //       optionTemplate += `
    //     <option value="${emp.name} (${emp.employeementId})-(${emp.departmentName})">
    //       ${emp.name} (${emp.employeementId})-(${emp.departmentName})
    //     </option>
    //   `;
    //     });

    //     optionTemplate += `</select>`;
    //     finalQuestionTemplate += optionTemplate;
    //   }

      // close question block
      finalQuestionTemplate += questionEndTemplate;
      surveyTemplate += finalQuestionTemplate;
    });

    return surveyTemplate;
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

    this.allSurveyQuestionList.forEach((question, index) => {

      let surveyQuestion = new SurveyQuestion();
      surveyQuestion.surveyQuestionId = question.surveyQuestionId;
      surveyQuestion.required = question.required;

      if (question.optionType == 'checkbox') {
        let response = [];
        form.elements[`question-${index + 1}`]?.forEach((checkboxOption) => {
          if (checkboxOption.checked) response.push(checkboxOption.value);
        });
        surveyQuestion.response = response.join(", ");
      } else {
        surveyQuestion.response = form.elements[`question-${index + 1}`].value;
      }
      surveyObj.surveyQuestionList.push(surveyQuestion);
    });

    //console.log("On Survey Submit : ", surveyObj);
    let inputValidated: boolean = this.validateSurveyResponse(surveyObj, template);
    if (!inputValidated) return;
    this.surveyService.setSurveyResponseByEmpId(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.openAlertMod(template, response.serviceResponse);
        this.quizSubmitted.emit(true);
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
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
    let flag = true;

    for (let index = 0; index < surveyObj.surveyQuestionList.length; index++) {
      let question = surveyObj.surveyQuestionList[index];
      if (question.required && !this.validationService.validateNullUndefinedEmptyString(question.response)) {
        this.alertMessage = `Please provide response for Question ${index + 1} !!`;
        this.openAlertMod(template, this.alertMessage);
        flag = false;
        break;
      }

    }
    return flag;
  }

  closeAlertMessage() {
    this.modalRef.close();
  }

  closeSuccessMessage() {
    this.modalRef.close();
    this.quizSubmitted.emit(true);
  }

}
