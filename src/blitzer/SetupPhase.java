package blitzer;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SetupPhase {
    public static void runSetup(RobotController rc) throws GameActionException {
        if (RobotPlayer.role == Role.ATTACKER) {
            MapLocation[] nearbyCrumbs = rc.senseNearbyCrumbs(-1);
            if (nearbyCrumbs.length == 0) {
                // MapLocation getTargetMapLocation = Utilities.getCrowdLocation(rc);
                // Crowd towards enemy lines
                // TODO: algorithm for this
                Pathfinder.moveTowards(rc, new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
            } else {
                Pathfinder.moveTowards(rc, nearbyCrumbs[0]);
            }
        } else if (RobotPlayer.role == Role.DEFENDER) {
            Contract[] buildContracts = Utilities.getBuildContracts(RobotPlayer.flagLocations[RobotPlayer.assignedFlag]);
            int currentTrapIndex = rc.readSharedArray(6 + RobotPlayer.assignedFlag);
            if (currentTrapIndex >= buildContracts.length) return;
            if (rc.canFill(buildContracts[currentTrapIndex].loc)) rc.fill(buildContracts[currentTrapIndex].loc); 
            else if (rc.canBuild(buildContracts[currentTrapIndex].trap, buildContracts[currentTrapIndex].loc)) {
                rc.build(buildContracts[currentTrapIndex].trap, buildContracts[currentTrapIndex].loc);
                Utilities.incrementSharedArray(rc, 6 + RobotPlayer.assignedFlag);
                return;
            }

            Pathfinder.straightShot(rc, buildContracts[currentTrapIndex].loc);
        }
    }
}