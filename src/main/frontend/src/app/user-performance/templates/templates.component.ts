import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DepartmentService } from 'src/app/services/department.service';
import { UserPerformanceService } from 'src/app/services/user-performance.service';
import { TemplateService } from 'src/app/services/template.service';
import { first } from 'rxjs/operators';
import { question } from 'src/app/models/question';
import { QuestionnaireDTO, QuestionDTO } from 'src/app/models/questionnaire-dto';
import { KraKpiService } from 'src/app/services/kpi-kra.service';
import { KpiTemplate } from 'src/app/models/kpiTemplate';
import { PerformanceService } from 'src/app/services/performance.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee } from 'src/app/models/employee';
import { User } from 'src/app/models/user';



import { Log } from 'src/app/models/log';
import { Feature } from 'src/app/models/feature';
import { LogService } from 'src/app/services/log.service';
@Component({
  selector: 'app-templates',
  templateUrl: './templates.component.html',
  styleUrls: ['./templates.component.css']
})
export class TemplatesComponent implements OnInit {
  activeTab: string = 'goals';
  selectedTemplateType: string = 'goals';
  currentUser: User;
  feature="templates";
  userMapping:any = {};
  log:Log;
  selectedDept: any = null;
  allDeptList: any[] = [];
  goalTemplates: any[] = [];
  kraKpiTemplates: any[] = [];
  questionnaireTemplates: any[] = [];
  templateForm: FormGroup;
  questionnaireForm!: FormGroup;
  kpikraForm:FormGroup;
  isEditing: boolean = false;
  editingTemplateId: number | null = null;
  selectedQuarterId: number | null = null;
  selectdepartmentId:number| null= null;

selectedDepartmentId: number | null = null;
  selectedQuarterIdForKpi: number | null = null;
quarterCyclesList: any;
selectedQuarter1: any;
selectedQuarter: any;
  errorMessage: any;
  currentEmployeeInfo:Employee = new Employee();
quarter: any;


  constructor(
    private fb: FormBuilder, 
    private departmentService: DepartmentService,
    private userPerformanceService: UserPerformanceService,
    private templateService: TemplateService,
    private kraKpiService : KraKpiService,
    private logService:LogService,
    private performanceService:PerformanceService,
    private authenticationService : AuthenticationService,
    private employeeService:EmployeeService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.templateForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      departmentId: [null, Validators.required],
      quarterId: [null],
      questions: [''],
      kpis: [''],
    });

    this.questionnaireForm = this.fb.group({
      questionTitle: ['', Validators.required],
      questionDescription: [''],
      createdBy: [null], 
      quarterId: [null, Validators.required],
      questions: this.fb.array([this.createQuestionField()]),
      departmentId: [null, Validators.required] 
    });

    this.kpikraForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      createdBy: [null],
      quarterId: [null, Validators.required],
      kpis: this.fb.array([this.kpiField()]),
      departmentId:[null , Validators.required]

    });
  }

  ngOnInit(): void {
    this.logService.updateLogInfo(this.log);

    this.onGetEmployeeInfo();
    this.fetchQuarters();
    this.fetchGoalTemplates();
    this.getAllDepartmentList();
    this.fetchQuestionnaireTemplates();
    this.fetchKraKpiTemplates();
    
    let featureMap:Feature = this.currentUser.userMapping.find(userMap => userMap.featureName == this.feature);
    featureMap.subFeatures?.forEach(sub => {
      this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
    });

  }

  async onGetEmployeeInfo(){
    this.currentEmployeeInfo = new Employee();
    let currentEmp = new Employee();
    currentEmp.empId = this.currentUser.empId;

    const response: any = await this.employeeService.getEmployeeByEmpId(currentEmp).toPromise();
    if (response.serviceStatus == "Success") {
      this.currentEmployeeInfo = response.serviceResponse;
      console.log('Employee info:', this.currentEmployeeInfo);
      this.fetchQuarters();

    } else {
      console.error(response.serviceResponse);
    }
  }

  kpiField(): FormGroup {
    return this.fb.group({
      description: ['', Validators.required],
      id: [null]
    });
  }

  get kpis(): FormArray {
    return this.kpikraForm.get('kpis') as FormArray;
  }
  addKpi():void{
    if (this.kpis.length < 10 && this.kpis.at(this.kpis.length - 1).valid) {
      this.kpis.push(this.kpiField());
    }
  }

  removeKpi(index:number):void{
    if (this.kpis.length > 1) {
      this.kpis.removeAt(index);
    }
  }
  
  fetchQuarters(): void {
    

    this.performanceService.getAllQuarterCycles().subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.quarterCyclesList = response.serviceResponse;
          // this.selectedQuarter = this.quarterCyclesList[0].quarterId;
          
        } else {
          this.errorMessage = response.serviceMessage || 'Failed to load quarters.';
        }
      },
      error: (error) => {
        this.errorMessage = error.message || 'Error fetching quarters.';
      }
    });

  

  }

  createQuestionField(): FormGroup {
    return this.fb.group({
      questionText: ['', Validators.required],
      id: [null]
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

  onSubmit() {
    console.log('Form valid?', this.questionnaireForm.valid);
    
    if (this.questionnaireForm.valid) {
      if (!this.selectedQuarterId) {
        alert('Please select a quarter');
        return;
      }
      
      if (!this.selectedDepartmentId) {
        alert('Please select a department');
        return;
      }
      
      const questionDTOs: QuestionDTO[] = this.questions.controls.map((control: any) => {
        return {
          id: control.value.id,
          questionText: control.value.questionText
        };
      });
      
      const questionnaireData: QuestionnaireDTO = {
        questionId: this.isEditing ? this.editingTemplateId : undefined,
        questionTitle: this.questionnaireForm.value.questionTitle,
        questionDescription: this.questionnaireForm.value.questionDescription,
        createdBy: this.currentUser.empId, 
        questions: questionDTOs,
        response: null,
      };
      
      console.log('Form data to submit:', questionnaireData);
      console.log('Quarter ID:', this.selectedQuarterId);
      console.log('Department ID:', this.selectedDepartmentId);
      
      if (this.isEditing && this.editingTemplateId) {
        this.updateQuestionnaireTemplate(this.editingTemplateId, questionnaireData);
      } else {
        this.createQuestionnaireTemplate(questionnaireData, this.selectedQuarterId, this.selectedDepartmentId);
      }
    } else {
      console.log('Form validation errors:', this.getFormValidationErrors());
      this.questionnaireForm.markAllAsTouched();
      alert('Please fill in all required fields correctly');
    }
  }
  
  getFormValidationErrors() {
    const errors = {};
    Object.keys(this.questionnaireForm.controls).forEach(key => {
      const control = this.questionnaireForm.get(key);
      if (control && control.errors) {
        errors[key] = control.errors;
      }
    });
    return errors;
  }
  onQuarterSelection(event: any) {
    this.selectedQuarterId = +event.target.value;
    console.log('Quarter selected:', this.selectedQuarterId);
  }
  
  onDepartmentSelection(event: any) {
    this.selectedDepartmentId = +event.target.value;
    console.log('Department selected:', this.selectedDepartmentId);
  }

  createQuestionnaireTemplate(formData: QuestionnaireDTO, quarterId: number, departmentId: number) {
    this.templateService.createQuestionnaireTemplate(formData, quarterId, departmentId)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            alert('Questionnaire template created successfully!');
            this.resetQuestionnaireForm();
            this.setActiveTab('questionnaire');
            this.fetchQuestionnaireTemplates();
          } else {
            console.error('Error creating questionnaire template:', response.serviceMessage);
            alert('Failed to create questionnaire template: ' + response.serviceMessage);
          }
        },
        error: (error) => {
          console.error('HTTP error creating questionnaire template:', error);
          alert('Error creating questionnaire template. Please try again.');
        }
      });
  }



  createKraKpiTemplate(formData: KpiTemplate) {
    // console.log('Sending KPI template data:', formData);
    this.kraKpiService.createKpiTemplate(formData,formData.quarterId,formData.departmentId)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          console.log('KPI template creation response:', response);
          if (response.serviceStatus === "Success") {
            alert('KRA-KPI template created successfully!');
            this.resetKraKpiForm();
            this.setActiveTab('kra-kpi');
            this.fetchKraKpiTemplates();
          } else {
            console.error('Error creating KRA-KPI template:', response.serviceMessage);
            alert('Failed to create KRA-KPI template: ' + response.serviceMessage);
          }
        },
        error: (error) => {
          console.error('HTTP error creating KRA-KPI template:', error);
          alert('Error creating KRA-KPI template. Please try again.');
        }
      });
  }


  updateQuestionnaireTemplate(id: number, formData: QuestionnaireDTO) {
    this.templateService.updateQuestionnaire(id, formData)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            alert('Questionnaire template updated successfully!');
            this.resetQuestionnaireForm();
            this.setActiveTab('questionnaire');
            this.fetchQuestionnaireTemplates();
          } else {
            console.error('Error updating questionnaire template:', response.serviceMessage);
            alert('Failed to update questionnaire template: ' + response.serviceMessage);
          }
        },
        error: (error) => {
          console.error('HTTP error updating questionnaire template:', error);
          alert('Error updating questionnaire template. Please try again.');
        }
      });
  }

  updateKraKpiTemplate(id: number, formData: KpiTemplate) {
    this.kraKpiService.updateKpiTemplate(id, formData)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            alert('KRA-KPI template updated successfully!');
            this.resetKraKpiForm();
            this.setActiveTab('kra-kpi');
            this.fetchKraKpiTemplates();
          } else {
            console.error('Error updating KRA-KPI template:', response.serviceMessage);
            alert('Failed to update KRA-KPI template: ' + response.serviceMessage);
          }
        },
        error: (error) => {
          console.error('HTTP error updating KRA-KPI template:', error);
          alert('Error updating KRA-KPI template. Please try again.');
        }
      });
  }

  resetQuestionnaireForm() {
    this.questionnaireForm.reset();
  
    while (this.questions.length !== 0) {
      this.questions.removeAt(0);
    }
  
    this.questions.push(this.createQuestionField());
    
    this.isEditing = false;
    this.editingTemplateId = null;
  }
  resetKraKpiForm() {
    this.kpikraForm.reset();
   
    while (this.kpis.length !== 0) {
      this.kpis.removeAt(0);
    }
   
    this.kpis.push(this.kpiField());

    this.isEditing = false;
    this.editingTemplateId = null;
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
    if (tab === 'goals') this.fetchGoalTemplates();
    if (tab === 'kra-kpi') {
      this.fetchKraKpiTemplates(); 
    }
    if (tab === 'questionnaire') this.fetchQuestionnaireTemplates();
    if (tab === 'create-template') {
      this.resetForm();
      if (this.selectedTemplateType === 'questionnaire') {
        this.resetQuestionnaireForm();
      } else if (this.selectedTemplateType === 'kra-kpi') {
        this.resetKraKpiForm();
      }
    }
  }

  fetchQuestionnaireTemplates() {
    this.templateService.getAllQuestionnaires()
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.questionnaireTemplates = response.serviceResponse;
            console.log('ques template:: ',this.questionnaireTemplates);
          } else {
            console.error('Error fetching questionnaire templates:', response.serviceMessage);
          }
        },
        error: (error) => {
          console.error('HTTP error fetching questionnaire templates:', error);
        }
      });
  }

  editQuestionnaireTemplate(template: any) {
    this.isEditing = true;
    this.editingTemplateId = template.questionId;
    this.setActiveTab('create-template');
    this.selectedTemplateType = 'questionnaire';
    while (this.questions.length !== 0) {
      this.questions.removeAt(0);
    }

    this.questionnaireForm.patchValue({
      questionTitle: template.questionTitle,
      questionDescription: template.questionDescription,
      quarterId: template.quarterId,
      createdBy: template.createdBy
    });
     if (template.questions && template.questions.length > 0) {
      template.questions.forEach((question: any) => {
        const questionGroup = this.fb.group({
          questionText: [question.questionText, Validators.required],
          id: [question.id] 
        });
        this.questions.push(questionGroup);
      });
    } else {
      
      this.questions.push(this.createQuestionField());
    }
  }
  editKraKpiTemplate(template: any) {
    this.isEditing = true;
    this.editingTemplateId = template.kpiId;
    this.setActiveTab('create-template');
    this.selectedTemplateType = 'kra-kpi';
    while (this.kpis.length !== 0) {
      this.kpis.removeAt(0);
    }

    this.kpikraForm.patchValue({
      name: template.name,
      description: template.description,
      quarterId: template.quarterId,
      createdBy: template.createdBy
    });

    if (template.kpis && template.kpis.length > 0) {
      template.kpis.forEach((kpi: any) => {
        const kpiGroup = this.fb.group({
          description: [kpi.description, Validators.required],
          id: [kpi.id] 
        });
        this.kpis.push(kpiGroup);
      });
    } else {
     
      this.kpis.push(this.kpiField());
    }
  }

  deleteQuestionnaireTemplate(questionId: number) {
    if (confirm('Are you sure you want to delete this questionnaire template?')) {
      this.templateService.deleteQuestionnaire(questionId)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              alert('Questionnaire template deleted successfully!');
              this.fetchQuestionnaireTemplates();
            } else {
              console.error('Error deleting questionnaire template:', response.serviceMessage);
              alert('Failed to delete questionnaire template: ' + response.serviceMessage);
            }
          },
          error: (error) => {
            console.error('HTTP error deleting questionnaire template:', error);
            alert('Error deleting questionnaire template. Please try again.');
          }
        });
    }
  }

  deleteKraKpiTemplate(id: number) {
    console.log('Attempting to delete KPI with ID:', id);
    if (confirm('Are you sure you want to delete this KRA-KPI template?'))  {
      this.kraKpiService.deleteKpiTemplate(id)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              alert('KRA-KPI template deleted successfully!');
              this.fetchKraKpiTemplates();
            } else {
              console.error('Error deleting KRA-KPI template:', response.serviceMessage);
              alert('Failed to delete KRA-KPI template: ' + response.serviceMessage);
            }
          },
          error: (error) => {
            console.error('HTTP error deleting KRA-KPI template:', error);
            alert('Error deleting KRA-KPI template. Please try again.');
          }
        });
    }
  }
  onQuarterChange() {
    if (this.selectedQuarterId) {
      this.templateService.getQuestionsByQuarter(this.selectedQuarterId)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              this.questionnaireTemplates = response.serviceResponse;
            } else {
              console.error('Error fetching questionnaires by quarter:', response.serviceMessage);
            }
          },
          error: (error) => {
            console.error('HTTP error fetching questionnaires by quarter:', error);
          }
        });
    } else {
      this.fetchQuestionnaireTemplates();
    }
  }
  onKpiQuarterChange() {
    if (this.selectedQuarterIdForKpi) {
      this.kraKpiService.getKpisByQuarter(this.selectedQuarterIdForKpi)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              this.kraKpiTemplates = response.serviceResponse;
            } else {
              console.error('Error fetching KRA-KPIs by quarter:', response.serviceMessage);
            }
          },
          error: (error) => {
            console.error('HTTP error fetching KRA-KPIs by quarter:', error);
          }
        });
    } else {
      this.fetchKraKpiTemplates();
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
      if (this.activeTab === 'goals') {
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
      } else if (this.activeTab === 'kra-kpi') {
        this.kraKpiService.getKraKpiTemplatesByDepartmentId(this.selectedDept.deptId)
          .pipe(first())
          .subscribe({
            next: (response: any) => {
              if (response.serviceStatus === "Success") {
                this.kraKpiTemplates = response.serviceResponse;
              } else {
                console.error('Error fetching department KRA/KPI templates:', response.serviceMessage);
              }
            },
            error: (error) => {
              console.error('HTTP error fetching department KRA/KPI templates:', error);
            }
          });
      }
    } else {
      if (this.activeTab === 'goals') {
        this.fetchGoalTemplates();
      } else if (this.activeTab === 'kra-kpi') {
        this.fetchKraKpiTemplates();
      }
    }
  }
  

  onKraKpiSubmit() {
    if (this.kpikraForm.valid) {
      const formData = this.kpikraForm.value;
      
      formData.createdBy = this.currentEmployeeInfo.empId; 
  
      const kpiDTOs = this.kpis.controls.map(control => {
        const kpiFormGroup = control as FormGroup;
        return {
          id: kpiFormGroup.value.id,
          description: kpiFormGroup.value.description
        };
      });
  
      const kpiTemplate: KpiTemplate = {
        kpiId: this.isEditing ? this.editingTemplateId : undefined,
        name: formData.name,
        description: formData.description,
        createdBy: formData.createdBy,
        quarterId: formData.quarterId,
        kpis: kpiDTOs,
        departmentId: formData.departmentId
      };
  
      console.log('KRA-KPI Form Data:', kpiTemplate);
      
      if (this.isEditing && this.editingTemplateId) {
        this.updateKraKpiTemplate(this.editingTemplateId, kpiTemplate);
      } else {
        this.createKraKpiTemplate(kpiTemplate);
      }
    } else {
      this.kpikraForm.markAllAsTouched();
    }
  }


  fetchKraKpiTemplates() {
    
    this.kraKpiService.getAllKpis()
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.kraKpiTemplates = response.serviceResponse;
            console.log(this.kraKpiTemplates);
          } else {
            console.error('Error fetching KRA-KPI templates:', response.serviceMessage);
            this.kraKpiTemplates = [];
          }
        },
        error: (error) => {
          console.error('HTTP error fetching KRA-KPI templates:', error);
          this.kraKpiTemplates = [];
        }
      });
  }
  

  resetForm() {
    this.templateForm.reset();
    this.isEditing = false;
    this.editingTemplateId = null;
    
    this.updateFormValidation();
  }

  updateFormValidation() {
   
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
    
    this.templateForm.get('description')?.updateValueAndValidity();
    this.templateForm.get('metrics')?.updateValueAndValidity();
    this.templateForm.get('questions')?.updateValueAndValidity();
  }

  updateTemplateForm() {
    this.resetForm();
    this.updateFormValidation();
    
    // if (this.selectedTemplateType === 'questionnaire') {
    //   this.resetQuestionnaireForm();
    // }
  }

  editTemplate(template: any) {
    this.isEditing = true;
    this.editingTemplateId = template.templateId;
    this.activeTab = 'create-template';
    this.selectedTemplateType = 'goals'; 
    
    const dept = this.allDeptList.find(d => d.deptId === template.departmentId);
  
    this.templateForm.patchValue({
      title: template.title,
      description: template.description,
      departmentId: template.departmentId,
      // quarterId: template.quarterId,
      department: dept ? dept.name : ''
    });
    
    this.updateFormValidation();
  }

  deleteTemplate(templateId: number) {
    if (confirm('Are you sure you want to delete this template?')) {
      this.userPerformanceService.deleteGoalTemplate(templateId)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
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
      Object.keys(this.templateForm.controls).forEach(key => {
        this.templateForm.get(key)?.markAsTouched();
      });
      return;
    }
    
    const formValue = { ...this.templateForm.value };
    
    if (this.selectedTemplateType === 'questionnaire' && formValue.questions) {
      formValue.questions = formValue.questions.split(',').map((q: string) => q.trim());
    }
    
    if (this.isEditing && this.editingTemplateId) {
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
      this.userPerformanceService.createGoalTemplate(formValue)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              this.resetForm();
              this.setActiveTab('goals');
              this.fetchGoalTemplates();
            } else {
              console.error('Error creating questionnaire template:', response.serviceMessage);
              alert('Failed to create questionnaire  template: ' + response.serviceMessage);
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