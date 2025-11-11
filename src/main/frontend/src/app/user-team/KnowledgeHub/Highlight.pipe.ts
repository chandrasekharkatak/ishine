import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Pipe({
  name: 'highlight'
})
export class HighlightPipe implements PipeTransform {

  constructor(private sanitizer: DomSanitizer) {}

  transform(text: any, query: string): SafeHtml {
    if (!query || !text) {
      return text;
    }

    const textStr = typeof text === 'string' ? text : String(text);
    
    const escapedQuery = query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
    const regex = new RegExp(escapedQuery, 'gi');
    
    const highlighted = textStr.replace(
      regex, 
      match => `<span class="highlight">${match}</span>`
    );
    
    return this.sanitizer.bypassSecurityTrustHtml(highlighted);
  }

}
