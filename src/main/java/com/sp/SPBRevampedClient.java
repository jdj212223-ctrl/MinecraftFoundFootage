package com.sp;

import com.sp.block.client.renderer.FluorescentLightBlockEntityRenderer;
import com.sp.block.client.renderer.ThinFluorescentLightBlockEntityRenderer;
import com.sp.block.client.renderer.TinyFluorescentLightBlockEntityRenderer;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.cca_stuff.WorldEvents;
import com.sp.compat.modmenu.ConfigDefinitions;
import com.sp.compat.modmenu.ConfigStuff;
import com.sp.entity.client.model.SmilerModel;
import com.sp.entity.client.renderer.SkinWalkerRenderer;
import com.sp.entity.client.renderer.SmilerRenderer;
import com.sp.entity.client.renderer.WalkerRenderer;
import com.sp.init.*;
import com.sp.networking.InitializePackets;
import com.sp.networking.callbacks.ClientConnectionEvents;
import com.sp.util.MathStuff;
import com.sp.util.TickTimer;
import com.sp.world.levels.BackroomsLevel;
import com.sp.world.levels.custom.InfiniteGrassBackroomsLevel;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.joml.*;

import java.util.Vector;


public class SPBRevampedClient implements ClientModInitializer {
    static boolean inBackrooms = false;
    public static boolean isLightning = false;
    public static Camera camera;
    public static Vector3f cameraBobOffset;

    public static TickTimer tickTimer = new TickTimer();
    public static boolean blackScreen;
    public static boolean youCantEscape;

    private static boolean shouldBeUnmuted = false;
    private static boolean hasShownCursedFogMessage = false;

    private static final Random random = Random.create();
    private static final Random random2 = Random.create(34563264);

    public static boolean shouldRenderWarp = false;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            // Custom HUD rendering can be added here
        });

        InitializePackets.registerS2CPackets();

        ModKeyBinds.initializeKeyBinds();

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.POOLROOMS_SKY_BLOCK, RenderLayers.getPoolroomsSky());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BOTTOM_TRIM, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_1, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_2, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_3, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_4, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_5, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_6, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_7, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_8, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_TEXT_99, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_ARROW_1, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_ARROW_2, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_ARROW_3, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_ARROW_4, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_SMALL_1, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_SMALL_2, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_DRAWING_DOOR, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WALL_DRAWING_WINDOW, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.RUG_1, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.RUG_2, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.RED_METAL_CASING, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.WINDOW, RenderLayer.getTranslucent());

        BlockEntityRendererFactories.register(ModBlockEntities.FLUORESCENT_LIGHT_BLOCK_ENTITY, FluorescentLightBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.THIN_FLUORESCENT_LIGHT_BLOCK_ENTITY, ThinFluorescentLightBlockEntityRenderer::new);
        BlockEntityRendererFactories.register(ModBlockEntities.TINY_FLUORESCENT_LIGHT_BLOCK_ENTITY, TinyFluorescentLightBlockEntityRenderer::new);

        EntityRendererRegistry.register(ModEntities.SKIN_WALKER_ENTITY, SkinWalkerRenderer::new);
        EntityRendererRegistry.register(ModEntities.WALKER_ENTITY, WalkerRenderer::new);
        EntityRendererRegistry.register(ModEntities.SMILER_ENTITY, SmilerRenderer::new);

        EntityModelLayerRegistry.registerModelLayer(ModModelLayers.SMILER, SmilerModel::getTexturedModelData);

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(SPBRevamped.MOD_ID, "after_resources");
            }

            @Override
            public void reload(ResourceManager manager) {
                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    SPBRevamped.LOGGER.info("Running on macOS with OpenGL 4.1 support - Sodium & Iris compatible!");
                }
            }
        });

        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            if (client.world != null && client.player != null) {
                HelpfulHintManager.sendMessages(client.player);
                
                // Send Cursed Fog shader message on first join
                if (!hasShownCursedFogMessage) {
                    sendCursedFogMessage(client.player);
                    hasShownCursedFogMessage = true;
                }
            }
        }));

        ClientConnectionEvents.DISCONNECT.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player != null) {
                PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);
                playerComponent.setDoingCutscene(false);
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            // Cleanup
        });

        ClientTickEvents.END_WORLD_TICK.register((client) -> {
            Vector<TickTimer> tickTimers = TickTimer.getAllInstances();
            if (!tickTimers.isEmpty()) {
                for (TickTimer timer : tickTimers) {
                    timer.addCurrentTick();
                }
            }

            // Fixes Minecraft spectating not loading chunks bug
            MinecraftClient client1 = MinecraftClient.getInstance();
            PlayerEntity player = client1.player;
            if (player != null) {
                if (player != client1.getCameraEntity() && client1.getCameraEntity() != null) {
                    Vec3d pos = client1.getCameraEntity().getPos();
                    player.setPosition(pos);
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            PlayerEntity playerClient = client.player;
            if (playerClient != null) {
                setInBackrooms(BackroomsLevels.isInBackrooms(playerClient.getWorld().getRegistryKey()));

                if (!client.player.isSpectator() && !client.player.isCreative()) {
                    client.options.debugEnabled = false;
                }
            }
        });
    }

    private static void sendCursedFogMessage(PlayerEntity player) {
        Text cursedFogLink = Text.literal("https://modrinth.com/shaders/cursed-fog")
                .styled(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://modrinth.com/shaders/cursed-fog"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to open Cursed Fog on Modrinth")))
                        .withUnderline(true)
                );

        Text message1 = Text.literal("[SPB-Revamped] ")
                .append(Text.literal("For the best visual experience, consider installing ")
                        .append(Text.literal("Cursed Fog").styled(s -> s.withBold(true)))
                        .append(" shader!"));

        Text message2 = Text.literal("[SPB-Revamped] ")
                .append(Text.literal("Download: ").append(cursedFogLink));

        if (player instanceof ClientPlayerEntity clientPlayer) {
            clientPlayer.sendMessage(message1, false);
            clientPlayer.sendMessage(message2, false);
        }
    }

    public static boolean isInBackrooms() {
        return inBackrooms;
    }

    public static void setInBackrooms(boolean inBackrooms) {
        SPBRevampedClient.inBackrooms = inBackrooms;
    }

    public static boolean isInLevel(BackroomsLevel level) {
        if (MinecraftClient.getInstance().world == null) {
            return false;
        }

        return BackroomsLevels.isInBackroomsLevel(MinecraftClient.getInstance().world, level);
    }
}
