import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { first } from 'rxjs/operators';
import { Project } from '../models/project';
import { ProjectInsight } from '../models/projectInsight';
import { ProjectMilestone } from '../models/projectMilestone';
import { ProjectModule } from '../models/projectModule';
import { ProjectQuestion } from '../models/projectQuestion';
import { ProjectResponse } from '../models/projectResponse';
import { ProjectResponsePoint } from '../models/projectResponsePoint';
import { ProjectSubModule } from '../models/projectSubModule';
import { ProjectInsightService } from './project-insight.service';
import { ProjectService } from './project.service';
import { ValidationService } from './validation.service';
import { EmployeeService } from './employee.service';
import { saveAs } from 'file-saver';
import { Observable } from 'rxjs';
import { SurveyOption } from '../models/sureyOption';
import { FormField } from '../models/formField';
import { ProjectInsightQuestionDetails } from '../models/projectInsightQuestionDetails';
import { ProjectInsightGroupDetails } from '../models/projectInsightGroupDetails';
import { ProjectInsightProjectDetails } from '../models/projectInsightDetails';
interface ProjectSectionData {
  projectInsightProjectDetails: ProjectInsightProjectDetails;
  fields: FormField[];
  questions: ProjectInsightQuestionDetails[];
  groups: Record<string, GroupSectionData>;
  createdBy: any;
}

interface ExcelRow {
  SrNo: any,
  Section: any;
  Title: any;
  OptionType: any;
  Option: any;
  Value: any;
  FieldWidth: any;
  IsMultiSelect: any;
  Required: any;
}

interface GroupSectionData {
  projectInsightGroupDetails: ProjectInsightGroupDetails;
  fields: FormField[];
  questions: ProjectInsightQuestionDetails[];
  subGroups: Record<string, GroupSectionData>;
}

type tableColumnDataTypes = "Text" | "Number" | "Date";

interface ParsedColumn {
  name: string;
  type: tableColumnDataTypes;
}

@Injectable({
  providedIn: 'root'
})
export class ProjectInsightImportExportService {

  private allowedOptionTypes = ['text', 'textarea', 'select', 'checkbox', 'radio', 'date', 'number', 'email'];
  private allowedListOptionTypes = ['select', 'checkbox', 'radio'];
  private allowedGroupTypes = ['milestone', 'feature', 'activity', 'tasks'];
  private headers = ["Section", "Title", "FieldWidth", "Required", "OptionType", "IsMultiSelect", "Option", "Value"];

  constructor(private validationService: ValidationService, private projectService: ProjectService
    , private projectInsightService: ProjectInsightService, private employeeService: EmployeeService
  ) { }

  private sheetMapping = {
    Project: { key: 'projectData', mapFn: this.mapProjectInsightDetails.bind(this) },
    Milestones: { key: 'milestones', mapFn: this.mapProjectInsightMilestoneList.bind(this) },
    Modules: { key: 'modules', mapFn: this.mapProjectInsightModuleList.bind(this) },
    SubModules: { key: 'subModules', mapFn: this.mapProjectInsightSubModuleList.bind(this) },
    'Sub-SubModules': { key: 'subSubModules', mapFn: this.mapProjectInsightSubSubModuleList.bind(this) },
    Questions: { key: 'questions', mapFn: this.mapProjectInsightQuestions.bind(this) },
  };


  async exportProjectInsightToExcel(entity: any, entityType: any, name: any, parentId: any): Promise<any> {
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    const sheetsData: { [sheetName: string]: any[] } = {
      'Project': [],
      'Milestones': [],
      'Modules': [],
      'SubModules': [],
      'Sub-SubModules': [],
      'Questions': []
    };

    // Helper function to add data if available
    const addSheetIfData = (data: any[], sheetName: string) => {
      if (data.length > 0) {
        const ws = XLSX.utils.json_to_sheet(data);
        const columnWidths = Object.keys(data[0]).map(key => {
          const maxLength = Math.max(
            key.length,
            ...data.map(row => (row[key] ? row[key].toString().length : 0))
          );
          return { wch: maxLength + 2 }; // add padding
        });
        ws['!cols'] = columnWidths;
        XLSX.utils.book_append_sheet(workbook, ws, sheetName);
      }
    };

    let employeeList = await this.getEmployeeList();

    const processEntity = (entity: any, type: string, parentId: any, parentType: any) => {
      switch (type) {
        case 'Project':
          sheetsData['Project'].push({ ProjectId: entity?.projectId, ProjectName: entity?.projectName, });
          break;
        case 'Milestone':
          sheetsData['Milestones'].push(...this.getMilestoneList([entity], parentId, parentType, employeeList));
          break;
        case 'Module':
          sheetsData['Modules'].push(...this.getModuleList([entity], parentId, parentType, employeeList));
          break;
        case 'SubModule':
          sheetsData['SubModules'].push(...this.getSubModuleList([entity], parentId, parentType, employeeList));
          break;
        case 'Sub-SubModule':
          sheetsData['Sub-SubModules'].push(...this.getSubSubModuleList([entity], parentId, parentType, employeeList));
          break;
        case 'Question':
          sheetsData['Questions'].push(...this.getQuestionList([entity], parentId, parentType));
          break;
      }

      // Handle Questions
      if (entity?.questionList) {
        sheetsData['Questions'].push(...this.getQuestionList(entity.questionList, entity[this.getIdField(type)], type));
      }

      // Handle Child Entities
      if (entity?.projectInsightMilestoneList) {
        entity.projectInsightMilestoneList.forEach((milestone) => processEntity(milestone, 'Milestone', entity[this.getIdField(type)], type));
      }
      if (entity?.moduleList) {
        entity.moduleList.forEach((module) => processEntity(module, 'Module', entity[this.getIdField(type)], type));
      }
      if (entity?.subModuleList) {
        entity.subModuleList.forEach((subModule) => processEntity(subModule, 'SubModule', entity[this.getIdField(type)], type));
      }
      if (entity?.subSubModuleList) {
        entity.subSubModuleList.forEach((subSubModule) => processEntity(subSubModule, 'Sub-SubModule', entity[this.getIdField(type)], type));
      }
    };

    // Start Processing
    processEntity(entity, entityType, parentId, this.getParentType(entityType));

    // Add sheets
    for (const sheetName in sheetsData) {
      addSheetIfData(sheetsData[sheetName], sheetName);
    }

    // Save
    XLSX.writeFile(workbook, `Project_Insight_${name}.xlsx`, {
      bookType: 'xlsx',
    });
  }

  async exportEntityDetailOrQuestionsToExcel(entity: any, entityType: any, name: any, parentId: any, parentType: any): Promise<any> {
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    const sheetsData: { [sheetName: string]: any[] } = {
      'Project': [],
      'Milestones': [],
      'Modules': [],
      'SubModules': [],
      'Sub-SubModules': [],
      'Questions': []
    };

    let employeeList = await this.getEmployeeList();

    // Helper function to add data if available
    const addSheetIfData = (data: any[], sheetName: string) => {
      if (data.length > 0) {
        const ws = XLSX.utils.json_to_sheet(data);
        const columnWidths = Object.keys(data[0]).map(key => {
          const maxLength = Math.max(
            key.length,
            ...data.map(row => (row[key] ? row[key].toString().length : 0))
          );
          return { wch: maxLength + 2 }; // add padding
        });
        ws['!cols'] = columnWidths;
        XLSX.utils.book_append_sheet(workbook, ws, sheetName);
      }
    };

    if (entityType?.includes('Question')) {
      sheetsData['Questions'].push(...this.getQuestionListForQuestionsExcel(entity));
    } else if (entityType?.includes('Details')) {
      switch (parentType) {
        case 'Project':
          sheetsData['Project'].push({ ProjectId: entity?.projectId, ProjectName: entity?.projectName });
          break;
        case 'Milestone':
          sheetsData['Milestones'].push(...this.getMilestoneList([entity], parentId, 'Project', employeeList));
          break;
        case 'Module':
          sheetsData['Modules'].push(...this.getModuleList([entity], parentId, 'Milestone', employeeList));
          break;
        case 'SubModule':
          sheetsData['SubModules'].push(...this.getSubModuleList([entity], parentId, 'Module', employeeList));
          break;
        case 'Sub-SubModule':
          sheetsData['Sub-SubModules'].push(...this.getSubSubModuleList([entity], parentId, 'Sub-Module', employeeList));
          break;
      }
    }
    // Add sheets
    for (const sheetName in sheetsData) {
      addSheetIfData(sheetsData[sheetName], sheetName);
    }

    // Save
    XLSX.writeFile(workbook, `Project_Insight_${name}.xlsx`);
  }

  async exportProjectInsightEntityQuestionsToExcel(entity: any, entityType: any, projectId: any, projectName: any): Promise<any> {
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    const sheetsData: { [sheetName: string]: any[] } = { 'Project': [], 'Questions': [] };
    sheetsData['Project'].push({ ProjectId: projectId, ProjectName: projectName });
    if (entityType == 'Question') {
      sheetsData['Questions'].push(...this.getQuestionListForQuestionsAndResponseExcel(entity));
    } else {
      sheetsData['Questions'].push(...this.getQuestionListByEntityForQuestionsAndResponseExcel(entity, entityType));
    }
    // Helper function to add data if available
    const addSheetIfData = (data: any[], sheetName: string) => {
      if (data.length > 0) {
        const ws = XLSX.utils.json_to_sheet(data);
        const columnWidths = Object.keys(data[0]).map(key => {
          const maxLength = Math.max(
            key.length,
            ...data.map(row => (row[key] ? row[key].toString().length : 0))
          );
          return { wch: maxLength + 2 }; // add padding
        });
        ws['!cols'] = columnWidths;
        XLSX.utils.book_append_sheet(workbook, ws, sheetName);
      }
    };

    // Add sheets
    for (const sheetName in sheetsData) {
      addSheetIfData(sheetsData[sheetName], sheetName);
    }
    // Save
    XLSX.writeFile(workbook, `Project_Insight_Questions_${name}.xlsx`);
  }

  // GETTER AND SETTER FUNCTIONS 

  getMilestoneList(projectInsightMilestoneList: any, parentId: any, parentType: any, employeeList: any) {
    let milestoneSheet: any[] = [];
    projectInsightMilestoneList?.forEach((milestone) => {
      milestoneSheet.push({
        MilestoneId: milestone.milestoneId,
        Milestone: milestone.milestone,
        Description: milestone.description,
        ParentId: parentId,
        ParentType: parentType,
        AssignedToUserId: this.parseAssignedToUserIdAndReturnEmpId(milestone.assignedToUserId, employeeList),
        ActionType: 'Update'
      });
    });
    return milestoneSheet;
  }

  getModuleList(moduleList: any, parentId: any, parentType: any, employeeList: any[]) {
    let moduleSheet: any[] = [];
    moduleList?.forEach((module) => {
      moduleSheet.push({
        ModuleId: module.moduleId,
        Module: module.module,
        Description: module.description,
        ParentId: parentId,
        ParentType: parentType,
        AssignedToUserId: this.parseAssignedToUserIdAndReturnEmpId(module.assignedToUserId, employeeList),
        ActionType: 'Update'
      });
    });
    return moduleSheet;
  }

  getSubModuleList(subModuleList: any, parentId: any, parentType: any, employeeList: any[]) {
    let subModuleSheet: any[] = [];
    subModuleList?.forEach((subModule) => {
      subModuleSheet.push({
        SubModuleId: subModule.subModuleId,
        SubModule: subModule.subModule,
        Description: subModule.description,
        ParentId: parentId,
        ParentType: parentType,
        AssignedToUserId: this.parseAssignedToUserIdAndReturnEmpId(subModule.assignedToUserId, employeeList),
        ActionType: 'Update'
      });
    });
    return subModuleSheet;
  }

  getSubSubModuleList(subModuleList: any, parentId: any, parentType: any, employeeList: any[]) {
    let subModuleSheet: any[] = [];
    subModuleList?.forEach((subModule) => {
      subModuleSheet.push({
        SubModuleId: subModule.subModuleId ? subModule.subModuleId : subModule.subModuleId,
        SubModule: subModule.subModule,
        Description: subModule.description,
        ParentId: parentId,
        ParentType: parentType,
        AssignedToUserId: this.parseAssignedToUserIdAndReturnEmpId(subModule.assignedToUserId, employeeList),
        ActionType: 'Update'
      });
      if (this.isValidList(subModule?.subSubModuleList)) {
        subModuleSheet.push(...this.getSubSubModuleList(subModule?.subSubModuleList, subModule.subModuleId, 'Sub-SubModule', employeeList));
      }
    });
    return subModuleSheet;
  }

  // getSubSubModuleList2(subModuleList: any, parentId: any, subModuleType: any, subSubModuleSheet: any, questionSheet: any) {
  //   subSubModuleSheet.push(...this.getSubModuleList(subModuleList, parentId, subModuleType));
  //   subModuleList.forEach((subModule, subModuleIndex) => {
  //     if (subModule?.questionList) {
  //       questionSheet.push(...this.getQuestionList(subModule?.questionList, subModule?.subModuleId, 'Sub-SubModule'));
  //     }
  //     if (subModule?.subSubModuleList) {
  //       this.getSubSubModuleList(subModule?.subSubModuleList, subModule.subModuleId, 'Sub-SubModule', subSubModuleSheet, questionSheet);
  //     }
  //   });
  // }

  getQuestionList(questionList: any, parentId: any, parentType: any) {
    let questionSheet: any[] = [];
    questionList?.forEach((question) => {
      questionSheet.push({
        QuestionId: question.questionId,
        Question: question.question,
        Description: question.description,
        OptionType: question.optionType,
        // Options: question.optionType && question.optionType != 'text' ? JSON.stringify(this.parseOptionsToList(question.optionType, question.options)) : null,
        ParentId: parentId,
        ParentType: parentType,
        ActionType: 'Update'
      });
    });
    return questionSheet;
  }

  getQuestionListForQuestionsExcel(questionList: any) {
    let questionSheet: any[] = [];
    questionList?.forEach((question) => {
      questionSheet.push({
        QuestionId: question.questionId,
        Question: question.question,
        Description: question.description,
        OptionType: question.optionType,
        // Options: question.optionType && question.optionType != 'text' ? JSON.stringify(this.parseOptionsToList(question.optionType, question.options)) : null,
        ParentId: question.entityId,
        ParentType: question.entityType,
        ActionType: 'Update'
      });
    });
    return questionSheet;
  }

  getMilestoneQuestionList(projectInsightMilestoneList: any) {
    let questionList: any[] = [];
    for (let entity of projectInsightMilestoneList) {
      if (this.isValidList(entity?.questionList)) {
        questionList.push(...this.getQuestionListForQuestionsAndResponseExcel(entity?.questionList));
      }
      if (this.isValidList(entity?.moduleList)) {
        questionList.push(...this.getModuleQuestionList(entity.moduleList));
      }
    }
    return questionList;
  }

  getModuleQuestionList(moduleList: any) {
    let questionList: any[] = [];
    for (let entity of moduleList) {
      if (this.isValidList(entity?.questionList)) {
        questionList.push(...this.getQuestionListForQuestionsAndResponseExcel(entity?.questionList));
      }
      if (this.isValidList(entity?.subModuleList)) {
        questionList.push(...this.getSubModuleQuestionList(entity.subModuleList));
      }
    }
    return questionList;
  }

  getSubModuleQuestionList(subModuleList: any) {
    let questionList: any[] = [];
    for (let entity of subModuleList) {
      if (this.isValidList(entity?.questionList)) {
        questionList.push(...this.getQuestionListForQuestionsAndResponseExcel(entity?.questionList));
      }
      if (this.isValidList(entity?.subSubModuleList)) {
        questionList.push(...this.getSubSubModuleQuestionList(entity.subSubModuleList));
      }
    }
    return questionList;
  }

  getSubSubModuleQuestionList(subSubModuleList: any) {
    let questionList: any[] = [];
    for (let entity of subSubModuleList) {
      if (this.isValidList(entity?.questionList)) {
        questionList.push(...this.getQuestionListForQuestionsAndResponseExcel(entity?.questionList));
      }
      if (this.isValidList(entity?.subSubModuleList)) {
        questionList.push(...this.getSubSubModuleQuestionList(entity.subSubModuleList));
      }
    }
    return questionList;
  }

  getQuestionListByEntityForQuestionsAndResponseExcel(entity: any, entityType: any) {
    let questionList: any[] = [];
    if (this.isValidList(entity?.questionList)) {
      questionList.push(...this.getQuestionListForQuestionsAndResponseExcel(entity?.questionList));
    }
    if (entityType == 'Project') {
      if (this.isValidList(entity?.projectInsightMilestoneList)) {
        questionList.push(...this.getMilestoneQuestionList(entity?.projectInsightMilestoneList));
      }
    }
    else if (entityType == 'Milestone') {
      if (this.isValidList(entity?.moduleList)) {
        questionList.push(...this.getModuleQuestionList(entity?.moduleList));
      }
    }
    else if (entityType == 'Module') {
      if (this.isValidList(entity?.subModuleList)) {
        questionList.push(...this.getSubModuleQuestionList(entity?.subModuleList));
      }
    }
    else if (entityType == 'SubModule') {
      if (this.isValidList(entity?.subSubModuleList)) {
        questionList.push(...this.getSubSubModuleQuestionList(entity?.subSubModuleList));
      }
    }
    else if (entityType == 'Sub-SubModule') {
      if (this.isValidList(entity?.subSubModuleList)) {
        questionList.push(...this.getSubSubModuleQuestionList(entity?.subSubModuleList));
      }
    }
    return questionList;
  }

  getQuestionListForQuestionsAndResponseExcel(questionList: any) {
    let questionSheet: any[] = [];
    questionList?.forEach((question) => {
      questionSheet.push({
        QuestionId: question.questionId,
        Question: question.question,
        Description: question.description,
        OptionType: question.optionType,
        // Options: question.optionType && question.optionType != 'text' ? JSON.stringify(this.parseOptionsToList(question.optionType, question.options)) : null,
        EntityId: question.entityId,
        EntityType: question.entityType,
        OptionsList: question.optionsList,
        Response: this.parseResponse(question.projectResponseList)
      });
    });
    return questionSheet;
  }

  getQuestionListByEntityForResponseExcel(entity: any, entityType: any) {
    let questionList: any[] = [];
    if (this.isValidList(entity?.questionList)) {
      for (let projQuestion of entity?.questionList) {
        questionList.push(projQuestion);
      }
    }
    if (this.isValidList(entity?.projectInsightMilestoneList)) {
      for (let milestone of entity?.projectInsightMilestoneList) {
        if (this.isValidList(milestone?.questionList)) {
          for (let milestoneQuestion of milestone?.questionList) {
            questionList.push(milestoneQuestion);
          }
        }

        if (this.isValidList(milestone?.moduleList)) {
          for (let module of milestone?.moduleList) {
            if (this.isValidList(module?.questionList)) {
              for (let moduleQuestion of module?.questionList) {
                questionList.push(moduleQuestion);
              }
            }

            if (this.isValidList(module?.subModuleList)) {
              for (let subModule of module?.subModuleList) {
                if (this.isValidList(subModule?.questionList)) {
                  for (let subModuleQuestion of subModule?.questionList) {
                    questionList.push(subModuleQuestion);
                  }
                }
                questionList.push(...this.getSubModuleQuestions(subModule?.subSubModuleList));
              }
            }
          }
        }
      }
    }
    return questionList;
  }

  getSubModuleQuestions(subSubModuleList: any[]) {
    let questionList: any[] = [];
    if (this.isValidList(subSubModuleList)) {
      for (let subModule of subSubModuleList) {
        if (this.isValidList(subModule?.questionList)) {
          for (let subModuleQuestion of subModule?.questionList) {
            questionList.push(subModuleQuestion);
          }
        }
        if (this.isValidList(subModule?.subSubModuleList)) {
          questionList.push(...this.getSubModuleQuestions(subModule?.subSubModuleList));
        }
      }
    }
    return questionList;
  }

  mapProjectInsightDetails(sheetData: any, dataHeaders: any) {
    let projectInsight: ProjectInsight = new ProjectInsight();
    if (sheetData) {
      let projectInsightData = sheetData[0];
      projectInsight.projectId = projectInsightData.ProjectId;
      projectInsight.projectName = projectInsightData.ProjectName;
    }
    return projectInsight;
  }

  mapProjectInsightMilestoneList(sheetData: any, dataHeaders: any, employeeList: any[]) {
    let milestoneSheet: any[] = [];
    if (sheetData) {
      for (let index = 0; index < sheetData?.length; index++) {
        let milestoneData = sheetData[index];
        let projectMilestone: ProjectMilestone = new ProjectMilestone();
        projectMilestone.milestone = milestoneData.Milestone;
        projectMilestone.milestoneId = milestoneData.MilestoneId;
        projectMilestone.description = milestoneData.Description;
        projectMilestone.assignedToUserId = this.parseAssignedToUserId(milestoneData.AssignedToUserId, employeeList);
        projectMilestone.projectId = milestoneData?.ParentId;
        projectMilestone.actionType = milestoneData?.ActionType;
        milestoneSheet.push(projectMilestone);
      }
    }
    return milestoneSheet;
  }

  mapProjectInsightModuleList(sheetData: any, dataHeaders: any, employeeList: any[]) {
    let moduleSheet: any[] = [];
    if (sheetData) {
      for (let index = 0; index < sheetData?.length; index++) {
        let moduleData = sheetData[index];
        let projectModule: ProjectModule = new ProjectModule();
        projectModule.moduleId = moduleData.ModuleId;
        projectModule.module = moduleData.Module;
        projectModule.description = moduleData.Description;
        projectModule.milestoneId = moduleData.ParentId;
        projectModule.assignedToUserId = this.parseAssignedToUserId(moduleData.AssignedToUserId, employeeList);
        projectModule.actionType = moduleData?.ActionType;
        moduleSheet.push(projectModule);
      }
    }
    return moduleSheet;
  }

  mapProjectInsightSubModuleList(sheetData: any, dataHeaders: any, employeeList: any[]) {
    let subModuleSheet: any[] = [];
    if (sheetData) {
      for (let index = 0; index < sheetData?.length; index++) {
        let subModuleData = sheetData[index];
        let subModule: ProjectSubModule = new ProjectSubModule();
        subModule.subModuleId = subModuleData.SubModuleId;
        subModule.subModule = subModuleData.SubModule;
        subModule.description = subModuleData.Description;
        subModule.moduleId = subModuleData.ParentId;
        subModule.assignedToUserId = this.parseAssignedToUserId(subModuleData.AssignedToUserId, employeeList);
        subModule.actionType = subModuleData?.ActionType;
        subModuleSheet.push(subModule);
      }
    }
    return subModuleSheet;
  }

  mapProjectInsightSubSubModuleList(sheetData: any, dataHeaders: any, employeeList: any[]) {
    let subModuleSheet: any[] = [];
    if (sheetData) {
      for (let index = 0; index < sheetData?.length; index++) {
        let subModuleData = sheetData[index];
        let subModule: ProjectSubModule = new ProjectSubModule();
        subModule.subModuleId = subModuleData.SubModuleId;
        subModule.subModule = subModuleData.SubModule;
        subModule.description = subModuleData.Description;
        subModule.moduleId = subModuleData.ParentId;
        subModule.assignedToUserId = this.parseAssignedToUserId(subModuleData.AssignedToUserId, employeeList);
        subModule.actionType = subModuleData?.ActionType;
        subModuleSheet.push(subModule);
      }
    }
    return subModuleSheet;
  }

  mapProjectInsightQuestions(sheetData: any, dataHeaders: any) {
    let questionSheet: any[] = [];
    if (sheetData) {
      for (let index = 0; index < sheetData?.length; index++) {
        let questionData = sheetData[index];
        let question: ProjectQuestion = new ProjectQuestion();
        question.questionId = questionData?.QuestionId;
        question.question = questionData?.Question;
        question.description = questionData?.Description;
        question.optionType = questionData?.OptionType;
        let options = this.parseListToOptions(questionData?.OptionType, questionData?.Options);
        question.options = questionData?.OptionType != 'text' && options != null ? JSON.stringify(options) : null;
        question.optionsList = questionData?.OptionType != 'text' ? options : null;
        question.currentActiveBadgeLevel = 'Details';
        question.entityId = questionData?.ParentId ? questionData?.ParentId : questionData?.EntityId;
        question.entityType = questionData?.ParentType ? questionData?.ParentType : questionData?.EntityType;
        question.actionType = questionData?.ActionType;
        question.response = questionData?.Response;
        questionSheet.push(question);
      }
    }
    return questionSheet;
  }


  // VALIDATION FUNCTIONS

  async getExcelSheetNames(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        const data = new Uint8Array(e.target.result);
        const workbook = XLSX.read(data, { type: 'array' });
        // Get sheet names
        const sheetNames = workbook.SheetNames;

        const hierarchy = ['Milestone', 'Module', 'SubModule', 'Sub-SubModule', 'Question'];
        const entityType = this.getHighestHierarchy(hierarchy, sheetNames);

        resolve(entityType);
      };
      reader.onerror = (error) => reject(error);
      reader.readAsArrayBuffer(file);
    });
  }

  getHighestHierarchy(hierarchy: string[], sheetNames: string[]): string | null {
    const normalizedSheetNames = sheetNames.map(name =>
      name.toLowerCase().endsWith('s') ? name.slice(0, -1).toLowerCase() : name.toLowerCase()
    );
    for (const level of hierarchy) {
      const normalizedLevel = level.toLowerCase();
      if (normalizedSheetNames.includes(normalizedLevel)) {
        return level;
      }
    }
    return null;
  }

  isValidList(list: any[]): boolean {
    return this.validationService.validateNullUndefinedEmptyList(list);
  }

  isValidJsonArray(jsonString: string): boolean {
    try {
      const parsed = JSON.parse(jsonString);
      return Array.isArray(parsed);
    } catch (e) {
      return false;
    }
  }

  async validateProjectInsightImportFromExcel(file: any): Promise<any> {
    return new Promise(async (resolve, reject) => {
      try {
        const sheetDataMap = await this.extractAllSheetData(file);
        const { projectData, milestoneList, moduleList, subModuleList, subSubModuleList, questionsList } = sheetDataMap;
        if (!projectData) {
          resolve('Excel Must contain Project Level Data.');
        }

        let error = await this.validateProjectInsight(projectData);
        if (error) {
          resolve(error);
        }

        const v = this.validationService;  // shorthand
        const isEmpty = (list: any) => v.validateNullUndefinedEmptyList(list);

        if (!isEmpty(milestoneList) && (isEmpty(moduleList) || isEmpty(subModuleList) || isEmpty(subSubModuleList))) {
          resolve('At least one Milestone is required before adding Modules/SubModules/Sub-SubModules.');
        }
        if (!isEmpty(moduleList) && (isEmpty(subModuleList) || isEmpty(subSubModuleList))) {
          resolve('At least one Module is required before adding SubModules/Sub-SubModules.');
        }
        if (!isEmpty(subModuleList) && isEmpty(subSubModuleList)) {
          resolve('At least one SubModule is required before adding Sub-SubModules.');
        }

        const validationError = this.firstNonNull([
          this.validateProjectInsightMilestoneList(milestoneList, projectData?.projectId),
          this.validateProjectInsightModuleList(moduleList, milestoneList),
          this.validateProjectInsightSubModuleList(subModuleList, moduleList),
          this.validateProjectInsightSubSubModuleList(subSubModuleList, subModuleList),
          this.validateProjectInsightQuestions(projectData, milestoneList, moduleList, subModuleList, subSubModuleList, questionsList)
        ]);

        return resolve(validationError || 'Success');
      } catch (error) {
        reject(error);
      }
    });
  }

  async validateProjectInsight(projectData: any): Promise<string | null> {
    if (projectData) {
      if (!this.validationService.validateNullUndefinedEmptyString(projectData?.projectName)) {
        return 'Kindly enter the Project Name in the Project sheet.';
      }

      let projectObj = new Project();
      projectObj.projectName = projectData?.projectName.trim();
      try {
        const response: any = await this.projectService.getProjectByName(projectObj).pipe(first()).toPromise();
        if (response.serviceStatus === "Success") {
          const obj = response.serviceResponse;
          if (!this.validationService.validateNullUndefinedEmptyString(projectData?.projectId)) {
            projectData.projectId = obj.projectId;
          }
          if (this.validationService.validateNullUndefinedEmptyString(projectData?.projectId) && obj.projectId !== projectData?.projectId) {
            return 'Kindly enter a valid Project ID matching the Project Name in the Project sheet.';
          }
        } else {
          return response.serviceResponse;
        }
      } catch (error) {
        return 'Error occurred while validating project name.';
      }
    }
    return null;
  }

  validateProjectInsightMilestoneList(projectInsightMilestoneList: any, projectId: any): any {
    if (this.validationService.validateNullUndefinedEmptyList(projectInsightMilestoneList)) {
      let milestoneIdList: any[] = [];
      milestoneIdList = projectInsightMilestoneList.map(milestone => milestone?.milestoneId);
      const duplicates = milestoneIdList.filter((id, index, self) =>
        id !== null && self.indexOf(id) !== index && self.lastIndexOf(id) === index
      );
      if (this.validationService.validateNullUndefinedEmptyList(duplicates)) {
        return `Kindly enter unique values for the Milestone ID in the Milestones sheet for ${duplicates}`;
      }
      for (let milestoneIndex = 0; milestoneIndex < projectInsightMilestoneList?.length; milestoneIndex++) {
        const milestone = projectInsightMilestoneList[milestoneIndex];
        if (!this.validationService.validateNullUndefinedEmptyString(milestone?.milestoneId)) {
          return `Kindly enter the Milestone ID in the Milestones sheet for ${milestoneIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(milestone?.milestone)) {
          return `Kindly enter the Milestone in the Milestones sheet for ${milestoneIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyList(milestone?.assignedToUserId)) {
          return `Kindly provide Assign User in the Milestones sheet for ${milestoneIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(milestone?.projectId)) {
          return `Kindly enter the Parent ID in the Milestones sheet for ${milestoneIndex + 1}`;
        }
        if (this.validationService.validateNullUndefinedEmptyString(projectId) && projectId !== milestone?.projectId) {
          return `Valid Parent ID not found in the Milestones sheet for ${milestoneIndex + 1}`;
        }
      }
    }
  }

  validateProjectInsightModuleList(moduleList: any, projectInsightMilestoneList: any): any {
    if (this.validationService.validateNullUndefinedEmptyList(moduleList)) {
      let moduleIdList: any[] = [];
      moduleIdList = moduleList.map(module => module?.moduleId);
      const duplicates = moduleIdList.filter((id, index, self) =>
        id !== null && self.indexOf(id) !== index && self.lastIndexOf(id) === index
      );
      if (this.validationService.validateNullUndefinedEmptyList(duplicates)) {
        return `Kindly enter unique values for the Module ID in the Modules sheet for ${duplicates}`;
      }

      let milestoneIdList: any[] = [];
      if (this.validationService.validateNullUndefinedEmptyList(projectInsightMilestoneList)) {
        milestoneIdList = projectInsightMilestoneList.map(milestone => milestone?.milestoneId);
      }
      for (let moduleIndex = 0; moduleIndex < moduleList?.length; moduleIndex++) {
        const module = moduleList[moduleIndex];
        if (!this.validationService.validateNullUndefinedEmptyString(module?.moduleId)) {
          return `Kindly enter the Module ID in the Modules sheet for ${moduleIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(module?.module)) {
          return `Kindly enter the Module in the Modules sheet for ${moduleIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyList(module?.assignedToUserId)) {
          return `Kindly provide Assign User in the Modules sheet for ${moduleIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(module?.milestoneId)) {
          return `Kindly enter the Parent ID in the Modules sheet for ${moduleIndex + 1}`;
        }
        if (this.validationService.validateNullUndefinedEmptyList(milestoneIdList) && !milestoneIdList.includes(module?.milestoneId)) {
          return `Valid Parent ID not found in the Modules sheet for ${moduleIndex + 1}`;
        }
      }
    }
  }

  validateProjectInsightSubModuleList(subModuleList: any, moduleList: any): any {
    if (this.validationService.validateNullUndefinedEmptyList(subModuleList)) {
      let subModuleIdList: any[] = [];
      subModuleIdList = subModuleList.map(subModule => subModule?.subModuleId);
      const duplicates = subModuleIdList.filter((id, index, self) =>
        id !== null && self.indexOf(id) !== index && self.lastIndexOf(id) === index
      );
      if (this.validationService.validateNullUndefinedEmptyList(duplicates)) {
        return `Kindly enter unique values for the SubModule ID in the SubModules sheet for ${duplicates}`;
      }

      let moduleIdList: any[] = [];
      if (this.validationService.validateNullUndefinedEmptyList(moduleList)) {
        moduleIdList = moduleList.map(module => module?.moduleId);
      }
      for (let subModuleIndex = 0; subModuleIndex < subModuleList?.length; subModuleIndex++) {
        const subModule = subModuleList[subModuleIndex];
        if (!this.validationService.validateNullUndefinedEmptyString(subModule?.subModuleId)) {
          return `Kindly enter the SubModule ID in the SubModules sheet for ${subModuleIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(subModule?.subModule)) {
          return `Kindly enter the SubModule in the SubModules sheet for ${subModuleIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyList(subModule?.assignedToUserId)) {
          return `Kindly provide Assign User in the SubModules sheet for ${subModuleIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(subModule?.moduleId)) {
          return `Kindly enter the Parent ID in the SubModules sheet for ${subModuleIndex + 1}`;
        }
        if (this.validationService.validateNullUndefinedEmptyList(moduleIdList) && !moduleIdList.includes(subModule?.moduleId)) {
          return `Valid Parent ID not found in the SubModules sheet for ${subModuleIndex + 1}`;
        }
      }
    }
  }

  validateProjectInsightSubSubModuleList(subSubModuleList: any, subModuleList: any): any {
    if (this.validationService.validateNullUndefinedEmptyList(subSubModuleList)) {
      let subSubModuleIdList: any[] = [];
      subSubModuleIdList = subSubModuleList.map(subModule => subModule?.subModuleId);
      const duplicates = subSubModuleIdList.filter((id, index, self) =>
        id !== null && self.indexOf(id) !== index && self.lastIndexOf(id) === index
      );
      if (this.validationService.validateNullUndefinedEmptyList(duplicates)) {
        return `Kindly enter unique values for the Sub-SubModule ID in the Sub-SubModules sheet for ${duplicates}`;
      }

      let subModuleIdList: any[] = [];
      if (this.validationService.validateNullUndefinedEmptyList(subModuleList)) {
        subModuleIdList = subModuleList.map(subModule => subModule?.subModuleId);
        subModuleIdList = [...subSubModuleList.map(subSubModule => subSubModule?.moduleId)];
      }
      for (let subModuleIndex = 0; subModuleIndex < subSubModuleList?.length; subModuleIndex++) {
        const subModule = subSubModuleList[subModuleIndex];
        if (!this.validationService.validateNullUndefinedEmptyString(subModule?.subModuleId)) {
          return `Kindly enter the SubModule ID in the Sub-SubModules sheet for ${subModuleIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(subModule?.subModule)) {
          return `Kindly enter the SubModule in the Sub-SubModules sheet for ${subModuleIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyList(subModule?.assignedToUserId)) {
          return `Kindly provide Assign User in the Sub-SubModules sheet for ${subModuleIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(subModule?.moduleId)) {
          return `Kindly enter the Parent ID in the Sub-SubModules sheet for ${subModuleIndex + 1}`;
        }
        if (this.validationService.validateNullUndefinedEmptyList(subModuleIdList) && !subModuleIdList.includes(subModule?.moduleId)) {
          return `Valid Parent ID not found in the Sub-SubModules sheet for ${subModuleIndex + 1}`;
        }
      }
    }
  }

  validateProjectInsightQuestions(projectData: ProjectInsight, projectInsightMilestoneList: any[], moduleList: any[], subModuleList: any[], subSubModuleList: any[], questionsList: any[]): any {
    if (this.validationService.validateNullUndefinedEmptyList(questionsList)) {
      let questionIdList: any[] = [];
      questionIdList = questionIdList.map(question => question?.questionId);
      const duplicates = questionIdList.filter((id, index, self) =>
        id !== null && self.indexOf(id) !== index && self.lastIndexOf(id) === index
      );
      if (this.validationService.validateNullUndefinedEmptyList(duplicates)) {
        return `Kindly enter unique values for the Question ID in the Questions sheet for ${duplicates}`;
      }
      let milestoneIdList: any[] = [];
      if (this.validationService.validateNullUndefinedEmptyList(projectInsightMilestoneList)) {
        milestoneIdList = projectInsightMilestoneList.map(milestone => milestone?.milestoneId);
      }
      let moduleIdList: any[] = [];
      if (this.validationService.validateNullUndefinedEmptyList(moduleList)) {
        moduleIdList = moduleList.map(module => module?.moduleId);
      }

      let subModuleIdList: any[] = [];
      if (this.validationService.validateNullUndefinedEmptyList(subModuleList)) {
        subModuleIdList = subModuleList.map(subModule => subModule?.subModuleId);
      }

      let subSubModuleIdList: any[] = [];
      if (this.validationService.validateNullUndefinedEmptyList(subSubModuleList)) {
        subSubModuleIdList = subSubModuleList.map(subModule => subModule?.subModuleId);
      }

      for (let questionIndex = 0; questionIndex < questionsList?.length; questionIndex++) {
        const question = questionsList[questionIndex];
        if (!this.validationService.validateNullUndefinedEmptyString(question?.questionId)) {
          return `Kindly enter the Question ID in the Questions sheet for ${questionIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(question?.question)) {
          return `Kindly enter the Question in the Questions sheet for ${questionIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(question?.optionType)) {
          return `Kindly enter the Option Type in the Questions sheet for ${questionIndex + 1}`;
        }
        if (question?.optionType != 'text' && question?.optionType != 'checkbox' && question?.optionType != 'radio') {
          return `Kindly enter the proper Option Type(checkbox, radio, text) in the Questions sheet for ${questionIndex + 1}`;
        }
        if (question?.optionType == 'radio' || question?.optionType == 'checkbox') {
          if (!this.validationService.validateNullUndefinedEmptyString(question?.options)) {
            return `Kindly provide Options in the Questions sheet for ${questionIndex + 1}`;
          }
          const options = JSON.parse(question?.options);
          const isValid = Array.isArray(options);
          if (!isValid) {
            return `Kindly provide valid Options in the Questions sheet for ${questionIndex + 1}`;
          }
        }
        if (!this.validationService.validateNullUndefinedEmptyString(question?.entityId)) {
          return `Kindly enter the Parent ID in the Questions sheet for ${questionIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(question?.entityType)) {
          return `Kindly enter the Parent Type in the Questions sheet for ${questionIndex + 1}`;
        }
        if (question?.entityType == 'Project' && question?.entityId != projectData?.projectId) {
          return `Kindly enter valid Parent ID for the Entity Type Project in the Questions sheet for ${questionIndex + 1}`;
        } else if (question?.entityType == 'Milestone' && !milestoneIdList?.includes(question?.entityId)) {
          return `Kindly enter valid Parent ID for the Entity Type Milestone in the Questions sheet for ${questionIndex + 1}`;
        } else if (question?.entityType == 'Module' && !moduleIdList?.includes(question?.entityId)) {
          return `Kindly enter valid Parent ID for the Entity Type Module in the Questions sheet for ${questionIndex + 1}`;
        } else if (question?.entityType == 'SubModule' && !subModuleIdList?.includes(question?.entityId)) {
          return `Kindly enter valid Parent ID for the Entity Type SubModule in the Questions sheet for ${questionIndex + 1}`;
        } else if (question?.entityType == 'Sub-SubModule' && !subSubModuleIdList?.includes(question?.entityId)) {
          return `Kindly enter valid Parent ID for the Entity Type Sub-SubModule in the Questions sheet for ${questionIndex + 1}`;
        }
      }
    }
  }

  async validateProjectInsightEntityImportFromExcel(file: any, entityType: any, entityId?: any): Promise<any> {
    return new Promise((resolve, reject) => {
      try {
        const sheetDataMap = this.extractAllSheetData(file);
        const validationError = this.validateEntityByType(entityType, sheetDataMap, entityId);
        return resolve(validationError || 'Success');
      } catch (error) {
        reject(error);
      }
    });
  }

  validateEntityByType(entityType: string, data: any, entityId?: any): string | null {
    const { projectData, milestoneList, moduleList, subModuleList, subSubModuleList, questionsList } = data;
    const v = this.validationService;  // shorthand

    const isEmpty = (list: any) => v.validateNullUndefinedEmptyList(list);

    const validations: { [key: string]: () => string | null } = {
      Question: () => this.validateProjectInsightEntityQuestions(questionsList, entityId),

      Milestone: () => {
        if (!isEmpty(milestoneList) && (isEmpty(moduleList) || isEmpty(subModuleList) || isEmpty(subSubModuleList))) {
          return 'At least one Milestone is required before adding Modules/SubModules/Sub-SubModules.';
        }
        if (!isEmpty(moduleList) && (isEmpty(subModuleList) || isEmpty(subSubModuleList))) {
          return 'At least one Module is required before adding SubModules/Sub-SubModules.';
        }
        if (!isEmpty(subModuleList) && isEmpty(subSubModuleList)) {
          return 'At least one SubModule is required before adding Sub-SubModules.';
        }

        return this.firstNonNull([
          this.validateProjectInsightMilestoneList(milestoneList, entityId),
          this.validateProjectInsightModuleList(moduleList, milestoneList),
          this.validateProjectInsightSubModuleList(subModuleList, moduleList),
          this.validateProjectInsightSubSubModuleList(subSubModuleList, subModuleList),
          this.validateProjectInsightQuestions(projectData, milestoneList, moduleList, subModuleList, subSubModuleList, questionsList)
        ]);
      },

      Module: () => {
        if (!isEmpty(moduleList) && (isEmpty(subModuleList) || isEmpty(subSubModuleList))) {
          return 'At least one Module is required before adding SubModules/Sub-SubModules.';
        }
        if (!isEmpty(subModuleList) && isEmpty(subSubModuleList)) {
          return 'At least one SubModule is required before adding Sub-SubModules.';
        }

        return this.firstNonNull([
          this.validateProjectInsightModuleList(moduleList, milestoneList),
          this.validateProjectInsightSubModuleList(subModuleList, moduleList),
          this.validateProjectInsightSubSubModuleList(subSubModuleList, subModuleList),
          this.validateProjectInsightQuestions(projectData, milestoneList, moduleList, subModuleList, subSubModuleList, questionsList)
        ]);
      },

      SubModule: () => {
        if (!isEmpty(subModuleList) && isEmpty(subSubModuleList)) {
          return 'At least one SubModule is required before adding Sub-SubModules.';
        }

        return this.firstNonNull([
          this.validateProjectInsightSubModuleList(subModuleList, moduleList),
          this.validateProjectInsightSubSubModuleList(subSubModuleList, subModuleList),
          this.validateProjectInsightQuestions(projectData, milestoneList, moduleList, subModuleList, subSubModuleList, questionsList)
        ]);
      },

      'Sub-SubModule': () => {
        return this.firstNonNull([
          this.validateProjectInsightSubSubModuleList(subSubModuleList, subModuleList),
          this.validateProjectInsightQuestions(projectData, milestoneList, moduleList, subModuleList, subSubModuleList, questionsList)
        ]);
      }
    };

    const validateFn = validations[entityType];
    return validateFn ? validateFn() : null;
  }

  validateProjectInsightEntityQuestions(questionsList: any[], entityId: any): any {
    if (this.validationService.validateNullUndefinedEmptyList(questionsList)) {
      let questionIdList: any[] = [];
      questionIdList = questionIdList.map(question => question?.questionId);
      const duplicates = questionIdList.filter((id, index, self) =>
        id !== null && self.indexOf(id) !== index && self.lastIndexOf(id) === index
      );
      if (this.validationService.validateNullUndefinedEmptyList(duplicates)) {
        return `Kindly enter unique values for the Question ID in the Questions sheet for ${duplicates}`;
      }

      for (let questionIndex = 0; questionIndex < questionsList?.length; questionIndex++) {
        const question = questionsList[questionIndex];
        if (!this.validationService.validateNullUndefinedEmptyString(question?.questionId)) {
          return `Kindly enter the Question ID in the Questions sheet for ${questionIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(question?.question)) {
          return `Kindly enter the Question in the Questions sheet for ${questionIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(question?.optionType)) {
          return `Kindly enter the Option Type in the Questions sheet for ${questionIndex + 1}`;
        }
        if (question?.optionType != 'text' && question?.optionType != 'checkbox' && question?.optionType != 'radio') {
          return `Kindly enter the proper Option Type(checkbox, radio, text) in the Questions sheet for ${questionIndex + 1}`;
        }
        if (question?.optionType == 'radio' || question?.optionType == 'checkbox') {
          if (!this.validationService.validateNullUndefinedEmptyString(question?.options)) {
            return `Kindly provide Options in the Questions sheet for ${questionIndex + 1}`;
          }
          const options = JSON.parse(question?.options);
          const isValid = Array.isArray(options);
          if (!isValid) {
            return `Kindly provide valid Options in the Questions sheet for ${questionIndex + 1}`;
          }
        }
        if (!this.validationService.validateNullUndefinedEmptyString(question?.entityId)) {
          return `Kindly enter the Parent ID in the Questions sheet for ${questionIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyString(question?.entityType)) {
          return `Kindly enter the Parent Type in the Questions sheet for ${questionIndex + 1}`;
        }
        if (question?.entityType == 'Project' && entityId && question?.entityId != entityId) {
          return `Kindly enter valid Parent ID for the Entity Type Project in the Questions sheet for ${questionIndex + 1}`;
        } else if (question?.entityType == 'Milestone' && entityId && question?.entityId != entityId) {
          return `Kindly enter valid Parent ID for the Entity Type Milestone in the Questions sheet for ${questionIndex + 1}`;
        } else if (question?.entityType == 'Module' && entityId && question?.entityId != entityId) {
          return `Kindly enter valid Parent ID for the Entity Type Module in the Questions sheet for ${questionIndex + 1}`;
        } else if (question?.entityType == 'SubModule' && entityId && question?.entityId != entityId) {
          return `Kindly enter valid Parent ID for the Entity Type SubModule in the Questions sheet for ${questionIndex + 1}`;
        } else if (question?.entityType == 'Sub-SubModule' && entityId && question?.entityId != entityId) {
          return `Kindly enter valid Parent ID for the Entity Type Sub-SubModule in the Questions sheet for ${questionIndex + 1}`;
        }
      }
    }
  }

  // OBJECT CREATION AND CONVERSION FUNCTIONS

  async convertJsonDataToProjectInsightDetails(file: any): Promise<any> {
    const workbook = await this.readExcelFile(file);
    const sheetInfo = this.extractSheetData(workbook, 'Project');
    return sheetInfo ? this.mapProjectInsightDetails(sheetInfo.sheetData, sheetInfo.dataHeaders) : [];
  }

  async convertJsonDataToProjectInsightObj(file: any): Promise<any> {
    const workbook = await this.readExcelFile(file);
    const sheets = this.processSheets(workbook, this.sheetMapping);
    const projectData = sheets.projectData || new ProjectInsight();
    const projectInsightObj = this.createProjectInsightObj(projectData, sheets.milestones, sheets.modules, sheets.subModules, sheets.subSubModules, sheets.questions);
    return projectInsightObj;
  }

  async convertJsonDataToModuleList(file: any): Promise<any> {
    const workbook = await this.readExcelFile(file);
    const sheets = this.processSheets(workbook, this.sheetMapping);
    const projectModuleList = this.createProjectInsightModuleList(sheets.modules, sheets.subModules, sheets.subSubModules, sheets.questions);
    return projectModuleList;
  }

  async convertJsonDataToSubModuleList(file: any): Promise<any> {
    const workbook = await this.readExcelFile(file);
    const sheets = this.processSheets(workbook, this.sheetMapping);
    const projectSubModuleList = this.createProjectInsightSubModuleList(sheets.subModules, sheets.subSubModules, sheets.questions, 'SubModule');
    return projectSubModuleList;
  }

  async convertJsonDataToSubSubModuleList(file: any): Promise<any> {
    const workbook = await this.readExcelFile(file);
    const sheets = this.processSheets(workbook, this.sheetMapping);
    const projectSubSubModuleList = this.createProjectInsightSubSubModuleList(sheets.subSubModules, sheets.questions);
    return projectSubSubModuleList;
  }

  async convertJsonDataToEntityQuestionList(file: any): Promise<any> {
    const workbook = await this.readExcelFile(file);
    const sheetInfo = this.extractSheetData(workbook, 'Questions');
    return sheetInfo ? this.mapProjectInsightQuestions(sheetInfo.sheetData, sheetInfo.dataHeaders) : [];
  }

  createProjectInsightObj(projectData: ProjectInsight, projectInsightMilestoneList: any[], moduleList: any[], subModuleList: any[], subSubModuleList: any[], questionsList: any[]) {
    // if (!projectData) return null;
    const assignQuestions = (entityList: any[], entityIdKey: string, entityType: string) => {
      entityList?.forEach(entity => {
        entity.questionList = questionsList?.filter(q => q.entityId === entity[entityIdKey] && q.entityType === entityType) || [];
      });
    };

    const filterByParentId = (childList: any[], childKey: string, parentId: any) => {
      return childList?.filter(child => child[childKey] === parentId) || [];
    };

    // Project-level questions
    projectData.questionList = questionsList?.filter(q => q.entityId === projectData?.projectId && q.entityType === 'Project') || [];

    // Milestones
    projectData.projectInsightMilestoneList = projectInsightMilestoneList || [];
    assignQuestions(projectData.projectInsightMilestoneList, 'milestoneId', 'Milestone');

    projectData.projectInsightMilestoneList.forEach(milestone => {
      milestone.moduleList = filterByParentId(moduleList, 'milestoneId', milestone.milestoneId);
      assignQuestions(milestone.moduleList, 'moduleId', 'Module');

      milestone.moduleList?.forEach(module => {
        module.subModuleList = filterByParentId(subModuleList, 'moduleId', module.moduleId);
        assignQuestions(module.subModuleList, 'subModuleId', 'SubModule');

        module.subModuleList?.forEach(subModule => {
          subModule.subSubModuleList = filterByParentId(subSubModuleList, 'moduleId', subModule.subModuleId);
          assignQuestions(subModule.subSubModuleList, 'subModuleId', 'Sub-SubModule');
        });
      });
    });
    return projectData;
  }

  createProjectInsightModuleList(moduleList: any[], subModuleList: any[], subSubModuleList: any[], questionsList: any[]) {
    const assignQuestions = (entityList: any[], entityIdKey: string, entityType: string) => {
      entityList?.forEach(entity => {
        entity.questionList = questionsList?.filter(q => q.entityId === entity[entityIdKey] && q.entityType === entityType) || [];
      });
    };

    const filterByParentId = (childList: any[], childKey: string, parentId: any) => {
      return childList?.filter(child => child[childKey] === parentId) || [];
    };

    assignQuestions(moduleList, 'moduleId', 'Module');

    moduleList.forEach((module) => {
      module.subModuleList = filterByParentId(subModuleList, 'moduleId', module.moduleId);
      assignQuestions(module.subModuleList, 'subModuleId', 'SubModule');

      module.subModuleList?.forEach(subModule => {
        subModule.subSubModuleList = filterByParentId(subSubModuleList, 'moduleId', subModule.subModuleId);
        assignQuestions(subModule.subSubModuleList, 'subModuleId', 'Sub-SubModule');
      });
    });
    return moduleList;
  }

  createProjectInsightSubModuleList(subModuleList: any[], subSubModuleList: any[], questionsList: any[], subModuleType: any) {
    const assignQuestions = (entityList: any[], entityIdKey: string, entityType: string) => {
      entityList?.forEach(entity => {
        entity.questionList = questionsList?.filter(q => q.entityId === entity[entityIdKey] && q.entityType === entityType) || [];
      });
    };

    const filterByParentId = (childList: any[], childKey: string, parentId: any) => {
      return childList?.filter(child => child[childKey] === parentId) || [];
    };

    assignQuestions(subModuleList, 'subModuleId', subModuleType);

    subModuleList.forEach((subModule) => {
      subModule.subSubModuleList = filterByParentId(subSubModuleList, 'moduleId', subModule.subModuleId);
      assignQuestions(subModule.subSubModuleList, 'subModuleId', 'Sub-SubModule');
      if (this.validationService.validateNullUndefinedEmptyList(subModule?.subSubModuleList)) {
        this.createProjectInsightSubModuleList(subModule?.subSubModuleList, subSubModuleList, questionsList, 'Sub-SubModule');
      }

    });
    return subModuleList;
  }

  createProjectInsightSubSubModuleList(subSubModuleList: any[], questionsList: any[]) {
    const assignQuestions = (entityList: any[], entityIdKey: string, entityType: string) => {
      entityList?.forEach(entity => {
        entity.questionList = questionsList?.filter(q => q.entityId === entity[entityIdKey] && q.entityType === entityType) || [];
      });
    };

    const filterByParentId = (childList: any[], childKey: string, parentId: any) => {
      return childList?.filter(child => child[childKey] === parentId) || [];
    };

    assignQuestions(subSubModuleList, 'subModuleId', 'Sub-SubModule');

    subSubModuleList.forEach((subModule) => {
      subModule.subSubModuleList = filterByParentId(subSubModuleList, 'moduleId', subModule.subModuleId);
      assignQuestions(subModule.subSubModuleList, 'subModuleId', 'Sub-SubModule');
    });
    return subSubModuleList;
  }

  // EXCEL HELPERS FUNCTIONS

  readExcelFile(file: any): Promise<XLSX.WorkBook> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        try {
          const data: string = e.target.result;
          const workbook: XLSX.WorkBook = XLSX.read(data, { type: 'binary' });
          resolve(workbook);
        } catch (error) {
          reject(error);
        }
      };
      reader.onerror = () => reject(reader.error);
      reader.readAsBinaryString(file);
    });
  }

  extractSheetData(workbook: XLSX.WorkBook, sheetName: string) {
    const worksheet = workbook.Sheets[sheetName];
    if (!worksheet) return null;
    const sheetData = XLSX.utils.sheet_to_json(worksheet, { defval: null });
    const dataHeaders = this.getHeaders(worksheet);
    return { sheetData, dataHeaders };
  }
  async extractAllSheetData(file: any): Promise<any> {
    let employeeList = await this.getEmployeeList();
    const sheetDataMap: any = {};
    const workbook = await this.readExcelFile(file);
    workbook.SheetNames.forEach(sheetName => {
      const sheetInfo = this.extractSheetData(workbook, sheetName);
      switch (sheetName) {
        case 'Project':
          sheetDataMap.projectData = this.mapProjectInsightDetails(sheetInfo.sheetData, sheetInfo.dataHeaders);
          break;
        case 'Milestones':
          sheetDataMap.milestoneList = this.mapProjectInsightMilestoneList(sheetInfo.sheetData, sheetInfo.dataHeaders, employeeList);
          break;
        case 'Modules':
          sheetDataMap.moduleList = this.mapProjectInsightModuleList(sheetInfo.sheetData, sheetInfo.dataHeaders, employeeList);
          break;
        case 'SubModules':
          sheetDataMap.subModuleList = this.mapProjectInsightSubModuleList(sheetInfo.sheetData, sheetInfo.dataHeaders, employeeList);
          break;
        case 'Sub-SubModules':
          sheetDataMap.subSubModuleList = this.mapProjectInsightSubSubModuleList(sheetInfo.sheetData, sheetInfo.dataHeaders, employeeList);
          break;
        case 'Questions':
          sheetDataMap.questionsList = this.mapProjectInsightQuestions(sheetInfo.sheetData, sheetInfo.dataHeaders);
          break;
      }
    });
    return sheetDataMap;
  }

  processSheets(workbook: XLSX.WorkBook, sheetMap: any) {
    const result: any = {};
    workbook.SheetNames.forEach(sheetName => {
      const sheetInfo = this.extractSheetData(workbook, sheetName);
      if (!sheetInfo) return;

      const handler = sheetMap[sheetName];
      if (handler) {
        result[handler.key] = handler.mapFn(sheetInfo.sheetData, sheetInfo.dataHeaders);
      }
    });
    return result;
  }

  // UTILITY FUNCTIONS
  getParentType(entityType: string): string {
    switch (entityType) {
      case 'Milestone': return 'Project';
      case 'Module': return 'Milestone';
      case 'SubModule': return 'Module';
      case 'Sub-SubModule': return 'SubModule';
      default: return '';
    }
  }

  getHeaders(worksheet: XLSX.WorkSheet): string[] {
    const headers: string[] = [];
    const range = XLSX.utils.decode_range(worksheet['!ref']);
    for (let C = range.s.c; C <= range.e.c; ++C) {
      const headerCell = XLSX.utils.encode_cell({ r: range.s.r, c: C });
      headers.push(worksheet[headerCell]?.v || '');
    }
    return headers;
  }

  getIdField(type: string) {
    switch (type) {
      case 'Project': return 'projectId';
      case 'Milestone': return 'milestoneId';
      case 'Module': return 'moduleId';
      case 'SubModule': return 'subModuleId';
      case 'Sub-SubModule': return 'subModuleId';
    }
  }

  parseResponse(projectResponseList) {
    if (this.isValidList(projectResponseList)) {
      return projectResponseList[0].response;
    } else {
      return '';
    }
  }

  parseListToOptions(optionType: any, options: any) {
    try {
      if (optionType == 'radio' || optionType == 'checkbox') {
        let optionValuesList: any[] = [];
        let optionsList = JSON.parse(options || '[]');
        if (optionsList && optionsList.length > 0) {
          optionsList?.forEach((option) => {
            optionValuesList.push({ 'optionValue': option })
          });
          return optionValuesList;
        } else {
          return null;
        }
      } else {
        return null;
      }
    } catch (error) {
      return null
    }
  }

  private firstNonNull(errors: (string | null)[]): string | null {
    return errors.find(error => !!error) || null;
  }

  parseAssignedToUserIdAndReturnEmpId(assignedToUserId: any, employeeList: any[]) {
    try {
      if (this.isValidList(employeeList)) {
        let employeementIds = employeeList.filter(employee => assignedToUserId?.includes(employee.empId))
          .map(employee => employee.employeementId);
        return JSON.stringify(employeementIds || []);
      } else {
        return '[]'
      }
    } catch (e) {
      console.error("Invalid JSON format for AssignedToUserId");
    }
  }

  parseAssignedToUserId(assignedToUserId: any, employeeList: any[]) {
    let userIdList: number[] = [];
    let employeementIds: number[] = [];
    try {
      if (this.isValidList(employeeList)) {
        const parsed = JSON.parse(assignedToUserId);
        if (Array.isArray(parsed)) {
          userIdList = parsed.map(Number);
        } else {
          return [];
        }
        employeementIds = employeeList.filter(employee => userIdList?.includes(employee.employeementId))
          .map(employee => employee.empId);
      }
      return employeementIds;
    } catch (e) {
      console.error("Invalid JSON format for AssignedToUserId");
    }
  }

  parseOptionsOfQuestionToList(questionList: ProjectQuestion[]) {
    if (this.isValidList(questionList)) {
      questionList.forEach((questionObj: ProjectQuestion) => {
        if (questionObj.optionType == "radio" || questionObj.optionType == "checkbox") {
          questionObj.optionsList = JSON.parse(questionObj.options || '[]');
        }
      });
    }
  }

  createSubModuleListObject(subModuleList: ProjectSubModule[], subModuleType: any) {
    subModuleList.forEach((submodule: any, submodIndex) => {
      this.parseOptionsOfQuestionToList(submodule.questionList);
      if (this.isValidList(submodule.subSubModuleList)) {
        this.createSubModuleListObject(submodule.subSubModuleList, "Sub-SubModule");
      }
    });
  }

  async getProjectIdFromExcel(file): Promise<any> {
    let projectInsight = await this.convertJsonDataToProjectInsightDetails(file);
    if (projectInsight) {
      return projectInsight.projectId;
    } else {
      return null;
    }
  }

  async callGetAllProjectInsightQuestionsByProjectIdAndEmpId(projectId: any, empId: any, performanceTabName: any, employeeRole: any, emptyResponses: boolean): Promise<any> {
    let projectInsightObj: any = await this.getAllProjectInsightQuestionsByProjectIdAndEmpId(projectId, empId, performanceTabName, employeeRole);
    if (projectInsightObj) {
      if (emptyResponses) {
        await this.exportProjectInsightEntityQuestionsToExcel(projectInsightObj, 'Project', projectInsightObj?.projectId, projectInsightObj?.projectName);
      }
      return projectInsightObj;
    } else {
      return null;
    }
  }

  async callGetAllProjectInsightQuestionsByProjectIdAndEmpIdForValidation(projectId: any, empId: any, performanceTabName: any, employeeRole: any): Promise<any> {
    let projectInsightObj: any = await this.getAllProjectInsightQuestionsByProjectIdAndEmpId(projectId, empId, performanceTabName, employeeRole);
    if (projectInsightObj) {
      let questionList = await this.getQuestionListByEntityForQuestionsAndResponseExcel(projectInsightObj, 'Project');
      return questionList;
    } else {
      return null;
    }
  }

  // API Call
  async getAllProjectInsightQuestionsByProjectIdAndEmpId(projectId: any, empId: any, performanceTabName: any, employeeRole: any): Promise<any> {
    let projObj = new ProjectInsight();
    projObj.empId = empId
    projObj.projectId = projectId;
    projObj.performanceTabName = performanceTabName;
    projObj.employeeRole = employeeRole;
    return this.projectInsightService.getAllProjectInsightQuestionsByProjectIdAndEmpId(projObj).pipe(first())
      .toPromise()
      .then(async (response: any) => {
        if (response.serviceStatus == "Success") {
          let projectInsightObj: ProjectInsight = new ProjectInsight();
          projectInsightObj = response.serviceResponse;
          this.parseOptionsOfQuestionToList(projectInsightObj.questionList);
          projectInsightObj.projectInsightMilestoneList.forEach((milestone: ProjectMilestone, mileIndex) => {
            this.parseOptionsOfQuestionToList(milestone.questionList);
            if (this.isValidList(milestone.moduleList)) {
              milestone.moduleList.forEach((module: any, modIndex) => {
                this.parseOptionsOfQuestionToList(module.questionList);
                if (this.isValidList(module.subModuleList)) {
                  this.createSubModuleListObject(module.subModuleList, "SubModule");
                }
              });
            }
          });
          return projectInsightObj;
        } else {
          return null;
        }
      })
      .catch(error => {
        console.log(error);
        throw (error);
      });
  }

  async getAllQuestionsByProjectId(projectId: any): Promise<any> {
    let projectInsight: ProjectInsight = new ProjectInsight();
    projectInsight.projectId = projectId;
    return this.projectInsightService.getAllQuestionsByProjectId(projectInsight).pipe(first())
      .toPromise()
      .then((response: any) => {
        if (response.serviceStatus === 'Success') {
          let projectInsightObj: ProjectInsight = new ProjectInsight();
          projectInsightObj = response.serviceResponse;
          projectInsightObj.deletedProjectInsightEntityList = [];
          this.parseOptionsOfQuestionToList(projectInsightObj.questionList);
          projectInsightObj.projectInsightMilestoneList.forEach((milestone: ProjectMilestone, mileIndex) => {
            this.parseOptionsOfQuestionToList(milestone.questionList);
            if (this.isValidList(milestone.moduleList)) {
              milestone.moduleList.forEach((module: any, modIndex) => {
                this.parseOptionsOfQuestionToList(module.questionList);
                if (this.isValidList(module.subModuleList)) {
                  this.createSubModuleListObject(module.subModuleList, "SubModule");
                }
              });
            }
          });
          return projectInsightObj;
        } else {
          return null;
        }
      })
      .catch(error => {
        console.log(error);
        throw error;
      });
  }


  async getEmployeeList(): Promise<any> {
    let employeeList = [];
    return this.employeeService.getAllEmployees().pipe(first())
      .toPromise()
      .then((response: any) => {
        if (response.serviceStatus == "Success") {
          employeeList = response.serviceResponse;
          employeeList = employeeList.filter(x => x.employmentstatus != 'InActive');
        } else {
          console.error(response.serviceResponse)
        }
        return employeeList;
      })
      .catch(error => {
        console.log(error);
        return [];
      });
  }

  // New Logic [Start] 

  downloadTemplate(): void {
    // --- Sheet 1 (Instructions) ---
    const instructions = [
      ["1. Project Section"],
      ["Mandatory Fields: ProjectName, IndustryDomain"],
      ["To add fields in a Project:"],
      ["- In the Section column, enter 'Project'."],
      ["- Allowed OptionType values: text, textarea, select, checkbox, radio, date, number, email"],
      [""],

      ["2. Group Section"],
      ["Mandatory Fields: GroupTitle, GroupType"],
      ["To add fields in a Group:"],
      ["- Use 'Group-1', 'Group-2', ... for groups."],
      ["- For sub-groups: 'Group-1-1', 'Group-1-2', ..."],
      ["Allowed GroupType values: milestone, feature, activity, tasks"],
      ["Allowed OptionType values: text, textarea, select, checkbox, radio, date, number, email"],
      [""],

      ["3. Questions & Data"],
      ["To add a Question:"],
      ["- Use 'Project-|-Question' or 'Group-1-|-Question'."],
      ["Allowed OptionType values for Questions: text, checkbox, radio"],
      ["Options Format (for select, checkbox, radio):[\"Option 1\", \"Option 2\", \"Option 3\"]"],
      ["Value Format (for select, checkbox): [\"Value 1\", \"Value 2\", \"Value 3\"]"],
      ["(Values must match one of the options.)"],
      [""],

      ["4. General Rules"],
      ["- Follow suffix-based hierarchy for groups/sub-groups."],
      ["- Keep OptionType lowercase."],
      ["- Mandatory fields cannot be blank."],
      ["- Use 'select' instead of 'dropdown'."],
      ["- Date format must be 'YYYY-MM-DD'."],
      [""],

      ["5. Field-Specific Rules"],
      ["- Required → Only for form fields (not questions). Allowed values: true, false"],
      ["- IsMultiSelect → Only if OptionType = select. Allowed values: true, false"],
      ["- FieldWidth → Only for form fields (not questions). Allowed values: 25, 33, 50, 75, 100"]
    ];

    // --- Sheet 2 (Headers + Example Data) ---
    

    const exampleData = [
      // Project Section
      ["Project", "ProjectName", "50", "true", "text", "", "", "Test-Project-1"],
      ["Project", "IndustryDomain", "50", "true", "select", "false", '["Banking","Finance","IT"]', "Banking"],

      // Project Question
      ["Project-|-Question", "What is the purpose of the project?", "100", "", "textarea", "", "", "To create a central repository"],

      // Group Section
      ["Group-1", "GroupTitle", "50", "true", "text", "", "", "Test-Group-1"],
      ["Group-1", "GroupType", "50", "true", "select", "false", '["milestone","feature","activity","tasks"]', "milestone"],

      // Group Question
      ["Group-1-|-Question", "This is Test-Group-1 Question-1?", "100", "", "radio", "", '["Yes","No"]', "Yes"],

      // Sub Group Section
      ["Group-1-1", "GroupTitle", "50", "true", "text", "", "", "Test-Sub-Group-1"],
      ["Group-1-1", "GroupType", "50", "true", "select", "false", '["milestone","feature","activity","tasks"]', "feature"],

      // Sub Group Question
      ["Group-1-1-|-Question", "This is Test-Sub-Group-1 Question-1?", "100", "", "checkbox", "true", '["Option1","Option2","Option3"]', '["Option1","Option3"]']
    ];

    // Create workbook
    const wb = XLSX.utils.book_new();
    const ws1 = XLSX.utils.aoa_to_sheet(instructions);
    const ws2 = XLSX.utils.aoa_to_sheet([this.headers, ...exampleData]);

    XLSX.utils.book_append_sheet(wb, ws1, "Instructions");
    XLSX.utils.book_append_sheet(wb, ws2, "Template");

    // Export
    XLSX.writeFile(wb, "Project_Group_Template.xlsx");
  }

  parseExcel(file: File): Observable<{ success: boolean; message?: string; structure?: ProjectSectionData }> {
    return new Observable(observer => {
      const reader = new FileReader();

      reader.onload = (e: any) => {
        try {
          const workbook = this.readWorkbook(e.target.result);
          const sheet = workbook.Sheets[workbook.SheetNames[0]];

          const headerRow = XLSX.utils.sheet_to_json(sheet, { header: 1, range: 0, defval: '' })[0] as string[];

          const missing = this.headers.filter(h => !headerRow.includes(h));
          const extra = headerRow.filter(h => h && !this.headers.includes(h?.trim()));

          if (missing.length > 0 || extra.length > 0) {
            observer.next({
              success: false,
              message: `Invalid headers. Missing: ${missing.join(', ') || 'None'} | Unexpected: ${extra.join(', ') || 'None'}`
            });
            observer.complete();
            return;
          }

          const rows = XLSX.utils.sheet_to_json<ExcelRow>(sheet, { defval: '' });
          const projectStructure: ProjectSectionData = { fields: [], questions: [], groups: {}, projectInsightProjectDetails: null, createdBy: null };

          for (let i = 0; i < rows.length; i++) {
            const row = rows[i];
            const validation = this.validateRow(row, i + 2);
            if (!validation.success) {
              observer.next(validation);
              observer.complete();
              return;
            }
            this.addRowToProjectStructure(projectStructure, row.Section?.trim(), row);
          }

          observer.next({ success: true, structure: projectStructure });
          observer.complete();
        } catch (err) {
          observer.error(err);
        }
      };

      reader.readAsArrayBuffer(file);
    });
  }

  private readWorkbook(buffer: ArrayBuffer): XLSX.WorkBook {
    const data = new Uint8Array(buffer);
    return XLSX.read(data, { type: 'array', cellDates: true });
  }

  private validateRow(row: ExcelRow, rowNumber: number): { success: boolean; message?: string } {
    const section = row?.Section?.trim();
    const title = row?.Title?.trim();
    const optionType = row?.OptionType?.trim()?.toLowerCase();
    const value = this.extractValue(row?.Value);

    if (!section && !title) return { success: true };

    // Project-level validations
    if (section?.toLowerCase() === 'project') {
      if (this.isMissing(title, value, ['projectname', 'project name'])) {
        return this.fail(rowNumber, 'Project Name is mandatory');
      }
      if (this.isMissing(title, value, ['industrydomain', 'industry domain'])) {
        return this.fail(rowNumber, 'Industry Domain is mandatory');
      }
    }

    // Group-level validations
    if (section?.toLowerCase().startsWith('group')) {
      if (this.isMissing(title, value, ['grouptitle', 'group title'])) {
        return this.fail(rowNumber, `Group Title is mandatory for ${section}`);
      }
      if (['grouptype', 'group type'].includes(title?.toLowerCase())) {
        if (!value) {
          return this.fail(rowNumber, `Group Type is mandatory for ${section}`);
        }

        if (!this.allowedGroupTypes.includes(value.toLowerCase())) {
          return this.fail(rowNumber, `Invalid Group Type for ${section}. Allowed: milestone, feature, activity, tasks`);
        }
      }
    }

    // OptionType validations
    if (optionType && !this.allowedOptionTypes.includes(optionType)) {
      return this.fail(rowNumber, `Invalid OptionType '${optionType}' in section ${section}`);
    }

    if (optionType === 'date' && value && !this.isValidISODate(this.formatDateToYMD(new Date(value)))) {
      return this.fail(rowNumber, `Invalid date in section ${section}. Expected 'YYYY-MM-DD'.`);
    }

    if (optionType === 'email' && value && !this.isValidEmail(value)) {
      return this.fail(rowNumber, `Invalid Email format`);
    }

    // List option validations
    if (optionType && this.allowedListOptionTypes.includes(optionType)) {
      const optionsValidation = this.validateOptionsAndValues(optionType, row.Option, value, section, rowNumber, row);
      if (!optionsValidation.success) return optionsValidation;
    }

    return { success: true };
  }

  private validateOptionsAndValues(optionType: string, optionRaw: string, value: any, section: string, rowNumber: number, row: any) {
    const optionsListObj = this.parseOptionsToList(optionType, optionRaw?.trim());
    if (!['grouptype', 'group type'].includes(row?.Title?.toLowerCase()?.trim())) {
      if (!optionsListObj || !optionsListObj?.success) {
        return this.fail(rowNumber, `Invalid Options format for section ${section}. Expected: ["Option 1","Option 2"]`);
      }
    }

    if (!value) return { success: true };

    if (['checkbox', 'select'].includes(optionType)) {
      const valuesListObj = this.parseValuesToList(optionType, value);
      if (!['grouptype', 'group type'].includes(row?.Title?.toLowerCase()?.trim())) {
        if (!valuesListObj || !valuesListObj.success) {
          return this.fail(rowNumber, `Invalid Values format for section ${section}. Expected: ["Value1","Value2"]`);
        }
        if (this.hasInvalidValues(valuesListObj.obj, optionsListObj.obj)) {
          return this.fail(rowNumber, `Only values from options are allowed in section ${section}`);
        }
      }
    }

    if (optionType === 'radio' && !optionRaw.includes(value)) {
      return this.fail(rowNumber, `Only values from options are allowed in section ${section}`);
    }
    return { success: true };
  }

  private extractValue(value: any): string | null {
    if (!this.validationService.validateNullUndefinedEmptyString(value)) return null;
    return typeof value === 'string' ? value.trim() : value;
  }

  private isMissing(title: string, value: any, expectedTitles: string[]): boolean {
    return expectedTitles.includes(title?.toLowerCase()) && !value;
  }

  private fail(rowNumber: number, message: string) {
    return { success: false, message: `Row ${rowNumber}: ${message}` };
  }

  private addRowToProjectStructure(projectStructure: ProjectSectionData, sectionName: string, row: ExcelRow) {
    if (!sectionName) return;

    const isQuestion = sectionName.toLowerCase().includes('-|-question');
    sectionName = sectionName.replace('-|-Question', '');

    if (sectionName.toLowerCase() === 'project') {
      this.addProjectRow(projectStructure, row, isQuestion);
      return;
    }

    if (sectionName.toLowerCase().startsWith('group')) {
      this.addGroupRow(projectStructure, sectionName, row, isQuestion);
    }
  }

  private addProjectRow(projectStructure: ProjectSectionData, row: ExcelRow, isQuestion: boolean) {
    if (isQuestion) {
      projectStructure.questions ??= [];
      projectStructure.questions.push(this.transformExcelRowToQuestion(row));
      return;
    }

    projectStructure.projectInsightProjectDetails ??= new ProjectInsightProjectDetails();

    switch (row.Title.toLowerCase()) {
      case 'projectname':
      case 'project name':
        projectStructure.projectInsightProjectDetails.projectName = row.Value;
        break;
      case 'industry domain':
      case 'industrydomain': {
        const valuesListObj = this.parseFieldValuesToList(row.OptionType, row.Value);
        if (valuesListObj) {
          projectStructure.projectInsightProjectDetails.industryDomain = valuesListObj;
        }
        break;
      }
      default:
        projectStructure.fields ??= [];
        this.addAdditionalInfoToProjectDetails(projectStructure.projectInsightProjectDetails, row);
        projectStructure.fields.push(this.transformExcelRowToField(row));
    }
  }

  private addGroupRow(projectStructure: ProjectSectionData, sectionName: string, row: ExcelRow, isQuestion: boolean) {
    if (this.isImmediateGroup(sectionName)) {
      const group = this.getOrCreateGroup(projectStructure.groups, sectionName, row);
      this.addToGroup(group, row, isQuestion);
    } else {
      const parentName = this.getParentGroupName(sectionName);
      if (!parentName) return;

      const parentGroup = this.findOrCreateParentGroup(projectStructure, parentName, row);
      parentGroup.subGroups ??= {};
      const subGroup = this.getOrCreateSubGroup(parentGroup, sectionName, row);

      this.addToGroup(subGroup, row, isQuestion);
    }
  }

  private getOrCreateGroup(groups: Record<string, GroupSectionData>, sectionName: string, row: ExcelRow): GroupSectionData {
    if (!groups[sectionName]) {
      groups[sectionName] = this.createGroup(sectionName, row);
    }
    return groups[sectionName];
  }

  private getOrCreateSubGroup(parentGroup: GroupSectionData, sectionName: string, row: ExcelRow): GroupSectionData {
    let subGroup = parentGroup.subGroups?.[sectionName];
    if (!subGroup) {
      subGroup = this.createGroup(sectionName, row);
      parentGroup.subGroups[sectionName] = subGroup;
    }
    return subGroup;
  }

  private createGroup(sectionName: string, row: ExcelRow): GroupSectionData {
    const details = new ProjectInsightGroupDetails();
    details.isDraft = 'Y';

    if (['grouptitle', 'group title'].includes(row?.Title.toLowerCase())) {
      details.groupTitle = row?.Value;
    }
    if (['grouptype', 'group type'].includes(row?.Title.toLowerCase())) {
      details.groupType = row?.Value.toLowerCase();
    }

    const group: GroupSectionData = { fields: [], questions: [], subGroups: {}, projectInsightGroupDetails: details };
    (group as any)._sectionName = sectionName;
    return group;
  }

  private addToGroup(group: GroupSectionData, row: ExcelRow, isQuestion: boolean) {
    if (['grouptype', 'group type'].includes(row?.Title.toLowerCase()) && !group.projectInsightGroupDetails.groupType) {
      group.projectInsightGroupDetails.groupType = row?.Value.toLowerCase();
    }

    if (isQuestion) {
      group.questions ??= [];
      group.questions.push(this.transformExcelRowToQuestion(row));
    } else {
      if (!['grouptype', 'group type', 'grouptitle', 'group title'].includes(row.Title.toLowerCase())) {
        group.fields ??= [];
        group.projectInsightGroupDetails.additionalInfo = this.addAdditionalInfoToDetails(group.projectInsightGroupDetails, row);
        group.fields.push(this.transformExcelRowToField(row));
      }
    }
  }

  addAdditionalInfoToDetails(projectInsightGroupDetails: ProjectInsightGroupDetails, excelRow: ExcelRow) {
    if (!projectInsightGroupDetails.additionalInfo) {
      projectInsightGroupDetails.additionalInfo = new Map<string, any>();
    }

    if (excelRow?.Title) {
      projectInsightGroupDetails.additionalInfo[this.cleanString(excelRow.Title)] = this.parseFieldValuesToList(excelRow.OptionType, excelRow?.Value);
    }
    return projectInsightGroupDetails.additionalInfo;
  }

  addAdditionalInfoToProjectDetails(projectInsightProjectDetails: ProjectInsightProjectDetails, excelRow: ExcelRow) {
    if (!projectInsightProjectDetails.additionalInfo) {
      projectInsightProjectDetails.additionalInfo = new Map<string, any>();
    }
    if (excelRow?.Title) {
      projectInsightProjectDetails.additionalInfo[this.cleanString(excelRow.Title)] = this.parseFieldValuesToList(excelRow.OptionType, excelRow?.Value);
    }
  }

  private transformExcelRowToQuestion(excelRow: ExcelRow): ProjectInsightQuestionDetails {
    const question = new ProjectInsightQuestionDetails();
    question.optionType = excelRow?.OptionType;
    question.optionsList = this.parseOptionsToList(excelRow?.OptionType, excelRow?.Option)?.obj ?? [];
    question.addToQuestionBank = false;
    question.question = excelRow?.Title;
    question.description = '';
    return question;
  }

  private transformExcelRowToField(excelRow: ExcelRow): FormField {
    const field = new FormField();
    field.id = this.generateUniqueId();
    field.label = excelRow.Title;
    field.name = `${this.cleanString(excelRow.Title)}`;
    field.placeholder = '';
    field.optionSource = 'static';
    field.rowPosition = 0;
    field.multiple = excelRow?.IsMultiSelect || false;;
    field.apiUrl = '';
    field.apiLabelKey = '';
    field.apiValueKey = '';
    field.required = excelRow?.Required || false;
    field.parentField = '';
    field.dependentApiUrl = '';
    field.dependentLabelKey = '';
    field.dependentValueKey = '';
    field.dependentParamName = '';
    field.width = excelRow?.FieldWidth;
    field.type = excelRow?.OptionType;
    field.defaultValue = (!['checkbox', 'select'].includes(field.type)) ? excelRow?.Value : '';
    const optionsListObj = this.parseOptionsToFormList(excelRow?.OptionType, excelRow?.Option);
    if (optionsListObj && optionsListObj?.success) {
      field.multiple = true;
      field.options = optionsListObj.obj;
    }
    return field;
  }

  private getGroupDepth(section: string): number {
    // Normalize: remove optional "Project-" and "Group-" prefixes
    let normalized = section.replace(/^Project-/, '').replace(/^Group-/, '');
    return normalized.split('-').filter(p => p.trim() !== '').length;
  }

  private isImmediateGroup(section: string): boolean {
    return this.getGroupDepth(section) === 1;
  }

  private isSubGroup(section: string): boolean {
    return this.getGroupDepth(section) > 1;
  }

  private getParentGroupName(section: string): string | null {
    if (!this.isSubGroup(section)) return null;

    const hasProjectPrefix = section.startsWith('Project-');
    let normalized = section.replace(/^Project-/, '').replace(/^Group-/, '');
    const parts = normalized.split('-');
    const parent = parts.slice(0, parts.length - 1).join('-');

    return hasProjectPrefix ? `Project-Group-${parent}` : `Group-${parent}`;
  }

  private parseOptionsToList(optionType: string, optionsRaw: any) {
    if (!['radio', 'checkbox', 'select'].includes(optionType)) return null;
    return this.parseJsonToSurveyOptions(optionsRaw, 'Options');
  }

  private parseOptionsToFormList(optionType: string, optionsRaw: any) {
    if (!['radio', 'checkbox', 'select'].includes(optionType)) return null;
    return this.parseJsonToFormOptions(optionsRaw, 'Options');
  }

  private parseValuesToList(optionType: string, valuesRaw: any) {
    if (!['checkbox', 'select'].includes(optionType)) return null;
    return this.parseJsonToSurveyOptions(valuesRaw, 'Values');
  }

  private parseFieldValuesToList(optionType: string, valuesRaw: any) {
    if (!['checkbox', 'select'].includes(optionType)) return valuesRaw;
    try {
      const parsed = JSON.parse(valuesRaw || '[]');
      return parsed;
    } catch {
      return [];
    }
  }

  private parseJsonToSurveyOptions(input: any, label: string): { success: boolean; obj?: SurveyOption[]; message?: string } {
    try {
      const parsed = JSON.parse(input || '[]');
      if (!Array.isArray(parsed) || parsed.length === 0) {
        return { success: false, message: `${label} must be a non-empty array` };
      }
      const options = parsed.map((v: string) => {
        const opt = new SurveyOption();
        opt.optionValue = v;
        return opt;
      });
      return { success: true, obj: options };
    } catch {
      return { success: false, message: `${label} must be a valid JSON array` };
    }
  }

  private parseJsonToFormOptions(input: any, label: string): { success: boolean; obj?: any; message?: string } {
    try {
      const parsed = JSON.parse(input || '[]');
      if (!Array.isArray(parsed) || parsed.length === 0) {
        return { success: false, message: `${label} must be a non-empty array` };
      }
      const options = parsed.map((v: string) => {
        const opt = {
          label: v,
          value: v
        }
        return opt;
      });
      return { success: true, obj: options };
    } catch {
      return { success: false, message: `${label} must be a valid JSON array` };
    }
  }

  private findOrCreateParentGroup(projectStructure: ProjectSectionData, parentName: string, row: ExcelRow): GroupSectionData {
    if (!projectStructure.groups[parentName]) {
      projectStructure.groups[parentName] = {
        projectInsightGroupDetails: {} as ProjectInsightGroupDetails,
        fields: [],
        questions: [],
        subGroups: {}
      };
    }
    return projectStructure.groups[parentName];
  }

  private isValidISODate(dateStr: string): boolean {
    const isoRegex = /^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$/;
    if (!isoRegex.test(dateStr)) return false;

    const date = new Date(dateStr);
    if (isNaN(date.getTime())) return false;

    const [year, month, day] = dateStr.split('-').map(Number);
    return (
      date.getUTCFullYear() === year &&
      date.getUTCMonth() + 1 === month &&
      date.getUTCDate() === day
    );
  }

  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  private hasInvalidValues(values: SurveyOption[], options: SurveyOption[]): boolean {
    const optionSet = new Set(options.map(o => o.optionValue?.toLowerCase()?.trim()));
    return values.some(v => !optionSet.has(v.optionValue?.toLowerCase()?.trim()));
  }

  private formatDateToYMD(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  parseRangeToObjects(sheet: XLSX.WorkSheet, headerStart: string, headerEnd: string, dataStart: string, dataEnd: string): { message: string, obj: Record<string, any>[] } {
    const result: Record<string, any>[] = [];

    const decodeCell = (cellRef: string) => XLSX.utils.decode_cell(cellRef);
    const headerStartCell = decodeCell(headerStart);
    const headerEndCell = decodeCell(headerEnd);
    const dataStartCell = decodeCell(dataStart);
    const dataEndCell = decodeCell(dataEnd);

    const columnCount = headerEndCell.c - headerStartCell.c + 1;

    // Extract headers with type info
    const headers: ParsedColumn[] = [];
    for (let c = headerStartCell.c; c <= headerEndCell.c; c++) {
      const cellAddr = XLSX.utils.encode_cell({ r: headerStartCell.r, c });
      const cell = sheet[cellAddr];
      if (!cell) {
        return { message: `Missing header at column ${c + 1}`, obj: null };
      }

      const [name, type] = String(cell.v).split("-|-");
      if (!name || !type) {
        return { message: `Invalid header format at ${cellAddr}. Expected "Name-|-Type".`, obj: null };
      }

      if (!["Text", "Number", "Date"].includes(type)) {
        return { message: `Unsupported type "${type}" at ${cellAddr}.`, obj: null };
      }

      headers.push({ name: name.trim(), type: type as tableColumnDataTypes });
    }

    // Validation: ensure headers match expected count
    if (headers.length !== columnCount) {
      return { message: "Header cells are scattered or incomplete.", obj: null };
    }

    // Extract rows
    for (let r = dataStartCell.r; r <= dataEndCell.r; r++) {
      const rowObj: Record<string, any> = {};
      let filledCols = 0;

      for (let c = dataStartCell.c; c <= dataEndCell.c; c++) {
        const cellAddr = XLSX.utils.encode_cell({ r, c });
        const cell = sheet[cellAddr];
        const headerIndex = c - dataStartCell.c;
        const { name, type } = headers[headerIndex];

        let value = cell ? cell.v : null;

        // Type validation
        if (this.validationService.validateNullUndefinedEmptyString(value)) {
          switch (type) {
            case "Text":
              if (typeof value !== "string") {
                return { message: `Invalid value at ${cellAddr}: expected Text, got ${typeof value}`, obj: null };
              }
              break;
            case "Number":
              if (isNaN(Number(value))) {
                return { message: `Invalid value at ${cellAddr}: expected Number, got ${value}`, obj: null };
              }
              value = Number(value);
              break;
            case "Date":
              const dateVal = XLSX.SSF.parse_date_code(value) || new Date(value);
              if (isNaN(new Date(dateVal).getTime())) {
                return { message: `Invalid value at ${cellAddr}: expected Date, got ${value}`, obj: null };
              }
              value = new Date(dateVal);
              break;
          }
          filledCols++;
        }
        rowObj[name] = value;
      }

      // Validation: row must be complete or empty
      if (filledCols > 0 && filledCols < columnCount) {
        return { message: `Row ${r + 1} is incomplete (expected ${columnCount} columns, found ${filledCols}).`, obj: null };
      }

      if (filledCols > 0) {
        result.push(rowObj);
      }
    }
    return { message: 'Success', obj: result };
  }

  generateUniqueId(): string {
    return Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }

  cleanString(input: string): string {
    if (!this.validationService.validateNullUndefinedEmptyString(input)) return '';
    return input
      .replace(/[^a-zA-Z0-9]/g, "") // remove everything except letters & numbers
      .toLowerCase();               // convert to lowercase
  }
}