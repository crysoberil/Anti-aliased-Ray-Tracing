package com.raytracing.tracer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import com.raytracing.primiteves.Color3;
import com.raytracing.primiteves.Plane3;
import com.raytracing.primiteves.Point3;
import com.raytracing.primiteves.Ray;
import com.raytracing.primiteves.Vector3;
import com.raytracing.shapes.CheckerBoard;
import com.raytracing.shapes.Cylinder;
import com.raytracing.shapes.Shape;
import com.raytracing.shapes.Sphere;
import com.raytracing.shapes.Triangle;

public class RayTracer {
    private static final int PARALLEL_THREADS = Runtime.getRuntime().availableProcessors();
    private static final boolean DEBUG_MODE = true;

    private boolean fastRenderMode;
    private int dimension;
    private int recDepth;
    private Shape[] shapes;
    private Point3[] lights;
    private double lightIntensity;
    private Point3 eyePos;
    private Vector3 dirUp, dirForward, dirRight;
    private int[][] pixelsRGB;
    private RayTracerThread[] threads;
    private Semaphore syncSemaphore = new Semaphore(1);
    private int antiAliasingSamples;

    public RayTracer(File sdlFile) throws IOException {
        fastRenderMode = true;
        Scanner in = new Scanner(sdlFile);
        readFromFile(in);
        in.close();
    }

    public RayTracer(String fileLocation) throws IOException {
        fastRenderMode = true;
        InputStream iStream = RayTracer.class.getResourceAsStream(fileLocation);
        Scanner in = new Scanner(iStream);
        readFromFile(in);
        in.close();
    }

    public void increaseAntiAliasingSamples() {
        antiAliasingSamples += 5;
    }

    public void resetAntiAliasingSamples() {
        antiAliasingSamples = 0;
    }

    public boolean isAtFastRenderMode() {
        return fastRenderMode;
    }

    public void setFastRenderMode(boolean fastRenderMode) {
        this.fastRenderMode = fastRenderMode;
    }

    private void readFromFile(Scanner in) {
        ArrayList<Shape> shapeCollection = new ArrayList<>();
        ArrayList<Point3> lightCollection = new ArrayList<>();

        while (in.hasNext()) {
            String s = in.next();

            if (s.equals("recDepth"))
                recDepth = in.nextInt();
            // Dimension
            else if (s.equals("pixels")) {
                dimension = in.nextInt();
                pixelsRGB = new int[dimension][dimension];
            }
            // Read object
            else if (s.equals("objStart")) {
                String objType = in.next();

                if (objType.equals("CHECKERBOARD"))
                    readCheckerBoard(in, shapeCollection);
                else if (objType.equals("SPHERE"))
                    readSphere(in, shapeCollection);
                else if (objType.equals("CYLINDER"))
                    readCylinder(in, shapeCollection);
                else if (objType.equals("TRIANGLE"))
                    readTriangle(in, shapeCollection);
            }
            else if (s.equals("light")) {
                Point3 lightPos = Point3.read(in);
                lightCollection.add(lightPos);
            }
        }

        shapes = new Shape[shapeCollection.size()];
        lights = new Point3[lightCollection.size()];

        for (int i = 0; i < shapes.length; i++)
            shapes[i] = shapeCollection.get(i);

        for (int i = 0; i < lights.length; i++)
            lights[i] = lightCollection.get(i);

        // Compute intensity for each light source
        lightIntensity = lights.length == 0 ? 0 : 1.0 / lights.length;
    }

    public int[][] getPixelMatrix(Point3 eyePosition, Vector3 upDirection, Vector3 forwardDirection,
            Vector3 rightDirection) {
        eyePos = eyePosition;
        dirUp = upDirection;
        dirForward = forwardDirection;
        dirRight = rightDirection;

        try {
            syncSemaphore.acquire();
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        threads = new RayTracerThread[PARALLEL_THREADS];

        long traceBeginTime = System.currentTimeMillis();

        for (int threadNo = 0; threadNo < PARALLEL_THREADS; threadNo++) {
            threads[threadNo] = new RayTracerThread(pixelsRGB, dimension, threadNo, PARALLEL_THREADS);
            threads[threadNo].start();
        }

        for (int threadNo = 0; threadNo < PARALLEL_THREADS; threadNo++) {
            try {
                threads[threadNo].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (RayTracerThread thread : threads) {
            if (thread.interrupted) {
                syncSemaphore.release();
                return null;
            }
        }

        long traceEndTime = System.currentTimeMillis();

        if (DEBUG_MODE)
            System.out.println("Raster generated in " + (traceEndTime - traceBeginTime) + " ms");

        syncSemaphore.release();

        return pixelsRGB;
    }

    public int getDimension() {
        return dimension;
    }

    private Ray getInitialRay(double i, double j) {
        double multiplier = 1.41;
        double normalizedI = (i / dimension) - 0.5;
        double normalizedJ = (-j / dimension) + 0.5;

        normalizedI *= multiplier;
        normalizedJ *= multiplier;

        Vector3 rayDirection = dirRight.scaleBy(normalizedI).add(dirUp.scaleBy(normalizedJ)).add(dirForward);
        return new Ray(eyePos, rayDirection);
    }

    // private Ray getInitialRayParallel(double i, double j) {
    // double multiplier = 1.41;
    // double normalizedI = (i / dimension) - 0.5;
    // double normalizedJ = (-j / dimension) + 0.5;
    //
    // normalizedI *= multiplier;
    // normalizedJ *= multiplier;
    //
    // Vector3 viewDirection =
    // dirRight.scaleBy(normalizedI).add(dirUp.scaleBy(normalizedJ)).add(dirForward)
    // .unitVector();
    //
    // // Point3 viewportPoint =
    // //
    // eyePos.addVector(dirRight.scaleBy(normalizedI)).addVector(dirUp.scaleBy(normalizedJ));
    // return new Ray(eyePos, viewDirection);
    // }

    private Shape getClosestRayIntersection(Ray r, RaySurfaceIntersection bestIntersection) {
        bestIntersection.intersected = false;

        Shape closestShape = null;

        for (Shape shape : shapes) {
            RaySurfaceIntersection newIntersection = shape.findIntersection(r);

            if (newIntersection.isCloserIntersectionThan(bestIntersection)) {
                bestIntersection.morphTo(newIntersection);
                closestShape = shape;
            }
        }

        return closestShape;
    }

    // n, s, r, v are normalized
    private Color3 phongIlluminationColor(Shape shape, Color3 col, Vector3 n, Vector3 s, Vector3 r, Vector3 v) {
        Color3 diffuseColor = col.scaleBy(Math.max(0.0, lightIntensity * shape.diffuseCoeff * s.dot(n)));

        Color3 specularColor = null;
        double rDotV = r.dot(v);

        if (rDotV < 1e-2)
            specularColor = Color3.COLOR_BLACK;
        else {
            specularColor = Color3.COLOR_WHITE
                    .scaleBy(lightIntensity * shape.specularCoeff * Math.pow(rDotV, shape.specularExponent));
        }

        Color3 combined = diffuseColor.add(specularColor);

        return combined;
    }

    private Color3 getColorContributionFromLights(Shape shape, RaySurfaceIntersection closestIntersection,
            Color3 colorAtIntersection) {
        if (colorAtIntersection.equals(Color3.COLOR_BLACK))
            return Color3.COLOR_BLACK;

        Color3 res = colorAtIntersection.scaleBy(shape.ambientCoeff);

        // Contributions from the light sources
        for (Point3 lightPos : lights) {
            double tLight = closestIntersection.intersectionPoint.distanceTo(lightPos);

            if (tLight < 0)
                continue;

            Ray rayToLight = new Ray(closestIntersection.intersectionPoint, lightPos);

            rayToLight.advanceByEpsilon();

            boolean rayReachesLight = true;

            for (int j = 0; j < shapes.length && rayReachesLight; j++) {
                double newT = shapes[j].getIntersectionT(rayToLight);
                if (newT >= 0 && newT < tLight)
                    rayReachesLight = false;
            }

            if (rayReachesLight) {
                Vector3 v = closestIntersection.incidentRay.direction.reverseVector().unitVector();
                Vector3 r = rayToLight.direction.reverseVector()
                        .getReflectionDirection(closestIntersection.intersectionNormal);
                res = res.add(phongIlluminationColor(shape, colorAtIntersection, closestIntersection.intersectionNormal,
                        rayToLight.direction, r, v));
            }
        }

        return res;
    }

    private Color3 getRefractionColor(RaySurfaceIntersection intersection, int recursionDepthLeft, Shape shape,
            double[] REFRACTION_ALPHA) {
        if (recursionDepthLeft == 0)
            return Color3.COLOR_BLACK;

        Vector3 i = intersection.incidentRay.direction;
        Vector3 n = intersection.intersectionNormal;

        double cosThetaI = n.dot(i.reverseVector());
        double thetaI = Math.acos(cosThetaI);
        double sinThetaT = Math.sin(thetaI) / shape.refractiveIndex;

        if (sinThetaT < -1 || sinThetaT > 1) // Total internal reflection
        {
            Ray reflectedViewRay = new Ray(intersection.intersectionPoint, intersection.reflectionDirection);
            reflectedViewRay.advanceByEpsilon();
            return traceViewRayOut(reflectedViewRay, recursionDepthLeft - 1);
        }

        // else normal refraction
        double thetaT = Math.asin(sinThetaT);
        Vector3 refractedDirection = i.scaleBy(1.0 / shape.refractiveIndex);
        double mul = (1.0 / shape.refractiveIndex) * cosThetaI - Math.cos(thetaT);
        refractedDirection = refractedDirection.add(n.scaleBy(mul));

        Ray refractionRay = new Ray(intersection.intersectionPoint, refractedDirection);
        refractionRay.advanceByEpsilon();

        Color3 ret = traceViewRayOut(refractionRay, recursionDepthLeft);
        double r0 = (1.0 - shape.refractiveIndex) / (1.0 + shape.refractiveIndex);
        r0 *= r0;
        REFRACTION_ALPHA[0] = 1.0 - (r0 + (1 - r0) * Math.pow(1 - Math.cos(thetaI), 5)); // Schlick's
                                                                                         // approximation
        return ret;
    }

    private Color3 traceViewRayOut(Ray r, int recursionDepthLeft) {
        if (recursionDepthLeft == 0)
            return Color3.COLOR_BLACK;

        RaySurfaceIntersection closestIntersection = new RaySurfaceIntersection();
        Shape closestShape = getClosestRayIntersection(r, closestIntersection);

        if (!closestIntersection.intersected) // Ray shot to void
            return Color3.COLOR_BLACK;

        Color3 colorAtIntersection = closestShape.getColorAtPoint(closestIntersection.intersectionPoint);

        if (fastRenderMode)
            return colorAtIntersection;

        Color3 colorFromLight = getColorContributionFromLights(closestShape, closestIntersection, colorAtIntersection);

        // Contribution from reflection
        Ray reflectedViewRay = new Ray(closestIntersection.intersectionPoint, closestIntersection.reflectionDirection);
        reflectedViewRay.advanceByEpsilon();
        Color3 reflectionContribution = traceViewRayOut(reflectedViewRay, recursionDepthLeft - 1)
                .scaleBy(closestShape.refCoeff);

        if (!closestShape.isRefractable()) // not triangle
            return colorFromLight.add(reflectionContribution);

        // Must consider total internal reflection
        double[] REFRACTION_ALPHA = new double[1];
        Color3 refractionContribution = getRefractionColor(closestIntersection, recursionDepthLeft - 1, closestShape,
                REFRACTION_ALPHA).scaleBy(closestShape.refCoeff);

        Color3 finalColor = colorFromLight;
        finalColor = finalColor.add(refractionContribution.scaleBy(REFRACTION_ALPHA[0]));
        finalColor = finalColor.add(reflectionContribution.scaleBy(1 - REFRACTION_ALPHA[0]));

        return finalColor;
    }

    private Color3 traceRayToPixel(int i, int j) {
        if (antiAliasingSamples == 0) {
            Ray r = getInitialRay(i, j);
            return traceViewRayOut(r, recDepth);
        }
        else {
            double delAngle = 2 * Math.PI / antiAliasingSamples;
            Color3 ret = new Color3(0, 0, 0);

            double pixelRadius = .37;

            for (int k = 0; k < antiAliasingSamples; k++) {
                double nI = i + pixelRadius * Math.cos(k * delAngle);
                double nJ = j + pixelRadius * Math.sin(k * delAngle);
                Ray r = getInitialRay(nI, nJ);
                Color3 col = traceViewRayOut(r, recDepth);
                ret = ret.add(col.scaleBy(1.0 / antiAliasingSamples));
            }

            return ret;
        }
    }

    private void readTriangle(Scanner in, ArrayList<Shape> shapeCollection) {

        Triangle triangle = new Triangle();

        while (true) {
            String propertyType = in.next();

            if (propertyType.equals("objEnd"))
                break;

            if (propertyType.equals("a"))
                triangle.a = Point3.read(in);
            else if (propertyType.equals("b"))
                triangle.b = Point3.read(in);
            else if (propertyType.equals("c"))
                triangle.c = Point3.read(in);
            else
                readDefaultProperty(triangle, propertyType, in);
        }

        triangle.preProcess();
        shapeCollection.add(triangle);
    }

    private void readCylinder(Scanner in, ArrayList<Shape> shapeCollection) {
        Cylinder cylinder = new Cylinder();

        while (true) {
            String propertyType = in.next();
            if (propertyType.equals("objEnd"))
                break;

            if (propertyType.equals("radius"))
                cylinder.radius = in.nextDouble();
            else if (propertyType.equals("xCenter"))
                cylinder.center_x = in.nextDouble();
            else if (propertyType.equals("zCenter"))
                cylinder.center_z = in.nextDouble();
            else if (propertyType.equals("yMin"))
                cylinder.y_min = in.nextDouble();
            else if (propertyType.equals("yMax"))
                cylinder.y_max = in.nextDouble();
            else
                readDefaultProperty(cylinder, propertyType, in);
        }

        cylinder.reComputePlanes();

        shapeCollection.add(cylinder);
    }

    private void readSphere(Scanner in, ArrayList<Shape> shapeCollection) {
        Sphere sphere = new Sphere();

        while (true) {
            String propertyType = in.next();
            if (propertyType.equals("objEnd"))
                break;

            if (propertyType.equals("center"))
                sphere.center = Point3.read(in);
            else if (propertyType.equals("radius"))
                sphere.radius = in.nextDouble();
            else
                readDefaultProperty(sphere, propertyType, in);
        }

        shapeCollection.add(sphere);
    }

    private void readCheckerBoard(Scanner in, ArrayList<Shape> shapeCollection) {
        CheckerBoard checkerBoard = new CheckerBoard();
        checkerBoard.cellWidth = 20;

        // Three points on xz plane
        Point3 p1 = new Point3(1, 0, 1);
        Point3 p2 = new Point3(-1, 0, 1);
        Point3 p3 = new Point3(5, 0, 7);
        checkerBoard.plane = new Plane3(p1, p2, p3);

        while (true) {
            String propertyType = in.next();
            if (propertyType.equals("objEnd"))
                break;

            if (propertyType.equals("colorOne"))
                checkerBoard.color1 = Color3.read(in);
            else if (propertyType.equals("colorTwo"))
                checkerBoard.color2 = Color3.read(in);
            else
                readDefaultProperty(checkerBoard, propertyType, in);
        }

        shapeCollection.add(checkerBoard);
    }

    private void readDefaultProperty(Shape shape, String propertyType, Scanner in) {
        if (propertyType.equals("ambCoeff"))
            shape.ambientCoeff = in.nextDouble();
        else if (propertyType.equals("difCoeff"))
            shape.diffuseCoeff = in.nextDouble();
        else if (propertyType.equals("refCoeff"))
            shape.refCoeff = in.nextDouble();
        else if (propertyType.equals("specCoeff"))
            shape.specularCoeff = in.nextDouble();
        else if (propertyType.equals("specExp"))
            shape.specularExponent = in.nextDouble();
        else if (propertyType.equals("refractiveIndex"))
            shape.refractiveIndex = in.nextDouble();
        else if (propertyType.equals("color"))
            shape.color = Color3.read(in);
        else
            System.err.println("SDL PARSE ERROR\n");
    }

    public BufferedImage getBufferedImage(Point3 eyePosition, Vector3 upDirection, Vector3 forwardDirection,
            Vector3 rightDirection) {
        int[] pixelArray = getPixelArray(eyePosition, upDirection, forwardDirection, rightDirection);

        if (pixelArray == null)
            return null;

        BufferedImage image = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++)
                image.setRGB(i, j, pixelArray[i * dimension + j]);
        }

        return image;
    }

    private int[] getPixelArray(Point3 eyePosition, Vector3 upDirection, Vector3 forwardDirection,
            Vector3 rightDirection) {
        int[][] pixelsRGB = getPixelMatrix(eyePosition, upDirection, forwardDirection, rightDirection);

        if (pixelsRGB == null)
            return null;

        int[] pixelArr = new int[dimension * dimension];

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++)
                pixelArr[i * dimension + j] = pixelsRGB[i][j];
        }

        return pixelArr;
    }

    public void interruptCurrentWork() {
        if (threads != null) {
            for (RayTracerThread thread : threads) {
                if (thread != null)
                    thread.interruptThread();
            }
        }
    }

    private class RayTracerThread extends Thread {
        private boolean interrupted;
        private int[][] pixelsRGB;
        private int dimension;
        private int threadNo;
        private int totalThreadCount;

        public RayTracerThread(int[][] pixelsRGB, int dimension, int threadNo, int totalThreadCount) {
            this.pixelsRGB = pixelsRGB;
            this.dimension = dimension;
            this.threadNo = threadNo;
            this.totalThreadCount = totalThreadCount;
            interrupted = false;
        }

        public void interruptThread() {
            interrupted = true;
        }

        @Override
        public void run() {
            for (int i = 0; i < dimension; i++) {
                for (int j = threadNo; j < dimension; j += totalThreadCount) {
                    if (interrupted)
                        return;

                    Color3 pixelColor = traceRayToPixel(i, j);
                    pixelsRGB[i][j] = pixelColor.getRGB();
                }
            }
        }
    }
}
