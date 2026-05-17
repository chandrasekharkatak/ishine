package com.apmosys.employeeportal.service;

import org.springframework.stereotype.Service;

import com.apmosys.employeeportal.model.PortalConfig;
import com.apmosys.employeeportal.repository.PortalConfigRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

@Service
public class DynamicScheduler implements SchedulingConfigurer {
	
	@Autowired
	PortalConfigRepository portalConfigRepository;
	
	@Autowired
	CronJobService cronJobService;

	@Bean
    public TaskScheduler poolScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        scheduler.setPoolSize(1);
        scheduler.initialize();
        return scheduler;
    }
	
	public String getCronExpression() {
		String cronExpression = null;
		String dsrDay = null;
		List<PortalConfig> portalConfig = portalConfigRepository.findAll();
		if(!portalConfig.isEmpty()) {
			for(PortalConfig obj: portalConfig)
				if(obj.getConfigName().equals("DSR Day")) {
					dsrDay = obj.getConfigValue();
				}
			cronExpression = "0 0 4 "+dsrDay+" * ?";
			System.out.println(cronExpression + " cronExpresssion");
		}
		return cronExpression;
	}
	
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		taskRegistrar.setScheduler(poolScheduler());
		
		taskRegistrar.addTriggerTask(() -> scheduleCron(getCronExpression()), t -> {
            CronTrigger crontrigger = new CronTrigger(getCronExpression());
            return crontrigger.nextExecutionTime(t);
        });
	}

	private void scheduleCron(String cronExpression) {
		System.out.println(cronExpression + " : cronExpression");
		cronJobService.monthlyTimesheetExcelGenerator();
	}

}
