/*
 * This File was initially copied from the JavaBot (https://github.com/Java-Discord/JavaBot) and might be
 * modified to better fit this project's purpose.
 */
package dev.moontm.giveawaybot.data.h2db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface to modify statements.
 */
@FunctionalInterface
public interface StatementModifier {
	void modify(PreparedStatement s) throws SQLException;
}
