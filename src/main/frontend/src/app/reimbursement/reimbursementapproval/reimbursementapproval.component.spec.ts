import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReimbursementapprovalComponent } from './reimbursementapproval.component';

describe('ReimbursementapprovalComponent', () => {
  let component: ReimbursementapprovalComponent;
  let fixture: ComponentFixture<ReimbursementapprovalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReimbursementapprovalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReimbursementapprovalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
