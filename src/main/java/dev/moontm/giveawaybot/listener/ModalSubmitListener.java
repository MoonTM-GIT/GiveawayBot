package dev.moontm.giveawaybot.listener;

import dev.moontm.giveawaybot.command.Responses;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class ModalSubmitListener extends ListenerAdapter {

	@Override
	public void onModalInteraction(@NotNull ModalInteractionEvent event) {
		String[] id = event.getInteraction().getModalId().split(":");
		event.deferReply(true).queue();
		switch (id[0]) {
			case "giveaway-create" -> handleGiveawayCreation(event).queue();
			default -> Responses.error(event.getHook(), "").queue();
		}
	}

	private WebhookMessageAction<Message> handleGiveawayCreation(ModalInteractionEvent event) {
		//TODO: Add Logic
		return Responses.success(event.getHook(), "Success!", "Successfully Created Giveaway.");
	}
}