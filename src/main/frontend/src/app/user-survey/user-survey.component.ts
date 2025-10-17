import { LocationStrategy } from '@angular/common';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { Feature } from '../models/feature';
import { SurveyOption } from '../models/sureyOption';
import { Survey } from '../models/survey';
import { SurveyQuestion } from '../models/surveyQuestion';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { SurveyService } from '../services/survey.service';
import { ValidationService } from '../services/validation.service';
import { PortalService } from '../services/portal.service';

@Component({
  selector: 'app-user-survey',
  templateUrl: './user-survey.component.html',
  styleUrls: ['./user-survey.component.css']
})
export class UserSurveyComponent implements OnInit {

  feature = "Survey";
  currentUser: User;
  userMapping: any = {};

  currentSurveyId:any;
  currentSurveyIdedit:any;
  isEdit: boolean = false;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  //modal 
  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();
  @ViewChild('preview_response_template') previewResponseTemplate: TemplateRef<any>

  isSurveyForm: boolean = false;
  isSurveyList: boolean = false;

  surveyObj: Survey = new Survey();
  allSurveyQuestionList: SurveyQuestion[] = [new SurveyQuestion()];

  allSurveyList: any[] = [];
  allAnsweredSurveyList: any[] = [];

  myResponseList: any[] = [];

  isSurveyLoaded: boolean = false;

  // Column Filter
  isSearchEnabled:boolean=false;
  filters:any = {};
  surveyColumns:any[] = ['surveyName','description'];
  allEmployeeList: any;

  constructor(
    private validationService: ValidationService,
    private modalService: BsModalService,
    private authenticationService: AuthenticationService,
    private surveyService: SurveyService,
    private locationStrategy: LocationStrategy,
    private route: ActivatedRoute,
    private router : Router,
    private portalService: PortalService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    // Dynamic Subfeature Flags 
    let featureMap: Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });
    //console.log(this.feature, this.userMapping);

    this.route.params.subscribe((params:Params) => {
      this.currentSurveyId = params['id'];
    });

    this.route.params.subscribe((params: Params) => {
      console.log("params", params);
      
      this.currentSurveyIdedit = params['id'];  // Get the survey ID
      this.isEdit = params['id'] && params['id'].includes('edit'); // Check if 'edit' exists in the URL
    
      console.log('Survey ID:', this.currentSurveyIdedit);
      console.log('Is Edit:', this.isEdit);
    });
    
    this.isEdit = this.route.snapshot.url.some(segment => segment.path === 'edit');
    console.log('Is Edit:', this.isEdit);


  

    this.sectionViewInit();

    //console.log("allSurveyQuestionList : ", this.allSurveyQuestionList);

    this.preventBackButton();
  }
  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    })
  }

  sectionViewInit() {
    this.showSurveys();
  }

  showSurveyForm() {
    this.surveyObj = new Survey();
    this.allSurveyQuestionList = [];

    this.isSurveyForm = true;

    this.isSurveyList = false;

  }

  showSurveys() {
    this.isSurveyList = true;

    this.isSurveyForm = false;

    this.getAllSurveys();
  }

  getAllSurveys() {
    this.allSurveyList = [];

    this.surveyService.getAllSurveys().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allSurveyList = response.serviceResponse;

        this.allSurveyList = this.allSurveyList.filter(x => x.type != "exit" && x.isActive == "true");
        if(this.currentSurveyId != null){
          let currentSurvey = this.allSurveyList.find(x => x.surveyId == this.currentSurveyId);

          // Check if user has already taken survey
          currentSurvey.empId = this.currentUser.empId;
          if(this.isEdit){
            this.surveyObj = this.surveyService.getSurveyData();
            this.onViewMyResponse(this.surveyObj);
            this.onTakeSurvey(currentSurvey);

          }else {
            this.surveyService.getSurveyResponseByEmpIdAndSurveyId(currentSurvey).pipe(first()).subscribe((response: any) => {
              if (response.serviceStatus == "Success") {
                this.currentSurveyId = null;
                this.router.navigate(['/user-survey']);
              } else {
                this.onTakeSurvey(currentSurvey);
              }
            });

          }

        }
        
        this.getAllAnsweredSurveys();
        //console.log("this.allSurveyList : ", this.allSurveyList);
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  getAllAnsweredSurveys() {
    this.allAnsweredSurveyList = [];

    let surveyObj = new Survey();
    surveyObj.empId = this.currentUser.empId;

    this.surveyService.getAnsweredSurveysByEmpId(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allAnsweredSurveyList = response.serviceResponse;
        //console.log("this.allAnsweredSurveyList : ", response.serviceResponse);
        this.allAnsweredSurveyList.forEach((answeredSurvey: Survey) => {
          let surveyObj = this.allSurveyList.find((survey: Survey) => answeredSurvey.surveyId == survey.surveyId);
          if (surveyObj) surveyObj.isAnswered = true;
        });
      } else {
        console.error(response.serviceResponse);
      }
    });
  }

  // onTakeSurvey(surveyObj: Survey) {
  //   this.isSurveyLoaded = false;
  //   this.surveyObj = new Survey();
  //   this.allSurveyQuestionList = [];

  //   this.isSurveyForm = true;
  //   this.isSurveyList = false;

  //   this.surveyService.getAllQuestionsBySurveyId(surveyObj).pipe(first()).subscribe((response: any) => {
  //     if (response.serviceStatus == "Success") {
  //       this.allSurveyQuestionList = response.serviceResponse;

  //       this.surveyObj = surveyObj;
  //       this.allSurveyQuestionList.forEach((survey: SurveyQuestion) => {
  //         survey.optionsList = JSON.parse(survey.options);
  //         survey.required = JSON.parse(survey.required);
  //       });

  //       const surveyQuestionsTemplate: string = this.createTemplate();

  //       const formStart = `<form id="surveyForm">`
  //       const formEnd = `</form>`
  //       const surveyTemplate = formStart + surveyQuestionsTemplate + formEnd;

  //       this.isSurveyForm = true;
  //       this.isSurveyList = false;

  //       setTimeout(() => {
  //         let surveyContainer = document.getElementById('surveyContainer');
  //         surveyContainer.insertAdjacentHTML('afterbegin', surveyTemplate);
  //         this.isSurveyLoaded = true;
  //       }, 1000)

  //     } else {
  //       console.error(response.serviceResponse);
  //     }
  //   });
  // }

onTakeSurvey(surveyObj: Survey) {
  this.isSurveyLoaded = false;
  this.surveyObj = new Survey();
  this.allSurveyQuestionList = [];

  this.isSurveyForm = true;
  this.isSurveyList = false;

  this.surveyService.getAllQuestionsBySurveyId(surveyObj).pipe(first()).subscribe((response: any) => {
    if (response.serviceStatus === "Success") {
      this.allSurveyQuestionList = response.serviceResponse;
      this.surveyObj = surveyObj;

      this.allSurveyQuestionList.forEach((survey: SurveyQuestion) => {
        survey.optionsList = JSON.parse(survey.options);
        survey.required = JSON.parse(survey.required);
      });

      this.portalService.getAllEmployeeForPortalConfig().pipe(first()).subscribe((empResponse: any) => {
        if (empResponse.serviceStatus === "Success") {
          this.allEmployeeList = empResponse.serviceResponse.map(emp => ({
            ...emp,
            employeementId: emp.isApmosysProduct === "true"
              ? "AP-" + emp.employeementId
              : "A-" + emp.employeementId
          }));

          const surveyQuestionsTemplate: string = this.createTemplate();
          const formStart = `<form id="surveyForm">`;
          const formEnd = `</form>`;
          const surveyTemplate = formStart + surveyQuestionsTemplate + formEnd;

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

              this.isSurveyLoaded = true;
            }
          }, 500);
        } else {
          console.error(empResponse.serviceResponse);
        }
      });
    } else {
      console.error(response.serviceResponse);
    }
  });
}




  onViewMyResponse(surveyObj: Survey) {
    console.log("surveyObj", surveyObj);
    
    this.myResponseList = [];
    this.surveyObj = surveyObj;

    surveyObj.empId = this.currentUser.empId;
    //console.log("For View My Response : ", surveyObj);
    this.surveyService.getSurveyResponseByEmpIdAndSurveyId(surveyObj).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.myResponseList = response.serviceResponse;
        //console.log("this.myResponseList : ", this.myResponseList);
        if(!this.isEdit){
          this.openSurveyPreviewMod(this.previewResponseTemplate);
        }
      } else {
        console.error(response.serviceResponse);
      }
    });
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

  onSubmit(template: TemplateRef<any>) {
    const form: any = document.getElementById('surveyForm');

    let surveyObj = new Survey();
    surveyObj.empId = this.currentUser.empId;
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
        this.router.navigate(['/user-survey']);
        this.showSurveys();
      } else {
        this.openAlertMod(template, response.serviceResponse);
      }
    });
  }


createTemplate(): string {
  let surveyTemplate = ``;

  this.allSurveyQuestionList.forEach((question: SurveyQuestion, qIndex) => {

    let finalQuestionTemplate = ``;
    const questionStartTemplate = `<div class="row"><div class="form-group">`;
    const questionEndTemplate = `</div></div>`;
    const questionRequiredTemplate = `<span class="text-danger">*</span>`;
    const isQuestionRequired = (question.required === true) ? questionRequiredTemplate : '';

    // Question header
    const questionTemplate = `
      <h5 class="mb-0">
        <i class="fa-solid fa-q question-icon"></i>.&nbsp;
        ${question.question ?? ''}${isQuestionRequired}
      </h5>
      <small class="text-secondary">${question.description ?? ''}</small>
    `;

    finalQuestionTemplate = questionStartTemplate + questionTemplate;

    if (question.optionType === "text") {
      finalQuestionTemplate += `<textarea class="form-control" rows="1" name="question-${qIndex + 1}"></textarea>`;
    } 
    else if (question.optionType === "checkbox") {
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
    } 
    else if (question.optionType === "radio") {
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
  else if (question.optionType == "dropdown") {
  let optionTemplate = `
    <select class="form-select" name="question-${qIndex + 1}">
      <option value="" disabled selected>Select an Employee</option>
  `;

  this.allEmployeeList.forEach((emp: any) => {
    optionTemplate += `
      <option value="${emp.name} (${emp.employeementId})-(${emp.departmentName})">
        ${emp.name} (${emp.employeementId})-(${emp.departmentName})
      </option>
    `;
  });

  optionTemplate += `</select>`;
  finalQuestionTemplate += optionTemplate;
}

    // close question block
    finalQuestionTemplate += questionEndTemplate;
    surveyTemplate += finalQuestionTemplate;
  });

  return surveyTemplate;
}


  onClickEdit(surveyObj: Survey): void {
    console.log("Survey", surveyObj);
    this.modalRef.hide();
    

    this.surveyService.setSurveyData(surveyObj);
    
    // Navigate to the edit page
    this.router.navigate(['/user-survey', surveyObj.surveyId, 'edit']);
  }


  //modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }

  openSurveyPreviewMod(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template, { class: 'modal-lg' });
  }

  cancelRequest() {
    this.modalRef.hide();
  }

  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort){	
    //console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;      
    }
  }

  // advance search
  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }
  onSearch(searchData){
    this.filters = searchData;
    //console.log("Updated Filter : ", this.filters);
  }
  
  //end


    getAllEmployees(template: TemplateRef<any>) {
    this.portalService.getAllEmployeeForPortalConfig().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allEmployeeList = response.serviceResponse;
        // this.allEmployeeList = this.allEmployeeList.filter(x => x.employmentstatus != 'InActive');
        this.allEmployeeList.forEach((employee) => {
          employee.employeementId = "A-".concat(employee.employeementId)
        });
        //console.log("allEmployeeList : ", this.allEmployeeList);
      } else {
        this.openAlertMod(template, response.serviceResponse)
      }
    });
  }

}

function compare(a: number | string, b: number | string, isAsc: boolean) {
  return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
}