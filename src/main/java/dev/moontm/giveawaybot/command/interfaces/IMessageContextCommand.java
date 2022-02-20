package dev.moontm.giveawaybot.command.interfaces;

import dev.moontm.giveawaybot.command.ResponseException;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

/**
 * Interface that handles Discord's Message Context Commands.
 */
public interface IMessageContextCommand {
	ReplyCallbackAction handleMessageContextCommandInteraction(MessageContextInteractionEvent event) throws ResponseException;
}
