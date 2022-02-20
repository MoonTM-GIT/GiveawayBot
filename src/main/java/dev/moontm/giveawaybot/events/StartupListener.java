package dev.moontm.giveawaybot.events;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.Constants;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Comparator;
/**
 * Listens for the {@link ReadyEvent}.
 */
@Slf4j
public class StartupListener extends ListenerAdapter {

	/**
	 * The default guild, that is chosen upon startup based on the member count.
	 */
	public static Guild defaultGuild;

	@Override
	public void onReady(ReadyEvent event) {
		// Initialize all guild-specific configuration.
		Bot.config.loadGuilds(event.getJDA().getGuilds());
		Bot.config.flush();
		log.info("Logged in as {}{}{}", Constants.TEXT_WHITE, event.getJDA().getSelfUser().getAsTag(), Constants.TEXT_RESET);
		//log.info("Guilds: " + GuildUtils.getGuildList(event.getJDA().getGuilds(), true, true));
		var optionalGuild = event.getJDA().getGuilds().stream().max(Comparator.comparing(Guild::getMemberCount));
		optionalGuild.ifPresent(guild -> defaultGuild = guild);

		log.info("Starting Guild initialization\n");
		for (var guild : event.getJDA().getGuilds()) {
			Bot.interactionHandler.registerCommands(guild);
			//TODO: Re-Implement this:
			//GuildUtils.getLogChannel(guild).sendMessage("I have just been booted up!").queue();
		}
	}
}
