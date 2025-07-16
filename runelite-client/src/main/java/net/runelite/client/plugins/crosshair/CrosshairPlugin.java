package net.runelite.client.plugins.crosshair;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.crosshair.CrosshairOverlay;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

@PluginDescriptor(
    name = "crosshair",
    description = "deez nuts",
    tags = {"action", "activity", "external", "integration", "status"}
)
@Slf4j
public class CrosshairPlugin extends Plugin
{
    @Inject
    private OverlayManager overlayManager;

    @Inject
    private CrosshairOverlay crosshairOverlay;

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(crosshairOverlay);
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(crosshairOverlay);
    }
}
