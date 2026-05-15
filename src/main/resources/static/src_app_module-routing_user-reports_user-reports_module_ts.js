"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_user-reports_user-reports_module_ts"],{

/***/ 13152:
/*!****************************************************************************!*\
  !*** ./src/app/module-routing/user-reports/user-reports-routing.module.ts ***!
  \****************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UserReportsRoutingModule: () => (/* binding */ UserReportsRoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_user_report_attendance_reconciliation_attendance_reconciliation_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/user-report/attendance-reconciliation/attendance-reconciliation.component */ 41875);
/* harmony import */ var src_app_user_report_query_master_query_master_query_master_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/user-report/query-master/query-master/query-master.component */ 58901);
/* harmony import */ var src_app_user_report_report_dashboard_report_dashboard_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/user-report/report-dashboard/report-dashboard.component */ 24827);
/* harmony import */ var src_app_user_report_report_list_report_list_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/user-report/report-list/report-list.component */ 4811);
/* harmony import */ var src_app_user_report_user_report_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! src/app/user-report/user-report.component */ 89372);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/core */ 37580);








const routes = [{
  path: '',
  component: src_app_user_report_user_report_component__WEBPACK_IMPORTED_MODULE_4__.UserReportComponent,
  children: [{
    path: 'report-list',
    component: src_app_user_report_report_list_report_list_component__WEBPACK_IMPORTED_MODULE_3__.ReportListComponent
  }, {
    path: 'report-dashboard',
    component: src_app_user_report_report_dashboard_report_dashboard_component__WEBPACK_IMPORTED_MODULE_2__.ReportDashboardComponent
  }, {
    path: 'query-master',
    component: src_app_user_report_query_master_query_master_query_master_component__WEBPACK_IMPORTED_MODULE_1__.QueryMasterComponent
  }, {
    path: 'attendance-reconciliation',
    component: src_app_user_report_attendance_reconciliation_attendance_reconciliation_component__WEBPACK_IMPORTED_MODULE_0__.AttendanceReconciliationComponent
  }]
}];
class UserReportsRoutingModule {
  static {
    this.ɵfac = function UserReportsRoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UserReportsRoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵdefineNgModule"]({
      type: UserReportsRoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵsetNgModuleScope"](UserReportsRoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule]
  });
})();

/***/ }),

/***/ 87089:
/*!********************************************************************!*\
  !*** ./src/app/module-routing/user-reports/user-reports.module.ts ***!
  \********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   UserReportsModule: () => (/* binding */ UserReportsModule)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _user_reports_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./user-reports-routing.module */ 13152);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class UserReportsModule {
  static {
    this.ɵfac = function UserReportsModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || UserReportsModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: UserReportsModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _user_reports_routing_module__WEBPACK_IMPORTED_MODULE_0__.UserReportsRoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](UserReportsModule, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _user_reports_routing_module__WEBPACK_IMPORTED_MODULE_0__.UserReportsRoutingModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_user-reports_user-reports_module_ts.js.map