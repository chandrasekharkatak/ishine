import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LeaveConfigComponent } from './leave-config.component';

describe('LeaveConfigComponent', () => {
  let component: LeaveConfigComponent;
  let fixture: ComponentFixture<LeaveConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LeaveConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LeaveConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
