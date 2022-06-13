import javax.xml.transform.Result;
import java.awt.image.RescaleOp;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Admin extends Person {

    Admin(){super("Anonymous");}
    Admin(String username){
        super(username);
    }

    // implementation of methods that are displayed in driver class
    //this method takes to-from time.. number of passengers for a carrier as parameters

    public void addRoute(ArrayList<String> flightinfo) throws Exception {
        String query = "'" + flightinfo.get(0) + "'";
        for (int i = 1; i <= flightinfo.size() - 1; i++) {
            String infopiece = "," + "'" + flightinfo.get(i) + "'";
            query += infopiece;
        }

        statement.execute(
                "INSERT INTO Flights(flightId,travelTo,travelFrom,numberOfBcatSeats,numberOfEcatSeats,takeOffTime,priceE,priceB) VALUES("
                        + query + ")");
    }

    public String cancelRoute(String flightId) throws SQLException{

        String mostRecentlyBookedDate=null;
        statement.execute("SELECT bookedForDate FROM Bookings WHERE flightId = '"+flightId+"' AND bookedForDate>=date('now') ORDER BY bookedForDate DESC LIMIT 1");
        ResultSet resultSet = statement.getResultSet();

        if(resultSet.next()){
            mostRecentlyBookedDate = resultSet.getString("bookedForDate");
            statement.execute("UPDATE Flights SET activeTill = '"+mostRecentlyBookedDate+"' WHERE flightId = '"+flightId+"';");
        }else {
            statement.execute("DELETE FROM Flights WHERE FlightId='"+flightId+"';");
        }
        return mostRecentlyBookedDate;
    }


    // admin wants to cancel a flight for a date..
    public void cancelFlight( String flightId,String date, String message) throws SQLException{
        java.util.Date dt = new java.util.Date();

        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String currentDateTime = sdf.format(dt);
        String notification = "Your Booking for Flight "+flightId+ " has been cancelled! " + message;

        //flightId and date of cancellation gets added to CancelledFlights table..
        // advantage -> the user wont be able to see the flightId happens to exists in the cancellations table
        statement.execute("INSERT INTO CancelledFlights(flightId,cancelledDate,cancelledBy,whenCancelled) VALUES("+
                flightId+",'" +date+ "','"+username+
                "','"+currentDateTime+"')");
        //get the users who have booked flight-being-cancelled on date admin has given.
        statement.execute("SELECT bookedBy FROM Bookings WHERE bookedForDate = '"+date+"' AND FlightId ='"+flightId+"' ");
        ResultSet resultSet = statement.getResultSet();
        ArrayList<String> usernames =  new ArrayList();
        while(resultSet.next()){
            usernames.add(resultSet.getString("bookedBy"));
        }

        // send notification to users who had booked the cancelled flight
        for(String username: usernames){
            statement.execute("INSERT INTO Notifications(username,notification,whenNotified) VALUES('" +
                    username+"','"+notification+
                    "','"+currentDateTime+"')");
        }
        //rows  bookings table gets deleted. where conditions get true
        statement.execute("DELETE FROM Bookings WHERE flightId ="+flightId+" AND bookedForDate = '"+date+"';");


    }
}
