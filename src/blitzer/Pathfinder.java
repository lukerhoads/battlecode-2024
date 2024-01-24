package blitzer;

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

    public static void straightShot(RobotController rc, MapLocation dest) throws GameActionException {
        if (rc.canMove(rc.getLocation().directionTo(dest))) rc.move(rc.getLocation().directionTo(dest));
    }

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

        if(bugState == 0) {
            bugDir = rc.getLocation().directionTo(destination);
            if(rc.canMove(bugDir)){
                rc.move(bugDir);
            } else if (rc.canFill(rc.getLocation().add(bugDir))) {
                rc.fill(rc.getLocation().add(bugDir));
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