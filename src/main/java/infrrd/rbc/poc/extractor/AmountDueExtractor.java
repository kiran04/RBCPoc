package infrrd.rbc.poc.extractor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class AmountDueExtractor {

	public String extract (String getText) {
		
		getText = getText.toLowerCase();
		
		return getStrightForward (getText);
		
		
		
		
	}

	private String getStrightForward(String getText) {
		
		getText = getText.replace("duo", "due");
		String[] lineSplit  = getText.split("\n");
		
		String found = "";
		String amountDue = "((?<=amount[ ]?due.{0,20})(\\d[,. \\d]*\\d))";
		Pattern p ;
		try {
			p = Pattern.compile(amountDue);
			for (int i = 0; i < lineSplit.length; i++) {
				Matcher m = p.matcher(lineSplit[i]);
				if (m.find()) {
					// System.out.println("amount due-" +m.group());
					found = "amount due-" + m.group();
					break;
				}

			}
		}
		catch(Exception e) {}
		
		
		if(StringUtils.isEmpty(found)) {
			//String balanceExtractor = "((?<=final[ ]?balance[^\\d]{0,50})([-]?[ ]*[$]?[8]\\d[,. \\d]*))";
			//
			try {
			for(int i=0;i<lineSplit.length;i++) {
				if(StringUtils.isEmpty(found)) {
					if ((lineSplit[i].contains("final") && lineSplit[i].contains("balance"))
							|| (lineSplit[i].contains("balance") && lineSplit[i].contains("due") && !lineSplit[i].contains("as at"))) {
						
						if(lineSplit[i].contains("due") && !lineSplit[i].substring(lineSplit[i].indexOf("due")).contains("$")) {
							
							int balanceIndex = lineSplit[i].indexOf("balance");
							
							String nextLine = lineSplit[i+1];
							
							//find closest dollar in next line
							int initialIndex = 0;
							
							int dollarIndex=Integer.MAX_VALUE;
							boolean dollarInLineFound =false;
							while(nextLine.indexOf("$", initialIndex)>-1) {
								
								int currentDollarIndex = nextLine.indexOf("$", initialIndex);
								
								if(Math.abs(currentDollarIndex-balanceIndex)<dollarIndex) {
									dollarInLineFound= true;
									dollarIndex = currentDollarIndex;	
									initialIndex= currentDollarIndex+1;
								}	
							}
							if(dollarInLineFound) {
								String dollarString=nextLine.substring(dollarIndex);
								
								String dollarStringregex = "[$][ ]*[-]?[ ]*\\d[\\d,. ]*";
								
								p = Pattern.compile(dollarStringregex);
								Matcher m = p.matcher(dollarString);
								
								if(m.find()) {
									//System.out.println("balancevalue-" + m.group());
									found = "balancevalue-" + m.group();
									break;		
								}	
							}
							else {
								if (i + 2 < lineSplit.length) {
									nextLine = lineSplit[i + 2];
									if (nextLine.indexOf("$") > -1) {
										String dollarStringregex = "[$][ ]*[-]?[ ]*\\d[\\d,. ]*";
										p = Pattern.compile(dollarStringregex);
										Matcher m = p.matcher(nextLine);
										if (m.find()) {

											found = "balancevalue-" + m.group();
											break;
										}
									}
								}
							}
						}
						else {

							String balanceExtractor = "([-]?[ ]*[$]?[ ]*\\d[,. \\d]*)";
							p = Pattern.compile(balanceExtractor);
							Matcher m = p.matcher(lineSplit[i]);
							while (m.find()) {

								//System.out.println("balancevalue-" + m.group());
								found = "balancevalue-" + m.group();
								break;

							}

						}
					}
				}
			}
			}
			catch(Exception e) {
				
			}
		}
		
		if(StringUtils.isEmpty(found)) {
			try {
			
			String refundAmountRegex = "((?<=refund.{0,25})[ ]*([$]?[ ]*\\d[,. \\d]*))";
			
			
			for(int i=0;i<lineSplit.length;i++) {
				if(StringUtils.isEmpty(found)) {
				
					if(lineSplit[i].contains("refund")) {
						
						p = Pattern.compile(refundAmountRegex);
						Matcher m = p.matcher(lineSplit[i]);
						
						if(m.find()) {
							//System.out.println("refund-" +m.group(2));	
							found = "refund-" +m.group(2);
						}
						else
						{

							
							int balanceIndex = lineSplit[i].indexOf("refund");
							
							String nextLine = lineSplit[i+1];
							
							//find closest dollar in next line
							int initialIndex = 0;
							
							int dollarIndex=Integer.MAX_VALUE;
							boolean dollarInLineFound =false;
							while(nextLine.indexOf("$", initialIndex)>-1) {
								
								int currentDollarIndex = nextLine.indexOf("$", initialIndex);
								
								if(Math.abs(currentDollarIndex-balanceIndex)<dollarIndex) {
									dollarInLineFound= true;
									dollarIndex = currentDollarIndex;	
									initialIndex= currentDollarIndex+1;
								}	
							}
							if(dollarInLineFound) {
								String dollarString=nextLine.substring(dollarIndex);
								
								String dollarStringregex = "[$][ ]*[-]?[ ]*\\d[\\d,. ]*";
								
								p = Pattern.compile(dollarStringregex);
								m = p.matcher(dollarString);
								
								if(m.find()) {
									//System.out.println("refund-" + m.group());
									found = "refund-" + m.group();
									break;		
								}	
							}		 
						}
					}
				}			
			}
			}
			catch(Exception e ) {}
		}
		
		if (StringUtils.isEmpty(found)) {
			try {
			String motantDueRgex = "((?<=montant[ ]?d[ûu].{0,20})(\\d[,. \\d]*))";
			for (int i = 0; i < lineSplit.length; i++) {

				p = Pattern.compile(motantDueRgex);
				Matcher m = p.matcher(lineSplit[i]);
				if(m.find()) {
					found = m.group().replace(",", ".");
					found  = "amount due-" + found.replace(" ", "");
					break;
				}

			}
			}
			catch(Exception e) {
				
			}
		}
		
		if(StringUtils.isEmpty(found)) {
			//String balanceExtractor = "((?<=final[ ]?balance[^\\d]{0,50})([-]?[ ]*[$]?[8]\\d[,. \\d]*))";
			//
			try {
				for (int i = 0; i < lineSplit.length; i++) {
					if (StringUtils.isEmpty(found)) {
						if ((lineSplit[i].contains("dû") || lineSplit[i].contains("du"))
								&& lineSplit[i].contains("solde")) {

							if (lineSplit[i].contains("solde")
									&& !lineSplit[i].substring(lineSplit[i].indexOf("solde")).contains("$")) {

								int balanceIndex = lineSplit[i].indexOf("solde");

								String nextLine = lineSplit[i + 1];

								// find closest dollar in next line
								int initialIndex = 0;

								int dollarIndex = Integer.MAX_VALUE;
								boolean dollarInLineFound = false;
								while (nextLine.indexOf("$", initialIndex) > -1) {

									int currentDollarIndex = nextLine.indexOf("$", initialIndex);

									if (Math.abs(currentDollarIndex - balanceIndex) < dollarIndex) {
										dollarInLineFound = true;
										dollarIndex = currentDollarIndex;
										initialIndex = currentDollarIndex + 1;
									}
								}
								if (dollarInLineFound) {
									String dollarString = nextLine.substring(dollarIndex);

									String dollarStringregex = "[$][ ]*[-]?[ ]*\\d[\\d,. ]*";

									p = Pattern.compile(dollarStringregex);
									Matcher m = p.matcher(dollarString);

									if (m.find()) {
										// System.out.println("balancevalue-" + m.group());
										found = "balancevalue-" + m.group().replace(",", ".");
										break;
									}
								} else {
									if (i + 2 < lineSplit.length) {
										nextLine = lineSplit[i + 2];
										if (nextLine.indexOf("$") > -1) {
											String dollarStringregex = "[$][ ]*[-]?[ ]*\\d[\\d,. ]*";
											p = Pattern.compile(dollarStringregex);
											Matcher m = p.matcher(nextLine);
											if (m.find()) {

												found = "balancevalue-" + m.group().replace(",", ".");
												break;
											}
										}
									}
								}
							} else {

								String balanceExtractor = "([-]?[ ]*[$]?[ ]*\\d[,. \\d]*)";
								p = Pattern.compile(balanceExtractor);
								Matcher m = p.matcher(lineSplit[i]);
								while (m.find()) {

									// System.out.println("balancevalue-" + m.group());
									found = "balancevalue-" + m.group().replace(",", ".");
									break;

								}

							}
						}
					}
				}
			}
			catch (Exception e){}
		}
		
		return found;
	
	}
	
	
	
	

}
