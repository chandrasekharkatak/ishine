import { DatePipe, LocationStrategy } from '@angular/common';
import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import * as JSZip from 'jszip';
import * as moment from 'moment';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { TrainingService } from 'src/app/services/training.service';
import * as pdfjsLib from 'pdfjs-dist';
import { GlobalWorkerOptions } from 'pdfjs-dist';
import * as ExcelJS from 'exceljs';
import { saveAs } from 'file-saver';

GlobalWorkerOptions.workerSrc = `//cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjsLib.version}/pdf.worker.min.js`;

@Component({
  standalone: false,
  selector: 'app-training-config',
  templateUrl: './training-config.component.html',
  styleUrls: ['./training-config.component.css']
})
export class TrainingConfigComponent implements OnInit, OnDestroy {

  feature: any = "Training Config";
  currentUser: User;
  userMapping: any = {};

  // Sorting
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType: any;

  // Pagination
  page: number = 1;

  // Flags
  isTable: boolean = false;
  isTrainingForm: boolean = false;
  isContentForm: boolean = false;
  isEditMode: boolean = false;
  isContentEditMode: boolean = false;
  isContentAccordionOpen: boolean = true; // Accordion open by default during creation

  // Data
  trainings: any[] = [];
  trainingContents: any[] = [];
  selectedTraining: any = null;
  trainingFormData: any = {
    trainingId: null,
    trainingName: '',
    trainingType: '',
    mandatoryFlag: 'false',
    effectiveFrom: '',
    effectiveTo: '',
    // frequencyPerYear: 2,
    lockEnabled: 'false',
    minViewTimeMinutes: null,
    consentRequired: 'true',
    skipAllowed: 'false',
    deadlineEnabled: 'true', // Mandatory: deadline always enabled
    deadlinePattern: 'YEARLY', // Default: once per year (last day of year)
    customDeadlineMonths: '',
    activeStatus: 'true'
  };

  contentFormData: any = {
    contentType: 'PDF',
    contentName: '',
    effectiveFrom: '',
    effectiveTo: '',
    externalLinkUrl: '',
    file: null
  };

  // Preview
  previewUrl: string = '';
  safePreviewUrl: SafeResourceUrl | null = null;
  showPreview: boolean = false;
  previewModalRef: NgbModalRef;
  @ViewChild('preview_modal') previewModalTemplate: TemplateRef<any>;

  // View Content
  viewingContent: any = null;
  isViewingExistingContent: boolean = false;

  // PPTX Preview
  pptxSlides: any[] = [];
  currentSlideIndex: number = 0;
  isLoadingPPTX: boolean = false;
  showThumbnails: boolean = false; // Default: thumbnails disabled
  isFullscreen: boolean = false;

  // Modal
  alertMessage: any;
  alertType: 'success' | 'error' | 'warning' | 'info' = 'info';
  modalRef: NgbModalRef;
  @ViewChild('alert_message') alertTemplate: TemplateRef<any>;
  @ViewChild('create_quiz_template') createQuizTemplate!: TemplateRef<any>;
  @ViewChild('show_training_response') showTrainingResponse!: TemplateRef<any>;
  @ViewChild('add_training_type') addTrainingTypeContent!: TemplateRef<any>;
  isResponseSearchEnabled: boolean = false;
  responseTableFilters: any = {};
  responsePage: number = 1;
  trainingResponseColumns: any[] = ['blank', 'empName', 'lastCompletedCycleNumber', 'lastCompletedOn'];
  maxResponseSize: number = 10;

  // Filter
  filters: any = {};
  isSearchEnabled: boolean = false;
  trainingsColumns: any[] = ['blank', 'trainingName', 'trainingType', 'mandatoryFlag', 'lockEnabled', 'activeStatus', 'createdByName', 'createdOn'];
  contentColumns: any[] = ['blank', 'contentName', 'contentType', 'effectiveFrom', 'effectiveTo', 'activeStatus', 'createdByName', 'createdOn'];

  // File upload
  file: any = null;
  maxFileSize: any;
  fileSize: number = 0;

  // Training types
  // trainingTypes: string[] = ['Induction', 'POSH', 'CyberSecurity', 'Compliance', 'Safety'];
  trainingTypes: any[] = [];
  // Deadline patterns
  deadlinePatterns: any[] = [
    { value: 'YEARLY', label: 'Annual (Once a year - January)' },
    { value: 'MID_YEAR', label: 'Biannual (Twice a year - January & July)' },
    { value: 'QUARTERLY', label: 'Quarterly (Every quarter - March, June, September, December)' }
  ];
  preloadedSlides: Map<number, string> = new Map(); // Cache for preloaded slides
  preloadQueue: number[] = [];

  contentTypes: any[] = [
  // { value: 'PPT', label: 'PowerPoint' },
  { value: 'PDF', label: 'PDF Document' },
  // { value: 'IMAGE', label: 'Image' },
  { value: 'VIDEO', label: 'Video' },
  { value: 'AUDIO', label: 'Audio' },
  { value: 'LINK', label: 'External Link' }
];
  allTrainingResponse: any[] = [];
  sortResponseColumn: string = '';
  sortResponseColumnType: string = '';
  sortResponseDirection: string = '';
  slides: string[] = [];
  currentSlide = 0;
  previewImageBlob: string | null = null;
  isLoadingPreview = false;
  totalSlides: number = 0;
  slideBlobs: string[] = []; // Store blob URLs

  showTypeModal: boolean = false;
  newTrainingType: string = '';
  
  constructor(
    private authenticationService: AuthenticationService,
    private locationStrategy: LocationStrategy,
    private modalService: NgbModal,
    private trainingService: TrainingService,
    private sanitizer: DomSanitizer,
    private router: Router,
    private date: DatePipe
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
  }

  ngOnInit(): void {
    let featureMap = this.currentUser.userMapping.find((userMap: any) => userMap.featureName == this.feature);
    if (featureMap && featureMap.subFeatures) {
      featureMap.subFeatures.forEach((sub: any) => {
        this.userMapping[sub.subFeatureName.replaceAll(' ', '_').toLowerCase()] = sub.isActive;
      });
    }

    this.sectionViewInit();
    this.preventBackButton();
    this.getAllTrainingTypes();
    this.maxFileSize = 30;
  }

  preventBackButton() {
    history.pushState(null, null, location.href);
    this.locationStrategy.onPopState(() => {
      history.pushState(null, null, location.href);
    });
  }

  sectionViewInit() {
    this.showTable();
  }

  showTable() {
    if(!this.userMapping.get_all_trainings){
      this.openAlertMod(this.alertTemplate, 'You do not have permission to view all trainings', 'warning');
      return;
    }
    this.isTable = true;
    this.isTrainingForm = false;
    this.isContentForm = false;
    this.filters = {};
    this.isSearchEnabled = false;
    this.getAllTrainings();
  }

  showTrainingForm() {
    this.isTrainingForm = true;
    this.isTable = false;
    this.isContentForm = false;
    this.isEditMode = false;
    this.isContentAccordionOpen = true; // Open accordion by default
    this.resetTrainingForm();
    this.resetContentForm();
    // Load content if editing existing training
    if (this.trainingFormData.trainingId) {
      this.getTrainingContent(this.trainingFormData.trainingId);
    }
  }

  showContentForm(training: any) {
    this.selectedTraining = training;
    this.isContentForm = true;
    this.isTable = false;
    this.isTrainingForm = false;
    this.isContentEditMode = false;
    this.resetContentForm();
    this.getTrainingContent(training.trainingId);
  }

  showContentFormFromTrainingForm() {
    // Get training object from form data
    if (!this.trainingFormData.trainingId) {
      this.openAlertMod(this.alertTemplate, 'Please save the training first before adding content', 'warning');
      return;
    }

    const training = {
      trainingId: this.trainingFormData.trainingId,
      trainingName: this.trainingFormData.trainingName
    };

    this.showContentForm(training);
  }

  resetTrainingForm() {
    this.trainingFormData = {
      trainingId: null,
      trainingName: '',
      trainingType: '',
      mandatoryFlag: 'false',
      effectiveFrom: '',
      effectiveTo: '',
      // frequencyPerYear: 2,
      lockEnabled: 'false',
      minViewTimeMinutes: null,
      consentRequired: 'true',
      skipAllowed: 'false',
      deadlineEnabled: 'true', // Mandatory: deadline always enabled
      deadlinePattern: 'YEARLY', // Default: once per year (last day of year)
      customDeadlineMonths: '',
      activeStatus: 'true'
    };
  }

  onLockEnabledChange(value: string) {
    // When lock is enabled, automatically set skip to false (lock means hard mandatory)
    if (value === 'true') {
      this.trainingFormData.skipAllowed = 'false';
      this.trainingFormData.mandatoryFlag='true';
    }
  }

  resetContentForm() {
  // Clean up preview URL if exists
  if (this.previewUrl && this.previewUrl.startsWith('blob:')) {
    URL.revokeObjectURL(this.previewUrl);
  }

  this.contentFormData = {
    contentType: 'PDF',
    contentName: '',
    effectiveFrom: '',
    effectiveTo: '',
    externalLinkUrl: '',
    file: null
  };
  this.file = null;
  this.fileSize = 0;
  this.previewUrl = '';
  this.safePreviewUrl = null;
  this.showPreview = false;
}

  getAllTrainings() {
    this.trainingService.getAllTrainings().subscribe({
      next: (response: any) => {
        this.trainings = response.serviceResponse || [];
      },
      error: (error: any) => {
        this.openAlertMod(this.alertTemplate, error.error?.serviceStatus || 'Failed to load trainings', 'error');
      }
    });
  }

  getTrainingContent(trainingId: number) {
    this.trainingService.getTrainingContent(trainingId).subscribe({
      next: (response: any) => {
        this.trainingContents = response.serviceResponse || [];
        
        // If editing training, populate content form with active content
        if (this.isEditMode && this.trainingContents.length > 0) {
          // Find active content (prefer currently active, otherwise use first one)
          const activeContent = this.trainingContents.find((c: any) => c.isCurrentlyActive === true) || 
                                this.trainingContents.find((c: any) => c.activeStatus === 'true') || 
                                this.trainingContents[0];
          
          if (activeContent) {
            this.populateContentForm(activeContent);
          }
        }
      },
      error: (error: any) => {
        this.openAlertMod(this.alertTemplate, error.error?.serviceStatus || 'Failed to load training content', 'error');
      }
    });
  }

  populateContentForm(content: any) {
    if (!content) {
      return;
    }
    
    this.contentFormData = {
      contentType: content.contentType || 'PDF',
      contentName: content.contentName || '',
      effectiveFrom: content.effectiveFrom ? moment(content.effectiveFrom).format('YYYY-MM-DD') : '',
      effectiveTo: content.effectiveTo ? moment(content.effectiveTo).format('YYYY-MM-DD') : '',
      externalLinkUrl: content.externalLinkUrl || '',
      file: null, // File cannot be loaded, user needs to re-upload if changing
      contentId: content.contentId || null,
      existingContentPath: content.contentPath || null, // Store existing file path
      existingFileName: content.contentName || null // Store existing file name for display
    };
    
    // Reset file-related fields since we can't load the file
    this.file = null;
    this.fileSize = 0;
    
    // Generate preview URL for LINK type
    if (content.contentType === 'LINK' && content.externalLinkUrl) {
      this.previewUrl = content.externalLinkUrl;
      this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl);
      this.showPreview = true;
    } else {
      this.previewUrl = '';
      this.safePreviewUrl = null;
      this.showPreview = false;
    }
    
    // Clear PPTX slides
    this.pptxSlides = [];
    this.currentSlideIndex = 0;
    
    // Trigger change detection for content type to update form visibility
    setTimeout(() => {
      this.onContentTypeChange();
    }, 100);
  }

  viewExistingContent() {
    if (!this.contentFormData.contentId) {
      return;
    }
    
    // Download/view existing content file
    this.trainingService.downloadContent(this.contentFormData.contentId)
      .subscribe({
        next: (response: any) => {
          const blob = response.body;
          const contentDisposition = response.headers.get('content-disposition');
          let fileName = this.contentFormData.existingFileName || 'content';
          const contentType = response.headers.get('content-type') || 'application/octet-stream';
          
          if (contentDisposition) {
            const fileNameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
            if (fileNameMatch && fileNameMatch[1]) {
              fileName = fileNameMatch[1].replace(/['"]/g, '');
            }
          }
          
          // Create blob URL and open in new tab
          // const url = window.URL.createObjectURL(blob);
          // window.open(url, '_blank');

          this.previewUrl = URL.createObjectURL(
            new Blob([blob], { type: contentType })
          );

          this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl);
          this.showPreview = true;
          const file = new File([blob], fileName, { type: contentType });

          if (contentType.startsWith('application/pdf')) {
            this.contentFormData.contentType = 'PDF';
            this.parsePDFFile(file);
          } else {
            this.contentFormData.contentType = 'OTHER';
            const url = window.URL.createObjectURL(blob);
            window.open(url, '_blank');
          }
        },
        error: (error) => {
          console.error('Error downloading content:', error);
          this.openAlertMod(this.alertTemplate, 'Failed to view content. Please try again.', 'error');
        }
      });
  }

  onCreateTraining() {
    if (!this.validateTrainingForm()) {
      return;
    }

    this.contentFormData.effectiveFrom = this.trainingFormData.effectiveFrom;
    this.contentFormData.effectiveTo = this.trainingFormData.effectiveTo;

    // Validate content is added during creation
    if (!this.validateContentForm()) {
      //this.openAlertMod(this.alertTemplate, 'Content is required to create a training. Please add content first.', 'warning');
      this.isContentAccordionOpen = true; // Open accordion to show content form
      return;
    }

    // Build FormData with both training and content as JSON strings
    const formData = new FormData();

    // Training DTO as JSON string
    const trainingDTO = {
      trainingName: this.trainingFormData.trainingName,
      trainingType: this.trainingFormData.trainingType,
      mandatoryFlag: this.trainingFormData.mandatoryFlag || 'false',
      effectiveFrom: this.trainingFormData.effectiveFrom ? moment(this.trainingFormData.effectiveFrom).format('YYYY-MM-DD') : null,
      effectiveTo: this.trainingFormData.effectiveTo ? moment(this.trainingFormData.effectiveTo).format('YYYY-MM-DD') : null,
      // frequencyPerYear: this.trainingFormData.frequencyPerYear || 2,
      lockEnabled: this.trainingFormData.lockEnabled || 'false',
      minViewTimeMinutes: this.trainingFormData.minViewTimeMinutes || null,
      consentRequired:  'true',
      skipAllowed: this.trainingFormData.skipAllowed || 'false',
      deadlineEnabled: this.trainingFormData.deadlineEnabled || 'false',
      deadlinePattern: this.trainingFormData.deadlinePattern || null,
      customDeadlineMonths: this.trainingFormData.customDeadlineMonths || null,
      createdBy: this.currentUser.empId
    };

    // Content DTO as JSON string
    const contentDTO = {
      contentType: this.contentFormData.contentType,
      contentName: this.contentFormData.contentName,
      effectiveFrom: this.trainingFormData.effectiveFrom ? moment(this.trainingFormData.effectiveFrom).format('YYYY-MM-DD') : null,
      effectiveTo: this.trainingFormData.effectiveTo ? moment(this.trainingFormData.effectiveTo).format('YYYY-MM-DD') : null,
      externalLinkUrl: this.contentFormData.contentType === 'LINK' ? this.contentFormData.externalLinkUrl : null
    };
     formData.append(
       'trainingDTO',
       new Blob([JSON.stringify(trainingDTO)], { type: 'application/json' })
     );
     
     formData.append(
       'contentDTO',
       new Blob([JSON.stringify(contentDTO)], { type: 'application/json' })
     );
    // File (if applicable)
    if (this.contentFormData.contentType !== 'LINK' && this.file) {
      formData.append('file', this.file);
    }

    // Send training and content together
    this.trainingService.createTrainingWithContent(formData).
    subscribe({
    next: (response: any) => {
      console.log("success===> ",response)
      const createdTraining = response.serviceResponse;
      this.openCreateQuizModal(createdTraining);
    },
    error: (error: any) => {
      console.log("HTTP error => ", error);
      this.openAlertMod(this.alertTemplate,
        error?.error?.serviceStatus || 'Something went wrong',
        'error');
    }
  });
  }

  onUpdateTraining() {
    if (!this.validateTrainingForm()) {
      return;
    }
    // Ensure trainingId is set
    if (!this.trainingFormData.trainingId && this.selectedTraining) {
      this.trainingFormData.trainingId = this.selectedTraining.trainingId;
    }

    this.contentFormData.effectiveFrom = this.trainingFormData.effectiveFrom;
    this.contentFormData.effectiveTo = this.trainingFormData.effectiveTo;
      // Validate content form
      if (!this.validateContentForm()) {
        // this.openAlertMod(this.alertTemplate, 'Please complete the content form correctly', 'warning');
        this.isContentAccordionOpen = true;
        return;
      }

      // Build FormData with both training and content as JSON strings
      const formData = new FormData();

      // Training DTO as JSON string
      const trainingDTO = {
        trainingId: this.trainingFormData.trainingId,
        trainingName: this.trainingFormData.trainingName,
        trainingType: this.trainingFormData.trainingType,
        mandatoryFlag: this.trainingFormData.mandatoryFlag || 'false',
        effectiveFrom: this.trainingFormData.effectiveFrom ? moment(this.trainingFormData.effectiveFrom).format('YYYY-MM-DD') : null,
        effectiveTo: this.trainingFormData.effectiveTo ? moment(this.trainingFormData.effectiveTo).format('YYYY-MM-DD') : null,
        // frequencyPerYear: this.trainingFormData.frequencyPerYear || 2,
        lockEnabled: this.trainingFormData.lockEnabled || 'false',
        minViewTimeMinutes: this.trainingFormData.minViewTimeMinutes || null,
        consentRequired:  'true',
        skipAllowed: this.trainingFormData.skipAllowed || 'false',
        deadlineEnabled: this.trainingFormData.deadlineEnabled || 'false',
        deadlinePattern: this.trainingFormData.deadlinePattern || null,
        customDeadlineMonths: this.trainingFormData.customDeadlineMonths || null,
        activeStatus: this.trainingFormData.activeStatus || 'true',
        updatedBy: this.currentUser.empId
      };

      // Content DTO as JSON string
      const contentDTO: any = {
        contentType: this.contentFormData.contentType,
        contentName: this.contentFormData.contentName,
        effectiveFrom: this.contentFormData.effectiveFrom ? moment(this.contentFormData.effectiveFrom).format('YYYY-MM-DD') : null,
        effectiveTo: this.contentFormData.effectiveTo ? moment(this.contentFormData.effectiveTo).format('YYYY-MM-DD') : null,
        externalLinkUrl: this.contentFormData.contentType === 'LINK' ? this.contentFormData.externalLinkUrl : null
      };

      // if(!this.file){
      //   contentDTO.contentPath = this.contentFormData.contentPath;
      //   contentDTO.contentId = this.contentFormData.contentId;
      //   contentDTO.mimeType = this.contentFormData.mimeType;
      //   contentDTO.createdBy = this.contentFormData.createdBy;
      //   contentDTO.createdOn = this.contentFormData.createdOn;
      //   contentDTO.updatedBy = this.contentFormData.updatedBy;
      // }
      
      // Include contentId if editing existing content
      if (this.contentFormData.contentId) {
        contentDTO.contentId = this.contentFormData.contentId;
        // Don't send contentPath if no new file is uploaded - backend will retrieve existing path
      }
      
     formData.append(
       'trainingDTO',
       new Blob([JSON.stringify(trainingDTO)], { type: 'application/json' })
     );
     
     formData.append(
       'contentDTO',
       new Blob([JSON.stringify(contentDTO)], { type: 'application/json' })
     );
      // File (if applicable)
      if (this.contentFormData.contentType !== 'LINK' && this.file) {
        formData.append('file', this.file);
      }

      // Send training and content together
      this.trainingService.updateTrainingWithContent(formData).subscribe({
        next: (response: any) => {
          this.openAlertMod(this.alertTemplate, 'Training and content updated successfully', 'success');
          // Reload content list after update
          if (this.trainingFormData.trainingId) {
            this.getTrainingContent(this.trainingFormData.trainingId);
          }
          this.resetContentForm();
          this.showTable();
        },
        error: (error: any) => {
          console.log("error=> ", error);
          this.openAlertMod(this.alertTemplate, error.error?.serviceStatus || 'Failed to update training', 'error');
        }
      });
    
  }

  onAddQuiz(training: any) {
    // Navigate to survey-config with training context
    this.router.navigate(['/configuration/survey-config'], {
      queryParams: {
        source: 'training',
        trainingId: training.trainingId,
        trainingName: training.trainingName
      }
    });
  }

  onEditTraining(training: any) {
    this.isEditMode = true;
    this.isTrainingForm = true;
    this.isTable = false;
    this.isContentAccordionOpen = true; // Open content accordion when editing
    this.selectedTraining = training; // Store selected training for Add Content
    this.trainingFormData = {
      trainingId: training.trainingId,
      trainingName: training.trainingName,
      trainingType: training.trainingType,
      mandatoryFlag: training.mandatoryFlag,
      effectiveFrom: training.effectiveFrom ? moment(training.effectiveFrom).format('YYYY-MM-DD') : '',
      effectiveTo: training.effectiveTo ? moment(training.effectiveTo).format('YYYY-MM-DD') : '',
      // frequencyPerYear: training.frequencyPerYear,
      lockEnabled: training.lockEnabled,
      minViewTimeMinutes: training.minViewTimeMinutes,
      consentRequired: training.consentRequired,
      skipAllowed: training.skipAllowed,
      deadlineEnabled: training.deadlineEnabled || 'true', // Default to true since deadline is mandatory
      deadlinePattern: training.deadlinePattern || 'YEARLY', // Default to YEARLY if not set
      customDeadlineMonths: training.customDeadlineMonths || '',
      activeStatus: training.activeStatus
    };
    // Load content when editing - this will populate the form
    this.getTrainingContent(training.trainingId);
  }
  
  console = console;

  onViewContent(training: any) {
  this.clearAllPreviewData();

  // Step 1: Load training content
  this.trainingService.getTrainingContent(training.trainingId)
    .subscribe({
      next: (response: any) => {
        const contents = response.serviceResponse || [];

        if (contents.length === 0) {
          this.openAlertMod(this.alertTemplate, 'No content available for this training', 'info');
          return;
        }

        // Step 2: Pick active content
        const activeContent = contents.find((c: any) => c.activeStatus === 'true') || contents[0];

        this.viewingContent = activeContent;
        this.isViewingExistingContent = true;

        // Step 3: Populate content form
        this.contentFormData = {
          contentType: activeContent.contentType,
          contentName: activeContent.contentName,
          effectiveFrom: activeContent.effectiveFrom,
          effectiveTo: activeContent.effectiveTo,
          externalLinkUrl: activeContent.externalLinkUrl || ''
        };

        // Step 4: Handle different content types
        const contentType = activeContent.contentType?.toUpperCase();

        // LINK type - redirect
        if (contentType === 'LINK') {
          this.previewUrl = activeContent.externalLinkUrl;
          this.safePreviewUrl = this.previewUrl ? this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl) : null;
          this.showPreview = true;
          this.openPreviewModal();
          return;
        }

        // For all other FILE types (IMAGE, VIDEO, AUDIO)
        this.trainingService.downloadContent(activeContent.contentId)
          .subscribe({
            next: (resp: any) => {
              const blob: Blob = resp.body;
              const contentType = resp.headers.get('Content-Type') || 'application/octet-stream';

              // Extract filename from header
              let fileName = activeContent.contentName || 'content';
              const disposition = resp.headers.get('Content-Disposition');
              if (disposition) {
                const match = disposition.match(/filename="(.+)"/);
                if (match && match[1]) {
                  fileName = match[1];
                }
              }

              // Create blob URL for the file
              this.previewUrl = URL.createObjectURL(
                new Blob([blob], { type: contentType })
              );
              this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl);
              this.showPreview = true;

              if (activeContent.contentType === 'PDF') {
                  const file = new File([blob], fileName, { type: contentType });
                  this.file = file;
                  this.fileSize = blob.size / 1024 / 1024;
                  this.parsePDFFile(file);
                } else {
                  this.file = new File([blob], fileName, { type: contentType });
                  this.fileSize = blob.size / 1024 / 1024;
                }

              // Store file info for download/preview
              this.file = new File([blob], fileName, { type: contentType });
              this.fileSize = blob.size / 1024 / 1024;

              this.openPreviewModal();
            },
            error: (error: any) => {
              if (error?.error instanceof Blob) {
                error.error.text().then((text: string) => {
                  this.openAlertMod(this.alertTemplate, text || 'Error loading content', 'error');
                });
              } else {
                this.openAlertMod(this.alertTemplate, error.error?.serviceStatus || 'Error loading content', 'error');
              }
            }
          });
      },
      error: (error: any) => {
        this.openAlertMod(this.alertTemplate, error.error?.serviceStatus || 'Error loading training content', 'error');
      }
    });
}

  onAddContent() {
    if (!this.validateContentForm()) {
      return;
    }

    // Use trainingId from form if available, otherwise from selectedTraining
    const trainingId = this.trainingFormData.trainingId || (this.selectedTraining ? this.selectedTraining.trainingId : null);
    if (!trainingId) {
      this.openAlertMod(this.alertTemplate, 'Training ID not found. Please save the training first.', 'warning');
      return;
    }

    const formData = new FormData();
    formData.append('trainingId', trainingId.toString());
    formData.append('contentType', this.contentFormData.contentType);
    formData.append('contentName', this.contentFormData.contentName);
    formData.append('effectiveFrom', moment(this.contentFormData.effectiveFrom).format('YYYY-MM-DD'));
    if (this.contentFormData.effectiveTo) {
      formData.append('effectiveTo', moment(this.contentFormData.effectiveTo).format('YYYY-MM-DD'));
    }
    formData.append('createdBy', this.currentUser.empId.toString());

    if (this.contentFormData.contentType === 'LINK') {
      formData.append('externalLinkUrl', this.contentFormData.externalLinkUrl);
    } else if (this.file) {
      formData.append('file', this.file);
    }

    this.trainingService.addTrainingContent(formData).subscribe({
      next: (response: any) => {
      if (response.serviceStatus === 'Success') {
        this.openAlertMod(this.alertTemplate, 'Content added successfully', 'success');
        // Reload content list
        if (trainingId) {
          this.getTrainingContent(trainingId);
        }
        this.resetContentForm();
      } else {
        this.openAlertMod(this.alertTemplate, response.serviceResponse || 'Failed to add content', 'error');
      }
    },
    error: (error: any) => {
      console.log("In the error: ", error);
      
      this.openAlertMod(this.alertTemplate, 'Error adding content: ' + error.message, 'error');
    }
  });
  }

  onEditContent(content: any) {
    // TODO: Implement content editing if needed
    this.openAlertMod(this.alertTemplate, 'Content editing feature coming soon', 'info');
  }

  onDeactivateTraining(training: any, template: TemplateRef<any>) {
    this.modalRef = this.modalService.open(template, { centered: true });
    this.modalRef.result.then((result) => {
      if (result === 'confirm') {
        this.trainingService.deactivateTraining(training.trainingId, this.currentUser.empId).subscribe({
          next: (response: any) => {
            this.openAlertMod(this.alertTemplate, 'Training deactivated successfully', 'success');
            this.getAllTrainings();
          },
          error: (error: any) => {
            this.openAlertMod(this.alertTemplate, error.error?.serviceStatus || 'Failed to deactivate training', 'error');
          }
        });
      }
    });
  }

  private openCreateQuizModal(createdTraining: any): void {

  this.modalRef = this.modalService.open(this.createQuizTemplate, {
    centered: true
  });

  this.modalRef.result.then((result) => {

    if (result === 'yes') {

      // Redirect to quiz page
      this.router.navigate(['/configuration/survey-config'], {
        queryParams: {
          source: 'trainingAccept',
          trainingId: createdTraining.trainingId,
          trainingName: createdTraining.trainingName,

        }
      });

    } else {

      // Stay in training page
      this.resetContentForm();
      this.resetTrainingForm();
      this.showTable();
    }

  }).catch(() => {

    // If dismissed (X button)
    this.resetContentForm();
    this.resetTrainingForm();
    this.showTable();
  });
}


  onFileSelect(event: any) {
  const file = event.target.files[0];
  if (file) {
    const maxSizeInBytes = this.maxFileSize * 1024 * 1024;
    if (file.size > maxSizeInBytes) {
      this.openAlertMod(this.alertTemplate, `File size exceeds ${this.maxFileSize}MB limit`, 'error');
      event.target.value = '';
      return;
    }
    this.file = file;
    this.fileSize = file.size / 1024 / 1024;

    // Generate preview URL for file
    this.generatePreviewUrl();
  }
}

  generatePreviewUrl() {
    this.safePreviewUrl = null;

    if (this.contentFormData.contentType === 'LINK') {
      this.previewUrl = this.contentFormData.externalLinkUrl || '';
      this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl);
      this.showPreview = !!this.previewUrl;
      return;
    }

    if (!this.file) {
      this.showPreview = false;
      this.previewUrl = '';
      this.pptxSlides = [];
      return;
    }

    // Create blob URL using the file
    this.previewUrl = URL.createObjectURL(this.file);

    // SANITIZE HERE
    this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl);

    this.showPreview = true;

    // if (this.contentFormData.contentType === 'PPT') {
    //   if (this.file.name.toLowerCase().endsWith('.pptx')) {
    //     this.parsePPTXFile(this.file);
    //   } else {
    //     this.pptxSlides = [];
    //   }
    // }

    if (this.contentFormData.contentType === 'PDF') {
      // Don't parse PDF here - it will be handled when preview button is clicked
      this.pptxSlides = [];
    }
  }

  
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
        if (relativePath.startsWith('ppt/slides/slide') && relativePath.endsWith('.xml') && !relativePath.includes('_rels')) {
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
        const slideNum = slideFile.path.match(/slide(\d+)/)?.[1] || (i + 1).toString();
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
          console.warn('Could not parse relationship file:', relsPath, relsError);
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
                `ppt/media/image${embedId}.jpeg`
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
          hasContent: slideImages.length > 0 || slideTexts.length > 0
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

  onContentTypeChange() {
    // Reset preview when content type changes
    this.showPreview = false;
    this.previewUrl = '';
    this.file = null;
    this.contentFormData.externalLinkUrl = '';
  }

  onExternalLinkChange() {
    // Update preview when external link changes
    if (this.contentFormData.contentType === 'LINK') {
      this.generatePreviewUrl();
    }
  }

  openPreviewModal() {
  // For viewing existing content, we already have previewUrl set
  if (this.isViewingExistingContent) {
    this.previewModalRef = this.modalService.open(this.previewModalTemplate, {
      size: 'xl',
      centered: true,
      modalDialogClass: 'preview-modal',
      windowClass: 'preview-modal-window'
    });
    return;
  }

  // For new content upload - use previewFile instead of file
  if (!this.file && !(this.contentFormData.contentType === 'LINK' && this.contentFormData.externalLinkUrl)) {
    this.openAlertMod(this.alertTemplate, 'Please select a file or enter a URL first', 'warning');
    return;
  }

  // Handle PDF files for new uploads (ONLY PDF)
  if (this.contentFormData.contentType === 'PDF' && this.file) {
    // Open modal immediately with loading state
    this.isLoadingPreview = true;
    this.previewModalRef = this.modalService.open(this.previewModalTemplate, {
      size: 'xl',
      centered: true,
      modalDialogClass: 'preview-modal',
      windowClass: 'preview-modal-window'
    });
    
    // Then start parsing PDF using previewFile
    this.parsePDFFile(this.file).catch(error => {
      console.error('Error parsing PDF:', error);
      this.isLoadingPreview = false;
      this.openAlertMod(this.alertTemplate, 'Failed to parse PDF file', 'error');
    });
    return;
  }

  // For all other file types (including PPT - leave as is)
  // Generate preview URL if not already generated
  if (!this.previewUrl) {
    this.generatePreviewUrl();
  }

  if (this.previewUrl || (this.contentFormData.contentType === 'LINK' && this.contentFormData.externalLinkUrl)) {
    this.previewModalRef = this.modalService.open(this.previewModalTemplate, {
      size: 'xl',
      centered: true,
      modalDialogClass: 'preview-modal',
      windowClass: 'preview-modal-window'
    });
  }
}

  openPreviewInNewTab() {
    if (this.previewUrl) {
      window.open(this.previewUrl, '_blank');
    }
  }

  downloadPreviewFile() {
    if (this.file && this.previewUrl) {
      const link = document.createElement('a');
      link.href = this.previewUrl;
      link.download = this.file.name;
      link.click();
    }
  }

  downloadFile() {
    // Download file from preview URL
    if (this.previewUrl) {
      const fileName = this.file?.name ||
        (this.viewingContent?.contentName || 'content') +
        (this.viewingContent?.contentPath ? '.' + this.viewingContent.contentPath.split('.').pop() : '');
      const link = document.createElement('a');
      link.href = this.previewUrl;
      link.download = fileName;
      link.click();
    } else {
      this.openAlertMod(this.alertTemplate, 'File not available for download', 'warning');
    }
  }

  clearAllPreviewData() {
  // Clear preloaded slides
  this.preloadedSlides.forEach((blobUrl) => {
      URL.revokeObjectURL(blobUrl);
  });
  this.preloadedSlides.clear();
  
  // Clear slide blobs
  this.slideBlobs.forEach(blobUrl => {
      URL.revokeObjectURL(blobUrl);
  });
  this.slideBlobs = [];
  
  // Clear current blob
  if (this.previewImageBlob) {
      URL.revokeObjectURL(this.previewImageBlob);
      this.previewImageBlob = null;
  }
  
  // Reset all slide-related variables
  this.slides = [];
  this.totalSlides = 0;
  this.currentSlideIndex = 0;
  this.pptxSlides = [];
  
  // Reset other preview variables
  this.previewUrl = '';
  this.safePreviewUrl = null;
  this.showPreview = false;
  this.isLoadingPreview = false;
  this.file = null;
  this.fileSize = 0;
}

  closePreviewModal() {
    if (this.previewModalRef) {
        this.previewModalRef.close();
    }
    
    // Clean up all preloaded blobs
    this.preloadedSlides.forEach((blobUrl) => {
        URL.revokeObjectURL(blobUrl);
    });
    this.preloadedSlides.clear(); // Clear the cache
    
    // Clean up slide blobs array
    this.slideBlobs.forEach(blobUrl => {
        URL.revokeObjectURL(blobUrl);
    });
    this.slideBlobs = [];
    
    // Clean up current blob
    if (this.previewImageBlob) {
        URL.revokeObjectURL(this.previewImageBlob);
        this.previewImageBlob = null;
    }
    
    // Clean up viewing state
    if (this.isViewingExistingContent) {
        // Revoke blob URL if it was created
        if (this.previewUrl && this.previewUrl.startsWith('blob:')) {
            URL.revokeObjectURL(this.previewUrl);
        }
        this.isViewingExistingContent = false;
        this.viewingContent = null;
        this.previewUrl = '';
        this.safePreviewUrl = null;
        this.showPreview = false;
        this.file = null;
        this.fileSize = 0;
        this.pptxSlides = [];
        this.slides = [];
        this.totalSlides = 0;
        this.currentSlideIndex = 0;
    }
  }

 

  onPPTPreviewError(event: any) {
    // Handle PPT preview error - show fallback options
    console.log('PPT preview error:', event);
  }

  onPDFPreviewError(event: any) {
    // Handle PDF preview error - show fallback options
    console.log('PDF preview error:', event);
    this.openAlertMod(this.alertTemplate, 'PDF preview failed. Please try downloading the file.', 'warning');
  }

  getOfficeViewerUrl(): string {
    if (this.contentFormData.contentType === 'PPT' && this.previewUrl) {
      // Microsoft Office Online Viewer - may not work with blob URLs due to CORS
      return `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(this.previewUrl)}`;
    }
    return '';
  }

  getGoogleDocsViewerUrl(): string {
    if (this.contentFormData.contentType === 'PPT' && this.previewUrl) {
      // Google Docs Viewer - may not work with blob URLs due to CORS
      return `https://docs.google.com/viewer?url=${encodeURIComponent(this.previewUrl)}&embedded=true`;
    }
    return '';
  }

  tryOfficeViewer() {
    if (this.previewUrl) {
      const officeViewerUrl = this.getOfficeViewerUrl();
      if (officeViewerUrl) {
        window.open(officeViewerUrl, '_blank');
      } else {
        this.openAlertMod(this.alertTemplate, 'Preview URL not available', 'warning');
      }
    }
  }

  tryGoogleDocsViewer() {
    if (this.previewUrl) {
      const googleViewerUrl = this.getGoogleDocsViewerUrl();
      if (googleViewerUrl) {
        window.open(googleViewerUrl, '_blank');
      } else {
        this.openAlertMod(this.alertTemplate, 'Preview URL not available', 'warning');
      }
    }
  }

  // Clean up object URLs when component is destroyed
  ngOnDestroy() {
    if (this.previewUrl && this.previewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(this.previewUrl);
    }
    // Clean up PPTX slide image URLs
    this.pptxSlides.forEach(slide => {
      slide.images.forEach((imgUrl: string) => {
        if (imgUrl.startsWith('blob:')) {
          URL.revokeObjectURL(imgUrl);
        }
      });
    });
  }

  validateTrainingForm(): boolean {
    // Training name validation
    if (!this.trainingFormData.trainingName || !this.trainingFormData.trainingName.trim()) {
      this.openAlertMod(this.alertTemplate, 'Training name is required', 'error');
      return false;
    }
    if (this.trainingFormData.trainingName.trim().length < 3) {
      this.openAlertMod(this.alertTemplate, 'Training name must be at least 3 characters long', 'error');
      return false;
    }
    if (this.trainingFormData.trainingName.trim().length > 255) {
      this.openAlertMod(this.alertTemplate, 'Training name cannot exceed 255 characters', 'error');
      return false;
    }

    // Training type validation
    if (!this.trainingFormData.trainingType) {
      this.openAlertMod(this.alertTemplate, 'Training type is required', 'error');
      return false;
    }

    // Date validation
    if (!this.trainingFormData.effectiveFrom) {
      this.openAlertMod(this.alertTemplate, 'Effective from date is required', 'error');
      return false;
    }

    // Effective To should be after Effective From
    if (this.trainingFormData.effectiveTo && this.trainingFormData.effectiveFrom) {
      const fromDate = moment(this.trainingFormData.effectiveFrom);
      const toDate = moment(this.trainingFormData.effectiveTo);
      if (toDate.isBefore(fromDate)) {
        this.openAlertMod(this.alertTemplate, 'Effective to date must be after effective from date', 'error');
        return false;
      }
    }

    // Frequency validation
    // if (this.trainingFormData.frequencyPerYear &&
    //   (this.trainingFormData.frequencyPerYear < 1 || this.trainingFormData.frequencyPerYear > 12)) {
    //   this.openAlertMod(this.alertTemplate, 'Frequency per year must be between 1 and 12', 'error');
    //   return false;
    // }

    // Lock enabled validation
    if (this.trainingFormData.lockEnabled === 'true') {
      if (!this.trainingFormData.minViewTimeMinutes || this.trainingFormData.minViewTimeMinutes < 1) {
        this.openAlertMod(this.alertTemplate, 'Minimum view time (in minutes) is required when lock is enabled', 'error');
        return false;
      }
      if (this.trainingFormData.minViewTimeMinutes > 1440) {
        this.openAlertMod(this.alertTemplate, 'Minimum view time cannot exceed 1440 minutes (24 hours)', 'error');
        return false;
      }
    }

    // Deadline validation - deadline is mandatory
    if (this.trainingFormData.deadlineEnabled !== 'true') {
      this.openAlertMod(this.alertTemplate, 'Deadline is mandatory for all trainings', 'error');
      return false;
    }
    if (!this.trainingFormData.deadlinePattern) {
      this.openAlertMod(this.alertTemplate, 'Deadline pattern is required', 'error');
      return false;
    }
    if (this.trainingFormData.deadlinePattern === 'CUSTOM' && !this.trainingFormData.customDeadlineMonths) {
      this.openAlertMod(this.alertTemplate, 'Custom deadline months are required for custom pattern', 'error');
      return false;
    }
    if (this.trainingFormData.deadlinePattern === 'CUSTOM' && this.trainingFormData.customDeadlineMonths) {
      // Validate custom deadline months format (comma-separated numbers 1-12)
      const months = this.trainingFormData.customDeadlineMonths.split(',').map((m: string) => parseInt(m.trim()));
      if (months.some((m: number) => isNaN(m) || m < 1 || m > 12)) {
        this.openAlertMod(this.alertTemplate, 'Custom deadline months must be comma-separated numbers between 1-12 (e.g., 1,6,12)', 'error');
        return false;
      }
    }

    return true;
  }

  validateContentForm(): boolean {
    // Content name validation
    if (!this.contentFormData.contentName || !this.contentFormData.contentName.trim()) {
      this.openAlertMod(this.alertTemplate, 'Content name is required', 'error');
      return false;
    }
    if (this.contentFormData.contentName.trim().length < 3) {
      this.openAlertMod(this.alertTemplate, 'Content name must be at least 3 characters long', 'error');
      return false;
    }
    if (this.contentFormData.contentName.trim().length > 255) {
      this.openAlertMod(this.alertTemplate, 'Content name cannot exceed 255 characters', 'error');
      return false;
    }

    // Date validation
    if (!this.contentFormData.effectiveFrom) {
      this.openAlertMod(this.alertTemplate, 'Effective from date is required', 'error');
      return false;
    }

    // Effective To should be after Effective From
    if (this.contentFormData.effectiveTo && this.contentFormData.effectiveFrom) {
      const fromDate = moment(this.contentFormData.effectiveFrom);
      const toDate = moment(this.contentFormData.effectiveTo);
      if (toDate.isBefore(fromDate)) {
        this.openAlertMod(this.alertTemplate, 'Effective to date must be after effective from date', 'error');
        return false;
      }
    }

    // Validate content dates align with training dates (if training form is being used)
    if (this.trainingFormData.effectiveFrom) {
      const trainingFrom = moment(this.trainingFormData.effectiveFrom);
      const contentFrom = moment(this.contentFormData.effectiveFrom);
      if (contentFrom.isBefore(trainingFrom)) {
        this.openAlertMod(this.alertTemplate, 'Content effective from date cannot be before training effective from date', 'error');
        return false;
      }
      if (this.trainingFormData.effectiveTo && this.contentFormData.effectiveTo) {
        const trainingTo = moment(this.trainingFormData.effectiveTo);
        const contentTo = moment(this.contentFormData.effectiveTo);
        if (contentTo.isAfter(trainingTo)) {
          this.openAlertMod(this.alertTemplate, 'Content effective to date cannot be after training effective to date', 'error');
          return false;
        }
      }
    }

    // Content type specific validation
    if (this.contentFormData.contentType === 'LINK') {
      if (!this.contentFormData.externalLinkUrl || !this.contentFormData.externalLinkUrl.trim()) {
        this.openAlertMod(this.alertTemplate, 'External link URL is required for link content', 'error');
        return false;
      }
      // URL validation
      try {
        new URL(this.contentFormData.externalLinkUrl.trim());
      } catch (e) {
        this.openAlertMod(this.alertTemplate, 'Please enter a valid URL (e.g., https://example.com)', 'error');
        return false;
      }
    } else {
      // File is required for creation, but optional for update if contentId exists
      if (!this.file && !this.contentFormData.contentId) {
        this.openAlertMod(this.alertTemplate, 'File is required for ' + this.contentFormData.contentType + ' content', 'error');
        return false;
      }
      // File type validation
      const allowedTypes: any = {
        'PPT': ['application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation'],
        'PDF': ['application/pdf'],
        'VIDEO': ['video/mp4', 'video/webm', 'video/ogg', 'video/quicktime', 'video/x-msvideo'],
        'AUDIO': ['audio/mpeg', 'audio/mp3', 'audio/wav', 'audio/ogg', 'audio/webm']
      };
      const allowedExtensions: any = {
        'PPT': ['.ppt', '.pptx'],
        'PDF': ['.pdf'],
        'VIDEO': ['.mp4', '.webm', '.ogg', '.mov', '.avi'],
        'AUDIO': ['.mp3', '.wav', '.ogg', '.webm']
      };

      // Check if content type has allowed extensions defined
      if (!allowedExtensions[this.contentFormData.contentType]) {
        this.openAlertMod(this.alertTemplate, `Invalid content type: ${this.contentFormData.contentType}`, 'error');
        return false;
      }

      // File validation - only validate if file is provided (new file upload)
      // If updating without new file (contentId exists), skip file validation
      if (this.file) {
        const fileExtension = '.' + this.file.name.split('.').pop()?.toLowerCase();
        if (!fileExtension || fileExtension === '.') {
          this.openAlertMod(this.alertTemplate, 'File must have a valid extension', 'error');
          return false;
        }

        if (!allowedExtensions[this.contentFormData.contentType].includes(fileExtension)) {
          this.openAlertMod(this.alertTemplate,
            `Invalid file type for ${this.contentFormData.contentType}. Allowed extensions: ${allowedExtensions[this.contentFormData.contentType].join(', ')}`,
            'error');
          return false;
        }

        // Additional MIME type validation (if browser provides it)
        if (this.file.type && allowedTypes[this.contentFormData.contentType]) {
          const isValidMimeType = allowedTypes[this.contentFormData.contentType].some(
            (allowedType: string) => this.file.type.toLowerCase().includes(allowedType.toLowerCase().split('/')[1])
          );
          if (!isValidMimeType && !this.file.type.includes('application/octet-stream')) {
            // Warn but don't block - MIME type can be unreliable
            console.warn(`MIME type mismatch: Expected ${allowedTypes[this.contentFormData.contentType].join(' or ')}, got ${this.file.type}`);
          }
        }
      }
      // If no file and contentId exists, it's an update without file change - validation passes
    }

    return true;
  }

  toggleSearch() {
    this.isSearchEnabled = !this.isSearchEnabled;
    if (!this.isSearchEnabled) {
      this.filters = {};
    }
  }

  onSearch(event: any) {
    this.filters = event;
  }

  sortData(event: Sort) {
    this.sortColumn = event.active;
    this.sortColumnType = event.active.split('|')[1] || 'string';
    this.sortDirection = event.direction;
  }

  openAlertMod(template: TemplateRef<any>, message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info') {
    this.alertMessage = message;
    this.alertType = type;
    this.modalRef = this.modalService.open(template, {
      backdrop: false,
      windowClass: 'alert-toast-modal',
      modalDialogClass: 'alert-toast-dialog',
      size: 'sm'
    });

    // Auto-close after 3 seconds for success messages
    if (type === 'success') {
      setTimeout(() => {
        if (this.modalRef) {
          this.modalRef.close();
        }
      }, 3000);
    }
  }

  handlePageChange(event: number) {
    this.page = event;
  }

  onViewTrainingResponse(training: any) {
    console.log('View training response for:', training);
    this.trainingService.getTrainingResponses(training.trainingId).subscribe({
      next: (response:any) =>{
        this.allTrainingResponse = response.serviceResponse || [];
        this.maxResponseSize = this.allTrainingResponse.length;
         this.responsePage = 1;
        this.showTrainingResponseModal();
      }, error: (error) =>{
        this.openAlertMod(this.alertTemplate, 'Error fetching training responses', 'error');
        console.log(error);
      }
    })
  }

  showTrainingResponseModal(){
    this.modalRef = this.modalService.open(this.showTrainingResponse, {
      backdrop: false,
      windowClass: 'alert-toast-modal',
      modalDialogClass: 'alert-toast-dialog',
      size: 'lg'
    });
  }

  handleResponsePageChange(event: number) {
    this.responsePage = event;
  }

  onResponseSearch(event: any) {
    this.responseTableFilters = event;
    this.responsePage = 1;
  }

  sortResponseData(event: Sort) {
    this.sortResponseColumn = event.active;
    this.sortResponseColumnType = event.active.split('|')[1] || 'string';
    this.sortResponseDirection = event.direction;
  }

  toggleResponseSearch() {
    this.isResponseSearchEnabled = !this.isResponseSearchEnabled;
    if (!this.isResponseSearchEnabled) {
      this.responseTableFilters = {};
    }
  }
  
  openAddTypeModal() {
    this.newTrainingType = '';
    this.modalRef = this.modalService.open(this.addTrainingTypeContent, { 
      centered: true,
      backdrop: false, 
      keyboard: false     
    });
  }

  getAllTrainingTypes() {
    this.trainingService.getAllTrainingTypes().subscribe({
      next: (response: any) => {
        console.log(response , '=============');
        if (response.serviceStatus === 'Success') {
          this.trainingTypes = response.serviceResponse || [];
          console.log(this.trainingTypes,'=========training types ==========')
        } else {
          this.openAlertMod(this.alertTemplate, response.message || 'Failed to load training types', 'error');
        }
      },
      error: (error: any) => {
        this.openAlertMod(this.alertTemplate, error.error?.message || 'Failed to load training types', 'error');
      }
    });
  }

  saveNewTrainingType() {
   const typeName = this.newTrainingType.trim();
    
    // Check if input is empty
    if (!typeName) {
        this.openAlertMod(this.alertTemplate, 'Please enter a training type name', 'warning');
        return;
    }

    console.log('Submitting new training type:', typeName);

    this.trainingService.addTrainingType(this.newTrainingType.trim(), this.currentUser.empId).subscribe({
      next: (response: any) => {
         console.log('Response:', response);

        if (response.serviceStatus === 'Success') {
          this.modalRef.close();
          this.openAlertMod(this.alertTemplate, 'Training type added successfully', 'success');
          console.log("=======addedddddd");
          this.getAllTrainingTypes(); // dropdown method
          this.newTrainingType = '';
        } else {
          this.openAlertMod(this.alertTemplate, response.message || 'Failed to add training type', 'error');
        }
      },
      error: (error: any) => {
        this.openAlertMod(this.alertTemplate, error.error?.message || 'Failed to add training type', 'error');
      }
    });
  }

async renderLocalPDFPage(index: number): Promise<void> {
  // CRITICAL FIX: Check if this page is already being rendered or is in cache
  if (this.preloadedSlides.has(index)) {
    console.log(`Page ${index} already cached, skipping render`);
    if (index === this.currentSlideIndex) {
      this.previewImageBlob = this.preloadedSlides.get(index)!;
      this.isLoadingPreview = false;
    }
    return Promise.resolve();
  }
  
  // Check if this index is currently being rendered (prevent duplicate renders)
  if (this.preloadQueue.includes(index)) {
    console.log(`Page ${index} is already in queue, skipping`);
    return Promise.resolve();
  }
  
  // Add to queue to prevent duplicate renders
  this.preloadQueue.push(index);
  
  try {
    // Only set loading for the current slide
    if (index === this.currentSlideIndex) {
      this.isLoadingPreview = true;
    }

    const fileToUse = this.file;
    
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
loadSlide(index: number) {
  if (index < 0 || index >= this.totalSlides) return;

  this.currentSlideIndex = index;

  // Check if already preloaded
  if (this.preloadedSlides.has(index)) {
      this.previewImageBlob = this.preloadedSlides.get(index)!;
      this.isLoadingPreview = false;
      
      // Preload next slides in background
      this.triggerPreload(index);
      return;
  }

  // CASE 1: NEW UPLOAD - PDF (client-side) - Load on demand
  if (this.file && this.contentFormData.contentType === 'PDF') {
      // Only show loading if page isn't already being rendered
      if (!this.preloadQueue.includes(index)) {
          this.isLoadingPreview = true;
      }
      
      this.renderLocalPDFPage(index).then(() => {
          // After loading current page, preload next few in background
          this.triggerPreload(index);
      });
      return;
  }
}

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

   onPreviewError(event: any) {
      console.log('Preview error:', event);
      // Show fallback image or message
      event.target.src = 'assets/images/no-preview.png';
      event.target.alt = 'Preview not available';
    }

triggerPreload(currentIndex: number) {
  console.log('Triggering preload from index:', currentIndex);
  
  // For new uploads (client-side PDF)
  if (this.file && this.contentFormData.contentType === 'PDF') {
    // Preload next 3 slides only (to match your initial preload)
    for (let i = 1; i <= 3; i++) { // Changed from 5 to 3
        const nextIndex = currentIndex + i;
        if (nextIndex < this.totalSlides && !this.preloadedSlides.has(nextIndex) && !this.preloadQueue.includes(nextIndex)) {
            console.log('Preloading slide:', nextIndex);
            // Don't await - let it load in background
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

toggleFullscreen() {
    this.isFullscreen = !this.isFullscreen;
    const modalElement = document.querySelector('.preview-modal .modal-content');
    
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

async parsePDFFile(file: File): Promise<void> {
  try {
    // Clear previous data but KEEP the file reference
    this.isLoadingPreview = true;
    this.clearAllPreviewData();
    
    this.file = file;
    
    const arrayBuffer = await file.arrayBuffer();
    const loadingTask = pdfjsLib.getDocument({ data: arrayBuffer });
    const pdf = await loadingTask.promise;
    
    this.totalSlides = pdf.numPages;
    this.slides = new Array(this.totalSlides);
    
    // Load the first page - AWAIT this completely
    await this.renderLocalPDFPage(0);
    
    // Now preload next pages ONE BY ONE with delay
    const preloadNextPages = async () => {
      for (let i = 1; i <= 3; i++) { // Reduced to 3 for better performance
        if (i < this.totalSlides) {
          // Wait a bit between each preload
          await new Promise(resolve => setTimeout(resolve, 200));
          this.renderLocalPDFPage(i).catch(err => 
            console.error(`Failed to preload slide ${i}:`, err)
          );
        }
      }
    };
    
    // Start preloading in background
    preloadNextPages();
    
    this.isLoadingPreview = false;
    console.log("Parse pdffile: ", this.file);
    return Promise.resolve();
    
  } catch (error) {
    console.error('Error parsing PDF file:', error);
    this.isLoadingPreview = false;
    this.openAlertMod(this.alertTemplate, 'Failed to parse PDF file. Please try again.', 'error');
    return Promise.reject(error);
  }
}

downloadResponsesToExcel(): void {
  if (!this.allTrainingResponse || this.allTrainingResponse.length === 0) {
    this.openAlertMod(this.alertTemplate, 'No data available to download', 'warning');
    return;
  }

  try {
    // Create a new workbook
    const workbook = new ExcelJS.Workbook();
    workbook.creator = this.currentUser?.name || 'System';
    workbook.created = new Date();
    workbook.modified = new Date();

    // Add a worksheet
    const worksheet = workbook.addWorksheet('Training Responses', {
      properties: { tabColor: { argb: 'FF2E75B6' } },
      pageSetup: { paperSize: 9, orientation: 'landscape' }
    });

    // Define columns based on your data structure
    // Adjust these column definitions based on your actual response data structure
    const columns = [
      { header: 'S.No', key: 'sno', width: 8 },
      { header: 'Training Name', key: 'trainingName', width: 8 },
      { header: 'Employee ID', key: 'empId', width: 8 },
      { header: 'Employee Name', key: 'empName', width: 25 },
      { header: 'Last Completed Cycle', key: 'lastCompletedCycleNumber', width: 18 },
      { header: 'Last Completed On', key: 'lastCompletedOn', width: 18 },
      { header: 'Consent Given', key: 'consentGiven', width: 15 },
      { header: 'Status', key: 'status', width: 15 },
    ];

    worksheet.columns = columns;

    // Style the header row
    const headerRow = worksheet.getRow(1);
    headerRow.font = { bold: true, color: { argb: 'FFFFFFFF' }, size: 12 };
    headerRow.fill = {
      type: 'pattern',
      pattern: 'solid',
      fgColor: { argb: 'FF2E75B6' }
    };
    headerRow.alignment = { vertical: 'middle', horizontal: 'center' };
    headerRow.height = 25;

    // Add data rows
    this.allTrainingResponse.forEach((response: any, index: number) => {
      // Map your response object to row data
      // Adjust this mapping based on your actual response structure
      const rowData = {
        sno: index + 1,
        trainingName: response.trainingName || '',
        empId: response.empId || '',
        empName: response.empName || response.employeeName || '',
        lastCompletedCycleNumber: response.lastCompletedCycleNumber || '',
        lastCompletedOn: response.lastCompletedOn 
          ? this.date.transform(response.lastCompletedOn, 'dd-MMM-yyyy') 
          : '',
        consentGiven: response.consentGiven?.toLowerCase() == 'true' ? 'Yes' : 'No',
        status: 'Completed',
      };

      const row = worksheet.addRow(rowData);

      // Style data rows
      row.eachCell({ includeEmpty: true }, (cell, colNumber) => {
        cell.alignment = { 
          vertical: 'middle', 
          horizontal: colNumber === 1 ? 'center' : 'left',
          wrapText: true
        };
        cell.font = { size: 11 };
        
        // Add borders
        cell.border = {
          top: { style: 'thin', color: { argb: 'FFD3D3D3' } },
          left: { style: 'thin', color: { argb: 'FFD3D3D3' } },
          bottom: { style: 'thin', color: { argb: 'FFD3D3D3' } },
          right: { style: 'thin', color: { argb: 'FFD3D3D3' } }
        };
      });

      // Color code based on status
      if (rowData.status?.toLowerCase() === 'completed') {
        row.getCell('status').fill = {
          type: 'pattern',
          pattern: 'solid',
          fgColor: { argb: 'FFC6EFCE' } // Light green
        };
      } else if (rowData.status?.toLowerCase() === 'incomplete' || rowData.status?.toLowerCase() === 'pending') {
        row.getCell('status').fill = {
          type: 'pattern',
          pattern: 'solid',
          fgColor: { argb: 'FFFFC7CE' } // Light red
        };
      } else if (rowData.status?.toLowerCase() === 'in progress') {
        row.getCell('status').fill = {
          type: 'pattern',
          pattern: 'solid',
          fgColor: { argb: 'FFFFEB9C' } // Light yellow
        };
      }
    });

    // Auto-filter
    worksheet.autoFilter = {
      from: { row: 1, column: 1 },
      to: { row: 1, column: columns.length }
    };

    // Add summary row at the bottom (optional)
    const summaryRow = worksheet.addRow({
      sno: 'Total',
      empId: `${this.allTrainingResponse.length} Responses`
    });
    summaryRow.font = { bold: true };
    summaryRow.getCell('sno').alignment = { horizontal: 'right' };
    summaryRow.getCell('empId').alignment = { horizontal: 'left' };

    // Merge cells for summary if needed
    worksheet.mergeCells(`A${worksheet.rowCount}:B${worksheet.rowCount}`);

    // Generate Excel file
    workbook.xlsx.writeBuffer().then((buffer) => {
      const blob = new Blob([buffer], { 
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
      });
      
      // Generate filename with training name and timestamp
      const trainingName = this.selectedTraining?.trainingName || 'Training';
      const timestamp = this.date.transform(new Date(), 'ddMMMyyyy_HHmm') || new Date().getTime().toString();
      const fileName = `${trainingName}_Responses_${timestamp}.xlsx`;
      
      saveAs(blob, fileName);
      
      // this.openAlertMod(this.alertTemplate, 'Excel file downloaded successfully', 'success');
    }).catch((error) => {
      console.error('Error generating Excel file:', error);
      this.openAlertMod(this.alertTemplate, 'Failed to generate Excel file', 'error');
    });

  } catch (error) {
    console.error('Error in downloadResponsesToExcel:', error);
    this.openAlertMod(this.alertTemplate, 'Failed to download Excel file', 'error');
  }
}

// Add this to your component class
getCompletedCount(): number {
  return this.allTrainingResponse.filter(r => 
    r.lastCompletedCycleNumber && r.lastCompletedCycleNumber > 0
  ).length;
}

getPendingCount(): number {
  return this.allTrainingResponse.filter(r => 
    !r.lastCompletedCycleNumber || r.lastCompletedCycleNumber === 0
  ).length;
}

Math = Math;

// In your component
// In your component
onImageClick(event: MouseEvent) {
    const imageElement = event.target as HTMLImageElement;
    const clickX = event.offsetX; 
    const imageWidth = imageElement.clientWidth;
    
    if (clickX < imageWidth / 2) {
        this.previousSlide();
    } else {
        this.nextSlide();
    }
}

}
