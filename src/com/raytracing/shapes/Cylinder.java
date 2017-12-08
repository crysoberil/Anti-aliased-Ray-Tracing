package com.raytracing.shapes;

import com.raytracing.primiteves.Plane3;
import com.raytracing.primiteves.Point3;
import com.raytracing.primiteves.Ray;
import com.raytracing.tracer.RaySurfaceIntersection;

public class Cylinder extends Shape {
    public double radius;
    public double center_x, center_z;
    public double y_max, y_min;
    public Plane3 topPlane, bottomPlane;

    public void reComputePlanes() {
        Point3 pTop1 = new Point3(radius, y_max, 0);
        Point3 pTop2 = new Point3(0, y_max, 0);
        Point3 pTop3 = new Point3(0, y_max, radius);

        topPlane = new Plane3(pTop1, pTop2, pTop3);

        Point3 pBot1 = new Point3(radius, y_min, 0);
        Point3 pBot2 = new Point3(0, y_min, 0);
        Point3 pBot3 = new Point3(0, y_min, radius);

        bottomPlane = new Plane3(pBot1, pBot2, pBot3);
    }

    @Override
    public RaySurfaceIntersection findIntersection(Ray ray) {
        double d1 = ray.start.x - center_x;
        double d2 = ray.start.z - center_z;

        double a = ray.direction.x * ray.direction.x + ray.direction.z * ray.direction.z;
        double b = 2.0 * (d1 * ray.direction.x + d2 * ray.direction.z);
        double c = d1 * d1 + d2 * d2 - radius * radius;

        double[] tS = new double[4];

        if (b * b < 4 * a * c) { // No hit with cylinder
            tS[0] = tS[1] = -1e10;
        }
        else {
            double rootDel = Math.sqrt(b * b - 4 * a * c);

            tS[0] = (-b + rootDel) / (2.0 * a);
            tS[1] = (-b - rootDel) / (2.0 * a);
        }

        for (int i = 0; i < 2; i++) {
            if (tS[i] >= 0) {
                Point3 intersectionPoint = ray.getPointOnLine(tS[i]);
                if (intersectionPoint.y < y_min || intersectionPoint.y > y_max)
                    tS[i] = -1e10;
            }
        }

        RaySurfaceIntersection intTop = topPlane.findIntersection(ray);
        RaySurfaceIntersection intBot = bottomPlane.findIntersection(ray);

        tS[2] = (intTop.intersected ? intTop.t : -1e10);
        tS[3] = (intBot.intersected ? intBot.t : -1e10);

        for (int i = 2; i < 4; i++) {
            if (tS[i] >= 0) {
                Point3 intPoint = (i == 2 ? intTop : intBot).intersectionPoint;
                double v1 = Math.abs(center_x - intPoint.x);
                double v2 = Math.abs(center_z - intPoint.z);
                if (v1 * v1 + v2 * v2 > radius * radius)
                    tS[i] = -1e10;
            }
        }

        int bestTIdx = -1;
        double bestT = 1e150;

        for (int i = 0; i < 4; i++) {
            if (tS[i] >= 0) {
                if (tS[i] < bestT) {
                    bestT = tS[i];
                    bestTIdx = i;
                }
            }
        }

        if (bestTIdx == -1)
            return RaySurfaceIntersection.NO_INTERSECTION;

        if (bestTIdx == 2)
            return intTop;

        if (bestTIdx == 3)
            return intBot;

        RaySurfaceIntersection intersection = new RaySurfaceIntersection();

        intersection.intersected = true;
        intersection.t = bestT;

        intersection.intersectionPoint = ray.getPointOnLine(bestT);
        Point3 sameHeightCenter = new Point3(center_x, intersection.intersectionPoint.y, center_z);
        intersection.intersectionNormal = sameHeightCenter.vectorTo(intersection.intersectionPoint).unitVector();
        intersection.reflectionDirection = ray.direction.getReflectionDirection(intersection.intersectionNormal);
        intersection.incidentRay = ray;

        return intersection;
    }

    @Override
    public double getIntersectionT(Ray ray) {
        double d1 = ray.start.x - center_x;
        double d2 = ray.start.z - center_z;

        double a = ray.direction.x * ray.direction.x + ray.direction.z * ray.direction.z;
        double b = 2.0 * (d1 * ray.direction.x + d2 * ray.direction.z);
        double c = d1 * d1 + d2 * d2 - radius * radius;

        double[] tS = new double[4];

        if (b * b < 4 * a * c) { // No hit with cylinder
            tS[0] = tS[1] = -1e10;
        }
        else {
            double rootDel = Math.sqrt(b * b - 4 * a * c);

            tS[0] = (-b + rootDel) / (2.0 * a);
            tS[1] = (-b - rootDel) / (2.0 * a);
        }

        for (int i = 0; i < 2; i++) {
            if (tS[i] >= 0) {
                Point3 intersectionPoint = ray.getPointOnLine(tS[i]);
                if (intersectionPoint.y < y_min || intersectionPoint.y > y_max)
                    tS[i] = -1e10;
            }
        }

        tS[2] = topPlane.getIntersectionT(ray);
        tS[3] = bottomPlane.getIntersectionT(ray);

        for (int i = 2; i < 4; i++) {
            if (tS[i] >= 0) {
                Point3 intPoint = ray.getPointOnLine(tS[i]);
                double v1 = Math.abs(center_x - intPoint.x);
                double v2 = Math.abs(center_z - intPoint.z);
                if (v1 * v1 + v2 * v2 > radius * radius)
                    tS[i] = -1e10;
            }
        }

        int bestTIdx = -1;
        double bestT = 1e150;

        for (int i = 0; i < 4; i++) {
            if (tS[i] >= 0) {
                if (tS[i] < bestT) {
                    bestT = tS[i];
                    bestTIdx = i;
                }
            }
        }

        if (bestTIdx == -1)
            return -1e10;

        return bestT;
    }
}
