import { Component, Input, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { ProjectResponse } from 'src/app/models/projectResponse';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectService } from 'src/app/services/project.service';
import { Editor, Toolbar } from 'ngx-editor';

@Component({
  standalone: false,
  selector: 'app-group-browser',
  templateUrl: './group-browser.component.html',
  styleUrls: ['./group-browser.component.css']
})
export class GroupBrowserComponent implements OnInit {
  @Input() projectId: string | null = null;
  @Input() project: any | null = null;
  @Input() groupId: string | null = null;
  @Input() groupName: string | null = null;

  @ViewChild('Open_Question_Overview') quesDetailView!: TemplateRef<any>;
  @ViewChild('alert_message') alertMessageTemplate!: TemplateRef<any>;
  @ViewChild('ask_confirmation') confirmation!: TemplateRef<any>;
  @ViewChild('ask_level_confirmation') levelConfirmation!: TemplateRef<any>;

  modalRef:NgbModalRef;

  alertMessage: string = '';
  alertMessage2: string = '';
  alertMessage3: string = '';

  currentUser: any;
  groups: any[] = [];
  questions: any[] = [];
  quesStatusMap: { [key: string]: number } = {};

  allChildsRecursiv: boolean = false;
  selectedGroup: any = null;
  expandedQuestionId: string | null = null;
  selectedQuestionDetails: any = null;
  groupStates: {
    [id: string]: {
      isOpen: boolean;
      loading?: boolean;
      groups?: any[];
      questions?: any[];
    }
  } = {};
  showGroupCard: any = null;
  selectedGroupId: string | null = null;
  showContextMenu = false;
  contextMenuX = 0;
  contextMenuY = 0;
  selectedText = '';
  newTag: string = '';


  //Text Editor
  editor: Editor;
  toolbar: Toolbar = [
    ['undo', 'redo'],
    ['bold', 'italic', 'underline', 'strike', 'superscript', 'subscript'],
    ['align_justify', 'align_left', 'align_center', 'align_right'],
    ['ordered_list', 'bullet_list'],
    ['indent', 'outdent'],
    [{ heading: ['h1', 'h2', 'h3'] }],
    ['text_color', 'background_color'],
    ['horizontal_rule'],
    ['format_clear'],
    ['code']
  ];
  constructor(
    private authenticationService: AuthenticationService,
    public projectService: ProjectService,
    private projectInsightService: ProjectInsightService,
    private modalService: NgbModal
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit() {
    this.editor = new Editor();
    if(this.projectId){
      this.loadGroups(this.projectId,'Project');
      this.loadQuestionsByGroupOrProjectId(this.projectId,'Project');
    }else{
     // this.loadGroups(this.groupId,'Group');
    }
  }

  ngOnDestroy(): void {
    this.editor.destroy();
  }

  loadGroups(parentId:any,parentType:any) {
    const payload = {
      parentId: parentId,
      parentType: parentType,
      empId:this.currentUser.empId
    };
    this.projectInsightService.getAllGroupsStatusInfo(payload).pipe(first()).subscribe({
      next: (response: any) => {
        this.groups = response;
        this.groups.forEach((item)=>{
          this.projectService.projectMap.set(item.projectId,item);
        });
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.open(this.alertMessageTemplate, { modalDialogClass: 'modal-sm' });
      }
    });
  }

  loadQuestionsByGroupOrProjectId(parentId:any,parentType:any){
    if(parentType == 'Project'){
      this.selectedGroup = null;
      this.showGroupCard = null;
      this.selectedGroupId = null;
    }
    let payload = {
      parentId:parentId,
      parentType:parentType,
      empId:this.currentUser.empId
    }
    this.projectInsightService.getAllQuestionsForUserByParentIdAndParentType(payload).pipe(first()).subscribe({
      next: (response: any) => {
        this.questions = response.questions;
        this.quesStatusMap = response.statusMap;
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.open(this.alertMessageTemplate, { modalDialogClass: 'modal-sm' });
      }
    });
  }

  sendForUpdate(QG: any, isQuestion: number) {
    let projectIds: any[] = [];
    let groupIds: any[] = [];

    if (isQuestion === 1 || isQuestion === 2) { // Question or Group
      projectIds = QG.parentPathIds.slice(0, 1);
      groupIds = QG.parentPathIds.slice(1);
      if (isQuestion === 2) { // Group
        groupIds.push(QG.id);
      }
    } else if (isQuestion === 3) { // Project
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

  refreshCountsByParentPath(payload: any) {
    this.projectInsightService.refreshByParentPath(payload).pipe(first()).subscribe({
      next: (response: any) => {
        Object.entries(response).forEach(([key, value]) => {
          this.projectService.projectMap.set(key, value);
        });
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.open(this.alertMessageTemplate, { modalDialogClass: 'modal-sm' });
      }
    });
  }

  backToGroupList() {
    this.selectedGroup = null;
    this.expandedQuestionId = null;
    this.selectedQuestionDetails = null;
  }

  getQuesAndResponse(question: any) {
    console.log('Question is : ',question);
      const payload = {
        parentId: question.id,
        empId: this.currentUser.empId
      };
      this.projectInsightService.getQuestionDetailsById(payload).subscribe((res: any) => {
        this.selectedQuestionDetails = res;
        console.log('Question and response details is : ',this.selectedQuestionDetails);
        this.modalRef = this.modalService.open(this.quesDetailView, { modalDialogClass: 'modal-lg' });
      });
  }

  saveDraft(response:any,question:any) {
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
          this.cancelRequest();
          this.sendForUpdate(question,1);
          this.alertMessage = res;
          this.modalRef = this.modalService.open(this.alertMessageTemplate);
        },
        error: (error: any) => {
          this.cancelRequest();
          this.alertMessage = error;
          this.modalRef = this.modalService.open(this.alertMessageTemplate, { modalDialogClass: 'modal-sm' });
        }
      });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'Approved': return 'text-success fw-bold';
      case 'Draft': return 'text-warning fw-bold';
      case 'Pending': return 'text-danger fw-bold';
      default: return '';
    }
  }
  getBackgroundColor(status: number): string {
    switch (status) {
      case 1: return '#ffe5e5';
      case 2: return '#f7ee79';
      case 3: return '#e6f0ff';
      default: return '#ffffff';
    }
  }

  sendQuestionsForApproval(group: any) {
    if (group?.totalCount === 0) {
      this.alertMessage = 'No Questions Present in this group';
      this.modalRef = this.modalService.open(this.alertMessageTemplate);
    } else if (group?.pendingCount > 0) {
      this.alertMessage2 = 'Some questions are not answered. Do you wish to submit only answered questions for review?';
      this.modalRef = this.modalService.open(this.confirmation);
    } else {
      this.askLevelApproval();
    }
  }

  askLevelApproval() {
    this.cancelRequest();
    this.alertMessage3 = 'Do you wish to submit only this group’s questions or include all child groups recursively?';
    this.modalRef = this.modalService.open(this.levelConfirmation);
  }

  saveConsent(consent: boolean) {
    this.cancelRequest();
    this.allChildsRecursiv = consent;
    this.sendAnsweredForApproval(this.selectedGroup);
  }

  sendAnsweredForApproval(group: any) {
    const request = {
      empId: this.currentUser.empId,
      parentType: 'Group',
      parentId: group.projectId,
      toAllChilds: this.allChildsRecursiv
    };
    // TODO: call API to submit answers
    console.log('Submit Request:', request);
    this.projectInsightService.assignQuestionsToReviewers(request).pipe(first()).subscribe({
      next: (res: any) => {
        this.cancelRequest();
        this.loadQuestionsByGroupOrProjectId(group.projectId,'Group');
        this.sendForUpdate(this.showGroupCard,2);
        this.alertMessage = res;
        this.modalRef = this.modalService.open(this.alertMessageTemplate);
      },
      error: (error: any) => {
        this.cancelRequest();
        this.alertMessage = error;
        this.modalRef = this.modalService.open(this.alertMessageTemplate, { modalDialogClass: 'modal-sm' });
      }
    });
  }

  toggleGroup(group: any, event: MouseEvent) {
    event.stopPropagation();
    this.getAllgroupstatusdata(group.projectId,'Group');
  }

  selectGroup(group: any) {
    this.selectedGroup = group;
    this.selectedGroupId = group.projectId;
    console.log('group on card is : ',this.selectedGroup);

    const state = this.groupStates[group.projectId] || { isOpen: false, groups: [], questions: [] };
    this.groupStates[group.projectId] = state;

    this.projectInsightService
      .getGroupStatusInfo(group.projectId, 'Group')
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          this.showGroupCard = response;
          this.groupStates[group.id] = { ...state };
          this.loadQuestionsByGroupOrProjectId(group.projectId,'Group');
        },
        error: (error: any) => {
          this.alertMessage = error;
          this.modalRef = this.modalService.open(this.alertMessageTemplate, { modalDialogClass: 'modal-sm' });
        }
      });
  }

  getGroupStatusInfo(group:any){
    console.log('payload : ',group);
    this.projectInsightService.getGroupStatusInfo(group.projectId,'Group').pipe(first()).subscribe({
        next: (response: any) => {
          console.log("show card for selected group data : ",response);
          this.selectedGroup = response;
        },
        error: (error: any) => {
          this.alertMessage = error;
          this.modalRef = this.modalService.open(this.alertMessageTemplate, { modalDialogClass: 'modal-sm' });
        }
      });
  }

  getAllgroupstatusdata(parentId:any,parentType:any){
    let payload = {
      parentId:parentId,
      parentType:parentType,
      empId:this.currentUser.empId
    }
    const state = this.groupStates[parentId] || { isOpen: false, groups: [], questions: [] };
    state.isOpen = !state.isOpen;
    this.groupStates[parentId] = state;

    // Load only if expanding for the first time
    if (state.isOpen && (!state.groups || state.groups.length === 0)) {
      state.loading = true;
      this.groupStates[parentId] = { ...state };

    this.projectInsightService.getAllGroupsStatusInfo(payload).pipe(first()).subscribe({
        next: (response: any) => {
          state.groups = response;
          response.forEach((item)=>{
            this.projectService.projectMap.set(item.projectId,item);
          });
            state.loading = false;
            this.groupStates[parentId] = { ...state };
        },
        error: (error: any) => {
          state.loading = false;
            this.groupStates[parentId] = { ...state };
            this.alertMessage = error;
            this.modalRef = this.modalService.open(this.alertMessageTemplate, { modalDialogClass: 'modal-sm' });
        }
      });
    }
  }


  cancelRequest() {
    this.modalRef?.close();
  }

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

  closeContextMenu() {
    this.showContextMenu = false;
  }
}
