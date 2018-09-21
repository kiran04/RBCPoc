package infrrd.rbc.poc.extractor;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.WordUtils;
import org.springframework.stereotype.Component;

@Component
public class IssuedDateExtractor {

	public String extract(String getText) {
		
		String found1 = "";
		getText = getText.toLowerCase();
		getText=getText.replace(" mays", "may 3");
		List<String> keywords = Arrays.asList("issued","assessment","notice","l[' ]avis");
		
		
		String regexForIssuedDate1 = "(?<=({0}).{0,25}[; ]([\\d]{1,2})[, -]{1,2}([a-z]{3})[, -]{1,2}([\\d]{4}))";
		String regexForIssuedDate2 = "(?<=({0}).{0,25}([a-z]{3})[, -]{1,2}([\\d]{1,2})[, -]{1,2}([\\d]{4}))";
		
		Pattern p;
		boolean found = false;
		try {
		for(String current : keywords) {
			
			p = Pattern.compile(regexForIssuedDate1.replace("{0}",current ));
			Matcher m = p.matcher(getText);
			
			if(m.find()) {
				//System.out.println(m.group(0));
				//System.out.println("date issued- "+ m.group(2)+m.group(3)+m.group(4));
				found = true;
				found1 = m.group(2)+" "+m.group(3)+ " "+m.group(4);
				break;
			}
		}
		}
		catch(Exception e) {
			
		}
		try {
		if(!found) {
			for (String current : keywords) {

				p = Pattern.compile(regexForIssuedDate2.replace("{0}", current));
				Matcher m = p.matcher(getText);

				if (m.find()) {
					//System.out.println("date issued- " + m.group(2) + m.group(3) + m.group(4));
					found = true;
					found1 = m.group(2)+" " + m.group(3)+ " " + m.group(4);
					break;
				}
			}
		}
		}
		catch(Exception e) {}
		
		return WordUtils.capitalize(found1);
		
	}

}
