/*
 * This File was initially copied from the JavaBot (https://github.com/Java-Discord/JavaBot) and might be
 * modified to better fit this project's purpose.
 */
package dev.moontm.giveawaybot.data.h2db;

import java.sql.Connection;

/**
 * Simple Interface that handles transactions.
 */
public interface TransactionFunction {
	void execute(Connection c) throws Exception;
}
