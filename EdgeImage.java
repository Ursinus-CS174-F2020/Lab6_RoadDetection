/**
 * Programmer: Chris Tralie
 * Purpose: To compute an edge image via gradients and nonmax suppression
 * from gradient directions.  For simplicity of code that uses this, keep
 * the grayscale image, gradient components, and gradient public
 */

public class EdgeImage {
    public float[][] image;
    public float[][] dX;
    public float[][] dY;
    public float[][] grad;
    public Picture picture;

    public EdgeImage(String filename) {
        picture = new Picture(filename);
        image = ImageUtils.getGrayscaleArray(picture);
    }

    /**
    * Perform a time domain convolution with a 1D convolution kernel on an image, zeropadding
    * at the edges
    * @param I An MxN scalar image
    * @param kernel A 1D kernel
    * @param horiz If true, do a horizontal convolution.  If false, do a vertical convolution
    * @return The result of the convolution
    */
    public float[][] getConvolution(float[][] I, float[] kernel, boolean horiz) {
        int M = I.length;
        int N = I[0].length;
        int d = (kernel.length-1)/2;
        float[][] ret = new float[M][N];
        for (int i = 0; i < M; i++) {
            ret[i] = new float[N];
            for (int j = 0; j < N; j++) {
                float sum = 0.0f;
                for (int k = 0; k < kernel.length; k++) {
                    int row = i;
                    int col = j;
                    if (horiz) {
                        col = col-d+k;
                        if (col > 0 && col < N) {
                            sum += kernel[k]*I[row][col];
                        }
                        else {
                            sum = 0.0f;
                            break;
                        }
                    }
                    else {
                        row = row-d+k;
                        if (row > 0 && row < M) {
                            sum += kernel[k]*I[row][col];
                        }
                        else {
                            sum = 0.0f;
                            break;
                        }
                    }
                }
                ret[i][j] = sum;
            }
        }
        return ret;
    }

    /**
    * Compute the gradient of the image
    * @param sigma Standard deviation of Gaussian in pixels
    */
    public void computeGradient(int sigma, int kernelWidth) {
        int M = image.length;
        int N = image[0].length;
        // Initialize gradients
        float[][] gradX = new float[M][N];
        float[][] gradY = new float[M][N];
        for (int i = 0; i < M; i++) {
            gradX[i] = new float[N];
            gradY[i] = new float[N];
        }
        float[] gaussKernel = ImageUtils.getGaussKernel(sigma, kernelWidth);
        float[] gradKernel = ImageUtils.getGaussGradKernel(sigma, kernelWidth);
        // Compute gradient in x
        dX = getConvolution(image, gaussKernel, false);
        dX = getConvolution(dX, gradKernel, true);
        // Compute gradient in y
        dY = getConvolution(image, gaussKernel, true);
        dY = getConvolution(dY, gradKernel, false);
        // Compute the magnitude of the gradient
        grad = new float[M][N];
        for (int i = 0; i < M; i++) {
            grad[i] = new float[N];
            for (int j = 0; j < N; j++) {
                grad[i][j] = (float)Math.sqrt(dX[i][j]*dX[i][j] + dY[i][j]*dY[i][j]);
            }
        }
    }

    /**
    * Perform nonmax suppression using gradient directions
    */
    public void nonmaxSuppression() {
        int M = grad.length;
        int N = grad[0].length;
        int[][] dirs = {{0, 1}, {1, 1}, {1, 0}, {1, -1}, {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}};
        boolean[][] toKeep = new boolean[M][N];
        for (int i = 0; i < M; i++) {
            toKeep[i] = new boolean[N];
            for (int j = 0; j < N; j++) {
                double dir = Math.atan2(dY[i][j], dX[i][j]);
                dir /= (2*Math.PI);
                if (dir < 0) {
                    dir += 1;
                }
                int idx = (int)Math.round(dir*8);
                idx = idx%8;
                int di = dirs[idx][0];
                int dj = dirs[idx][1];
                boolean greatest = true;
                for (int k = 0; k < 2; k++) { // Forward and backward
                    if (i + di >= 0 && i + di < M && j + dj >= 0 && j + dj < N) {
                        if (grad[i+di][j+dj] > grad[i][j]) {
                            greatest = false;
                        }
                    }
                    di *= -1;
                    dj *= -1;
                }
                toKeep[i][j] = greatest;
            }
        }
        for (int i = 0; i < M; i++) {
            for (int j = 0; j < N; j++) {
                if (!toKeep[i][j]) {
                    grad[i][j] = 0.0f;
                }
            }
        }
    }

}
