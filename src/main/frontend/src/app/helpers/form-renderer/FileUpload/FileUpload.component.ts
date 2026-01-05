import { Component, EventEmitter, Input, Output, ElementRef, ViewChild, TemplateRef } from '@angular/core';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { first } from 'rxjs/operators';
import { ProjectInsightService } from 'src/app/services/project-insight.service';
import { environment } from 'src/environments/environment';

export interface PreviewableFile {
  name: string;
  size?: number;
  type?: string;
  url?: string;
  file?: File;
}

@Component({
  standalone: false,
  selector: 'app-FileUpload',
  templateUrl: './FileUpload.component.html',
  styleUrls: ['./FileUpload.component.scss']
})
export class FileUploadComponent {
  @Input() field: any = null;
  @Input() viewMode: string = 'View';
  @Input() projectName!: string;

  @Output() closeUploadModal = new EventEmitter<void>();
  @Output() uploadFiles = new EventEmitter<any>();
  @Output() removeFiles = new EventEmitter<any>();

  @ViewChild('fileInput') fileInput!: ElementRef;
  @ViewChild("alert_message_modal") alertMessageTemplate!: TemplateRef<any>;
  @ViewChild("delete_message_modal") deleteMessageTemplate!: TemplateRef<any>;

  selectedFiles: PreviewableFile[] = [];
  previewFile: PreviewableFile | null = null;
  fileUrl: SafeUrl | null = null;
  isPreviewOpen = false;
  baseurl = environment.baseUrl;
  alertModalRef:NgbModalRef;
  deleteModalRef:NgbModalRef;
  alertMessage: string = '';
  filesToRemove : string[] = [];

  constructor(
    private sanitizer: DomSanitizer,
    private readonly projectInsightService: ProjectInsightService,
    private modal: NgbModal
  ) {}

  ngOnInit() {
    const value = this.field?.value || [];
    if (!Array.isArray(value)) return;
    this.selectedFiles = value.map(fileName => ({ name: fileName }));
  }

  stripFieldName(input: string): string {
    const match = input.match(/\((.*?)\)/);
    return match ? match[1] : input;
  }

  onFileSelected(event: any) {
    const files: FileList = event.target.files;
    if (!files || files.length === 0) return;

    const newFiles: PreviewableFile[] = Array.from(files).map(f => ({
      name: f.name,
      size: f.size,
      type: f.type,
      file: f
    }));

    // Remove duplicates
    this.selectedFiles.forEach(f => {
      const existingFile = newFiles.find(nf => nf.name.trim().toLowerCase() === this.stripFieldName(f.name).trim().toLowerCase());
      if (existingFile) {
        newFiles.splice(newFiles.indexOf(existingFile), 1);
      }
    });

    this.selectedFiles = [...this.selectedFiles, ...newFiles];

    if (this.fileInput) {
      this.fileInput.nativeElement.value = '';
    }
  }

  getName(name: string, length?: number) {
    return name.length > (length || 30) ? name.slice(0, length || 30) + '...' : name;
  }

  onClose(): void {
    this.closeUploadModal.emit();
  }

  addFilesToRemove(fileName: string, index: number) {
    this.filesToRemove.push(fileName);
    this.alertMessage = `Are you sure you want to delete ${fileName}?`;
    this.deleteModalRef = this.modal.open(this.deleteMessageTemplate, { modalDialogClass: 'modal-sm' });
  }

  removeFile() {
    this.projectInsightService.deleteFiles(this.filesToRemove, this.projectName).pipe(first()).subscribe({
      next: () => {
        this.selectedFiles = this.selectedFiles.filter(file => !this.filesToRemove.includes(file.name));
        const data: string[] = this.selectedFiles.map(file => file.name);
        // this.field.value = data;
        this.removeFiles.emit({ field: this.field, files: data });

        if (this.isPreviewOpen && this.previewFile && this.filesToRemove.includes(this.previewFile.name)) {
          this.closePreview();
        }

        this.filesToRemove = [];
        this.alertMessage = "File deleted successfully";
        this.alertModalRef = this.modal.open(this.alertMessageTemplate, { modalDialogClass: 'modal-sm' });
      },
      error: () => {
        this.alertMessage = "Error deleting file";
        this.alertModalRef = this.modal.open(this.alertMessageTemplate, { modalDialogClass: 'modal-sm' });
      },
      complete: () => {
        this.deleteModalRef?.close();
      }
    });
  }

  cancelRequest() {
    if (this.alertModalRef) this.alertModalRef?.close();
    if (this.deleteModalRef) {
      this.filesToRemove = [];
      this.deleteModalRef?.close();
    }
  }

  onPreviewFile(file: PreviewableFile) {
    this.fileUrl = null;
    this.previewFile = file;
    const fileType = this.getFileType(file);

    if (fileType === 'image' || fileType === 'pdf') {
      if (file.file) {
        this.fileUrl = this.sanitizer.bypassSecurityTrustResourceUrl(URL.createObjectURL(file.file));
        this.isPreviewOpen = true;
      } else {
        this.projectInsightService.viewProjectInsightFile(file.name, this.projectName).pipe(first()).subscribe({
          next: (resp: any) => {
            const blob = this.base64ToBlob(resp.data, resp.contentType);
            this.isPreviewOpen = true;
            this.fileUrl = this.sanitizer.bypassSecurityTrustResourceUrl(URL.createObjectURL(blob));
          },
          error: () => {
            this.alertMessage = "Error viewing file";
            this.alertModalRef = this.modal.open(this.alertMessageTemplate, { modalDialogClass: 'modal-sm' });
          }
        });
      }
    } else {
      this.isPreviewOpen = true;
    }
  }

  base64ToBlob(base64Data: string, contentType: string): Blob {
    const byteCharacters = atob(base64Data);
    const byteNumbers = new Array(byteCharacters.length);
    for (let i = 0; i < byteCharacters.length; i++) byteNumbers[i] = byteCharacters.charCodeAt(i);
    return new Blob([new Uint8Array(byteNumbers)], { type: contentType });
  }

  downloadFile() {
    const file = this.previewFile;
    if (!file) return;

    if (file.file) {
      const url = URL.createObjectURL(file.file);
      const a = document.createElement('a');
      a.href = url;
      a.download = file.name;
      a.click();
      URL.revokeObjectURL(url);
    } else {
      this.projectInsightService.viewProjectInsightFile(file.name, this.projectName).pipe(first()).subscribe({
        next: (resp: any) => {
          const blob = this.base64ToBlob(resp.data, resp.contentType || 'application/octet-stream');
          const url = URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = file.name;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          URL.revokeObjectURL(url);
        }
      });
    }
  }

  closePreview() {
    if (this.previewFile?.file && this.fileUrl) {
      const unsafeUrl = this.sanitizer.sanitize(4, this.fileUrl);
      if (unsafeUrl && unsafeUrl.startsWith('blob:')) URL.revokeObjectURL(unsafeUrl);
    }
    this.previewFile = null;
    this.fileUrl = null;
    this.isPreviewOpen = false;
  }

  getFileType(file: PreviewableFile): string {
    if (!file?.name) return 'other';
    const ext = file.name.split('.').pop()?.toLowerCase();
    if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext!)) return 'image';
    if (ext === 'pdf') return 'pdf';
    return 'other';
  }

  getFilesToUpload(): File[] {
    const files = this.selectedFiles.filter(f => f.file instanceof File).map(f => f.file) as File[];
    console.log("Files: ", files);

    return files;
  }

  onUpload() {
    const filesToUpload = this.getFilesToUpload();
    if (filesToUpload.length > 0) {
      this.uploadFiles.emit({ field: this.field, files: filesToUpload });
    }



    this.alertMessage = "File uploaded successfully";
    this.alertModalRef = this.modal.open(this.alertMessageTemplate, { modalDialogClass: 'modal-sm' });

    this.onClose();
  }

  onSave() {
    this.cancelRequest();
  }
}
