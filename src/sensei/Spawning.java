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
                int flagIndex = blitzer.Utilities.locationListContains(RobotPlayer.startingAllyFlagLocations, spawnLocations[i]);
                if (flagIndex != -1) RobotPlayer.assignedAllyFlag = flagIndex; 
                rc.spawn(spawnLocations[i]);
                if (spawnLocations.length - i < 4) RobotPlayer.levelGrinder = true;
                else break;
            }
        }
    }
}
