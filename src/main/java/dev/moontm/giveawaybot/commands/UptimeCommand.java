package dev.moontm.giveawaybot.commands;

import com.dynxsty.dih4jda.interactions.commands.SlashCommand;
import dev.moontm.giveawaybot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.TimeUnit;

/**
 * Command that displays the current Gateway ping.
 */
public class UptimeCommand extends SlashCommand {

    public UptimeCommand() {
        this.setType(Type.GLOBAL);
        this.setCommandData(Commands.slash("uptime", "Shows the Bot's current uptime."));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String botImage = event.getJDA().getSelfUser().getAvatarUrl();
        var e = new EmbedBuilder()
                .setAuthor(getUptime(), null, botImage)
                .setColor(Bot.config.getSystems().getSlashCommandConfig().getDefaultColor())
                .build();
        event.replyEmbeds(e).setEphemeral(true).queue();
    }

    public String getUptime() {
        long uptimeMS = ManagementFactory.getRuntimeMXBean().getUptime();
        long uptimeDAYS = TimeUnit.MILLISECONDS.toDays(uptimeMS);
        uptimeMS -= TimeUnit.DAYS.toMillis(uptimeDAYS);
        long uptimeHRS = TimeUnit.MILLISECONDS.toHours(uptimeMS);
        uptimeMS -= TimeUnit.HOURS.toMillis(uptimeHRS);
        long uptimeMIN = TimeUnit.MILLISECONDS.toMinutes(uptimeMS);
        uptimeMS -= TimeUnit.MINUTES.toMillis(uptimeMIN);
        long uptimeSEC = TimeUnit.MILLISECONDS.toSeconds(uptimeMS);

        return String.format("%sd %sh %smin %ss",
                uptimeDAYS, uptimeHRS, uptimeMIN, uptimeSEC);
    }
}
