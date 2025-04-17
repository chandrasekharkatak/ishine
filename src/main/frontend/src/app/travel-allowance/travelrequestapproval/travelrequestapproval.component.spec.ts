import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TravelrequestapprovalComponent } from './travelrequestapproval.component';

describe('TravelrequestapprovalComponent', () => {
  let component: TravelrequestapprovalComponent;
  let fixture: ComponentFixture<TravelrequestapprovalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TravelrequestapprovalComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TravelrequestapprovalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
