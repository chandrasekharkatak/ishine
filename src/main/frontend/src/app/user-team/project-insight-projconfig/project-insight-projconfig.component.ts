import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { BehaviorSubject } from 'rxjs';
import { first } from 'rxjs/operators';
import { ProjectInsightProjconfigService } from 'src/app/services/project-insight-projconfig.service';
import { FormBuilder, FormGroup, Validators, FormArray, FormControl } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { FormRendererComponent } from 'src/app/helpers/form-renderer/form-renderer.component';
import { DepartmentService } from 'src/app/services/department.service';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Template } from '@angular/compiler/src/render3/r3_ast';
import { FormBuilderService } from 'src/app/services/form-builder.service';
import { ProjectQuestion } from 'src/app/models/projectQuestion';
import { ProjectInsightEntity } from 'src/app/models/projectInsightEntity';
import { SurveyOption } from 'src/app/models/sureyOption';
import { ValidationService } from 'src/app/services/validation.service';
import { ProjectInsight } from 'src/app/models/projectInsight';
import { ProjectResponse } from 'src/app/models/projectResponse';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { User } from 'src/app/models/user';
import { AngularEditorConfig } from '@kolkov/angular-editor';
import { Document } from 'src/app/models/document';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { ApiSourceService } from 'src/app/services/api-source.service';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';

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

interface FormField {
  id: string;
  type: string;
  label: string;
  name: string;
  required?: boolean;
  placeholder?: string;
  defaultValue?: any;
  options?: any;
  optionSource?: 'static' | 'api' | 'dependent';
  apiUrl?: string;
  apiLabelKey?: string;
  apiValueKey?: string;
  width: number;
  rowPosition: number;
  parentField?: string;
  dependentApiUrl?: string;
  dependentLabelKey?: string;
  dependentValueKey?: string;
  dependentParamName?: string;
  multiple?: boolean;
  tableConfig?: TableFieldConfig;
}

interface TableFieldConfig {
  columns: { name: string; label: string; type: string }[];
  rows: number;
}

@Component({
  selector: 'app-project-insight-projconfig',
  templateUrl: './project-insight-projconfig.component.html',
  styleUrls: ['./project-insight-projconfig.component.css']
})
export class ProjectInsightProjconfigComponent implements OnInit {
  fieldPalette = [
    { type: 'text', label: 'Text Input' },
    { type: 'textarea', label: 'Text Area' },
    { type: 'select', label: 'Dropdown' },
    { type: 'checkbox', label: 'Checkbox' },
    { type: 'radio', label: 'Radio' },
    { type: 'date', label: 'Date' },
    { type: 'number', label: 'Number' },
    { type: 'email', label: 'Email' },
    { type: 'file', label: 'File Upload' },
    { type: 'table', label: 'Table' }
  ];


  @ViewChild(FormRendererComponent) formRenderer!: FormRendererComponent;
  @ViewChild('open_create_project_modal') openCreateProjectModal: TemplateRef<any>;
  @ViewChild('alert_message') alertMessageTemplate: TemplateRef<any>;
  @ViewChild('delete_template') deleteTemplate: TemplateRef<any>;
  @ViewChild('addFieldModal') addFieldModal: TemplateRef<any>;

  expandedPaths: { [key: string]: boolean } = {};

  //Add new Field
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

  apiList = [];

  rows = Array(8).fill({});

  behaviouralSubjectOnj = new BehaviorSubject(null);
  modalRef: BsModalRef = new BsModalRef();
  bsModalRef: BsModalRef = new BsModalRef();
  documentPreviewModalRef: BsModalRef = new BsModalRef();
  currentUser: User;

  //Question section
  file: any;
  fileName: any;
  projectInsightObj: ProjectInsight = new ProjectInsight();
  displayedResponseUserId: any[] = [];
  rolesGreaterThanManager: any[] = ['HOD', 'SuperAdmin', 'HR', 'RMG'];
  currentQuestionIndex: number | null = null;
  currentQuestionList: ProjectQuestion[] | null = null;

  // Form
  dynamicForm: FormGroup;

  //breadcrumb
  currentNodePath: FormNode[] = [];

  //List
  allDeptList: any[] = [];
  allFormListList: any[] = [];
  allProjectInsightProjectList: any[] = [];
  allDomainDataList: any[] = [];
  allEmployeeList:any[] = [];

  fields: any[] = [];
  projectInsightProjectObj: any = {};

  //object
  selectedDepartment: any;
  selectedFormId: any;
  selectedFormType: any;
  alertMessage: any;
  projectInsightId: any;

  //columnList
  projectColumns: any[] = ['blank', '', '', '', '', ''];

  //boolean
  isCreateForm: boolean = false;
  isTable: boolean = false;
  isDomainStructure: boolean = false;
  isCurrentEmployeeRoleGreaterThanManager: boolean;
  isSelected: any
  isEdit: boolean = true;

  //domain tree
  expanded: { [key: string]: boolean } = {};

  //Utility
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;
  abbreviationError: string = '';
  filters: any = {};
  isSearchEnabled: boolean = false;

  // Add new properties for field dependency
  dependentFieldsMap: Map<string, any[]> = new Map();
  fieldDependencies: Map<string, string> = new Map();
  dependentOptionsMap: { [key: string]: any[] } = {};

  // Add new properties for enhanced form handling
  dependentFieldOptions: Map<string, Map<string, any[]>> = new Map();
  multiSelectDependentFields: Map<string, FormArray> = new Map();

  // Public property for template binding
  layoutConfig: any[][] = [];

  projectFormLayout: any;
  groupFormLayout: any;
  subGroupFormLayout: any;

  // Data structure to hold all form data
  projectData = {
    project: {},
    groups: [] as Array<{
      group: any,
      subGroups: any[]
    }>
  };

  // Form state management
  currentFormType: 'project' | 'group' | 'subgroup' = 'project';
  currentGroupIndex: number = -1;
  currentSubGroupIndex: number = -1;

  // Dynamic form fields and layout for the group/module
  currentGroupObj: any = {};
  formStructure: any = null;
  currentFormData: any = {};
  currentFormPath: string[] = [];

  rootNode: FormNode = null;
  showContextMenu = false;
  contextMenuX = 0;
  contextMenuY = 0;
  selectedText = '';
  newTag: string = '';

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

  constructor(
    private projectInsightProjconfigService: ProjectInsightProjconfigService,
    private formBuilder: FormBuilder,
    private departmentService: DepartmentService,
    private modalService: BsModalService,
    private formBuilderService: FormBuilderService,
    private validationService: ValidationService,
    private authenticationService: AuthenticationService,
    private projectInsightService: ProjectInsightService,
    private apiSourceService: ApiSourceService
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x);  }

  ngOnInit(): void {
    this.isCurrentEmployeeRoleGreaterThanManager = this.rolesGreaterThanManager.includes(this.currentUser?.employeeRole);
    this.openTableView();
    this.getAllApiSourceList();
    this.getAllEmployeeList();

    this.projectData.project = {};
    this.showTable();
  }

  showTable() {
    this.isTable = true;
    this.isCreateForm = false;

    this.getAllProjectInsightProjectList();
  }

  openViewProjectInsight(projectInsightId: any){
    this.cancelRequest();

    this.isEdit = false;
    this.isCreateForm = true;
    this.isTable = false;

    this.projectInsightId = projectInsightId;
    this.getProjectInsightByInsightId(projectInsightId);
  }

  openEditProjectInsight(projectInsightId: any){
    this.cancelRequest();

    this.isEdit = true;
    this.isCreateForm = true;
    this.isTable = false;

    this.projectInsightId = projectInsightId;
    this.getProjectInsightByInsightId(projectInsightId);
  }

  openDeleteProjectInsight(projectInsightId: any){
    this.modalRef = this.modalService.show(this.deleteTemplate, { class: 'modal-sm' });
    this.projectInsightId = projectInsightId;
  }

  editExsitingFields() {
    this.showExistingFieldsList = true;
    this.newFieldType = null;
    this.editingField = null;
    this.editingFieldIndex = -1;
  }

  showExistingFields() {
    this.showExistingFieldsList = true;
    this.newFieldType = null;
    this.editingField = null;
    this.editingFieldIndex = -1;
  }

  backToFieldPalette() {
    this.showExistingFieldsList = false;
    this.newFieldType = null;
    this.editingField = null;
    this.editingFieldIndex = -1;
  }

  populateFormWithData(data: any): void {
    if (data && this.dynamicForm) {
      // Use setTimeout to ensure form is fully initialized
      setTimeout(() => {
        this.dynamicForm.patchValue(data);
      }, 0);
    }
  }

  loadProjectData(projectId: string): void {
    console.log('Loading project data for ID:', projectId);

    // For now, using the existing projectInsightProjectObj
    // In real implementation, you would make an API call here
    setTimeout(() => {
      this.populateFormWithData(this.projectInsightProjectObj);
    }, 100);
  }

  openTableView() {
    this.isCreateForm = false;
    this.isTable = true;
  }

  openCreateProject() {
    this.getAllDepartmentList();
    this.modalRef = this.modalService.show(this.openCreateProjectModal);
  }

  showCreateProject() {
    this.cancelRequest();
    this.isCreateForm = true;
    this.isTable = false;

    this.getFormByFormId(this.selectedFormId);

    // Load initial options after a short delay to ensure form is ready
    setTimeout(async () => {
      await this.loadInitialOptions();
    }, 100);
  }

  getAllDepartmentList() {
    this.allDeptList = [];
    this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllEmployeeList(){
    this.formBuilderService.getAllEmployeeList().pipe(first()).subscribe({
      next: (response: any) => {
        this.allEmployeeList = response;
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      }
    });
  }

  getFormByFormId(formId: string) {
    this.cancelRequest();
    this.formBuilderService.getByDynamicFormById(formId).pipe(first()).subscribe({
      next: (response: any) => {
        this.rootNode = this.buildFormNodeTree(response);
        this.startProjectForm();
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      }
    });
  }

  deleteProjectInsightById(){
    this.cancelRequest();
    this.projectInsightService.deleteProjectInsightById(this.projectInsightId).pipe(first()).subscribe({
      next: (response: any) => {
        this.alertMessage = response.serviceMessage;
        this.modalRef = this.modalService.show(this.alertMessageTemplate);

        this.getAllProjectInsightProjectList();
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      }
    });
  }

  onGlobalSearch(){
    this.projectInsightService.searchProjectInsight(this.searchKeyword).pipe(first()).subscribe({
      next: (response: any) => {
        this.allProjectInsightProjectList = response;
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      }
    });
  }

  getAllProjectInsightProjectList(){
    this.projectInsightService.getAllProjectInsight().pipe(first()).subscribe({
      next: (response: any) => {
        this.allProjectInsightProjectList = response;
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      }
    });
  }

  getProjectInsightByInsightId(projectInsightId: any) {
    this.projectInsightService.getProjectInsightByInsightId(projectInsightId).pipe(first()).subscribe({
      next: (response: any) => {
        this.rootNode = this.buildFormNodeTree(response.structure);
        this.mergeFormDataIntoStructure(this.rootNode, response.data);
        this.currentNodePath = [this.rootNode];
        this.startProjectForm();
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      }
    });
  }
  
  mergeFormDataIntoStructure(structure: any, data: any) {
    if (structure.fields && Array.isArray(structure.fields) && data.fields) {
      structure.fields.forEach(field => {
        field.value = data.fields[field.name];
      });
      structure.formData = data.fields;
    }
    if (data.questions) {
      structure.questionList = data.questions;
    }
    if (structure.children && data.child) {
      for (let i = 0; i < structure.children.length; i++) {
        this.mergeFormDataIntoStructure(structure.children[i], data.child[i]);
      }
    }
  }

  getLayoutConfigForNode(node: FormNode): any[][] {
    const rows = new Map<number, any[]>();
    let currentRow = 0;
    let currentRowWidth = 0;

    (node.fields || []).forEach(field => {
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

  buildFormNodeTree(formDef: any): FormNode {
    const node: FormNode = {
      id: formDef.id,
      formName: formDef.formName,
      fields: formDef.fields || [],
      formData: {},
      layoutConfig: this.getLayoutConfig(formDef.fields || []),
      children: []
    };
    node.children = (formDef.children || []).map(child => this.buildFormNodeTree(child));
    return node;
  }

  getAllDynamicFormByDepartmentAndType() {
    let formObject = {
      departmentId: this.selectedDepartment
    }
    this.formBuilderService.getAllDynamicFormByDepartmentAndType(formObject).pipe(first()).subscribe({
      next: (response: any) => {
        this.allFormListList = response;
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      }
    });
  }

  getAllDomainData(): void {
    this.projectInsightProjconfigService.getAllDomainData().pipe(first()).subscribe({
        next: (response: any) => {
          if (response?.serviceStatus === "Success") {
            this.allDomainDataList = response.serviceResponse;
          } else {
            console.error('Service responded with an error:', response?.serviceResponse || 'Unknown error');
          }
        },
        error: (err: any) => {
          console.error('API call failed:', err);
        }
      });
  }

  toggle(node: any, type: string, idField: string) {
    const key = `${type}-${node[idField]}`;
    this.expanded[key] = !this.expanded[key];
  }

  isExpanded(node: any, type: string, idField: string) {
    const key = `${type}-${node[idField]}`;
    return !!this.expanded[key];
  }

  toggleByPath(path: number[]) {
    const key = path.join('-');
    this.expandedPaths[key] = !this.expandedPaths[key];
  }

  isExpandedByPath(path: number[]) {
    const key = path.join('-');
    return !!this.expandedPaths[key];
  }

  get projectInsightProjectTreeBreadcrumb(): any {
    if (!this.rootNode) {
      return null;
    }
    
    return {
      ...this.rootNode,
      groupList: this.buildGroupList(this.rootNode.children || [])
    };
  }

  private buildGroupList(children: FormNode[]): any[] {
    return children.map(child => ({
      ...child,
      groupList: this.buildGroupList(child.children || [])
    }));
  }

  addItem(type: string, parent: any, event: MouseEvent) {
    event.stopPropagation(); // Prevents toggling when clicking +
    // Open dialog, show inline input, or emit event
    // type: 'domain' | 'subdomain' | 'service' | 'business_feature' | 'feature' | 'subfeature'
    // parent: the parent object at this level, or null for top-level domain
    console.log('Add', type, 'under', parent);
  }

  toggleDomainProjectView(event: any) { }

  onSelectChange(event: any, fieldName: string) {
    const selectedValue = event.value;
    const field = this.fields.find(f => f.name === fieldName);

    if (this.dependentFieldsMap.has(fieldName)) {
      const dependentFields = this.dependentFieldsMap.get(fieldName)!;

      dependentFields.forEach(dependentFieldName => {
        this.handleDependentFieldChange(dependentFieldName, selectedValue);
      });
    }
  }

  onMultiSelectChange(event: any, field: any) {
    const selectedValues = event.value;

    if (this.dependentFieldsMap.has(field.name)) {
      const dependentFields = this.dependentFieldsMap.get(field.name)!;

      dependentFields.forEach(dependentFieldName => {
        this.handleMultiSelectDependentFieldChange(dependentFieldName, selectedValues);
      });
    }
  }

  onDependentSelectChange(event: any, field: any, parentValue: string) {
    const selectedValue = event.value;

    if (this.dependentFieldsMap.has(field.name)) {
      const dependentFields = this.dependentFieldsMap.get(field.name)!;

      dependentFields.forEach(dependentFieldName => {
        this.handleDependentFieldChange(dependentFieldName, selectedValue);
      });
    }
  }

  private updateLayout(): void {
    if (this.rootNode) {
      this.rootNode.layoutConfig = this.getLayoutConfig(this.rootNode.fields);
      this.rootNode.children.forEach(child => this.updateLayoutForTree(child));
    }
  }

  private updateLayoutForTree(node: FormNode): void {
    node.layoutConfig = this.getLayoutConfig(node.fields);
    node.children.forEach(child => this.updateLayoutForTree(child));
  }

  async handleDependentFieldChange(fieldName: string, parentValue: string) {
    const field = this.fields.find(f => f.name === fieldName);
    if (!field || field.optionSource !== 'dependent') return;
    const currentValue = this.dynamicForm.get(fieldName)?.value;
    if (currentValue === parentValue) return;

    this.dynamicForm.get(fieldName)?.setValue('');

    field.options = [];

    if (parentValue) {
      await this.loadDependentOptions(field, parentValue);
      this.updateLayout();
    }
  }

  async handleMultiSelectDependentFieldChange(fieldName: string, parentValues: string[]) {
    const field = this.fields.find(f => f.name === fieldName);
    if (!field || field.optionSource !== 'dependent') return;
    this.dynamicForm.get(fieldName)?.setValue('');

    field.options = [];

    if (parentValues && parentValues.length > 0) {
      const allOptions: any[] = [];
      for (const parentValue of parentValues) {
        if (parentValue) {
          const options = await this.loadDependentOptions(field, parentValue);
          allOptions.push(...options);
        }
      }

      // Remove duplicates
      field.options = this.removeDuplicateOptions(allOptions);
      this.updateLayout();
    }
  }

  removeDuplicateOptions(options: any[]): any[] {
    const seen = new Set();
    return options.filter(option => {
      const duplicate = seen.has(option.value);
      seen.add(option.value);
      return !duplicate;
    });
  }

  async loadDependentOptions(field: any, parentValue: string): Promise<any[]> {
    if (!field.parentField || !field.dependentApiUrl) return [];

    if (!parentValue) return [];

    try {
      const url = field.dependentApiUrl.replace('{parentValue}', parentValue);
      const response:any = await this.apiSourceService.loadDynamicApi(url).toPromise();

      if (response && Array.isArray(response)) {
        const options = response.map(item => ({
          label: item[field.dependentLabelKey || 'name'],
          value: item[field.dependentValueKey || 'id']
        }));

        // Cache the options
        if (!this.dependentFieldOptions.has(field.name)) {
          this.dependentFieldOptions.set(field.name, new Map());
        }
        this.dependentFieldOptions.get(field.name)!.set(parentValue, options);

        return options;
      }
    } catch (error) {
      console.error(`Error loading dependent options for ${field.name}:`, error);
      return [];
    }

    return [];
  }

  extractOptionsFromResponse(response: any, field: any): any[] {
    const options = [];
    
    if (Array.isArray(response)) {
        response.forEach(item => {
            options.push({
                label: item[field.dependentLabelKey || 'label'],
                value: item[field.dependentValueKey || 'value']
            });
        });
    } else if (response.data && Array.isArray(response.data)) {
        response.data.forEach((item: any) => {
            options.push({
                label: item[field.dependentLabelKey || 'label'],
                value: item[field.dependentValueKey || 'value']
            });
        });
    }
    
    return options;
  }

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

  async loadInitialOptions() {
    const promises = [];
    for (const field of this.fields) {
      if (field.optionSource === 'api') {
        promises.push(this.loadApiOptions(field));
      }
    }
    await Promise.all(promises);
    this.updateLayout();
  }

  async loadApiOptions(field: any): Promise<any[]> {
    if (!field.apiUrl) return [];

    try {
      const response = this.apiSourceService.loadDynamicApi(field.apiUrl).toPromise();

      if (response && Array.isArray(response)) {
        const options = response.map(item => ({
          label: item[field.apiLabelKey || 'name'],
          value: item[field.apiValueKey || 'id']
        }));
        field.options = options;
        return options;
      }
    } catch (error) {
      console.error(`Error loading API options for ${field.name}:`, error);
      return [];
    }

    return [];
  }

  // New methods for enhanced form handling
  isParentMultiSelect(field: any): boolean {
    const parentField = this.fields.find(f => f.name === field.parentField);
    return parentField && parentField.multiple;
  }

  getParentSelectedValues(field: any): string[] {
    const parentFieldName = field.parentField;
    const parentValue = this.dynamicForm.get(parentFieldName)?.value;
    return Array.isArray(parentValue) ? parentValue : [];
  }

  getParentSelectedValue(field: any): string {
    const parentFieldName = field.parentField;
    return this.dynamicForm.get(parentFieldName)?.value || '';
  }

  getParentLabel(field: any, parentValue: string): string {
    const parentField = this.fields.find(f => f.name === field.parentField);
    if (!parentField || !parentField.options) return parentValue;

    const option = parentField.options.find((opt: any) => opt.value === parentValue);
    return option ? option.label : parentValue;
  }

  getDependentControlName(field: any, parentValue: string): string {
    return `${field.name}_${parentValue}`;
  }

  getDependentOptions(field: any, parentValue: string): any[] {
    if (!this.dependentFieldOptions.has(field.name)) {
      return [];
    }

    const fieldOptions = this.dependentFieldOptions.get(field.name)!;
    return fieldOptions.get(parentValue) || [];
  }

  //pagination
  page = 1;
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

  getFormStatus(): any {
    return {
      valid: this.dynamicForm?.valid,
      touched: this.dynamicForm?.touched,
      dirty: this.dynamicForm?.dirty,
      values: this.dynamicForm?.value,
      projectObj: this.projectInsightProjectObj
    };
  }

  resetForm(): void {
    this.dynamicForm?.reset();
    this.populateFormWithData(this.projectInsightProjectObj);
  }

  getFormValue(fieldName: string): any {
    return this.dynamicForm.get(fieldName)?.value;
  }

  // Helper method to mark all form controls as touched
  markFormGroupTouched(formGroup: FormGroup) {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      } else {
        control?.markAsTouched();
      }
    });
  }

  onCheckboxChange(event: any, fieldName: string, value: string) {
    const control = this.dynamicForm.get(fieldName);
    if (control) {
      if (event.target.checked) {
        control.setValue(value);
      } else {
        control.setValue('');
      }
    }
  }


  // utitlity
  cancelRequest() {
    this.modalRef.hide();
  }

  //Breadcrumb
  startProjectForm() {
    this.currentNodePath = [this.rootNode];
  }

  get currentNode(): FormNode {
    return this.currentNodePath[this.currentNodePath.length - 1];
  }

  navigateToNode(index: number) {
    this.currentNodePath = this.currentNodePath.slice(0, index + 1);
  }

  navigateToTreeNode(path: number[]) {
    let node = this.rootNode;
    const newPath = [node];
    for (const idx of path) {
      if (!node.children || !node.children[idx]) break;
      node = node.children[idx];
      newPath.push(node);
    }
    this.currentNodePath = newPath;
  }

  private findExistingGroup(): FormNode | null {
    if (!this.rootNode.children || this.rootNode.children.length === 0) {
      return null;
    }
    return this.rootNode.children[0];
  }

  // --------------------- SubGroup impl------------------
  addSubGroup() {
    let newSubGroup: FormNode;
    const templateSubGroup = this.rootNode.children?.[0]?.children?.[0] || null;

    if (templateSubGroup) {
      newSubGroup = this.cloneFormNode(templateSubGroup, false);
    } else {
      newSubGroup = this.getDefaultSubGroupStructure();
    }

    const node = this.currentNode;

    if (node === this.rootNode) {
      if (this.rootNode.children?.[0]) {
        this.currentNodePath.push(this.rootNode.children[0]);
        this.addSubGroup();
      } else {
        this.addGroup();
      }
      return;
    }

    if (this.rootNode.children.includes(node)) {
      node.children.push(newSubGroup);
      this.currentNodePath.push(newSubGroup);
      return;
    }

    const parentGroup = this.findParentNode(node);
    if (parentGroup) {
      parentGroup.children.push(newSubGroup);
      this.currentNodePath.push(newSubGroup);
      return;
    }
    console.error('Cannot find parent group for subgroup');
  }

  addSubSubGroup() {
    const node = this.currentNode;
    if (node === this.rootNode) {
      return;
    }

    const templateSubGroup = this.rootNode.children?.[0]?.children?.[0] || null;
    let newSubGroup: FormNode;
    if (templateSubGroup) {
      newSubGroup = this.cloneFormNode(templateSubGroup, false);
    } else {
      newSubGroup = this.getDefaultSubGroupStructure();
    }
  
    if (!node.children) node.children = [];
    node.children.push(newSubGroup);
    this.currentNodePath.push(newSubGroup);
  }

  isSubGroupOrDeeper(node: FormNode): boolean {
    return node !== this.rootNode && !this.rootNode.children.includes(node);
  }

  private findParentNode(childNode: FormNode): FormNode | null {
    return this.findParentNodeRecursive(this.rootNode, childNode);
  }

  private findParentNodeRecursive(parent: FormNode, targetChild: FormNode): FormNode | null {
    if (!parent.children) return null;
    if (parent.children.includes(targetChild)) {
      return parent;
    }
    for (const child of parent.children) {
      const result = this.findParentNodeRecursive(child, targetChild);
      if (result) return result;
    }
    return null;
  }

  private generateUniqueId(): string {
    return Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }

  private getDefaultSubGroupStructure(): FormNode {
    const idTitle = this.generateUniqueId();
    const idType = this.generateUniqueId();
    const idObjective = this.generateUniqueId();
    const idOutcomes = this.generateUniqueId();
    const idRemarks = this.generateUniqueId();
  
    const fields = [
      {
        id: idTitle,
        type: 'text',
        label: 'Sub Group Title',
        name: 'subgroupTitle',
        required: true,
        placeholder: 'Enter Sub Group Title',
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
        name: 'subgroupType',
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
        name: 'subgroupObjective',
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
        name: 'subgroupoutcomes',
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
        name: 'subgroupRemarks',
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
      children: [],
      questionList: []
    };
  }

  // group logic
  addGroup() {
    let newGroup: FormNode;
    const existingGroup = this.findExistingGroup();

    if (existingGroup) {
      newGroup = this.cloneFormNode(existingGroup, false);
    } else {
      newGroup = this.getDefaultGroupStructure();
    }
    this.rootNode.children.push(newGroup);
    this.currentNodePath.push(newGroup);

    console.log(this.currentNodePath, " : this.currentNodePath");
  }

  private getDefaultGroupStructure(): FormNode {
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
      children: [],
      questionList: []
    };
  }

  removeGroup(index: number) {
    this.rootNode.children.splice(index, 1);
  }

  removeSubGroup(group: FormNode, subIndex: number) {
    group.children.splice(subIndex, 1);
  }

  cloneFormNode(node: FormNode, cloneChildren: boolean = true): FormNode {
    const clonedFields = (node.fields || []).map(field => {
      const clonedField = { ...field };
      clonedField.value = null;
      return clonedField;
    });
  
    return {
      id: this.generateUniqueId(),
      formName: node.formName || '',
      fields: clonedFields,
      formData: {},
      layoutConfig: this.getLayoutConfig(clonedFields),
      children: cloneChildren ? node.children.map(child => this.cloneFormNode(child)) : [],
      questionList: []
    };
  }

  isGroupNode(node: FormNode): boolean {
    return this.rootNode && this.rootNode.children && this.rootNode.children.includes(node);
  }

  isSubGroupNode(node: FormNode): boolean {
    if (!this.rootNode || !this.rootNode.children) return false;
    for (const group of this.rootNode.children) {
      if (group.children && group.children.includes(node)) {
        return true;
      }
    }
    return false;
  }

  deleteGroup(node: FormNode) {
    if (!this.rootNode || !this.rootNode.children) return;
    const idx = this.rootNode.children.indexOf(node);
    if (idx > -1) {
      this.rootNode.children.splice(idx, 1);
      this.currentNodePath = [this.rootNode];
    }
  }

  deleteSubGroup(node: FormNode) {
    if (!this.rootNode || !this.rootNode.children) return;
    for (const group of this.rootNode.children) {
      if (group.children) {
        const idx = group.children.indexOf(node);
        if (idx > -1) {
          group.children.splice(idx, 1);
          this.currentNodePath = [this.rootNode, group];
          return;
        }
      }
    }
  }

  getNodeDisplayName(node: any): string {
    if (node.formData) {
      if (node.formData.grouptitle || node.formData.groupTitle) {
        return node.formData.grouptitle || node.formData.groupTitle;
      }
      if (
        node.formData.subgrouptitle ||
        node.formData.subGroupTitle ||
        node.formData.subgroupTitle
      ) {
        return (
          node.formData.subgrouptitle ||
          node.formData.subGroupTitle ||
          node.formData.subgroupTitle
        );
      }
      if (node.formData.projectname) {
        const projectNameField = (node.fields || []).find(f => f.name === 'projectname');
        if (projectNameField && projectNameField.options) {
          if (Array.isArray(node.formData.projectname)) {
            const selectedLabels = node.formData.projectname.map(val => {
              const opt = projectNameField.options.find(opt => opt.value == val);
              return opt ? opt.label : val;
            });
            if (selectedLabels.length > 0) return selectedLabels.join(', ');
          } else {
            const selected = projectNameField.options.find(opt => opt.value == node.formData.projectname);
            if (selected) return selected.label;
          }
        }
        return node.formData.projectname;
      }
      const tableField = (node.fields || []).find(f => f.type === 'table');
      if (tableField && node.formData[tableField.name]) {
        return `Table (${node.formData[tableField.name].length} rows)`;
      }
    }
  
    if (node.fields) {
      if (node.fields.grouptitle || node.fields.groupTitle) return node.fields.grouptitle || node.fields.groupTitle;
      if (node.fields.subgrouptitle || node.fields.subGroupTitle) return node.fields.subgrouptitle || node.fields.subGroupTitle;
    }
  
    if (node.formName) {
      if (node.formName.toLowerCase().includes('group')) return 'New Group';
      if (node.formName.toLowerCase().includes('subgroup')) return 'New SubGroup';
      if (node.formName.toLowerCase().includes('project')) return 'New Project';
      return node.formName;
    }
    return 'New Node';
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

  collectFormData(node: FormNode): any {
    return {
      fields: node.formData,
      questions: node.questionList || [],
      child: node.children.map(child => this.collectFormData(child))
    };
  }

  onFieldsUpdated(event: {fields: any[], layoutConfig: any[][]}) {
    this.currentNode.fields = event.fields;
    this.currentNode.layoutConfig = event.layoutConfig;
    this.currentNode.fields = [...this.currentNode.fields];
    this.currentNode.layoutConfig = [...this.currentNode.layoutConfig];
    
    console.log('Fields updated in parent:', this.currentNode.fields);
    console.log('Layout config updated in parent:', this.currentNode.layoutConfig);
  }

  onSaveAndAssign() {
    this.cancelRequest();
    const structure = this.rootNode;
    const data = this.collectFormData(this.rootNode);

    const projectFieldKey = Object.keys(data.fields || {}).find(
      key => key.toLowerCase().includes('project')
    );
    const projectId = projectFieldKey ? data.fields[projectFieldKey] : null;
  
    if (!projectId) {
      this.alertMessage = "Project Name/ Field is required to save and Assign.";
      this.modalRef = this.modalService.show(this.alertMessageTemplate);
      return;
    }

    const payload = {
      structure: structure,
      data: data,
      isDraft: "N",
      createdBy: this.currentUser.empId,
      id: this.projectInsightId
    };

    this.projectInsightService.onSaveAndAssign(payload).pipe(first()).subscribe(
      (response: any) => {
        this.formRenderer.dynamicForm.markAllAsTouched();

        this.alertMessage = response.serviceStatus;
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      },
      (error) => {
        console.error('Save and assign failed:', error);
      }
    )
  }

  onSaveAsDraft() {
    this.cancelRequest();
    const structure = this.rootNode;
    const data = this.collectFormData(this.rootNode);

    const projectFieldKey = Object.keys(data.fields || {}).find(
      key => key.toLowerCase().includes('project')
    );
    const projectId = projectFieldKey ? data.fields[projectFieldKey] : null;
  
    if (!projectId) {
      this.alertMessage = "Project Name/ Field is required to save as draft.";
      this.modalRef = this.modalService.show(this.alertMessageTemplate);
      return;
    }
  
    const payload = {
      structure: structure,
      data: data,
      isDraft: "N",
      createdBy: this.currentUser.empId,
      id: this.projectInsightId
    };
  
    this.projectInsightService.onSaveAsDraft(payload).pipe(first()).subscribe(
      (response: any) => {
        this.alertMessage = response.serviceStatus;
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      },
      (error) => {
        console.error('Save and Draft failed:', error);
      }
    );
  }

  onFormValueChange(node: FormNode, value: any) {
    console.log('Form value changed:', value);
    node.formData = value;
    Object.assign(node.fields, value);
  }

  navigateToProject() {
    this.currentNodePath = [this.rootNode];
  }


  // ------------------------------------------ QUESTION -------------------------------------

  clearFileInput(): void {
    this.file = null;
    this.fileName = null;
    const fileInput = document.getElementById('project-data-input-file') as HTMLInputElement;
    if (fileInput)
      fileInput.value = '';
  }

  onFileChange(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.file = file;
      this.fileName = file.name;
      const fileExtension = this.fileName.split(".").pop();
      let elem = document.getElementById('project-data-input-file') as HTMLInputElement;
      if (fileExtension !== 'xlsx' && fileExtension !== 'xls') {
        this.alertMessage = "Only .xlsx file is allowed.";
        this.bsModalRef = this.modalService.show(this.alertMessageTemplate, { class: 'modal-sm' });
        this.clearFileInput();
        return false;
      }
    } else {
      this.clearFileInput();
    }
  }

  // Question Configurations
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
          this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
          const previewContainer = document.getElementById(previewElementId);
          previewContainer.innerHTML = "<span class='mt-3' style='display:inline-block;'>File size must be lesser than or equal to 5MB. </span>";
          response.document = null;
          response.documentFileName = null;
          return false;
        }

        if (file && file.type === 'application/pdf') {
          this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
          const previewContainer = document.getElementById(previewElementId);
          const reader = new FileReader();
          reader.onload = function (e) {
            const pdfData = e.target.result;
            previewContainer.innerHTML = `<embed src="${pdfData}" type="application/pdf" width="100%" height="100%">`;
          };
          reader.readAsDataURL(file);
        }
        else if (file && file.type.startsWith('image/')) {
          this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
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
      this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
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

  getUserUploadedFileForQuestion(question: any, fileName: any, documentPreviewTemplate: TemplateRef<any>, previewElementId: any) {
    let documentObj = new Document();
    documentObj.empId = this.currentUser.empId;
    documentObj.documentName = fileName;
    documentObj.typeId = question.entityId;
    documentObj.typeName = question.entityType;
    this.projectInsightService.getUserUploadedFileForQuestion(documentObj).subscribe((response: any) => {
      this.documentPreviewModalRef = this.modalService.show(documentPreviewTemplate, { class: 'modal-xl' });
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


  // -------------------- Add new Field -----------------------------------
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
  
  addNewField(node: FormNode) {
    console.log('Adding new field to node:', node);
    
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
    
    console.log('addFieldTargetNode set to:', this.addFieldTargetNode);
    this.showFieldTypePalette();
  }
  
  showFieldTypePalette() {
    this.addFieldModalRef = this.modalService.show(this.addFieldModal);
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
    console.log('Field updated successfully:', this.currentNode.fields[this.editingFieldIndex]);
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
      
      console.log('Field deleted successfully : ', this.currentNode.fields);
      console.log('Field deleted Layout config : ', this.currentNode.layoutConfig);
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

    console.log(this.addFieldTargetNode.fields, ": this.addFieldTargetNode.fields ===");
    console.log(this.addFieldTargetNode.layoutConfig, ": this.addFieldTargetNode.layoutConfig ===");
  
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

  getAllApiSourceList(){
    this.apiSourceService.getAllApiSourceList().pipe(first()).subscribe({
      next: (response: any) => {
        this.apiList = response;

        setTimeout(async () => {
          await this.loadInitialOptions();
        }, 200);
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      }
    });
  }
  
  mapApiOptions(data: any[], labelKey: string, valueKey: string): any[] {
    const getValue = (obj: any, path: string) =>
      path.split('.').reduce((acc, part) => acc && acc[part], obj);
    return data.map(item => ({
      label: getValue(item, labelKey),
      value: getValue(item, valueKey)
    }));
  }

  closeAddFieldModal(){
    if (this.addFieldModalRef) {
      this.addFieldModalRef.hide();
    }
    this.editingField = null;
    this.editingFieldIndex = -1;
    this.originalFieldData = null;
    this.showExistingFieldsList = false;
    this.newFieldType = null;
  }

  generateId() {
    return Math.random().toString(36).substr(2, 9);
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
    
    console.log('Field added to current node:', this.currentNode);
    console.log('Updated fields:', this.currentNode.fields);
  
    this.editingField = null;
    this.editingFieldIndex = -1;
    this.showExistingFieldsList = false;
    this.newFieldType = null;
    this.closeAddFieldModal();
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

  setTableRows(val: number) {
    if (this.editingField && this.editingField.tableConfig) {
      this.editingField.tableConfig.rows = Number(val);
    }
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

  async loadDependentOptionsFormBuilder() {
    if (!this.editingField.parentField || !this.editingField.dependentApiUrl) {
      return;
    }

    try {
      // For preview, we'll use a mock value - in real implementation, 
      // this would be called when parent field value changes
      const mockParentValue = '1'; // This would be the actual selected value
      const url = this.editingField.dependentApiUrl.replace('{parentValue}', mockParentValue);
      
      // Make API call to get dependent options
      const response:any = await this.apiSourceService.loadDynamicApi(url).toPromise();
      
      if (response) {
        this.editingField.options = response.map(item => ({
          label: item[this.editingField.dependentLabelKey || 'name'],
          value: item[this.editingField.dependentValueKey || 'id']
        }));
      }
    } catch (error) {
      console.error('Error loading dependent options:', error);
      // Show error message to user
    }
  }

  //  Import / Export Implementation
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
  
  collectRows(node: any,parentId: string | null,level: string,rows: any[],columns: any[],idField: string,parentField: string,
                   wb: XLSX.WorkBook,entityType: string) {
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
    const node = this.rootNode;
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

      ws['!cols'] = columns.map(col =>
        (col.key.endsWith('Id') && !col.key.endsWith('Name')) ? { hidden: true } : {}
      );
  
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
  
      this.applyImportedDataToNode(this.rootNode, null, sheetDataMap);
      this.alertMessage = 'Form data imported successfully!';
      this.modalRef = this.modalService.show(this.alertMessageTemplate);
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

  // Modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.alertMessage = message;
    this.bsModalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  closeDocumentPreviewTemplate() {
    this.documentPreviewModalRef.hide();
  }
}
