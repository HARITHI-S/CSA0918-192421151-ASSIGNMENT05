package com.co5.assignment;

import java.nio.file.*;
import java.io.*;
import java.util.*;

public final class Benchmarks {
    private Benchmarks() {}
    public static void run() throws Exception {
        Path dir=Path.of("data"); Files.createDirectories(dir);
        System.out.println("\n[Benchmark] Line counter: generated test files");
        for (int mb : new int[]{1,10,50}) {
            Path p=dir.resolve("attendance_"+mb+"mb.txt");
            try(OutputStream out=Files.newOutputStream(p)) {
                byte[] line="attendance-record-0123456789\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                long target=mb*1024L*1024L, written=0;
                while(written+line.length<=target){out.write(line);written+=line.length;}
                while(written<target){out.write('x');written++;}
            }
            long u=LineCounter.timeMillis(()->LineCounter.countUnbuffered(p));
            long b=LineCounter.timeMillis(()->LineCounter.countBuffered(p));
            System.out.printf(Locale.ROOT,"%d MB (%d bytes): unbuffered=%d ms, buffered=%d ms%n",mb,Files.size(p),u,b);
        }
        System.out.println("\n[Benchmark] Pass lookup by sequential scan");
        for(int n:new int[]{1000,10000,50000,100000}) {
            Path p=dir.resolve("lookup_"+n+".csv");
            try(BufferedWriter w=Files.newBufferedWriter(p)) {
                for(int i=0;i<n;i++) w.write(String.format(Locale.ROOT,"P%06d,R%06d,Student%d,%d,Stop,2027-12-31,4500.0%n",i,i,i,i%20));
            }
            PassStore store=new PassStore(p); String target=String.format(Locale.ROOT,"P%06d",n-1);
            long ms=LineCounter.timeMillis(()->store.find(target));
            System.out.printf(Locale.ROOT,"%d records, last-record lookup: %d ms%n",n,ms);
        }
    }
}
