package hrds.producer.common;

public interface FileDataValidator
{
	boolean isNewLine(String lineText);
	boolean isSkipLine(String lineText);
}
