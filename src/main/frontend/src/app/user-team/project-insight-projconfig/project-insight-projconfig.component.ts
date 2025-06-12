import { Component, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { BehaviorSubject } from 'rxjs';
import { first } from 'rxjs/operators';
import { ProjectInsightProjconfigService } from 'src/app/services/project-insight-projconfig.service';

@Component({
  selector: 'app-project-insight-projconfig',
  templateUrl: './project-insight-projconfig.component.html',
  styleUrls: ['./project-insight-projconfig.component.css']
})
export class ProjectInsightProjconfigComponent implements OnInit {
  rows = Array(8).fill({});

  behaviouralSubjectOnj = new BehaviorSubject(null);

  //List
  allProjectInsightProjectList:any[] = [];
  allDomainDataList:any[] = [
    {
      "domain_id": "1",
      "domain": "Banking",
      "subDomainList": [
        {
          "subdomain_id": "1",
          "subdomain": "Retail Banking",
          "domain_id": "1",
          "serviceList": [
            {
              "service_id": "1",
              "service": "Deposit Services",
              "subdomain_id": "1",
              "businessFeatureList": [
                {
                  "business_feature_id": "1",
                  "business_feature": "CASA",
                  "service_id": "1",
                  "featureList": [
                    {
                      "feature_id": "1",
                      "feature_name": "Account Opening",
                      "parent_feature_id": null,
                      "business_feature_id": "1",
                      "featureList": [
                        {
                          "feature_id": "2",
                          "feature_name": "XYZ",
                          "parent_feature_id": "1",
                          "business_feature_id": "1"
                        }
                      ]
                    },
                    {
                      "feature_id": "3",
                      "feature_name": "KYC",
                      "parent_feature_id": null,
                      "business_feature_id": "1"
                    }
                  ]
                },
                {
                  "business_feature_id": "2",
                  "business_feature": "Fixed Deposit",
                  "service_id": "1"
                }
              ]
            }
          ]
        },
        {
          "subdomain_id": "2",
          "subdomain": "Corporate Banking",
          "domain_id": "1",
          "serviceList": []
        }
      ]
    },
    {
      "domain_id": "2",
      "domain": "Healthcare",
      "subDomainList": []
    }  
  ];
  projectInsightProject: any = {
    "project": "ICICI BANK",
    "projectid": "1",
    "groupList": [
      {
        "groupid": 1,
        "groupName": "Architecture",
        "title": "System Design Overview",
        "groupList": [
          {
            "groupid": 76,
            "groupName": "Load Balancing",
            "title": "Load Distribution Mechanism",
            "groupList": [
              {
                "groupid": 45,
                "groupName": "ALB",
                "title": "Application Load Balancer Setup",
                "groupList": [
                  {
                    "groupid": 101,
                    "groupName": "Routing Rules",
                    "title": "Rules for URL path-based routing",
                    "groupList": []
                  },
                  {
                    "groupid": 102,
                    "groupName": "Health Checks",
                    "title": "Configured for backend service health",
                    "groupList": []
                  }
                ]
              },
              {
                "groupid": 46,
                "groupName": "NLB",
                "title": "Network Load Balancer Setup",
                "groupList": []
              }
            ]
          },
          {
            "groupid": 77,
            "groupName": "Microservices",
            "title": "Microservice Deployment Architecture",
            "groupList": [
              {
                "groupid": 103,
                "groupName": "Service Registry",
                "title": "Eureka setup for service discovery",
                "groupList": []
              }
            ]
          }
        ]
      },
      {
        "groupid": 2,
        "groupName": "User",
        "title": "User Access & Authentication",
        "groupList": [
          {
            "groupid": 104,
            "groupName": "Login Module",
            "title": "OAuth2 / SSO Integration",
            "groupList": []
          },
          {
            "groupid": 105,
            "groupName": "User Roles",
            "title": "Role-based Access Control",
            "groupList": []
          }
        ]
      },
      {
        "groupid": 3,
        "groupName": "Deployment",
        "title": "CI/CD Pipeline",
        "groupList": [
          {
            "groupid": 106,
            "groupName": "Jenkins",
            "title": "Build and Deploy Automation",
            "groupList": []
          },
          {
            "groupid": 107,
            "groupName": "Kubernetes",
            "title": "Container Orchestration",
            "groupList": []
          }
        ]
      }
    ]
  };  

  //columnList
  projectColumns:any[] = ['blank','','','','',''];

  //boolean
  isCreateForm:boolean = false;
  isTable:boolean = false;
  isDomainStructure:boolean = false;
  isSelected:any

  //domain tree
  expanded: { [key: string]: boolean } = {};

  //Utility
  sortDirection = 'asc';
  sortColumn: any;
  sortColumnType:any;
  abbreviationError: string = '';
  filters:any = {};
  isSearchEnabled:boolean = false;

  constructor(
    private projectInsightProjconfigService:ProjectInsightProjconfigService,
  ) { }

  ngOnInit(): void {
    this.openTableView();
  }

  openTableView(){
    this.isCreateForm = false;
    this.isTable = true;
  }

  openCreateProject(){
    this.isCreateForm = true;
    this.isTable = false;
  }

  //Domain
  getAllDomainData(): void {
    this.projectInsightProjconfigService.getAllDomainData()
      .pipe(first())
      .subscribe({
        next: (response: any) => {
          if (response?.serviceStatus === "Success") {
            this.allDomainDataList = response.serviceResponse;
          } else {
            console.error('Service responded with an error:', response?.serviceResponse || 'Unknown error');
          }
        },
        error: (err: any) => {
          console.error('API call failed:', err);
        }
      });
  }  

  toggle(node: any, type: string, idField: string) {
    const key = `${type}-${node[idField]}`;
    this.expanded[key] = !this.expanded[key];
  }

  isExpanded(node: any, type: string, idField: string) {
    const key = `${type}-${node[idField]}`;
    return !!this.expanded[key];
  }

  addItem(type: string, parent: any, event: MouseEvent) {
    event.stopPropagation(); // Prevents toggling when clicking +
    // Open dialog, show inline input, or emit event
    // type: 'domain' | 'subdomain' | 'service' | 'business_feature' | 'feature' | 'subfeature'
    // parent: the parent object at this level, or null for top-level domain
    console.log('Add', type, 'under', parent);
  }

  selectGroup(group: any){

  }

  toggleDomainProjectView(event: any){}

  //pagination
  page = 1;
  handlePageChange(event) {
    this.page = event;
  }

  sortData(sort: Sort){
    //console.log(sort);
    if(sort.active){
      let sortParams:any[] = sort.active?.split("|");
      this.sortColumn = sortParams[0];
      this.sortColumnType = sortParams[1];
      this.sortDirection = sort.direction;
    }
  }

  toggleSearch(){
    this.isSearchEnabled = !this.isSearchEnabled;
    if(!this.isSearchEnabled){
      this.filters = {};
    }
  }

  onSearch(searchData){
    this.filters = searchData;
  }

}
