package net.m9studio.springrelay.config.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import net.m9studio.springrelay.config.model.Entry;
import net.m9studio.springrelay.config.model.EntryParameter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EntryLoader {

    private final ObjectMapper mapper;
    private final Validator validator;

    public EntryLoader() {
        this.mapper = new ObjectMapper(new YAMLFactory())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    public List<Entry> loadAllFromDir(File dir) throws IOException {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Config directory not found: " + (dir == null ? "null" : dir));
        }

        List<Entry> all = new ArrayList<>();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files == null) return all;

        for (File f : files) {
            List<Entry> entries = mapper.readValue(f, new TypeReference<List<Entry>>() {});
            // валидация каждого Entry
            for (Entry e : entries) {
                validate(e, "File: " + f.getName());
            }
            all.addAll(entries);
        }
        return all;
    }

    private void validate(Entry e, String context) {
        Set<ConstraintViolation<Entry>> violations = validator.validate(e);
        if (!violations.isEmpty()) {
            // Добавим контекст (имя файла) для удобства
            throw new ConstraintViolationException(
                    context + " -> " + violations.iterator().next().getMessage(), violations);
        }
        // при желании — доп. кросс-проверки параметров
        if (e.getParameters() != null) {
            for (EntryParameter p : e.getParameters()) {
                Set<ConstraintViolation<EntryParameter>> pv = validator.validate(p);
                if (!pv.isEmpty()) {
                    throw new ConstraintViolationException(context + " (parameter '" + p.getName() + "')",
                                                           (Set) pv);
                }
            }
        }
    }
}
