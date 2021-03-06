package WayofTime.bloodmagic.ritual.harvest;

import WayofTime.bloodmagic.api.BlockStack;
import WayofTime.bloodmagic.api.iface.IHarvestHandler;
import WayofTime.bloodmagic.api.registry.HarvestRegistry;
import net.minecraft.block.BlockStem;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

/**
 * Harvest handler for crops with stems such as Pumpkins and Melons.
 * {@link OreDictionary#WILDCARD_VALUE} is used as a wildcard to allow the crop
 * to be harvested at any metadata. Rotation based crop blocks are a good reason
 * to use this (see pumpkins). <br>
 * Register a new crop for this handler with
 * {@link HarvestRegistry#registerStemCrop(BlockStack, BlockStack)}
 */
public class HarvestHandlerStem implements IHarvestHandler
{
    public HarvestHandlerStem()
    {
        HarvestRegistry.registerStemCrop(new BlockStack(Blocks.PUMPKIN, OreDictionary.WILDCARD_VALUE), new BlockStack(Blocks.PUMPKIN_STEM, 7));
        HarvestRegistry.registerStemCrop(new BlockStack(Blocks.MELON_BLOCK), new BlockStack(Blocks.MELON_STEM, 7));
    }

    @Override
    public boolean harvestAndPlant(World world, BlockPos pos, BlockStack blockStack)
    {
        boolean retFlag = false;
        List<ItemStack> drops = new ArrayList<ItemStack>();
        BlockPos cropPos = pos;
        if (HarvestRegistry.getStemCrops().containsKey(blockStack))
        {
            EnumFacing cropDir = blockStack.getState().getValue(BlockStem.FACING);

            if (cropDir != EnumFacing.UP)
            {
                cropPos = pos.offset(cropDir);
                BlockStack probableCrop = BlockStack.getStackFromPos(world, cropPos);
                BlockStack regCrop = HarvestRegistry.getStemCrops().get(blockStack);

                if ((regCrop.getMeta() == OreDictionary.WILDCARD_VALUE && regCrop.getBlock() == probableCrop.getBlock()) || regCrop.equals(probableCrop))
                {
                    drops = probableCrop.getBlock().getDrops(world, cropPos, probableCrop.getState(), 0);
                    world.destroyBlock(cropPos, false);
                    retFlag = true;
                }
            }
        }

        if (!world.isRemote)
        {
            for (ItemStack drop : drops)
            {
                EntityItem item = new EntityItem(world, cropPos.getX(), cropPos.getY() + 0.5, cropPos.getZ(), drop);
                world.spawnEntity(item);
            }
        }

        return retFlag;
    }
}
