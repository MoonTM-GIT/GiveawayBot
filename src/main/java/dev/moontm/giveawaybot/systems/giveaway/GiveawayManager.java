package dev.moontm.giveawaybot.systems.giveaway;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.Constants;
import dev.moontm.giveawaybot.systems.giveaway.dao.GiveawayRepository;
import dev.moontm.giveawaybot.systems.giveaway.model.Giveaway;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.pagination.ReactionPaginationAction;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GiveawayManager {
	public GiveawayManager() {
		Runnable checksRunnable = new Runnable() {
			public void run() {
				try {
					long startTime = System.nanoTime();
					List<Giveaway> giveaways = new GiveawayRepository(Bot.dataSource.getConnection()).getActive();
					final int[] i = {0};
					for (Giveaway giveaway:giveaways) {
						//Check if Guild still exists
						Guild guild = Bot.jda.getGuildById(giveaway.getGuildId());
						if (guild == null) {
							new GiveawayRepository(Bot.dataSource.getConnection()).markInactive(giveaway.getId());
							Bot.giveawayStateManager.cancelSchedule(giveaway);
							i[0]++;
							continue;
						}
						//Check if Channel still exists
						TextChannel channel = Bot.jda.getTextChannelById(giveaway.getChannelId());
						if (channel == null) {
							new GiveawayRepository(Bot.dataSource.getConnection()).markInactive(giveaway.getId());
							Bot.giveawayStateManager.cancelSchedule(giveaway);
							i[0]++;
							continue;
						}
						//Check if Message still exists
						channel.retrieveMessageById(giveaway.getMessageId()).queue(message -> {
							message.retrieveReactionUsers(Bot.jda.getEmoteById(Constants.emojiId)).queue(reactors -> {
								try {
									long[] participants = new GiveawayRepository(Bot.dataSource.getConnection()).getById(giveaway.getId()).get().getParticipants();
									reactors.forEach(reactor -> {
										if (Arrays.stream(participants).noneMatch(x -> x == reactor.getIdLong()) && canParticipate(reactor)) {
											addParticipant(giveaway, reactor.getIdLong());
											i[0]++;
										}
									});
									for (long participant:participants) {
										User user = Bot.jda.getUserById(participant);
										if (reactors.stream().noneMatch(reactor -> reactor == user)) {
											removeParticipant(giveaway, participant);
											i[0]++;
										}
									}
								} catch (SQLException e) {e.printStackTrace();}
							});
						}, error -> {
							try {
								new GiveawayRepository(Bot.dataSource.getConnection()).markInactive(giveaway.getId());
								Bot.giveawayStateManager.cancelSchedule(giveaway);
								i[0]++;
							} catch (SQLException ex) {
								error.printStackTrace();
							}
						});
					}
					long endTime = System.nanoTime();
					long totalTime = ((endTime-startTime) / 1000000);
					log.info("Periodic check took {} Milliseconds, made {} changes.", totalTime,i[0]);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		};
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(checksRunnable, 2, 5, TimeUnit.MINUTES);
	}

	public boolean canParticipate(User user){
		if (user.isBot() || user.isSystem()) return false;
		return true;
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
