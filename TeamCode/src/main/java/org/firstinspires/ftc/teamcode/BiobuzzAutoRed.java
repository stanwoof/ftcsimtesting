package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "BIOBUZZ Auto Red", group = "BIOBUZZ")
public class BiobuzzAutoRed extends BiobuzzAutoBase {
    @Override
    protected int allianceSign() {
        return 1;
    }
}
