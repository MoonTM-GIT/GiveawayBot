package dev.moontm.giveawaybot;

import com.zaxxer.hikari.HikariDataSource;
import dev.moontm.giveawaybot.command.InteractionHandler;
import dev.moontm.giveawaybot.data.config.BotConfig;
import dev.moontm.giveawaybot.data.h2db.DbHelper;
import dev.moontm.giveawaybot.listener.ModalSubmitListener;
import dev.moontm.giveawaybot.listener.StartupListener;
import dev.moontm.giveawaybot.tasks.PresenceUpdater;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

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

	/**
	 * The set of configuration properties that this bot uses.
	 */
	public static BotConfig config;
	/**
	 * A reference to the slash command listener that's the main point of
	 * interaction for users with this bot. It's marked as a publicly accessible
	 * reference so that {@link InteractionHandler#registerCommands} can
	 * be called wherever it's needed.
	 */
	public static InteractionHandler interactionHandler;
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

	private Bot() {
	}

	/**
	 * The main method that starts the bot. This involves a few steps:
	 * <ol>
	 *     <li>Setting the time zone to UTC, to keep our sanity when working with times.</li>
	 *     <li>Loading the configuration JSON file.</li>
	 *     <li>Initializing the {@link InteractionHandler} listener (which reads command data from a YAML file).</li>
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
		interactionHandler = new InteractionHandler();
		asyncPool = Executors.newScheduledThreadPool(config.getSystems().getAsyncPoolSize());
		var jda = JDABuilder.createDefault(config.getSystems().getJdaBotToken())
				.setStatus(OnlineStatus.DO_NOT_DISTURB)
				.setChunkingFilter(ChunkingFilter.ALL)
				.setMemberCachePolicy(MemberCachePolicy.ALL)
				.enableIntents(GatewayIntent.GUILD_MEMBERS)
				.addEventListeners(interactionHandler)
				.build();
		addEventListeners(jda);
	}

	/**
	 * Adds all the bot's event listeners to the JDA instance, except for the
	 * main {@link InteractionHandler} listener.
	 *
	 * @param jda The JDA bot instance to add listeners to.
	 */
	private static void addEventListeners(JDA jda) {
		jda.addEventListener(
				new StartupListener(),
				new ModalSubmitListener(),
				PresenceUpdater.standardActivities()
		);
	}
}

