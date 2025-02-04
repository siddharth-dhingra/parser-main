package com.parser.Parser.utils;

import com.parser.Parser.Application.model.Finding;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FindingComparator {

    public static boolean hasSignificantDifferences(Finding existingFinding, Finding incomingFinding) {
        String existingHash = computeComparisonHash(existingFinding);
        String incomingHash = computeComparisonHash(incomingFinding);
        return !existingHash.equals(incomingHash);
    }

    private static String computeComparisonHash(Finding finding) {
        String severity = (finding.getSeverity() != null) ? finding.getSeverity().toString() : "";
        String status = (finding.getStatus() != null) ? finding.getStatus().toString() : "";
        // Concatenate the fields together with a delimiter.
        String combined = severity + "::" + status;
        return sha256Hex(combined);
    }

    private static String sha256Hex(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error computing SHA-256 hash", e);
        }
    }
}
