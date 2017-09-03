/**
 * 
 */
package starter.cfsrest.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

/**
 * @author JackIce
 * @date  2016年10月17日
 * @description 
 */
public class CommonUtils {
public static String getServerTime(String formatString){
	if (StringUtils.isEmpty(formatString)) {
		formatString = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	}
	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatString);
	Date date = new Date();
	String serverTime = simpleDateFormat.format(date);
	return serverTime;
}
}
