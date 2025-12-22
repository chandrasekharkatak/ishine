import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { Feature } from 'src/app/models/feature';
import { User } from 'src/app/models/user';
import { AuthenticationService } from 'src/app/services/authentication.service';
import { BreadcrumbService } from 'src/app/services/breadcrumb.service';

@Component({
  standalone: false,
  selector: 'app-project-insight',
  templateUrl: './project-insight.component.html',
  styleUrls: ['./project-insight.component.css']
})

export class ProjectInsightComponent implements OnInit {

  tabName: any = 'Project Insight';
  currentUser: User;
  userMapping: any = {};
  activeTab: string = 'project-insight-details';

  currentBreadcrumbList: any[] = [];

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private authenticationService: AuthenticationService,
    private breadcrumbService: BreadcrumbService,
  ) {
    this.authenticationService.currentUser.subscribe(x => this.currentUser = x);
    this.breadcrumbService.currentBreadcrumb.subscribe(x => this.currentBreadcrumbList = x);
  }

  ngOnInit(): void {
    let featureMap: Feature[] = this.currentUser.userMapping.filter(userMap => userMap.tabName == this.tabName);
    featureMap?.forEach(feat => {
      let inActiveSubfeatures = feat.subFeatures.filter(sub => {
        if (sub.isActive === false) return sub;
      });
      this.userMapping[feat.featureName.replaceAll(' ', '_').toLowerCase()] = (inActiveSubfeatures.length === feat.subFeatures.length) ? false : true;
    });
    console.log("=========================== user mapping");
    console.log(this.userMapping);
  }

  ngAfterViewInit(): void {
    this.setActiveTab();
  }

  ngOnDestroy(): void {
    this.removeActiveTab();
  }

  setActiveTab() {
    this.route.queryParams.subscribe((params) => {
      let tabName = params.tabName;
      if (tabName) {
        const tab = document.getElementById(tabName);
        tab.classList.add('active');
        let activeRouteLink = tab.getAttribute('routerLink');
        this.router.navigate(['./' + activeRouteLink],
          {
            relativeTo: this.route,
            queryParams: params,
            queryParamsHandling: 'merge'
          });
      }
      else {
        const tab = document.getElementById('projecInsightTab').querySelector('.nav-link');
        tab.classList.add('active');
        let activeRouteLink = tab.getAttribute('routerLink');
        this.router.navigate(['./' + activeRouteLink], { relativeTo: this.route });
      }
    });
  }

  removeActiveTab() {
    const tab = document.getElementById('projecInsightTab').querySelector('.nav-link.active');
    tab?.classList.remove('active');
  }

}
