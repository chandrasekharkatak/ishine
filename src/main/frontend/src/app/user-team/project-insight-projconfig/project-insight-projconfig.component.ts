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
interface FormNode {
  id: string;
  formName: string;
  fields: any[];
  formData: any;
  layoutConfig?: any[];
  children: FormNode[];
  questionList?: ProjectQuestion[];
}

@Component({
  selector: 'app-project-insight-projconfig',
  templateUrl: './project-insight-projconfig.component.html',
  styleUrls: ['./project-insight-projconfig.component.css']
})
export class ProjectInsightProjconfigComponent implements OnInit {
  @ViewChild(FormRendererComponent) formRenderer!: FormRendererComponent;
  @ViewChild('open_create_project_modal') openCreateProjectModal: TemplateRef<any>;
  @ViewChild('alert_message') alertMessageTemplate: TemplateRef<any>;

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

  // Form
  dynamicForm: FormGroup;

  //breadcrumb
  currentNodePath: FormNode[] = [];

  //List
  allDeptList: any[] = [];
  allFormListList: any[] = [];
  allProjectInsightProjectList: any[] = [];
  allDomainDataList: any[] = [];

  fields: any[] = [];
  projectInsightProjectObj: any = {};

  //object
  selectedDepartment: any;
  selectedFormId: any;
  selectedFormType: any;
  alertMessage: any;

  //columnList
  projectColumns: any[] = ['blank', '', '', '', '', ''];

  //boolean
  isCreateForm: boolean = false;
  isTable: boolean = false;
  isDomainStructure: boolean = false;
  isCurrentEmployeeRoleGreaterThanManager: boolean;
  isSelected: any

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

  // Add new properties for enhanced form handling
  dependentFieldOptions: Map<string, Map<string, any[]>> = new Map();
  multiSelectDependentFields: Map<string, FormArray> = new Map();

  // Add a property to cache the layout config
  private _layoutConfigCache: any[][] = [];
  private _fieldsHash: string = '';

  // Public property for template binding
  layoutConfig: any[][] = [];

  // Dynamic form layouts (these will be loaded from your JSON configurations)
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

  // Updated data structure to handle nested forms
  formStructure: any = null;
  currentFormData: any = {};
  currentFormPath: string[] = []; // Track current form path like ['project', 'group1', 'subgroup1']

  rootNode: FormNode = null; // The root of the form tree
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
    private http: HttpClient,
    private departmentService: DepartmentService,
    private modalService: BsModalService,
    private formBuilderService: FormBuilderService,
    private validationService: ValidationService,
    private authenticationService: AuthenticationService,
    private projectInsightService: ProjectInsightService,
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x);  }

  ngOnInit(): void {
    this.isCurrentEmployeeRoleGreaterThanManager = this.rolesGreaterThanManager.includes(this.currentUser?.employeeRole);
    this.openTableView();

    setTimeout(async () => {
      await this.loadInitialOptions();
    }, 200);

    this.projectData.project = {};

  }

  showTable() {
    this.isTable = true;
    this.isCreateForm = false;
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
    this.projectInsightProjconfigService.getAllDomainData()
      .pipe(first())
      .subscribe({
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

    // Check if this field has dependent fields
    if (this.dependentFieldsMap.has(fieldName)) {
      const dependentFields = this.dependentFieldsMap.get(fieldName)!;

      // Clear and reload options for all dependent fields
      dependentFields.forEach(dependentFieldName => {
        this.handleDependentFieldChange(dependentFieldName, selectedValue);
      });
    }
  }

  onMultiSelectChange(event: any, field: any) {
    const selectedValues = event.value;

    // Check if this field has dependent fields
    if (this.dependentFieldsMap.has(field.name)) {
      const dependentFields = this.dependentFieldsMap.get(field.name)!;

      // Handle multi-select dependent fields
      dependentFields.forEach(dependentFieldName => {
        this.handleMultiSelectDependentFieldChange(dependentFieldName, selectedValues);
      });
    }
  }

  onDependentSelectChange(event: any, field: any, parentValue: string) {
    const selectedValue = event.value;

    // Check if this field has dependent fields
    if (this.dependentFieldsMap.has(field.name)) {
      const dependentFields = this.dependentFieldsMap.get(field.name)!;

      // Clear and reload options for all dependent fields
      dependentFields.forEach(dependentFieldName => {
        this.handleDependentFieldChange(dependentFieldName, selectedValue);
      });
    }
  }

  // Method to update layout when fields change
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

    // Prevent infinite loops by checking if the value is actually changing
    const currentValue = this.dynamicForm.get(fieldName)?.value;
    if (currentValue === parentValue) return;

    // Clear the dependent field's value
    this.dynamicForm.get(fieldName)?.setValue('');

    // Clear existing options
    field.options = [];

    // Load new options only if parent value is not empty
    if (parentValue) {
      await this.loadDependentOptions(field, parentValue);
      // Update layout after options are loaded
      this.updateLayout();
    }
  }

  async handleMultiSelectDependentFieldChange(fieldName: string, parentValues: string[]) {
    const field = this.fields.find(f => f.name === fieldName);
    if (!field || field.optionSource !== 'dependent') return;

    // Clear the dependent field's value
    this.dynamicForm.get(fieldName)?.setValue('');

    // Clear existing options
    field.options = [];

    // Load new options for each parent value only if there are values
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

      // Update layout after options are loaded
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
      const response = await this.http.get<any[]>(url).toPromise();

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
      // Return empty array on error to prevent hanging
      return [];
    }

    return [];
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

  // initializeDependencies() {
  //   this.dependentFieldsMap.clear();
  //   this.fieldDependencies.clear();

  //   this.fields.forEach(field => {
  //     if (field.optionSource === 'dependent' && field.parentField) {
  //       this.fieldDependencies.set(field.name, field.parentField);

  //       if (!this.dependentFieldsMap.has(field.parentField)) {
  //         this.dependentFieldsMap.set(field.parentField, []);
  //       }
  //       this.dependentFieldsMap.get(field.parentField)!.push(field.name);
  //     }
  //   });
  // }

  async loadInitialOptions() {
    const promises = [];
    for (const field of this.fields) {
      if (field.optionSource === 'api') {
        promises.push(this.loadApiOptions(field));
      }
    }

    // Wait for all API options to load
    await Promise.all(promises);

    // Update layout after all options are loaded
    this.updateLayout();
  }

  async loadApiOptions(field: any): Promise<any[]> {
    if (!field.apiUrl) return [];

    try {
      const response = await this.http.get<any[]>(field.apiUrl).toPromise();

      if (response && Array.isArray(response)) {
        const options = response.map(item => ({
          label: item[field.apiLabelKey || 'name'],
          value: item[field.apiValueKey || 'id']
        }));

        // Cache the options
        field.options = options;
        return options;
      }
    } catch (error) {
      console.error(`Error loading API options for ${field.name}:`, error);
      // Return empty array on error to prevent hanging
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

  onSubmit() {
    // Access the form group from the child
    const formValue = this.formRenderer.dynamicForm.value;
    if (this.formRenderer.dynamicForm.valid) {
      // Call your API with formValue
      // Example: this.apiService.saveProject(formValue).subscribe(...)
    } else {
      // Optionally mark all fields as touched to show validation errors
      this.formRenderer.dynamicForm.markAllAsTouched();
    }
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

  // loadFormLayouts() {
  //   // Load your JSON form layouts
  //   // This is where you'll fetch your dynamic form configurations
  //   this.projectFormLayout = {
  //     // Your project form layout JSON
  //   };
  //   this.groupFormLayout = {
  //     // Your group form layout JSON
  //   };
  //   this.subGroupFormLayout = {
  //     // Your subgroup form layout JSON
  //   };
  // }

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

    // Try to get the template subgroup from the first group and its first child
    const templateSubGroup = this.rootNode.children?.[0]?.children?.[0] || null;
    console.log('Template SubGroup:', templateSubGroup);

    if (templateSubGroup) {
      newSubGroup = this.cloneFormNode(templateSubGroup, false);
    } else {
      // Use default for the very first subgroup
      newSubGroup = this.getDefaultSubGroupStructure();
    }

    const node = this.currentNode;

    // If at project level, move to first group
    if (node === this.rootNode) {
      if (this.rootNode.children?.[0]) {
        this.currentNodePath.push(this.rootNode.children[0]);
        this.addSubGroup();
      } else {
        this.addGroup();
      }
      return;
    }

    // If at group level, add to its children
    if (this.rootNode.children.includes(node)) {
      node.children.push(newSubGroup);
      this.currentNodePath.push(newSubGroup);
      return;
    }

    // If at subgroup level, add sibling to parent group
    const parentGroup = this.findParentNode(node);
    if (parentGroup) {
      parentGroup.children.push(newSubGroup);
      this.currentNodePath.push(newSubGroup);
      return;
    }

    // Fallback error
    console.error('Cannot find parent group for subgroup');
  }

  private findExistingSubGroup(groupNode: FormNode): FormNode | null {
    if (!groupNode.children || groupNode.children.length === 0) {
      return null;
    }
    return groupNode.children[0];
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
    return 'subgroup_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }

  private getDefaultSubGroupStructure(): FormNode {
    //  API call later ============================
    return {
      id: this.generateUniqueId(),
      formName: '',
      fields: [],
      formData: {},
      layoutConfig: [],
      children: [],
      questionList: []
    };
  }

  // group logic
  addGroup() {
    let newGroup: FormNode;
    const existingGroup = this.findExistingGroup();

    if (existingGroup) {
      // Pass false to NOT clone children
      newGroup = this.cloneFormNode(existingGroup, false);
    } else {
      newGroup = this.getDefaultGroupStructure();
    }
    this.rootNode.children.push(newGroup);
    this.currentNodePath.push(newGroup);

    console.log(this.currentNodePath, " : this.currentNodePath");
  }

  private getDefaultGroupStructure(): FormNode {
    return {
      id: this.generateUniqueId(),
      formName: '',
      fields: [],
      formData: {},
      layoutConfig: [],
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
    return {
      id: this.generateUniqueId(),
      formName: node.formName || '',
      fields: JSON.parse(JSON.stringify(node.fields)),
      formData: {},
      layoutConfig: this.getLayoutConfig(node.fields || []),
      children: cloneChildren ? node.children.map(child => this.cloneFormNode(child)) : [],
      questionList: []
    };
  }

  getNodeDisplayName(node: any): string {
    if (node.fields) {
      if (node.fields.grouptitle) return node.fields.grouptitle;
      if (node.fields.subgrouptitle) return node.fields.subgrouptitle;
    }
    return node.formName || '';
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

  onSaveAndAssign() {
    const dataToSave = this.collectFormData(this.rootNode);
    console.log('Saving structure:', dataToSave);
  }

  onSaveAsDraft() {
    const dataToSave = this.collectFormData(this.rootNode);
    console.log('Saving structure:', dataToSave);
  }

  onFormValueChange(node: FormNode, value: any) {
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
  addQuestion(entity: FormNode) {
    let question: ProjectQuestion = new ProjectQuestion();
    if (!entity.questionList) {
      entity.questionList = [];
    }
    entity.questionList.push(question);
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

      // Cast event.target to HTMLElement
      const element = event.target as HTMLElement;
      const rect = element.getBoundingClientRect();

      // Get click coordinates
      this.contextMenuX = event.clientX;
      this.contextMenuY = event.clientY;

      // Ensure menu stays within viewport
      const viewportWidth = window.innerWidth;
      const viewportHeight = window.innerHeight;
      const menuWidth = 200; // Approximate width of context menu
      const menuHeight = 160; // Approximate height of context menu

      // Adjust if menu would go outside viewport
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











  // Modals
  openAlertMod(template: TemplateRef<any>, message: any) {
    this.alertMessage = message;
    this.bsModalRef = this.modalService.show(template, { class: 'modal-sm' });
  }

  closeDocumentPreviewTemplate() {
    this.documentPreviewModalRef.hide();
  }
}
