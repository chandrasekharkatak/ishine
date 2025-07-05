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

interface FormNode {
  id: string;
  formName: string;
  fields: any[];
  formData: any;
  layoutConfig?: any[];
  children: FormNode[];
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

  constructor(
    private projectInsightProjconfigService: ProjectInsightProjconfigService,
    private formBuilder: FormBuilder,
    private http: HttpClient,
    private departmentService: DepartmentService,
    private modalService: BsModalService,
    private formBuilderService: FormBuilderService,
  ) { }

  ngOnInit(): void {
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

    // Build the tree structure dynamically from rootNode
    const treeStructure = {
      project: this.rootNode.formName,
      projectid: this.rootNode.id,
      groupList: this.buildGroupList(this.rootNode.children || [])
    };

    return treeStructure;
  }

  // Helper method to build group list
  private buildGroupList(children: FormNode[]): any[] {
    return children.map(child => ({
      groupid: child.id,
      groupName: child.formName,
      title: child.formName,
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
      children: []
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
      children: []
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
      fields: node.fields,
      formData: {},
      layoutConfig: this.getLayoutConfig(node.fields || []),
      children: cloneChildren ? node.children.map(child => this.cloneFormNode(child)) : []
    };
  }

  collectFormData(node: FormNode): any {
    return {
      fields: node.formData,
      child: node.children.map(child => this.collectFormData(child))
    };
  }

  onSaveAndAssign() {
    const dataToSave = this.collectFormData(this.rootNode);
    console.log('Saving structure:', dataToSave);
  }

  onFormValueChange(node: FormNode, value: any) {
    node.formData = value;
  }

  navigateToProject() {
    this.currentNodePath = [this.rootNode];
  }
}
