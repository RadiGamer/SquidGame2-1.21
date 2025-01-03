package com.radi;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.lwjgl.glfw.GLFW;

public class MinigameCircleScreen extends Screen {
    private static final int TEXTURE_SIZE = 71;
    private static final float FADE_DURATION = 20f;

    private static final String[] LETTERS = {"a", "e", "f", "g", "j", "l", "p"};
    private static final Identifier[] INACTIVE_TEXTURES = new Identifier[7];
    private static final Identifier[] ACTIVE_TEXTURES = new Identifier[7];
    private static final Identifier LINE_TEXTURE = Identifier.of("squidgamegame2screens", "textures/gui/juego2/linea.png");


    static {
        for (int i = 0; i < LETTERS.length; i++) {
            INACTIVE_TEXTURES[i] = Identifier.of("squidgamegame2screens", "textures/gui/juego2/juego_2_" + LETTERS[i] + "_1.png");
            ACTIVE_TEXTURES[i] = Identifier.of("squidgamegame2screens", "textures/gui/juego2/juego_2_" + LETTERS[i] + "_2.png");
        }
    }

    private int activeIndex = 0;
    private float fadeAlpha = 0f;
    private boolean fadeIn = true;
    private float spinAngle = 0f;
    private float spinSpeed = 3f;
    private float centerX, centerY;
    private float topRowY;
    private boolean[] completedCircles = new boolean[7];

    private final int[] correctStartAngles = {255, 175, 85, 40, 225, 60 + 90, 255 + 90};
    private final int[] correctEndAngles = {310, 185, 95, 50, 250, 70 + 90, 5};

    public MinigameCircleScreen() {
        super(Text.of("Spin Minigame"));
    }

    @Override
    protected void init() {
        centerX = this.width / 2f - TEXTURE_SIZE / 2f;
        centerY = this.height / 2f - TEXTURE_SIZE / 2f;
        topRowY = this.height / 4f;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        super.render(context, mouseX, mouseY, delta);

        renderTopRow(context);

        renderCenterTexture(context, delta);

    }

    private void renderTopRow(DrawContext context) {
        float spacing = TEXTURE_SIZE + 10;
        float scaledTextureSize = TEXTURE_SIZE * 0.8f; // Reduce size by 20%
        for (int i = 0; i < LETTERS.length; i++) {
            if (i == activeIndex) continue;

            float x = centerX + (i - 3) * spacing;
            float y = topRowY + (float) Math.sin(System.currentTimeMillis() / 200.0 + i * 0.5) * 2;

            final int textureIndex = i;
            Identifier texture = completedCircles[i] ? ACTIVE_TEXTURES[textureIndex] : INACTIVE_TEXTURES[textureIndex];

            context.drawTexture(
                    texture,
                    (int) x,
                    (int) y,
                    0,
                    0,
                    (int) scaledTextureSize,
                    (int) scaledTextureSize,
                    (int) scaledTextureSize,
                    (int) scaledTextureSize
            );
        }
    }

    private void renderCenterTexture(DrawContext context, float delta) {
        spinAngle += delta * spinSpeed;
        if (spinAngle >= 360f) {
            spinAngle %= 360f;
        }

        if (fadeIn) {
            fadeAlpha = Math.min(fadeAlpha + (1f / FADE_DURATION), 1f);
        } else {
            fadeAlpha = Math.max(fadeAlpha - (1f / FADE_DURATION), 0f);
        }

        int alpha = (int) (fadeAlpha * 255);
        int color = (alpha << 24) | 0xFFFFFF;

        context.drawTexture(
                ACTIVE_TEXTURES[activeIndex],
                (int) centerX,
                (int) centerY,
                0,
                0,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );


        renderRotatedLine(context);
    }

    private void renderRotatedLine(DrawContext context) {
        context.getMatrices().push();

        context.getMatrices().translate(centerX + TEXTURE_SIZE / 2, centerY + TEXTURE_SIZE / 2, 0);

        context.getMatrices().multiply(RotationAxis.POSITIVE_Z.rotationDegrees(spinAngle));

        context.getMatrices().translate(-TEXTURE_SIZE / 2, -TEXTURE_SIZE / 2, 0);

        context.drawTexture(
                LINE_TEXTURE,
                0,
                0,
                0,
                0,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                TEXTURE_SIZE,
                TEXTURE_SIZE
        );

        context.getMatrices().pop();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == getKeyForCurrentLetter()) {
            if (isInCorrectArea(spinAngle)) {
                completedCircles[activeIndex] = true;
                if (activeIndex < LETTERS.length - 1) {
                    activeIndex++;
                    fadeIn = true;
                    fadeAlpha = 0f;
                    spinSpeed += 0.5f;
                } else {
                    onGameComplete();
                }
            } else {
                resetGame();
            }
        } else {
            resetGame();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean isInCorrectArea(float angle) {
        int startAngle = correctStartAngles[activeIndex];
        int endAngle = correctEndAngles[activeIndex];

        if (startAngle > endAngle) {
            return angle >= startAngle || angle <= endAngle;
        }
        return angle >= startAngle && angle <= endAngle;
    }

    private void resetGame() {
        activeIndex = 0;
        spinSpeed = 3f;
        spinAngle = 0f; // Reset spin angle
        fadeIn = true;
        fadeAlpha = 0f;
        for (int i = 0; i < completedCircles.length; i++) {
            completedCircles[i] = false;
        }
    }

    private int getKeyForCurrentLetter() {
        char letter = LETTERS[activeIndex].charAt(0);
        return GLFW.GLFW_KEY_A + (letter - 'a');
    }

    private void onGameComplete() {
        //TODO MANDAR PAQUETE AL SERVER AQUI
        this.client.setScreen(null);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
