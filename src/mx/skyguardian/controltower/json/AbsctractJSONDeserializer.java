package mx.skyguardian.controltower.json;

import java.io.IOException;

import mx.skyguardian.controltower.bean.AbstractWialonEntity;
import mx.skyguardian.controltower.http.remoting.AbstractUser;
import mx.skyguardian.controltower.http.remoting.WialonSession;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbsctractJSONDeserializer {
	public abstract AbstractWialonEntity getGEOPosition(JSONObject jsonObj, String unitId, AbstractUser user);
	public abstract AbstractWialonEntity getLastMsgReport(JSONObject jsonObj);
	public abstract AbstractWialonEntity getPOI(JSONObject jsonObj, WialonSession session) throws JSONException, IOException;
}
