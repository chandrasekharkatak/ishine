"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_configuration_configuration_module_ts"],{

/***/ 5816:
/*!******************************************************************************!*\
  !*** ./src/app/module-routing/configuration/configuration-routing.module.ts ***!
  \******************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   ConfigurationRoutingModule: () => (/* binding */ ConfigurationRoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_23__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_configuration_configuration_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/configuration/configuration.component */ 97872);
/* harmony import */ var src_app_configuration_dept_config_dept_config_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/configuration/dept-config/dept-config.component */ 35589);
/* harmony import */ var src_app_configuration_designation_config_designation_config_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/configuration/designation-config/designation-config.component */ 81251);
/* harmony import */ var src_app_configuration_document_document_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/configuration/document/document.component */ 31553);
/* harmony import */ var src_app_configuration_domain_config_domain_config_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! src/app/configuration/domain-config/domain-config.component */ 77277);
/* harmony import */ var src_app_configuration_employee_config_employee_config_component__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! src/app/configuration/employee-config/employee-config.component */ 71849);
/* harmony import */ var src_app_configuration_home_config_home_config_component__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! src/app/configuration/home-config/home-config.component */ 43469);
/* harmony import */ var src_app_configuration_leave_config_leave_config_component__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! src/app/configuration/leave-config/leave-config.component */ 9839);
/* harmony import */ var src_app_configuration_newsletter_config_newsletter_config_component__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! src/app/configuration/newsletter-config/newsletter-config.component */ 93477);
/* harmony import */ var src_app_configuration_on_boarding_on_boarding_component__WEBPACK_IMPORTED_MODULE_9__ = __webpack_require__(/*! src/app/configuration/on-boarding/on-boarding.component */ 79833);
/* harmony import */ var src_app_configuration_others_others_component__WEBPACK_IMPORTED_MODULE_10__ = __webpack_require__(/*! src/app/configuration/others/others.component */ 99745);
/* harmony import */ var src_app_configuration_performance_config_performance_config_component__WEBPACK_IMPORTED_MODULE_11__ = __webpack_require__(/*! src/app/configuration/performance-config/performance-config.component */ 59745);
/* harmony import */ var src_app_configuration_portal_config_portal_config_component__WEBPACK_IMPORTED_MODULE_12__ = __webpack_require__(/*! src/app/configuration/portal-config/portal-config.component */ 38209);
/* harmony import */ var src_app_configuration_reimbursment_config_reimbursment_config_component__WEBPACK_IMPORTED_MODULE_13__ = __webpack_require__(/*! src/app/configuration/reimbursment-config/reimbursment-config.component */ 12985);
/* harmony import */ var src_app_configuration_rewards_config_rewards_config_component__WEBPACK_IMPORTED_MODULE_14__ = __webpack_require__(/*! src/app/configuration/rewards-config/rewards-config.component */ 30753);
/* harmony import */ var src_app_configuration_role_config_role_config_component__WEBPACK_IMPORTED_MODULE_15__ = __webpack_require__(/*! src/app/configuration/role-config/role-config.component */ 13837);
/* harmony import */ var src_app_configuration_skill_certfification_config_skill_certfification_config_component__WEBPACK_IMPORTED_MODULE_16__ = __webpack_require__(/*! src/app/configuration/skill-certfification-config/skill-certfification-config.component */ 75289);
/* harmony import */ var src_app_configuration_survey_config_survey_config_component__WEBPACK_IMPORTED_MODULE_17__ = __webpack_require__(/*! src/app/configuration/survey-config/survey-config.component */ 81537);
/* harmony import */ var src_app_configuration_timesheet_config_timesheet_config_component__WEBPACK_IMPORTED_MODULE_18__ = __webpack_require__(/*! src/app/configuration/timesheet-config/timesheet-config.component */ 23413);
/* harmony import */ var src_app_configuration_travel_config_travel_config_component__WEBPACK_IMPORTED_MODULE_19__ = __webpack_require__(/*! src/app/configuration/travel-config/travel-config.component */ 77269);
/* harmony import */ var src_app_configuration_upload_policies_upload_policies_component__WEBPACK_IMPORTED_MODULE_20__ = __webpack_require__(/*! src/app/configuration/upload-policies/upload-policies.component */ 11125);
/* harmony import */ var src_app_configuration_training_config_training_config_component__WEBPACK_IMPORTED_MODULE_21__ = __webpack_require__(/*! src/app/configuration/training-config/training-config.component */ 56041);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_22__ = __webpack_require__(/*! @angular/core */ 37580);

























const routes = [{
  path: '',
  component: src_app_configuration_configuration_component__WEBPACK_IMPORTED_MODULE_0__.ConfigurationComponent,
  children: [{
    path: 'employee',
    component: src_app_configuration_employee_config_employee_config_component__WEBPACK_IMPORTED_MODULE_5__.EmployeeConfigComponent
  }, {
    path: 'domain',
    component: src_app_configuration_domain_config_domain_config_component__WEBPACK_IMPORTED_MODULE_4__.DomainConfigComponent
  }, {
    path: 'department',
    component: src_app_configuration_dept_config_dept_config_component__WEBPACK_IMPORTED_MODULE_1__.DeptConfigComponent
  }, {
    path: 'role',
    component: src_app_configuration_role_config_role_config_component__WEBPACK_IMPORTED_MODULE_15__.RoleConfigComponent
  }, {
    path: 'leave',
    component: src_app_configuration_leave_config_leave_config_component__WEBPACK_IMPORTED_MODULE_7__.LeaveConfigComponent
  }, {
    path: 'home-config',
    component: src_app_configuration_home_config_home_config_component__WEBPACK_IMPORTED_MODULE_6__.HomeConfigComponent
  }, {
    path: 'portal-config',
    component: src_app_configuration_portal_config_portal_config_component__WEBPACK_IMPORTED_MODULE_12__.PortalConfigComponent
  }, {
    path: 'survey-config',
    component: src_app_configuration_survey_config_survey_config_component__WEBPACK_IMPORTED_MODULE_17__.SurveyConfigComponent
  }, {
    path: 'upload-policies',
    component: src_app_configuration_upload_policies_upload_policies_component__WEBPACK_IMPORTED_MODULE_20__.UploadPoliciesComponent
  }, {
    path: 'on-boarding',
    component: src_app_configuration_on_boarding_on_boarding_component__WEBPACK_IMPORTED_MODULE_9__.OnBoardingComponent
  }, {
    path: 'designation',
    component: src_app_configuration_designation_config_designation_config_component__WEBPACK_IMPORTED_MODULE_2__.DesignationConfigComponent
  }, {
    path: 'newsletter',
    component: src_app_configuration_newsletter_config_newsletter_config_component__WEBPACK_IMPORTED_MODULE_8__.NewsletterConfigComponent
  }, {
    path: 'document',
    component: src_app_configuration_document_document_component__WEBPACK_IMPORTED_MODULE_3__.DocumentComponent
  }, {
    path: 'other',
    component: src_app_configuration_others_others_component__WEBPACK_IMPORTED_MODULE_10__.OthersComponent
  }, {
    path: 'rewards-config',
    component: src_app_configuration_rewards_config_rewards_config_component__WEBPACK_IMPORTED_MODULE_14__.RewardsConfigComponent
  }, {
    path: 'performance-config',
    component: src_app_configuration_performance_config_performance_config_component__WEBPACK_IMPORTED_MODULE_11__.PerformanceConfigComponent
  }, {
    path: 'travel-config',
    component: src_app_configuration_travel_config_travel_config_component__WEBPACK_IMPORTED_MODULE_19__.TravelConfigComponent
  }, {
    path: 'reimbursment-config',
    component: src_app_configuration_reimbursment_config_reimbursment_config_component__WEBPACK_IMPORTED_MODULE_13__.ReimbursmentConfigComponent
  }, {
    path: 'timesheet-config',
    component: src_app_configuration_timesheet_config_timesheet_config_component__WEBPACK_IMPORTED_MODULE_18__.TimesheetConfigComponent
  }, {
    path: 'skill-certfication-config',
    component: src_app_configuration_skill_certfification_config_skill_certfification_config_component__WEBPACK_IMPORTED_MODULE_16__.SkillCertfificationConfigComponent
  }, {
    path: 'training-config',
    component: src_app_configuration_training_config_training_config_component__WEBPACK_IMPORTED_MODULE_21__.TrainingConfigComponent
  }]
}];
class ConfigurationRoutingModule {
  static {
    this.ɵfac = function ConfigurationRoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || ConfigurationRoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_22__["ɵɵdefineNgModule"]({
      type: ConfigurationRoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_22__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_23__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_23__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_22__["ɵɵsetNgModuleScope"](ConfigurationRoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_23__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_23__.RouterModule]
  });
})();

/***/ }),

/***/ 33097:
/*!**********************************************************************!*\
  !*** ./src/app/module-routing/configuration/configuration.module.ts ***!
  \**********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   ConfigurationModule: () => (/* binding */ ConfigurationModule)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _configuration_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./configuration-routing.module */ 5816);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class ConfigurationModule {
  static {
    this.ɵfac = function ConfigurationModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || ConfigurationModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: ConfigurationModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _configuration_routing_module__WEBPACK_IMPORTED_MODULE_0__.ConfigurationRoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](ConfigurationModule, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _configuration_routing_module__WEBPACK_IMPORTED_MODULE_0__.ConfigurationRoutingModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_configuration_configuration_module_ts.js.map