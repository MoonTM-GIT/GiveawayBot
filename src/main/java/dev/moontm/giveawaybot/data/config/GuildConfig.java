package dev.moontm.giveawaybot.data.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dev.moontm.giveawaybot.data.config.guild.SlashCommandConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A collection of guild-specific configuration items, each of which represents
 * a group of many individual settings.
 */
@Data
@Slf4j
public class GuildConfig {
	private transient Guild guild;
	private transient Path file;

	private SlashCommandConfig slashCommand;

	/**
	 * Constructor that initializes all Config classes.
	 *
	 * @param guild The current guild.
	 * @param file  The config file.
	 */
	public GuildConfig(Guild guild, Path file) {
		this.file = file;
		// Initialize all config items.
		this.slashCommand = new SlashCommandConfig();
		this.setGuild(guild);
	}

	/**
	 * Loads an instance of the configuration from the given path, or creates a
	 * new empty configuration file there if none exists yet.
	 *
	 * @param guild The guild to load config for.
	 * @param file  The path to the configuration JSON file.
	 * @return The config that was loaded.
	 * @throws JsonSyntaxException  if the config file's JSON is invalid.
	 * @throws UncheckedIOException if an IO error occurs.
	 */
	public static GuildConfig loadOrCreate(Guild guild, Path file) {
		Gson gson = new GsonBuilder().create();
		GuildConfig config;
		if (Files.exists(file)) {
			try (var reader = Files.newBufferedReader(file)) {
				config = gson.fromJson(reader, GuildConfig.class);
				config.setFile(file);
				config.setGuild(guild);
				log.info("Loaded config from {}", file);
			} catch (JsonSyntaxException e) {
				log.error("Invalid JSON found! Please fix or remove config file " + file + " and restart.", e);
				throw e;
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		} else {
			log.info("No config file found. Creating an empty one at {}", file);
			config = new GuildConfig(guild, file);
			config.flush();
		}

		return config;
	}

	private void setGuild(Guild guild) {
		this.guild = guild;
		if (this.slashCommand == null) this.slashCommand = new SlashCommandConfig();
		this.slashCommand.setGuildConfig(this);
	}

	/**
	 * Saves this config to its file path.
	 */
	public synchronized void flush() {
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
		try (var writer = Files.newBufferedWriter(this.file)) {
			gson.toJson(this, writer);
			writer.flush();
		} catch (IOException e) {
			log.error("Could not flush config.", e);
		}
	}

	@Nullable
	public Object resolve(String propertyName) throws UnknownPropertyException {
		var result = ReflectionUtils.resolveField(propertyName, this);
		return result.map(pair -> {
			try {
				return pair.first().get(pair.second());
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		}).orElse(null);
	}

	/**
	 * Attempts to set a configuration property's value by its name, using '.'
	 * to concatenate property names, similar to {@link GuildConfig#resolve(String)}.
	 *
	 * @param propertyName The name of the property to set.
	 * @param value        The value to set.
	 */
	public void set(String propertyName, String value) throws UnknownPropertyException {
		var result = ReflectionUtils.resolveField(propertyName, this);
		result.ifPresent(pair -> {
			try {
				ReflectionUtils.set(pair.first(), pair.second(), value);
				this.flush();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}
}
