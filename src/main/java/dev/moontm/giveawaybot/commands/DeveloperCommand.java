package dev.moontm.giveawaybot.commands;

import com.dynxsty.dih4jda.commands.interactions.slash.ISlashCommand;
import com.dynxsty.dih4jda.commands.interactions.slash.dao.GuildSlashCommand;
import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.util.Responses;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;

public class DeveloperCommand extends GuildSlashCommand implements ISlashCommand {

	public DeveloperCommand() {
		this.setCommandData(Commands.slash("dev", "Collection of Developer Commands.").addOption(OptionType.STRING, "action", "The Action to take.", true).setDefaultEnabled(false));
		this.setCommandPrivileges(CommandPrivilege.enableUser(Bot.config.getSystems().getAdminId()));
	}

	@Override
	public void handleSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (event.getUser().getIdLong() != Bot.config.getSystems().getAdminId()) Responses.error(event, "You're not allowed to execute this command.");
		switch(event.getOption("action").getAsString().toLowerCase()) {
			case "stop" -> stopBot(event);
			//add more
		}
	}

	private void stopBot(SlashCommandInteractionEvent event) {
		Bot.dataSource.close();
		Responses.success(event, "Stopping", "Stopping the Bot.").complete();
		System.exit(0);
	}
}
