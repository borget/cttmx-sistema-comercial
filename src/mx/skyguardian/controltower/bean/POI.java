package mx.skyguardian.controltower.bean;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="poi", namespace="http://com.cttmx.commercial")
public class POI extends AbstractWialonEntity {
	private Long unitID = 0L;
	private POIRows rows = new POIRows();
	
	public Long getUnitID() {
		return unitID;
	}
	public void setUnitID(Long unitID) {
		this.unitID = unitID;
	}
	
	@XmlElement
	public POIRows getRows() {
		return rows;
	}
	public void setRows(POIRows rows) {
		this.rows = rows;
	}

}
