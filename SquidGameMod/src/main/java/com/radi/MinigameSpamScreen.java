package com.radi;

import com.github.razorplay01.minecraft_utiles.minecrafteventsutilescommon.network.packet.ScreenPacket;
import com.radi.networking.packet.FabricCustomPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.sound.SoundEvents;
import org.lwjgl.glfw.GLFW;

public class MinigameSpamScreen extends Screen {
    private static final Identifier BAR_TEXTURE = Identifier.of("squidgamegame2screens", "textures/gui/juego1_1.png");
    private static final Identifier ARROW_TEXTURE = Identifier.of("squidgamegame2screens", "textures/gui/juego1_2.png");
    private static final Identifier SPACEBAR_TEXTURE_1 = Identifier.of("squidgamegame2screens", "textures/gui/spacebar1.png");
    private static final Identifier SPACEBAR_TEXTURE_2 = Identifier.of("squidgamegame2screens", "textures/gui/spacebar2.png");

    private int progress = 0;
    private int maxProgress = 100;
    private int DESCENT_RATE = 1;
    private boolean gameOver = false;
    private boolean lastKeyPressed = false;
    private long lastKeyPressTime = 0;
    private static final long SPAM_THRESHOLD = 100;
    private boolean spacebarState = false; // Tracks which texture to show

    public MinigameSpamScreen() {
        super(Text.of("Spam Minigame"));
    }

    @Override
    protected void init() {
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE && !gameOver) {
            if (!lastKeyPressed || System.currentTimeMillis() - lastKeyPressTime > SPAM_THRESHOLD) {
                progress = Math.min(progress + 3, maxProgress);
                lastKeyPressed = true;
                lastKeyPressTime = System.currentTimeMillis();

                spacebarState = !spacebarState; // Toggle the spacebar texture

                // Play spacebar press sound
                playPressSound();

                if (progress >= maxProgress) {
                    onGameComplete();
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_SPACE) {
            lastKeyPressed = false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        if (!gameOver) {
            if (progress > 0) {
                progress = Math.max(progress - DESCENT_RATE, 0);
            }
        }
    }

    private void onGameComplete() {
        gameOver = true;

        ClientPlayNetworking.send(new FabricCustomPayload(new ScreenPacket("CompleteSpam")));
        this.client.setScreen(null);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int barX = centerX - (int) (20 * 1.2);
        int barY = centerY - (int) (53 * 1.2);
        int barWidth = (int) (40 * 1.2);
        int barHeight = (int) (106 * 1.2);
        int arrowWidth = (int) (8 * 1.2);
        int arrowHeight = (int) (14 * 1.2);
        int arrowX = barX + barWidth + (int) (5 * 1.2);
        int arrowY = barY + (int) ((1 - (progress / (float) maxProgress)) * barHeight) - arrowHeight / 2;

        int spacebarWidth = (int) (50 * 1.2); // Adjust size of the spacebar textures
        int spacebarHeight = (int) (20 * 1.2);
        int spacebarX = centerX - spacebarWidth / 2;
        int spacebarY = centerY + barHeight / 2 + 10;

        super.render(context, mouseX, mouseY, delta);

        context.drawTexture(ARROW_TEXTURE, arrowX, arrowY, 0.0F, 0.0F, arrowWidth, arrowHeight, arrowWidth, arrowHeight);
        context.drawTexture(BAR_TEXTURE, barX, barY, 0.0F, 0.0F, barWidth, barHeight, barWidth, barHeight);

        Identifier spacebarTexture = spacebarState ? SPACEBAR_TEXTURE_1 : SPACEBAR_TEXTURE_2;
        context.drawTexture(spacebarTexture, spacebarX, spacebarY, 0.0F, 0.0F, spacebarWidth, spacebarHeight, spacebarWidth, spacebarHeight);
    }

    private void playPressSound() {
        if (this.client != null && this.client.player != null) {
            this.client.player.playSound(SoundEvents.BLOCK_LEVER_CLICK, 1.0F, 1.2F);
        }
    }
}
