import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApproverWorkflowComponent } from './approver-workflow.component';

describe('ApproverWorkflowComponent', () => {
  let component: ApproverWorkflowComponent;
  let fixture: ComponentFixture<ApproverWorkflowComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ApproverWorkflowComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ApproverWorkflowComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
