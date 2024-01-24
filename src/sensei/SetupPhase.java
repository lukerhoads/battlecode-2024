package sensei;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SetupPhase {
    public static MapLocation digFill;

    public static void play(RobotController rc) throws GameActionException {
        if (RobotPlayer.defender) {
            Pathfinder.moveTowards(rc, digFill);
        } else if (RobotPlayer.levelGrinder && rc.getLevel(SkillType.BUILD) < 5) {
            if (digFill != null) {
                if (rc.canFill(digFill)) {
                    rc.fill(digFill);
                    digFill = null;
                }
            }

            for (int i=RobotPlayer.directions.length; --i>=0;) {
                MapLocation digLoc = rc.getLocation().add(RobotPlayer.directions[i]);
                if (rc.canDig(digLoc)) {
                    rc.dig(digLoc);
                    digFill = digLoc;
                    break;
                } 
            }
        } else {
            MapLocation[] crumbLocations = rc.senseNearbyCrumbs(-1);
            if (crumbLocations.length > 0) {
                Pathfinder.moveTowards(rc, crumbLocations[0]);
            }

            MapLocation[] enemyFlagLocations = rc.senseBroadcastFlagLocations();
            if (enemyFlagLocations.length >= 3) { 
                Pathfinder.moveTowards(rc, enemyFlagLocations[RobotPlayer.assignedFlag]);
                Communication.populateEnemyFlagLocations(rc, enemyFlagLocations);
            } else {
                MapLocation[] storedEnemyFlagLocations = Communication.getEnemyFlagLocations(rc);
                if (storedEnemyFlagLocations != null) {
                    // System.out.println("Got map location stored")
                    Pathfinder.moveTowards(rc, storedEnemyFlagLocations[RobotPlayer.assignedFlag]);
                }
            }
        }
    }
}