package infrrd.rbc.poc.extractor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;


@Component
public class TaxYearExtractor {

	public String extract(String getText) {
		
		getText = getText.toLowerCase();
		
		return getByKeywordsnInSameLine(getText);
	}

	private String getByKeywordsnInSameLine(String getText) {
		
		//System.out.println(getText);
		
		getText=getText.replace("yean", "year");
		
		String found = "";
		
		String[] lineSplit  = getText.split("\n");
		Pattern p;
		String taxYearRegex = "((?<=tax[ ]{0,2}year)[^0-9\\r\\n]+([0-9]{4}))";
		p = Pattern.compile(taxYearRegex);
		
		try {
		for(int i =0;i<lineSplit.length;i++) {
			
			Matcher m = p.matcher(lineSplit[i]);
			
			if(m.find()) {
				found  = m.group(2);
			}	
		}
		}
		catch(Exception e) {}
	
		try {
		if(StringUtils.isEmpty(found)) {
			String taxationYearRegex = "([^0-9\\r\\n]{0,25}([0-9]{4})(?=[ ]*tax[ a-z]*year))";
			p = Pattern.compile(taxationYearRegex);
			for(int i =0;i<lineSplit.length;i++) {
				
				Matcher m = p.matcher(lineSplit[i]);
				
				if(m.find()) {
					//System.out.println(m.group());
					//System.out.println(m.group(2));
					found  = m.group(2);
				}	
			}
		}}
		catch(Exception e) {}
		
		try {
		if(StringUtils.isEmpty(found)) {
			String taxationYearRegex = "((?<=taxation[ ]{0,2}year)[^0-9\\r\\n]{0,25}([0-9]{4}))";
			p = Pattern.compile(taxationYearRegex);
			for(int i =0;i<lineSplit.length;i++) {
				
				Matcher m = p.matcher(lineSplit[i]);
				
				if(m.find()) {
					//System.out.println(m.group());
					//System.out.println(m.group(2));
					found  = m.group(2);
				}	
			}
		}}
		catch(Exception e) {}
		
		try {
		if(StringUtils.isEmpty(found)) {
			String taxationYearRegex = "((?<=of[ ]{0,2}assessment)[^0-9\\r\\n]{0,25}([0-9]{4}))";
			p = Pattern.compile(taxationYearRegex);
			for(int i =0;i<lineSplit.length;i++) {
				
				Matcher m = p.matcher(lineSplit[i]);
				
				if(m.find()) {
					//System.out.println(m.group());
					//System.out.println(m.group(2));
					found  = m.group(2);
				}	
			}
		}}
		catch(Exception e) {}
		
		try {
		if(StringUtils.isEmpty(found)) {
			String taxationYearRegex = "((?<=ann[ée]e[ ]{0,2}d[' ]?imposition)[^0-9\\r\\n]*([0-9]{4}))";
			p = Pattern.compile(taxationYearRegex);
			for(int i =0;i<lineSplit.length;i++) {
				
				Matcher m = p.matcher(lineSplit[i]);
				
				if(m.find()) {
					found  = m.group(2);
				}	
			}
		}}
		catch(Exception e) {
			
		}
		
		
		try {
		if(StringUtils.isEmpty(found)) {
			String taxationYearRegex = "([^0-9\\r\\n]{0,25}([0-9]{4})(?=[ ]*ann[ a-z]*imposition))";
			p = Pattern.compile(taxationYearRegex);
			for(int i =0;i<lineSplit.length;i++) {
				
				Matcher m = p.matcher(lineSplit[i]);
				
				if(m.find()) {
					found  = m.group(2);
				}	
			}
		}}
		catch(Exception e) {}
		return found;
		
	}

}
