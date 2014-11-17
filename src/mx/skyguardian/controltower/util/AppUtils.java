package mx.skyguardian.controltower.util;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.util.PropertyPlaceholderHelper;
public class AppUtils {
	private static Logger log = Logger.getLogger(AppUtils.class);
	private static final int MAX_SECOND = 59;
	private static final int MAX_MINUTE = 59;
	private static final int MAX_HOUR = 23;
	
	@Resource(name = "appProperties")
	private static Properties appProperties;
	
	private AppUtils(){}
	
	public static String getFormattedDate(String format, Long epoch){
		String date = new java.text.SimpleDateFormat(format).format(new java.util.Date (epoch*1000));
		return date;
	}
	
	public static String getTimeFrom(Integer timeTo, String interval){
		
		Long intervalSec = Long.valueOf(interval) * 3600;
				
		return String.valueOf(timeTo-intervalSec);
	}
	
	public static String getURL(String url, Map<String, String> propertiesMap) {
		PropertyPlaceholderHelper h = new PropertyPlaceholderHelper("$[", "]");
		Properties p = new Properties();

		for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
			p.setProperty(entry.getKey(), entry.getValue());
		}

		return h.replacePlaceholders(url, p);
	}
	
	public static Long getFromDatetime(){
		
		TimeZone defaultTimeZone = TimeZone.getDefault();
		
		DateTime fromDate;
		
		String timeZone = appProperties.getProperty("mx.skyguardian.controltower.poi.time.zone");
		
		long posixTime = 0L;
		
		if (!defaultTimeZone.getID().equalsIgnoreCase(timeZone)){
			Calendar datetimeWithTZ = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
 
		     
		    int offset = datetimeWithTZ.getTimeZone().getOffset(datetimeWithTZ.getTimeInMillis());
		    			
		    fromDate =  new DateTime(DateTimeZone.UTC );
			
			fromDate = fromDate.plus(offset);
								
			fromDate = setTo0Hrs(fromDate);
			
			fromDate = fromDate.minus(offset);
			
			posixTime = fromDate.getMillis();
			
		} else {
			fromDate = new DateTime();
			fromDate = setTo0Hrs(fromDate);
			posixTime = fromDate.getMillis();
			
		}
		
		
		return posixTime/1000;
		
	}
	
	private static DateTime setTo0Hrs(DateTime dateTime) {
		int sec = dateTime.getSecondOfMinute();
		int min = dateTime.getMinuteOfHour();
		int hr = dateTime.getHourOfDay();
		
		dateTime=dateTime.minusSeconds(sec);
		dateTime=dateTime.minusMinutes(min);
		dateTime=dateTime.minusHours(hr);
		
		return dateTime;
	}	
	
	public static Long getToDatetime(){
		
		TimeZone defaultTimeZone = TimeZone.getDefault();
		
		DateTime toDate;
		
		String timeZone = appProperties.getProperty("mx.skyguardian.controltower.poi.time.zone");
		
		long posixTime = 0L;
		
		if (!defaultTimeZone.getID().equalsIgnoreCase(timeZone)){
			Calendar datetimeWithTZ = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
			 
		    int offset = datetimeWithTZ.getTimeZone().getOffset(datetimeWithTZ.getTimeInMillis());
		    			
		    toDate =  new DateTime(DateTimeZone.UTC );
			
		    toDate = toDate.plus(offset);
								
		    toDate = setTo023_59Hrs(toDate);
			
		    toDate = toDate.minus(offset);
			
			posixTime = toDate.getMillis();
			
		} else {
			toDate = new DateTime();
			toDate = setTo023_59Hrs(toDate);
			posixTime = toDate.getMillis();
		}
		
		return posixTime/1000;
		
	}
	
	private static DateTime setTo023_59Hrs(DateTime dateTime) {
		int sec = MAX_SECOND-dateTime.getSecondOfMinute();
		int min = MAX_MINUTE-dateTime.getMinuteOfHour();
		int hr = MAX_HOUR-dateTime.getHourOfDay();
				
		dateTime=dateTime.plusSeconds(sec);
		dateTime=dateTime.plusMinutes(min);
		dateTime=dateTime.plusHours(hr);
		
		return dateTime;
	}
	
	public static String getDatetime(String time){
	
		TimeZone defaultTimeZone = TimeZone.getDefault();
		
		Calendar baseDate;
		
		DateFormat dateFormat = new SimpleDateFormat(appProperties.getProperty("mx.skyguardian.controltower.poi.datetime.format"));
		
		String timeZone = appProperties.getProperty("mx.skyguardian.controltower.poi.time.zone");
		
		Long posixDatetime = Long.valueOf(time)*1000L;
		
		String dateTime = "";
		if (!defaultTimeZone.getID().equalsIgnoreCase(timeZone)){
			
			baseDate = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
		    dateFormat.setTimeZone(baseDate.getTimeZone());
		    baseDate.setTimeInMillis(posixDatetime);
		      
		    dateTime = dateFormat.format(baseDate.getTime());

		} else {
			baseDate = (GregorianCalendar)GregorianCalendar.getInstance();
			baseDate.setTimeInMillis(posixDatetime);
			dateTime = dateFormat.format(baseDate.getTime());
		}
		
		return dateTime;
	}

	public static void setAppProperties(Properties appProperties) {
		AppUtils.appProperties = appProperties;
	}
	
}
