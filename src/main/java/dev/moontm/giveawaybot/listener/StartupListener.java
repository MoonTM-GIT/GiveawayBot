/**
 * This File was initially copied from the JavaBot (https://github.com/Java-Discord/JavaBot) and might be
 * modified to better fit this project's purpose.
 */
package dev.moontm.giveawaybot.listener;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.Constants;
import dev.moontm.giveawaybot.giveaway.GiveawayManager;
import dev.moontm.giveawaybot.giveaway.GiveawayStateManager;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Listens for the {@link ReadyEvent}.
 */
@Slf4j
public class StartupListener extends ListenerAdapter {

	@Override
	public void onReady(ReadyEvent event) {
		Bot.config.flush();
		log.info("Logged in as {}{}{}", Constants.TEXT_WHITE, event.getJDA().getSelfUser().getAsTag(), Constants.TEXT_RESET);
		log.info(Bot.jda.getGuilds().size() + " Guilds.");
		log.info("Setting Up Giveaway Managers.");
		Bot.giveawayStateManager = new GiveawayStateManager();
		Bot.giveawayManager = new GiveawayManager();
		log.info("Everything Ready.");
	}
}
