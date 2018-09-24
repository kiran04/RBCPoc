package infrrd.rbc.poc.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import infrrd.rbc.poc.RbcPOCApplication;
import infrrd.rbc.poc.service.StorageService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {
	
	@Override
	public File uploadFile(File file) {
		// TODO Auto-generated method stub

		// Save file
		final String targetPath = RbcPOCApplication.DOCUMENTS_DIRECTORY + File.separator +file.getName().replace(" ", "") ;
		final File targetFile = new File(targetPath);
		try {
			targetFile.createNewFile();
			final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
			final BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
			FileCopyUtils.copy(inputStream, outputStream);
			outputStream.close();
			inputStream.close();
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
		log.info("File saved successfully at temp location: " + targetFile.getAbsolutePath());

		return targetFile;
	
	}

	@Override
	public File uploadFile(MultipartFile file) {
		final String targetPath = RbcPOCApplication.DOCUMENTS_DIRECTORY + File.separator +file.getOriginalFilename().replace(" ", "") ;
		final File targetFile = new File(targetPath);
		try {
			targetFile.createNewFile();
			final BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
			final BufferedInputStream inputStream = new BufferedInputStream(file.getInputStream());
			FileCopyUtils.copy(inputStream, outputStream);
			outputStream.close();
			inputStream.close();
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
		log.info("File saved successfully at temp location: " + targetFile.getAbsolutePath());

		return targetFile;
	}

}
