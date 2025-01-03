package com.radi;

import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.network.IPacket;
import com.github.razorplay01.minecraft_events_utiles.minecrafteventsutilescommon.network.packet.ScreenPacket;
import com.radi.networking.packet.FabricCustomPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SquidGameGame2Screens implements ModInitializer, ClientModInitializer {
	public static final String MOD_ID = "squidgamegame2screens";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static KeyBinding openMinigame1Key;
	private static KeyBinding openMinigame2Key;
	private static KeyBinding openMinigame3Key;
	private static KeyBinding openMinigame4Key;

	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");
	}

	@Override
	public void onInitializeClient() {

		initializeNetworking();

//		openMinigame1Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
//				"key.squidgamegame2screens.open_minigame_1", // Translation key for first minigame
//				InputUtil.Type.KEYSYM,                       // Type of input
//				GLFW.GLFW_KEY_V,                             // Default key (N)
//				"category.squidgamegame2screens"             // Category for the keybinding
//		));
//
//		// Register the second minigame keybinding
//		openMinigame2Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
//				"key.squidgamegame2screens.open_minigame_2", // Translation key for second minigame
//				InputUtil.Type.KEYSYM,                       // Type of input
//				GLFW.GLFW_KEY_B,                             // Default key (M)
//				"category.squidgamegame2screens"             // Category for the keybinding
//		));
//		openMinigame3Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
//				"key.squidgamegame2screens.open_minigame_3", // Translation key for second minigame
//				InputUtil.Type.KEYSYM,                       // Type of input
//				GLFW.GLFW_KEY_N,                             // Default key (M)
//				"category.squidgamegame2screens"             // Category for the keybinding
//		));
//		openMinigame4Key = KeyBindingHelper.registerKeyBinding(new KeyBinding(
//				"key.squidgamegame2screens.open_minigame_4", // Translation key for second minigame
//				InputUtil.Type.KEYSYM,                       // Type of input
//				GLFW.GLFW_KEY_M,                             // Default key (M)
//				"category.squidgamegame2screens"             // Category for the keybinding
//		));
//
//		// Listen for key press events
//		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
//			if (openMinigame1Key.wasPressed() && client.currentScreen == null) {
//				// Open the first minigame screen
//				client.setScreen(new MinigameSpamScreen());
//			}
//			if (openMinigame2Key.wasPressed() && client.currentScreen == null) {
//				// Open the second minigame screen
//				client.setScreen(new MinigameCircleScreen());
//			}
//			if (openMinigame3Key.wasPressed() && client.currentScreen == null) {
//				// Open the second minigame screen
//				client.setScreen(new MinigameSpinScreen());
//			}
//			if (openMinigame4Key.wasPressed() && client.currentScreen == null) {
//				// Open the second minigame screen
//				client.setScreen(new MinigameArrowScreen());
//			}
//		});
	}

	private static void initializeNetworking() {
		SquidGameGame2Screens.LOGGER.info("Registering Packets for " + SquidGameGame2Screens.MOD_ID);

		// Register the custom payload types
		PayloadTypeRegistry.playC2S().register(FabricCustomPayload.CUSTOM_PAYLOAD_ID, FabricCustomPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(FabricCustomPayload.CUSTOM_PAYLOAD_ID, FabricCustomPayload.CODEC);

		ClientPlayNetworking.registerGlobalReceiver(FabricCustomPayload.CUSTOM_PAYLOAD_ID, (payload, context) -> {
			context.client().execute(() -> {
				IPacket packet = payload.packet();

				if (packet instanceof ScreenPacket screenPacket) {
					String screenType = screenPacket.getPacket();
					SquidGameGame2Screens.LOGGER.info("ScreenPacket received with type: {}", screenType);

					MinecraftClient client = MinecraftClient.getInstance();
					if (client != null) {
						switch (screenType) {
							case "ScreenSpin" -> client.setScreen(new MinigameSpinScreen());
							case "ScreenSpam" -> client.setScreen(new MinigameSpamScreen());
							case "ScreenArrow" -> client.setScreen(new MinigameArrowScreen());
							case "ScreenCircle" -> client.setScreen(new MinigameCircleScreen());
							default -> SquidGameGame2Screens.LOGGER.warn("Unknown screen type received: {}", screenType);
						}
					}
				} else {
					SquidGameGame2Screens.LOGGER.warn("Received an unrecognized packet type: {}", packet.getPacketId());
				}
			});
		});
	}


}