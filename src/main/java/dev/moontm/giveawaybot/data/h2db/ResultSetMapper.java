/*
 * This File was initially copied from the JavaBot (https://github.com/Java-Discord/JavaBot) and might be
 * modified to better fit this project's purpose.
 */
package dev.moontm.giveawaybot.data.h2db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Interface for mapping {@link ResultSet}s.
 *
 * @param <T> The generic type.
 */
@FunctionalInterface
public interface ResultSetMapper<T> {
	T map(ResultSet rs) throws SQLException;
}
