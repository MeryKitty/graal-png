package io.github.merykitty.graalpng;

import java.io.IOException;
import java.nio.file.Path;

public class Test {
    public static void main(String[] args, long l) throws IOException {
        var image = Image.createImage(2000, 1000);
        int red = 0xffff0000;
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 2000; j++) {
                if (i % 100 <= 50) {
                    image.putPixel(i, j, red);
                }
            }
        }
        image.writeImage(Path.of("./data/test.png"));
    }

    public static void main(String[] args) throws IOException {
        var image = Image.readImage(Path.of("./data/test.png"));
        for (int row = 0; row < image.height(); row++) {
            for (int col = 0; col < image.width(); col++) {
                int pixel = image.getPixel(row, col);
                int alpha = pixel >>> 24;
                int red = (pixel >>> 16) & 0xff;
                int green = (pixel >>> 8) & 0xff;
                int blue = pixel & 0xff;
                red /= 2;
                green /= 2;
                blue /= 2;
                pixel = (alpha << 24) | (red << 16) | (green << 8) | blue;
                image.putPixel(row, col, pixel);
            }
        }
        image.writeImage(Path.of("./data/test1.png"));
    }
}
