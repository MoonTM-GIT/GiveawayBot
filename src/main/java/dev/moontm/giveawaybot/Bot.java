/**
 * This File was initially copied from the JavaBot (https://github.com/Java-Discord/JavaBot) and might be
 * modified to better fit this project's purpose.
 */
package dev.moontm.giveawaybot;

import com.dynxsty.dih4jda.DIH4JDA;
import com.dynxsty.dih4jda.DIH4JDABuilder;
import com.zaxxer.hikari.HikariDataSource;
import dev.moontm.giveawaybot.data.config.BotConfig;
import dev.moontm.giveawaybot.data.h2db.DbHelper;
import dev.moontm.giveawaybot.listener.AutoCompleteListener;
import dev.moontm.giveawaybot.listener.ModalSubmitListener;
import dev.moontm.giveawaybot.listener.ReactionListener;
import dev.moontm.giveawaybot.listener.StartupListener;
import dev.moontm.giveawaybot.giveaway.GiveawayManager;
import dev.moontm.giveawaybot.giveaway.GiveawayStateManager;
import dev.moontm.giveawaybot.tasks.PresenceUpdater;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.utils.ChunkingFilter;

import java.nio.file.Path;
import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * The main class where the bot is initialized.
 */
@Slf4j
public class Bot {

	public static JDA jda;
	/**
	 * The set of configuration properties that this bot uses.
	 */
	public static BotConfig config;
	/**
	 * A reference to the data source that provides access to the relational
	 * database that this bot users for certain parts of the application. Use
	 * this to obtain a connection and perform transactions.
	 */
	public static HikariDataSource dataSource;
	/**
	 * A general-purpose thread pool that can be used by the bot to execute
	 * tasks outside the main event processing thread.
	 */
	public static ScheduledExecutorService asyncPool;
	public static GiveawayStateManager giveawayStateManager;
	public static GiveawayManager giveawayManager;

	private Bot() {}

	/**
	 * The main method that starts the bot. This involves a few steps:
	 * <ol>
	 *     <li>Setting the time zone to UTC, to keep our sanity when working with times.</li>
	 *     <li>Loading the configuration JSON file.</li>
	 *     <li>Initializing the {@link DIH4JDA} interaction-handler.</li>
	 *     <li>Creating and configuring the {@link JDA} instance that enables the bot's Discord connectivity.</li>
	 *     <li>Adding event listeners to the bot.</li>
	 * </ol>
	 *
	 * @param args Command-line arguments.
	 * @throws Exception If any exception occurs during bot creation.
	 */
	public static void main(String[] args) throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
		config = new BotConfig(Path.of("config"));
		dataSource = DbHelper.initDataSource(config);
		asyncPool = Executors.newScheduledThreadPool(config.getSystems().getAsyncPoolSize());
		jda = JDABuilder.createDefault(config.getSystems().getJdaBotToken())
				.setStatus(OnlineStatus.DO_NOT_DISTURB)
				//.addEventListeners()
				.build();
		DIH4JDABuilder.setJDA(jda)
				.setCommandsPackage("dev.moontm.giveawaybot.commands")
				.build();
		addEventListeners(jda);
	}

	/**
	 * Adds all the bot's event listeners to the JDA instance.
	 *
	 * @param jda The JDA bot instance to add listeners to.
	 */
	private static void addEventListeners(JDA jda) {
		jda.addEventListener(
				new StartupListener(),
				new ModalSubmitListener(),
				new ReactionListener(),
				new AutoCompleteListener(),
				PresenceUpdater.standardActivities()
		);
	}
}

