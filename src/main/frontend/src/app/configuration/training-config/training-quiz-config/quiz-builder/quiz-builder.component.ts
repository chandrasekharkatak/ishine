import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnInit,
  ViewChild,
  TemplateRef,
} from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { SurveyOption } from 'src/app/models/sureyOption';
import { Survey } from 'src/app/models/survey';
import { SurveyQuestion } from 'src/app/models/surveyQuestion';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { SurveyService } from 'src/app/services/survey.service';
import { QuizViewModalComponent } from 'src/app/training-content-view/quiz-view-modal/quiz-view-modal.component';
import { SurveyRow } from '../training-quiz-config.component';

export interface QuizOption {
  text: string;
  correct: boolean;
}

export interface QuizQuestion {
  id: number; // local UI id
  dbId?: number; // backend id
  text: string;
  inputType: 'radio' | 'checkbox';
  options: QuizOption[];
  collapsed: boolean;
}

export interface ValidationError {
  field: string;
  message: string;
}

@Component({
  standalone: false,
  selector: 'app-quiz-builder',
  templateUrl: './quiz-builder.component.html',
  styleUrls: ['./quiz-builder.component.css'],
})
export class QuizBuilderComponent implements OnInit {
  @Input() trainingName = '';
  @Input() trainingId: number | null = null;
  @Input() editingQuizId: number | null = null;
  @Input() isActive: boolean = false;
  @Input() editingQuiz?: SurveyRow;

  @Output() backToList = new EventEmitter<void>();

  @ViewChild('askForBackTpl') askForBackTpl!: TemplateRef<any>;
  @ViewChild('askForResetTpl') askForResetTpl!: TemplateRef<any>;
  @ViewChild('alertTemplate') alertTemplate!: TemplateRef<any>;
  alertMessage: string = '';

  // ── Config fields ──────────────────────────────────────
  quizTitle = '';
  quizDesc = '';
  cutoffQuestions: number | null = null;

  questions: QuizQuestion[] = [];
  private qIdCounter = 0;
  private initialSnapshot = '';

  // ── Validation ─────────────────────────────────────────
  validationErrors: ValidationError[] = [];
  invalidQuestionIds = new Set<number>();
  showValidationPanel = false;

  // ── Preview modal ──────────────────────────────────────
  private previewModalRef: NgbModalRef | null = null;
  previewActiveIndex = 0;

  // ── Toast ──────────────────────────────────────────────
  toastMessage = '';
  toastType: 'success' | 'error' = 'success';
  toastVisible = false;
  private toastTimer: any;
  quizObj: Survey = new Survey();
  currUser: User;
  isDirty = false;

  readonly OPTION_LETTERS = ['A', 'B', 'C', 'D', 'E', 'F'];

  constructor(
    private modalService: NgbModal,
    private auth: AuthenticationService,
    private surveyService: SurveyService,
  ) {
    this.currUser = this.auth.currentUserValue;
  }

  ngOnInit(): void {
    if (this.editingQuizId) {
      // Pre-populate from provided record data if available
      if (this.editingQuiz) {
        this.quizTitle = this.editingQuiz.surveyName || '';
        this.quizDesc = this.editingQuiz.description || '';
        this.cutoffQuestions = this.editingQuiz.cutOffQuestions || null;
        this.isActive = this.editingQuiz.isActive.toLowerCase() === 'true';
      }
      console.log("This editing Quiz", this.editingQuiz);
      
      this.fetchQuiz(this.editingQuizId);
    } else {
      // For new quiz, initialize with empty state (or dummy if desired)
      this.questions = [];
      this.initialSnapshot = this.getSnapshot();
      this.isDirty = false;
    }
  }

  private fetchQuiz(id: number): void {
    const s = new Survey();
    s.surveyId = id as unknown as any;
    this.surveyService.getAllQuestionsBySurveyId(s, true).subscribe({
      next: (res: any) => {
        const questionsList = res.serviceResponse;
        if (Array.isArray(questionsList)) {
          this.questions = questionsList.map((sq: any) => ({
            id: this.genId(),
            dbId: sq.surveyQuestionId,
            text: sq.question,
            inputType: sq.optionType || 'radio',
            collapsed: true,
            options: JSON.parse(sq.options).map((opt: any) => ({
              text: opt.optionValue,
              correct: sq.correctAnswer && sq.correctAnswer.split(', ').includes(opt.optionValue)
            }))
          }));
        }
        this.initialSnapshot = this.getSnapshot();
        this.isDirty = false;
      },
      error: (err) => {
        console.error('Error fetching quiz', err);
        this.initialSnapshot = this.getSnapshot();
        this.isDirty = false;
      }
    });
  }

  private getSnapshot(): string {
    return JSON.stringify({
      title: this.quizTitle,
      desc: this.quizDesc,
      cutoff: this.cutoffQuestions,
      isActive: this.isActive,
      questions: this.questions.map(q => ({
        text: q.text,
        type: q.inputType,
        options: q.options.map(o => ({ text: o.text, correct: o.correct }))
      }))
    });
  }

  get totalQuestions(): number {
    return this.questions.length;
  }

  get pageHeading(): string {
    if (this.editingQuizId) {
      return `Editing Quiz #${this.editingQuizId}${this.trainingName ? ' · ' + this.trainingName : ''}`;
    }
    return this.trainingName
      ? `Adding Quiz for ${this.trainingName}`
      : 'Create New Quiz';
  }

  private genId(): number {
    return ++this.qIdCounter;
  }

  // ── Question completeness check ────────────────────────
  /** Returns true if the question is fully filled (text + ≥2 filled options + correct answer set) */
  isQuestionComplete(q: QuizQuestion): boolean {
    if (!q.text.trim()) return false;
    if (this.filledOptionCount(q) < 2) return false;
    if (!this.hasCorrect(q)) return false;
    return true;
  }

  /** Returns the first incomplete question, or null if all complete */
  private getFirstIncompleteQuestion(): QuizQuestion | null {
    return this.questions.find((q) => !this.isQuestionComplete(q)) ?? null;
  }

  // ── Add Question (blocked if last is incomplete) ────────
  addQuestion(): void {
    // If there are existing questions, the last one must be complete
    if (this.questions.length > 0) {
      const incomplete = this.getFirstIncompleteQuestion();
      if (incomplete) {
        // Expand the incomplete card and highlight it
        incomplete.collapsed = false;
        this.invalidQuestionIds.add(incomplete.id);
        const idx = this.questionIndex(incomplete) + 1;
        this.alertMessage = `Question ${idx} is incomplete — fill it before adding a new one.`;
        this.modalService.open(this.alertTemplate, { size: 'sm' });
        setTimeout(() => {
          const el = document.getElementById('q-' + incomplete.id);
          if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 80);
        return;
      }
    }

    const q: QuizQuestion = {
      id: this.genId(),
      text: '',
      inputType: 'radio',
      options: [
        { text: '', correct: false },
        { text: '', correct: false },
        { text: '', correct: false },
        { text: '', correct: false },
      ],
      collapsed: false,
    };
    this.questions.push(q);
    this.updateDirty();
    setTimeout(() => {
      const el = document.getElementById('q-' + q.id);
      if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }, 80);
  }

  // ── Clear invalid highlight when user edits ────────────
  onQuestionChange(q: QuizQuestion): void {
    if (this.isQuestionComplete(q)) {
      this.invalidQuestionIds.delete(q.id);
    }
    this.updateDirty();
  }

  // ── Full validation for Save ────────────────────────────
  validateQuiz(): ValidationError[] {
    const errors: ValidationError[] = [];
    this.invalidQuestionIds.clear();

    // 1. Quiz title
    if (!this.quizTitle.trim()) {
      errors.push({ field: 'quizTitle', message: 'Quiz title is required.' });
    }

    // 2. At least one question
    if (this.questions.length === 0) {
      errors.push({
        field: 'questions',
        message: 'Add at least one question before saving.',
      });
    }

    // 3. Cutoff questions ≤ total questions
    if (
      this.cutoffQuestions != null &&
      this.cutoffQuestions > this.questions.length
    ) {
      errors.push({
        field: 'cutoffQuestions',
        message: `Cutoff questions (${this.cutoffQuestions}) cannot exceed total questions (${this.questions.length}).`,
      });
    }

    if (this.cutoffQuestions != null && this.cutoffQuestions < 1) {
      errors.push({
        field: 'cutoffQuestions',
        message: 'Cutoff questions must be at least 1.',
      });
    }

    // 4. Per-question validation
    this.questions.forEach((q, i) => {
      const qNum = i + 1;
      let qHasError = false;

      // 4a. Question text
      if (!q.text.trim()) {
        errors.push({
          field: `q${q.id}_text`,
          message: `Q${qNum}: Question text is empty.`,
        });
        qHasError = true;
      }

      // 4b. At least 2 options with text
      const filledCount = this.filledOptionCount(q);
      if (filledCount < 2) {
        errors.push({
          field: `q${q.id}_options`,
          message: `Q${qNum}: At least 2 answer options must be filled.`,
        });
        qHasError = true;
      }

      // 4c. Correct answer selected
      if (!this.hasCorrect(q)) {
        errors.push({
          field: `q${q.id}_correct`,
          message: `Q${qNum}: No correct answer selected.`,
        });
        qHasError = true;
      }

      // 4d. Correct answer must be among filled options
      if (this.hasEmptyCorrectOption(q)) {
        errors.push({
          field: `q${q.id}_correct_empty`,
          message: `Q${qNum}: Correct answer option cannot be empty.`,
        });
        qHasError = true;
      }

      if (qHasError) this.invalidQuestionIds.add(q.id);
    });

    return errors;
  }

  // ── Input type ──────────────────────────────────────────
  setInputType(
    q: QuizQuestion,
    type: 'radio' | 'checkbox',
    event: Event,
  ): void {
    event.stopPropagation();
    if (q.inputType === type) return;
    q.inputType = type;
    if (type === 'radio') {
      let found = false;
      q.options.forEach((o) => {
        if (o.correct && found) o.correct = false;
        if (o.correct) found = true;
      });
    }
    this.onQuestionChange(q);
  }

  setCorrect(q: QuizQuestion, index: number): void {
    if (q.inputType === 'checkbox') {
      q.options[index].correct = !q.options[index].correct;
    } else {
      q.options.forEach((o, i) => (o.correct = i === index));
    }
    this.onQuestionChange(q);
  }

  hasCorrect(q: QuizQuestion): boolean {
    return q.options.some((o) => o.correct);
  }

  correctCount(q: QuizQuestion): number {
    return q.options.filter((o) => o.correct).length;
  }

  /** Number of options that have non-empty text — used in template (no arrow fns allowed inline) */
  filledOptionCount(q: QuizQuestion): number {
    let count = 0;
    for (const o of q.options) {
      if (o.text.trim()) count++;
    }
    return count;
  }

  /** True if any option is marked correct but has no text */
  hasEmptyCorrectOption(q: QuizQuestion): boolean {
    for (const o of q.options) {
      if (o.correct && !o.text.trim()) return true;
    }
    return false;
  }

  correctStatusText(q: QuizQuestion): string {
    if (!this.hasCorrect(q)) return '○ No correct answer set';
    if (q.inputType === 'checkbox') {
      const cnt = this.correctCount(q);
      return `✓ ${cnt} correct answer${cnt > 1 ? 's' : ''} selected`;
    }
    return '✓ Correct answer set';
  }

  addOption(q: QuizQuestion): void {
    if (q.options.length >= 6) return;
    q.options.push({ text: '', correct: false });
    this.updateDirty();
  }

  removeOption(q: QuizQuestion): void {
    if (q.options.length <= 2) return;
    // If the removed option was correct, clear correct status
    const last = q.options[q.options.length - 1];
    if (last.correct) {
      // nothing extra needed — it will just be removed
    }
    q.options.pop();
    this.onQuestionChange(q);
  }

  toggleCollapse(q: QuizQuestion): void {
    q.collapsed = !q.collapsed;
  }

  duplicateQuestion(q: QuizQuestion): void {
    const copy: QuizQuestion = JSON.parse(JSON.stringify(q));
    copy.id = this.genId();
    copy.collapsed = false;
    const idx = this.questions.findIndex((x) => x.id === q.id);
    this.questions.splice(idx + 1, 0, copy);
    this.updateDirty();
  }

  deleteQuestion(id: number): void {
    const idx = this.questions.findIndex((x) => x.id === id);
    if (idx > -1) {
      this.questions.splice(idx, 1);
      this.invalidQuestionIds.delete(id);
      // re-run validation panel if open
      if (this.showValidationPanel) {
        this.validationErrors = this.validateQuiz();
        if (this.validationErrors.length === 0)
          this.showValidationPanel = false;
      }
      this.updateDirty();
    }
  }

  updateDirty(): void {
    this.isDirty = this.getSnapshot() !== this.initialSnapshot;
  }

  questionIndex(q: QuizQuestion): number {
    return this.questions.findIndex((x) => x.id === q.id);
  }

  previewText(q: QuizQuestion): string {
    if (!q.text) return '';
    return q.text.length > 72 ? q.text.slice(0, 72) + '…' : q.text;
  }

  clearAll(): void {
    this.modalService
      .open(this.askForResetTpl, {
        backdrop: true,
        keyboard: true,
        scrollable: false,
        windowClass: 'quiz-preview-modal-window',
        modalDialogClass: 'modal-sm',
      })
      .result.then((action) => {
        if (action === 'confirm') {
          if (this.editingQuizId) {
            if (this.editingQuiz) {
              this.quizTitle = this.editingQuiz.surveyName || '';
              this.quizDesc = this.editingQuiz.description || '';
              this.cutoffQuestions = this.editingQuiz.cutOffQuestions || null;
              this.isActive = String(this.editingQuiz.isActive).toLowerCase() === 'true';
            }
            this.fetchQuiz(this.editingQuizId);
          } else {
            this.quizTitle = '';
            this.quizDesc = '';
            this.cutoffQuestions = null;
            this.isActive = false;
            this.questions = [];
            this.initialSnapshot = this.getSnapshot();
            this.isDirty = false;
          }
          this.invalidQuestionIds.clear();
          this.validationErrors = [];
          this.showValidationPanel = false;
        }
      })
      .catch(() => {
        // User cancelled
      });
  }

  onBack(): void {
    const currentSnapshot = this.getSnapshot();
    if (currentSnapshot !== this.initialSnapshot) {
      this.modalService
        .open(this.askForBackTpl, {
          backdrop: true,
          keyboard: true,
          scrollable: false,
          windowClass: 'quiz-preview-modal-window',
          modalDialogClass: 'modal-sm',
        })
        .result.then(() => {
          this.backToList.emit();
        })
        .catch(() => {
          // User cancelled
        });
    } else {
      this.backToList.emit();
    }
  }

  saveQuiz(): void {
    this.validationErrors = this.validateQuiz();
    if (this.validationErrors.length > 0) {
      this.showValidationPanel = true;
      // Expand all invalid questions
      this.questions.forEach((q) => {
        if (this.invalidQuestionIds.has(q.id)) q.collapsed = false;
      });
      // Scroll to first error
      const firstInvalidId = this.questions.find((q) =>
        this.invalidQuestionIds.has(q.id),
      )?.id;
      if (firstInvalidId) {
        setTimeout(() => {
          const el = document.getElementById('q-' + firstInvalidId);
          if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 80);
      }
      this.alertMessage = `Please fix ${this.validationErrors.length} error${this.validationErrors.length > 1 ? 's' : ''} before saving.`;
      this.modalService.open(this.alertTemplate, { size: 'sm' });
      return;
    }
    this.showValidationPanel = false;

    this.quizObj.empId = this.currUser.empId;
    this.quizObj.surveyName = this.quizTitle;
    this.quizObj.description = this.quizDesc;
    this.quizObj.isActive = this.isActive;
    this.quizObj.cutOffQuestions = this.cutoffQuestions;
    this.quizObj.surveyId = (this.editingQuizId as any) || null;
    this.quizObj.createdBy = this.currUser.empId;
    this.quizObj.updatedBy = this.currUser.empId;

    // Set training context if applicable
    if (this.trainingId) {
      this.quizObj.type = 'quiz';
      this.quizObj.trainingId = this.trainingId;
      this.quizObj.isMandatory = true; // Default to mandatory
      this.quizObj.mustPassToComplete = false; // Default
    }

    // Convert questions to SurveyQuestion format
    this.quizObj.surveyQuestionList = this.questions.map((q, index) => {
      const surveyQuestion = new SurveyQuestion();
      if (q.dbId) surveyQuestion.surveyQuestionId = q.dbId;
      surveyQuestion.surveyId = (this.editingQuizId as any) || null;

      // Basic question info
      surveyQuestion.question = q.text;
      surveyQuestion.optionType = q.inputType; // 'radio' or 'checkbox'
      surveyQuestion.required = true; // Questions are required by default in quiz
      surveyQuestion.description = this.quizDesc; // Optional description field

      // Convert options to SurveyOption array
      const optionsList: SurveyOption[] = [];
      q.options.forEach((opt) => {
        const option = new SurveyOption();
        option.optionValue = opt.text;
        optionsList.push(option);
      });
      surveyQuestion.optionsList = optionsList;

      // Stringify options for API
      surveyQuestion.options = JSON.stringify(optionsList);

      // Set correct answer(s) based on question type
      if (q.inputType === 'radio') {
        // For radio, find the one correct option text
        const correctOption = q.options.find((opt) => opt.correct);
        surveyQuestion.correctAnswer = correctOption ? correctOption.text : '';
      } else {
        // For checkbox, store correct answers as comma-separated string
        const correctOptions = q.options
          .filter((opt) => opt.correct)
          .map((opt) => opt.text)
          .join(', ');
        surveyQuestion.correctAnswer = correctOptions;
      }

      return surveyQuestion;
    });

    console.log('quizObj', this.quizObj);

    // Now save the quiz
    const request = this.editingQuizId 
      ? this.surveyService.updateSurvey(this.quizObj)
      : this.surveyService.createSurvey(this.quizObj);

    request.subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.alertMessage = this.editingQuizId ? 'Quiz updated successfully!' : 'Quiz created successfully!';
          this.modalService
            .open(this.alertTemplate, { size: 'sm' })
            .result.then(() => {
              this.backToList.emit();
            })
            .catch(() => {
              this.backToList.emit();
            });
        } else {
          this.alertMessage = 'Error saving quiz: ' + response.serviceResponse;
          this.modalService.open(this.alertTemplate, {
            size: 'sm',
          });
        }
      },
      error: (error: any) => {
        this.alertMessage = 'Error saving quiz. Please try again.';
        this.modalService.open(this.alertTemplate, { size: 'sm' });
      },
    });
  }

  dismissValidation(): void {
    this.showValidationPanel = false;
  }

  openPreviewModal(): void {
    const modalRef = this.modalService.open(QuizViewModalComponent, {
      size: 'xl',
      windowClass: 'quiz-preview-modal-window',
      backdrop: 'static',
    });

    modalRef.componentInstance.config = {
      mode: 'preview',
      questions: this.questions.map((q) => ({
        question: q.text,
        optionType: q.inputType,
        optionsList: q.options.map((opt) => ({ optionValue: opt.text })),
        correctAnswer:
          q.inputType === 'radio'
            ? q.options.find((opt) => opt.correct)?.text
            : q.options
                .filter((opt) => opt.correct)
                .map((opt) => opt.text)
                .join(', '),
      })),
      quizTitle: this.quizTitle,
      cutoffQuestions: this.cutoffQuestions || 0,
      totalQuestions: this.questions.length,
    };
  }

  // kept for any legacy calls
  showPreview(): void {
    this.openPreviewModal();
  }
  closePreview(): void {
    if (this.previewModalRef) this.previewModalRef.dismiss();
  }

  onOverlayClick(event: MouseEvent): void {
    if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
      this.closePreview();
    }
  }

  trackById(_: number, q: QuizQuestion): number {
    return q.id;
  }
  trackByIndex(i: number): number {
    return i;
  }
  trackByField(_: number, e: ValidationError): string {
    return e.field;
  }
}
