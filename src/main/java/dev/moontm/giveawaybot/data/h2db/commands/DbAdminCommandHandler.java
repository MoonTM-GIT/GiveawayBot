package dev.moontm.giveawaybot.data.h2db.commands;

import dev.moontm.giveawaybot.command.DelegatingCommandHandler;

/**
 * Handler class for all Database related commands.
 */
public class DbAdminCommandHandler extends DelegatingCommandHandler {
/**
* Adds all subcommands {@link DelegatingCommandHandler#addSubcommand}.
*/
	public DbAdminCommandHandler() {
		this.addSubcommand("export-schema", new ExportSchemaSubcommand());
		this.addSubcommand("export-table", new ExportTableSubcommand());
		this.addSubcommand("migrations-list", new MigrationsListSubcommand());
		this.addSubcommand("migrate", new MigrateSubcommand());
	}
}

//TODO:Add DM-Admin Commands