import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewPerformanceComponent } from './view-performance.component';

describe('ViewPerformanceComponent', () => {
  let component: ViewPerformanceComponent;
  let fixture: ComponentFixture<ViewPerformanceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewPerformanceComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewPerformanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
