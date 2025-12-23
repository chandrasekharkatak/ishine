import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { SanitizerService } from '../services/sanitizer.service';

@Pipe({
  standalone: false,
  name: 'safeHtml'
})
export class SafeHtmlPipe implements PipeTransform {

  constructor(private domSanitizer: DomSanitizer, private sanitizer: SanitizerService) {}

  transform(value: string): SafeHtml {
    const clean = this.sanitizer.sanitizeInput(value); // remove XSS
    return this.domSanitizer.bypassSecurityTrustHtml(clean);
  }
}
