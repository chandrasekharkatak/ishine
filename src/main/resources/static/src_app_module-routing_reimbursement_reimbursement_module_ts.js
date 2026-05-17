"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_reimbursement_reimbursement_module_ts"],{

/***/ 34965:
/*!**********************************************************************!*\
  !*** ./src/app/module-routing/reimbursement/reimbursement.module.ts ***!
  \**********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   ReimbursementModule: () => (/* binding */ ReimbursementModule)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _reimbursement_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./reimbursement-routing.module */ 84628);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class ReimbursementModule {
  static {
    this.ɵfac = function ReimbursementModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || ReimbursementModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: ReimbursementModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _reimbursement_routing_module__WEBPACK_IMPORTED_MODULE_0__.ReimbursementRoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](ReimbursementModule, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _reimbursement_routing_module__WEBPACK_IMPORTED_MODULE_0__.ReimbursementRoutingModule]
  });
})();

/***/ }),

/***/ 84628:
/*!******************************************************************************!*\
  !*** ./src/app/module-routing/reimbursement/reimbursement-routing.module.ts ***!
  \******************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   ReimbursementRoutingModule: () => (/* binding */ ReimbursementRoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_reimbursement_my_reimbursement_my_reimbursement_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/reimbursement/my-reimbursement/my-reimbursement.component */ 6103);
/* harmony import */ var src_app_reimbursement_reimbursement_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/reimbursement/reimbursement.component */ 40772);
/* harmony import */ var src_app_reimbursement_reimbursementapproval_reimbursementapproval_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/reimbursement/reimbursementapproval/reimbursementapproval.component */ 22967);
/* harmony import */ var src_app_reimbursement_total_reimbursementrequest_total_reimbursementrequest_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/reimbursement/total-reimbursementrequest/total-reimbursementrequest.component */ 96685);
/* harmony import */ var src_app_reimbursement_view_reimbursement_view_reimbursement_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! src/app/reimbursement/view-reimbursement/view-reimbursement.component */ 89057);
/* harmony import */ var src_app_reimbursement_reimbursement_dashboard_reimbursement_dashboard_component__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! src/app/reimbursement/reimbursement-dashboard/reimbursement-dashboard.component */ 85891);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @angular/core */ 37580);









const routes = [{
  path: '',
  component: src_app_reimbursement_reimbursement_component__WEBPACK_IMPORTED_MODULE_1__.ReimbursementComponent,
  children: [{
    path: '',
    redirectTo: 'my-reimbursement',
    pathMatch: 'full'
  }, {
    path: 'my-reimbursement',
    component: src_app_reimbursement_my_reimbursement_my_reimbursement_component__WEBPACK_IMPORTED_MODULE_0__.MyReimbursementComponent
  }, {
    path: 'view-reimbursement',
    component: src_app_reimbursement_view_reimbursement_view_reimbursement_component__WEBPACK_IMPORTED_MODULE_4__.ViewReimbursementComponent
  }, {
    path: 'approve-reimbursement',
    component: src_app_reimbursement_reimbursementapproval_reimbursementapproval_component__WEBPACK_IMPORTED_MODULE_2__.ReimbursementapprovalComponent
  }, {
    path: 'reimbursement-dashboard',
    component: src_app_reimbursement_reimbursement_dashboard_reimbursement_dashboard_component__WEBPACK_IMPORTED_MODULE_5__.ReimbursementDashboardComponent
  }, {
    path: 'total-reimbursement',
    component: src_app_reimbursement_total_reimbursementrequest_total_reimbursementrequest_component__WEBPACK_IMPORTED_MODULE_3__.TotalReimbursementrequestComponent
  }]
}];
class ReimbursementRoutingModule {
  static {
    this.ɵfac = function ReimbursementRoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || ReimbursementRoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_6__["ɵɵdefineNgModule"]({
      type: ReimbursementRoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_6__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_7__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_7__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_6__["ɵɵsetNgModuleScope"](ReimbursementRoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_7__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_7__.RouterModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_reimbursement_reimbursement_module_ts.js.map