/*
 * This File was initially copied from the JavaBot (https://github.com/Java-Discord/JavaBot) and might be
 * modified to better fit this project's purpose.
 */
package dev.moontm.giveawaybot.data.h2db;

import java.sql.SQLException;

/**
 * Functional interface for defining operations that consume a specified data-
 * access object.
 *
 * @param <T> The type of the data access object.
 */
@FunctionalInterface
public interface DaoConsumer<T> {
	void consume(T dao) throws SQLException;
}
