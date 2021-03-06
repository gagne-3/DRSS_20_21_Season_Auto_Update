package org.firstinspires.ftc.teamcode.Test_Code.Previous_Code.Auto_Tests.Odometry;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

public class RunOdometry{
	HardwareMap hwMap;

	/*
	* Encoder Start Positons (Program Zero Point)
	*/
	int leftEncoderStartPosition = 0;
	int rightEncoderStartPosition = 0;
	int centerEncoderStartPosition = 0;

	/**
	 * Encoder Relative Zero Positions (Incremental)
	 */
	double leftEncoderRelativeZero = 0;
	double rightEncoderRelativeZero = 0;
	double centerEncoderRelativeZero = 0;

	/**
	 * Encoder Delta Position
	 */
	double leftEncoderDelta = 0;
	double rightEncoderDelta = 0;
	double centerEncoderDelta = 0;

	/**
	 * Encoder Target Position
	 */
	double leftEncoderTarget = 0;
	double rightEncoderTarget = 0;
	double centerEncoderTarget = 0;

	public static final double     COUNTS_PER_MOTOR_REV    = 1440;    // eg: E8T-360-250-S-D-D-B Encoder
	public static final double     WHEEL_DIAMETER_INCHES   = 1.0;     // For figuring circumference
	public static final double     COUNTS_PER_INCH         = (COUNTS_PER_MOTOR_REV) / (WHEEL_DIAMETER_INCHES * 3.1415);

	/**
	 * Absolute Positions
	 */
	double x = 0;
	double y = 0;

	/**
	 * plug left encoder into frontleft, right encoder into frontright, center encoder into backleft (arbitary assignments)
	 */
	public DcMotor FR = null;
	public DcMotor FL = null;
	public DcMotor BR = null;
	public DcMotor BL = null;
	private DcMotor leftEncoder = null;
	private DcMotor rightEncoder = null;
	private DcMotor centerEncoder = null;

	double FRPOWER = 0;
	double FLPOWER = 0;
	double BRPOWER = 0;
	double BLPOWER = 0;

	public void init(HardwareMap ahwMap, Boolean initEncoders) {
		hwMap = ahwMap;

		/**
		 * Init Motors
		 */
		FR = hwMap.get(DcMotor.class, "FR");
		FL = hwMap.get(DcMotor.class, "FL");
		BR = hwMap.get(DcMotor.class, "BR");
		BL = hwMap.get(DcMotor.class, "BL");

		FR.setDirection(DcMotorSimple.Direction.REVERSE);
		FL.setDirection(DcMotorSimple.Direction.REVERSE);
		BR.setDirection(DcMotorSimple.Direction.REVERSE);
		BL.setDirection(DcMotorSimple.Direction.REVERSE);

		/**
		 * Init Encoders
		 */
		leftEncoder = hwMap.get(DcMotor.class, "FL");
		rightEncoder = hwMap.get(DcMotor.class, "FR");
		centerEncoder = hwMap.get(DcMotor.class, "BL");

		leftEncoder.setDirection(DcMotorSimple.Direction.REVERSE);
		rightEncoder.setDirection(DcMotorSimple.Direction.FORWARD);
		centerEncoder.setDirection(DcMotorSimple.Direction.FORWARD);

		/**
		 * Init Encoder Variables
		 */
		if (initEncoders) {
			zeroEncoders();
		}
	}

	/**
	 * Run Initialization Program (With Encoder Init)
	 */
	public void init(HardwareMap ahwMap) {
		init(ahwMap, true);
	}

	public double getLeftTicks() {
		double counts = leftEncoder.getCurrentPosition() - leftEncoderStartPosition;
		return counts;
	}

	public double getRightTicks() {
		double counts = rightEncoder.getCurrentPosition() - rightEncoderStartPosition;
		return counts;
	}
	public double getCenterTicks() {
		double counts = centerEncoder.getCurrentPosition() - centerEncoderStartPosition;
		return counts;
	}

	public void zeroEncoders() { //Get the starting position of the encoders
		leftEncoderStartPosition = leftEncoder.getCurrentPosition();
		rightEncoderStartPosition = rightEncoder.getCurrentPosition();
		centerEncoderStartPosition = centerEncoder.getCurrentPosition();
	}

	public void setRelative() { //Get the position of the encoders at the time before the movement
		leftEncoderRelativeZero = leftEncoder.getCurrentPosition() - leftEncoderStartPosition;
		rightEncoderRelativeZero = rightEncoder.getCurrentPosition() - rightEncoderStartPosition;
		centerEncoderRelativeZero = centerEncoder.getCurrentPosition() - centerEncoderStartPosition;
	}

	public void setMotorPower(double frontLeftPow, double frontRightPow, double backLeftPow, double backRightPow) { //Sets power to the motors
		FL.setPower(frontLeftPow);
		FR.setPower(frontRightPow);
		BL.setPower(backLeftPow);
		BR.setPower(backRightPow);
	}

	public float findPower(double currentReading, double targetPos, double startPos) { //Calculates power for movement
		targetPos -= startPos; //Sets our reading relative to our starting position
		currentReading -= startPos; //Sets our reading relative to our starting position

		if (Math.abs(currentReading-targetPos) <= 360) {
			return 0;
		}

		double power = 1-(Math.abs(currentReading)/targetPos); //Calculate motor power

		double finalPower = Range.clip((power+0.25),.25,1); //Control the range of powers we are able to give the motors
		return (float) finalPower; //Return our calculated power
	}

	public double inchToCount(double inches) { //Converts inches to counts
		double count = inches * COUNTS_PER_INCH;
		return count;
	}

	public double averageEncoders() { //Averages the counts of left and right encoders
		double averageEncoderCount = (leftEncoder.getCurrentPosition() + rightEncoder.getCurrentPosition()) / 2;
		double averageStartCount = (leftEncoderStartPosition + rightEncoderStartPosition) / 2;

		double averageCount = averageEncoderCount - averageStartCount();

		return averageCount;
	}

	public double averageStartCount() { //Averages the starting position of left and right encoders
		double averageStartCount = (leftEncoderStartPosition + rightEncoderStartPosition) / 2;

		return averageStartCount;
	}

	public double averageRelativeZero() { //Averages the relative position of left and right encoders
		double averageRelativeZero = (leftEncoderRelativeZero + rightEncoderRelativeZero) / 2;

		return averageRelativeZero;
	}

	public void runToPosition(double inchX, double inchY, double speed) {
		float powerX = 1;
		float powerY = 1;

		double targetCountX = inchToCount(inchX);
		//double targetCountY = inchToCount(inchY)-averageStartCount();
		double targetCountY = inchToCount(inchY);

		setRelative();

		while (powerX != 0 && powerY != 0) {
			double CountX = centerEncoder.getCurrentPosition()-centerEncoderStartPosition;
			//double CountY = averageEncoders();
			double CountY = rightEncoder.getCurrentPosition()-rightEncoderStartPosition;

			powerX = -(findPower(CountX, targetCountX, centerEncoderRelativeZero));
			//powerY = findPower(CountY, targetCountY, averageRelativeZero());
			powerY = (findPower(CountY, targetCountY, rightEncoderRelativeZero));

			/*double FrontRight = powerY - powerX;
			double FrontLeft = -powerY - powerX;
			double BackRight = powerY + powerX;
			double BackLeft = -powerY + powerX;*/

			double FrontRight = powerX - powerY;
			double FrontLeft = -powerX - powerY;
			double BackRight = powerX + powerY;
			double BackLeft = -powerX + powerY;

			FrontRight = Range.clip(Math.pow(FrontRight, 3), -speed, speed);
			FrontLeft = Range.clip(Math.pow(FrontLeft, 3), -speed, speed);
			BackRight = Range.clip(Math.pow(BackRight, 3), -speed, speed);
			BackLeft = Range.clip(Math.pow(BackLeft, 3), -speed, speed);

			FRPOWER = FrontRight;
			FLPOWER = FrontLeft;
			BRPOWER = BackRight;
			BLPOWER = BackLeft;

			setMotorPower(FrontLeft, FrontRight, BackLeft, BackRight);
		}

		setMotorPower(0, 0, 0, 0);

		x = centerEncoder.getCurrentPosition() - centerEncoderStartPosition;
		//y = averageEncoders();
		y = rightEncoder.getCurrentPosition() - rightEncoderStartPosition;

		return;
	}

	double sendPower (double motor) {
		if (motor == 1) {
			return FRPOWER;
		}

		if (motor == 2) {
			return FLPOWER;
		}

		if (motor == 3) {
			return BLPOWER;
		}

		if (motor == 4) {
			return BRPOWER;
		}

		return 69;
	}
}
