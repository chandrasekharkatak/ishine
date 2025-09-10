import { HttpClient } from '@angular/common/http';
import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges, ChangeDetectorRef, ViewChild, TemplateRef } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ApiSourceService } from 'src/app/services/api-source.service';
import { KnowledgeHubService } from 'src/app/services/knowledge-hub.service';
import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { ResizeEvent } from 'angular-resizable-element';
import { FormField } from 'src/app/models/formField';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { environment } from 'src/environments/environment';
import { FileUploadComponent } from './FileUpload/FileUpload.component';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';

@Component({
  selector: 'app-form-renderer',
  templateUrl: './form-renderer.component.html',
  styleUrls: ['./form-renderer.component.css','form-renderer.component.scss']
})
export class FormRendererComponent implements OnInit, OnChanges {

  @ViewChild('file_upload_modal') fileUploadModalTemplate: TemplateRef<any>;

  @Input() viewMode!: any;
  @Input() fields: any[] = [];
  @Input() layoutConfig: any[][] = [];
  @Input() formData: any = {};
  @Input() query: string = "";
  @Input() isDragEnabled:boolean;
  @Input() isResizeEnabled:boolean;
  @Input() projectName: string = "";

  @Output() formValueChange = new EventEmitter<any>();
  @Output() formSubmit = new EventEmitter<any>();
  @Output() fieldsUpdated = new EventEmitter<{ fields: any[], layoutConfig: any[][] }>();
  @Output() selectedDomainIds = new EventEmitter<any[]>();

  isLoading = true;
  private clickTimeout: any = 500;
  private clickCount = 0;
  // searchControls: { [fieldName: string]: FormControl } = {};
  // filteredDependentOptions: { [fieldName: string]: any[] } = {};

  resizeInfo: { [fieldName: string]: { width: number; height: number; cols: number } } = {}
  isResizing: { [fieldName: string]: boolean } = {}
  selectedFiles: any[] = [];
  selectedFileField: any = null;
  baseUrl = environment.baseUrl;
  fileUploadModalRef: BsModalRef = new BsModalRef();

  dynamicForm: FormGroup;
  dependentFieldOptions: Map<string, Map<string, any[]>> = new Map();
  apiCache: Map<string, any[]> = new Map<string, any[]>();
  dependentOptionsMap: { [fieldName: string]: any[] } = {};

  constructor(private fb: FormBuilder, private apiSourceService: ApiSourceService,private knowledgeHubService: KnowledgeHubService, private projectInsightService: ProjectInsightService, private http:HttpClient, private modalService:BsModalService,  private cdr: ChangeDetectorRef) { }

  async ngOnInit() {
    this.isLoading = true;
    await this.prepareApiOptions();
    this.buildForm();
    this.isLoading = false;
  }

  async ngOnChanges(changes: SimpleChanges) {
    if (
      (changes.fields && changes.fields.currentValue !== changes.fields.previousValue) ||
      (changes.formData && changes.formData.currentValue !== changes.formData.previousValue)
    ) {
      await this.prepareApiOptions();
      this.buildForm();
    }
  }

  get formControlsCount(): number {
    return this.dynamicForm ? Object.keys(this.dynamicForm.controls).length : 0;
  }

  getTableData(field: any): any[] {
    if (this.viewMode === 'View') {
      const formArray = this.dynamicForm.get(field.name) as FormArray;
      return formArray.value;
    }
    return [];
  }

  getTableRows(field: any): any[] {
    return Array.from({ length: field.tableConfig.rows });
  }

  highlight(text: any): string {
    return this.knowledgeHubService.highlight(text, this.query, !!this.query);
  }

  async prepareApiOptions() {
    const apiUrlMap = new Map<string, any[]>();

    const apiFields = this.fields?.filter(f => f.optionSource?.toLowerCase() === 'api' && f.apiUrl);
    const uniqueApiUrls = [...new Set(apiFields?.map(f => f.apiUrl))];

    for (const apiUrl of uniqueApiUrls) {
      const options = await this.loadApiOptions(apiUrl);
      apiUrlMap.set(apiUrl, options);
    }

    if (this.fields && this.fields?.length > 0) {
      for (const field of this?.fields) {
        if (field.optionSource?.toLowerCase() === 'api' && field.apiUrl) {
          field.options = this.getOptionsFromApiResponse(field, apiUrlMap.get(field.apiUrl));
        }

        if (field.optionSource === 'dependent' && field.parentField && this.formData?.[field.parentField]) {
          const parentValue = this.formData[field.parentField];
          field.options = await this.getDependentOptions(field, parentValue);
        }
      }
    }
  }

  buildForm() {
    const controls: any = {};
    this.fields?.forEach(field => {
      const validators = [];
      if (field.required) validators.push(Validators.required);
      // --- Table Field Handling ---
      if (field.type === 'table') {
        if (!this.formData[field.name] || !Array.isArray(this.formData[field.name])) {
          this.formData[field.name] = Array(field.tableConfig.rows)
            .fill(null)
            .map(() => {
              const rowObj = {};
              field.tableConfig.columns.forEach(col => rowObj[col.name] = '');
              return rowObj;
            });
        }

        // Create a FormArray of FormGroups for the table
        const rowsArray = new FormArray([]);
        for (let i = 0; i < field.tableConfig.rows; i++) {
          const rowGroup = {};
          field.tableConfig.columns.forEach(col => {
            rowGroup[col.name] = new FormControl(this.formData[field.name][i][col.name] || '');
          });
          rowsArray.push(new FormGroup(rowGroup));
        }
        controls[field.name] = rowsArray;
        return;
      }

      let defaultValue =
        this.formData[field.name] !== undefined ? this.formData[field.name] :
          field.value !== undefined ? field.value :
            field.defaultValue !== undefined ? field.defaultValue : '';

      if (field.type === 'checkbox') {
        controls[field.name] = [Array.isArray(defaultValue) ? defaultValue : [], validators];
      }

      // if (field.type === 'select' && field.multiple) {
      //   if (Array.isArray(field.options) && field.options.length > 0) {
      //     const optionType = typeof field.options[0].value;
      //     defaultValue = Array.isArray(defaultValue) ? defaultValue.map(v =>
      //       optionType === 'string' ? String(v) : Number(v)
      //     ) : [];
      //   } else {
      //     defaultValue = Array.isArray(defaultValue) ? defaultValue : [];
      //   }

      //   if ((field.name as string).toLowerCase() === "domain") {
      //     this.selectedDomainIds.emit(defaultValue);
      //   }
      // }

      if(field.type === "file" && this.formData[field.name]) {
        defaultValue = defaultValue != null ? this.formData[field.name] : []
      }

      if (field.type === 'select' && field.multiple) {
        if (!Array.isArray(defaultValue)) {
          defaultValue = defaultValue != null ? [defaultValue] : [];
        }

        // if (Array.isArray(field.options) && field.options.length > 0) {
        //   const optionType = typeof field.options[0].value;
        //   defaultValue = defaultValue.map(v => {
        //     if (v && typeof v === 'object' && 'id' in v) {
        //       return optionType === 'string' ? String(v.id) : Number(v.id);
        //     }
        //     return optionType === 'string' ? String(v) : Number(v);
        //   });
        // }

        if ((field.name as string).toLowerCase() === "domainname") {        
          this.selectedDomainIds.emit(defaultValue);
        }
      }
      controls[field.name] = [defaultValue, validators];
    });

    this.dynamicForm = this.fb.group(controls);

  //   this.fields?.forEach(field => {
  //   if (field.optionSource?.toLowerCase() === 'dependent') {
  //     // Initialize search control if it doesn't exist
  //     if (!this.searchControls[field.name]) {
  //       this.searchControls[field.name] = new FormControl('');
  //     }
      
  //     // Initialize filtered options with empty array
  //     if (!this.filteredDependentOptions[field.name]) {
  //       this.filteredDependentOptions[field.name] = [];
  //     }

  //     // Set up search subscription
  //     this.searchControls[field.name].valueChanges.subscribe(searchText => {
  //       const options = this.dependentOptionsMap[field.name] || [];
  //       if (!searchText) {
  //         this.filteredDependentOptions[field.name] = options;
  //       } else {
  //         const lowerSearch = searchText.toLowerCase();
  //         this.filteredDependentOptions[field.name] = options.filter(opt =>
  //           opt.label.toLowerCase().includes(lowerSearch)
  //         );
  //       }
  //       // Trigger change detection
  //       this.cdr.detectChanges();
  //     });
  //   }
  // });

    this.fields?.forEach(field => {
      if (field.type === 'table') {
        const formArray = this.dynamicForm.get(field.name) as FormArray;
        formArray.valueChanges.subscribe((rows: any[]) => {
          this.formData[field.name] = rows;
        });
      }
    });
    this.dynamicForm.valueChanges.subscribe(val => {
      this.formValueChange.emit(val);
    });

  //   this.fields?.forEach(field => {
  //   if (field.optionSource?.toLowerCase() === 'dependent') {
  //     // Initialize search control for this dependent field
  //     this.searchControls[field.name] = new FormControl('');

  //     // Initially, show all dependent options
  //     this.filteredDependentOptions[field.name] = this.dependentOptionsMap[field.name] || [];

  //     // Listen for search input changes
  //     this.searchControls[field.name].valueChanges.subscribe(searchText => {
  //       const options = this.dependentOptionsMap[field.name] || [];
  //       if (!searchText) {
  //         this.filteredDependentOptions[field.name] = options;
  //       } else {
  //         const lowerSearch = searchText.toLowerCase();
  //         this.filteredDependentOptions[field.name] = options.filter(opt =>
  //           opt.label.toLowerCase().includes(lowerSearch)
  //         );
  //       }
  //     });
  //   }
  // });

    this.loadDependentOptionsForExistingData();
  }

  compareObjects(o1: any, o2: any): boolean {
    if (!o1 || !o2) return o1 === o2;
    
    // Handle case where one is an object and the other is a primitive
    if (typeof o1 === 'object' && typeof o2 !== 'object') {
      return o1.name === o2 || o1.id === o2;
    }
    if (typeof o1 !== 'object' && typeof o2 === 'object') {
      return o1 === o2.name || o1 === o2.id;
    }
    
    // Both are objects
    if (typeof o1 === 'object' && typeof o2 === 'object') {
      return o1.name === o2.name || o1.id === o2.id;
    }
    
    // Both are primitives
    return o1 === o2;
  }
  
  async loadInitialOptions(changed: boolean = false) {
    // console.log("loadInitialOptions");
    const apiUrlMap = new Map<string, any[]>();

    const apiFields = this.fields?.filter(f => f.optionSource?.toLowerCase() === 'api' && f.apiUrl);
    const uniqueApiUrls = [...new Set(apiFields?.map(f => f.apiUrl))];

    for (const apiUrl of uniqueApiUrls) {
      const response = await this.loadApiOptions(apiUrl);
      apiUrlMap.set(apiUrl, response);
    }

    for (const field of this.fields) {
      if (field.optionSource === 'api' && field.apiUrl) {
        if (!changed) {
          field.options = this.getOptionsFromApiResponse(field, apiUrlMap.get(field.apiUrl));
        } else {
          if (field.label.toLowerCase() === 'Domain'.toLowerCase()) {
            field.options = this.getOptionsFromApiResponse(field, apiUrlMap.get(field.apiUrl));

          }
        }
      }
    }
    await this.loadDependentOptionsForExistingData();
  }

  async loadDependentOptionsForExistingData() {
    if (this.fields && this.fields?.length > 0) {
      for (const field of this?.fields) {
        // console.log("field", field);

        if (field.optionSource === 'dependent' && field.parentField) {
          const parentValue = this.formData[field.parentField];
          if (parentValue) {
            await this.getDependentOptions(field, parentValue);
          }
        }
      }
    }
  }

  onCheckboxChange(event: any, fieldName: string, value: string) {
    const control = this.dynamicForm.get(fieldName);
    if (control) {
      let current = control.value || [];
      if (!Array.isArray(current)) current = [];
      if (event.target.checked) {
        if (!current.includes(value)) {
          control.setValue([...current, value]);
        }
      } else {
        control.setValue(current.filter((v: string) => v !== value));
      }
    }
  }

  createArray(n: number): any[] {
    return Array.from({ length: n });
  }

  // getBootstrapCol(width: number): number {
  //   if (!width) return 12;
  //   if (width <= 25) return 3;      // 25%
  //   if (width <= 33) return 4;      // 33%
  //   if (width <= 50) return 6;      // 50%
  //   if (width <= 75) return 9;      // 75%
  //   return 12;                      // 100%
  // }

  getFormValue(fieldName: string): any {
    return this.dynamicForm.get(fieldName)?.value;
  }

  async loadApiOptions(apiUrl): Promise<any[]> {
    // Check cache first
    if (this.apiCache.has(apiUrl)) {
      return this.apiCache.get(apiUrl);
    }

    try {
      // this shoudl be changed in production
      const response = !apiUrl.startsWith('https') ? await this.apiSourceService.loadDynamicApi(apiUrl).toPromise() : await this.http.get(apiUrl).toPromise();
      if (response && Array.isArray(response)) {
        // Cache the result
        this.apiCache.set(apiUrl, response);
        return response;
      }
    } catch (error) {
      console.error(`Error loading API options for ${apiUrl}:`, error);
      return [];
    }
    return [];
  }

  // Dependency helpers
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

  async getDependentOptions(field: any, parentValue: string): Promise<any[]> {
    if (!field.parentField || !field.dependentApiUrl || !parentValue) {
      return [];
    }

    if (this.dependentFieldOptions.has(field.name)) {
      const fieldOptions = this.dependentFieldOptions.get(field.name)!;
      if (fieldOptions.has(parentValue)) {
        return fieldOptions.get(parentValue) || [];
      }
    }

    try {
      const url = field.dependentApiUrl.replace(`{${field.dependentParamName}}`, parentValue);
      const response = await this.apiSourceService.loadDynamicApi(url).toPromise();

      if (response && Array.isArray(response)) {
        const options = response?.map(item => ({
          label: item[field.dependentLabelKey || 'name'],
          value: String(item[field.dependentValueKey || 'id'])
        }));
        field.options = options;

        if (!this.dependentFieldOptions.has(field.name)) {
          this.dependentFieldOptions.set(field.name, new Map());
        }
        this.dependentFieldOptions.get(field.name)!.set(parentValue, options);
        this.dependentOptionsMap[field.name] = options;


        // Filter testing start
      //   this.filteredDependentOptions[field.name] = options;

      //   const searchText = this.searchControls[field.name]?.value;
      // if (searchText) {
      //   const lowerSearch = searchText.toLowerCase();
      //   this.filteredDependentOptions[field.name] = options.filter(opt =>
      //     opt.label.toLowerCase().includes(lowerSearch)
      //   );
      // }

      // fitler testing end

        return options;
      }
    } catch (error) {
      console.error(`Error loading dependent options for ${field.name}:`, error);
      return [];
    }

    return [];
  }

  patchDependentFieldValues() {
    this.fields?.forEach(field => {
      if (
        field.optionSource === 'dependent' &&
        field.parentField &&
        this.formData &&
        this.formData[field.name] !== undefined &&
        field.options &&
        field.options.length > 0
      ) {
        const ctrl = this.dynamicForm.get(field.name);
        const formValue = ctrl?.value;
        const dataValue = String(this.formData[field.name]);
        if (formValue !== dataValue) {
          ctrl?.setValue(dataValue, { emitEvent: false });
        }
      }
    });
  }

  private generateUniqueId(): string {
    return Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }

  async onDomainSelectChange(selectedDomainIds: any, field: any, changed: boolean = true) {
    console.log("Selected domain ids: ", selectedDomainIds);
    
    
    
    if (!Array.isArray(selectedDomainIds)) selectedDomainIds = [selectedDomainIds];
    // console.log("Field: ", field);
    
    // this.fields = this.fields.filter(f =>
    //   f.isDynamicallyCreated !== 'true' && f.isDynamicallyCreated !== true
    // );
    // this.layoutConfig = this.layoutConfig.map(row =>
    //   row.filter(f =>
    //     f.isDynamicallyCreated !== 'true' && f.isDynamicallyCreated !== true
    //   )
    // );

    // console.log("Fields: ", this.fields);
    
    // console.log("LayoutConfig: ", this.layoutConfig);
    

    const type = this.getTypeForField(field);

    const newDomainIds = selectedDomainIds.map(d => d.id);

    if (changed) this.selectedDomainIds.emit(newDomainIds);

    // field.value = selectedDomainIds;

    const existingchilds = new Set();

    for (const parentDomainId of selectedDomainIds) {
      const domainId = parentDomainId.id;
      let children: any[] = await this.apiSourceService.getAllNextFieldAndOption(domainId, type).toPromise();

      
      if (children && children.length > 0) {
        const domainFieldTemplate = { ...field };
        const hierarchyType = children.find(child => child.id === domainId)?.hierarchyType;
        
        const parentOption = field.options.find(opt => opt.value === domainId);
        const parentLabel = parentOption ? parentOption.label : domainId;
        const uniqueLabel = `${this.getChildrenHierarchyType(type)} (${parentLabel})`;
        
        const uniqueName = `${type}_${domainId}`;
        
        existingchilds.add(uniqueName);
        
        if (this.fields.some(f => f.name === uniqueName)) continue;
        
        const uniqueId = this.fields.find(f => f.name === uniqueName)?.id || this.generateUniqueId();
        
        this.fields = this.fields.filter(f => f.name !== uniqueName);

        const newField = {
          ...domainFieldTemplate,
          id: uniqueId,
          name: uniqueName,
          label: uniqueLabel,
          hierarchyType: hierarchyType,
          options: children.map(child => ({
            label: child.name,
            value: child.id,
            isChildAvailable: child.isChildAvailable,
            hierarchyType: child.hierarchyType,
            isActive: !!child.isActive,
          })),
          multiple: true,
          dynamicDomainChild: true,
          parentDomainId: domainId,
          isDynamicallyCreated: true,
          apiUrl: `api/getAllNextFieldAndOption/${type}/${domainId}`,
          apiLabelKey: null,
          apiValueKey: null,
          parentDynamicId: field.id,
          dependentApiUrl: "api/getAllNextFieldAndOption/{type}/{id}",
          dependentLabelKey: "name",
          dependentValueKey: "id",
          dependentParamName: "id",
          value: null
        };


        // this.fields.push(newField);

        const parentIndex = this.fields.findIndex(f => f.id === field.id);
          // console.log("parentIndex: ", parentIndex);
          
          if (parentIndex >= 0) {
            this.fields.splice(parentIndex + 1, 0, newField); // Insert after parent
          } else {
            this.fields.push(newField); // Fallback if parent not found
          }

          // console.log("this.fields after pushing: ", this.fields);
          

        const domainFieldRowIndex = this.layoutConfig.findIndex(row =>
          row.some(f => f.name === field.name)
        );

        if (domainFieldRowIndex >= 0) {
          this.layoutConfig[domainFieldRowIndex].push(newField);
        } else {
          if (!this.layoutConfig[0]) {
            this.layoutConfig[0] = [];
          }
          this.layoutConfig[0].push(newField);
        }
      }
    }
    
    const nonExistingField = this.fields.find(f => f.parentDynamicId == field.id && !existingchilds.has(f.name));
    
    const descendantIds = this.getDescendantIds(nonExistingField?.id) || [];

    descendantIds?.push(nonExistingField);

    // const abcd:any[] = []

    // for(const f of this.fields){
    //   if(descendantIds?.some(d => d?.id == f.id)){
    //     console.log("f: ", f);
    //   }else{
    //     abcd.push(f)
    //   }
    // }

    this.fields = this.fields.filter(f => !descendantIds?.some(d => d?.id == f.id));
    this.layoutConfig = this.layoutConfig.map(row => row.filter(f => !descendantIds?.some(d => d?.id == f.id)));


    this.fields = [...this.fields];
    console.log("This.fields: ", this.fields);
    
    this.layoutConfig = [...this.layoutConfig];
    // this.buildForm();
    this.fieldsUpdated.emit({
      fields: [...this.fields],
      layoutConfig: [...this.layoutConfig]
    });
  }

  // onFileChange(event: any, field:any) {
  onFileChange(field:any) {
    // const files: FileList = event.target.files;
    // if (!files || files.length === 0) return;

    // const fieldName = field.name;
    // const fieldLabel = field.label;
    // const control = this.dynamicForm.get(fieldName);
    // this.isFileUploadModalOpen = true;
    this.fileUploadModalRef = this.modalService.show(this.fileUploadModalTemplate);
    this.selectedFileField = field;

    // Always store as array
    // const fileArray: File[] = Array.from(files);
    // control?.setValue(fileArray);

    // const existingField = this.fields.find(f => f.name === fieldName);

    // if (existingField) {
    //   existingField.value = fileArray;
    // } else {      
    //   const domainFieldTemplate = {...this.fields};
      
    //   const uniqueId = this.fields.find(f => f.label === fieldLabel)?.id || this.generateUniqueId();
      
    //   const newField = {
    //     ...domainFieldTemplate,
    //     id: this.generateUniqueId(),
    //     name: fieldName,
    //     label: fieldLabel,
    //     hierarchyType: null,
    //     options: null,
    //     multiple: true,
    //     dynamicDomainChild: true,
    //     parentDomainId: null,
    //     isDynamicallyCreated: true,
    //     apiUrl: `api/getAllFiles/${uniqueId}`,
    //     apiLabelKey: null,
    //     apiValueKey: null,
    //     parentDynamicId: null,
    //     dependentApiUrl: null,
    //     dependentLabelKey: null,
    //     dependentValueKey: null,
    //     dependentParamName: null,
    //     value: fileArray
    //   };

    //   this.fields.push(newField);
      
    //   const domainFieldRowIndex = this.layoutConfig.findIndex(row =>
    //       row.some(f => f.label === field.label)
    //     );

    //     if (domainFieldRowIndex >= 0) {
    //       this.layoutConfig[domainFieldRowIndex].push(newField);
    //     } else {
    //       if (!this.layoutConfig[0]) {
    //         this.layoutConfig[0] = [];
    //       }
    //       this.layoutConfig[0].push(newField);
    //     }

    //     this.fields = [...this.fields];
    
    //     this.layoutConfig = [...this.layoutConfig];
    //     // this.buildForm();
    //     this.fieldsUpdated.emit({
    //       fields: [...this.fields],
    //       layoutConfig: [...this.layoutConfig]
    //     });

    //   control?.markAsTouched();
    //   control?.updateValueAndValidity();
      
    // }
    // console.log(`Files selected for ${fieldName}:`, this.dynamicForm.value);
  }

  closeUploadModal() {
    this.fileUploadModalRef?.hide();
    this.selectedFileField = null;
  }

  uploadFiles(field:any,files: File[]) {

    const nonExistingFiles: File[] = files.filter(f => !f.name.toLowerCase().startsWith("uploaded") );

    if(nonExistingFiles.length == 0){
      return;
    }

    this.projectInsightService.uploadFiles(nonExistingFiles, this.projectName).subscribe({
      next: (response: any) => {
        console.log('Files uploaded successfully:', response);
        this.selectedFiles = []
        const currentValues = this.dynamicForm?.value;
        
        field.value = field?.value ? [...field.value, ...response] : response;
        console.log("Field Value: ", field);

        this.layoutConfig = this.layoutConfig.map(row => {
          return row.map(f => {
            if (f.id === field.id) {
              return {
                ...f,
                value: field.value
              };
            }
            return f;
          });
        })

        this.fields = this.fields.map(f => {
          if (f.id === field.id) {
            return {
              ...f,
              value: field.value
            };
          }
          return f;
        })

        console.log("Fields: ", this.fields);
        console.log("LayoutConfig: ", this.layoutConfig);

        this.dynamicForm.get(field.name)?.setValue(field.value);

        this.formValueChange.emit(this.dynamicForm.value);        
        
        this.fieldsUpdated.emit({
          fields: [...this.fields],
          layoutConfig: [...this.layoutConfig]
        });
        
      },
      error: (error: any) => {
        console.error('Error uploading files:', error);
      }
    });
    console.log("Fields Value: ", this.fields);
  }

  bulidPath(field:any){
    return this.baseUrl + field.value
  }

  isParentActive(parentId: string, fieldLabel: string): boolean {
    // Normalize the label (extract what's inside parentheses if present)
    if (fieldLabel.includes("(")) {
      fieldLabel = fieldLabel.split("(")[1]?.replace(")", "").trim();
    }

    // Find the parent field
    const parentField = this.fields.find(f => f.id === parentId);
    if (!parentField) {
      return false; // parent not found
    }

    // Check if this parent has the given option active
    const isActive = parentField.options?.some(opt => opt.label === fieldLabel && opt.isActive) || false;
    if (!isActive) {
      return false; // stop immediately if inactive
    }

    // Recursively check parent's parent (if exists)
    if (parentField.parentDynamicId) {
      return this.isParentActive(parentField.parentDynamicId, parentField.label);
    }

    // No more parents, all checked are active
    return true;
  }

  private getTypeForField(field: any): string {
    const parentField = this.fields.find(f => f.id === field.parentDynamicId);
    if (parentField?.options) {
      const selectedOption = parentField.options.find(opt => field.label.includes(opt.label));
      return (selectedOption?.hierarchyType || '').toUpperCase();
    }
    
    return (field.label || '').toUpperCase(); 
  }

  private getDescendantIds(parentId) {
    if(!parentId) return;
      console.log("parentId: ", parentId);
    
      const directChildren = this.fields.filter(f => f.parentDynamicId === parentId);
      console.log("directChildren: ", directChildren);
    
      let allDescendants = [...directChildren];

      directChildren.forEach(child => {
        allDescendants = allDescendants.concat(this.getDescendantIds(child.id));
      });

      return allDescendants;
    };

  async onDomainChildSelectChange(selectedChildIds: any[], field: any) {
    const currentValues = this.dynamicForm?.value || {};
    
    // selectedChildIds = selectedChildIds.filter((childId: any) => {
    //   const option = field.options.find((opt: any) => opt.value === childId.id);
    //   console.log("option: ", option);
      
    //   return option?.isActive === true;
    // });
    

    if (selectedChildIds.length == 0) {

      const allDescendants = this.getDescendantIds(field.id);

      const descendantIds = allDescendants.map(d => d.id);

      // Remove all descendants from fields and layoutConfig
      this.fields = this.fields.filter(f =>
        f.parentDynamicId != field.id && !descendantIds.includes(f.id)
      );

      this.layoutConfig = this.layoutConfig.map(row =>
        row.filter(f =>
          f.parentDynamicId != field.id && !descendantIds.includes(f.id)
        )
      );

    }    

    const existingchilds = new Set();
    for (const domainId of selectedChildIds || []) {
      const childId = domainId.id;
      if (!childId) continue;
      const selectedOption = field.options.find((opt: any) => opt.value === childId);
      if (selectedOption && selectedOption.isChildAvailable) {
        let type = selectedOption.hierarchyType;

        console.log("Type for domain select change: ", type);
        

        const fieldName = `${type}_${childId}`;
        console.log("Unique name inside domain select change: ", fieldName);
          existingchilds.add(fieldName);

        const uniqueId = this.fields.find(f => f.name === fieldName)?.id || this.generateUniqueId();

        if (this.fields.some(f => f.name === fieldName ) ) continue;

        const option = field.options.find((opt: any) => opt.value === childId);
        if(!option?.isActive) continue;


        const children: any = await this.apiSourceService.getAllNextFieldAndOption(childId, type).toPromise();        
        
        if (children && children.length > 0) {
          const domainFieldTemplate = { ...field }; 
          const newLabel = this.getChildrenHierarchyType(type);
          const parentOption = field.options.find(opt => opt.value === childId);
          const parentLabel = parentOption ? parentOption.label : childId;

          const newField = {
            ...domainFieldTemplate,
            id: uniqueId,
            name: fieldName,
            label: `${newLabel} (${parentLabel})`,
            hierarchyType: newLabel,
            options: children.map(child => ({
              label: child.name,
              value: child.id,
              isChildAvailable : child.isChildAvailable,
              hierarchyType: child.hierarchyType,
              isActive: !!child.isActive,
            })),
            multiple: true,
            dynamicDomainChild: true,
            parentDomainId: childId,
            isDynamicallyCreated: true,
            apiUrl: `api/getAllNextFieldAndOption/${type}/${childId}`,
            apiLabelKey: null,
            apiValueKey: null,
            dependentApiUrl: "api/getAllNextFieldAndOption/{type}/{id}",
            dependentLabelKey: "name",
            parentDynamicId: field.id,
            dependentValueKey: "id",
            dependentParamName: "id"
          };

          const parentIndex = this.fields.findIndex(f => f.id === field.id);
          console.log("New field: ", newField);
          
          
          if (parentIndex >= 0) {
            this.fields.splice(parentIndex + 1, 0, newField); // Insert after parent
          } else {
            this.fields.push(newField); // Fallback if parent not found
          }

          console.log("this.fields after pushing: ", this.fields);

          // Find the row where the parent field is located and add the new field there
          const parentFieldRowIndex = this.layoutConfig.findIndex(row =>
            row.some(f => f.name === field.name)
          );

          if (parentFieldRowIndex >= 0) {
            this.layoutConfig[parentFieldRowIndex].push(newField);
          } else {
            if (!this.layoutConfig[0]) {
              this.layoutConfig[0] = [];
            }
            this.layoutConfig[0].push(newField);
          }
        }
      }
    }

    console.log("existingchilds: ", existingchilds, );
    console.log("The exisitng fields are: ", this.fields);
    

    const nonExistingField = this.fields.find(f => f.parentDynamicId == field.id && !existingchilds.has(f.name));

    const descendantIds = this.getDescendantIds(nonExistingField?.id) || [];

    descendantIds?.push(nonExistingField);

    // const abcd:any[] = []

    // for(const f of this.fields){
    //   if(descendantIds?.some(d => d?.id == f.id)){
    //     console.log("f: ", f);
    //   }else{
    //     abcd.push(f)
    //   }
    // }

    this.fields = this.fields.filter(f => !descendantIds?.some(d => d?.id == f.id));
    this.layoutConfig = this.layoutConfig.map(row => row.filter(f => !descendantIds?.some(d => d?.id == f.id)));
    // this.buildForm();
    // 4. Restore any previous values
    // if (this.dynamicForm && currentValues) {
    //     this.dynamicForm.patchValue(currentValues, { emitEvent: false });
    // }

    if (this.dynamicForm) {
      const valuesToRestore = {};
      Object.keys(currentValues).forEach(key => {
        if (this.dynamicForm.contains(key)) {
          valuesToRestore[key] = currentValues[key];
        }
      });
      this.dynamicForm.patchValue(valuesToRestore, { emitEvent: false });
    }

    this.formValueChange.emit(this.dynamicForm.value);

    // Emit updated fields to parent
    this.fieldsUpdated.emit({
      fields: [...this.fields],
      layoutConfig: [...this.layoutConfig]
    });
  }

  getFileValue(field:any): any {
    const value = field?.value;
    if(!Array.isArray(value)) return;
    return value?.map(fileName => ({ name: fileName }));
  }

  getTitleCaseLabel(label: string): string {
    if (!label) return '';
    return label
      .split(/[\s_-]+/)
      .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join(' ');
  }


  async onSelectChange(event: any, field: any) {

    if(this.viewMode === 'View'){
      return;
    }
    
    if (this.fields.some(f => f.parentField === field.name)) {
      for (const depField of this.fields.filter(f => f.parentField === field.name)) {
        if (depField.optionSource === 'dependent') {
          if (this.dependentFieldOptions.has(depField.name)) {
            this.dependentFieldOptions.get(depField.name)!.clear();
          }

          this.dynamicForm.get(depField.name)?.setValue(depField.multiple ? [] : '');

          if (this.isParentMultiSelect(depField)) {
            const parentValues = event.value || [];
            for (const parentValue of parentValues) {
              if (parentValue) {
                const options = await this.getDependentOptions(depField, parentValue);
                this.dependentOptionsMap[depField.name] = options;
              }
            }
          } else {
            const parentValue = event.value;
            if (parentValue) {
              const options = await this.getDependentOptions(depField, parentValue);
              this.dependentOptionsMap[depField.name] = options;
            }
          }
        }
      }
    } else {
      let value = event.value;
      // if(!Array.isArray(value)) value = [value];
      field.value = value;
      this.dynamicForm.get(field.name)?.setValue(value);
      this.formValueChange.emit(this.dynamicForm.value);
    }

    console.log("this.fields: ", this.fields);
    
  }

  fileValueChange(field:any, fileValue:string[]){
    this.fields = this.fields.map(f => {
      if (f.id === field.id) {
        f.value = fileValue;
        return f;
      }
      return f;
    })

    this.layoutConfig = this.layoutConfig.map(row => {
      return row.map(f => {
        if (f.id === field.id) {
          f.value = fileValue;
          return f;
        }
        return f;
      })
    })
    this.dynamicForm.get(field.name)?.setValue(fileValue);
    this.formValueChange.emit(this.dynamicForm.value);
  }

  onSubmit() {
    if (this.dynamicForm.valid) {
      this.formSubmit.emit(this.dynamicForm.value);
    } else {
      this.markFormGroupTouched(this.dynamicForm);
    }
  }

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

  getFieldStyle(field: any): { [key: string]: string } {
    const width = field.width || 33;
    return {
      flex: `0 0 ${width}%`,
      minWidth: '220px',
    };
  }

  getType(type:string){
    switch (type.toLowerCase().trim()) {
      case 'domain': return 'D';
      case 'subdomain': return 'SD';
      case 'service': return 'S';
      case 'subservice': return 'SS';
    }
  }

  getChildrenHierarchyType(type: string) {
    const capitalizeWords = (str: string) =>
      str
        .toLowerCase()
        .split("/")
        .map(word => word.charAt(0).toUpperCase() + word.slice(1))
        .join("/");

    switch (type.toLowerCase().trim()) {
      case 'domain':
        return capitalizeWords("subdomain/service");
      case 'subdomain':
        return capitalizeWords("subdomain/service");
      default:
        return capitalizeWords("subservice");
    }
  }


  getOptionsFromApiResponse(field: any, response: any) {
    const options = response?.map(item => ({
      label: item[field.apiLabelKey || 'name'] || item['label'],
      value: item[field.apiValueKey || 'id'] || item['value'],
      isChildAvailable: item['isChildAvailable'],
      hierarchyType: item['hierarchyType'],
      isActive: item['isActive']
    }));
    return options;
  }

  drop(event: CdkDragDrop<FormField[]>, rowIndex: number): void {
    const targetRow = this.layoutConfig[rowIndex]
    if (event.previousContainer !== event.container && targetRow.length >= 4) {
      return // ignore drop
    }

    if (event.previousContainer === event.container) {
      moveItemInArray(targetRow, event.previousIndex, event.currentIndex)
    } else {
      transferArrayItem(event.previousContainer.data, targetRow, event.previousIndex, event.currentIndex)
    }

    this.updateFieldPositions(rowIndex)
    this.normalizeRow(rowIndex)

    if (event.previousContainer !== event.container) {
      const prevIndex = +event.previousContainer.id.split("-")[1]
      this.updateFieldPositions(prevIndex)
      this.normalizeRow(prevIndex)
    }

    this.cleanupRows();
    this.sortAllRows();
    this.layoutConfig = this.layoutConfig.map((row) => [...row])
    this.fieldsUpdated.emit({
      fields: [...this.fields],
      layoutConfig: [...this.layoutConfig],
    })
  }

  onResizeStart(event: any, field: FormField): void {
    const dragElement = event?.target?.closest("[cdkDrag]")
    if (dragElement) {
      dragElement.setAttribute("cdkDragDisabled", "true")
    }

    this.isResizing[field.name] = true
    const rect = event.rectangle
    if (rect) {
      this.resizeInfo[field.name] = {
        width: Math.round(rect.width),
        height: Math.round(rect.height || 0),
        cols: this.getBootstrapCol(field.width),
      }
    }
  }

  onResize(event: ResizeEvent, field: FormField): void {
    if (!event?.rectangle?.width) return
    const parentElement = (event as any).element?.parentElement as HTMLElement
    const parentWidth = parentElement?.offsetWidth || 1200
    // Convert px → bootstrap cols
    const widthPercent = (event.rectangle.width / parentWidth) * 100
    let col = Math.round((widthPercent / 100) * 12)
    // Clamp between col-md-3 and col-md-12
    col = Math.max(3, Math.min(12, col))
    // Live preview
    field.tempCol = col
    this.resizeInfo[field.name] = {
      width: Math.round((col / 12) * parentWidth),
      height: Math.round(event.rectangle.height || 0),
      cols: col,
    }
  }

  onResizeEnd(event: ResizeEvent, field: FormField, rowIndex: number): void {
    if (event?.rectangle?.width) {
      const parentElement = (event as any).element?.parentElement as HTMLElement
      const parentWidth = parentElement?.offsetWidth || 1200
      let newCols = Math.round((event.rectangle.width / parentWidth) * 12)
      newCols = Math.max(3, Math.min(12, newCols))
      field.width = (newCols / 12) * 100
    }

    this.normalizeRow(rowIndex)
    this.fieldsUpdated.emit({
      fields: [...this.fields],
      layoutConfig: [...this.layoutConfig],
    })
  }

  onResizeEndComplete(event: ResizeEvent, field: FormField, rowIndex: number): void {
    this.onResizeEnd(event, field, rowIndex)
    this.isResizing[field.name] = false

    // Ensure row consistency
    const row = this.layoutConfig[rowIndex]
    if (row) {
      const totalCols = row.reduce((sum, f) => sum + this.getBootstrapCol(f.width), 0)

      // Case 1: Field is full-width → isolate in its own row
      if (this.getBootstrapCol(field.width) === 12 && row.length > 1) {
        const fieldIndex = row.findIndex((f) => f.name === field.name)
        row.splice(fieldIndex, 1)
        field.rowPosition = rowIndex + 1
        field.index = 0
        this.layoutConfig.splice(rowIndex + 1, 0, [field])
        this.updateFieldPositions(rowIndex)
      }

      // Case 2: Overflow (>12 cols) → move extras to next row
      else if (totalCols > 12) {
        let currentCols = 0
        const newRow: FormField[] = []
        for (let i = row.length - 1; i >= 0; i--) {
          currentCols += this.getBootstrapCol(row[i].width)
          if (currentCols > 12) {
            const movedField = row.splice(i, 1)[0]
            movedField.rowPosition = rowIndex + 1
            newRow.unshift(movedField)
          }
        }
        if (newRow.length) {
          newRow.forEach((field, index) => {
            field.index = index
          })
          this.layoutConfig.splice(rowIndex + 1, 0, newRow)
          this.updateFieldPositions(rowIndex)
        }
      }
    }

    setTimeout(() => {
      const dragElement = document.querySelector(`[data-field-name="${field.name}"]`)
      if (dragElement && this.isDragEnabled) {
        dragElement.removeAttribute("cdkDragDisabled")
      }
      setTimeout(() => delete this.resizeInfo[field.name], 1000)
    }, 100)

    this.cleanupRows();
    this.sortAllRows();
    this.fieldsUpdated.emit({
      fields: [...this.fields],
      layoutConfig: [...this.layoutConfig],
    })
  }

  normalizeRow(rowIndex: number): void {
    const row = this.layoutConfig[rowIndex]
    if (!row) return

    // If a field is col-md-12 → isolate it
    for (let i = 0; i < row.length; i++) {
      if (this.getBootstrapCol(row[i].width) === 12 && row.length > 1) {
        const [field] = row.splice(i, 1)
        field.rowPosition = rowIndex + 1
        field.index = 0
        this.layoutConfig.splice(rowIndex + 1, 0, [field])
        this.updateFieldPositions(rowIndex)
        return this.normalizeRow(rowIndex) // recheck current row
      }
    }

    // If row exceeds 12 cols → move extras
    let totalCols = 0
    const newRow: FormField[] = []
    for (let i = 0; i < row.length; i++) {
      const col = this.getBootstrapCol(row[i].width)
      if (totalCols + col <= 12) {
        totalCols += col
      } else {
        const movedField = row.splice(i, 1)[0]
        movedField.rowPosition = rowIndex + 1
        newRow.push(movedField)
        i--
      }
    }

    if (newRow.length) {
      newRow.forEach((field, index) => {
        field.index = index
      })
      this.layoutConfig.splice(rowIndex + 1, 0, newRow)
      this.updateFieldPositions(rowIndex)
      this.normalizeRow(rowIndex + 1) // normalize next row too
    }
  }

  getBootstrapCol(widthPercent: number): number {
    const col = Math.round((widthPercent / 100) * 12)
    return Math.min(12, Math.max(3, col)) // col-3 … col-12
  }

  adjustCols(field: FormField, delta: number): void {
    const currentCol = this.getBootstrapCol(field.width)
    let newCol = currentCol + delta
    // Clamp between col-md-3 and col-md-12
    newCol = Math.max(3, Math.min(12, newCol))
    field.width = (newCol / 12) * 100
    const parentElement = document.querySelector(".row") as HTMLElement
    const parentWidth = parentElement ? parentElement.offsetWidth : 1200
    this.resizeInfo[field.name] = {
      width: Math.round((field.width / 100) * parentWidth),
      height: field.height || 0,
      cols: newCol,
    }
  }

  getConnectedDropLists(currentRowIndex: number): string[] {
    return this.layoutConfig.map((_, i) => "row-" + i).filter((id) => id !== "row-" + currentRowIndex)
  }

  cleanupRows(): void {
    // Remove empty rows
    this.layoutConfig = this.layoutConfig.filter((row) => row && row.length > 0)

    // Reindex all rows and fields for consistency
    this.layoutConfig.forEach((row, rowIndex) => {
      row.forEach((field, fieldIndex) => {
        field.rowPosition = rowIndex
        field.index = fieldIndex
      })
    })
  }

  updateFieldPositions(rowIndex: number): void {
    const row = this.layoutConfig[rowIndex]
    if (!row) return

    // Update rowPosition and index for proper alignment
    row.forEach((field, index) => {
      field.rowPosition = rowIndex
      field.index = index
    })
  }

  trackByRowIndex(index: number, row: FormField[]): number {
    return index
  }

  trackByField(index: number, field: FormField): string {
    return field.name
  }

  sortAllRows(): void {
    this.layoutConfig = this.layoutConfig?.map((row) =>
      [...row].sort((a, b) => {
        // First by rowPosition
        const posA = a.rowPosition ?? 0
        const posB = b.rowPosition ?? 0
        if (posA !== posB) return posA - posB

        // Then by index
        const indexA = a.index ?? 0
        const indexB = b.index ?? 0
        if (indexA !== indexB) return indexA - indexB

        // Finally by width
        const colA = this.getBootstrapCol(a.width)
        const colB = this.getBootstrapCol(b.width)
        return colA - colB
      })
    )
  }

}
