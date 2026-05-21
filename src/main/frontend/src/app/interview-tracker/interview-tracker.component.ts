import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import * as moment from 'moment';
import * as Highcharts from 'highcharts';
import { InterviewTrackerService } from 'src/app/services/interview-tracker.service';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ScheduleInterviewComponent } from './schedule-interview/schedule-interview.component';
import { first } from 'rxjs/operators';
import { buildInterviewUserMapping, InterviewUserMapping } from './interview-permissions';

@Component({
    standalone: false,
    selector: 'app-interview-tracker',
    templateUrl: './interview-tracker.component.html',
    styleUrls: ['./interview-tracker.component.css']
})
export class InterviewTrackerComponent implements OnInit, AfterViewInit {

    @ViewChild('interviewStatusChart') interviewStatusChartRef: ElementRef;
    @ViewChild('selectionStatusChart') selectionStatusChartRef: ElementRef;
    @ViewChild('onboardingStatusChart') onboardingStatusChartRef: ElementRef;

    dateRangePreset: string = 'Last Month';
    startDate: any;
    endDate: any;
    showCustomDate: boolean = false;

    clientList: any[] = [];
    departmentList: any[] = [];
    employeeList: any[] = [];

    selectedClients: any[] = [];
    selectedDepartments: any[] = [];
    selectedEmployees: any[] = [];

    dashboardStats: any = {};
    isLoading: boolean = false;
    activeFilterObj: any = {};
    userMapping: InterviewUserMapping = buildInterviewUserMapping(null);

    dateRangeOptions = [
        { value: 'Last Week', label: 'Last Week' },
        { value: 'Last Month', label: 'Last Month' },
        { value: 'Last Quarter', label: 'Last Quarter' },
        { value: 'Last Year', label: 'Last Year' },
        { value: 'Custom', label: 'Custom' }
    ];

    constructor(
        private interviewTrackerService: InterviewTrackerService,
        private authenticationService: AuthenticationService,
        private modalService: NgbModal
    ) { }

    ngOnInit(): void {
        this.userMapping = buildInterviewUserMapping(this.authenticationService.currentUserValue);
        this.setDateRange('Last Month');
        if (this.userMapping.view_interview) {
            this.loadDropdownData();
            this.applyFilters();
        }
    }

    ngAfterViewInit(): void {
        setTimeout(() => {
            this.renderCharts();
        }, 500);
    }

    setDateRange(preset: string): void {
        this.dateRangePreset = preset;
        this.showCustomDate = preset === 'Custom';
        if (!this.showCustomDate) {
            let start: moment.Moment;
            const end = moment().endOf('day');
            switch (preset) {
                case 'Last Week':
                    start = moment().subtract(7, 'days').startOf('day');
                    break;
                case 'Last Month':
                    start = moment().subtract(1, 'month').startOf('day');
                    break;
                case 'Last Quarter':
                    start = moment().subtract(3, 'months').startOf('day');
                    break;
                case 'Last Year':
                    start = moment().subtract(1, 'year').startOf('day');
                    break;
                default:
                    start = moment().subtract(1, 'month').startOf('day');
            }
            this.startDate = start.format('YYYY-MM-DD');
            this.endDate = end.format('YYYY-MM-DD');
        }
    }

    loadDropdownData(): void {
        this.interviewTrackerService.getClientList().pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus === 'Success' && response.serviceResponse) {
                this.clientList = response.serviceResponse.clients || [];
                this.departmentList = response.serviceResponse.departments || [];
                this.employeeList = response.serviceResponse.employees || [];
            }
        });
    }

    resetFilters(): void {
        this.dateRangePreset = 'Last Month';
        this.setDateRange('Last Month');
        this.selectedClients = [];
        this.selectedDepartments = [];
        this.selectedEmployees = [];
        this.applyFilters();
    }

    hasActiveFilters(): boolean {
        return (this.selectedClients?.length > 0)
            || (this.selectedDepartments?.length > 0)
            || (this.selectedEmployees?.length > 0)
            || this.dateRangePreset === 'Custom';
    }

    applyFilters(): void {
        this.isLoading = true;
        this.activeFilterObj = {
            startDate: this.startDate,
            endDate: this.endDate,
            clients: this.selectedClients,
            departmentIds: this.mapToNumericIds(this.selectedDepartments),
            employeeIds: this.mapToNumericIds(this.selectedEmployees)
        };

        this.interviewTrackerService.getDashboardStats(this.activeFilterObj).pipe(first()).subscribe((response: any) => {
            if (response.serviceStatus === 'Success' && response.serviceResponse) {
                const data = response.serviceResponse;
                this.computeDashboardStats(data);
            }
            setTimeout(() => {
                this.renderCharts();
                this.isLoading = false;
            }, 100);
        });
    }

    computeDashboardStats(data: any[]): void {
        const futureInterviews = data.filter((item: any) => moment(item.date).isAfter(moment(), 'day')).length;

        const interviewStatusBreakdown: any = {};
        data.forEach((item: any) => {
            interviewStatusBreakdown[item.interviewStatus] = (interviewStatusBreakdown[item.interviewStatus] || 0) + 1;
        });

        const selectionStatusBreakdown: any = {};
        data.forEach((item: any) => {
            selectionStatusBreakdown[item.selectionStatus] = (selectionStatusBreakdown[item.selectionStatus] || 0) + 1;
        });

        const onboardingStatusBreakdown: any = {};
        data.forEach((item: any) => {
            onboardingStatusBreakdown[item.onboardingStatus] = (onboardingStatusBreakdown[item.onboardingStatus] || 0) + 1;
        });

        this.dashboardStats = {
            futureInterviews,
            totalInterviews: data.length,
            interviewStatusBreakdown,
            selectionStatusBreakdown,
            onboardingStatusBreakdown
        };
    }

    renderCharts(): void {
        this.renderInterviewStatusChart();
        this.renderSelectionStatusChart();
        this.renderOnboardingStatusChart();
    }

    renderInterviewStatusChart(): void {
        if (!this.interviewStatusChartRef || !this.interviewStatusChartRef.nativeElement) return;
        const data = this.dashboardStats.interviewStatusBreakdown || {};
        const seriesData = Object.keys(data).map(key => ({ name: key, y: data[key] }));

        Highcharts.chart(this.interviewStatusChartRef.nativeElement, {
            chart: { type: 'pie' } as any,
            title: { text: '' },
            credits: { enabled: false },
            navigation: { buttonOptions: { enabled: false } },
            legend: { position: 'bottom' },
            plotOptions: {
                pie: {
                    innerSize: '50%',
                    dataLabels: { enabled: true, format: '{point.name}: {point.y}' }
                }
            },
            colors: ['#00a8b8', '#132b67', '#960200', '#857c6f'],
            series: [{
                name: 'Interview Status',
                colorByPoint: true,
                data: seriesData
            }]
        } as any);
    }

    renderSelectionStatusChart(): void {
        if (!this.selectionStatusChartRef || !this.selectionStatusChartRef.nativeElement) return;
        const data = this.dashboardStats.selectionStatusBreakdown || {};
        const seriesData = Object.keys(data).map(key => ({ name: key, y: data[key] }));

        Highcharts.chart(this.selectionStatusChartRef.nativeElement, {
            chart: { type: 'pie' } as any,
            title: { text: '' },
            credits: { enabled: false },
            navigation: { buttonOptions: { enabled: false } },
            legend: { position: 'bottom' },
            plotOptions: {
                pie: {
                    innerSize: '50%',
                    dataLabels: { enabled: true, format: '{point.name}: {point.y}' }
                }
            },
            colors: ['#28a745', '#dc3545', '#ffc107', '#17a2b8'],
            series: [{
                name: 'Selection Status',
                colorByPoint: true,
                data: seriesData
            }]
        } as any);
    }

    renderOnboardingStatusChart(): void {
        if (!this.onboardingStatusChartRef || !this.onboardingStatusChartRef.nativeElement) return;
        const data = this.dashboardStats.onboardingStatusBreakdown || {};
        const seriesData = Object.keys(data).map(key => ({ name: key, y: data[key] }));

        Highcharts.chart(this.onboardingStatusChartRef.nativeElement, {
            chart: { type: 'pie' } as any,
            title: { text: '' },
            credits: { enabled: false },
            navigation: { buttonOptions: { enabled: false } },
            legend: { position: 'bottom' },
            plotOptions: {
                pie: {
                    innerSize: '50%',
                    dataLabels: { enabled: true, format: '{point.name}: {point.y}' }
                }
            },
            colors: ['#28a745', '#ffc107', '#6c757d'],
            series: [{
                name: 'Onboarding Status',
                colorByPoint: true,
                data: seriesData
            }]
        } as any);
    }

    /**
     * app-my-select with valueKey="id" stores primitives; without valueKey it stores { id, name }.
     */
    private mapToNumericIds(selection: any[]): number[] {
        if (!selection || selection.length === 0) {
            return [];
        }
        return selection
            .map((item: any) => {
                if (item == null) {
                    return null;
                }
                if (typeof item === 'object') {
                    const id = item.id != null ? item.id : item.empId;
                    return id != null ? Number(id) : null;
                }
                return Number(item);
            })
            .filter((id: number | null) => id != null && !Number.isNaN(id as number)) as number[];
    }

    openScheduleInterview(): void {
        if (!this.userMapping.schedule_interview) {
            return;
        }
        const modalRef = this.modalService.open(ScheduleInterviewComponent, {
            size: 'lg',
            centered: true,
            backdrop: 'static'
        });
        modalRef.result.then((result) => {
            if (result === 'saved') {
                this.applyFilters();
            }
        }).catch((reason) => {
            if (reason !== 'cancel' && reason !== 'ESC') {
                console.log('Modal dismissed:', reason);
            }
        });
    }
}
