package com.mypractice.files;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.jooq.lambda.Unchecked;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypractice.IoClient;
import com.mypractice.UseDto;

public class FilesExporter {

	static List<UseDto> dtos = new ArrayList<>();
	static {
		dtos.add(new UseDto("Nasruddin", "9987353738"));
	}

	public static File exportFile(String fileName, Consumer<Writer> contentWriter) throws Exception {
		File file = new File(fileName);
		try (Writer writer = new FileWriter(file)) {
			contentWriter.accept(writer);
			return file;
		} catch (Exception e) {
			throw e;
		}
	}

	public static List<File> createFiles(List<String> files) {
		List<File> files2 = new ArrayList<>();
		try {
			files.forEach(obj -> {
				try {
					File file = FilesExporter.exportFile(obj, Unchecked.consumer(m -> writeDate(m, obj)));
					files2.add(file);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return files2;
	}

	private static void writeDate(Writer writer, String ext) throws IOException {
		// TODO Auto-generated method stub
		ext = IoClient.findExtension(ext);
		System.out.println("FilesExporter.writeDate()" + ext);
		if (ext.equals("txt")) {
			int no = (int)(Math.random()*(200-100+1)+100);  
			if(no%2 == 0) {
				dtos.stream().map(o -> o.getName() + "," + o.getMobileNo()).forEach(Unchecked.consumer(writer::write));
			}
		} else if (ext.equals("csv")) {
			writer.write("Name;Mobile\n");
			dtos.stream().map(o -> o.getName() + ";" + o.getMobileNo()).forEach(Unchecked.consumer(writer::write));

		} else {
			dtos.stream().map(o -> {
				try {
					return new ObjectMapper().writeValueAsString(o);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}
				return null;
			}).forEach(Unchecked.consumer(writer::write));
		}
	}
	public String readAndCopy(List<File> files)  {
		files.forEach(obj -> {
			String filePath = obj.getAbsolutePath();
			String content = null;
			try {
				content = Files.lines(Paths.get(filePath)).collect(Collectors.joining(System.lineSeparator()));
				Path path = Paths.get(obj.getParentFile() + "/quarantine_folder//");
				if (!Files.exists(path)) {
					Files.createDirectories(path);
				}
				System.out.println("FilesExporter.readAndCopy() content ["+content+"]");
				System.out.println(Objects.nonNull(content) && !content.isEmpty());
				if (Objects.nonNull(content) && !content.isEmpty()) {
					String target = path.toString() + "//" + obj.getName();
					Files.move(Paths.get(filePath), Paths.get(target));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		LocalDateTime endDateTime = LocalDateTime.now();
		return endDateTime.toString();
	}

}
