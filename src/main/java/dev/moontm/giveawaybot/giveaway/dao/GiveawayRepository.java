package dev.moontm.giveawaybot.giveaway.dao;

import dev.moontm.giveawaybot.giveaway.model.Giveaway;
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

	/**
	 * Inserts the given {@link Giveaway} into the Database.
	 *
	 * @param giveaway The {@link Giveaway} to insert.
	 * @return The {@link Giveaway}, with ID.
	 * @throws SQLException If anything goes wrong,
	 */
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
		statement.executeUpdate();
		ResultSet rs = statement.getGeneratedKeys();
		if (rs.next()) {
			giveaway.setId(rs.getInt("id"));
		}
		log.info("Inserted new Giveaway: {}", giveaway);
		this.con.close();
		return giveaway;
	}

	//UPDATING:

	/**
	 * Marks the given {@link Giveaway} as inactive.
	 *
	 * @param id The {@link Giveaway}'s ID.
	 * @throws SQLException If anything goes wrong.
	 */
	public void markInactive(long id) throws SQLException {
		PreparedStatement statement = con.prepareStatement("UPDATE giveaways SET active = FALSE WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, id);
		statement.executeUpdate();
		this.con.close();
	}

	/**
	 * Updates the {@link Giveaway}'s participants.
	 *
	 * @param old The old {@link Giveaway}.
	 * @param participants The array of participant user-IDs.
	 * @return The updated {@link Giveaway}.
	 * @throws SQLException If anything goes wrong.
	 */
	public Giveaway updateParticipants(Giveaway old, long[] participants) throws SQLException {
		PreparedStatement statement = con.prepareStatement("UPDATE giveaways SET participants = ? WHERE id = ?");
		statement.setArray(1, con.createArrayOf("BIGINT", Arrays.stream(participants).mapToObj(o -> (Object) o).toArray()));
		statement.setLong(2, old.getId());
		int rows = statement.executeUpdate();
		if (rows == 0) throw new SQLException("Could not update Meeting participants. Meeting: " + old);
		old.setParticipants(participants);
		this.con.close();
		return old;
	}

	/**
	 * Updates the {@link Giveaway}'s winners.
	 *
	 * @param old The old {@link Giveaway}.
	 * @param winners The array of winner user-IDs.
	 * @return The updated {@link Giveaway}.
	 * @throws SQLException If anything goes wrong.
	 */
	public Giveaway updateWinners(Giveaway old, long[] winners) throws SQLException {
		PreparedStatement statement = con.prepareStatement("UPDATE giveaways SET winners = ? WHERE id = ?");
		statement.setArray(1, con.createArrayOf("BIGINT", Arrays.stream(winners).mapToObj(o -> (Object) o).toArray()));
		statement.setLong(2, old.getId());
		int rows = statement.executeUpdate();
		if (rows == 0) throw new SQLException("Could not update Meeting winners. Meeting: " + old);
		old.setWinners(winners);
		this.con.close();
		return old;
	}

	/**
	 * Updates the {@link Giveaway}'s Message.
	 *
	 * @param old The old {@link Giveaway}.
	 * @param messageId The new message id.
	 * @return The updated {@link Giveaway}
	 * @throws SQLException If anything goes wrong.
	 */
	public Giveaway updateMessage(Giveaway old, long messageId) throws SQLException {
		PreparedStatement statement = con.prepareStatement("UPDATE giveaways SET message_id = ? WHERE id = ?");
		statement.setLong(1, messageId);
		statement.setLong(2, old.getId());
		int rows = statement.executeUpdate();
		if (rows == 0) throw new SQLException("Could not update Meeting winners. Meeting: " + old);
		old.setMessageId(messageId);
		this.con.close();
		return old;
	}

	//GETTING:

	/**
	 * Gets a giveaway by it's ID.
	 *
	 * @param id The ID of the {@link Giveaway}.
	 * @return An {@link Optional} with the {@link Giveaway}.
	 * @throws SQLException If anything goes wrong.
	 */
	public Optional<Giveaway> getById(long id) throws SQLException {
		Giveaway giveaway = null;
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE id = ?", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, id);
		ResultSet rs = statement.executeQuery();
		if (rs.next()) {
			giveaway = readGiveaway(rs);
		}
		rs.close();
		this.con.close();
		return Optional.ofNullable(giveaway);
	}

	/**
	 * Gets a {@link List} of active {@link Giveaway}s.
	 *
	 * @return The {@link List} of {@link Giveaway}s.
	 * @throws SQLException If anything goes wrong.
	 */
	public List<Giveaway> getActive() throws SQLException {
		List<Giveaway> giveaways = new ArrayList<>();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE active = TRUE", Statement.RETURN_GENERATED_KEYS);
		ResultSet rs = statement.executeQuery();
		while (rs.next()) {
			giveaways.add(readGiveaway(rs));
		}
		return giveaways;
	}

	/**
	 * Gets all active giveaways the user is hosting.
	 *
	 * @param hostId The user id of the host.
	 * @return The {@link List} of {@link Giveaway}s.
	 * @throws SQLException If anything goes wrong.
	 */
	public List<Giveaway> getActiveByHostId(long hostId) throws SQLException {
		List<Giveaway> giveaways = new ArrayList<>();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE hosted_by = ? AND active = TRUE", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, hostId);
		ResultSet rs = statement.executeQuery();
		while (rs.next()) {
			giveaways.add(readGiveaway(rs));
		}
		rs.close();
		this.con.close();
		return giveaways;
	}

	/**
	 * Gets all giveaways the user has hosted.
	 *
	 * @param hostId The user id of the host.
	 * @return The {@link List} of {@link Giveaway}s.
	 * @throws SQLException If anything goes wrong.
	 */
	public List<Giveaway> getAllByHostId(long hostId) throws SQLException {
		List<Giveaway> giveaways = new ArrayList<>();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE hosted_by = ?", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, hostId);
		ResultSet rs = statement.executeQuery();
		while (rs.next()) {
			giveaways.add(readGiveaway(rs));
		}
		rs.close();
		this.con.close();
		return giveaways;
	}

	/**
	 * Gets all active giveaways the user is participating in.
	 *
	 * @param participantId The user id of the participant.
	 * @return The {@link List} of {@link Giveaway}s.
	 * @throws SQLException If anything goes wrong.
	 */
	public List<Giveaway> getActiveByParticipantId(long participantId) throws SQLException {
		List<Giveaway> giveaways = new ArrayList<>();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE ARRAY_CONTAINS(participants, ?) = TRUE AND active = TRUE", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, participantId);
		ResultSet rs = statement.executeQuery();
		while (rs.next()) {
			giveaways.add(readGiveaway(rs));
		}
		rs.close();
		this.con.close();
		return giveaways;
	}

	/**
	 * Gets all giveaways the user has participated in.
	 *
	 * @param participantId The user id of the host.
	 * @return The {@link List} of {@link Giveaway}s.
	 * @throws SQLException If anything goes wrong.
	 */
	public List<Giveaway> getAllByParticipantId(long participantId) throws SQLException {
		List<Giveaway> giveaways = new ArrayList<>();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE ARRAY_CONTAINS(participants, ?) = TRUE", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, participantId);
		ResultSet rs = statement.executeQuery();
		while (rs.next()) {
			giveaways.add(readGiveaway(rs));
		}
		rs.close();
		this.con.close();
		return giveaways;
	}

	/**
	 * Gets all active giveaways in the given Guild.
	 *
	 * @param guildId The id of the guild.
	 * @return The {@link List} of {@link Giveaway}s.
	 * @throws SQLException If anything goes wrong.
	 */
	public List<Giveaway> getActiveByGuildId(long guildId) throws SQLException {
		List<Giveaway> giveaways = new ArrayList<>();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE guild_id = ? AND active = TRUE", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, guildId);
		ResultSet rs = statement.executeQuery();
		while (rs.next()) {
			giveaways.add(readGiveaway(rs));
		}
		rs.close();
		this.con.close();
		return giveaways;
	}

	/**
	 * Gets all giveaways that have been hosted in the guild.
	 *
	 * @param participantId The user id of the host.
	 * @return The {@link List} of {@link Giveaway}s.
	 * @throws SQLException If anything goes wrong.
	 */
	public List<Giveaway> getAllByGuildId(long participantId) throws SQLException {
		List<Giveaway> giveaways = new ArrayList<>();
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE guild_id = ?", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, participantId);
		ResultSet rs = statement.executeQuery();
		while (rs.next()) {
			giveaways.add(readGiveaway(rs));
		}
		rs.close();
		this.con.close();
		return giveaways;
	}

	/**
	 * Gets a Giveaway from the given Message-Id
	 *
	 * @param messageId The ID of the Message.
	 * @return An {@link Optional} of the {@link Giveaway}.
	 * @throws SQLException If anything goes wrong.
	 */
	public Optional<Giveaway> getByMessageId(long messageId) throws SQLException {
		Giveaway giveaway = null;
		PreparedStatement statement = con.prepareStatement("SELECT * FROM giveaways WHERE message_id = ?", Statement.RETURN_GENERATED_KEYS);
		statement.setLong(1, messageId);
		ResultSet rs = statement.executeQuery();
		if (rs.next()) {
			giveaway = readGiveaway(rs);
		}
		rs.close();
		this.con.close();
		return Optional.ofNullable(giveaway);
	}

	/**
	 * Gets the amount of active Giveaways in the Database.
	 *
	 * @return The amount.
	 */
	public int getActiveGiveawayCount() {
		try {
			int count = 0;
			PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM giveaways WHERE active = TRUE", Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			this.con.close();
			return count;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Gets total amount of Giveaways in the Database.
	 *
	 * @return The amount.
	 */
	public int getTotalGiveawayCount() {
		try {
			int count = 0;
			PreparedStatement statement = con.prepareStatement("SELECT COUNT(*) FROM giveaways", Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = statement.executeQuery();
			if (rs.next()) {
				count = rs.getInt(1);
			}
			rs.close();
			this.con.close();
			return count;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Reads the given {@link ResultSet} into a {@link Giveaway}
	 *
	 * @param rs The {@link ResultSet}.
	 * @return The {@link Giveaway}.
	 * @throws SQLException If anything goes wrong.
	 */
	public static Giveaway readGiveaway(ResultSet rs) throws SQLException {
		Giveaway giveaway = new Giveaway();
		giveaway.setId(rs.getLong("id"));
		giveaway.setGuildId(rs.getLong("guild_id"));
		giveaway.setChannelId(rs.getLong("channel_id"));
		giveaway.setMessageId(rs.getLong("message_id"));
		giveaway.setHostedBy(rs.getLong("hosted_by"));
		giveaway.setWinners(convertArrayToLongArray(rs.getArray("winners")));
		giveaway.setParticipants(convertArrayToLongArray(rs.getArray("participants")));
		giveaway.setCreatedAt(rs.getTimestamp("created_at"));
		giveaway.setDueAt(rs.getTimestamp("due_at"));
		giveaway.setWinnerPrize(rs.getString("winner_prize"));
		giveaway.setWinnerAmount(rs.getInt("winner_amount"));
		giveaway.setActive(rs.getBoolean("active"));
		return giveaway;
	}

	/**
	 * Converts an {@link Array} to a Long-Array.
	 *
	 * @param array The {@link Array}.
	 * @return The Long-Array.
	 * @throws SQLException If anything goes wrong.
	 */
	public static long[] convertArrayToLongArray(Array array) throws SQLException {
		long[] emptyArray = new long[0];
		if (array == null) return emptyArray;
		Object[] tmp = (Object[]) array.getArray();
		long[] longArray = new long[tmp.length];
		for (int i = 0; i < tmp.length; i++) {
			longArray[i] = (Long) tmp[i];
		}
		return longArray;
	}

	/**
	 * Converts an Object-Array to a Long-Array.
	 *
	 * @param array The Object-Array.
	 * @return The Long-Array.
	 */
	public static long[] convertObjectArrayToLongArray(Object[] array) {
		long[] emptyArray = new long[0];
		if (array == null) return emptyArray;
		long[] longArray = new long[array.length];
		for (int i = 0; i < array.length; i++) {
			longArray[i] = (Long) array[i];
		}
		return longArray;
	}
}
