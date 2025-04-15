import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LMSComponent } from './lms.component';

describe('LMSComponent', () => {
  let component: LMSComponent;
  let fixture: ComponentFixture<LMSComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LMSComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LMSComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
