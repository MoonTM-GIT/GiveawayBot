package dev.moontm.giveawaybot.systems.giveaway.subcommands;

import dev.moontm.giveawaybot.command.ResponseException;
import dev.moontm.giveawaybot.command.interfaces.ISlashCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

public class DeleteGiveawaySubCommand implements ISlashCommand {
	@Override
	public ReplyCallbackAction handleSlashCommandInteraction(SlashCommandInteractionEvent event) throws ResponseException {
		return null;
	}
}
