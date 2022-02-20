package dev.moontm.giveawaybot.listener;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;


@Slf4j
public class AutoCompleteListener extends ListenerAdapter {
	@Override
	public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
		switch (event.getName()) {
			//TODO:Implement
			default -> throw new IllegalStateException("Unknown Command: " + event.getName());
		}
	}
}
