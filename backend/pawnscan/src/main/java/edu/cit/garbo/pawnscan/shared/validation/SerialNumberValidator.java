package edu.cit.garbo.pawnscan.shared.validation;

import java.util.Locale;
import java.util.regex.Pattern;

public final class SerialNumberValidator {

    public static final int MAX_LENGTH = 255;
    public static final String ALLOWED_CHARACTERS_PATTERN = "^[A-Za-z0-9 _./:#=+\\-]+$";
    public static final String ALLOWED_CHARACTERS_MESSAGE =
            "Serial number can only contain letters, numbers, spaces, and - _ . / : # + =";

    private static final Pattern SERIAL_PATTERN = Pattern.compile(ALLOWED_CHARACTERS_PATTERN);

    private SerialNumberValidator() {
    }

    public static String normalize(String serialNumber) {
        return serialNumber == null ? "" : serialNumber.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean isValid(String normalizedSerialNumber) {
        return normalizedSerialNumber != null
                && !normalizedSerialNumber.isBlank()
                && normalizedSerialNumber.length() <= MAX_LENGTH
                && SERIAL_PATTERN.matcher(normalizedSerialNumber).matches();
    }
}
