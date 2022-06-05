package dev.moontm.giveawaybot.commands;

import com.dynxsty.dih4jda.interactions.commands.SlashCommand;
import dev.moontm.giveawaybot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

/**
 * Command that displays the current Gateway ping.
 */
public class PingCommand extends SlashCommand {

	public PingCommand() {
		this.setType(Type.GLOBAL);
		this.setCommandData(Commands.slash("ping", "Pong!"));
	}

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		long gatewayPing = event.getJDA().getGatewayPing();
		String botImage = event.getJDA().getSelfUser().getAvatarUrl();
		var e = new EmbedBuilder()
				.setAuthor(gatewayPing + "ms", null, botImage)
				.setColor(Bot.config.getSystems().getSlashCommandConfig().getDefaultColor())
				.build();
		event.replyEmbeds(e).setEphemeral(true).queue();
	}
}
