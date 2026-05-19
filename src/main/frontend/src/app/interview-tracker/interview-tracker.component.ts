import { Component, OnInit, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import * as moment from 'moment';
import * as Highcharts from 'highcharts';
import { InterviewTrackerService } from 'src/app/services/interview-tracker.service';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ScheduleInterviewComponent } from './schedule-interview/schedule-interview.component';
import { first } from 'rxjs/operators';

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

    dateRangeOptions = [
        { value: 'Last Week', label: 'Last Week' },
        { value: 'Last Month', label: 'Last Month' },
        { value: 'Last Quarter', label: 'Last Quarter' },
        { value: 'Last Year', label: 'Last Year' },
        { value: 'Custom', label: 'Custom' }
    ];

    constructor(
        private interviewTrackerService: InterviewTrackerService,
        private modalService: NgbModal
    ) { }

    ngOnInit(): void {
        this.setDateRange('Last Month');
        this.loadDropdownData();
        this.applyFilters();
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

    applyFilters(): void {
        this.isLoading = true;
        this.activeFilterObj = {
            startDate: this.startDate,
            endDate: this.endDate,
            clients: this.selectedClients,
            departmentIds: this.selectedDepartments.map((d: any) => d.id),
            employeeIds: this.selectedEmployees.map((e: any) => e.id)
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
        const scheduledInterviews = data.filter((item: any) => item.interviewStatus === 'Scheduled').length;

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
            scheduledInterviews,
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

    openScheduleInterview(): void {
        const modalRef = this.modalService.open(ScheduleInterviewComponent, {
            size: 'lg',
            centered: true,
            backdrop: 'static'
        });
        modalRef.result.then((result) => {
            if (result === 'saved') {
                this.applyFilters();
            }
        }).catch(() => { });
    }
}
