import { Component, EventEmitter, Input, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { Document } from 'src/app/models/document';
import { ProjectInsight } from 'src/app/models/projectInsight';
import { ProjectInsightEntity } from 'src/app/models/projectInsightEntity';
import { ProjectQuestion } from 'src/app/models/projectQuestion';
import { ProjectResponse } from 'src/app/models/projectResponse';
import { SurveyOption } from 'src/app/models/sureyOption';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { FormBuilderService } from 'src/app/services/form-builder.service';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ValidationService } from 'src/app/services/validation.service';




interface FormNode {
  id: string;
  formName: string;
  fields: any[];
  formData: any;
  layoutConfig?: any[];
  children: FormNode[];
  questionList?: ProjectQuestion[];
  fieldDependencies?: { [key: string]: string };
  dependentFieldsMap?: { [key: string]: string[] };
}

@Component({
  standalone: false,
  selector: 'app-question-renderer',
  templateUrl: './question-renderer.component.html',
  styleUrls: ['./question-renderer.component.css']
})
export class QuestionRendererComponent implements OnInit {

  @ViewChild('alert_message') alertMessageTemplate: TemplateRef<any>;

  @Input() entity: any;
  @Input() entityType: string;
  @Input() renderType: 'edit' | 'view' | 'answer' | 'approval' = 'edit';
  @Input() projectId: any;
  // @Output() responseChanged = new EventEmitter<{ path: string[], value: any }>();

  currentUser: User;
  alertMessage: any;

  bsModalRef:NgbModalRef;
  documentPreviewModalRef:NgbModalRef;
  modalRef:NgbModalRef;

  //Question section
  file: any;
  fileName: any;
  displayedResponseUserId: any[] = [];
  rolesGreaterThanManager: any[] = ['HOD', 'SuperAdmin', 'HR', 'RMG'];
  currentQuestionIndex: number | null = null;
  currentQuestionList: any[] | null = null;
  showContextMenu = false;
  contextMenuX = 0;
  contextMenuY = 0;
  selectedText = '';
  newTag: string = '';

  allEmployeeList: any[] = [];
  projectInsightObj: any = {}; // Set as needed
  isCurrentEmployeeRoleGreaterThanManager: boolean = false; // Set as needed
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
    ]
  };


  constructor( private validationService: ValidationService,
    private authenticationService: AuthenticationService,
    private modalService: NgbModal,
    private projectInsightService: ProjectInsightService,
    private formBuilderService: FormBuilderService,) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
   }

  ngOnInit(): void {
    this.getAllEmployeeList();
  }


  navigateToQuestion(questionList: ProjectQuestion[], questionIndex: number) {
    this.currentQuestionList = questionList;
    this.currentQuestionIndex = questionIndex;
  }

  showQuestionOverview(entity: FormNode) {
    this.currentQuestionList = entity.questionList;
    this.currentQuestionIndex = null;
  }

  addQuestion(entity: FormNode) {
    let question: ProjectQuestion = new ProjectQuestion();
    if (!entity.questionList) {
      entity.questionList = [];
    }
    entity.questionList.push(question);
    this.navigateToQuestion(entity.questionList, entity.questionList.length - 1);
  }

  deleteQuestion(questionList: ProjectQuestion[], questionIndex: any) {
    if (!this.projectInsightObj.deletedProjectInsightEntityList) {
      this.projectInsightObj.deletedProjectInsightEntityList = [];
    }
    questionList.forEach((question, index) => {
      if (index == questionIndex && this.validationService.validateNullUndefinedEmptyString(question.questionId)) {
        let projectInsightEntity = new ProjectInsightEntity();
        projectInsightEntity.entityId = question.questionId;
        projectInsightEntity.entityType = 'Question';
        this.projectInsightObj.deletedProjectInsightEntityList.push(projectInsightEntity);
      }
    });
    questionList?.splice(questionIndex, 1);
  }

  // Option Configurations
  addOption(i, questionObj: ProjectQuestion) {
    questionObj?.optionsList.splice(i + 1, 0, new SurveyOption());
  }

  removeOption(i, questionObj: ProjectQuestion) {
    questionObj?.optionsList.splice(i, 1);
  }

  setOption(questionObj: ProjectQuestion) {
    if (questionObj.optionType == "checkbox" || questionObj.optionType == "radio") {
      questionObj.optionsList = [];
      questionObj.optionsList.splice(1, 0, new SurveyOption());
    }
  }

  getAllEmployeeList(){
    this.formBuilderService.getAllEmployeeList().pipe(first()).subscribe({
      next: (response: any) => {
        this.allEmployeeList = response;
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.open(this.alertMessageTemplate);
      }
    });
  }
  
  checkIfUserIdIsPresentInDisplayResponseUserId(response: any): boolean {
    return this.displayedResponseUserId?.includes(response?.responseBy);
  }

  onRecommendedResponseChange(event: any, response: any, entity: any) {
    if (event.target.checked) {
      response.isRecommendedChecked = true;
    }
    entity?.projectResponseList.forEach((responseObj) => {
      if (responseObj.projectInsightResponseId != response.projectInsightResponseId) {
        responseObj.isRecommendedChecked = false;
      }
    });
    entity.recommendedResponseId = response?.projectInsightResponseId;
  }

  //context menu
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
  
  onCheckboxChangeForQuestionSection(event: any, value: string, question: any, option: any, response: any) {
    if (response.responseList == null || response.responseList == undefined) {
      response.responseList = [];
    }
    if (event.target.checked) {
      option.isChecked = true;
      response.responseList.push(value);
    } else {
      response.responseList = response.responseList.filter((item: string) => item != value);
      option.isChecked = false;
    }
  }

  onQuestionFileChange(event: any, question: any, alertTemplate: TemplateRef<any>, previewElementId: any, documentPreviewTemplate: TemplateRef<any>, response: any) {
    const file = event.target.files[0];
    if (file) {
      response.document = file;
      const inputId = event.target.id;
      var fileExtension = file.name.split('.').pop().toLowerCase();
      response.documentFileName = this.currentUser.empId + '-' + inputId + '.' + fileExtension;
    }

    const MAX_SIZE = 5 * 1024 * 1024;
    if (file) {
      if (file.size > MAX_SIZE) {
        this.alertMessage = "File size must be lesser than or equal to 1MB."
        this.openAlertMod(alertTemplate, this.alertMessage);
        response.document = null;
        response.documentFileName = null;
        return false;
      }
    }
    this.previewUploadedFile(question, response.document, response.documentFileName, previewElementId, documentPreviewTemplate, alertTemplate, false, response);
  }

  previewUploadedFile(question, uploadedFile: any, fileName: any, previewElementId: any, documentPreviewTemplate: TemplateRef<any>, alertTemplate: TemplateRef<any>, downloadFile: any, response: any) {
    if ((fileName != undefined && fileName != null)) {
      const MAX_SIZE = 5 * 1024 * 1024;
      const file = uploadedFile;

      if (uploadedFile != undefined && uploadedFile != null) {
        if (file.size > MAX_SIZE) {
          this.documentPreviewModalRef = this.modalService.open(documentPreviewTemplate, { modalDialogClass: 'modal-xl' });
          const previewContainer = document.getElementById(previewElementId);
          previewContainer.innerHTML = "<span class='mt-3' style='display:inline-block;'>File size must be lesser than or equal to 5MB. </span>";
          response.document = null;
          response.documentFileName = null;
          return false;
        }

        if (file && file.type === 'application/pdf') {
          this.documentPreviewModalRef = this.modalService.open(documentPreviewTemplate, { modalDialogClass: 'modal-xl' });
          const previewContainer = document.getElementById(previewElementId);
          const reader = new FileReader();
          reader.onload = function (e) {
            const pdfData = e.target.result;
            previewContainer.innerHTML = `<embed src="${pdfData}" type="application/pdf" width="100%" height="100%">`;
          };
          reader.readAsDataURL(file);
        }
        else if (file && file.type.startsWith('image/')) {
          this.documentPreviewModalRef = this.modalService.open(documentPreviewTemplate, { modalDialogClass: 'modal-xl' });
          const previewContainer = document.getElementById(previewElementId);
          var fileName2 = file.name;
          var fileExtension = fileName2.split('.').pop().toLowerCase();
          var allowedExtensions = ['jpg', 'jpeg', 'png', 'jpg2'];
          if (allowedExtensions.indexOf(fileExtension) === -1) {
            previewContainer.innerHTML = "<span class='mt-3' style='display:inline-block;'> Please select only image file (jpg, jpeg, png, jpg2) Or PDF </span>";
            response.document = null;
            response.documentFileName = null;
            return;
          }
          const reader = new FileReader();
          reader.onload = function (e) {
            const pdfData = e.target.result;
            previewContainer.innerHTML = `<img src="${pdfData}" class="img-fluid"  width="100%" height="100%">`;
          };
          reader.readAsDataURL(file);
        }
        else {
          if (downloadFile) {
            this.downloadFile(file, fileName);
            return;
          }
        }
      } else {
        this.getUserUploadedFileForQuestion(question, fileName, documentPreviewTemplate, previewElementId);
      }
    } else {
      this.documentPreviewModalRef = this.modalService.open(documentPreviewTemplate, { modalDialogClass: 'modal-xl' });
      const previewContainer = document.getElementById(previewElementId);
      response.document = null;
      response.documentFileName = null;
      previewContainer.innerHTML = `<h3 style='padding: 12px; display:inline-block;'>No Data Found.</h3>`;
    }
  }

  downloadFile(file: any, fileName: any) {
    const reader = new FileReader();
    reader.readAsArrayBuffer(file);
    reader.onload = () => {
      const fileBlob = new Blob([reader.result as ArrayBuffer], { type: file.type });
      if (fileBlob) {
        const url = window.URL.createObjectURL(fileBlob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
      }
    }
  }

  onResponseChange(response: any) {
    if (response.response) {
      let payload = {
        empId: this.currentUser.empId,
        projectId: this.projectId
      };

      this.projectInsightService.getReviewersForQuestion(payload).pipe(first()).subscribe(
        (dbResponse: any) => {
          if (!Array.isArray(response.reviewerInfo)) {
            response.reviewerInfo = [];
          }
          const exists = response.reviewerInfo.some(
            (r: any) => r.reviewerid === dbResponse?.serviceResponse?.reviewerid
          );
          if (!exists) {
            response.reviewerInfo.push({
              reviewerid: dbResponse?.serviceResponse?.reviewerid,
              reviewedOn: null,
              isApproved: null,
              marks: null,
              remarks: null,
              reviewAssignedOn: new Date()
            });
          }
          // this.responseChanged.emit({ path, value: response });
        },
        (error) => {
          console.error('Failed to fetch reviewers:', error);
        }
      );
    }
  }

  getUserUploadedFileForQuestion(question: any, fileName: any, documentPreviewTemplate: TemplateRef<any>, previewElementId: any) {
    let documentObj = new Document();
    documentObj.empId = this.currentUser.empId;
    documentObj.documentName = fileName;
    documentObj.typeId = question.entityId;
    documentObj.typeName = question.entityType;
    this.projectInsightService.getUserUploadedFileForQuestion(documentObj).subscribe((response: any) => {
      this.documentPreviewModalRef = this.modalService.open(documentPreviewTemplate, { modalDialogClass: 'modal-xl' });
      const previewContainer = document.getElementById(previewElementId);

      if (response.serviceStatus === 'Fail') {
        previewContainer.innerHTML = "<span class='mt-3' style='display:inline-block;' > File Not Found.</span>";
      } else {
        const base64Data = response?.serviceResponse?.body;
        let contentTypeList = response?.serviceResponse?.headers["Content-Type"];
        let contentType = contentTypeList[0]
        if (contentType == 'application/pdf') {
          previewContainer.innerHTML = `<embed src="data:application/pdf;base64,${base64Data}" type="application/pdf" width="100%" height="800px" />`;
        }
        else if (contentType.startsWith('image/')) {
          previewContainer.innerHTML = `<img src="data:${contentType};base64,${base64Data}" class="img-fluid" style="max-height:800px;" />`;
        }
        else {
          let file = this.base64ToBlob(base64Data, contentType, 512);
          this.downloadFile(file, fileName);
          previewContainer.innerHTML = "<span class='mt-3' style='display:inline-block;' > Can't Open File.</span>";
          this.closeDocumentPreviewTemplate();
        }
      }
    });
  }

  base64ToBlob(base64: string, contentType: string, sliceSize = 512): Blob {
    const byteCharacters = atob(base64); // decode base64
    const byteArrays = [];
    for (let offset = 0; offset < byteCharacters?.length; offset += sliceSize) {
      const slice = byteCharacters.slice(offset, offset + sliceSize);

      const byteNumbers = new Array(slice?.length);
      for (let i = 0; i < slice?.length; i++) {
        byteNumbers[i] = slice.charCodeAt(i);
      }
      const byteArray = new Uint8Array(byteNumbers);
      byteArrays.push(byteArray);
    }
    return new Blob(byteArrays, { type: contentType });
  }

  removeUploadedFile(response: any) {
    if (response?.document) {
      response.document = null;
    }
    if (response?.documentFileName) {
      response.documentFileName = null;
    }
  }


  // ------------------------------ Response Start -------------------------

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
        const emp = this.allEmployeeList.find((e: any) => e.empId === empId);
        const response = new ProjectResponse();
        response.responseBy = empId;
        response.responseByEmpName = emp ? emp.name : '';
        response.assignedOn = new Date();
        question.projectResponseList.push(response);
      }
    });
  }

  getVisibleResponses(question: any): any[] {
    const currentUserId = this.currentUser.empId;

    // Future : make changes for the reviewer logic
    const isReviewer = this.currentUser.employeeRole === 'Reviewer';
  
    if (isReviewer) {
      // Reviewer: show all responses if all are submitted (isDraft == "N")
      if (question.projectResponseList.every(r => r.isDraft === "N")) {
        return question.projectResponseList;
      } else {
        // Optionally, show nothing or a message
        return [];
      }
    } else {
      // Normal user
      const myResponse = question.projectResponseList.find(r => r.responseBy == currentUserId);
      if (!myResponse) {
        // User hasn't answered yet, show nothing or prompt
        return [];
      }
      if (myResponse.isDraft === "Y") {
        // User can only see/edit their own draft response
        return [myResponse];
      } else {
        // User has submitted, show all responses (read-only)
        return question.projectResponseList;
      }
    }
  }
  
  canEditResponse(response: any): boolean {
    const currentUserId = this.currentUser.empId;
    const isReviewer = this.currentUser.employeeRole === 'Reviewer'; // adjust as per your logic
    if (isReviewer) return false;
    return response.responseBy == currentUserId && response.isDraft === "Y";
  }

  //Models
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.alertMessage = message;
    this.bsModalRef = this.modalService.open(template, { modalDialogClass: 'modal-sm' });
  }

  closeDocumentPreviewTemplate() {
    this.documentPreviewModalRef.close();
  }

  cancelRequest() {
    this.modalRef.close();
  }
}
