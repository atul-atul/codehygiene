package codehygiene.util;

import java.util.List;

import static java.lang.String.join;

public class FileLinesTuple {
	private String fileName;
	private List<String> lines;

	@Override
	public String toString() {
		return "\n{" +
					 "fileName='" + fileName + '\'' +
					 ", lines=\n" + join("\n\t",lines) +
					 '}';
	}

	public FileLinesTuple(String fileName, List<String> lines) {
		this.fileName = fileName;
		this.lines = lines;
	}
}
