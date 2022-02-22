package dev.moontm.giveawaybot.systems.giveaway;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.systems.giveaway.dao.GiveawayRepository;
import dev.moontm.giveawaybot.systems.giveaway.jobs.GiveawayStartJob;
import dev.moontm.giveawaybot.systems.giveaway.model.Giveaway;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.Date;
import java.util.List;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Slf4j
public class GiveawayStateManager {
	private List<Giveaway> activeGiveaways;
	private Scheduler scheduler;

	public GiveawayStateManager() {
		try {
			scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.start();
			this.activeGiveaways = new GiveawayRepository(Bot.dataSource.getConnection()).getActive();

			for (Giveaway giveaway : activeGiveaways){
				scheduleGiveaway(giveaway);
			}
			log.info("Scheduled {} giveaways.", activeGiveaways.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void scheduleGiveaway(Giveaway giveaway) {
		JobDetail job = newJob(GiveawayStartJob.class)
				.withIdentity(String.valueOf(giveaway.getId()))
				.build();
		Date runTime = new Date(giveaway.getDueAt().getTime());
		Trigger trigger = newTrigger()
				.withIdentity(String.valueOf(giveaway.getId()))
				.startAt(runTime)
				.build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public void cancelSchedule(Giveaway giveaway){
		try {
			scheduler.deleteJob(JobKey.jobKey(String.valueOf(giveaway.getId())));
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	public void triggerJob(Giveaway giveaway) {
		try {
			scheduler.triggerJob(JobKey.jobKey(String.valueOf(giveaway.getId())));
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}
