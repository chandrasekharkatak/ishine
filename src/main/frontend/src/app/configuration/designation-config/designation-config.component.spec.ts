import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DesignationConfigComponent } from './designation-config.component';

describe('DesignationConfigComponent', () => {
  let component: DesignationConfigComponent;
  let fixture: ComponentFixture<DesignationConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DesignationConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DesignationConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
