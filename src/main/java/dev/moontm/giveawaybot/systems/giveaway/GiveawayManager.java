package dev.moontm.giveawaybot.systems.giveaway;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.systems.giveaway.dao.GiveawayRepository;
import dev.moontm.giveawaybot.systems.giveaway.model.Giveaway;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.SQLException;

public class GiveawayManager {

	public Giveaway addParticipant(Giveaway giveaway, long participantId) {
		long[] newParticipants = ArrayUtils.add(giveaway.getParticipants(), participantId);
		try {
			return new GiveawayRepository(Bot.dataSource.getConnection()).updateParticipants(giveaway, newParticipants);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Giveaway removeParticipant(Giveaway giveaway, long participantId) {
		long[] newParticipants = ArrayUtils.removeElement(giveaway.getParticipants(), participantId);
		try {
			return new GiveawayRepository(Bot.dataSource.getConnection()).updateParticipants(giveaway, newParticipants);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
