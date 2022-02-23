package dev.moontm.giveawaybot.listener;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.Constants;
import dev.moontm.giveawaybot.systems.giveaway.GiveawayManager;
import dev.moontm.giveawaybot.systems.giveaway.GiveawayStateManager;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
		Bot.config.flush();
		log.info("Logged in as {}{}{}", Constants.TEXT_WHITE, event.getJDA().getSelfUser().getAsTag(), Constants.TEXT_RESET);
		//log.info("Guilds: " + GuildUtils.getGuildList(event.getJDA().getGuilds(), true, true));
		log.info("Setting Up Giveaway Managers.");
		Bot.giveawayStateManager = new GiveawayStateManager();
		Bot.giveawayManager = new GiveawayManager();
		log.info("Everything Ready.");
	}
}
