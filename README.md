# Anti-aliased-Ray-Tracing

A ray tracing system which parses 3D scene described with orientation of geometric primitives and allows the user to explore in real-time the CPU based ray traced scene with ultra high realism.

Features
--------
- CPU based ray tracer built from scratch.
- Users can explore the ray-traced world in real-time using keyboard control.
- Parallelises the task of tracing ray by using all of the available CPU cores.
- Provides a framework that can be expanded with new geometric primitives which enables it to be able to render more complex scenes.
- Anti-aliasing level can be controlled.

Dependencies
------------
The project requires:
- Java SE 7

How to use
------------
The entry point is ``` com.raytracing.main.Main ```.

The camera controls are as following-
* `w`: forward
* `a`: left
* `s`: back
* `d`: right
* `left arrow` / `right arrow`: yaw
* `up arrow` / `down arrow`: pitch
* `,` / `.` : roll
* `[` / `]`: fly
* `0`: reset camera position and orientation

Other controls-
* `g`: toggle fast rendering on or off. In fast rendering mode, only ray tracking is executed(no reflection or refraction is allowed)
* `h`: enable anti-aliasing
* `j`: disable anti-aliasing
