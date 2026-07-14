package com.helios.platform.pulse.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Converter(autoApply = true)
public class LocalDateTimeStringConverter implements AttributeConverter<LocalDateTime, String> {

    // El formato actual en BD es "yyyy-MM-dd HH:mm"
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public String convertToDatabaseColumn(LocalDateTime attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.format(FORMATTER);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        // Si el string es solo "yyyy-MM-dd", le agregamos tiempo para que no falle el parseo
        if (dbData.length() == 10) {
            dbData = dbData + " 00:00";
        }
        return LocalDateTime.parse(dbData, FORMATTER);
    }
}
