package be.dontletit.wordle;

import java.time.LocalDate;

/** A set in which at least one card was first printed, as exposed by MTGODatabase. */
public record SetDto(String code, String name, String type, LocalDate releasedAt, int cardCount) {}
