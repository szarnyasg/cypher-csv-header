package hu.bme.mit.cch;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

public class FileUtil {

    public static int countLines(String filePath) throws IOException {
        File file = new File(filePath);
        try (LineNumberReader reader = new LineNumberReader(new FileReader(file))) {
            // scan through the file
            while (reader.readLine() != null) {}
            return reader.getLineNumber();
        }
    }
}
