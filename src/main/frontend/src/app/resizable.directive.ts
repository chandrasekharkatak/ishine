import { Directive, ElementRef, Renderer2, Input, OnInit, HostListener } from '@angular/core';

@Directive({
  standalone: false,
  selector: '[appResizable]'
})
export class ResizableDirective implements OnInit {

  @Input() column: any;

  private startX = 0;
  private startWidth = 0;
  private isResizing = false;

  constructor(
    private el: ElementRef,
    private renderer: Renderer2
  ) { }

  ngOnInit(): void {

    const handle =
      this.renderer.createElement('span');

    this.renderer.addClass(
      handle,
      'resize-handle'
    );

    this.renderer.appendChild(
      this.el.nativeElement,
      handle
    );

    this.renderer.listen(
      handle,
      'mousedown',
      (event: MouseEvent) => {
        this.initResize(event);
      }
    );
  }

  initResize(event: MouseEvent): void {

    event.preventDefault();
    event.stopPropagation();

    this.isResizing = true;

    this.startX = event.pageX;

    this.startWidth =
      this.el.nativeElement.offsetWidth;
  }

  @HostListener('document:mousemove', ['$event'])
  onMouseMove(event: MouseEvent): void {

    if (!this.isResizing) return;

    const width =
      this.startWidth +
      (event.pageX - this.startX);

    this.column.width =
      Math.max(width, this.column.minWidth || 100);
  }

  @HostListener('document:mouseup')
  onMouseUp(): void {
    this.isResizing = false;
  }
}