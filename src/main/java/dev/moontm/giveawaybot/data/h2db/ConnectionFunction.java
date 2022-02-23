/*
 * This File was initially copied from the JavaBot (https://github.com/Java-Discord/JavaBot) and might be
 * modified to better fit this project's purpose.
 */
package dev.moontm.giveawaybot.data.h2db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface for connecting to the H2 SQL Database.
 *
 * @param <T> The generic type that is returned.
 */
@FunctionalInterface
public interface ConnectionFunction<T> {
	T apply(Connection c) throws SQLException;
}
