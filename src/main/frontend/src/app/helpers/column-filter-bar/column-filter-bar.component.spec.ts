import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ColumnFilterBarComponent } from './column-filter-bar.component';

describe('ColumnFilterBarComponent', () => {
  let component: ColumnFilterBarComponent;
  let fixture: ComponentFixture<ColumnFilterBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ColumnFilterBarComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ColumnFilterBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
