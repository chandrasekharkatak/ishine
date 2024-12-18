import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Employee360BiomaxComponent } from './employee360-biomax.component';

describe('Employee360BiomaxComponent', () => {
  let component: Employee360BiomaxComponent;
  let fixture: ComponentFixture<Employee360BiomaxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Employee360BiomaxComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Employee360BiomaxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
