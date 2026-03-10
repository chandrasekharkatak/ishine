import {
  Component,
  OnDestroy,
  OnInit,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import * as JSZip from 'jszip';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { SurveyService } from '../services/survey.service';
import { TrainingService } from '../services/training.service';
import * as pdfjsLib from 'pdfjs-dist';
import { GlobalWorkerOptions } from 'pdfjs-dist';

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

  // Currently viewing training (for modal)
  viewingTraining: any = null;
  isViewingTraining: boolean = false;

  // Training viewing
  elapsedTime: number = 0; // in seconds
  elapsedTimeDisplay: string = '00:00';
  timerInterval: any;
  minTimeReached: boolean = false;
  consentButtonEnabled: boolean = false;
  hasVisitedLink: boolean = false;

  // Content viewing
  contentUrl: string = '';
  previewUrl: string = '';
  safePreviewUrl: SafeResourceUrl | null = null;
  contentType: string = '';
  isExternalLink: boolean = false;
  file: File | null = null;
  fileSize: number = 0;
  pptxSlides: any[] = [];
  currentSlideIndex: number = 0;
  showThumbnails: boolean = false;

  // Auto-opening deadline-crossed trainings
  deadlineCrossedTrainings: any[] = [];
  currentDeadlineCrossedIndex: number = -1;
  isAutoOpening: boolean = false;

  // Accordion states
  mustAttendExpanded: boolean = true; // Expanded by default
  allTrainingsExpanded: boolean = true; // Expanded by default
  quizButtonEnabled: boolean = false;
  showQuizSubmitComponent: boolean = false;
  isLoadingPPTX: boolean = false;
  isFullscreen: boolean = false;
  selectedFilterStatus: string = 'ALL';
  filteredTrainingsByStatus: any[] = [];

  contentFormData: any = {
    contentType: '',
  };
  slides: string[] = [];
  totalSlides: number = 0;
  preloadedSlides: Map<number, string> = new Map();
  isLoadingPreview: boolean = false;
  previewImageBlob: string | null = null;
  currentPDFFile: File | null = null;

preloadQueue: number[] = [];
pdfFile: File | null = null;

  // Modals
  modalRef: NgbModalRef;
  @ViewChild('alert_message') alertTemplate: TemplateRef<any>;
  @ViewChild('training_view_modal') trainingViewModalTemplate: TemplateRef<any>;

  alertMessage: string = '';
  alertType: string = 'info';

  constructor(
    private authenticationService: AuthenticationService,
    private trainingService: TrainingService,
    private router: Router,
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

  ngOnInit(): void {
    this.checkLockStatus();
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
      this.loadUserTrainings();
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

          // Apply filter to all trainings
          this.applyAllTrainingsFilter();

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
            this.setupTrainingContent();
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
    if (!this.allTrainingsFilter || this.allTrainingsFilter.trim() === '') {
      this.allTrainingsFiltered = [...this.allTrainings];
    } else {
      const filterLower = this.allTrainingsFilter.toLowerCase().trim();
      this.allTrainingsFiltered = this.allTrainings.filter(
        (t) =>
          (t.trainingName &&
            t.trainingName.toLowerCase().includes(filterLower)) ||
          (t.trainingType &&
            t.trainingType.toLowerCase().includes(filterLower)),
        // (t.status && t.status.toLowerCase().includes(filterLower))
      );
    }
    // Reset to first page when filter changes
    this.allTrainingsPage = 1;
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
  setupTrainingContent() {
    if (!this.pendingTraining || !this.pendingTraining.content) {
      return;
    }

    const content = this.pendingTraining.content;
    this.contentType = content.contentType;

    if (content.contentType === 'LINK') {
      this.isExternalLink = true;
      this.contentUrl = content.externalLinkUrl;
      this.hasVisitedLink = false;
    } else {
      this.isExternalLink = false;
      // For file content, construct download URL
      this.contentUrl = `${this.trainingService['baseUrl']}api/training/downloadContent/${content.contentId}`;
    }

    // Reset timer
    this.elapsedTime = 0;
    this.elapsedTimeDisplay = '00:00';
    this.minTimeReached = false;
    this.consentButtonEnabled = false;

    // Start timer if min time is configured
    if (
      this.pendingTraining.minViewTimeMinutes &&
      this.pendingTraining.minViewTimeMinutes > 0
    ) {
      this.startTimer();
    } else {
      // No time requirement, enable consent immediately
      this.minTimeReached = true;
      if (this.pendingTraining.consentRequired === 'true') {
        this.consentButtonEnabled = true;
      }
    }
  }

  startTimer() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }

    const minTimeSeconds = this.pendingTraining.minViewTimeMinutes * 60;

    this.timerInterval = setInterval(() => {
      this.elapsedTime++;

      const minutes = Math.floor(this.elapsedTime / 60);
      const seconds = this.elapsedTime % 60;
      this.elapsedTimeDisplay = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

      if (this.elapsedTime >= minTimeSeconds) {
        this.minTimeReached = true;
        if (this.pendingTraining.consentRequired === 'true') {
          // this.consentButtonEnabled = true;
          if (this.viewingTraining.hasQuiz) {
            this.quizButtonEnabled = true;
            this.consentButtonEnabled = true;
          } else {
            this.consentButtonEnabled = true;
          }
        }
        clearInterval(this.timerInterval);
      }
    }, 1000);
  }

  onExternalLinkClick() {
    if (this.isExternalLink && this.contentUrl) {
      window.open(this.contentUrl, '_blank');
      this.hasVisitedLink = true;

      // For external links, enable consent after clicking
      // this.consentButtonEnabled = true;
      if (this.viewingTraining.hasQuiz) {
        this.quizButtonEnabled = true;
        this.consentButtonEnabled = true;
      } else if (this.pendingTraining.consentRequired === 'true') {
        this.consentButtonEnabled = true;
      }
    }
  }

  onContentDownload() {
    if (!this.isExternalLink && this.contentUrl) {
      window.open(this.contentUrl, '_blank');
    }
  }

  onSubmitConsent() {
    if (!this.pendingTraining || !this.pendingTraining.content) {
      this.openAlert(
        'Training information is missing. Please refresh the page.',
        'error',
      );
      return;
    }

    // Validate minimum time if required
    if (
      this.pendingTraining.minViewTimeMinutes &&
      this.pendingTraining.minViewTimeMinutes > 0
    ) {
      if (!this.minTimeReached) {
        this.openAlert(
          `Please view the training for at least ${this.pendingTraining.minViewTimeMinutes} minutes before submitting consent.`,
          'warning',
        );
        return;
      }
    }

    // Validate external link visit
    if (this.isExternalLink && !this.hasVisitedLink) {
      this.openAlert(
        'Please click on the external link to visit the training content before submitting consent.',
        'warning',
      );
      return;
    }

    const consentData = {
      trainingId: this.pendingTraining.trainingId,
      contentId: this.pendingTraining.content.contentId,
      empId: this.currentUser.empId,
      completionCycleNumber: this.pendingTraining.currentCycleNumber,
    };

    this.trainingService.submitConsent(consentData).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          // Stop timer
          if (this.timerInterval) {
            clearInterval(this.timerInterval);
          }

          // Show success message
          this.openAlert(
            'Training completed successfully! Your consent has been submitted.',
            'success',
          );

          // Clear pending training
          this.pendingTraining = null;
          this.resetQuizView();

          // Reload trainings to remove from mandatory list and update lock status
          setTimeout(() => {
            this.checkLockStatus();
            this.loadUserTrainings();
          }, 1500);
        }
        // this.openAlert(response.serviceResponse || 'Failed to submit consent', 'error');
      },
      error: (error: any) => {
        // this.openAlert('Error submitting consent: ' + (error.error?.message || error.message), 'error');
        this.openAlert(
          error.serviceStatus || 'Failed to submit consent',
          'error',
        );
      },
    });
  }

  onSkipTraining() {
    if (!this.pendingTraining) {
      return;
    }

    // Check if skip is allowed
    if (this.pendingTraining.skipAllowed !== 'true') {
      this.openAlertMod(
        this.alertTemplate,
        'Skip is not allowed for this training.',
      );
      return;
    }

    // Check if deadline has passed
    if (this.pendingTraining.isDeadlineCrossed) {
      this.openAlertMod(
        this.alertTemplate,
        'Deadline has passed. Skip is not allowed.',
      );
      return;
    }

    const skipData = {
      trainingId: this.pendingTraining.trainingId,
      empId: this.currentUser.empId,
      cycleNumber: this.pendingTraining.currentCycleNumber,
      quizId: this.pendingTraining.quizId,
    };

    this.trainingService.skipTraining(skipData).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          // Stop timer
          if (this.timerInterval) {
            clearInterval(this.timerInterval);
          }

          // Route to home page after successful skip
          setTimeout(() => {
            this.router.navigate(['/home']);
          }, 500);
        } else {
          this.openAlertMod(
            this.alertTemplate,
            response.serviceResponse || 'Failed to skip training',
          );
        }
      },
      error: (error: any) => {
        // this.openAlertMod(this.alertTemplate, 'Error skipping training: ' + error.message);
        this.openAlertMod(
          this.alertTemplate,
          error.serviceStatus || 'Failed to skip training',
        );
      },
    });
  }

  openAlertMod(template: TemplateRef<any>, message: string) {
    this.alertMessage = message;
    this.modalRef = this.modalService.open(template, {
      centered: true,
      size: 'sm',
    });
  }

  getProgressPercentage(): number {
    if (!this.pendingTraining || !this.pendingTraining.minViewTimeMinutes) {
      return 0;
    }
    const minTimeSeconds = this.pendingTraining.minViewTimeMinutes * 60;
    return Math.min((this.elapsedTime / minTimeSeconds) * 100, 100);
  }

  // viewTraining(training: any, isDeadlineCrossed: boolean = false) {
  //   if (!training || !training.content) {
  //     this.openAlert('Training content not available', 'warning');
  //     return;
  //   }
  //   this.showQuizSubmitComponent = false;
  //   this.viewingTraining = training;
  //   this.isViewingTraining = true;

  //   // Reset timer and flags
  //   this.elapsedTime = 0;
  //   this.elapsedTimeDisplay = '00:00';
  //   this.minTimeReached = false;
  //   this.consentButtonEnabled = false;
  //   this.hasVisitedLink = false;

  //   // Clear previous content
  //   this.previewUrl = '';
  //   this.safePreviewUrl = null;
  //   this.file = null;
  //   this.fileSize = 0;
  //   this.pptxSlides = [];
  //   this.currentSlideIndex = 0;

  //   const content = training.content;
  //   this.contentType = content.contentType;
  //   this.contentFormData.contentType = content.contentType;

  //   // For completed trainings: no timer, no lock
  //   if (training.status === 'COMPLETED') {
  //     this.minTimeReached = true;
  //     this.consentButtonEnabled = false;
  //     this.quizButtonEnabled = false;
  //   } else {
  //     // For pending or skipped trainings: check if timer needed
  //     if (!training.hasSeenContent && training.minViewTimeMinutes && training.minViewTimeMinutes > 0) {
  //       this.startTimerForViewing(training.minViewTimeMinutes);
  //     } else {
  //       this.minTimeReached = true;
  //       if (training.consentRequired === 'true') {
  //         this.consentButtonEnabled = true;
  //       } else if(training.hasQuiz){
  //         this.quizButtonEnabled = true;
  //       }
  //     }
  //   }

  //   // Setup content
  //   if (content.contentType === 'LINK') {
  //     this.isExternalLink = true;
  //     this.previewUrl = content.externalLinkUrl;
  //     this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl);
  //     this.openTrainingViewModal();
  //   } else {
  //     this.isExternalLink = false;
  //     // Download content for preview
  //     this.trainingService.downloadContent(content.contentId).subscribe({
  //       next: (resp: any) => {
  //         const blob: Blob = resp.body;
  //         if (!blob || blob.size === 0) {
  //           this.openAlert('File is empty or could not be loaded', 'error');
  //           return;
  //         }

  //         const contentType = resp.headers.get('Content-Type') || 'application/octet-stream';
  //         let fileName = content.contentName || 'content';

  //         // Extract filename from header
  //         const disposition = resp.headers.get('Content-Disposition');
  //         if (disposition) {
  //           const patterns = [
  //             /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/,
  //             /filename="([^"]+)"/,
  //             /filename=([^;]+)/
  //           ];
  //           for (const pattern of patterns) {
  //             const match = disposition.match(pattern);
  //             if (match && match[1]) {
  //               fileName = match[1].replace(/['"]/g, '').trim();
  //               break;
  //             }
  //           }
  //         }

  //         // Add extension if missing
  //         if (content.contentPath && !fileName.includes('.')) {
  //           const pathParts = content.contentPath.split('.');
  //           if (pathParts.length > 1) {
  //             fileName += '.' + pathParts[pathParts.length - 1];
  //           }
  //         }

  //         this.previewUrl = URL.createObjectURL(blob);
  //         this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl);
  //         this.file = new File([blob], fileName, { type: contentType });
  //         this.fileSize = blob.size / 1024 / 1024;

  //         // Parse PPTX if needed
  //         if (content.contentType === 'PPT' && fileName.toLowerCase().endsWith('.pptx')) {
  //           this.parsePPTXFile(this.file);
  //         }

  //         this.openTrainingViewModal();
  //       },
  //       error: (error) => {
  //         if (error?.error instanceof Blob) {
  //           error.error.text().then((text: string) => {
  //             this.openAlert(text || 'Error loading content', 'error');
  //           });
  //         } else {
  //           this.openAlert('Error loading content', 'error');
  //         }
  //       }
  //     });
  //   }
  // }

  viewTraining(training: any, isDeadlineCrossed: boolean = false) {
  if (!training || !training.content) {
    this.openAlert('Training content not available', 'warning');    return;
  }
  this.showQuizSubmitComponent = false;
  this.viewingTraining = training;
  this.isViewingTraining = true;

  // Reset timer and flags
  this.elapsedTime = 0;
  this.elapsedTimeDisplay = '00:00';
  this.minTimeReached = false;
  this.consentButtonEnabled = false;
  this.hasVisitedLink = false;

  // Clear previous content
  this.previewUrl = '';
  this.safePreviewUrl = null;
  this.file = null;
  this.pdfFile = null;
  this.fileSize = 0;
  this.pptxSlides = [];
  this.slides = [];
  this.totalSlides = 0;
  this.previewImageBlob = null;
  this.currentSlideIndex = 0;
  this.preloadedSlides.clear();
  this.preloadQueue = [];

  const content = training.content;
  this.contentType = content.contentType;
  this.contentFormData.contentType = content.contentType;

  // For completed trainings: no timer, no lock
  if (training.status === 'COMPLETED') {
    this.minTimeReached = true;
    this.consentButtonEnabled = false;
    this.quizButtonEnabled = false;
  } else {
    // For pending or skipped trainings: check if timer needed
    if (
      !training.hasSeenContent &&
      training.minViewTimeMinutes &&
      training.minViewTimeMinutes > 0
    ) {
      this.startTimerForViewing(training.minViewTimeMinutes);
    } else {
      this.minTimeReached = true;
      if (training.consentRequired === 'true') {
        this.consentButtonEnabled = true;
      } else if (training.hasQuiz) {
        this.quizButtonEnabled = true;
      }
    }
  }

  // Setup content
  if (content.contentType === 'LINK') {
    this.isExternalLink = true;
    this.previewUrl = content.externalLinkUrl;
    this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
      this.previewUrl,
    );
    this.openTrainingViewModal();
  } else {
    this.isExternalLink = false;
    // Download content for preview
    this.trainingService.downloadContent(content.contentId).subscribe({
      next: (resp: any) => {
        const blob: Blob = resp.body;
        if (!blob || blob.size === 0) {
          this.openAlert('File is empty or could not be loaded', 'error');
          return;
        }

        const contentType =
          resp.headers.get('Content-Type') || 'application/octet-stream';
        let fileName = content.contentName || 'content';

        // Extract filename from header
        const disposition = resp.headers.get('Content-Disposition');
        if (disposition) {
          const patterns = [
            /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/,
            /filename="([^"]+)"/,
            /filename=([^;]+)/,
          ];
          for (const pattern of patterns) {
            const match = disposition.match(pattern);
            if (match && match[1]) {
              fileName = match[1].replace(/['"]/g, '').trim();
              break;
            }
          }
        }

        // Add extension if missing
        if (content.contentPath && !fileName.includes('.')) {
          const pathParts = content.contentPath.split('.');
          if (pathParts.length > 1) {
            fileName += '.' + pathParts[pathParts.length - 1];
          }
        }

        this.previewUrl = URL.createObjectURL(blob);
        this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
          this.previewUrl,
        );
        this.file = new File([blob], fileName, { type: contentType });
        this.fileSize = blob.size / 1024 / 1024;

        // Handle PDF files with client-side rendering
        if (content.contentType === 'PDF') {
          this.pdfFile = this.file;
          this.parsePDFFile(this.file);
        }
        // Parse PPTX if needed
        else if (
          content.contentType === 'PPT' &&
          fileName.toLowerCase().endsWith('.pptx')
        ) {
          this.parsePPTXFile(this.file);
        }

        this.openTrainingViewModal();
      },
      error: (error) => {
        if (error?.error instanceof Blob) {
          error.error.text().then((text: string) => {
            this.openAlert(text || 'Error loading content', 'error');
          });
        } else {
          this.openAlert('Error loading content', 'error');
        }
      },
    });
  }
}

async parsePDFFile(file: File): Promise<void> {
  try {
    this.isLoadingPreview = true;
    this.clearAllPreviewData();
    
    this.pdfFile = file;
    
    const arrayBuffer = await file.arrayBuffer();
    const loadingTask = pdfjsLib.getDocument({ data: arrayBuffer });
    const pdf = await loadingTask.promise;
    
    this.totalSlides = pdf.numPages;
    this.slides = new Array(this.totalSlides);
    
    // Load the first page
    await this.renderLocalPDFPage(0);
    
    // Preload next 3 pages with delays
    const preloadNextPages = async () => {
      for (let i = 1; i <= 3; i++) {
        if (i < this.totalSlides) {
          await new Promise(resolve => setTimeout(resolve, 200));
          this.renderLocalPDFPage(i).catch(err => 
            console.error(`Failed to preload slide ${i}:`, err)
          );
        }
      }
    };
    
    preloadNextPages();
    
    this.isLoadingPreview = false;
    console.log("PDF parsed successfully");
    
  } catch (error) {
    console.error('Error parsing PDF file:', error);
    this.isLoadingPreview = false;
    this.openAlert('Failed to parse PDF file', 'error');
  }
}

  startTimerForViewing(minViewTimeMinutes: number) {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }

    const minTimeSeconds = minViewTimeMinutes * 60;

    this.timerInterval = setInterval(() => {
      this.elapsedTime++;

      const minutes = Math.floor(this.elapsedTime / 60);
      const seconds = this.elapsedTime % 60;
      this.elapsedTimeDisplay = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

      if (this.elapsedTime >= minTimeSeconds) {
        console.log('min time reached');

        this.minTimeReached = true;
        if (this.viewingTraining) {
          if (
            this.viewingTraining.lastCompletedOn != null &&
            this.viewingTraining.status == 'PENDING'
          ) {
            this.consentButtonEnabled = true;
            this.quizButtonEnabled = false;
          } else if (
            this.viewingTraining.hasQuiz &&
            this.viewingTraining.status !== 'COMPLETED'
          ) {
            this.quizButtonEnabled = true;
            this.consentButtonEnabled = false;
          } else if (
            this.viewingTraining.consentRequired === 'true' &&
            this.viewingTraining.status !== 'COMPLETED'
          ) {
            this.consentButtonEnabled = true;
          } else {
            this.quizButtonEnabled = false;
            this.consentButtonEnabled = false;
          }
        }
        clearInterval(this.timerInterval);
      }
    }, 1000);
  }

  // async parsePPTXFile(file: File) {
  //   try {
  //     const zip = await JSZip.loadAsync(file);
  //     const slideFiles = Object.keys(zip.files)
  //       .filter(path => path.startsWith('ppt/slides/slide') && path.endsWith('.xml'))
  //       .map(path => ({ path, file: zip.files[path] }))
  //       .sort((a, b) => {
  //         const aNum = parseInt(a.path.match(/slide(\d+)/)?.[1] || '0');
  //         const bNum = parseInt(b.path.match(/slide(\d+)/)?.[1] || '0');
  //         return aNum - bNum;
  //       });

  //     this.pptxSlides = [];

  //     for (let i = 0; i < slideFiles.length; i++) {
  //       const slideFile = slideFiles[i];
  //       const slideXml = await slideFile.file.async('string');

  //       const parser = new DOMParser();
  //       const xmlDoc = parser.parseFromString(slideXml, 'text/xml');

  //       const slideNum = slideFile.path.match(/slide(\d+)/)?.[1] || (i + 1).toString();
  //       const relsPath = `ppt/slides/_rels/slide${slideNum}.xml.rels`;

  //       const relationshipMap: Map<string, string> = new Map();
  //       try {
  //         if (zip.file(relsPath)) {
  //           const relsXml = await zip.file(relsPath)!.async('string');
  //           const relsDoc = parser.parseFromString(relsXml, 'text/xml');
  //           const relationships = relsDoc.getElementsByTagName('Relationship');

  //           for (let r = 0; r < relationships.length; r++) {
  //             const rel = relationships[r];
  //             const id = rel.getAttribute('Id');
  //             const target = rel.getAttribute('Target');
  //             const type = rel.getAttribute('Type');

  //             if (id && target && type && type.includes('image')) {
  //               relationshipMap.set(id, target);
  //             }
  //           }
  //         }
  //       } catch (relsError) {
  //         console.warn('Could not parse relationship file:', relsPath, relsError);
  //       }

  //       const slideImages: string[] = [];
  //       const imageElements = xmlDoc.getElementsByTagName('a:blip');

  //       for (let j = 0; j < imageElements.length; j++) {
  //         const embedId = imageElements[j].getAttribute('r:embed');
  //         if (embedId) {
  //           let imagePath = relationshipMap.get(embedId);

  //           if (!imagePath) {
  //             const possiblePaths = [
  //               `ppt/media/image${embedId}.png`,
  //               `ppt/media/image${embedId}.jpg`,
  //               `ppt/media/image${embedId}.jpeg`
  //             ];

  //             for (const path of possiblePaths) {
  //               if (zip.file(path)) {
  //                 imagePath = path;
  //                 break;
  //               }
  //             }
  //           }

  //           if (imagePath) {
  //             const imageFile = zip.file(imagePath);
  //             if (imageFile) {
  //               const imageBlob = await imageFile.async('blob');
  //               const imageUrl = URL.createObjectURL(imageBlob);
  //               slideImages.push(imageUrl);
  //             }
  //           }
  //         }
  //       }

  //       const slideTexts: string[] = [];
  //       const textElements = xmlDoc.getElementsByTagName('a:t');
  //       for (let t = 0; t < textElements.length; t++) {
  //         const text = textElements[t].textContent?.trim();
  //         if (text) {
  //           slideTexts.push(text);
  //         }
  //       }

  //       this.pptxSlides.push({
  //         slideNumber: i + 1,
  //         images: slideImages,
  //         texts: slideTexts
  //       });
  //     }

  //     this.currentSlideIndex = 0;
  //   } catch (error) {
  //     console.error('Error parsing PPTX:', error);
  //     this.openAlert('Error parsing PowerPoint file', 'error');
  //   }
  // }

  async parsePPTXFile(file: File) {
    try {
      this.isLoadingPPTX = true;
      this.pptxSlides = [];
      this.currentSlideIndex = 0;
      this.isFullscreen = false;

      // Read the file as array buffer
      const arrayBuffer = await file.arrayBuffer();
      const zip = await JSZip.loadAsync(arrayBuffer);

      // Get slide files (ppt/slides/slide1.xml, slide2.xml, etc.)
      const slideFiles: any[] = [];
      zip.forEach((relativePath, file) => {
        if (
          relativePath.startsWith('ppt/slides/slide') &&
          relativePath.endsWith('.xml') &&
          !relativePath.includes('_rels')
        ) {
          slideFiles.push({ path: relativePath, file: file });
        }
      });

      // Sort slides by number
      slideFiles.sort((a, b) => {
        const numA = parseInt(a.path.match(/slide(\d+)/)?.[1] || '0');
        const numB = parseInt(b.path.match(/slide(\d+)/)?.[1] || '0');
        return numA - numB;
      });

      // Extract images and text from slides
      for (let i = 0; i < slideFiles.length; i++) {
        const slideFile = slideFiles[i];
        const slideXml = await slideFile.file.async('string');

        // Parse slide XML
        const parser = new DOMParser();
        const xmlDoc = parser.parseFromString(slideXml, 'text/xml');

        // Get slide number for relationship file lookup
        const slideNum =
          slideFile.path.match(/slide(\d+)/)?.[1] || (i + 1).toString();
        const relsPath = `ppt/slides/_rels/slide${slideNum}.xml.rels`;

        // Parse relationship file to map IDs to actual file paths
        const relationshipMap: Map<string, string> = new Map();
        try {
          const relsFile = zip.file(relsPath);
          if (relsFile) {
            const relsXml = await relsFile.async('string');
            const relsDoc = parser.parseFromString(relsXml, 'text/xml');
            const relationships = relsDoc.getElementsByTagName('Relationship');

            for (let r = 0; r < relationships.length; r++) {
              const rel = relationships[r];
              const id = rel.getAttribute('Id');
              const target = rel.getAttribute('Target');
              const type = rel.getAttribute('Type');

              // Map image relationships
              if (id && target && type && type.includes('image')) {
                // Resolve relative path
                let imagePath = target;
                if (target.startsWith('../')) {
                  imagePath = target.replace('../', 'ppt/');
                } else if (!target.startsWith('ppt/')) {
                  imagePath = `ppt/${target}`;
                }
                relationshipMap.set(id, imagePath);
              }
            }
          }
        } catch (relsError) {
          console.warn(
            'Could not parse relationship file:',
            relsPath,
            relsError,
          );
        }

        // Extract images using relationship mapping
        const slideImages: string[] = [];
        const imageElements = xmlDoc.getElementsByTagName('a:blip');

        for (let j = 0; j < imageElements.length; j++) {
          const embedId = imageElements[j].getAttribute('r:embed');
          if (embedId) {
            // Look up the actual image path from relationship map
            let imagePath = relationshipMap.get(embedId);

            // Fallback: try direct path if relationship map doesn't have it
            if (!imagePath) {
              // Try different possible paths
              const possiblePaths = [
                `ppt/media/${embedId}`,
                `ppt/media/image${embedId}.png`,
                `ppt/media/image${embedId}.jpg`,
                `ppt/media/image${embedId}.jpeg`,
              ];

              for (const path of possiblePaths) {
                if (zip.file(path)) {
                  imagePath = path;
                  break;
                }
              }
            }

            if (imagePath) {
              const imageFile = zip.file(imagePath);
              if (imageFile) {
                try {
                  const imageBlob = await imageFile.async('blob');
                  const imageUrl = URL.createObjectURL(imageBlob);
                  slideImages.push(imageUrl);
                } catch (imgError) {
                  console.warn('Error loading image:', imagePath, imgError);
                }
              }
            }
          }
        }

        // Extract text content - check multiple possible text element tags
        const slideTexts: string[] = [];

        // Method 1: Direct text elements (a:t)
        const textElements = xmlDoc.getElementsByTagName('a:t');
        for (let k = 0; k < textElements.length; k++) {
          const text = textElements[k].textContent;
          if (text && text.trim()) {
            slideTexts.push(text.trim());
          }
        }

        // Method 2: Text paragraphs (a:p)
        const paraElements = xmlDoc.getElementsByTagName('a:p');
        for (let p = 0; p < paraElements.length; p++) {
          const para = paraElements[p];
          const paraTexts = para.getElementsByTagName('a:t');
          let paraText = '';
          for (let pt = 0; pt < paraTexts.length; pt++) {
            const text = paraTexts[pt].textContent;
            if (text) {
              paraText += text;
            }
          }
          if (paraText.trim()) {
            // Avoid duplicates
            if (!slideTexts.includes(paraText.trim())) {
              slideTexts.push(paraText.trim());
            }
          }
        }

        // Method 3: Text runs (a:r)
        const runElements = xmlDoc.getElementsByTagName('a:r');
        for (let r = 0; r < runElements.length; r++) {
          const run = runElements[r];
          const runTexts = run.getElementsByTagName('a:t');
          let runText = '';
          for (let rt = 0; rt < runTexts.length; rt++) {
            const text = runTexts[rt].textContent;
            if (text) {
              runText += text;
            }
          }
          if (runText.trim()) {
            // Avoid duplicates
            if (!slideTexts.includes(runText.trim())) {
              slideTexts.push(runText.trim());
            }
          }
        }

        // If we have content (images or text), add the slide
        // Even if empty, we'll add it to show the slide structure
        this.pptxSlides.push({
          slideNumber: i + 1,
          images: slideImages,
          texts: slideTexts,
          hasContent: slideImages.length > 0 || slideTexts.length > 0,
        });
      }

      this.isLoadingPPTX = false;
    } catch (error) {
      console.error('Error parsing PPTX file:', error);
      this.isLoadingPPTX = false;
      // Fallback: show download option
      this.pptxSlides = [];
    }
  }

  // toggleFullscreen() {
  //   const element = document.documentElement;
  //   if (!document.fullscreenElement) {
  //     element.requestFullscreen().catch(err => {
  //       console.error('Error entering fullscreen:', err);
  //     });
  //   } else {
  //     document.exitFullscreen();
  //   }
  // }

  toggleThumbnails() {
    this.showThumbnails = !this.showThumbnails;
  }

  onExternalLinkClickForModal() {
    if (this.isExternalLink && this.previewUrl) {
      window.open(this.previewUrl, '_blank');
      this.hasVisitedLink = true;

      if (
        this.viewingTraining &&
        this.viewingTraining.consentRequired === 'true'
      ) {
        this.consentButtonEnabled = true;
      }
    }
  }

  onSubmitConsentForModal() {
    if (!this.viewingTraining || !this.viewingTraining.content) {
      return;
    }

    // Validate minimum time if required (for pending or skipped trainings)
    if (
      this.viewingTraining.status === 'PENDING' ||
      this.viewingTraining.status === 'SKIPPED'
    ) {
      if (
        this.viewingTraining.minViewTimeMinutes &&
        this.viewingTraining.minViewTimeMinutes > 0
      ) {
        if (!this.minTimeReached) {
          this.openAlert(
            `Please view the training for at least ${this.viewingTraining.minViewTimeMinutes} minutes before submitting consent.`,
            'warning',
          );
          return;
        }
      }

      // Validate external link visit
      if (this.isExternalLink && !this.hasVisitedLink) {
        this.openAlert(
          'Please click on the external link to visit the training content before submitting consent.',
          'warning',
        );
        return;
      }
    }

    const consentData = {
      trainingId: this.viewingTraining.trainingId,
      contentId: this.viewingTraining.content.contentId,
      empId: this.currentUser.empId,
      completionCycleNumber: this.viewingTraining.currentCycleNumber,
    };

    this.trainingService.submitConsent(consentData).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          // Stop timer
          if (this.timerInterval) {
            clearInterval(this.timerInterval);
          }
          // Close current training view
          this.closeTrainingView();
          // Show success message
          this.openAlert(
            'Training completed successfully! Your consent has been submitted.',
            'success',
          );

          this.currentUser.trainingLockStatus = null;
          this.authenticationService.setcurrentUserSubject(this.currentUser);

          // Reload trainings to remove from mandatory list and update lock status
          setTimeout(() => {
            this.checkLockStatus();
            this.loadUserTrainings();

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
                  this.deadlineCrossedTrainings[
                    this.currentDeadlineCrossedIndex
                  ],
                  true,
                );
              }, 500);
            } else {
              this.isAutoOpening = false;
            }
          }, 1500);
        }
      },
      error: (error: any) => {
        // this.openAlert('Error submitting consent: ' + (error.error?.message || error.message), 'error');
        this.openAlert(
          error.serviceStatus || 'Failed to submit consent',
          'error',
        );
      },
    });
  }

  onSkipTrainingForModal() {
    if (!this.viewingTraining) {
      return;
    }

    // Skip not allowed for deadline-crossed trainings
    if (this.viewingTraining.isDeadlineCrossed) {
      this.openAlert('Deadline has passed. Skip is not allowed.', 'error');
      return;
    }

    // Check if skip is allowed
    if (this.viewingTraining.skipAllowed !== 'true') {
      this.openAlert('Skip is not allowed for this training.', 'error');
      return;
    }

    const skipData = {
      trainingId: this.viewingTraining.trainingId,
      empId: this.currentUser.empId,
      cycleNumber: this.viewingTraining.currentCycleNumber,
    };

    this.trainingService.skipTraining(skipData).subscribe({
      next: (response: any) => {
        if (response.serviceStatus === 'Success') {
          // Stop timer
          if (this.timerInterval) {
            clearInterval(this.timerInterval);
          }

          // Close current training view
          this.closeTrainingView();

          // Route to home page after successful skip
          setTimeout(() => {
            this.router.navigate(['/home']);
          }, 500);
        }
      },
      error: (error: any) => {
        // this.openAlert('Error skipping training: ' + error.message, 'error');
        this.openAlert(
          error.serviceStatus || 'Failed to skip training',
          'error',
        );
      },
    });
  }

  openTrainingViewModal() {
    if (this.trainingViewModalTemplate) {
      this.modalRef = this.modalService.open(this.trainingViewModalTemplate, {
        size: 'xl',
        centered: true,
        backdrop: this.isAutoOpening ? 'static' : true,
        keyboard: !this.isAutoOpening,
      });

      this.modalRef.result.finally(() => {
        this.closeTrainingView();
      });
    }
  }

  closeTrainingView() {
    if (this.modalRef) {
      this.modalRef.close();
      this.modalRef = null;
    }

    // Stop timer
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }

    // Revoke blob URLs
    if (this.previewUrl && this.previewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(this.previewUrl);
    }

    // Clean up preloaded slides
    this.preloadedSlides.forEach((blobUrl) => {
      if (blobUrl && blobUrl.startsWith('blob:')) {
        URL.revokeObjectURL(blobUrl);
      }
    });
    this.preloadedSlides.clear();

    // Clean up PPTX slide image URLs
    if (this.pptxSlides && this.pptxSlides.length > 0) {
      this.pptxSlides.forEach((slide) => {
        if (slide.images) {
          slide.images.forEach((imgUrl: string) => {
            if (imgUrl && imgUrl.startsWith('blob:')) {
              URL.revokeObjectURL(imgUrl);
            }
          });
        }
      });
    }

    // Reset PDF variables
    this.slides = [];
    this.totalSlides = 0;
    this.previewImageBlob = null;
    this.isLoadingPreview = false;
    this.currentPDFFile = null;

    this.viewingTraining = null;
    this.isViewingTraining = false;
    this.previewUrl = '';
    this.safePreviewUrl = null;
    this.file = null;
    this.fileSize = 0;
    this.pptxSlides = [];
    this.currentSlideIndex = 0;
    this.elapsedTime = 0;
    this.elapsedTimeDisplay = '00:00';
    this.minTimeReached = false;
    this.consentButtonEnabled = false;
    this.quizButtonEnabled = false;
    this.hasVisitedLink = false;
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

  // ngOnDestroy() {
  //   if (this.timerInterval) {
  //     clearInterval(this.timerInterval);
  //   }
  //   this.closeTrainingView();
  //   if (this.modalRef) {
  //     this.modalRef.close();
  //     this.modalRef = null;
  //   }
  // }
  closeContentModal() {
    this.modalRef.close();
    this.modalRef = null;
    this.resetQuizView();
  }

  goToQuiz() {
    this.showQuizSubmitComponent = true;
  }

  onQuizCompleted(event: boolean) {
    this.resetQuizView();
    this.showQuizSubmitComponent = false;
    this.modalRef.close();
    this.modalRef = null;
    setTimeout(() => {
      this.checkLockStatus();
      this.loadUserTrainings();

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

  resetQuizView() {
    this.viewingTraining = null;
    this.isViewingTraining = false;
    this.previewUrl = '';
    this.safePreviewUrl = null;
    this.file = null;
    this.fileSize = 0;
    this.pptxSlides = [];
    this.currentSlideIndex = 0;
    this.elapsedTime = 0;
    this.elapsedTimeDisplay = '00:00';
    this.minTimeReached = false;
    this.consentButtonEnabled = false;
    this.quizButtonEnabled = false;
    this.hasVisitedLink = false;
  }

  // Add these methods to your component class

  downloadPreviewFile() {
    if (this.file) {
      const url = URL.createObjectURL(this.file);
      const a = document.createElement('a');
      a.href = url;
      a.download = this.file.name;
      a.click();
      URL.revokeObjectURL(url);
    } else if (this.previewUrl) {
      window.open(this.previewUrl, '_blank');
    }
  }

  openPreviewInNewTab() {
    if (this.previewUrl) {
      window.open(this.previewUrl, '_blank');
    }
  }

  toggleFullscreen() {
    this.isFullscreen = !this.isFullscreen;
    const modalElement = document.querySelector('.preview-modal-content');

    if (this.isFullscreen) {
      if (modalElement) {
        if ((modalElement as any).requestFullscreen) {
          (modalElement as any).requestFullscreen();
        } else if ((modalElement as any).webkitRequestFullscreen) {
          (modalElement as any).webkitRequestFullscreen();
        } else if ((modalElement as any).mozRequestFullScreen) {
          (modalElement as any).mozRequestFullScreen();
        } else if ((modalElement as any).msRequestFullscreen) {
          (modalElement as any).msRequestFullscreen();
        }
      }
    } else {
      if ((document as any).exitFullscreen) {
        (document as any).exitFullscreen();
      } else if ((document as any).webkitExitFullscreen) {
        (document as any).webkitExitFullscreen();
      } else if ((document as any).mozCancelFullScreen) {
        (document as any).mozCancelFullScreen();
      } else if ((document as any).msExitFullscreen) {
        (document as any).msExitFullscreen();
      }
    }
  }
  // Add this to ensure images are properly cleaned up
  ngOnDestroy() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
    this.closeTrainingView();

    // Clean up preloaded slides
    this.preloadedSlides.forEach((blobUrl) => {
      if (blobUrl && blobUrl.startsWith('blob:')) {
        URL.revokeObjectURL(blobUrl);
      }
    });
    this.preloadedSlides.clear();

    // Clean up PPTX slide image URLs
    if (this.pptxSlides && this.pptxSlides.length > 0) {
      this.pptxSlides.forEach((slide) => {
        if (slide.images) {
          slide.images.forEach((imgUrl: string) => {
            if (imgUrl && imgUrl.startsWith('blob:')) {
              URL.revokeObjectURL(imgUrl);
            }
          });
        }
      });
    }

    if (this.modalRef) {
      this.modalRef.close();
      this.modalRef = null;
    }
  }

  // Add these methods
  setFilter(status: string) {
    this.selectedFilterStatus = status;
    this.applyFilterByStatus();
  }

  applyFilterByStatus() {
    if (this.selectedFilterStatus === 'ALL') {
      this.allTrainingsFiltered = [...this.allTrainings];
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
            t.trainingName.toLowerCase().includes(filterLower)) ||
          (t.trainingType &&
            t.trainingType.toLowerCase().includes(filterLower)),
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
    if (this.viewingTraining.status.toLowerCase() == 'completed') {
      return false;
    }

    if (!this.viewingTraining.hasQuiz) {
      return false;
    } else if (
      this.viewingTraining.hasQuiz &&
      !this.viewingTraining.quizAttempted
    ) {
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

  async renderLocalPDFPage(index: number): Promise<void> {
  // Check cache first
  if (this.preloadedSlides.has(index)) {
    console.log(`Page ${index} already cached`);
    if (index === this.currentSlideIndex) {
      this.previewImageBlob = this.preloadedSlides.get(index)!;
      this.isLoadingPreview = false;
    }
    return Promise.resolve();
  }
  
  // Check if already in queue
  if (this.preloadQueue.includes(index)) {
    console.log(`Page ${index} already in queue`);
    return Promise.resolve();
  }
  
  // Add to queue
  this.preloadQueue.push(index);
  
  try {
    if (index === this.currentSlideIndex) {
      this.isLoadingPreview = true;
    }

    const fileToUse = this.pdfFile;
    
    if (!fileToUse) {
      throw new Error('No PDF file available');
    }

    const arrayBuffer = await fileToUse.arrayBuffer();
    const loadingTask = pdfjsLib.getDocument({ data: arrayBuffer });
    const pdf = await loadingTask.promise;
    const page = await pdf.getPage(index + 1);

    const viewport = page.getViewport({ scale: 0.8 });

    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');
    
    if (!context) {
      throw new Error('Could not get canvas context');
    }

    canvas.width = viewport.width;
    canvas.height = viewport.height;

    const renderContext = {
      canvasContext: context,
      viewport: viewport
    };

    await page.render(renderContext).promise;

    const blob = await new Promise<Blob>((resolve, reject) => {
      canvas.toBlob((b) => {
        if (b) {
          resolve(b);
        } else {
          reject(new Error('Failed to create blob from canvas'));
        }
      }, 'image/jpeg', 0.8);
    });

    const url = URL.createObjectURL(blob);
    
    if (this.preloadedSlides.has(index)) {
      URL.revokeObjectURL(this.preloadedSlides.get(index)!);
    }

    this.preloadedSlides.set(index, url);
    
    if (index === this.currentSlideIndex) {
      this.previewImageBlob = url;
    }
    
    const queueIndex = this.preloadQueue.indexOf(index);
    if (queueIndex > -1) {
      this.preloadQueue.splice(queueIndex, 1);
    }
    
    if (index === this.currentSlideIndex) {
      this.isLoadingPreview = false;
    }
    
    return Promise.resolve();
  } catch (error) {
    console.error('Error rendering PDF page:', error);
    
    const queueIndex = this.preloadQueue.indexOf(index);
    if (queueIndex > -1) {
      this.preloadQueue.splice(queueIndex, 1);
    }
    
    if (index === this.currentSlideIndex) {
      this.isLoadingPreview = false;
    }
    
    return Promise.reject(error);
  }
}

// Add loadSlide method
loadSlide(index: number) {
  if (index < 0 || index >= this.totalSlides) return;

  this.currentSlideIndex = index;

  // Check cache
  if (this.preloadedSlides.has(index)) {
    this.previewImageBlob = this.preloadedSlides.get(index)!;
    this.isLoadingPreview = false;
    this.triggerPreload(index);
    return;
  }

  // Load on demand
  if (this.pdfFile) {
    if (!this.preloadQueue.includes(index)) {
      this.isLoadingPreview = true;
    }
    
    this.renderLocalPDFPage(index).then(() => {
      this.triggerPreload(index);
    });
  }
}

// Add triggerPreload method
triggerPreload(currentIndex: number) {
  console.log('Triggering preload from index:', currentIndex);
  
  if (this.pdfFile) {
    // Preload next 3 slides only
    for (let i = 1; i <= 4; i++) {
      const nextIndex = currentIndex + i;
      if (nextIndex < this.totalSlides && 
          !this.preloadedSlides.has(nextIndex) && 
          !this.preloadQueue.includes(nextIndex)) {
        console.log('Preloading slide:', nextIndex);
        this.renderLocalPDFPage(nextIndex).catch(err => 
          console.error(`Failed to preload slide ${nextIndex}:`, err)
        );
      }
    }

    for (let i = 1; i <= 3; i++) {
      const prevIndex = currentIndex - i;
      if (prevIndex >= 0 && 
          !this.preloadedSlides.has(prevIndex) && 
          !this.preloadQueue.includes(prevIndex)) {
        console.log('Preloading previous slide:', prevIndex);
        this.renderLocalPDFPage(prevIndex).catch(err => 
          console.error(`Failed to preload slide ${prevIndex}:`, err)
        );
      }
    }
  }
}

// Add navigation methods
nextSlide() {
  if (this.currentSlideIndex < this.totalSlides - 1) {
    this.currentSlideIndex++;
    this.loadSlide(this.currentSlideIndex);
  }
}

previousSlide() {
  if (this.currentSlideIndex > 0) {
    this.currentSlideIndex--;
    this.loadSlide(this.currentSlideIndex);
  }
}

goToSlide(index: number) {
  if (index >= 0 && index < this.totalSlides) {
    this.currentSlideIndex = index;
    this.loadSlide(index);
  }
}

// Add clearAllPreviewData method
clearAllPreviewData() {
  this.preloadedSlides.forEach((blobUrl) => {
    if (blobUrl && blobUrl.startsWith('blob:')) {
      URL.revokeObjectURL(blobUrl);
    }
  });
  this.preloadedSlides.clear();
  this.preloadQueue = [];
  
  if (this.previewImageBlob && this.previewImageBlob.startsWith('blob:')) {
    URL.revokeObjectURL(this.previewImageBlob);
    this.previewImageBlob = null;
  }
  
  this.totalSlides = 0;
  this.currentSlideIndex = 0;
}
}
