package sensei;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

class Node {
    public MapLocation loc;
    public int g;
    public int h;

    public Node(MapLocation location, int gscore, int hscore) {
        loc = location;
        g = gscore;
        h = hscore;
    }
}

public class Pathfinder {
    // Implement new pathfinding algorithm.
    // Based on circle of radius sensing.
    // Uses more bytecode but accurate.
    static Dijkstra6 djik6;
    static Dijkstra13 djik13;
    static Dijkstra20 djik20;

    public Pathfinder(RobotController rc) {
        djik6 = new Dijkstra6(rc);
        djik13 = new Dijkstra13(rc);
        djik20 = new Dijkstra20(rc);
    }

    public static void moveTowards(RobotController rc, MapLocation dest) throws GameActionException {
        int bytecodeLeft = Clock.getBytecodesLeft();
        Direction dir;
        if (bytecodeLeft < 3000) {
            dir = djik13.getBestDirection(dest, rc.getLocation().directionTo(dest).opposite());
        } else if (bytecodeLeft < 2000) {
            dir = djik6.getBestDirection(dest, rc.getLocation().directionTo(dest).opposite());
        } else {
            dir = djik20.getBestDirection(dest, rc.getLocation().directionTo(dest).opposite());
        }
        if (dir != null) {
            if (rc.canMove(dir)) rc.move(dir);
            else if (rc.canFill(rc.getLocation().add(dir))) rc.fill(rc.getLocation().add(dir));
        }
    }

    public static void moveRandom(RobotController rc) throws GameActionException {
        Direction randomDir = Direction.allDirections()[RobotPlayer.rng.nextInt(8)];
        if (rc.canMove(randomDir)) rc.move(randomDir);
    }
}
