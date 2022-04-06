package dev.moontm.giveawaybot.commands;

import com.dynxsty.dih4jda.commands.interactions.slash_command.dao.GlobalSlashCommand;
import dev.moontm.giveawaybot.commands.giveawaysubcommands.CancelGiveawaySubcommand;
import dev.moontm.giveawaybot.commands.giveawaysubcommands.CreateGiveawaySubcommand;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class GiveawayCommand extends GlobalSlashCommand {

	public GiveawayCommand() {
		this.setCommandData(Commands.slash("giveaway", "Commands Related to Giveaways."));
		this.setSubcommands(CreateGiveawaySubcommand.class, CancelGiveawaySubcommand.class);
	}
}
