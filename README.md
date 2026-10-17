# ftcsimtesting

Autonomous code experiments for FIRST Tech Challenge, aimed at scoring as many AUTO points as possible.

## BIOBUZZ (2026-27) autonomous

`TeamCode/src/main/java/org/firstinspires/ftc/teamcode/` follows the standard FTC SDK layout, so the files can be dropped into a `FtcRobotController` checkout (or OnBot Java).

- `BiobuzzAutoBase.java` holds the routine; `BiobuzzAutoRed` / `BiobuzzAutoBlue` pick the alliance, and Blue mirrors every strafe and turn.

### AUTO scoring (Game Manual TU05, Table 10-2)

| Achievement | Points | How |
|---|---|---|
| LEAVE | 3 | Robot no longer touching the perimeter wall at the end of AUTO |
| PARK | 5 | Robot at least partially in its LOADING ZONE at the end of AUTO |
| HIVE TIP | 20 each | Launch POLLEN into your HIVE's upward CELL until it flips |

Each robot starts with 4 preloaded POLLEN, and each upward CELL already holds 3 NECTAR.

### Routine

1. Drive off the wall (LEAVE) while the launcher spins up.
2. Turn to the HIVE and launch the 4 preloaded POLLEN (first TIP).
3. If at least 16 s remain, collect POLLEN from the GARDEN and launch at the CELL that is now facing up (second TIP).
4. Drive back toward the wall, stopping short of it, so the robot is in the LOADING ZONE without touching the wall (PARK).

That makes 28 points with one TIP and 48 with two, per robot.

### Hardware config names

`frontLeft`, `frontRight`, `backLeft`, `backRight` (mecanum), `launcher` (flywheel motor with encoder), `intake` (motor), `feeder` (servo), `imu`.

### Tuning before a real run

Every field distance, heading and launcher velocity in `BiobuzzAutoBase` is a placeholder. Measure them from the [BIOBUZZ field model on Onshape](https://cad.onshape.com/documents/a355e772e3d24813de7852ee/w/f106353168f1f92100b81259/e/95d1e1e442b4138cccaf2d73) or on a practice field. The manual does not say how many POLLEN it takes to TIP a HIVE, so test whether the 4-ball preload alone is enough.
