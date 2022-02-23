package dev.moontm.giveawaybot.giveaway;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.giveaway.dao.GiveawayRepository;
import dev.moontm.giveawaybot.giveaway.jobs.GiveawayStartJob;
import dev.moontm.giveawaybot.giveaway.model.Giveaway;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.Date;
import java.util.List;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Slf4j
public class GiveawayStateManager {
	private Scheduler scheduler;

	/**
	 * Creates a {@link GiveawayStateManager} instance.
	 * This manages everything related to the {@link org.quartz.core.QuartzScheduler}.
	 */
	public GiveawayStateManager() {
		try {
			scheduler = new StdSchedulerFactory().getScheduler();
			scheduler.start();

			List<Giveaway> activeGiveaways = new GiveawayRepository(Bot.dataSource.getConnection()).getActive();
			for (Giveaway giveaway : activeGiveaways) {
				scheduleGiveaway(giveaway);
			}
			log.info("Scheduled {} giveaways.", activeGiveaways.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Schedules the given {@link Giveaway}.
	 *
	 * @param giveaway The {@link Giveaway} to schedule.
	 */
	public void scheduleGiveaway(Giveaway giveaway) {
		JobDetail job = newJob(GiveawayStartJob.class)
				.withIdentity(String.valueOf(giveaway.getId()))
				.build();
		Date runTime = new Date(giveaway.getDueAt().getTime());
		Trigger trigger = newTrigger()
				.withIdentity(String.valueOf(giveaway.getId()))
				.startAt(runTime)
				.withSchedule(simpleSchedule().withMisfireHandlingInstructionFireNow())
				.build();
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes a {@link Giveaway} schedule.
	 *
	 * @param giveaway The {@link Giveaway} to cancel.
	 */
	public void cancelSchedule(Giveaway giveaway) {
		try {
			scheduler.deleteJob(JobKey.jobKey(String.valueOf(giveaway.getId())));
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
}
