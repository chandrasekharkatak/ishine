import { Component, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { BehaviorSubject } from 'rxjs';
import { first } from 'rxjs/operators';
import { ProjectInsightProjconfigService } from 'src/app/services/project-insight-projconfig.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-project-insight-projconfig',
  templateUrl: './project-insight-projconfig.component.html',
  styleUrls: ['./project-insight-projconfig.component.css']
})
export class ProjectInsightProjconfigComponent implements OnInit {
  rows = Array(8).fill({});

  behaviouralSubjectOnj = new BehaviorSubject(null);

  // Form
  dynamicForm: FormGroup;

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

  fields: any[] = [
    {
      "id": "ojfw41t8j",
      "type": "select",
      "label": "Project Name",
      "name": "projectName",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
      "options": [
          {
              "label": "Leanne Graham",
              "value": 1
          },
          {
              "label": "Ervin Howell",
              "value": 2
          },
          {
              "label": "Clementine Bauch",
              "value": 3
          },
          {
              "label": "Patricia Lebsack",
              "value": 4
          },
          {
              "label": "Chelsey Dietrich",
              "value": 5
          },
          {
              "label": "Mrs. Dennis Schulist",
              "value": 6
          },
          {
              "label": "Kurtis Weissnat",
              "value": 7
          },
          {
              "label": "Nicholas Runolfsdottir V",
              "value": 8
          },
          {
              "label": "Glenna Reichert",
              "value": 9
          },
          {
              "label": "Clementina DuBuque",
              "value": 10
          }
      ],
      "optionSource": "api",
      "width": 33,
      "rowPosition": 0,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
      "apiLabelKey": "name",
      "apiValueKey": "id"
  },
  {
      "id": "m6trg1tqn",
      "type": "select",
      "label": "Project Manager(s)",
      "name": "projectManager",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
      "options": [
          {
              "label": "Leanne Graham",
              "value": 1
          },
          {
              "label": "Ervin Howell",
              "value": 2
          },
          {
              "label": "Clementine Bauch",
              "value": 3
          },
          {
              "label": "Patricia Lebsack",
              "value": 4
          },
          {
              "label": "Chelsey Dietrich",
              "value": 5
          },
          {
              "label": "Mrs. Dennis Schulist",
              "value": 6
          },
          {
              "label": "Kurtis Weissnat",
              "value": 7
          },
          {
              "label": "Nicholas Runolfsdottir V",
              "value": 8
          },
          {
              "label": "Glenna Reichert",
              "value": 9
          },
          {
              "label": "Clementina DuBuque",
              "value": 10
          }
      ],
      "optionSource": "api",
      "width": 33,
      "rowPosition": 0,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
      "apiLabelKey": "name",
      "apiValueKey": "id"
  },
  {
      "id": "9w90cn4uo",
      "type": "select",
      "label": "Team Lead",
      "name": "teamLead",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
      "options": [
          {
              "label": "Leanne Graham",
              "value": 1
          },
          {
              "label": "Ervin Howell",
              "value": 2
          },
          {
              "label": "Clementine Bauch",
              "value": 3
          },
          {
              "label": "Patricia Lebsack",
              "value": 4
          },
          {
              "label": "Chelsey Dietrich",
              "value": 5
          },
          {
              "label": "Mrs. Dennis Schulist",
              "value": 6
          },
          {
              "label": "Kurtis Weissnat",
              "value": 7
          },
          {
              "label": "Nicholas Runolfsdottir V",
              "value": 8
          },
          {
              "label": "Glenna Reichert",
              "value": 9
          },
          {
              "label": "Clementina DuBuque",
              "value": 10
          }
      ],
      "optionSource": "api",
      "width": 33,
      "rowPosition": 0,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
      "apiLabelKey": "name",
      "apiValueKey": "id"
  },
  {
      "id": "1wz04my4d",
      "type": "select",
      "label": "Client",
      "name": "client",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
      "options": [
          {
              "label": "Leanne Graham",
              "value": 1
          },
          {
              "label": "Ervin Howell",
              "value": 2
          },
          {
              "label": "Clementine Bauch",
              "value": 3
          },
          {
              "label": "Patricia Lebsack",
              "value": 4
          },
          {
              "label": "Chelsey Dietrich",
              "value": 5
          },
          {
              "label": "Mrs. Dennis Schulist",
              "value": 6
          },
          {
              "label": "Kurtis Weissnat",
              "value": 7
          },
          {
              "label": "Nicholas Runolfsdottir V",
              "value": 8
          },
          {
              "label": "Glenna Reichert",
              "value": 9
          },
          {
              "label": "Clementina DuBuque",
              "value": 10
          }
      ],
      "optionSource": "api",
      "width": 33,
      "rowPosition": 1,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
      "apiLabelKey": "name",
      "apiValueKey": "id"
  },
  {
      "id": "2zrri96cm",
      "type": "select",
      "label": "Outcome",
      "name": "outcome",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
      "options": [
          {
              "label": "Leanne Graham",
              "value": 1
          },
          {
              "label": "Ervin Howell",
              "value": 2
          },
          {
              "label": "Clementine Bauch",
              "value": 3
          },
          {
              "label": "Patricia Lebsack",
              "value": 4
          },
          {
              "label": "Chelsey Dietrich",
              "value": 5
          },
          {
              "label": "Mrs. Dennis Schulist",
              "value": 6
          },
          {
              "label": "Kurtis Weissnat",
              "value": 7
          },
          {
              "label": "Nicholas Runolfsdottir V",
              "value": 8
          },
          {
              "label": "Glenna Reichert",
              "value": 9
          },
          {
              "label": "Clementina DuBuque",
              "value": 10
          }
      ],
      "optionSource": "api",
      "width": 33,
      "rowPosition": 1,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
      "apiLabelKey": "name",
      "apiValueKey": "id"
  },
  {
      "id": "g62apa02d",
      "type": "select",
      "label": "Technology Stack",
      "name": "technologystack",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
      "options": [
          {
              "label": "Leanne Graham",
              "value": 1
          },
          {
              "label": "Ervin Howell",
              "value": 2
          },
          {
              "label": "Clementine Bauch",
              "value": 3
          },
          {
              "label": "Patricia Lebsack",
              "value": 4
          },
          {
              "label": "Chelsey Dietrich",
              "value": 5
          },
          {
              "label": "Mrs. Dennis Schulist",
              "value": 6
          },
          {
              "label": "Kurtis Weissnat",
              "value": 7
          },
          {
              "label": "Nicholas Runolfsdottir V",
              "value": 8
          },
          {
              "label": "Glenna Reichert",
              "value": 9
          },
          {
              "label": "Clementina DuBuque",
              "value": 10
          }
      ],
      "optionSource": "api",
      "width": 33,
      "rowPosition": 1,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
      "apiLabelKey": "name",
      "apiValueKey": "id"
  },
  {
      "id": "ucqn86cja",
      "type": "select",
      "label": "Delivery Model",
      "name": "deliveryModel",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
      "options": [
          {
              "label": "Leanne Graham",
              "value": 1
          },
          {
              "label": "Ervin Howell",
              "value": 2
          },
          {
              "label": "Clementine Bauch",
              "value": 3
          },
          {
              "label": "Patricia Lebsack",
              "value": 4
          },
          {
              "label": "Chelsey Dietrich",
              "value": 5
          },
          {
              "label": "Mrs. Dennis Schulist",
              "value": 6
          },
          {
              "label": "Kurtis Weissnat",
              "value": 7
          },
          {
              "label": "Nicholas Runolfsdottir V",
              "value": 8
          },
          {
              "label": "Glenna Reichert",
              "value": 9
          },
          {
              "label": "Clementina DuBuque",
              "value": 10
          }
      ],
      "optionSource": "api",
      "width": 33,
      "rowPosition": 2,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
      "apiLabelKey": "name",
      "apiValueKey": "id"
  },
  {
      "id": "usgort71d",
      "type": "date",
      "label": "Start Date",
      "name": "startDate",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
      "optionSource": "static",
      "width": 33,
      "rowPosition": 2
  },
  {
      "id": "nfzjtbozl",
      "type": "select",
      "label": "Business Function",
      "name": "businessFucntion",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
      "options": [
          {
              "label": "Leanne Graham",
              "value": 1
          },
          {
              "label": "Ervin Howell",
              "value": 2
          },
          {
              "label": "Clementine Bauch",
              "value": 3
          },
          {
              "label": "Patricia Lebsack",
              "value": 4
          },
          {
              "label": "Chelsey Dietrich",
              "value": 5
          },
          {
              "label": "Mrs. Dennis Schulist",
              "value": 6
          },
          {
              "label": "Kurtis Weissnat",
              "value": 7
          },
          {
              "label": "Nicholas Runolfsdottir V",
              "value": 8
          },
          {
              "label": "Glenna Reichert",
              "value": 9
          },
          {
              "label": "Clementina DuBuque",
              "value": 10
          }
      ],
      "optionSource": "api",
      "width": 33,
      "rowPosition": 2,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
      "apiLabelKey": "name",
      "apiValueKey": "id"
  },
  {
      "id": "qzvdyrhdr",
      "type": "select",
      "label": "Select Domain",
      "name": "selectDomain",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
      "options": [
          {
              "label": "Leanne Graham",
              "value": 1
          },
          {
              "label": "Ervin Howell",
              "value": 2
          },
          {
              "label": "Clementine Bauch",
              "value": 3
          },
          {
              "label": "Patricia Lebsack",
              "value": 4
          },
          {
              "label": "Chelsey Dietrich",
              "value": 5
          },
          {
              "label": "Mrs. Dennis Schulist",
              "value": 6
          },
          {
              "label": "Kurtis Weissnat",
              "value": 7
          },
          {
              "label": "Nicholas Runolfsdottir V",
              "value": 8
          },
          {
              "label": "Glenna Reichert",
              "value": 9
          },
          {
              "label": "Clementina DuBuque",
              "value": 10
          }
      ],
      "optionSource": "api",
      "width": 33,
      "rowPosition": 3,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
      "apiLabelKey": "name",
      "apiValueKey": "id"
  },
  {
      "id": "b51ka883d",
      "type": "select",
      "label": "Select Sub-Domain",
      "name": "subdomain",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
      "options": [
          {
              "label": "Leanne Graham",
              "value": 1
          },
          {
              "label": "Ervin Howell",
              "value": 2
          },
          {
              "label": "Clementine Bauch",
              "value": 3
          },
          {
              "label": "Patricia Lebsack",
              "value": 4
          },
          {
              "label": "Chelsey Dietrich",
              "value": 5
          },
          {
              "label": "Mrs. Dennis Schulist",
              "value": 6
          },
          {
              "label": "Kurtis Weissnat",
              "value": 7
          },
          {
              "label": "Nicholas Runolfsdottir V",
              "value": 8
          },
          {
              "label": "Glenna Reichert",
              "value": 9
          },
          {
              "label": "Clementina DuBuque",
              "value": 10
          }
      ],
      "optionSource": "api",
      "width": 33,
      "rowPosition": 3,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
      "apiLabelKey": "name",
      "apiValueKey": "id"
  },
  {
      "id": "2toiepk7c",
      "type": "select",
      "label": "Select Services",
      "name": "service",
      "required": false,
      "placeholder": "",
      "defaultValue": "",
      "options": [
          {
              "label": "Leanne Graham",
              "value": 1
          },
          {
              "label": "Ervin Howell",
              "value": 2
          },
          {
              "label": "Clementine Bauch",
              "value": 3
          },
          {
              "label": "Patricia Lebsack",
              "value": 4
          },
          {
              "label": "Chelsey Dietrich",
              "value": 5
          },
          {
              "label": "Mrs. Dennis Schulist",
              "value": 6
          },
          {
              "label": "Kurtis Weissnat",
              "value": 7
          },
          {
              "label": "Nicholas Runolfsdottir V",
              "value": 8
          },
          {
              "label": "Glenna Reichert",
              "value": 9
          },
          {
              "label": "Clementina DuBuque",
              "value": 10
          }
      ],
      "optionSource": "api",
      "width": 33,
      "rowPosition": 3,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
      "apiLabelKey": "name",
      "apiValueKey": "id"
  },
  {
      "id": "nh1279lky",
      "type": "select",
      "label": "Select Business Features",
      "name": "businessFeature",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
      "options": [
          {
              "label": "Leanne Graham",
              "value": 1
          },
          {
              "label": "Ervin Howell",
              "value": 2
          },
          {
              "label": "Clementine Bauch",
              "value": 3
          },
          {
              "label": "Patricia Lebsack",
              "value": 4
          },
          {
              "label": "Chelsey Dietrich",
              "value": 5
          },
          {
              "label": "Mrs. Dennis Schulist",
              "value": 6
          },
          {
              "label": "Kurtis Weissnat",
              "value": 7
          },
          {
              "label": "Nicholas Runolfsdottir V",
              "value": 8
          },
          {
              "label": "Glenna Reichert",
              "value": 9
          },
          {
              "label": "Clementina DuBuque",
              "value": 10
          }
      ],
      "optionSource": "api",
      "width": 33,
      "rowPosition": 4,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
      "apiLabelKey": "name",
      "apiValueKey": "id"
  },
  {
      "id": "jyynz556k",
      "type": "select",
      "label": "Select Sub-Business Features",
      "name": "subBusinessFeature",
      "required": false,
      "placeholder": "",
      "defaultValue": "",
      "options": [
          {
              "label": "Leanne Graham",
              "value": 1
          },
          {
              "label": "Ervin Howell",
              "value": 2
          },
          {
              "label": "Clementine Bauch",
              "value": 3
          },
          {
              "label": "Patricia Lebsack",
              "value": 4
          },
          {
              "label": "Chelsey Dietrich",
              "value": 5
          },
          {
              "label": "Mrs. Dennis Schulist",
              "value": 6
          },
          {
              "label": "Kurtis Weissnat",
              "value": 7
          },
          {
              "label": "Nicholas Runolfsdottir V",
              "value": 8
          },
          {
              "label": "Glenna Reichert",
              "value": 9
          },
          {
              "label": "Clementina DuBuque",
              "value": 10
          }
      ],
      "optionSource": "api",
      "width": 33,
      "rowPosition": 4,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
      "apiLabelKey": "name",
      "apiValueKey": "id"
    }
  ];
  projectInsightProjectObj: any = {
    "projectName": "2",
    "projectManager": "4",
    "teamLead": "5",
    "client": "6",
    "outcome": "4",
    "technologystack": "3",
    "deliveryModel": "3",
    "startDate": "2025-06-13",
    "businessFucntion": "9",
    "selectDomain": "5",
    "subdomain": "6",
    "service": "5",
    "businessFeature": "8",
    "subBusinessFeature": "6"
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

  // Add new properties for field dependency
  dependentFieldsMap: Map<string, any[]> = new Map();
  fieldDependencies: Map<string, string> = new Map();

  constructor(
    private projectInsightProjconfigService:ProjectInsightProjconfigService,
    private formBuilder: FormBuilder,
    private http: HttpClient
  ) { }

  ngOnInit(): void {
    this.buildDynamicForm();
    this.setupFormValueChanges();
    this.openTableView();
    this.initializeDependencies();
    this.loadInitialOptions();
  }

  showTable(){
    this.isTable = true;
    this.isCreateForm = false;
  }

  setupFormValueChanges(): void {
    this.dynamicForm.valueChanges.subscribe(values => {
      this.syncFormToProjectObj(values);
    });
  }

  syncFormToProjectObj(formValues: any): void {
    Object.keys(formValues).forEach(key => {
      this.projectInsightProjectObj[key] = formValues[key];
    });
  
    console.log('Form values synced to projectInsightProjectObj:', this.projectInsightProjectObj);
  }  

  buildDynamicForm(): void {
    const formControls: any = {};
    
    this.fields.forEach(field => {
      if (!field.name || field.name.trim() === '') {
        console.warn('Field without name found:', field.label);
        return;
      }

      const validators = [];
      if (field.required) {
        validators.push(Validators.required);
      }
      const defaultValue = this.projectInsightProjectObj[field.name] || field.defaultValue || '';
      formControls[field.name] = [defaultValue, validators];
    });
    
    this.dynamicForm = this.formBuilder.group(formControls);
  }

  populateFormWithData(data: any): void {
    if (data && this.dynamicForm) {
      this.dynamicForm.patchValue(data);
    }
  }

  loadProjectData(projectId: string): void {
    console.log('Loading project data for ID:', projectId);
    
    // For now, using the existing projectInsightProjectObj
    // In real implementation, you would make an API call here
    setTimeout(() => {
      this.populateFormWithData(this.projectInsightProjectObj);
    }, 100);
  }

  onProjectSelect(projectId: string): void {
    this.loadProjectData(projectId);
  }

  openTableView(){
    this.isCreateForm = false;
    this.isTable = true;
  }

  openCreateProject(){
    this.isCreateForm = true;
    this.isTable = false;
  }

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

  getBootstrapCol(width: number): number {
    // Convert percentage width to Bootstrap column size
    if (width <= 25) return 3;      // col-md-3 (25%)
    if (width <= 33) return 4;      // col-md-4 (33.33%)
    if (width <= 50) return 6;      // col-md-6 (50%)
    if (width <= 75) return 9;      // col-md-9 (75%)
    return 12;                      // col-md-12 (100%)
  }

  onSelectChange(event: any, field: any) {
    const selectedValue = event.target.value;
    
    // Check if this field has dependent fields
    if (this.dependentFieldsMap.has(field.name)) {
      const dependentFields = this.dependentFieldsMap.get(field.name)!;
      
      // Clear and reload options for all dependent fields
      dependentFields.forEach(dependentFieldName => {
        this.handleDependentFieldChange(dependentFieldName, selectedValue);
      });
    }
  }

  async handleDependentFieldChange(fieldName: string, parentValue: string) {
    const field = this.fields.find(f => f.name === fieldName);
    if (!field || field.optionSource !== 'dependent') return;

    // Clear the dependent field's value
    this.dynamicForm.get(fieldName)?.setValue('');
    
    // Clear existing options
    field.options = [];
    
    // Load new options
    await this.loadDependentOptions(field);
  }

  async loadDependentOptions(field: any): Promise<any[]> {
    if (!field.parentField || !field.dependentApiUrl) return [];

    const parentValue = this.dynamicForm.get(field.parentField)?.value;
    if (!parentValue) return [];

    try {
      const url = field.dependentApiUrl.replace('{parentValue}', parentValue);
      const response = await this.http.get<any[]>(url).toPromise();
      
      if (response) {
        const options = response.map(item => ({
          label: item[field.dependentLabelKey || 'name'],
          value: item[field.dependentValueKey || 'id']
        }));
        
        // Cache the options
        field.options = options;
        return options;
      }
    } catch (error) {
      console.error(`Error loading dependent options for ${field.name}:`, error);
    }
    
    return [];
  }

  getLayoutConfig(): any[][] {
    const rows: any[][] = [];
    let currentRow: any[] = [];
    let currentWidth = 0;

    this.fields.forEach(field => {
      const fieldWidth = field.width || 100;
      
      if (currentWidth + fieldWidth > 100) {
        if (currentRow.length > 0) {
          rows.push(currentRow);
        }
        currentRow = [field];
        currentWidth = fieldWidth;
      } else {
        currentRow.push(field);
        currentWidth += fieldWidth;
      }
    });

    if (currentRow.length > 0) {
      rows.push(currentRow);
    }

    return rows;
  }

  initializeDependencies() {
    this.dependentFieldsMap.clear();
    this.fieldDependencies.clear();

    this.fields.forEach(field => {
      if (field.optionSource === 'dependent' && field.parentField) {
        this.fieldDependencies.set(field.name, field.parentField);
        
        if (!this.dependentFieldsMap.has(field.parentField)) {
          this.dependentFieldsMap.set(field.parentField, []);
        }
        this.dependentFieldsMap.get(field.parentField)!.push(field.name);
      }
    });
  }

  loadInitialOptions() {
    for (const field of this.fields) {
      if (field.optionSource === 'api') {
        this.loadApiOptions(field);
      }
    }
  }

  async loadApiOptions(field: any): Promise<any[]> {
    if (!field.apiUrl) return [];

    try {
      const response = await this.http.get<any[]>(field.apiUrl).toPromise();
      
      if (response) {
        const options = response.map(item => ({
          label: item[field.apiLabelKey || 'name'],
          value: item[field.apiValueKey || 'id']
        }));
        
        // Cache the options
        field.options = options;
        return options;
      }
    } catch (error) {
      console.error(`Error loading API options for ${field.name}:`, error);
    }
    
    return [];
  }

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

  getFormStatus(): any {
    return {
      valid: this.dynamicForm?.valid,
      touched: this.dynamicForm?.touched,
      dirty: this.dynamicForm?.dirty,
      values: this.dynamicForm?.value,
      projectObj: this.projectInsightProjectObj
    };
  }

  resetForm(): void {
    this.dynamicForm?.reset();
    this.populateFormWithData(this.projectInsightProjectObj);
  }

  onSubmit() {
    if (this.dynamicForm.valid) {
      console.log('Form submitted:', this.dynamicForm.value);
      // Add form submission logic here
    } else {
      console.log('Form is invalid');
      // Mark all fields as touched to show validation errors
      this.markFormGroupTouched(this.dynamicForm);
    }
  }

  getFormValue(fieldName: string): any {
    return this.dynamicForm.get(fieldName)?.value;
  }

  // Helper method to mark all form controls as touched
  markFormGroupTouched(formGroup: FormGroup) {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      if (control instanceof FormGroup) {
        this.markFormGroupTouched(control);
      } else {
        control?.markAsTouched();
      }
    });
  }
}
