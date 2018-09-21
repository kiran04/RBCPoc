package infrrd.rbc.poc.extractor;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class TaxAmountsExtractor {

	public Map<String, String> extract(String getText) {
		
		getText = getText.toLowerCase();
		
		String totalIncome = totalIncomeExtract(getText);
		String netIncome = netIncomeExtract(getText);
		String taxableIncome = taxableIncome(getText);
		String totalPayable = totalPayableTax(getText);
		String totalCredits = totalCredits(getText);
		String incomeTax  = incomeTaxExtract(getText);
		
		
		
		Map<String,String> valuesMap  = new HashMap<>();
		if(StringUtils.isEmpty(totalIncome)) 
			valuesMap.put("Total Income", "");
		else
			valuesMap.put("Total Income", refine(totalIncome.split("-")[1]));
		
		if(StringUtils.isEmpty(netIncome)) 
			valuesMap.put("Net Income", "");
		else
			valuesMap.put("Net Income", refine(netIncome.split("-")[1]));
		if(StringUtils.isEmpty(taxableIncome)) 
			valuesMap.put("Taxable Income", "");
		else
			valuesMap.put("Taxable Income", refine(taxableIncome.split("-")[1]));
		if(!StringUtils.isEmpty(totalPayable))
			valuesMap.put("Total Payable(Tax)",refine( totalPayable.split("-")[1]));
		else
			valuesMap.put("Total Payable(Tax)", "");
		if(!StringUtils.isEmpty(totalCredits))
			valuesMap.put("Total Credits(Tax)", refine(totalCredits.split("-")[1]));
		else
			valuesMap.put("Total Credits(Tax)", "");
		
		if(!StringUtils.isEmpty(incomeTax))
			valuesMap.put("Income Tax", refine(incomeTax.split("-")[1]));
		else
			valuesMap.put("Income Tax", "");
		
		
		if(!StringUtils.isEmpty(totalPayable) && !StringUtils.isEmpty(totalCredits)) {
			
			try {
				float totalPayableInt = Float.parseFloat(totalPayable.split("-")[1].replaceAll("[,$]", ""));
				float totalCreditsInt = Float.parseFloat(totalCredits.split("-")[1].replaceAll("[,$]", ""));
				float total = totalPayableInt-totalCreditsInt;
				String total1 = total+"";
				int indexOfdot = total1.indexOf(".");
				if(indexOfdot==-1)
					total1 = total1+".00";
				else if(indexOfdot!=total1.length()-3) {
					if(indexOfdot==total1.length()-2)
						total1 = total1+"0";
					else
						total1 = total1.substring(0,indexOfdot+3);	
				}
				valuesMap.put("Payable minus Credits(Net Tax)","$"+total1);
			}
			catch(Exception e) {	
			}	
		}
		else if (!StringUtils.isEmpty(totalPayable)) {
			String balanceBeforePenaltyAndInterest = balanceBeforePenaltyAndInterest(getText);
			if (!StringUtils.isEmpty(balanceBeforePenaltyAndInterest)) {
				try {
					float totalPayableInt = Float.parseFloat(totalPayable.split("-")[1].replaceAll("[,$]", ""));
					float balanceBeforePenaltyAndInterestInt = Float.parseFloat(balanceBeforePenaltyAndInterest.replaceAll("[,$]", ""));
					float total = totalPayableInt - balanceBeforePenaltyAndInterestInt;
					valuesMap.put("Payable minus Credits(Net Tax)",refine( balanceBeforePenaltyAndInterest + ""));
					String total1 = total+"";
					int indexOfdot = total1.indexOf(".");
					if(indexOfdot==-1)
						total1 = total1+".00";
					else if(indexOfdot!=total1.length()-3) {
						if(indexOfdot==total1.length()-2)
							total1 = total1+"0";
						else
							total1 = total1.substring(0,indexOfdot+3);	
					}
					valuesMap.put("Total Credits(Tax)",refine (total1));
				} catch (Exception e) {

				}
			} else {
				valuesMap.put("Payable minus Credits(Net Tax)","");
			}

		}
		else {
			valuesMap.put("Payable minus Credits(Net Tax)","");
		}
		
		
		return valuesMap;
	}

	
	
	
	
	
	
	private String incomeTaxExtract(String getText) {

		
		String found = "";
		String[] listLines = getText.split("\n");

		String totalIncomeRegex = "((?<=income[ ]{0,2}tax)[^\\d]*(\\d[ .,\\d]*))";
		Pattern p = Pattern.compile(totalIncomeRegex);
		Matcher m;
		try {
			for (int i = 0; i < listLines.length; i++) {
				if (StringUtils.isEmpty(found)) {
					String currentLine = listLines[i];
					m = p.matcher(currentLine);

					while (m.find()) {
						if (!currentLine.contains("deduct") && !currentLine.contains("adjust") && !currentLine.contains("contrib") && !currentLine.contains("taxable")) {
							if (currentLine.contains("$")) {
								Pattern p1 = Pattern.compile("([$][^\\d]*(\\d[ .,\\d]*))");
								Matcher m1 = p1.matcher(currentLine);
								if (m1.find()) {
									// System.out.println("net income -"+ m1.group());
									found = "income tax -" + m1.group();
								}
							} else {
								// System.out.println("net income -"+ m.group(2));
								found = "income tax -" + m.group(2);

								String valueFound = found.split("-")[1];

								Pattern p3 = Pattern.compile("\\d[ ]{4,}\\d");
								Matcher m3 = p3.matcher(valueFound);

								if (m3.find()) {
									Pattern p4 = Pattern.compile("[ ](\\d[ .,\\d]*)");
									Matcher m4 = p4.matcher(valueFound);
									if (m4.find()) {
										// System.out.println("net income -"+ m4.group(1));
										found = "income tax -" + m4.group(1);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}
		try {
			if (StringUtils.isEmpty(found)) {
				String revenueNet = "((?<=imp[ôo]t[ ]{0,2}revenu[])[^\\d]*(\\d[ .,\\d]*))";
				p = Pattern.compile(revenueNet);
				for (int i = 0; i < listLines.length; i++) {
					if (StringUtils.isEmpty(found)) {
						String currentLine = listLines[i];
						m = p.matcher(currentLine);

						while (m.find()) {
							if (currentLine.contains("$")) {
								Pattern p1 = Pattern.compile("([^\\d]*(\\d[ .,\\d]*)[$])");
								Matcher m1 = p1.matcher(currentLine);
								if (m1.find()) {
									// System.out.println("net income -"+ m1.group());
									found = m1.group().replace(",", ".").replace(" ", "");
									;
									found = "net income -" + found;
								}
							} else {
								// System.out.println("net income -"+ m.group(2));
								found = m.group(2).replace(",", ".");
								found = "net income -" + found;

								String valueFound = found.split("-")[1];

								Pattern p3 = Pattern.compile("\\d[ ]{4,}\\d");
								Matcher m3 = p3.matcher(valueFound);

								if (m3.find()) {
									Pattern p4 = Pattern.compile("[ ](\\d[ .,\\d]*)");
									Matcher m4 = p4.matcher(valueFound);
									if (m4.find()) {
										found = m4.group(1).replace(",", ".").replace(" ", "");
										;
										found = "net income -" + found;
									}
								} else
									found = found.replace(" ", "");
							}

						}
					}
				}
			}
		} catch (Exception e) {
		}

		return found;

	}







	public String refine(String oldValue) {
		
		String refined  = oldValue;
		int initial = 0;
		try {
			
			refined  = refined.replace(" ", "");
			while (oldValue.indexOf(".", initial) > -1) {
				int indexFound = refined.indexOf(".", initial);

				if (indexFound != refined.length() - 3) {
					refined = refined.substring(0, indexFound) + "" + refined.substring(indexFound + 1);
				}
				initial = indexFound + 1;
			}
			if(refined.lastIndexOf(",")==refined.length() - 3) {
				refined = refined.substring(0, refined.lastIndexOf(",")) + "." + refined.substring(refined.lastIndexOf(",") + 1);
			}
			if(refined.indexOf(".")==-1) {
				
				refined = refined + ".00";
			}
			refined = refined.replace(",", "");
			if(!refined.contains("$")) {
				
				refined = "$"+refined;
				
			}
		}
		catch(Exception e){
			return oldValue;
		}
		
		return refined;
		
	}







	private String balanceBeforePenaltyAndInterest(String getText) {


		String found = "";
		String[] listLines = getText.split("\n");
		String totalIncomeRegex = "((?<=balance[ ]{0,2}be.{2,8}pen.{2,15}int.{8,12})[^\\d]*(\\d[ .,\\d]*))";
		Pattern p= Pattern.compile(totalIncomeRegex);
		Matcher m;
		try {
		for(int i =0;i<listLines.length;i++) {
			if(StringUtils.isEmpty(found)) {
				String currentLine  = listLines[i];
				m = p.matcher(currentLine);
				
				while (m.find()) {
					if (currentLine.contains("$")) {
						Pattern p1 = Pattern.compile("([-]?[$][^\\d]*(\\d[ .,\\d]*))");
						Matcher m1 = p1.matcher(currentLine);
						if (m1.find()) {
							// System.out.println("taxable income -"+ m1.group());
							found =  m1.group();
						}
					} else {
						// System.out.println("taxable income -"+ m.group(2));
						found =  m.group(2);
						
						String valueFound = found;

						Pattern p3 = Pattern.compile("\\d[ ]{4,}\\d");
						Matcher m3 = p3.matcher(valueFound);

						if (m3.find()) {
							Pattern p4 = Pattern.compile("[ ](\\d[ .,\\d]*)");
							Matcher m4 = p4.matcher(valueFound);
							if (m4.find()) {
								// System.out.println("taxable income -"+ m4.group(1));
								found =  m4.group(1);
							}
						}
					}

				}
			}
			else
				break;
		}}
		catch(Exception e) {}
		return found;
	
	
	}







	private String totalCredits(String getText) {


		String found = "";
		String[] listLines = getText.split("\n");
		String totalIncomeRegex = "((?<=total[ ]{0,2}credits)[^\\d]*(\\d[ .,\\d]*))";
		Pattern p= Pattern.compile(totalIncomeRegex);
		Matcher m;
		
		try {
		for(int i =0;i<listLines.length;i++) {
			if(StringUtils.isEmpty(found)) {
				String currentLine  = listLines[i];
				m = p.matcher(currentLine);
				
				while(m.find()) {
					if(!currentLine.contains("non") && !currentLine.contains("minus")) {
						if(currentLine.contains("$")) {
							Pattern p1 = Pattern.compile("([$][^\\d]*(\\d[ .,\\d]*))");
							Matcher m1 = p1.matcher(currentLine);
							if(m1.find()) {
								//System.out.println("taxable income -"+ m1.group());
								found="total credits -"+ m1.group();
							}	
						}else
						{
							//System.out.println("taxable income -"+ m.group(2));
							found="total payable -"+ m.group(2);
							
							String valueFound  = found.split("-")[1];
							
							Pattern p3  = Pattern.compile("\\d[ ]{4,}\\d");
							Matcher m3 = p3.matcher(valueFound);
							
							if(m3.find()) {
								Pattern p4 = Pattern.compile("[ ](\\d[ .,\\d]*)");
								Matcher m4 = p4.matcher(valueFound);
								if(m4.find()) {
									//System.out.println("taxable income -"+ m4.group(1));
									found="total credits -"+ m4.group(1);	
								}	
							}
						}
					}
				}
			}
		}}
		catch(Exception e) {}
		
		try {
		if(StringUtils.isEmpty(found)) {
			String totalCreditsRegex = "((?<=total[ ]{0,2}des[ ]{0,2}cr[eé]dits)[^\\d]*(\\d[ .,\\d]*))";
			p= Pattern.compile(totalCreditsRegex);
			for(int i =0;i<listLines.length;i++) {
				if(StringUtils.isEmpty(found)) {
					String currentLine  = listLines[i];
					m = p.matcher(currentLine);
					
					while(m.find()) {
						if(!currentLine.contains("non") && !currentLine.contains("moins")) {
							if(currentLine.contains("$")) {
								Pattern p1 = Pattern.compile("([^\\d]*(\\d[ .,\\d]*)[$])");
								Matcher m1 = p1.matcher(currentLine);
								if(m1.find()) {
									//System.out.println("taxable income -"+ m1.group());
									found="total credits -"+ m1.group().replace(",", ".").replace(" ", "");	
								}	
							}else
							{
								//System.out.println("taxable income -"+ m.group(2));
								found="total payable -"+ m.group(2).replace(",", ".");
								
								String valueFound  = found.split("-")[1];
								
								Pattern p3  = Pattern.compile("\\d[ ]{4,}\\d");
								Matcher m3 = p3.matcher(valueFound);
								
								if(m3.find()) {
									Pattern p4 = Pattern.compile("[ ](\\d[ .,\\d]*)");
									Matcher m4 = p4.matcher(valueFound);
									if(m4.find()) {
										//System.out.println("taxable income -"+ m4.group(1));
										found="total credits -"+ m4.group(1).replace(",", ".").replace(" ", "");	
									}	
								}
								else
									found = found.replace(" ", "");
							}
						}
					}
				}
			}}
		}
		catch(Exception e) {}
		return found;
	}







	private String totalPayableTax(String getText) {

		String found = "";
		String[] listLines = getText.split("\n");
		String totalIncomeRegex = "((?<=total[ ]{0,2}payable)[^\\d]*(\\d[ .,\\d]*))";
		Pattern p= Pattern.compile(totalIncomeRegex);
		Matcher m;
		
		try {
		for(int i =0;i<listLines.length;i++) {
			if(StringUtils.isEmpty(found)) {
				String currentLine  = listLines[i];
				m = p.matcher(currentLine);
				
				while(m.find()) {
					if(!currentLine.contains("deduct") && !currentLine.contains("adjust") && !currentLine.contains("minus")) {
						if(currentLine.contains("$")) {
							Pattern p1 = Pattern.compile("([$][^\\d]*(\\d[ .,\\d]*))");
							Matcher m1 = p1.matcher(currentLine);
							if(m1.find()) {
								//System.out.println("taxable income -"+ m1.group());
								found="total payable -"+ m1.group();
							}	
						}else
						{
							//System.out.println("taxable income -"+ m.group(2));
							found="total payable -"+ m.group(2);
							
							String valueFound  = found.split("-")[1];
							
							Pattern p3  = Pattern.compile("\\d[ ]{4,}\\d");
							Matcher m3 = p3.matcher(valueFound);
							
							if(m3.find()) {
								Pattern p4 = Pattern.compile("[ ](\\d[ .,\\d]*)");
								Matcher m4 = p4.matcher(valueFound);
								if(m4.find()) {
									//System.out.println("taxable income -"+ m4.group(1));
									found="total payable -"+ m4.group(1);	
								}	
							}
						}
					}
				}
			}
		}}
		catch(Exception e) {}
		try {
		if(StringUtils.isEmpty(found)) {
			String totalAPayer = "((?<=total.{0,7}payer)[^\\d]*(\\d[ .,\\d]*))";
			p= Pattern.compile(totalAPayer);
			
			for (int i = 0; i < listLines.length; i++) {
				if (StringUtils.isEmpty(found)) {
					String currentLine = listLines[i];
					m = p.matcher(currentLine);

					while (m.find()) {
						if (currentLine.contains("$")) {
							Pattern p1 = Pattern.compile("([^\\d]*(\\d[ .,\\d]*)[$])");
							Matcher m1 = p1.matcher(currentLine);
							if (m1.find()) {
								// System.out.println("taxable income -"+ m1.group());
								found = "total payable -" + m1.group().replace(",", ".").replace(" ", "");
							}
						} else {
							// System.out.println("taxable income -"+ m.group(2));
							found = "total payable -" + m.group(2).replace(",", ".");

							String valueFound = found.split("-")[1];

							Pattern p3 = Pattern.compile("\\d[ ]{4,}\\d");
							Matcher m3 = p3.matcher(valueFound);

							if (m3.find()) {
								Pattern p4 = Pattern.compile("[ ](\\d[ .,\\d]*)");
								Matcher m4 = p4.matcher(valueFound);
								if (m4.find()) {
									// System.out.println("taxable income -"+ m4.group(1));
									found = "total payable -" + m4.group(1).replace(",", ".").replace(" ", "");;
								}
							}
							else
								found = found.replace(" ", "");
						}

					}
				}
			}	
		} }
		catch(Exception e) {}
		return found;
	
	}







	private String taxableIncome(String getText) {
		String found = "";
		String[] listLines = getText.split("\n");
		String totalIncomeRegex = "((?<=taxable[ ]{0,2}income)[^\\d]*(\\d[ .,\\d]*))";
		Pattern p= Pattern.compile(totalIncomeRegex);
		Matcher m;
		
		try {
		for(int i =0;i<listLines.length;i++) {
			if(StringUtils.isEmpty(found)) {
				String currentLine  = listLines[i];
				m = p.matcher(currentLine);
				
				while(m.find()) {
					if(!currentLine.contains("deduct") && !currentLine.contains("adjust")) {
						if(currentLine.contains("$")) {
							Pattern p1 = Pattern.compile("([$][^\\d]*(\\d[ .,\\d]*))");
							Matcher m1 = p1.matcher(currentLine);
							if(m1.find()) {
								//System.out.println("taxable income -"+ m1.group());
								found="taxable income -"+ m1.group();
							}	
						}else
						{
							//System.out.println("taxable income -"+ m.group(2));
							found="taxable income -"+ m.group(2);
							
							String valueFound  = found.split("-")[1];
							
							Pattern p3  = Pattern.compile("\\d[ ]{4,}\\d");
							Matcher m3 = p3.matcher(valueFound);
							
							if(m3.find()) {
								Pattern p4 = Pattern.compile("[ ](\\d[ .,\\d]*)");
								Matcher m4 = p4.matcher(valueFound);
								if(m4.find()) {
									//System.out.println("taxable income -"+ m4.group(1));
									found="taxable income -"+ m4.group(1);	
								}	
							}
						}
					}
				}
			}
		}}
		catch(Exception e) {}
		try {
		if(StringUtils.isEmpty(found)) {
			String revenueimposable = "((?<=revenu[ ]{0,2}imposable)[^\\d]*(\\d[ .,\\d]*))";
			p= Pattern.compile(revenueimposable);
			
			for(int i =0;i<listLines.length;i++) {
				if(StringUtils.isEmpty(found)) {
					String currentLine  = listLines[i];
					m = p.matcher(currentLine);
					
					while (m.find()) {
						if (currentLine.contains("$")) {
							Pattern p1 = Pattern.compile("([^\\d]*(\\d[ .,\\d]*)[$])");
							Matcher m1 = p1.matcher(currentLine);
							if (m1.find()) {
								found = m1.group().replace(",", ".").replace(" ", "");;
								found = "taxable income -" + found;
							}
						} else {
							// System.out.println("taxable income -"+ m.group(2));
							found = "taxable income -" + m.group(2);

							String valueFound = found.split("-")[1];

							Pattern p3 = Pattern.compile("\\d[ ]{4,}\\d");
							Matcher m3 = p3.matcher(valueFound);

							if (m3.find()) {
								Pattern p4 = Pattern.compile("[ ](\\d[ .,\\d]*)");
								Matcher m4 = p4.matcher(valueFound);
								if (m4.find()) {
									// System.out.println("taxable income -"+ m4.group(1));
									found = "taxable income -" + m4.group(1).replace(",", ".").replace(" ", "");;
								}
							}
							else
								found = found.replace(" ", "");
						}
					}
					
				}
			}
			
		}}
		catch(Exception e) {}
		
		
		
		return found;
	}

	private String netIncomeExtract(String getText) {
		
		
		getText = getText.replace("net irvbbms", "net income");
		String found = "";
		String[] listLines = getText.split("\n");
		
		String totalIncomeRegex = "((?<=net[ ]{0,2}income)[^\\d]*(\\d[ .,\\d]*))";
		Pattern p= Pattern.compile(totalIncomeRegex);
		Matcher m;
		try {
		for(int i =0;i<listLines.length;i++) {
			if(StringUtils.isEmpty(found)) {
				String currentLine  = listLines[i];
				m = p.matcher(currentLine);
				
				while(m.find()) {
					if(!currentLine.contains("deduct") && !currentLine.contains("adjust")) {
						if(currentLine.contains("$")) {
							Pattern p1 = Pattern.compile("([$][^\\d]*(\\d[ .,\\d]*))");
							Matcher m1 = p1.matcher(currentLine);
							if(m1.find()) {
								//System.out.println("net income -"+ m1.group());
								found="net income -"+ m1.group();
							}	
						}else
						{
							//System.out.println("net income -"+ m.group(2));
							found="net income -"+ m.group(2);
							
							String valueFound  = found.split("-")[1];
							
							Pattern p3  = Pattern.compile("\\d[ ]{4,}\\d");
							Matcher m3 = p3.matcher(valueFound);
							
							if(m3.find()) {
								Pattern p4 = Pattern.compile("[ ](\\d[ .,\\d]*)");
								Matcher m4 = p4.matcher(valueFound);
								if(m4.find()) {
									//System.out.println("net income -"+ m4.group(1));
									found="net income -"+ m4.group(1);	
								}	
							}
						}	
					}
				}
			}
		}}
		catch(Exception e) {}
		try {
		if(StringUtils.isEmpty(found)) {
			String revenueNet = "((?<=revenu[ ]{0,2}net)[^\\d]*(\\d[ .,\\d]*))";
			p= Pattern.compile(revenueNet);
			for (int i = 0; i < listLines.length; i++) {
				if (StringUtils.isEmpty(found)) {
					String currentLine = listLines[i];
					m = p.matcher(currentLine);

					while (m.find()) {
						if (currentLine.contains("$")) {
							Pattern p1 = Pattern.compile("([^\\d]*(\\d[ .,\\d]*)[$])");
							Matcher m1 = p1.matcher(currentLine);
							if (m1.find()) {
								// System.out.println("net income -"+ m1.group());
								found = m1.group().replace(",", ".").replace(" ", "");;
								found = "net income -" + found;
							}
						} else {
							// System.out.println("net income -"+ m.group(2));
							found = m.group(2).replace(",", ".");
							found = "net income -" + found;

							String valueFound = found.split("-")[1];

							Pattern p3 = Pattern.compile("\\d[ ]{4,}\\d");
							Matcher m3 = p3.matcher(valueFound);

							if (m3.find()) {
								Pattern p4 = Pattern.compile("[ ](\\d[ .,\\d]*)");
								Matcher m4 = p4.matcher(valueFound);
								if (m4.find()) {
									found = m4.group(1).replace(",", ".").replace(" ", "");;
									found = "net income -" + found;
								}
							}
							else
								found = found.replace(" ", "");
						}

					}
				}
			}	
		}}
		catch(Exception e) {}
		
		
		return found;
	}

	private String totalIncomeExtract(String getText) {
		getText = getText.replace("totaliinconie", "total income");
		String found = "";
		String[] listLines = getText.split("\n");
		String totalIncomeRegex = "((?<=total[ ]{0,2}income)[^\\d]*(\\d[ .,\\d]*))";
		Pattern p= Pattern.compile(totalIncomeRegex);
		Matcher m;
		try {
		for(int i =0;i<listLines.length;i++) {
			if(StringUtils.isEmpty(found)) {
				String currentLine  = listLines[i];
				//currentLine = currentLine.replaceAll("\\(.*\\)", "");
				m = p.matcher(currentLine);
				while(m.find()) {
					if(!currentLine.contains("deduc")) {
						if(currentLine.contains("$")) {
							Pattern p1 = Pattern.compile("([$][^\\d]*(\\d[ .,\\d]*))");
							Matcher m1 = p1.matcher(currentLine);
							if(m1.find()) {
								//System.out.println("total income -"+ m1.group());
								found="total income -"+ m1.group();
							}	
						}else
						{
							//System.out.println("total income -"+ m.group(2));
							found="total income -"+ m.group(2);
						}	
					}	
				}
			}	
		}}
		catch(Exception e) {}
		
		try {
		if (StringUtils.isEmpty(found) ) {

			String revenueTotal = "((?<=revenu[ ]{0,2}total)[^\\d]*(\\d[ .,\\d]*))";
			p = Pattern.compile(revenueTotal);
			for (int i = 0; i < listLines.length; i++) {
				if (StringUtils.isEmpty(found)) {
					String currentLine = listLines[i];
					m = p.matcher(currentLine);
					while (m.find()) {
						if (currentLine.contains("$")) {
							Pattern p1 = Pattern.compile("([^\\d]*(\\d[ .,\\d]*)[$])");
							Matcher m1 = p1.matcher(currentLine);
							if (m1.find()) {
								// System.out.println("total income -"+ m1.group());
								found = "total income -" + m1.group().replace(" ", "");;
							}
						} else {
							// System.out.println("total income -"+ m.group(2));
							found = m.group(2).replace(",", ".").replace(" ", "");;
							found = "total income -" + found;
						}
					}
				}
			}
		}}
		catch(Exception e) {}
		
		
		return found;
	}
}
