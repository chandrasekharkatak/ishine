"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_skill-matrix_skill-matrix_module_ts"],{

/***/ 5024:
/*!****************************************************************************!*\
  !*** ./src/app/module-routing/skill-matrix/skill-matrix-routing.module.ts ***!
  \****************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   SkillMatrixRoutingModule: () => (/* binding */ SkillMatrixRoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_skill_matrix_skill_matrix_container_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/skill-matrix/skill-matrix-container.component */ 29762);
/* harmony import */ var src_app_skill_matrix_skill_matrix_submit_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/skill-matrix/skill-matrix-submit.component */ 89052);
/* harmony import */ var src_app_skill_matrix_skill_matrix_my_submissions_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/skill-matrix/skill-matrix-my-submissions.component */ 74861);
/* harmony import */ var src_app_skill_matrix_skill_matrix_approve_requests_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/skill-matrix/skill-matrix-approve-requests.component */ 14605);
/* harmony import */ var src_app_skill_matrix_skill_matrix_master_configuration_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! src/app/skill-matrix/skill-matrix-master-configuration.component */ 84322);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! @angular/core */ 37580);








const routes = [{
  path: '',
  component: src_app_skill_matrix_skill_matrix_container_component__WEBPACK_IMPORTED_MODULE_0__.SkillMatrixContainerComponent,
  children: [{
    path: '',
    pathMatch: 'full',
    redirectTo: 'submit-for-review'
  }, {
    path: 'submit-for-review',
    component: src_app_skill_matrix_skill_matrix_submit_component__WEBPACK_IMPORTED_MODULE_1__.SkillMatrixSubmitComponent
  }, {
    path: 'my-submissions',
    component: src_app_skill_matrix_skill_matrix_my_submissions_component__WEBPACK_IMPORTED_MODULE_2__.SkillMatrixMySubmissionsComponent
  }, {
    path: 'approve-requests',
    component: src_app_skill_matrix_skill_matrix_approve_requests_component__WEBPACK_IMPORTED_MODULE_3__.SkillMatrixApproveRequestsComponent
  }, {
    path: 'master-configuration',
    component: src_app_skill_matrix_skill_matrix_master_configuration_component__WEBPACK_IMPORTED_MODULE_4__.SkillMatrixMasterConfigurationComponent
  }]
}];
class SkillMatrixRoutingModule {
  static {
    this.ɵfac = function SkillMatrixRoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || SkillMatrixRoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵdefineNgModule"]({
      type: SkillMatrixRoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_5__["ɵɵsetNgModuleScope"](SkillMatrixRoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_6__.RouterModule]
  });
})();

/***/ }),

/***/ 35089:
/*!********************************************************************!*\
  !*** ./src/app/module-routing/skill-matrix/skill-matrix.module.ts ***!
  \********************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   SkillMatrixModule: () => (/* binding */ SkillMatrixModule)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _skill_matrix_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./skill-matrix-routing.module */ 5024);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class SkillMatrixModule {
  static {
    this.ɵfac = function SkillMatrixModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || SkillMatrixModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: SkillMatrixModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _skill_matrix_routing_module__WEBPACK_IMPORTED_MODULE_0__.SkillMatrixRoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](SkillMatrixModule, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _skill_matrix_routing_module__WEBPACK_IMPORTED_MODULE_0__.SkillMatrixRoutingModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_skill-matrix_skill-matrix_module_ts.js.map