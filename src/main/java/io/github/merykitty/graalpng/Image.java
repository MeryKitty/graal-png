package io.github.merykitty.graalpng;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

import org.graalvm.polyglot.*;

public class Image {
    private static final Context POLYGLOT_CONTEXT;
    private static final Value PNG_READ;
    private static final Value PNG_WRITE;

    static {
        try {
            POLYGLOT_CONTEXT = Context.newBuilder().allowAllAccess(true).build();
            var file = new File(Image.class.getResource("pngutils.so").toURI());
            var source = Source.newBuilder("llvm", file).build();
            var lib = POLYGLOT_CONTEXT.eval(source);
            PNG_READ = lib.getMember("png_read");
            PNG_WRITE = lib.getMember("png_write");
        } catch (Exception e) {
            throw new RuntimeException("Fail to load library", e);
        }
    }

    private final int width, height;
    private final int[] data;

    private Image(int width, int height, int[] data) {
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public int getPixel(int row, int col) {
        return data[col * width + col];
    }

    public void putPixel(int row, int col, int value) {
        data[row * width + col] = value;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public static Image createImage(int width, int height) {
        int[] data = new int[width * height];
        return new Image(width, height, data);
    }

    public void writeImage(Path path) {
        var pathBuffer = Arrays.copyOf(path.toString().getBytes(StandardCharsets.US_ASCII), path.toString().length());
        PNG_WRITE.executeVoid(pathBuffer, pathBuffer.length, this.width, this.height, this.data, ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
    }

    public static Image readImage(Path path) throws IOException {
        try (var inputStream = new DataInputStream(new FileInputStream(new File(path.toUri())))) {
            inputStream.readLong();
            inputStream.readLong();
            int width = inputStream.readInt();
            int height = inputStream.readInt();
            int[] data = new int[width * height];
            var pathBuffer = Arrays.copyOf(path.toString().getBytes(StandardCharsets.US_ASCII), path.toString().length());
            PNG_READ.executeVoid(pathBuffer, pathBuffer.length, width, height, data, ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
            return new Image(width, height, data);
        }
    }
}
