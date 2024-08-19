package p1;

public class CabTrip {
    private String cabId;
    private String numberPlate;
    private String cabType;
    private String driverName;
    private Boolean ongoingTrip;
    private String pickupLocation;
    private String destination;
    private Integer passengerCount;

    // Constructor
    public CabTrip(String cabId, String numberPlate, String cabType, String driverName, Boolean ongoingTrip, String pickupLocation, String destination, Integer passengerCount) {
        this.cabId = cabId;
        this.numberPlate = numberPlate;
        this.cabType = cabType;
        this.driverName = driverName;
        this.ongoingTrip = ongoingTrip;
        this.pickupLocation = pickupLocation;
        this.destination = destination;
        this.passengerCount = passengerCount;
    }

    // Getters and setters for all fields
    public String getCabId() { return cabId; }
    public void setCabId(String cabId) { this.cabId = cabId; }

    public String getNumberPlate() { return numberPlate; }
    public void setNumberPlate(String numberPlate) { this.numberPlate = numberPlate; }

    public String getCabType() { return cabType; }
    public void setCabType(String cabType) { this.cabType = cabType; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public Boolean getOngoingTrip() { return ongoingTrip; }
    public void setOngoingTrip(Boolean ongoingTrip) { this.ongoingTrip = ongoingTrip; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public Integer getPassengerCount() { return passengerCount; }
    public void setPassengerCount(Integer passengerCount) { this.passengerCount = passengerCount; }
}