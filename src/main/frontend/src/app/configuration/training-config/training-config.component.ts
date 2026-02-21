import { Component, OnInit, OnDestroy, TemplateRef, ViewChild } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { LocationStrategy } from '@angular/common';
import { Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { TrainingService } from 'src/app/services/training.service';
import * as moment from 'moment';
import * as JSZip from 'jszip';

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
    skipAllowed: 'true',
    deadlineEnabled: 'true', // Mandatory: deadline always enabled
    deadlinePattern: 'YEARLY', // Default: once per year (last day of year)
    customDeadlineMonths: '',
    activeStatus: 'true'
  };

  contentFormData: any = {
    contentType: 'PPT',
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
  trainingTypes: string[] = ['Induction', 'POSH', 'CyberSecurity', 'Compliance', 'Safety'];

  // Deadline patterns
  deadlinePatterns: any[] = [
    { value: 'YEARLY', label: 'Yearly (December 31 - Last Day of Year)' },
    { value: 'MID_YEAR', label: 'Mid Year (June, December)' },
    // { value: 'YEAR_END', label: 'Year End (June, December)' },
    { value: 'QUARTERLY', label: 'Quarterly (March, June, September, December)' }
    // { value: 'CUSTOM', label: 'Custom Months' }
  ];

  contentTypes: any[] = [
    { value: 'PPT', label: 'PowerPoint' },
    { value: 'PDF', label: 'PDF Document' },
    { value: 'VIDEO', label: 'Video' },
    { value: 'AUDIO', label: 'Audio' },
    { value: 'LINK', label: 'External Link' }
  ];

  constructor(
    private authenticationService: AuthenticationService,
    private locationStrategy: LocationStrategy,
    private modalService: NgbModal,
    private trainingService: TrainingService,
    private sanitizer: DomSanitizer,
    private router: Router
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
    this.maxFileSize = 20;
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
      skipAllowed: 'true',
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
      contentType: 'PPT',
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
    this.trainingService.getAllTrainings().pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.trainings = response.serviceResponse || [];
      } else {
        this.openAlertMod(this.alertTemplate, 'Failed to load trainings', 'error');
      }
    }, error => {
      this.openAlertMod(this.alertTemplate, 'Error loading trainings: ' + error.message, 'error');
    });
  }

  getTrainingContent(trainingId: number) {
    this.trainingService.getTrainingContent(trainingId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
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
      } else {
        this.openAlertMod(this.alertTemplate, 'Failed to load training content', 'error');
      }
    }, error => {
      this.openAlertMod(this.alertTemplate, 'Error loading training content: ' + error.message, 'error');
    });
  }

  populateContentForm(content: any) {
    if (!content) {
      return;
    }
    
    this.contentFormData = {
      contentType: content.contentType || 'PPT',
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
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          const blob = response.body;
          const contentDisposition = response.headers.get('content-disposition');
          let fileName = this.contentFormData.existingFileName || 'content';
          
          if (contentDisposition) {
            const fileNameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
            if (fileNameMatch && fileNameMatch[1]) {
              fileName = fileNameMatch[1].replace(/['"]/g, '');
            }
          }
          
          // Create blob URL and open in new tab
          const url = window.URL.createObjectURL(blob);
          window.open(url, '_blank');
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
      consentRequired: this.trainingFormData.consentRequired || 'true',
      skipAllowed: this.trainingFormData.skipAllowed || 'true',
      deadlineEnabled: this.trainingFormData.deadlineEnabled || 'false',
      deadlinePattern: this.trainingFormData.deadlinePattern || null,
      customDeadlineMonths: this.trainingFormData.customDeadlineMonths || null,
      createdBy: this.currentUser.empId
    };

    // Content DTO as JSON string
    const contentDTO = {
      contentType: this.contentFormData.contentType,
      contentName: this.contentFormData.contentName,
      effectiveFrom: this.contentFormData.effectiveFrom ? moment(this.contentFormData.effectiveFrom).format('YYYY-MM-DD') : null,
      effectiveTo: this.contentFormData.effectiveTo ? moment(this.contentFormData.effectiveTo).format('YYYY-MM-DD') : null,
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
    this.trainingService.createTrainingWithContent(formData).pipe(first()).
    subscribe({
    next: (response: any) => {
      console.log("success===> ",response)
      if (response.serviceStatus === 'Success') {
        this.openAlertMod(this.alertTemplate, 
          'Training and content created successfully', 
          'success');
        this.resetContentForm();
        this.resetTrainingForm();
        this.showTable();
      } else {
        this.openAlertMod(this.alertTemplate, 
          response.serviceStatus || 'Failed to create training', 
          'error');
      }
    },
    error: (error: any) => {
      console.log("HTTP error => ", error);

      const backendMessage =
        error?.error?.serviceStatus ||
        error?.error?.serviceResponse ||
        error?.message ||
        'Something went wrong';
         console.log("backendMessage==>  ",backendMessage)
      this.openAlertMod(this.alertTemplate,
        backendMessage,
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
        consentRequired: this.trainingFormData.consentRequired || 'true',
        skipAllowed: this.trainingFormData.skipAllowed || 'true',
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
      this.trainingService.updateTrainingWithContent(formData).pipe(first()).subscribe((response: any) => {
        if (response.serviceStatus === 'Success') {
          this.openAlertMod(this.alertTemplate, 'Training and content updated successfully', 'success');
          // Reload content list after update
          if (this.trainingFormData.trainingId) {
            this.getTrainingContent(this.trainingFormData.trainingId);
          }
          this.resetContentForm();
          this.showTable();
        } else {
          this.openAlertMod(this.alertTemplate, response.serviceResponse || 'Failed to update training', 'error');
        }
      }, error => {
        console.log("error=> ", error);
        this.openAlertMod(this.alertTemplate, 'Error updating training: ' + (error.error?.message || error.message), 'error');
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
  onViewContent(training: any) {

    // Step 1: Load training content
    this.trainingService.getTrainingContent(training.trainingId)
      .pipe(first())
      .subscribe((response: any) => {

        if (response.serviceStatus !== 'Success') {
          this.openAlertMod(this.alertTemplate, 'Failed to load training content', 'error');
          return;
        }

        const contents = response.serviceResponse || [];

        if (contents.length === 0) {
          this.openAlertMod(this.alertTemplate, 'No content available for this training', 'info');
          return;
        }

        // Step 2: Pick active content (unchanged logic)
        const activeContent =
          contents.find((c: any) => c.activeStatus === 'true') || contents[0];

        this.viewingContent = activeContent;
        this.isViewingExistingContent = true;

        // Step 3: Populate content form (unchanged)
        this.contentFormData = {
          contentType: activeContent.contentType,
          contentName: activeContent.contentName,
          effectiveFrom: activeContent.effectiveFrom,
          effectiveTo: activeContent.effectiveTo,
          externalLinkUrl: activeContent.externalLinkUrl || ''
        };

        // Step 4: LINK content handling (unchanged)
        if (activeContent.contentType === 'LINK') {
          this.previewUrl = activeContent.externalLinkUrl;
          this.safePreviewUrl = this.previewUrl ? this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl) : null;
          this.showPreview = true;
          this.openPreviewModal();
          return;
        }

        // Step 5: FILE content handling (blob download)
        this.trainingService.downloadContent(activeContent.contentId)
          .pipe(first())
          .subscribe({

            next: (resp: any) => {

              const blob: Blob = resp.body;
              const contentType =
                resp.headers.get('Content-Type') || 'application/octet-stream';

              // Extract filename from header
              let fileName = activeContent.contentName || 'content';
              const disposition = resp.headers.get('Content-Disposition');

              if (disposition) {
                const match = disposition.match(/filename="(.+)"/);
                if (match && match[1]) {
                  fileName = match[1];
                }
              }

              // Create preview URL
              this.previewUrl = URL.createObjectURL(
                new Blob([blob], { type: contentType })
              );
              this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl);
              this.showPreview = true;

              // PPT parsing logic (unchanged)
              if (
                activeContent.contentType === 'PPT' &&
                activeContent.contentPath &&
                activeContent.contentPath.toLowerCase().endsWith('.pptx')
              ) {
                const file = new File([blob], fileName, { type: contentType });
                this.file = file;
                this.fileSize = blob.size / 1024 / 1024;
                this.parsePPTXFile(file);
              } else {
                this.file = new File([blob], fileName, { type: contentType });
                this.fileSize = blob.size / 1024 / 1024;
              }

              this.openPreviewModal();
            },

            error: (error) => {
              // Blob-safe error handling
              if (error?.error instanceof Blob) {
                error.error.text().then((text: string) => {
                  this.openAlertMod(
                    this.alertTemplate,
                    text || 'Error loading content',
                    'error'
                  );
                });
              } else {
                this.openAlertMod(
                  this.alertTemplate,
                  'Error loading content',
                  'error'
                );
              }
            }
          });

      }, error => {
        this.openAlertMod(
          this.alertTemplate,
          'Error loading training content',
          'error'
        );
      });
  }

  onViewConten1t(training: any) {
    // Load training content first
    this.trainingService.getTrainingContent(training.trainingId).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        const contents = response.serviceResponse || [];
        if (contents.length === 0) {
          this.openAlertMod(this.alertTemplate, 'No content available for this training', 'info');
          return;
        }

        // Find active content (prefer active, otherwise use first one)
        const activeContent = contents.find((c: any) => c.activeStatus === 'true') || contents[0];
        this.viewingContent = activeContent;
        this.isViewingExistingContent = true;

        // Set content form data for preview
        this.contentFormData = {
          contentType: activeContent.contentType,
          contentName: activeContent.contentName,
          effectiveFrom: activeContent.effectiveFrom,
          effectiveTo: activeContent.effectiveTo,
          externalLinkUrl: activeContent.externalLinkUrl || ''
        };

        // Handle different content types
        if (activeContent.contentType === 'LINK') {
          // For links, use the URL directly
          this.previewUrl = activeContent.externalLinkUrl;
          this.safePreviewUrl = this.previewUrl ? this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl) : null;
          this.showPreview = true;
          this.openPreviewModal();
        } else {
          // For files, download and create blob URL
          this.trainingService.downloadContent(activeContent.contentId).pipe(first()).subscribe({
            next: (resp: any) => {
              // With observe: 'response', resp.body contains the blob
              const blob: Blob = resp.body;

              if (!blob || blob.size === 0) {
                this.openAlertMod(this.alertTemplate, 'File is empty or could not be loaded', 'error');
                return;
              }

              const contentType = resp.headers.get('Content-Type') || 'application/octet-stream';
              let fileName = activeContent.contentName || 'content';

              // Extract filename from header
              const disposition = resp.headers.get('Content-Disposition');
              if (disposition) {
                const patterns = [
                  /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/,
                  /filename="([^"]+)"/,
                  /filename=([^;]+)/
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
              if (activeContent.contentPath && !fileName.includes('.')) {
                const pathParts = activeContent.contentPath.split('.');
                if (pathParts.length > 1) {
                  fileName += '.' + pathParts[pathParts.length - 1];
                }
              }

              this.previewUrl = URL.createObjectURL(blob);
              this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl);
              this.showPreview = true;

              // For PPTX files, parse them
              if (activeContent.contentType === 'PPT' && activeContent.contentPath && activeContent.contentPath.toLowerCase().endsWith('.pptx')) {
                const file = new File([blob], fileName, { type: contentType });
                this.file = file;
                this.fileSize = blob.size / 1024 / 1024;
                this.parsePPTXFile(file);
              } else {
                this.file = new File([blob], fileName, { type: contentType });
                this.fileSize = blob.size / 1024 / 1024;
              }

              this.openPreviewModal();
            },
            error: (error: any) => {
              console.error('Download error:', error);
              if (error?.error instanceof Blob) {
                error.error.text().then((text: string) => {
                  try {
                    const errorObj = JSON.parse(text);
                    this.openAlertMod(this.alertTemplate, errorObj.serviceResponse || 'Error loading content', 'error');
                  } catch {
                    this.openAlertMod(this.alertTemplate, text || 'Error loading content', 'error');
                  }
                }).catch(() => {
                  this.openAlertMod(this.alertTemplate, 'Error loading content', 'error');
                });
              } else {
                const errorMsg = error?.error?.message || error?.message || 'Error loading content';
                this.openAlertMod(this.alertTemplate, errorMsg, 'error');
              }
            }
          });
        }
      } else {
        this.openAlertMod(this.alertTemplate, 'Failed to load training content', 'error');
      }
    }, error => {
      this.openAlertMod(this.alertTemplate, 'Error loading training content: ' + error.message, 'error');
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
        this.trainingService.deactivateTraining(training.trainingId, this.currentUser.empId).pipe(first()).subscribe((response: any) => {
          if (response.serviceStatus === 'Success') {
            this.openAlertMod(this.alertTemplate, 'Training deactivated successfully', 'success');
            this.getAllTrainings();
          } else {
            this.openAlertMod(this.alertTemplate, response.serviceResponse || 'Failed to deactivate training', 'error');
          }
        }, error => {
          this.openAlertMod(this.alertTemplate, 'Error deactivating training: ' + error.message, 'error');
        });
      }
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
     
         // Create blob URL
         this.previewUrl = URL.createObjectURL(this.file);
     
         // SANITIZE HERE (KEY FIX)
         this.safePreviewUrl =
           this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl);
     
         this.showPreview = true;
     
         if (this.contentFormData.contentType === 'PPT') {
           if (this.file.name.toLowerCase().endsWith('.pptx')) {
             this.parsePPTXFile(this.file);
           } else {
             this.pptxSlides = [];
           }
         }
     
         if (this.contentFormData.contentType === 'PDF') {
           this.pptxSlides = [];
         }
       }
     
  // async parsePPTXFile(file: File) {
  //   try {
  //     this.isLoadingPPTX = true;
  //     this.pptxSlides = [];
  //     this.currentSlideIndex = 0;

  //     // Read the file as array buffer
  //     const arrayBuffer = await file.arrayBuffer();
  //     const zip = await JSZip.loadAsync(arrayBuffer);

  //     // Get slide files (ppt/slides/slide1.xml, slide2.xml, etc.)
  //     const slideFiles: any[] = [];
  //     zip.forEach((relativePath, file) => {
  //       if (relativePath.startsWith('ppt/slides/slide') && relativePath.endsWith('.xml') && !relativePath.includes('_rels')) {
  //         slideFiles.push({ path: relativePath, file: file });
  //       }
  //     });

  //     // Sort slides by number
  //     slideFiles.sort((a, b) => {
  //       const numA = parseInt(a.path.match(/slide(\d+)/)?.[1] || '0');
  //       const numB = parseInt(b.path.match(/slide(\d+)/)?.[1] || '0');
  //       return numA - numB;
  //     });

  //     // Extract images and text from slides
  //     for (let i = 0; i < slideFiles.length; i++) {
  //       const slideFile = slideFiles[i];
  //       const slideXml = await slideFile.file.async('string');

  //       // Parse slide XML
  //       const parser = new DOMParser();
  //       const xmlDoc = parser.parseFromString(slideXml, 'text/xml');

  //       // Get slide number for relationship file lookup
  //       const slideNum = slideFile.path.match(/slide(\d+)/)?.[1] || (i + 1).toString();
  //       const relsPath = `ppt/slides/_rels/slide${slideNum}.xml.rels`;

  //       // Parse relationship file to map IDs to actual file paths
  //       const relationshipMap: Map<string, string> = new Map();
  //       try {
  //         const relsFile = zip.file(relsPath);
  //         if (relsFile) {
  //           const relsXml = await relsFile.async('string');
  //           const relsDoc = parser.parseFromString(relsXml, 'text/xml');
  //           const relationships = relsDoc.getElementsByTagName('Relationship');

  //           for (let r = 0; r < relationships.length; r++) {
  //             const rel = relationships[r];
  //             const id = rel.getAttribute('Id');
  //             const target = rel.getAttribute('Target');
  //             const type = rel.getAttribute('Type');

  //             // Map image relationships
  //             if (id && target && type && type.includes('image')) {
  //               // Resolve relative path
  //               let imagePath = target;
  //               if (target.startsWith('../')) {
  //                 imagePath = target.replace('../', 'ppt/');
  //               } else if (!target.startsWith('ppt/')) {
  //                 imagePath = `ppt/${target}`;
  //               }
  //               relationshipMap.set(id, imagePath);
  //             }
  //           }
  //         }
  //       } catch (relsError) {
  //         console.warn('Could not parse relationship file:', relsPath, relsError);
  //       }

  //       // Extract images using relationship mapping
  //       const slideImages: string[] = [];
  //       const imageElements = xmlDoc.getElementsByTagName('a:blip');

  //       for (let j = 0; j < imageElements.length; j++) {
  //         const embedId = imageElements[j].getAttribute('r:embed');
  //         if (embedId) {
  //           // Look up the actual image path from relationship map
  //           let imagePath = relationshipMap.get(embedId);

  //           // Fallback: try direct path if relationship map doesn't have it
  //           if (!imagePath) {
  //             // Try different possible paths
  //             const possiblePaths = [
  //               `ppt/media/${embedId}`,
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
  //               try {
  //                 const imageBlob = await imageFile.async('blob');
  //                 const imageUrl = URL.createObjectURL(imageBlob);
  //                 slideImages.push(imageUrl);
  //               } catch (imgError) {
  //                 console.warn('Error loading image:', imagePath, imgError);
  //               }
  //             }
  //           }
  //         }
  //       }

  //       // Extract text content - check multiple possible text element tags
  //       const slideTexts: string[] = [];

  //       // Method 1: Direct text elements (a:t)
  //       const textElements = xmlDoc.getElementsByTagName('a:t');
  //       for (let k = 0; k < textElements.length; k++) {
  //         const text = textElements[k].textContent;
  //         if (text && text.trim()) {
  //           slideTexts.push(text.trim());
  //         }
  //       }

  //       // Method 2: Text paragraphs (a:p)
  //       const paraElements = xmlDoc.getElementsByTagName('a:p');
  //       for (let p = 0; p < paraElements.length; p++) {
  //         const para = paraElements[p];
  //         const paraTexts = para.getElementsByTagName('a:t');
  //         let paraText = '';
  //         for (let pt = 0; pt < paraTexts.length; pt++) {
  //           const text = paraTexts[pt].textContent;
  //           if (text) {
  //             paraText += text;
  //           }
  //         }
  //         if (paraText.trim()) {
  //           // Avoid duplicates
  //           if (!slideTexts.includes(paraText.trim())) {
  //             slideTexts.push(paraText.trim());
  //           }
  //         }
  //       }

  //       // Method 3: Text runs (a:r)
  //       const runElements = xmlDoc.getElementsByTagName('a:r');
  //       for (let r = 0; r < runElements.length; r++) {
  //         const run = runElements[r];
  //         const runTexts = run.getElementsByTagName('a:t');
  //         let runText = '';
  //         for (let rt = 0; rt < runTexts.length; rt++) {
  //           const text = runTexts[rt].textContent;
  //           if (text) {
  //             runText += text;
  //           }
  //         }
  //         if (runText.trim()) {
  //           // Avoid duplicates
  //           if (!slideTexts.includes(runText.trim())) {
  //             slideTexts.push(runText.trim());
  //           }
  //         }
  //       }

  //       // If we have content (images or text), add the slide
  //       // Even if empty, we'll add it to show the slide structure
  //       this.pptxSlides.push({
  //         slideNumber: i + 1,
  //         images: slideImages,
  //         texts: slideTexts,
  //         hasContent: slideImages.length > 0 || slideTexts.length > 0
  //       });
  //     }

  //     this.isLoadingPPTX = false;
  //   } catch (error) {
  //     console.error('Error parsing PPTX file:', error);
  //     this.isLoadingPPTX = false;
  //     // Fallback: show download option
  //     this.pptxSlides = [];
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

  nextSlide() {
    if (this.currentSlideIndex < this.pptxSlides.length - 1) {
      this.currentSlideIndex++;
    }
  }

  previousSlide() {
    if (this.currentSlideIndex > 0) {
      this.currentSlideIndex--;
    }
  }

  goToSlide(index: number) {
    if (index >= 0 && index < this.pptxSlides.length) {
      this.currentSlideIndex = index;
    }
  }

  toggleFullscreen() {
    this.isFullscreen = !this.isFullscreen;
    const viewerElement = document.querySelector('.pptx-viewer-wrapper');
    if (viewerElement) {
      if (this.isFullscreen) {
        if ((viewerElement as any).requestFullscreen) {
          (viewerElement as any).requestFullscreen();
        } else if ((viewerElement as any).webkitRequestFullscreen) {
          (viewerElement as any).webkitRequestFullscreen();
        } else if ((viewerElement as any).mozRequestFullScreen) {
          (viewerElement as any).mozRequestFullScreen();
        }
      } else {
        if ((document as any).exitFullscreen) {
          (document as any).exitFullscreen();
        } else if ((document as any).webkitExitFullscreen) {
          (document as any).webkitExitFullscreen();
        } else if ((document as any).mozCancelFullScreen) {
          (document as any).mozCancelFullScreen();
        }
      }
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

    // For new content upload
    if (!this.file && !(this.contentFormData.contentType === 'LINK' && this.contentFormData.externalLinkUrl)) {
      this.openAlertMod(this.alertTemplate, 'Please select a file or enter a URL first', 'warning');
      return;
    }

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

  closePreviewModal() {
    if (this.previewModalRef) {
      this.previewModalRef.close();
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
}
