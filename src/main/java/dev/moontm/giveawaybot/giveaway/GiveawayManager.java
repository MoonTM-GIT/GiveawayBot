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

	/**
	 * This creates a new {@link GiveawayManager} instance.
	 *
	 * Includes a periodic check with the following steps for each active Giveaway:
	 * <ol>
	 *     <li>Check if the Guild still exists.</li>
	 *     <li>Check if the Channel still exists.</li>
	 *     <li>Check if the Message still exists.</li>
	 *     <li>Check for any discrepancies between Reactions and the Database.</li>
	 * </ol>
	 */
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

	/**
	 * Deletes a {@link Giveaway} from the Database and cancels its schedule, but not the message.
	 *
	 * @param giveaway The {@link Giveaway} to cancel.
	 */
	private void deleteGiveaway(Giveaway giveaway) {
		try {
			new GiveawayRepository(Bot.dataSource.getConnection()).markInactive(giveaway.getId());
			Bot.giveawayStateManager.cancelSchedule(giveaway);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Deletes a {@link Giveaway} from the Database, cancels its schedule and deletes the Message.
	 *
	 * @param giveaway The {@link Giveaway} to cancel.
	 */
	public void deleteGiveawayAndMessage(Giveaway giveaway) {
		try {
			new GiveawayRepository(Bot.dataSource.getConnection()).markInactive(giveaway.getId());
			Guild guild = Bot.jda.getGuildById(giveaway.getGuildId());
			if (guild == null) return;
			TextChannel channel = guild.getTextChannelById(giveaway.getChannelId());
			if (channel == null) return;
			channel.deleteMessageById(giveaway.getMessageId()).queue(unused -> {}, throwable -> {
				log.info("Couldn't delete Message - Not Found (Giveaway {})", giveaway.getId());
			});
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks for discrepancies between a {@link List} of {@link User}s and the Database.
	 * If anything is found it attempts to solve the problem.
	 *
	 * @param giveaway The {@link Giveaway}.
	 * @param reactors The {@link List} of {@link User}s.
	 * @return The amount of changes made.
	 */
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

	/**
	 * Checks if the given {@link User} is a Bot or the System.
	 * @param user The {@link User} to check.
	 * @return False if the {@link User} is a Bot or the System.
	 */
	public boolean canParticipate(User user) {
		return !user.isBot() && !user.isSystem();
	}

	/**
	 * Adds a {@link User} to the Database.
	 *
	 * @param giveaway The {@link Giveaway} to add to.
	 * @param participantId The ID of the {@link User} to add.
	 * @return The updated {@link Giveaway}
	 */
	public Giveaway addParticipant(Giveaway giveaway, long participantId) {
		long[] newParticipants = ArrayUtils.add(giveaway.getParticipants(), participantId);
		try {
			return new GiveawayRepository(Bot.dataSource.getConnection()).updateParticipants(giveaway, newParticipants);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Removes a {@link User} from the Database.
	 *
	 * @param giveaway The {@link Giveaway} to remove from.
	 * @param participantId The ID of the {@link User} to remove.
	 * @return The updated {@link Giveaway}
	 */
	public Giveaway removeParticipant(Giveaway giveaway, long participantId) {
		long[] newParticipants = ArrayUtils.removeElement(giveaway.getParticipants(), participantId);
		try {
			return new GiveawayRepository(Bot.dataSource.getConnection()).updateParticipants(giveaway, newParticipants);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
