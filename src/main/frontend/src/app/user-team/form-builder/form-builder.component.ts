import { CdkDragDrop, moveItemInArray } from '@angular/cdk/drag-drop';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';

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
  optionSource?: 'static' | 'api';
  apiUrl?: string;
  apiLabelKey?: string;
  apiValueKey?: string;
}

@Component({
  selector: 'app-form-builder',
  templateUrl: './form-builder.component.html',
  styleUrls: ['./form-builder.component.css']
})
export class FormBuilderComponent implements OnInit {
  fieldPalette: FormField[] = [
    { id: '', type: 'text', label: 'Text Field', name: '' },
    { id: '', type: 'textarea', label: 'Text Area', name: '' },
    { id: '', type: 'select', label: 'Dropdown', name: '' },
    { id: '', type: 'checkbox', label: 'Checkbox', name: '' },
    { id: '', type: 'radio', label: 'Radio', name: '' },
    { id: '', type: 'date', label: 'Date', name: '' },
    { id: '', type: 'number', label: 'Number', name: '' },
    { id: '', type: 'email', label: 'Email', name: '' },
    { id: '', type: 'file', label: 'File Upload', name: '' },
    { id: '', type: 'section', label: 'Section Header', name: '' }
  ];
  fields: FormField[] = [];
  editingField: FormField | null = null;
  editingIndex: number = -1;
  showFieldConfig = false;
  preview = false;
  form: FormGroup;
  apiList = [
    { label: 'Countries API', url: 'https://restcountries.com/v3.1/all', labelKey: 'name.common', valueKey: 'cca2' },
    { label: 'Users API', url: 'https://jsonplaceholder.typicode.com/users', labelKey: 'name', valueKey: 'id' }
  ];

  constructor(private fb: FormBuilder, private http: HttpClient) {}

  ngOnInit() {}

  drop(event: CdkDragDrop<FormField[]>) {
    moveItemInArray(this.fields, event.previousIndex, event.currentIndex);
  }

  addFieldFromPalette(field: FormField) {
    const newField: FormField = {
      ...field,
      id: this.generateId(),
      label: field.label,
      name: '',
      required: false,
      placeholder: '',
      defaultValue: '',
      options: [],
      optionSource: 'static'
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

  saveFieldConfig() {
    if (!this.editingField) return;
    if (this.editingField.type === 'select' && this.editingField.optionSource === 'api') {
      this.editingField.options = [];
    }
    if (this.editingIndex === -1) {
      this.fields.push(this.editingField);
    } else {
      this.fields[this.editingIndex] = this.editingField;
    }
    this.editingField = null;
    this.editingIndex = -1;
    this.showFieldConfig = false;
  }

  removeField(i: number) {
    this.fields.splice(i, 1);
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

  onApiSelect(api: any) {
    if (this.editingField) {
      this.editingField.apiUrl = api.url;
      this.editingField.apiLabelKey = api.labelKey;
      this.editingField.apiValueKey = api.valueKey;
    }
  }

  saveForm() {
    alert('Form JSON saved to console!');
    console.log('Form JSON:', JSON.stringify(this.fields, null, 2));
  }

  generateId() {
    return Math.random().toString(36).substr(2, 9);
  }

  onCheckboxChange(event: any, fieldName: string, value: any) {
    const selected = this.form.get(fieldName).value || [];
    if (event.target.checked) {
      this.form.get(fieldName).setValue([...selected, value]);
    } else {
      this.form.get(fieldName).setValue(selected.filter((v: any) => v !== value));
    }
  }

  // --- Dynamic Form Rendering ---
  buildForm() {
    const group: any = {};
    this.fields.forEach(field => {
      group[field.name] = field.required
        ? [field.defaultValue || '', Validators.required]
        : [field.defaultValue || ''];
    });
    this.form = this.fb.group(group);
    this.fields.forEach(field => {
      if (field.type === 'checkbox') {
        group[field.name] = [[]];
      } else {
        group[field.name] = field.required
          ? [field.defaultValue || '', Validators.required]
          : [field.defaultValue || ''];
      }
      if (field.type === 'select' && field.optionSource === 'api' && field.apiUrl) {
        this.http.get<any[]>(field.apiUrl).subscribe(data => {
          field.options = this.mapApiOptions(data, field.apiLabelKey, field.apiValueKey);
        });
      }
    });
  }

  mapApiOptions(data: any[], labelKey: string, valueKey: string): FormFieldOption[] {
    // Support nested keys like 'name.common'
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
      alert(JSON.stringify(this.form.value, null, 2));
    }
  }
}
