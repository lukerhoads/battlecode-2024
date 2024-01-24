package blitzer;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Communication {
    public static void broadcastLocation(RobotController rc, int index, int x, int y) throws GameActionException {
        int encodedLocation = y * rc.getMapWidth() + x + 1;
        rc.writeSharedArray(index, encodedLocation); 
    }

    public static void broadcastEnemyFlagLocation(RobotController rc, int flagIndex, int x, int y) throws GameActionException {
        
    }

    public static int[] getLocation(RobotController rc, int index) throws GameActionException {
        int encodedLocation = rc.readSharedArray(index) - 1;
        int[] res = new int[2];
        res[0] = encodedLocation % rc.getMapWidth();
        res[1] = encodedLocation % rc.getMapHeight();
        return res;
    }
}
