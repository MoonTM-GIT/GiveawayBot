package dev.moontm.giveawaybot.systems.commands;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.command.interfaces.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

/**
 * Command that displays the current Gateway ping.
 */
public class PingCommand implements ISlashCommand {
	@Override
	public ReplyCallbackAction handleSlashCommandInteraction(SlashCommandInteractionEvent event) {
		long gatewayPing = event.getJDA().getGatewayPing();
		String botImage = event.getJDA().getSelfUser().getAvatarUrl();
		var e = new EmbedBuilder()
				.setAuthor(gatewayPing + "ms", null, botImage)
				.setColor(Bot.config.get(event.getGuild()).getSlashCommand().getDefaultColor())
				.build();
		return event.replyEmbeds(e);
	}
}
