import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NgxPaginationModule } from 'ngx-pagination';
import { MatSortModule } from '@angular/material/sort';
import { TeamAllTimesheetsTableComponent } from './team-all-timesheets-table.component';

describe('TeamAllTimesheetsTableComponent', () => {
  let component: TeamAllTimesheetsTableComponent;
  let fixture: ComponentFixture<TeamAllTimesheetsTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NgxPaginationModule, MatSortModule],
      declarations: [TeamAllTimesheetsTableComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TeamAllTimesheetsTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});


