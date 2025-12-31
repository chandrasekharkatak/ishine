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

  @Input() content = ""
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

}
