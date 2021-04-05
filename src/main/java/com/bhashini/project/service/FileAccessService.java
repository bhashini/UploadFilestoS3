package com.bhashini.project.service;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;

@Service
public class FileAccessService {

	private AmazonS3 s3Client;

	@Value("${amazonProperties.bucketName}")
	private String bucketName;
	@Value("${amazonProperties.accessKey}")
	private String accessKey;
	@Value("${amazonProperties.secretKey}")
	private String secretKey;

	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials awscredintials = new BasicAWSCredentials(this.accessKey, this.secretKey);

		s3Client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awscredintials))
				.withRegion(Regions.CA_CENTRAL_1).build();
	}

	public void uploadFile(MultipartFile multipartFile) throws IOException {
		File file = convertMultiPartToFile(multipartFile);
		String fileName = generateFileName(multipartFile);

		s3Client.putObject(new PutObjectRequest(this.bucketName, fileName, file));
		System.out.println("File upload is successful");

	}
	
	public void tagFile(String tag) throws IOException{
		
	}

	public ResponseEntity<ByteArrayResource> downloadFile(String fileName) throws IOException {
		byte[] content = null;
		final S3Object s3Object = this.s3Client.getObject(this.bucketName, fileName);
		final S3ObjectInputStream stream = s3Object.getObjectContent();
		try {
			content = IOUtils.toByteArray(stream);
			s3Object.close();
		} catch (IOException ex) {
			System.out.println("IO Error message: " + ex);
		}

		final ByteArrayResource resource = new ByteArrayResource(content);
		return ResponseEntity.ok().contentLength(content.length).header("Content-type", "application/octet-stream")
				.header("Content-disposition", "attachment; filename=\"" + fileName + "\"").body(resource);
	}

	public void deleteFile(String filename) {
		try {
			s3Client.deleteObject(this.bucketName, filename);
		} catch (AmazonServiceException ex) {
			System.out.println(ex.getErrorMessage());
			System.exit(1);
		}
	}

	public List<String> listFile() {
		List<String> keys = new ArrayList<>();
		ObjectListing ol = s3Client.listObjects(this.bucketName);
		List<S3ObjectSummary> files = ol.getObjectSummaries();
		for (S3ObjectSummary os : files) {
			keys.add(os.getKey());
		}
		return keys;
	}

	public String generateFileName(MultipartFile multiPart) {
		return new Date().getTime() + "-" + multiPart.getOriginalFilename().replace(" ", "_");
	}

	public File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}

}
