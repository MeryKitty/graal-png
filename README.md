# graal-png

A library which use GraalVM Truffle framework to serialise and deserialise PNG images through libpng.

## Structure

- Image.java: The class contains methods to interact with images such as read a pixel, write a pixel, read the image from disk, write the image to disk.
More detailed documentation can be found in this class.
- Test.java: A class which do simple read, write operation, for testing purposes.

## Example usage:

Reduce half the brightness of the image

'''
var image = Image.readImage(Path.of("path/to/input"));
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
image.writeImage(Path.of("path/to/output"));
'''
