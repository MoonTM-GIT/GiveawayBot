package dev.moontm.giveawaybot.util;

import dev.moontm.giveawaybot.Bot;
import dev.moontm.giveawaybot.giveaway.dao.GiveawayRepository;

import java.sql.SQLException;

public class DataUtil {
	public static int getActiveGiveawayCount() {
		try {
			return new GiveawayRepository(Bot.dataSource.getConnection()).getActiveGiveawayCount();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static int getTotalGiveawayCount() {
		try {
			return new GiveawayRepository(Bot.dataSource.getConnection()).getTotalGiveawayCount();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}
}
