package warrior;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class OldSetupPhase {
    public static void runSetup(RobotController rc) throws GameActionException {
        // ALL robots setting up!
        // Collecting crumbs, building traps, etc.
        if (RobotPlayer.role == Role.ATTACKER) {
            System.out.println("attacker");
            // Attacker! (specialized in attacking)
            // Move flags to optimal spot
            if (rc.hasFlag()) {
                MapLocation dropOffLocation = Utilities.optimalCorner(rc);
                int delta = (rc.getMapWidth() + rc.getMapHeight()) / 12;

                // Flags might be too close - check on test
                if (rc.getLocation().isWithinDistanceSquared(dropOffLocation, delta) && rc.canDropFlag(rc.getLocation()) && rc.senseLegalStartingFlagPlacement(rc.getLocation()) || rc.getRoundNum() > 100) {
                    // Robot in region to drop off flag - drop it off
                    rc.dropFlag(rc.getLocation());
                    int placedFlags = rc.readSharedArray(3);
                    rc.writeSharedArray(3, placedFlags + 1);
                } else {
                    // Robot not in valid placement, move towards it
                    Pathfinder.moveTowards(rc, dropOffLocation);
                }
            } else if (rc.readSharedArray(7) < 3) {
                FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1, rc.getTeam());
                if (nearbyFlags.length > 0 && !nearbyFlags[0].isPickedUp()) {
                    if (rc.canPickupFlag(nearbyFlags[0].getLocation())) {
                        // Able to pick up the flag!
                        rc.pickupFlag(nearbyFlags[0].getLocation());
                        int numFlagsPickedUp = rc.readSharedArray(7);
                        rc.writeSharedArray(7, numFlagsPickedUp + 1);
                    } else {
                        // Found a flag! Move towards it.
                        Pathfinder.moveTowards(rc, nearbyFlags[0].getLocation());
                    }
                    
                } else {
                    // Did not find a flag. Move randomly until we do.
                    Pathfinder.moveRandom(rc);
                }
            } else {
                // Move closest to enemy territory (center), ready to attack on barrier fall
                Pathfinder.moveTowards(rc, new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
            }
        } else if (RobotPlayer.role == Role.DEFENDER && rc.readSharedArray(3) >= 3) {
            System.out.println("defender");
            // Defender! Also building traps (specialized in building)
            // Building traps
            // If current location is within a radius of optimalFlagLocation then place
            if (rc.getLocation().isWithinDistanceSquared(Utilities.optimalCorner(rc), 30)) {
                // Build a trap, decide which one
                TrapType building = TrapType.NONE;
                int distance = rc.getLocation().distanceSquaredTo(Utilities.optimalCorner(rc));
                if (distance < 10) building = TrapType.EXPLOSIVE;
                else if (distance < 20) building = TrapType.STUN;
                else building = TrapType.WATER;
                int trapsBuiltAroundFlags = rc.readSharedArray(5);
                if (rc.canBuild(building, rc.getLocation())) {
                    rc.build(building, rc.getLocation());
                    rc.writeSharedArray(5, trapsBuiltAroundFlags + 1);
                } else {
                    Pathfinder.moveRandom(rc);
                }
            } else {
                Pathfinder.moveRandom(rc);
            }
        } else if (RobotPlayer.role == Role.HEALER) {
            // Healer! (specialized in healing)
            // Collecting crumbs
            MapLocation[] nearbyCrumbs = rc.senseNearbyCrumbs(-1);
            if (nearbyCrumbs.length == 0) {
                Pathfinder.moveRandom(rc);
            } else {
                Pathfinder.moveTowards(rc, nearbyCrumbs[0]);
            }
        }
    }
}