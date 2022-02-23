package dev.moontm.giveawaybot.systems.commands;

import com.dynxsty.dih4jda.commands.interactions.slash.dao.GuildSlashCommand;
import dev.moontm.giveawaybot.systems.commands.giveawaySubcommands.CreateGiveawaySubCommand;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class GiveawayCommandHandler extends GuildSlashCommand {

	public GiveawayCommandHandler() {
		this.setCommandData(Commands.slash("giveaway", "Commands Related to Giveaways."));
		this.setSubcommandClasses(CreateGiveawaySubCommand.class);
	}
}
