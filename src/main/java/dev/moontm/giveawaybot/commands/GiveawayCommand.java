package dev.moontm.giveawaybot.commands;

import com.dynxsty.dih4jda.interactions.commands.SlashCommand;
import dev.moontm.giveawaybot.commands.giveawaysubcommands.CancelGiveawaySubcommand;
import dev.moontm.giveawaybot.commands.giveawaysubcommands.CreateGiveawaySubcommand;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class GiveawayCommand extends SlashCommand {

	public GiveawayCommand() {
		this.setType(Type.GLOBAL);
		this.setCommandData(Commands.slash("giveaway", "Commands Related to Giveaways."));
		this.setSubcommands(CreateGiveawaySubcommand.class, CancelGiveawaySubcommand.class);
	}
}
