package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

/**
 * BIOBUZZ Auto Lab calibration. Run each test, measure with a tape, and type the results
 * into the Calibrate panel in Auto Lab. Gamepad1: A = 72 in straight, B = 4 x 90 deg spin
 * using encoders only (for track width), X = full-power sprint for max velocity / accel.
 */
@Autonomous(name = "Auto Lab Calibration", group = "Auto Lab")
public class AutoLabCalibration extends LinearOpMode {
    static final double WHEEL_DIAMETER_IN = 4.0940;
    static final double TICKS_PER_REV = 537.70;
    static final double GEAR_RATIO = 1.0000;
    static final double TICKS_PER_IN = TICKS_PER_REV / (GEAR_RATIO * Math.PI * WHEEL_DIAMETER_IN);
    static final double TRACK_GUESS_IN = 16.00;

    DcMotorEx[] left, right;
    IMU imu;

    @Override
    public void runOpMode() {
        left = new DcMotorEx[]{hardwareMap.get(DcMotorEx.class, "left_drive")};
        right = new DcMotorEx[]{hardwareMap.get(DcMotorEx.class, "right_drive")};
        for (DcMotorEx m : left) m.setDirection(DcMotorSimple.Direction.REVERSE);
        for (DcMotorEx m : right) m.setDirection(DcMotorSimple.Direction.FORWARD);
        for (DcMotorEx m : left) prep(m);
        for (DcMotorEx m : right) prep(m);
        imu = hardwareMap.get(IMU.class, "imu");
        imu.initialize(new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD)));
        waitForStart();
        while (opModeIsActive()) {
            telemetry.addLine("A: drive 72 in   B: spin 360 deg (encoders)   X: sprint");
            telemetry.addData("Encoder inches L/R", "%.2f / %.2f", in(left), in(right));
            telemetry.addData("IMU yaw", "%.2f", imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES));
            telemetry.update();
            if (gamepad1.a) straight72();
            if (gamepad1.b) spin360();
            if (gamepad1.x) sprint();
            idle();
        }
    }

    void prep(DcMotorEx m) {
        m.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        m.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        m.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    double in(DcMotorEx[] side) { double s = 0; for (DcMotorEx m : side) s += m.getCurrentPosition(); return s / side.length / TICKS_PER_IN; }
    void vel(double l, double r) { for (DcMotorEx m : left) m.setVelocity(l * TICKS_PER_IN); for (DcMotorEx m : right) m.setVelocity(r * TICKS_PER_IN); }

    /** Measure the real distance with a tape. DIST_SCALE = (encoder inches shown) / (tape inches). */
    void straight72() {
        imu.resetYaw();
        double l0 = in(left), r0 = in(right);
        while (opModeIsActive() && ((in(left) - l0) + (in(right) - r0)) / 2 < 72) vel(20, 20);
        vel(0, 0);
        sleep(500);
        double enc = ((in(left) - l0) + (in(right) - r0)) / 2;
        double drift = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
        report("Straight: encoder in", enc, "Heading drift deg", drift);
    }

    /** Spin until encoders say 360 deg at TRACK_GUESS. TRACK_EFF = TRACK_GUESS * 360 / (IMU degrees turned). */
    void spin360() {
        imu.resetYaw();
        double l0 = in(left), r0 = in(right), arc = Math.PI * TRACK_GUESS_IN;
        double turned = 0, last = 0;
        while (opModeIsActive() && ((in(right) - r0) - (in(left) - l0)) / 2 < arc) {
            vel(-12, 12);
            double y = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
            double d = y - last; if (d < -180) d += 360; if (d > 180) d -= 360;
            turned += d; last = y;
        }
        vel(0, 0);
        sleep(500);
        double y = imu.getRobotYawPitchRollAngles().getYaw(AngleUnit.DEGREES);
        double d = y - last; if (d < -180) d += 360; if (d > 180) d -= 360; turned += d;
        report("Spin: IMU degrees", turned, "Suggested TRACK_EFF in", TRACK_GUESS_IN * 360.0 / turned);
    }

    /** Full power for 1.5 s. Reports peak velocity and time to 90% of it. */
    void sprint() {
        ElapsedTime t = new ElapsedTime();
        double peak = 0, t90 = 0;
        for (DcMotorEx m : left) m.setPower(1);
        for (DcMotorEx m : right) m.setPower(1);
        double[] v = new double[200]; double[] ts = new double[200]; int n = 0;
        while (opModeIsActive() && t.seconds() < 1.5 && n < 200) {
            double s = 0; for (DcMotorEx m : left) s += m.getVelocity(); for (DcMotorEx m : right) s += m.getVelocity();
            v[n] = s / (left.length + right.length) / TICKS_PER_IN; ts[n] = t.seconds();
            peak = Math.max(peak, v[n]); n++;
            sleep(5);
        }
        for (DcMotorEx m : left) m.setPower(0);
        for (DcMotorEx m : right) m.setPower(0);
        for (int i = 0; i < n; i++) if (v[i] >= 0.9 * peak) { t90 = ts[i]; break; }
        report("Peak velocity in/s", peak, "Approx accel in/s^2", t90 > 0 ? 0.9 * peak / t90 : 0);
    }

    void report(String a, double av, String b, double bv) {
        while (opModeIsActive() && !gamepad1.y) {
            telemetry.addData(a, "%.2f", av);
            telemetry.addData(b, "%.3f", bv);
            telemetry.addLine("Write these down, press Y to continue");
            telemetry.update();
            idle();
        }
    }
}
