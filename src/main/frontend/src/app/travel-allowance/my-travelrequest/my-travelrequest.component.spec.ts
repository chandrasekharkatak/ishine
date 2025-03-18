import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyTravelrequestComponent } from './my-travelrequest.component';

describe('MyTravelrequestComponent', () => {
  let component: MyTravelrequestComponent;
  let fixture: ComponentFixture<MyTravelrequestComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MyTravelrequestComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyTravelrequestComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
