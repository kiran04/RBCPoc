package infrrd.rbc.poc.extractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = { "classpath:project.properties" })
public class FullNameExtractor {

	@Value("${lastNamesList}")
	private String lastNamesList;
	@Value("${stateCodes}")
	private String stateCodes;
	
	Properties prop;
	
	public String extract(String getText) {
		
		
		prop = new Properties();
		InputStream input = null;

		/*try {
		    input = new FileInputStream("classpath:project.properties");
		    prop.load(input);
		} catch (IOException ex) {
		    ex.printStackTrace();
		} finally {
		    try {
				input.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
		
		
		return tryAllWays(getText);
		
		
		
	}

	@SuppressWarnings("deprecation")
	private String tryAllWays(String getText) {
		
		String tempOutput = "";
		tempOutput = getByaddressKeyword(getText);
		
		if(StringUtils.isEmpty(tempOutput))
			tempOutput = getByLastName(getText);
		
		if(StringUtils.isEmpty(tempOutput))
		tempOutput = getByStateCodeCity(getText);
		
				
		
		
		
		
		return WordUtils.capitalize(tempOutput);
		
	}

	private String getByStateCodeCity(String getText) {
		
		if(getText.toLowerCase().contains("address"))
			return "";
			
		List<String> stateCodeList  =Arrays.asList(stateCodes.split("\\|"));
		
		List<String> foundStates= new ArrayList<String>();
		String found = "";
		try {
		for(String s:stateCodeList) {
			if(!found.equals(""))
				break;
			String cityTownVillageregex = "[, ]"+s+"[ ]{0,1}";
			
			Pattern p =Pattern.compile(cityTownVillageregex);
			Matcher m =p.matcher(getText);
			
			
			while (m.find()) {
				if(!found.equals(""))
					break;
				int indexfound = m.start();

				String currentline = getText.substring(0, indexfound);

				currentline = currentline.substring(currentline.lastIndexOf("\n")+1);
				
				if(!currentline.toLowerCase().contains("province")) {
					/*
					 * String cityTownVillageregex = prop.getProperty(s+"cities");
					 * 
					 * Pattern p =Pattern.compile(cityTownVillageregex); Matcher m
					 * =p.matcher(currentline.toLowerCase());
					 */
					int indexOfState = currentline.length() - 1;
	
					String previousLine = getText.substring(0, getText.indexOf(currentline));
					
					String lastLasttwoCharacters = previousLine.substring(previousLine.length()-2,previousLine.length());
					
					if(lastLasttwoCharacters.equals("\n\n")) {
						continue;
						
					}
					//System.out.println(lastLasttwoCharacters);
	
					previousLine = previousLine.substring(0, previousLine.lastIndexOf("\n"));
					
					previousLine = previousLine.substring(0, previousLine.lastIndexOf("\n"));
	
					previousLine = previousLine.substring(previousLine.lastIndexOf("\n") + 1);
	
					//System.out.println(previousLine);
	
					if (previousLine.length() < indexOfState) {
						
						indexOfState = previousLine.length()-1;
						
						StringBuffer sb = new StringBuffer(previousLine.charAt(indexOfState) + "");
						
						int iLeft = indexOfState - 1;
	
						while (iLeft > -1) {
	
							CharSequence current = previousLine.charAt(iLeft) + "";
							if (current.equals(" ")) {
								int ileftMinus3 = iLeft - 3;
								if (ileftMinus3 < -1) {
									ileftMinus3 = 0;
								}
	
								String leftThreeSpaces = previousLine.substring(ileftMinus3, iLeft);
	
								if (leftThreeSpaces.equals("   ")) {
									// nothing to insert
									break;
								} else {
									sb.insert(0, current + "");
									iLeft--;
								}
							} else {
								sb.insert(0, current + "");
								iLeft--;
							}
						}
						//System.out.println(sb);
						found = sb.toString();
	
					} else {
	
						// move towards right
						StringBuffer sb = new StringBuffer(previousLine.charAt(indexOfState) + "");
						int lenPreviousLine = previousLine.length();
	
						int iRight = indexOfState + 1;
	
						while (iRight <= lenPreviousLine - 1) {
	
							CharSequence current = previousLine.charAt(iRight) + "";
							if (current.equals(" ")) {
								// check next 3 indexes
								int iRightplus3 = iRight + 3;
								if (iRightplus3 > lenPreviousLine)
									iRightplus3 = lenPreviousLine;
								if(iRight+1>lenPreviousLine-1) {
									continue;
								}
								String rightThreeSpaces = previousLine.substring(iRight + 1, iRightplus3 + 1);
								if (rightThreeSpaces.equals("   ")) {
									// nothing to insert
									break;
								} else {
									sb.append(current + "");
									iRight++;
								}
	
							} else {
								sb.append(current + "");
								iRight++;
							}
	
						}
	
						int iLeft = indexOfState - 1;
	
						while (iLeft > -1) {
	
							CharSequence current = previousLine.charAt(iLeft) + "";
							if (current.equals(" ")) {
								int ileftMinus3 = iLeft - 3;
								if (ileftMinus3 < -1) {
									ileftMinus3 = 0;
								}
	
								String leftThreeSpaces = previousLine.substring(ileftMinus3, iLeft);
	
								if (leftThreeSpaces.equals("   ")) {
									// nothing to insert
									break;
								} else {
									sb.insert(0, current + "");
									iLeft--;
								}
							} else {
								sb.insert(0, current + "");
								iLeft--;
							}
						}
						//System.out.println(sb);
						found = sb.toString();
					}
				}	
				else
				{
					continue;
					
				}
			}
			
			
			
			
		}
		}
		catch (Exception e) {}
		return found;
	}

	private String getByaddressKeyword(String getText) {
		
		getText = getText.toLowerCase();
		String refinedOne = "";
		int indexOfAddress = getText.indexOf("address");
		try {
		if (indexOfAddress > -1) {
			int endIndex = getText.indexOf("\n", indexOfAddress);

			String addressLine = getText.substring(indexOfAddress, endIndex);

			// System.out.println(addressLine);

			String addressKeywordNameRegex = "((?<=address).{0,25}([a-z]*+)[ ]{1,2}([a-z]*+)[ ]{0,2}([a-z]*+)[ ]{0,20})";

			Pattern p = Pattern.compile(addressKeywordNameRegex);
			Matcher m = p.matcher(addressLine);
			

			if (m.find()) {
				refinedOne = m.group().replaceAll("[^a-z ]", "");
				refinedOne = refinedOne.replaceAll("[ ]{2}", "");
				// System.out.println(refinedOne);
			}}
		}
		catch(Exception e) {
			
		}
		return refinedOne;
	}

	private String getByLastName(String getText) {
		
		getText = getText.toLowerCase();
		if(getText.contains("address"))
			return "";
		String[] namesIndividual = lastNamesList.split("\\|");
		String refinedOne ="";
		
		String toBeConsidered = "";
		int index = Integer.MAX_VALUE;
		try {
			for (String eachOne : namesIndividual) {

				int foundIndex = getText.indexOf(eachOne);
				if (foundIndex != -1 && foundIndex < index) {
					String nameRegex1 = "[ ]{0,4}[^a-z]+" + eachOne + "[^a-z]+[ ]{0,4}";

					Pattern p1 = Pattern.compile(nameRegex1);
					Matcher m1 = p1.matcher(getText);
					if (m1.find()) {
						index = foundIndex;
						toBeConsidered = eachOne;
					}
				}
			}
			if (index == Integer.MAX_VALUE) {

				return "";
			}

			int endIndex = getText.indexOf("\n", index);
			int firstIndex = getText.substring(0, index).lastIndexOf("\n");

			String extractedLine = getText.substring(firstIndex + 1, endIndex);

			String nameRegex = "[ ]{0,4}[a-z ]*" + toBeConsidered + "[a-z ]*[ ]{0,4}";

			Pattern p = Pattern.compile(nameRegex);
			Matcher m = p.matcher(extractedLine);

			if (m.find()) {

				if (m.group().contains("    ")) {
					String nameRegex1 = "[ ]{4}[a-z ]*" + toBeConsidered + "[a-z ]*[ ]{0,4}";
					p = Pattern.compile(nameRegex1);
					Matcher m1 = p.matcher(m.group());
					if (m1.find()) {
						refinedOne = m1.group();
						if (m1.group().contains("    ")) {
							String nameRegex2 = "[ ]{0,4}[a-z ]*" + toBeConsidered + "[a-z ]*[ ]{4}";
							p = Pattern.compile(nameRegex2);
							Matcher m3 = p.matcher(m1.group());
							if (m3.find()) {
								refinedOne = m3.group();
							}
						}
					}

				}
				refinedOne = m.group().replaceAll("[^a-z ]", "");
				refinedOne = refinedOne.replaceAll("[ ]{2}", "");
				// System.out.println(refinedOne);
			}

		}
		catch(Exception e) {
			
		}
	
		
		return refinedOne;
	}

}
