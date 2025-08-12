package servernotoverloaded.beta.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import servernotoverloaded.beta.ServerNotOverloaded;

@Mixin(Entity.class)
public abstract class ExampleMixin {

	@Inject(method = "moveToWorld", at = @At("HEAD"), cancellable = true)
	private void onMoveToWorld(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
		Entity self = (Entity) (Object) this;

		boolean isItem = self instanceof ItemEntity;
		boolean isFallingBlock = self instanceof FallingBlockEntity;

		// 只处理掉落物或掉落方块
		if ((isItem || isFallingBlock) && destination.getRegistryKey() == World.END) {
			int chunkX = destination.getSpawnPos().getX() >> 4;
			int chunkZ = destination.getSpawnPos().getZ() >> 4;

			// 如果末地出生点区块未加载
			if (!destination.isChunkLoaded(chunkX, chunkZ)) {
				ServerNotOverloaded.LOGGER.info(
						"Redirected {} from entering unloaded End world",
						self.getType().toString()
				);

				// 获取当前世界和位置
				ServerWorld currentWorld = (ServerWorld) self.getWorld();
				BlockPos currentPos = self.getBlockPos();

				// 将实体移到传送门下方（或偏移到旁边以防再次掉入）
				self.setPosition(
						currentPos.getX() + 0.5,
						currentPos.getY() - 2,
						currentPos.getZ() + 0.5
				);

				// 阻止跨世界传送
				cir.setReturnValue((Entity) self);
			}
		}
	}
}
