/** Prototype department → skill library (subset of full spec; expand as APIs land). */
export interface SkillMatrixSubmitDeptConfig {
  name: string;
  badgeClass: string;
  requiredSkills: string[];
  optionalSkills: string[];
  subsBySkill: Record<string, string[]>;
  aspirations: string[];
}

function dept(
  name: string,
  badgeClass: string,
  requiredSkills: string[],
  optionalSkills: string[],
  subsBySkill: Record<string, string[]>,
  aspirations: string[]
): SkillMatrixSubmitDeptConfig {
  return { name, badgeClass, requiredSkills, optionalSkills, subsBySkill, aspirations };
}

function genericDept(name: string, badgeClass: string): SkillMatrixSubmitDeptConfig {
  return dept(
    name,
    badgeClass,
    [
      `${name} — core process & delivery`,
      `${name} — tools & systems in daily work`,
      `${name} — stakeholder communication`,
      `${name} — quality & compliance`
    ],
    [`${name} — advanced specialization`, 'Cross-team collaboration', 'Documentation & reporting'],
    {
      [`${name} — core process & delivery`]: [
        'Process execution & handoffs',
        'Issue identification & resolution',
        'Standard operating procedures',
        'Continuous improvement'
      ]
    },
    [`Senior ${name} track`, 'Cross-functional leadership', 'Industry certification']
  );
}

const development = dept(
  'Development',
  'b-pu',
  [
    'React.js / Angular / Vue',
    'Node.js / Python / Java',
    'TypeScript / JavaScript',
    'REST API design & integration',
    'Database (SQL + NoSQL)',
    'System design & architecture',
    'Git & version control',
    'Docker / Kubernetes',
    'Unit & integration testing',
    'Agile / Scrum'
  ],
  [
    'GraphQL',
    'Microservices patterns',
    'AWS / Azure / GCP',
    'Redis / Message queues',
    'React Native / Flutter',
    'CI/CD pipelines'
  ],
  {
    'React.js / Angular / Vue': [
      'Component architecture & patterns',
      'State management (Redux/Context/NgRx)',
      'Routing & navigation',
      'Performance optimization (lazy loading, memoization)',
      'Unit testing (RTL / Jasmine)'
    ],
    'Node.js / Python / Java': [
      'REST API development',
      'Authentication (JWT/OAuth2)',
      'ORM & database integration',
      'Async programming',
      'Unit testing (Mocha/PyTest/JUnit)'
    ],
    'System design & architecture': [
      'High availability & fault tolerance',
      'Scalability patterns',
      'Caching strategies (Redis)',
      'Event-driven design (Kafka/RabbitMQ)'
    ],
    'Docker / Kubernetes': [
      'Dockerfile & multi-stage builds',
      'K8s deployments & services',
      'Ingress & networking',
      'Helm charts'
    ],
    'Database (SQL + NoSQL)': [
      'Schema design & normalization',
      'Query optimization & indexing',
      'MongoDB aggregation pipeline',
      'Database migration strategies'
    ]
  },
  [
    'Solution architect',
    'Tech lead',
    'Full stack specialist',
    'Cloud-native engineer',
    'Engineering manager'
  ]
);

const hr = dept(
  'HR',
  'b-pi',
  [
    'Talent acquisition & sourcing',
    'Job description writing',
    'Structured interviewing',
    'HRBP & employee relations',
    'Performance management',
    'HR analytics & MIS',
    'Payroll processing',
    'Statutory compliance (PF/ESI/TDS/PT)',
    'HRMS tools administration',
    'Onboarding & offboarding'
  ],
  [
    'Compensation benchmarking',
    'L&D program design',
    'POSH compliance',
    'Employer branding'
  ],
  {
    'Talent acquisition & sourcing': [
      'LinkedIn & Naukri sourcing',
      'JD publishing & promotion',
      'ATS management',
      'Offer negotiation & closure'
    ],
    'Performance management': [
      'OKR / KRA goal setting',
      'Mid-year review facilitation',
      '360-degree feedback',
      'Appraisal calibration sessions'
    ],
    'Statutory compliance (PF/ESI/TDS/PT)': [
      'EPF monthly challan',
      'TDS on salary computation',
      'Form 16 generation',
      'Annual returns filing'
    ],
    'Payroll processing': [
      'Payroll calculation (CTC to in-hand)',
      'LOP & attendance integration',
      'Payslip generation',
      'Full & final settlement'
    ]
  },
  ['HR Manager', 'Compensation & benefits specialist', 'L&D head', 'CHRO track', 'HR analytics specialist']
);

const accounts = dept(
  'Accounts',
  'b-am',
  [
    'Bookkeeping & ledger management',
    'GST filing & compliance',
    'TDS / TCS computation',
    'Financial reporting',
    'Bank reconciliation',
    'Accounts payable & receivable',
    'Payroll accounting',
    'Tally / accounting software'
  ],
  ['SAP FICO', 'IFRS / Ind AS', 'Internal audit', 'MIS reporting', 'ERP integration'],
  {
    'GST filing & compliance': [
      'GSTR-1 filing',
      'GSTR-3B filing',
      'E-invoicing',
      'Reconciliation with GSTR-2B'
    ],
    'TDS / TCS computation': [
      'Salary TDS (26Q)',
      'Non-salary TDS (24Q)',
      'Form 16 / 16A generation',
      'TDS returns filing'
    ],
    'Financial reporting': ['P&L statement', 'Balance sheet', 'Cash flow analysis', 'MIS dashboards'],
    'Tally / accounting software': [
      'Voucher entry',
      'Bank reconciliation in Tally',
      'GST reports',
      'Inventory management'
    ]
  },
  ['SAP FICO specialist', 'Chartered Accountancy', 'CFO track', 'Forensic accounting']
);

const apm = dept(
  'APM',
  'b-bl',
  [
    'Dynatrace',
    'New Relic',
    'AppDynamics',
    'Application monitoring setup',
    'Distributed tracing',
    'Alert configuration & SLA',
    'Performance baselining',
    'Incident response for perf issues'
  ],
  ['OpenTelemetry', 'Splunk', 'Datadog', 'Log analysis (ELK)', 'Prometheus/Grafana'],
  {
    Dynatrace: [
      'OneAgent deployment',
      'Problem detection & RCA',
      'Dashboard & reporting',
      'Synthetic monitoring'
    ],
    'New Relic': ['APM agent setup', 'NRQL query writing', 'Alert policies', 'Infrastructure monitoring'],
    AppDynamics: ['Agent installation', 'Business transaction mapping', 'Baseline configuration', 'Health rules'],
    'Alert configuration & SLA': [
      'Threshold-based alerts',
      'SLO/SLI definition',
      'Escalation policies',
      'On-call rotation setup'
    ]
  },
  ['SRE practices', 'Kubernetes observability', 'FinOps', 'OpenTelemetry specialist', 'Observability architect']
);

const automationTesting = dept(
  'Automation Testing',
  'b-te',
  [
    'Selenium WebDriver',
    'Cypress / Playwright',
    'TestNG / JUnit / PyTest',
    'Page Object Model / frameworks',
    'CI/CD integration (Jenkins/GitLab)',
    'Python / Java for automation',
    'API automation (Rest Assured / Postman)',
    'Test reporting (Allure / ExtentReports)'
  ],
  ['Appium (mobile)', 'BDD / Cucumber', 'k6 / Gatling', 'Docker for test env', 'Jira / Zephyr / TestRail'],
  {
    'Selenium WebDriver': [
      'Element locator strategies',
      'Cross-browser testing',
      'Selenium Grid / parallel execution',
      'Screenshot & reporting'
    ],
    'Cypress / Playwright': [
      'Intercept & stub network calls',
      'CI pipeline integration',
      'Parallel test execution',
      'Visual regression testing'
    ],
    'Page Object Model / frameworks': [
      'Page class design',
      'Test data management',
      'Reusable utilities',
      'Reporting integration'
    ],
    'API automation (Rest Assured / Postman)': [
      'Request chaining',
      'Auth (Basic/OAuth/JWT)',
      'Response validation',
      'Newman CLI runner'
    ],
    'CI/CD integration (Jenkins/GitLab)': [
      'Pipeline script writing',
      'Test results publishing',
      'Slack/email notifications',
      'Artifact management'
    ]
  },
  ['SDET role', 'Performance testing', 'Cloud testing (BrowserStack)', 'AI-driven testing', 'QA architect']
);

/** Departments with rich prototype data (name must match button label). */
const RICH: Record<string, SkillMatrixSubmitDeptConfig> = {
  Development: development,
  HR: hr,
  Accounts: accounts,
  APM: apm,
  'Automation Testing': automationTesting
};

const OTHER_NAMES: { name: string; badge: string }[] = [
  { name: 'Super Admin', badge: 'b-gy' },
  { name: 'Business Development', badge: 'b-am' },
  { name: 'Functional Testing', badge: 'b-te' },
  { name: 'IT', badge: 'b-gy' },
  { name: 'Performance Testing', badge: 'b-bl' },
  { name: 'Production Support', badge: 'b-re' },
  { name: 'Security Testing', badge: 'b-re' },
  { name: 'Admin', badge: 'b-gy' },
  { name: 'Director', badge: 'b-pu' },
  { name: 'Resource Management Group', badge: 'b-am' },
  { name: 'Presales', badge: 'b-gr' },
  { name: 'Production Support 24x7', badge: 'b-re' },
  { name: 'Miscellaneous', badge: 'b-gy' },
  { name: 'RPA', badge: 'b-bl' },
  { name: 'Products and RND', badge: 'b-pu' },
  { name: 'Consultant', badge: 'b-am' },
  { name: 'Training', badge: 'b-gr' },
  { name: 'Floor Automation', badge: 'b-gy' },
  { name: 'Legal', badge: 'b-gy' },
  { name: 'ABCOE', badge: 'b-pu' },
  { name: 'CEO Office', badge: 'b-pu' }
];

const RICH_ORDER = [development, hr, accounts, apm, automationTesting];

export const SKILL_MATRIX_SUBMIT_DEPARTMENTS: SkillMatrixSubmitDeptConfig[] = [
  ...RICH_ORDER,
  ...OTHER_NAMES.map((o) => RICH[o.name] ?? genericDept(o.name, o.badge))
];
