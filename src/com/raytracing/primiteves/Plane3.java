package com.raytracing.primiteves;

import com.raytracing.tracer.RaySurfaceIntersection;

public class Plane3 {
	public final Vector3 normalDirection;
	public final double d;

	public Plane3(Point3 p1, Point3 p2, Point3 p3) {
		Vector3 v1 = p1.vectorTo(p2);
		Vector3 v2 = p2.vectorTo(p3);
		normalDirection = v1.cross(v2).unitVector();

		double a = normalDirection.x;
		double b = normalDirection.y;
		double c = normalDirection.z;

		d = -(a * p1.x + b * p1.y + c * p1.z);
	}

	public boolean isParallelTo(Vector3 v) {
		return normalDirection.isPerpendiculerTo(v);
	}

	public boolean isPerpendiculerTo(Vector3 v) {
		return normalDirection.isParallelTo(v);
	}

	public RaySurfaceIntersection findIntersection(Ray ray) {
        // If parallel to plane, then no intersection
        if (isParallelTo(ray.direction))
            return RaySurfaceIntersection.NO_INTERSECTION;

        RaySurfaceIntersection intersection = new RaySurfaceIntersection();

        // Otherwise there we have intersection
        double nom = (-d - normalDirection.dot(ray.start.asVector()));
        double denom = normalDirection.dot(ray.direction);
        intersection.t = nom / denom;

        if (intersection.t < 0 || intersection.t > 1e30) {
            intersection.intersected = false;
            intersection.t = -1e10;
            return intersection;
        }

        intersection.intersected = true;
        intersection.intersectionPoint = ray.getPointOnLine(intersection.t);

        if (normalDirection.dot(ray.direction) < 0)
            intersection.intersectionNormal = normalDirection;
        else
            intersection.intersectionNormal = normalDirection.reverseVector();

        intersection.reflectionDirection = ray.direction.getReflectionDirection(intersection.intersectionNormal);
        intersection.incidentRay = ray;

        return intersection;
    }
	
	public double getIntersectionT(Ray ray) {
        // If parallel to plane, then no intersection
        if (isParallelTo(ray.direction))
            return -1e10;

        // Otherwise there we have intersection
        double nom = (-d - normalDirection.dot(ray.start.asVector()));
        double denom = normalDirection.dot(ray.direction);
        double t = nom / denom;

        if (t < 0 || t > 1e30)
            return -1e10;
        
        return t;
    }
}
