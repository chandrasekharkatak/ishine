import {
  Component,
  OnDestroy,
  OnInit,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Router, ActivatedRoute } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import * as JSZip from 'jszip';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { SurveyService } from '../services/survey.service';
import { TrainingService } from '../services/training.service';
import * as pdfjsLib from 'pdfjs-dist';
import { GlobalWorkerOptions } from 'pdfjs-dist';
import { TrainingContentViewComponent } from '../training-content-view/training-content-view.component';

GlobalWorkerOptions.workerSrc = `//cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjsLib.version}/pdf.worker.min.js`;

@Component({
  standalone: false,
  selector: 'app-training',
  templateUrl: './training.component.html',
  styleUrls: ['./training.component.css'],
})
export class TrainingComponent implements OnInit, OnDestroy {
  currentUser: User;
  pendingTraining: any = null;
  lockStatus: any = null;
  isLocked: boolean = false;
  loading: boolean = false;

  // All trainings list
  allTrainings: any[] = [];
  mustAttendTrainings: any[] = []; // Trainings that must be attended (frozen)
  showAllTrainings: boolean = false;

  // Pagination and filters for All Trainings
  allTrainingsPage: number = 1;
  allTrainingsPageSize: number = 6;
  allTrainingsFilter: string = '';
  allTrainingsFiltered: any[] = [];

  // Training viewing (Legacy/Consolidated)
  viewingTraining: any = null;
  isViewingTraining: boolean = false;
  elapsedTime: number = 0; // in seconds
  elapsedTimeDisplay: string = '00:00';
  timerInterval: any;
  minTimeReached: boolean = false;
  consentButtonEnabled: boolean = false;
  hasVisitedLink: boolean = false;
  quizButtonEnabled: boolean = false;
  showQuizSubmitComponent: boolean = false;

  // Content viewing
  contentUrl: string = '';
  previewUrl: string = '';

  // Header display
  todayDate: string = '';
  isMustAttendBannerClosed: boolean = false;
  safePreviewUrl: SafeResourceUrl | null = null;
  contentType: string = '';
  isExternalLink: boolean = false;
  file: File | null = null;
  fileSize: number = 0;


  // Auto-opening deadline-crossed trainings
  deadlineCrossedTrainings: any[] = [];
  currentDeadlineCrossedIndex: number = -1;
  isAutoOpening: boolean = false;

  // Accordion states
  mustAttendExpanded: boolean = true; // Expanded by default
  allTrainingsExpanded: boolean = true; // Expanded by default

  selectedFilterStatus: string = 'ALL';
  filteredTrainingsByStatus: any[] = [];

  contentFormData: any = {
    contentType: '',
  };
  consentSub: any;
  quizSub: any;
  quizCompletedSub: any;
  linkSub: any;
  closeSub: any;

  // Modals
  modalRef: NgbModalRef;
  @ViewChild('alert_message') alertTemplate: TemplateRef<any>;
  @ViewChild('training_view_modal') trainingViewModalTemplate: TemplateRef<any>;
  @ViewChild(TrainingContentViewComponent) contentPreviewModal: TrainingContentViewComponent;

  alertMessage: string = '';
  alertType: string = 'info';

  constructor(
    private authenticationService: AuthenticationService,
    private trainingService: TrainingService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private modalService: NgbModal,
    private sanitizer: DomSanitizer,
    private surveyService: SurveyService,
  ) {
    this.authenticationService.currentUser.subscribe((x) => {
      this.currentUser = x;
      if (x && x.empId) {
        // this.checkLockStatus();
      }
    });
  }


  // Tracks whether the URL ?status= param explicitly set the tab
  private hasStatusParam: boolean = false;

  ngOnInit(): void {
    // Generate formatted date (e.g. "Mar 17, 2026")
    const options: Intl.DateTimeFormatOptions = { month: 'short', day: 'numeric', year: 'numeric' };
    this.todayDate = new Date().toLocaleDateString('en-US', options);

    // Read ?status= query param and pre-select the matching tab
    const statusParam = this.activatedRoute.snapshot.queryParamMap.get('status');
    if (statusParam) {
      const upperStatus = statusParam.toUpperCase();
      const validStatuses = ['ALL', 'PENDING', 'COMPLETED', 'MANDATORY', 'SKIPPED'];
      if (validStatuses.includes(upperStatus)) {
        this.selectedFilterStatus = upperStatus;
        this.hasStatusParam = true;
      }
    }
    this.checkLockStatus();
  }

  /** Called after trainings load. If no query param drove the tab selection,
   *  auto-pick: MANDATORY if user is locked → PENDING if any pending → ALL */
  private autoSelectDefaultTab(): void {
    if (this.hasStatusParam) return; // respect the URL param

    const isLocked = this.currentUser?.trainingLockStatus?.isLocked === true
                  || this.currentUser?.trainingLockStatus?.isHardLock === true;

    if (isLocked) {
      this.selectedFilterStatus = 'MANDATORY';
    } else {
      const hasPending = this.allTrainings.some(t => t.status === 'PENDING');
      this.selectedFilterStatus = hasPending ? 'PENDING' : 'ALL';
    }
    this.applyFilterByStatus();
  }



  // ngAfterViewInit(): void {
  //   setTimeout(() => {
  //     this.checkLockStatus();
  //   }, 500);
  // }

  checkLockStatus() {
    if (!this.currentUser || !this.currentUser.empId) {
      return;
    }

    // Check lock status from user object first
    // if (this.currentUser.trainingLockStatus) {
    //   this.lockStatus = this.currentUser.trainingLockStatus;
    //   this.isLocked = this.lockStatus.isLocked === true;

      // If locked, load all trainings to show the list
      // this.loadUserTrainings(); // Removed redundant call - already handled in subscribe
      // return;
    // }

    // If not in user object, fetch from API
    this.trainingService.getLockStatus(this.currentUser.empId).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success' && response.serviceResponse) {
          this.lockStatus = response.serviceResponse;
          this.isLocked = this.lockStatus.isLocked === true;

          // Update user object
          this.currentUser.trainingLockStatus = this.lockStatus;
          this.authenticationService.setcurrentUserSubject(this.currentUser);

          // Load trainings (will handle lock status in loadUserTrainings)
          this.loadUserTrainings();
        }
      },
      error: (error: any) => {
        console.error('Error checking lock status:', error);
        // Still load trainings even if lock check fails
        this.loadUserTrainings();
      },
    });
  }

  loadUserTrainings() {
    if (!this.currentUser || !this.currentUser.empId) {
      return;
    }

    this.loading = true;
    this.trainingService.getUserTrainings(this.currentUser.empId).subscribe({
      next: (response: any) => {
        this.loading = false;
        if (response.serviceStatus === 'Success' && response.serviceResponse) {
          // this.allTrainings = response.serviceResponse || [];
          this.allTrainings = this.sortTrainingsByStatusAndDeadline(
            response.serviceResponse || [],
          );
          this.showAllTrainings = true;

          // Filter "Must Attend" trainings based on new freeze logic:
          // a) Training mandatory AND lock enabled AND not attended (irrespective of deadline)
          // b) Training mandatory AND deadline crossed AND not attended (irrespective of lock enabled)
          this.mustAttendTrainings = this.allTrainings.filter(
            (t) =>
              t.mandatoryFlag === 'true' &&
              (t.status === 'PENDING' || t.status === 'SKIPPED') &&
              (t.lockEnabled === true || // Case a: lock enabled
                t.isDeadlineCrossed === true), // Case b: deadline crossed
          );

          // Apply text search filter
          this.applyAllTrainingsFilter();
          // Auto-select the right tab (respects URL ?status= or smart defaults)
          this.autoSelectDefaultTab();



          // Separate deadline-crossed trainings for auto-opening (only mandatory with lock enabled OR deadline crossed)
          this.deadlineCrossedTrainings = this.mustAttendTrainings.filter(
            (t) => t.isDeadlineCrossed === true,
          );

          // Auto-open first deadline-crossed training if any
          if (this.deadlineCrossedTrainings.length > 0) {
            this.currentDeadlineCrossedIndex = 0;
            this.isAutoOpening = true;
            this.viewTraining(this.deadlineCrossedTrainings[0], true);
          }

          // Check for single pending training (non-deadline-crossed, but must attend)
          const singlePending = this.mustAttendTrainings.find(
            (t) => !t.isDeadlineCrossed,
          );

          if (singlePending && this.deadlineCrossedTrainings.length === 0) {
            this.pendingTraining = singlePending;
            // Auto-open mandatory training for locked users
            if (this.isLocked) {
              this.viewTraining(singlePending);
            }
          }
        } else {
          this.allTrainings = [];
          this.mustAttendTrainings = [];
          this.allTrainingsFiltered = [];
        }
      },
      error: (error: any) => {
        this.loading = false;
        console.error('Error loading user trainings:', error);
        this.openAlert(
          'Error loading trainings: ' + error.error.serviceStatus,
          'error',
        );
      },
    });
  }

  // Reusable sort function
  sortTrainingsByStatusAndDeadline(
    trainings: any[],
    statusOrder: string[] = ['PENDING', 'SKIPPED', 'COMPLETED'],
  ): any[] {
    if (!trainings || !Array.isArray(trainings)) {
      return [];
    }

    return [...trainings].sort((a, b) => {
      // Get priority index (lower index = higher priority)
      const getPriority = (status: string): number => {
        const index = statusOrder.indexOf(status);
        return index !== -1 ? index : statusOrder.length; // Unknown statuses go to the end
      };

      const priorityA = getPriority(a.status);
      const priorityB = getPriority(b.status);

      // Sort by status priority first
      if (priorityA !== priorityB) {
        return priorityA - priorityB;
      }

      // If same status, sort by deadline (earlier deadline first)
      const dateA = a.deadline
        ? new Date(a.deadline).getTime()
        : Number.MAX_SAFE_INTEGER;
      const dateB = b.deadline
        ? new Date(b.deadline).getTime()
        : Number.MAX_SAFE_INTEGER;

      return dateA - dateB;
    });
  }

  applyAllTrainingsFilter() {
    this.applyFilterByStatus();
  }

  getPaginatedAllTrainings(): any[] {
    const start = (this.allTrainingsPage - 1) * this.allTrainingsPageSize;
    const end = start + this.allTrainingsPageSize;
    return this.allTrainingsFiltered.slice(start, end);
  }

  getTotalAllTrainingsPages(): number {
    return Math.ceil(
      this.allTrainingsFiltered.length / this.allTrainingsPageSize,
    );
  }
  /** Banner Skip Logic (Simplified) */
  onSkipTraining(training: any) {
    if (!training) return;

    // Check if skip is allowed
    if (training.skipAllowed !== 'true' || training.isDeadlineCrossed) {
      this.openAlert('Skip is not allowed for this training', 'warning');
      return;
    }

    this.trainingService.skipTraining({
      trainingId: training.trainingId,
      empId: this.currentUser.empId,
      cycleNumber: training.currentCycleNumber,
      quizId: training.quizId,
    }).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          this.openAlert('Training skipped temporarily. You can complete it later.', 'success');
          this.pendingTraining = null;
          this.resetQuizView();
          setTimeout(() => {
            this.checkLockStatus();
            this.loadUserTrainings();
          }, 1000);
        } else {
          this.openAlert(response.serviceResponse || 'Failed to skip training', 'error');
        }
      },
      error: (error: any) => {
        this.openAlert(error.serviceStatus || 'Failed to skip training', 'error');
      },
    });
  }

  // Delegated events from TrainingContentViewComponent (Modal)
  onSubmitConsentForModal() {
    // This is now purely for refreshing list after modal handles submission
    this.closeTrainingView();
    setTimeout(() => {
      this.checkLockStatus();
      this.loadUserTrainings();
    }, 1500);
  }

  onSkipTrainingForModal() {
    this.closeTrainingView();
    setTimeout(() => {
      this.router.navigate(['/home']);
    }, 500);
  }

  onQuizCompleted(passed: boolean) {
    this.resetQuizView();
    this.showQuizSubmitComponent = false;
    this.closeTrainingView();
    
    setTimeout(() => {
      this.checkLockStatus();
      // this.loadUserTrainings(); // Removed redundant call - checkLockStatus already calls loadUserTrainings

      // Move to next deadline-crossed training if auto-opening
      if (
        this.isAutoOpening &&
        this.deadlineCrossedTrainings.length > 0 &&
        this.currentDeadlineCrossedIndex <
          this.deadlineCrossedTrainings.length - 1
      ) {
        this.currentDeadlineCrossedIndex++;
        setTimeout(() => {
          this.viewTraining(
            this.deadlineCrossedTrainings[this.currentDeadlineCrossedIndex],
            true,
          );
        }, 500);
      } else {
        this.isAutoOpening = false;
      }
    }, 1500);
  }

  goToQuiz() {
    this.showQuizSubmitComponent = true;
  }


  openTrainingViewModal() {
    // Set all the input properties
    this.contentPreviewModal.content = this.viewingTraining.content;
    this.contentPreviewModal.contentType = this.contentType;
    this.contentPreviewModal.contentName = this.viewingTraining.content.contentName;
    this.contentPreviewModal.previewUrl = this.previewUrl;
    this.contentPreviewModal.file = this.file;
    this.contentPreviewModal.isAdminMode = false;
    this.contentPreviewModal.showTimer = true;
    this.contentPreviewModal.minViewTimeMinutes = this.viewingTraining.minViewTimeMinutes || 0;
    this.contentPreviewModal.status = this.viewingTraining.status;
    this.contentPreviewModal.hasSeenContent = this.viewingTraining.hasSeenContent || false;
    this.contentPreviewModal.hasQuiz = this.viewingTraining.hasQuiz || false;
    this.contentPreviewModal.quizAttempted = this.viewingTraining.quizAttempted || false;
    this.contentPreviewModal.consentRequired = this.viewingTraining.consentRequired || 'false';
    this.contentPreviewModal.mandatoryFlag = this.viewingTraining.mandatoryFlag || 'false';
    this.contentPreviewModal.trainingId = this.viewingTraining.trainingId;
    this.contentPreviewModal.cycleNumber = this.viewingTraining.currentCycleNumber;
    this.contentPreviewModal.contentId = this.viewingTraining.content.contentId;
    this.contentPreviewModal.quizId = this.viewingTraining.quizId;
    this.contentPreviewModal.isAlreadySubmitted = this.viewingTraining.isAlreadySubmitted || false;

    // Subscribe to events (unsubscribe in ngOnDestroy to prevent memory leaks)
    this.consentSub = this.contentPreviewModal.consentSubmitted.subscribe(() => this.onSubmitConsentForModal());
    this.quizSub = this.contentPreviewModal.quizClicked.subscribe(() => this.goToQuiz());
    this.quizCompletedSub = this.contentPreviewModal.quizCompleted.subscribe((passed: boolean) => this.onQuizCompleted(passed));
    this.linkSub = this.contentPreviewModal.linkVisited.subscribe(() => this.hasVisitedLink = true);
    this.closeSub = this.contentPreviewModal.closed.subscribe(() => this.closeTrainingView());

    // Open the modal
    this.contentPreviewModal.open();

    // No need to call parsePDFFile here - the shared component handles it
  }

  // Update your viewTraining method to prepare the data
  viewTraining(training: any, isDeadlineCrossed: boolean = false) {
    if (!training || !training.content) {
      this.openAlert('Training content not available', 'warning');
      return;
    }
    
    this.viewingTraining = training;
    
    // Clear previous content
    this.previewUrl = '';
    this.safePreviewUrl = null;
    this.file = null;
    this.fileSize = 0;

    const content = training.content;
    this.contentType = content.contentType;

    // Handle content loading based on type
    if (content.contentType === 'LINK') {
      this.previewUrl = content.externalLinkUrl;
      this.openTrainingViewModal();
    } else {
      // Download content for file types
      this.trainingService.downloadContent(content.contentId).subscribe({
        next: (resp: any) => {
          const blob: Blob = resp.body;
          const contentType = resp.headers.get('Content-Type') || 'application/octet-stream';
          
          let fileName = content.contentName || 'content';
          const disposition = resp.headers.get('Content-Disposition');
          if (disposition) {
            const match = disposition.match(/filename="(.+)"/);
            if (match && match[1]) {
              fileName = match[1];
            }
          }

          this.previewUrl = URL.createObjectURL(blob);
          this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl);
          this.file = new File([blob], fileName, { type: contentType });
          this.fileSize = blob.size / 1024 / 1024;

          this.openTrainingViewModal();
        },
        error: (error) => {
          this.openAlert('Error loading content', 'error');
        }
      });
    }
  }

  closeTrainingView() {
    // Revoke blob URLs
    if (this.previewUrl && this.previewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(this.previewUrl);
    }

    // Reset variables
    this.viewingTraining = null;
    this.isViewingTraining = false;
    this.previewUrl = '';
    this.safePreviewUrl = null;
    this.file = null;
    this.fileSize = 0;
  }

  openAlert(message: string, type: string = 'info') {
    this.alertMessage = message;
    this.alertType = type;
    this.modalRef = this.modalService.open(this.alertTemplate, {
      backdrop: false,
      windowClass: 'alert-toast-modal',
      modalDialogClass: 'alert-toast-dialog',
      size: 'sm',
    });
  }

  getProgressPercentageForModal(): number {
    if (!this.viewingTraining || !this.viewingTraining.minViewTimeMinutes) {
      return 0;
    }
    const minTimeSeconds = this.viewingTraining.minViewTimeMinutes * 60;
    return Math.min((this.elapsedTime / minTimeSeconds) * 100, 100);
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'COMPLETED':
        return 'badge-success';
      case 'PENDING':
        return 'badge-warning';
      case 'SKIPPED':
        return 'badge-info';
      default:
        return 'badge-secondary';
    }
  }

  canViewTraining(training: any): boolean {
    return training && training.content != null;
  }

  canSkipTraining(training: any): boolean {
    return (
      training.skipAllowed === 'true' &&
      !training.isDeadlineCrossed &&
      training.status === 'PENDING'
    );
  }

  downloadFile() {
    if (this.previewUrl) {
      window.open(this.previewUrl, '_blank');
    }
  }

  goToHome() {
    this.router.navigate(['/home']);
  }
  closeContentModal() {
    this.modalRef.close();
    this.modalRef = null;
    this.resetQuizView();
  }

  // ... existing goToQuiz removed as redundant with the one at line 390

  // ... existing onQuizCompleted removed as redundant with the one at line 382

  resetQuizView() {
    this.viewingTraining = null;
    this.isViewingTraining = false;
    this.previewUrl = '';
    this.safePreviewUrl = null;
    this.file = null;
    this.fileSize = 0;
  }



  // Add these methods
  setFilter(status: string) {
    this.selectedFilterStatus = status;
    this.applyFilterByStatus();
  }

  applyFilterByStatus() {
    if (this.selectedFilterStatus === 'ALL') {
      this.allTrainingsFiltered = [...this.allTrainings];
    } else if (this.selectedFilterStatus === 'MANDATORY') {
      this.allTrainingsFiltered = [...this.mustAttendTrainings];
    } else {
      this.allTrainingsFiltered = this.allTrainings.filter(
        (t) => t.status === this.selectedFilterStatus,
      );
    }

    // Apply text search filter on top of status filter
    if (this.allTrainingsFilter && this.allTrainingsFilter.trim() !== '') {
      const filterLower = this.allTrainingsFilter.toLowerCase().trim();
      this.allTrainingsFiltered = this.allTrainingsFiltered.filter(
        (t) =>
          (t.trainingName &&
            t.trainingName.toLowerCase().includes(filterLower)) 
            // || (t.trainingType &&
            // t.trainingType.toLowerCase().includes(filterLower)),
      );
    }

    // Reset to first page
    this.allTrainingsPage = 1;
  }

  getCountByStatus(status: string): number {
    return this.allTrainings.filter((t) => t.status === status).length;
  }

  applyAllTrainingsFilterStatus() {
    this.allTrainingsFilter = '';
    let filtered = this.allTrainings;

    if (this.selectedFilterStatus !== 'ALL') {
      filtered = filtered.filter((t) => t.status === this.selectedFilterStatus);
    }

    if (this.allTrainingsFilter && this.allTrainingsFilter.trim() !== '') {
      const filterLower = this.allTrainingsFilter.toLowerCase().trim();
      filtered = filtered.filter(
        (t) =>
          (t.trainingName &&
            t.trainingName.toLowerCase().includes(filterLower)) ||
          (t.trainingType &&
            t.trainingType.toLowerCase().includes(filterLower)),
      );
    }

    this.allTrainingsFiltered = filtered;
    this.allTrainingsPage = 1;
  }

  canSubmitConsent(): boolean {
    if (this.viewingTraining.status.toLowerCase() == 'completed') {
      return false;
    }

    if (!this.viewingTraining.hasSeenContent) {
      if (!this.viewingTraining.hasQuiz) {
        return true;
      } else if (
        this.viewingTraining.hasQuiz &&
        this.viewingTraining.quizAttempted
      ) {
        return true;
      }
    }

    return false;
  }

  canShowGoToQuiz(): boolean {
    if (this.viewingTraining.status.toLowerCase() === 'completed') {
      return false;
    }

    if (this.viewingTraining.hasQuiz && !this.viewingTraining.quizAttempted) {
      return true;
    }

    return false;
  }

  onPreviewError(event: any) {
    console.log('Preview error:', event);
    // Show fallback image or message
    event.target.src = 'assets/images/no-preview.png';
    event.target.alt = 'Preview not available';
  }

  getTextForNote(){
    if(!this.viewingTraining.hasSeenContent){
      if(this.viewingTraining.hasQuiz && !this.viewingTraining.quizAttempted){
        return 'If you have already completed the training then either a new content has been added or a new quiz has been added';
      } else {
        return 'A new Content has been added';
      }
    } else {
      if(this.viewingTraining.hasQuiz && !this.viewingTraining.quizAttempted){
        return 'A new Quiz has been added';
      }
    }

    return null;
    
  }

  ngOnDestroy() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
    
    // Unsubscribe from events
    if (this.consentSub) this.consentSub.unsubscribe();
    if (this.quizSub) this.quizSub.unsubscribe();
    if (this.quizCompletedSub) this.quizCompletedSub.unsubscribe();
    if (this.linkSub) this.linkSub.unsubscribe();
    if (this.closeSub) this.closeSub.unsubscribe();
    
    if (this.contentPreviewModal) {
      this.contentPreviewModal.close();
    }
    this.closeTrainingView();

    if (this.modalRef) {
      this.modalRef.close();
      this.modalRef = null;
    }
  }
}
