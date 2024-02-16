// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package frc.robot;
import frc.robot.Commands.*;
import frc.robot.Constants.Swerve_Constants;
import frc.robot.Subsystems.*;

import java.io.File;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ParallelCommandGroup;
import edu.wpi.first.wpilibj2.command.button.CommandPS4Controller;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;

public class RobotContainer {

  private CommandPS4Controller driver;
  private CommandXboxController operator;

  private Swerve swerve;
  private Arm arm;
  private Intake intake; 
  private Shooter shooter;
  private Vision vision;

  private Swerve_Commands swerve_Commands;
  private Arm_Commands arm_Commands;
  private Intake_Commands intake_Commands;
  private Shooter_Commands shooter_Commands;

  public RobotContainer() {
    driver = new CommandPS4Controller(0);
    operator = new CommandXboxController(1);

    swerve = new Swerve(new File(Filesystem.getDeployDirectory(),"swerve"));
    arm = new Arm(new File(Filesystem.getDeployDirectory(), "RobotConstants.json"));
    intake = new Intake();
    shooter = new Shooter();
    vision = new Vision();

    swerve_Commands = new Swerve_Commands(swerve);
    arm_Commands = new Arm_Commands(arm);
    intake_Commands = new Intake_Commands(intake);
    shooter_Commands = new Shooter_Commands(shooter);
    vision.setDefaultCommand(new AddVisionMeasurement(vision, swerve));

    configureBindings();
  }



  private void configureBindings() {

    //Swerve Bindings

    swerve.setDefaultCommand(swerve_Commands.drive(
      () -> MathUtil.applyDeadband(driver.getLeftX(), Swerve_Constants.LX_Deadband),
      () -> -MathUtil.applyDeadband(driver.getLeftY(), Swerve_Constants.LY_Deadband),
      () -> MathUtil.applyDeadband(driver.getRightX(), Swerve_Constants.RX_Deadband),
      () -> !driver.L1().getAsBoolean()
    ));

    driver.cross()
    .onTrue(swerve_Commands.aimAtTarget());

    driver.triangle()
    .onTrue(swerve_Commands.zeroGyro());

    // Arm Bindings

    arm.setDefaultCommand(arm_Commands.manualMove(() -> operator.getLeftY()));

    operator.b()
    .onTrue(arm_Commands.moveToAmp());

    operator.x()
    .onTrue(arm_Commands.moveToSpeaker());

    operator.y()
    .onTrue(arm_Commands.moveToIntake());

    // Intake/Shooter Bindings

    operator.rightBumper()
    .onTrue(intake_Commands.continuousIntake());
    
    operator.leftBumper()
    .onTrue(intake_Commands.intakeReverse())
    .onFalse(intake_Commands.stop());

    operator.a()
    .onTrue(new ParallelCommandGroup(shooter_Commands.shoot(arm.getMeasurement()), intake_Commands.intakeForward(1.5)))
    .onFalse(new ParallelCommandGroup(shooter_Commands.stop(), intake_Commands.stop()));
  }

  public Command getAutonomousCommand() {
    return swerve_Commands.runAuto("StartToMid");
  }
}