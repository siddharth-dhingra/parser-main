package com.parser.Parser.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parser.Parser.Application.model.Severity;
import com.parser.Parser.Application.model.Status;

public class ParserUtils {

    private ParserUtils() {}

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    public static Status mapStatus(String rawState) {
        if (rawState == null) {
            return Status.OPEN;
        }
        switch (rawState.toLowerCase()) {
            case "open":
                return Status.OPEN;
            case "auto_dismissed":
                return Status.SUPPRESSED;
            case "dismissed":
                return Status.FALSE_POSITIVE;
            case "fixed":
            case "resolved":
            case "closed":
                return Status.FIXED;
            case "confirmed":
                return Status.CONFIRM;
            default:
                return Status.OPEN;
        }
    }

    /**
     * Map raw severity string to our Severity enum, which includes SEVERE, CRITICAL, HIGH, MEDIUM, LOW, INFO.
     */
    public static Severity mapSeverity(String rawSev) {
        if (rawSev == null) {
            return Severity.INFO;
        }
        switch (rawSev.toLowerCase()) {
            case "critical":
                return Severity.CRITICAL;
            case "high":
            case "error":
                return Severity.HIGH;
            case "medium":
            case "moderate":
            case "warning":
                return Severity.MEDIUM;
            case "low":
            case "note":
                return Severity.LOW;
            default:
                return Severity.INFO;
        }
    }

    //     /**
//      * Parse a date string like "2025-01-27T05:06:14Z" into Instant.
//      */
//     private Instant parseDate(String dateStr) {
//         if (dateStr == null || dateStr.isEmpty()) {
//             return Instant.now();
//         }
//         try {
//             return Instant.parse(dateStr);
//         } catch (Exception e) {
//             return Instant.now();
//         }
//     }
}
