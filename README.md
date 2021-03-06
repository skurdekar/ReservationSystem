## Reservation Service
Implements a simple ticket service that facilitates discovery, temporary hold and final reservation of seats within a high
demand performance venue. The application will allow end users to hold and reserve the specified number of seats. Every hold will
timeout within a configurable number of seconds. Both the number of seats and rows in the venue are configurable via
application.conf file located in resources folder. The range for numberofseats is(100-5000), rowCount is (10-100) and
hold timeout is (10-120) seconds. All in input variables are assumed to be have integer range upper bounds.

## Assumptions and limitations
 - Users have already been authenticated based on their email id. All users are assumed to have valid accounts in the reservation system
 - Seats are assigned from left to right. The best seats are assumed to be the first ones in the row
 - Once contiguous seats are no longer found seats are randomly assigned and may not necessarily be close to each other
 - The system does not persist reservation data. If the application goes down it starts with a fully available venue

## Using the Service
The service TicketService provides 3 apis to hold, release and get information on the number of seats available
 - `numSeatsAvailable()` returns the number of seats currently available in the venue. This is the total number of seats
 minus the number of seats held and reserved

 - `findAndHoldSeats(numSeats, customerEmail)` will hold the specified number of seats. Returns a SeatHold object which contains a Array of Seat objects(seatArray), customer email(customerEmail) and seatHoldId which is a unique identifier (integer) for each successful hold operation. **If the hold operation is unsuccessful a -1 is returned for seatHoldId

 - `reserveSeats(seatHoldId, customerEmail)` will reserve the specified number of already held seats based on seatHoldId and email. The customerEmail should match the value specified for for hold operation otherwise the reserve operation will fail. The operation returns a confirmation id (String) for the reservation. **If the reservation is unsuccessful an empty string "" is returned.


## API Example
    import com.xyz.reservations.engine.ReservationApp
    import com.xyz.reservations.engine.TicketService
    import com.xyz.reservations.engine.TicketServiceImpl

    TicketService service = TicketServiceImpl.getInstance(
                Settings.seatCount, Settings.rowCount, Settings.holdTimeout);

    impl.numSeatsAvailable()

    SeatHold sh = impl.findAndHoldSeats(numSeats, email)

    String confId = impl.reserveSeats(sh.seatHoldId, email)

    (TicketServiceImpl)impl.printSeatMap()

    XXXXXHHsss
    ssssssssss
    ssssssssss
    ssssssssss
    ssssssssss
    ssssssssss
    ssssssssss
    ssssssssss
    ssssssssss
    ssssssssss

    X indicates reserved seats, H indicate held seats and s indicates vacant seats

## Building and Running the application
The application can be downloaded using the following command
 - `git clone https://github.com/skurdekar/ReservationSystem.git`

The application is packaged with the Gradle Build Tool. It can be built using the following command
 - `gradle build`

The application can be run as follows
 - `gradle run`

The build can be cleaned as follows
 - `gradle clean`
 
*`If gradle is not installed on your system the above commands can be run using gradlew (part of the repo)`

Once run, the application will prompt you with an input prompt for entering commands to process the reservation

`2018-01-19 00:58:16 INFO  TicketServiceImpl: - Created TicketService numSeats: 100, rows: 10, hold timeout(s): 30`
`Enter Command( hold [numseats email], reserve [holdId email], available, print, end ): `

Valid commands are **hold, reserve, available, print and end with their respective options as specified above

The application implements logging using the log4j library. The log configuration is available in log4j.properties
file in resources directory.

The application implements loading configurations using typesafe-config library. The configurations are stored in
application.conf file in resources directory

Alternatively the application can be packaged as a fat jar
 - `gradle fatJar`

Once packaged as a fat jar it can be run as a standalone java application
 - `java -classpath build/libs/ReservationSystem-all-1.0.jar com.xyz.reservations.engine.ReservationApp`


## Tests
 - See ReservationAppTest class for test examples
 Tests should be run using gradle
 - `gradle test`
 Test reports are generated in `build/reports/tests/test/classes` directory
 
 All tests are run everytime a gradle build command is run

## Sample Run via Console
     Created TicketService numSeats: 100, rows: 10, hold timeout(s): 30
     Enter Command( hold [numseats email], reserve [holdId email], available, print, end ):
     hold 4 sss@g.com
     Hold request successfully returned contiguous seats
     Hold succeeded HoldId: 1, notifying sss@g.com
     Enter Command( hold [numseats email], reserve [holdId email], available, print, end ):
     print

     HHHHssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss

     Enter Command( hold [numseats email], reserve [holdId email], available, print, end ):
     reserve 1 sss@gmcom
     java.lang.IllegalArgumentException: Invalid Email format sss@gmcom
      at com.xyz.reservations.engine.TicketServiceImpl.validateEmail(TicketServiceImpl.java:135)
      at com.xyz.reservations.engine.TicketServiceImpl.reserveSeats(TicketServiceImpl.java:89)
      at com.xyz.reservations.engine.ConsoleHandler.processCommand(ConsoleHandler.java:63)
      at com.xyz.reservations.engine.ConsoleHandler.readConsole(ConsoleHandler.java:30)
      at com.xyz.reservations.engine.ReservationApp.main(ReservationApp.java:19)
     Enter Command( hold [numseats email], reserve [holdId email], available, print, end ):
     reserve 1 sss@g.com
     Reserve Seats success with confirmation: CONF-1
     Enter Command( hold [numseats email], reserve [holdId email], available, print, end ):
     print

     XXXXssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss

     Enter Command( hold [numseats email], reserve [holdId email], available, print, end ):
     available
     Available Seats: 96
     Enter Command( hold [numseats email], reserve [holdId email], available, print, end ):
     reserve 2 sss@g.com
     Reservation failed. Seats were never held or hold has expired, notifying sss@g.com
     Enter Command( hold [numseats email], reserve [holdId email], available, print, end ):
     hold 10 ddd@f.com
     Hold request successfully returned contiguous seats
     Hold succeeded HoldId: 2, notifying ddd@f.com
     Enter Command( hold [numseats email], reserve [holdId email], available, print, end ):
     print

     XXXXssssss
     HHHHHHHHHH
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss
     ssssssssss

     Enter Command( hold [numseats email], reserve [holdId email], available, print, end ):
     Attempting to release hold: 2
     Released hold: 2, notifying ddd@f.com

## Contributors
Shailesh Kurdekar (skurdekar@gmail.com)


## License
Open Source Free to use and distribute without warranty or liability from the original author.
