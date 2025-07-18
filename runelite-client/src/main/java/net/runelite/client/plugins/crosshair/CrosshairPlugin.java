package net.runelite.client.plugins.crosshair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.List;
import net.runelite.client.util.ImageUtil;
import java.awt.image.BufferedImage;
import net.runelite.client.callback.ClientThread;

import javax.inject.Inject;

import java.awt.event.MouseEvent;
import java.awt.*;
import net.runelite.api.Point;
import net.runelite.client.ui.NavigationButton;
import net.runelite.api.Client;
import java.util.stream.Collectors;
import net.runelite.api.NPC;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.devtools.DevToolsPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.crosshair.CrosshairOverlay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@PluginDescriptor(name = "Crosshair Plugin", description = "Custom crosshair with click indicators")
public class CrosshairPlugin extends Plugin {
  private static final Logger log = LoggerFactory.getLogger(CrosshairPlugin.class);

  @Inject
  private Client client;

  @Inject
  private CrosshairOverlay overlay;

  @Inject
  private OverlayManager overlayManager;

  @Inject
  private ClientToolbar clientToolbar;

  @Inject
  private ClientThread clientThread;

  private String lastMenuOption = "";
  private int actionTick = -1000;
  private int currentTick = 0;

  private NavigationButton navButton;

  void clickDiff() {
    clientThread.invoke(() -> {
      var p = new Point(300, 300);
      MouseEvent me = new MouseEvent(
          client.getCanvas(),
          MouseEvent.MOUSE_MOVED,
          System.currentTimeMillis(),
          0, // Modifiers
          p.getX(),
          p.getY(),
          0, // Click count
          false, // Popup trigger
          MouseEvent.NOBUTTON // No button
      );
      client.getCanvas().dispatchEvent(me);
      log.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
      log.debug("Dispatched MOUSE_MOVED to Point ({}, {})",
          p.getX(), p.getY());

      // Log actual cursor position
      Point actualPos = client.getMouseCanvasPosition();
      log.debug("Actual cursor position after MouseEvent: ({}, {})",
          actualPos.getX(), actualPos.getY());

      log.debug("Difference: {},{}",
          p.getX() - actualPos.getX(), p.getY() - actualPos.getY());

      var bounds = client.getCanvas().getBounds();
      log.debug("Bounds: {}x{}@[{},{}]", bounds.width, bounds.height, bounds.x, bounds.y);

      var vpw = client.getViewportWidth();
      var vph = client.getViewportWidth();
      var vpx = client.getViewportXOffset();
      var vpy = client.getViewportYOffset();
      log.debug("View Port: {}x{}@[{},{}]", vpw, vph, vpx, vpy);
      log.debug("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    });
  }

  void MoveMouseToPoint(Point point) {
    clientThread.invoke(() -> {
      Rectangle canvasBounds = client.getCanvas().getBounds();
      Dimension stretchedDim = client.isStretchedEnabled() ? client.getStretchedDimensions() : null;
      Dimension realDim = client.isStretchedEnabled() ? client.getRealDimensions() : null;
      float scaleX = stretchedDim != null && realDim != null ? (float) stretchedDim.width / realDim.width : 1.0f;
      float scaleY = stretchedDim != null && realDim != null ? (float) stretchedDim.height / realDim.height : 1.0f;
      log.debug("Canvas bounds: x={}, y={}, width={}, height={}",
          canvasBounds.x, canvasBounds.y, canvasBounds.width, canvasBounds.height);
      log.debug("Camera: yaw={}, pitch={}, scale={}, stretched={}",
          client.getCameraYaw(), client.getCameraPitch(), client.getScale(),
          client.isStretchedEnabled());
      if (client.isStretchedEnabled()) {
        log.debug("Stretched mode: stretchedDim={}x{}, realDim={}x{}, scaleX={}, scaleY={}",
            stretchedDim.width, stretchedDim.height, realDim.width, realDim.height, scaleX, scaleY);
      }

      // Adjust for scaling in resizable/stretched mode
      int adjustedX = Math.round(point.getX() * scaleX);
      int adjustedY = Math.round(point.getY() * scaleY);

      log.debug("Adjusted Canvas Point for MouseEvent: ({}, {})", adjustedX, adjustedY);

      // Verify canvas bounds
      if (adjustedX < 0 || adjustedX >= client.getCanvasWidth() ||
          adjustedY < 0 || adjustedY >= client.getCanvasHeight()) {
        log.warn("Adjusted point ({}, {}) is outside canvas bounds ({}x{})",
            adjustedX, adjustedY, client.getCanvasWidth(), client.getCanvasHeight());
        return;
      }

      // Dispatch MOUSE_MOVED event
      MouseEvent me = new MouseEvent(
          client.getCanvas(),
          MouseEvent.MOUSE_MOVED,
          System.currentTimeMillis(),
          0, // Modifiers
          adjustedX,
          adjustedY,
          0, // Click count
          false, // Popup trigger
          MouseEvent.NOBUTTON // No button
      );
      client.getCanvas().dispatchEvent(me);
      log.debug("Dispatched MOUSE_MOVED to Adjusted Canvas Point ({}, {})",
          adjustedX, adjustedY);

      // Log actual cursor position
      Point actualPos = client.getMouseCanvasPosition();
      log.debug("Actual cursor position after MouseEvent: ({}, {})",
          actualPos.getX(), actualPos.getY());

    });
  }

  void MouseAlongPath(List<Point> path) {
    var rnd = new Random(System.currentTimeMillis());
    for (var p : path) {
      MoveMouseToPoint(p);
      try {
        int sleepTime = rnd.nextInt(41) + 10; // 10 to 50 ms
        Thread.sleep(sleepTime);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        e.printStackTrace();
      }
    }
  }

  void MoveToBanker() {
    clientThread.invoke(() -> {
      List<NPC> npcs = client.getTopLevelWorldView().npcs()
          .stream()
          .collect(Collectors.toCollection(ArrayList::new));
      NPC banker = null;
      List<Integer> bankerIds = Arrays.asList(1613, 1618, 1633, 1634, 3089, 5263, 6945, 6946);
      var distance = Integer.MAX_VALUE;
      for (var n : npcs) {
        if (bankerIds.contains(n.getId())) {
          // get the banker that is closest to the player
          var tmpDistance = client.getLocalPlayer().getWorldArea().distanceTo(n.getWorldLocation());
          log.info("Player is '{}' away from {}", tmpDistance, n.getId());
          if (tmpDistance < distance) {
            distance = tmpDistance;
            banker = n;
            log.info("chose: {}", banker.getId());
          }
        }
      }
      if (banker != null) {
        var hull = banker.getConvexHull();
        var bankerHullCenter = new Point(
            (int) hull.getBounds().getCenterX(),
            (int) hull.getBounds().getCenterY());
        log.info("hull center: {},{}", bankerHullCenter.getX(), bankerHullCenter.getY());

        // class path {
        // //define whatever decorators for json parsing
        // List<Point>
        // }

        // ProcessBuilder processBuilder = new ProcessBuilder("humanmove
        // --path-to={},{}", thisplace.x, thisplace.y);
        // BufferedReader reader = new BufferedReader(
        // new InputStreamReader(process.getInputStream()));
        // String line;
        // String rawJson = "";
        // while ((line = reader.readLine()) != null) {
        // rawJson += line;
        // }
        // convert json to class: path
        // call function to move mouse along path
        // MoveMouseToPoint(bankerHullCenter);

        var points = WindMouse.windMouse(new Point(0, 220), bankerHullCenter);

      }
    });
  }

  private void onNavButtonClick() {
    MoveToBanker();
  }

  @Override
  protected void startUp() {
    log.info("CrosshairPlugin started!");
    final BufferedImage icon = ImageUtil.loadImageResource(DevToolsPlugin.class, "devtools_icon.png");
    navButton = NavigationButton.builder()
        .tooltip("Crosshair Plugin")
        .icon(icon)
        .priority(0)
        .onClick(this::onNavButtonClick)
        .build();
    clientToolbar.addNavigation(navButton);
    overlayManager.add(overlay);
  }

  @Override
  protected void shutDown() {
    log.info("CrosshairPlugin shutting down!");
    overlayManager.remove(overlay);
    lastMenuOption = "";
    actionTick = -1000;
    currentTick = 0;
  }

  @Subscribe
  public void onGameTick(GameTick event) {
    currentTick++;
  }

  @Subscribe
  public void onMenuOptionClicked(MenuOptionClicked event) {
    lastMenuOption = event.getMenuOption();
    actionTick = currentTick;
  }

  // Getters for overlay
  public String getLastMenuOption() {
    return lastMenuOption;
  }

  public int getActionTick() {
    return actionTick;
  }

  public int getCurrentTick() {
    return currentTick;
  }
}
