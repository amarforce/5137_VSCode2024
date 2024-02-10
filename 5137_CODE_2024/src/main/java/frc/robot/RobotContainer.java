// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.Commands.*;
import frc.robot.Constants.*;
import frc.robot.Subsystems.*;

import java.io.File;

import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandPS4Controller;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;

public class RobotContainer {

  public static CommandPS4Controller driver;
  public static CommandXboxController operator;

  public static Arm arm;

  public static Arm_Commands arm_Commands;

  public RobotContainer() {

    driver = new CommandPS4Controller(0);
    operator = new CommandXboxController(1);
    arm = new Arm(new File(Filesystem.getDeployDirectory(), "RobotConstants.json"));

    arm_Commands = new Arm_Commands(arm);

    configureBindings();
  }

  private void configureBindings() {
    // Operator Bindings
    //arm.setDefaultCommand(arm_Commands.manualMove(() -> -operator.getLeftY()));

    operator.x()
    .onTrue(arm_Commands.moveToIntake());

    operator.y()
    .onTrue(arm_Commands.moveToDefault());

    operator.b()
    .onTrue(arm_Commands.moveToAmp());

    operator.a()
    .onTrue(arm_Commands.moveToSpeaker());
  }

  public Command getAutonomousCommand() {
    return Commands.print("No autonomous command configured");
  }
}
