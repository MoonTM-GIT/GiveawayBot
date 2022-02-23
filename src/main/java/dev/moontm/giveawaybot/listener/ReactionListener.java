package dev.moontm.giveawaybot.listener;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.giveaway.dao.GiveawayRepository;
import dev.moontm.giveawaybot.giveaway.model.Giveaway;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;

public class ReactionListener extends ListenerAdapter {

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		try {
			Optional<Giveaway> giveawayOptional = new GiveawayRepository(Bot.dataSource.getConnection()).getByMessageId(event.getMessageIdLong());
			if (giveawayOptional.isEmpty() || event.getReactionEmote().getIdLong() != Bot.config.getSystems().getGiveawayConfig().getParticipateEmoteId() || !canEnterGiveaway(event.getUser()))
				return;
			Giveaway giveaway = giveawayOptional.get();
			if (Arrays.stream(giveaway.getParticipants()).anyMatch(x -> x == event.getUserIdLong())) return;
			Bot.giveawayManager.addParticipant(giveaway, event.getUserIdLong());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		try {
			Optional<Giveaway> giveawayOptional = new GiveawayRepository(Bot.dataSource.getConnection()).getByMessageId(event.getMessageIdLong());
			if (giveawayOptional.isEmpty() || event.getReactionEmote().getIdLong() != Bot.config.getSystems().getGiveawayConfig().getParticipateEmoteId() || !canEnterGiveaway(event.getUser()))
				return;
			Giveaway giveaway = giveawayOptional.get();
			if (Arrays.stream(giveaway.getParticipants()).noneMatch(x -> x == event.getUserIdLong())) return;
			Bot.giveawayManager.removeParticipant(giveaway, event.getUserIdLong());
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean canEnterGiveaway(User user) {
		return !user.isBot() && !user.isSystem();
	}
}
