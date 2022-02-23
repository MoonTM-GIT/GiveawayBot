/*
 * This File was initially copied from the JavaBot (https://github.com/Java-Discord/JavaBot) and might be
 * modified to better fit this project's purpose.
 */
package dev.moontm.giveawaybot.data.h2db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Functional interface for defining operations that use a Connection.
 */
@FunctionalInterface
public interface ConnectionConsumer {
	void consume(Connection con) throws SQLException;
}
