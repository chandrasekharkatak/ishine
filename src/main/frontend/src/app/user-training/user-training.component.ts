import { Component, OnInit, OnDestroy, TemplateRef, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { User } from '../models/user';
import { AuthenticationService } from '../services/authentication.service';
import { TrainingService } from '../services/training.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import * as moment from 'moment';
import * as JSZip from 'jszip';

@Component({
  standalone: false,
  selector: 'app-user-training',
  templateUrl: './user-training.component.html',
  styleUrls: ['./user-training.component.css']
})
export class UserTrainingComponent implements OnInit, OnDestroy {

  currentUser: User;
  allTrainings: any[] = [];
  loading: boolean = false;
  
  // Currently viewing training
  viewingTraining: any = null;
  isViewingTraining: boolean = false;
  
  // Content viewing
  previewUrl: string = '';
  safePreviewUrl: SafeResourceUrl | null = null;
  contentType: string = '';
  isExternalLink: boolean = false;
  file: File | null = null;
  fileSize: number = 0;
  pptxSlides: any[] = [];
  currentSlideIndex: number = 0;
  showThumbnails: boolean = false;
  
  // Timer for deadline-crossed trainings
  elapsedTime: number = 0;
  elapsedTimeDisplay: string = '00:00';
  timerInterval: any;
  minTimeReached: boolean = false;
  consentButtonEnabled: boolean = false;
  hasVisitedLink: boolean = false;
  
  // Auto-opening deadline-crossed trainings
  deadlineCrossedTrainings: any[] = [];
  currentDeadlineCrossedIndex: number = -1;
  isAutoOpening: boolean = false;
  
  // Modal
  modalRef: NgbModalRef;
  @ViewChild('training_view_modal') trainingViewModalTemplate: TemplateRef<any>;
  @ViewChild('alert_message') alertTemplate: TemplateRef<any>;
  
  alertMessage: string = '';
  alertType: string = 'info';

  constructor(
    private authenticationService: AuthenticationService,
    private trainingService: TrainingService,
    private router: Router,
    private modalService: NgbModal,
    private sanitizer: DomSanitizer
  ) {
    this.authenticationService.currentUser.subscribe(x => {
      this.currentUser = x;
      if (x && x.empId) {
        this.loadUserTrainings();
      }
    });
  }

  ngOnInit(): void {
    this.loadUserTrainings();
  }

  ngOnDestroy(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
    this.closeTrainingView();
  }

  loadUserTrainings() {
    if (!this.currentUser || !this.currentUser.empId) {
      return;
    }

    this.loading = true;
    this.trainingService.getUserTrainings(this.currentUser.empId).pipe(first()).subscribe((response: any) => {
      this.loading = false;
      if (response.serviceStatus === 'Success' && response.serviceResponse) {
        this.allTrainings = response.serviceResponse || [];
        
        // Separate deadline-crossed trainings
        this.deadlineCrossedTrainings = this.allTrainings.filter(t => 
          t.isDeadlineCrossed === true && t.status === 'PENDING' && t.lockEnabled === true
        );
        
        // Auto-open first deadline-crossed training if any
        if (this.deadlineCrossedTrainings.length > 0) {
          this.currentDeadlineCrossedIndex = 0;
          this.isAutoOpening = true;
          this.viewTraining(this.deadlineCrossedTrainings[0], true);
        }
      } else {
        this.allTrainings = [];
      }
    }, error => {
      this.loading = false;
      console.error('Error loading user trainings:', error);
      this.openAlert('Error loading trainings: ' + error.message, 'error');
    });
  }

  viewTraining(training: any, isDeadlineCrossed: boolean = false) {
    if (!training || !training.content) {
      this.openAlert('Training content not available', 'warning');
      return;
    }

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
    this.fileSize = 0;
    this.pptxSlides = [];
    this.currentSlideIndex = 0;
    
    const content = training.content;
    this.contentType = content.contentType;
    
    // For completed trainings: no timer, no lock
    if (training.status === 'COMPLETED') {
      this.minTimeReached = true;
      this.consentButtonEnabled = false; // No consent needed for completed
    } else {
      // For pending trainings: check if timer needed
      if (training.minViewTimeMinutes && training.minViewTimeMinutes > 0) {
        this.startTimer(training.minViewTimeMinutes);
      } else {
        this.minTimeReached = true;
        if (training.consentRequired === 'true') {
          this.consentButtonEnabled = true;
        }
      }
    }
    
    // Setup content
    if (content.contentType === 'LINK') {
      this.isExternalLink = true;
      this.previewUrl = content.externalLinkUrl;
      this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl);
    } else {
      this.isExternalLink = false;
      // Download content for preview
      this.trainingService.downloadContent(content.contentId).pipe(first()).subscribe({
        next: (resp: any) => {
          const blob: Blob = resp.body;
          if (!blob || blob.size === 0) {
            this.openAlert('File is empty or could not be loaded', 'error');
            return;
          }

          const contentType = resp.headers.get('Content-Type') || 'application/octet-stream';
          let fileName = content.contentName || 'content';
          
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
          if (content.contentPath && !fileName.includes('.')) {
            const pathParts = content.contentPath.split('.');
            if (pathParts.length > 1) {
              fileName += '.' + pathParts[pathParts.length - 1];
            }
          }

          this.previewUrl = URL.createObjectURL(blob);
          this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl);
          this.file = new File([blob], fileName, { type: contentType });
          this.fileSize = blob.size / 1024 / 1024;

          // Parse PPTX if needed
          if (content.contentType === 'PPT' && fileName.toLowerCase().endsWith('.pptx')) {
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
        }
      });
      return; // Don't open modal yet, wait for content download
    }
    
    this.openTrainingViewModal();
  }

  startTimer(minViewTimeMinutes: number) {
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
        this.minTimeReached = true;
        if (this.viewingTraining.consentRequired === 'true') {
          this.consentButtonEnabled = true;
        }
        clearInterval(this.timerInterval);
      }
    }, 1000);
  }

  async parsePPTXFile(file: File) {
    try {
      const zip = await JSZip.loadAsync(file);
      const slideFiles = Object.keys(zip.files)
        .filter(path => path.startsWith('ppt/slides/slide') && path.endsWith('.xml'))
        .map(path => ({ path, file: zip.files[path] }))
        .sort((a, b) => {
          const aNum = parseInt(a.path.match(/slide(\d+)/)?.[1] || '0');
          const bNum = parseInt(b.path.match(/slide(\d+)/)?.[1] || '0');
          return aNum - bNum;
        });

      this.pptxSlides = [];

      for (let i = 0; i < slideFiles.length; i++) {
        const slideFile = slideFiles[i];
        const slideXml = await slideFile.file.async('string');

        const parser = new DOMParser();
        const xmlDoc = parser.parseFromString(slideXml, 'text/xml');

        const slideNum = slideFile.path.match(/slide(\d+)/)?.[1] || (i + 1).toString();
        const relsPath = `ppt/slides/_rels/slide${slideNum}.xml.rels`;

        const relationshipMap: Map<string, string> = new Map();
        try {
          if (zip.file(relsPath)) {
            const relsXml = await zip.file(relsPath)!.async('string');
            const relsDoc = parser.parseFromString(relsXml, 'text/xml');
            const relationships = relsDoc.getElementsByTagName('Relationship');

            for (let r = 0; r < relationships.length; r++) {
              const rel = relationships[r];
              const id = rel.getAttribute('Id');
              const target = rel.getAttribute('Target');
              const type = rel.getAttribute('Type');

              if (id && target && type && type.includes('image')) {
                relationshipMap.set(id, target);
              }
            }
          }
        } catch (relsError) {
          console.warn('Could not parse relationship file:', relsPath, relsError);
        }

        const slideImages: string[] = [];
        const imageElements = xmlDoc.getElementsByTagName('a:blip');

        for (let j = 0; j < imageElements.length; j++) {
          const embedId = imageElements[j].getAttribute('r:embed');
          if (embedId) {
            let imagePath = relationshipMap.get(embedId);

            if (!imagePath) {
              const possiblePaths = [
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
                const imageBlob = await imageFile.async('blob');
                const imageUrl = URL.createObjectURL(imageBlob);
                slideImages.push(imageUrl);
              }
            }
          }
        }

        const slideTexts: string[] = [];
        const textElements = xmlDoc.getElementsByTagName('a:t');
        for (let t = 0; t < textElements.length; t++) {
          const text = textElements[t].textContent?.trim();
          if (text) {
            slideTexts.push(text);
          }
        }

        this.pptxSlides.push({
          slideNumber: i + 1,
          images: slideImages,
          texts: slideTexts
        });
      }

      this.currentSlideIndex = 0;
    } catch (error) {
      console.error('Error parsing PPTX:', error);
      this.openAlert('Error parsing PowerPoint file', 'error');
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
    const element = document.documentElement;
    if (!document.fullscreenElement) {
      element.requestFullscreen().catch(err => {
        console.error('Error entering fullscreen:', err);
      });
    } else {
      document.exitFullscreen();
    }
  }

  toggleThumbnails() {
    this.showThumbnails = !this.showThumbnails;
  }

  onExternalLinkClick() {
    if (this.isExternalLink && this.previewUrl) {
      window.open(this.previewUrl, '_blank');
      this.hasVisitedLink = true;
      
      if (this.viewingTraining.consentRequired === 'true') {
        this.consentButtonEnabled = true;
      }
    }
  }

  onSubmitConsent() {
    if (!this.viewingTraining || !this.viewingTraining.content) {
      return;
    }

    // Validate minimum time if required (only for pending trainings)
    if (this.viewingTraining.status === 'PENDING') {
      if (this.viewingTraining.minViewTimeMinutes && this.viewingTraining.minViewTimeMinutes > 0) {
        if (!this.minTimeReached) {
          this.openAlert(`Please view the training for at least ${this.viewingTraining.minViewTimeMinutes} minutes before submitting consent.`, 'warning');
          return;
        }
      }

      // Validate external link visit
      if (this.isExternalLink && !this.hasVisitedLink) {
        this.openAlert('Please click on the external link to visit the training content before submitting consent.', 'warning');
        return;
      }
    }

    const consentData = {
      trainingId: this.viewingTraining.trainingId,
      contentId: this.viewingTraining.content.contentId,
      empId: this.currentUser.empId,
      completionCycleNumber: this.viewingTraining.currentCycleNumber
    };

    this.trainingService.submitConsent(consentData).pipe(first()).subscribe((response: any) => {
      if (response.serviceStatus === 'Success') {
        this.openAlert('Training completed successfully!', 'success');
        
        // Stop timer
        if (this.timerInterval) {
          clearInterval(this.timerInterval);
        }
        
        // Close current training view
        this.closeTrainingView();
        
        // Move to next deadline-crossed training if auto-opening
        if (this.isAutoOpening && this.currentDeadlineCrossedIndex < this.deadlineCrossedTrainings.length - 1) {
          this.currentDeadlineCrossedIndex++;
          setTimeout(() => {
            this.viewTraining(this.deadlineCrossedTrainings[this.currentDeadlineCrossedIndex], true);
          }, 1000);
        } else {
          this.isAutoOpening = false;
        }
        
        // Reload trainings
        setTimeout(() => {
          this.loadUserTrainings();
        }, 1000);
      } else {
        this.openAlert(response.serviceResponse || 'Failed to submit consent', 'error');
      }
    }, error => {
      this.openAlert('Error submitting consent: ' + error.message, 'error');
    });
  }

  onSkipTraining() {
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
      cycleNumber: this.viewingTraining.currentCycleNumber
    };

    this.trainingService.skipTraining(skipData).pipe(first()).subscribe((response: any) => {
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
      } else {
        this.openAlert(response.serviceResponse || 'Failed to skip training', 'error');
      }
    }, error => {
      this.openAlert('Error skipping training: ' + error.message, 'error');
    });
  }

  openTrainingViewModal() {
    if (this.trainingViewModalTemplate) {
      this.modalRef = this.modalService.open(this.trainingViewModalTemplate, {
        size: 'xl',
        centered: true,
        backdrop: this.isAutoOpening ? 'static' : true,
        keyboard: !this.isAutoOpening
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
    
    // Revoke PPTX slide image URLs
    if (this.pptxSlides && this.pptxSlides.length > 0) {
      this.pptxSlides.forEach(slide => {
        if (slide.images) {
          slide.images.forEach((imgUrl: string) => {
            if (imgUrl.startsWith('blob:')) {
              URL.revokeObjectURL(imgUrl);
            }
          });
        }
      });
    }
    
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
    this.hasVisitedLink = false;
  }

  openAlert(message: string, type: string = 'info') {
    this.alertMessage = message;
    this.alertType = type;
    this.modalRef = this.modalService.open(this.alertTemplate, {
      backdrop: false,
      windowClass: 'alert-toast-modal',
      modalDialogClass: 'alert-toast-dialog',
      size: 'sm'
    });
    
    if (type === 'success') {
      setTimeout(() => {
        if (this.modalRef) {
          this.modalRef.close();
        }
      }, 3000);
    }
  }

  getProgressPercentage(): number {
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
    return training.skipAllowed === 'true' && 
           !training.isDeadlineCrossed && 
           training.status === 'PENDING';
  }

  downloadFile() {
    if (this.previewUrl) {
      window.open(this.previewUrl, '_blank');
    }
  }
}
