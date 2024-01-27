package blitzer;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Utilities {
    public static void globalUpgrade(RobotController rc) throws GameActionException {
        if (rc.canBuyGlobal(GlobalUpgrade.ACTION)) rc.buyGlobal(GlobalUpgrade.ACTION);
        if (rc.canBuyGlobal(GlobalUpgrade.CAPTURING)) rc.buyGlobal(GlobalUpgrade.CAPTURING);
    }

    public static int locationListIndex(MapLocation[] lst, MapLocation obj) {
        for (int i=lst.length; --i>=0;) {
            if (lst[i].equals(obj)) return i;
        }

        return -1;
    }

    public static boolean locationListContains(MapLocation[] lst, MapLocation obj) {
        for (int i=lst.length; --i>=0;) {
            if (lst[i].equals(obj)) return true;
        }

        return false;
    }

    public static int minIndex(int[] list) {
        int res = 0;
        for (int i = 1; i < list.length; i++) {
            if (list[i] < list[res]) {
                res = i;
            }
        }
        
        return res;
    }

    public static MapLocation getClosestSpawnLocation(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocations = rc.getAllySpawnLocations();
        int[] spawnLocationDist = new int[spawnLocations.length];
        for (int i = 0; i < spawnLocations.length; i++) {
            spawnLocationDist[i] = rc.getLocation().distanceSquaredTo(spawnLocations[i]);
        }

        return spawnLocations[minIndex(spawnLocationDist)];
    }

    public static MapLocation[] getFlagLocations(RobotController rc) throws GameActionException {
        MapLocation[] allySpawnLocations = rc.getAllySpawnLocations();
        MapLocation[] flagLocations = new MapLocation[3];
        int numSet = 0;
        for (MapLocation allySpawnLocation : allySpawnLocations) {
            if (locationListContains(allySpawnLocations, allySpawnLocation.translate(0, 1)) 
                && locationListContains(allySpawnLocations, allySpawnLocation.translate(1, 1))
                && locationListContains(allySpawnLocations, allySpawnLocation.translate(1, 0))
                && locationListContains(allySpawnLocations, allySpawnLocation.translate(-1, 0))
                && locationListContains(allySpawnLocations, allySpawnLocation.translate(0, -1))
                && locationListContains(allySpawnLocations, allySpawnLocation.translate(-1, -1))
                && locationListContains(allySpawnLocations, allySpawnLocation.translate(1, -1))
                && locationListContains(allySpawnLocations, allySpawnLocation.translate(-1, 1))) {
                flagLocations[numSet] = allySpawnLocation;
                numSet++;
            }
        }
        
        return flagLocations;
    }

    public static Contract[] getBuildContracts(MapLocation loc) {
        Contract[] results = new Contract[8];
        results[0] = new Contract(loc.translate(2, 0), TrapType.EXPLOSIVE);
        results[1] = new Contract(loc.translate(1, 1), TrapType.EXPLOSIVE);
        results[2] = new Contract(loc.translate(0, 2), TrapType.EXPLOSIVE);
        results[3] = new Contract(loc.translate(-1, 1), TrapType.EXPLOSIVE);
        results[4] = new Contract(loc.translate(-2, 0), TrapType.EXPLOSIVE);
        results[5] = new Contract(loc.translate(-1, -1), TrapType.EXPLOSIVE);
        results[6] = new Contract(loc.translate(0, -2), TrapType.EXPLOSIVE);
        results[7] = new Contract(loc.translate(1, -1), TrapType.EXPLOSIVE);
        return results;
    }

    public static int assignRole(RobotController rc) throws GameActionException {
        for (int i=0; i<6; i++) {
            int defenderID = rc.readSharedArray(i);
            if (defenderID == 0) {
                rc.writeSharedArray(i, rc.getID());
                return i / 2;
            }
        }

        return -1;
    }

    public static HashSet<MapLocation> createLine(MapLocation a, MapLocation b) {
        HashSet<MapLocation> locs = new HashSet<>();
        int x = a.x, y = a.y;
        int dx = b.x - a.x;
        int dy = b.y - a.y;
        int sx = (int) Math.signum(dx);
        int sy = (int) Math.signum(dy);
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        int d = Math.max(dx,dy);
        int r = d/2;
        if (dx > dy) {
            for (int i = 0; i < d; i++) {
                locs.add(new MapLocation(x, y));
                x += sx;
                r += dy;
                if (r >= dx) {
                    locs.add(new MapLocation(x, y));
                    y += sy;
                    r -= dx;
                }
            }
        }
        else {
            for (int i = 0; i < d; i++) {
                locs.add(new MapLocation(x, y));
                y += sy;
                r += dx;
                if (r >= dy) {
                    locs.add(new MapLocation(x, y));
                    x += sx;
                    r -= dy;
                }
            }
        }
        locs.add(new MapLocation(x, y));
        return locs;
    }

    public static void incrementSharedArray(RobotController rc, int index) throws GameActionException {
        int val = rc.readSharedArray(index);
        rc.writeSharedArray(index, val + 1);
    }

    public static MapLocation reflectLocation(RobotController rc, MapLocation loc) throws GameActionException {
        return new MapLocation(rc.getMapWidth() - loc.x, rc.getMapHeight() - loc.y);
    }
}
