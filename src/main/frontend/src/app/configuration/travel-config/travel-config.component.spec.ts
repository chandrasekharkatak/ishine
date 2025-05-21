import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TravelConfigComponent } from './travel-config.component';

describe('TravelConfigComponent', () => {
  let component: TravelConfigComponent;
  let fixture: ComponentFixture<TravelConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TravelConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TravelConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
