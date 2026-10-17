package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/**
 * BIOBUZZ (2026-27) autonomous.
 *
 * AUTO points available to one robot (Game Manual TU05, Table 10-2):
 *   LEAVE      3  - no longer touching the perimeter wall at the end of AUTO
 *   PARK       5  - at least partially in our LOADING ZONE at the end of AUTO
 *   HIVE TIP  20  - each time our upward-facing CELL flips from launched POLLEN
 *
 * Plan: back off the wall (LEAVE), launch the 4 preloaded POLLEN into our HIVE's
 * upward CELL (TIP), grab more POLLEN from our GARDEN if time allows and launch
 * again at the CELL that is now facing up (second TIP), then PARK.
 *
 * All field distances are in inches and written for the RED alliance; BLUE
 * mirrors every lateral move and turn. Tune the constants on a real field.
 */
public abstract class BiobuzzAutoBase extends LinearOpMode {

    // ---- Robot geometry / tuning ----
    static final double TICKS_PER_REV = 537.7;          // goBILDA 5203 312 rpm
    static final double WHEEL_DIAMETER_IN = 104.0 / 25.4;
    static final double TICKS_PER_INCH = TICKS_PER_REV / (WHEEL_DIAMETER_IN * Math.PI);
    static final double DRIVE_POWER = 0.7;
    static final double TURN_KP = 0.02;
    static final double HEADING_TOLERANCE_DEG = 2.0;

    static final double LAUNCHER_VELOCITY = 1800;       // ticks/s, tune for CELL distance
    static final double LAUNCHER_VELOCITY_TOLERANCE = 60;
    static final double FEEDER_REST = 0.0;
    static final double FEEDER_PUSH = 0.6;
    static final long FEED_MS = 250;
    static final long RECOVER_MS = 350;
    static final int PRELOAD_POLLEN = 4;

    // ---- Field path (RED; inches / degrees) ----
    static final double LEAVE_DISTANCE = 12;            // drive off the wall
    static final double HIVE_HEADING_1 = 45;            // face the first upward CELL
    static final double HIVE_HEADING_2 = 135;           // face the CELL that is up after a TIP
    static final double GARDEN_STRAFE = 30;
    static final double GARDEN_FORWARD = 20;
    static final double GARDEN_INTAKE_MS = 1500;
    static final int GARDEN_POLLEN = 4;

    // Time budget: AUTO is 30 s. Always keep enough to drive back and park.
    static final double AUTO_SECONDS = 30.0;
    static final double PARK_RESERVE_SECONDS = 5.0;
    static final double SECOND_CYCLE_SECONDS = 11.0;    // garden trip + second volley

    private DcMotorEx frontLeft, frontRight, backLeft, backRight, launcher;
    private DcMotor intake;
    private Servo feeder;
    private IMU imu;
    private final ElapsedTime matchTimer = new ElapsedTime();

    /** +1 for RED, -1 for BLUE. */
    protected abstract int allianceSign();

    @Override
    public void runOpMode() {
        initHardware();
        telemetry.addData("Alliance", allianceSign() > 0 ? "RED" : "BLUE");
        telemetry.addLine("Ready: preload 4 POLLEN, robot against the wall");
        telemetry.update();

        waitForStart();
        matchTimer.reset();
        imu.resetYaw();
        if (isStopRequested()) return;

        // 1. LEAVE (+3): get off the perimeter wall and start the flywheel early.
        launcher.setVelocity(LAUNCHER_VELOCITY);
        drive(LEAVE_DISTANCE, 0);

        // 2. HIVE TIP (+20): empty the preload into the upward CELL.
        turnTo(HIVE_HEADING_1);
        launch(PRELOAD_POLLEN);

        // 3. Second TIP (+20) if there is time for a GARDEN trip and a second volley.
        if (secondsLeft() > SECOND_CYCLE_SECONDS + PARK_RESERVE_SECONDS) {
            turnTo(0);
            drive(GARDEN_FORWARD, GARDEN_STRAFE);
            intake.setPower(1.0);
            sleep((long) GARDEN_INTAKE_MS);
            intake.setPower(0);
            drive(-GARDEN_FORWARD, -GARDEN_STRAFE);
            turnTo(HIVE_HEADING_2);
            launch(GARDEN_POLLEN);
        }

        // 4. PARK (+5): reverse the leave move, stopping short of the wall so we
        //    stay in the LOADING ZONE without losing LEAVE.
        launcher.setVelocity(0);
        turnTo(0);
        drive(-(LEAVE_DISTANCE - 4), 0);
        stopDrive();
        telemetry.addData("Done at", "%.1f s", matchTimer.seconds());
        telemetry.update();
    }

    private void initHardware() {
        frontLeft = hardwareMap.get(DcMotorEx.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotorEx.class, "frontRight");
        backLeft = hardwareMap.get(DcMotorEx.class, "backLeft");
        backRight = hardwareMap.get(DcMotorEx.class, "backRight");
        launcher = hardwareMap.get(DcMotorEx.class, "launcher");
        intake = hardwareMap.get(DcMotor.class, "intake");
        feeder = hardwareMap.get(Servo.class, "feeder");
        imu = hardwareMap.get(IMU.class, "imu");

        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        backLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        for (DcMotorEx m : new DcMotorEx[]{frontLeft, frontRight, backLeft, backRight}) {
            m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
            m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
            m.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        }
        launcher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        feeder.setPosition(FEEDER_REST);

        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD)));
    }

    private double secondsLeft() {
        return AUTO_SECONDS - matchTimer.seconds();
    }

    /** Mecanum move by encoder: +forward, +strafe right (RED frame). */
    private void drive(double forwardIn, double strafeIn) {
        strafeIn *= allianceSign();
        int fl = (int) ((forwardIn + strafeIn) * TICKS_PER_INCH);
        int fr = (int) ((forwardIn - strafeIn) * TICKS_PER_INCH);
        int bl = (int) ((forwardIn - strafeIn) * TICKS_PER_INCH);
        int br = (int) ((forwardIn + strafeIn) * TICKS_PER_INCH);
        DcMotorEx[] motors = {frontLeft, frontRight, backLeft, backRight};
        int[] targets = {fl, fr, bl, br};
        for (int i = 0; i < motors.length; i++) {
            motors[i].setTargetPosition(motors[i].getCurrentPosition() + targets[i]);
            motors[i].setMode(DcMotor.RunMode.RUN_TO_POSITION);
            motors[i].setPower(DRIVE_POWER);
        }
        while (opModeIsActive() && secondsLeft() > 0
                && (frontLeft.isBusy() || frontRight.isBusy() || backLeft.isBusy() || backRight.isBusy())) {
            idle();
        }
        stopDrive();
        for (DcMotorEx m : motors) m.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    /** Turn in place to a field heading (RED frame, CCW positive). */
    private void turnTo(double headingDeg) {
        double target = headingDeg * allianceSign();
        while (opModeIsActive() && secondsLeft() > 0) {
            double error = AngleUnit.normalizeDegrees(target - imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
            if (Math.abs(error) < HEADING_TOLERANCE_DEG) break;
            double turn = Range.clip(error * TURN_KP, -0.5, 0.5);
            frontLeft.setPower(-turn);
            backLeft.setPower(-turn);
            frontRight.setPower(turn);
            backRight.setPower(turn);
        }
        stopDrive();
    }

    /** Fire POLLEN one at a time, waiting for the flywheel to recover between shots. */
    private void launch(int count) {
        launcher.setVelocity(LAUNCHER_VELOCITY);
        for (int i = 0; i < count && opModeIsActive() && secondsLeft() > PARK_RESERVE_SECONDS; i++) {
            ElapsedTime spinUp = new ElapsedTime();
            while (opModeIsActive() && spinUp.milliseconds() < 1500
                    && Math.abs(launcher.getVelocity() - LAUNCHER_VELOCITY) > LAUNCHER_VELOCITY_TOLERANCE) {
                idle();
            }
            feeder.setPosition(FEEDER_PUSH);
            sleep(FEED_MS);
            feeder.setPosition(FEEDER_REST);
            sleep(RECOVER_MS);
        }
    }

    private void stopDrive() {
        frontLeft.setPower(0);
        frontRight.setPower(0);
        backLeft.setPower(0);
        backRight.setPower(0);
    }
}
