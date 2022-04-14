package dev.moontm.giveawaybot.commands;

import com.dynxsty.dih4jda.commands.interactions.slash_command.ISlashCommand;
import com.dynxsty.dih4jda.commands.interactions.slash_command.dao.GlobalSlashCommand;
import dev.moontm.giveawaybot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

/**
 * Command that displays the current Gateway ping.
 */
public class UptimeCommand extends GlobalSlashCommand implements ISlashCommand {

    public UptimeCommand() {
        this.setCommandData(Commands.slash("uptime", "Shows the Bot's current uptime."));
    }

    @Override
    public void handleSlashCommandInteraction(SlashCommandInteractionEvent event) {
        long gatewayPing = event.getJDA().getGatewayPing();
        String botImage = event.getJDA().getSelfUser().getAvatarUrl();
        var e = new EmbedBuilder()
                .setAuthor(gatewayPing + "ms", null, botImage)
                .setColor(Bot.config.getSystems().getSlashCommandConfig().getDefaultColor())
                .build();
        event.replyEmbeds(e).setEphemeral(true).queue();
    }
}
