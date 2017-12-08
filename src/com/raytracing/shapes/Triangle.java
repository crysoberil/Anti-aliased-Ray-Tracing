package com.raytracing.shapes;

import com.raytracing.primiteves.Plane3;
import com.raytracing.primiteves.Point3;
import com.raytracing.primiteves.Ray;
import com.raytracing.primiteves.Vector3;
import com.raytracing.tracer.RaySurfaceIntersection;

public class Triangle extends Shape {
	public Plane3 plane;
	public Point3 a, b, c;
	public Vector3 edge0, edge1, edge2;

	public void preProcess() {
		plane = new Plane3(a, b, c);
		edge0 = b.subtractPoint(a);
		edge1 = c.subtractPoint(b);
		edge2 = a.subtractPoint(c);
	}

	@Override
	public RaySurfaceIntersection findIntersection(Ray ray) {
		RaySurfaceIntersection intersection = plane.findIntersection(ray);

		if (intersection.intersected) {
			Point3 p = intersection.intersectionPoint;
			Vector3 c0 = p.subtractPoint(a);
			Vector3 c1 = p.subtractPoint(b);
			Vector3 c2 = p.subtractPoint(c);

			boolean inside0 = plane.normalDirection.dot(edge0.cross(c0)) >= 0;
			boolean inside1 = plane.normalDirection.dot(edge1.cross(c1)) >= 0;
			boolean inside2 = plane.normalDirection.dot(edge2.cross(c2)) >= 0;

			if (!(inside0 && inside1 && inside2))
				return RaySurfaceIntersection.NO_INTERSECTION;
		}

		return intersection;
	}

	@Override
	public boolean isRefractable() {
		return true;
	}

    @Override
    public double getIntersectionT(Ray ray) {
        double t = plane.getIntersectionT(ray);

        if (t >= 0) {
            Point3 p = ray.getPointOnLine(t);
            Vector3 c0 = p.subtractPoint(a);
            Vector3 c1 = p.subtractPoint(b);
            Vector3 c2 = p.subtractPoint(c);

            boolean inside0 = plane.normalDirection.dot(edge0.cross(c0)) >= 0;
            boolean inside1 = plane.normalDirection.dot(edge1.cross(c1)) >= 0;
            boolean inside2 = plane.normalDirection.dot(edge2.cross(c2)) >= 0;

            if (!(inside0 && inside1 && inside2))
                return -1e10;
        }

        return t;
    }
}
