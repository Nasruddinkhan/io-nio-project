package com.mypractice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.mypractice.files.FilesExporter;
import com.mypractice.handler.ReadCsvFile;
import com.mypractice.handler.ReadJsonFile;
import com.mypractice.handler.ReadTxtFile;

public class IoClient {
	private static final String SLASH = "\\";
	static List<String> exList = new ArrayList<>();
	static {
		exList.add(".json");
		exList.add(".csv");
		exList.add(".txt");

	}
	static Supplier<String> getExtension = () -> exList.get(new Random().nextInt(exList.size()));
	static Function<String, String> directotyFunction = os -> os.contains("Windows") ? "C://" : "opt/";

	public static void main(String[] args) throws IOException {

		List<String> listOfFolders = createDirectory(5);
		String cantainFolders = createDirectory(5).stream().collect(Collectors.joining("/"));
		String osName = System.getProperty("os.name");
		String filePath = directotyFunction.apply(osName) + cantainFolders;
		Path path = Paths.get(filePath);
		if (!Files.exists(path)) {
			Files.createDirectories(path);
		}
		List<String> listOfFolderDirectory = getFolderList(listOfFolders, path.toString());
		Supplier<String> supplierDir = () -> listOfFolderDirectory
				.get(new Random().nextInt(listOfFolderDirectory.size()));
		List<String> fileList = createFileList(supplierDir);
		List<File> files = FilesExporter.createFiles(fileList);
		System.out.println("files [" + files.size() + "]");
		FilesExporter filesExporter = new FilesExporter();

		// It's group oapproach with java 8
		/*
		 * Map<String, List<File>> getFroupFiles = getFileGroup(files);
		 * readUsingThread(filesExporter, getFroupFiles);
		 */
		
		//readUsingThreadClass(listOfFolderDirectory,  filesExporter);
		readUsingExecutorService (listOfFolderDirectory,  filesExporter);

	}

	private static void readUsingExecutorService(List<String> listOfFolderDirectory, FilesExporter filesExporter) {
		// TODO Auto-generated method stub
		//ExecutorService executor = Executors.newSingleThreadExecutor();
		ExecutorService executor = Executors.newFixedThreadPool(3);

		executor.submit(() -> {
			listOfFolderDirectory.forEach(obj -> {
				try {
					List<File> files = Files.list(Paths.get(obj)).map(Path::toFile)
							.filter(o -> IoClient.findExtension(o.getName()).equals("csv"))
							.collect(Collectors.toList());
					filesExporter.readAndCopy(files);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		    System.out.println("END CSV "+ Thread.currentThread().getName());

		});
		executor.submit(() -> {
		    System.out.println("START TXT ; "+ Thread.currentThread().getName());
			listOfFolderDirectory.forEach(obj -> {
				try {
					List<File> files = Files.list(Paths.get(obj)).map(Path::toFile)
							.filter(o -> IoClient.findExtension(o.getName()).equals("txt"))
							.collect(Collectors.toList());
					filesExporter.readAndCopy(files);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		    System.out.println("END TXT : "+ Thread.currentThread().getName());

		});
		executor.submit(() -> {
		    System.out.println("START JSON : "+ Thread.currentThread().getName());
			listOfFolderDirectory.forEach(obj -> {
				try {
					List<File> files = Files.list(Paths.get(obj)).map(Path::toFile)
							.filter(o -> IoClient.findExtension(o.getName()).equals("json"))
							.collect(Collectors.toList());
					filesExporter.readAndCopy(files);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		    System.out.println("START END : "+ Thread.currentThread().getName());
		});
		
		executor.shutdown();
	}

	private static void readUsingThreadClass(List<String> listOfFolderDirectory, FilesExporter filesExporter) {
		// TODO Auto-generated method stub
		ReadCsvFile csvFile = new ReadCsvFile( listOfFolderDirectory,  filesExporter);
		Thread csvFileThread = new Thread(csvFile);
		csvFileThread.start();
		
		ReadJsonFile jsonFile = new ReadJsonFile(listOfFolderDirectory, filesExporter);
		Thread jsonFileThread = new Thread(jsonFile);
		jsonFileThread.start();

		ReadTxtFile readTxtFile = new ReadTxtFile(listOfFolderDirectory, filesExporter);
		Thread readTxtFileThread = new Thread(readTxtFile);
		readTxtFileThread.start();
	}

	private static void readUsingThread(FilesExporter filesExporter, Map<String, List<File>> getFiles) {
		Runnable jsonThread = () -> {
			filesExporter.readAndCopy(getFiles.get("json"));

		};
		Runnable csvThRunnable = () -> {
			filesExporter.readAndCopy(getFiles.get("csv"));
		};
		Runnable txtThread = () -> {
			filesExporter.readAndCopy(getFiles.get("txt"));
		};
		Thread thread1 = new Thread(jsonThread);
		thread1.start();

		Thread thread2 = new Thread(csvThRunnable);
		thread2.start();

		Thread thread3 = new Thread(txtThread);
		thread3.start();
	}

	private static Map<String, List<File>> getFileGroup(List<File> fileList) {
		return fileList.parallelStream()
				.collect(Collectors.groupingBy(s -> findExtension(s.getAbsoluteFile().getName())));
	}

	public static String findExtension(String fileName) {
		int lastIndex = fileName.lastIndexOf('.');
		if (lastIndex == -1) {
			return "";
		}
		return Optional.of(fileName.substring(lastIndex + 1)).get();
	}

	private static List<String> createFileList(Supplier<String> supplierDir) {
		List<String> fileList = new ArrayList<>();
		IntStream.range(0, 50).forEach(obj -> {
			fileList.add(supplierDir.get() + IoClient.SLASH + String.format("%s%s", System.currentTimeMillis(),
					new Random().nextInt(100000) + "_file" + getExtension.get()));

		});
		return fileList;
	}

	private static List<String> getFolderList(List<String> listOfFolders, String pathString) {
		return listOfFolders.parallelStream()
				.map(folder -> pathString.substring(0, pathString.lastIndexOf(folder) + folder.length()).toString())
				.collect(Collectors.toList());
	}

	private static List<String> createDirectory(int sequence) {
		List<String> lsiList = new ArrayList<>();
		IntStream.range(0, sequence).forEach(obj -> {
			lsiList.add("Folder_" + obj);
		});
		return lsiList;
	}
}
