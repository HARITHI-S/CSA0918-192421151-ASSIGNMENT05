package com.co5.assignment;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class PassStore {
    private final Path file;
    public PassStore(Path file) { this.file = file; }

    // CSV-safe writer: quote fields containing comma, quote or newline.
    private static String csv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            return "\"" + value.replace("\"", "\"\"") + "\"";
        return value;
    }

    private static List<String> parseCsv(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    field.append('"'); i++;
                } else quoted = !quoted;
            } else if (c == ',' && !quoted) {
                out.add(field.toString()); field.setLength(0);
            } else field.append(c);
        }
        if (quoted) throw new IllegalArgumentException("Unclosed quoted field");
        out.add(field.toString());
        return out;
    }

    public void add(Pass p) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(String.join(",", Arrays.stream(p.toCsvFields()).map(PassStore::csv).toList()));
            w.newLine();
        }
    }

    public String[] find(String passId) throws IOException {
        if (!Files.exists(file)) return null;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    List<String> fields = parseCsv(line);
                    if (fields.size() != 7) continue;
                    if (fields.get(0).equals(passId)) {
                        Integer.parseInt(fields.get(3));
                        Double.parseDouble(fields.get(6));
                        return fields.toArray(String[]::new);
                    }
                } catch (RuntimeException ignored) {
                    // A damaged row is isolated; continue scanning subsequent rows.
                }
            }
        }
        return null;
    }

    public List<String[]> listByRoute(int routeNo) throws IOException {
        List<String[]> result = new ArrayList<>();
        if (!Files.exists(file)) return result;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    List<String> f = parseCsv(line);
                    if (f.size() == 7 && Integer.parseInt(f.get(3)) == routeNo) {
                        Double.parseDouble(f.get(6));
                        result.add(f.toArray(String[]::new));
                    }
                } catch (RuntimeException ignored) { }
            }
        }
        return result;
    }

    public List<String[]> readValidRows(List<String> errors) throws IOException {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(file)) return rows;
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line; int lineNo = 0;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                try {
                    List<String> f = parseCsv(line);
                    if (f.size() != 7) throw new IllegalArgumentException("expected 7 fields");
                    Integer.parseInt(f.get(3)); Double.parseDouble(f.get(6));
                    if (f.stream().anyMatch(String::isBlank)) throw new IllegalArgumentException("empty field");
                    rows.add(f.toArray(String[]::new));
                } catch (RuntimeException ex) {
                    errors.add("Line " + lineNo + ": " + ex.getMessage() + " [" + line + "]");
                }
            }
        }
        return rows;
    }

    public Path getFile() { return file; }
}
