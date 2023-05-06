import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyResignationComponent } from './my-resignation.component';

describe('MyResignationComponent', () => {
  let component: MyResignationComponent;
  let fixture: ComponentFixture<MyResignationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MyResignationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyResignationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
