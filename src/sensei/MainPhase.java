package sensei;

import battlecode.common.*;
import blitzer.Pathfinder;
import blitzer.Utilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainPhase {
    public static void play(RobotController rc) throws GameActionException {
       // Dig
       // Build traps
       // follow A* (iteratively)
       // at the start of each turn:
       RobotInfo[] enemiesSeen = rc.senseNearbyRobots(20, rc.getTeam().opponent());
       for (RobotInfo enemySeen : enemiesSeen) {
            int distanceToEnemy = rc.getLocation().distanceSquaredTo(enemySeen.getLocation());
       if (distanceToEnemy <= 4) {
        //attack 
       }
       else if (distanceToEnemy <= 6) // maybe workshop this distance
       // post up
       }

       
       // when an enemy is sensed, stay just outside of attack range and attack if enemy moves forward
       // broadcast ('posted up condition' and number of enemies seen)
       // when ally arrives, share info, ally either 'post up' or swarm attack
       // wait until enough allies also posting up where we outnumber enemy
       // both move in at once to trade and gain ground
       // runner follows behind


       // all robots have same role
       // post up on seeing enemy
       // outnumber enemy
       // heuristic function 
       // when robot is close enough to flag / when robot can pick up flag, pick up
       // if flag picked up, beeline to closest ally flag location

       // Check if another robot has picked up a flag.
       if (rc.readSharedArray(10 + RobotPlayer.assignedFlag) != 0) {
            MapLocation dest = Communication.decodeLocation(rc, rc.readSharedArray(10 + RobotPlayer.assignedFlag));
            Pathfinder.moveTowards(rc, dest);
       } else if (rc.hasFlag()) {
            MapLocation destination = blitzer.Utilities.getClosestSpawnLocation(rc);
            if (rc.canDropFlag(destination)) {
                rc.dropFlag(destination);
                rc.writeSharedArray(10 + RobotPlayer.assignedFlag, 0);
            }
            Pathfinder.moveTowards(rc, destination);
            // Move towards spawn location, broadcasting location
            rc.writeSharedArray(10 + RobotPlayer.assignedFlag, Communication.encodeLocation(rc, rc.getLocation()));
       }
       
       // if ally robot has picked up flag, swarm to protect
    }
}