package mx.skyguardian.controltower.http.remoting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;

import mx.skyguardian.controltower.bean.AbstractWialonEntity;
import mx.skyguardian.controltower.bean.GeoPosition;
import mx.skyguardian.controltower.bean.POI;
import mx.skyguardian.controltower.bean.Unit;
import mx.skyguardian.controltower.bean.Units;
import mx.skyguardian.controltower.bean.Vehicle;
import mx.skyguardian.controltower.bean.VehicleHistory;
import mx.skyguardian.controltower.bean.VehicleHistoryItem;
import mx.skyguardian.controltower.bean.Vehicles;
import mx.skyguardian.controltower.exception.WialonAccessDeniedException;
import mx.skyguardian.controltower.exception.WialonInternalServerError;
import mx.skyguardian.controltower.json.AbsctractJSONDeserializer;
import mx.skyguardian.controltower.security.JasyptEncryptor;
import mx.skyguardian.controltower.util.AppUtils;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class SkyGuardianControlTowerManager implements IControlTowerManager {
	
	private static Logger log = Logger.getLogger(SkyGuardianControlTowerManager.class);
	
	private IWialonHTTPRequestExecutor httpReqExecutor = null;

	
	private AbsctractJSONDeserializer jsonDeserializer = null;
	
	@Resource(name = "appProperties")
	private Properties appProperties;
	
	public AbstractWialonEntity getUnit(String userName, String password, String unitId) throws WialonInternalServerError, IOException {
		
		WialonSession wialonSession = new WialonSession();
		wialonSession.setUserName(userName);
		wialonSession.setPassword(JasyptEncryptor.decryptPBEText(password));
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("user", wialonSession.getUserName());
		properties.put("password", wialonSession.getPassword());
		
		String loginUrl = AppUtils.getURL(
				appProperties.getProperty("mx.skyguardian.controltower.login.url"), properties);
		
		log.debug("SkyGuardianControlTowerManager.getUnit()-loginUrl="+loginUrl);
		
		
		JSONObject loginJSONObj = httpReqExecutor.getHTTPRequest(loginUrl);
		
		wialonSession.setEid((String) loginJSONObj.get("eid"));
		wialonSession.setServerTime((Integer) loginJSONObj.get("tm"));
		
		if (!wialonSession.getEid().isEmpty()) {
			properties.clear();
			properties.put("unitId", unitId);
			properties.put("eid", wialonSession.getEid());
			
			String unitUrl = AppUtils.getURL(
					appProperties.getProperty("mx.skyguardian.controltower.searchbyid.url"), properties);
			
			log.debug("SkyGuardianControlTowerManager.getUnit()-unitUrl="+unitUrl);
			
			JSONObject itemObj = httpReqExecutor.getHTTPRequest(unitUrl);
				if(!itemObj.isNull("item")) {
					JSONObject jsonItem = (JSONObject) itemObj.get("item"); 
					AbstractWialonEntity unit = new Unit();
					((Unit) unit).setUnitId(Long.parseLong((jsonItem.isNull("id"))?"0":jsonItem.get("id").toString()));
					((Unit) unit).setUnitName((jsonItem.isNull("nm"))?"invalidUnit":jsonItem.get("nm").toString());
					((Unit) unit).setGeoPosition(jsonDeserializer.getGEOPosition(jsonItem, unitId, wialonSession));
					((Unit) unit).setLastMsgReport((jsonDeserializer.getLastMsgReport(jsonItem)));
					return unit;
				}

		} else {
			log.error("Authentication error...");
			throw new WialonAccessDeniedException();
		}

		return new Unit();
	}
	
	public AbstractWialonEntity getUnits(String userName, String password) throws IOException {
		
		WialonSession wialonSession = new WialonSession();
		wialonSession.setUserName(userName);
		wialonSession.setPassword(JasyptEncryptor.decryptPBEText(password));
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("user", wialonSession.getUserName());
		properties.put("password", wialonSession.getPassword());
		
		String loginUrl = AppUtils.getURL(
				appProperties.getProperty("mx.skyguardian.controltower.login.url"), properties);
		
		log.debug("SkyGuardianControlTowerManager.getUnits()-loginUrl="+loginUrl);
		
		JSONObject loginJSONObj = httpReqExecutor.getHTTPRequest(loginUrl);
		
		wialonSession.setEid((String) loginJSONObj.get("eid"));
		wialonSession.setServerTime((Integer) loginJSONObj.get("tm"));
		
		if (!wialonSession.getEid().isEmpty()) {
			properties.clear();
			properties.put("sid", wialonSession.getEid());
			
			String unitsUrl = AppUtils.getURL(
					appProperties.getProperty("mx.skyguardian.controltower.search.units.url"), properties);
			
			log.debug("SkyGuardianControlTowerManager.getUnits()-unitsUrl="+unitsUrl);
			
			JSONObject itemObj = httpReqExecutor.getHTTPRequest(unitsUrl);
			
			if (!itemObj.isNull("searchSpec")) {
				JSONArray jsonArray = (JSONArray) itemObj.getJSONArray("items");
				if (jsonArray.length() > 0) {
						Units units = new Units();
					List<Unit> unitsList = new ArrayList<Unit>();
						for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonItem = (JSONObject) jsonArray.get(i);
							AbstractWialonEntity unit = new Unit();
						((Unit) unit).setUnitId(Long.parseLong((jsonItem.isNull("id")) ? "0" : jsonItem.get("id").toString()));
						((Unit) unit).setUnitName((jsonItem.isNull("nm")) ? "invalidUnit": jsonItem.get("nm").toString());
						((Unit) unit).setGeoPosition(jsonDeserializer.getGEOPosition(jsonItem, jsonItem.get("id").toString(),wialonSession));
						((Unit) unit).setLastMsgReport((jsonDeserializer.getLastMsgReport(jsonItem)));
							unitsList.add((Unit) unit);
					}
						units.setUnit(unitsList);
					return units;
				}
			}

		} else {
			log.error("Authentication error...");
			throw new WialonAccessDeniedException();
		}

		return new Units();
	}
	
	@Override
	public AbstractWialonEntity getVehicles(String userName, String password) throws WialonAccessDeniedException, IOException {
		
		WialonSession wialonSession = new WialonSession();
		wialonSession.setUserName(userName);
		wialonSession.setPassword(JasyptEncryptor.decryptPBEText(password));
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("user", wialonSession.getUserName());
		properties.put("password", wialonSession.getPassword());
		
		String loginUrl = AppUtils.getURL(
				appProperties.getProperty("mx.skyguardian.controltower.login.url"), properties);
		
		log.debug("SkyGuardianControlTowerManager.getVehicles()-loginUrl="+loginUrl);
		
		JSONObject loginJSONObj = httpReqExecutor.getHTTPRequest(loginUrl);
		
		wialonSession.setEid((String) loginJSONObj.get("eid"));
		wialonSession.setServerTime((Integer) loginJSONObj.get("tm"));
		
		if (!wialonSession.getEid().isEmpty()) {
			properties.clear();
			properties.put("sid", wialonSession.getEid());
			
			String unitsUrl = AppUtils.getURL(
					appProperties.getProperty("mx.skyguardian.controltower.search.units.url"), properties);
			
			log.debug("SkyGuardianControlTowerManager.getVehicles()-unitsUrl="+unitsUrl);
			
			JSONObject itemObj = httpReqExecutor.getHTTPRequest(unitsUrl);

			if (!itemObj.isNull("searchSpec")) {
				JSONArray jsonArray = (JSONArray) itemObj.getJSONArray("items");
				if (jsonArray.length() > 0) {
						Vehicles vehicles = new Vehicles();
					List<Vehicle> vehicleList = new ArrayList<Vehicle>();
						for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonItem = (JSONObject) jsonArray.get(i);
							AbstractWialonEntity vehicle = new Vehicle();
						((Vehicle) vehicle).setVehicleId(Long.parseLong((jsonItem.isNull("id")) ? "0" : jsonItem.get("id").toString()));
						((Vehicle) vehicle).setVehiclePlate((jsonItem.isNull("nm")) ? "unknown unit": jsonItem.get("nm").toString());
							vehicleList.add((Vehicle) vehicle);
					}
						vehicles.setVehicle(vehicleList);
					return vehicles;
				}
			}
			
		} else {
			log.error("Authentication error...");
			throw new WialonInternalServerError();
		}

		return new Vehicles();
	}
	
	public AbstractWialonEntity getVehiculeHistory(String vehicleId,String interval, String loadCount, String userName, String password) throws WialonAccessDeniedException, IOException {
		WialonSession wialonSession = new WialonSession();
		wialonSession.setUserName(userName);
		wialonSession.setPassword(JasyptEncryptor.decryptPBEText(password));
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("user", wialonSession.getUserName());
		properties.put("password", wialonSession.getPassword());
		
		String loginUrl = AppUtils.getURL(
				appProperties.getProperty("mx.skyguardian.controltower.login.url"), properties);
		
		log.debug("SkyGuardianControlTowerManager.getVehiculeHistory()-loginUrl="+loginUrl);
		
		JSONObject loginJSONObj = httpReqExecutor.getHTTPRequest(loginUrl);
		
		wialonSession.setEid((String) loginJSONObj.get("eid"));
		wialonSession.setServerTime((Integer) loginJSONObj.get("tm"));
		
		
		if (!wialonSession.getEid().isEmpty()) {
			properties.clear();
			
			Integer serverTime = wialonSession.getServerTime();
			String timeFrom = AppUtils.getTimeFrom(serverTime, interval);
			
			properties.put("vehiculeId", vehicleId);
			properties.put("timeFrom", timeFrom);
			properties.put("timeTo", String.valueOf(serverTime));
			properties.put("loadCount", loadCount);
			properties.put("sid", wialonSession.getEid());
			
			String vehiculeHURL = AppUtils.getURL(
					appProperties.getProperty("mx.skyguardian.controltower.vehicule.history.url"), properties);
			
			log.debug("SkyGuardianControlTowerManager.getVehiculeHistory()-vehiculeHURL="+vehiculeHURL);
			
			JSONObject itemObj = httpReqExecutor.getHTTPRequest(vehiculeHURL);
			if (!itemObj.isNull("messages")) {
				JSONArray jsonArray = (JSONArray) itemObj.getJSONArray("messages");
					if (jsonArray.length() > 0) {
					VehicleHistory vehicleHistory = new VehicleHistory();
					vehicleHistory.setVehicleId(Long.valueOf(vehicleId));
					
					List<VehicleHistoryItem> vehicleHistoryItemList = new ArrayList<VehicleHistoryItem>();
					
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonItem = (JSONObject) jsonArray.get(i);
							Long dateTimeSec = Long.parseLong((jsonItem.isNull("t"))?"0":jsonItem.get("t").toString());
						
						AbstractWialonEntity vehicleHistoryItem = new VehicleHistoryItem();
						
						((VehicleHistoryItem) vehicleHistoryItem).setGeoPosition(jsonDeserializer.getGEOPosition(jsonItem, vehicleId,wialonSession));
						
						((GeoPosition)((VehicleHistoryItem) vehicleHistoryItem).getGeoPosition()).setTimeUTC(dateTimeSec);
						
						((GeoPosition)((VehicleHistoryItem) vehicleHistoryItem).getGeoPosition()).setDateTime(
								AppUtils.getFormattedDate(
										appProperties.getProperty("mx.skyguardian.controltower.geoposition.datetime.format"), 
										dateTimeSec));
						
						((VehicleHistoryItem) vehicleHistoryItem).setLastMsgReport((jsonDeserializer.getLastMsgReport(jsonItem)));
						
						vehicleHistoryItemList.add((VehicleHistoryItem) vehicleHistoryItem);
					}
						
					vehicleHistory.setHistory(vehicleHistoryItemList);
					return vehicleHistory;
				}
			}
		} else {
			throw new WialonInternalServerError();
		}

		return new VehicleHistory();
	}
	
	@Override
	public AbstractWialonEntity getPOI(String unitID, String userName, String password) throws IOException {
		
		POI poi = new POI();
		
		WialonSession wialonSession = new WialonSession();
		wialonSession.setUserName(userName);
		wialonSession.setPassword(JasyptEncryptor.decryptPBEText(password));
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("user", wialonSession.getUserName());
		properties.put("password", wialonSession.getPassword());
		
		String loginUrl = AppUtils.getURL(appProperties.getProperty("mx.skyguardian.controltower.login.url"), properties);
		
		log.debug("SkyGuardianControlTowerManager.getPOI()-loginUrl="+loginUrl);
		
		JSONObject loginJSONObj = httpReqExecutor.getHTTPRequest(loginUrl);
		
		String eid = (String) loginJSONObj.get("eid");
		
		wialonSession.setEid(eid);
		wialonSession.setServerTime((Integer) loginJSONObj.get("tm"));
		
		log.debug("SERVER_TIME="+wialonSession.getServerTime());
		
		if (wialonSession.getEid() !=null && !wialonSession.getEid().isEmpty()) {
			properties.clear();
			properties.put("unitID", unitID);
			properties.put("fromDatetime", String.valueOf(AppUtils.getFromDatetime()));
			properties.put("toDatetime", String.valueOf(AppUtils.getToDatetime()));
			properties.put("sid", eid);
			
			String execReportUrl = AppUtils.getURL(appProperties.getProperty("mx.skyguardian.controltower.poi.exec.report.url"), properties);
			log.debug("SkyGuardianControlTowerManager.getPOI()-execReportUrl="+execReportUrl);
			
			JSONObject execJSONObj = httpReqExecutor.getHTTPRequest(execReportUrl);
			
			poi = (POI)jsonDeserializer.getPOI(execJSONObj, wialonSession);
			poi.setUnitID(Long.valueOf(unitID));
		} else {
			log.error("Authentication error...");
			throw new WialonAccessDeniedException();
		}

		return poi;
	}

	public void setHttpReqExecutor(IWialonHTTPRequestExecutor httpReqExecutor) {
		this.httpReqExecutor = httpReqExecutor;
	}
	
	public void setJsonDeserializer(AbsctractJSONDeserializer jsonDeserializer) {
		this.jsonDeserializer = jsonDeserializer;
	}

	public void setAppProperties(Properties appProperties) {
		this.appProperties = appProperties;
	}

	
}
