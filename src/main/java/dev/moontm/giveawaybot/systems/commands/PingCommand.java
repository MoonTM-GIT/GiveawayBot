package dev.moontm.giveawaybot.systems.commands;

import com.dynxsty.dih4jda.commands.interactions.slash.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

/**
 * Command that displays the current Gateway ping.
 */
public class PingCommand implements ISlashCommand {
	@Override
	public void handleSlashCommandInteraction(SlashCommandInteractionEvent event) {
		long gatewayPing = event.getJDA().getGatewayPing();
		String botImage = event.getJDA().getSelfUser().getAvatarUrl();
		var e = new EmbedBuilder()
				.setAuthor(gatewayPing + "ms", null, botImage)
				.setColor(Color.decode("#2F3136"))
				.build();
		event.replyEmbeds(e).queue();
	}
}
