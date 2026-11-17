package com.co5.assignment;

import java.io.*;
import java.nio.file.*;

public final class LineCounter {
    private LineCounter() {}
    public static long countUnbuffered(Path p) throws IOException {
        long lines = 0; boolean any = false; int last = -1;
        try (InputStream in = Files.newInputStream(p)) {
            int b;
            while ((b = in.read()) != -1) {
                any = true; last = b;
                if (b == '\n') lines++;
            }
        }
        return any && last != '\n' ? lines + 1 : lines;
    }
    public static long countBuffered(Path p) throws IOException {
        long lines = 0; boolean any = false; int last = -1;
        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(p), 64 * 1024)) {
            int b;
            while ((b = in.read()) != -1) {
                any = true; last = b;
                if (b == '\n') lines++;
            }
        }
        return any && last != '\n' ? lines + 1 : lines;
    }
    public static long countWithReader(Path p) throws IOException {
        long lines = 0; boolean any = false; int last = -1;
        try (Reader r = Files.newBufferedReader(p)) {
            int c;
            while ((c = r.read()) != -1) { any = true; last = c; if (c == '\n') lines++; }
        }
        return any && last != '\n' ? lines + 1 : lines;
    }
    public static long timeMillis(IoAction action) throws IOException {
        long start = System.nanoTime(); action.run();
        return (System.nanoTime() - start) / 1_000_000;
    }
    @FunctionalInterface public interface IoAction { void run() throws IOException; }
}
