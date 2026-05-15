"use strict";
(self["webpackChunkemployee_portal_revamp"] = self["webpackChunkemployee_portal_revamp"] || []).push([["src_app_module-routing_project-insight_project-insight_module_ts"],{

/***/ 13973:
/*!**************************************************************************!*\
  !*** ./src/app/module-routing/project-insight/project-insight.module.ts ***!
  \**************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   ProjectInsightModule: () => (/* binding */ ProjectInsightModule)
/* harmony export */ });
/* harmony import */ var _angular_common__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! @angular/common */ 84460);
/* harmony import */ var _project_insight_routing_module__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! ./project-insight-routing.module */ 28628);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! @angular/core */ 37580);



class ProjectInsightModule {
  static {
    this.ɵfac = function ProjectInsightModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || ProjectInsightModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineNgModule"]({
      type: ProjectInsightModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵdefineInjector"]({
      imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _project_insight_routing_module__WEBPACK_IMPORTED_MODULE_0__.ProjectInsightRoutingModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_1__["ɵɵsetNgModuleScope"](ProjectInsightModule, {
    imports: [_angular_common__WEBPACK_IMPORTED_MODULE_2__.CommonModule, _project_insight_routing_module__WEBPACK_IMPORTED_MODULE_0__.ProjectInsightRoutingModule]
  });
})();

/***/ }),

/***/ 28628:
/*!**********************************************************************************!*\
  !*** ./src/app/module-routing/project-insight/project-insight-routing.module.ts ***!
  \**********************************************************************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   ProjectInsightRoutingModule: () => (/* binding */ ProjectInsightRoutingModule)
/* harmony export */ });
/* harmony import */ var _angular_router__WEBPACK_IMPORTED_MODULE_7__ = __webpack_require__(/*! @angular/router */ 18431);
/* harmony import */ var src_app_user_team_Domain_Domain_component__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! src/app/user-team/Domain/Domain.component */ 84882);
/* harmony import */ var src_app_user_team_form_builder_form_builder_component__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! src/app/user-team/form-builder/form-builder.component */ 68022);
/* harmony import */ var src_app_user_team_KnowledgeHub_KnowledgeHub_component__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! src/app/user-team/KnowledgeHub/KnowledgeHub.component */ 19320);
/* harmony import */ var src_app_user_team_project_insight_components_project_insight_details_project_insight_details_component__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! src/app/user-team/project-insight/components/project-insight-details/project-insight-details.component */ 23080);
/* harmony import */ var src_app_user_team_project_insight_components_project_insight_question_library_project_insight_question_library_component__WEBPACK_IMPORTED_MODULE_4__ = __webpack_require__(/*! src/app/user-team/project-insight/components/project-insight-question-library/project-insight-question-library.component */ 82840);
/* harmony import */ var src_app_user_team_project_insight_project_insight_component__WEBPACK_IMPORTED_MODULE_5__ = __webpack_require__(/*! src/app/user-team/project-insight/project-insight.component */ 69240);
/* harmony import */ var _angular_core__WEBPACK_IMPORTED_MODULE_6__ = __webpack_require__(/*! @angular/core */ 37580);









const routes = [{
  path: '',
  component: src_app_user_team_project_insight_project_insight_component__WEBPACK_IMPORTED_MODULE_5__.ProjectInsightComponent,
  children: [{
    path: 'project-insight-details',
    component: src_app_user_team_project_insight_components_project_insight_details_project_insight_details_component__WEBPACK_IMPORTED_MODULE_3__.ProjectInsightDetailsComponent,
    children: [{
      path: 'department-forms',
      component: src_app_user_team_form_builder_form_builder_component__WEBPACK_IMPORTED_MODULE_1__.FormBuilderComponent
    }, {
      path: "knowledge-hub",
      component: src_app_user_team_KnowledgeHub_KnowledgeHub_component__WEBPACK_IMPORTED_MODULE_2__.KnowledgeHubComponent
    }, {
      path: "domains",
      component: src_app_user_team_Domain_Domain_component__WEBPACK_IMPORTED_MODULE_0__.DomainComponent
    }, {
      path: 'question-library',
      component: src_app_user_team_project_insight_components_project_insight_question_library_project_insight_question_library_component__WEBPACK_IMPORTED_MODULE_4__.ProjectInsightQuestionLibraryComponent
    }]
  }]
}];
class ProjectInsightRoutingModule {
  static {
    this.ɵfac = function ProjectInsightRoutingModule_Factory(__ngFactoryType__) {
      return new (__ngFactoryType__ || ProjectInsightRoutingModule)();
    };
  }
  static {
    this.ɵmod = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_6__["ɵɵdefineNgModule"]({
      type: ProjectInsightRoutingModule
    });
  }
  static {
    this.ɵinj = /*@__PURE__*/_angular_core__WEBPACK_IMPORTED_MODULE_6__["ɵɵdefineInjector"]({
      imports: [_angular_router__WEBPACK_IMPORTED_MODULE_7__.RouterModule.forChild(routes), _angular_router__WEBPACK_IMPORTED_MODULE_7__.RouterModule]
    });
  }
}
(function () {
  (typeof ngJitMode === "undefined" || ngJitMode) && _angular_core__WEBPACK_IMPORTED_MODULE_6__["ɵɵsetNgModuleScope"](ProjectInsightRoutingModule, {
    imports: [_angular_router__WEBPACK_IMPORTED_MODULE_7__.RouterModule],
    exports: [_angular_router__WEBPACK_IMPORTED_MODULE_7__.RouterModule]
  });
})();

/***/ })

}]);
//# sourceMappingURL=src_app_module-routing_project-insight_project-insight_module_ts.js.map