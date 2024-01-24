package blitzer;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainPhase {
    public static void play(RobotController rc) throws GameActionException {
        if (RobotPlayer.role == Role.ATTACKER) {
            // Populate enemy flags
            
            MapLocation[] enemyFlagLocations = rc.senseBroadcastFlagLocations();
            for (int i=0; i<enemyFlagLocations.length; i++) {
                Communication.broadcastLocation(rc, 9+i, enemyFlagLocations[i].x, enemyFlagLocations[i].y);
            }

            // Move towards enemy flags
            // If sensing to exact location, put in location and mark that it was sensed accurately
            // Go for the flag
            if (rc.hasFlag()) {
                Pathfinder.moveTowards(rc, Utilities.getClosestSpawnLocation(rc));
                return;
            }
        } else if (RobotPlayer.role == Role.DEFENDER) {
            RobotInfo[] enemies = rc.senseNearbyRobots();
            for (RobotInfo enemy : enemies) {
                if (rc.canAttack(enemy.getLocation())) {
                    rc.attack(enemy.getLocation());
                    return;
                }
            }

            // Repair traps if possible
            Contract[] buildContracts = Utilities.getBuildContracts(RobotPlayer.flagLocations[RobotPlayer.assignedFlag]);
            for (Contract buildContract : buildContracts) {
                if (rc.senseMapInfo(buildContract.loc).getTrapType() != TrapType.EXPLOSIVE) {
                    if (rc.canFill(buildContract.loc)) rc.fill(buildContract.loc);
                    else if (rc.canBuild(TrapType.EXPLOSIVE, buildContract.loc)) {
                        rc.build(TrapType.EXPLOSIVE, buildContract.loc);
                        return;
                    }
                }
            }
        }
    }
}