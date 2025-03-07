import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { DepartmentService } from 'src/app/services/department.service';
import { first } from 'rxjs/operators';


@Component({
  selector: 'app-templates',
  templateUrl: './templates.component.html',
  styleUrls: ['./templates.component.css']
})
export class TemplatesComponent implements OnInit {
  activeTab: string = 'goals';
  selectedTemplateType: string = 'goals';

  selectedDept:any;
  allDeptList: any[] = [];
  goalTemplates: any[] = [];
  kraKpiTemplates: any[] = [];
  questionnaireTemplates: any[] = [];

  templateForm: FormGroup;

  constructor(private fb: FormBuilder, private http: HttpClient,private departmentService: DepartmentService,) {
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
  }

  setActiveTab(tab: string) {
    this.activeTab = tab;
    if (tab === 'goals') this.fetchGoalTemplates();
    if (tab === 'kra-kpi') this.fetchKraKpiTemplates();
    if (tab === 'questionnaire') this.fetchQuestionnaireTemplates();
  }

  fetchGoalTemplates() {
    // this.http.get(`/api/goals/templates?department=${this.selectedDepartment}`)
    //   .subscribe((data: any) => this.goalTemplates = data);
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
}
