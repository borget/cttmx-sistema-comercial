package mx.skyguardian.controltower.http.remoting;

public abstract class AbstractUser {
	private String eid = "";
	private Boolean isActive = false;
	private Integer serverTime = 0;
	
	public Integer getServerTime() {
		return serverTime;
	}

	public void setServerTime(Integer serverTime) {
		this.serverTime = serverTime;
	}

	public Boolean isActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getEid() {
		return eid;
	}

	public void setEid(String eid) {
		this.eid = eid;
	}
	
}
