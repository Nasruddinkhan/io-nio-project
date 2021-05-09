package com.mypractice.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import com.mypractice.IoClient;
import com.mypractice.files.FilesExporter;

public class ReadCsvFile implements Runnable {
	private List<String> listOfFolderDirectory;
	private FilesExporter filesExporter;

	public ReadCsvFile(final List<String> listOfFolderDirectory, final FilesExporter filesExporter) {
		this.listOfFolderDirectory = listOfFolderDirectory;
		this.filesExporter = filesExporter;

	}

	@Override
	public void run() {
		listOfFolderDirectory.forEach(obj -> {
			try {
				List<File> files = Files.list(Paths.get(obj)).map(Path::toFile)
						.filter(o -> IoClient.findExtension(o.getName()).equals("csv")).collect(Collectors.toList());
				filesExporter.readAndCopy(files);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

}
