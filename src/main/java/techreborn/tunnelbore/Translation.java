package techreborn.tunnelbore;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.INBTSerializable;

public class Translation implements INBTSerializable<NBTTagCompound> {

	EnumFacing direction;
	double displacement;

	public Translation(double displacement, EnumFacing direction) {
		this.displacement = displacement;
		this.direction = direction;
	}

	public Translation(NBTTagCompound tagCompound){
		deserializeNBT(tagCompound);
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

	public Vec3d getOffset() {
		return new Vec3d(direction.getFrontOffsetX() * displacement, direction.getFrontOffsetY() * displacement, direction.getFrontOffsetZ() * displacement);
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound tagCompound = new NBTTagCompound();
		tagCompound.setInteger("direction", direction.ordinal());
		tagCompound.setDouble("displacement", displacement);
		return tagCompound;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		direction = EnumFacing.VALUES[nbt.getInteger("direction")];
		displacement = nbt.getDouble("displacement");
	}
}
