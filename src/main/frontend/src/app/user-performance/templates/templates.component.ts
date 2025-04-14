import { Component, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DepartmentService } from 'src/app/services/department.service';
import { UserPerformanceService } from 'src/app/services/user-performance.service';
import { TemplateService } from 'src/app/services/template.service';
import { first } from 'rxjs/operators';
import { QuestionnaireDTO, QuestionDTO } from 'src/app/models/questionnaire-dto';
import { KraKpiService } from 'src/app/services/kpi-kra.service';
import { KpiTemplate } from 'src/app/models/kpiTemplate';
import { PerformanceService } from 'src/app/services/performance.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { EmployeeService } from 'src/app/services/employee.service';
import { Employee } from 'src/app/models/employee';
import { User } from 'src/app/models/user';
import { BsModalService, BsModalRef } from 'ngx-bootstrap/modal';



import { Log } from 'src/app/models/log';
import { Feature } from 'src/app/models/feature';
import { LogService } from 'src/app/services/log.service';
@Component({
  selector: 'app-templates',
  templateUrl: './templates.component.html',
  styleUrls: ['./templates.component.css']
})
export class TemplatesComponent implements OnInit {

  @ViewChild('alert_message') alert_message: TemplateRef<any>;

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
alertMessage: any;
modalRef?: BsModalRef;
jobRoleObj: any;
// jobRoleObj: any;


  constructor(
    private fb: FormBuilder, 
    private departmentService: DepartmentService,
    private userPerformanceService: UserPerformanceService,
    private templateService: TemplateService,
    private modalService: BsModalService,
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
      departmentId:[null , Validators.required],
      employee_role: [null , Validators.required]
    });
  }

  ngOnInit(): void {
    this.logService.updateLogInfo(this.log);

    this.onGetEmployeeInfo();
    this.fetchQuarters();
    this.fetchGoalTemplates();
    this.getAllDepartmentList();
    
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

  onSubmit(template: TemplateRef<any>) {
    console.log('Submit button clicked!');
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
      
      // console.log('Form data to submit:', questionnaireData);
      // console.log('Quarter ID:', this.selectedQuarterId);
      // console.log('Department ID:', this.selectedDepartmentId);
      
      if (this.isEditing && this.editingTemplateId) {
        this.updateQuestionnaireTemplate(this.editingTemplateId, questionnaireData,this.alert_message);
      } else {
        this.createQuestionnaireTemplate(questionnaireData, this.selectedQuarterId, this.selectedDepartmentId, this.alert_message);
      }
    } else {
      console.log('Form validation errors:', this.getFormValidationErrors());
      this.questionnaireForm.markAllAsTouched();
      this.alertMessage = 'Please fill in all required fields correctly'
      this.openAlertMod(template, this.alertMessage);
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

  createQuestionnaireTemplate(formData: QuestionnaireDTO, quarterId: number, departmentId: number, template: TemplateRef<any>) {
    this.templateService.createQuestionnaireTemplate(formData, quarterId, departmentId)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.alertMessage = 'Questionnaire template created successfully!'
            this.openAlertMod(template, this.alertMessage);
            this.resetQuestionnaireForm();
            this.setActiveTab('questionnaire');
            this.fetchQuestionnaireTemplates();
          } else {
            this.alertMessage = `Failed to create questionnaire template: ${response.serviceMessage}`
            this.openAlertMod(template, this.alertMessage);
          }
        },
        error: (error) => {
          console.error('HTTP error creating questionnaire template:', error);
          this.alertMessage = 'Error creating questionnaire template. Please try again.'
          this.openAlertMod(template, this.alertMessage);
        }
      });
  }



  createKraKpiTemplate(formData: KpiTemplate, template: TemplateRef<any>) {
    // console.log('Sending KPI template data:', formData);
    this.kraKpiService.createKpiTemplate(formData,formData.quarterId,formData.departmentId,formData.employee_role)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          console.log('KPI template creation response:', response);
          if (response.serviceStatus === "Success") {
            this.alertMessage = 'KRA-KPI template created successfully!'
            this.openAlertMod(template, this.alertMessage);
            this.resetKraKpiForm();
            this.setActiveTab('kra-kpi');
            this.fetchKraKpiTemplates();
          } else {
            console.error('Error creating KRA-KPI template:', response.serviceMessage);
            this.alertMessage = `Failed to create KRA-KPI template:  ${response.serviceMessage}`
            this.openAlertMod(template, this.alertMessage);
          }
        },
        error: (error) => {
          console.error('HTTP error creating KRA-KPI template:', error);
          this.alertMessage = 'Error creating KRA-KPI template. Please try again.'
          this.openAlertMod(template, this.alertMessage);
        }
      });
  }


  updateQuestionnaireTemplate(id: number, formData: QuestionnaireDTO, template:TemplateRef<any>) {
    this.templateService.updateQuestionnaire(id, formData)
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response.serviceStatus === "Success") {
            this.alertMessage = 'Questionnaire template updated successfully!'
            this.openAlertMod(template, this.alertMessage);
            this.resetQuestionnaireForm();
            this.setActiveTab('questionnaire');
            this.fetchQuestionnaireTemplates();
          } else {
            console.error('Error updating questionnaire template:', response.serviceMessage);
            this.alertMessage = `Failed to update questionnaire template:  ${response.serviceMessage}`
            this.openAlertMod(template, this.alertMessage);
          }
        },
        error: (error) => {
          console.error('HTTP error updating questionnaire template:', error);
          this.alertMessage = 'Error updating questionnaire template. Please try again.'
          this.openAlertMod(template, this.alertMessage);
        }
      });
  }

  updateKraKpiTemplate(id: number, formData: KpiTemplate, template: TemplateRef<any>) {
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
            this.alertMessage = `Failed to update KRA-KPI template:  ${response.serviceMessage}`
            this.openAlertMod(template, this.alertMessage);
          }
        },
        error: (error) => {
          console.error('HTTP error updating KRA-KPI template:', error);
          this.alertMessage = 'Error updating KRA-KPI template. Please try again.'
          this.openAlertMod(template, this.alertMessage);
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

  deleteQuestionnaireTemplate(questionId: number,template: TemplateRef<any>) {
    if (confirm('Are you sure you want to delete this questionnaire template?')) {
      this.templateService.deleteQuestionnaire(questionId)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              this.alertMessage = 'Questionnaire template deleted successfully!'
              this.openAlertMod(template, this.alertMessage);
              this.fetchQuestionnaireTemplates();
            } else {
              this.alertMessage = `Failed to delete questionnaire template:  ${response.serviceMessage}`
              this.openAlertMod(template, this.alertMessage);
            }
          },
          error: (error) => {
            this.alertMessage = 'Error deleting questionnaire template. Please try again.'
            this.openAlertMod(template, this.alertMessage);
          }
        });
    }
  }

  deleteKraKpiTemplate(id: number, template: TemplateRef<any>) {
    if (confirm('Are you sure you want to delete this KRA-KPI template?'))  {
      this.kraKpiService.deleteKpiTemplate(id)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              this.alertMessage = 'KRA-KPI template deleted successfully!'
              this.openAlertMod(template, this.alertMessage);
              this.fetchKraKpiTemplates();
            } else {
              this.alertMessage = `Failed to delete KRA-KPI template:  ${response.serviceMessage}`
              this.openAlertMod(template, this.alertMessage);
            }
          },
          error: (error) => {
            console.error('HTTP error deleting KRA-KPI template:', error);
            this.alertMessage = 'Error deleting KRA-KPI template. Please try again.'
            this.openAlertMod(template, this.alertMessage);
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
      } else if (this.activeTab === 'questionnaire') { 
        this.templateService.getQuestionnaireTemplatesByDepartmentId(this.selectedDept.deptId)
          .pipe(first())
          .subscribe({
            next: (response: any) => {
              if (response.serviceStatus === "Success") {
                this.questionnaireTemplates = response.serviceResponse;
              } else {
                console.error("Error fetching department questionnaire templates:", response.serviceMessage);
              }
            },
            error: (error) => {
              console.error("HTTP error fetching department questionnaire:", error);
            }
          });
      }
    } else { 
      if (this.activeTab === 'goals') {
        this.fetchGoalTemplates();
      } else if (this.activeTab === 'kra-kpi') {
        this.fetchKraKpiTemplates();
      } else if (this.activeTab === 'questionnaire') { 
        this.fetchQuestionnaireTemplates();
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
        departmentId: formData.departmentId,
        employee_role: formData.employee_role

      };
  
      console.log('KRA-KPI Form Data:', kpiTemplate);
      
      if (this.isEditing && this.editingTemplateId) {
        this.updateKraKpiTemplate(this.editingTemplateId, kpiTemplate,this.alert_message);
      } else {
        this.createKraKpiTemplate(kpiTemplate,this.alert_message);
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
      this.templateForm.get('title')?.setValidators([Validators.required]);
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

  deleteGoalTemplate(templateId: number,template: TemplateRef<any>) {
    if (confirm('Are you sure you want to delete this template?')) {
      this.userPerformanceService.deleteGoalTemplate(templateId)
        .pipe(first())
        .subscribe({
          next: (response: any) => {
            if (response.serviceStatus === "Success") {
              this.fetchGoalTemplates();
            } else {
              this.alertMessage = `Failed to delete template:  ${response.serviceMessage}`
              this.openAlertMod(template, this.alertMessage);
            }
          },
          error: (error) => {
            this.alertMessage = 'Error deleting template. Please try again.'
            this.openAlertMod(template, this.alertMessage);
          }
        });
    }
  }

  createOrUpdateTemplate(template: TemplateRef<any>) {
    if (this.templateForm.invalid) {
      Object.keys(this.templateForm.controls).forEach(key => {
        this.templateForm.get(key)?.markAsTouched();
      });
      return;
    }
    
    const formValue = { ...this.templateForm.value };
    
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
              this.alertMessage = `Failed to update template:  ${response.serviceMessage}`
              this.openAlertMod(template, this.alertMessage);
            }
          },
          error: (error) => {
            console.error('HTTP error updating template:', error);
            this.alertMessage = 'Error updating template. Please try again.'
            this.openAlertMod(template, this.alertMessage);
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
              this.alertMessage = `Failed to create questionnaire  template:  ${response.serviceMessage}`
              this.openAlertMod(template, this.alertMessage);
            }
          },
          error: (error) => {
            console.error('HTTP error creating template:', error);
            this.alertMessage = 'Error creating template. Please try again.'
            this.openAlertMod(template, this.alertMessage);
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

  openAlertMod(template: TemplateRef<any>, message: any) {
    this.modalRef = this.modalService.show(template, { class: 'modal-sm' });
    this.alertMessage = message;
  }
}