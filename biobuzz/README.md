# BIOBUZZ Auto Lab (2026–2027)

AUTO planning for the **goBILDA 6WD drop-center StarterBot**. This robot has no odometry pods and no path following.

| File | What it is |
|---|---|
| `biobuzz-auto-lab.html` | The simulator. Open it in a browser. |
| `BiobuzzAutoLab.java` | AUTO OpMode exported from the example plan. Re-export from the sim after every change. |
| `AutoLabCalibration.java` | Tests that measure your real robot (A: 72 in straight, B: 360° spin, X: sprint). |

## How the sim matches the real robot

1. **One model.** The sim and the Java use the same constants: wheel size, ticks, top speed, acceleration and track width. The Java drives the same trapezoid speed profile the sim plans, using encoder velocity control.
2. **Calibrate first.** Run `AutoLabCalibration`, then enter the results in the sim's Calibrate tab and re-export.
3. **Drift model.** Without odometry, every move adds error. The drift cloud shows where 60 noisy runs end. Fit it to your real robot by measuring the end spread of 5+ real runs.
4. **Square on walls.** Driving into the perimeter resets heading, and the position across that wall. This is the main way to keep an AUTO repeatable without odometry.
5. **IMU heading.** Every drive holds heading on the Control Hub IMU, and every turn ends on it.

## Before running on the robot

- Set the motor names (Robot tab) to match your config. Defaults are `left_drive` and `right_drive`; you can list two names per side.
- Set the Control Hub mounting (logo/USB direction).
- Wire `initMechanisms`, `setIntake` and `launch` to the goBILDA example code. Keep the launch time equal to the sim's `LAUNCH_SPINUP_S + n * LAUNCH_PER_PIECE_S`.
- The field element positions in the sim are approximate. Load the official field drawing in the Field tab and drag the elements into place.
