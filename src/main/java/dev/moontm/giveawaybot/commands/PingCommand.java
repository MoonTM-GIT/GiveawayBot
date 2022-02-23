package dev.moontm.giveawaybot.commands;

import com.dynxsty.dih4jda.commands.interactions.slash.ISlashCommand;
import com.dynxsty.dih4jda.commands.interactions.slash.dao.GuildSlashCommand;
import dev.moontm.giveawaybot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

/**
 * Command that displays the current Gateway ping.
 */
public class PingCommand extends GuildSlashCommand implements ISlashCommand {

	public PingCommand() {
		this.setCommandData(Commands.slash("ping", "Pong!"));
	}

	@Override
	public void handleSlashCommandInteraction(SlashCommandInteractionEvent event) {
		long gatewayPing = event.getJDA().getGatewayPing();
		String botImage = event.getJDA().getSelfUser().getAvatarUrl();
		var e = new EmbedBuilder()
				.setAuthor(gatewayPing + "ms", null, botImage)
				.setColor(Bot.config.getSystems().getSlashCommandConfig().getDefaultColor())
				.build();
		event.replyEmbeds(e).queue();
	}
}
