package sensei;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SetupPhase {
    public static void play(RobotController rc) throws GameActionException {
        if (RobotPlayer.levelGrinder && rc.getLevel(SkillType.BUILD) < 5) {
            if (RobotPlayer.turnCount < 10) {
                Pathfinder.moveTowards(rc, RobotPlayer.reflectedSpawnLocation);
            }

            for (int i=RobotPlayer.directions.length; --i>=0;) {
                MapLocation digLoc = rc.getLocation().add(RobotPlayer.directions[i]);
                if (rc.canFill(digLoc)) {
                    rc.fill(digLoc);
                } else if (rc.canDig(digLoc)) {
                    rc.dig(digLoc);
                    // digFill = digLoc;
                }
            }
        } else {
            MapLocation[] crumbLocations = rc.senseNearbyCrumbs(-1);
            if (crumbLocations.length > 0 && !Pathfinder.hitObstacle) {
                Pathfinder.moveTowards(rc, crumbLocations[0]);
            }

            if (RobotPlayer.reflectedSpawnLocation != null) {
                Pathfinder.moveTowards(rc, RobotPlayer.reflectedSpawnLocation);
            }
            // Could do this but a bit complicated
            // MapLocation[] enemyFlagLocations = rc.senseBroadcastFlagLocations();
            // if (enemyFlagLocations.length >= 3) { 
            //     Pathfinder.moveTowards(rc, enemyFlagLocations[RobotPlayer.assignedEnemyFlag]);
            //     Communication.populateEnemyFlagLocations(rc, enemyFlagLocations);
            // } else {
            //     MapLocation[] storedEnemyFlagLocations = Communication.getEnemyFlagLocations(rc);
            //     if (storedEnemyFlagLocations != null) {
            //         // System.out.println("Got map location stored")
            //         Pathfinder.moveTowards(rc, storedEnemyFlagLocations[RobotPlayer.assignedEnemyFlag]);
            //     }
            // }
        }
    }
}