package dev.moontm.giveawaybot.giveaway.model;

import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

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

	public Giveaway(){};

	public Giveaway(long guildId, long channelId, long hostId, Timestamp dueAt, String prize, int winnerAmount) {
		this.guildId = guildId;
		this.channelId = channelId;
		this.hostedBy = hostId;
		this.createdAt = Timestamp.valueOf(LocalDateTime.now());
		this.dueAt = dueAt;
		this.winnerPrize = prize;
		this.winnerAmount = winnerAmount;
	}
}
