import { Component, OnInit } from '@angular/core';
import { Sort } from '@angular/material/sort';
import { BehaviorSubject } from 'rxjs';
import { first } from 'rxjs/operators';
import { ProjectInsightProjconfigService } from 'src/app/services/project-insight-projconfig.service';
import { FormBuilder, FormGroup, Validators, FormArray, FormControl } from '@angular/forms';
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
        "id": "j8dvgrc14",
      "type": "select",
      "label": "Project Name",
        "name": "projectname",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
        "optionSource": "api",
      "options": [
          {
              "label": "Leanne Graham",
                "value": "1"
          },
          {
              "label": "Ervin Howell",
                "value": "2"
          },
          {
              "label": "Clementine Bauch",
                "value": "3"
          },
          {
              "label": "Patricia Lebsack",
                "value": "4"
          },
          {
              "label": "Chelsey Dietrich",
                "value": "5"
          },
          {
              "label": "Mrs. Dennis Schulist",
                "value": "6"
          },
          {
              "label": "Kurtis Weissnat",
                "value": "7"
          },
          {
              "label": "Nicholas Runolfsdottir V",
                "value": "8"
          },
          {
              "label": "Glenna Reichert",
                "value": "9"
          },
          {
              "label": "Clementina DuBuque",
                "value": "10"
          }
      ],
      "width": 33,
      "rowPosition": 0,
        "multiple": false,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
        "apiLabelKe": null,
        "apiValueKey": "id",
        "parentField": null,
        "dependentApiUrl": null,
        "dependentLabelKey": null,
        "dependentValueKey": null,
        "dependentParamName": null
    },
    {
        "id": "j7v3aaeqz",
      "type": "select",
      "label": "Project Manager(s)",
        "name": "projectmanager",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
        "optionSource": "api",
      "options": [
          {
              "label": "Leanne Graham",
                "value": "1"
          },
          {
              "label": "Ervin Howell",
                "value": "2"
          },
          {
              "label": "Clementine Bauch",
                "value": "3"
          },
          {
              "label": "Patricia Lebsack",
                "value": "4"
          },
          {
              "label": "Chelsey Dietrich",
                "value": "5"
          },
          {
              "label": "Mrs. Dennis Schulist",
                "value": "6"
          },
          {
              "label": "Kurtis Weissnat",
                "value": "7"
          },
          {
              "label": "Nicholas Runolfsdottir V",
                "value": "8"
          },
          {
              "label": "Glenna Reichert",
                "value": "9"
          },
          {
              "label": "Clementina DuBuque",
                "value": "10"
          }
      ],
      "width": 33,
      "rowPosition": 0,
        "multiple": false,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
        "apiLabelKe": null,
        "apiValueKey": "id",
        "parentField": null,
        "dependentApiUrl": null,
        "dependentLabelKey": null,
        "dependentValueKey": null,
        "dependentParamName": null
    },
    {
        "id": "jxebjutpx",
      "type": "select",
      "label": "Team Lead",
        "name": "teamlead",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
        "optionSource": "api",
      "options": [
          {
              "label": "Leanne Graham",
                "value": "1"
          },
          {
              "label": "Ervin Howell",
                "value": "2"
          },
          {
              "label": "Clementine Bauch",
                "value": "3"
          },
          {
              "label": "Patricia Lebsack",
                "value": "4"
          },
          {
              "label": "Chelsey Dietrich",
                "value": "5"
          },
          {
              "label": "Mrs. Dennis Schulist",
                "value": "6"
          },
          {
              "label": "Kurtis Weissnat",
                "value": "7"
          },
          {
              "label": "Nicholas Runolfsdottir V",
                "value": "8"
          },
          {
              "label": "Glenna Reichert",
                "value": "9"
          },
          {
              "label": "Clementina DuBuque",
                "value": "10"
          }
      ],
      "width": 33,
      "rowPosition": 0,
        "multiple": false,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
        "apiLabelKe": null,
        "apiValueKey": "id",
        "parentField": null,
        "dependentApiUrl": null,
        "dependentLabelKey": null,
        "dependentValueKey": null,
        "dependentParamName": null
    },
    {
        "id": "3n3f8xhuc",
      "type": "select",
      "label": "Client",
      "name": "client",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
        "optionSource": "api",
      "options": [
          {
              "label": "Leanne Graham",
                "value": "1"
          },
          {
              "label": "Ervin Howell",
                "value": "2"
          },
          {
              "label": "Clementine Bauch",
                "value": "3"
          },
          {
              "label": "Patricia Lebsack",
                "value": "4"
          },
          {
              "label": "Chelsey Dietrich",
                "value": "5"
          },
          {
              "label": "Mrs. Dennis Schulist",
                "value": "6"
          },
          {
              "label": "Kurtis Weissnat",
                "value": "7"
          },
          {
              "label": "Nicholas Runolfsdottir V",
                "value": "8"
          },
          {
              "label": "Glenna Reichert",
                "value": "9"
          },
          {
              "label": "Clementina DuBuque",
                "value": "10"
          }
      ],
      "width": 33,
      "rowPosition": 1,
        "multiple": false,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
        "apiLabelKe": null,
        "apiValueKey": "id",
        "parentField": null,
        "dependentApiUrl": null,
        "dependentLabelKey": null,
        "dependentValueKey": null,
        "dependentParamName": null
    },
    {
        "id": "hel2hg2qh",
      "type": "select",
      "label": "Outcome",
      "name": "outcome",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
        "optionSource": "api",
      "options": [
          {
              "label": "Leanne Graham",
                "value": "1"
          },
          {
              "label": "Ervin Howell",
                "value": "2"
          },
          {
              "label": "Clementine Bauch",
                "value": "3"
          },
          {
              "label": "Patricia Lebsack",
                "value": "4"
          },
          {
              "label": "Chelsey Dietrich",
                "value": "5"
          },
          {
              "label": "Mrs. Dennis Schulist",
                "value": "6"
          },
          {
              "label": "Kurtis Weissnat",
                "value": "7"
          },
          {
              "label": "Nicholas Runolfsdottir V",
                "value": "8"
          },
          {
              "label": "Glenna Reichert",
                "value": "9"
          },
          {
              "label": "Clementina DuBuque",
                "value": "10"
          }
      ],
      "width": 33,
      "rowPosition": 1,
        "multiple": false,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
        "apiLabelKe": null,
        "apiValueKey": "id",
        "parentField": null,
        "dependentApiUrl": null,
        "dependentLabelKey": null,
        "dependentValueKey": null,
        "dependentParamName": null
    },
    {
        "id": "z2c5iky6b",
      "type": "select",
      "label": "Technology Stack",
      "name": "technologystack",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
        "optionSource": "api",
      "options": [
          {
              "label": "Leanne Graham",
                "value": "1"
          },
          {
              "label": "Ervin Howell",
                "value": "2"
          },
          {
              "label": "Clementine Bauch",
                "value": "3"
          },
          {
              "label": "Patricia Lebsack",
                "value": "4"
          },
          {
              "label": "Chelsey Dietrich",
                "value": "5"
          },
          {
              "label": "Mrs. Dennis Schulist",
                "value": "6"
          },
          {
              "label": "Kurtis Weissnat",
                "value": "7"
          },
          {
              "label": "Nicholas Runolfsdottir V",
                "value": "8"
          },
          {
              "label": "Glenna Reichert",
                "value": "9"
          },
          {
              "label": "Clementina DuBuque",
                "value": "10"
          }
      ],
      "width": 33,
      "rowPosition": 1,
        "multiple": false,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
        "apiLabelKe": null,
        "apiValueKey": "id",
        "parentField": null,
        "dependentApiUrl": null,
        "dependentLabelKey": null,
        "dependentValueKey": null,
        "dependentParamName": null
    },
    {
        "id": "rirx3fydu",
      "type": "select",
      "label": "Delivery Model",
      "name": "deliveryModel",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
        "optionSource": "api",
      "options": [
          {
              "label": "Leanne Graham",
                "value": "1"
          },
          {
              "label": "Ervin Howell",
                "value": "2"
          },
          {
              "label": "Clementine Bauch",
                "value": "3"
          },
          {
              "label": "Patricia Lebsack",
                "value": "4"
          },
          {
              "label": "Chelsey Dietrich",
                "value": "5"
          },
          {
              "label": "Mrs. Dennis Schulist",
                "value": "6"
          },
          {
              "label": "Kurtis Weissnat",
                "value": "7"
          },
          {
              "label": "Nicholas Runolfsdottir V",
                "value": "8"
          },
          {
              "label": "Glenna Reichert",
                "value": "9"
          },
          {
              "label": "Clementina DuBuque",
                "value": "10"
          }
      ],
      "width": 33,
      "rowPosition": 2,
        "multiple": false,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
        "apiLabelKe": null,
        "apiValueKey": "id",
        "parentField": null,
        "dependentApiUrl": null,
        "dependentLabelKey": null,
        "dependentValueKey": null,
        "dependentParamName": null
    },
    {
        "id": "a7ixsrmd2",
      "type": "date",
      "label": "Start Date",
      "name": "startDate",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
      "optionSource": "static",
        "options": null,
      "width": 33,
        "rowPosition": 2,
        "multiple": false,
        "apiUrl": null,
        "apiLabelKe": null,
        "apiValueKey": null,
        "parentField": null,
        "dependentApiUrl": null,
        "dependentLabelKey": null,
        "dependentValueKey": null,
        "dependentParamName": null
    },
    {
        "id": "or1xsk63c",
      "type": "select",
      "label": "Business Function",
        "name": "businessFunction",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
        "optionSource": "api",
      "options": [
          {
              "label": "Leanne Graham",
                "value": "1"
          },
          {
              "label": "Ervin Howell",
                "value": "2"
          },
          {
              "label": "Clementine Bauch",
                "value": "3"
          },
          {
              "label": "Patricia Lebsack",
                "value": "4"
          },
          {
              "label": "Chelsey Dietrich",
                "value": "5"
          },
          {
              "label": "Mrs. Dennis Schulist",
                "value": "6"
          },
          {
              "label": "Kurtis Weissnat",
                "value": "7"
          },
          {
              "label": "Nicholas Runolfsdottir V",
                "value": "8"
          },
          {
              "label": "Glenna Reichert",
                "value": "9"
          },
          {
              "label": "Clementina DuBuque",
                "value": "10"
          }
      ],
      "width": 33,
      "rowPosition": 2,
        "multiple": false,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
        "apiLabelKe": null,
        "apiValueKey": "id",
        "parentField": null,
        "dependentApiUrl": null,
        "dependentLabelKey": null,
        "dependentValueKey": null,
        "dependentParamName": null
    },
    {
        "id": "w5qmx8lwg",
      "type": "select",
        "label": "Domain",
        "name": "domain",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
        "optionSource": "api",
      "options": [
          {
              "label": "Leanne Graham",
                "value": "1"
          },
          {
              "label": "Ervin Howell",
                "value": "2"
          },
          {
              "label": "Clementine Bauch",
                "value": "3"
          },
          {
              "label": "Patricia Lebsack",
                "value": "4"
          },
          {
              "label": "Chelsey Dietrich",
                "value": "5"
          },
          {
              "label": "Mrs. Dennis Schulist",
                "value": "6"
          },
          {
              "label": "Kurtis Weissnat",
                "value": "7"
          },
          {
              "label": "Nicholas Runolfsdottir V",
                "value": "8"
          },
          {
              "label": "Glenna Reichert",
                "value": "9"
          },
          {
              "label": "Clementina DuBuque",
                "value": "10"
          }
      ],
      "width": 33,
      "rowPosition": 3,
        "multiple": true,
      "apiUrl": "https://jsonplaceholder.typicode.com/users",
        "apiLabelKe": null,
        "apiValueKey": "id",
        "parentField": null,
        "dependentApiUrl": null,
        "dependentLabelKey": null,
        "dependentValueKey": null,
        "dependentParamName": null
    },
    {
        "id": "45g3szpkp",
      "type": "select",
        "label": "Sub Domain",
      "name": "subdomain",
      "required": true,
      "placeholder": "",
      "defaultValue": "",
        "optionSource": "dependent",
        "options": [],
      "width": 33,
      "rowPosition": 3,
        "multiple": true,
        "apiUrl": null,
        "apiLabelKe": null,
        "apiValueKey": null,
        "parentField": "domain",
        "dependentApiUrl": "https://api.example.com/data?domain={parentValue}",
        "dependentLabelKey": "name",
        "dependentValueKey": "id",
        "dependentParamName": "domain"
    }
  ];
  projectInsightProjectObj: any = {
    "projectName": "2",
    "projectManager": ["4", "5"],
    "teamLead": "5",
    "client": "6",
    "outcome": "4",
    "technologystack": "3",
    "deliveryModel": "3",
    "startDate": "2025-06-13",
    "businessFucntion": "9",
    "selectDomain": "1",
    "subdomain": "",
    "service": "",
    "businessFeature": "",
    "subBusinessFeature": "",
    "xyz": "3"
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

  // Add new properties for enhanced form handling
  dependentFieldOptions: Map<string, Map<string, any[]>> = new Map();
  multiSelectDependentFields: Map<string, FormArray> = new Map();

  // Add a property to cache the layout config
  private _layoutConfigCache: any[][] = [];
  private _fieldsHash: string = '';
  
  // Public property for template binding
  layoutConfig: any[][] = [];

  constructor(
    private projectInsightProjconfigService:ProjectInsightProjconfigService,
    private formBuilder: FormBuilder,
    private http: HttpClient
  ) { }

  ngOnInit(): void {
    // Initialize form first
    this.buildDynamicForm();
    
    // Setup form value changes
    this.setupFormValueChanges();
    
    // Open table view by default
    this.openTableView();
    
    // Load initial options after form is ready
    setTimeout(async () => {
      await this.loadInitialOptions();
    }, 200);
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

  // Method to clear layout cache when fields are updated
  private clearLayoutCache(): void {
    this._layoutConfigCache = [];
    this._fieldsHash = '';
    this.layoutConfig = [];
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
      
      let defaultValue = this.projectInsightProjectObj[field.name] || field.defaultValue || '';
      
      // Handle multi-select fields
      if (field.type === 'select' && field.multiple) {
        defaultValue = Array.isArray(defaultValue) ? defaultValue : [];
      formControls[field.name] = [defaultValue, validators];
      } else {
        // For dependent fields, start with empty value
        if (field.optionSource === 'dependent') {
          formControls[field.name] = ['', validators];
        } else {
          formControls[field.name] = [defaultValue, validators];
        }
      }
    });
    
    this.dynamicForm = this.formBuilder.group(formControls);
    
    // Clear layout cache when form is rebuilt
    this.clearLayoutCache();
    
    // Initialize dependencies after form is built
    this.initializeDependencies();
    
    // Initialize layout config
    this.getLayoutConfig();
  }

  populateFormWithData(data: any): void {
    if (data && this.dynamicForm) {
      // Use setTimeout to ensure form is fully initialized
      setTimeout(() => {
      this.dynamicForm.patchValue(data);
      }, 0);
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
    
    // Ensure form is properly initialized when opening create form
    if (!this.dynamicForm) {
      this.buildDynamicForm();
    }
    
    // Load initial options after a short delay to ensure form is ready
    setTimeout(async () => {
      await this.loadInitialOptions();
    }, 100);
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

  onSelectChange(event: any, fieldName: string) {
    const selectedValue = event.value;
    const field = this.fields.find(f => f.name === fieldName);
    
    // Check if this field has dependent fields
    if (this.dependentFieldsMap.has(fieldName)) {
      const dependentFields = this.dependentFieldsMap.get(fieldName)!;
      
      // Clear and reload options for all dependent fields
      dependentFields.forEach(dependentFieldName => {
        this.handleDependentFieldChange(dependentFieldName, selectedValue);
      });
    }
  }

  onMultiSelectChange(event: any, field: any) {
    const selectedValues = event.value;
    
    // Check if this field has dependent fields
    if (this.dependentFieldsMap.has(field.name)) {
      const dependentFields = this.dependentFieldsMap.get(field.name)!;
      
      // Handle multi-select dependent fields
      dependentFields.forEach(dependentFieldName => {
        this.handleMultiSelectDependentFieldChange(dependentFieldName, selectedValues);
      });
    }
  }

  onDependentSelectChange(event: any, field: any, parentValue: string) {
    const selectedValue = event.value;
    
    // Check if this field has dependent fields
    if (this.dependentFieldsMap.has(field.name)) {
      const dependentFields = this.dependentFieldsMap.get(field.name)!;
      
      // Clear and reload options for all dependent fields
      dependentFields.forEach(dependentFieldName => {
        this.handleDependentFieldChange(dependentFieldName, selectedValue);
      });
    }
  }

  // Method to update layout when fields change
  private updateLayout(): void {
    this.getLayoutConfig();
  }

  async handleDependentFieldChange(fieldName: string, parentValue: string) {
    const field = this.fields.find(f => f.name === fieldName);
    if (!field || field.optionSource !== 'dependent') return;

    // Prevent infinite loops by checking if the value is actually changing
    const currentValue = this.dynamicForm.get(fieldName)?.value;
    if (currentValue === parentValue) return;

    // Clear the dependent field's value
    this.dynamicForm.get(fieldName)?.setValue('');
    
    // Clear existing options
    field.options = [];
    
    // Load new options only if parent value is not empty
    if (parentValue) {
      await this.loadDependentOptions(field, parentValue);
      // Update layout after options are loaded
      this.updateLayout();
    }
  }

  async handleMultiSelectDependentFieldChange(fieldName: string, parentValues: string[]) {
    const field = this.fields.find(f => f.name === fieldName);
    if (!field || field.optionSource !== 'dependent') return;

    // Clear the dependent field's value
    this.dynamicForm.get(fieldName)?.setValue('');
    
    // Clear existing options
    field.options = [];
    
    // Load new options for each parent value only if there are values
    if (parentValues && parentValues.length > 0) {
      const allOptions: any[] = [];
      for (const parentValue of parentValues) {
        if (parentValue) {
          const options = await this.loadDependentOptions(field, parentValue);
          allOptions.push(...options);
        }
      }
      
      // Remove duplicates
      field.options = this.removeDuplicateOptions(allOptions);
      
      // Update layout after options are loaded
      this.updateLayout();
    }
  }

  removeDuplicateOptions(options: any[]): any[] {
    const seen = new Set();
    return options.filter(option => {
      const duplicate = seen.has(option.value);
      seen.add(option.value);
      return !duplicate;
    });
  }

  async loadDependentOptions(field: any, parentValue: string): Promise<any[]> {
    if (!field.parentField || !field.dependentApiUrl) return [];

    if (!parentValue) return [];

    try {
      const url = field.dependentApiUrl.replace('{parentValue}', parentValue);
      const response = await this.http.get<any[]>(url).toPromise();
      
      if (response && Array.isArray(response)) {
        const options = response.map(item => ({
          label: item[field.dependentLabelKey || 'name'],
          value: item[field.dependentValueKey || 'id']
        }));
        
        // Cache the options
        if (!this.dependentFieldOptions.has(field.name)) {
          this.dependentFieldOptions.set(field.name, new Map());
        }
        this.dependentFieldOptions.get(field.name)!.set(parentValue, options);
        
        return options;
      }
    } catch (error) {
      console.error(`Error loading dependent options for ${field.name}:`, error);
      // Return empty array on error to prevent hanging
      return [];
    }
    
    return [];
  }

  getLayoutConfig(): any[][] {
    // Create a hash of the fields to check if they've changed
    const fieldsHash = JSON.stringify(this.fields.map(f => ({ id: f.id, width: f.width, rowPosition: f.rowPosition })));
    
    // If fields haven't changed and we have cached layout, return it
    if (this._fieldsHash === fieldsHash && this._layoutConfigCache.length > 0) {
      this.layoutConfig = this._layoutConfigCache;
      return this._layoutConfigCache;
    }

    // Calculate new layout
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

    // Cache the result and update hash
    this._layoutConfigCache = rows;
    this._fieldsHash = fieldsHash;
    this.layoutConfig = rows;

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

  async loadInitialOptions() {
    const promises = [];
    for (const field of this.fields) {
      if (field.optionSource === 'api') {
        promises.push(this.loadApiOptions(field));
      }
    }
    
    // Wait for all API options to load
    await Promise.all(promises);
    
    // Update layout after all options are loaded
    this.updateLayout();
  }

  async loadApiOptions(field: any): Promise<any[]> {
    if (!field.apiUrl) return [];

    try {
      const response = await this.http.get<any[]>(field.apiUrl).toPromise();
      
      if (response && Array.isArray(response)) {
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
      // Return empty array on error to prevent hanging
      return [];
    }
    
    return [];
  }

  // New methods for enhanced form handling
  isParentMultiSelect(field: any): boolean {
    const parentField = this.fields.find(f => f.name === field.parentField);
    return parentField && parentField.multiple;
  }

  getParentSelectedValues(field: any): string[] {
    const parentFieldName = field.parentField;
    const parentValue = this.dynamicForm.get(parentFieldName)?.value;
    return Array.isArray(parentValue) ? parentValue : [];
  }

  getParentSelectedValue(field: any): string {
    const parentFieldName = field.parentField;
    return this.dynamicForm.get(parentFieldName)?.value || '';
  }

  getParentLabel(field: any, parentValue: string): string {
    const parentField = this.fields.find(f => f.name === field.parentField);
    if (!parentField || !parentField.options) return parentValue;
    
    const option = parentField.options.find((opt: any) => opt.value === parentValue);
    return option ? option.label : parentValue;
  }

  getDependentControlName(field: any, parentValue: string): string {
    return `${field.name}_${parentValue}`;
  }

  getDependentOptions(field: any, parentValue: string): any[] {
    if (!this.dependentFieldOptions.has(field.name)) {
      return [];
    }
    
    const fieldOptions = this.dependentFieldOptions.get(field.name)!;
    return fieldOptions.get(parentValue) || [];
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

  onCheckboxChange(event: any, fieldName: string, value: string) {
    const control = this.dynamicForm.get(fieldName);
    if (control) {
      if (event.target.checked) {
        control.setValue(value);
      } else {
        control.setValue('');
      }
    }
  }
}
