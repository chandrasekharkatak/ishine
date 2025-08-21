import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators, UntypedFormArray, UntypedFormControl } from '@angular/forms';
import { ApiSourceService } from 'src/app/services/api-source.service';
import { KnowledgeHubService } from 'src/app/services/KnowledgeHub.service';
import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { ResizeEvent } from 'angular-resizable-element';
import { FormField } from 'src/app/models/formField';

@Component({
  selector: 'app-form-renderer',
  templateUrl: './form-renderer.component.html',
  styleUrls: ['./form-renderer.component.css','form-renderer.component.scss']
})
export class FormRendererComponent implements OnInit, OnChanges {

  @Input() viewMode!: any;
  @Input() fields: any[] = [];
  @Input() layoutConfig: any[][] = [];
  @Input() formData: any = {};
  @Input() query: string = "";

  @Output() formValueChange = new EventEmitter<any>();
  @Output() formSubmit = new EventEmitter<any>();
  @Output() fieldsUpdated = new EventEmitter<{ fields: any[], layoutConfig: any[][] }>();
  @Output() selectedDomainIds = new EventEmitter<any[]>();

  isLoading = true;
  isDragEnabled = true;
  isResizeEnabled = true;

  resizeInfo: { [fieldName: string]: { width: number; height: number; cols: number } } = {}
  isResizing: { [fieldName: string]: boolean } = {}

  dynamicForm: UntypedFormGroup;
  dependentFieldOptions: Map<string, Map<string, any[]>> = new Map();
  apiCache: Map<string, any[]> = new Map<string, any[]>();
  dependentOptionsMap: { [fieldName: string]: any[] } = {};

  constructor(private fb: UntypedFormBuilder, private apiSourceService: ApiSourceService,private knowledgeHubService: KnowledgeHubService) { }

  async ngOnInit() {
    this.isLoading = true;
    await this.prepareApiOptions();
    this.buildForm();
    this.isLoading = false;

    // console.log(this.fields, " : fields in form renderer");
    // console.log(this.layoutConfig, " : layoutconfig in form renderer");
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
        const rowsArray = new UntypedFormArray([]);
        for (let i = 0; i < field.tableConfig.rows; i++) {
          const rowGroup = {};
          field.tableConfig.columns.forEach(col => {
            rowGroup[col.name] = new UntypedFormControl(this.formData[field.name][i][col.name] || '');
          });
          rowsArray.push(new UntypedFormGroup(rowGroup));
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

        console.log("Processed default value for", field.name, ":", defaultValue);

        if ((field.name as string).toLowerCase() === "domain") {
          this.selectedDomainIds.emit(defaultValue);
        }
      }
      controls[field.name] = [defaultValue, validators];
    });

    this.dynamicForm = this.fb.group(controls);

    this.fields?.forEach(field => {
      if (field.type === 'table') {
        const formArray = this.dynamicForm.get(field.name) as UntypedFormArray;
        formArray.valueChanges.subscribe((rows: any[]) => {
          this.formData[field.name] = rows;
        });
      }
    });
    this.dynamicForm.valueChanges.subscribe(val => {
      this.formValueChange.emit(val);
    });
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
      const response = await this.apiSourceService.loadDynamicApi(apiUrl).toPromise();
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
      const children: any = await this.apiSourceService.getAllNextFieldAndOption(domainId, type).toPromise();

      console.log("Children in domain: ", children);
      

      if (children && children.length > 0) {
        const domainFieldTemplate = { ...field };
        const hierarchyType = children.find(child => child.id === domainId)?.hierarchyType;

        const parentOption = field.options.find(opt => opt.value === domainId);
        const parentLabel = parentOption ? parentOption.label : domainId;
        const uniqueLabel = `${type} (${parentLabel})`;

        const uniqueName = `${type}_${domainId}`;
        console.log("Unique name inside domain select change: ", uniqueName);
        

        
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
            hierarchyType: child.hierarchyType
          })),
          multiple: true,
          dynamicDomainChild: true,
          parentDomainId: domainId,
          isDynamicallyCreated: true,
          apiUrl: null,
          apiLabelKey: null,
          apiValueKey: null,
          parentDynamicId: field.id,
          dependentApiUrl: "api/getAllNextFieldAndOption/{type}/{id}",
          dependentLabelKey: "name",
          dependentValueKey: "id",
          dependentParamName: "id",
          value: null
        };

        // console.log("newField: ", newField); 

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
    this.layoutConfig = [...this.layoutConfig];
    // this.buildForm();
    this.fieldsUpdated.emit({
      fields: [...this.fields],
      layoutConfig: [...this.layoutConfig]
    });
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
    console.log("selectedChildIds: ", selectedChildIds);
    
    console.log("the fields before is: ", this.fields);

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

        if (this.fields.some(f => f.name === fieldName)) continue;


        const children: any = await this.apiSourceService.getAllNextFieldAndOption(childId, type).toPromise();        

        if (children && children.length > 0) {
          const domainFieldTemplate = { ...field }; 
          const newLabel = type;
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
              hierarchyType: child.hierarchyType
            })),
            multiple: true,
            dynamicDomainChild: true,
            parentDomainId: childId,
            isDynamicallyCreated: true,
            apiUrl: null,
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
    // console.log("onSelectChange called");
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
    }
  }

  onSubmit() {
    if (this.dynamicForm.valid) {
      this.formSubmit.emit(this.dynamicForm.value);
    } else {
      this.markFormGroupTouched(this.dynamicForm);
    }
  }

  markFormGroupTouched(formGroup: UntypedFormGroup) {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      if (control instanceof UntypedFormGroup) {
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

  getOptionsFromApiResponse(field: any, response: any) {
    const options = response?.map(item => ({
      label: item[field.apiLabelKey || 'name'] || item['label'],
      value: item[field.apiValueKey || 'id'] || item['value'],
      isChildAvailable: item['isChildAvailable'],
      hierarchyType: item['hierarchyType']
    }));
    return options;
  }
  drop(event: CdkDragDrop<FormField[]>, rowIndex: number): void {
    if (event.previousContainer === event.container) {
      moveItemInArray(this.layoutConfig[rowIndex], event.previousIndex, event.currentIndex)
    } else {
      transferArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex)
    }
  }

  onResizeEnd(event: ResizeEvent, field: FormField): void {
    if (!event.rectangle.width) return

    const parentElement = (event.rectangle as any).parentElement || (document.querySelector(".row") as HTMLElement)
    let parentWidth = event.rectangle.parentWidth

    if (!parentWidth && parentElement) {
      parentWidth = parentElement.offsetWidth
    }

    if (!parentWidth) {
      parentWidth = 1200
    }

    const widthPercent = (event.rectangle.width / parentWidth) * 100
    let col = Math.round((widthPercent / 100) * 12)
    col = Math.max(1, Math.min(12, col))
    field.width = (col / 12) * 100

    if (event.rectangle.height) {
      const step = 40
      field.height = Math.round(event.rectangle.height / step) * step
    }

    this.resizeInfo[field.name] = {
      width: Math.round(event.rectangle.width),
      height: Math.round(event.rectangle.height || 0),
      cols: col,
    }
  }

  onResizeStart(event: any, field: FormField): void {
    const dragElement = event.target.closest("[cdkDrag]")
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
    if (!event.rectangle.width) return

    const parentElement = (event.rectangle as any).parentElement || (document.querySelector(".row") as HTMLElement)
    let parentWidth = event.rectangle.parentWidth

    if (!parentWidth && parentElement) {
      parentWidth = parentElement.offsetWidth
    }

    if (!parentWidth) {
      parentWidth = 1200
    }

    const widthPercent = (event.rectangle.width / parentWidth) * 100
    let col = Math.round((widthPercent / 100) * 12)
    col = Math.max(1, Math.min(12, col))

    this.resizeInfo[field.name] = {
      width: Math.round(event.rectangle.width),
      height: Math.round(event.rectangle.height || 0),
      cols: col,
    }
  }

  onResizeEndComplete(event: ResizeEvent, field: FormField): void {
    this.onResizeEnd(event, field)

    this.isResizing[field.name] = false

    setTimeout(() => {
      const dragElement = document.querySelector(`[data-field-name="${field.name}"]`)
      if (dragElement && this.isDragEnabled) {
        dragElement.removeAttribute("cdkDragDisabled")
      }

      setTimeout(() => {
        delete this.resizeInfo[field.name]
      }, 1000)
    }, 100)
  }

  getBootstrapCol(widthPercent: number): number {
    return Math.max(1, Math.min(12, Math.round((widthPercent / 100) * 12))) || 3
  }

}
