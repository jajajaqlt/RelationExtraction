package tools;

import java.util.Date;

public class Time {
	public static String getCurrentTime() {
		Date date = new Date();
		return date.toString();
	}
}
