package junit.bowlerstudio;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import javax.xml.transform.TransformerFactory;

import org.apache.commons.math3.geometry.euclidean.threed.RotationConvention;
import org.apache.commons.math3.geometry.euclidean.threed.RotationOrder;
import org.junit.Test;

import com.bulletphysics.linearmath.Transform;
import com.neuronrobotics.bowlerstudio.physics.TransformFactory;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNR;
import com.neuronrobotics.sdk.addons.kinematics.math.RotationNRLegacy;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.common.Log;

public class RotationTest {
	RotationOrder[] list = { RotationOrder.ZYX
			// RotationOrder.XZY,
			// RotationOrder.YXZ,
			// RotationOrder.YZX,
			// RotationOrder.ZXY, RotationOrder.ZYX, RotationOrder.XYX,
			// RotationOrder.XZX, RotationOrder.YXY,
			// RotationOrder.YZY, RotationOrder.ZXZ, RotationOrder.ZYZ
	};
	RotationConvention[] conventions = { RotationConvention.VECTOR_OPERATOR };

	/**
	 * Test.
	 * 
	 * @throws FileNotFoundException
	 */
	@Test
	public void compareAffine() throws FileNotFoundException {
		int failCount = 0;
		int iterations = 100;

		Log.enableDebugPrint();
		for (RotationConvention conv : conventions) {
			RotationNR.setConvention(conv);
			System.out.println("\n\nUsing convention " + conv.toString());
			for (RotationOrder ro : list) {
				RotationNR.setOrder(ro);
				System.out.println("\n\nUsing rotationOrder " + ro.toString());
				failCount = 0;
				for (int i = 0; i < iterations; i++) {

					double rotationAngleDegrees = (Math.random() * 360) - 180;

					double rotationAngleRadians = Math.PI / 180 * rotationAngleDegrees;

					double[][] rotation = new double[3][3];
					// Rotation matrix, 1st column
					rotation[0][0] = Math.cos(rotationAngleRadians);
					rotation[1][0] = Math.sin(rotationAngleRadians);
					rotation[2][0] = 0;
					// Rotation matrix, 2nd column
					rotation[0][1] = -Math.sin(rotationAngleRadians);
					rotation[1][1] = Math.cos(rotationAngleRadians);
					rotation[2][1] = 0;
					// Rotation matrix, 3rd column
					rotation[0][2] = 0;
					rotation[1][2] = 0;
					rotation[2][2] = 1;
					// pure rotation in azumuth
					RotationNR newRot = new RotationNR(rotation);
					// Convert to Affine and back
					RotationNR oldRot = TransformFactory
							.affineToNr(TransformFactory.nrToAffine(new TransformNR(0, 0, 0, newRot))).getRotation();
					double[][] rotationMatrix = newRot.getRotationMatrix();
					System.out.println("Testing pure azumeth \nrotation " + rotationAngleDegrees + "\n as radian "
							+ Math.toRadians(rotationAngleDegrees) + "\n     Az " + oldRot.getRotationAzimuth()
							+ "\n     El " + oldRot.getRotationElevation() + "\n     Tl " + oldRot.getRotationTilt()
							+ "\n New Az " + newRot.getRotationAzimuth() + "\n New El " + newRot.getRotationElevation()
							+ "\n New Tl " + newRot.getRotationTilt());
					assertArrayEquals(rotation[0], rotationMatrix[0], 0.001);
					assertArrayEquals(rotation[1], rotationMatrix[1], 0.001);
					assertArrayEquals(rotation[2], rotationMatrix[2], 0.001);

					System.out.println(
							"Testing Quaturnion \nrotation " + "\n     qw " + oldRot.getRotationMatrix2QuaturnionW()
									+ "\n     qx " + oldRot.getRotationMatrix2QuaturnionX() + "\n     qy "
									+ oldRot.getRotationMatrix2QuaturnionY() + "\n     qz "
									+ oldRot.getRotationMatrix2QuaturnionZ() + "\nNEW  qw "
									+ newRot.getRotationMatrix2QuaturnionW() + "\nNEW  qx "
									+ newRot.getRotationMatrix2QuaturnionX() + "\nNEW  qy "
									+ newRot.getRotationMatrix2QuaturnionY() + "\nNEW  qz "
									+ newRot.getRotationMatrix2QuaturnionZ());
					assertArrayEquals(
							new double[] { Math.abs(oldRot.getRotationMatrix2QuaturnionW()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionX()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionY()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionZ()), },
							new double[] { Math.abs(newRot.getRotationMatrix2QuaturnionW()),
									Math.abs(newRot.getRotationMatrix2QuaturnionX()),
									Math.abs(newRot.getRotationMatrix2QuaturnionY()),
									Math.abs(newRot.getRotationMatrix2QuaturnionZ()), },
							0.001);
					// Check Euler angles

					assertArrayEquals(
							new double[] { oldRot.getRotationAzimuth(), oldRot.getRotationElevation(),
									oldRot.getRotationTilt() },
							new double[] { newRot.getRotationAzimuth(), newRot.getRotationElevation(),
									newRot.getRotationTilt() },
							0.001);
					// Check the old rotation against the known value
					assertArrayEquals(new double[] { Math.toRadians(rotationAngleDegrees), 0, 0 }, new double[] {
							oldRot.getRotationAzimuth(), oldRot.getRotationElevation(), oldRot.getRotationTilt() },
							0.001);

					// Check the new rotation against the known value
					assertArrayEquals(new double[] { Math.toRadians(rotationAngleDegrees), 0, 0 }, new double[] {
							newRot.getRotationAzimuth(), newRot.getRotationElevation(), newRot.getRotationTilt() },
							0.001);
				}
				// frame();
				// frame2();
				System.out.println("Frame test passed with " + ro);
				// return;
			}
		}
	}

	/**
	 * Test.
	 * 
	 * @throws FileNotFoundException
	 */
	@Test
	public void compareCSG() throws FileNotFoundException {
		int failCount = 0;
		int iterations = 100;
		Log.enableDebugPrint();
		for (RotationConvention conv : conventions) {
			for (RotationOrder ro : list) {
				failCount = 0;
				for (int i = 0; i < iterations; i++) {

					double rotationAngleDegrees = (Math.random() * 360) - 180;

					double rotationAngleRadians = Math.PI / 180 * rotationAngleDegrees;

					double[][] rotation = new double[3][3];
					// Rotation matrix, 1st column
					rotation[0][0] = Math.cos(rotationAngleRadians);
					rotation[1][0] = Math.sin(rotationAngleRadians);
					rotation[2][0] = 0;
					// Rotation matrix, 2nd column
					rotation[0][1] = -Math.sin(rotationAngleRadians);
					rotation[1][1] = Math.cos(rotationAngleRadians);
					rotation[2][1] = 0;
					// Rotation matrix, 3rd column
					rotation[0][2] = 0;
					rotation[1][2] = 0;
					rotation[2][2] = 1;
					// pure rotation in azumuth
					RotationNR newRot = new RotationNR(rotation);
					// Convert to Affine and back
					RotationNR oldRot = TransformFactory
							.csgToNR(TransformFactory.nrToCSG(new TransformNR(0, 0, 0, newRot))).getRotation();
					double[][] rotationMatrix = newRot.getRotationMatrix();
					System.out.println("\n\nTesting pure azumeth \nrotation " + rotationAngleDegrees + "\n as radian "
							+ Math.toRadians(rotationAngleDegrees) + "\n     Az " + oldRot.getRotationAzimuth()
							+ "\n     El " + oldRot.getRotationElevation() + "\n     Tl " + oldRot.getRotationTilt()
							+ "\n New Az " + newRot.getRotationAzimuth() + "\n New El " + newRot.getRotationElevation()
							+ "\n New Tl " + newRot.getRotationTilt());
					assertArrayEquals(rotation[0], rotationMatrix[0], 0.001);
					assertArrayEquals(rotation[1], rotationMatrix[1], 0.001);
					assertArrayEquals(rotation[2], rotationMatrix[2], 0.001);

					System.out.println(
							"Testing Quaturnion \nrotation " + "\n     qw " + oldRot.getRotationMatrix2QuaturnionW()
									+ "\n     qx " + oldRot.getRotationMatrix2QuaturnionX() + "\n     qy "
									+ oldRot.getRotationMatrix2QuaturnionY() + "\n     qz "
									+ oldRot.getRotationMatrix2QuaturnionZ() + "\nNEW  qw "
									+ newRot.getRotationMatrix2QuaturnionW() + "\nNEW  qx "
									+ newRot.getRotationMatrix2QuaturnionX() + "\nNEW  qy "
									+ newRot.getRotationMatrix2QuaturnionY() + "\nNEW  qz "
									+ newRot.getRotationMatrix2QuaturnionZ());
					assertArrayEquals(
							new double[] { Math.abs(oldRot.getRotationMatrix2QuaturnionW()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionX()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionY()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionZ()), },
							new double[] { Math.abs(newRot.getRotationMatrix2QuaturnionW()),
									Math.abs(newRot.getRotationMatrix2QuaturnionX()),
									Math.abs(newRot.getRotationMatrix2QuaturnionY()),
									Math.abs(newRot.getRotationMatrix2QuaturnionZ()), },
							0.001);
					// Check Euler angles

					assertArrayEquals(
							new double[] { oldRot.getRotationAzimuth(), oldRot.getRotationElevation(),
									oldRot.getRotationTilt() },
							new double[] { newRot.getRotationAzimuth(), newRot.getRotationElevation(),
									newRot.getRotationTilt() },
							0.001);
					// Check the old rotation against the known value
					assertArrayEquals(new double[] { Math.toRadians(rotationAngleDegrees), 0, 0 }, new double[] {
							oldRot.getRotationAzimuth(), oldRot.getRotationElevation(), oldRot.getRotationTilt() },
							0.001);

					// Check the new rotation against the known value
					assertArrayEquals(new double[] { Math.toRadians(rotationAngleDegrees), 0, 0 }, new double[] {
							newRot.getRotationAzimuth(), newRot.getRotationElevation(), newRot.getRotationTilt() },
							0.001);
				}
				// frame();
				// frame2();
				System.out.println("Frame test passed with " + ro);
				// return;
			}
		}
	}

	@Test
	public void compareBullet() throws FileNotFoundException {
		int failCount = 0;
		int iterations = 100;
		Log.enableDebugPrint();
		for (RotationConvention conv : conventions) {
			RotationNR.setConvention(conv);
			System.out.println("\n\nUsing convention " + conv.toString());
			for (RotationOrder ro : list) {
				RotationNR.setOrder(ro);
				System.out.println("\n\nUsing rotationOrder " + ro.toString());
				failCount = 0;
				for (int i = 0; i < iterations; i++) {

					double rotationAngleDegrees = (Math.random() * 360) - 180;

					double rotationAngleRadians = Math.PI / 180 * rotationAngleDegrees;

					double[][] rotation = new double[3][3];
					// Rotation matrix, 1st column
					rotation[0][0] = Math.cos(rotationAngleRadians);
					rotation[1][0] = Math.sin(rotationAngleRadians);
					rotation[2][0] = 0;
					// Rotation matrix, 2nd column
					rotation[0][1] = -Math.sin(rotationAngleRadians);
					rotation[1][1] = Math.cos(rotationAngleRadians);
					rotation[2][1] = 0;
					// Rotation matrix, 3rd column
					rotation[0][2] = 0;
					rotation[1][2] = 0;
					rotation[2][2] = 1;
					// pure rotation in azumuth
					RotationNR newRot = new RotationNR(rotation);
					// Convert to Affine and back
					Transform bulletTrans = new Transform();
					TransformFactory.nrToBullet(new TransformNR(0, 0, 0, newRot), bulletTrans);
					RotationNR oldRot = TransformFactory.bulletToNr(bulletTrans).getRotation();
					double[][] rotationMatrix = newRot.getRotationMatrix();
					System.out.println("Testing pure azumeth \nrotation " + rotationAngleDegrees + "\n as radian "
							+ Math.toRadians(rotationAngleDegrees) + "\n     Az " + oldRot.getRotationAzimuth()
							+ "\n     El " + oldRot.getRotationElevation() + "\n     Tl " + oldRot.getRotationTilt()
							+ "\n New Az " + newRot.getRotationAzimuth() + "\n New El " + newRot.getRotationElevation()
							+ "\n New Tl " + newRot.getRotationTilt());
					assertArrayEquals(rotation[0], rotationMatrix[0], 0.001);
					assertArrayEquals(rotation[1], rotationMatrix[1], 0.001);
					assertArrayEquals(rotation[2], rotationMatrix[2], 0.001);

					System.out.println(
							"Testing Quaturnion \nrotation " + "\n     qw " + oldRot.getRotationMatrix2QuaturnionW()
									+ "\n     qx " + oldRot.getRotationMatrix2QuaturnionX() + "\n     qy "
									+ oldRot.getRotationMatrix2QuaturnionY() + "\n     qz "
									+ oldRot.getRotationMatrix2QuaturnionZ() + "\nNEW  qw "
									+ newRot.getRotationMatrix2QuaturnionW() + "\nNEW  qx "
									+ newRot.getRotationMatrix2QuaturnionX() + "\nNEW  qy "
									+ newRot.getRotationMatrix2QuaturnionY() + "\nNEW  qz "
									+ newRot.getRotationMatrix2QuaturnionZ());
					assertArrayEquals(
							new double[] { Math.abs(oldRot.getRotationMatrix2QuaturnionW()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionX()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionY()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionZ()), },
							new double[] { Math.abs(newRot.getRotationMatrix2QuaturnionW()),
									Math.abs(newRot.getRotationMatrix2QuaturnionX()),
									Math.abs(newRot.getRotationMatrix2QuaturnionY()),
									Math.abs(newRot.getRotationMatrix2QuaturnionZ()), },
							0.001);
					// Check Euler angles
					// this check is needed to work around a known bug in the
					// legact implementation

					assertArrayEquals(
							new double[] { oldRot.getRotationAzimuth(), oldRot.getRotationElevation(),
									oldRot.getRotationTilt() },
							new double[] { newRot.getRotationAzimuth(), newRot.getRotationElevation(),
									newRot.getRotationTilt() },
							0.001);
					// Check the old rotation against the known value
					assertArrayEquals(new double[] { Math.toRadians(rotationAngleDegrees), 0, 0 }, new double[] {
							oldRot.getRotationAzimuth(), oldRot.getRotationElevation(), oldRot.getRotationTilt() },
							0.001);

					// Check the new rotation against the known value
					assertArrayEquals(new double[] { Math.toRadians(rotationAngleDegrees), 0, 0 }, new double[] {
							newRot.getRotationAzimuth(), newRot.getRotationElevation(), newRot.getRotationTilt() },
							0.001);
				}
				// frame();
				// frame2();
				System.out.println("Frame test passed with " + ro);
				// return;
			}
		}
	}

	/**
	 * Test.
	 * 
	 * @throws FileNotFoundException
	 */
	@Test
	public void compareAffineElevation() throws FileNotFoundException {
		int failCount = 0;
		int iterations = 100;

		Log.enableDebugPrint();
		for (RotationConvention conv : conventions) {
			RotationNR.setConvention(conv);
			System.out.println("\n\nUsing convention " + conv.toString());
			for (RotationOrder ro : list) {
				RotationNR.setOrder(ro);
				System.out.println("\n\nUsing rotationOrder " + ro.toString());
				failCount = 0;
				for (int i = 0; i < iterations; i++) {

					double rotationAngleDegrees = (Math.random() * 180) - 90;

					double rotationAngleRadians = Math.PI / 180 * rotationAngleDegrees;


					double[][] rotation = new double[3][3];
					// Rotation matrix, 1st column
					rotation[0][0] = Math.cos(rotationAngleRadians);
					rotation[1][0] = 0;
					rotation[2][0] = -Math.sin(rotationAngleRadians);
					// Rotation matrix, 2nd column
					rotation[0][1] = 0;
					rotation[1][1] = 1;
					rotation[2][1] = 0;
					// Rotation matrix, 3rd column
					rotation[0][2] = Math.sin(rotationAngleRadians);
					rotation[1][2] = 0;
					rotation[2][2] = Math.cos(rotationAngleRadians);
					
					RotationNR newRot = new RotationNR(rotation);
					// Convert to Affine and back
					RotationNR oldRot = TransformFactory
							.affineToNr(TransformFactory.nrToAffine(new TransformNR(0, 0, 0, newRot))).getRotation();
					double[][] rotationMatrix = newRot.getRotationMatrix();
					System.out.println("Testing pure Elevation \nrotation " + rotationAngleDegrees + "\n as radian "
							+ Math.toRadians(rotationAngleDegrees) + "\n     Az " + oldRot.getRotationAzimuth()
							+ "\n     El " + oldRot.getRotationElevation() + "\n     Tl " + oldRot.getRotationTilt()
							+ "\n New Az " + newRot.getRotationAzimuth() + "\n New El " + newRot.getRotationElevation()
							+ "\n New Tl " + newRot.getRotationTilt());
					assertArrayEquals(rotation[0], rotationMatrix[0], 0.001);
					assertArrayEquals(rotation[1], rotationMatrix[1], 0.001);
					assertArrayEquals(rotation[2], rotationMatrix[2], 0.001);

					System.out.println(
							"Testing Quaturnion \nrotation " + "\n     qw " + oldRot.getRotationMatrix2QuaturnionW()
									+ "\n     qx " + oldRot.getRotationMatrix2QuaturnionX() + "\n     qy "
									+ oldRot.getRotationMatrix2QuaturnionY() + "\n     qz "
									+ oldRot.getRotationMatrix2QuaturnionZ() + "\nNEW  qw "
									+ newRot.getRotationMatrix2QuaturnionW() + "\nNEW  qx "
									+ newRot.getRotationMatrix2QuaturnionX() + "\nNEW  qy "
									+ newRot.getRotationMatrix2QuaturnionY() + "\nNEW  qz "
									+ newRot.getRotationMatrix2QuaturnionZ());
					assertArrayEquals(
							new double[] { Math.abs(oldRot.getRotationMatrix2QuaturnionW()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionX()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionY()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionZ()), },
							new double[] { Math.abs(newRot.getRotationMatrix2QuaturnionW()),
									Math.abs(newRot.getRotationMatrix2QuaturnionX()),
									Math.abs(newRot.getRotationMatrix2QuaturnionY()),
									Math.abs(newRot.getRotationMatrix2QuaturnionZ()), },
							0.001);
					// Check Euler angles
					assertArrayEquals(
							new double[] { oldRot.getRotationAzimuth(), oldRot.getRotationElevation(),
									oldRot.getRotationTilt() },
							new double[] { newRot.getRotationAzimuth(), newRot.getRotationElevation(),
									newRot.getRotationTilt() },
							0.001);
					// Check the old rotation against the known value
					assertArrayEquals(new double[] {

							0, Math.toRadians(rotationAngleDegrees), 0 },
							new double[] { oldRot.getRotationAzimuth(), oldRot.getRotationElevation(),
									oldRot.getRotationTilt() },
							0.001);
					// Check the new rotation against the known value
					assertArrayEquals(new double[] { 0, Math.toRadians(rotationAngleDegrees), 0 }, new double[] {
							newRot.getRotationAzimuth(), newRot.getRotationElevation(), newRot.getRotationTilt() },
							0.001);
				
				}
				// frame();
				// frame2();
				System.out.println("Frame test passed with " + ro);
				// return;
			}
		}
	}

	/**
	 * Test.
	 * 
	 * @throws FileNotFoundException
	 */
	@Test
	public void compareCSGElevation() throws FileNotFoundException {
		int failCount = 0;
		int iterations = 100;
		Log.enableDebugPrint();
		for (RotationConvention conv : conventions) {
			for (RotationOrder ro : list) {
				failCount = 0;
				for (int i = 0; i < iterations; i++) {

					double rotationAngleDegrees = (Math.random() * 180) - 90;

					double rotationAngleRadians = Math.PI / 180 * rotationAngleDegrees;

					double[][] rotation = new double[3][3];
					// Rotation matrix, 1st column
					rotation[0][0] = Math.cos(rotationAngleRadians);
					rotation[1][0] = 0;
					rotation[2][0] = -Math.sin(rotationAngleRadians);
					// Rotation matrix, 2nd column
					rotation[0][1] = 0;
					rotation[1][1] = 1;
					rotation[2][1] = 0;
					// Rotation matrix, 3rd column
					rotation[0][2] = Math.sin(rotationAngleRadians);
					rotation[1][2] = 0;
					rotation[2][2] = Math.cos(rotationAngleRadians);
					
					RotationNR newRot = new RotationNR(rotation);
					// Convert to Affine and back
					RotationNR oldRot = TransformFactory
							.csgToNR(TransformFactory.nrToCSG(new TransformNR(0, 0, 0, newRot))).getRotation();
					double[][] rotationMatrix = newRot.getRotationMatrix();
					System.out.println("\n\nTesting pure Elevation \nrotation " + rotationAngleDegrees + "\n as radian "
							+ Math.toRadians(rotationAngleDegrees) + "\n     Az " + oldRot.getRotationAzimuth()
							+ "\n     El " + oldRot.getRotationElevation() + "\n     Tl " + oldRot.getRotationTilt()
							+ "\n New Az " + newRot.getRotationAzimuth() + "\n New El " + newRot.getRotationElevation()
							+ "\n New Tl " + newRot.getRotationTilt());
					assertArrayEquals(rotation[0], rotationMatrix[0], 0.001);
					assertArrayEquals(rotation[1], rotationMatrix[1], 0.001);
					assertArrayEquals(rotation[2], rotationMatrix[2], 0.001);

					System.out.println(
							"Testing Quaturnion \nrotation " + "\n     qw " + oldRot.getRotationMatrix2QuaturnionW()
									+ "\n     qx " + oldRot.getRotationMatrix2QuaturnionX() + "\n     qy "
									+ oldRot.getRotationMatrix2QuaturnionY() + "\n     qz "
									+ oldRot.getRotationMatrix2QuaturnionZ() + "\nNEW  qw "
									+ newRot.getRotationMatrix2QuaturnionW() + "\nNEW  qx "
									+ newRot.getRotationMatrix2QuaturnionX() + "\nNEW  qy "
									+ newRot.getRotationMatrix2QuaturnionY() + "\nNEW  qz "
									+ newRot.getRotationMatrix2QuaturnionZ());
					assertArrayEquals(
							new double[] { Math.abs(oldRot.getRotationMatrix2QuaturnionW()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionX()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionY()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionZ()), },
							new double[] { Math.abs(newRot.getRotationMatrix2QuaturnionW()),
									Math.abs(newRot.getRotationMatrix2QuaturnionX()),
									Math.abs(newRot.getRotationMatrix2QuaturnionY()),
									Math.abs(newRot.getRotationMatrix2QuaturnionZ()), },
							0.001);
					// Check Euler angles
					// Check Euler angles
					assertArrayEquals(
							new double[] { oldRot.getRotationAzimuth(), oldRot.getRotationElevation(),
									oldRot.getRotationTilt() },
							new double[] { newRot.getRotationAzimuth(), newRot.getRotationElevation(),
									newRot.getRotationTilt() },
							0.001);
					// Check the old rotation against the known value
					assertArrayEquals(new double[] {

							0, Math.toRadians(rotationAngleDegrees), 0 },
							new double[] { oldRot.getRotationAzimuth(), oldRot.getRotationElevation(),
									oldRot.getRotationTilt() },
							0.001);
					// Check the new rotation against the known value
					assertArrayEquals(new double[] { 0, Math.toRadians(rotationAngleDegrees), 0 }, new double[] {
							newRot.getRotationAzimuth(), newRot.getRotationElevation(), newRot.getRotationTilt() },
							0.001);
				}
				// frame();
				// frame2();
				System.out.println("Frame test passed with " + ro);
				// return;
			}
		}
	}

	@Test
	public void compareBulletElevation() throws FileNotFoundException {
		int failCount = 0;
		int iterations = 100;
		Log.enableDebugPrint();
		for (RotationConvention conv : conventions) {
			RotationNR.setConvention(conv);
			System.out.println("\n\nUsing convention " + conv.toString());
			for (RotationOrder ro : list) {
				RotationNR.setOrder(ro);
				System.out.println("\n\nUsing rotationOrder " + ro.toString());
				failCount = 0;
				for (int i = 0; i < iterations; i++) {

					double rotationAngleDegrees = (Math.random() * 180) - 90;

					double rotationAngleRadians = Math.PI / 180 * rotationAngleDegrees;

					double[][] rotation = new double[3][3];
					// Rotation matrix, 1st column
					rotation[0][0] = Math.cos(rotationAngleRadians);
					rotation[1][0] = 0;
					rotation[2][0] = -Math.sin(rotationAngleRadians);
					// Rotation matrix, 2nd column
					rotation[0][1] = 0;
					rotation[1][1] = 1;
					rotation[2][1] = 0;
					// Rotation matrix, 3rd column
					rotation[0][2] = Math.sin(rotationAngleRadians);
					rotation[1][2] = 0;
					rotation[2][2] = Math.cos(rotationAngleRadians);
					
					RotationNR newRot = new RotationNR(rotation);
					// Convert to Affine and back
					Transform bulletTrans = new Transform();
					TransformFactory.nrToBullet(new TransformNR(0, 0, 0, newRot), bulletTrans);
					RotationNR oldRot = TransformFactory.bulletToNr(bulletTrans).getRotation();
					double[][] rotationMatrix = newRot.getRotationMatrix();
					System.out.println("Testing pure Elevation \nrotation " + rotationAngleDegrees + "\n as radian "
							+ Math.toRadians(rotationAngleDegrees) + "\n     Az " + oldRot.getRotationAzimuth()
							+ "\n     El " + oldRot.getRotationElevation() + "\n     Tl " + oldRot.getRotationTilt()
							+ "\n New Az " + newRot.getRotationAzimuth() + "\n New El " + newRot.getRotationElevation()
							+ "\n New Tl " + newRot.getRotationTilt());
					assertArrayEquals(rotation[0], rotationMatrix[0], 0.001);
					assertArrayEquals(rotation[1], rotationMatrix[1], 0.001);
					assertArrayEquals(rotation[2], rotationMatrix[2], 0.001);

					System.out.println(
							"Testing Quaturnion \nrotation " + "\n     qw " + oldRot.getRotationMatrix2QuaturnionW()
									+ "\n     qx " + oldRot.getRotationMatrix2QuaturnionX() + "\n     qy "
									+ oldRot.getRotationMatrix2QuaturnionY() + "\n     qz "
									+ oldRot.getRotationMatrix2QuaturnionZ() + "\nNEW  qw "
									+ newRot.getRotationMatrix2QuaturnionW() + "\nNEW  qx "
									+ newRot.getRotationMatrix2QuaturnionX() + "\nNEW  qy "
									+ newRot.getRotationMatrix2QuaturnionY() + "\nNEW  qz "
									+ newRot.getRotationMatrix2QuaturnionZ());
					assertArrayEquals(
							new double[] { Math.abs(oldRot.getRotationMatrix2QuaturnionW()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionX()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionY()),
									Math.abs(oldRot.getRotationMatrix2QuaturnionZ()), },
							new double[] { Math.abs(newRot.getRotationMatrix2QuaturnionW()),
									Math.abs(newRot.getRotationMatrix2QuaturnionX()),
									Math.abs(newRot.getRotationMatrix2QuaturnionY()),
									Math.abs(newRot.getRotationMatrix2QuaturnionZ()), },
							0.001);
					// Check Euler angles
					// this check is needed to work around a known bug in the
					// legact implementation
					// Check Euler angles
					assertArrayEquals(
							new double[] { oldRot.getRotationAzimuth(), oldRot.getRotationElevation(),
									oldRot.getRotationTilt() },
							new double[] { newRot.getRotationAzimuth(), newRot.getRotationElevation(),
									newRot.getRotationTilt() },
							0.001);
					// Check the old rotation against the known value
					assertArrayEquals(new double[] {

							0, Math.toRadians(rotationAngleDegrees), 0 },
							new double[] { oldRot.getRotationAzimuth(), oldRot.getRotationElevation(),
									oldRot.getRotationTilt() },
							0.001);
					// Check the new rotation against the known value
					assertArrayEquals(new double[] { 0, Math.toRadians(rotationAngleDegrees), 0 }, new double[] {
							newRot.getRotationAzimuth(), newRot.getRotationElevation(), newRot.getRotationTilt() },
							0.001);
				}
				// frame();
				// frame2();
				System.out.println("Frame test passed with " + ro);
				// return;
			}
		}
	}
}
