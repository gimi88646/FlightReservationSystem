import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

//this class is to be extended by User class and Admin class
public abstract class Person {
    boolean isSignedIn=false;
    String name;
    String username;
    protected  Connection connection;
    protected  Statement statement;


//    {
//        try {
//             connection = DriverManager.getConnection("jdbc:sqlite:AirlineDatabase.db");
//             statement = connection.createStatement();
//
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        }
//    }



    Person(String username){
        this.username = username;

    }

    //should logout be a method?
    //if found it reassign the attributes of User Object and provide extra functionality
    // extra functionality  = history , view bookings, cancel bookings.


    //user and admin shares these common methods..kya mujhe inki implementation yahan krni chahiye yaa individually har class me alag se implement karu
    //apparently iske andar bhi wohii kam ho rha he... signedIn or username ko reassign krna

     void logOut(){
        isSignedIn=false;
        username="Anonymous";
     }
     void login(String username){
        this.username=username;
        isSignedIn=true;
     }
     boolean getSignedInStatus(){
        return isSignedIn;
     }

      void setConnection(Connection connection,Statement statement){
        this.connection=connection;
        this.statement=statement;
     }

}
