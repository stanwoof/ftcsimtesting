# previous-year-testing

Robot code for the FTCSIM puzzle **Competition Field 2025–2026: DECODE**
(https://ftcsim.org/puzzle/dqogzcvrcel/).

## How to run

1. Open the puzzle, open the code panel (green `<>` button), and pick the **Onbot Java** tab.
2. Paste a `.java` file from this repo over the editor contents.
3. Press **RUN!** (press **RESET** first if it's already running).

`mode` in `runOpMode()` picks the driver: `0` = keyboard (I/J/K/L drive, P shoots), `1` = gamepad, `2` = autonomous.

## Layout

| Path | What it is |
|---|---|
| `MyFIRSTJavaOpMode_starter.java` | The puzzle's starter Blocks program, auto-translated to Java by FTCSIM. Unmodified. |
| `MyFIRSTJavaOpMode_auto.java` | Autonomous: AprilTag-aligned shots + repeated load/shoot cycles. |

## Robot (from the starter config)

- `driveLeft`, `driveRight`: tank drive (`driveLeft` reversed). `(1,1)` drives toward the goal; `(-1,1)` turns.
- `shootwheel` (motor) + `artifactstopper` (servo: `0.2` = hold, `0` = release one artifact). `shoot()` fires one artifact.
- `webcam` with an AprilTag processor. Sensors not used yet: `color1`, `distance1`, `imu` (BNO055).
- The robot starts preloaded with 3 artifacts. Backing up from the goal ~1.5 s puts it in the loading zone, where the human player loads more.

## DECODE scoring (summary)

| Action | Points |
|---|---|
| LEAVE (auto, off the launch line) | 3 |
| Artifact CLASSIFIED (through the goal onto the ramp) | 3 each |
| Artifact OVERFLOW (through the goal while the ramp is full) | 1 each |
| PATTERN (ramp artifact colour matches the obelisk MOTIF) | 2 each |
| DEPOT (teleop) | 1 each |
| BASE, partially / fully parked (end of match) | 5 / 10 per robot, bonus when both robots are fully in |

- Goal AprilTags: **20 = blue goal**, **24 = red goal**. Obelisk motif tags: **21 = GPP, 22 = PGP, 23 = PPG**.
- The ramp holds 9 artifacts. Opening the GATE empties it so more shots count as CLASSIFIED again.

These values are from the FIRST DECODE game manual as we understand it. Check the [official manual](https://ftc-resources.firstinspires.org/ftc/game/manual-16) for exact rules. In free-play mode the sim doesn't show a score readout, so we judge results by watching artifacts land on the ramp.

## Measured in the sim

- Starter shot pose, as seen from the goal tag (ID 20): `range ≈ 63.9`, `yaw ≈ -9.1`. Shots from this pose land in the blue goal.
- If you square up to `yaw = 0`, every shot misses. The starter's angled pose is the one that scores.
