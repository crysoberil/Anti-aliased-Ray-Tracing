package com.raytracing.shapes;

import com.raytracing.primiteves.Point3;
import com.raytracing.primiteves.Ray;
import com.raytracing.primiteves.Vector3;
import com.raytracing.tracer.RaySurfaceIntersection;

public class Sphere extends Shape {
    public Point3 center;
    public double radius;

    @Override
    public RaySurfaceIntersection findIntersection(Ray ray) {
        double a = ray.direction.dot(ray.direction);
        Vector3 oMinusC = ray.start.subtractPoint(center);
        double b = 2 * ray.direction.dot(oMinusC);
        double c = oMinusC.dot(oMinusC) - radius * radius;

        if (b * b < 4 * a * c)
            return RaySurfaceIntersection.NO_INTERSECTION;

        double rootDel = Math.sqrt(b * b - 4 * a * c);
        double t1 = (-b + rootDel) / (2.0 * a);
        double t2 = (-b - rootDel) / (2.0 * a);

        if (t1 < 0 && t2 < 0)
            return RaySurfaceIntersection.NO_INTERSECTION;

        RaySurfaceIntersection intersection = new RaySurfaceIntersection();

        intersection.intersected = true;

        intersection.t = Math.min((t1 < 0 ? 1e200 : t1), (t2 < 0 ? 1e200 : t2));

        intersection.intersectionPoint = ray.getPointOnLine(intersection.t);
        intersection.intersectionNormal = intersection.intersectionPoint.subtractPoint(center).unitVector();
        intersection.reflectionDirection = ray.direction.getReflectionDirection(intersection.intersectionNormal);
        intersection.incidentRay = ray;

        return intersection;
    }

    @Override
    public double getIntersectionT(Ray ray) {
        double a = ray.direction.dot(ray.direction);
        Vector3 oMinusC = ray.start.subtractPoint(center);
        double b = 2 * ray.direction.dot(oMinusC);
        double c = oMinusC.dot(oMinusC) - radius * radius;

        if (b * b < 4 * a * c)
            return -1e10;

        double rootDel = Math.sqrt(b * b - 4 * a * c);
        double t1 = (-b + rootDel) / (2.0 * a);
        double t2 = (-b - rootDel) / (2.0 * a);

        if (t1 < 0 && t2 < 0)
            return -1e10;

        return Math.min((t1 < 0 ? 1e200 : t1), (t2 < 0 ? 1e200 : t2));
    }
}
