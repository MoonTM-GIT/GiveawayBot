package dev.moontm.giveawaybot.systems.giveaway;

import dev.moontm.giveawaybot.command.DelegatingCommandHandler;
import dev.moontm.giveawaybot.systems.giveaway.subcommands.CreateGiveawaySubCommand;
import dev.moontm.giveawaybot.systems.giveaway.subcommands.DeleteGiveawaySubCommand;

public class GiveawayCommandHandler extends DelegatingCommandHandler {

	public GiveawayCommandHandler() {
		this.addSubcommand("create", new CreateGiveawaySubCommand());
		this.addSubcommand("delete", new DeleteGiveawaySubCommand());
	}
}
