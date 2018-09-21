package infrrd.rbc.poc;

import java.io.File;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@SpringBootApplication
public class RbcPOCApplication {
	
	public static final String ROOT = "upload-dir";
	public static final String DOCUMENTS_DIRECTORY = "upload-dir" + File.separator + "documents";

	public static void main(String[] args) {
		SpringApplication.run(RbcPOCApplication.class, args);

	}
	
	@Bean
	CommandLineRunner init() {
		return (String[] args) -> {
			new File(RbcPOCApplication.ROOT).mkdir();
			new File(RbcPOCApplication.DOCUMENTS_DIRECTORY).mkdir();
		};
	}
	
	

}
