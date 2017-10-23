package com.af.pxl.palettes;

/**
 * Created by Aefyr on 23.10.2017.
 */

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import java.util.Arrays;

class Kmeans {
    public static final int MODE_CONTINUOUS = 1;
    public static final int MODE_ITERATIVE = 2;
    private Cluster[] clusters;

    private class Cluster {
        int blue;
        int blues;
        int green;
        int greens;
        int id;
        int pixelCount;
        int red;
        int reds;

        private Cluster(int id, int rgb) {
            int g = (rgb >> 8) & 255;
            int b = (rgb) & 255;
            this.red = (rgb >> 16) & 255;
            this.green = g;
            this.blue = b;
            this.id = id;
            addPixel(rgb);
        }

        public void clear() {
            this.red = 0;
            this.green = 0;
            this.blue = 0;
            this.reds = 0;
            this.greens = 0;
            this.blues = 0;
            this.pixelCount = 0;
        }

        int getId() {
            return this.id;
        }

        int getRGB() {
            return ((ViewCompat.MEASURED_STATE_MASK | ((this.reds / this.pixelCount) << 16)) | ((this.greens / this.pixelCount) << 8)) | (this.blues / this.pixelCount);
        }

        void addPixel(int color) {
            int g = (color >> 8) & 255;
            int b = (color) & 255;
            this.reds += (color >> 16) & 255;
            this.greens += g;
            this.blues += b;
            this.pixelCount++;
            this.red = this.reds / this.pixelCount;
            this.green = this.greens / this.pixelCount;
            this.blue = this.blues / this.pixelCount;
        }

        void removePixel(int color) {
            int g = (color >> 8) & 255;
            int b = (color) & 255;
            this.reds -= (color >> 16) & 255;
            this.greens -= g;
            this.blues -= b;
            this.pixelCount--;
            this.red = this.reds / this.pixelCount;
            this.green = this.greens / this.pixelCount;
            this.blue = this.blues / this.pixelCount;
        }

        int distance(int color) {
            int g = (color >> 8) & 255;
            int b = (color) & 255;
            int rx = Math.abs(this.red - ((color >> 16) & 255));
            int gx = Math.abs(this.green - g);
            return ((rx + gx) + Math.abs(this.blue - b)) / 3;
        }
    }

    public static Bitmap test(Bitmap sourceImage) {
        return new Kmeans().calculate(sourceImage, 16, 1);
    }

    public Bitmap calculate(Bitmap image, int k, int mode) {
        long start = System.currentTimeMillis();
        int w = image.getWidth();
        int h = image.getHeight();
        this.clusters = createClusters(image, k);
        int[] lut = new int[(w * h)];
        Arrays.fill(lut, -1);
        boolean pixelChangedCluster = true;
        int loops = 0;
        while (pixelChangedCluster) {
            int y;
            pixelChangedCluster = false;
            loops++;
            for (y = 0; y < h; y++) {
                int x;
                for (x = 0; x < w; x++) {
                    int pixel = image.getPixel(x, y);
                    Cluster cluster = findMinimalCluster(pixel);
                    if (lut[(w * y) + x] != cluster.getId()) {
                        if (mode == 1) {
                            if (lut[(w * y) + x] != -1) {
                                this.clusters[lut[(w * y) + x]].removePixel(pixel);
                            }
                            cluster.addPixel(pixel);
                        }
                        pixelChangedCluster = true;
                        lut[(w * y) + x] = cluster.getId();
                    }
                }
            }
            if (mode == 2) {
                for (Cluster clear : this.clusters) {
                    clear.clear();
                }
                for (y = 0; y < h; y++) {
                    for (int x = 0; x < w; x++) {
                        this.clusters[lut[(w * y) + x]].addPixel(image.getPixel(x, y));
                    }
                }
            }
        }
        Bitmap result = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                result.setPixel(x, y, this.clusters[lut[(w * y) + x]].getRGB());
            }
        }
        Log.d("Kmeans", "Clustered to " + k + " clusters in " + loops + " loops in " + (System.currentTimeMillis() - start) + " ms.");
        return result;
    }

    private Cluster[] createClusters(Bitmap image, int k) {
        Cluster[] result = new Cluster[k];
        int x = 0;
        int y = 0;
        int dx = image.getWidth() / k;
        int dy = image.getHeight() / k;
        for (int i = 0; i < k; i++) {
            result[i] = new Cluster(i, image.getPixel(x, y));
            x += dx;
            y += dy;
        }
        return result;
    }

    private Cluster findMinimalCluster(int rgb) {
        Cluster wCluster = null;
        int min = Integer.MAX_VALUE;
        for (Cluster cCluster: clusters) {
            int distance = cCluster.distance(rgb);
            if (distance < min) {
                min = distance;
                wCluster = cCluster;
            }
        }
        return wCluster;
    }
}