import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { AngularEditorConfig } from '@kolkov/angular-editor';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first, take } from 'rxjs/operators';
import { FormRendererComponent } from 'src/app/helpers/form-renderer/form-renderer.component';
import { FieldPaletteItem, FieldPalette } from 'src/app/models/fieldPaletteItem';
import { FormField } from 'src/app/models/formField';
import { FormNode } from 'src/app/models/formNode';
import { ProjectInsightQuestionDetails } from 'src/app/models/projectInsightQuestionDetails';
import { ProjectResponse } from 'src/app/models/projectResponse';
import { SurveyOption } from 'src/app/models/sureyOption';
import { User } from 'src/app/models/user';
import { ApiSourceService } from 'src/app/services/api-source.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { FormBuilderService } from 'src/app/services/form-builder.service';
import { ProjectInsightProjconfigService } from 'src/app/services/project-insight-projconfig.service';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ProjectInsightDomainServiceService } from 'src/app/services/ProjectInsightDomainService.service';
import { ValidationService } from 'src/app/services/validation.service';
import { Domain, SubDomain, SubService } from '../Type';
import { Sort } from '@angular/material/sort';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import { ProjectInsightFormDetails } from 'src/app/models/projectInsightFormDetails';
import { ProjectInsightDetailsDTO } from 'src/app/models/projectInsightDetailsDTO';
import { ProjectInsightGroupDetails } from 'src/app/models/projectInsightGroupDetails';
import { ProjectService } from 'src/app/services/project.service';

@Component({
  selector: 'app-project-insight',
  templateUrl: './project-insight.component.html',
  styleUrls: ['./project-insight.component.scss', './project-insight.component.css']
})

export class ProjectInsightComponent implements OnInit {

  //Text Editor
  editorConfig: AngularEditorConfig = {
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

  fieldPalette: FieldPaletteItem[] = FieldPalette.getAll();

  @ViewChild(FormRendererComponent) formRenderer!: FormRendererComponent;
  @ViewChild('alert_message') alertMessageTemplate: TemplateRef<any>;
  @ViewChild('delete_template') deleteTemplate: TemplateRef<any>;
  @ViewChild('add_field_modal') addFieldModal: TemplateRef<any>;
  @ViewChild('open_create_project_modal') openCreateProjectModal: TemplateRef<any>;
  @ViewChild('add_or_update_question_modal') addOrUpdateQuestionModal: TemplateRef<any>;
  @ViewChild('add_new_group_modal') addNewGroupModal: TemplateRef<any>;
  @ViewChild('clone_modal') cloneModal: TemplateRef<any>;


  modalRef: BsModalRef = new BsModalRef();
  alertModalRef: BsModalRef = new BsModalRef();
  bsModalRef: BsModalRef = new BsModalRef();
  documentPreviewModalRef: BsModalRef = new BsModalRef();
  addOrUpdateQuestionModalRef: BsModalRef = new BsModalRef();
  addGroupDetailsModalRef: BsModalRef = new BsModalRef();
  deleteQuestionModalRef: BsModalRef = new BsModalRef();

  //Add new Field to Form
  addFieldModalRef: BsModalRef;
  newFieldType: any = null;
  newFieldConfig: any = {};
  addFieldTargetNode: FormNode;
  showFieldConfig = false;
  editingField: FormField | null = null;
  editingIndex: number = -1;
  editingFieldIndex: number = -1;
  originalFieldData: any = null;
  showExistingFieldsList: boolean = false;
  searchKeyword: any;
  currentVersion: any;
  selectedDomainId: number | null = null;

  //List
  apiList: any[] = [];
  allDeptList: any[] = [];
  allDepartmentWiseFormList: any[] = [];
  allProjectInsightProjectList: any[] = [];
  allDomainDataList: any[] = [];
  allDomainList: string[] = []
  allEmployeeList: any[] = [];
  currentQuestionList: ProjectInsightQuestionDetails[] | [];
  rolesGreaterThanManager: any[] = ['HOD', 'SuperAdmin', 'HR', 'RMG'];
  allProjectList: any[] = [];
  selectedDeptList: any[] = [];
  allDomains : any[] = [];
  selectedProject: any;
  selectedDepartments: number[] = [];

  //columnList
  projectColumns: any[] = ['blank', '', '', '', '', ''];

  //boolean
  isSelected: boolean;
  isEdit: boolean = true;
  isCreateForm: boolean = false;
  isCurrentEmployeeRoleGreaterThanManager: boolean;
  isDomainStructure: boolean = false;
  isTable: boolean = false;
  isQuestionUpdate: boolean = false;
  isSearchEnabled: boolean = false;
  isVisible: boolean = false;
  isCurrentNodeGroup: boolean = false;
  createDomainModal: boolean = false;

  //Utility
  page = 1;
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  abbreviationError: string = '';
  filters: any = {};
  limit = 10;

  questionRenderType: 'edit' | 'view' | 'answer' | 'approval' = 'edit';

  currentUser: User;
  currentNode: FormNode;
  rootNode: FormNode;
  question: ProjectInsightQuestionDetails = new ProjectInsightQuestionDetails();
  deletedQuestion: ProjectInsightQuestionDetails = new ProjectInsightQuestionDetails();
  projectInsightGroupDetails: ProjectInsightGroupDetails = new ProjectInsightGroupDetails();

  currentNodePath: FormNode[] = [];
  expandedPaths: { [key: string]: boolean } = {};

  alertMessage: any;
  deleteProjectInsightId: any;
  projectInsightId: any;
  selectedDepartment: any;
  selectedFormId: any;
  selectedFormType: any;
  parentId: any
  parentType: any;
  groupId: any;
  groupName: any;
  groupType: any;
  hierarchyType?: string;
  parentDynamicId?: string;
  cloneProjectInsightId: any;
  filterModal: boolean = false;
  existingFilter = {}

  title = "";
  addedDomainId = -1;
  currentItem: any = null;
  parentItem: string = null;
  allDomainWithProject: any = {};
  domainColors: any = {};
  selectedDomain: string = null;
  alreadySelected: boolean = false;
  childType: string = '';
  childrenSubDomain: number = null;
  modalTitleMap = {
    'domain': 'Add Sub-Domain',
    'subDomain': 'Add Sub-Domain',
    'service': 'Add Service',
    'subService': 'Add Sub-Service'
  };


  constructor(
    private formBuilder: FormBuilder,
    private departmentService: DepartmentService,
    private modalService: BsModalService,
    private formBuilderService: FormBuilderService,
    private validationService: ValidationService,
    private authenticationService: AuthenticationService,
    private projectInsightService: ProjectInsightService,
    private projectService: ProjectService,
    private apiSourceService: ApiSourceService,
    private projectInsightDomainService: ProjectInsightDomainServiceService,
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    this.isCurrentEmployeeRoleGreaterThanManager = this.rolesGreaterThanManager.includes(this.currentUser?.employeeRole);
    this.showTable();
    this.getAllApiSourceList();
    this.getAllEmployeeList();
    // this.loadInitialOptions();
    this.getAllProjectWithDomain();
  }

  showTable() {
    this.isTable = true;
    this.isCreateForm = false;
    this.currentQuestionList = [];
    this.getAllProjectInsightProjectList();
  }

  getAllEmployeeList() {
    this.formBuilderService.getAllEmployeeList().pipe(first()).subscribe({
      next: (response: any) => {
        this.allEmployeeList = response;
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      }
    });
  }

  // Project Insight APIs & Methods [Start]

  getAllProjectInsightProjectList(domain?: string | number, unique_name?: string) {
    this.projectInsightService.getAllProjectInsight(domain, unique_name).pipe(first()).subscribe({
      next: (response: any) => {
        this.allProjectInsightProjectList = response;
      },
      error: (error: any) => {
        this.alertMessage =
          error?.error?.serviceResponse || 'An unexpected error occurred.';
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      }
    });
  }

  openViewProjectInsight(projectInsightId: any) {
    this.cancelRequest();
    this.isEdit = false;
    this.isCreateForm = true;
    this.isTable = false;
    this.projectInsightId = projectInsightId;
    this.getProjectInsightDetailsByObjectId(projectInsightId);
  }

  openEditProjectInsight(projectInsightId: any) {
    this.cancelRequest();
    this.isEdit = true;
    this.isCreateForm = true;
    this.isTable = false;
    this.projectInsightId = projectInsightId;
    this.getProjectInsightDetailsByObjectId(projectInsightId);
  }

  openDeleteProjectInsight(projectInsightId: any) {
    this.deleteProjectInsightId = projectInsightId;
    this.modalRef = this.modalService.show(this.deleteTemplate, { class: 'modal-sm' });
  }

  getProjectInsightDetailsByObjectId(projectInsightId: any) {
    this.projectInsightService.getProjectInsightDetailsByObjectId(projectInsightId).pipe(first()).subscribe({
      next: (response: any) => {
        this.rootNode = this.transformFormDetailsToFormNode(response?.projectInsightFormDetails);
        this.currentNode = this.transformFormDetailsToFormNode(response?.projectInsightFormDetails);
        this.mergeFormDataIntoFormStructure(this.currentNode, response?.projectInsightProjectDetails);
        this.currentNodePath = [this.currentNode];
        this.getProjectInsightQuestionDetailsByParentIdAndParentType(this.currentNode?.parentId, this.currentNode?.parentType);
        this.getProjectInsightGroupDetailsByParentIdAndParentType(this.currentNode?.parentId, this.currentNode?.parentType);
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      }
    });
  }

  mergeFormDataIntoFormStructure(structure: any, data: any) {
    if (structure.fields && Array.isArray(structure.fields) && data?.additionalInfo) {
      structure.fields.forEach(field => {
        field.value = data.additionalInfo[field.name];
      });
      structure.formData = data.additionalInfo;
    }
    if (data?.questions) {
      structure.questionList = data.questions;
    }
    if (structure?.children && data?.child) {
      for (let i = 0; i < structure.children.length; i++) {
        this.mergeFormDataIntoFormStructure(structure.children[i], data.child[i]);
      }
    }
  }

  saveProjectInsightDetails(isDraft: any) {
    this.cancelRequest();

    let projectInsightDetailsDTO: ProjectInsightDetailsDTO = new ProjectInsightDetailsDTO();
    projectInsightDetailsDTO.projectInsightFormDetails = this.transformFormNodeToFormDetails(this.rootNode);
    projectInsightDetailsDTO.projectInsightGroupDetails = null;
    projectInsightDetailsDTO.projectInsightQuestionDetails = null;
    projectInsightDetailsDTO.projectInsightProjectDetails.isDraft = isDraft;
    projectInsightDetailsDTO.projectInsightProjectDetails.id = this.projectInsightId;
    projectInsightDetailsDTO.projectInsightProjectDetails.createdBy = this.currentUser.empId;
    projectInsightDetailsDTO.projectInsightProjectDetails.projectId = this.getProjectId(this.rootNode.formData);
    projectInsightDetailsDTO.projectInsightProjectDetails.additionalInfo = this.rootNode.formData;
    let fields = JSON.parse(JSON.stringify(projectInsightDetailsDTO.projectInsightFormDetails.fields));
    projectInsightDetailsDTO.projectInsightFormDetails.fields = this.resetOptionsForOptionTypeAPI(fields);

    this.projectInsightService.saveProjectInsightDetails(projectInsightDetailsDTO).pipe(take(1)).subscribe(
      (response: any) => {
        this.alertMessage = response.serviceStatus;
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      },
      (error) => {
        console.log(error, " : error");
        this.alertMessage = error?.error?.serviceResponse || 'An unexpected error occurred.';
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      }
    );
  }

  resetOptionsForOptionTypeAPI(fields: any) {
    if (fields) {
      fields.forEach(row => {
        if (row.optionSource?.toLowerCase() === 'api') {
          row.options = [];
        }
      });
    }
    return fields;
  }

  getProjectId(data: any) {
    const projectFieldKey = Object.keys(data || {}).find(
      key => key.toLowerCase().includes('projectname')
    );
    const projectId = projectFieldKey ? data[projectFieldKey] : null;
    if (!projectId) {
      this.alertMessage = "Project Name/ Field is required to save as draft.";
      this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      return;
    }
    return projectId;
  }

  transformFormNodeToFormDetails(formNode: FormNode): any {
    const formDetails: ProjectInsightFormDetails = {
      id: formNode.id,
      formName: formNode.formName,
      parentId: formNode.parentId,
      parentType: formNode.parentType,
      fields: formNode.fields
    };
    return formDetails;
  }

  transformFormDetailsToFormNode(formDetails: any): FormNode {
    const node: FormNode = {
      id: formDetails.id,
      formName: formDetails.formName,
      parentId: formDetails.parentId,
      parentType: formDetails.parentType,
      fields: formDetails.fields || [],
      formData: {},
      layoutConfig: this.getLayoutConfig(formDetails.fields || [])
    };
    return node;
  }

  deleteProjectInsightById() {
    this.cancelRequest();
    this.projectInsightService.deleteProjectInsightById(this.deleteProjectInsightId).pipe(first()).subscribe({
      next: (response: any) => {
        this.alertMessage = response.serviceMessage;
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
        this.getAllProjectInsightProjectList();
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      }
    });
  }

  getProjectInsightGroupDetailsByObjectId(projectInsightId: any) {
    this.projectInsightService.getProjectInsightGroupDetailsByObjectId(projectInsightId).pipe(first()).subscribe({
      next: (response: any) => {
        this.currentNode = this.transformFormDetailsToFormNode(response?.projectInsightFormDetails);
        this.mergeFormDataIntoFormStructure(this.currentNode, response?.projectInsightGroupDetails);
        this.currentNodePath = [this.currentNode];
        this.getProjectInsightQuestionDetailsByParentIdAndParentType(this.currentNode?.parentId, this.currentNode?.parentType);
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      }
    });
  }

  getProjectInsightGroupDetailsByParentIdAndParentType(parentId: any, parentType: any) {
    let temp = [];
    this.projectInsightService.getProjectInsightGroupDetailsByParentIdAndParentType(parentId, parentType).pipe(first()).subscribe({
      next: (response: any) => {
        temp = response.serviceResponse;
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      }
    });
  }

  saveProjectInsightStaticGroupDetails(isDraft: any) {
    this.cancelRequest();
    let inputValidated: boolean = this.validateGroupDetails();
    if (!inputValidated) return;

    this.projectInsightGroupDetails.isDraft = isDraft;
    this.projectInsightGroupDetails.createdBy = this.currentUser.empId;
    this.projectInsightGroupDetails.parentId = this.currentNode.parentId;
    this.projectInsightGroupDetails.parentType = this.currentNode.parentType;

    this.projectInsightService.saveProjectInsightStaticGroupDetails(this.projectInsightGroupDetails).pipe(first()).subscribe(
      (response: any) => {
        this.alertMessage = response.serviceStatus;
        this.closeAddGroupDetailsModal();
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });


      },
      (error) => {
        console.log(error, " : error");
        this.alertMessage = error?.serviceStatus || 'An unexpected error occurred.';
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      }
    );
  }

  saveProjectInsightGroupDetails(isDraft: any) {
    this.cancelRequest();
    if (isDraft && isDraft === 'Y') {
      // validations 

    }
    let projectInsightDetailsDTO: ProjectInsightDetailsDTO = new ProjectInsightDetailsDTO();
    projectInsightDetailsDTO.projectInsightFormDetails = this.transformFormNodeToFormDetails(this.currentNode);
    projectInsightDetailsDTO.projectInsightProjectDetails = null;
    projectInsightDetailsDTO.projectInsightQuestionDetails = null;
    projectInsightDetailsDTO.projectInsightGroupDetails.id = this.projectInsightId;
    projectInsightDetailsDTO.projectInsightGroupDetails.createdBy = this.currentUser.empId;
    projectInsightDetailsDTO.projectInsightGroupDetails.additionalInfo = this.currentNode.formData;
    projectInsightDetailsDTO.projectInsightGroupDetails.isDraft = isDraft;
    projectInsightDetailsDTO.projectInsightGroupDetails.parentId = this.currentNode.parentId;
    projectInsightDetailsDTO.projectInsightGroupDetails.parentType = this.currentNode.parentType;
    let fields = JSON.parse(JSON.stringify(projectInsightDetailsDTO.projectInsightFormDetails.fields));
    projectInsightDetailsDTO.projectInsightFormDetails.fields = this.resetOptionsForOptionTypeAPI(fields);

    this.projectInsightService.saveProjectInsightGroupDetails(projectInsightDetailsDTO).pipe(take(1)).subscribe(
      (response: any) => {
        this.alertMessage = response.serviceStatus;
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      },
      (error) => {
        console.log(error, " : error");
        this.alertMessage = error?.error?.serviceResponse || 'An unexpected error occurred.';
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      }
    );
  }

  getNodeDisplayName(group) {

  }

  openAddGroupDetailsModal() {
    this.projectInsightGroupDetails = new ProjectInsightGroupDetails();
    this.projectInsightGroupDetails.parentId = this.currentNode.parentId;
    this.projectInsightGroupDetails.parentType = this.currentNode.parentType;
    this.addGroupDetailsModalRef = this.modalService.show(this.addNewGroupModal, { class: 'modal-lg modal-dialog-centered' });
  }

  addProjectInsightGroup() {
    this.currentQuestionList = [];
    let newGroup: FormNode;
    newGroup = this.getDefaultGroupStructure();
    newGroup.parentId = this.currentNode.parentId;
    newGroup.parentType = this.currentNode.parentType;
    this.currentNode = newGroup;
    this.currentNodePath.push(newGroup);
  }
  closeAddGroupDetailsModal() {
    if (this.addGroupDetailsModalRef) {
      this.addGroupDetailsModalRef.hide();
    }
  }
  // Project Insight APIs & Methods [End]

  // Add Field to Form [Start]

  generateId() {
    return Math.random().toString(36).substr(2, 9);
  }

  generateUniqueId(): string {
    return Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }

  getFieldIcon(type: string): string {
    const icons: { [key: string]: string } = {
      text: 'fa fa-font',
      textarea: 'fa fa-align-left',
      select: 'fa fa-caret-square-down',
      checkbox: 'fa fa-check-square',
      radio: 'fa fa-dot-circle',
      date: 'fa fa-calendar',
      number: 'fa fa-hashtag',
      email: 'fa fa-envelope',
      file: 'fa fa-file'
    };
    return icons[type] || 'fa fa-question';
  }

  showExistingFields() {
    this.showExistingFieldsList = true;
    this.newFieldType = null;
    this.editingField = null;
    this.editingFieldIndex = -1;
  }

  addNewField(node: FormNode) {
    this.addFieldTargetNode = node;
    this.editingField = {
      id: this.generateUniqueId(),
      type: 'text',
      label: '',
      name: '',
      required: false,
      placeholder: '',
      defaultValue: '',
      options: [],
      optionSource: 'static',
      width: 100,
      rowPosition: 0,
      multiple: false
    };

    this.editingIndex = -1;
    this.showFieldConfig = true;
    this.showFieldTypePalette();
  }

  showFieldTypePalette() {
    this.addFieldModalRef = this.modalService.show(this.addFieldModal);
  }

  editExsitingFields() {
    this.showExistingFieldsList = true;
    this.newFieldType = null;
    this.editingField = null;
    this.editingFieldIndex = -1;
  }

  editExistingField(field: any, index: number) {
    this.editingField = JSON.parse(JSON.stringify(field));
    this.editingFieldIndex = index;
    this.originalFieldData = JSON.parse(JSON.stringify(field));
    this.newFieldType = null;
    this.showExistingFieldsList = false;

    if (['select', 'checkbox', 'radio'].includes(this.editingField.type) && !this.editingField.options) {
      this.editingField.options = [];
    }

    if (this.editingField.type === 'table' && !this.editingField.tableConfig) {
      this.editingField.tableConfig = {
        columns: [{ name: 'col1', label: 'Column 1', type: 'text' }],
        rows: 1
      };
    }
  }

  saveEditedField() {
    if (!this.editingField || this.editingFieldIndex === -1) return;
    if (!this.editingField.label || !this.editingField.name) {
      alert('Label and Name are required fields');
      return;
    }
    this.currentNode.fields[this.editingFieldIndex] = { ...this.editingField };
    this.currentNode.layoutConfig = this.getLayoutConfig(this.currentNode.fields);
    this.editingField = null;
    this.editingFieldIndex = -1;
    this.originalFieldData = null;
    this.showExistingFieldsList = false;
    this.newFieldType = null;
    this.closeAddFieldModal();
  }

  deleteField() {
    if (this.editingFieldIndex === -1) return;
    this.currentNode.fields.splice(this.editingFieldIndex, 1);
    this.currentNode.layoutConfig = this.getLayoutConfig(this.currentNode.fields);
    this.currentNode.fields = [...this.currentNode.fields];
    this.currentNode.layoutConfig = [...this.currentNode.layoutConfig];
    this.editingField = null;
    this.editingFieldIndex = -1;
    this.originalFieldData = null;
    this.showExistingFieldsList = false;
    this.newFieldType = null;
    this.closeAddFieldModal();
  }

  deleteFieldFromList(index: number) {
    if (index == null || index < 0) return;
    this.currentNode.fields.splice(index, 1);
    this.currentNode.layoutConfig = this.getLayoutConfig(this.currentNode.fields);
    this.currentNode.fields = [...this.currentNode.fields];
    this.currentNode.layoutConfig = [...this.currentNode.layoutConfig];
  }

  startFieldConfig(type: any) {
    this.editingField = {
      id: this.generateUniqueId(),
      type: type.type,
      label: '',
      name: '',
      required: false,
      placeholder: '',
      defaultValue: '',
      options: ['select', 'checkbox', 'radio'].includes(type.type) ? [] : undefined,
      optionSource: 'static',
      width: 100,
      rowPosition: 0,
      multiple: false
    };
    this.showFieldConfig = true;
  }

  saveFieldConfig() {
    if (!this.editingField) return;
    this.editingField.width = Number(this.editingField.width);
    if (['select', 'checkbox', 'radio'].includes(this.editingField.type) && this.editingField.optionSource === 'static') {
      if (typeof this.editingField.options === 'string') {
        this.editingField.options = this.editingField.options.split(',').map((opt: string, i: number) => ({
          label: opt.trim(),
          value: opt.trim() || i
        }));
      }
    }
    if (this.editingField.type === 'table' && this.editingField.tableConfig) {
      this.editingField.tableConfig.rows = Number(this.editingField.tableConfig.rows) || 1;
    }

    // Add the field to the current node
    this.addFieldTargetNode.fields.push(this.editingField);
    this.addFieldTargetNode.layoutConfig = this.getLayoutConfig(this.addFieldTargetNode.fields);
    this.editingField = null;
    this.showFieldConfig = false;
  }

  removeOptionFormBuilder(i: number) {
    if (this.editingField && this.editingField.options) {
      this.editingField.options.splice(i, 1);
    }
  }

  addOptionFormBuilder() {
    if (this.editingField) {
      if (!this.editingField.options) this.editingField.options = [];
      this.editingField.options.push({ label: '', value: '' });
    }
  }

  onOptionSourceChange() {
    if (this.editingField) {
      if (this.editingField.optionSource === 'static') {
        if (!this.editingField.options) {
          this.editingField.options = [];
        }
      } else if (this.editingField.optionSource === 'api') {
        this.editingField.options = [];
      } else if (this.editingField.optionSource === 'dependent') {
        this.editingField.parentField = '';
        this.editingField.dependentApiUrl = '';
        this.editingField.dependentLabelKey = 'name';
        this.editingField.dependentValueKey = 'id';
        this.editingField.dependentParamName = '';
        this.editingField.options = [];
      }
    }
  }

  onApiSelect(event: any) {
    if (this.editingField && event.target.value) {
      const selectedApi = this.apiList.find(api => api.url === event.target.value);
      if (selectedApi) {
        this.editingField.apiUrl = selectedApi.url;
        this.editingField.apiLabelKey = selectedApi.labelKey;
        this.editingField.apiValueKey = selectedApi.valueKey;
        this.loadApiOptionsFormBuilder();
      }
    }
  }

  getNonDependentApis() {
    return this.apiList.filter(api => api.isdependent === 'N');
  }

  getDependentApis() {
    return this.apiList.filter(api => api.isdependent === 'Y');
  }

  onDependentApiSelect(event: any) {
    if (this.editingField && event.target.value) {
      const selectedApi = this.apiList.find(api => api.url === event.target.value);
      if (selectedApi) {
        const parentFieldName = this.editingField.parentField;
        const parentField = this.currentNode.fields.find(f => f.name === parentFieldName);
        const parentValueKey = parentField?.apiValueKey || parentFieldName;
        this.editingField.dependentApiUrl = `${selectedApi.url}/{${parentValueKey}}`;
        this.editingField.dependentLabelKey = selectedApi.labelKey;
        this.editingField.dependentValueKey = selectedApi.valueKey;
        this.editingField.dependentParamName = parentValueKey;
      }
    }
  }

  closeAddFieldModal() {
    if (this.addFieldModalRef) {
      this.addFieldModalRef.hide();
    }
    this.editingField = null;
    this.editingFieldIndex = -1;
    this.originalFieldData = null;
    this.showExistingFieldsList = false;
    this.newFieldType = null;
  }

  addFieldFromPalette(field: any) {
    const newField: FormField = {
      id: this.generateId(),
      type: field.type,
      label: field.label,
      name: '',
      required: false,
      placeholder: '',
      defaultValue: '',
      options: field.type === 'select' || field.type === 'checkbox' || field.type === 'radio' ? [] : undefined,
      optionSource: 'static',
      width: 100,
      rowPosition: 0,
      multiple: false,
      ...(field.type === 'table' ? {
        tableConfig: {
          columns: [
            { name: 'col1', label: 'Column 1', type: 'text' },
            { name: 'col2', label: 'Column 2', type: 'text' }
          ],
          rows: 2
        }
      } : {})
    };

    this.editingField = newField;
    this.editingIndex = -1;
    this.showFieldConfig = true;
    this.newFieldType = field;
    this.editingFieldIndex = -1;
    this.showExistingFieldsList = false;
  }

  backToFieldPalette() {
    this.showExistingFieldsList = false;
    this.newFieldType = null;
    this.editingField = null;
    this.editingFieldIndex = -1;
  }

  addFieldToCurrentNode() {
    if (!this.editingField) return;
    this.editingField.width = Number(this.editingField.width);
    if (['select', 'checkbox', 'radio'].includes(this.editingField.type) && this.editingField.optionSource === 'static') {
      if (this.editingField.options && typeof this.editingField.options === 'string') {
        this.editingField.options = this.editingField.options.split(',').map((opt: string, i: number) => ({
          label: opt.trim(),
          value: opt.trim() || i
        }));
      }
    }

    this.currentNode.fields.push(this.editingField);
    this.currentNode.layoutConfig = this.getLayoutConfig(this.currentNode.fields);
    this.currentNode.fields = [...this.currentNode.fields];
    this.editingField = null;
    this.editingFieldIndex = -1;
    this.showExistingFieldsList = false;
    this.newFieldType = null;
    this.closeAddFieldModal();
  }

  getAvailableParentFields(currentField: any): any[] {
    if (!this.currentNode || !this.currentNode.fields) return [];
    return this.currentNode.fields.filter(field =>
      field.name !== currentField.name &&
      ['select', 'radio'].includes(field.type) &&
      field.optionSource !== 'dependent'
    );
  }

  onParentFieldChange() {
    if (this.editingField && this.editingField.parentField) {
      const parentField = this.currentNode.fields.find(f => f.name === this.editingField.parentField);
      const parentValueKey = parentField?.apiValueKey || parentField?.name;
      if (parentField) {
        if (!this.editingField.dependentParamName) {
          this.editingField.dependentParamName = parentValueKey;
        }

        if (this.editingField.dependentApiUrl) {
          const baseUrl = this.editingField.dependentApiUrl.split('/{')[0];
          this.editingField.dependentApiUrl = `${baseUrl}/{${parentValueKey}}`;
        }

        if (!this.currentNode.fieldDependencies) {
          this.currentNode.fieldDependencies = {};
        }
        if (!this.currentNode.dependentFieldsMap) {
          this.currentNode.dependentFieldsMap = {};
        }

        this.currentNode.fieldDependencies[this.editingField.name] = this.editingField.parentField;
        if (!this.currentNode.dependentFieldsMap[this.editingField.parentField]) {
          this.currentNode.dependentFieldsMap[this.editingField.parentField] = [];
        }
        this.currentNode.dependentFieldsMap[this.editingField.parentField].push(this.editingField.name);
      }
    }
  }
  // Add Field to Form [End]

  // Dynamic API Option fetch [Start]
  getAllApiSourceList() {
    this.apiSourceService.getAllApiSourceList().pipe(first()).subscribe({
      next: (response: any) => {
        this.apiList = response;
        // setTimeout(async () => {
        //   await this.loadInitialOptions();
        // }, 200);
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      }
    });
  }

  loadApiOptionsFormBuilder() {
    if (this.editingField && this.editingField.apiUrl) {
      this.apiSourceService.loadDynamicApi(this.editingField.apiUrl).subscribe({
        next: (data: any) => {
          this.editingField!.options = this.mapApiOptions(data, this.editingField!.apiLabelKey!, this.editingField!.apiValueKey!);
        },
        error: (error) => {
          console.error('Error loading API options:', error);
        }
      });
    }
  }

  mapApiOptions(data: any[], labelKey: string, valueKey: string): any[] {
    const getValue = (obj: any, path: string) =>
      path.split('.').reduce((acc, part) => acc && acc[part], obj);
    return data.map(item => ({
      label: getValue(item, labelKey),
      value: getValue(item, valueKey)
    }));
  }
  // Dynamic API Option fetch [End]

  // Department Fetch [Start]

  getAllDynamicFormByDepartmentAndType() {
    this.selectedFormId = null;
    let formObject = { allDepartmentIds: this.selectedDepartments }
    this.formBuilderService.getAllDynamicFormByDepartmentAndType(formObject).pipe(first()).subscribe({
      next: (response: any) => {
        this.allDepartmentWiseFormList = response;
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      }
    });
  }

  getAllProjects(): void {
    this.allProjectList = [];
    this.projectService.getAllProjectsList().pipe(first()).subscribe(
      (response: any) => {
        console.log("All Projects List API success.");
        this.allProjectList  = response;
      },
      (error) => {
        console.error('Error in fetching all projects: ', error);
      }
    );
  }
  
  getAllDepartmentList() {
    this.allDeptList = [];
    this.selectedDepartments = [];
    this.selectedDeptList = [];
    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getProjectName(projectId: number): string { 
    const project = this.allProjectList.find(p => p.projectId == projectId);
    return project ? project.projectName : '';
  }
  
  getDepartmentName(deptId: number): string {
    const dept = this.allDeptList.find(d => d.deptId == deptId);
    return dept ? dept.name : '';
  }  

  getAllDynamicDepartmentsByProject() {
    let departmentObject = {
      projectId: this.selectedProject
    }
    this.selectedDeptList = [];
    this.departmentService.getAllDepartmentsByProjectId(departmentObject).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.selectedDeptList = response.serviceResponse;
        // this.selectedDeptList.forEach(dept => {
        //   this.selectedDepartments.push(dept.deptId);
        // });
        this.selectedDepartments = this.selectedDeptList.map(dept => dept.deptId);
        this.getAllDynamicFormByDepartmentAndType();
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllDomainsList(){
    this.projectInsightDomainService.getAllDomainList().subscribe({
      next: (res: any[]) => {
        this.allDomains = res;
        console.log("All Domains : ",res);
      }, error: (error: any) => {
        console.error("Error Getting All Domains List : ",error);
        throw error;
      }
    });
  }


  // Department Fetch [End]

  // Domain [Start]

  toggle(item: any) {
    item.isOpen = !item.isOpen;
  }

  toggleDomainProjectView(event: any) { }

  toggleByPath(path: number[]) {
    const key = path.join('-');
    this.expandedPaths[key] = !this.expandedPaths[key];
  }

  isExpandedByPath(path: number[]) {
    const key = path.join('-');
    return !!this.expandedPaths[key];
  }

  openCreateDomainModal() {
    this.createDomainModal = true;
  }

  closeCreateDomainModal() {
    this.createDomainModal = false;
  }

  openModal(item: any, type: string, parent: string) {
    this.currentItem = item;
    this.childType = type;
    this.parentItem = parent;
    this.isVisible = true;
    this.title = "Add new Item in " + item.name;
  }

  closeModal() {
    this.isVisible = false;
    this.currentItem = null;
    this.childType = '';
  }

  changeChildType(childType: string) {
    this.childType = childType;
  }

  saveModal(value: string) {
    if (!value || value.trim() === '') {
      this.alertMessage = "Please enter a valid value"
      return;
    }
    this.addData(this.currentItem, this.childType, this.parentItem, value);
    this.closeModal();
  }

  getAllProjectWithDomain() {
    this.apiSourceService.getAllPRojectWithDomain().subscribe({
      next: (res: any[]) => {
        console.log("res", res);
        this.allDomainList = Object.keys(res);
        this.allDomainList.forEach((domain, index) => {
          this.domainColors[domain] = this.getRandomColor();
        });
        this.allDomainWithProject = res
      }, error: (error: any) => {
        throw error;
      }
    });
  }

  loadAllProjectInsightDomain(ids: any[]) {

    if (ids.length === 0) {
      this.allDomainDataList = [];
      return;
    }

    let changed = false;

    for (const id of ids) {
      if (!this.allDomainDataList.some(domain => domain.id === id)) {
        changed = true;
        break;
      }
    }

    if (!changed) {
      return;
    }

    this.allDomainDataList = [];

    this.projectInsightDomainService.getAllProjectInsightDomain(ids).subscribe({
      next: (res: any[]) => {

        this.allDomainDataList = res.filter(domain => domain.isActive).map(domain => ({
          ...domain,
          isOpen: false,
          ...this.processChildren(domain.children || [])
        }));

      }, error: (error: any) => {
        throw error;
      }
    });
  }

  processChildren(children: any[]): { subDomains: any[]; services: any[] } {
    const subDomains = [];
    const services = [];

    for (const child of children) {
      if (!child.isActive) continue;

      const base = {
        ...child,
        isOpen: false
      };

      if (child.type === 'subDomain') {
        const { subDomains: subSubDomains, services: subServices } = this.processChildren(child.children || []);
        subDomains.push({
          ...base,
          children: [], // Optional: keep if backend uses it
          subDomains: subSubDomains,
          services: subServices
        });
      } else if (child.type === 'service') {
        const { subServices } = this.processSubServices(child.children || []);
        services.push({
          ...base,
          subServices
        });
      }
    }

    return { subDomains, services };
  }

  processSubServices(children: any[]): { subServices: any[] } {
    const subServices = [];

    for (const child of children) {
      if (!child.isActive) continue;

      if (child.type === 'subService') {
        const { subServices: nestedSubServices } = this.processSubServices(child.children || []);
        subServices.push({
          ...child,
          isOpen: false,
          subServices: nestedSubServices
        });
      }
    }

    return { subServices };
  }


  getRandomColor(): string {
    const colors = [
      '#1e3a8a', '#4338ca', '#5b21b6', '#7c3aed', '#9333ea',
      '#a21caf', '#be123c', '#b91c1c', '#dc2626', '#c2410c',
      '#b45309', '#92400e', '#0f766e', '#065f46', '#047857',
      '#064e3b', '#0c4a6e', '#1e40af', '#1d4ed8', '#2563eb',
      '#3b82f6', '#312e81', '#422006', '#78350f', '#3f6212',
      '#365314', '#14532d', '#166534', '#115e59', '#134e4a'
    ];


    return colors[Math.floor(Math.random() * colors.length)];
  }

  selectDomain(domain: string) {
    if (!this.alreadySelected || !(domain == this.selectedDomain)) {
      this.selectedDomain = domain
      this.alreadySelected = true
      this.getAllProjectInsightProjectList(domain);
    } else {
      this.selectedDomain = null
      this.alreadySelected = false
      this.getAllProjectInsightProjectList();
    }
  }

  addData(item: any, child: string, parent: string, value?: string) {

    if (!value || value.trim() === '') {
      this.alertMessage = "Please enter a valid value";
      return;
    }

    const regex = /^[a-zA-Z0-9 ]+$/

    if (!regex.test(value)) {
      this.alertMessage = "Please enter a valid value";
      return;
    }

    if (parent == "domain") {

      this.projectInsightDomainService.editDomain({
        "parent_id": item.id,
        "parent_id_name": "domain",
        "name": value,
        "type": child
      }).subscribe({
        next: (res: any) => {
          if (child == 'subDomain') {
            item.subDomains.push({ name: value, id: res, isOpen: false, subDomains: [], services: [] });
          } if (child == 'service') {
            item.services.push({ service: value, serviceId: res, isOpen: false, subServices: [] });
          }
        }, error: (error: any) => {
          throw error;
        }
      })

    } else if (child === 'subDomain') {
      this.projectInsightDomainService.editDomain({
        "parent_id": item.id,
        "parent_id_name": "subDomain",
        "name": value,
        "type": child
      }).subscribe({
        next: (res: any) => {
          item.children.push({ name: value, id: res, isOpen: false, subDomains: [], services: [] });
        }, error: (error: any) => {
          throw error;
        }
      })

    } else if (child === 'service') {
      this.projectInsightDomainService.editDomain({
        "parent_id": item.id,
        "parent_id_name": parent,
        "name": value,
        "type": child
      }).subscribe({
        next: (res: any) => {
          item.services.push({ name: value, id: res, isOpen: false, subServices: [] });
        }, error: (error: any) => {
          throw error;
        }
      })

    } else if (child === 'subService') {
      this.projectInsightDomainService.editDomain({
        "parent_id": item.id,
        "parent_id_name": parent,
        "name": value,
        "type": child
      }).subscribe({
        next: (res: any) => {
          item.subServices.push({ name: value, id: res, isOpen: false, subServices: [] });
        }, error: (error: any) => {
          throw error;
        }
      })
    }

    this.apiSourceService.setIdToRemove(this.addedDomainId);
    this.title = ""

  }

  getName(name: string) {
    if (name.length > 10) {
      return name.substring(0, 9) + '...';
    }
    return name;
  }

  addIsOpenToSubDomains(subDomains: SubDomain[]): SubDomain[] {
    return subDomains.filter(subDomain => subDomain.isActive).map(subDomain => ({
      ...subDomain,
      isOpen: false,
      children: this.addIsOpenToSubDomains(subDomain.children), // Recursively handle children
      services: subDomain.services.filter(service => service.isActive).map(service => ({
        ...service,
        isOpen: false,
        subServices: this.addIsOpenToSubServices(service.subServices) // Handle subServices
      }))
    }));
  }

  addIsOpenToSubServices(subServices: SubService[]): SubService[] {
    return subServices.filter(subService => subService.isActive).map(subService => ({
      ...subService,
      isOpen: false,
      children: this.addIsOpenToSubServices(subService.children) // Recursively handle children
    }));
  }

  selectChildrenOfDomain(childrenSubDomain: number, domain: string, unique_name: string) {
    this.childrenSubDomain = childrenSubDomain
    this.selectedDomain = domain
    this.alreadySelected = true
    this.getAllProjectInsightProjectList(childrenSubDomain, unique_name);
  }
  // Domain [End]

  // Searching & Sorting Logic  [Start]
  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort) {
    //console.log(sort);
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  onSearch(searchData) {
    this.filters = searchData;
  }

  onGlobalSearch() {
    this.projectInsightService.searchProjectInsight(this.searchKeyword, this.page - 1, this.limit).pipe(first()).subscribe({
      next: (response: any) => {
        this.allProjectInsightProjectList = response.content;
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      }
    });
  }
  // Searching & Sorting Logic [End]

  // Create Project [Start]
  showCreateProject() {
    this.cancelRequest();
    this.isTable = false;
    this.isCreateForm = true;
    this.getFormByFormId(this.selectedFormId);
    // Load initial options after a short delay to ensure form is ready
    // setTimeout(async () => {
    //   await this.loadInitialOptions();
    // }, 100);
  }

  getFormByFormId(formId: string) {
    this.cancelRequest();
    this.formBuilderService.getByDynamicFormById(formId).pipe(first()).subscribe({
      next: (response: any) => {
        this.currentNode = this.buildFormNodeTree(response);
        this.startProjectForm();
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      }
    });
  }

  openCreateProject() {
    this.getAllProjects();
    this.getAllDepartmentList();
    this.selectedProject = null;
    this.selectedDepartments = [];
    this.selectedFormId = null;
    this.modalRef = this.modalService.show(this.openCreateProjectModal);
  }

  getProjectTitle(): string {
    let projectNameField;
    if (Array.isArray(this.rootNode.fields)) {
      projectNameField = this.rootNode.fields.find(f => f.name === 'projectname');
    }
    else if (typeof this.rootNode.fields === 'object') {
      projectNameField = Object.values(this.rootNode.fields).find((f: any) => f.name === 'projectname');
    }
    if (!projectNameField) return this.rootNode.formName || 'Project';

    const selectedValue = this.rootNode.formData?.projectname || (this.rootNode.fields as any)?.projectname;
    if (!selectedValue) return this.rootNode.formName || 'Project';

    const selectedOption = (projectNameField.options || []).find(opt => opt.value == selectedValue);
    return selectedOption ? selectedOption.label : this.rootNode.formName || 'Project';
  }

  getProjectInsightFormTitle(): string {
    this.rootNode;
    return null;
  }
  // Create Project [End]

  // Form Node Structure [Start]

  buildFormNodeTree(formDef: any): FormNode {
    const node: FormNode = {
      id: formDef.id,
      formName: formDef.formName,
      fields: formDef.fields || [],
      formData: {},
      layoutConfig: this.getLayoutConfig(formDef.fields || []),
    };
    return node;
  }
  // Form Node Structure [End]

  //Breadcrumb [Start]

  startProjectForm() {
    this.currentNodePath = [this.currentNode];
  }

  navigateToProject() {
    this.currentNodePath = [this.rootNode];
  }

  navigateToTreeNode(path: number[]) {
    let node = this.rootNode;
    const newPath = [node];
    for (const idx of path) {
      // if (!node.children || !node.children[idx]) break;
      // node = node.children[idx];
      // newPath.push(node);
    }
    this.currentNodePath = newPath;
  }

  navigateToNode(index: number) {
    this.currentNodePath = this.currentNodePath.slice(0, index + 1);
  }
  //Breadcrumb [End]

  // Form Layout Config [Start]
  getLayoutConfig(fields: any[]): any[][] {
    const rows = new Map<number, any[]>();
    let currentRow = 0;
    let currentRowWidth = 0;
    (fields || []).forEach(field => {
      const fieldWidth = Number(field.width) || 100;
      if (currentRowWidth + fieldWidth > 100) {
        currentRow++;
        currentRowWidth = fieldWidth;
      } else {
        currentRowWidth += fieldWidth;
      }
      if (!rows.has(currentRow)) {
        rows.set(currentRow, []);
      }
      rows.get(currentRow)?.push(field);
    });
    return Array.from(rows.values());
  }

  setTableRows(val: number) {
    if (this.editingField && this.editingField.tableConfig) {
      this.editingField.tableConfig.rows = Number(val);
    }
  }

  onFieldsUpdated(event: { fields: any[], layoutConfig: any[][] }) {
    this.currentNode.fields = event.fields;
    this.currentNode.layoutConfig = event.layoutConfig;
    this.currentNode.fields = [...this.currentNode.fields];
    this.currentNode.layoutConfig = [...this.currentNode.layoutConfig];
  }

  onFormValueChange(node: FormNode, value: any) {
    node.formData = value;
    Object.assign(node.fields, value);
  }

  getDefaultGroupStructure(): FormNode {
    const idTitle = this.generateUniqueId();
    const idType = this.generateUniqueId();
    const idObjective = this.generateUniqueId();
    const idOutcomes = this.generateUniqueId();
    const idRemarks = this.generateUniqueId();

    const fields = [
      {
        id: idTitle,
        type: 'text',
        label: 'Group Title',
        name: 'groupTitle',
        required: true,
        placeholder: 'Enter Group Title',
        defaultValue: '',
        optionSource: 'static',
        width: 50,
        rowPosition: 0,
        multiple: false
      },
      {
        id: idType,
        type: 'select',
        label: 'Type',
        name: 'groupType',
        required: true,
        placeholder: '',
        defaultValue: '',
        options: [
          { label: 'Milestone', value: 'milestone' },
          { label: 'Feature', value: 'feature' },
          { label: 'Activity', value: 'activity' },
          { label: 'Tasks', value: 'tasks' }
        ],
        optionSource: 'static',
        width: 50,
        rowPosition: 0,
        multiple: false
      },
      {
        id: idObjective,
        type: 'textarea',
        label: 'Objective',
        name: 'groupObjective',
        required: false,
        placeholder: 'Enter Objective',
        defaultValue: '',
        optionSource: 'static',
        width: 100,
        rowPosition: 1,
        multiple: false
      },
      {
        id: idOutcomes,
        type: 'textarea',
        label: 'Outcome(s)',
        name: 'groupoutcomes',
        required: false,
        placeholder: 'Enter Outcome(s)',
        defaultValue: '',
        optionSource: 'static',
        width: 100,
        rowPosition: 2,
        multiple: false
      },
      {
        id: idRemarks,
        type: 'textarea',
        label: 'Remarks',
        name: 'groupRemarks',
        required: false,
        placeholder: 'Enter Remarks',
        defaultValue: '',
        optionSource: 'static',
        width: 100,
        rowPosition: 3,
        multiple: false
      }
    ];
    const layoutConfig = [
      [fields[0], fields[1]],
      [fields[2]],
      [fields[3]],
      [fields[4]]
    ];

    return {
      id: this.generateUniqueId(),
      formName: '',
      fields,
      formData: {},
      layoutConfig,
      questionList: []
    };
  }

  get projectInsightProjectTreeBreadcrumb(): any {
    if (!this.rootNode) {
      return null;
    }
    return {
      ...this.rootNode,
      groupList: []
    };
  }
  // Form Layout Config [End]

  // Question Logic [Start]
  openAddOrUpdateQuestionModal(currentNode: any, isQuestionUpdate: any, question?: any) {
    this.questionRenderType = 'edit';
    this.parentId = currentNode.parentId;
    this.parentType = currentNode.parentType;
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
        this.alertMessage = response.serviceStatus;
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
        this.closeAddOrUpdateQuestionModal();
        this.getProjectInsightQuestionDetailsByParentIdAndParentType(this.parentId, this.parentType);
      },
      (error) => {
        console.log(error, " : error");
        this.alertMessage = error?.error?.serviceResponse || 'An unexpected error occurred.';
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      }
    );
  }

  openDeleteQuestionModal(question: any) {
    this.deletedQuestion = question;
    this.deletedQuestion.parentId = this.currentNode.parentId;
    this.deletedQuestion.parentType = this.currentNode.parentType;
    this.addOrUpdateQuestionModalRef = this.modalService.show(this.addOrUpdateQuestionModal, { class: 'modal-md' });
  }

  closeDeleteQuestionModal() {
    if (this.deleteQuestionModalRef) {
      this.deleteQuestionModalRef.hide();
    }
  }

  getProjectInsightQuestionDetailsByParentIdAndParentType(questionParentId: any, questionParentType: any) {
    this.currentQuestionList = [];
    this.projectInsightService.getProjectInsightQuestionDetailsByParentIdAndParentType(questionParentId, questionParentType).pipe(first()).subscribe({
      next: (response: any) => {
        this.currentQuestionList = response.serviceResponse;
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
      }
    });
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
        const emp = this.allEmployeeList.find((e: any) => e.empId === empId);
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

  // Import/ Export Impl [Start]

  getFieldColumns(fields: any[]) {
    return fields.map(f => ({
      header: f.label || f.name,
      key: f.name,
      type: f.type
    }));
  }

  getDataRow(columns: any[], formData: any) {
    const row: any = {};
    columns.forEach(col => {
      row[col.key] = formData ? formData[col.key] || '' : '';
    });
    return row;
  }

  collectEntities(
    node: any,
    parentId: string | null,
    entityType: string,
    sheets: any,
    parentType: string | null = null,
    indexPath: number[] = [],
    parentReadableName: string = ''
  ) {
    if (!sheets[entityType]) {
      const columns = this.getFieldColumns(node.fields || []);

      columns.unshift({ header: `${entityType} Name`, key: `${entityType}Name`, type: 'string' });
      // Add technical columns (hidden)
      columns.unshift({ header: `${entityType}Id`, key: `${entityType}Id`, type: 'id' });
      if (parentType) {
        columns.unshift({ header: `parent${parentType}Id`, key: `parent${parentType}Id`, type: 'id' });
        columns.unshift({ header: `parent${parentType}Name`, key: `parent${parentType}Name`, type: 'string' });
      }
      sheets[entityType] = { columns, rows: [], hiddenCols: [] };
    }
    const columns = sheets[entityType].columns;

    const row = this.getDataRow(columns, node.formData || {});
    row[`${entityType}Id`] = node.id;
    if (parentType && parentId) row[`parent${parentType}Id`] = parentId;
    if (parentType && parentReadableName) row[`parent${parentType}Name`] = parentReadableName;

    let readableName = '';
    if (entityType.toLowerCase().includes('group') && indexPath.length > 0) {
      readableName = indexPath.map((idx, i) => {
        if (i === 0) return `Group-${idx + 1}`;
        return `SubGroup-${idx + 1}`;
      }).join(' ');
    } else if (entityType.toLowerCase().includes('project')) {
      readableName = node.formName || 'Project';
    }
    row[`${entityType}Name`] = readableName;

    sheets[entityType].rows.push(row);

    (node.fields || []).forEach(f => {
      if (f.type === 'table' && node.formData && node.formData[f.name]) {
        const tableData = node.formData[f.name];
        if (Array.isArray(tableData) && f.tableConfig && f.tableConfig.columns) {
          const tableSheetName = `${entityType}_${f.label || f.name}_${node.id}`;
          if (!sheets[tableSheetName]) {
            const tableColumns = f.tableConfig.columns.map((col: any) => ({
              header: col.label,
              key: col.name,
              type: col.type
            }));
            sheets[tableSheetName] = { columns: tableColumns, rows: [] };
          }
          tableData.forEach((rowData: any) => {
            sheets[tableSheetName].rows.push(this.getDataRow(sheets[tableSheetName].columns, rowData));
          });
        }
      }
    });

    (node.children || []).forEach((child, idx) => {
      const childType = child.formName?.replace(/\s+/g, '') || 'Child';
      this.collectEntities(
        child,
        node.id,
        childType,
        sheets,
        entityType,
        [...indexPath, idx],
        readableName
      );
    });
  }

  collectRows(node: any, parentId: string | null, level: string, rows: any[], columns: any[], idField: string, parentField: string,
    wb: XLSX.WorkBook, entityType: string) {
    const row = this.getDataRow(columns, node.formData || {});
    row[idField] = node.id;
    if (parentId) row[parentField] = parentId;
    rows.push(row);

    (node.fields || []).forEach(f => {
      if (f.type === 'table' && node.formData && node.formData[f.name]) {
        const tableData = node.formData[f.name];
        if (Array.isArray(tableData) && f.tableConfig && f.tableConfig.columns) {
          const tableColumns = f.tableConfig.columns.map((col: any) => ({
            header: col.label,
            key: col.name,
            type: col.type
          }));
          const tableSheetData = [
            tableColumns.map(c => c.header),
            tableColumns.map(c => c.key),
            tableColumns.map(c => c.type),
            ...tableData.map((row: any) => tableColumns.map(c => row[c.key] || ''))
          ];
          const wsTable = XLSX.utils.aoa_to_sheet(tableSheetData);
          const sheetName = `${entityType}_${f.label || f.name}_${node.id}`.substring(0, 31);
          XLSX.utils.book_append_sheet(wb, wsTable, sheetName);
        }
      }
    });

    (node.children || []).forEach(child => {
      if (level === 'project') {
        this.collectRows(child, node.id, 'group', rows, columns, 'groupId', 'parentProjectId', wb, 'Group');
      } else if (level === 'group') {
        this.collectRows(child, node.id, 'subgroup', rows, columns, 'subGroupId', 'parentGroupId', wb, 'SubGroup');
      }
    });
  }

  exportTemplate() {
    const node = this.currentNode;
    if (!node) return;
    const wb = XLSX.utils.book_new();
    const sheets: any = {};

    // Start recursion with root node
    this.collectEntities(node, null, node.formName.replace(/\s+/g, ''), sheets);

    // Write each sheet
    Object.keys(sheets).forEach(sheetName => {
      const { columns, rows } = sheets[sheetName];
      const sheetData = [
        columns.map(c => c.header),
        columns.map(c => c.key),
        columns.map(c => c.type),
        ...rows.map(row => columns.map(c => row[c.key] || ''))
      ];
      const ws = XLSX.utils.aoa_to_sheet(sheetData);

      // ws['!cols'] = columns.map(col =>
      //   (col.key.endsWith('Id') && !col.key.endsWith('Name')) ? { hidden: true } : {}
      // );
      XLSX.utils.book_append_sheet(wb, ws, sheetName.substring(0, 31)); // Excel sheet name limit
    });

    // Save
    const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
    saveAs(new Blob([wbout], { type: 'application/octet-stream' }), 'form-structure.xlsx');
  }

  onFileChangeImport(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (e: any) => {
      const data = new Uint8Array(e.target.result);
      const workbook = XLSX.read(data, { type: 'array' });

      const sheetDataMap: { [sheet: string]: any[] } = {};
      workbook.SheetNames.forEach(sheetName => {
        const ws = workbook.Sheets[sheetName];
        const rows: any[][] = XLSX.utils.sheet_to_json(ws, { header: 1 });
        if (rows.length < 4) return;

        const keys = rows[1];
        const dataRows = rows.slice(3);
        sheetDataMap[sheetName] = dataRows.map(rowArr => {
          const rowObj: any = {};
          keys.forEach((key: string, idx: number) => {
            rowObj[key] = rowArr[idx];
          });
          return rowObj;
        });
      });
      this.applyImportedDataToNode(this.currentNode, null, sheetDataMap);
      this.alertMessage = 'Form data imported successfully!';
      this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
    };
    reader.readAsArrayBuffer(file);
  }

  findOptionValueByLabel(field: any, input: any): any {
    if (!field.options || input === undefined || input === null || input === '') return input;
    const inputStr = String(input).trim();
    if (!inputStr) return input;

    let found = field.options.find(opt => String(opt.label).toLowerCase() === inputStr.toLowerCase());
    if (found) return found.value;

    found = field.options.find(opt => String(opt.label).toLowerCase().includes(inputStr.toLowerCase()));
    if (found) return found.value;

    found = field.options.find(opt => String(opt.value) == inputStr);
    if (found) return found.value;
    return input;
  }

  applyImportedDataToNode(node: any, parentId: string | null, sheetDataMap: any) {
    const entityType = node.formName.replace(/\s+/g, '');
    const sheetRows = sheetDataMap[entityType];
    if (!sheetRows) return;

    let nodeRow;
    if (!parentId) {
      nodeRow = sheetRows.find((row: any) => row[`${entityType}Id`] == node.id);
    } else {
      nodeRow = sheetRows.find((row: any) =>
        row[`parent${node.parentType}Id`] == parentId && row[`${entityType}Id`] == node.id
      );
    }

    if (nodeRow) {
      const formData: any = {};
      (node.fields || []).forEach(field => {
        const val = nodeRow[field.name];
        if (val === undefined) return;
        if (field.type === 'select' && field.options) {
          formData[field.name] = this.findOptionValueByLabel(field, val);
        } else if (field.type === 'table' && field.tableConfig) {
          formData[field.name] = val;
        } else {
          formData[field.name] = val;
        }
      });
      node.formData = formData;
    }

    (node.children || []).forEach(child => {
      child.parentType = entityType;
      this.applyImportedDataToNode(child, node.id, sheetDataMap);
    });
  }
  // Import/ Export Impl [End]

  // Clone [Start]
  opencloneProjectModal() {
    this.modalRef = this.modalService.show(this.cloneModal);
  }

  getCloneProjectInsight() {
    this.cancelRequest();
    this.projectInsightService.getProjectInsightByInsightId(this.cloneProjectInsightId).pipe(first()).subscribe({
      next: (response: any) => {
        response.version = null;
        response.id = null;
        this.rootNode = this.buildFormNodeTree(response.structure);
        this.mergeFormDataIntoFormStructure(this.rootNode, response.data);
        this.currentNodePath = [this.rootNode];
        this.startProjectForm();
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      }
    });
  }
  // Clone [End]

  //  Filter [Start]

  openFilterModal() {
    this.filterModal = true;
  }

  closeFilterModal() {
    this.filterModal = false;
  }

  applyFilterModal(filter: any) {
    this.projectInsightService.filterProjectInsight(filter).subscribe({
      next: (res: any) => {
        this.existingFilter = filter
        this.allProjectInsightProjectList = [...res.content];
        this.closeFilterModal();
      }, error: (error: any) => {
        throw error;
      }
    })
  }
  //  Filter [End]

  // Validations [Start]
  validateGroupDetails() {
    let flag = true;
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectInsightGroupDetails.groupTitle)) {
      this.alertMessage = "Please Provide Group Title !!"
      this.openAlertMod(this.alertMessage);
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(this.projectInsightGroupDetails.groupType)) {
      this.alertMessage = "Please select Group Type !!"
      this.openAlertMod(this.alertMessage);
      return false;
    }
    return flag;
  }
  // Validations [End]

  // Modals
  openAlertMod(message: any) {
    this.modalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
  }

  // Close Modal
  cancelRequest() {
    this.modalRef.hide();
  }
}
