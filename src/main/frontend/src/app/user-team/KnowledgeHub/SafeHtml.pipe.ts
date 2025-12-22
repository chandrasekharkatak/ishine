import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({ name: 'safeHtml' })
export class SafeHtmlPipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}

  transform(value: string): SafeHtml {
    if (!value) return value;
    // Allow span elements with class attribute
    return this.sanitizer.bypassSecurityTrustHtml(
      value.replace(/<span class="highlight">/g, '<span class="highlight">')
    );
  }
}