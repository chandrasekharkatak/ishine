import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewTravelrequestComponent } from './view-travelrequest.component';

describe('ViewTravelrequestComponent', () => {
  let component: ViewTravelrequestComponent;
  let fixture: ComponentFixture<ViewTravelrequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewTravelrequestComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewTravelrequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
