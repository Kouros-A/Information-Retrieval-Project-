package org.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class PositionTrackingFileWriter extends Writer {
    private FileWriter writer;
    private long currentPosition;

    public PositionTrackingFileWriter(File filename) throws IOException {
        writer = new FileWriter(filename);
        currentPosition = 0;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
        currentPosition += len;
    }

    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

}
