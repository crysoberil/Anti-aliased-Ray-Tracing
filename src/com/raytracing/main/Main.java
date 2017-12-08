package com.raytracing.main;

import com.raytracing.tracer.RayTracer;

public class Main {
    public static void main(String[] args) throws Exception {
        String sdlFileLocation = "/resources/ray_tracing_input.txt";
        RayTracer rayTracer = new RayTracer(sdlFileLocation);
        RayTracingDisplay.displayTracing(rayTracer);
    }
}
