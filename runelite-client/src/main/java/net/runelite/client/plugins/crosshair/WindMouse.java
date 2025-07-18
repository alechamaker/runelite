package net.runelite.client.plugins.crosshair;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.runelite.api.Point;

public class WindMouse {
  private static final double SQRT3 = Math.sqrt(3);
  private static final double SQRT5 = Math.sqrt(5);
  private static final Random random = new Random();
  private static double g0 = 9, w0 = 3, m0 = 15, d0 = 12;

  public static List<int[]> windMouse(int startX, int startY, int destX, int destY) {
    List<int[]> path = new ArrayList<>();
    double currentX = startX;
    double currentY = startY;
    double vX = 0, vY = 0, wX = 0, wY = 0;

    // Add the starting point to the path
    path.add(new int[] { startX, startY });

    while (currentX != destX && currentY != destY) {
      // Calculate Euclidean distance
      double dist = Math.hypot(destX - startX, destY - startY);
      if (dist < 1) {
        break;
      }

      double wMag = Math.min(w0, dist);
      if (dist >= d0) {
        wX = wX / SQRT3 + (2 * random.nextDouble() - 1) * wMag / SQRT5;
        wY = wY / SQRT3 + (2 * random.nextDouble() - 1) * wMag / SQRT5;
      } else {
        wX /= SQRT3;
        wY /= SQRT3;
        if (m0 < 3) {
          m0 = random.nextDouble() * 3 + 3;
        } else {
          m0 /= SQRT5;
        }
      }

      vX += wX + g0 * (destX - startX) / dist;
      vY += wY + g0 * (destY - startY) / dist;

      double vMag = Math.hypot(vX, vY);
      if (vMag > m0) {
        double vClip = m0 / 2 + random.nextDouble() * m0 / 2;
        vX = (vX / vMag) * vClip;
        vY = (vY / vMag) * vClip;
      }

      startX += vX;
      startY += vY;
      int moveX = (int) Math.round(startX);
      int moveY = (int) Math.round(startY);

      if ((int) currentX != moveX || (int) currentY != moveY) {
        path.add(new int[] { moveX, moveY });
        currentX = moveX;
        currentY = moveY;
      }
    }

    return path;
  }

  public static List<Point> windMouse(Point start, Point dest) {
    var points = windMouse(start.getX(), start.getY(), dest.getX(), dest.getY());
    List<Point> result = new ArrayList<>();
    for (var p : points) {
      result.add(new Point(p[0], p[1]));
    }
    return result;
  }
}
