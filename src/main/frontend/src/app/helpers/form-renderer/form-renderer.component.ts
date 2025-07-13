import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormArray, FormControl } from '@angular/forms';
import { ApiSourceService } from 'src/app/services/api-source.service';

@Component({
  selector: 'app-form-renderer',
  templateUrl: './form-renderer.component.html',
  styleUrls: ['./form-renderer.component.css']
})
export class FormRendererComponent implements OnInit, OnChanges {
  @Input() fields: any[] = [];
  @Input() layoutConfig: any[][] = [];
  @Input() formData: any = {};
  @Output() formValueChange = new EventEmitter<any>();
  @Output() formSubmit = new EventEmitter<any>();

  isLoading = true;

  dynamicForm: FormGroup;
  dependentFieldOptions: Map<string, Map<string, any[]>> = new Map();
  private apiCache = new Map<string, any[]>();
  dependentOptionsMap: { [fieldName: string]: any[] } = {};

  constructor(private fb: FormBuilder, private apiSourceService: ApiSourceService) {}

  async ngOnInit() {
    this.isLoading = true;
    await this.prepareApiOptions();
    this.buildForm();
    this.isLoading = false;

    console.log(this.fields, " : fields in form renderer");
    console.log(this.layoutConfig, " : layoutconfig in form renderer");
  }

  // async ngOnChanges(changes: SimpleChanges) {
  //   if (
  //     (changes.fields && changes.fields.currentValue !== changes.fields.previousValue) ||
  //     (changes.formData && changes.formData.currentValue !== changes.formData.previousValue)
  //   ) {
  //     this.isLoading = true;
  //     await this.prepareApiOptions();
  //     this.buildForm();
  //     this.isLoading = false;
  //   }
  // }

  async ngOnChanges(changes: SimpleChanges) {
    if (
      (changes.fields && changes.fields.currentValue !== changes.fields.previousValue) ||
      (changes.formData && changes.formData.currentValue !== changes.formData.previousValue)
    ) {
      await this.prepareApiOptions();
      this.buildForm();
    }
  }

  // async ngOnChanges(changes: SimpleChanges) {
  //   if (changes.fields || changes.formData) {
  //     await this.prepareApiOptions();
  //     this.buildForm();
  //   }
  // }

  getTableRows(field: any): any[] {
    return Array.from({ length: field.tableConfig.rows });
  }

  async prepareApiOptions() {
    const apiPromises = this.fields.map(async field => {
      if (field.optionSource === 'api' && field.apiUrl) {
        field.options = await this.loadApiOptions(field);
      }
      if (
        field.optionSource === 'dependent' &&
        field.parentField &&
        this.formData &&
        this.formData[field.parentField]
      ) {
        const parentValue = this.formData[field.parentField];
        field.options = await this.getDependentOptions(field, parentValue);
      }
    });
    await Promise.all(apiPromises);
  }

  buildForm() {
    console.log('Building form with fields:', this.fields);
    console.log('Form data:', this.formData);
    
    const controls: any = {};
    this.fields.forEach(field => {
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
        
        console.log(`Created FormArray for ${field.name}:`, controls[field.name]);
        return;
      }
  
      let defaultValue =
        this.formData[field.name] !== undefined ? this.formData[field.name] :
        field.value !== undefined ? field.value :
        field.defaultValue !== undefined ? field.defaultValue : '';
  
      if (field.type === 'select' && field.multiple) {
        defaultValue = Array.isArray(defaultValue) ? defaultValue : [];
      }
  
      controls[field.name] = [defaultValue, validators];
      
      console.log(`Created control for ${field.name}:`, controls[field.name]);
    });
    
    this.dynamicForm = this.fb.group(controls);
    
    this.fields.forEach(field => {
      if (field.type === 'table') {
        const formArray = this.dynamicForm.get(field.name) as FormArray;
        formArray.valueChanges.subscribe((rows: any[]) => {
          this.formData[field.name] = rows;
          console.log(`Table ${field.name} updated:`, rows);
        });
      }
    });
    
    console.log('Form group created:', this.dynamicForm);
    console.log('Form controls:', Object.keys(this.dynamicForm.controls));
    
    this.dynamicForm.valueChanges.subscribe(val => {
      this.formValueChange.emit(val);
    });
  
    this.loadDependentOptionsForExistingData();
  }
  
  async loadInitialOptions() {
    for (const field of this.fields) {
      if (field.optionSource === 'api' && field.apiUrl) {
        field.options = await this.loadApiOptions(field);
      }
    }
    await this.loadDependentOptionsForExistingData();
  }

  async loadDependentOptionsForExistingData() {
    for (const field of this.fields) {
      if (field.optionSource === 'dependent' && field.parentField) {
        const parentValue = this.formData[field.parentField];
        if (parentValue) {
          await this.getDependentOptions(field, parentValue);
        }
      }
    }
  }

  createArray(n: number): any[] {
    return Array.from({ length: n });
  }

  getBootstrapCol(width: number): number {
    if (!width) return 12;
    if (width <= 25) return 3;      // 25%
    if (width <= 33) return 4;      // 33%
    if (width <= 50) return 6;      // 50%
    if (width <= 75) return 9;      // 75%
    return 12;                      // 100%
  }

  getFormValue(fieldName: string): any {
    return this.dynamicForm.get(fieldName)?.value;
  }

  async loadApiOptions(field: any): Promise<any[]> {
    // Check cache first
    if (this.apiCache.has(field.apiUrl)) {
      return this.apiCache.get(field.apiUrl);
    }
  
    try {
      const response = await this.apiSourceService.loadDynamicApi(field.apiUrl).toPromise();
      if (response && Array.isArray(response)) {
        const options = response.map(item => ({
          label: item[field.apiLabelKey || 'name'] || item['label'],
          value: item[field.apiValueKey || 'id'] || item['value']
        }));
        
        // Cache the result
        this.apiCache.set(field.apiUrl, options);
        return options;
      }
    } catch (error) {
      console.error(`Error loading API options for ${field.name}:`, error);
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
        const options = response.map(item => ({
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
    this.fields.forEach(field => {
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

  async onSelectChange(event: any, field: any) {
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

  getFieldStyle(field: any): {[key: string]: string} {
    const width = field.width || 33;
    return {
      flex: `0 0 ${width}%`,
      minWidth: '220px',
    };
  }
}
