import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Sort } from '@angular/material/sort';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { first } from 'rxjs/operators';
import { DepartmentService } from 'src/app/services/department.service';
import { FormBuilderService } from 'src/app/services/form-builder.service';

interface FormFieldOption {
  label: string;
  value: any;
}

interface FormField {
  id: string;
  type: string;
  label: string;
  name: string;
  required?: boolean;
  placeholder?: string;
  defaultValue?: any;
  options?: FormFieldOption[];
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
}

@Component({
  selector: 'app-form-builder',
  templateUrl: './form-builder.component.html',
  styleUrls: ['./form-builder.component.scss']
})
export class FormBuilderComponent implements OnInit {
  @ViewChild('alert_message') alertMessageTemplate : TemplateRef<any>;
  
  fieldPalette = [
    { type: 'text', label: 'Text Input' },
    { type: 'textarea', label: 'Text Area' },
    { type: 'select', label: 'Dropdown' },
    { type: 'checkbox', label: 'Checkbox' },
    { type: 'radio', label: 'Radio' },
    { type: 'date', label: 'Date' },
    { type: 'number', label: 'Number' },
    { type: 'email', label: 'Email' },
    { type: 'file', label: 'File Upload' }
  ];

  alertMessage: any;
  modalRef: BsModalRef = new BsModalRef();

  isTable: boolean = false;
  isCreation: boolean = false;
  isUpdateForm: boolean = false;

  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;

  filters:any = {};
  isSearchEnabled:boolean = false;
  formColumns:any[] = ['blank','formName','departmentName','blank'];

  formName: any;
  id: any;
  departmentId: any;
  fields: FormField[] = [];
  allDeptList: any[] = [];
  allFormListList: any[] = [];
  editingField: FormField | null = null;
  editingIndex: number = -1;
  showFieldConfig = false;
  preview = false;
  form: FormGroup;
  isDragging = false;
  apiList = [
    { label: 'Countries API', url: 'https://restcountries.com/v3.1/all', labelKey: 'name.common', valueKey: 'cca2' },
    { label: 'Users API', url: 'https://jsonplaceholder.typicode.com/users', labelKey: 'name', valueKey: 'id' }
  ];

  dependentFieldsMap: Map<string, any[]> = new Map();
  fieldDependencies: Map<string, string> = new Map();

  constructor(private fb: FormBuilder,
    private http: HttpClient,
    private departmentService: DepartmentService,
    private formBuilderService: FormBuilderService,
    private modalService: BsModalService) {
    this.form = this.fb.group({});
  }

  ngOnInit() {
    this.showTable();
  }

  showCreateForm(){
    this.isCreation = true;
    this.isTable = false;
    this.isUpdateForm = false;

    this.resetForm();
    this.getAllDepartmentList();
  }

  showTable(){
    this.isTable = true;
    this.isCreation = false;
    this.isUpdateForm = false;

    this.getAllDynamicForm();
  }

  resetForm(){
    this.formName = null;
    this.id = null;
    this.departmentId = null;
    this.fields = [];
  }

  showUpdateForm(formObj: any){
    this.isCreation = true;
    this.isUpdateForm = true;
    this.isTable = false;

    this.formName = formObj.formName;
    this.id = formObj.id;
    this.departmentId = formObj.departmentId;
    this.fields = formObj.fields;
  }

  openDeleteDepartment(template: TemplateRef<any> ,formObj: any){
    this.formName = formObj.formName;
    this.id = formObj.id;
    this.departmentId = formObj.departmentId;
    this.modalRef = this.modalService.show(template);
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

  drop(event: CdkDragDrop<FormField[]>) {
    moveItemInArray(this.fields, event.previousIndex, event.currentIndex);
    this.updateRowPositions();
  }

  updateRowPositions() {
    let currentRow = 0;
    let currentRowWidth = 0;

    this.fields.forEach((field, index) => {
      if (currentRowWidth + field.width > 100) {
        currentRow++;
        currentRowWidth = field.width;
      } else {
        currentRowWidth += field.width;
      }
      field.rowPosition = currentRow;
    });
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
      rowPosition: 0
    };

    this.editingField = newField;
    this.editingIndex = -1;
    this.showFieldConfig = true;
  }

  editField(field: FormField, i: number) {
    this.editingField = JSON.parse(JSON.stringify(field));
    this.editingIndex = i;
    this.showFieldConfig = true;
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
      }
    }
  }

  loadApiOptions() {
    if (this.editingField && this.editingField.apiUrl) {
      this.http.get<any[]>(this.editingField.apiUrl).subscribe({
        next: (data) => {
          this.editingField!.options = this.mapApiOptions(data, this.editingField!.apiLabelKey!, this.editingField!.apiValueKey!);
        },
        error: (error) => {
          console.error('Error loading API options:', error);
        }
      });
    }
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

  saveFieldConfig() {
    if (!this.editingField) return;
    this.editingField.width = Number(this.editingField.width);

    // Handle API options
    if (this.editingField.type === 'select' && this.editingField.optionSource === 'api') {
      // For API options, we don't need to do anything special here
      // The options will be loaded when the form is built
    }

    if (this.editingField.optionSource === 'dependent' && this.editingField.parentField) {
      this.fieldDependencies.set(this.editingField.name, this.editingField.parentField);
      
      if (!this.dependentFieldsMap.has(this.editingField.parentField)) {
        this.dependentFieldsMap.set(this.editingField.parentField, []);
      }
      
      const dependentFields = this.dependentFieldsMap.get(this.editingField.parentField)!;
      if (!dependentFields.includes(this.editingField.name)) {
        dependentFields.push(this.editingField.name);
      }
    }

    if (this.editingIndex === -1) {
      this.fields.push(this.editingField);
    } else {
      this.fields[this.editingIndex] = this.editingField;
    }

    this.updateRowPositions();
    this.editingField = null;
    this.editingIndex = -1;
    this.showFieldConfig = false;
  }

  removeField(i: number) {
    const fieldToRemove = this.fields[i];
    this.fieldDependencies.delete(fieldToRemove.name);
    this.dependentFieldsMap.forEach((dependentFields, parentField) => {
      const updatedDependentFields = dependentFields.filter(f => f !== fieldToRemove.name);
      if (updatedDependentFields.length === 0) {
        this.dependentFieldsMap.delete(parentField);
      } else {
        this.dependentFieldsMap.set(parentField, updatedDependentFields);
      }
    });

    this.fields.splice(i, 1);
    this.updateRowPositions();
  }

  addOption() {
    if (this.editingField) {
      if (!this.editingField.options) this.editingField.options = [];
      this.editingField.options.push({ label: '', value: '' });
    }
  }

  removeOption(idx: number) {
    if (this.editingField && this.editingField.options) {
      this.editingField.options.splice(idx, 1);
    }
  }

  saveForm() {
    this.cancelRequest();
    const formConfig = {
      formName: this.formName,
      departmentId: this.departmentId,
      fields: this.fields,
      // layout: this.getLayoutConfig()
    };
    console.log('Form Configuration:', formConfig);
    this.formBuilderService.createDynamicForm(formConfig).pipe(first()).subscribe({
      next: (response: any) => {
        this.formName = null;
        this.departmentId = null;
        this.fields = [];
        console.log(response);
      },
      error: (error: any) => {
        this.alertMessage = "Unable to save form !!";
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      }
    });
  }

  updateForm(){
    this.cancelRequest();
    const formConfig = {
      id: this.id,
      formName: this.formName,
      departmentId: this.departmentId,
      fields: this.fields
    };
    console.log('Form Configuration:', formConfig);
    this.formBuilderService.updateDynamicForm(this.id,formConfig).pipe(first()).subscribe({
      next: (response: any) => {
        this.formName = null;
        this.departmentId = null;
        this.fields = [];
        console.log(response);
      },
      error: (error: any) => {
        this.alertMessage = "Unable to save form !!";
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      }
    });
  }

  onDeleteForm(template: TemplateRef<any>){
    this.cancelRequest();
    this.formBuilderService.deleteFormById(this.id).pipe(first()).subscribe({
      next: (response: any) => {
        this.showTable();
        console.log(response);
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(template);
      }
    });
  }

  getAllDynamicForm(){
    this.cancelRequest();
    this.formBuilderService.getAllDynamicForm().pipe(first()).subscribe({
      next: (response: any) => {
        this.allFormListList = response;
        console.log(response);
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.show(this.alertMessageTemplate);
      }
    });
  }

  getLayoutConfig() {
    const rows = new Map<number, FormField[]>();
    let currentRow = 0;
    let currentRowWidth = 0;

    this.fields.forEach(field => {
        // Ensure width is a number and convert to bootstrap column width
        const fieldWidth = Number(field.width);
        
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

  generateId() {
    return Math.random().toString(36).substr(2, 9);
  }

  onCheckboxChange(event: any, fieldName: string, value: any) {
    const selected = this.form.get(fieldName)?.value || [];
    if (event.target.checked) {
      this.form.get(fieldName)?.setValue([...selected, value]);
    } else {
      this.form.get(fieldName)?.setValue(selected.filter((v: any) => v !== value));
    }
  }

  buildForm() {
    const group: any = {};
    this.fields.forEach(field => {
      if (field.type === 'checkbox') {
        group[field.name] = [[]];
      } else {
        group[field.name] = field.required
          ? [field.defaultValue || '', Validators.required]
          : [field.defaultValue || ''];
      }

      // if (field.type === 'select' && field.optionSource === 'api' && field.apiUrl) {
      //   this.http.get<any[]>(field.apiUrl).subscribe(data => {
      //     field.options = this.mapApiOptions(data, field.apiLabelKey!, field.apiValueKey!);
      //   });
      // }
    });
    this.form = this.fb.group(group);
  }

  mapApiOptions(data: any[], labelKey: string, valueKey: string): FormFieldOption[] {
    const getValue = (obj: any, path: string) =>
      path.split('.').reduce((acc, part) => acc && acc[part], obj);
    return data.map(item => ({
      label: getValue(item, labelKey),
      value: getValue(item, valueKey)
    }));
  }

  showPreview() {
    this.buildForm();
    this.preview = true;
  }

  hidePreview() {
    this.preview = false;
  }

  onSubmit() {
    if (this.form.valid) {
      console.log('Form Values:', this.form.value);
    }
  }

  onSelectChange(event: any, fieldName: string) {
    const selectedValue = event.target.value;
    
    // Check if this field has dependent fields
    if (this.dependentFieldsMap.has(fieldName)) {
      const dependentFields = this.dependentFieldsMap.get(fieldName)!;
      
      // Clear and reload options for all dependent fields
      dependentFields.forEach(dependentFieldName => {
        this.loadDependentFieldOptions(dependentFieldName, selectedValue);
        
        // Clear the dependent field's value
        const dependentField = this.fields.find(f => f.name === dependentFieldName);
        if (dependentField) {
          this.form.get(dependentFieldName)?.setValue('');
        }
      });
    }
  }

  getAvailableParentFields(currentField: any): any[] {
    return this.fields.filter(field => 
      field.name !== currentField.name && 
      ['select', 'radio'].includes(field.type) &&
      field.optionSource !== 'dependent' // Prevent circular dependencies
    );
  }

  onParentFieldChange() {
    if (this.editingField.parentField) {
      const parentField = this.fields.find(f => f.name === this.editingField.parentField);
      if (parentField) {
        // Set default parameter name if not specified
        if (!this.editingField.dependentParamName) {
          this.editingField.dependentParamName = parentField.name;
        }
        
        // Set default API URL template if not specified
        if (!this.editingField.dependentApiUrl) {
          this.editingField.dependentApiUrl = `https://api.example.com/data?${this.editingField.dependentParamName}={parentValue}`;
        }
        
        // Update dependency mappings
        this.fieldDependencies.set(this.editingField.name, this.editingField.parentField);
        
        if (!this.dependentFieldsMap.has(this.editingField.parentField)) {
          this.dependentFieldsMap.set(this.editingField.parentField, []);
        }
        this.dependentFieldsMap.get(this.editingField.parentField)!.push(this.editingField.name);
      }
    }
  }

  async loadDependentOptions() {
    if (!this.editingField.parentField || !this.editingField.dependentApiUrl) {
      return;
    }

    try {
      // For preview, we'll use a mock value - in real implementation, 
      // this would be called when parent field value changes
      const mockParentValue = '1'; // This would be the actual selected value
      const url = this.editingField.dependentApiUrl.replace('{parentValue}', mockParentValue);
      
      // Make API call to get dependent options
      const response = await this.http.get<any[]>(url).toPromise();
      
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

  async loadDependentFieldOptions(fieldName: string, parentValue: string) {
    const field = this.fields.find(f => f.name === fieldName);
    if (!field || field.optionSource !== 'dependent' || !field.dependentApiUrl) {
      return;
    }

    try {
      const url = field.dependentApiUrl.replace('{parentValue}', parentValue);
      const response = await this.http.get<any[]>(url).toPromise();
      
      if (response) {
        field.options = response.map(item => ({
          label: item[field.dependentLabelKey || 'name'],
          value: item[field.dependentValueKey || 'id']
        }));
      }
    } catch (error) {
      console.error(`Error loading options for ${fieldName}:`, error);
      field.options = [];
    }
  }

  // Method to get field dependencies for debugging
  getFieldDependencies(): any {
    const dependencies: any = {};
    this.fieldDependencies.forEach((parent, child) => {
      dependencies[child] = parent;
    });
    return dependencies;
  }

  //pagination
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

  cancelRequest() {
    this.modalRef.hide();
  }
}
