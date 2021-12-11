package lime.features.module.impl.combat.killaura;

import lime.core.Lime;
import lime.core.events.impl.Event2D;
import lime.core.events.impl.Event3D;
import lime.core.events.impl.EventMotion;
import lime.features.module.impl.combat.Criticals;
import lime.features.module.impl.combat.KillAura;
import lime.utils.combat.CombatUtils;
import lime.utils.other.MathUtils;
import lime.utils.other.Timer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;

public class Multi extends KillAuraMode {

    public Multi(KillAura killAura) {
        super(killAura);
    }

    private final Timer cpsTimer = new Timer();
    private ArrayList<EntityLivingBase> entities = new ArrayList<>();
    private int index;

    @Override
    public void onEnable() {
        entities.clear();
        index = 0;
    }

    @Override
    public void onDisable() {
        KillAura.entity = null;
    }

    @Override
    public void on3D(Event3D e) {
        if(!entities.isEmpty()) {
            for (EntityLivingBase entity : entities) {
                if(killAura.isValid(entity)) {
                    killAura.renderJello(entity);
                }
            }
        }
    }

    @Override
    public void onMotion(EventMotion e) {
        this.entities = getEntities();

        if(!entities.isEmpty()) {
            index = Math.min(index, entities.size() - 1);

            if(!killAura.keepSprint.isEnabled()) {
                mc.thePlayer.setSprinting(false);
                e.setSprint(false);
                mc.gameSettings.keyBindSprint.pressed = false;
            }

            if(killAura.autoBlockState.is(e.getState().name()) && killAura.hasSword() && killAura.autoBlock.is("basic") && !KillAura.isBlocking) {
                mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(0, 0, 0), 255, mc.thePlayer.getHeldItem(), 0, 0, 0));
                mc.thePlayer.setItemInUse(mc.thePlayer.getHeldItem(), 32767);
                KillAura.isBlocking = true;
            }

            if(killAura.hasSword() && KillAura.isBlocking) {
                mc.thePlayer.setItemInUse(mc.thePlayer.getHeldItem(), 32767);
            }

            if(!killAura.hasSword()) {
                KillAura.isBlocking = false;
            }

            // Rotations
            if(!killAura.rotations.is("none")) {
                float[] rotations = null;

                switch(killAura.rotations.getSelected().toLowerCase()) {
                    case "basic":
                        rotations = CombatUtils.getEntityRotations(entities.get(index), true);
                        break;
                }

                if(rotations == null || rotations[1] > 90 || rotations[1] < -90) return;

                e.setYaw(rotations[0]);
                e.setPitch(rotations[1]);

                mc.thePlayer.setRotationsTP(e);

                // Ray Cast
                if(killAura.rayCast.isEnabled()) {
                    Entity entity1 = CombatUtils.raycastEntity(killAura.range.getCurrent(), rotations);
                    if(entity1 == null) return;
                    //entity = (EntityLivingBase) entity1;
                }
            }

            if(!killAura.state.is(e.getState().name())) return;
            int cps = Math.max(killAura.cps.intValue() + (int) MathUtils.random(-killAura.randomizeCps.intValue(), killAura.randomizeCps.intValue()), 1);
            if(cpsTimer.hasReached(20 / cps * 50L) && mc.thePlayer.getDistanceToEntity(entities.get(index)) <= killAura.range.getCurrent()) {
                mc.thePlayer.swingItem();
                mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(entities.get(index), C02PacketUseEntity.Action.ATTACK));
                cpsTimer.reset();

                if(!mc.thePlayer.onGround && !Lime.getInstance().getModuleManager().getModuleC(Criticals.class).isToggled() && killAura.particles.isEnabled()) {
                    mc.thePlayer.onCriticalHit(entities.get(index));
                }
                if(killAura.hasSword() && killAura.particles.isEnabled() && EnchantmentHelper.getEnchantmentLevel(16, mc.thePlayer.getHeldItem()) != 0) {
                    mc.thePlayer.onEnchantmentCritical(entities.get(index));
                }

                ++index;
                if(index + 1 > entities.size()) {
                    index = 0;
                }
            }
        } else {
            index = 0;
        }
    }

    private ArrayList<EntityLivingBase> getEntities() {
        ArrayList<EntityLivingBase> entities = new ArrayList<>();

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if(entity instanceof EntityLivingBase && killAura.isValid(entity) && entity != mc.thePlayer) {
                entities.add((EntityLivingBase) entity);
            }
        }

        killAura.sortEntities(entities, true);

        if(!entities.isEmpty() && mc.thePlayer.getDistanceToEntity(entities.get(0)) <= killAura.range.getCurrent()) {
            entities.removeIf(entity -> mc.thePlayer.getDistanceToEntity(entity) > killAura.range.getCurrent());
        }

        killAura.sortEntities(entities, false);

        return entities;
    }

    @Override
    public void on2D(Event2D e) {

    }

    @Override
    public EntityLivingBase getTargetedEntity() {
        return entities.isEmpty() ? null : entities.get(index);
    }
}
