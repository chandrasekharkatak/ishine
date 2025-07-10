import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators, FormArray } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

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

  dynamicForm: FormGroup;
  dependentFieldOptions: Map<string, Map<string, any[]>> = new Map();

  constructor(private fb: FormBuilder, private http: HttpClient) {}

  ngOnInit() {
    console.log(this.fields, " : fields ==================");
    console.log(this.layoutConfig, " : layoutConfig ==================");

    this.buildForm();
    this.loadInitialOptions();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.fields || changes.formData) {
      this.buildForm();
      this.loadInitialOptions();
    }
  }

  buildForm() {
    console.log('Building form with fields:', this.fields);
    console.log('Form data:', this.formData);
    
    const controls: any = {};
    this.fields.forEach(field => {
      const validators = [];
      if (field.required) validators.push(Validators.required);
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
    
    console.log('Form group created:', this.dynamicForm);
    console.log('Form controls:', Object.keys(this.dynamicForm.controls));
    
    this.dynamicForm.valueChanges.subscribe(val => {
      this.formValueChange.emit(val);
    });
  }

  // Load options for API-driven fields
  async loadInitialOptions() {
    for (const field of this.fields) {
      if (field.optionSource === 'api' && field.apiUrl) {
        field.options = await this.loadApiOptions(field);
      }
    }
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
    try {
      const response = await this.http.get<any[]>(field.apiUrl).toPromise();
      if (response && Array.isArray(response)) {
        return response.map(item => ({
          label: item[field.apiLabelKey || 'name'] || item['label'],
          value: item[field.apiValueKey || 'id'] || item['value']
        }));
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

  getDependentOptions(field: any, parentValue: string): any[] {
    if (!this.dependentFieldOptions.has(field.name)) {
      return [];
    }
    const fieldOptions = this.dependentFieldOptions.get(field.name)!;
    return fieldOptions.get(parentValue) || [];
  }

  async onSelectChange(event: any, field: any) {
    // For dependent fields, update options
    if (this.fields.some(f => f.parentField === field.name)) {
      for (const depField of this.fields.filter(f => f.parentField === field.name)) {
        if (depField.optionSource === 'dependent') {
          if (this.isParentMultiSelect(depField)) {
            // For each selected parent value, load options
            const parentValues = event.value;
            for (const parentValue of parentValues) {
              await this.loadDependentOptions(depField, parentValue);
            }
          } else {
            await this.loadDependentOptions(depField, event.value);
          }
        }
      }
    }
  }

  async loadDependentOptions(field: any, parentValue: string): Promise<any[]> {
    if (!field.parentField || !field.dependentApiUrl || !parentValue) return [];
    try {
      const url = field.dependentApiUrl.replace('{parentValue}', parentValue);
      const response = await this.http.get<any[]>(url).toPromise();
      if (response && Array.isArray(response)) {
        const options = response.map(item => ({
          label: item[field.dependentLabelKey || 'name'],
          value: item[field.dependentValueKey || 'id']
        }));
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
