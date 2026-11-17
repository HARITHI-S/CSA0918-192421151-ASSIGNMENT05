package com.co5.assignment;

public record Pass(String passId, String regNo, String name, int routeNo,
                   String boardingStop, String validityDate, double amount) {
    public String[] toCsvFields() {
        return new String[]{passId, regNo, name, String.valueOf(routeNo),
                boardingStop, validityDate, String.valueOf(amount)};
    }
}
