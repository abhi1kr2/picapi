package net.tripletwenty.picapi;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bearbob on 30.08.19.
 */
public class Picture {
    public static final int WHITE = 16777215;
    public static final int BLACK = 0;

    private String name;
    private String filePath;
    private BufferedImage image;
    private int[][] pixelValues = null;

    public Picture (String filePath) throws MalformedURLException, IOException {
        URL sUrl = new File(filePath).toURI().toURL();
        this.image = ImageIO.read(sUrl);
        this.filePath = filePath;
        this.name = sUrl.getFile();
    }

    public BufferedImage getImage () {
        return this.image;
    }

    /**
     * Compare both pictures pixel by pixel
     * @param otherPicture The picture to compare the current picture to
     * @return True if both pictures are considered exactly equal
     */
    public boolean equals (Picture otherPicture) {
        return this.equals(otherPicture, new Position2D(0, 0));
    }

    /**
     * Compare both pictures pixel by pixel
     * @param otherPicture The picture to compare the current picture to
     * @param threshold The allowed threshold below which two pixels are considered equal
     * @return True if both pictures are considered equal considering the threshold
     */
    public boolean equals (Picture otherPicture, float threshold) {
        return this.equals(otherPicture, new Position2D(0, 0), threshold);
    }

    /**
     * Compare both pictures pixel by pixel
     * @param otherPicture The picture to compare the current picture to
     * @param allowedDifferences The total number of pixels that may be different in both pictures to still consider them equal
     * @return True if the amount of pixels that are different is below or equal to the allowedDifferences
     */
    public boolean equals (Picture otherPicture, int allowedDifferences) {
        return this.equals(otherPicture, new Position2D(0, 0), allowedDifferences);
    }

    /**
     * Compare both pictures pixel by pixel
     * @param otherPicture The picture to compare the current picture to
     * @param startPosition Marks the position in the current picture where the otherPicture is aligned to
     * @return
     */
    public boolean equals (Picture otherPicture, Position2D startPosition) {
        return this.equals(otherPicture, new Position2D(0, 0), 0);
    }

    public boolean equals (Picture otherPicture, Position2D startPosition, float threshold) {
        return this.getMaximumDifference(otherPicture, startPosition) <= threshold;
    }

    public boolean equals (Picture otherPicture, Position2D startPosition, int allowedDifferences) {
        return this.getNumberOfDifferentPixels(otherPicture, startPosition) <= allowedDifferences;
    }

    public int getNumberOfDifferentPixels (Picture otherPicture) {
        return this.getNumberOfDifferentPixels(otherPicture, new Position2D(0, 0));
    }

    public int getNumberOfDifferentPixels (Picture otherPicture, Position2D startPosition) {
        return this.getDifferences(otherPicture, startPosition).length;
    }

    public float getMaximumDifference (Picture otherPicture) {
        return this.getMaximumDifference(otherPicture, new Position2D(0, 0));
    }

    public float getMaximumDifference (Picture otherPicture, Position2D startPosition) {
        Float[] diffSet = this.getDifferences(otherPicture, startPosition);
        float max = 0f;
        for(float diff : diffSet) {
            max = Math.max(diff, max);
        }
        return max;
    }

    public float getAverageDifference (Picture otherPicture) {
        return this.getAverageDifference(otherPicture, new Position2D(0, 0));
    }

    public Float getAverageDifference (Picture otherPicture, Position2D startPosition) {
        Float[] diffSet = this.getDifferences(otherPicture, startPosition);
        float sum = 0f;
        for(float diff : diffSet) {
            sum += diff;
        }
        return sum/diffSet.length;
    }

    public Float[] getDifferences (Picture otherPicture) {
        return this.getDifferences(otherPicture, new Position2D(0, 0));
    }

    /**
     *
     * @param otherPicture The picture to compare the current picture to
     * @param startPosition Marks the position in the current picture where the otherPicture is aligned to
     * @return
     */
    public Float[] getDifferences (Picture otherPicture, Position2D startPosition) {
        PicLog.debug("This size: "+propertiesToString(this.getPixelValues()));
        PicLog.debug("Other size: "+propertiesToString(otherPicture.getPixelValues()));

        List<Float> differences = new ArrayList<>();

        if(this.getPixelValues().length - startPosition.getPosY() != otherPicture.getPixelValues().length) {
            PicLog.error("Image height does not match.");
        }
        if(this.getPixelValues()[0].length - startPosition.getPosX() != otherPicture.getPixelValues()[0].length) {
            PicLog.error("Image width does not match.");
        }

        for(int y = startPosition.getPosY(); y < this.getPixelValues().length; y++) {
            for(int x = startPosition.getPosX(); x < this.getPixelValues()[0].length; x++) {
                int thisPixel = this.getPixelValues()[y][x];
                int otherPixel = otherPicture.getPixelValues()[y][x];
                int diff = Math.abs(thisPixel - otherPixel);
                if(diff > 0) {
                    differences.add((float)diff/WHITE);
                }

            }
        }
        PicLog.trace("Size of differences: "+differences.size());

        return differences.toArray(new Float[differences.size()]);
    }

    public boolean contains (Picture subPicture) {
        return this.contains(subPicture, 0f);
    }

    public boolean contains (Picture subPicture, float threshold) {
        return false; //TODO
    }

    private static String propertiesToString(int[][] image) {
        if (image == null) {
            return "[null:null]";
        }
        if (image.length < 1) {
            return "[0:null]";
        }
        return "["+image.length+":"+image[0].length+"]";
    }

    public int[][] getPixelValues() {
        if(this.pixelValues == null){
            this.pixelValues = convertTo2DWithoutUsingGetRGB(this.image);
        }
        return this.pixelValues;
    }

    private static int[][] convertTo2DWithoutUsingGetRGB(BufferedImage image) {

        final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        final int width = image.getWidth();
        final int height = image.getHeight();
        final boolean hasAlphaChannel = image.getAlphaRaster() != null;

        int[][] result = new int[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += -16777216; // 255 alpha
                argb += ((int) pixels[pixel] & 0xff); // blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }

        return result;
    }

}
