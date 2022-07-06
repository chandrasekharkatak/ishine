package com.apmosys.employeeportal.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.model.Project;
import com.apmosys.employeeportal.repository.ProjectRepository;

@Service
public class CronJobService {

	@Autowired
	ProjectRepository projectRepository;

	@Value("${po.db.url}")
	private String url;

	@Value("${po.db.username}")
	private String username;

	@Value("${po.db.password}")
	private String password;

	@Scheduled(cron = "0 0 0 * * *")
	public void authenticateUser() {

		System.out.println(LocalDateTime.now());
		try {

			String query = "SELECT cd.clientName,cd.clientLocation,cd.state,pfc.projectName,p.description,p.projectManagerId,u.empId,p.approvedOn FROM PoFixedCost pfc\n"
					+ "INNER JOIN ClientDetails cd ON pfc.clientId = cd.clientid\n"
					+ "INNER JOIN Project p ON p.name = pfc.projectName\n"
					+ "INNER JOIN User u ON u.id = p.projectManagerId\n"
					+ "WHERE p.status = \"Approved\" and p.approvedOn between now() - INTERVAL 5 DAY AND now() ORDER BY p.id DESC";
			Class.forName("com.mysql.cj.jdbc.Driver");
			int i = 0;

			try (Connection con = DriverManager.getConnection(url, username, password)) {
				PreparedStatement ps = con.prepareStatement(query);

				try (ResultSet rs = ps.executeQuery();) {

					while (rs.next()) {

						Project newProject = new Project();

						newProject.setClientName(rs.getString(1) != null ? rs.getString(1) : null);
						newProject.setClientLocation(rs.getString(2) != null ? rs.getString(2) : null);
						newProject.setState(rs.getString(3) != null ? rs.getString(3) : null);
						newProject.setProjectName(rs.getString(4) != null ? rs.getString(4) : null);
						newProject.setDescription(rs.getString(5) != null ? rs.getString(5) : null);
						newProject.setProjectManagerId(rs.getLong(6) != 0L ? rs.getLong(6) : null);

						if (rs.getString(7) != null) {
							String empIdString = rs.getString(7);
							empIdString = empIdString.replace("A-", "");
							newProject.setEmpId(Long.parseLong(empIdString));

						}
						newProject.setApprovedOn(rs.getTimestamp(8) != null ? rs.getTimestamp(8) : null);

						System.out.println(newProject);
						System.out.println(projectRepository.save(newProject) != null
								? "Project " + newProject.getProjectName() + " added to Employee portal"
								: "Failed to add " + newProject.getProjectName() + " project to Employee portal");
						i++;
					}

					System.out.println(
							i == 0 ? "No new projects found at PO portal." : i + " new project(s) found at PO portal");

				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(
						i == 0 ? "No new projects found at PO portal." : i + " new project(s) found at PO portal");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
