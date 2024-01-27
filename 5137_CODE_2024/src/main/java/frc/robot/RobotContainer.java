// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.
package frc.robot;
import frc.robot.Commands.*;
import frc.robot.Subsystems.*;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandPS4Controller;
public class RobotContainer {
  public CommandPS4Controller driver;
  public CommandPS4Controller operator;
  private Intake intake; 
  private Intake_Commands intake_Commands;
  public RobotContainer() {
    driver = new CommandPS4Controller(0);
    operator = new CommandPS4Controller(1);
    intake = new Intake();
    private final Shooter shooter; 
    shooter = new Shooter();

    intake_Commands = new Intake_Commands(intake);

    configureBindings();
  }



  private void configureBindings() {
    operator.square()
    .onTrue(intake_Commands.intakeForward());
    operator.triangle()
    .onTrue(intake_Commands.toStop());
    operator.circle()
    .onTrue(intake_Commands.intakeReverse());
    operator.cross()
    .onTrue(intake_Commands.continuousIntake());

  }

  public Command getAutonomousCommand() {
    return Commands.print("No autonomous command configured");
  }
}