import { Component, ElementRef, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { PageDTO } from 'src/app/models/pageDTO';
import { ProjectInsightQuestionDetails } from 'src/app/models/projectInsightQuestionDetails';
import { ProjectInsightQuestionLibraryEntry } from 'src/app/models/projectInsightQuestionLibraryEntry';
import { SurveyOption } from 'src/app/models/sureyOption';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { DepartmentService } from 'src/app/services/department.service';
import { ProjectInsightQuestionLibraryService } from 'src/app/services/project-insight-question-library.service';
import { ValidationService } from 'src/app/services/validation.service';
import * as XLSX from 'xlsx';
import { saveAs } from 'file-saver';
import { ProjectInsightFacetCategory } from 'src/app/models/projectInsightFacetCategory';
import { ProjectInsightFacetService } from 'src/app/services/project-insight-facet.service';

@Component({
  standalone: false,
  selector: 'app-project-insight-question-library',
  templateUrl: './project-insight-question-library.component.html',
  styleUrls: ['./project-insight-question-library.component.scss']
})
export class ProjectInsightQuestionLibraryComponent implements OnInit {

  @ViewChild('alert_message_modal') alertMessageTemplate: TemplateRef<any>;
  @ViewChild('add_or_update_project_insight_question_entry_modal') addOrUpdateProjectInsightQuestionEntryModal: TemplateRef<any>;
  @ViewChild('delete_project_insight_question_entry_modal') deleteProjectInsightQuestionEntryModal: TemplateRef<any>;
  @ViewChild('fileInput') fileInput: ElementRef;


  alertModalRef:NgbModalRef;
  addOrUpdateProjectInsightQuestionEntryModalRef:NgbModalRef;
  deleteProjectInsightQuestionEntryModalRef:NgbModalRef;

  // Variables
  searchKeyword: any;
  alertMessage: any = '';
  isQuestionUpdate: boolean = false;

  currentUser: User;
  deptColors: any = {};
  files: FileList;
  questionLibraryEntryPage: PageDTO = new PageDTO();
  questionLibraryEntry: ProjectInsightQuestionLibraryEntry = new ProjectInsightQuestionLibraryEntry();
  deleteQuestionLibraryEntry: ProjectInsightQuestionLibraryEntry = new ProjectInsightQuestionLibraryEntry();

  // List
  allDeptList: any[] = [];
  departmentColors: string[] = [];
  selectedDeptList: any[] = [];
  selectedDeptIdList: any[] = [];
  projectInsightQuestionLibraryEntryList: ProjectInsightQuestionLibraryEntry[] = [];
  headers: string[] = [];
  excelData: any[] = [];
  modifiedExcelData: ProjectInsightQuestionLibraryEntry[] = [];
  requiredHeaders = ['Sr.no', 'Question', 'Description', 'OptionType', 'OptionsList(List)', 'Depts(List)'];
  filteredCategories:ProjectInsightFacetCategory[] = [];
  facetCategoryList:ProjectInsightFacetCategory[] = [];

  filterText: string = '';

  constructor(
    private departmentService: DepartmentService,
    private modalService: NgbModal,
    private validationService: ValidationService,
    private authenticationService: AuthenticationService,
    private projectInsightFacetService: ProjectInsightFacetService,
    private projectInsightQuestionLibraryService: ProjectInsightQuestionLibraryService,
  ) { this.authenticationService.currentUser.subscribe(x => this.currentUser = x); }

  ngOnInit(): void {
    this.questionLibraryEntryPage = new PageDTO();
    this.resetQuestionLibraryEntryPage();
    this.getAllDepartmentList();
    this.getAllProjectInsightQuestionsEntry();
  }

  // Fetch And Save APIs [Start]
  getAllDepartmentList() {
    this.allDeptList = [];
    this.selectedDeptList = [];
    this.departmentService.getAllDeptsList().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus == "Success") {
        this.allDeptList = response.serviceResponse;
        this.generateRandomColors();
      } else {
        console.error(response.serviceResponse)
      }
    });
  }

  getAllProjectInsightQuestionsEntry() {
    this.projectInsightQuestionLibraryEntryList = [];
    this.projectInsightQuestionLibraryService.getAllProjectInsightQuestionsEntry(this.questionLibraryEntryPage).pipe(first()).subscribe((response: any) => {
      this.projectInsightQuestionLibraryEntryList = response?.content || [];
    });
  }

  getAllProjectInsightQuestionsByDepartment(deptId: any) {
    this.projectInsightQuestionLibraryEntryList = [];
    if (!this.validationService.validateNullUndefinedEmptyList(this.selectedDeptIdList)) {
      this.selectedDeptIdList = [];
      this.selectedDeptIdList.push(deptId);
    } else {
      if (this.selectedDeptIdList.includes(deptId)) {
        this.selectedDeptIdList.forEach((departmentId, index) => {
          if (departmentId == deptId) {
            this.selectedDeptIdList.splice(index, 1);
            return;
          }
        });
      } else {
        this.selectedDeptIdList.push(deptId);
      }
    }
    this.questionLibraryEntryPage.filterIdList = this.selectedDeptIdList;
    if (this.validationService.validateNullUndefinedEmptyList(this.questionLibraryEntryPage.filterIdList)) {
      this.projectInsightQuestionLibraryService.getAllProjectInsightQuestionEntriesByDepartment(this.questionLibraryEntryPage).pipe(first()).subscribe((response: any) => {
        this.projectInsightQuestionLibraryEntryList = response?.content || [];
      });
    } else {
      this.resetQuestionLibraryEntryPage();
      this.getAllProjectInsightQuestionsEntry();
    }
  }

  addOrUpdateQuestionLibraryEntry() {
    let inputValidated: boolean = this.validateProjectInsightQuestionLibraryEntry(this.questionLibraryEntry);
    if (!inputValidated) return;

    if (this.isQuestionUpdate) {
      this.questionLibraryEntry.updatedBy = this.currentUser.empId;
    } else {
      this.questionLibraryEntry.createdBy = this.currentUser.empId;
    }
    this.projectInsightQuestionLibraryService.saveProjectInsightQuestionLibraryEntry(this.questionLibraryEntry).pipe(first()).subscribe(
      (response: any) => {
        this.openAlertModal(response.serviceStatus);
        this.closeAddOrUpdateProjectInsightQuestionEntryModal();
        this.getAllProjectInsightQuestionsEntry();
      },
      (error) => {
        console.log(error, " : error");
        this.openAlertModal(error?.error?.serviceResponse || 'An unexpected error occurred.');
      }
    );
  }

  deleteProjectInsightQuestionLibraryEntryById() {
    this.projectInsightQuestionLibraryService.deleteProjectInsightQuestionLibraryEntryById(this.deleteQuestionLibraryEntry).pipe(first()).subscribe({
      next: (response: any) => {
        this.openAlertModal(response.serviceStatus);
        this.closeDeleteProjectInsightQuestionEntryModal();
        this.resetQuestionLibraryEntryPage();
        this.getAllProjectInsightQuestionsEntry();
      },
      error: (error: any) => {
        this.openAlertModal(error);
      }
    });
  }

  saveEntryToQuestionLibraryFromExcel() {
    let obj = { projectInsightQuestionLibraryEntryList: this.modifiedExcelData, createdBy: this.currentUser.empId };
    this.projectInsightQuestionLibraryService.saveEntryToQuestionLibraryFromExcel(obj).pipe(first()).subscribe({
      next: (response: any) => {
        this.openAlertModal(response.serviceStatus);
        this.clearFileInput();
        this.resetQuestionLibraryEntryPage();
        this.getAllProjectInsightQuestionsEntry();
      },
      error: (error: any) => {
        this.openAlertModal(error);
      }
    });
  }
  // Fetch And Save APIs [End]

  // Searching & Sorting [Start]
  onGlobalSearch() {
    this.questionLibraryEntryPage.searchKeyword = this.searchKeyword;
    this.projectInsightQuestionLibraryService.getAllProjectInsightQuestionEntriesByFilter(this.questionLibraryEntryPage).pipe(first()).subscribe({
      next: (response: any) => {
        this.projectInsightQuestionLibraryEntryList = response?.content || [];
      },
      error: (error: any) => {
        this.openAlertModal(error);
      }
    });
  }

  handlePageChange(event) {
    this.questionLibraryEntryPage.page = event;
  }

  sortData(sort: Sort) {
    if (sort.active) {
      let sortParams: any[] = sort.active?.split("|");
      this.questionLibraryEntryPage.sortColumn = sortParams[0] ? sortParams[0] : 'createdOn';
      this.questionLibraryEntryPage.sortColumnType = sortParams[1];
      this.questionLibraryEntryPage.sortDirection = sort.direction === 'asc' ? 'asc' : 'desc';
    } else {
      this.resetQuestionLibraryEntryPage();
    }
  }
  // Searching & Sorting [End]


  // Option Configurations [Start]
  addOption(optionIndex: any) {
    this.questionLibraryEntry?.optionsList.splice(optionIndex + 1, 0, new SurveyOption());
  }

  removeOption(optionIndex: any) {
    this.questionLibraryEntry?.optionsList.splice(optionIndex, 1);
  }

  setOption() {
    if (this.questionLibraryEntry?.optionType == "checkbox" || this.questionLibraryEntry?.optionType == "radio") {
      this.questionLibraryEntry.optionsList = [];
      this.questionLibraryEntry.optionsList.splice(1, 0, new SurveyOption());
    }
  }
  // Option Configurations [End]


  // Utility [Start]
  generateRandomColors() {
    this.departmentColors = this.allDeptList.map(() => this.getRandomColor());
  }

  getRandomColor(): string {
    const letters = '0123456789ABCDEF';
    let color = '#';
    for (let i = 0; i < 6; i++) {
      color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
  }

  resetQuestionLibraryEntryPage() {
    this.questionLibraryEntryPage.page = 1;
    this.questionLibraryEntryPage.size = 10;
    this.questionLibraryEntryPage.sortDirection = 'desc';
    this.questionLibraryEntryPage.sortColumn = 'createdOn';
  }

  async getDeptIdsFromDeptName(deptNames: any, questionIndex: any) {
    let deptNamesTemp: any;
    try {
      deptNamesTemp = JSON.parse(deptNames || '[]');
    } catch (error) {
      this.openAlertModal(`Invalid format for Department in question #${questionIndex + 1}. Please check the format: ["Dept1", "Dept2"].`);
      this.clearFileInput();
      return [];
    }

    try {
      const response: any = await this.departmentService.getAllDeptsList().pipe(first()).toPromise();
      const depts = response.serviceResponse || [];
      const deptsIds = depts
        .filter((dept: any) => deptNamesTemp.includes(dept.name))
        .map((dept: any) => dept.deptId);

      if (!this.validationService.validateNullUndefinedEmptyList(deptsIds)) {
        this.openAlertModal(`Please provide at least One Valid Department for Question ${questionIndex + 1} !!`);
        this.clearFileInput();
        return [];
      }
      return deptsIds;
    } catch (error) {
      console.error('Error fetching department list:', error);
      return [];
    }
  }

  parseOptionsToList(optionType: any, options: any, questionIndex: number) {
    if (optionType === 'radio' || optionType === 'checkbox') {
      let optionsList = [];
      try {
        optionsList = JSON.parse(options || '[]');
      } catch (error) {
        this.openAlertModal(`Invalid format for Options in question #${questionIndex + 1}. Please check the format: ["Option 1", "Option 2"].`);
        this.clearFileInput();
        return [];
      }
      let quotedOptionValues: SurveyOption[] = [];
      if (Array.isArray(optionsList) && optionsList.length > 0) {
        for (let i = 0; i < optionsList.length; i++) {
          let temp: SurveyOption = new SurveyOption();
          temp.optionValue = optionsList[i];
          quotedOptionValues.push(temp);
        }
        return quotedOptionValues;
      } else {
        return [];
      }
    } else {
      return null;
    }
  }
  // Utility [End]

  // Validations [Start]
  validateProjectInsightQuestionLibraryEntry(questionLibraryEntry: any) {
    let flag = true;
    if (!this.validationService.validateNullUndefinedEmptyString(questionLibraryEntry?.question)) {
      this.openAlertModal("Please Enter Question !!");
      return false;
    }
    if (!this.validationService.validateNullUndefinedEmptyString(questionLibraryEntry?.optionType)) {
      this.openAlertModal("Please select option type !!");
      return false;
    }
    if (questionLibraryEntry?.optionType !== 'checkbox' && questionLibraryEntry?.optionType !== 'radio' && questionLibraryEntry?.optionType !== 'text') {
      this.openAlertModal("Please select a valid option type !!");
      return false;
    }
    if ((questionLibraryEntry?.optionType && questionLibraryEntry?.optionType === 'checkbox' || questionLibraryEntry?.optionType === 'radio') && !this.validationService.validateNullUndefinedEmptyList(questionLibraryEntry?.optionsList)) {
      this.openAlertModal("Please add options for Option Type Checkbox or Radio !!");
      return false;
    }
    if ((questionLibraryEntry?.optionType && questionLibraryEntry?.optionType === 'checkbox' || questionLibraryEntry?.optionType === 'radio') && this.validationService.validateNullUndefinedEmptyList(questionLibraryEntry?.optionsList)) {
      if ((questionLibraryEntry?.optionType && questionLibraryEntry?.optionType === 'checkbox' || questionLibraryEntry?.optionType === 'radio') && questionLibraryEntry?.optionsList?.length < 2) {
        this.openAlertModal("Please provide atleast 2 options for Option Type Checkbox or Radio !!");
        return false;
      }
      for (let i = 0; i < questionLibraryEntry?.optionsList?.length; i++) {
        let option = questionLibraryEntry?.optionsList[i];
        if (!this.validationService.validateNullUndefinedEmptyString(option?.optionValue)) {
          this.openAlertModal(`Option cannot be null or Empty for Option ${i + 1}!!`);
          return false;
        }
      }
    }
    if (!this.validationService.validateNullUndefinedEmptyList(questionLibraryEntry.deptIds)) {
      this.openAlertModal("Please select atleast One Department !!");
      return false;
    }
    return flag;
  }
  // Validations [End]

  // Import & Export [Start]
  exportTemplate() {
    // Function to create and download Excel
    const generateExcel = (deptNames?: any[]) => {
      const exampleData = [
        {
          'Sr.no': 1,
          'Question': 'What is your project name?',
          'Description': 'Provide the official project name',
          'OptionType': 'text // e.g., text, radio, checkbox',
          'OptionsList(List)': '["Option 1", "Option 2"] // COMMA SEPARATED OPTIONS EACH IN STRAIGHT DOUBLE QUOTES',
          'Depts(List)': '["Development", "APM"] // COMMA SEPARATED DEPT NAME EACH IN STRAIGHT DOUBLE QUOTES'
        }
      ];

      const wsTemplate: XLSX.WorkSheet = XLSX.utils.json_to_sheet(exampleData, {
        header: [
          'Sr.no', 'Question', 'Description', 'OptionType', 'OptionsList(List)', 'Depts(List)'
        ]
      });

      const wb: XLSX.WorkBook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, wsTemplate, 'Question_Library_Template');

      // Add Departments sheet only if deptNames is provided
      if (deptNames && deptNames.length > 0) {
        const wsDepartments: XLSX.WorkSheet = XLSX.utils.json_to_sheet(deptNames, {
          header: ['Dept Name']
        });
        XLSX.utils.book_append_sheet(wb, wsDepartments, 'Departments');
      }

      const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'array' });
      saveAs(
        new Blob([wbout], { type: 'application/octet-stream' }),
        'Question_Library_Export_Template.xlsx'
      );
    };

    // Fetch department list
    this.departmentService.getAllDepartments().pipe(first()).subscribe(
      (response: any) => {
        const deptNames = response.serviceResponse.map((dept) => ({
          'Dept Name': dept.name || ''
        }));
        generateExcel(deptNames); // With departments
      },
      (error) => {
        console.error('Error fetching department list:', error);
        generateExcel(); // Without departments
      }
    );
  }

  clearFileInput(): void {
    const fileInput = document.getElementById('inputQuestionLibraryEntry') as HTMLInputElement;
    fileInput.value = '';
    this.headers = null;
    this.files = null;
  }

  handleFileInput(event: any) {
    this.files = event.target.files;
    let elem = document.getElementById('inputQuestionLibraryEntry') as HTMLInputElement;
    if (this.files && this.files.length > 0) {
      const file: File = this.files[0];
      const fileName = file.name;
      const fileExtension = fileName.split(".").pop();

      if (fileExtension !== 'xlsx') {
        this.openAlertModal("Only .xlsx file is allowed.");
        this.clearFileInput();
        elem.value = null;
        return false;
      }

      const reader: FileReader = new FileReader();

      reader.onload = (e: any) => {
        const data: string = e.target.result;
        const workbook: XLSX.WorkBook = XLSX.read(data, { type: 'binary' });
        const sheetName: string = workbook.SheetNames[0];
        const worksheet: XLSX.WorkSheet = workbook.Sheets[sheetName];
        // Get & Validate headers
        this.headers = this.getHeaders(worksheet);
        const isValid = this.requiredHeaders.every(header => this.headers.includes(header));
        if (!isValid) {
          this.openAlertModal("Invalid Excel format: Missing required headers.");
          this.clearFileInput();
          return;
        }
        // Process the data
        const jsonData: any[] = XLSX.utils.sheet_to_json(worksheet, { raw: false });
        this.processData(jsonData);
      };
      reader.readAsBinaryString(file);
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

  async processData(data: any[]): Promise<void> {
    this.excelData = [];
    data.forEach((row: any) => {
      const rowData: any = {}; // Create an object to store the row data
      this.headers.forEach(header => {
        // Assign the header and corresponding value to the rowData object
        rowData[header] = row[header];
      });
      this.excelData.push(rowData);
    });

    if (!this.validationService.validateNullUndefinedEmptyList(this.excelData)) {
      this.openAlertModal("Uploaded Excel Must contain atleast One Question to Process !!");
      this.clearFileInput();
      return;
    }

    /// make OptionType Data to Uppercase
    this.excelData = this.excelData.map(row => {
      return {
        ...row,
        OptionType: row.OptionType ? row?.OptionType.toLowerCase().trim() : null
      };
    });

    this.modifiedExcelData = [];
    let outerFlag = true;
    let outerMessage = '';

    for (let i = 0; i < this.excelData?.length; i++) {
      const row = this.excelData[i];

      const projectInsightQuestionLibraryEntry = new ProjectInsightQuestionLibraryEntry();
      projectInsightQuestionLibraryEntry.question = row["Question"];
      if (!this.validationService.validateNullUndefinedEmptyString(projectInsightQuestionLibraryEntry?.question)) {
        outerFlag = false;
        outerMessage = `Please provide Question ${i + 1} !!`;
        break;
      }
      projectInsightQuestionLibraryEntry.description = row["Description"];
      projectInsightQuestionLibraryEntry.optionType = row["OptionType"];
      if (!this.validationService.validateNullUndefinedEmptyString(projectInsightQuestionLibraryEntry?.optionType)) {
        outerFlag = false;
        outerMessage = `Please provide Option type for Question ${i + 1} !!`;
        break;
      }
      if (projectInsightQuestionLibraryEntry?.optionType.toLowerCase() !== 'checkbox' && projectInsightQuestionLibraryEntry?.optionType.toLowerCase() !== 'radio' && projectInsightQuestionLibraryEntry?.optionType.toLowerCase() !== 'text') {
        outerFlag = false;
        outerMessage = `Please provide a valid Option type for Question ${i + 1} !!`;
        break;
      }

      projectInsightQuestionLibraryEntry.optionsList = projectInsightQuestionLibraryEntry?.optionType && projectInsightQuestionLibraryEntry?.optionType.toLowerCase() !== 'text' ? this.parseOptionsToList(projectInsightQuestionLibraryEntry?.optionType.toLowerCase(), row["OptionsList(List)"], i) : [];
      if (projectInsightQuestionLibraryEntry?.optionType.toLowerCase() === 'checkbox' || projectInsightQuestionLibraryEntry?.optionType.toLowerCase() === 'radio') {
        if (!this.validationService.validateNullUndefinedEmptyList(projectInsightQuestionLibraryEntry?.optionsList)) {
          outerFlag = false;
          outerMessage = `Please add options for Option Type Checkbox or Radio for Question ${i + 1} !!`;
          break;
        }
        if (projectInsightQuestionLibraryEntry?.optionsList?.length < 2) {
          outerFlag = false;
          outerMessage = `Please provide atleast 2 options for Option Type Checkbox or Radio for Question ${i + 1} !!`;
          break;
        }

        let flag = true;
        let message = '';
        for (let j = 0; j < projectInsightQuestionLibraryEntry?.optionsList?.length; j++) {
          let option = projectInsightQuestionLibraryEntry?.optionsList[j];
          if (!this.validationService.validateNullUndefinedEmptyString(option?.optionValue)) {
            message = `Option cannot be null or Empty for Option ${i + 1} !!`;
            flag = false;
            break;
          }
        }

        if (!flag) {
          outerFlag = false;
          outerMessage = message;
          break;
        }
      }

      try {
        projectInsightQuestionLibraryEntry.depts = JSON.parse(row["Depts(List)"] || '[]');
      } catch (error) {
        this.openAlertModal(`Invalid format for Department in question #${i + 1}. Please check the format: ["Dept1", "Dept2"].`);
        this.clearFileInput();
        return;
      }

      projectInsightQuestionLibraryEntry.deptIds = await this.getDeptIdsFromDeptName(row["Depts(List)"], i);
      this.modifiedExcelData.push(projectInsightQuestionLibraryEntry);
    }
    if (!outerFlag) {
      this.openAlertModal(outerMessage);
      this.clearFileInput();
      return;
    }
    console.log(this.modifiedExcelData);
  }
  // Import & Export [End]

  // Modals [Start]
  openAlertModal(message: any) {
    this.alertMessage = message;
    this.alertModalRef = this.modalService.open(this.alertMessageTemplate, { modalDialogClass: 'modal-sm' });
  }

  cancelRequest() {
    if (this.alertModalRef) {
      this.alertModalRef?.close();
    }
  }

  async openCreateOrUpdateQuestionLibraryEntryModal(isQuestionUpdate: any, question?: any) {
    this.isQuestionUpdate = isQuestionUpdate;
    this.questionLibraryEntry = question || new ProjectInsightQuestionLibraryEntry();
    await this.getAllProjectInsightFacetCategory();
    this.addOrUpdateProjectInsightQuestionEntryModalRef = this.modalService.open(this.addOrUpdateProjectInsightQuestionEntryModal, { modalDialogClass: 'modal-lg modal-dialog-centered' });
  }

  openDeleteQuestionLibraryEntryModal(question: any) {
    this.deleteQuestionLibraryEntry = question;
    this.deleteProjectInsightQuestionEntryModalRef = this.modalService.open(this.deleteProjectInsightQuestionEntryModal, { modalDialogClass: 'modal-sm' });

  }

  closeAddOrUpdateProjectInsightQuestionEntryModal() {
    if (this.addOrUpdateProjectInsightQuestionEntryModalRef) {
      this.addOrUpdateProjectInsightQuestionEntryModalRef?.close();
    }
  }

  closeDeleteProjectInsightQuestionEntryModal() {
    if (this.deleteProjectInsightQuestionEntryModalRef) {
      this.deleteProjectInsightQuestionEntryModalRef?.close();
    }
  }
  // Modals [End]

  getAllProjectInsightFacetCategory(): Promise<any> {
    this.facetCategoryList = [];
    this.filteredCategories = [];
    return this.projectInsightFacetService.getAllProjectInsightFacetCategory().pipe(first())
      .toPromise()
      .then((response: any) => {
        this.facetCategoryList = response;
        this.filteredCategories = response;
      })
      .catch(error => {
        console.log(error, " : error");
        this.openAlertModal('An unexpected error occurred while fetching Project Insight Facet.');
      });
  }

  filterCategories() {
    if (!this.validationService.validateNullUndefinedEmptyString(this.filterText)) {
      return;
    }
    const filterValue = (this.filterText || '').toLowerCase();
    this.filteredCategories = this.facetCategoryList?.filter(option =>
      option.categoryName.toLowerCase().includes(filterValue) &&
      !this.questionLibraryEntry?.facetCategoryList?.some(
        selected => selected.categoryName.toLowerCase() === option.categoryName.toLowerCase()
      )
    );
  }

  addFacetCategoryToField(selectedValue?: string) {
    const value = (selectedValue || this.filterText || '').trim();
    if (!value) return;

    if (!this.questionLibraryEntry.facetCategoryList) {
      this.questionLibraryEntry.facetCategoryList = [];
    }

    const allCategoryList = this.facetCategoryList?.map(obj => obj.categoryName.toLowerCase()) || [];
    const categoryList = this.questionLibraryEntry.facetCategoryList.map(obj => obj.categoryName.toLowerCase());

    if (allCategoryList.includes(value.toLowerCase()) && !categoryList.includes(value.toLowerCase())) {
      const facet = this.facetCategoryList.find(
        obj => obj.categoryName.trim().toLowerCase() === value.toLowerCase()
      );
      if (facet) {
        this.questionLibraryEntry.facetCategoryList.push(facet);
      }
      this.resetFilter();
      return;
    }

    if (!categoryList.includes(value.toLowerCase())) {
      this.questionLibraryEntry.facetCategoryList.push({ categoryName: value } as ProjectInsightFacetCategory);
    }
    this.resetFilter();
  }

  private resetFilter() {
    this.filterText = null;
    this.filteredCategories = this.facetCategoryList?.filter(
      option =>
        !this.questionLibraryEntry.facetCategoryList.some(
          selected => selected.categoryName.toLowerCase() === option.categoryName.toLowerCase()
        )
    );
  }

  removeFacetCategoryFromField(index: number) {
    if (this.questionLibraryEntry.facetCategoryList) {
      let question = this.questionLibraryEntry?.facetCategoryList[index];
      if (question && question?.facetCategoryId != undefined && question?.facetCategoryId != null) {
        if (!this.questionLibraryEntry?.facetCategoryIds) {
          return;
        }
        this.questionLibraryEntry.facetCategoryIds = this.questionLibraryEntry?.facetCategoryIds?.filter(facetId => facetId !== question.facetCategoryId);
      }
      this.questionLibraryEntry.facetCategoryList.splice(index, 1);
    }
    this.filteredCategories = this.facetCategoryList?.filter(option =>
      !this.questionLibraryEntry?.facetCategoryList?.some(
        selected => selected.categoryName.toLowerCase() === option.categoryName.toLowerCase()
      )
    );
  }

  clearSearch() {
    this.filterText = null;
  }
}
