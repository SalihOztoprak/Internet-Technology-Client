import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NonblockingBufferedReader {
    private final BlockingQueue<String> lines = new LinkedBlockingQueue<>();
    private volatile boolean closed = false;
    private Thread backgroundReaderThread;

    public NonblockingBufferedReader(final BufferedReader bufferedReader) {
        backgroundReaderThread = new Thread(() -> {
            try {
                while (!Thread.interrupted()) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    lines.add(line);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                closed = true;
            }
        });
        backgroundReaderThread.setDaemon(true);
        backgroundReaderThread.start();
    }

    public String readLine() throws IOException {
        try {
            return (closed) && (lines.isEmpty()) ? null : lines.poll(500L, java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new IOException("The BackgroundReaderThread was interrupted!", e);
        }
    }

    public void close() {
        if (backgroundReaderThread != null) {
            backgroundReaderThread.interrupt();
            backgroundReaderThread = null;
        }
    }
}
