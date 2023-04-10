package org.easy.util;

import java.awt.Dimension;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

	
	public static void main(String[] args) {
		
		System.out.println(convertDate("19/08/1981", "dd/MM/yyyy"));
		
	}
	
	public static Date convertDate(String dateStr, String dateFormat) {
		DateFormat sdf = new SimpleDateFormat(dateFormat);
        
		sdf.setLenient(false);
        
        try {
        
        	return sdf.parse(dateStr);
        
        } catch (ParseException e) {
        
        	return null;
        
        }
        
	}
	
	public static boolean isDate(String dateStr, String dateFormat) {
		DateFormat sdf = new SimpleDateFormat(dateFormat);
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
        } catch (ParseException e) {
            return false;
        }
        return true;
	}
	
	public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

	    int original_width = imgSize.width;
	    int original_height = imgSize.height;
	    int bound_width = boundary.width;
	    int bound_height = boundary.height;
	    int new_width = original_width;
	    int new_height = original_height;

	    // first check if we need to scale width
	    if (original_width > bound_width) {
	        //scale width to fit
	        new_width = bound_width;
	        //scale height to maintain aspect ratio
	        new_height = (new_width * original_height) / original_width;
	    }

	    // then check if we need to scale even with the new height
	    if (new_height > bound_height) {
	        //scale height to fit instead
	        new_height = bound_height;
	        //scale width to maintain aspect ratio
	        new_width = (new_height * original_width) / original_height;
	    }

	    return new Dimension(new_width, new_height);
	}
}
