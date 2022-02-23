package dev.moontm.giveawaybot.giveaway;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.giveaway.dao.GiveawayRepository;
import dev.moontm.giveawaybot.giveaway.model.Giveaway;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class GiveawayManager {

	public GiveawayManager() {
		Runnable checksRunnable = () -> {
			AtomicInteger i = new AtomicInteger();
			try {
				long startTime = System.nanoTime();
				List<Giveaway> giveaways = new GiveawayRepository(Bot.dataSource.getConnection()).getActive();
				for (Giveaway giveaway : giveaways) {
					//Check if Guild still exists
					Guild guild = Bot.jda.getGuildById(giveaway.getGuildId());
					if (guild == null) {
						deleteGiveaway(giveaway);
						i.getAndIncrement();
						continue;
					}
					//Check if Channel still exists
					TextChannel channel = Bot.jda.getTextChannelById(giveaway.getChannelId());
					if (channel == null) {
						deleteGiveaway(giveaway);
						i.getAndIncrement();
						continue;
					}
					//Check if Message still exists
					channel.retrieveMessageById(giveaway.getMessageId()).queue(message -> {
						message.retrieveReactionUsers(Bot.jda.getEmoteById(Bot.config.getSystems().getGiveawayConfig().getParticipateEmoteId())).queue(reactors -> {
							i.addAndGet(checkReactions(giveaway, reactors));
						});
					}, error -> {
						deleteGiveaway(giveaway);
						i.getAndIncrement();
					});
				}
				long endTime = System.nanoTime();
				long totalTime = ((endTime - startTime) / 1000000);
				log.info("Periodic check took {} Milliseconds, made {} changes.", totalTime, i.intValue());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		};
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(checksRunnable, 2, 5, TimeUnit.MINUTES);
	}

	public void deleteGiveaway(Giveaway giveaway) {
		try {
			new GiveawayRepository(Bot.dataSource.getConnection()).markInactive(giveaway.getId());
			Bot.giveawayStateManager.cancelSchedule(giveaway);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public int checkReactions(Giveaway giveaway, List<User> reactors) {
		try {
			long[] participants = new GiveawayRepository(Bot.dataSource.getConnection()).getById(giveaway.getId()).get().getParticipants();
			AtomicInteger i = new AtomicInteger();
			reactors.forEach(reactor -> {
				if (Arrays.stream(participants).noneMatch(x -> x == reactor.getIdLong()) && canParticipate(reactor)) {
					addParticipant(giveaway, reactor.getIdLong());
					i.getAndIncrement();
				}
			});
			for (long participant : participants) {
				User user = Bot.jda.getUserById(participant);
				if (reactors.stream().noneMatch(reactor -> reactor == user)) {
					removeParticipant(giveaway, participant);
					i.getAndIncrement();
				}
			}
			return i.intValue();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public boolean canParticipate(User user) {
		return !user.isBot() && !user.isSystem();
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
