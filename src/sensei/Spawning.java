package sensei;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Spawning {
    public static void spamSpawn(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocations = rc.getAllySpawnLocations();
        for (int i=spawnLocations.length; --i>=0;) {
            if (rc.canSpawn(spawnLocations[i])) {
                rc.spawn(spawnLocations[i]);
                RobotPlayer.spawnLocation = spawnLocations[i];
                RobotPlayer.reflectedSpawnLocation = blitzer.Utilities.reflectLocation(rc, spawnLocations[i]);
                if (i % 25 == 0) RobotPlayer.levelGrinder = true;
                else break;
            }
        }
    }
}
