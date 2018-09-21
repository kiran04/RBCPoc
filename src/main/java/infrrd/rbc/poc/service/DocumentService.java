package infrrd.rbc.poc.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

	//String processDocument(MultipartFile file) throws IOException;
	
	Map<String, String> processDocument(File file) throws IOException;
	
}
