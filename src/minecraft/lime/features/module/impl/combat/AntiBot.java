package lime.features.module.impl.combat;

import lime.core.Lime;
import lime.core.events.EventTarget;
import lime.core.events.impl.EventMotion;
import lime.core.events.impl.EventPacket;
import lime.features.module.Category;
import lime.features.module.Module;
import lime.features.module.ModuleData;
import lime.features.setting.impl.BoolValue;
import lime.features.setting.impl.EnumValue;
import lime.ui.notifications.Notification;
import lime.utils.other.ChatUtils;
import lime.utils.other.MathUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S0CPacketSpawnPlayer;
import net.minecraft.network.play.server.S18PacketEntityTeleport;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@ModuleData(name = "Anti Bot", category = Category.COMBAT)
public class AntiBot extends Module {

    private final EnumValue mode = new EnumValue("Mode", this, "Funcraft", "Funcraft", "Hypixel", "Mineplex");
    private final BoolValue remove = new BoolValue("Remove", this, false);
    private final Map<Integer, Double> distanceMap = new HashMap<>();

    private final ArrayList<Integer> bots = new ArrayList<>();
    @Override
    public void onEnable() {
        bots.clear();
    }

    public boolean checkBot(EntityPlayer ent) {
        if(mode.is("funcraft")) {
            if(ent instanceof AbstractClientPlayer && ent != mc.thePlayer && ent.ticksExisted < 30) {
                if(!((AbstractClientPlayer) ent).hasSkin() && ent.ticksExisted < 25) {
                    AbstractClientPlayer player = (AbstractClientPlayer) ent;
                    if(mc.thePlayer.getDistanceToEntity(player) <= 5 && player.motionY == 0 && !player.onGround && player.rotationYaw != -180 && player.rotationPitch != 0) {
                        if(remove.isEnabled())
                            mc.theWorld.removeEntity(ent);
                        return true;
                    }
                }
            }
        }

        if(mode.is("mineplex")) {
            return bots.contains(ent.getEntityId());
        }
        return false;
    }

    public boolean checkBots() {
        if(mode.is("funcraft")) {
            for (Entity ent : mc.theWorld.getLoadedEntityList()) {
                if(ent instanceof AbstractClientPlayer && ent != mc.thePlayer && ent.ticksExisted < 30) {
                    if(!((AbstractClientPlayer) ent).hasSkin() && ent.ticksExisted < 25) {
                        AbstractClientPlayer player = (AbstractClientPlayer) ent;
                        if(mc.thePlayer.getDistanceToEntity(player) <= 5 && player.motionY == 0 && !player.onGround && player.rotationYaw != -180 && player.rotationPitch != 0) {
                            if(remove.isEnabled())
                                mc.theWorld.removeEntity(ent);
                            return true;
                        }
                    }
                }
            }
        }

        if(mode.is("mineplex")) {

        }
        return false;
    }

    @EventTarget
    public void onMotion(EventMotion e) {
        this.setSuffix(mode.getSelected());
        if(remove.isEnabled() && e.isPre())
            checkBots();

        if(mode.is("hypixel")) {
            for(Entity entity : mc.theWorld.getLoadedEntityList()) {

                if(entity.getDisplayName().getFormattedText().toLowerCase().contains("npc")) continue;

                if((entity instanceof EntityPlayer)) {
                    double distance = 0;
                    if(distanceMap.containsKey(entity.getEntityId())) {
                        distance = distanceMap.get(entity.getEntityId());
                    }
                    if(entity.getName().contains("\247") || entity.getDisplayName().getFormattedText().startsWith("ยง") || (distance > 14.5 && distance < 17)) {
                        mc.theWorld.removeEntity(entity);
                    }
                }

                if(entity.isInvisible()) {
                    if(!isOnTab(entity) && mc.thePlayer.ticksExisted > 100 && entity.ticksExisted > 5)
                        mc.theWorld.removeEntity(entity);
                }
            }
        }
    }

    @EventTarget
    public void onPacket(EventPacket e) {
        if(e.getPacket() instanceof S0CPacketSpawnPlayer) {
            S0CPacketSpawnPlayer p = (S0CPacketSpawnPlayer) e.getPacket();
            distanceMap.put(p.getEntityID(), mc.thePlayer.getDistance(p.getX(), p.getY(), p.getZ()));

            if(mode.is("mineplex")) {
                if(p.func_148944_c().size() < 3) {
                    this.bots.add(p.getEntityID());
                }
            }
        }
    }

    private boolean isOnTab(Entity entity){
        return mc.getNetHandler().getPlayerInfoMap()
                .stream()
                .anyMatch(info -> info.getGameProfile().getName().equals(entity.getName()));
    }
}
