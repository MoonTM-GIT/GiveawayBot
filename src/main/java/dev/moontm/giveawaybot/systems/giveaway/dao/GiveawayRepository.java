package dev.moontm.giveawaybot.systems.giveaway.dao;

import dev.moontm.giveawaybot.systems.giveaway.model.Giveaway;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
public class GiveawayRepository {
	private final Connection con;

	public GiveawayRepository(Connection connection) {
		con = connection;
	}

	public Giveaway insert(Giveaway giveaway) throws SQLException {
		PreparedStatement statement = con.prepareStatement("INSERT INTO giveaways (guild_id, channel_id, hosted_by, created_at, due_at, winner_prize, winner_amount) VALUES (?, ?, ?, ?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS
		);
		statement.setLong(1, giveaway.getGuildId());
		statement.setLong(2, giveaway.getChannelId());
		statement.setLong(3, giveaway.getHostedBy());
		statement.setTimestamp(4, giveaway.getCreatedAt());
		statement.setTimestamp(5, giveaway.getDueAt());
		statement.setString(6, giveaway.getWinnerPrize());
		statement.setInt(7, giveaway.getWinnerAmount());
		int rows = statement.executeUpdate();
		if (rows == 0) throw new SQLException("Giveaway wasn't inserted.");
		ResultSet rs = statement.getGeneratedKeys();
		if (rs.next()){
			giveaway.setId(rs.getInt("id"));
		}
		log.info("Inserted new Giveaway: {}", giveaway);
		return giveaway;
	}

	//UPDATING:

	public void markInactive(long id) throws SQLException {
		PreparedStatement statement = con.prepareStatement("UPDATE giveaways SET active = FALSE WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, id);
		statement.executeUpdate();
	}

	public Giveaway updateParticipants(Giveaway old, long[] participants) throws SQLException {
		PreparedStatement statement = con.prepareStatement("UPDATE giveaways SET participants = ? WHERE id = ?");
		statement.setArray(1, con.createArrayOf("BIGINT", Arrays.stream(participants).mapToObj(o -> (Object) o).toArray()));
		statement.setLong(2, old.getId());
		int rows = statement.executeUpdate();
		if (rows == 0) throw new SQLException("Could not update Meeting participants. Meeting: " + old);
		old.setParticipants(participants);
		return old;
	}

	public Giveaway updateWinners(Giveaway old, long[] winners) throws SQLException {
		PreparedStatement statement = con.prepareStatement("UPDATE giveaways SET winners = ? WHERE id = ?");
		statement.setArray(1, con.createArrayOf("BIGINT", Arrays.stream(winners).mapToObj(o -> (Object) o).toArray()));
		statement.setLong(2, old.getId());
		int rows = statement.executeUpdate();
		if (rows == 0) throw new SQLException("Could not update Meeting winners. Meeting: " + old);
		old.setWinners(winners);
		return old;
	}

	public Giveaway updateMessage(Giveaway old, long messageId) throws SQLException {
		PreparedStatement statement = con.prepareStatement("UPDATE giveaways SET message_id = ? WHERE id = ?");
		statement.setLong(1, messageId);
		statement.setLong(2, old.getId());
		int rows = statement.executeUpdate();
		if (rows == 0) throw new SQLException("Could not update Meeting winners. Meeting: " + old);
		old.setMessageId(messageId);
		return old;
	}

	//GETTING:

	public Optional<Giveaway> getById(long id) throws SQLException {
		Giveaway giveaway = null;
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, id);
		ResultSet rs = statement.executeQuery();
		if (rs.next()) {
			giveaway = this.readGiveaway(rs);
		}
		rs.close();
		return Optional.ofNullable(giveaway);
	}

	public List<Giveaway> getActive() throws SQLException {
		List<Giveaway> giveaways = new ArrayList<>();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE active = TRUE", Statement.RETURN_GENERATED_KEYS);
		ResultSet rs = statement.executeQuery();
		while (rs.next()){
			giveaways.add(this.readGiveaway(rs));
		}
		rs.close();
		return giveaways;
	}

	public List<Giveaway> getActiveByHostId(long hostId) throws SQLException {
		List<Giveaway> giveaways = new ArrayList<>();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE hosted_by = ? AND active = TRUE", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, hostId);
		ResultSet rs = statement.executeQuery();
		while (rs.next()){
			giveaways.add(this.readGiveaway(rs));
		}
		rs.close();
		return giveaways;
	}

	public List<Giveaway> getAllByHostId(long hostId) throws SQLException {
		List<Giveaway> giveaways = new ArrayList<>();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE hosted_by = ?", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, hostId);
		ResultSet rs = statement.executeQuery();
		while (rs.next()){
			giveaways.add(this.readGiveaway(rs));
		}
		rs.close();
		return giveaways;
	}

	public List<Giveaway> getActiveByParticipantId(long participantId) throws SQLException {
		List<Giveaway> giveaways = new ArrayList<>();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE ARRAY_CONTAINS(participants, ?) = TRUE AND active = TRUE", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, participantId);
		ResultSet rs = statement.executeQuery();
		while (rs.next()){
			giveaways.add(this.readGiveaway(rs));
		}
		rs.close();
		return giveaways;
	}

	public List<Giveaway> getAllByParticipantId(long participantId) throws SQLException {
		List<Giveaway> giveaways = new ArrayList<>();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE ARRAY_CONTAINS(participants, ?) = TRUE", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, participantId);
		ResultSet rs = statement.executeQuery();
		while (rs.next()){
			giveaways.add(this.readGiveaway(rs));
		}
		rs.close();
		return giveaways;
	}

	public List<Giveaway> getActiveByGuildId(long guildId) throws SQLException {
		List<Giveaway> giveaways = new ArrayList<>();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE guild_id = ? AND active = TRUE", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, guildId);
		ResultSet rs = statement.executeQuery();
		while (rs.next()){
			giveaways.add(this.readGiveaway(rs));
		}
		rs.close();
		return giveaways;
	}

	public List<Giveaway> getAllByGuildId(long participantId) throws SQLException {
		List<Giveaway> giveaways = new ArrayList<>();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE guild_id = ?", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, participantId);
		ResultSet rs = statement.executeQuery();
		while (rs.next()){
			giveaways.add(this.readGiveaway(rs));
		}
		rs.close();
		return giveaways;
	}

	public Giveaway readGiveaway(ResultSet rs) throws SQLException {
		Giveaway giveaway = new Giveaway();
		giveaway.setId(rs.getLong("id"));
		giveaway.setGuildId(rs.getLong("guild_id"));
		giveaway.setChannelId(rs.getLong("channel_id"));
		giveaway.setMessageId(rs.getLong("message_id"));
		giveaway.setHostedBy(rs.getLong("created_by"));
		giveaway.setWinners(this.convertArrayToLongArray(rs.getArray("winners")));
		giveaway.setParticipants(this.convertArrayToLongArray(rs.getArray("participants")));
		giveaway.setCreatedAt(rs.getTimestamp("created_at"));
		giveaway.setDueAt(rs.getTimestamp("due_at"));
		giveaway.setWinnerPrize(rs.getString("winner_prize"));
		giveaway.setWinnerAmount(rs.getInt("winner_amount"));
		giveaway.setActive(rs.getBoolean("active"));
		return giveaway;
	}

	private long[] convertArrayToLongArray(Array array) throws SQLException {
		Object[] tmp = (Object[]) array.getArray();
		long[] longArray = new long[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			longArray[i] = (Long) tmp[i];
		}
		return longArray;
	}
}
