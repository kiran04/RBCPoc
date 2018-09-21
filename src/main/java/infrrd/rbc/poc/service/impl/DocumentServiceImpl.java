package infrrd.rbc.poc.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import infrrd.rbc.poc.extractor.AmountDueExtractor;
import infrrd.rbc.poc.extractor.FullNameExtractor;
import infrrd.rbc.poc.extractor.IssuedDateExtractor;
import infrrd.rbc.poc.extractor.SocialInsuranceNumberExtractor;
import infrrd.rbc.poc.extractor.TaxAmountsExtractor;
import infrrd.rbc.poc.extractor.TaxYearExtractor;
import infrrd.rbc.poc.service.DocumentService;
import infrrd.rbc.poc.service.StorageService;
import infrrd.rbc.poc.utils.RefineUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService{

	
	@Autowired
	StorageService storageService;
	
	@Autowired
	SocialInsuranceNumberExtractor socialInsuranceNumberExtractor;
	@Autowired
	IssuedDateExtractor issuedDateExtractor;
	@Autowired
	FullNameExtractor fullNameExtractor;
	@Autowired
	TaxYearExtractor taxYearExtractor;
	@Autowired
	AmountDueExtractor amountDueExtractor;
	@Autowired
	TaxAmountsExtractor taxAmountsExtractor;
	@Autowired
	RefineUtils refineUtils;
	

	private String url;
	
	
	@Override
	public Map<String, String> processDocument(File file) throws IOException {
		// TODO Auto-generated method stub
		final File uploadedFile;
		uploadedFile = storageService.uploadFile(file);
		Map<String, String> values = getTextFromFiles(uploadedFile);
		
		//Map<String, String> values = getTextFromPDFUsingPoppler(uploadedFile);
		//Map<String, String> values = getTextLocally(uploadedFile);
		
		return values;
		
		
	}
	

	private Map<String, String> getTextLocally(File uploadedFile) throws IOException {

		String staticFolder = "/home/kiranc/Downloads/RBC_POV_Documents/all_pdfs/tesseractNeeded";
		String fileFolder = uploadedFile.getName().substring(0, uploadedFile.getName().lastIndexOf("."));
		File textFilesFolder = new File(staticFolder + "/" + fileFolder + "/" + fileFolder);
		Map<Integer, String> intAbsolutePathNames = new HashMap<Integer, String>();

		if (textFilesFolder.isDirectory()) {
			int numberOfPages = textFilesFolder.list(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches(".*[.]txt");
				}
			}).length;

			for (int i = 0; i < numberOfPages; i++) {
				intAbsolutePathNames.put(i + 1, textFilesFolder.getAbsolutePath() + "/page-" + (i + 1) + ".txt");
			}
		}

		BufferedReader br;

		StringBuilder sb = new StringBuilder("");

		StringBuilder sbonly1 = new StringBuilder("");

		
			for (int i = 0; i < intAbsolutePathNames.size(); i++) {
				try {
					br = new BufferedReader(new FileReader(intAbsolutePathNames.get(i + 1)));
					try {
						String line = br.readLine();
						while (line != null) {
							sb.append(line);
							sb.append("\n");
							if (i == 0) {
								sbonly1.append(line);
								sbonly1.append("\n");
							}
							line = br.readLine();
						}
					} finally {
						br.close();
					}
				} catch (IOException io) {

				}
			}
		
		String refinedWordsAll = refineUtils.refineThis(sb.toString());
		String refinedWordsFirstPage = refineUtils.refineThis(sbonly1.toString());
		Map<String, String> allTaxRelatedStuff = extractFields(refinedWordsAll, refinedWordsFirstPage);
		return allTaxRelatedStuff;

	}


	private Map<String, String> getTextFromFiles(File uploadedFile) throws IOException {

		Map<Integer, String> allFiles = getTextFromPythonApp(uploadedFile);
		
		String parentDirectoryPlusPDFTextDirectory = uploadedFile.getParentFile().getAbsolutePath() + "/"
				+ uploadedFile.getName().substring(0, uploadedFile.getName().lastIndexOf("."));

		BufferedReader br;

		StringBuilder sb = new StringBuilder("");

		StringBuilder sbonly1 = new StringBuilder("");
		try {
		if (allFiles.size() > 0) {
			for (int i = 0; i < allFiles.size(); i++) {
				try {
					br = new BufferedReader(new FileReader(allFiles.get(i + 1)));
					try {
						String line = br.readLine();
						while (line != null) {
							sb.append(line);
							sb.append("\n");
							if (i == 0) {
								sbonly1.append(line);
								sbonly1.append("\n");
							}
							line = br.readLine();
						}
					} finally {
						br.close();
					}
				} catch (IOException io) {
					
				}
			}
		}
		
		
		if(StringUtils.isEmpty(sb.toString())){
				throw new Exception(
						"No text found in all text files in the directory sent to python app.The directory path is "
								+ parentDirectoryPlusPDFTextDirectory);
			
		}}
		catch(Exception e) {
			log.info(e.toString());
		}
		String refinedWordsAll = refineUtils.refineThis(sb.toString());
		String refinedWordsFirstPage = refineUtils.refineThis(sbonly1.toString());
		Map<String, String> allTaxRelatedStuff = extractFields(refinedWordsAll, refinedWordsFirstPage);
		return allTaxRelatedStuff;

	}
	
	private Map<String, String> extractFields(String getText, String getTextPage1) throws IOException {

		String SocialInsuranceNumber = socialInsuranceNumberExtractor.extract(getTextPage1);

		String issuedDate = issuedDateExtractor.extract(getTextPage1);

		String fullName = fullNameExtractor.extract(getTextPage1);

		String taxYear = taxYearExtractor.extract(getTextPage1);

		String amountDueBalanceRefund = amountDueExtractor.extract(getText);

		Map<String, String> allTaxRelatedStuff = taxAmountsExtractor.extract(getText);

		if (StringUtils.isEmpty(SocialInsuranceNumber))
			allTaxRelatedStuff.put("SIN", "");
		else
			allTaxRelatedStuff.put("SIN", SocialInsuranceNumber);

		if (StringUtils.isEmpty(issuedDate))
			allTaxRelatedStuff.put("Issued Date", "");
		else
			allTaxRelatedStuff.put("Issued Date", issuedDate);
		if (StringUtils.isEmpty(fullName))
			allTaxRelatedStuff.put("Full Name", "");
		else
			allTaxRelatedStuff.put("Full Name", fullName);

		if (StringUtils.isEmpty(taxYear))
			allTaxRelatedStuff.put("Tax Year", "");
		else
			allTaxRelatedStuff.put("Tax Year", taxYear);
		if (!StringUtils.isEmpty(amountDueBalanceRefund)) {
			if (amountDueBalanceRefund.contains("--")) {
				allTaxRelatedStuff.put("Balance", "-" + taxAmountsExtractor.refine(amountDueBalanceRefund.split("-")[2]));

			} else
				allTaxRelatedStuff.put(amountDueBalanceRefund.split("-")[0], taxAmountsExtractor.refine(amountDueBalanceRefund.split("-")[1]));
		} else {
			allTaxRelatedStuff.put("Balance", "");
		}

		return allTaxRelatedStuff;
	}

	
	private Map<Integer, String> getTextFromPythonApp(File uploadedFile) {

		url = "http://127.0.0.1:8090/extract/data";
		Map<Integer, String> intAbsolutePathNames = new HashMap<Integer, String>();

		String parentDirectoryPlusPDFTextDirectory = uploadedFile.getParentFile().getAbsolutePath() + "/"
				+ uploadedFile.getName().substring(0, uploadedFile.getName().lastIndexOf("."));
		Map<String, String> requestBody = new HashMap<String, String>();
		requestBody.put("pdf_doc_location", uploadedFile.getAbsolutePath());
		requestBody.put("page_dir_location", parentDirectoryPlusPDFTextDirectory);

		JSONObject someMap = new JSONObject(requestBody);

		RestTemplate template = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setCacheControl("no-cache");
		HttpEntity<String> entity = new HttpEntity<String>(someMap.toString(), headers);
		try {
			ResponseEntity<String> response = template.exchange(this.url, HttpMethod.POST, entity, String.class);

			if (response.getStatusCode().equals(HttpStatus.OK)) {

				File textFilesFolder = new File(parentDirectoryPlusPDFTextDirectory);

				if (textFilesFolder.isDirectory()) {
					int numberOfPages = textFilesFolder.list(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.matches(".*[.]txt");
						}
					}).length;

					// String[] fileNames =

					for (int i = 0; i < numberOfPages; i++) {
						intAbsolutePathNames.put(i + 1,
								parentDirectoryPlusPDFTextDirectory + "/page-" + (i + 1) + ".txt");
					}
				}
			}
			else {
				throw new Exception("Couldnt connect to python app.Received status code - "+response.getStatusCode());
			}
			
			if (intAbsolutePathNames.size() == 0) {

				throw new Exception("No text files present in the directory file path sent to python app.Path is "
						+ parentDirectoryPlusPDFTextDirectory);

			}
		}
		catch(Exception e) {
			log.info(e.toString());
		}
		
		// }

		return intAbsolutePathNames;
	}

	private Map<String, String> getTextFromPDFUsingPoppler(File uploadedFileuploadedFile) throws IOException {

		String fileName = uploadedFileuploadedFile.getAbsolutePath();
		File textfile = new File(fileName.substring(0, fileName.lastIndexOf(".")) + ".txt");
		String cmd = "pdftotext -layout " + uploadedFileuploadedFile.getAbsolutePath() + " "
				+ textfile.getAbsolutePath();

		try {
			Process process = Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		BufferedReader br;

		br = new BufferedReader(new FileReader(textfile));

		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}

			String refinedWords = refineUtils.refineThis(sb.toString());

			return extractFields(refinedWords, refinedWords);
		} finally {
			br.close();
		}

	}
}
