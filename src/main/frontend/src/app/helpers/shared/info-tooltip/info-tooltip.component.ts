import {
  Component,
  Input,
  ElementRef,
  ViewChild,
  TemplateRef,
  ViewContainerRef
} from '@angular/core';

import {
  Overlay,
  OverlayRef,
  FlexibleConnectedPositionStrategy
} from '@angular/cdk/overlay';

import { TemplatePortal } from '@angular/cdk/portal';

@Component({
  standalone: false,
  selector: 'app-info-tooltip',
  templateUrl: './info-tooltip.component.html',
  styleUrls: ['./info-tooltip.component.css']
})
export class InfoTooltipComponent {

  @Input() content: string | string[] = "";
  @Input() bgColor = "#1e1e2f";
  @Input() iconBgColor = "#3b82f6";
  @Input() size = 24;
  @Input() shouldShine = false;
  @Input() pdfPath?: string;
  @Input() pdfCtaText: string = 'Open Guide';

  @ViewChild('trigger', { static: false }) trigger!: ElementRef;
@ViewChild('tooltipTemplate', { static: false }) tooltipTemplate!: TemplateRef<any>;

  overlayRef!: OverlayRef;

  isPdfHovered = false;
  private hoverTimeout: any;
  private isMouseOverTooltip = false;

  constructor(
    private overlay: Overlay,
    private vcr: ViewContainerRef
  ) {}

  showTooltip() {
  console.log('trigger:', this.trigger);
  console.log('template:', this.tooltipTemplate);

  if (!this.trigger || !this.tooltipTemplate) {
    console.warn('❌ ViewChild not ready');
    return;
  }

  if (!this.overlayRef) {
    this.createOverlay();
  }
}

createOverlay() {
  const positionStrategy = this.overlay.position()
    .flexibleConnectedTo(this.trigger.nativeElement)
    .withPositions([
      {
        originX: 'center',
        originY: 'bottom',
        overlayX: 'center',
        overlayY: 'top',
        offsetY: 10
      },
      {
        originX: 'center',
        originY: 'top',
        overlayX: 'center',
        overlayY: 'bottom',
        offsetY: -10
      }
    ])
    .withPush(true);

  this.overlayRef = this.overlay.create({
    positionStrategy,
    scrollStrategy: this.overlay.scrollStrategies.reposition()
  });

  const portal = new TemplatePortal(this.tooltipTemplate, this.vcr);
  this.overlayRef.attach(portal);
console.log('✅ overlay created');
  this.bindOverlayEvents(); // 👈 important
}

bindOverlayEvents() {
  const overlayEl = this.overlayRef.overlayElement;

  overlayEl.addEventListener('mouseenter', () => {
    this.isMouseOverTooltip = true;
    clearTimeout(this.hoverTimeout);
  });

  overlayEl.addEventListener('mouseleave', () => {
    this.isMouseOverTooltip = false;
    this.hideTooltip();
  });
}
  hideTooltip() {
  this.hoverTimeout = setTimeout(() => {
    if (!this.isMouseOverTooltip && this.overlayRef) {
      this.overlayRef.dispose();
      this.overlayRef = null!;
    }
  }, 200); // 👈 slightly increased delay
}

  openPdf(): void {
    if (this.pdfPath) {
      window.open(this.pdfPath, '_blank');
    }
  }

  get sliderText(): string {
    return this.isPdfHovered ? 'Click to view' : this.pdfCtaText;
  }

  get isList(): boolean {
    return Array.isArray(this.content);
  }

  get listItems(): string[] {
    return Array.isArray(this.content) ? this.content : [];
  }

  getPastelColor(index: number): string {
    const colors = ["#fca5a5", "#93c5fd", "#86efac", "#fde68a", "#c4b5fd"];
    return colors[index % colors.length];
  }

  addOverlayHoverListeners() {
  const overlayEl = this.overlayRef.overlayElement;

  overlayEl.addEventListener('mouseenter', () => {
    clearTimeout(this.hoverTimeout);
  });

  overlayEl.addEventListener('mouseleave', () => {
    this.hideTooltip();
  });
}
}