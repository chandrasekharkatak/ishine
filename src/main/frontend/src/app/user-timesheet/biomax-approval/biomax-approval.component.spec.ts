import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BiomaxApprovalComponent } from './biomax-approval.component';

describe('BiomaxApprovalComponent', () => {
  let component: BiomaxApprovalComponent;
  let fixture: ComponentFixture<BiomaxApprovalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BiomaxApprovalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BiomaxApprovalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
