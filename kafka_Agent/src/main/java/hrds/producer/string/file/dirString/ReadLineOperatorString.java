package hrds.producer.string.file.dirString;

import hrds.producer.common.FileDataValidator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class ReadLineOperatorString {

	public void readLine(String filePath) {

		RandomAccessFile readFile = null;
		String charset = null;
		try {
			readFile = new RandomAccessFile(new File(filePath), "r");
			StringBuilder lineBuffer = new StringBuilder();
			while( true ) {
				String line = readFile.readLine();
				if( line != null ) {
					if( isNewLine(line) ) {
						if(lineBuffer.length() < 1){
							lineBuffer.append(line);
						}else{
//							"ISO-8859-1"
							String message = new String(lineBuffer.toString().getBytes("ISO-8859-1"), charset);
							System.out.println(message);
							//messages转GenericRecord 进kafka 
							//记录文件读取位置
						}
					}else{
						lineBuffer.append("\n").append(line);
					}
				}else{
					break;
				}
			}
		}
		catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isNewLine(String line) {

		FileDataValidator fileDataValidator = null;
		String filedataValidatorImplClassName = "";
		if( !filedataValidatorImplClassName.isEmpty() ){
			Class<?> clazz;
			try {
				clazz = Class.forName(filedataValidatorImplClassName);
				fileDataValidator = (FileDataValidator)clazz.newInstance();
			}
			catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		if( fileDataValidator.isNewLine(line)) {
			return fileDataValidator.isNewLine(line);
		}else{
			return true;
		}
	}

}
