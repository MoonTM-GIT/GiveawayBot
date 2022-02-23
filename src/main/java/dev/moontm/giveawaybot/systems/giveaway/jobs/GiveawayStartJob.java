package dev.moontm.giveawaybot.systems.giveaway.jobs;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.systems.giveaway.dao.GiveawayRepository;
import dev.moontm.giveawaybot.systems.giveaway.model.Giveaway;
import dev.moontm.giveawaybot.util.ColorUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GiveawayStartJob implements Job {
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			long giveawayId = Long.parseLong(context.getJobDetail().getKey().getName());
			Giveaway giveaway = new GiveawayRepository(Bot.dataSource.getConnection()).getById(giveawayId).get();
			List<User> participants = new ArrayList<>();
			for (long id : giveaway.getParticipants()) {
				Bot.jda.retrieveUserById(id).queue(participants::add);
			}
			List<User> winners = drawWinners(participants, giveaway.getWinnerAmount());
			List<Long> winnerIds = new ArrayList<Long>();
			winners.forEach(user -> winnerIds.add(user.getIdLong()));

			TextChannel channel = Bot.jda.getGuildById(giveaway.getGuildId()).getTextChannelById(giveaway.getChannelId());
			channel.editMessageEmbedsById(giveaway.getMessageId(), buildWinnerEmbed(giveaway, winners)).queue();
			StringBuilder sb = new StringBuilder();
			if (winners.size() == 1) {
				channel.sendMessage(String.format("Congratulations %s, you've won 1x %s", winners.get(0), giveaway.getWinnerPrize())).queue();
			} else {
				for (User winner : winners) {
					if (sb.length() != 0) {
						sb.append(", ");
					}
					sb.append(winner.getAsMention());
				}
				channel.sendMessage(String.format("Congratulations %s, you've each won 1x %s", sb, giveaway.getWinnerPrize())).queue();
			}
			new GiveawayRepository(Bot.dataSource.getConnection()).updateWinners(giveaway, GiveawayRepository.convertObjectArraytToLongArray(winnerIds.toArray()));
			new GiveawayRepository(Bot.dataSource.getConnection()).markInactive(giveawayId);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception ignored) {
			ignored.printStackTrace();
		}
	}

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
			sb.append(winner.getAsMention() + "\n");
		}
		final String[] hostTag = new String[1];
		Bot.jda.retrieveUserById(giveaway.getHostedBy()).queue(user -> hostTag[0] = user.getAsTag());

		EmbedBuilder eb = new EmbedBuilder()
				.setTitle("Giveaway Over!", "https://javadiscord.net")//TODO: Set Invite URL
				.setDescription(String.format("%sx %s", giveaway.getWinnerAmount(), giveaway.getWinnerPrize()))
				.addField("Winners", sb.toString(), true)
				.setColor(ColorUtils.randomPastel())
				.setFooter(String.format("Hosted by %s | %s", hostTag[0], giveaway.getId()))
				.setTimestamp(giveaway.getDueAt().toLocalDateTime());

		return eb.build();
	}
}
