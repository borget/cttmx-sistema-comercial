package mx.skyguardian.controltower.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import mx.skyguardian.controltower.bean.AbstractWialonEntity;
import mx.skyguardian.controltower.bean.GeoPosition;
import mx.skyguardian.controltower.bean.LastMsgReport;
import mx.skyguardian.controltower.bean.LastMsgReport251;
import mx.skyguardian.controltower.bean.LastMsgReport252;
import mx.skyguardian.controltower.bean.LastMsgReport253;
import mx.skyguardian.controltower.bean.LastMsgReport254;
import mx.skyguardian.controltower.bean.POI;
import mx.skyguardian.controltower.bean.POIRow;
import mx.skyguardian.controltower.bean.POIRows;
import mx.skyguardian.controltower.http.remoting.AbstractUser;
import mx.skyguardian.controltower.http.remoting.IWialonHTTPRequestExecutor;
import mx.skyguardian.controltower.http.remoting.WialonSession;
import mx.skyguardian.controltower.util.AppUtils;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SimpleJSONDeserializer extends AbsctractJSONDeserializer {
	private IWialonHTTPRequestExecutor httpReqExecutor = null;
	
	@Resource(name = "appProperties")
	private Properties appProperties;
	
	private static Logger log = Logger.getLogger(SimpleJSONDeserializer.class);

	@Override
	public AbstractWialonEntity getGEOPosition(JSONObject jsonObj, String unitId, AbstractUser user) throws JSONException {
		AbstractWialonEntity geoPosition = null;
		
		if (jsonObj != null) {
			if (!jsonObj.isNull("pos")) {
				JSONObject geoPos = jsonObj.getJSONObject("pos");
				geoPosition = new GeoPosition(
						Long.parseLong((geoPos.isNull("t"))?"21600":geoPos.get("t").toString()),
						Double.parseDouble((geoPos.isNull("y"))?"0.0":geoPos.get("y").toString()),
						Double.parseDouble((geoPos.isNull("x"))?"0.0":geoPos.get("x").toString()),
						Double.parseDouble((geoPos.isNull("z"))?"0":geoPos.get("z").toString()),
						Integer.parseInt((geoPos.isNull("s"))?"0":geoPos.get("s").toString()),
						Integer.parseInt((geoPos.isNull("c"))?"0":geoPos.get("c").toString()),
						Integer.parseInt((geoPos.isNull("sc"))?"0":geoPos.get("sc").toString()));
				
				((GeoPosition)geoPosition).setDateTime(AppUtils.getFormattedDate(appProperties.getProperty(
						"mx.skyguardian.controltower.geoposition.datetime.format"),((GeoPosition)geoPosition).getTimeUTC()));
				
				try {
					
					Map<String, String> properties = new HashMap<String, String>();
					properties.put("longitud", (((GeoPosition) geoPosition).getLongitud()).toString());
					properties.put("latitud", (((GeoPosition) geoPosition).getLatitud()).toString());
					properties.put("unitId", unitId);
					properties.put("user", ((WialonSession)user).getUserName());
					properties.put("password",((WialonSession)user).getPassword());
					
					String geoPosUrl = AppUtils.getURL(
							appProperties.getProperty("mx.skyguardian.controltower.geoposition.url"), properties);
					
					JSONObject geoPosDesc = httpReqExecutor.getHTTPRequest(geoPosUrl);
			
					if(geoPosDesc.length()!=0){
						String geoPosDescString = (geoPosDesc.isNull("jsonArray"))?"No description":geoPosDesc.get("jsonArray").toString();
						//byte[] latin1 = geoPosDescString.getBytes("ISO-8859-1");
						//((GeoPosition)geoPosition).setPositionDesc(new String (latin1, "UTF-8"));
						((GeoPosition)geoPosition).setPositionDesc(geoPosDescString);
					}

				} catch (IOException e) {
					log.error("There was an error retriving the Geo Position Description.");
					e.printStackTrace();
				} 
				return geoPosition;
			}
			
		}
		
		return new GeoPosition();
	}
	
	@Override
	public AbstractWialonEntity getLastMsgReport(JSONObject jsonObj) throws JSONException {
		AbstractWialonEntity report = new LastMsgReport();
		if (jsonObj != null) {
			if (!jsonObj.isNull("lmsg")) {
				JSONObject lmsg = jsonObj.getJSONObject("lmsg");
				if (lmsg != null) {
					if (!lmsg.isNull("p")) {
						JSONObject jsonReport = lmsg.getJSONObject("p");
							processPosition(jsonReport, report);
					}
				}
				
			} else if (!jsonObj.isNull("p")) {
				JSONObject jsonReport = jsonObj.getJSONObject("p");
				processPosition(jsonReport, report);
			}
		}
		return report;
	}
	
	private void processPosition(JSONObject jsonReport, AbstractWialonEntity report){
		int reportID = Integer.parseInt((jsonReport.isNull("report_id"))?"0":jsonReport.get("report_id").toString()); 
		switch (reportID) {
		
			case 2:
				setReport2(report, jsonReport);
				break;
				
			case 251:
				AbstractWialonEntity report251 = new LastMsgReport251();
				setReport2(report251, jsonReport);
				setReport251(report251, jsonReport);
				report = report251;
				break;
				
			case 252:
				AbstractWialonEntity report252 = new LastMsgReport252();
				setReport2(report252, jsonReport);
				setReport252(report252, jsonReport);
				report = report252;
				break;
				
			case 253:
				AbstractWialonEntity report253 = new LastMsgReport253();
				setReport2(report253, jsonReport);
				setReport253(report253, jsonReport);
				report = report253;
				break;
				
			case 254:
				AbstractWialonEntity report254 = new LastMsgReport254();
				setReport2(report254, jsonReport);
				setReport254(report254, jsonReport);
				report = report254;
				break;
			default:
				setReport2(report, jsonReport);
				break;
		}
	}
	
	@Override
	public AbstractWialonEntity getPOI(JSONObject jsonObj, WialonSession session) throws JSONException, IOException {

		POI poi = null;
		
		if (!jsonObj.isNull("jsonArray")){
			String json = jsonObj.get("jsonArray").toString();
			
			JSONArray jsonArray = new JSONArray(json);
			
			JSONObject item0 = jsonArray.getJSONObject(1);
			
			JSONObject reportResult = item0.getJSONObject("reportResult");
			
			JSONArray tables = reportResult.getJSONArray("tables");
			
			JSONObject tables0 = tables.getJSONObject(0);
			
			String rows = String.valueOf(tables0.get("rows"));
			
			log.debug("POIRows.length():" + rows);
			
			Map<String, String> properties = new HashMap<String, String>();
			properties.put("rangeTo", rows);
			properties.put("sid", session.getEid());
			
			String resultUrl = AppUtils.getURL(appProperties.getProperty("mx.skyguardian.controltower.poi.result.rows.url"), properties);

			log.debug("SimpleJSONDeserializer.getPOI()-resultUrl="+resultUrl);
			
			JSONObject resultRowsJSON = httpReqExecutor.getHTTPRequest(resultUrl);

			if (!resultRowsJSON.isNull("jsonArray")){
				String result = resultRowsJSON.get("jsonArray").toString();
				JSONArray resultArray = new JSONArray(result);
				
				poi = new POI();
				POIRows poiRows = new POIRows();
				List<POIRow> listPois = new ArrayList<POIRow>();				
				for (int i = 0; i<resultArray.length();i++) {
					JSONObject item = resultArray.getJSONObject(i);
					POIRow poiRow = new POIRow();
					
					Short number = Short.parseShort((item.isNull("n"))?"0":item.get("n").toString());
					poiRow.setNumber(++number);

					JSONArray c = item.getJSONArray("c");
					
					if (c.length()>0) {
						JSONObject timeFrom = findObject(c, "Time");
						String timeFromValue = (timeFrom.isNull("v"))?"0":timeFrom.get("v").toString();
						poiRow.setDateTime(AppUtils.getDatetime(timeFromValue));

						String geocercaValue = getGeocercaDescription(c);
						poiRow.setGeocerca(geocercaValue!=null ? geocercaValue : "Geocerca not found.");
					}
					listPois.add(poiRow);
				}
				poiRows.setRow(listPois);
				poi.setRows(poiRows);
			}	
			
		}
		return poi;
	}
	
	private String getGeocercaDescription(JSONArray jArray) {
		String geocercaValue = null;
		Object geocerca = jArray.get(2);
		
		if (geocerca != null && geocerca instanceof String) {
			geocercaValue = (String)geocerca;
		}
		
		return geocercaValue;
	}
	
	private JSONObject findObject(JSONArray jsonArray, String typeOfObj) {
		if (typeOfObj.equals("Time")) {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.optJSONObject(i);
				if (obj != null) {
					if (!obj.isNull("t") && !obj.isNull("v")) {
						return obj;
					} 
				}
			}
		} else if (typeOfObj.equals("PDI")) {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.optJSONObject(i);
				if (obj != null) {
					if (!obj.isNull("t") && obj.isNull("v")) {
						return obj;
					} 
				}
			}
		}
		
		return new JSONObject();
	}

	private void setReport2(AbstractWialonEntity report, JSONObject jsonReport) {
		((LastMsgReport) report).setReportId(Integer.parseInt((jsonReport.isNull("report_id"))?"0":jsonReport.get("report_id").toString()));
		((LastMsgReport) report).setOdometer(Double.parseDouble((jsonReport.isNull("odometer"))?"0.0":jsonReport.get("odometer").toString()));
		((LastMsgReport) report).setHdop(Double.parseDouble((jsonReport.isNull("hdop"))?"0":jsonReport.get("hdop").toString()));
		((LastMsgReport) report).setAdc1(Double.parseDouble((jsonReport.isNull("adc1"))?"0":jsonReport.get("adc1").toString()));
		((LastMsgReport) report).setTemp1(Double.parseDouble((jsonReport.isNull("temp1"))?"0":jsonReport.get("temp1").toString()));
		((LastMsgReport) report).setTemp2(Double.parseDouble((jsonReport.isNull("temp2"))?"0":jsonReport.get("temp2").toString()));
		((LastMsgReport) report).setDl(Integer.parseInt((jsonReport.isNull("DL"))?"0":jsonReport.get("DL").toString()));
		((LastMsgReport) report).setTw(Integer.parseInt((jsonReport.isNull("TW"))?"0":jsonReport.get("TW").toString()));
		((LastMsgReport) report).setMt(Integer.parseInt((jsonReport.isNull("MT"))?"0":jsonReport.get("MT").toString()));
		((LastMsgReport) report).setIp(Integer.parseInt((jsonReport.isNull("IP"))?"0":jsonReport.get("IP").toString()));
		((LastMsgReport) report).setPs(Integer.parseInt((jsonReport.isNull("PS"))?"0":jsonReport.get("PS").toString()));
		((LastMsgReport) report).setSs(Integer.parseInt((jsonReport.isNull("SS"))?"0":jsonReport.get("SS").toString()));
		((LastMsgReport) report).setHa(Integer.parseInt((jsonReport.isNull("HA"))?"0":jsonReport.get("HA").toString()));
		((LastMsgReport) report).setHb(Integer.parseInt((jsonReport.isNull("HB"))?"0":jsonReport.get("HB").toString()));
		((LastMsgReport) report).setHc(Integer.parseInt((jsonReport.isNull("HC"))?"0":jsonReport.get("HC").toString()));
		((LastMsgReport) report).setJd(Integer.parseInt((jsonReport.isNull("JD"))?"0":jsonReport.get("JD").toString()));
		((LastMsgReport) report).setBl(Integer.parseInt((jsonReport.isNull("BL"))?"0":jsonReport.get("BL").toString()));
		((LastMsgReport) report).setEg(Integer.parseInt((jsonReport.isNull("EG"))?"0":jsonReport.get("EG").toString()));
		((LastMsgReport) report).setMv(Integer.parseInt((jsonReport.isNull("MV"))?"0":jsonReport.get("MV").toString()));
		((LastMsgReport) report).setRd(Integer.parseInt((jsonReport.isNull("RD"))?"0":jsonReport.get("RD").toString()));
		((LastMsgReport) report).setOp(Integer.parseInt((jsonReport.isNull("OP"))?"0":jsonReport.get("OP").toString()));
		((LastMsgReport) report).setIn0(Integer.parseInt((jsonReport.isNull("IN0"))?"0":jsonReport.get("IN0").toString()));
		((LastMsgReport) report).setIn1(Integer.parseInt((jsonReport.isNull("IN1"))?"0":jsonReport.get("IN1").toString()));
		((LastMsgReport) report).setOd(Integer.parseInt((jsonReport.isNull("OD"))?"0":jsonReport.get("OD").toString()));
	}
	
	
	private void setReport251(AbstractWialonEntity report, JSONObject jsonReport) {
		((LastMsgReport251) report).setEngineRpm(Integer.parseInt((jsonReport.isNull("engine_rpm"))?"0":jsonReport.get("engine_rpm").toString()));
		((LastMsgReport251) report).setJ1939Speed(Integer.parseInt((jsonReport.isNull("j1939_speed"))?"0":jsonReport.get("j1939_speed").toString()));
		((LastMsgReport251) report).setFuelCons(Integer.parseInt((jsonReport.isNull("fuel_cons"))?"0":jsonReport.get("fuel_cons").toString()));
		((LastMsgReport251) report).setJ1939FuelLevel(Integer.parseInt((jsonReport.isNull("j1939_fuel_level"))?"0":jsonReport.get("j1939_fuel_level").toString()));
		((LastMsgReport251) report).setAxle1(Integer.parseInt((jsonReport.isNull("axle1"))?"0":jsonReport.get("axle1").toString()));
		((LastMsgReport251) report).setAxle2(Integer.parseInt((jsonReport.isNull("axle2"))?"0":jsonReport.get("axle2").toString()));
		((LastMsgReport251) report).setAxle3(Integer.parseInt((jsonReport.isNull("axle3"))?"0":jsonReport.get("axle3").toString()));
		((LastMsgReport251) report).setAxle4(Integer.parseInt((jsonReport.isNull("axle4"))?"0":jsonReport.get("axle4").toString()));
		((LastMsgReport251) report).setEngBoostPressure(Integer.parseInt((jsonReport.isNull("eng_boost_pressure"))?"0":jsonReport.get("eng_boost_pressure").toString()));
		((LastMsgReport251) report).setCoolantTemp(Integer.parseInt((jsonReport.isNull("coolant_temp"))?"0":jsonReport.get("coolant_temp").toString()));
		((LastMsgReport251) report).setAccelPos(Integer.parseInt((jsonReport.isNull("accel_pos"))?"0":jsonReport.get("accel_pos").toString()));
		((LastMsgReport251) report).setBrakePos(Integer.parseInt((jsonReport.isNull("brake_pos"))?"0":jsonReport.get("brake_pos").toString()));
		((LastMsgReport251) report).setPtAirPressure(Integer.parseInt((jsonReport.isNull("pt_air_pressure"))?"0":jsonReport.get("pt_air_pressure").toString()));
		((LastMsgReport251) report).setBrakePressure1(Integer.parseInt((jsonReport.isNull("brake_pressure1"))?"0":jsonReport.get("brake_pressure1").toString()));
		((LastMsgReport251) report).setBrakePressure2(Integer.parseInt((jsonReport.isNull("brake_pressure2"))?"0":jsonReport.get("brake_pressure2").toString()));
	}
	
	private void setReport252(AbstractWialonEntity report, JSONObject jsonReport) {
		((LastMsgReport252) report).setJ1939Odo(Integer.parseInt((jsonReport.isNull("j1939_odo"))?"0":jsonReport.get("j1939_odo").toString()));
		((LastMsgReport252) report).setTotalFuel(Integer.parseInt((jsonReport.isNull("total_fuel"))?"0":jsonReport.get("total_fuel").toString()));
		((LastMsgReport252) report).setBrakeTimes(Integer.parseInt((jsonReport.isNull("brake_times"))?"0":jsonReport.get("brake_times").toString()));
		((LastMsgReport252) report).setClutchTimes(Integer.parseInt((jsonReport.isNull("clutch_times"))?"0":jsonReport.get("clutch_times").toString()));
		((LastMsgReport252) report).setCruiseTime(Integer.parseInt((jsonReport.isNull("cruise_time"))?"0":jsonReport.get("cruise_time").toString()));
		((LastMsgReport252) report).setPtoTime(Integer.parseInt((jsonReport.isNull("pto_time"))?"0":jsonReport.get("pto_time").toString()));
	}
	
	private void setReport253(AbstractWialonEntity report, JSONObject jsonReport) {
		((LastMsgReport253) report).setDriversState(Integer.parseInt((jsonReport.isNull("drivers_state"))?"0":jsonReport.get("drivers_state").toString()));
		((LastMsgReport253) report).setDriver1Card(Integer.parseInt((jsonReport.isNull("driver1_card"))?"0":jsonReport.get("driver1_card").toString()));
		((LastMsgReport253) report).setDriver2Card(Integer.parseInt((jsonReport.isNull("driver2_card"))?"0":jsonReport.get("driver2_card").toString()));
		((LastMsgReport253) report).setTachographStatus(Integer.parseInt((jsonReport.isNull("tachograph_status"))?"0":jsonReport.get("tachograph_status").toString()));
		((LastMsgReport253) report).setTachographSpeed(Integer.parseInt((jsonReport.isNull("tachograph_speed"))?"0":jsonReport.get("tachograph_speed").toString()));
		((LastMsgReport253) report).setTachoExtInfo(Integer.parseInt((jsonReport.isNull("tacho_ext_info"))?"0":jsonReport.get("tacho_ext_info").toString()));
		
		((LastMsgReport253) report).setIn2(Integer.parseInt((jsonReport.isNull("IN2"))?"0":jsonReport.get("IN2").toString()));
		((LastMsgReport253) report).setGq(Integer.parseInt((jsonReport.isNull("GQ"))?"0":jsonReport.get("GQ").toString()));
		((LastMsgReport253) report).setGs(Integer.parseInt((jsonReport.isNull("GS"))?"0":jsonReport.get("GS").toString()));
	}
	
	private void setReport254(AbstractWialonEntity report, JSONObject jsonReport) {
		((LastMsgReport254) report).setVehicleId(Integer.parseInt((jsonReport.isNull("vehicle_id"))?"0":jsonReport.get("vehicle_id").toString()));
		((LastMsgReport254) report).setServiceDist(Integer.parseInt((jsonReport.isNull("service_dist"))?"0":jsonReport.get("service_dist").toString()));
		((LastMsgReport254) report).setEngHours(Integer.parseInt((jsonReport.isNull("eng_hours"))?"0":jsonReport.get("eng_hours").toString()));
		((LastMsgReport254) report).setEngStarter(Integer.parseInt((jsonReport.isNull("eng_starter"))?"0":jsonReport.get("eng_starter").toString()));
		((LastMsgReport254) report).setAbsActive(Integer.parseInt((jsonReport.isNull("abs_active"))?"0":jsonReport.get("abs_active").toString()));
	}

	public void setHttpReqExecutor(IWialonHTTPRequestExecutor httpReqExecutor) {
		this.httpReqExecutor = httpReqExecutor;
	}

	public void setAppProperties(Properties appProperties) {
		this.appProperties = appProperties;
	}

}
