package warrior;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Pathfinder {
    public static void moveTowards(RobotController rc, MapLocation dest) throws GameActionException {
        bugNav2(rc, dest);
    }

    public static void moveRandomWithDest(RobotController rc, MapLocation loc) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(loc);
        if (rc.canMove(dir)) rc.move(dir);
        else if (rc.canFill(rc.getLocation().add(dir))) rc.fill(rc.getLocation().add(dir));
        else if (!rc.senseMapInfo(rc.getLocation().add(dir)).isPassable()) {
            // Try to go orthogonal to direction or even backwards
            if (rc.canMove(dir.rotateLeft())) rc.move(dir.rotateLeft());
            else if (rc.canMove(dir.rotateRight())) rc.move(dir.rotateRight());
            else if (rc.canMove(dir.opposite())) rc.move(dir.opposite());
        }
    }

    public static void moveRandom(RobotController rc) throws GameActionException {
        Direction randomDir = Direction.allDirections()[RobotPlayer.rng.nextInt(8)];
        if (rc.canMove(randomDir)) rc.move(randomDir);
    }

    // Need new pathfinding algorithms

    private static MapLocation prevDest = null;
    private static HashSet<MapLocation> line = null;
    private static int obstacleStartDist = 0;
    private static int bugState = 0; // 0 head to target, 1 circle obstacle
    private static Direction bugDir = null;

    public static void bugNav2(RobotController rc, MapLocation destination) throws GameActionException{
        if(!destination.equals(prevDest)) {
            prevDest = destination;
            line = Utilities.createLine(rc.getLocation(), destination);
        }

        for(MapLocation loc : line) {
            rc.setIndicatorDot(loc, 255, 0, 0);
        }

        if(bugState == 0) {
            bugDir = rc.getLocation().directionTo(destination);
            if(rc.canMove(bugDir)){
                rc.move(bugDir);
            } else {
                bugState = 1;
                obstacleStartDist = rc.getLocation().distanceSquaredTo(destination);
                bugDir = rc.getLocation().directionTo(destination);
            }
        } else {
            if(line.contains(rc.getLocation()) && rc.getLocation().distanceSquaredTo(destination) < obstacleStartDist) {
                bugState = 0;
            }

            for(int i = 0; i < 9; i++){
                if(rc.canMove(bugDir)){
                    rc.move(bugDir);
                    bugDir = bugDir.rotateRight();
                    bugDir = bugDir.rotateRight();
                    break;
                } else {
                    bugDir = bugDir.rotateLeft();
                }
            }
        }
    }
}