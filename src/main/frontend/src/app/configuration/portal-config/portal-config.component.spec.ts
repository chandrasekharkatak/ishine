import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PortalConfigComponent } from './portal-config.component';

describe('PortalConfigComponent', () => {
  let component: PortalConfigComponent;
  let fixture: ComponentFixture<PortalConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PortalConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PortalConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
