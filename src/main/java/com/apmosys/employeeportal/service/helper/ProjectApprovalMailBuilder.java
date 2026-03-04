package com.apmosys.employeeportal.service.helper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.apmosys.employeeportal.dto.EmployeeMailDTO;
import com.apmosys.employeeportal.model.Employee;
import com.apmosys.employeeportal.model.EmployeeTeamMap;
import com.apmosys.employeeportal.repository.EmployeeRepository;
import com.apmosys.employeeportal.repository.ResourceRequirementRepository;

@Component
public class ProjectApprovalMailBuilder {
      public String buildApprovalMail(
            Employee employeeObj,
            String projectName,
            Map<String, List<EmployeeTeamMap>> modifiedTeamsMap,
            Map<String, List<EmployeeTeamMap>> allTeamsMap,
            EmployeeRepository employeeRepository) {

        StringBuilder html = new StringBuilder();

        Set<Long> empIds = new HashSet<>();

        modifiedTeamsMap.values().stream()
                .flatMap(List::stream)
                .map(EmployeeTeamMap::getEmpId)
                .forEach(empIds::add);

        allTeamsMap.values().stream()
                .flatMap(List::stream)
                .map(EmployeeTeamMap::getEmpId)
                .forEach(empIds::add);

        Map<Long, EmployeeMailDTO> employeeMap = new HashMap<>();

        if (!empIds.isEmpty()) {
            employeeMap = employeeRepository.getEmployeeMailDetails(empIds)
                    .stream()
                    .collect(Collectors.toMap(
                            EmployeeMailDTO::getEmpId,
                            Function.identity(),
                            (existing, replacement) -> existing
                    ));
        }
        html.append("Dear Recipients, <br><br>")
                .append(employeeObj.getName())
                .append(" has approved the project: <b>")
                .append(projectName)
                .append("</b><br>")
                .append("The Project Info with Team & Team Member details will be shared with PoPortal.<br><br>");

        /*
         * ===============================
         * Modified Teams Section
         * ===============================
         */
        if (modifiedTeamsMap != null && !modifiedTeamsMap.isEmpty()) {

            html.append(
                    "<h4 style='color:#2E86C1;font-family:Arial, sans-serif;'>Modified Teams With Employees</h4>");

            html.append(
                    "<table border='1' cellspacing='0' cellpadding='8' style='border-collapse:collapse;width:100%;font-family:Arial, sans-serif;font-size:13px;border:1px solid #BFC9CA;'>");

            html.append(
                    "<thead style='background-color:#2E86C1;color:#FFFFFF;text-align:left;'>")
                    .append("<tr>")
                    .append("<th style='padding:8px;color:#FFFFFF;'>Team Name</th>")
                    .append("<th style='padding:8px;color:#FFFFFF;'>Employee ID</th>")
                    .append("<th style='padding:8px;color:#FFFFFF;'>Employee Name</th>")
                    .append("<th style='padding:8px;color:#FFFFFF;'>Role</th>")
                    .append("</tr>")
                    .append("</thead><tbody>");

            boolean alternate = false;

            for (Map.Entry<String, List<EmployeeTeamMap>> entry : modifiedTeamsMap.entrySet()) {

                String teamName = entry.getKey();
                List<EmployeeTeamMap> members = entry.getValue();

                for (EmployeeTeamMap member : members) {

//                    String name = employeeRepository.getEmployeeName(member.getEmpId());
//
//                    Long employmentId =
//                            employeeRepository.getEmployeeEmployeementId(member.getEmpId());
//
//                    List<String> roleList =
//                            resourceRequirementRepository.getResourceRoleFromEmpId(member.getEmpId());
                	EmployeeMailDTO emp = employeeMap.get(member.getEmpId());

                	String name = emp != null ? emp.getName() : "-";
                	Long employmentId = emp != null ? emp.getEmployeementId() : null;
                	String role = emp != null && emp.getRole() != null ? emp.getRole() : "-";

//                    String role = roleList != null ? String.join(",", roleList) : "-";

                    String rowColor = alternate ? "#F8F9F9" : "#FFFFFF";
                    alternate = !alternate;

                    html.append("<tr style='background-color:")
                            .append(rowColor)
                            .append(";'>")
                            .append("<td style='padding:8px;'>")
                            .append(teamName != null ? teamName : "-")
                            .append("</td>")
                            .append("<td style='padding:8px;'>")
                            .append(employmentId != null ? "A-" + employmentId : "-")
                            .append("</td>")
                            .append("<td style='padding:8px;'>")
                            .append(name != null ? name : "-")
                            .append("</td>")
                            .append("<td style='padding:8px;'>")
                            .append(role)
                            .append("</td>")
                            .append("</tr>");
                }
            }

            html.append("</tbody></table><br><br>");
        }

        /*
         * ===============================
         * All Teams Section
         * ===============================
         */

        if (allTeamsMap != null && !allTeamsMap.isEmpty()) {

            html.append(
                    "<h4 style='color:#2E86C1;font-family:Arial, sans-serif;'>All Teams & Members</h4>");

            for (Map.Entry<String, List<EmployeeTeamMap>> entry : allTeamsMap.entrySet()) {

                String teamName = entry.getKey();
                List<EmployeeTeamMap> members = entry.getValue();

                html.append(
                        "<h5 style='color:#1F618D;margin-top:10px;font-family:Arial, sans-serif;'>Team: ")
                        .append(teamName != null ? teamName : "-")
                        .append("</h5>");

                html.append(
                        "<table border='1' cellspacing='0' cellpadding='8' style='border-collapse:collapse;width:100%;font-family:Arial, sans-serif;font-size:13px;border:1px solid #BFC9CA;'>");

                html.append(
                        "<thead style='background-color:#2874A6;color:#FFFFFF;text-align:left;'>")
                        .append("<tr>")
                        .append("<th style='padding:8px;color:#FFFFFF;'>Employee ID</th>")
                        .append("<th style='padding:8px;color:#FFFFFF;'>Employee Name</th>")
                        .append("<th style='padding:8px;color:#FFFFFF;'>Role</th>")
                        .append("</tr>")
                        .append("</thead><tbody>");

                boolean alternate2 = false;

                for (EmployeeTeamMap member : members) {

//                    String name = employeeRepository.getEmployeeName(member.getEmpId());
//
//                    Long employmentId =
//                            employeeRepository.getEmployeeEmployeementId(member.getEmpId());
//
//                    List<String> roleList =
//                            resourceRequirementRepository.getResourceRoleFromEmpId(member.getEmpId());
//
//                    String role = roleList != null ? String.join(",", roleList) : "-";
                	  EmployeeMailDTO emp = employeeMap.get(member.getEmpId());

                      String name = emp != null ? emp.getName() : "-";
                      Long employmentId = emp != null ? emp.getEmployeementId() : null;
                      String role = emp != null && emp.getRole() != null ? emp.getRole() : "-";

                    

                    String rowColor = alternate2 ? "#F8F9F9" : "#FFFFFF";
                    alternate2 = !alternate2;

                    html.append("<tr style='background-color:")
                            .append(rowColor)
                            .append(";'>")
                            .append("<td style='padding:8px;'>")
                            .append(employmentId != null ? "A-" + employmentId : "-")
                            .append("</td>")
                            .append("<td style='padding:8px;'>")
                            .append(name != null ? name : "-")
                            .append("</td>")
                            .append("<td style='padding:8px;'>")
                            .append(role)
                            .append("</td>")
                            .append("</tr>");
                }

                html.append("</tbody></table><br>");
            }
        }

        html.append("<br><b>Regards,<br>Ishine</b>");

        return html.toString();
    }
}
