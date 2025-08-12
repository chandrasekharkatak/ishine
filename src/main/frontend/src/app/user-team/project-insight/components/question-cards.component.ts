import { Component, Input, Output, EventEmitter, TemplateRef, ViewChild } from '@angular/core';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Observable } from 'rxjs';
import { first } from 'rxjs/operators';
import { ProjectInsightProjectDetails } from 'src/app/models/projectInsightDetails';
import { ProjectInsightGroupDetails } from 'src/app/models/projectInsightGroupDetails';
import { ProjectInsightQuestionDetails } from 'src/app/models/projectInsightQuestionDetails';
import { ProjectResponse } from 'src/app/models/projectResponse';
import { SurveyOption } from 'src/app/models/sureyOption';
import { User } from 'src/app/models/user';
import { ApiSourceService } from 'src/app/services/api-source.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { FormBuilderService } from 'src/app/services/form-builder.service';
import { ProjectInsightDomainService } from 'src/app/services/project-insight-domain.service';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectService } from 'src/app/services/project.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-question-cards',
  templateUrl: './question-cards.component.html',
  styleUrls: ['./question-cards.component.scss']
})
export class QuestionCardsComponent {

  @ViewChild('alert_message_modal') alertMessageTemplate: TemplateRef<any>;
  @ViewChild('add_or_update_question_modal') addOrUpdateQuestionModal: TemplateRef<any>;
  @ViewChild('delete_question_modal') deleteQuestionModal: TemplateRef<any>;

  alertModalRef: BsModalRef = new BsModalRef();
  addOrUpdateQuestionModalRef: BsModalRef = new BsModalRef();
  deleteQuestionModalRef: BsModalRef = new BsModalRef();

  @Input() currentNodeType: any;
  @Input() projectInsightGroupDetails: ProjectInsightGroupDetails;
  @Input() projectInsightProjectDetails: ProjectInsightProjectDetails;
  @Input() currentNode: any;
  @Input() rootNode: any;

  allEmployeeList = [] = [];
  questionList: ProjectInsightQuestionDetails[] = [];

  currentUser: User;
  alertMessage: any;
  parentId: any
  parentType: any;

  question: ProjectInsightQuestionDetails = new ProjectInsightQuestionDetails();
  deletedQuestion: ProjectInsightQuestionDetails = new ProjectInsightQuestionDetails();

  isQuestionUpdate: boolean = true;

  constructor(
    private modalService: BsModalService,
    private formBuilderService: FormBuilderService,
    private validationService: ValidationService,
    private authenticationService: AuthenticationService,
    private projectInsightService: ProjectInsightService,
    private apiSourceService: ApiSourceService,
    private projectInsightDomainService: ProjectInsightDomainService,
    private employeeService: EmployeeService,
    private projectService: ProjectService
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    this.getAllEmployeeList();
  }

  getAllEmployeeList() {
    this.allEmployeeList = [];
    this.formBuilderService.getAllEmployeeList().pipe(first()).subscribe({
      next: (response: any) => {
        this.allEmployeeList = response;
      },
      error: (error: any) => {
        this.openAlertModal(error);
      }
    });
  }

  // Question Logic [Start]
  async getProjectInsightQuestionDetailsByParentIdAndParentType(parentId: any, parentType: any): Promise<void> {
    this.questionList = [];
    try {
      const response: any = await this.projectInsightService
        .getProjectInsightQuestionDetailsByParentIdAndParentType(parentId, parentType)
        .pipe(first())
        .toPromise();
      this.questionList = response.serviceResponse;
    } catch (error) {
      this.openAlertModal(error);
      throw error;
    }
  }

  openAddOrUpdateQuestionModal(isQuestionUpdate: any, question?: any) {
    if (this.currentNodeType == 'Project') {
      if (this.projectInsightProjectDetails && !this.validationService.validateNullUndefinedEmptyString(this.projectInsightProjectDetails?.id)) {
        this.openAlertModal('Kindly Save Project Insight Details before adding a Question.');
        return;
      }
    } else {
      if (this.projectInsightGroupDetails && !this.validationService.validateNullUndefinedEmptyString(this.projectInsightGroupDetails?.id)) {
        this.openAlertModal('Kindly Save Group Details before adding a Question.');
        return;
      }
    }
    if (!this.currentNode || !this.validationService.validateNullUndefinedEmptyString(this.currentNode?.parentId) || !this.validationService.validateNullUndefinedEmptyString(this.currentNode?.parentType)) {
      if (this.currentNodeType == 'Project') {
        this.parentId = this.validationService.validateNullUndefinedEmptyString(this.rootNode.parentId) ? this.rootNode.parentId : this.projectInsightProjectDetails.id;
        this.parentType = this.validationService.validateNullUndefinedEmptyString(this.rootNode.parentType) ? this.rootNode.parentType : 'Project';
      } else {
        this.parentId = this.validationService.validateNullUndefinedEmptyString(this.currentNode?.parentId) ? this.currentNode?.parentId : this.projectInsightGroupDetails.id;
        this.parentType = this.validationService.validateNullUndefinedEmptyString(this.currentNode?.parentType) ? this.currentNode?.parentType : 'Group';
      }
    } else {
      this.parentId = this.currentNode.parentId;
      this.parentType = this.currentNode.parentType;
    }
    this.isQuestionUpdate = isQuestionUpdate;
    this.question = question || new ProjectInsightQuestionDetails();
    this.addOrUpdateQuestionModalRef = this.modalService.show(this.addOrUpdateQuestionModal, { class: 'modal-lg modal-dialog-centered' });
  }

  closeAddOrUpdateQuestionModal() {
    if (this.addOrUpdateQuestionModalRef) {
      this.addOrUpdateQuestionModalRef.hide();
    }
  }

  addOrUpdateQuestion() {
    let projectInsightQuestionDetails = this.question;
    projectInsightQuestionDetails.parentId = this.parentId;
    projectInsightQuestionDetails.parentType = this.parentType;
    if (this.isQuestionUpdate) {
      projectInsightQuestionDetails.updatedBy = this.currentUser.empId;
    } else {
      projectInsightQuestionDetails.createdBy = this.currentUser.empId;
    }
    this.projectInsightService.saveProjectInsightQuestionDetails(projectInsightQuestionDetails).pipe(first()).subscribe(
      (response: any) => {
        this.openAlertModal(response.serviceStatus);
        this.closeAddOrUpdateQuestionModal();
        this.getProjectInsightQuestionDetailsByParentIdAndParentType(this.parentId, this.parentType);
      },
      (error) => {
        console.log(error, " : error");
        this.openAlertModal(error?.error?.serviceResponse || 'An unexpected error occurred.');
      }
    );
  }

  openDeleteQuestionModal(question: any) {
    this.deletedQuestion = question;
    this.deletedQuestion.parentId = this.currentNode.parentId;
    this.deletedQuestion.parentType = this.currentNode.parentType;
    this.deleteQuestionModalRef = this.modalService.show(this.deleteQuestionModal, { class: 'modal-md' });
  }

  deleteProjectInsightQuestionDetails() {
    this.projectInsightService.deleteProjectInsightQuestionDetails(this.deletedQuestion).pipe(first()).subscribe({
      next: (response: any) => {
        this.openAlertModal(response.serviceStatus);
        this.closeDeleteQuestionModal();
        this.getProjectInsightQuestionDetailsByParentIdAndParentType(this.deletedQuestion.parentId, this.deletedQuestion.parentType);
      },
      error: (error: any) => {
        this.openAlertModal(error);
      }
    });
  }

  closeDeleteQuestionModal() {
    if (this.deleteQuestionModalRef) {
      this.deleteQuestionModalRef.hide();
    }
  }

  onAssignToChange(question: any) {
    if (!question.projectResponseList) {
      question.projectResponseList = [];
    }
    const selectedEmpIds = question.toAssignEmployeeList || [];
    question.projectResponseList = question.projectResponseList.filter(
      (resp: ProjectResponse) => selectedEmpIds.includes(resp.responseBy)
    );

    selectedEmpIds.forEach((empId: number) => {
      if (!question.projectResponseList.some((resp: ProjectResponse) => resp.responseBy === empId)) {
        const emp: any = this.allEmployeeList.find((e: any) => e.empId == empId);
        const response = new ProjectResponse();
        response.responseBy = empId;
        response.responseByEmpName = emp ? emp.name : '';
        response.assignedOn = new Date();
        question.projectResponseList.push(response);
      }
    });
  }
  // Question Logic [End]

  // Option Configurations [Start]
  addOption(i, questionObj: ProjectInsightQuestionDetails) {
    questionObj?.optionsList.splice(i + 1, 0, new SurveyOption());
  }

  removeOption(i, questionObj: ProjectInsightQuestionDetails) {
    questionObj?.optionsList.splice(i, 1);
  }

  setOption(questionObj: ProjectInsightQuestionDetails) {
    if (questionObj.optionType == "checkbox" || questionObj.optionType == "radio") {
      questionObj.optionsList = [];
      questionObj.optionsList.splice(1, 0, new SurveyOption());
    }
  }
  // Option Configurations [End]


  // Modals [Start]
  openAlertModal(message: any) {
    this.alertMessage = message;
    this.alertModalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
  }

  cancelRequest() {
    if (this.alertModalRef) {
      this.alertModalRef.hide();
    }
  }
  // Modals [End]

}

