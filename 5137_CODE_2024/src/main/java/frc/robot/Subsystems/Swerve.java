package frc.robot.Subsystems;

import frc.robot.Constants.Swerve_Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.MathUtils;
import org.photonvision.PhotonUtils;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import swervelib.SwerveDrive;
import swervelib.parser.SwerveParser;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.PathPlannerAuto;
import com.pathplanner.lib.path.GoalEndState;
import com.pathplanner.lib.path.PathConstraints;
import com.pathplanner.lib.path.PathPlannerPath;
import com.pathplanner.lib.path.RotationTarget;
import com.pathplanner.lib.util.HolonomicPathFollowerConfig;
import com.pathplanner.lib.util.PIDConstants;
import com.pathplanner.lib.util.ReplanningConfig;

public class Swerve extends SubsystemBase {

    private SwerveDrive swerve;
    private SendableChooser<Command> autoChooser;
    private Field2d swerveField;

    private AprilTagFieldLayout aprilTagFieldLayout;
    private PIDController turnController;
    private PIDController driveController;

    private boolean alignToSpeaker;

    public Swerve(File directory) {
        try {
            swerve = new SwerveParser(directory).createSwerveDrive(Swerve_Constants.maxVelocity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
          aprilTagFieldLayout = AprilTagFieldLayout.loadFromResource(AprilTagFields.k2024Crescendo.m_resourceFile);
        } catch (IOException e) {
          e.printStackTrace();
        }

        turnController = new PIDController(Swerve_Constants.turnKP, Swerve_Constants.turnKI, Swerve_Constants.turnKD);
        driveController = new PIDController(Swerve_Constants.driveKP, Swerve_Constants.driveKI, Swerve_Constants.driveKD);
        turnController.setTolerance(0.02, 0.01);
        driveController.setTolerance(0.05, 0.01);
        swerveField = new Field2d();
        SmartDashboard.putData("Swerve Field", swerveField);
        motorInvert();
        swerve.chassisVelocityCorrection = true;
        alignToSpeaker = false;
    }

    public void setUpPathPlanner() {
        AutoBuilder.configureHolonomic(
            this::getPose,
            this::resetOdometry,
            this::getRobotVelocity,
            this::setChassisSpeeds,
            new HolonomicPathFollowerConfig(
                new PIDConstants(1, 0.0, 0.4),
                new PIDConstants(2, 0.0, 0.4),
                Swerve_Constants.maxModuleSpeed,
                swerve.swerveDriveConfiguration.getDriveBaseRadiusMeters(),
                new ReplanningConfig()),
            () -> {
                var alliance = DriverStation.getAlliance();
                if(alliance.isPresent())
                {
                return alliance.get() == DriverStation.Alliance.Red;
                }
                return false;
            },
            this);
            autoChooser = AutoBuilder.buildAutoChooser("Mid2");

        SmartDashboard.putData("Auto Selection", autoChooser);
    }

    public void drive(Translation2d translation2d, double rotationSpeed, boolean fieldRelative) {
        if (alignToSpeaker) {
            swerve.drive(translation2d, getSpeakerAimVelocity(), fieldRelative, true);
        } else {
           swerve.drive(translation2d, rotationSpeed, fieldRelative, true); 
        }
    }

    public Command getAuto() {
        Pose2d autoStartingPose = PathPlannerAuto.getStaringPoseFromAutoFile(autoChooser.getSelected().getName()); 
        if(isRedAlliance()) autoStartingPose = flipPose(autoStartingPose);
        swerve.resetOdometry(autoStartingPose);
        return autoChooser.getSelected();
    }

    public void driveToAmp(){
        Pose2d targetPose = isRedAlliance() ? flipPose(Swerve_Constants.ampAlignPose) : Swerve_Constants.ampAlignPose;       
        PathConstraints constraints = new PathConstraints(3.0, 3.0, Units.degreesToRadians(360), Units.degreesToRadians(720));
        Command pathfindingCommand = AutoBuilder.pathfindToPose(targetPose,constraints,0.0, 0.1);
        pathfindingCommand.addRequirements(this);
        pathfindingCommand.schedule();;
    }

    public void driveToTrap(){
        Pose2d targetPose = isRedAlliance() ? flipPose(Swerve_Constants.trapAlignPose) : Swerve_Constants.trapAlignPose;       
        PathConstraints constraints = new PathConstraints(3.0, 3.0, Units.degreesToRadians(360), Units.degreesToRadians(720));
        Command pathfindingCommand = AutoBuilder.pathfindToPose(targetPose,constraints,0.0, 0.1);
        pathfindingCommand.addRequirements(this);
        pathfindingCommand.schedule();
    }

    public void setChassisSpeeds(ChassisSpeeds velocity) {
        swerve.setChassisSpeeds(velocity);
    }

    public void resetOdometry(Pose2d pose) {
        swerve.resetOdometry(pose);
      }

    public void zeroGyro() {
        swerve.zeroGyro();        
    }

    public ChassisSpeeds getRobotVelocity() {
        return swerve.getRobotVelocity();
    }

    public Pose2d getPose() {
        return swerve.getPose();
    }

    public void addVisionMeasurement(Pose2d pose, double timestamp){ 
        swerve.addVisionMeasurement(pose, timestamp);
    }

    public void setSpeakerAlign(boolean align) {
        this.alignToSpeaker = align;
    }

    public double getSpeakerAimVelocity() {
        return allianceInvert()*turnController.calculate(getRadiansToTarget(),0);
    }

    public boolean turnAligned() {
        return (turnController.atSetpoint());
    }

    public double getRadiansToTarget() {
        Pose2d targetPose = isRedAlliance() ? aprilTagFieldLayout.getTagPose(4).get().toPose2d() : aprilTagFieldLayout.getTagPose(7).get().toPose2d();
        double radiansToPose = MathUtils.normalizeAngle(PhotonUtils.getYawToPose(swerve.getPose(), targetPose).rotateBy(new Rotation2d(Math.toRadians(180))).getRadians(),0);
        return radiansToPose;
    }

    public double getDistanceToTarget() {
        Pose2d targetPose = isRedAlliance() ? aprilTagFieldLayout.getTagPose(4).get().toPose2d() : aprilTagFieldLayout.getTagPose(7).get().toPose2d();
        double distanceToPose = PhotonUtils.getDistanceToPose(swerve.getPose(), targetPose);
        return distanceToPose;
    }

    public void autonomousInit(){
        motorInvert();
    }

    public void motorInvert(){
        boolean invert = isRedAlliance(); 
        for(int i = 0; i < 4; i++){
            swerve.getModules()[i].getDriveMotor().setInverted(invert);
        } 
        //TODO: Check if redesigned method actually inverts properly
    }

    public boolean isRedAlliance(){
        return DriverStation.getAlliance().isPresent() ? DriverStation.getAlliance().get() == DriverStation.Alliance.Red : false; 
    }

    public Pose2d flipPose(Pose2d pose) {
        return new Pose2d(Swerve_Constants.fieldLengthMeters - pose.getX(), pose.getY(), new Rotation2d(Math.PI).minus(pose.getRotation()));
    }

    public double allianceInvert()
    {
        if(isRedAlliance())
        {
            return -1;
        }
        return 1;
    }

    @Override
    public void periodic() {
        SmartDashboard.putString("SwervePose", swerve.getPose().toString());
       //swerveField.setRobotPose(swerve.getPose());
        //SmartDashboard.putNumber("Distance to Speaker", getDistanceToTarget());
    }
}