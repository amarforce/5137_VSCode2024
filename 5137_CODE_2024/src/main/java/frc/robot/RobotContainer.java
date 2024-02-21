// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.robot.Commands.*;
import frc.robot.Constants.*;
import frc.robot.Subsystems.*;


import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandPS4Controller;

public class RobotContainer {

  public CommandPS4Controller driver;
  public CommandPS4Controller operator;

  public LED led;
  

  public LED_Commands led_Commands;

  public RobotContainer() {

    driver = new CommandPS4Controller(0);
    operator = new CommandPS4Controller(1);

    led = new LED();

    led_Commands = new LED_Commands(led);

    configureBindings();
  }

  private void configureBindings() {

    operator.square()
    .onTrue(led_Commands.rbow());
    operator.cross()
    .onTrue(led_Commands.purpleGerbert());
    operator.circle()
    .onTrue(led_Commands.barbiePink());
    operator.triangle()
    .onTrue(led_Commands.deepSage());
  }
   
  public Command getAutonomousCommand() {
    return Commands.print("No autonomous command configured");
  }
}
