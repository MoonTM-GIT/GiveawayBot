package dev.moontm.giveawaybot.listener;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.giveaway.dao.GiveawayRepository;
import dev.moontm.giveawaybot.giveaway.model.Giveaway;
import dev.moontm.giveawaybot.util.Responses;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class ModalSubmitListener extends ListenerAdapter {

	@Override
	public void onModalInteraction(@NotNull ModalInteractionEvent event) {
		String[] id = event.getInteraction().getModalId().split(":");
		event.deferReply(true).queue();
		switch (id[0]) {
			case "giveaway-create" -> handleGiveawayCreation(event).queue();
			default -> Responses.error(event.getHook(), "").queue();
		}
	}

	/**
	 * This is used when the giveaway creation modal is submitted.
	 * It performs various checks and then creates the giveaway.
	 *
	 * @param event The {@link ModalInteractionEvent} that caused this.
	 * @return A {@link WebhookMessageAction<Message>} that is returned to the user.
	 */
	private WebhookMessageAction<Message> handleGiveawayCreation(ModalInteractionEvent event) {
		try (Connection con = Bot.dataSource.getConnection()) {

			TextChannel giveawayChannel = Bot.jda.getTextChannelById(event.getInteraction().getModalId().split(":")[1]);
			String giveawayPrize = event.getValue("giveaway-prize").getAsString();
			if (!isInteger(event.getValue("giveaway-winner-amount").getAsString()))
				return Responses.error(event.getHook(), "Winner amount must be a number.");
			int giveawayWinnerAmount = Integer.parseInt(event.getValue("giveaway-winner-amount").getAsString());

			String dateOption = event.getValue("giveaway-due-date").getAsString();
			LocalDateTime dueAt;
			try {
				dueAt = LocalDateTime.parse(dateOption, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
			} catch (DateTimeParseException e) {
				return Responses.error(event.getHook(), "Couldn't parse date. Please follow the following pattern:`dd/MM/YYYY HH:mm (Example: 13/09/2019 13:05)`.");
			}
			if (dueAt.isBefore(LocalDateTime.now()) || dueAt.isAfter(LocalDateTime.now().plusYears(2))) {
				return Responses.error(event.getHook(), "You've provided an invalid date! The date must be somewhere between now and 2 years from now. Please try again.");
			}

			Giveaway giveaway = new Giveaway(event.getGuild().getIdLong(), giveawayChannel.getIdLong(), event.getUser().getIdLong(), Timestamp.valueOf(dueAt), giveawayPrize, giveawayWinnerAmount);

			Giveaway inserted = new GiveawayRepository(con).insert(giveaway);
			giveawayChannel.sendMessageEmbeds(buildGiveawayEmbed(inserted)).queue(message -> {
				try {
					new GiveawayRepository(Bot.dataSource.getConnection()).updateMessage(inserted, message.getIdLong());
					message.addReaction(Bot.jda.getEmoteById(Bot.config.getSystems().getGiveawayConfig().getParticipateEmoteId())).queue();
				} catch (SQLException e) {
					Responses.error(event.getHook(), "An Unexpected Error Occurred.").queue();
				}
			});
			Bot.giveawayStateManager.scheduleGiveaway(new GiveawayRepository(Bot.dataSource.getConnection()).getById(inserted.getId()).get());
		} catch (SQLException e) {
			Responses.error(event.getHook(), "An Unexpected Error Occurred.");
		}
		return Responses.success(event.getHook(), "Giveaway Created!", "Successfully Created Meeting!");
	}

	/**
	 * Builds the Embed that is sent to the channel that users that want to participate can react on.
	 *
	 * @param giveaway The {@link Giveaway}.
	 * @return The built {@link MessageEmbed}
	 */
	private MessageEmbed buildGiveawayEmbed(Giveaway giveaway) {
		EmbedBuilder eb = new EmbedBuilder()
				.setTitle("Giveaway", Bot.config.getSystems().getBotInviteLike())
				.setDescription(String.format("%sx %s", giveaway.getWinnerAmount(), giveaway.getWinnerPrize()))
				.addField("Concludes", String.format("<t:%d:R>", giveaway.getDueAt().getTime()/1000), true)
				.setColor(Bot.config.getSystems().getSlashCommandConfig().getDefaultColor())
				.setFooter(String.format("Hosted by %s | %s", Bot.jda.getUserById(giveaway.getHostedBy()).getAsTag(), giveaway.getId()))
				.setTimestamp(giveaway.getDueAt().toLocalDateTime());
		return eb.build();
	}

	/**
	 * Checks if a given String is an Integer.
	 *
	 * @param strInteger The String to check.
	 * @return True if it is, false if it isn't.
	 */
	public static boolean isInteger(String strInteger) {
		if (strInteger == null) {
			return false;
		}
		try {
			double d = Integer.parseInt(strInteger);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
}