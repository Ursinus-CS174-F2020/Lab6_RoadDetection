/**
 * Programmer: Chris Tralie
 * Purpose: Some general purpose utilities for images, including 
 * color to grayscale and generation of gaussian and gaussian
 * derivative kernels
 */
import java.awt.Color;

public class ImageUtils {

    /**
    * Convert a picture object to a 2D array of grayscale floats
    * @param picture A picture object
    * @return A 2D array of grayscale values between 0 and 1
    */
    public static float[][] getGrayscaleArray(Picture picture) {
        int rows = picture.height();
        int cols = picture.width();
        float[][] image = new float[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                image[i][j] = getBrightness(picture.get(j, i));
            }
        }
        return image;
    }

    /**
    * Convert a java.awt.Color object to a grayscale float
    * @param c A java.awt.Color object
    * @return A value between 0 and 1 representing grayscale
    */
    public static float getBrightness(Color c) {
        return 0.3f*(c.getRed()/255.0f) + 0.59f*(c.getGreen()/255.0f) + 0.11f*(c.getBlue()/255.0f);
    }
    
    /**
    * Save an image to a file
    * @param image Image to save
    * @param filename String to which to save image
    * @param normalize Whether to normalize so the max gradient is white
    * @param thresh Threshold
    */
    public static void saveImage(float[][] image, String filename, boolean normalize, float thresh) {
        // Compute final gradient
        int M = image.length;
        int N = image[0].length;
        float max = 1.0f;
        if (normalize) {
            max = 0.0f;
            for (int i = 0; i < M; i++) {
                for (int j = 0; j < N; j++) {
                    if (image[i][j] > max) {
                        max = image[i][j];
                    }
                }
            }
        }
        // Normalize gradient and save to temporary image
        Picture pic = new Picture(N, M);
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                float val = image[i][j]/max;
                if (val > thresh) {
                    pic.set(j, i, new Color(val, val, val));
                }
            }
        }
        pic.save(filename);
    }


    /**
    * Get a convolution kernel for a Gaussian
    * @returns An array with the elements in the convolution kernel
    */
    public static float[] getGaussKernel(int sigma, int kernelWidth) {
        float[] kernel = new float[sigma*kernelWidth*2+1];
        float totalWeight = 0.0f;
        for (int i = 0; i < kernel.length; i++) {
            float x = i - kernelWidth*sigma;
            float weight = (float)Math.exp(-(x*x)/(2*sigma*sigma));
            kernel[i] = weight;
            totalWeight += weight;
        }
        for (int i = 0; i < kernel.length; i++) {
            kernel[i] /= totalWeight;
        }
        return kernel;
    }

    /**
    * Get a convolution kernel for the derivative of a Gaussian
    * @param sigma The width of the Gaussian
    * @returns An array with the elements in the convolution kernel
    */
    public static float[] getGaussGradKernel(int sigma, int kernelWidth) {
        float[] kernel = new float[sigma*kernelWidth*2+1];
        float totalWeight = 0.0f;
        for (int i = 0; i < kernel.length; i++) {
            float x = i - kernelWidth*sigma;
            float weight = x*(float)Math.exp(-(x*x)/(2*sigma*sigma));
            kernel[i] = weight;
            totalWeight += weight*x;
        }
        for (int i = 0; i < kernel.length; i++) {
            kernel[i] /= totalWeight;
        }
        return kernel;
    }
}
