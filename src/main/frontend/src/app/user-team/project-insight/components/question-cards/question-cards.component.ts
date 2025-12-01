import { Component, Input, Output, EventEmitter, TemplateRef, ViewChild } from '@angular/core';
import { UntypedFormControl } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, first, startWith, switchMap, tap } from 'rxjs/operators';
import { ENTITY_TYPES, EntityType } from 'src/app/models/EntityType';
import { ProjectInsightProjectDetails } from 'src/app/models/projectInsightDetails';
import { ProjectInsightFacetCategory } from 'src/app/models/projectInsightFacetCategory';
import { ProjectInsightGroupDetails } from 'src/app/models/projectInsightGroupDetails';
import { ProjectInsightQuestionDetails } from 'src/app/models/projectInsightQuestionDetails';
import { ProjectResponse } from 'src/app/models/projectResponse';
import { SurveyOption } from 'src/app/models/sureyOption';
import { User } from 'src/app/models/user';
import { ApiSourceService } from 'src/app/services/api-source.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { FormBuilderService } from 'src/app/services/form-builder.service';
import { KnowledgeHubService } from 'src/app/services/knowledge-hub.service';
import { ProjectInsightDomainService } from 'src/app/services/project-insight-domain.service';
import { ProjectInsightFacetService } from 'src/app/services/project-insight-facet.service';
import { ProjectInsightQuestionLibraryService } from 'src/app/services/project-insight-question-library.service';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectService } from 'src/app/services/project.service';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  standalone: false,
  selector: 'app-question-cards',
  templateUrl: './question-cards.component.html',
  styleUrls: ['./question-cards.component.scss']
})
export class QuestionCardsComponent {

  @ViewChild('alert_message_modal') alertMessageTemplate: TemplateRef<any>;
  @ViewChild('add_or_update_question_modal') addOrUpdateQuestionModal: TemplateRef<any>;
  @ViewChild('delete_question_modal') deleteQuestionModal: TemplateRef<any>;
  @ViewChild('Open_Question_Overview') quesDetailView!: TemplateRef<any>;
  @ViewChild('Open_Response_History') responseHistoryView!: TemplateRef<any>;
  @ViewChild('Open_Question_Approval') responseApproval!: TemplateRef<any>;
  @ViewChild('open_confirmtion_Approval') confirmationApproval!: TemplateRef<any>;
  @ViewChild('open_confirmtion_Reassign') confirmationReassign!: TemplateRef<any>;

  alertModalRef:NgbModalRef;
  confirmationForApproval:NgbModalRef;
  confirmationForReassign:NgbModalRef;
  giveResponse:NgbModalRef;
  showHistoryResponse:NgbModalRef;
  addOrUpdateQuestionModalRef:NgbModalRef;
  deleteQuestionModalRef:NgbModalRef;

  @Input() viewMode!: any;
  @Input() currentNodeType: any;
  @Input() projectInsightGroupDetails: ProjectInsightGroupDetails;
  @Input() projectInsightProjectDetails: ProjectInsightProjectDetails;
  @Input() currentNode: any;
  @Input() rootNode: any;
  @Input() isQuestionOverview:boolean = false;
  @Input() searching = {value:false, query:""};
  @Input() isApprovalTab:boolean = false;

  allEmployeeList = [] = [];
  questionList: ProjectInsightQuestionDetails[] = [];

  parentId: any
  parentType: any;
  alertMessage: any;
  isQuestionUpdate: boolean = true;

  currentUser: User;
  question: ProjectInsightQuestionDetails = new ProjectInsightQuestionDetails();
  deletedQuestion: ProjectInsightQuestionDetails = new ProjectInsightQuestionDetails();
  selectedQuestionDetails:any;
  
  approvalConsent:boolean = null;
  reassignChecked:boolean = true;
  showSubmitApproval:boolean = false;
  questionControl = new UntypedFormControl('');
  
  filteredQuestions$: Observable<any[]> = of([]);
  filteredQuestions: any[] = [];
  responseHistory: any[]=[];
  allDeptList: any[] = [];
  filteredCategories:ProjectInsightFacetCategory[] = [];
  facetCategoryList:ProjectInsightFacetCategory[] = [];

  filterText: string = '';
  
  quesStatusMap: { [key: string]: number } = {};
  versionHistoryMap: {[key: string]: number} = {};
  showContextMenu = false;
  contextMenuX = 0;
  contextMenuY = 0;
  selectedText = '';
  newTag: string = '';
  editorConfig: any = {
    editable: true,
    spellcheck: true,
    height: '20rem',
    minHeight: '5rem',
    width: 'auto',
    minWidth: '0',
    translate: 'yes',
    enableToolbar: true,
    showToolbar: true,
    placeholder: 'Enter Response here...',
    defaultParagraphSeparator: '',
    defaultFontName: '',
    defaultFontSize: '',
    uploadWithCredentials: false,
    sanitize: false,
    toolbarPosition: 'top',
    fonts: [{ class: 'arial', name: 'Arial' }],
    toolbarHiddenButtons: [
      [
        'insertImage',
        'insertVideo'
      ]
    ],
    modules: {
    toolbar: true
  }
  };

  readonlyEditorConfig = {
    ...this.editorConfig,
    editable: false,
    enableToolbar: false,
    showToolbar: false,
     modules: {
    toolbar: false     
  },
  };

  selectedFromList = false;

  constructor(
    private modalService: NgbModal,
    private formBuilderService: FormBuilderService,
    private validationService: ValidationService,
    private authenticationService: AuthenticationService,
    private projectInsightService: ProjectInsightService,
    private apiSourceService: ApiSourceService,
    private projectInsightDomainService: ProjectInsightDomainService,
    private employeeService: EmployeeService,
    private projectService: ProjectService,
    private departmentService: DepartmentService,
    private projectInsightFacetService: ProjectInsightFacetService,
    private projectInsightQuestionLibraryService: ProjectInsightQuestionLibraryService,
    private knowledgeHubService: KnowledgeHubService
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    this.getAllEmployeeList();
    this.getAllDepartmentList();

    this.filteredQuestions$ = this.questionControl.valueChanges.pipe(
      startWith(''),
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(value => {
        if (typeof value === 'string' && value.trim().length > 0) {
          return this.projectInsightQuestionLibraryService.searchQuestionLibrary(value);
        } else {
          return of([]);
        }
      }),
      tap(data => this.filteredQuestions = data)
    );

    // Detect manual typing and clear other fields if not selected
    this.questionControl.valueChanges.subscribe(value => {
      if (!this.selectedFromList) {
        // User typed a new question, clear other fields
        this.question.question = value;
        if (!this.isQuestionUpdate) {
          this.question.id = null;
          this.question.description = '';
          this.question.optionType = '';
          this.question.optionsList = [];
          this.question.addToQuestionBank = false;
        }
      }
      this.selectedFromList = false; // reset after handling
    });
  }

  getAllAssignedQuestionsForUser(parentId:any,parentType:any){
    let payload = {
      parentId:parentId,
      parentType:parentType,
      empId:this.currentUser.empId
    }
    this.projectInsightService.getAllQuestionsForUserByParentIdAndParentType(payload).pipe(first()).subscribe({
      next: (response: any) => {
        this.questionList = response.questions;
        this.quesStatusMap = response.statusMap;
        this.versionHistoryMap = response.historyMap;
      },
      error: (error: any) => {
        this.openAlertModal(error);  
      }
    });
  }

  getAllQuestionsForApprovalByParentIdAndParentType(parentId:any,parentType:any){
    let payload = {
      parentId:parentId,
      parentType:parentType,
      empId:this.currentUser.empId
    }
    this.projectInsightService.getAllQuestionsForApprovalTab(payload).pipe(first()).subscribe({
      next: (response: any) => {
        this.questionList = response.questions;
        this.quesStatusMap = response.statusMap;
      },
      error: (error: any) => {
        this.openAlertModal(error);  
      }
    });
  }

  getAllDepartmentList() {
    this.allDeptList = [];
    this.departmentService.getAllDeptsList().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  highlight(text: any): string {
    
    // if (!this.searching.value) return text;
    // if (!this.searching.query || text == null) {
    //   return typeof text === 'string' ? text : JSON.stringify(text);
    // }

    // const textStr = typeof text === 'string' ? text : JSON.stringify(text, null, 2);
    // const escapedQuery = this.searching.query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    // const regex = new RegExp(escapedQuery, 'gi');

    // console.log("Found in text: ", textStr.match(regex));
    
    // return textStr.replace(regex, match =>
    //   `<span class="highlight">${match}</span>`
    // );

    const highlightedText = this.knowledgeHubService.highlight(text, this.searching.query, !!this.searching.value);
    // if(text.toLowerCase().includes("i")){
    //   console.log("Highlighted Text : ", highlightedText);
    // }
    return highlightedText;
    
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

  async openAddOrUpdateQuestionModal(isQuestionUpdate: any, question?: any) {
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
    await this.getAllProjectInsightFacetCategory();
    if (this.isQuestionUpdate) {
      this.projectInsightService.getProjectInsightQuestionDetailsByObjectId(question?.id).pipe(first()).subscribe((response: any) => {
        if (response?.serviceStatus == 'Success') {
          this.question = response.serviceResponse;
          this.questionControl.setValue(this.question.question, { emitEvent: false });
          this.addOrUpdateQuestionModalRef = this.modalService.open(this.addOrUpdateQuestionModal, { modalDialogClass: 'modal-lg modal-dialog-centered', backdrop: 'static', keyboard: false });
        } else {
          this.openAlertModal(response?.serviceResponse || 'An unexpected error occurred.');
          return;
        }
      },
        (error) => {
          this.openAlertModal(error?.error?.serviceResponse || 'An unexpected error occurred.');
          return;
        }
      );
    } else {
      this.questionControl.setValue('', { emitEvent: false });
      this.question = new ProjectInsightQuestionDetails();
      this.addOrUpdateQuestionModalRef = this.modalService.open(this.addOrUpdateQuestionModal, { modalDialogClass: 'modal-lg modal-dialog-centered', backdrop: 'static', keyboard: false });
    }
  }

  closeAddOrUpdateQuestionModal() {
    if (this.addOrUpdateQuestionModalRef) {
      this.addOrUpdateQuestionModalRef.close();
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
    let inputValidated: boolean = this.validateProjectInsightQuestionLibraryEntry(projectInsightQuestionDetails);
    if (!inputValidated) return;

    projectInsightQuestionDetails.isQuestionUpdate = this.isQuestionUpdate;
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
    this.deleteQuestionModalRef = this.modalService.open(this.deleteQuestionModal, { modalDialogClass: 'modal-md' });
  }

  openResponseModal(question:any){
    console.log('Question is : ',question);
      this.giveResponse?.close();
      const payload = {
        parentId: question.id,
        empId: this.currentUser.empId
      };
      this.projectInsightService.getQuestionDetailsById(payload).subscribe((res: any) => {
        this.selectedQuestionDetails = res;
        console.log('Question and response details is : ',this.selectedQuestionDetails);
        if(this.isApprovalTab){
          this.approvalConsent = null;
          this.showSubmitApproval = false;
          let n = res?.response?.reviewerInfo.length;
          if(res?.response?.reviewerInfo[n-1].isApproved == null && res?.response?.reviewerInfo[n-1].reviewerid == this.currentUser.empId)this.showSubmitApproval = true;
          this.giveResponse = this.modalService.open(this.responseApproval, { modalDialogClass: 'modal-xl' });
        }else{
          this.giveResponse = this.modalService.open(this.quesDetailView, { modalDialogClass: 'modal-lg' });
        }
      });
  }

  openHistoryModal(questionId: any, event: MouseEvent) {
    event.stopPropagation();
    let payload = {
      parentId:questionId,
      empId:this.currentUser.empId
    }
    this.projectInsightService.getResponseHistory(payload).subscribe((res: any) => {
      this.responseHistory = res;
      this.showHistoryResponse = this.modalService.open(this.responseHistoryView, { modalDialogClass: 'modal-lg' });
    });
  }
  

  saveDraft(response:any,question:any) {
    if(this.quesStatusMap[question.id] == 3){
      this.giveResponse?.close();
      this.openAlertModal('The Response you filled earlier for this Question is already Assigned for review. You cannot change it now');
    }else{
      if(response?.isDraft == null){
        response.lastSavedOn = new Date().toISOString().slice(0, 19);
        response.isDraft = true;
        response.quesId = question.id;
        response.responseBy = this.currentUser.empId;
        response.responseByEmpName = this.currentUser.name;
        response.reviewerInfo = null;
      } else{
        response.lastSavedOn = new Date().toISOString().slice(0, 19);
        response.reviewerInfo = null;
        if(question.optionType != null){
          response.optionsList = question.optionsList;
        }
      }
      this.projectInsightService.saveAnswerAsDraft(response).pipe(first()).subscribe({
        next: (res: any) => {
          this.quesStatusMap[question.id] = 2;
          this.giveResponse?.close();
          this.sendForUpdate(question,ENTITY_TYPES.QUESTION);
          this.openAlertModal(res);
        },
        error: (error: any) => {
          this.cancelRequest();
          this.openAlertModal(error);
          }
      });
    }
  }

  submitApprovalConsent(){
    let n = this.selectedQuestionDetails.response.reviewerInfo.length;
    if(this.approvalConsent==null){
      this.openAlertModal('Please fill and verify the consent Properly before Submit.');
    }else if(!this.approvalConsent && 
      (this.selectedQuestionDetails?.response?.reviewerInfo[n-1]?.remarks==null ||
        this.selectedQuestionDetails?.response?.reviewerInfo[n-1]?.remarks=='')){
        this.openAlertModal('Remark is manditory if you are ❌Rejecting the Application');
    }else if(this.selectedQuestionDetails?.response?.reviewerInfo[n-1]?.marks == null && this.approvalConsent){
        this.openAlertModal('Please select the marks/rating between 1 to 10 to continue as it is manditory if you are ✅Approving the Application');
    }else{
      this.openConfirmationForApproval();
    }
  }

  saveMyApproval(){
    this.confirmationForApproval?.close();
    let n = this.selectedQuestionDetails.response.reviewerInfo.length;
    this.selectedQuestionDetails.response.reviewerInfo[n-1].isApproved = this.approvalConsent;
    let payload = {
      response:this.selectedQuestionDetails.response,
      doReassign:this.reassignChecked,
      editedByApprover:this.reassignChecked==false && this.approvalConsent==false,
      reviewerName:this.currentUser.name
    }
    this.projectInsightService.saveApproval(payload).pipe(first()).subscribe({
      next: (res: any) => {
        this.selectedQuestionDetails.response = res;
        this.giveResponse?.close();
        this.sendApprovalCountsForUpdate(this.selectedQuestionDetails.question);
        this.getAllQuestionsForApprovalByParentIdAndParentType(this.selectedQuestionDetails?.question?.parentId,this.selectedQuestionDetails?.question?.parentType);
        if(payload.editedByApprover){
          this.openAlertModal('Response Modified and Forwarded Sucessfully.');
          this.approvalConsent = null;
        }else{
          let message = 'Response '+this.approvalConsent?'✅ Approved':'❌ Rejected';
          if(payload.doReassign)message+=' and Reassigned Back to Responder.'
          this.openAlertModal(message);
          this.approvalConsent = null;
        }
      },
      error: (error: any) => {
        this.cancelRequest();
        this.openAlertModal(error);
        }
    });
  }

  necessaryChecks(quesResponsepair:any){
    let type = quesResponsepair?.approvalType;
    let quesAnsPair = quesResponsepair?.quesAns;
    if(type=='reject'){
      if(quesAnsPair.response.reviewerInfo[quesAnsPair.response.reviewerInfo.length-1].remarks==''){
        this.openAlertModal('Remark is Manditory if Rejecting the Response.');
      }else{
        this.saveApproval(quesAnsPair,type);
      }
    }else if(type=='edit'){
      if(quesAnsPair.response.reviewerInfo[quesAnsPair.response.reviewerInfo.length-1].marks==null 
        || quesAnsPair.response.reviewerInfo[quesAnsPair.response.reviewerInfo.length-1].marks==0){
          this.openAlertModal('Marks/Rating is manditory while Approving Responses.');
      }
      else if(quesAnsPair.response.reviewerInfo[quesAnsPair.response.reviewerInfo.length-1].quality==''){
        this.openAlertModal('Quality-Rating is manditory while approving Responses.')
      }else if(quesAnsPair.response.reviewerInfo[quesAnsPair.response.reviewerInfo.length-1].remarks==''){
        this.openAlertModal('Remark is Manditory while Editing the Response.');
      }else{
        this.saveApproval(quesAnsPair,type);
      }
    }else if(type=='approve'){
      if(quesAnsPair.response.reviewerInfo[quesAnsPair.response.reviewerInfo.length-1].marks==null 
        || quesAnsPair.response.reviewerInfo[quesAnsPair.response.reviewerInfo.length-1].marks==0){
          this.openAlertModal('Marks/Rating is manditory while Approving Responses.');
      }
      else if(quesAnsPair.response.reviewerInfo[quesAnsPair.response.reviewerInfo.length-1].quality==''){
        this.openAlertModal('Quality-Rating is manditory while approving Responses.')
      }else{
        this.saveApproval(quesAnsPair,type);
      }
    }
  }

  saveApproval(quesResponsepair:any,type:any){
    let reassign = false;
    if(quesResponsepair.response.reviewerInfo.length==1 && type=='reject')reassign=true;
    let payload = {
      response:quesResponsepair.response,
      doReassign:reassign,
      editedByApprover:type=='edit',
      reviewerName:this.currentUser.name
    }
    this.projectInsightService.saveApproval(payload).pipe(first()).subscribe({
      next: (res: any) => {
        this.updateAndShow(type,quesResponsepair.question);
      },
      error: (error: any) => {
        this.cancelRequest();
        this.openAlertModal(error);
        }
    });
  }

  SaveApproverEditedResponse(){
    let n = this.selectedQuestionDetails.response.reviewerInfo.length;
    this.selectedQuestionDetails.response.reviewerInfo[n - 1].remarks =
    `Edited By ${this.currentUser.name} at level ${this.selectedQuestionDetails.response.reviewerInfo[n - 1].level}`;
    this.selectedQuestionDetails.response.reviewerInfo[n-1].marks=10;
    this.approvalConsent = true;
    this.saveMyApproval();
  }

  cleanAndReassign(responseId:any){
    if(responseId == null){
      this.openAlertModal('Response is Null. Unable to initiate Reassign.');
    }else{
      // this.openReassignConfirmationAlert();
    let payload = {
      parentId:responseId
    }
    this.projectInsightService.cleanReassignResponse(payload).pipe(first()).subscribe({
      next: (res: any) => {
        this.giveResponse?.close();
        this.openAlertModal(res);
        this.sendApprovalCountsForUpdate(this.selectedQuestionDetails?.question);
        this.getAllQuestionsForApprovalByParentIdAndParentType(this.selectedQuestionDetails?.question?.parentId,this.selectedQuestionDetails?.question?.parentType);
        //update Question Status List and update group/project counts.
      },
      error: (error: any) => {
        this.cancelRequest();
        this.openAlertModal(error);
        }
    });
    }
  }

  reassignResponse(responseId:any){
    this.confirmationForReassign?.close();
    let payload = {
      parentId:responseId
    }
    this.projectInsightService.cleanReassignResponse(responseId).pipe(first()).subscribe({
      next: (res: any) => {
        this.giveResponse?.close();
        this.openAlertModal(res);
        this.sendApprovalCountsForUpdate(this.selectedQuestionDetails?.question);
        this.getAllQuestionsForApprovalByParentIdAndParentType(this.selectedQuestionDetails?.question?.parentId,this.selectedQuestionDetails?.question?.parentType);
        //update Question Status List and update group/project counts.
      },
      error: (error: any) => {
        this.cancelRequest();
        this.openAlertModal(error);
        }
    });
  }

  sendForUpdate(QG: any, type: EntityType) {
    let projectIds: any[] = [];
    let groupIds: any[] = [];
  
    if (type === ENTITY_TYPES.QUESTION || type === ENTITY_TYPES.GROUP) {
      projectIds = QG.parentPathIds.slice(0, 1);
      groupIds = QG.parentPathIds.slice(1);
  
      if (type === ENTITY_TYPES.GROUP) {
        groupIds.push(QG.id);
      }
    } else if (type === ENTITY_TYPES.PROJECT) {
      projectIds = [QG.id];
      groupIds = [];
    }
  
    const payload = {
      projects: projectIds,
      groups: groupIds,
      empId: this.currentUser.empId
    };
  
    this.refreshCountsByParentPath(payload);
  }  

  sendApprovalCountsForUpdate(question:any){
    let projectIds: any[] = [];
    let groupIds: any[] = [];
    projectIds = question.parentPathIds.slice(0, 1);
    groupIds = question.parentPathIds.slice(1);
    const payload = {
      projects: projectIds,
      groups: groupIds,
      empId: this.currentUser.empId
    };
    this.projectInsightService.refreshCountsForApprovalTabByQuestion(payload).pipe(first()).subscribe({
      next: (response: any) => {
        Object.entries(response).forEach(([key, value]) => {
          this.projectService.projectMap.set(key, value);
        });
      },
      error: (error: any) => {
        this.openAlertModal('Error in refreshing counts : '+error);
        }
    });

  }

  refreshCountsByParentPath(payload: any) {
    this.projectInsightService.refreshByParentPath(payload).pipe(first()).subscribe({
      next: (response: any) => {
        Object.entries(response).forEach(([key, value]) => {
          this.projectService.projectMap.set(key, value);
        });
      },
      error: (error: any) => {
        this.openAlertModal('Error in refreshing counts : '+error);
        }
    });
  }  

  updateAndShow(type:any,question:any){
    this.giveResponse?.close();
    this.sendApprovalCountsForUpdate(question);
    this.getAllQuestionsForApprovalByParentIdAndParentType(question.parentId,question.parentType);
    let message = 'Response ' + type=='approve'?'✅Approved':type=='edit'?'Edited and ✅Approved':type=='reject'?'❌Rejected':'Verified';
    this.openAlertModal(message);
  }

  getBackgroundColor(status: number): string {
    switch (status) {
      case 1: return '#f54d4d';
      case 2: return '#f7ee79';
      case 3: return '#2769f7';
      default: return '#ffffff';
    }
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
      this.deleteQuestionModalRef.close();
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

  onOptionSelected(event: MatAutocompleteSelectedEvent) {
    const selectedQuestion = this.filteredQuestions.find(q => q.question === event.option.value);
    if (!selectedQuestion) return;

    // Fetch complete object from backend if needed
    this.projectInsightQuestionLibraryService.getEntryFromsearchQuestionLibraryByText(selectedQuestion?.id)
      .subscribe(response => {
        if (response?.serviceStatus === 'Success') {
          const tempQuestion = response.serviceResponse;
          this.question.question = tempQuestion.question;
          this.question.description = tempQuestion.description;
          this.question.optionType = tempQuestion.optionType;
          this.question.optionsList = tempQuestion.optionsList;
          this.selectedFromList = true;
          // Update FormControl to match selected
          this.questionControl.setValue(tempQuestion.question, { emitEvent: false });
        } else {
          this.openAlertModal(response?.serviceResponse || 'An unexpected error occurred.');
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

  // Validations [Start]
  validateProjectInsightQuestionLibraryEntry(questionLibraryEntry: any) {
    let flag = true;
    if (!this.validationService.validateNullUndefinedEmptyString(questionLibraryEntry?.question)) {
      this.openAlertModal("Please Enter Question !!");
      return false;
    }
    if (!this.isQuestionUpdate && questionLibraryEntry?.addToQuestionBank && !this.validationService.validateNullUndefinedEmptyList(questionLibraryEntry.deptIds)) {
      this.openAlertModal("Please select atleast One Department !!");
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(questionLibraryEntry?.optionType)) {
      this.openAlertModal("Please select option type !!");
      return false;
    }
    if (questionLibraryEntry?.optionType !== 'checkbox' && questionLibraryEntry?.optionType !== 'radio' && questionLibraryEntry?.optionType !== 'text') {
      this.openAlertModal("Please select a valid option type !!");
      return false;
    }
    if ((questionLibraryEntry?.optionType && questionLibraryEntry?.optionType === 'checkbox' || questionLibraryEntry?.optionType === 'radio') && !this.validationService.validateNullUndefinedEmptyList(questionLibraryEntry?.optionsList)) {
      this.openAlertModal("Please add options for Option Type Check or Radio !!");
      return false;
    }
    if ((questionLibraryEntry?.optionType && questionLibraryEntry?.optionType === 'checkbox' || questionLibraryEntry?.optionType === 'radio') && this.validationService.validateNullUndefinedEmptyList(questionLibraryEntry?.optionsList)) {
      if ((questionLibraryEntry?.optionType && questionLibraryEntry?.optionType === 'checkbox' || questionLibraryEntry?.optionType === 'radio') && questionLibraryEntry?.optionsList?.length < 2) {
        this.openAlertModal("Please provide atleast 2 options for Option Type Check or Radio !!");
        return false;
      }
      for (let i = 0; i < questionLibraryEntry?.optionsList?.length; i++) {
        let option = questionLibraryEntry?.optionsList[i];
        if (!this.validationService.validateNullUndefinedEmptyString(option?.optionValue)) {
          this.openAlertModal(`Option cannot be null or Empty for Option ${i + 1}!!`);
          return false;
        }
      }
    }
    if (questionLibraryEntry?.toAssignedEmployeeIdList && !this.validationService.validateNullUndefinedEmptyList(questionLibraryEntry?.toAssignedEmployeeIdList)) {
      this.openAlertModal("Kindly select at least one user to assign !!");
      return false;
    }
    return flag;
  }
  // Validations [End]


  // Modals [Start]
  openConfirmationForApproval(){
    this.confirmationForApproval = this.modalService.open(this.confirmationApproval, { modalDialogClass: 'modal-md' });
  }

  openReassignConfirmationAlert(){
    this.confirmationForReassign = this.modalService.open(this.confirmationReassign, {modalDialogClass: 'modal-md'} );
  }

  openAlertModal(message: any) {
    this.alertMessage = message;
    this.alertModalRef = this.modalService.open(this.alertMessageTemplate, { modalDialogClass: 'modal-sm' });
  }

  cancelRequest() {
    if (this.alertModalRef) {
      this.alertModalRef.close();
    }
  }
  // Modals [End]

  // response Model Configurations
  handleContextMenu(event: MouseEvent) {
    event.preventDefault();
    const selection = window.getSelection();
    if (selection && selection.toString().trim().length > 0) {
      this.selectedText = selection.toString();

      const element = event.target as HTMLElement;
      const rect = element.getBoundingClientRect();

      this.contextMenuX = event.clientX;
      this.contextMenuY = event.clientY;

      const viewportWidth = window.innerWidth;
      const viewportHeight = window.innerHeight;
      const menuWidth = 200;
      const menuHeight = 160;

      if (this.contextMenuX + menuWidth > viewportWidth) {
        this.contextMenuX = viewportWidth - menuWidth;
      }

      if (this.contextMenuY + menuHeight > viewportHeight) {
        this.contextMenuY = viewportHeight - menuHeight;
      }
      this.showContextMenu = true;
    }
  }

  addTag(response: ProjectResponse) {
    if (this.selectedText && this.selectedText.trim()) {
      if (!response.tags) {
        response.tags = [];
      }
      if (!response.tags.includes(this.selectedText.trim())) {
        response.tags.push(this.selectedText.trim());
      }
      this.showContextMenu = false;
    }
  }

  addManualTag(response: ProjectResponse) {
    if (response.newTag && response.newTag.trim()) {
      if (!response.tags) {
        response.tags = [];
      }
      if (!response.tags.includes(response.newTag.trim())) {
        response.tags.push(response.newTag.trim());
      }
      response.newTag = '';
    }
  }

  removeTag(response: ProjectResponse, index: number) {
    if (response.tags) {
      response.tags.splice(index, 1);
    }
  }

  stripHtml(html: string): string {
    if (!html) return '';
    return html
      .replace(/<div>/gi, '\n')
      .replace(/<br\s*\/?>/gi, '\n')
      .replace(/<\/div>/gi, '')
      .replace(/<[^>]+>/g, '')
      .trim();
  }  

  closeContextMenu() {
    this.showContextMenu = false;
  }

  getAllProjectInsightFacetCategory(): Promise<any> {
    this.facetCategoryList = [];
    this.filteredCategories = [];
    return this.projectInsightFacetService.getAllProjectInsightFacetCategory().pipe(first())
      .toPromise()
      .then((response: any) => {
        this.facetCategoryList = response;
        this.filteredCategories = response;
      })
      .catch(error => {
        console.log(error, " : error");
        this.openAlertModal('An unexpected error occurred while fetching Project Insight Facet.');
      });
  }

  filterCategories() {
    if (!this.validationService.validateNullUndefinedEmptyString(this.filterText)) {
      return;
    }
    const filterValue = (this.filterText || '').toLowerCase();
    this.filteredCategories = this.facetCategoryList?.filter(option =>
      option.categoryName.toLowerCase().includes(filterValue) &&
      !this.question?.facetCategoryList?.some(
        selected => selected.categoryName.toLowerCase() === option.categoryName.toLowerCase()
      )
    );
  }

  addFacetCategoryToField(selectedValue?: string) {
    const value = (selectedValue || this.filterText || '').trim();
    if (!value) return;

    if (!this.question.facetCategoryList) {
      this.question.facetCategoryList = [];
    }

    const allCategoryList = this.facetCategoryList?.map(obj => obj.categoryName.toLowerCase()) || [];
    const categoryList = this.question.facetCategoryList.map(obj => obj.categoryName.toLowerCase());

    if (allCategoryList.includes(value.toLowerCase()) && !categoryList.includes(value.toLowerCase())) {
      const facet = this.facetCategoryList.find(
        obj => obj.categoryName.trim().toLowerCase() === value.toLowerCase()
      );
      if (facet) {
        this.question.facetCategoryList.push(facet);
      }
      this.resetFilter();
      return;
    }

    if (!categoryList.includes(value.toLowerCase())) {
      this.question.facetCategoryList.push({ categoryName: value } as ProjectInsightFacetCategory);
    }
    this.resetFilter();
  }

  private resetFilter() {
    this.filterText = null;
    this.filteredCategories = this.facetCategoryList?.filter(
      option =>
        !this.question.facetCategoryList.some(
          selected => selected.categoryName.toLowerCase() === option.categoryName.toLowerCase()
        )
    );
  }

  removeFacetCategoryFromField(index: number) {
    if (this.question.facetCategoryList) {
      let question = this.question?.facetCategoryList[index];
      if (question && question?.facetCategoryId != undefined && question?.facetCategoryId != null) {
        if (!this.question?.facetCategoryIds) {
          return;
        }
        this.question.facetCategoryIds = this.question?.facetCategoryIds?.filter(facetId => facetId !== question.facetCategoryId);
      }
      this.question.facetCategoryList.splice(index, 1);
    }
    this.filteredCategories = this.facetCategoryList?.filter(option =>
      !this.question?.facetCategoryList?.some(
        selected => selected.categoryName.toLowerCase() === option.categoryName.toLowerCase()
      )
    );
  }

  clearSearch() {
    this.filterText = null;
  }
}

