package techreborn.tunnelbore;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class Translation {

	double displacement;
	final EnumFacing direction;

	public Translation(double displacement, EnumFacing direction) {
		this.displacement = displacement;
		this.direction = direction;
	}

	public Translation(EnumFacing direction) {
		this.direction = direction;
	}

	public double getDisplacement() {
		return displacement;
	}

	public void setDisplacement(double displacement) {
		this.displacement = displacement;
	}

	public void move(double displacement) {
		this.displacement += displacement;
	}

	public EnumFacing getDirection() {
		return direction;
	}

	public Vec3d getOffset(){
		return new Vec3d(direction.getFrontOffsetX() * displacement, direction.getFrontOffsetY() * displacement, direction.getFrontOffsetZ() * displacement);
	}
}
