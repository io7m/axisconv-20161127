package com.io7m.experimental.axisconv;

import com.io7m.jnull.NullCheck;
import com.io7m.jtensors.Matrix3x3DType;
import com.io7m.jtensors.MatrixHeapArrayM3x3D;
import com.io7m.jtensors.MatrixM3x3D;
import com.io7m.jtensors.VectorI3D;
import com.io7m.jtensors.VectorM3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Conversion
{
  private static final Logger LOG;

  static {
    LOG = LoggerFactory.getLogger(Conversion.class);
  }

  private Conversion()
  {
  }

  enum Axis
  {
    AXIS_POSITIVE_X('x', new VectorI3D(1.0, 0.0, 0.0)),
    AXIS_POSITIVE_Y('y', new VectorI3D(0.0, 1.0, 0.0)),
    AXIS_POSITIVE_Z('z', new VectorI3D(0.0, 0.0, 1.0)),
    AXIS_NEGATIVE_X('x', new VectorI3D(-1.0, 0.0, 0.0)),
    AXIS_NEGATIVE_Y('y', new VectorI3D(0.0, -1.0, 0.0)),
    AXIS_NEGATIVE_Z('z', new VectorI3D(0.0, 0.0, -1.0)),;

    private final char name;
    private final VectorI3D vector;

    Axis(
      final char name,
      final VectorI3D v)
    {
      this.name = name;
      this.vector = v;
    }
  }

  static final class AxisArrangement
  {
    private final Axis right;
    private final Axis up;
    private final Axis forward;

    AxisArrangement(
      final Axis right,
      final Axis up,
      final Axis forward)
    {
      this.right = NullCheck.notNull(right, "right");
      this.up = NullCheck.notNull(up, "up");
      this.forward = NullCheck.notNull(forward, "forward");

      if ((int) right.name == (int) up.name
        || (int) up.name == (int) forward.name
        || (int) forward.name == (int) right.name) {
        throw new IllegalArgumentException("Axes must be perpendicular");
      }
    }

    Matrix3x3DType basis()
    {
      final VectorI3D column_x;
      if (this.right.name == 'x') {
        column_x = this.right.vector;
      } else if (this.up.name == 'x') {
        column_x = this.up.vector;
      } else {
        column_x = this.forward.vector;
      }

      final VectorI3D column_y;
      if (this.right.name == 'y') {
        column_y = this.right.vector;
      } else if (this.up.name == 'y') {
        column_y = this.up.vector;
      } else {
        column_y = this.forward.vector;
      }

      final VectorI3D column_z;
      if (this.right.name == 'z') {
        column_z = this.right.vector;
      } else if (this.up.name == 'z') {
        column_z = this.up.vector;
      } else {
        column_z = this.forward.vector;
      }

      final Matrix3x3DType m = MatrixHeapArrayM3x3D.newMatrix();
      m.setR0C0D(column_x.getXD());
      m.setR1C0D(column_x.getYD());
      m.setR2C0D(column_x.getZD());

      m.setR0C1D(column_y.getXD());
      m.setR1C1D(column_y.getYD());
      m.setR2C1D(column_y.getZD());

      m.setR0C2D(column_z.getXD());
      m.setR1C2D(column_z.getYD());
      m.setR2C2D(column_z.getZD());
      return m;
    }
  }


  public static void main(
    final String[] args)
  {
    final AxisArrangement og = new AxisArrangement(
      Axis.AXIS_POSITIVE_X, Axis.AXIS_POSITIVE_Y, Axis.AXIS_NEGATIVE_Z);
    final AxisArrangement dx = new AxisArrangement(
      Axis.AXIS_POSITIVE_X, Axis.AXIS_POSITIVE_Y, Axis.AXIS_POSITIVE_Z);

    final MatrixM3x3D.ContextMM3D c = new MatrixM3x3D.ContextMM3D();

    final Matrix3x3DType dx_basis = dx.basis();
    final Matrix3x3DType dx_basis_inv = MatrixHeapArrayM3x3D.newMatrix();
    MatrixM3x3D.invert(c, dx_basis, dx_basis_inv);

    final Matrix3x3DType og_basis = og.basis();
    final Matrix3x3DType og_basis_inv = MatrixHeapArrayM3x3D.newMatrix();
    MatrixM3x3D.invert(c, og_basis, og_basis_inv);

    final Matrix3x3DType og_to_dx = MatrixHeapArrayM3x3D.newMatrix();
    MatrixM3x3D.multiply(dx_basis, og_basis_inv, og_to_dx);

    final Matrix3x3DType dx_to_og = MatrixHeapArrayM3x3D.newMatrix();
    MatrixM3x3D.multiply(og_basis, dx_basis_inv, dx_to_og);

    final VectorI3D source_og = new VectorI3D(0.0, 0.0, -10.0);
    final VectorI3D target_dx = new VectorI3D(0.0, 0.0, 10.0);
    final VectorM3D result = new VectorM3D();

    MatrixM3x3D.multiplyVector3D(c, og_to_dx, source_og, result);

    LOG.debug("source_og: {}", source_og);
    LOG.debug("target_dx: {}", target_dx);
    LOG.debug("result:    {}", result);
    LOG.debug("matrix:\n{}", og_to_dx);

    MatrixM3x3D.multiplyVector3D(c, dx_to_og, target_dx, result);

    LOG.debug("source_og: {}", source_og);
    LOG.debug("target_dx: {}", target_dx);
    LOG.debug("result:    {}", result);
    LOG.debug("matrix:\n{}", og_to_dx);
  }
}
