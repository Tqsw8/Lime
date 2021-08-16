package lime.features.module.impl.world;

import lime.core.events.EventTarget;
import lime.core.events.impl.EventUpdate;
import lime.features.module.Category;
import lime.features.module.Module;
import lime.features.module.ModuleData;
import lime.utils.other.InventoryUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

@ModuleData(name = "Eater", category = Category.WORLD)
public class Eater extends Module {

    @EventTarget
    public void onUpdate(EventUpdate e){
        final int radius = 5;
        if(mc.thePlayer.ticksExisted % 2 != 0) return;
        for (int x = -radius; x < radius; ++x) {
            for (int y2 = radius; y2 > -radius; --y2) {
                for (int z = -radius; z < radius; ++z) {
                    final int xPos = (int) this.mc.thePlayer.posX + x;
                    final int yPos = (int) this.mc.thePlayer.posY + y2;
                    final int zPos = (int) this.mc.thePlayer.posZ + z;
                    final BlockPos blockPos = new BlockPos(xPos, yPos, zPos);
                    final Block block = this.mc.theWorld.getBlockState(blockPos).getBlock();
                    if (cakeBesideOrAt(blockPos) && (block == Blocks.cake || block == Blocks.wool || block == Blocks.hardened_clay || block == Blocks.stained_hardened_clay || block == Blocks.planks || block == Blocks.end_stone)) {
                        if(block == Blocks.cake && cakeCovered(blockPos)) continue;
                        mc.getNetHandler().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.NORTH));
                        mc.getNetHandler().sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.NORTH));
                    }
                }
            }
        }
    }

    private int pickaxeSlot() {
        for(int i = 36; i < 45; ++i) {
            if(InventoryUtils.getSlot(i).getHasStack()) {
                ItemStack itemStack = InventoryUtils.getSlot(i).getStack();
                if(itemStack.getItem() instanceof ItemPickaxe) {
                    return i - 36;
                }
            }
        }
        return -1;
    }

    private int axeSlot() {
        for(int i = 36; i < 45; ++i) {
            if(InventoryUtils.getSlot(i).getHasStack()) {
                ItemStack itemStack = InventoryUtils.getSlot(i).getStack();
                if(itemStack.getItem() instanceof ItemAxe) {
                    return i - 36;
                }
            }
        }
        return -1;
    }

    private boolean cakeBesideOrAt(BlockPos blockPos) {
        for (EnumFacing face : new EnumFacing[] {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.DOWN}) {
            if (mc.theWorld.getBlockState(blockPos.offset(face)).getBlock() == Blocks.cake) {
                return true;
            }
        }
        return mc.theWorld.getBlockState(blockPos).getBlock() == Blocks.cake;
    }

    private boolean cakeCovered(BlockPos blockPos) {
        for (EnumFacing face : new EnumFacing[] {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST, EnumFacing.UP}) {
            if (mc.theWorld.getBlockState(blockPos.offset(face)).getBlock() == Blocks.air) {
                return false;
            }
        }
        return true;
    }
}
