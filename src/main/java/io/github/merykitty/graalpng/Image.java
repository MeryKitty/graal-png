package io.github.merykitty.graalpng;

import java.io.*;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

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

    /**
     * Read the pixel of an image, the return value is an int which is represent the colour value of the
     * pixel in ARGB order
     *
     * @param row The row of the pixel
     * @param col The column of the pixel
     * @return The colour value of the pixel, in ARGB order
     */
    public int getPixel(int row, int col) {
        Objects.checkIndex(row, height);
        Objects.checkIndex(col, width);
        return data[row * width + col];
    }

    /**
     * Put a colour value to a pixel at the specified position
     *
     * @param row The row of the pixel
     * @param col The column of the pixel
     * @param value The colour value of the pixel, in ARGB order
     */
    public void putPixel(int row, int col, int value) {
        Objects.checkIndex(row, height);
        Objects.checkIndex(col, width);
        data[row * width + col] = value;
    }

    /**
     * Get the width of the image
     *
     * @return The width of the image
     */
    public int width() {
        return width;
    }

    /**
     * Get the height of the image
     *
     * @return The height of the image
     */
    public int height() {
        return height;
    }

    /**
     * Create an blank image with all pixel set to completely transparent
     *
     * @param width The width of the created image
     * @param height The height of the create image
     * @return The created image
     */
    public static Image createImage(int width, int height) {
        int[] data = new int[width * height];
        return new Image(width, height, data);
    }

    /**
     * Serialise an image to a file specified by path
     *
     * @param path The path to the output file
     * @throws IOException
     */
    public void writeImage(Path path) throws IOException {
        try {
            var pathBuffer = Arrays.copyOf(path.toString().getBytes(StandardCharsets.US_ASCII), path.toString().length());
            PNG_WRITE.executeVoid(pathBuffer, pathBuffer.length, this.width, this.height, this.data, ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Read an image from disk
     *
     * @param path The path to the input file
     * @return The image
     * @throws IOException
     */
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
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
