package blitzer;

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
    static Role role = Role.UNSET;
    static int assignedFlag = -1;
    static MapLocation[] flagLocations = new MapLocation[3];

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
        flagLocations = Utilities.getFlagLocations(rc);
        if (role == Role.UNSET) {
            int newRole = Utilities.assignRole(rc);
            if (newRole != -1) {
                role = Role.DEFENDER;
                assignedFlag = newRole;
            } else {
                role = Role.ATTACKER;
            }
        }

        while (true) {
            turnCount += 1; 

            if (turnCount % 750 == 0) {
                Utilities.globalUpgrade(rc);
            } 

            try {
                if (!rc.isSpawned()) {
                    if (role == Role.ATTACKER) {
                        MapLocation randomLocation = rc.getAllySpawnLocations()[rng.nextInt(27)];
                        if (rc.canSpawn(randomLocation)) rc.spawn(randomLocation);
                    } else if (role == Role.DEFENDER) {
                        if (rc.canSpawn(flagLocations[assignedFlag].translate(-1, 0))) rc.spawn(flagLocations[assignedFlag].translate(-1, 0));
                        else if (rc.canSpawn(flagLocations[assignedFlag].translate(1, 0))) rc.spawn(flagLocations[assignedFlag].translate(1, 0));
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
