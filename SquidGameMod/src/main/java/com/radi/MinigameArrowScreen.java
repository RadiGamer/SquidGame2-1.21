package com.radi;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MinigameArrowScreen extends Screen {
    private static final Identifier BAR_TEXTURE = Identifier.of("squidgamegame2screens", "textures/gui/juego3.png");
    private static final Identifier ARROW_TEXTURE_DEFAULT = Identifier.of("squidgamegame2screens", "textures/gui/juego3_g.png");
    private static final Identifier ARROW_TEXTURE_SUCCESS = Identifier.of("squidgamegame2screens", "textures/gui/juego3_green.png");
    private static final Identifier ARROW_TEXTURE_FAIL = Identifier.of("squidgamegame2screens", "textures/gui/juego3_r.png");

    private static final int BAR_HEIGHT = (int) (108 * 1.2);
    private static final int BAR_WIDTH = (int) (42 * 1.2);
    private static final int ARROW_HEIGHT = (int) (10 * 1.2);
    private static final int ARROW_WIDTH = (int) (25 * 1.2);
    private static final int TARGET_HEIGHT = (int) (20 * 1.2);
    private static final int TOTAL_ATTEMPTS = 5;

    private int arrowY = 0;
    private int barX, barY;
    private boolean arrowGoingUp = true;
    private int arrowSpeed = 2;
    private int progress = 0;

    private Identifier currentArrowTexture = ARROW_TEXTURE_DEFAULT;
    private int feedbackCounter = 0;

    public MinigameArrowScreen() {
        super(Text.of("Arrow Minigame"));
    }

    @Override
    protected void init() {
        barX = this.width / 2 - BAR_WIDTH / 2;
        barY = this.height / 2 - BAR_HEIGHT / 2;
        arrowY = barY;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        super.render(context, mouseX, mouseY, delta);
        renderBar(context);

        renderProgressDots(context);
        renderArrow(context);
    }


    private void renderBar(DrawContext context) {
        context.drawTexture(BAR_TEXTURE, barX, barY, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
    }

    private void renderArrow(DrawContext context) {
        context.drawTexture(
                currentArrowTexture,
                barX + BAR_WIDTH / 2 - ARROW_WIDTH / 2,
                arrowY,
                0,
                0,
                ARROW_WIDTH,
                ARROW_HEIGHT,
                ARROW_WIDTH,
                ARROW_HEIGHT
        );

        if (feedbackCounter > 0) {
            feedbackCounter--;
            if (feedbackCounter == 0) {
                currentArrowTexture = ARROW_TEXTURE_DEFAULT;
            }
        }
    }

    private void renderProgressDots(DrawContext context) {
        int dotWidth = (int) (3 * 1.2);
        int dotHeight = (int) (3 * 1.2);
        int leftX = barX + (int) (3 * 1.2) - dotWidth / 2;
        int rightX = barX + (int) (37 * 1.2) - dotWidth / 2;

        int[] yLevels = {
                barY + (int) (85 * 1.2) - dotHeight / 2,
                barY + (int) (70 * 1.2) - dotHeight / 2,
                barY + (int) (54 * 1.2) - dotHeight / 2,
                barY + (int) (34 * 1.2) - dotHeight / 2,
                barY + (int) (13 * 1.2) - dotHeight / 2
        };

        for (int i = 0; i < TOTAL_ATTEMPTS; i++) {
            int color = (i < progress) ? 0xFF00FF00 : 0xFF444444;
            int dotY = yLevels[i];

            context.fill(leftX, dotY, leftX + dotWidth, dotY + dotHeight, color);
            context.fill(rightX, dotY, rightX + dotWidth, dotY + dotHeight, color);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 32) { // ESPACIO
            int targetTop = barY + (int) (98 * 1.2) - TARGET_HEIGHT / 2;
            int targetBottom = targetTop + TARGET_HEIGHT;

            if (arrowY >= targetTop && arrowY + ARROW_HEIGHT <= targetBottom) {
                progress++;
                currentArrowTexture = ARROW_TEXTURE_SUCCESS;
                feedbackCounter = 10;
                if (progress >= TOTAL_ATTEMPTS) {
                    onGameComplete();
                } else {
                    arrowSpeed++;
                }
            } else {
                progress = 0;
                arrowSpeed = 2;
                currentArrowTexture = ARROW_TEXTURE_FAIL;
                feedbackCounter = 10;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        if (arrowGoingUp) {
            arrowY -= arrowSpeed;
            if (arrowY <= barY) {
                arrowGoingUp = false;
            }
        } else {
            arrowY += arrowSpeed;
            if (arrowY + ARROW_HEIGHT >= barY + BAR_HEIGHT) {
                arrowGoingUp = true;
            }
        }
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
