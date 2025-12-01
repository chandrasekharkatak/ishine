import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators, UntypedFormControl } from '@angular/forms';
import { Sort } from '@angular/material/sort';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { ApiSourceService } from 'src/app/services/api-source.service';
import { DepartmentService } from 'src/app/services/department.service';
import { FormBuilderService } from 'src/app/services/form-builder.service';
import * as XLSX from 'xlsx';

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
  multiple?: boolean;
  tableConfig?: TableFieldConfig;
}

interface TableFieldConfig {
  columns: { name: string; label: string; type: string }[];
  rows: number;
}

@Component({
  standalone: false,
  selector: 'app-form-builder',
  templateUrl: './form-builder.component.html',
  styleUrls: ['./form-builder.component.scss']
})
export class FormBuilderComponent implements OnInit {
  @ViewChild('alert_message') alertMessageTemplate : TemplateRef<any>;

  instructions = [
    ["Column", "Description", "Example"],
    ["type", "Field type. Allowed: text, textarea, select, checkbox, radio, date, number, email, file, table", "text"],
    ["label", "Field label (displayed to the user)", "Employee Name"],
    ["name", "Unique key for the field (no spaces or special characters)", "employeeName"],
    ["required", "Is this field mandatory? true/false", "true"],
    ["placeholder", "Placeholder text (for text, textarea, number, email)", "Enter your name"],
    ["defaultValue", "Default value for the field", "John Doe"],
    ["optionSource", "For select/checkbox/radio: static, api, dependent", "static"],
    ["options", "For static: JSON array of {label, value}. Leave blank for others.", `[{"label":"A","value":"a"}]`],
    ["apiUrl", "For optionSource=api: API endpoint to fetch options", "https://api.example.com/items"],
    ["apiLabelKey", "For optionSource=api: Key for label in API response", "name"],
    ["apiValueKey", "For optionSource=api: Key for value in API response", "id"],
    ["width", "Field width as percentage (100, 50, 33, 25)", "100"],
    ["rowPosition", "Row number for layout (auto or leave blank)", "0"],
    ["parentField", "For dependent: name of parent field", "department"],
    ["dependentApiUrl", "For dependent: API endpoint with {parentValue} placeholder", "https://api.example.com/subitems?parentId={parentValue}"],
    ["dependentLabelKey", "For dependent: Key for label in API response", "name"],
    ["dependentValueKey", "For dependent: Key for value in API response", "id"],
    ["dependentParamName", "For dependent: Query parameter name for parent value (default: parent field name)", "departmentId"],
    ["multiple", "For select: Allow multiple selection? true/false", "false"],
    ["tableConfig", "For table: JSON object with columns and rows", `{"columns":[{"name":"col1","label":"Column 1","type":"text"}],"rows":2}`]
  ];
  
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

  alertMessage: any;
  modalRef:NgbModalRef;

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
  parentFormId: any;
  fields: FormField[] = [];
  allDeptList: any[] = [];
  allFormListList: any[] = [];
  editingField: FormField | null = null;
  editingIndex: number = -1;
  showFieldConfig = false;
  preview = false;
  form: UntypedFormGroup;
  isDragging = false;
  apiList = [];

  dependentFieldsMap: Map<string, any[]> = new Map();
  fieldDependencies: Map<string, string> = new Map();

  layoutConfig: FormField[][] = [];
  previewLayoutConfig: any[] = [];

  parentSelectedValuesMap: { [key: string]: any[] } = {};
  dependentOptionsMap: { [key: string]: any[] } = {};

  // Add these new properties for the form renderer
  previewFormData: any = {};

  constructor(private fb: UntypedFormBuilder,
    private departmentService: DepartmentService,
    private formBuilderService: FormBuilderService,
    private modalService: NgbModal,
    private apiSourceService: ApiSourceService) {
    this.form = this.fb.group({});
  }

  ngOnInit() {
    this.showTable();
    this.updateLayoutConfig();
  }

  showCreateForm(){
    this.isCreation = true;
    this.isTable = false;
    this.isUpdateForm = false;

    this.resetForm();
    this.getAllDepartmentList();
    this.getAllApiSourceList();
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
    this.parentFormId = null;
    this.fields = [];
  }

  showUpdateForm(formObj: any){
    this.isCreation = true;
    this.isUpdateForm = true;
    this.isTable = false;

    this.formName = formObj.formName;
    this.id = formObj.id;
    this.departmentId = formObj.departmentId;
    this.parentFormId = formObj.parentFormId;
    this.fields = formObj.fields;

    this.getAllDepartmentList();
    this.getAllApiSourceList();
  }

  openDeleteDepartment(template: TemplateRef<any> ,formObj: any){
    this.formName = formObj.formName;
    this.id = formObj.id;
    this.departmentId = formObj.departmentId;
    this.parentFormId = formObj.parentFormId;
    this.modalRef = this.modalService.open(template);
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
  }

  editField(field: FormField, i: number) {
    this.editingField = JSON.parse(JSON.stringify(field));
    this.editingIndex = i;
    this.showFieldConfig = true;
  }

  downloadForm(form: any) {
    const headers = [
      'type', 'label', 'name', 'required', 'placeholder', 'defaultValue',
      'optionSource', 'options', 'apiUrl', 'apiLabelKey', 'apiValueKey',
      'width', 'rowPosition', 'parentField', 'dependentApiUrl',
      'dependentLabelKey', 'dependentValueKey', 'dependentParamName', 'multiple',
      'tableConfig'
    ];
    
    const formRows = (form.fields || []).map(f => {
      const row = {
        ...f,
        options: f.options && Array.isArray(f.options) ? JSON.stringify(f.options) : '',
        required: f.required ? 'true' : 'false',
        multiple: f.multiple ? 'true' : 'false',
        tableConfig: ''
      };
  
      if (f.type === 'table' && f.tableConfig) {
        try {
          row.tableConfig = JSON.stringify(f.tableConfig);
        } catch (error) {
          console.error('Error stringifying tableConfig:', error);
          row.tableConfig = JSON.stringify({
            columns: [
              { name: 'col1', label: 'Column 1', type: 'text' },
              { name: 'col2', label: 'Column 2', type: 'text' }
            ],
            rows: 2
          });
        }
      }
  
      return row;
    });
    
    const wsInstructions = XLSX.utils.aoa_to_sheet(this.instructions);
    const ws = XLSX.utils.json_to_sheet(formRows, { header: headers });
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, wsInstructions, 'Instructions');
    XLSX.utils.book_append_sheet(wb, ws, 'FormTemplate');
    XLSX.writeFile(wb, `${form.formName || 'form'}.xlsx`);
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
        
        const parentField = this.fields.find(f => f.name === parentFieldName);
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
      },
      error: (error: any) => {
        this.alertMessage = error;
        this.modalRef = this.modalService.open(this.alertMessageTemplate);
      }
    });
  }

  loadApiOptions() {
    if (this.editingField && this.editingField.apiUrl) {
      this.apiSourceService.loadDynamicApi(this.editingField.apiUrl).pipe(first()).subscribe({
        next: (data: any) => {
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

  setTableRows(val: number) {
    if (this.editingField && this.editingField.tableConfig) {
      this.editingField.tableConfig.rows = Number(val);
    }
  }

  saveFieldConfig() {
    if (!this.editingField) return;

    if(!this.editingField.name || !this.editingField.label || !this.editingField.type){
      this.alertMessage = "All fields must have a name, label, and type.";
      this.modalRef = this.modalService.open(this.alertMessageTemplate);
      return;
    }

    this.editingField.width = Number(this.editingField.width);

    if (this.editingField.type === 'table' && this.editingField.tableConfig) {
      this.editingField.tableConfig.rows = Number(this.editingField.tableConfig.rows) || 1;
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
      parentFormId: this.parentFormId,
      fields: this.fields,
      // layout: this.getLayoutConfig()
    };

    // check if the fields are valid
    const validFields = this.fields.every(field => field.name && field.label && field.type);
    if (!validFields) {
      this.alertMessage = "All fields must have a name, label, and type.";
      this.modalRef = this.modalService.open(this.alertMessageTemplate);
      return;
    }

    console.log('Form Configuration:', formConfig);
    this.formBuilderService.createDynamicForm(formConfig).pipe(first()).subscribe({
      next: (response: any) => {
        this.formName = null;
        this.departmentId = null;
        this.parentFormId = null;
        this.fields = [];

        this.alertMessage = response.serviceMessage;
        this.modalRef = this.modalService.open(this.alertMessageTemplate);
      },
      error: (error: any) => {
        this.alertMessage = "Unable to save form !!";
        this.modalRef = this.modalService.open(this.alertMessageTemplate);
      }
    });
  }

  updateForm(){
    this.cancelRequest();
    const formConfig = {
      id: this.id,
      formName: this.formName,
      departmentId: this.departmentId,
      parentFormId: this.parentFormId,
      fields: this.fields
    };
    console.log('Form Configuration:', formConfig);
    this.formBuilderService.updateDynamicForm(this.id,formConfig).pipe(first()).subscribe({
      next: (response: any) => {
        this.formName = null;
        this.departmentId = null;
        this.parentFormId = null;
        this.fields = [];

        this.alertMessage = response.serviceMessage;
        this.modalRef = this.modalService.open(this.alertMessageTemplate);
      },
      error: (error: any) => {
        this.alertMessage = "Unable to save form !!";
        this.modalRef = this.modalService.open(this.alertMessageTemplate);
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
        this.modalRef = this.modalService.open(template);
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
        this.modalRef = this.modalService.open(this.alertMessageTemplate);
      }
    });
  }

  getLayoutConfig() {
    console.log("hi getLayoutConfig");
    
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

      if (field.type === 'select' && field.optionSource === 'api' && field.apiUrl) {
        this.apiSourceService.loadDynamicApi(field.apiUrl).subscribe((data: any) => {
          field.options = this.mapApiOptions(data, field.apiLabelKey!, field.apiValueKey!);
        });
      }

      if (field.type === 'select' && field.multiple) {
        this.form.addControl(field.name, new UntypedFormControl([]));
      } else {
        this.form.addControl(field.name, new UntypedFormControl(''));
      }
    });
    this.form = this.fb.group(group);
    this.layoutConfig = this.getLayoutConfig();

    console.log(this.fields);
    console.log(this.form);
    console.log(this.layoutConfig, " --layoutConfig");
    
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
    this.preview = true;
    // Initialize preview form data with default values
    this.previewFormData = {};
    this.previewLayoutConfig = this.getLayoutConfig();
    this.fields.forEach(field => {
      if (field.defaultValue) {
        this.previewFormData[field.name] = field.defaultValue;
      }
    });
  }

  hidePreview() {
    this.preview = false;
  }

  getAvailableParentFields(currentField: any): any[] {
    return this.fields.filter(field => 
      field.name !== currentField.name && 
      ['select', 'radio'].includes(field.type) &&
      field.optionSource !== 'dependent' // Prevent circular dependencies
    );
  }

  onParentFieldChange() {
    if (this.editingField && this.editingField.parentField) {
      const parentField = this.fields.find(f => f.name === this.editingField.parentField);
      const parentValueKey = parentField?.apiValueKey || parentField.name;
      
      if (parentField) {
        if (!this.editingField.dependentParamName) {
          this.editingField.dependentParamName = parentValueKey;
        }
        
        // If there's already a dependent API URL selected, update it with the new parent field
        if (this.editingField.dependentApiUrl) {
          const baseUrl = this.editingField.dependentApiUrl.split('/{')[0];
          this.editingField.dependentApiUrl = `${baseUrl}/{${this.editingField.parentField}}`;
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
    this.modalRef.close();
  }

  updateLayoutConfig() {
    const rows = new Map<number, FormField[]>();
    let currentRow = 0;
    let currentRowWidth = 0;

    this.fields.forEach(field => {
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

    this.layoutConfig = Array.from(rows.values());
  }

  // ------------------------ Import / Export excel ----------------------------------------------

  exportTemplate() {
    const headers = [
      'type', 'label', 'name', 'required', 'placeholder', 'defaultValue',
      'optionSource', 'options', 'apiUrl', 'apiLabelKey', 'apiValueKey',
      'width', 'rowPosition', 'parentField', 'dependentApiUrl',
      'dependentLabelKey', 'dependentValueKey', 'dependentParamName', 'multiple',
      'tableConfig'
    ];
  
    const sampleRow = {
      type: 'text',
      label: 'Employee Name',
      name: 'employeeName',
      required: true,
      placeholder: 'Enter name',
      defaultValue: '',
      optionSource: '',
      options: '',
      apiUrl: '',
      apiLabelKey: '',
      apiValueKey: '',
      width: 100,
      rowPosition: 0,
      parentField: '',
      dependentApiUrl: '',
      dependentLabelKey: '',
      dependentValueKey: '',
      dependentParamName: '',
      multiple: false,
      tableConfig: ''
    };
  
    const sampleTableRow = {
      type: 'table',
      label: 'Employee Table',
      name: 'employeeTable',
      required: false,
      placeholder: '',
      defaultValue: '',
      optionSource: '',
      options: '',
      apiUrl: '',
      apiLabelKey: '',
      apiValueKey: '',
      width: 100,
      rowPosition: 0,
      parentField: '',
      dependentApiUrl: '',
      dependentLabelKey: '',
      dependentValueKey: '',
      dependentParamName: '',
      multiple: false,
      tableConfig: JSON.stringify({
        columns: [
          { name: 'col1', label: 'Column 1', type: 'text' },
          { name: 'col2', label: 'Column 2', type: 'text' }
        ],
        rows: 2
      })
    };
  
    const wsInstructions = XLSX.utils.aoa_to_sheet(this.instructions);
    const ws = XLSX.utils.json_to_sheet([sampleRow, sampleTableRow], { header: headers });
  
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, wsInstructions, 'Instructions');
    XLSX.utils.book_append_sheet(wb, ws, 'FormTemplate');
  
    XLSX.writeFile(wb, 'form_template.xlsx');
  }

  onFileChange(evt: any) {
    const target: DataTransfer = <DataTransfer>(evt.target);
    if (target.files.length !== 1) return;
  
    const reader: FileReader = new FileReader();
    reader.onload = (e: any) => {
      const bstr: string = e.target.result;
      const wb: XLSX.WorkBook = XLSX.read(bstr, { type: 'binary' });
      const wsname: string = wb.SheetNames.includes('FormTemplate')
        ? 'FormTemplate'
        : wb.SheetNames[0];
  
      const ws: XLSX.WorkSheet = wb.Sheets[wsname];
      const data = XLSX.utils.sheet_to_json(ws, { defval: '' });
  
      this.fields = data.map((row: any) => {
        const field = {
          ...row,
          required: row.required === 'true' || row.required === true,
          multiple: row.multiple === 'true' || row.multiple === true,
          options: row.options
            ? (typeof row.options === 'string' && row.options.trim() !== ''
                ? JSON.parse(row.options)
                : [])
            : [],
          width: Number(row.width) || 100,
          rowPosition: Number(row.rowPosition) || 0
        };
  
        if (row.type === 'table' && row.tableConfig) {
          try {
            if (typeof row.tableConfig === 'string' && row.tableConfig.trim() !== '') {
              field.tableConfig = JSON.parse(row.tableConfig);
            } else if (typeof row.tableConfig === 'object') {
              field.tableConfig = row.tableConfig;
            } else {
              field.tableConfig = {
                columns: [
                  { name: 'col1', label: 'Column 1', type: 'text' },
                  { name: 'col2', label: 'Column 2', type: 'text' }
                ],
                rows: 2
              };
            }
          } catch (error) {
            console.error('Error parsing tableConfig:', error);
            field.tableConfig = {
              columns: [
                { name: 'col1', label: 'Column 1', type: 'text' },
                { name: 'col2', label: 'Column 2', type: 'text' }
              ],
              rows: 2
            };
          }
        }
  
        return field;
      });
  
      this.updateLayoutConfig();
    };
    reader.readAsBinaryString(target.files[0]);
  }
}
