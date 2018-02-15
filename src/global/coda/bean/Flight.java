package global.coda.bean;

public class Flight {
private String flightNo;
private String airlineName,sourceName,destinationName;
public String getFlightNo() {
	return flightNo;
}
public void setFlightNo(String flno) {
	this.flightNo = flno;
}
public String getAirlineName() {
	return airlineName;
}
public void setAirlineName(String airlineName) {
	this.airlineName = airlineName;
}
public String getSourceName() {
	return sourceName;
}
public void setSourceName(String sourceName) {
	this.sourceName = sourceName;
}
public String getDestinationName() {
	return destinationName;
}
public void setDestinationName(String destinationName) {
	this.destinationName = destinationName;
}

public void setFlight(String flno, String airline, String boarding, String dest) {
	setFlightNo(flno);
	setAirlineName(airline);
	setSourceName(boarding);
	setDestinationName(dest);
	
}
}
