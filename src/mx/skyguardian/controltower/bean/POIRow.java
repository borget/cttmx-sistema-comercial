package mx.skyguardian.controltower.bean;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "row", namespace = "http://com.cttmx.commercial")
public class POIRow extends AbstractWialonEntity {
	
	Short number = 0;
	String dateTime = "01/01/1970 00:00:00";
	String geocerca = "Geocerca not found.";

	public POIRow() {
	
	}
		
	public POIRow(Short number, String dateTime, String geocerca) {
		super();
		this.number = number;
		this.dateTime = dateTime;
		this.geocerca = geocerca;
	}
		
	public Short getNumber() {
		return number;
	}
	public void setNumber(Short number) {
		this.number = number;
	}
	
	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	public String getGeocerca() {
		return geocerca;
	}

	public void setGeocerca(String geocerca) {
		this.geocerca = geocerca;
	}
}
