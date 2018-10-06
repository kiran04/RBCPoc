package infrrd.rbc.poc.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

	Map<String, String> processDocumentwitoutUploading(File file, String fileExtension) throws IOException;
	
	Map<String, String> processDocument(File file, String fileExtension) throws IOException;
	
}
