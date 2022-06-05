package dev.moontm.giveawaybot.commands.giveawaysubcommands;

import com.dynxsty.dih4jda.interactions.commands.SlashCommand;
import dev.moontm.giveawaybot.util.Responses;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.requests.restaction.interactions.ModalCallbackAction;

public class CreateGiveawaySubcommand extends SlashCommand.Subcommand {

	public CreateGiveawaySubcommand() {
		this.setSubcommandData(new SubcommandData("create", "Start Giveaway creation process for the current channel."));
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		if (!canCreateMeetings(event.getMember())) {
			Responses.error(event, "Unfortunately you are not able to create a command at the moment.").queue();
		}
		if (event.getChannelType() != ChannelType.TEXT) {
			Responses.error(event, "Giveaways may only be created in Text Channels.").queue();
		}
		buildCreateModal(event).queue();
	}

	/**
	 * Builds the Giveaway-Creation Modal. Has a few options;
	 * <ol>
	 *     <li>The prize the winners will receive.</li>
	 *     <li>The amount of winners.</li>
	 *     <li>The date in the dd/MM/YYYY HH:mm format.</li>
	 * </ol>
	 * @param event The {@link SlashCommandInteractionEvent} that caused this.
	 * @return The {@link ModalCallbackAction}.
	 */
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
