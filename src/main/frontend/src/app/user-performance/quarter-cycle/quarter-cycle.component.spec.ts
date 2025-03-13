import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QuarterCycleComponent } from './quarter-cycle.component';

describe('QuarterCycleComponent', () => {
  let component: QuarterCycleComponent;
  let fixture: ComponentFixture<QuarterCycleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ QuarterCycleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(QuarterCycleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
