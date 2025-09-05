import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
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
import ExcelJs from 'exceljs/dist/exceljs.min.js';
import { TableFieldConfig } from '../models/tableFieldConfig';

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

export interface ValidationResult {
  success: boolean;
  message?: string;
}

export interface AnalyzeResult {
  validation: ValidationResult;
  tableConfig?: TableFieldConfig;
  tableData?: any[];
}

type tableColumnDataTypes = "Text" | "Number" | "Date";

@Injectable({
  providedIn: 'root'
})
export class ProjectInsightImportExportService {

  workBook: any;
  rowTableResult: any
  private allowedOptionTypes = ['Checkbox', 'Date', 'Email', 'File', 'Number', 'Radio', 'Select', 'Table', 'Text', 'Textarea'];
  private allowedListOptionTypes = ['Checkbox', 'Radio', 'Select'];
  private allowedGroupTypes = ['Activity', 'Feature', 'Milestone', 'Tasks'];
  private headers = ["Section", "Title", "FieldWidth", "Required", "OptionType", "IsMultiSelect", "Option", "Value"];

  instructions: any = [
    ["1. Project Section"],
    ["Mandatory Fields: ProjectName, IndustryDomain"],
    ["To add fields in a Project:"],
    ["- In the Section column, enter 'Project'."],
    ["- Allowed OptionType: 'Checkbox, Date, Email,  Number, Radio, Select, Table, Text, Textarea"],
    [""],

    ["2. Group Section"],
    ["Mandatory Fields: GroupTitle, GroupType"],
    ["To add fields in a Group:"],
    ["- Use 'Group-1', 'Group-2', ... for groups."],
    ["- Use 'Group-1-1', 'Group-1-2' for subgroups."],
    ["Allowed GroupType values: Activity, Feature, Milestone, Tasks"],
    ["- Allowed OptionType: 'Checkbox, Date, Email,  Number, Radio, Select, Table, Text, Textarea"],
    [""],

    ["3. Questions & Data"],
    ["To add a Question:"],
    ["- Use 'Project-|-Question' or 'Group-1-|-Question'."],
    ["Allowed OptionType values for Questions: Checkbox, Radio, Text"],
    ["Options Format (Checkbox, Radio, Select): Option 1, Option 2, Option 3 or [\"Option 1\", \"Option 2\", \"Option 3\"]"],
    ["Value Format (for Checkbox, Select): Value 1, Value 2, Value 3 or [\"Value 1\", \"Value 2\", \"Value 3\"]"],
    ["Values must match one of the options(Checkbox, Radio, Select)."],
    [""],

    ["4. General Rules"],
    ["- Follow suffix-based hierarchy for Groups/Sub-Groups."],
    ["- Mandatory fields cannot be blank."],
    ["- Date format must be 'YYYY-MM-DD'."],
    ["For a Table OptionType, specify the value as SheetName!A1:D5, including the sheet name and top-left to bottom-right cell range."]
    [""],

    ["5. Field-Specific Rules"],
    ["- Required → Only for form fields (not questions). Allowed values: True, False"],
    ["- IsMultiSelect → Only if OptionType = select. Allowed values: True, False"],
    ["- FieldWidth → Only for form fields (not questions). Allowed values: 25, 33, 50, 75, 100"]
  ];

  exampleData: any = [
    // Project Section
    ["Project", "ProjectName", "", "True", "Text", "", "", "Test-Project-1"],
    ["Project", "IndustryDomain", "", "True", "Select", "True", "Banking,Finance,IT", "Banking,IT"],

    // Project Question
    ["Project-|-Question", "What is the purpose of the project?", "", "", "Textarea", "", "", "To create a central repository."],

    // Group Section
    ["Group-1", "GroupTitle", "", "True", "Text", "", "", "Test-Group-1"],
    ["Group-1", "GroupType", "", "True", "Select", "False", 'Activity, Feature, Milestone, Tasks', "Milestone"],

    // Group Question
    ["Group-1-|-Question", "This is Test-Group-1 Question-1?", "", "", "Radio", "", 'Yes,No', "Yes"],

    // Sub Group Section
    ["Group-1-1", "GroupTitle", "", "True", "Text", "", "", "Test-Sub-Group-1"],
    ["Group-1-1", "GroupType", "", "True", "Select", "False", 'Activity, Feature, Milestone, Tasks', "Feature"],

    // Sub Group Question
    ["Group-1-1-|-Question", "This is Test-Sub-Group-1 Question-1?", "", "", "Select", "True", 'Option1,Option2,Option3', 'Option1,Option3']
  ];

  constructor(private validationService: ValidationService, private projectService: ProjectService
    , private projectInsightService: ProjectInsightService, private employeeService: EmployeeService
  ) { }

  async downloadTemplate() {
    const workbook = new ExcelJs.Workbook();
    // Sheet 1: Template
    const sheet = workbook.addWorksheet('Template');
    // Headers
    const headerRow = sheet.addRow(this.headers);
    // Style headers: bold + background color
    headerRow.eachCell((cell: any) => {
      cell.font = { bold: true, color: { argb: 'FFFFFFFF' } };
      cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: '4472C4' } };
      cell.alignment = { vertical: 'middle', horizontal: 'center' };
    });

    // Example Data
    this.exampleData.forEach(row => sheet.addRow(row));

    // Apply dropdowns
    this.applyDropdowns(sheet);

    // Autofit column widths based on content
    sheet.columns.forEach((column: any) => {
      let maxLength = 10; // minimum width
      column.eachCell({ includeEmpty: true }, (cell: any) => {
        const cellValue = cell.value ? cell.value.toString() : '';
        maxLength = Math.max(maxLength, cellValue.length);
      });
      column.width = maxLength + 2; // add some padding
    });

    // Sheet 2: Instructions
    const instructionSheet = workbook.addWorksheet('Instructions');
    this.instructions.forEach(row => instructionSheet.addRow(row));

    // Adjust width of first column in Instructions sheet
    instructionSheet.getColumn(1).width = 100;

    // Export file
    const buffer = await workbook.xlsx.writeBuffer();
    saveAs.saveAs(new Blob([buffer]), 'Project_Insight_Template.xlsx');
  }

  private applyDropdowns(sheet: any) {
    // FieldWidth dropdown
    sheet.getColumn(3).eachCell((cell: any, rowNumber: number) => {
      if (rowNumber > 1) {
        cell.alignment = { vertical: 'middle', horizontal: 'center' };
        cell.dataValidation = { type: 'list', allowBlank: true, formulae: ['"25,33,50,75,100"'] };
      }
    });

    // Required dropdown
    sheet.getColumn(4).eachCell((cell: any, rowNumber: number) => {
      if (rowNumber > 1) {
        cell.alignment = { vertical: 'middle', horizontal: 'center' };
        cell.dataValidation = { type: 'list', allowBlank: true, formulae: ['"True,False"'] };
      }
    });

    // OptionType dropdown
    sheet.getColumn(5).eachCell((cell: any, rowNumber: number) => {
      if (rowNumber > 1) {
        cell.alignment = { vertical: 'middle', horizontal: 'center' };
        cell.dataValidation = { type: 'list', allowBlank: true, formulae: ['"Checkbox,Date,Email,File,Number,Radio,Select,Table,Text,Textarea"'] };
      }
    });

    // IsMultiSelect dropdown
    sheet.getColumn(6).eachCell((cell: any, rowNumber: number) => {
      if (rowNumber > 1) {
        cell.alignment = { vertical: 'middle', horizontal: 'center' };
        cell.dataValidation = { type: 'list', allowBlank: true, formulae: ['"True,False"'] };
      }
    });
  }

  async downloadProjectInsightDetailsExcel(dataList: any[]): Promise<void> {
    const workbook = new ExcelJs.Workbook();

    // --- Sheet 1: Project Insight Details ---
    const sheet = workbook.addWorksheet('Project Insight Details');

    // Add headers
    const headerRow = sheet.addRow(this.headers);

    // Style headers: bold + background color
    headerRow.eachCell((cell: any) => {
      cell.font = { bold: true, color: { argb: 'FFFFFFFF' } };
      cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: '4472C4' } };
      cell.alignment = { vertical: 'middle', horizontal: 'center' };
    });

    // Add data rows
    dataList.forEach(dto => {
      sheet.addRow([
        dto.section,
        dto.title,
        dto.fieldWidth,
        dto.required,
        dto.optionType,
        dto.isMultiSelect,
        Array.isArray(dto.option) ? JSON.stringify(dto.option) : dto.option,
        this.processValue(workbook, dto)
      ]);
    });

    // Apply dropdown validations
    this.applyDropdownsWithValues(sheet);

    // Autofit column widths
    sheet.columns.forEach((column: any) => {
      let maxLength = 10; // min width
      column.eachCell({ includeEmpty: true }, (cell: any) => {
        const cellValue = cell.value ? cell.value.toString() : '';
        maxLength = Math.max(maxLength, cellValue.length);
      });
      column.width = maxLength + 2;
    });

    // --- Sheet 2: Instructions ---
    const instructionSheet = workbook.addWorksheet('Instructions');
    this.instructions.forEach(row => instructionSheet.addRow(row));
    instructionSheet.getColumn(1).width = 100;

    // Export file
    const buffer = await workbook.xlsx.writeBuffer();
    saveAs.saveAs(new Blob([buffer]), 'Project_Insight_Details.xlsx');
  }

  private applyDropdownsWithValues(sheet: ExcelJs.Worksheet) {
    // FieldWidth dropdown
    sheet.getColumn(3).eachCell((cell: any, rowNumber: number) => {
      if (rowNumber > 1) {
        cell.alignment = { vertical: 'middle', horizontal: 'center' };
        cell.dataValidation = {
          type: 'list',
          allowBlank: true,
          formulae: ['"25,33,50,75,100"']
        };
        // keep existing value if present
        if (cell.value && !['25', '33', '50', '75', '100'].includes(cell.value.toString())) {
          cell.value = null; // reset if invalid
        }
      }
    });

    // Required dropdown
    sheet.getColumn(4).eachCell((cell: any, rowNumber: number) => {
      if (rowNumber > 1) {
        cell.alignment = { vertical: 'middle', horizontal: 'center' };
        cell.dataValidation = {
          type: 'list',
          allowBlank: true,
          formulae: ['"True,False"']
        };
        if (cell.value !== null && cell.value !== undefined) {
          const normalized = (cell.value === true || cell.value === 'true') ? 'True'
            : (cell.value === false || cell.value === 'false') ? 'False' : null;
          cell.value = normalized;
        }
      }
    });

    // OptionType dropdown
    const optionTypes = ["Checkbox", "Date", "Email", "File", "Number", "Radio", "Select", "Table", "Text", "Textarea"];
    sheet.getColumn(5).eachCell((cell: any, rowNumber: number) => {
      if (rowNumber > 1) {
        cell.alignment = { vertical: 'middle', horizontal: 'center' };
        cell.dataValidation = {
          type: 'list',
          allowBlank: true,
          formulae: [`"${optionTypes.join(',')}"`]
        };

        if (cell.value && this.validationService.validateNullUndefinedEmptyString(cell.value)) {
          const rawValue = cell.value.toString().toLowerCase().trim();

          // Find matching option (case-insensitive)
          const matched = optionTypes.find(opt => opt.toLowerCase() === rawValue);

          if (matched) {
            cell.value = matched; // normalize to correct spelling + case
          } else {
            cell.value = null; // reset if invalid or typo
          }
        }
      }
    });

    // IsMultiSelect dropdown
    sheet.getColumn(6).eachCell((cell: any, rowNumber: number) => {
      if (rowNumber > 1) {
        cell.alignment = { vertical: 'middle', horizontal: 'center' };
        cell.dataValidation = {
          type: 'list',
          allowBlank: true,
          formulae: ['"True,False"']
        };
        if (cell.value !== null && cell.value !== undefined) {
          const normalized = (cell.value === true || cell.value === 'true') ? 'True'
            : (cell.value === false || cell.value === 'false') ? 'False' : null;
          cell.value = normalized;
        }
      }
    });
  }

  processValue(workbook: any, data: any) {
    if (this.validationService.validateNullUndefinedEmptyString(data?.optionType) && data?.optionType?.toLowerCase().trim() !== 'table') {
      return Array.isArray(data?.value) ? JSON.stringify(data?.value) : data?.value;
    } else if (this.validationService.validateNullUndefinedEmptyString(data?.optionType) && data?.optionType?.toLowerCase().trim() === 'table') {
      return this.addTableFromConfig(workbook, this.cleanString(data?.title), data?.tableConfig, data?.value)
    } else {
      return data?.value;
    }
  }

  addTableFromConfig(workbook: ExcelJs.Workbook, sheetName: string, tableConfig: any, value: any): string {
    const sheet = workbook.addWorksheet(sheetName);

    // --- Add Header Row ---
    const headerLabels = tableConfig.columns.map((col: any) => col.label);
    const headerRow = sheet.addRow(headerLabels);

    // Style header row
    headerRow.eachCell((cell: ExcelJs.Cell) => {
      cell.font = { bold: true, color: { argb: 'FFFFFFFF' } };
      cell.fill = { type: 'pattern', pattern: 'solid', fgColor: { argb: '4472C4' } };
      cell.alignment = { vertical: 'middle', horizontal: 'center', wrapText: true };
    });

    // --- Add Data Rows ---
    for (let r = 0; r < tableConfig.rows; r++) {
      const valueObj = value?.[r] || {};
      const rowValues = tableConfig.columns.map((col: any) => {
        const rawValue = valueObj[col.name];
        return rawValue !== undefined && rawValue !== null ? rawValue : '';
      });
      sheet.addRow(rowValues);
    }

    // --- Autofit column widths ---
    sheet.columns.forEach((col: any) => {
      let maxLength = 10;
      col.eachCell({ includeEmpty: true }, (cell: ExcelJs.Cell) => {
        const val = cell.value ? cell.value.toString() : '';
        maxLength = Math.max(maxLength, val.length);
      });
      col.width = maxLength + 2;
    });

    // --- Return range ---
    const lastColIndex = tableConfig.columns.length;
    const lastColLetter = this.getExcelColumnLetter(lastColIndex);
    const lastRow = tableConfig.rows + 1; // +1 for header
    return `${sheetName}!A1:${lastColLetter}${lastRow}`;
  }

  // Utility: Convert column index → Excel letter (A, B, ... Z, AA, AB, etc.)
  private getExcelColumnLetter(colNum: number): string {
    let letter = '';
    while (colNum > 0) {
      const mod = (colNum - 1) % 26;
      letter = String.fromCharCode(65 + mod) + letter;
      colNum = Math.floor((colNum - mod) / 26);
    }
    return letter;
  }


  parseExcel(file: File): Observable<{ success: boolean; message?: string; structure?: ProjectSectionData }> {
    this.workBook = null;
    return new Observable(observer => {
      const reader = new FileReader();

      reader.onload = (e: any) => {
        try {
          const workbook = this.readWorkbook(e.target.result);
          this.workBook = workbook;
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

          const projectNameOrIndustryDomainValidation = this.checkTitleColumn(rows);
          if (!projectNameOrIndustryDomainValidation.success) {
            observer.next(projectNameOrIndustryDomainValidation);
            observer.complete();
            return;
          }

          for (let i = 0; i < rows.length; i++) {
            const row = rows[i];
            if (row && row?.OptionType?.trim()?.toLowerCase() === 'file') {
              continue;
            }
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

  private fail(rowNumber: number, message: string) {
    return { success: false, message: `Row ${rowNumber}: ${message}` };
  }

  private readWorkbook(buffer: ArrayBuffer): XLSX.WorkBook {
    const data = new Uint8Array(buffer);
    return XLSX.read(data, { type: 'array', cellDates: true });
  }

  private extractValue(value: any): string | null {
    if (!this.validationService.validateNullUndefinedEmptyString(value)) return null;
    return typeof value === 'string' ? value.trim() : value;
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

  private getParentGroupName(section: string): string | null {
    if (!this.isSubGroup(section)) return null;

    const hasProjectPrefix = section.startsWith('Project-');
    let normalized = section.replace(/^Project-/, '').replace(/^Group-/, '');
    const parts = normalized.split('-');
    const parent = parts.slice(0, parts.length - 1).join('-');

    return hasProjectPrefix ? `Project-Group-${parent}` : `Group-${parent}`;
  }

  private getGroupDepth(section: string): number {
    // Normalize: remove optional "Project-" and "Group-" prefixes
    let normalized = section.replace(/^Project-/, '').replace(/^Group-/, '');
    return normalized.split('-').filter(p => p.trim() !== '').length;
  }

  private addRowToProjectStructure(projectStructure: ProjectSectionData, sectionName: string, row: ExcelRow) {
    if (!sectionName) return;
    this.rowTableResult = this.analyzeRowTable(row?.Value, this.workBook);

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

    switch (row.Title.toLowerCase()?.trim()) {
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
        projectStructure.fields.push(this.transformExcelRowToField(row));
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

  private createGroup(sectionName: string, row: ExcelRow): GroupSectionData {
    const details = new ProjectInsightGroupDetails();
    details.isDraft = 'Y';

    if (['grouptitle', 'group title'].includes(row?.Title.toLowerCase()?.trim())) {
      details.groupTitle = row?.Value;
    }
    if (['grouptype', 'group type'].includes(row?.Title.toLowerCase()?.trim())) {
      details.groupType = row?.Value.toLowerCase()?.trim();
    }

    const group: GroupSectionData = { fields: [], questions: [], subGroups: {}, projectInsightGroupDetails: details };
    (group as any)._sectionName = sectionName;
    return group;
  }

  private addToGroup(group: GroupSectionData, row: ExcelRow, isQuestion: boolean) {
    if (['grouptype', 'group type'].includes(row?.Title.toLowerCase()?.trim()) && !group.projectInsightGroupDetails.groupType) {
      group.projectInsightGroupDetails.groupType = row?.Value.toLowerCase()?.trim();
    }

    if (isQuestion) {
      group.questions ??= [];
      group.questions.push(this.transformExcelRowToQuestion(row));
    } else {
      if (!['grouptype', 'group type', 'grouptitle', 'group title'].includes(row.Title?.toLowerCase()?.trim())) {
        group.fields ??= [];
        group.projectInsightGroupDetails.additionalInfo = this.addAdditionalInfoToDetails(group.projectInsightGroupDetails, row);
        group.fields.push(this.transformExcelRowToField(row));
      }
    }
  }

  private addAdditionalInfoToDetails(projectInsightGroupDetails: ProjectInsightGroupDetails, excelRow: ExcelRow) {
    if (!projectInsightGroupDetails.additionalInfo) {
      projectInsightGroupDetails.additionalInfo = new Map<string, any>();
    }

    if (excelRow?.Title) {
      if (excelRow?.OptionType?.toLowerCase().trim() === 'table') {
        if (this.rowTableResult?.validation?.success) {
          projectInsightGroupDetails.additionalInfo[this.cleanString(excelRow.Title)] = this.rowTableResult?.tableData;
        }
      } else {
        projectInsightGroupDetails.additionalInfo[this.cleanString(excelRow.Title)] = this.parseFieldValuesToList(excelRow.OptionType, excelRow?.Value);
      }
    }
    return projectInsightGroupDetails.additionalInfo;
  }

  private addAdditionalInfoToProjectDetails(projectInsightProjectDetails: ProjectInsightProjectDetails, excelRow: ExcelRow) {
    if (!projectInsightProjectDetails.additionalInfo) {
      projectInsightProjectDetails.additionalInfo = new Map<string, any>();
    }
    if (excelRow?.Title) {
      if (excelRow?.OptionType?.toLowerCase().trim() === 'table') {
        if (this.rowTableResult?.validation?.success) {
          projectInsightProjectDetails.additionalInfo[this.cleanString(excelRow.Title)] = this.rowTableResult?.tableData;
        }
      } else {
        projectInsightProjectDetails.additionalInfo[this.cleanString(excelRow.Title)] = this.parseFieldValuesToList(excelRow.OptionType, excelRow?.Value);
      }
    }
  }

  private transformExcelRowToQuestion(excelRow: ExcelRow): ProjectInsightQuestionDetails {
    const question = new ProjectInsightQuestionDetails();
    question.optionType = excelRow?.OptionType?.toLowerCase()?.trim();
    question.optionsList = this.parseOptionsToList(excelRow?.OptionType?.toLowerCase()?.trim(), excelRow?.Option)?.obj ?? [];
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
    if (['industrydomain', 'industry domain'].includes(excelRow?.Title?.toLowerCase()?.trim())) {
      this.assignDomainDefaultValue(field);
    }
    field.width = excelRow?.FieldWidth;
    field.type = excelRow?.OptionType?.toLowerCase()?.trim();
    field.defaultValue = (!['checkbox', 'select'].includes(field.type)) ? excelRow?.Value : '';
    const optionsListObj = this.parseOptionsToFormList(excelRow?.OptionType, excelRow?.Option);
    if (optionsListObj && optionsListObj?.success) {
      field.options = optionsListObj.obj;
    }
    if (field.type === 'table') {
      field.defaultValue = '';
      if (this.rowTableResult?.validation?.success) {
        field.tableConfig = this.rowTableResult?.tableConfig;
      }
    }
    return field;
  }

  private assignDomainDefaultValue(field: any) {
    field.type = 'select';
    field.label = 'Domain';
    field.name = 'domainname';
    field.optionSource = 'api';
    field.multiple = true;
    field.apiUrl = 'api/getAllProjectInsightDomain';
    field.apiLabelKey = 'name';
    field.apiValueKey = 'id';
    field.required = true;
    field.options = [];
    field.width = 33;
    field.rowPosition = 0;
  }

  private formatDateToYMD(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private generateUniqueId(): string {
    return Date.now() + '_' + Math.random().toString(36).substr(2, 9);
  }

  private cleanString(input: string): string {
    if (!this.validationService.validateNullUndefinedEmptyString(input)) return '';
    return input
      .replace(/[^a-zA-Z0-9]/g, "") // remove everything except letters & numbers
      .toLowerCase();               // convert to lowercase
  }

  private parseOptionsToList(optionType: string, optionsRaw: any) {
    if (!['radio', 'checkbox', 'select'].includes(optionType?.toLowerCase()?.trim())) return null;
    return this.parseJsonToSurveyOptions(optionsRaw, 'Options');
  }

  private parseOptionsToFormList(optionType: string, optionsRaw: any) {
    if (!['radio', 'checkbox', 'select'].includes(optionType?.toLowerCase()?.trim())) return null;
    return this.parseJsonToFormOptions(optionsRaw, 'Options');
  }

  private parseValuesToList(optionType: string, valuesRaw: any) {
    if (!['checkbox', 'select'].includes(optionType?.toLowerCase()?.trim())) return null;
    return this.parseJsonToSurveyOptions(valuesRaw, 'Values');
  }

  private parseFieldValuesToList(optionType: string, valuesRaw: any) {
    if (!['checkbox', 'select'].includes(optionType?.toLowerCase()?.trim())) return valuesRaw;
    try {
      if (!valuesRaw || valuesRaw.trim().length === 0) {
        return [];
      }
      let parsed: any[] = [];
      if (valuesRaw.trim().startsWith("[")) { // Case 1: input is JSON array string
        parsed = JSON.parse(valuesRaw);
      } else {  // Case 2: plain comma-separated string
        parsed = valuesRaw.split(",").map((v: string) => v.trim()).filter(Boolean);
      }
      if (!Array.isArray(parsed) || parsed.length === 0) {
        return [];
      }
      return parsed;
    } catch {
      return [];
    }
  }

  private parseJsonToSurveyOptions(input: any, label: string): { success: boolean; obj?: SurveyOption[]; message?: string } {
    try {
      let parsed: any[] = [];
      if (!input || input.trim().length === 0) {
        return { success: false, message: `${label} must not be empty` };
      }
      if (input.trim().startsWith("[")) { // Case 1: input is JSON array string
        parsed = JSON.parse(input);
      } else {  // Case 2: plain comma-separated string
        parsed = input.split(",").map((v: string) => v.trim()).filter(Boolean);
      }
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
      return { success: false, message: `${label} must be a valid JSON array or comma-separated list` };
    }
  }

  private parseJsonToFormOptions(input: any, label: string): { success: boolean; obj?: any; message?: string } {
    try {

      let parsed: string[] = [];
      if (!input || input.trim().length === 0) {
        return { success: false, message: `${label} must not be empty` };
      }
      if (input.trim().startsWith("[")) { // Case 1: input is JSON array string
        parsed = JSON.parse(input);
      } else {  // Case 2: plain comma-separated string
        parsed = input.split(",").map((v: string) => v.trim()).filter(Boolean);
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
      return { success: false, message: `${label} must be a valid JSON array or comma-separated list` };
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

  private checkTitleColumn(rows: ExcelRow[]) {
    const projectTargets = ["projectname", "project name"];
    const projectFound = rows.some(row => {
      const cellValue = row.Title?.toString().toLowerCase().trim();
      return cellValue && projectTargets.includes(cellValue);
    });
    if (!projectFound) {
      return { success: false, message: `Project Name is mandatory` };
    }

    const industryDomainTargets = ['industrydomain', 'industry domain'];
    const industryDomainFound = rows.some(row => {
      const cellValue = row.Title?.toString().toLowerCase().trim();
      return cellValue && industryDomainTargets.includes(cellValue);
    });
    if (!industryDomainFound) {
      return { success: false, message: `Industry Domain is mandatory` };
    }

    return { success: true, message: `Success` };
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

        if (!this.isValidGroupType(value.toLowerCase())) {
          return this.fail(rowNumber, `Invalid Group Type for ${section}. Allowed: Milestone, Feature, Activity, Tasks`);
        }
      }
    }

    // OptionType validations
    if (!this.validationService.validateNullUndefinedEmptyString(optionType)) {
      return this.fail(rowNumber, `OptionType cannot be null or empty in section ${section}`);
    }

    if (optionType && !this.isValidOptionType(optionType)) {
      return this.fail(rowNumber, `Invalid OptionType '${optionType}' in section ${section}`);
    }

    if (optionType === 'date' && value && !this.isValidISODate(this.formatDateToYMD(new Date(value)))) {
      return this.fail(rowNumber, `Invalid date in section ${section}. Expected 'YYYY-MM-DD'.`);
    }

    if (optionType === 'email' && value && !this.isValidEmail(value)) {
      return this.fail(rowNumber, `Invalid Email format`);
    }

    if (optionType === 'table') {
      if (!this.validationService.validateNullUndefinedEmptyString(value)) {
        return this.fail(rowNumber, `Table Range cannot be null or empty in section ${section}`);
      }

      if (!this.isValidSheetRange(value)) {
        return this.fail(rowNumber, `Invalid Table Range format : '${value}'. Expected format e.g. "SheetName!A1:D5" in section ${section}`);
      }
      this.rowTableResult = this.analyzeRowTable(value, this.workBook);
      if (!this.rowTableResult?.validation?.success) {
        return this.fail(rowNumber, `${this.rowTableResult?.validation?.message} for section ${section}`);
      }
    }

    // List option validations
    if (optionType && this.isValidOptionListType(optionType)) {
      const optionsValidation = this.validateOptionsAndValues(optionType, row.Option, value, section, rowNumber, row);
      if (!optionsValidation.success) return optionsValidation;
    }

    return { success: true };
  }

  private validateOptionsAndValues(optionType: string, optionRaw: string, value: any, section: string, rowNumber: number, row: any) {
    const optionsListObj = this.parseOptionsToList(optionType, optionRaw?.trim());
    if (!['grouptype', 'group type', 'industrydomain', 'industry domain'].includes(row?.Title?.toLowerCase()?.trim()) && !row?.Title?.toLowerCase()?.trim().includes('domain')) {
      if (!optionsListObj || !optionsListObj?.success) {
        return this.fail(rowNumber, `Invalid Options format for section ${section}. Expected: Option 1,Option 2`);
      }
    }

    if (!value) return { success: true };

    if (['checkbox', 'select'].includes(optionType?.toLowerCase()?.trim())) {
      const valuesListObj = this.parseValuesToList(optionType, value);
      if (!['grouptype', 'group type', 'industrydomain', 'industry domain'].includes(row?.Title?.toLowerCase()?.trim()) && !row?.Title?.toLowerCase()?.trim().includes('domain')) {
        if (!valuesListObj || !valuesListObj.success) {
          return this.fail(rowNumber, `Invalid Values format for section ${section}. Expected: Value1,Value2`);
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

  private hasInvalidValues(values: SurveyOption[], options: SurveyOption[]): boolean {
    const optionSet = new Set(options.map(o => o.optionValue?.toLowerCase()?.trim()));
    return values.some(v => !optionSet.has(v.optionValue?.toLowerCase()?.trim()));
  }

  private isImmediateGroup(section: string): boolean {
    return this.getGroupDepth(section) === 1;
  }

  private isSubGroup(section: string): boolean {
    return this.getGroupDepth(section) > 1;
  }

  private isValidOptionType(optionType: any) {
    return this.allowedOptionTypes.some(optionT => optionT?.toLowerCase() === optionType.toLowerCase());
  }

  private isValidOptionListType(optionType: any) {
    return this.allowedListOptionTypes.some(option => option?.toLowerCase() === optionType.toLowerCase());
  }

  private isValidGroupType(groupType: any) {
    return this.allowedGroupTypes.some(groupT => groupT?.toLowerCase() === groupType.toLowerCase());
  }

  private isMissing(title: string, value: any, expectedTitles: any[]): boolean {
    return expectedTitles.includes(title?.toLowerCase()) && !value;
  }

  private isValidSheetRange(range: string): boolean {
    const regex = /^[A-Za-z0-9_ ]+![A-Z]+\d+:[A-Z]+\d+$/;
    return regex.test(range);
  }

  private analyzeRowTable(ref: string, workbook: XLSX.WorkBook): AnalyzeResult {
    try {
      const [sheetName, rangePart] = ref.split('!');
      if (!sheetName || !rangePart) {
        return { validation: { success: false, message: `Invalid reference format: '${ref}'. Expected format e.g. "SheetName!A1:D5"` } };
      }

      const sheet = workbook.Sheets[sheetName];
      if (!sheet) {
        return { validation: { success: false, message: `Sheet '${sheetName}' not found in workbook` } };
      }

      let decoded: XLSX.Range;
      try {
        decoded = XLSX.utils.decode_range(rangePart);
      } catch {
        return { validation: { success: false, message: `Invalid range '${rangePart}' in '${ref}'` } };
      }

      // Load full sheet
      let sheetData: any[][] = XLSX.utils.sheet_to_json(sheet, { header: 1, defval: '' });

      if (sheetData.length === 0 || sheetData[0].length === 0) {
        return { validation: { success: false, message: `Range '${rangePart}' produced no data in '${sheetName}'` } };
      }

      // --- Fill merged cells relative to sliced range
      if (sheet['!merges']) {
        sheet['!merges'].forEach(merge => {
          const start = merge.s;
          const end = merge.e;
          const value = sheetData[start.r - decoded.s.r]?.[start.c - decoded.s.c];
          for (let r = start.r; r <= end.r; r++) {
            for (let c = start.c; c <= end.c; c++) {
              const rr = r - decoded.s.r;
              const cc = c - decoded.s.c;
              if (rr >= 0 && cc >= 0 && rr < sheetData.length && cc < sheetData[0].length) {
                if (!sheetData[rr][cc]) sheetData[rr][cc] = value;
              }
            }
          }
        });
      }
      console.log('==========================================================================');
      console.log(sheetData);

      // --- Headers
      const headers: string[] = [];
      sheetData[0].forEach((headerValue, i) => {
        if (!headerValue) {
          return { validation: { success: false, message: `Missing header in '${sheetName}' at column index ${decoded.s.c + i + 1}` } };
        }
        headers.push(headerValue.toString().trim());
      });

      if (headers.length === 0) {
        return { validation: { success: false, message: `No headers found in range '${rangePart}' of '${sheetName}'` } };
      }

      // --- Data rows
      const dataRows: any[] = [];
      let rowCount = 0;
      for (let r = 1; r < sheetData.length; r++) { // start after header row
        const rowObj: any = {};
        headers.forEach((header, i) => {
          rowObj[this.cleanString(header)] = sheetData[r]?.[i] ?? '';
        });

        const isEmpty = Object.values(rowObj).every(v => v === '');
        if (!isEmpty) {
          rowCount++;
          dataRows.push(rowObj);
        }
      }

      if (dataRows.length === 0) {
        return { validation: { success: false, message: `No data rows found in range '${rangePart}' of '${sheetName}'` } };
      }

      const tableConfig: TableFieldConfig = {
        columns: headers.map(h => ({ name: this.cleanString(h), label: h, type: 'text' })),
        rows: rowCount
      };

      return { validation: { success: true }, tableConfig, tableData: dataRows };

    } catch (err: any) {
      return { validation: { success: false, message: `Unexpected error: ${err.message}` } };
    }
  }

}