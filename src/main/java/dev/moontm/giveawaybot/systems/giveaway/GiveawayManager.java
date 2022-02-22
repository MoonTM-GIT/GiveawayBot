package dev.moontm.giveawaybot.systems.giveaway;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.systems.giveaway.dao.GiveawayRepository;
import dev.moontm.giveawaybot.systems.giveaway.model.Giveaway;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GiveawayManager {
	public GiveawayManager() {
		Runnable checksRunnable = new Runnable() {
			public void run() {
				try {
					List<Giveaway> giveaways = new GiveawayRepository(Bot.dataSource.getConnection()).getActive();
					for (Giveaway giveaway:giveaways) {
						//Check if Guild still exists
						Guild guild = Bot.jda.getGuildById(giveaway.getGuildId());
						if (guild == null) {
							new GiveawayRepository(Bot.dataSource.getConnection()).markInactive(giveaway.getId());
							Bot.giveawayStateManager.cancelSchedule(giveaway);
							continue;
						}
						//Check if Channel still exists
						TextChannel channel = Bot.jda.getTextChannelById(giveaway.getChannelId());
						if (channel == null) {
							new GiveawayRepository(Bot.dataSource.getConnection()).markInactive(giveaway.getId());
							Bot.giveawayStateManager.cancelSchedule(giveaway);
							continue;
						}
						//Check if Message still exists
						channel.retrieveMessageById(giveaway.getMessageId()).queue(m -> {}, e -> {
							try {
								new GiveawayRepository(Bot.dataSource.getConnection()).markInactive(giveaway.getId());
								Bot.giveawayStateManager.cancelSchedule(giveaway);
							} catch (SQLException ex) {
								e.printStackTrace();
							}
						});
						if (giveaway.getDueAt().before(Timestamp.from(Instant.now()))) Bot.giveawayStateManager.triggerJob(giveaway);
					}
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		};
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(checksRunnable, 2, 10, TimeUnit.MINUTES);
	}

	public Giveaway addParticipant(Giveaway giveaway, long participantId) {
		long[] newParticipants = ArrayUtils.add(giveaway.getParticipants(), participantId);
		try {
			return new GiveawayRepository(Bot.dataSource.getConnection()).updateParticipants(giveaway, newParticipants);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Giveaway removeParticipant(Giveaway giveaway, long participantId) {
		long[] newParticipants = ArrayUtils.removeElement(giveaway.getParticipants(), participantId);
		try {
			return new GiveawayRepository(Bot.dataSource.getConnection()).updateParticipants(giveaway, newParticipants);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
