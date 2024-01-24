package warrior_2nd_comp;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Utilities {
    public static void spawn(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        MapLocation randomLoc = spawnLocs[RobotPlayer.rng.nextInt(spawnLocs.length)];
        if (rc.canSpawn(randomLoc)) rc.spawn(randomLoc);
    }

    public static Role assignRole(RobotController rc) throws GameActionException {
        int maxNumAttackers = 20;
        int numAttackers = rc.readSharedArray(0);
        if (numAttackers < maxNumAttackers) {
            rc.writeSharedArray(0, numAttackers + 1);
            return Role.ATTACKER;
        }

        int maxNumDefenders = 25;
        int numDefenders = rc.readSharedArray(1);
        if (numDefenders < maxNumDefenders) {
            rc.writeSharedArray(1, numDefenders + 1);
            return Role.DEFENDER;
        }

        int maxNumCrumbCollectors = 5;
        int numCrumbCollectors = rc.readSharedArray(2);
        if (numCrumbCollectors < maxNumCrumbCollectors) {
            rc.writeSharedArray(2, numCrumbCollectors + 1);
            return Role.CRUMBS;
        }

        return Role.UNSET;
    }

    public static void globalUpgrade(RobotController rc) throws GameActionException {
        if (rc.canBuyGlobal(GlobalUpgrade.ACTION)) rc.buyGlobal(GlobalUpgrade.ACTION);
        if (rc.canBuyGlobal(GlobalUpgrade.CAPTURING)) rc.buyGlobal(GlobalUpgrade.CAPTURING);
    }

    public static MapLocation optimalCorner(RobotController rc) {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        int totalX = 0;
        int totalY = 0;
        for (int i = 0; i < spawnLocs.length; i++) {
            totalX += spawnLocs[i].x;
            totalY += spawnLocs[i].y;
        }

        int averageX = totalX / spawnLocs.length;
        int averageY = totalY / spawnLocs.length;
        if (averageX > rc.getMapWidth() / 2) {
            if (averageY > rc.getMapHeight() / 2) {
                return new MapLocation(rc.getMapWidth() - 1, rc.getMapHeight() - 1);
            } else {
                return new MapLocation(rc.getMapWidth() - 1, 0);
            }
        } else {
            if (averageY > rc.getMapHeight() / 2) {
                return new MapLocation(0, rc.getMapHeight() - 1);
            } else {
                return new MapLocation(0, 0);
            }
        }
    }

    public static MapLocation[] optimalDropLocations(RobotController rc) throws GameActionException {
        MapLocation[] results = new MapLocation[3];
        MapLocation optimalLocation = calculateOptimalDropoffLocation(rc);
        MapLocation[] alternateLocations = modifyDropOffLocation(rc, optimalLocation);
        results[0] = optimalLocation;
        results[1] = alternateLocations[0];
        results[2] = alternateLocations[1];
        return results;
    }

    public static MapLocation[] modifyDropOffLocation(RobotController rc, MapLocation optimalCorner) throws GameActionException {
        MapLocation[] results = new MapLocation[2];
        int halfMapHeight = rc.getMapHeight() / 2;
        int halfMapWidth = rc.getMapWidth() / 2;
        if (optimalCorner.y > halfMapHeight - rc.getMapHeight() / 5 && optimalCorner.y < halfMapHeight + rc.getMapHeight() / 5) {
            results[0] = optimalCorner.translate(0, -6);
            results[1] = optimalCorner.translate(0, 6);
            return results;
        } 
        
        if (optimalCorner.x > halfMapWidth - rc.getMapWidth() / 5 && optimalCorner.x < halfMapWidth + rc.getMapWidth() / 5) {
            results[0] = optimalCorner.translate(-6, 0);
            results[1] = optimalCorner.translate(-6, 0);
            return results;
        } 

        if (optimalCorner.y > halfMapHeight) {
            if (optimalCorner.x > halfMapWidth) {
                results[0] = optimalCorner.translate(0, -6);
                results[1] = optimalCorner.translate(-6, 0);
            } else {
                results[0] = optimalCorner.translate(0, -6);
                results[1] = optimalCorner.translate(6, 0);
            }
        } else {
            if (optimalCorner.x > halfMapWidth) {
                results[0] = optimalCorner.translate(0, 6);
                results[1] = optimalCorner.translate(-6, 0);
            } else {
                results[0] = optimalCorner.translate(0, 6);
                results[1] = optimalCorner.translate(6, 0);
            }
        }

        return results;
    } 

    public static int evaluateEquation(int slope, int eqX, int eqY, int x) {
        return slope * (x - eqX) + eqY;
    }

    public static int inverseEvaluateEquation(int slope, int eqX, int eqY, int y) {
        return ((y - eqY) / slope) + eqX;
    }

    public static boolean validDefenseDirection(RobotController rc, Direction dir, MapLocation optimalCorner) {
        if (optimalCorner.y > rc.getMapHeight() / 2) {
            if (optimalCorner.x > rc.getMapWidth() / 2) {
                return dir == Direction.WEST || dir == Direction.SOUTHWEST || dir == Direction.SOUTH;
            } else {
                return dir == Direction.EAST || dir == Direction.SOUTH || dir == Direction.SOUTHEAST;
            }
        } else {
            if (optimalCorner.x > rc.getMapWidth() / 2) {
                return dir == Direction.WEST || dir == Direction.NORTH || dir == Direction.NORTHWEST;
            } else {
                return dir == Direction.EAST || dir == Direction.SOUTH || dir == Direction.NORTHEAST;
            }
        }
    }

    public static MapLocation calculateOptimalDropoffLocation(RobotController rc) {
        MapLocation[] spawnLocations = rc.getAllySpawnLocations();
        double[] x = new double[27];
        double[] y = new double[27];
        int totalX = 0;
        int totalY = 0;
        for (int i = 0; i < spawnLocations.length; i++) {
            x[i] = spawnLocations[i].x;
            y[i] = spawnLocations[i].y;
            totalX += spawnLocations[i].x;
            totalY += spawnLocations[i].y;
        }
        LinearRegression rg = new LinearRegression(x, y);
        int inverseReciprocalSlope = (int)(-(1/rg.slope()));
        int avgX = totalX / spawnLocations.length;
        int avgY = totalY / spawnLocations.length;
        // Evaluate at both 0 and max width
        int evalAt0 = evaluateEquation(inverseReciprocalSlope, avgX, avgY, 0);
        int evalAtMax = evaluateEquation(inverseReciprocalSlope, avgX, avgY, rc.getMapWidth() - 1);
        MapLocation pointOne = new MapLocation(0, evalAt0);
        MapLocation pointTwo = new MapLocation(rc.getMapWidth() - 1, evalAtMax);
        if (evalAt0 > rc.getMapHeight()) {
            pointOne = new MapLocation(inverseEvaluateEquation(inverseReciprocalSlope, avgX, avgY, rc.getMapHeight() - 1), rc.getMapHeight() - 1);
        } else if (evalAt0 < 0) {
            pointOne = new MapLocation(inverseEvaluateEquation(inverseReciprocalSlope, avgX, avgY, 0), 0);
        } else if (evalAtMax > rc.getMapHeight()) {
            pointTwo = new MapLocation(inverseEvaluateEquation(inverseReciprocalSlope, avgX, avgY, rc.getMapHeight() - 1), rc.getMapHeight() - 1);
        } else if (evalAtMax < 0) {
            pointTwo = new MapLocation(inverseEvaluateEquation(inverseReciprocalSlope, avgX, avgY, 0), 0);
        }

        // See which one is closer to avg -- whichever is closer is the chosen point.
        MapLocation avgLocation = new MapLocation(avgX, avgY);
        if (pointOne.distanceSquaredTo(avgLocation) < pointTwo.distanceSquaredTo(avgLocation)) {
            return pointOne;
        }

        return pointTwo;
    }

    public static int encodeLocation(RobotController rc, MapLocation loc) {
        return loc.y * rc.getMapWidth() + loc.x;
    }

    public static MapLocation decodeLocation(RobotController rc, int num) {
        return new MapLocation(num % rc.getMapWidth(), num / rc.getMapWidth());
    }

    public static void build(RobotController rc, MapLocation targetFlag) throws GameActionException {
        TrapType building = TrapType.NONE;
        int distance = rc.getLocation().distanceSquaredTo(targetFlag);
        if (distance < rc.getMapWidth() / 6) building = TrapType.EXPLOSIVE;
        else if (distance < rc.getMapWidth() / 4) building = TrapType.STUN;
        else if (distance > rc.getMapWidth() / 3) building = TrapType.WATER;
        int trapsBuiltAroundFlags = rc.readSharedArray(5);
        if (rc.canBuild(building, rc.getLocation()) && building != TrapType.NONE) {
            rc.build(building, rc.getLocation());
            rc.writeSharedArray(5, trapsBuiltAroundFlags + 1);
        } else {
            Pathfinder.moveRandom(rc);
        }
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

    public static void incrementSharedArray(RobotController rc, int index) throws GameActionException {
        int val = rc.readSharedArray(index);
        rc.writeSharedArray(index, val + 1);
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

    public static boolean validDropLocation(RobotController rc, int numFlag) throws GameActionException {
        MapLocation[] otherDropLocations = new MapLocation[2];
        int appendIndex = 0;
        for (int i = 1; i < 4; i++) {
            if (i != numFlag) {
                otherDropLocations[appendIndex] = Utilities.decodeLocation(rc, rc.readSharedArray(7 + i));
                appendIndex++;
            }
        }

        for (MapLocation otherDropLoc : otherDropLocations) {
            if (rc.getLocation().distanceSquaredTo(otherDropLoc) < 6) {
                return false;
            }
        }

        return true;
    }

    public static MapLocation getClosestSpawnLocation(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocations = rc.getAllySpawnLocations();
        int[] spawnLocationDist = new int[spawnLocations.length];
        for (int i = 0; i < spawnLocations.length; i++) {
            spawnLocationDist[i] = rc.getLocation().distanceSquaredTo(spawnLocations[i]);
        }

        return spawnLocations[minIndex(spawnLocationDist)];
    }
}