/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.factory;

import java.util.ArrayList;

import net.minecraft.src.BuildCraftFactory;
import net.minecraft.src.EntityLiving;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.core.Orientations;
import net.minecraft.src.buildcraft.api.core.Position;
import net.minecraft.src.buildcraft.core.Box;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.ITextureProvider;

public class BlockQuarry extends BlockMachineRoot implements ITextureProvider {

	int textureTop;
	int textureFront;
	int textureSide;

	public BlockQuarry(int i) {
		super(i, Material.iron);

		setHardness(1.5F);
		setResistance(10F);
		setStepSound(soundStoneFootstep);

		textureSide = 2 * 16 + 9;
		textureFront = 2 * 16 + 7;
		textureTop = 2 * 16 + 8;

	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving) {
		super.onBlockPlacedBy(world, i, j, k, entityliving);

		Orientations orientation = Utils.get2dOrientation(new Position(entityliving.posX, entityliving.posY, entityliving.posZ),
				new Position(i, j, k));

		world.setBlockMetadataWithNotify(i, j, k, orientation.reverse().ordinal());
	}

	@Override
	public int getBlockTextureFromSideAndMetadata(int i, int j) {
		// If no metadata is set, then this is an icon.
		if (j == 0 && i == 3) {
			return textureFront;
		}

		if (i == j) {
			return textureFront;
		}

		switch (i) {
		case 1:
			return textureTop;
		default:
			return textureSide;
		}
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileQuarry();
	}

	public void searchFrames(World world, int i, int j, int k) {
		int width2 = 1;
		if (!world.checkChunksExist(i - width2, j - width2, k - width2, i + width2, j + width2, k + width2))
			return;

		int blockID = world.getBlockId(i, j, k);

		if (blockID != BuildCraftFactory.frameBlock.blockID)
			return;

		int meta = world.getBlockMetadata(i, j, k);

		if ((meta & 8) == 0) {
			world.setBlockMetadata(i, j, k, meta | 8);

			Orientations[] dirs = Orientations.dirs();

			for (Orientations dir : dirs) {
				switch (dir) {
				case YPos:
					searchFrames(world, i, j + 1, k);
				case YNeg:
					searchFrames(world, i, j - 1, k);
				case ZPos:
					searchFrames(world, i, j, k + 1);
				case ZNeg:
					searchFrames(world, i, j, k - 1);
				case XPos:
					searchFrames(world, i + 1, j, k);
				case XNeg:
				default:
					searchFrames(world, i - 1, j, k);
				}
			}
		}
	}

	private void markFrameForDecay(World world, int x, int y, int z){
		if (world.getBlockId(x, y, z) == BuildCraftFactory.frameBlock.blockID){
			world.setBlockMetadata(x, y, z, 1);
		}
	}
	
	@Override
	public void onBlockRemoval(World world, int i, int j, int k) {
		
		if (APIProxy.isRemote()){
			return;
		}
		
		TileEntity tile = world.getBlockTileEntity(i, j, k);
		if (tile instanceof TileQuarry){
			TileQuarry quarry = (TileQuarry)tile;
			Box box = quarry.box;
			
			//X - Axis
			for (int x = box.xMin; x <= box.xMax; x++) {
				markFrameForDecay(world, x, box.yMin, box.zMin);
				markFrameForDecay(world, x, box.yMax, box.zMin);
				markFrameForDecay(world, x, box.yMin, box.zMax);
				markFrameForDecay(world, x, box.yMax, box.zMax);
			}
			
			//Z - Axis
			for (int z = box.zMin + 1; z <= box.zMax - 1; z++) {
				markFrameForDecay(world, box.xMin, box.yMin, z);
				markFrameForDecay(world, box.xMax, box.yMin, z);
				markFrameForDecay(world, box.xMin, box.yMax, z);
				markFrameForDecay(world, box.xMax, box.yMax, z);
			}
			
			//Y - Axis
			for (int y = box.yMin + 1; y <= box.yMax -1; y++) {
				
				markFrameForDecay(world, box.xMin, y, box.zMin);
				markFrameForDecay(world, box.xMax, y, box.zMin);
				markFrameForDecay(world, box.xMin, y, box.zMax);
				markFrameForDecay(world, box.xMax, y, box.zMax);
			}
			quarry.destroy();
		}
		
		Utils.preDestroyBlock(world, i, j, k);

//		byte width = 1;
//		int width2 = width + 1;
//
//		if (world.checkChunksExist(i - width2, j - width2, k - width2, i + width2, j + width2, k + width2)) {
//
//			boolean frameFound = false;
//			for (int z = -width; z <= width; ++z) {
//
//				for (int y = -width; y <= width; ++y) {
//
//					for (int x = -width; x <= width; ++x) {
//
//						int blockID = world.getBlockId(i + z, j + y, k + x);
//
//						if (blockID == BuildCraftFactory.frameBlock.blockID) {
//							searchFrames(world, i + z, j + y, k + x);
//							frameFound = true;
//							break;
//						}
//					}
//					if (frameFound)
//						break;
//				}
//				if (frameFound)
//					break;
//			}
//		}

		super.onBlockRemoval(world, i, j, k);
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
