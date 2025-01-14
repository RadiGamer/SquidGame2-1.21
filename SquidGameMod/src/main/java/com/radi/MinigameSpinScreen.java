package com.radi;

import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.packet.ScreenPacket;
import com.radi.networking.packet.FabricCustomPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;

public class MinigameSpinScreen extends Screen {
    private static final int CENTER_RADIUS = 15;
    private static final int BAR_HEIGHT = 200;
    private static final int BAR_WIDTH = 20;
    private static final float TARGET_PROGRESS = 70.0f;
    private static final Identifier TROMPO_TEXTURE = Identifier.of("squidgamegame2screens", "textures/gui/trompo.png");

    private float progress = 0.0f;
    private float mouseAngle = 0.0f;
    private float lastMouseAngle = 0.0f;
    private boolean isMouseHeld = false;
    private String message = "Manten el Click!";

    public MinigameSpinScreen() {
        super(Text.of("Spin Minigame"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        renderBackground(context, mouseX, mouseY, delta);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Darkened background overlay
        context.fill(0, 0, this.width, this.height, 0xAA000000);

        renderCenterTexture(context, centerX, centerY, mouseAngle);
        renderProgressBar(context, centerX - 50, centerY);
        renderMessage(context, centerX, centerY + 120);
    }

    private void renderCenterTexture(DrawContext context, int centerX, int centerY, float angle) {
        MatrixStack matrices = context.getMatrices();

        matrices.push();
        matrices.translate(centerX, centerY, 0);
        matrices.multiply(net.minecraft.util.math.RotationAxis.POSITIVE_Z.rotationDegrees(angle));

        int textureSize = 100;
        int halfSize = textureSize / 2;

        // Draw texture
        context.drawTexture(
                TROMPO_TEXTURE, // Identifier for the texture
                -halfSize, -halfSize, // Position relative to the center
                0, 0, // Texture position
                textureSize, textureSize, // Texture dimensions
                textureSize, textureSize // Image dimensions
        );

        matrices.pop();
    }

    private void renderProgressBar(DrawContext context, int x, int centerY) {
        int barY = centerY - BAR_HEIGHT / 2;

        // Background bar with gradient
        context.fillGradient(
                x, barY,
                x + BAR_WIDTH, barY + BAR_HEIGHT,
                0xFF333333, 0xFF111111
        );

        // Filled progress bar
        int filledHeight = (int) ((progress / TARGET_PROGRESS) * BAR_HEIGHT);
        int filledY = barY + BAR_HEIGHT - filledHeight;
        context.fillGradient(
                x, filledY,
                x + BAR_WIDTH, barY + BAR_HEIGHT,
                0xFFFF5555, 0xFFFF0000
        );

        int borderThickness = 1;

        context.fill(x, barY, x + BAR_WIDTH, barY + borderThickness, 0xFFFFFFFF);
        context.fill(x, barY + BAR_HEIGHT - borderThickness, x + BAR_WIDTH, barY + BAR_HEIGHT, 0xFFFFFFFF);
        context.fill(x, barY, x + borderThickness, barY + BAR_HEIGHT, 0xFFFFFFFF);
        context.fill(x + BAR_WIDTH - borderThickness, barY, x + BAR_WIDTH, barY + BAR_HEIGHT, 0xFFFFFFFF);

    }

    private void renderMessage(DrawContext context, int centerX, int y) {
        int textWidth = this.textRenderer.getWidth(message);
        int textX = centerX - textWidth / 2;
        context.drawText(this.textRenderer, message, textX, y, 0xFFFFFF, true);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isMouseHeld = true;
            message = "Gira!";

            // Play mouse press sound
            playMousePressSound();
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isMouseHeld = false;
            message = "Manten el Click!";
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void tick() {
        if (isMouseHeld) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            double dx = this.client.mouse.getX() - centerX;
            double dy = this.client.mouse.getY() - centerY;

            mouseAngle = (float) Math.toDegrees(Math.atan2(dy, dx));
            if (mouseAngle < 0) mouseAngle += 360;

            float angleDiff = mouseAngle - lastMouseAngle;
            if (angleDiff < -180) angleDiff += 360;
            if (angleDiff > 180) angleDiff -= 360;

            if (Math.abs(angleDiff) > 5) {
                progress += Math.abs(angleDiff) / 180.0f; // Increased spin impact
                progress = Math.min(progress, TARGET_PROGRESS);

            }

            lastMouseAngle = mouseAngle;
            if (progress >= TARGET_PROGRESS) {
                onGameComplete();
            }
        }
    }

    private void onGameComplete() {
        // Play completion sound

        ClientPlayNetworking.send(new FabricCustomPayload(new ScreenPacket("CompleteSpin")));
        progress = TARGET_PROGRESS;
        this.client.setScreen(null);
    }

    private void playMousePressSound() {
        if (this.client != null && this.client.player != null) {
            this.client.player.playSound(SoundEvents.BLOCK_LEVER_CLICK, 1.0F, 1.0F);
        }
    }
}
