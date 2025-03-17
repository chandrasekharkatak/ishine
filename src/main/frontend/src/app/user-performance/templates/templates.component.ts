import { Component, OnInit, TemplateRef } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { DepartmentService } from 'src/app/services/department.service';
import { first } from 'rxjs/operators';
import { question } from 'src/app/models/question';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { ValidationService } from 'src/app/services/validation.service';


@Component({
  selector: 'app-templates',
  templateUrl: './templates.component.html',
  styleUrls: ['./templates.component.css']
})
export class TemplatesComponent implements OnInit {
  activeTab: string = 'goals';
  selectedTemplateType: string = 'goals';

  questionnaireForm!: FormGroup;
  selectedDept:any;
  allDeptList: any[] = [];
  goalTemplates: any[] = [];
  kraKpiTemplates: any[] = [];
  questionnaireTemplates: any[] = [];
  allQuesList: any[] = [];

  templateForm: FormGroup;
  alertMessage: string;
  modalRef: BsModalRef = new BsModalRef();

  quesObj: question = new question();

  constructor(
    private fb: FormBuilder, 
    private http: HttpClient,
    public validationService: ValidationService,
    private departmentService: DepartmentService,
    private modalService: BsModalService,
  ) {
    this.templateForm = this.fb.group({
      name: [''],
      description: [''],
      metrics: [''],
      questions: ['']
    });
  }

  ngOnInit(): void {
    this.fetchGoalTemplates();
    this.getAllDepartmentList();

    this.questionnaireForm = this.fb.group({
      title: ['', Validators.required],
      questions: this.fb.array([this.createQuestionField()])
    });
    
  }

  createQuestionField(): FormGroup {
    return this.fb.group({
      question: ['', Validators.required]
    });
  }

  get questions(): FormArray {
    return this.questionnaireForm.get('questions') as FormArray;
  }

  addQuestion(): void {
    if (this.questions.length < 10 && this.questions.at(this.questions.length - 1).valid) {
      this.questions.push(this.createQuestionField());
    }
  }

  removeQuestion(index: number): void {
    if (this.questions.length > 1) {
      this.questions.removeAt(index);
    }
  }

  onSubmit(): void {
    if (this.questionnaireForm.valid) {
      const formData = this.questionnaireForm.value;
      this.http.post('YOUR_BACKEND_URL', formData).subscribe(
        response => {
          console.log('Form submitted successfully:', response);
        },
        error => {
          console.error('Error submitting form:', error);
        }
      );
    }
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
    if (tab === 'goals') this.fetchGoalTemplates();
    if (tab === 'kra-kpi') this.fetchKraKpiTemplates();
    if (tab === 'questionnaire') this.fetchQuestionnaireTemplates();
  }

  fetchGoalTemplates() {
    this.http.get(`/api/goals-templates/department/department=${this.selectedDept}`)
      .subscribe((data: any) => this.goalTemplates = data);
  }

  fetchKraKpiTemplates() {
    // this.http.get(`/api/kra-kpi/templates?department=${this.selectedDepartment}`)
    //   .subscribe((data: any) => this.kraKpiTemplates = data);
  }

  fetchQuestionnaireTemplates() {
    // this.http.get(`/api/questionnaire/templates?department=${this.selectedDepartment}`)
    //   .subscribe((data: any) => this.questionnaireTemplates = data);
  }

  updateTemplateForm() {
    this.templateForm.reset();
  }

  createTemplate() {
    let payload = { ...this.templateForm.value };
    if (this.selectedTemplateType === 'questionnaire') {
      payload.questions = payload.questions.split(',').map(q => q.trim());
    }

    this.http.post(`/api/${this.selectedTemplateType}/create`, payload)
      .subscribe(response => console.log('Template created:', response));
  }

  getAllDepartmentList() {
      this.allDeptList = [];
  
      this.departmentService.getAllDepartments().pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus == "Success") {
          this.allDeptList = response.serviceResponse;
        } else {
          console.error(response.serviceResponse);
        }
      });
    }
 

  openAlertMod(template: TemplateRef<any>, message: any) {
      this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
      this.alertMessage = message;
    }
  
    cancelRequest() {
      this.modalRef.hide();
    }

    
}
