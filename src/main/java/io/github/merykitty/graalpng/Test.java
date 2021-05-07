package io.github.merykitty.graalpng;

import java.io.IOException;
import java.nio.file.Path;

public class Test {
    public static void main(String[] args, int l) {
        var image = Image.createImage(1000, 1000);
        int red = 0xffff0000;
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                if ((i + j) % 2 == 0) {
                    image.putPixel(i, j, red);
                }
            }
        }
        image.writeImage(Path.of("./data/test.png"));
    }

    public static void main(String[] args) throws IOException {
        var image = Image.readImage(Path.of("./data/test.png"));
        image.writeImage(Path.of("./data/test1.png"));
    }
}
