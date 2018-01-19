package com.xyz.reservations.engine;

import com.xyz.reservations.domain.SeatHold;
import com.xyz.reservations.service.TicketService;
import com.xyz.reservations.util.EmailValidator;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleHandler {

    private static EmailValidator emv = new EmailValidator();

    /**
     * Read and process user input
     * @param service
     */
    public static void readConsole(TicketService service) {

        String command = "";
        do {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                Thread.sleep(1000);
                System.out.println("Enter Command( hold [numseats email], reserve [holdId email], available, print, end ): ");
                String s = br.readLine();
                String[] splitInput = s.split(" ");
                if (splitInput.length > 0) {
                    command = splitInput[0];
                    processCommand(command, splitInput, service);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } while (!"end".equalsIgnoreCase(command));
    }

    private static void processCommand(String command, String[] splitInput, TicketService service) {

        if ("hold".equalsIgnoreCase(command)) {
            if (splitInput.length == 3) {
                int numSeats = Integer.parseInt(splitInput[1]);
                String email = splitInput[2];
                SeatHold hold = service.findAndHoldSeats(numSeats, email);
                /*if (hold != null) {
                    System.out.println("Hold created HoldId: " + hold.seatHoldId);
                }*/
            } else {
                System.out.println("Invalid number of arguments for hold operation (numseats email)");
            }
        } else if ("release".equalsIgnoreCase(command)) {
            if (splitInput.length == 2) {
                int holdId = Integer.parseInt(splitInput[1]);
                ((TicketServiceImpl)service).releaseHold(holdId);
                //System.out.println("Hold released HoldId: " + holdId);
            } else {
                System.out.println("Invalid number of arguments for release operation (holdid)");
            }
        } else if ("reserve".equalsIgnoreCase(command)) {
            if (splitInput.length == 3) {
                int holdId = Integer.parseInt(splitInput[1]);
                String email = splitInput[2];
                String reserveConfCode = service.reserveSeats(holdId, email);
                /*if (!"".equals(reserveConfCode)) {
                    System.out.println("Reservation complete Confirmation Code :" + reserveConfCode);
                }*/
            } else {
                System.out.println("Invalid number of arguments for release operation (holdid email)");
            }
        } else if ("print".equalsIgnoreCase(command)) {
            System.out.println(((TicketServiceImpl)service).printSeatMap());
        } else if ("available".equalsIgnoreCase(command)) {
            System.out.println("Available Seats: " + service.numSeatsAvailable());
        } else if ("end".equalsIgnoreCase(command)) {
            System.exit(0);
        }
    }
}
