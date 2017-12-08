package com.raytracing.tracer;

import com.raytracing.primiteves.Point3;
import com.raytracing.primiteves.Ray;
import com.raytracing.primiteves.Vector3;

public class RaySurfaceIntersection {
	public static final RaySurfaceIntersection NO_INTERSECTION = new RaySurfaceIntersection(false);

	public Ray incidentRay;
	public boolean intersected;
	public double t;
	public Point3 intersectionPoint;
	public Vector3 intersectionNormal;
	public Vector3 reflectionDirection;

	private RaySurfaceIntersection(boolean intersected) {
		if (!intersected) {
			this.intersected = false;
			this.t = -1e10;
		}
	}

	public RaySurfaceIntersection() {
		this(false);
	}

	public boolean isCloserIntersectionThan(RaySurfaceIntersection sec) {
		return intersected && (!sec.intersected || (sec.intersected && t < sec.t));
	}

	public void morphTo(RaySurfaceIntersection sec) {
		incidentRay = sec.incidentRay;
		intersected = sec.intersected;
		t = sec.t;
		intersectionPoint = sec.intersectionPoint;
		intersectionNormal = sec.intersectionNormal;
		reflectionDirection = sec.reflectionDirection;
	}
}
