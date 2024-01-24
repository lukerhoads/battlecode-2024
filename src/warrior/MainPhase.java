package warrior;

import battlecode.common.*;
import battlecode.world.Trap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainPhase {
    public static int lockedTarget = -1;
    public static int assignedFlag = -1;

    public static void play(RobotController rc) throws GameActionException {
        // Robots doing jobs based on their assignment
        if (RobotPlayer.role == Role.ATTACKER) {
            // Attacker! (specialized in attacking)
            // Only resense every 100 rounds.
            if (RobotPlayer.turnCount % 100 == 0 || RobotPlayer.turnCount == 201) {
                MapLocation[] enemyFlagLocations = rc.senseBroadcastFlagLocations();
                for (int i = 0; i < enemyFlagLocations.length; i++) {
                    rc.writeSharedArray(23 + i, Utilities.encodeLocation(rc, enemyFlagLocations[i]));
                }
            }

            if (RobotPlayer.turnCount > 200) {
                if (assignedFlag == -1) {
                    int[] numAssigned = new int[3];
                    numAssigned[0] = rc.readSharedArray(20);
                    numAssigned[1] = rc.readSharedArray(21);
                    numAssigned[2] = rc.readSharedArray(22);
                    int minIndex = Utilities.minIndex(numAssigned);
                    assignedFlag = minIndex + 1;
                    Utilities.incrementSharedArray(rc, 19 + assignedFlag);
                }

                if (rc.hasFlag()) {
                    Pathfinder.moveTowards(rc, Utilities.getClosestSpawnLocation(rc));
                    return;
                }
    
                // Have assigned flag and its approximate location.
                MapLocation assignedFlagLocation = Utilities.decodeLocation(rc, rc.readSharedArray(22 + assignedFlag));
                // MapInfo[] nearbyEnemyFlagMapInfo = rc.senseNearbyMapInfos(assignedFlagLocation);
                FlagInfo[] nearbyFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
                if (nearbyFlags.length > 0) {
                    if (rc.canPickupFlag(nearbyFlags[0].getLocation())) {
                        rc.pickupFlag(nearbyFlags[0].getLocation());
                    } else {
                        Pathfinder.moveTowards(rc, nearbyFlags[0].getLocation());
                    }
                } else {
                    Pathfinder.moveTowards(rc, assignedFlagLocation);
                }
            }
        } else if (RobotPlayer.role == Role.DEFENDER) {
            // Defender! Also repairing traps (specialized in building)
            RobotInfo[] nearbyEnemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            if (nearbyEnemyRobots.length > 0) {
                if (lockedTarget == -1) {
                    int minDist = rc.getMapWidth();
                    int minId = -1;
                    for (RobotInfo nearbyEnemy : nearbyEnemyRobots) {
                        if (rc.getLocation().distanceSquaredTo(nearbyEnemy.getLocation()) < minDist) {
                            minDist = rc.getLocation().distanceSquaredTo(nearbyEnemy.getLocation());
                            minId = nearbyEnemy.getID();
                        }
                    }  
        
                    if (minId != -1) lockedTarget = minId;
                } else {
                    boolean lockedIn = false;
                    for (RobotInfo nearbyEnemy : nearbyEnemyRobots) {
                        if (nearbyEnemy.getID() == lockedTarget) {
                            lockedIn = true;
                            if (rc.canAttack(nearbyEnemy.location)) {
                                rc.attack(nearbyEnemy.location);
                            } else {
                                Pathfinder.moveTowards(rc, nearbyEnemy.location);
                            }
                        }
                    }

                    if (!lockedIn) lockedTarget = -1;
                }
            } else {
                // Think about building/repairing traps
                // Going to build randomly now
                int[] numTrapsBuilt = new int[3];
                numTrapsBuilt[0] = rc.readSharedArray(17);
                numTrapsBuilt[1] = rc.readSharedArray(18);
                numTrapsBuilt[2] = rc.readSharedArray(19);
                int minIdx = Utilities.minIndex(numTrapsBuilt);
                TrapType[] traps = new TrapType[3];
                traps[0] = TrapType.EXPLOSIVE;
                traps[1] = TrapType.STUN;
                traps[2] = TrapType.WATER;
                if (rc.canBuild(traps[minIdx], rc.getLocation())) {
                    rc.build(traps[minIdx], rc.getLocation());
                    Utilities.incrementSharedArray(rc, 17 + minIdx);
                } else {
                    Pathfinder.moveRandom(rc);
                }
            }
        } else if (RobotPlayer.role == Role.HEALER) {
            // Healer! (specialized in healing)
            RobotInfo[] nearbyRobots = rc.senseNearbyRobots(2, rc.getTeam());
            boolean healed = false;
            for (RobotInfo nearbyRobot : nearbyRobots) {
                if (nearbyRobot.health < GameConstants.DEFAULT_HEALTH && rc.canHeal(nearbyRobot.location)) {
                    rc.heal(nearbyRobot.location);
                    healed = true;
                    break;
                }
            }

            if (!healed) {
                Pathfinder.moveRandom(rc);
            }
        }
    }
}