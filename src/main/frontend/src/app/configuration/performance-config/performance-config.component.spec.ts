import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PerformanceConfigComponent } from './performance-config.component';

describe('PerformanceConfigComponent', () => {
  let component: PerformanceConfigComponent;
  let fixture: ComponentFixture<PerformanceConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PerformanceConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PerformanceConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
