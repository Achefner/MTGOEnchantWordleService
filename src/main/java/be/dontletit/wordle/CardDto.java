package be.dontletit.wordle;

import java.time.LocalDate;
import java.util.List;

/** Card as exposed by MTGODatabase and passed through, unchanged, to the front end. */
public record CardDto(
        String id,
        String name,
        String manaCost,
        List<String> types,
        String text,
        Double power,
        Double toughness,
        List<String> surtypes,
        List<String> subtypes,
        String colorIdentity,
        List<String> sets,
        String originalPrintedSet,
        String originalPrintedSetName,
        String originalPrintedSetType,
        LocalDate firstPrintedAt,
        String rarity,
        String imgUrl
) {}
