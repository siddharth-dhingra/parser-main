package com.parser.Parser.utils;

import com.parser.Parser.Application.model.Finding;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FindingHashCalculator {

    public static String computeHash(Finding finding) {
        Object numberObj = (finding.getAdditionalData() != null) 
                ? finding.getAdditionalData().get("number") 
                : null;
        String numberStr = (numberObj != null) ? numberObj.toString() : "";
        String title = (finding.getTitle() != null) ? finding.getTitle() : "";
        String uniqueString = numberStr + "::" + title;
        return sha256Hex(uniqueString);
    }

    private static String sha256Hex(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error computing hash", e);
        }
    }
}