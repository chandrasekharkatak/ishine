import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({
  name: 'highlight'
})
export class HighlightPipe implements PipeTransform {

  constructor(private sanitizer: DomSanitizer) {}

  transform(value: string, searchTerm: string): SafeHtml {
    if (!searchTerm || !value) return value;

    const escapedTerm = searchTerm.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&');
    const regex = new RegExp(`(${escapedTerm})`, 'gi');
    const highlighted = value.replace(regex, `<span style="background-color: #cce5ff; color: #004085;">$1</span>`);

    return this.sanitizer.bypassSecurityTrustHtml(highlighted);
  }

}
