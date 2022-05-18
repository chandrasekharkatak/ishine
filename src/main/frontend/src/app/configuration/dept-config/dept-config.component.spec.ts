import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeptConfigComponent } from './dept-config.component';

describe('DeptConfigComponent', () => {
  let component: DeptConfigComponent;
  let fixture: ComponentFixture<DeptConfigComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DeptConfigComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DeptConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
