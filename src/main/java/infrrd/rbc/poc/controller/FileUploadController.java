package infrrd.rbc.poc.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import infrrd.rbc.poc.service.DocumentService;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping(path = "/api/rbc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class FileUploadController {
	
	@Autowired
	DocumentService documentService;
	
	@RequestMapping("/noa")
	@PostMapping()
	public ResponseEntity<?> newDocumentAgain(@RequestBody Map<String,String> requestBody) throws IOException, JSONException {
		File file =  new File(requestBody.get("file"));
		
				
		if ( "pdf".equalsIgnoreCase(FilenameUtils.getExtension(file.getName()))) {
			
			Map<String, String> output = documentService.processDocument(file);
			log.info("Processing document with filename {}", file.getName());
			
			File CSVFile= new File(file.getParentFile().getAbsolutePath()+"/"+"ExtractedOutput.csv");
			PrintWriter pw = new PrintWriter(CSVFile);
			createCSV(pw); 
			appendValues (file.getName(),pw , output);
			pw.close();
			Map<String, String> csvFilePath = new HashMap<String, String>();
			csvFilePath.put("Extracted values CSV Path", CSVFile.getAbsolutePath());
			
			log.info("<--------------------------COMPLETED REQUEST------------------->");
			log.info("Extracted values are put in the CSV with pathName- "+ CSVFile.getAbsolutePath());
			return new ResponseEntity<String>(new JSONObject(csvFilePath).toString(), HttpStatus.OK);
		} 
		else if (file.isDirectory()) {
			
			log.info("<------------------START-------------------->");
			log.info("*********************************************");
			log.info("Processing files in directory with name {}", file.getAbsolutePath());

			File[] allPDFs = file.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.matches(".*[.]pdf");
				}
			} );
		
			log.info("Total number of PDF files in the given directory are "+ allPDFs.length);
			log.info("Estimated time to complete the extractions is "+ allPDFs.length+" mins.");
			
			
			
			File CSVFile= new File(file.getAbsolutePath()+"/"+"ExtractedOutput.csv");
			PrintWriter pw = new PrintWriter(CSVFile);
			
			createCSV(pw);
			
			for(int i =0;i<allPDFs.length;i++) {
				
				log.info("<----------------Individual File Starts--------------->");
				log.info("Processing PDF with filename {}", allPDFs[i].getName());
				Map<String, String> output = documentService.processDocument(allPDFs[i]);
				
				log.info("The Extracted values are "+new JSONObject(output).toString());
				appendValues (allPDFs[i].getName(),pw , output);
				
				log.info("<----------------Individual File Ends--------------->");
				
				
			}
			pw.close();
			Map<String, String> csvFilePathJson = new HashMap<String, String>();
			log.info("*****************************************************");
			log.info("*****************************************************");
			log.info("*****************************************************");
			log.info("********************ALL DONE*************************");
			log.info("The extracted values are in the CSV file path - "+CSVFile.getAbsolutePath());
			log.info("<--------------------------COMPLETED REQUEST------------------->");
			
			csvFilePathJson.put("Extracted values CSV Path- ", CSVFile.getAbsolutePath());
			return new ResponseEntity<String>(new JSONObject(csvFilePathJson).toString(), HttpStatus.OK);
		}

		else {				
			log.info("File of invalid type: ", file.getName());
			return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
		}
	}

	private void appendValues(String fileName, PrintWriter pw, Map<String, String> output) throws FileNotFoundException {
		
        StringBuilder sb = new StringBuilder();
        
        String[] allParameters = {"SIN" , "Issued Date","Full Name","Tax Year","Amount Due/Balance","Refund","Total Income","Net Income",
        		"Taxable Income","Total Payable(Tax)","Total Credits(Tax)" , "Payable minus Credits(Net Tax)","Income Tax"};
        
        sb.append("\n\n\n\n");
        sb.append(fileName);
        for(int i = 0 ;i<allParameters.length;i++) {
	        sb.append(',');
	        sb.append(allParameters[i]);
	        sb.append(',');
	        if(allParameters[i].equals("Amount Due/Balance") || allParameters[i].equals("Refund") ) {
	        	if(allParameters[i].equals("Amount Due/Balance")) {
	        		if(output.containsKey("amount due"))
	        			sb.append("\""+output.get("amount due")+"\"");
	        		else if(output.containsKey("balancevalue"))
	        			sb.append("\""+output.get("balancevalue")+"\"");
	        		else if (output.containsKey("Balance"))
	        			sb.append("\""+output.get("Balance")+"\"");
	        		else
	        			sb.append("");		
	        	}
	        	else {
	        		if(output.containsKey("refund"))
	        			sb.append("\""+output.get("refund")+"\"");
	        		else
	        			sb.append("");
	        	}	
	        }
	        else {
	        	if(output.containsKey(allParameters[i]))
	        		sb.append("\""+output.get(allParameters[i])+"\"");
	        	else
	        		sb.append("");  	
	        }
	        sb.append('\n');
        }
        pw.append(sb.toString());
        pw.flush();
        
		
	}

	private void createCSV(PrintWriter pw2) throws FileNotFoundException {
		
        StringBuilder sb = new StringBuilder();
        sb.append("File Name");
        sb.append(',');
        sb.append("Key Name");
        sb.append(',');
        sb.append("Value");
        sb.append('\n');
        pw2.append(sb.toString());

		
	}

}
