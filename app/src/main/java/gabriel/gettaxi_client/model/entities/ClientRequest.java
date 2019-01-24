package gabriel.gettaxi_client.model.entities;


import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;
import java.util.Random;

public class ClientRequest implements Serializable {

    private String clientName;
    private String driverID = "0";
    private String phoneNumber;
    private String email;
    private ClientRequestStatus ClientRequestStatus;
    private double sourceLongitude;
    private double sourceLatitude;
    private double destinationLongitude;
    private double destinationLatitude;
    private String sourceAddress;
    private String destinationAddress;
    private Date departureTime;
    private Date arrivalTime;

    // Use to print to screen informations
    private String travelTime;

    @Exclude
    private String travelPrice;
    @Exclude
    private double travelDistance;

    //region ***** CONSTRUCTORS *****

    public ClientRequest()
    {
        String[] firstNames = {"Gabriel", "Yaacov", "Jacques"};
        Random r = new Random(System.currentTimeMillis());

        clientName = firstNames[r.nextInt(firstNames.length)];
        phoneNumber = "053" + r.nextInt(1_000_000);
        email = clientName + "@gmail.com";
        ClientRequestStatus = ClientRequestStatus.AWAITING;
        sourceLongitude = 31.765028;
        sourceLatitude = 35.191166;
        destinationLongitude = 31.761981;
        destinationLatitude = 35.190829;
        departureTime = new Date(2018,11,1,16,0);
        arrivalTime = new Date(2018,11,1,17,30);
    }

    public ClientRequest(String clientName, String phoneNumber, String email, ClientRequestStatus ClientRequestStatus,
                         double sourceLongitude, double sourceLatitude,
                         double destinationLongitude, double destinationLatitude,
                         Time departureTime, Time arrivalTime) {
        this.clientName = clientName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.ClientRequestStatus = ClientRequestStatus;
        this.sourceLongitude = sourceLongitude;
        this.sourceLatitude = sourceLatitude;
        this.destinationLongitude = destinationLongitude;
        this.destinationLatitude = destinationLatitude;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    //endregion

    //region ***** GETTERS/SETTERS *****

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getDriverID() {
        return driverID;
    }

    public void setDriverID(String driverID) {
        this.driverID = driverID;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ClientRequestStatus getClientRequestStatus() {
        return ClientRequestStatus;
    }

    public void setClientRequestStatus(ClientRequestStatus ClientRequestStatus) {
        this.ClientRequestStatus = ClientRequestStatus;
    }

    public double getSourceLongitude() {
        return sourceLongitude;
    }

    public void setSourceLongitude(double sourceLongitude) {
        this.sourceLongitude = sourceLongitude;
    }

    public double getSourceLatitude() {
        return sourceLatitude;
    }

    public void setSourceLatitude(double sourceLatitude) {
        this.sourceLatitude = sourceLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getTravelTime() {
        return travelTime;
    }

    public void setTravelTime(String travelTime) {
        this.travelTime = travelTime;
    }

    public String getTravelPrice() {
        return travelPrice;
    }

    public void setTravelPrice(String travelPrice) {
        this.travelPrice = travelPrice;
    }

    public double getTravelDistance() {
        return travelDistance;
    }

    public void setTravelDistance(double travelDistance) {
        this.travelDistance = travelDistance;
    }

    //endregion

    //region ***** ADMINISTRATION *****

    @Override
    public String toString() {
        return  clientName + '\n' +
                "Tel. " + phoneNumber + '\n' +
                "Email: " + email + '\n' +
                "ClientRequest Status: " + ClientRequestStatus;
    }

    //endregion
}