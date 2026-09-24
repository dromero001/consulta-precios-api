package com.ecommerce.prices.webapp.configuration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.Parser;
import org.springframework.format.Printer;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StrictLocalDateTimeFormatConfiguration implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatterForFieldAnnotation(new LocalDateTimeWithoutOffsetFormatterFactory());
    }

    private static final class LocalDateTimeWithoutOffsetFormatterFactory implements AnnotationFormatterFactory<DateTimeFormat> {

        @Override
        public Set<Class<?>> getFieldTypes() {
            return Set.of(LocalDateTime.class);
        }

        @Override
        public Printer<LocalDateTime> getPrinter(DateTimeFormat annotation, Class<?> fieldType) {
            return (value, locale) -> DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value);
        }

        @Override
        public Parser<LocalDateTime> getParser(DateTimeFormat annotation, Class<?> fieldType) {
            return (text, locale) -> LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
