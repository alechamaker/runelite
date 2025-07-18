package net.runelite.client.ui.overlay.crosshair;

import java.awt.*;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.plugins.crosshair.CrosshairPlugin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CrosshairOverlay extends Overlay {
  private static final float STROKE_WIDTH = 2.0f;

  private final Client client;
  private final CrosshairPlugin plugin;

  @Inject
  public CrosshairOverlay(Client client, CrosshairPlugin plugin) {
    this.client = client;
    this.plugin = plugin;
    setPosition(OverlayPosition.DYNAMIC);
    setLayer(OverlayLayer.ABOVE_WIDGETS);
  }

  @Override
  public Dimension render(Graphics2D graphics) {
    Point mousePos = client.getMouseCanvasPosition();
    if (mousePos == null) {
      return null;
    }

    int width = client.getCanvasWidth();
    int height = client.getCanvasHeight();

    Color color = Color.LIGHT_GRAY;

    if (plugin.getCurrentTick() - plugin.getActionTick() <= 1) {
      if (plugin.getLastMenuOption().equalsIgnoreCase("Walk here")) {
        color = Color.YELLOW;
      } else {
        color = Color.RED;
      }
    }

    graphics.setColor(color);
    graphics.setStroke(new BasicStroke(STROKE_WIDTH));

    graphics.drawLine(mousePos.getX(), 0, mousePos.getX(), height);
    graphics.drawLine(0, mousePos.getY(), width, mousePos.getY());
    OverlayUtil.renderTextLocation(graphics,
        mousePos,
        String.format("[%d, %d]", mousePos.getX(), mousePos.getY()),
        color);

    return null;
  }
}
