package com.co5.assignment;

import java.io.IOException;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("CO5 Assignment 5 - Java Persistence Engineering");
        System.out.println("Java: " + System.getProperty("java.version"));
        runFileDemo();
        runLineCounterDemo();
        runAdversarialFileDemo();
        if (args.length > 0 && args[0].equalsIgnoreCase("--bench")) Benchmarks.run();
        if (args.length > 0 && args[0].equalsIgnoreCase("--db")) runDatabaseTasks();
        else System.out.println("\nDatabase tasks skipped. Configure MySQL, then run: mvn exec:java -Dexec.args=--db");
    }

    private static void runFileDemo() throws IOException {
        System.out.println("\n[B1] Pass store demonstration");
        Path file=Path.of("data","passes.csv");
        Files.createDirectories(file.getParent()); Files.deleteIfExists(file);
        PassStore store=new PassStore(file);
        store.add(new Pass("P1001","192311001","Arun",12,"Central Stop","2027-12-31",4500));
        store.add(new Pass("P1002","192311002","Meena, Devi",7,"Library","2027-12-31",3800));
        Files.writeString(file,"P-BAD,DAMAGED LINE\n",StandardOpenOption.APPEND);
        store.add(new Pass("P1003","192311003","Divya",7,"Hostel","2027-12-31",3800));
        System.out.println("Find P1003: "+Arrays.toString(store.find("P1003")));
        System.out.println("Find missing: "+Arrays.toString(store.find("P9999")));
        System.out.println("Route 7 count: "+store.listByRoute(7).size());
        System.out.println("CSV file: "+file.toAbsolutePath());
    }

    private static void runLineCounterDemo() throws IOException {
        System.out.println("\n[B1] Buffered/unbuffered line count timings (actual values from this machine)");
        Path p=Path.of("data","sample_1mb.txt"); Files.createDirectories(p.getParent());
        try(var w=Files.newBufferedWriter(p)) { for(int i=0;i<12000;i++) w.write("Attendance record "+i+"\n"); }
        long a=LineCounter.timeMillis(()->LineCounter.countUnbuffered(p));
        long b=LineCounter.timeMillis(()->LineCounter.countBuffered(p));
        System.out.println("Lines: "+LineCounter.countBuffered(p)+", unbuffered ms: "+a+", buffered ms: "+b);
        System.out.println("For full 1/10/50 MB and growth measurements run the --bench mode (see README). ");
    }

    private static void runAdversarialFileDemo() throws IOException {
        System.out.println("\n[B2] Damaged-file validation");
        Path p=Path.of("data","damaged.csv");
        Files.writeString(p,"P1,R1,Good,2,Gate,2027-12-31,4500\nP2,R2,Bad,three,Gate,2027-12-31,xx\nempty\n");
        List<String> errors=new ArrayList<>();
        List<String[]> valid=new PassStore(p).readValidRows(errors);
        System.out.println("Valid rows: "+valid.size());
        errors.forEach(System.out::println);
    }

    private static void runDatabaseTasks() throws SQLException, IOException {
        System.out.println("\n[DB] Connecting to MySQL using DB_URL, DB_USER and DB_PASSWORD...");
        try(Connection c=Db.connect()) {
            JdbcTasks.metadata(c);
            JdbcTasks.verifyTable(c);
            try(Statement s=c.createStatement()) {
                s.executeUpdate("DELETE FROM bus_pass");
                s.executeUpdate("DELETE FROM app_user");
                s.executeUpdate("INSERT INTO app_user(user_id,pwd) VALUES('student','correct-password')");
            }
            JdbcTasks.unsafeLoginDemo(c,"' OR '1'='1' -- ","wrong");
            JdbcTasks.safeLoginDemo(c,"' OR '1'='1' -- ","wrong");
            for(String mode:List.of("statement","prepared","batch")) {
                try(Statement s=c.createStatement()) { s.executeUpdate("DELETE FROM bus_pass"); }
                long ms=JdbcTasks.insertGenerated(c,1000,mode,true);
                System.out.println(mode+" 1000 rows transaction time ms: "+ms);
            }
            System.out.println("B3/B4 database demo completed. Repeat with 10000 and 100000 via JdbcTasks.insertGenerated.");
        }
    }
}
