package lightning.modules.impl.movement;

import lightning.events.SubscribeEvent;
import lightning.events.impl.PlayerMoveEvent;
import lightning.modules.Module;
import lightning.modules.RegisterModule;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;

@RegisterModule(name = "AntiVoid", description = "Prevents you from falling into the void.", category = Module.Category.MOVEMENT)
public class AntiVoidModule extends Module {
    @SubscribeEvent
    public void onPlayerMove(PlayerMoveEvent arg0) {
        if ((mc.player == null) || (mc.level == null)) {
            return;
        }
        if (falling()) {
            arg0.setCancelled(true);
            arg0.setMovement(new Vec3(arg0.getMovement().x, 0.0, arg0.getMovement().z));
            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(mc.player.getX(), mc.player.getY(), mc.player.getZ(), true, mc.player.horizontalCollision));
        }
    }

    private boolean falling() {
        if ((mc.player == null) || (mc.level == null)) {
            return false;
        }
        int var1 = mc.level.getMinY();
        int var2 = ((int) mc.player.getY());
        while (var2 >= var1) {
            Object var3 = BlockPos.containing(mc.player.getX(), ((double) var2), mc.player.getZ());
            Object var4 = mc.level.getBlockState(var3);
            if (var4.isAir()) break label_4;
            if (var4.getCollisionShape(mc.level, var3).isEmpty()) {
                break label_4;
            }
            return false;
            label_4:
            var2--;
        }
        return mc.player.fallDistance > 0.0;
    }

}
