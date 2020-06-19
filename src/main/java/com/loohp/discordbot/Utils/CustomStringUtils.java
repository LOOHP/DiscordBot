package com.loohp.discordbot.Utils;

import java.util.ArrayList;
import java.util.List;

public class CustomStringUtils {
	
	public static String[] splitStringToArgs(String str) {
		List<String> tokens = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();

		boolean insideQuote = false;

		for (char c : str.toCharArray()) {
		    if (c == '"') {
		        insideQuote = !insideQuote;
		    } else if (c == ' ' && !insideQuote) {
		    	if (sb.length() > 0) {
					tokens.add(sb.toString());
				}
		        sb.delete(0, sb.length());
		    } else {
		        sb.append(c);
		    }
		}
		tokens.add(sb.toString());
		
		return tokens.toArray(new String[tokens.size()]);
	}

}
