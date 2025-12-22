import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PerformanceManagementSystemComponent } from './performance-management-system.component';

describe('PerformanceManagementSystemComponent', () => {
  let component: PerformanceManagementSystemComponent;
  let fixture: ComponentFixture<PerformanceManagementSystemComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PerformanceManagementSystemComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PerformanceManagementSystemComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
