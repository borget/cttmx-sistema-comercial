package mx.skyguardian.controltower.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "rows", namespace = "http://com.cttmx.commercial")
public class POIRows extends AbstractWialonEntity {
	
	private List<POIRow> row = new ArrayList<POIRow>();

	@XmlElement
	public List<POIRow> getRow() {
		return row;
	}

	public void setRow(List<POIRow> row) {
		this.row = row;
	}

}
