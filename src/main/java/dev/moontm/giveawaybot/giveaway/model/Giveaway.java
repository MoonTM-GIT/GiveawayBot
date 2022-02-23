package dev.moontm.giveawaybot.giveaway.model;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Giveaway {
	private long id;
	private long guildId;
	private long channelId;
	private long messageId;
	private long hostedBy;
	private long[] winners;
	private long[] participants;
	private Timestamp createdAt;
	private Timestamp dueAt;
	private String winnerPrize;
	private int winnerAmount;
	private boolean active = true;
}
