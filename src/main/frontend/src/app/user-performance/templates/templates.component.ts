<<<<<<< Updated upstream
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
=======
import { Component, OnInit, TemplateRef } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
>>>>>>> Stashed changes
import { DepartmentService } from 'src/app/services/department.service';
import { UserPerformanceService } from 'src/app/services/user-performance.service';
import { first } from 'rxjs/operators';
import { question } from 'src/app/models/question';
import { BsModalRef, BsModalService } from 'ngx-bootstrap/modal';
import { Domain } from 'src/app/models/domain';
import { ValidationService } from 'src/app/services/validation.service';

@Component({
  selector: 'app-templates',
  templateUrl: './templates.component.html',
  styleUrls: ['./templates.component.css']
})
export class TemplatesComponent implements OnInit {
toggleAllGoalTemplates($event: Event) {
throw new Error('Method not implemented.');
}
areAllGoalTemplatesSelected() {
throw new Error('Method not implemented.');
}
toggleAllQuestionnaireTemplates($event: Event) {
throw new Error('Method not implemented.');
}
areAllQuestionnaireTemplatesSelected() {
throw new Error('Method not implemented.');
}
toggleAllKraKpiTemplates($event: Event) {
throw new Error('Method not implemented.');
}
areAllKraKpiTemplatesSelected() {
throw new Error('Method not implemented.');
}
  activeTab: string = 'goals';
  selectedTemplateType: string = 'goals';
<<<<<<< Updated upstream
  
  selectedDept: any = null;
=======

  questionnaireForm!: FormGroup;
  selectedDept:any;
>>>>>>> Stashed changes
  allDeptList: any[] = [];
  goalTemplates: any[] = [];
  kraKpiTemplates: any[] = [];
  questionnaireTemplates: any[] = [];
<<<<<<< Updated upstream
  
  templateForm: FormGroup;
  isEditing: boolean = false;
  editingTemplateId: number | null = null;
  
  constructor(
    private fb: FormBuilder, 
    private departmentService: DepartmentService,
    private userPerformanceService: UserPerformanceService
=======
  allQuesList: any[] = [];
  // allSpecializationList:any[] = [];
  allSpecializationList: Domain[] = [new Domain()];

  templateForm: FormGroup;
  alertMessage: string;
  modalRef: BsModalRef = new BsModalRef();

  quesObj: question = new question();
  domainObj: Domain = new Domain();

  constructor(
    private fb: FormBuilder, 
    private http: HttpClient,
    public validationService: ValidationService,
    private departmentService: DepartmentService,
    private modalService: BsModalService,
>>>>>>> Stashed changes
  ) {
    this.templateForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      departmentId: [null, Validators.required],
      quarterId: [null],
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
    if (tab === 'create-template') {
      this.resetForm();
    }
  }

  fetchGoalTemplates() {
    this.userPerformanceService.getAllGoalTemplates()
    .pipe(first())
    .subscribe({
      next: (response: any) => {
        if (response.serviceStatus === "Success") {
          this.goalTemplates = response.serviceResponse;
        } else {
          console.error('Error fetching goal templates:', response.serviceMessage);
        }
      },
      error: (error) => {
        console.error('HTTP error fetching goal templates:', error);
      }
    });
  }

  onDepartmentChange() {
    if (this.selectedDept && this.selectedDept !== 'all') {
      this.userPerformanceService.getGoalTemplatesByDepartmentId(this.selectedDept.deptId)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              this.goalTemplates = response.serviceResponse;
            } else {
              console.error('Error fetching department goal templates:', response.serviceMessage);
            }
          },
          error: (error) => {
            console.error('HTTP error fetching department templates:', error);
          }
        });
    } else {
      // If no department is selected or "All Departments" is selected, fetch all templates
      this.fetchGoalTemplates();
    }
    // this.http.get(`/api/goals-templates/department/department=${this.selectedDept}`)
    //   .subscribe((data: any) => this.goalTemplates = data);
  }

  fetchKraKpiTemplates() {
    // Implement when backend is ready
  }

  fetchQuestionnaireTemplates() {
    // Implement when backend is ready
  }

  resetForm() {
    this.templateForm.reset();
    this.isEditing = false;
    this.editingTemplateId = null;
    
    // Reset validators based on the selected template type
    this.updateFormValidation();
  }

  updateFormValidation() {
    // Update form validation based on the selected template type
    if (this.selectedTemplateType === 'goals') {
      this.templateForm.get('description')?.setValidators([Validators.required]);
      this.templateForm.get('metrics')?.clearValidators();
      this.templateForm.get('questions')?.clearValidators();
    } else if (this.selectedTemplateType === 'kra-kpi') {
      this.templateForm.get('description')?.clearValidators();
      this.templateForm.get('metrics')?.setValidators([Validators.required]);
      this.templateForm.get('questions')?.clearValidators();
    } else if (this.selectedTemplateType === 'questionnaire') {
      this.templateForm.get('description')?.clearValidators();
      this.templateForm.get('metrics')?.clearValidators();
      this.templateForm.get('questions')?.setValidators([Validators.required]);
    }
    
    // Update form controls validity
    this.templateForm.get('description')?.updateValueAndValidity();
    this.templateForm.get('metrics')?.updateValueAndValidity();
    this.templateForm.get('questions')?.updateValueAndValidity();
  }

  updateTemplateForm() {
    this.resetForm();
    this.updateFormValidation();
  }

  editTemplate(template: any) {
    this.isEditing = true;
    this.editingTemplateId = template.templateId;
    this.activeTab = 'create-template';
    this.selectedTemplateType = 'goals'; // Assuming we're editing a goal template
    
    const dept = this.allDeptList.find(d => d.deptId === template.departmentId);
  
    this.templateForm.patchValue({
      title: template.title,
      description: template.description,
      departmentId: template.departmentId,
      quarterId: template.quarterId,
      // Add department if you need to send it
      department: dept ? dept.name : ''
    });
    
    // Update form validation for the selected template type
    this.updateFormValidation();
  }

  deleteTemplate(templateId: number) {
    if (confirm('Are you sure you want to delete this template?')) {
      this.userPerformanceService.deleteGoalTemplate(templateId)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              // Successfully deleted, now refresh the list
              this.fetchGoalTemplates();
            } else {
              console.error('Error deleting template:', response.serviceMessage);
              alert('Failed to delete template: ' + response.serviceMessage);
            }
          },
          error: (error) => {
            console.error('HTTP error deleting template:', error);
            alert('Error deleting template. Please try again.');
          }
        });
    }
  }

  createOrUpdateTemplate() {
    if (this.templateForm.invalid) {
      // Mark all fields as touched to trigger validation messages
      Object.keys(this.templateForm.controls).forEach(key => {
        this.templateForm.get(key)?.markAsTouched();
      });
      return;
    }
<<<<<<< Updated upstream
    
    const formValue = { ...this.templateForm.value };
    
    if (this.selectedTemplateType === 'questionnaire' && formValue.questions) {
      formValue.questions = formValue.questions.split(',').map((q: string) => q.trim());
    }
    
    if (this.isEditing && this.editingTemplateId) {
      // Update existing template
      this.userPerformanceService.updateGoalTemplate(this.editingTemplateId, formValue)
      .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              this.resetForm();
              this.setActiveTab('goals');
              this.fetchGoalTemplates();
            } else {
              console.error('Error updating template:', response.serviceMessage);
              alert('Failed to update template: ' + response.serviceMessage);
            }
          },
          error: (error) => {
            console.error('HTTP error updating template:', error);
            alert('Error updating template. Please try again.');
          }
        });
    } else {
      // Create new template
      this.userPerformanceService.createGoalTemplate(formValue)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              this.resetForm();
              this.setActiveTab('goals');
              this.fetchGoalTemplates();
            } else {
              console.error('Error creating template:', response.serviceMessage);
              alert('Failed to create template: ' + response.serviceMessage);
            }
          },
          error: (error) => {
            console.error('HTTP error creating template:', error);
            alert('Error creating template. Please try again.');
          }
        });
    }
  }

  getAllDepartmentList() {
    this.departmentService.getAllDepartments()
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.allDeptList = response.serviceResponse;
          } else {
            console.error(response.serviceResponse);
          }
        },
        error: (error) => {
          console.error('HTTP error fetching departments:', error);
        }
      });
  }
}
=======

    addInputSpecializationField(template?: TemplateRef<any>, currentSpecializationName?: any) {
  
      // Check validation (Optional)
      if(this.validationService && !this.validationService.validateTeamActivity(currentSpecializationName)) {
        let selectedSpec = this.allSpecializationList.find(spec => spec.specializationName === currentSpecializationName);
        if(selectedSpec) selectedSpec.specializationName = '';
        
        this.alertMessage = "Please Enter Valid Specialization Name !!";
        this.openAlertMod(template, this.alertMessage);
        return;
      }
    
      // Add new empty specialization field
      const newDomainObj = new Domain();
      
      // Explicitly trigger change detection by assigning a new array reference
      this.allSpecializationList = [...this.allSpecializationList, newDomainObj];
    }
    
  
    removeInputSpecializationField(spec:any){
      this.allSpecializationList.forEach((value, index) => {
        if (value == spec) {
          this.allSpecializationList.splice(index, 1);
        }
      });
      //console.log(this.allSpecializationList, " :this.allSpecializationList");
    }
 

  

  openAlertMod(template: TemplateRef<any>, message: any) {
      this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
      this.alertMessage = message;
    }
  
    cancelRequest() {
      this.modalRef.hide();
    }

    
}
>>>>>>> Stashed changes
