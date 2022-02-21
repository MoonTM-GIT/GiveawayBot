package dev.moontm.giveawaybot.systems.giveaway.subcommands;

import dev.moontm.giveawaybot.command.ResponseException;
import dev.moontm.giveawaybot.command.Responses;
import dev.moontm.giveawaybot.command.interfaces.ISlashCommand;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

public class CreateGiveawaySubCommand implements ISlashCommand {
	@Override
	public ReplyCallbackAction handleSlashCommandInteraction(SlashCommandInteractionEvent event) throws ResponseException {
		if (!canCreateMeetings(event.getMember())) {
			return Responses.error(event, "Unfortunately you are not able to create a command at the moment.");
		}
		if (event.getChannelType() != ChannelType.TEXT) {
			return Responses.error(event, "Giveaways may only be created in Text Channels.");
		}
		this.buildCreateModal(event).queue();
		return null;
	}

	private ModalCallbackAction buildCreateModal(SlashCommandInteractionEvent event) {

		TextInput giveawayPrize = TextInput.create("giveaway-prize", "Giveaway Prize", TextInputStyle.SHORT)
				.setRequired(true)
				.setMaxLength(64)
				.setPlaceholder("Very Tasty Bananas")
				.build();

		TextInput winnerAmount = TextInput.create("giveaway-winner-amount", "Amount of Winners", TextInputStyle.SHORT)
				.setRequired(true)
				.setPlaceholder("5")
				.build();

		TextInput endDate = TextInput.create("giveaway-due-date", "Giveaway End Date", TextInputStyle.SHORT)
				.setRequired(true)
				.setMaxLength(17)
				.setPlaceholder("dd/MM/YYYY HH:mm (Example: 13/09/2019 13:05)")
				.build();

		Modal modal = Modal.create("giveaway-create:" + event.getChannel().getId(), "Create Giveaway")
				.addActionRows(ActionRow.of(giveawayPrize), ActionRow.of(winnerAmount), ActionRow.of(endDate))
				.build();

		return event.replyModal(modal);
	}


	private boolean canCreateMeetings(Member member) {
		return !member.getUser().isSystem() && !member.getUser().isBot() && !member.isPending() && !member.isTimedOut();
	}
}
