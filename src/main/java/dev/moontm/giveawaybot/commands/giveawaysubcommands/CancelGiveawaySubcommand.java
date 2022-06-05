package dev.moontm.giveawaybot.commands.giveawaysubcommands;

import com.dynxsty.dih4jda.interactions.commands.SlashCommand;
import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.giveaway.dao.GiveawayRepository;
import dev.moontm.giveawaybot.giveaway.model.Giveaway;
import dev.moontm.giveawaybot.util.Responses;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.sql.SQLException;
import java.util.Optional;

public class CancelGiveawaySubcommand extends SlashCommand.Subcommand {

	public CancelGiveawaySubcommand() {
		this.setSubcommandData(new SubcommandData("cancel", "Cancel a Giveaway.").addOption(OptionType.INTEGER, "giveaway-id", "The Giveaway you want to cancel.", true, true));
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		try {
			OptionMapping idOption = event.getOption("giveaway-id");
			if (idOption == null) {
				Responses.error(event, "Missing Arguments.").queue();
				return;
			}
			Optional<Giveaway> giveawayOptional = new GiveawayRepository(Bot.dataSource.getConnection()).getById(idOption.getAsLong());
			if (giveawayOptional.isEmpty()) {
				Responses.error(event, "Couldn't find giveaway.").queue();
				return;
			}
			Giveaway giveaway = giveawayOptional.get();
			if (giveaway.getHostedBy() != event.getUser().getIdLong()) {
				Responses.error(event, "You cannot cancel other people's giveaways.");
				return;
			}
			Bot.giveawayManager.deleteGiveawayAndMessage(giveaway);
			Responses.success(event, "Deleted Giveaway!", "Successfully Deleted Giveaway.").queue();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
