package dev.moontm.giveawaybot.systems.commands;

import com.dynxsty.dih4jda.commands.interactions.slash.dao.GuildSlashCommand;
import dev.moontm.giveawaybot.systems.commands.giveawaysubcommands.CreateGiveawaySubcommand;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class GiveawayCommand extends GuildSlashCommand {

	public GiveawayCommand() {
		this.setCommandData(Commands.slash("giveaway", "Commands Related to Giveaways."));
		this.setSubcommandClasses(CreateGiveawaySubcommand.class);
	}
}
