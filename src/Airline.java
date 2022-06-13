import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Airline {

//     Flight[] calender; // this will contain all the flights that are to takeoff within 30 days
     User user = new User();
     Admin admin= new Admin();

     Connection connection;
     Statement statement;
     Airline() throws SQLException{
          connection = DriverManager.getConnection("jdbc:sqlite:AirlineDatabase.db");
          statement = connection.createStatement();

          admin.setConnection(connection,statement);
          user.setConnection(connection,statement);

          //check which active till is < dateNow and remove it
          statement.execute("DELETE FROM Flights WHERE activeTill<date('now')");


     }
     void login(String username, String password){
//          String query = "SELECT username,password WHERE username=''"
          try {
               statement.execute("CREATE TABLE IF NOT EXISTS Users(username TEXT UNIQUE,password TEXT,fullName TEXT,cnic TEXT,contact TEXT,address TEXT,email TEXT,gender TEXT,dateOfBirth TEXT,whenAccountCreated TEXT,userType TEXT)");
               statement.execute("SELECT username,userType,fullName FROM Users WHERE username='" + username + "' AND password='"+password+"'");
               ResultSet resultSet = statement.getResultSet();
               if (resultSet.next()){
                   //call users sign method.. in that sign in take the attributes from database and reassign the user object
                    // according to the in database
//                    if userType == 'regular'
                    String userType = resultSet.getString("userType");
                    if (userType.equals("regular")){
                         user.login(resultSet.getString("username"));
                         System.out.println("WELCOME "+resultSet.getString("fullName"));
                    }
                    else if(userType.equals("admin")){
                         admin.login(resultSet.getString("username"));
                         System.out.println("WELCOME "+resultSet.getString("fullName"));
                    }
               }else {
                    System.out.println("User Not Found, make sure you've entered right ID and password");
               }

          } catch (SQLException queryFailed) {
               System.out.println("Something went wrong: "+ queryFailed.getMessage());
          }
     }

     void signUp(String[] userInfo) throws  SQLException{
          StringBuilder insertInfoSql = new StringBuilder("INSERT INTO Users(fullName,username,password,cnic,contact,address,gender,dateOfBirth,email,whenAccountCreated,userType) VALUES('");

          for(int i=0;i<userInfo.length-1;i++){
               insertInfoSql.append(userInfo[i]).append("','");
          }

          insertInfoSql.append(userInfo[userInfo.length-1]).append("',datetime('now'),'regular');");
          statement.execute(insertInfoSql.toString());
          login(userInfo[1],userInfo[2]);
     }

     ResultSet getFlights(String date, String to, String from,int numberOfPassengers,char seatType) throws SQLException {
          /*
          * dont include flights in the resultset if they are in cancelledFlights Table for the date user has provided
          * */

          String sql ="SELECT * From (SELECT * " +
                  "                  FROM Flights " +
                  "                  WHERE NOT (flightId IN " +
                  "                  (SELECT flightId " +
                  "                  FROM CancelledFlights " +
                  "                  WHERE cancelledDate = '"+date+"'))) " +
                  "  WHERE (( activeTill>='"+date+"' OR activeTill IS NUll)) " +
                  "AND travelFrom='"+from+"' AND travelTo='"+to+"';";
          statement.execute(sql); // jab inactiveSince ka attribute add hoga tou mujhe iska date check rkhna parega k
          ResultSet flightsResultSet = statement.getResultSet();

          ArrayList<int[]> flightsData = new ArrayList();
          while(flightsResultSet.next()){
               int[] flightdata = new int[2];
               flightdata[0] = flightsResultSet.getInt("flightId");  // error.. no such column 'flightId'
               flightdata[1] = flightsResultSet.getInt("numberOf"+seatType+"catSeats");
               flightsData.add(flightdata);
          }
          if(flightsData.size()==0) return null;
          //this is the case when resultset has some data....   flightsDate.size()==0 means no data
          ArrayList<Integer> flightIds = new ArrayList<>();
          for (int[] flightData : flightsData) {
               int flightId = flightData[0];
               int numberOfEorBseatTypes = flightData[1];
               statement.execute("SELECT count(*) AS seatCount FROM Bookings WHERE flightId ='" + flightId + "' AND seatType='" + seatType + "' AND bookedForDate='" + date + "';");
               int seatCount = statement.getResultSet().getInt("seatCount");
               if (seatCount + numberOfPassengers <= numberOfEorBseatTypes) {
                    flightIds.add(flightId);
               }
          }
          StringBuilder query = new StringBuilder("flightId=" + flightIds.get(0));
          for (int i=1;i<flightIds.size();i++){
               query.append(" OR flightId=").append(flightIds.get(i));
          }

          statement.execute("SELECT * FROM Flights WHERE "+query+";");
          return  statement.getResultSet();
          //case: no flights exist? if count inside while loop doesn't get incremented return null.. then handle it in driver
     }
     ResultSet getFlights(String from,String to) throws SQLException{
          //this method gets called by admin for route cancellation

          // this method is for admin where he gets to see the flights that are not scheduled to be deleted.
          statement.execute("SELECT flightId,travelTo,travelFrom,takeOffTime FROM Flights WHERE travelFrom='"+from+"' AND travelTo='"+to+"' AND ActiveTill IS NULL;");
          return statement.getResultSet();
     }
     ResultSet getFlights(String from,String destination, String date) throws  SQLException{
          /*
          @ this method gets flights when admin wants to cancel a flight for a date
          * flight id should not be present in the cancellation for the given date
           */
          String sql = "SELECT * From (SELECT * " +
                  "FROM Flights " +
                  "WHERE  NOT (flightId IN " +
                  "(SELECT flightId " +
                  "FROM CancelledFlights " +
                  "WHERE cancelledDate = '"+date+"'))) WHERE ( activeTill>='"+date+"' OR activeTill IS NUll) AND travelFrom='"+from+"' AND travelTo='"+destination+"';";
          statement.execute(sql);
          // got the ids and make sure the ids not already exists in cancellation table
          //get only those flightIds that are not present in cancellation table for the date provided
          return statement.getResultSet();
     }
     ArrayList<String> getFroms() throws SQLException{
          ArrayList<String> froms = new ArrayList();
          statement.execute("SELECT DISTINCT travelFrom FROM FLights;");
          ResultSet resultSet = statement.getResultSet();
          while (resultSet.next()) {
               froms.add(resultSet.getString("travelFrom"));
          }
          return froms;
     }
     ArrayList<String> getDestinations(String from) throws SQLException{
          ArrayList<String> destinations = new ArrayList();
          statement.execute("SELECT DISTINCT travelTo FROM flights WHERE travelFrom='"+from+"';");
          ResultSet resultSet = statement.getResultSet();
          while (resultSet.next()) {
               destinations.add(resultSet.getString("travelTo"));
          }
          return destinations;
     }
     boolean usernameAlreadyExists(String username) throws SQLException{
          /**
           * method returns true if username is not present in the database table
           */

          statement.execute("SELECT username FROM Users WHERE username='"+username+"';");
          ResultSet resultSet = statement.getResultSet();

          return resultSet.next();
     }
}


