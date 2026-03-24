import { Component, HostListener, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Survey } from 'src/app/models/survey';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { SurveyService } from 'src/app/services/survey.service';
import { QuizViewModalComponent } from 'src/app/training-content-view/quiz-view-modal/quiz-view-modal.component';
import * as XLSX from 'xlsx-js-style';

export interface SurveyRow {
  surveyId: number;
  surveyName: string;
  createdOn: string;
  createdByName: string;
  isActive: string;
  description: string;
  cutOffQuestions: number;
  totalQuestions: number;
}

export type FilterStatus = 'all' | 'true' | 'completed' | 'false';

@Component({
  standalone: false,
  selector: 'app-training-quiz-config',
  templateUrl: './training-quiz-config.component.html',
  styleUrls: ['./training-quiz-config.component.css']
})
export class TrainingQuizConfigComponent implements OnInit {

  trainingName = '';
  trainingId: number | null = null;

  showBuilder = false;
  editingQuizId: number | null = null;
  editingQuiz?: SurveyRow;

  activeFilter: FilterStatus = 'all';

  allSurveyList: SurveyRow[] = [];

  alertMessage: string = '';
  pendingRow: SurveyRow | null = null;
  pendingAction: 'activate' | 'deactivate' | 'complete' | null = null;
  
  // ── Responses ──────────────────────────────────────────
  quizResponses: any[] = [];
  selectedQuizName: string = '';
  selectedUserResponse: any = null;
  responsePage: number = 1;
  responsePageSize: number = 5;
  @ViewChild('responsesTemplate') responsesTemplate!: TemplateRef<any>;
  @ViewChild('userDetailTemplate') userDetailTemplate!: TemplateRef<any>;

  @ViewChild('alertTemplate') alertTemplate!: TemplateRef<any>;
  @ViewChild('confirmTemplate') confirmTemplate!: TemplateRef<any>;

  currentUser: any;
  menuPosition = { top: '0px', right: '0px' };

  constructor(
    private route: ActivatedRoute, 
    private router: Router,
    private surveyService: SurveyService, 
    private modalService: NgbModal,
    private authService: AuthenticationService
  ) {
    this.authService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.trainingName = params['trainingName'] || '';
      this.trainingId   = params['trainingId'] ? +params['trainingId'] : null;
    });
    this.getAllSurveys();
  }

  onBack(): void {
    this.router.navigate(['/configuration/training-config']);
  }

  getAllSurveys(): void {
    this.surveyService.getAllSurveys(this.trainingId, 'quiz').subscribe({
      next: (res: any) => {
        this.allSurveyList = res.serviceResponse;
      },
      error: (err: any) => {
        console.error(err);
        this.alertMessage = 'Error fetching Quiz';
        this.modalService.open(this.alertTemplate, {windowClass: "modal-sm"});
      }
    });
  }

  // ── Filter cards ───────────────────────────────────────
  get totalCount():     number { return this.allSurveyList.length; }
  get activeCount():    number { return this.allSurveyList.filter(s => s.isActive?.toLowerCase() === 'true').length; }
  get completedCount(): number { return this.allSurveyList.filter(s => s.isActive?.toLowerCase() === 'completed').length; }
  get inactiveCount():  number { return this.allSurveyList.filter(s => s.isActive?.toLowerCase() === 'false').length; }

  get filteredList(): SurveyRow[] {
    if (this.activeFilter === 'all') return this.allSurveyList;
    return this.allSurveyList.filter(s => s.isActive?.toLowerCase() === this.activeFilter.toLowerCase());
  }

  setFilter(f: FilterStatus): void {
    this.activeFilter = f;
  }

  // ── Table actions ──────────────────────────────────────
  onEdit(row: SurveyRow): void {
    this.editingQuiz = row;
    this.editingQuizId = row.surveyId;
    this.showBuilder = true;
  }

  onCreateNew(): void {
    this.editingQuizId = null;
    this.showBuilder = true;
  }

  onBuilderBack(): void {
    this.showBuilder = false;
    this.editingQuizId = null;
    this.getAllSurveys();
  }

  onPreview(row: SurveyRow): void {
    const s = new Survey();
    s.surveyId = row.surveyId as any;
    
    this.surveyService.getAllQuestionsBySurveyId(s, true, true).subscribe({
      next: (response: any) => {
        const questionsList = response.serviceResponse;
        if (Array.isArray(questionsList)) {
          const mappedQuestions = questionsList.map((sq: any) => ({
            question: sq.question,
            optionType: sq.optionType || 'radio',
            correctAnswer: sq.correctAnswer,
            optionsList: JSON.parse(sq.options).map((opt: any) => ({
              optionValue: opt.optionValue
            }))
          }));

          const modalRef = this.modalService.open(QuizViewModalComponent, {
            size: 'xl',
            windowClass: 'quiz-preview-modal-window',
            backdrop: 'static',
          });

          modalRef.componentInstance.config = {
            mode: 'preview',
            questions: mappedQuestions,
            quizTitle: row.surveyName,
            cutoffQuestions: row.cutOffQuestions,
            totalQuestions: row.totalQuestions
          };
        }
      },
      error: (err) => {
        console.error('Error fetching quiz questions', err);
      }
    });
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    // Close all menus when clicking elsewhere
    this.allSurveyList.forEach(s => (s as any)['_menuOpen'] = false);
  }

  onViewResponses(row: SurveyRow): void {
    const s = new Survey();
    s.surveyId = row.surveyId as any;
    s.type = 'quiz';
    this.selectedQuizName = row.surveyName;

    this.surveyService.getSurveyAllResponsesBySurveyId(s).subscribe({
      next: (res: any) => {
        if (res.serviceStatus === 'Success' && Array.isArray(res.serviceResponse)) {
          this.responsePage = 1;
          this.groupQuizResponses(res.serviceResponse);
          this.modalService.open(this.responsesTemplate, { size: 'lg', windowClass: 'quiz-responses-modal' });
        } else {
          this.alertMessage = 'No responses found for this quiz.';
          this.openAlertMod(this.alertTemplate, this.alertMessage);
        }
      },
      error: (err) => {
        console.error(err);
        this.alertMessage = 'Error fetching responses.';
        this.openAlertMod(this.alertTemplate, this.alertMessage);
      }
    });
  }

  private groupQuizResponses(flatList: any[]): void {
    const groupedMap = new Map<string, any>();

    flatList.forEach((item) => {
      const key = item.employmentIdAccToET || item.employeementId.toString();
      if (!groupedMap.has(key)) {
        groupedMap.set(key, {
          empId: key,
          name: item.name,
          dateAttended: item.createdOn,
          marksObtained: item.marksObtained,
          passStatus: item.passStatus,
          cutOffQuestions: item.cuttOffQuestions,
          responses: [],
          expanded: false
        });
      }
      groupedMap.get(key).responses.push({
        question: item.question,
        response: item.response,
        correctAnswer: item.correctAnswer,
        options: item.options
      });
    });

    this.quizResponses = Array.from(groupedMap.values());
  }

  onExportToExcel(): void {
    if (!this.quizResponses || this.quizResponses.length === 0) return;

    // Build the data rows
    const exportData: any[] = [];
    
    // Get unique questions from all responses to build headers
    const allQuestionsSet = new Set<string>();
    this.quizResponses.forEach(resp => {
      resp.responses.forEach((qr: any) => allQuestionsSet.add(qr.question));
    });
    const questionHeaders = Array.from(allQuestionsSet);

    this.quizResponses.forEach(resp => {
      const row: any = {
        'Training Name': this.trainingName,
        'Quiz Name': this.selectedQuizName,
        'Cut Off Questions': resp.cutOffQuestions || 'N/A',
        'Total Questions': resp.responses?.length || 'N/A',
        'Employee ID': resp.empId,
        'Employee Name': resp.name,
        'Date Attended': this.formatDate(resp.dateAttended),
        'Marks Obtained': resp.marksObtained,
        'Pass Status': resp.passStatus
      };

      // Add each question's response as a column
      questionHeaders.forEach(qHeader => {
        const matchingResp = resp.responses.find((r: any) => r.question === qHeader);
        row[qHeader] = matchingResp ? matchingResp.response : 'N/A';
      });

      exportData.push(row);
    });

    // Create Worksheet
    const worksheet = XLSX.utils.json_to_sheet(exportData);

    // Styling the Worksheet
    const range = XLSX.utils.decode_range(worksheet['!ref'] || 'A1');
    for (let R = range.s.r; R <= range.e.r; ++R) {
      for (let C = range.s.c; C <= range.e.c; ++C) {
        const cell_address = { c: C, r: R };
        const cell_ref = XLSX.utils.encode_cell(cell_address);
        const cell = worksheet[cell_ref];

        if (!cell) continue;

        // Header Style (Row 0)
        if (R === 0) {
          cell.s = {
            fill: { fgColor: { rgb: "D9E9FF" } }, // Light Blue
            font: { bold: true, color: { rgb: "000000" } },
            alignment: { horizontal: "center", vertical: "center" },
            border: {
              top: { style: "thin", color: { rgb: "000000" } },
              bottom: { style: "thin", color: { rgb: "000000" } },
              left: { style: "thin", color: { rgb: "000000" } },
              right: { style: "thin", color: { rgb: "000000" } }
            }
          };
        } else {
          // Identify Pass Status Column (it's column index 8 or "I")
          const headerCellValue = worksheet[XLSX.utils.encode_cell({ c: C, r: 0 })]?.v;
          if (headerCellValue === 'Pass Status') {
            const status = cell.v ? String(cell.v).toLowerCase() : '';
            if (status === 'pass') {
              cell.s = {
                fill: { fgColor: { rgb: "C6EFCE" } }, // Light Green
                font: { color: { rgb: "006100" }, bold: true },
                alignment: { horizontal: "center" }
              };
            } else if (status === 'fail') {
              cell.s = {
                fill: { fgColor: { rgb: "FFC7CE" } }, // Light Red
                font: { color: { rgb: "9C0006" }, bold: true },
                alignment: { horizontal: "center" }
              };
            }
          }
        }
      }
    }

    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Quiz Responses');

    // Export with dynamic filename
    const filename = `${this.selectedQuizName.replace(/\s+/g, '_')}_Responses.xlsx`;
    XLSX.writeFile(workbook, filename);
  }

  onShowDetailUser(userResp: any): void {
    this.selectedUserResponse = userResp;
    this.modalService.open(this.userDetailTemplate, { size: 'lg', centered: true, windowClass: 'quiz-detail-modal' });
  }

  getPassCount(): number {
    return this.quizResponses.filter(r => r.passStatus?.toLowerCase() === 'pass').length;
  }

  onDeactivate(row: SurveyRow): void {
    this.pendingRow = row;
    this.pendingAction = 'deactivate';
    this.openAlertMod(this.confirmTemplate, `Deactivate "${row.surveyName}"?`);
  }

  onActivate(row: SurveyRow): void {
    this.pendingRow = row;
    this.pendingAction = 'activate';
    this.openAlertMod(this.confirmTemplate, `Activate "${row.surveyName}"? Other active quizzes for this training will be deactivated.`);
  }

  onComplete(row: SurveyRow): void {
    this.pendingRow = row;
    this.pendingAction = 'complete';
    this.openAlertMod(this.confirmTemplate, `Mark "${row.surveyName}" as Completed? It will no longer be available for new attempts.`);
  }

  confirmAction(modal: any): void {
    modal.close();
    if (!this.pendingRow || !this.pendingAction) return;
    const statusMap: Record<string, string> = { activate: 'true', deactivate: 'false', complete: 'Completed' };
    const msgMap: Record<string, string> = {
      activate: 'Quiz activated successfully.',
      deactivate: 'Quiz deactivated successfully.',
      complete: 'Quiz marked as completed.'
    };
    this.changeStatus(this.pendingRow, statusMap[this.pendingAction], msgMap[this.pendingAction]);
    this.pendingRow = null;
    this.pendingAction = null;
  }

  private changeStatus(row: SurveyRow, status: string | boolean, msg: string): void {
    const s = new Survey();
    s.surveyId = row.surveyId;
    s.isActive = status as any;
    s.type = 'quiz';
    (s as any).trainingId = this.trainingId;
    s.updatedBy = this.currentUser?.empId;

    this.surveyService.changeSurveyStatus(s).subscribe({
      next: (res: any) => {
        if (res.serviceStatus === 'Success') {
          this.alertMessage = msg;
          this.openAlertMod(this.alertTemplate, msg);
          this.getAllSurveys();
        } else {
          this.alertMessage = res.serviceResponse || 'Failed to update status';
          this.openAlertMod(this.alertTemplate, this.alertMessage);
        }
      },
      error: (err) => {
        console.error(err);
        this.alertMessage = 'An error occurred while updating status';
        this.openAlertMod(this.alertTemplate, this.alertMessage);
      }
    });
  }

  openAlertMod(template: TemplateRef<any>, message: string): void {
    this.alertMessage = message;
    this.modalService.open(template, { size: 'sm', centered: true });
  }

  isCompleted(row: SurveyRow): boolean {
    return row.isActive?.toLowerCase() === 'completed';
  }

  isInactive(row: SurveyRow): boolean {
    return row.isActive?.toLowerCase() === 'false';
  }

  statusLabel(s: string): string {
    const val = s?.toLowerCase();
    return val === 'completed' ? 'Completed' : val === 'true' ? 'Active' : 'Inactive';
  }

  statusClass(s: string): string {
    const val = s?.toLowerCase();
    return val === 'completed' ? 'badge-completed' : val === 'true' ? 'badge-active' : 'badge-inactive';
  }

  formatDate(d: string): string {
    return new Date(d).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  openMenu(event: MouseEvent, row: SurveyRow): void {
    event.stopPropagation();
    this.allSurveyList.forEach(s => (s as any)['_menuOpen'] = false);
    const btn = event.currentTarget as HTMLElement;
    const rect = btn.getBoundingClientRect();
    const menuWidth = 175;
    (row as any)['_menuTop']  = `${rect.bottom + 6}px`;
    (row as any)['_menuLeft'] = `${rect.right - menuWidth}px`;
    (row as any)['_menuOpen'] = true;
  }

  trackBySurveyId(_: number, s: SurveyRow): number { return s.surveyId; }
}