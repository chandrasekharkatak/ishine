import { Component, Input } from '@angular/core';
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
   standalone: false,
  selector: 'app-info-tooltip',
  // imports: [],
  templateUrl: './info-tooltip.component.html',
  styleUrl: './info-tooltip.component.css'
})
export class InfoTooltipComponent {

  isPdfHovered = false;

 @Input() content: string | string[] = ""
  @Input() bgColor = "#000000ff"
  @Input() iconBgColor = "#0b0b0bff"

  @Input() size = 24 
  @Input() shouldShine = true 
  @Input() pdfPath?: string;
  @Input() pdfCtaText: string = 'Open User Guide';


  openPdf(): void {
  if (!this.pdfPath) return;
  window.open(this.pdfPath, '_blank');
}

get sliderText(): string {
  return this.isPdfHovered ? 'Click to view' : this.pdfCtaText;
}

 get isList(): boolean {
    return Array.isArray(this.content)
  }

  get listItems(): string[] {
    return Array.isArray(this.content) ? this.content : []
  }

  getPastelColor(index: number): string {
    const colors = [
      "#fca6a0ff",
      "#87d7f2ff",
      "#65ee85ff",
      "#fbd972ff", 
      "#bc88f4ff",
      "#f29d7bff", 
    ]
    return colors[index % colors.length]
  }

}
