"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_user-performance_user-performance_module_ts"],{

/***/ 2958:
/*!************************************************************************************!*\
  !*** ./src/app/module-routing/user-performance/user-performance-routing.module.ts ***!
  \************************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UserPerformanceRoutingModule: () => (/* binding */ UserPerformanceRoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_8__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_user_performance_performance_dashboard_performance_dashboard_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/user-performance/performance-dashboard/performance-dashboard.component */ 22549);
/* harmony import */ var src_app_user_performance_performance_management_system_performance_management_system_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/user-performance/performance-management-system/performance-management-system.component */ 94402);
/* harmony import */ var src_app_user_performance_quarter_cycle_quarter_cycle_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/user-performance/quarter-cycle/quarter-cycle.component */ 89653);
/* harmony import */ var src_app_user_performance_team_dashboard_team_dashboard_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/user-performance/team-dashboard/team-dashboard.component */ 33979);
/* harmony import */ var src_app_user_performance_templates_templates_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! src/app/user-performance/templates/templates.component */ 74677);
/* harmony import */ var src_app_user_performance_user_performance_component__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! src/app/user-performance/user-performance.component */ 84690);
/* harmony import */ var src_app_user_performance_view_performance_view_performance_component__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! src/app/user-performance/view-performance/view-performance.component */ 17483);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! @angular/core */ 37580);










const routes = [{
  path: '',
  component: src_app_user_performance_user_performance_component__WEBPACK_IMPORTED_MODULE_5__.UserPerformanceComponent,
  children: [{
    path: '',
    redirectTo: 'performance-dashboard',
    pathMatch: 'full'
  }, {
    path: 'performance-dashboard',
    component: src_app_user_performance_performance_dashboard_performance_dashboard_component__WEBPACK_IMPORTED_MODULE_0__.PerformanceDashboardComponent
  }, {
    path: 'team-dashboard',
    component: src_app_user_performance_team_dashboard_team_dashboard_component__WEBPACK_IMPORTED_MODULE_3__.TeamDashboardComponent
  }, {
    path: 'templates',
    component: src_app_user_performance_templates_templates_component__WEBPACK_IMPORTED_MODULE_4__.TemplatesComponent
  }, {
    path: 'quarter-cycle',
    component: src_app_user_performance_quarter_cycle_quarter_cycle_component__WEBPACK_IMPORTED_MODULE_2__.QuarterCycleComponent
  }, {
    path: 'view-performance/:id',
    component: src_app_user_performance_view_performance_view_performance_component__WEBPACK_IMPORTED_MODULE_6__.ViewPerformanceComponent
  }, {
    path: 'performance-management-system',
    component: src_app_user_performance_performance_management_system_performance_management_system_component__WEBPACK_IMPORTED_MODULE_1__.PerformanceManagementSystemComponent
  }]
}];
class UserPerformanceRoutingModule {
  static {
    this.ɵfac = function UserPerformanceRoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UserPerformanceRoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵdefineNgModule"]({
      type: UserPerformanceRoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_8__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_8__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_7__["ɵɵsetNgModuleScope"](UserPerformanceRoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_8__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_8__.RouterModule]
  });
})();

/***/ }),

/***/ 53815:
/*!****************************************************************************!*\
  !*** ./src/app/module-routing/user-performance/user-performance.module.ts ***!
  \****************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UserPerformanceModule: () => (/* binding */ UserPerformanceModule)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _user_performance_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./user-performance-routing.module */ 2958);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class UserPerformanceModule {
  static {
    this.ɵfac = function UserPerformanceModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UserPerformanceModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: UserPerformanceModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _user_performance_routing_module__WEBPACK_IMPORTED_MODULE_0__.UserPerformanceRoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](UserPerformanceModule, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _user_performance_routing_module__WEBPACK_IMPORTED_MODULE_0__.UserPerformanceRoutingModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_user-performance_user-performance_module_ts.js.map