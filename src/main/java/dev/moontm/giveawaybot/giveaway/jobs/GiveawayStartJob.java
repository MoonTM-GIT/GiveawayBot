package dev.moontm.giveawaybot.giveaway.jobs;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.giveaway.dao.GiveawayRepository;
import dev.moontm.giveawaybot.giveaway.model.Giveaway;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class GiveawayStartJob implements Job {
	/**
	 * This is ran when a Giveaway should conclude, this has a few steps:
	 * <ol>
	 *     <li>Retrieve the Participants from Discord.</li>
	 *     <li>Randomly draw the winners.</li>
	 *     <li>Edit the Giveaway Message.</li>
	 *     <li>Update the Database.</li>
	 * </ol>
	 *
	 * @param context The {@link JobExecutionContext}.
	 * @throws JobExecutionException If anything goes wrong.
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			long giveawayId = Long.parseLong(context.getJobDetail().getKey().getName());
			Optional<Giveaway> giveawayOptional = new GiveawayRepository(Bot.dataSource.getConnection()).getById(giveawayId);
			if (giveawayOptional.isEmpty()) return;
			Giveaway giveaway = giveawayOptional.get();

			List<User> participants = new ArrayList<>();
			for (long id : giveaway.getParticipants()) {
				Bot.jda.retrieveUserById(id).queue(participant -> {
					if (!participant.isBot()) participants.add(participant);
				});
			}
			List<User> winners = drawWinners(participants, giveaway.getWinnerAmount());
			List<Long> winnerIds = new ArrayList<>();
			winners.forEach(user -> winnerIds.add(user.getIdLong()));

			Guild guild = Bot.jda.getGuildById(giveaway.getGuildId());
			if (guild == null) return;
			TextChannel channel = guild.getTextChannelById(giveaway.getChannelId());
			if (channel == null) return;
			channel.editMessageEmbedsById(giveaway.getMessageId(), buildWinnerEmbed(giveaway, winners)).queue();
			String emoji = Bot.config.getSystems().getGiveawayConfig().getCongratulateEmote();
			StringBuilder sb = new StringBuilder();
			if (winners.size() == 1) {
				channel.sendMessage(String.format("%sCongratulations %s, you've won 1x `%s`%s", emoji, winners.get(0), giveaway.getWinnerPrize(), emoji)).queue();
			} else {
				for (User winner : winners) {
					if (sb.length() != 0) {
						sb.append(", ");
					}
					sb.append(winner.getAsMention());
				}
				channel.sendMessage(String.format("%sCongratulations %s, you've each won 1x `%s`%s", emoji, sb, giveaway.getWinnerPrize(), emoji)).queue();
			}
			new GiveawayRepository(Bot.dataSource.getConnection()).updateWinners(giveaway, GiveawayRepository.convertObjectArrayToLongArray(winnerIds.toArray()));
			new GiveawayRepository(Bot.dataSource.getConnection()).markInactive(giveawayId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Randomly draws a given amount of {@link User}s from a list.
	 *
	 * @param participants The {@link List} of {@link User}s to draw from.
	 * @param amount The amount of {@link User}s to draw.
	 * @return The {@link List} of {@link User}s that we're drawn.
	 */
	public List<User> drawWinners(List<User> participants, int amount) {
		Random random = new Random();
		List<User> winners = new ArrayList<>();
		if (participants.isEmpty()) return winners;
		if (participants.size() < amount) amount = participants.size();

		for (int i = 0; i < amount; i++) {
			int randomIndex = random.nextInt(participants.size());
			User randomUser = participants.get(randomIndex);
			winners.add(randomUser);
			participants.remove(randomIndex);
		}
		return winners;
	}

	public MessageEmbed buildWinnerEmbed(Giveaway giveaway, List<User> winners) {
		StringBuilder sb = new StringBuilder();
		for (User winner : winners) {
			sb.append(winner.getAsMention()).append("\n");
		}
		final String[] hostTag = new String[1];
		Bot.jda.retrieveUserById(giveaway.getHostedBy()).queue(user -> hostTag[0] = user.getAsTag());

		EmbedBuilder eb = new EmbedBuilder()
				.setTitle("Giveaway Concluded!", Bot.config.getSystems().getBotInviteLike())
				.setDescription(String.format("%sx %s", giveaway.getWinnerAmount(), giveaway.getWinnerPrize()))
				.addField("Winners", sb.toString(), true)
				.setColor(Bot.config.getSystems().getSlashCommandConfig().getDefaultColor())
				.setFooter(String.format("Hosted by %s | %s", hostTag[0], giveaway.getId()))
				.setTimestamp(giveaway.getDueAt().toLocalDateTime());
		return eb.build();
	}
}
