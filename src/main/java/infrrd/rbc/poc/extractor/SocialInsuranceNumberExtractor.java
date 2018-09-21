package infrrd.rbc.poc.extractor;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SocialInsuranceNumberExtractor {
	
	
	

	public String extract(String getText) {
		
		
		String found  = "";
		try {
		getText = getText.toLowerCase();
		List<String> keywords = Arrays.asList("social","insurance","identification","sociale","assurance");
		   
		
		String regexForSIN  = "(?<=({0}).{0,50}([\\dx]{3})[ -]?([\\dx]{3})[ -]?([\\dx]{3}))";
		
		Pattern p;
		
		for(String current : keywords) {
			
			p = Pattern.compile(regexForSIN.replace("{0}",current ));
			Matcher m = p.matcher(getText);
			
			if(m.find()) {
				//System.out.println("Social insurance number- "+ m.group(2)+m.group(3)+m.group(4));
				found=  m.group(2)+m.group(3)+m.group(4);
				break;
			}
		}
		}
		catch(Exception e) {}
		return found;
		
	}

}
