package warrior_2nd_comp;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SetupPhase {
    public static void runSetup(RobotController rc) throws GameActionException {
        // ALL robots setting up!
        // Collecting crumbs, building traps, etc.
        if (RobotPlayer.turnCount > 175) {
            Pathfinder.moveTowards(rc, new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
            return;
        }

        if (RobotPlayer.role == Role.ATTACKER) {
            if (rc.hasFlag()) {
                // Find the flag dropoff location
                int numFlag = 1;
                MapLocation dropOffLocation = Utilities.decodeLocation(rc, rc.readSharedArray(8));
                if (rc.readSharedArray(11) == 1 && rc.readSharedArray(12) == 1) {
                    dropOffLocation = Utilities.decodeLocation(rc, rc.readSharedArray(10));
                    numFlag = 3;
                } else if (rc.readSharedArray(11) == 1) {
                    dropOffLocation = Utilities.decodeLocation(rc, rc.readSharedArray(9));
                    numFlag = 2;
                }

                if (rc.canDropFlag(dropOffLocation)) {
                    rc.dropFlag(dropOffLocation);
                    Utilities.incrementSharedArray(rc, 3);
                    rc.writeSharedArray(10 + numFlag, 1);
                    rc.writeSharedArray(7 + numFlag, Utilities.encodeLocation(rc, dropOffLocation));
                } else if (RobotPlayer.turnCount > 75 && Utilities.validDropLocation(rc, numFlag)) {
                    rc.dropFlag(rc.getLocation());
                    Utilities.incrementSharedArray(rc, 3);
                    // Signal that flag n has been dropped off
                    rc.writeSharedArray(10 + numFlag, 1);
                    rc.writeSharedArray(7 + numFlag, Utilities.encodeLocation(rc, rc.getLocation()));
                } else Pathfinder.moveTowards(rc, dropOffLocation);
            } else if (rc.readSharedArray(7) < 3) {
                // Find flags to pick up
                FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1, rc.getTeam());
                if (nearbyFlags.length > 0 && !nearbyFlags[0].isPickedUp()) {
                    if (rc.canPickupFlag(nearbyFlags[0].getLocation())) {
                        // Able to pick up the flag!
                        rc.pickupFlag(nearbyFlags[0].getLocation());
                        Utilities.incrementSharedArray(rc, 7);
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
        } else if (RobotPlayer.role == Role.DEFENDER) {
            MapLocation[] optimalLocations = new MapLocation[3];
            MapLocation optimalLocation = Utilities.decodeLocation(rc, rc.readSharedArray(8));
            MapLocation optimalLocation2 = Utilities.decodeLocation(rc, rc.readSharedArray(9));
            MapLocation optimalLocation3 = Utilities.decodeLocation(rc, rc.readSharedArray(10));
            optimalLocations[0] = optimalLocation;
            optimalLocations[1] = optimalLocation2;
            optimalLocations[2] = optimalLocation3;
            if (rc.getLocation().isWithinDistanceSquared(optimalLocation, rc.getMapWidth() / 2)) {
                if (Utilities.validDefenseDirection(rc, optimalLocation.directionTo(rc.getLocation()), optimalLocation)) {
                    Utilities.build(rc, optimalLocation);
                    int numTrapsBuilt1 = rc.readSharedArray(14);
                    rc.writeSharedArray(14, numTrapsBuilt1 + 1);
                } else {
                    Pathfinder.moveRandom(rc);
                }
            } else if (rc.getLocation().isWithinDistanceSquared(optimalLocation2, rc.getMapWidth() / 2)) {
                if (Utilities.validDefenseDirection(rc, optimalLocation2.directionTo(rc.getLocation()), optimalLocation)) {
                    Utilities.build(rc, optimalLocation2);
                    int numTrapsBuilt1 = rc.readSharedArray(15);
                    rc.writeSharedArray(15, numTrapsBuilt1 + 1);
                } else {
                    Pathfinder.moveRandom(rc);
                }
            } else if (rc.getLocation().isWithinDistanceSquared(optimalLocation3, rc.getMapWidth() / 2)) {
                if (Utilities.validDefenseDirection(rc, optimalLocation3.directionTo(rc.getLocation()), optimalLocation)) {
                    Utilities.build(rc, optimalLocation3);
                    int numTrapsBuilt1 = rc.readSharedArray(16);
                    rc.writeSharedArray(16, numTrapsBuilt1 + 1);
                } else {
                    Pathfinder.moveRandom(rc);
                }
            } else {
                // Find whichever location needs more traps
                int[] numTraps = new int[3];
                numTraps[0] = rc.readSharedArray(14);
                numTraps[1] = rc.readSharedArray(15);
                numTraps[2] = rc.readSharedArray(16);
                int min = Utilities.minIndex(numTraps);
                Pathfinder.moveTowards(rc, optimalLocations[min]);
            }
        } else if (RobotPlayer.role == Role.CRUMBS) {
            MapLocation[] nearbyCrumbs = rc.senseNearbyCrumbs(-1);
            if (nearbyCrumbs.length == 0) {
                Pathfinder.moveRandom(rc);
            } else {
                Pathfinder.moveTowards(rc, nearbyCrumbs[0]);
            }
        }
    }
}