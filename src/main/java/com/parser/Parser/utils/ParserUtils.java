package com.parser.Parser.utils;

import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parser.Parser.Application.model.Severity;
import com.parser.Parser.Application.model.Status;
import com.parser.Parser.Application.model.ToolType;

public class ParserUtils {

    private ParserUtils() {}

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    // public static Status mapStatus(String rawState, String reason) {
    //     if (rawState == null) {
    //         return Status.OPEN;
    //     }
    //     switch (rawState.toLowerCase()) {
    //         case "open":
    //             return Status.OPEN;
    //         case "auto_dismissed":
    //             return Status.SUPPRESSED;
    //         case "dismissed":
    //             // return Status.FALSE_POSITIVE;
    //             // If there's a reason like "false positive," map to that
    //             if (reason != null) {
    //                 String reasonLower = reason.toLowerCase(Locale.ROOT);
    //                 // Map "false positive" to our FALSE_POSITIVE
    //                 if ("false positive".equals(reasonLower) || "false_positive".equals(reasonLower) || "inaccurate".equals(reasonLower)) {
    //                     return Status.FALSE_POSITIVE;
    //                 }
    //             }
    //             return Status.SUPPRESSED;
    //         case "fixed":
    //         case "resolved":
    //         case "closed":
    //             return Status.FIXED;
    //         case "confirmed":
    //             return Status.CONFIRM;
    //         default:
    //             return Status.OPEN;
    //     }
    // }

    public static Status mapStatus(String rawState, String reason, ToolType toolType) {
        if (toolType == null) {
            return Status.OPEN;
        }
        switch (toolType) {
            case DEPENDABOT:
                return mapStatusForDependabot(rawState, reason);
            case CODESCAN:
                return mapStatusForCodescan(rawState, reason);
            case SECRETSCAN:
                return mapStatusForSecretscan(rawState, reason);
            default:
                return Status.OPEN;
        }
    }

    public static Status mapStatusForDependabot(String rawState, String reason) {
        if (rawState == null) {
            return Status.OPEN;
        }
        String state = rawState.toLowerCase(Locale.ROOT);
        if ("open".equals(state)) {
            return Status.OPEN;
        } else if ("dismissed".equals(state)) {
            if (reason != null && reason.toLowerCase(Locale.ROOT).equals("inaccurate")) {
                return Status.FALSE_POSITIVE;
            }
            return Status.SUPPRESSED;
        }
        return Status.OPEN;
    }

    /**
     * Codescan mapping:
     * - "open" -> OPEN
     * - "dismissed" with reason "false positive" (or "false_positive") -> FALSE_POSITIVE
     * - "dismissed" with any other reason -> SUPPRESSED
     */
    public static Status mapStatusForCodescan(String rawState, String reason) {
        if (rawState == null) {
            return Status.OPEN;
        }
        String state = rawState.toLowerCase(Locale.ROOT);
        if ("open".equals(state)) {
            return Status.OPEN;
        } else if ("dismissed".equals(state)) {
            if (reason != null) {
                String reasonLower = reason.toLowerCase(Locale.ROOT);
                if (reasonLower.equals("false positive") || reasonLower.equals("false_positive")) {
                    return Status.FALSE_POSITIVE;
                }
            }
            return Status.SUPPRESSED;
        }
        return Status.OPEN;
    }

    /**
     * SecretScan mapping:
     * - "open" -> OPEN
     * - "resolved" with reason "false positive" (or "false_positive") -> FALSE_POSITIVE
     * - "resolved" with any other reason -> SUPPRESSED
     */
    public static Status mapStatusForSecretscan(String rawState, String reason) {
        if (rawState == null) {
            return Status.OPEN;
        }
        String state = rawState.toLowerCase(Locale.ROOT);
        if ("open".equals(state)) {
            return Status.OPEN;
        } else if ("resolved".equals(state)) {
            if (reason != null) {
                String reasonLower = reason.toLowerCase(Locale.ROOT);
                if (reasonLower.equals("false positive") || reasonLower.equals("false_positive")) {
                    return Status.FALSE_POSITIVE;
                }
            }
            return Status.SUPPRESSED;
        }
        return Status.OPEN;
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
