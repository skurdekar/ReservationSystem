package com.xyz.reservations.domain;

public class Seat {
    public int row;
    public int column;
    public String status = "A"; //A, H, R (free, held, reserved)

    public Seat(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public String toString() {
        return "Seat Row: " + row + " Column: " + column + " Status: " + status;
    }
}
