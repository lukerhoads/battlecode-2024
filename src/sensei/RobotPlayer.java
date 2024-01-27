package sensei;

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
    static Pathfinder pathfinder;
    static boolean levelGrinder;
    static MapLocation[] startingAllyFlagLocations;
    static MapLocation spawnLocation;
    static MapLocation reflectedSpawnLocation;

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
        pathfinder = new Pathfinder(rc);
        if (startingAllyFlagLocations == null) startingAllyFlagLocations = blitzer.Utilities.getFlagLocations(rc);
        while (true) {
            turnCount += 1; 

            if (turnCount % 750 == 0) {
                warrior.Utilities.globalUpgrade(rc);
            } 

            try {
                if (!rc.isSpawned()) {
                    Spawning.spamSpawn(rc);
                } else {
                    if (rc.getRoundNum() > 200) {
                        MainPhase.play(rc);
                    } else {
                        SetupPhase.play(rc);
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
