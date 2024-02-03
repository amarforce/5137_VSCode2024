package frc.robot.Subsystems;

import frc.robot.Robot;
import frc.robot.Constants.Arm_Constants;

import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.TrapezoidProfile.State;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismRoot2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;

import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.sim.CANcoderSimState;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkBase.IdleMode;
import com.revrobotics.CANSparkLowLevel.MotorType;

public class Arm extends ProfiledPIDSubsystem {
    private CANSparkMax leftMotor;
    private CANSparkMax rightMotor;
    private CANcoder canCoder;
    private ArmFeedforward feedForward;

    private CANcoderSimState canCoderSim;

    private Mechanism2d armSimMech;
    private MechanismRoot2d armSimRoot;
    private MechanismLigament2d armSim;

    public Arm() {
        super(
            new ProfiledPIDController(
                Arm_Constants.kP,
                Arm_Constants.kI,
                Arm_Constants.kD,
                new TrapezoidProfile.Constraints(
                Arm_Constants.kMaxVelocity,
                Arm_Constants.kMaxAcceleration)),
            0.0);

        leftMotor = new CANSparkMax(Arm_Constants.leftMotorID, MotorType.kBrushless);
        rightMotor = new CANSparkMax(Arm_Constants.rightMotorID, MotorType.kBrushless);

        leftMotor.setSmartCurrentLimit(Arm_Constants.maxSupplyCurrent);
        rightMotor.setSmartCurrentLimit(Arm_Constants.maxSupplyCurrent);

        leftMotor.setIdleMode(IdleMode.kBrake);
        rightMotor.setIdleMode(IdleMode.kBrake);

        leftMotor.setInverted(true);

        canCoder = new CANcoder(Arm_Constants.canCoderID);
        feedForward = new ArmFeedforward(
            Arm_Constants.kS,
            Arm_Constants.kG,
            Arm_Constants.kV,
            Arm_Constants.kA
        );

        if (Robot.isSimulation()) {
            canCoder.setPosition(0.0);
            canCoderSim = canCoder.getSimState();
            armSimMech = new Mechanism2d(10, 10, new Color8Bit(Color.kBlack));
            armSimRoot = armSimMech.getRoot("ArmRoot", 5, 0);
            armSim = armSimRoot.append(new MechanismLigament2d("Arm", 5, 105, 10, new Color8Bit(Color.kRed)));
            SmartDashboard.putData("Arm Sim", armSimMech);
        }

        setGoal(0.0);
    }

    @Override
    public void useOutput(double output, State setpoint) {
        double feed = feedForward.calculate(setpoint.position, setpoint.velocity);
        leftMotor.setVoltage(output + feed);
        rightMotor.setVoltage(output + feed);
        
        if (Robot.isSimulation()) {
            canCoderSim.setVelocity(setpoint.velocity);
            canCoderSim.addPosition(Math.toDegrees(setpoint.velocity)*0.02);
            armSim.setAngle(-Math.toDegrees(getMeasurement())+180);
        }
    }

    @Override
    public double getMeasurement() {
        return Math.toRadians(canCoder.getPosition().getValueAsDouble());
    }

    @Override 
    public void simulationPeriodic() {
        useOutput(super.m_controller.calculate(getMeasurement()), super.m_controller.getSetpoint());
    }

    public void runManual(double output) {
        leftMotor.set(0.3*output);
        rightMotor.set(0.3*output);
    }

    public double getGoal() {
        return super.m_controller.getGoal().position;
    }

    public boolean getMovementFinished() {
        return (Math.abs(this.getMeasurement() - super.m_controller.getGoal().position)) < Arm_Constants.errorMargin;
    }

    public void release() {
        leftMotor.setIdleMode(IdleMode.kCoast);
        rightMotor.setIdleMode(IdleMode.kCoast);
    }

    @Override
    public void periodic() {
        //System.out.println("Measure: "+this.getMeasurement()+", Goal: "+this.getGoal());
        //useOutput(super.m_controller.calculate(getMeasurement()), super.m_controller.getSetpoint());
    }
}