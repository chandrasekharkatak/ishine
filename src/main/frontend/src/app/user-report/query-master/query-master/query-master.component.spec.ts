import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QueryMasterComponent } from './query-master.component';

describe('QueryMasterComponent', () => {
  let component: QueryMasterComponent;
  let fixture: ComponentFixture<QueryMasterComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ QueryMasterComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(QueryMasterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
