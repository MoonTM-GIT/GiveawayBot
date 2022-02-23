package dev.moontm.giveawaybot.listener;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.giveaway.dao.GiveawayRepository;
import dev.moontm.giveawaybot.giveaway.model.Giveaway;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.restaction.interactions.AutoCompleteCallbackAction;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class AutoCompleteListener extends ListenerAdapter {
	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
		switch (event.getName()) {
			case "giveaway" -> handleGiveawayCommand(event);
			default -> throw new IllegalStateException("Unknown Command: " + event.getName());
		}
	}

	private void handleGiveawayCommand(CommandAutoCompleteInteractionEvent event) {
			switch (event.getSubcommandName()) {
				case "cancel" -> getUserGiveaways(event);
				default -> throw new IllegalArgumentException("Unknown Command: " + event.getSubcommandName());
			};
	}

	private void getUserGiveaways(CommandAutoCompleteInteractionEvent event) {
		try {
			List<Giveaway> userGiveaways = new GiveawayRepository(Bot.dataSource.getConnection()).getActiveByHostId(event.getUser().getIdLong());
			ArrayList<Command.Choice> choices = new ArrayList<>();
			for(Giveaway giveaway:userGiveaways) {
				choices.add(new Command.Choice(String.format("%d - %s", giveaway.getId(), giveaway.getWinnerPrize()), giveaway.getId()));
			}
			event.replyChoices(choices).queue();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
