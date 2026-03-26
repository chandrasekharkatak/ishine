import { Component, Input, Output, EventEmitter, OnDestroy, TemplateRef, ViewChild, HostListener } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import * as pdfjsLib from 'pdfjs-dist';
import { GlobalWorkerOptions } from 'pdfjs-dist';
import { QuizViewModalComponent, QuizViewConfig } from './quiz-view-modal/quiz-view-modal.component';

GlobalWorkerOptions.workerSrc = `//cdnjs.cloudflare.com/ajax/libs/pdf.js/${pdfjsLib.version}/pdf.worker.min.js`;

@Component({
  standalone: false,
  selector: 'app-training-content-view',
  templateUrl: './training-content-view.component.html',
  styleUrls: ['./training-content-view.component.css'],
})
export class TrainingContentViewComponent implements OnDestroy {
  @ViewChild('quizSubmitRef') quizSubmitRef: QuizViewModalComponent;
  @Input() content: any; // Content data
  @Input() contentType: string;
  @Input() contentName: string;
  @Input() previewUrl: string;
  @Input() file: File | null = null;
  @Input() isAdminMode: boolean = false; // For admin/config view
  @Input() showTimer: boolean = false; // For employee training view
  @Input() minViewTimeMinutes: number = 0;
  @Input() status: string = ''; // PENDING, COMPLETED, etc.
  @Input() mandatoryFlag: string = 'false'; // 'true' or 'false'
  @Input() hasSeenContent: boolean = false;
  @Input() hasQuiz: boolean = false;
  @Input() quizAttempted: boolean = false;
  @Input() isAlreadySubmitted: boolean = false;
  @Input() lastCompletedOn: any = null;
  @Input() consentRequired: string = 'false';
  @Input() trainingId: number;
  @Input() cycleNumber: number;
  @Input() contentId: number;
  @Input() quizId: number;
  
  @Output() consentSubmitted = new EventEmitter<void>();
  @Output() quizClicked = new EventEmitter<void>();
  @Output() quizCompleted = new EventEmitter<boolean>();
  @Output() closed = new EventEmitter<void>();
  @Output() linkVisited = new EventEmitter<void>();

  @ViewChild('content_preview_modal') modalTemplate: TemplateRef<any>;

  modalRef: NgbModalRef;
  
  // Quiz submission state (local + from parent)
  get quizFinished(): boolean {
    return this.quizAttempted || this.isAlreadySubmitted || this.formSubmitted;
  }

  // Preview state
  safePreviewUrl: SafeResourceUrl | null = null;
  isExternalLink: boolean = false;
  
  // PDF state
  slides: string[] = [];
  totalSlides: number = 0;
  currentSlideIndex: number = 0;
  isLoadingPreview: boolean = false;
  previewImageBlob: string | null = null;
  preloadedSlides: Map<number, string> = new Map();
  preloadQueue: number[] = [];
  
  // Timer state
  elapsedTime: number = 0;
  elapsedTimeDisplay: string = '00:00';
  timerInterval: any;
  minTimeReached: boolean = false;
  hasVisitedLink: boolean = false;
  
  // UI state
  isFullscreen: boolean = false;
  activeTab: 'content' | 'quiz' = 'content';
  formSubmitted: boolean = false;
  isCompletedAlertClosed: boolean = false;
  isAdminAlertClosed: boolean = false;
  hasOpenedQuizOnce: boolean = false;

  // Quiz question sidebar state
  quizQuestionCount: number = 0;
  quizCurrentIndex: number = 0;
  quizAnsweredQuestions: boolean[] = [];
  
  constructor(
    private modalService: NgbModal,
    private sanitizer: DomSanitizer
  ) {}

  open() {
    this.activeTab = 'content'; // Reset to content view on open
    this.isCompletedAlertClosed = false; // Reset alert visibility
    this.isAdminAlertClosed = false;
    this.hasOpenedQuizOnce = false; // Reset quiz initialization flag
    this.initializeContent();
    this.modalRef = this.modalService.open(this.modalTemplate, {
      size: 'xl',
      scrollable: true,
      windowClass: 'preview-modal-window',
      backdrop: 'static',
      keyboard: false
    });

    this.modalRef.result.finally(() => {
      this.close();
    });
  }

  initializeContent() {
    this.isExternalLink = this.contentType === 'LINK';
    
    if (this.isExternalLink) {
      this.safePreviewUrl = this.sanitizer.bypassSecurityTrustResourceUrl(this.previewUrl);
    }

    // Parse PDF if content type is PDF and file is available
    if (this.contentType === 'PDF' && this.file) {
      this.parsePDFFile(this.file);
    }

    // Start timer if needed (for employee view)
    if (this.showTimer && this.minViewTimeMinutes > 0 && 
        (this.status === 'PENDING' || this.status === 'SKIPPED') && 
        !this.hasSeenContent) {
      this.startTimer();
    } else if (this.hasSeenContent || this.status === 'COMPLETED') {
      this.minTimeReached = true;
    }
  }

  startTimer() {
    if (this.timerInterval) clearInterval(this.timerInterval);

    const minTimeSeconds = this.minViewTimeMinutes * 60;

    this.timerInterval = setInterval(() => {
      this.elapsedTime++;
      
      const minutes = Math.floor(this.elapsedTime / 60);
      const seconds = this.elapsedTime % 60;
      this.elapsedTimeDisplay = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

      if (this.elapsedTime >= minTimeSeconds) {
        this.minTimeReached = true;
        clearInterval(this.timerInterval);
      }
    }, 10);
  }

  // PDF Methods
  async parsePDFFile(file: File) {
    try {
      this.isLoadingPreview = true;
      this.clearAllPreviewData();
      
      const arrayBuffer = await file.arrayBuffer();
      const loadingTask = pdfjsLib.getDocument({ data: arrayBuffer });
      const pdf = await loadingTask.promise;
      
      this.totalSlides = pdf.numPages;
      this.slides = new Array(this.totalSlides);
      
      await this.renderPDFPage(0);
      
      // Preload next pages
      for (let i = 1; i <= 3; i++) {
        if (i < this.totalSlides) {
          setTimeout(() => this.renderPDFPage(i), i * 200);
        }
      }
      
      this.isLoadingPreview = false;
    } catch (error) {
      console.error('Error parsing PDF:', error);
      this.isLoadingPreview = false;
    }
  }

  async renderPDFPage(index: number) {
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

      await page.render({ canvasContext: context, viewport }).promise;

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
      
      // Clean up old cache if exists
      if (this.preloadedSlides.has(index)) {
        URL.revokeObjectURL(this.preloadedSlides.get(index)!);
      }

      this.preloadedSlides.set(index, url);
      
      if (index === this.currentSlideIndex) {
        this.previewImageBlob = url;
      }

      // Remove from queue
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
      
      // Remove from queue on error
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
    
    if (this.preloadedSlides.has(index)) {
      this.previewImageBlob = this.preloadedSlides.get(index)!;
      this.isLoadingPreview = false;
      this.triggerPreload(index);
    } else if (this.file) {
      this.isLoadingPreview = true;
      this.renderPDFPage(index).then(() => {
        this.triggerPreload(index);
      });
    }
  }

  triggerPreload(currentIndex: number) {
    if (this.file) {
      // Preload next 3 slides
      for (let i = 1; i <= 3; i++) {
        const nextIndex = currentIndex + i;
        if (nextIndex < this.totalSlides && 
            !this.preloadedSlides.has(nextIndex) && 
            !this.preloadQueue.includes(nextIndex)) {
          this.renderPDFPage(nextIndex).catch(err => 
            console.error(`Failed to preload slide ${nextIndex}:`, err)
          );
        }
      }

      // Preload previous 3 slides
      for (let i = 1; i <= 3; i++) {
        const prevIndex = currentIndex - i;
        if (prevIndex >= 0 && 
            !this.preloadedSlides.has(prevIndex) && 
            !this.preloadQueue.includes(prevIndex)) {
          this.renderPDFPage(prevIndex).catch(err => 
            console.error(`Failed to preload slide ${prevIndex}:`, err)
          );
        }
      }
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
    this.loadSlide(index);
  }

  // Link handling
  onExternalLinkClick() {
    if (this.previewUrl) {
      window.open(this.previewUrl, '_blank');
      this.hasVisitedLink = true;
      this.linkVisited.emit();
    }
  }

  // Timer progress
  getProgressPercentage(): number {
    if (!this.minViewTimeMinutes) return 0;
    const minTimeSeconds = this.minViewTimeMinutes * 60;
    return Math.min((this.elapsedTime / minTimeSeconds) * 100, 100);
  }

  // Fullscreen toggle
  toggleFullscreen() {
    this.isFullscreen = !this.isFullscreen;
    const modalElement = document.querySelector('.modal-content');
    
    if (this.isFullscreen) {
      if ((modalElement as any)?.requestFullscreen) {
        (modalElement as any).requestFullscreen();
      } else if ((modalElement as any)?.webkitRequestFullscreen) {
        (modalElement as any).webkitRequestFullscreen();
      } else if ((modalElement as any)?.mozRequestFullScreen) {
        (modalElement as any).mozRequestFullScreen();
      } else if ((modalElement as any)?.msRequestFullscreen) {
        (modalElement as any).msRequestFullscreen();
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

  @HostListener('document:fullscreenchange')
  @HostListener('document:webkitfullscreenchange')
  @HostListener('document:mozfullscreenchange')
  @HostListener('document:MSFullscreenChange')
  onFullscreenChange() {
    this.isFullscreen = !!(document.fullscreenElement || 
                           (document as any).webkitFullscreenElement || 
                           (document as any).mozFullScreenElement || 
                           (document as any).msFullscreenElement);
  }

  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    // Ignore keyboard shortcuts if the user is typing in an input field (e.g. quiz text areas)
    if (event.target instanceof HTMLInputElement || event.target instanceof HTMLTextAreaElement) {
      return;
    }

    const key = event.key.toLowerCase();
    
    if (key === 'f') {
      this.toggleFullscreen();
    } else if ((key === 'n' || event.key === 'ArrowRight') && this.contentType === 'PDF') {
      this.nextSlide();
    } else if ((key === 'p' || event.key === 'ArrowLeft') && this.contentType === 'PDF') {
      this.previousSlide();
    }
  }

  // Download/Open actions
  downloadFile() {
    if (this.previewUrl) {
      const a = document.createElement('a');
      a.href = this.previewUrl;
      a.download = this.contentName || 'download';
      a.click();
    }
  }

  openInNewTab() {
    if (this.previewUrl) {
      window.open(this.previewUrl, '_blank');
    }
  }

  // Actions
  onSubmitConsent() {
    if (this.showTimer && this.minViewTimeMinutes > 0 && !this.minTimeReached) {
      return; // Time not reached
    }
    if (this.isExternalLink && !this.hasVisitedLink) {
      return; // Link not visited
    }
    this.consentSubmitted.emit();
  }

  goToQuiz() {
    this.activeTab = 'quiz';
    this.quizClicked.emit();
  }

  onContentTabClick() {
    if (this.activeTab === 'quiz' && !this.quizAttempted && !this.isAlreadySubmitted && !this.formSubmitted) {
      // User is taking the quiz, don't allow going back to content until submitted
      return;
    }
    this.activeTab = 'content';
  }

  onQuizTabClick() {
    if (this.canSwitchToQuiz()) {
      this.activeTab = 'quiz';
      this.hasOpenedQuizOnce = true;
    }
  }

  canSwitchToQuiz(): boolean {
    // If timer is active and not reached, user cannot switch to quiz (even to view responses)
    if (this.showTimer && this.minViewTimeMinutes > 0 && !this.minTimeReached && !this.hasSeenContent) {
      return false;
    }

    // If external link and not visited
    if (this.isExternalLink && !this.hasVisitedLink) {
      return false;
    }

    // Otherwise, allow if they have seen content OR if they already attended the quiz
    return true;
  }

  onQuizSubmitted(passed: boolean) {
    this.quizAttempted = true;
    this.formSubmitted = true;
    this.hasSeenContent = true;
    this.quizSubmitRef.close();
    // this.close();
  }

  onQuizQuestionStateChanged(state: { index: number; answered: boolean[] }) {
    this.quizCurrentIndex = state.index;
    this.quizAnsweredQuestions = state.answered;
    this.quizQuestionCount = state.answered.length;
  }

  get quizConfig(): QuizViewConfig {
    return {
      mode: 'attend',
      trainingId: this.trainingId,
      quizId: this.quizId,
      cycleNumber: this.cycleNumber,
      contentId: this.contentId,
      isAlreadySubmitted: this.quizFinished,
      quizTitle: 'Training Quiz'
    };
  }

  jumpToQuizQuestion(index: number) {
    if (this.quizSubmitRef) {
      this.quizSubmitRef.goToQuestion(index);
    }
  }

  close() {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }

    if(this.formSubmitted){
      this.quizCompleted.emit(true);
    }
    
    // Clean up blob URLs
    this.preloadedSlides.forEach((url) => URL.revokeObjectURL(url));
    this.preloadedSlides.clear();
    
    if (this.previewImageBlob) {
      URL.revokeObjectURL(this.previewImageBlob);
    }
    
    if (this.previewUrl && this.previewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(this.previewUrl);
    }
    
    if (this.modalRef) {
      this.modalRef.close();
    }
    
    this.closed.emit();
    this.formSubmitted = false;
    this.hasOpenedQuizOnce = false; // Clean up on close
  }

  clearAllPreviewData() {
    this.preloadedSlides.forEach((url) => URL.revokeObjectURL(url));
    this.preloadedSlides.clear();
    this.preloadQueue = [];
    
    if (this.previewImageBlob) {
      URL.revokeObjectURL(this.previewImageBlob);
      this.previewImageBlob = null;
    }
    
    this.totalSlides = 0;
    this.currentSlideIndex = 0;
    this.slides = [];
  }

  onPreviewError(event: any) {
    event.target.src = 'assets/images/no-preview.png';
    event.target.alt = 'Preview not available';
  }

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

  canSubmitConsent(): boolean {
    if (this.status?.toLowerCase() === 'completed') {
      return false;
    }

    if (!this.hasSeenContent) {
      if (!this.hasQuiz) {
        return true;
      } else if (this.hasQuiz && this.quizAttempted) {
        return true;
      }
    }

    return false;
  }

  canShowGoToQuiz(): boolean {
    if (this.status?.toLowerCase() === 'completed') {
      return false;
    }

    if (this.hasQuiz && !this.quizAttempted) {
      return true;
    }

    return false;
  }

  get updateTipMessage(): string | null {
    if (this.isAdminMode) return null;

    if(!this.lastCompletedOn){
      return 'A new training has been added.';
    } else {
      if(this.hasQuiz){
        if(!this.quizAttempted){
          if(this.hasSeenContent){
            return 'A new quiz has been added.';
          } else {
            return 'A new content and quiz has been added.';
          }
        } else {
          if(!this.hasSeenContent){
            return 'A new content has been added.';
          }
        }
      } else {
        if(!this.hasSeenContent){
          return 'A new content has been added.'
        }
      }
    }

    return null;
  }

  ngOnDestroy() {
    this.close();
  }
}