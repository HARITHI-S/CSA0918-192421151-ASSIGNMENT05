package com.co5.assignment;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.*;

public final class Migration {
    private Migration() {}
    public static void migrate(Path csvFile, Connection connection) throws Exception {
        List<String> rejected=new ArrayList<>();
        List<String[]> valid=new PassStore(csvFile).readValidRows(rejected);
        System.out.println("Validated records: "+valid.size()+", rejected records: "+rejected.size());
        rejected.forEach(System.out::println);
        int loaded=PassLoader.loadAll(connection,valid);
        System.out.println("Committed records: "+loaded);
    }
}
