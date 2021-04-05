package com.bhashini.project.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bhashini.project.service.FileAccessService;

@RestController
public class FileAccessController {

	@Autowired
	FileAccessService fileAccessService;

	@PostMapping("/uploadFile")
	public String uploadFile(@RequestPart(value = "file") MultipartFile file) throws IOException {
		this.fileAccessService.uploadFile(file);
		return "File Upload Successful";
	}

	@GetMapping("/downloadFile")
	public ResponseEntity<ByteArrayResource> downloadFile(@RequestPart(value = "filename") String fileName)
			throws IOException {
		return this.fileAccessService.downloadFile(fileName);
	}

	@DeleteMapping("/deleteFile")
	public String deleteFile(@RequestPart(value = "filename") String filename) {
		this.fileAccessService.deleteFile(filename);
		return "File Delete Successfully";
	}
	
	@GetMapping("/listFile")
	public List<String> listFile(){
		 return this.fileAccessService.listFile();
	}

}
