/**
 * Programmer: Chris Tralie (modified by CS 174 student)
 * Purpose: To use the hough transform to find lines in an edge image.  Students
 * will convert this into thread-safe parallel code by making this implement the
 * runnable interface and having it compute different ranges of angles in parallel
*/
import java.awt.Color;
import java.util.Arrays;

public class Hough {
    private EdgeImage image;

    public Hough(EdgeImage image) {
        this.image = image;
    }

    /**
     * Draw a line at a particular angle at a particular distance
     * from the center
     * @param angle Angle between 0 and 2PI
     * @param r Distance from center
     */
    public void drawLine(double angle, double r) {
        int M = image.grad.length;
        int N = image.grad[0].length;
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        double di = -s;
        double dj = c;
        for (int k = 0; k < 2; k++) {
            double i = r*c + image.grad.length/2;
            double j = r*s + image.grad[0].length/2;
            while (i >= 0 && j >= 0 && i < M && j < N) {
                int ii = (int)i;
                int jj = (int)j;
                image.picture.set(jj, ii, Color.RED);
                i += di;
                j += dj;
            }
            di *= -1;
            dj *= -1;
        }
    }

    /**
     * 
     */
    public void getEdges(int NAngles, int NRad, float thresh) {
        int M = image.grad.length;
        int N = image.grad[0].length;
        for (int a = 0; a < NAngles; a++) {
            double angle = 2*Math.PI*a/NAngles;
            double c = Math.cos(angle);
            double s = Math.sin(angle);
            for (int r = 0; r < NRad; r++) {
                double di = -s;
                double dj = c;
                float total = 0.0f;
                for (int k = 0; k < 2; k++) {
                    double i = r*c + image.grad.length/2;
                    double j = r*s + image.grad[0].length/2;
                    while (i >= 0 && j >= 0 && i < M && j < N) {
                        int ii = (int)i;
                        int jj = (int)j;
                        total += image.grad[ii][jj];
                        i += di;
                        j += dj;
                    }
                    di *= -1;
                    dj *= -1;
                }
                if (total > thresh) {
                    drawLine(angle, r);
                }
            }
        }
        image.picture.save("edges.png");
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            throw new IllegalArgumentException("Error: Need to specify image path and threshold");
        }
        String imagepath = args[0];
        float thresh = Float.parseFloat(args[1]);
        EdgeImage im = new EdgeImage(imagepath);
        int sigma = 3;
        // Step 1: Compute gradient
        im.computeGradient(sigma, 3);
        // Step 2: Nonmax suppression
        im.nonmaxSuppression();
        // Step 3: Get edges
        Hough h = new Hough(im);
        h.getEdges(4000, 1000, thresh);
    }
}
