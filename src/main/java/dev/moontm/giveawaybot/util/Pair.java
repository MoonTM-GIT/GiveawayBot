/**
 * This File was initially copied from the JavaBot (https://github.com/Java-Discord/JavaBot) and might be
 * modified to better fit this project's purpose.
 */
package dev.moontm.giveawaybot.util;

/**
 * Utility class that stores to variables.
 *
 * @param <F>    First generic.
 * @param <S>    Second generic.
 * @param first  First variable.
 * @param second Second variable.
 */
public record Pair<F, S>(F first, S second) {
}
