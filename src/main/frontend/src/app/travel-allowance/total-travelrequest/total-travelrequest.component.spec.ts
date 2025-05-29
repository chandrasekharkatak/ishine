import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TotalTravelrequestComponent } from './total-travelrequest.component';

describe('TotalTravelrequestComponent', () => {
  let component: TotalTravelrequestComponent;
  let fixture: ComponentFixture<TotalTravelrequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TotalTravelrequestComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TotalTravelrequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
