import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { DepartmentService } from 'src/app/services/department.service';
import { UserPerformanceService } from 'src/app/services/user-performance.service';
import { TemplateService } from 'src/app/services/template.service';
import { first } from 'rxjs/operators';
import { question } from 'src/app/models/question';
// import { QuestionnaireDTO } from 'src/app/models/questionnaire-dto';
import { QuestionnaireDTO, QuestionDTO } from 'src/app/models/questionnaire-dto';
import { KraKpiService } from 'src/app/services/kpi-kra.service';
import { KpiTemplate } from 'src/app/models/kpiTemplate';
@Component({
  selector: 'app-templates',
  templateUrl: './templates.component.html',
  styleUrls: ['./templates.component.css']
})
export class TemplatesComponent implements OnInit {
  activeTab: string = 'goals';
  selectedTemplateType: string = 'goals';
  
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


  



  constructor(
    private fb: FormBuilder, 
    private departmentService: DepartmentService,
    private userPerformanceService: UserPerformanceService,
    private templateService: TemplateService,
    private kraKpiService : KraKpiService
  ) {
    this.templateForm = this.fb.group({
      title: ['', Validators.required],
      description: [''],
      departmentId: [null, Validators.required],
      quarterId: [null],
      metrics: [''],
      questions: ['']
    });

    // Initialize the questionnaire form with a FormArray for questions
    this.questionnaireForm = this.fb.group({
      questionTitle: ['', Validators.required],
      questionDescription: [''],
      createdBy: [null], // Will be set before submission
      quarterId: [null, Validators.required],
      questions: this.fb.array([this.createQuestionField()])
    });

    this.kpikraForm = this.fb.group({
      name: ['', Validators.required],
      description: [''],
      createdBy: [null], // Will be set before submission
      quarterId: [null, Validators.required],
      kpis: this.fb.array([this.kpiField()])
    });
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

  ngOnInit(): void {
    this.fetchGoalTemplates();
    this.getAllDepartmentList();
    this.fetchQuestionnaireTemplates();
    this.fetchKraKpiTemplates();
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
    if (this.questionnaireForm.valid) {
     
      const formData = this.questionnaireForm.value;
      
      formData.createdBy = 1; 

      const questionDTOs: question[] = this.questions.controls.map(control => {
        const questionFormGroup = control as FormGroup;
        return {
          id: questionFormGroup.value.id,
          questionText: questionFormGroup.value.questionText
        };
      });

     
      const questionnaireDTO: QuestionnaireDTO = {
        questionId: this.isEditing ? this.editingTemplateId : undefined,
        questionTitle: formData.questionTitle,
        questionDescription: formData.questionDescription,
        createdBy: formData.createdBy,
        quarterId: formData.quarterId,
        questions: questionDTOs
      };

      console.log('Questionnaire Form Data:', questionnaireDTO);
      
      if (this.isEditing && this.editingTemplateId) {
        this.updateQuestionnaireTemplate(this.editingTemplateId, questionnaireDTO);
      } else {
        this.createQuestionnaireTemplate(questionnaireDTO);
      }
    } else {
      
      this.questionnaireForm.markAllAsTouched();
    }
  }

  createQuestionnaireTemplate(formData: QuestionnaireDTO) {
    this.templateService.createQuestionnaireTemplate(formData)
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

  resetQuestionnaireForm() {
    this.questionnaireForm.reset();
  
    while (this.questions.length !== 0) {
      this.questions.removeAt(0);
    }
  
    this.questions.push(this.createQuestionField());
    
    this.isEditing = false;
    this.editingTemplateId = null;
  }

  // setActiveTab(tab: string) {
  //   this.activeTab = tab;
  //   if (tab === 'goals') this.fetchGoalTemplates();
  //   if (tab === 'kra-kpi') this.fetchKraKpiTemplates();
  //   if (tab === 'questionnaire') this.fetchQuestionnaireTemplates();
  //   if (tab === 'create-template') {
  //     this.resetForm();
  //     // Reset the questionnaire form when switching to create template tab
  //     if (this.selectedTemplateType === 'questionnaire') {
  //       this.resetQuestionnaireForm();
  //     }
  //   }
  // }

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
  // constructor(
  //   private fb: FormBuilder, 
  //   private departmentService: DepartmentService,
  //   private userPerformanceService: UserPerformanceService,
  //   private templateService: TemplateService
  // ) {
  //   this.templateForm = this.fb.group({
  //     title: ['', Validators.required],
  //     description: [''],
  //     departmentId: [null, Validators.required],
  //     quarterId: [null],
  //     metrics: [''],
  //     questions: ['']
  //   });

  //   // Initialize the questionnaire form with a FormArray for questions
  //   this.questionnaireForm = this.fb.group({
  //     questionTitle: ['', Validators.required],
  //     questionDescription: [''],
  //     createdBy: [null], // Will be set before submission
  //     quarterId: [null],
  //     questions: this.fb.array([this.createQuestionField()])
  //   });
  // }

  // ngOnInit(): void {
  //   this.fetchGoalTemplates();
  //   this.getAllDepartmentList();
  //   this.fetchQuestionnaireTemplates();
  // }

  // // Create a single question form field
  // createQuestionField(): FormGroup {
  //   return this.fb.group({
  //     questionText: ['', Validators.required]
  //   });
  // }

  // // Get access to the questions FormArray
  // get questions(): FormArray {
  //   return this.questionnaireForm.get('questions') as FormArray;
  // }

  // // Add a new question field
  // addQuestion(): void {
  //   if (this.questions.length < 10 && this.questions.at(this.questions.length - 1).valid) {
  //     this.questions.push(this.createQuestionField());
  //   }
  // }

  // // Remove a question field
  // removeQuestion(index: number): void {
  //   if (this.questions.length > 1) {
  //     this.questions.removeAt(index);
  //   }
  // }

  // 
  // onSubmit() {
  //   if (this.questionnaireForm.valid) {
  //     const formData = this.questionnaireForm.value;
  //     // Set the logged-in user ID (replace with actual implementation)
  //     formData.createdBy = 1; // Example: Current user ID

  //     console.log('Questionnaire Form Data:', formData);
      
  //     if (this.isEditing && this.editingTemplateId) {
  //       this.updateQuestionnaireTemplate(this.editingTemplateId, formData);
  //     } else {
  //       this.createQuestionnaireTemplate(formData);
  //     }
  //   } else {
  //     // Mark all fields as touched to trigger validation messages
  //     this.questionnaireForm.markAllAsTouched();
  //   }
  // }

  // // Create a new questionnaire template
  // createQuestionnaireTemplate(formData: QuestionnaireDTO) {
  //   this.templateService.createQuestionnaireTemplate(formData)
  //     .pipe(first())
  //     .subscribe({
  //       next: (response: any) => {
  //         if (response.serviceStatus === "Success") {
  //           alert('Questionnaire template created successfully!');
  //           this.resetQuestionnaireForm();
  //           this.setActiveTab('questionnaire');
  //           this.fetchQuestionnaireTemplates();
  //         } else {
  //           console.error('Error creating questionnaire template:', response.serviceMessage);
  //           alert('Failed to create questionnaire template: ' + response.serviceMessage);
  //         }
  //       },
  //       error: (error) => {
  //         console.error('HTTP error creating questionnaire template:', error);
  //         alert('Error creating questionnaire template. Please try again.');
  //       }
  //     });
  // }

  // // Update an existing questionnaire template
  // updateQuestionnaireTemplate(id: number, formData: QuestionnaireDTO) {
  //   this.templateService.updateQuestionnaire(id, formData)
  //     .pipe(first())
  //     .subscribe({
  //       next: (response: any) => {
  //         if (response.serviceStatus === "Success") {
  //           alert('Questionnaire template updated successfully!');
  //           this.resetQuestionnaireForm();
  //           this.setActiveTab('questionnaire');
  //           this.fetchQuestionnaireTemplates();
  //         } else {
  //           console.error('Error updating questionnaire template:', response.serviceMessage);
  //           alert('Failed to update questionnaire template: ' + response.serviceMessage);
  //         }
  //       },
  //       error: (error) => {
  //         console.error('HTTP error updating questionnaire template:', error);
  //         alert('Error updating questionnaire template. Please try again.');
  //       }
  //     });
  // }

  // // Reset the questionnaire form
  // resetQuestionnaireForm() {
  //   this.questionnaireForm.reset();
  //   // Clear the questions FormArray except for one empty question
  //   while (this.questions.length !== 0) {
  //     this.questions.removeAt(0);
  //   }
  //   // Add one empty question field
  //   this.questions.push(this.createQuestionField());
    
  //   this.isEditing = false;
  //   this.editingTemplateId = null;
  // }

  // setActiveTab(tab: string) {
  //   this.activeTab = tab;
  //   if (tab === 'goals') this.fetchGoalTemplates();
  //   if (tab === 'kra-kpi') this.fetchKraKpiTemplates();
  //   if (tab === 'questionnaire') this.fetchQuestionnaireTemplates();
  //   if (tab === 'create-template') {
  //     this.resetForm();
  //     // Reset the questionnaire form when switching to create template tab
  //     if (this.selectedTemplateType === 'questionnaire') {
  //       this.resetQuestionnaireForm();
  //     }
  //   }
  // }

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

  // fetchQuestionnaireTemplates() {
  //   this.templateService.getAllQuestionnaires()
  //     .pipe(first())
  //     .subscribe({
  //       next: (response: any) => {
  //         if (response.serviceStatus === "Success") {
  //           this.questionnaireTemplates = response.serviceResponse;
  //         } else {
  //           console.error('Error fetching questionnaire templates:', response.serviceMessage);
  //         }
  //       },
  //       error: (error) => {
  //         console.error('HTTP error fetching questionnaire templates:', error);
  //       }
  //     });
  // }

  // editQuestionnaireTemplate(template: any) {
  //   this.isEditing = true;
  //   this.editingTemplateId = template.id;
  //   this.activeTab = 'create-template';
  //   this.selectedTemplateType = 'questionnaire';
    
  //   // Clear existing questions
  //   while (this.questions.length !== 0) {
  //     this.questions.removeAt(0);
  //   }
    
  //   // Patch the basic form values
  //   this.questionnaireForm.patchValue({
  //     questionTitle: template.questionTitle,
  //     questionDescription: template.questionDescription,
  //     quarterId: template.quarterId,
  //     createdBy: template.createdBy
  //   });
    
  //   // Add questions from the template
  //   if (template.questions && template.questions.length > 0) {
  //     template.questions.forEach((question: any) => {
  //       const questionGroup = this.fb.group({
  //         questionText: [question.questionText, Validators.required],
  //         id: [question.id] // Keep the question ID for backend reference
  //       });
  //       this.questions.push(questionGroup);
  //     });
  //   } else {
  //     // If no questions, add one empty question field
  //     this.questions.push(this.createQuestionField());
  //   }
  // }

  // deleteQuestionnaireTemplate(id: number) {
  //   if (confirm('Are you sure you want to delete this questionnaire template?')) {
  //     this.templateService.deleteQuestionnaire(id)
  //       .pipe(first())
  //       .subscribe({
  //         next: (response: any) => {
  //           if (response.serviceStatus === "Success") {
  //             alert('Questionnaire template deleted successfully!');
  //             this.fetchQuestionnaireTemplates();
  //           } else {
  //             console.error('Error deleting questionnaire template:', response.serviceMessage);
  //             alert('Failed to delete questionnaire template: ' + response.serviceMessage);
  //           }
  //         },
  //         error: (error) => {
  //           console.error('HTTP error deleting questionnaire template:', error);
  //           alert('Error deleting questionnaire template. Please try again.');
  //         }
  //       });
  //   }
  // }

  // onQuarterChange() {
  //   if (this.selectedQuarterId) {
  //     this.templateService.getQuestionsByQuarter(this.selectedQuarterId)
  //       .pipe(first())
  //       .subscribe({
  //         next: (response: any) => {
  //           if (response.serviceStatus === "Success") {
  //             this.questionnaireTemplates = response.serviceResponse;
  //           } else {
  //             console.error('Error fetching questionnaires by quarter:', response.serviceMessage);
  //           }
  //         },
  //         error: (error) => {
  //           console.error('HTTP error fetching questionnaires by quarter:', error);
  //         }
  //       });
  //   } else {
  //     this.fetchQuestionnaireTemplates();
  //   }
  // }

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
      this.fetchGoalTemplates();
    }
  }

  onKraKpiSubmit() {
    if (this.kpikraForm.valid) {
      
      const formData = this.kpikraForm.value;
      
      if (!formData.quarterId) {
        alert('Please select a quarter');
        return;
      }
  
      formData.createdBy = 1; 
  
     
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
        kpis: kpiDTOs
      };
  
      console.log('KRA-KPI Form Data:', kpiTemplate);
  
      if (this.isEditing && this.editingTemplateId) {
        this.updateKraKpiTemplate(this.editingTemplateId, kpiTemplate);
      } else {
        this.createKraKpiTemplate(kpiTemplate);
      }
    } else {
      this.kpikraForm.markAllAsTouched();
      alert('Please fill all required fields');
    }
  }
  createKraKpiTemplate(formData: KpiTemplate) {
    console.log('Sending KPI template data:', formData);
    this.kraKpiService.createKpiTemplate(formData)
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

  deleteKraKpiTemplate(kpiId: number) {
    if (confirm('Are you sure you want to delete this KRA-KPI template?')) {
      this.kraKpiService.deleteKpiTemplate(kpiId)
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
  onKpiQuarterChange() {
    if (this.selectedQuarterId) {
      this.kraKpiService.getKpisByQuarter(this.selectedQuarterId)
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

  resetKraKpiForm() {
    this.kpikraForm.reset();
   
    while (this.kpis.length !== 0) {
      this.kpis.removeAt(0);
    }
   
    this.kpis.push(this.kpiField());

    this.isEditing = false;
    this.editingTemplateId = null;
  }
  fetchKraKpiTemplates() {
    this.kraKpiService.getAllKpis()
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response && response.serviceStatus === "Success") {
            this.kraKpiTemplates = Array.isArray(response.serviceResponse) 
              ? response.serviceResponse 
              : [response.serviceResponse];
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
    
    if (this.selectedTemplateType === 'questionnaire') {
      this.resetQuestionnaireForm();
    }
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
      quarterId: template.quarterId,
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