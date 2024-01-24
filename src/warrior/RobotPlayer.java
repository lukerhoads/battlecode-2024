package warrior;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public strictfp class RobotPlayer {
    static int turnCount = 0;
    static final Random rng = new Random(6147);
    static Mode mode = Mode.ATTACK;
    static Role role = Role.UNSET;

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // Decide on role
        if (role == Role.UNSET) {
            role = Utilities.assignRole(rc);
        }

        MapLocation[] optimalDropoffLocations = Utilities.optimalDropLocations(rc);
        rc.writeSharedArray(8, Utilities.encodeLocation(rc, optimalDropoffLocations[0]));
        rc.writeSharedArray(9, Utilities.encodeLocation(rc, optimalDropoffLocations[1]));
        rc.writeSharedArray(10, Utilities.encodeLocation(rc, optimalDropoffLocations[2]));

        while (true) {
            turnCount += 1; 

            // Apply global upgrade if it applies
            if (turnCount % 750 == 0) {
                Utilities.globalUpgrade(rc);
            } 

            try {
                if (!rc.isSpawned()) {
                    if (role == Role.ATTACKER || role == Role.HEALER) {
                        Utilities.spawn(rc);
                    } else if (role == Role.DEFENDER) {
                        // If all flags have been placed, spawn
                        if (rc.readSharedArray(3) >= 3) {
                            Utilities.spawn(rc);
                        }
                    }
                } else {
                    if (rc.getRoundNum() >= GameConstants.SETUP_ROUNDS) {
                        MainPhase.play(rc);
                    } else {
                        SetupPhase.runSetup(rc);
                    }
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                Clock.yield();
            }
        }
    }
}
