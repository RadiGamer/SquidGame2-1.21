
package com.radi;

import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.IPacket;
import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.packet.ScreenPacket;
import com.radi.networking.packet.FabricCustomPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SquidGameGame2Screens implements ModInitializer, ClientModInitializer {
	public static final String MOD_ID = "squidgamegame2screens";

	private boolean lastKeyWasA = false;
	private boolean isMoving = false;
	private static String currentLetter = "A";
	private static String errorMessage = null; // Error message to display
	private static long errorDisplayTime = 0;// Default to "A"


	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static KeyBinding openMinigame1Key;
	private static KeyBinding openMinigame2Key;
	private static KeyBinding openMinigame3Key;
	private static KeyBinding openMinigame4Key;

	private Vec3d lastValidPosition;


	private static boolean movementEnabled = false;
	private long lastKeyPressTime = 0;
	private static final long KEY_PRESS_COOLDOWN = 200;


	@Override
	public void onInitialize() {

		LOGGER.info("Hello Fabric world!");
	}

	@Override
	public void onInitializeClient() {

		initializeNetworking();

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player == null) return;

			if (movementEnabled) {
				disableDefaultMovement(client.player);
				handleCustomMovement(client);
			}
		});


		HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
			if (movementEnabled) {
				renderLetter(matrixStack);
			}
			if (errorMessage != null && System.currentTimeMillis() - errorDisplayTime < 2000) {
				renderError(matrixStack);
			}
		});


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

	private void disableDefaultMovement(ClientPlayerEntity player) {
		// Stop all movement
		player.setVelocity(0, player.getVelocity().y, 0);
		player.velocityModified = true;

		// Suppress input
		if (player.input != null) {
			player.input.movementForward = 0;  // Disable forward/backward movement
			player.input.movementSideways = 0; // Disable sideways movement
		}
	}

	private void movePlayerForward(ClientPlayerEntity player) {
		double speed = 0.5;
		Vec3d forwardVector = player.getRotationVec(1.0F).multiply(speed);

		// Update last valid position
		lastValidPosition = player.getPos();

		// Apply forward movement
		Vec3d newVelocity = new Vec3d(forwardVector.x, player.getVelocity().y, forwardVector.z);
		player.setVelocity(newVelocity);
		player.velocityModified = true; // Mark the velocity as modified
	}

	private void notifyInvalidInput(ClientPlayerEntity player, String message) {
		setErrorMessage(message); // Update overlay with error message

		if (lastValidPosition != null) {
			// Teleport player to the last valid position
			player.setPosition(lastValidPosition.x, lastValidPosition.y, lastValidPosition.z);
			player.velocityModified = true; // Ensure the teleportation takes effect
		}
	}

	public static void setErrorMessage(String message) {
		errorMessage = message;
		errorDisplayTime = System.currentTimeMillis(); // Start error display timer
	}

	private void renderError(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.player == null) {
			return;
		}

		MatrixStack matrices = context.getMatrices();

		// Center the error message
		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();
		int x = screenWidth / 2;
		int y = screenHeight / 3 + 40;

		// Draw the error message
		context.drawTextWithShadow(client.textRenderer, Text.literal(errorMessage).formatted(Formatting.RED, Formatting.BOLD), x - 70, y, 0xFFFFFF);
	}
	public static void setNextLetter(String letter) {
		currentLetter = letter;
	}

	public static void setMovementEnabled(boolean enabled) {
		movementEnabled = enabled;
	}

	private void renderLetter(DrawContext context) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client == null || client.player == null) {
			return;
		}

		MatrixStack matrices = context.getMatrices();

		// Message in Spanish
		String message = "Presiona la tecla:";
		String letter = currentLetter;

		// Center the text
		int screenWidth = client.getWindow().getScaledWidth();
		int screenHeight = client.getWindow().getScaledHeight();
		int x = screenWidth / 2;
		int y = screenHeight / 3;

		// Draw the message
		context.drawTextWithShadow(client.textRenderer, Text.literal(message).formatted(Formatting.YELLOW), x - 50, y - 10, 0xFFFFFF);

		// Draw the letter (Beautified)
		context.drawTextWithShadow(client.textRenderer, Text.literal(letter).formatted(Formatting.BOLD, Formatting.GREEN), x - 5, y + 10, 0xFFFFFF);
	}
	private void handleCustomMovement(MinecraftClient client) {
		if (!movementEnabled) return;

		ClientPlayerEntity player = client.player;
		if (player == null) return;

		long currentTime = System.currentTimeMillis();
		if (currentTime - lastKeyPressTime < KEY_PRESS_COOLDOWN) {
			return; // Ignore inputs during cooldown
		}

		// Block W and S keys (forward and backward movement)
		if (client.options.forwardKey.isPressed() || client.options.backKey.isPressed()) {
			notifyInvalidInput(player, "¡Solo puedes mover una pierna a la vez!");
			return;
		}

		// Handle custom A/D-based movement
		if (client.options.leftKey.isPressed()) { // A key
			if (!lastKeyWasA) {
				lastKeyWasA = true;
				lastKeyPressTime = currentTime; // Update the last key press time
				movePlayerForward(player);
				setNextLetter("D"); // Set next expected key to D
			} else {
				notifyInvalidInput(player, "¡Tecla incorrecta! Pulsa la tecla correcta.");
			}
		} else if (client.options.rightKey.isPressed()) { // D key
			if (lastKeyWasA) {
				lastKeyWasA = false;
				lastKeyPressTime = currentTime; // Update the last key press time
				movePlayerForward(player);
				setNextLetter("A"); // Set next expected key to A
			} else {
				notifyInvalidInput(player, "¡Tecla incorrecta! Pulsa la tecla correcta.");
			}
		}
	}

	private static void initializeNetworking() {
		SquidGameGame2Screens.LOGGER.info("Registering Packets for " + SquidGameGame2Screens.MOD_ID);

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
							case "ADMovementenable" -> {
								movementEnabled = true;
								setMovementEnabled(true);
								setNextLetter("A"); // Start with "A"
								SquidGameGame2Screens.LOGGER.info("A/D Movement enabled.");
							}
							case "ADMovementdisable" -> {
								movementEnabled = false;
								setMovementEnabled(false);
								SquidGameGame2Screens.LOGGER.info("A/D Movement disabled.");
							}

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
