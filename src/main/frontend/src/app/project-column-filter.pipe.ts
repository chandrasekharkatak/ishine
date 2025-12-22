import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  standalone:false,
  name: 'projectColumnFilter'
})
export class ProjectColumnFilterPipe implements PipeTransform {

  transform(projectList: any[], filters: any): any[] {
    if (!projectList || !filters || Object.keys(filters).length === 0) {
      return projectList;
    }

    const normalizedFilters = {};
    Object.keys(filters).forEach(key => {
      normalizedFilters[key] = filters[key].toLowerCase();
    });

    return projectList
      .map(project => {
        const filteredTeams = project.teamDetails
          .map(team => {
            const filteredEmployees = team.mappedEmployeeDetails.filter(emp => {

              return Object.keys(normalizedFilters).every(filterKey => {
                const searchValue = normalizedFilters[filterKey];

                const valueMap = {
                  projectName: project.projectName,
                  projectManager: project.projectManager,
                  apmosysRM: project.apmosysRM,
                  clientRM: project.clientRM,
                  poNo: project.poNo,
                  poProjectType: project.poProjectType,
                  teamName: team.teamName,
                  employeeName: emp.employeeName,
                  jobRole: emp.jobRole,
                  deptName: emp.deptName,
                  mobileNo: emp.mobileNo?.toString(),
                  email: emp.email,
                  billable: emp.billable,
                  billableType: emp.billableType
                };

                const fieldValue = valueMap[filterKey];

                return fieldValue
                  ?.toString()
                  .toLowerCase()
                  .includes(searchValue);
              });

            });

            return filteredEmployees.length
              ? { ...team, mappedEmployeeDetails: filteredEmployees }
              : null;
          })
          .filter(team => team !== null);

        return filteredTeams.length
          ? { ...project, teamDetails: filteredTeams }
          : null;
      })
      .filter(project => project !== null);
  }
}
