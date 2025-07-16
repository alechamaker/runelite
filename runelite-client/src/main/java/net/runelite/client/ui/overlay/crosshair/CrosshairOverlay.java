package net.runelite.client.ui.overlay.crosshair;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import javax.inject.Singleton;

import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

@Singleton
public class CrosshairOverlay extends Overlay
{
	private static final Color CROSSHAIR_COLOR = Color.RED;
	private static final float STROKE_WIDTH = 2.0f;

	private final Client client;

	@Inject
	private CrosshairOverlay(Client client)
	{
		this.client = client;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Point mousePos = client.getMouseCanvasPosition();
		if (mousePos == null)
		{
			return null;
		}

		int width = client.getCanvasWidth();
		int height = client.getCanvasHeight();

		graphics.setColor(CROSSHAIR_COLOR);
		graphics.setStroke(new BasicStroke(STROKE_WIDTH));

		// Vertical line
		graphics.drawLine(mousePos.getX(), 0, mousePos.getX(), height);

		// Horizontal line
		graphics.drawLine(0, mousePos.getY(), width, mousePos.getY());

		return null;
	}
}
