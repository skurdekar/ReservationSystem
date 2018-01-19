package com.xyz.reservations.engine;

import com.xyz.reservations.domain.Seat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class TicketServiceCore {

    final static Log logger = LogFactory.getLog(TicketServiceCore.class);

    private Seat[][] seatMap = null;
    private int numRows;
    private int numSeatsAvailable;
    private int numSeats;
    private int seatsPerRow;

    private static int confId;

    /**
     * Construct a TicketServiceCore object
     *
     * @param seatCount
     * @param rowCount
     */
    public TicketServiceCore(int seatCount, int rowCount) {

        //validate parameters
        if (seatCount > 5000 || seatCount < 100) {
            throw new IllegalArgumentException("Seat count cannot be greater than 5000 or less than 0");
        }

        if (rowCount > 100 || rowCount < 10) {
            throw new IllegalArgumentException("Row count cannot be greater than 100 or less than 0");
        }

        if (seatCount % rowCount > 0) {
            throw new IllegalArgumentException("Seat Count has to be defined in multiples of Row count");
        }

        //initialize member variables
        numSeats = seatCount;
        numRows = rowCount;
        seatsPerRow = numSeats / numRows;
        numSeatsAvailable = seatCount;

        //initialize seat map
        initSeatMap();
    }

    /**
     * Creates an empty row of Seats in the seat map
     */
    private void initSeatMap() {
        seatMap = new Seat[numRows][seatsPerRow];
        for (int rowIndex = 0; rowIndex < numRows; rowIndex++) {
            for (int seatIndex = 0; seatIndex < seatsPerRow; seatIndex++) {
                seatMap[rowIndex][seatIndex] = new Seat(rowIndex + 1, seatIndex + 1);
            }
        }
    }

    /**
     * returns the number of available seats in a particular row
     * @param index
     * @return List of seats
     */
    private List<Seat> getAvailableSeatsInRow(int index) {
        List<Seat> seatList = new ArrayList<>();
        Seat[] row = seatMap[index];
        for (Seat seat : row) {
            if (seat.status.equals("A")) {
                seatList.add(seat);
            }
        }
        return seatList;
    }

    /**
     * Return the number of available seats in the entire venue
     *
     * @return the number of available seats
     */
    public synchronized int numSeatsAvailable() {
        return numSeatsAvailable;
    }

    /**
     * Holds the best available seats. The algorithm looks for contiguous and all available seats
     * and returns the contiguous seats if not if not retuns any of the free seats
     *
     * @param numSeatsToHold
     * @return List of best available seats
     */
    public synchronized List<Seat> findAndHoldSeats(int numSeatsToHold) {
        if (numSeatsToHold <= 0 || numSeatsToHold > numSeats) {
            throw new IllegalArgumentException("Invalid value for hold seats: " + numSeatsToHold);
        }

        if(numSeatsToHold > numSeatsAvailable){
            throw new IllegalStateException("Number of seats available is less than requested seats");
        }

        List<Seat> contigFreeSeats = new ArrayList<>(numSeatsToHold);//list of contiguous free seats
        List<Seat> freeSeats = new ArrayList<>(numSeatsToHold);//list of all available free seats

        int rowIndex = 0;
        for (Seat[] row : seatMap) {
            List<Seat> availableSeats = getAvailableSeatsInRow(rowIndex);
            if (availableSeats.size() > 0) {//make sure free seats are available in current row
                int prevSeatIndex = availableSeats.get(0).column;
                for (int i = 0; i < numSeatsToHold && i < availableSeats.size(); i++) {
                    Seat seat = availableSeats.get(i);
                    int currSeatIndex = seat.column;
                    if (freeSeats.size() < numSeatsToHold) {//add to free seats if needed
                        freeSeats.add(seat);
                    }
                    if (currSeatIndex == prevSeatIndex + 1 || currSeatIndex == prevSeatIndex) {//contiguous seat or first available seat
                        contigFreeSeats.add(seat);
                    } else {
                        contigFreeSeats.clear();//reset if either condition is not met
                    }
                    prevSeatIndex = currSeatIndex;
                }
            }
            if (contigFreeSeats.size() == numSeatsToHold) {//if we have contiguous seats we should hold them and stop processing
                break;
            } else {
                //if(contigFreeSeats.size() < numSeatsToHold){//if contigseats are
                contigFreeSeats.clear();
            }
            rowIndex++;
        }

        //return seats from contigmap else from freemap, if neither are enough return empty array
        return postProcessHold(contigFreeSeats, freeSeats, numSeatsToHold);
    }

    private List<Seat> postProcessHold(List<Seat> contigFreeSeats, List<Seat> freeSeats, int numSeatsToHold){
        //return seats from contiglist else from freelist, if neither are enough return empty list
        List<Seat> retval = null;
        if (contigFreeSeats.size() == numSeatsToHold) {
            logger.info("Hold request successfully returned contiguous seats");
            retval = contigFreeSeats;
        } else if (freeSeats.size() == numSeatsToHold) {
            logger.warn("Seats found may not be together");
            retval = freeSeats;
        } else {
            logger.error("Could not find seats to hold. Not enough seats available");
            retval = new ArrayList<>();
        }

        numSeatsAvailable -= retval.size();//decrement numSeatsAvailable
        for (Seat seat : retval) {//mark seats as Held
            seat.status = "H";
        }
        return retval;
    }

    /**
     * Release hold for the seats specified. Only if the seats are on hold will release the hold and
     * increment the number of seats available
     *
     * @param heldSeats
     */
    public synchronized void releaseHold(List<Seat> heldSeats) {
        for (Seat seat : heldSeats) {
            if (seat.status.equals("H")) {
                seat.status = "A";
                numSeatsAvailable += 1;
            }
        }
    }

    /**
     * Reserve the seats in the list
     *
     * @param seatList
     * @return reservation confirmation id
     */
    public synchronized String reserveSeats(List<Seat> seatList) {
        String retval = "";
        boolean reserved = false;
        if (seatList != null) {
            for (Seat seat : seatList) {
                if (seat.status.equals("H")) {
                    reserved = true;
                    seat.status = "X";
                }
            }
        }
        if(reserved) {
            confId++;
            retval = "CONF-" + confId;
            logger.info("Reserve Seats success with confirmation: " + retval);
        }else{
            logger.error("Reserve Seats failed as seats no longer available");
        }

        return retval;
    }

    /**
     * Print the seat map (H indicates hold, X indicated reserved and s indicates vacant
     * @return
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        for (int rowIndex = 0; rowIndex < seatMap.length; rowIndex++) {
            Seat[] row = seatMap[rowIndex];
            for (int seatIndex = 0; seatIndex < row.length; seatIndex++) {
                if (row[seatIndex].status.equals("H")) {
                    sb.append("H");//onhold
                } else if (row[seatIndex].status.equals("X")) {
                    sb.append("X");//reserved
                } else {
                    sb.append("s");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
