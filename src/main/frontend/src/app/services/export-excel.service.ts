import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import { ProjectInsight } from '../models/projectInsight';
import { ProjectMilestone } from '../models/projectMilestone';
import { ProjectModule } from '../models/projectModule';
import { ProjectSubModule } from '../models/projectSubModule';
import { ProjectQuestion } from '../models/projectQuestion';
import { ValidationService } from './validation.service';
import { first } from 'rxjs/operators';
import { Project } from '../models/project';
import { ProjectService } from './project.service';
import { ProjectInsightService } from './project-insight.service';

@Injectable({
  providedIn: 'root'
})
export class ExportExcelService {

  constructor(private validationService: ValidationService, private projectService: ProjectService
    , private projectInsightService: ProjectInsightService
  ) { }

  private sheetMapping = {
    Project: { key: 'projectData', mapFn: this.mapProjectInsightDetails.bind(this) },
    Milestones: { key: 'milestones', mapFn: this.mapProjectInsightMilestoneList.bind(this) },
    Modules: { key: 'modules', mapFn: this.mapProjectInsightModuleList.bind(this) },
    SubModules: { key: 'subModules', mapFn: this.mapProjectInsightSubModuleList.bind(this) },
    'Sub-SubModules': { key: 'subSubModules', mapFn: this.mapProjectInsightSubSubModuleList.bind(this) },
    Questions: { key: 'questions', mapFn: this.mapProjectInsightQuestions.bind(this) },
  };

  // EXCEL EXPORT AND IMPORT FUNCTIONS

  exportTableDataToExcel(arr: any[], name: string) {
    const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(arr);
    const book: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(book, worksheet, 'Sheet1');
    XLSX.writeFile(book, name);
  }

  exportTableDataToExcelWithDescription(arr: any[], name: string) {
    const worksheet: XLSX.WorkSheet = XLSX.utils.aoa_to_sheet(arr);
    const book: XLSX.WorkBook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(book, worksheet, 'Sheet1');
    XLSX.writeFile(book, name);
  }

  exportProjectInsightToExcel(entity: any, entityType: any, name: any, parentId: any) {
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

    const processEntity = (entity: any, type: string, parentId: any, parentType: any) => {
      switch (type) {
        case 'Project':
          sheetsData['Project'].push({ ProjectId: entity?.projectId, ProjectName: entity?.projectName, });
          break;
        case 'Milestone':
          sheetsData['Milestones'].push(...this.getMilestoneList([entity], parentId, parentType));
          break;
        case 'Module':
          sheetsData['Modules'].push(...this.getModuleList([entity], parentId, parentType));
          break;
        case 'SubModule':
          sheetsData['SubModules'].push(...this.getSubModuleList([entity], parentId, parentType));
          break;
        case 'Sub-SubModule':
          sheetsData['Sub-SubModules'].push(...this.getSubModuleList([entity], parentId, parentType));
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
    XLSX.writeFile(workbook, `Project_Insight_${name}.xlsx`);
  }

  exportEntityDetailOrQuestionsToExcel(entity: any, entityType: any, name: any, parentId: any, parentType: any) {
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

    if (entityType?.includes('Question')) {
      sheetsData['Questions'].push(...this.getQuestionListForQuestionsExcel(entity));
    } else if (entityType?.includes('Details')) {
      switch (parentType) {
        case 'Project':
          sheetsData['Project'].push({ ProjectId: entity?.projectId, ProjectName: entity?.projectName });
          break;
        case 'Milestone':
          sheetsData['Milestones'].push(...this.getMilestoneList([entity], parentId, 'Project'));
          break;
        case 'Module':
          sheetsData['Modules'].push(...this.getModuleList([entity], parentId, 'Milestone'));
          break;
        case 'SubModule':
          sheetsData['SubModules'].push(...this.getSubModuleList([entity], parentId, 'Module'));
          break;
        case 'Sub-SubModule':
          sheetsData['Sub-SubModules'].push(...this.getSubModuleList([entity], parentId, 'Sub-Module'));
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

  async exportProjectInsightEntityQuestionsToExcel(entity: any, entityType: any, name?: any) :Promise<any> {
    const workbook: XLSX.WorkBook = XLSX.utils.book_new();
    const sheetsData: { [sheetName: string]: any[] } = { 'Questions': [] };

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

  getMilestoneList(projectInsightMilestoneList: any, parentId: any, parentType: any) {
    let milestoneSheet: any[] = [];
    projectInsightMilestoneList?.forEach((milestone) => {
      milestoneSheet.push({
        MilestoneId: milestone.milestoneId,
        Milestone: milestone.milestone,
        Description: milestone.description,
        ParentId: parentId,
        ParentType: parentType,
        AssignedToUserId: JSON.stringify(milestone.assignedToUserId),
        ActionType: 'Update'
      });
    });
    return milestoneSheet;
  }

  getModuleList(moduleList: any, parentId: any, parentType: any) {
    let moduleSheet: any[] = [];
    moduleList?.forEach((module) => {
      moduleSheet.push({
        ModuleId: module.moduleId,
        Module: module.module,
        Description: module.description,
        ParentId: parentId,
        ParentType: parentType,
        AssignedToUserId: JSON.stringify(module.assignedToUserId),
        ActionType: 'Update'
      });
    });
    return moduleSheet;
  }

  getSubModuleList(subModuleList: any, parentId: any, parentType: any) {
    let subModuleSheet: any[] = [];
    subModuleList?.forEach((subModule) => {
      subModuleSheet.push({
        SubModuleId: subModule.subModuleId ? subModule.subModuleId : subModule.subModuleId,
        SubModule: subModule.subModule,
        Description: subModule.description,
        ParentId: parentId,
        ParentType: parentType,
        AssignedToUserId: JSON.stringify(subModule.assignedToUserId),
        ActionType: 'Update'
      });
    });
    return subModuleSheet;
  }

  getSubSubModuleList(subModuleList: any, parentId: any, subModuleType: any, subSubModuleSheet: any, questionSheet: any) {
    subSubModuleSheet.push(...this.getSubModuleList(subModuleList, parentId, subModuleType));
    subModuleList.forEach((subModule, subModuleIndex) => {
      if (subModule?.questionList) {
        questionSheet.push(...this.getQuestionList(subModule?.questionList, subModule?.subModuleId, 'Sub-SubModule'));
      }
      if (subModule?.subSubModuleList) {
        this.getSubSubModuleList(subModule?.subSubModuleList, subModule.subModuleId, 'Sub-SubModule', subSubModuleSheet, questionSheet);
      }
    });
  }

  getQuestionList(questionList: any, parentId: any, parentType: any) {
    let questionSheet: any[] = [];
    questionList?.forEach((question) => {
      questionSheet.push({
        QuestionId: question.questionId,
        Question: question.question,
        Description: question.description,
        OptionType: question.optionType,
        Options: question.optionType && question.optionType != 'text' ? JSON.stringify(this.parseOptionsToList(question.optionType, question.options)) : null,
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
        Options: question.optionType && question.optionType != 'text' ? JSON.stringify(this.parseOptionsToList(question.optionType, question.options)) : null,
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
        Options: question.optionType && question.optionType != 'text' ? JSON.stringify(this.parseOptionsToList(question.optionType, question.options)) : null,
        ParentId: question.entityId,
        ParentType: question.entityType,
        Response: ''
      });
    });
    return questionSheet;
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

  mapProjectInsightMilestoneList(sheetData: any, dataHeaders: any) {
    let milestoneSheet: any[] = [];
    if (sheetData) {
      for (let index = 0; index < sheetData?.length; index++) {
        let milestoneData = sheetData[index];
        let projectMilestone: ProjectMilestone = new ProjectMilestone();
        projectMilestone.milestone = milestoneData.Milestone;
        projectMilestone.milestoneId = milestoneData.MilestoneId;
        projectMilestone.description = milestoneData.Description;
        projectMilestone.assignedToUserId = this.parseAssignedToUserId(milestoneData.AssignedToUserId);
        projectMilestone.projectId = milestoneData?.ParentId;
        projectMilestone.actionType = milestoneData?.ActionType;
        milestoneSheet.push(projectMilestone);
      }
    }
    return milestoneSheet;
  }

  mapProjectInsightModuleList(sheetData: any, dataHeaders: any) {
    let moduleSheet: any[] = [];
    if (sheetData) {
      for (let index = 0; index < sheetData?.length; index++) {
        let moduleData = sheetData[index];
        let projectModule: ProjectModule = new ProjectModule();
        projectModule.moduleId = moduleData.ModuleId;
        projectModule.module = moduleData.Module;
        projectModule.description = moduleData.Description;
        projectModule.milestoneId = moduleData.ParentId;
        projectModule.assignedToUserId = this.parseAssignedToUserId(moduleData.AssignedToUserId);
        projectModule.actionType = moduleData?.ActionType;
        moduleSheet.push(projectModule);
      }
    }
    return moduleSheet;
  }

  mapProjectInsightSubModuleList(sheetData: any, dataHeaders: any) {
    let subModuleSheet: any[] = [];
    if (sheetData) {
      for (let index = 0; index < sheetData?.length; index++) {
        let subModuleData = sheetData[index];
        let subModule: ProjectSubModule = new ProjectSubModule();
        subModule.subModuleId = subModuleData.SubModuleId;
        subModule.subModule = subModuleData.SubModule;
        subModule.description = subModuleData.Description;
        subModule.moduleId = subModuleData.ParentId;
        subModule.assignedToUserId = this.parseAssignedToUserId(subModuleData.AssignedToUserId);
        subModule.actionType = subModuleData?.ActionType;
        subModuleSheet.push(subModule);
      }
    }
    return subModuleSheet;
  }

  mapProjectInsightSubSubModuleList(sheetData: any, dataHeaders: any) {
    let subModuleSheet: any[] = [];
    if (sheetData) {
      for (let index = 0; index < sheetData?.length; index++) {
        let subModuleData = sheetData[index];
        let subModule: ProjectSubModule = new ProjectSubModule();
        subModule.subModuleId = subModuleData.SubModuleId;
        subModule.subModule = subModuleData.SubModule;
        subModule.description = subModuleData.Description;
        subModule.moduleId = subModuleData.ParentId;
        subModule.assignedToUserId = this.parseAssignedToUserId(subModuleData.AssignedToUserId);
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
        question.entityId = questionData?.ParentId;
        question.entityType = questionData?.ParentType;
        question.actionType = questionData?.ActionType;
        questionSheet.push(question);
      }
    }
    return questionSheet;
  }


  // VALIDATION FUNCTIONS

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
        const v = this.validationService;  // shorthand
        const isEmpty = (list: any) => v.validateNullUndefinedEmptyList(list);

        if (!isEmpty(milestoneList) && (isEmpty(moduleList) || isEmpty(subModuleList) || isEmpty(subSubModuleList))) {
          return 'At least one Milestone is required before adding Modules/SubModules/Sub-SubModules.';
        }
        if (!isEmpty(moduleList) && (isEmpty(subModuleList) || isEmpty(subSubModuleList))) {
          return 'At least one Module is required before adding SubModules/Sub-SubModules.';
        }
        if (!isEmpty(subModuleList) && isEmpty(subSubModuleList)) {
          return 'At least one SubModule is required before adding Sub-SubModules.';
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
        if (!this.validationService.validateNullUndefinedEmptyList(milestone?.projectId)) {
          return `Kindly enter the Parent ID in the Milestones sheet for ${milestoneIndex + 1}`;
        }
        if (this.validationService.validateNullUndefinedEmptyList(projectId) && projectId !== milestone?.projectId) {
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
        if (!this.validationService.validateNullUndefinedEmptyList(module?.milestoneId)) {
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
        if (!this.validationService.validateNullUndefinedEmptyList(subModule?.moduleId)) {
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
        if (!this.validationService.validateNullUndefinedEmptyList(subModule?.moduleId)) {
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
        if (!this.validationService.validateNullUndefinedEmptyList(question?.optionType)) {
          return `Kindly enter the Option Type in the Questions sheet for ${questionIndex + 1}`;
        }
        if (question?.optionType != 'text' && question?.optionType != 'checkbox' && question?.optionType != 'radio') {
          return `Kindly enter the proper Option Type(checkbox, radio, text) in the Questions sheet for ${questionIndex + 1}`;
        }
        if (question?.optionType == 'radio' || question?.optionType == 'checkbox') {
          if (!this.validationService.validateNullUndefinedEmptyList(question?.options)) {
            return `Kindly provide Options in the Questions sheet for ${questionIndex + 1}`;
          }
          const options = JSON.parse(question?.options);
          const isValid = Array.isArray(options);
          if (!isValid) {
            return `Kindly provide valid Options in the Questions sheet for ${questionIndex + 1}`;
          }
        }
        if (!this.validationService.validateNullUndefinedEmptyList(question?.entityId)) {
          return `Kindly enter the Parent ID in the Questions sheet for ${questionIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyList(question?.entityType)) {
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
        if (!this.validationService.validateNullUndefinedEmptyList(question?.optionType)) {
          return `Kindly enter the Option Type in the Questions sheet for ${questionIndex + 1}`;
        }
        if (question?.optionType != 'text' && question?.optionType != 'checkbox' && question?.optionType != 'radio') {
          return `Kindly enter the proper Option Type(checkbox, radio, text) in the Questions sheet for ${questionIndex + 1}`;
        }
        if (question?.optionType == 'radio' || question?.optionType == 'checkbox') {
          if (!this.validationService.validateNullUndefinedEmptyList(question?.options)) {
            return `Kindly provide Options in the Questions sheet for ${questionIndex + 1}`;
          }
          const options = JSON.parse(question?.options);
          const isValid = Array.isArray(options);
          if (!isValid) {
            return `Kindly provide valid Options in the Questions sheet for ${questionIndex + 1}`;
          }
        }
        if (!this.validationService.validateNullUndefinedEmptyList(question?.entityId)) {
          return `Kindly enter the Parent ID in the Questions sheet for ${questionIndex + 1}`;
        }
        if (!this.validationService.validateNullUndefinedEmptyList(question?.entityType)) {
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
    const sheetDataMap: any = {};
    const workbook = await this.readExcelFile(file);
    workbook.SheetNames.forEach(sheetName => {
      const sheetInfo = this.extractSheetData(workbook, sheetName);
      switch (sheetName) {
        case 'Project':
          sheetDataMap.projectData = this.mapProjectInsightDetails(sheetInfo.sheetData, sheetInfo.dataHeaders);
          break;
        case 'Milestones':
          sheetDataMap.milestoneList = this.mapProjectInsightMilestoneList(sheetInfo.sheetData, sheetInfo.dataHeaders);
          break;
        case 'Modules':
          sheetDataMap.moduleList = this.mapProjectInsightModuleList(sheetInfo.sheetData, sheetInfo.dataHeaders);
          break;
        case 'SubModules':
          sheetDataMap.subModuleList = this.mapProjectInsightSubModuleList(sheetInfo.sheetData, sheetInfo.dataHeaders);
          break;
        case 'Sub-SubModules':
          sheetDataMap.subSubModuleList = this.mapProjectInsightSubSubModuleList(sheetInfo.sheetData, sheetInfo.dataHeaders);
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

  parseOptionsToList(optionType: any, options: any) {
    if (optionType == 'radio' || optionType == 'checkbox') {
      let optionsList = JSON.parse(options || '[]');
      let quotedOptionValues: string[] = [];
      if (optionsList && optionsList.length > 0) {
        quotedOptionValues = optionsList.map(option => `${option?.optionValue}`);
        return quotedOptionValues;
      }
    } else {
      return null;
    }
  }

  parseListToOptions(optionType: any, options: any) {
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
  }

  private firstNonNull(errors: (string | null)[]): string | null {
    return errors.find(error => !!error) || null;
  }

  parseAssignedToUserId(assignedToUserId: any) {
    let userIdList: number[] = [];
    try {
      const parsed = JSON.parse(assignedToUserId);
      if (Array.isArray(parsed)) {
        userIdList = parsed.map(Number);
      }
      return userIdList;
    } catch (e) {
      console.error("Invalid JSON format for AssignedToUserId");
    }
  }

  parseOptionsOfQuestionToList(questionList: ProjectQuestion[]) {
    if (this.isValidList(questionList)) {
      questionList.forEach((questionObj: ProjectQuestion) => {
        questionObj.projectResponseList = [];
        if (questionObj.optionType == "checkbox" || questionObj.optionType == "radio") {
          questionObj.optionsList = JSON.parse(questionObj.options || '[]');
        }
      });
    }
  }

  createSubModuleListObject(subModuleList: ProjectSubModule[], subModuleType: any) {
    subModuleList.forEach((submodule: any, submodIndex) => {
      this.parseOptionsOfQuestionToList(submodule.questionList);
      if (submodule.subSubModuleList != null && submodule.subSubModuleList?.length != 0) {
        this.createSubModuleListObject(submodule.subSubModuleList, "Sub-SubModule");
      }
    });
  }

  async callGetAllProjectInsightQuestionsByProjectIdAndEmpId(projectId: any, empId: any,performanceTabName : any,employeeRole:any) : Promise<any>{
    let projectInsightObj = await this.getAllProjectInsightQuestionsByProjectIdAndEmpId(projectId, empId,performanceTabName,employeeRole);
    if(projectInsightObj){
      await this.exportProjectInsightEntityQuestionsToExcel(projectInsightObj,'Project');
      return 'Success';
    } else {
      return 'Fail';
    }
  }

  // API Call
  async getAllProjectInsightQuestionsByProjectIdAndEmpId(projectId: any, empId: any,performanceTabName : any,employeeRole:any): Promise<any> {
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
            if (milestone.moduleList != null && milestone.moduleList?.length != 0) {
              milestone.moduleList.forEach((module: any, modIndex) => {
                this.parseOptionsOfQuestionToList(module.questionList);
                if (module.subModuleList != null && module.subModuleList?.length != 0) {
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
        throw(error);
      });
  }

}

