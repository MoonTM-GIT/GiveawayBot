/*
 * This File was initially copied from the JavaBot (https://github.com/Java-Discord/JavaBot) and might be
 * modified to better fit this project's purpose.
 */
package dev.moontm.giveawaybot.data.config;

import lombok.Data;

import java.awt.*;

/**
 * Contains configuration settings for various systems which the bot uses, such
 * as databases or dependencies that have runtime properties.
 */
@Data
public class SystemsConfig {
	/**
	 * The token used to create the JDA Discord bot instance.
	 */
	private String jdaBotToken = "";

	/**
	 * The number of threads to allocate to the bot's general purpose async
	 * thread pool.
	 */
	private int asyncPoolSize = 4;

	/**
	 * Id of the user that is allowed to use the /dev command.
	 */
	private long adminId = 0;


	/**
	 * The Bot's Invite limk.
	 */
	private String botInviteLike = "";

	/**
	 * Configuration for the Hikari connection pool that's used for the bot's
	 * SQL data source.
	 */
	private HikariConfig hikariConfig = new HikariConfig();

	/**
	 * Configuration for everything related to giveaways.
	 */
	private GiveawayConfig giveawayConfig = new GiveawayConfig();

	/**
	 * Configuration for everything related to commands.
	 */
	private SlashCommandConfig slashCommandConfig = new SlashCommandConfig();

	/**
	 * Configuration settings for the Hikari connection pool.
	 */
	@Data
	public static class HikariConfig {
		private String jdbcUrl = "jdbc:h2:tcp://localhost:9125/./giveaway_bot";
		private int maximumPoolSize = 5;
	}

	@Data
	public static class GiveawayConfig {
		private long participateEmoteId = 0;
		private String  congratulateEmote = "<(a:)name:id>";
	}

	@Data
	public static class SlashCommandConfig {
		private String defaultColorHex = "#2F3136";
		private String warningColorHex = "#EBA434";
		private String errorColorHex = "#EB3434";
		private String infoColorHex = "#34A2EB";
		private String successColorHex = "#49DE62";

		public Color getDefaultColor() {
			return Color.decode(this.defaultColorHex);
		}

		public Color getWarningColor() {
			return Color.decode(this.warningColorHex);
		}

		public Color getErrorColor() {
			return Color.decode(this.errorColorHex);
		}

		public Color getInfoColor() {
			return Color.decode(this.infoColorHex);
		}

		public Color getSuccessColor() {
			return Color.decode(this.successColorHex);
		}
	}
}
