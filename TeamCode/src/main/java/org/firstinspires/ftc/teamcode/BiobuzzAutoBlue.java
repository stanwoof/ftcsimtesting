package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

@Autonomous(name = "BIOBUZZ Auto Blue", group = "BIOBUZZ")
public class BiobuzzAutoBlue extends BiobuzzAutoBase {
    @Override
    protected int allianceSign() {
        return -1;
    }
}
