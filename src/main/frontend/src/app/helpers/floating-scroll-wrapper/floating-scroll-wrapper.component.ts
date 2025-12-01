import {
  Component,
  AfterViewInit,
  ViewChild,
  ElementRef,
  HostListener
} from '@angular/core';

@Component({
  standalone:false,
  selector: 'app-floating-scroll-wrapper',
  templateUrl: './floating-scroll-wrapper.component.html',
  styleUrls: ['./floating-scroll-wrapper.component.css']
})
export class FloatingScrollWrapperComponent implements AfterViewInit {

  @ViewChild('floatingScrollWrapper') floatingScrollWrapper!: ElementRef;
  @ViewChild('floatingScrollInner') floatingScrollInner!: ElementRef;
  @ViewChild('scrollBody') scrollBody!: ElementRef;

  ngAfterViewInit() {
    this.updateScrollSizes();
    this.syncScrolling();
    this.observeContentChanges();
  }

  /** Match widths */
  updateScrollSizes() {
    const bodyEl = this.scrollBody.nativeElement;
    const wrapperEl = this.floatingScrollWrapper.nativeElement;
    const innerEl = this.floatingScrollInner.nativeElement;

    // Floating scrollbar visible width = scrollBody visible width
    wrapperEl.style.width = bodyEl.clientWidth + 'px';

    // Inner scroll content width = actual scrollWidth
    innerEl.style.width = bodyEl.scrollWidth + 'px';
  }

  /** Sync scroll both ways */
  syncScrolling() {
    const bodyEl = this.scrollBody.nativeElement;
    const wrapperEl = this.floatingScrollWrapper.nativeElement;

    wrapperEl.addEventListener('scroll', () => {
      bodyEl.scrollLeft = wrapperEl.scrollLeft;
    });

    bodyEl.addEventListener('scroll', () => {
      wrapperEl.scrollLeft = bodyEl.scrollLeft;
    });
  }

  /** Auto adjust when content changes */
  observeContentChanges() {
    const bodyEl = this.scrollBody.nativeElement;
    const resizeObserver = new ResizeObserver(() => {
      this.updateScrollSizes();
    });

    resizeObserver.observe(bodyEl);
  }

  /** Auto adjust on screen resize */
  @HostListener('window:resize')
  onWindowResize() {
    this.updateScrollSizes();
  }
}
