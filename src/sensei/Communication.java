package sensei;

import battlecode.common.*;

public class Communication {
    // static void assignTargetFlag(RobotController rc) throws GameActionException {
    //     int numOneAssigned = rc.readSharedArray(0);
    //     int numTwoAssigned = rc.readSharedArray(1);
    //     int numThreeAssigned = rc.readSharedArray(2);
    //     if (numOneAssigned < numTwoAssigned && numOneAssigned < numThreeAssigned) {
    //         rc.writeSharedArray(0, numOneAssigned + 1);
    //         RobotPlayer.assignedEnemyFlag = 0;
    //         return;
    //     } else if (numTwoAssigned < numOneAssigned && numTwoAssigned < numThreeAssigned) {
    //         rc.writeSharedArray(1, numTwoAssigned);
    //         RobotPlayer.assignedEnemyFlag = 1;
    //         return;
    //     }
        
    //     rc.writeSharedArray(2, numThreeAssigned + 1);
    //     RobotPlayer.assignedEnemyFlag = 2;
    // }

    public static int encodeLocation(RobotController rc, MapLocation loc) {
        return loc.y * rc.getMapWidth() + loc.x + 1;
    }

    public static MapLocation decodeLocation(RobotController rc, int num) {
        return new MapLocation((num - 1) % rc.getMapWidth(), (num - 1) / rc.getMapWidth());
    }

    public static void populateEnemyFlagLocations(RobotController rc, MapLocation[] locations) throws GameActionException {
        for (int i=locations.length; --i>=0;) {
            int currentLocation = rc.readSharedArray(3 + i);
            if (currentLocation == 0) {
                rc.writeSharedArray(3 + i, encodeLocation(rc, locations[i]));
            }
        }
    }

    public static MapLocation[] getEnemyFlagLocations(RobotController rc) throws GameActionException {
        MapLocation[] res = new MapLocation[3];
        for (int i=res.length; --i>=0;) {
            int mapLoc = rc.readSharedArray(3 + i);
            if (mapLoc > 0) {
                res[i] = decodeLocation(rc, mapLoc);
            } else {
                return null;
            }
        } 

        return res;
    }

    public static void broadcastFlagRunawayLocation(RobotController rc, MapLocation loc) throws GameActionException {
        if (MainPhase.assignedRunawayFlag != -1) {
            int flagLoc = rc.readSharedArray(10 + MainPhase.assignedRunawayFlag);
            if (flagLoc != 0) rc.writeSharedArray(10 + MainPhase.assignedRunawayFlag, encodeLocation(rc, loc));
        }

        for (int i=0; i<3; i++) {
            int flagLoc = rc.readSharedArray(10 + i);
            if (flagLoc != 0) rc.writeSharedArray(10 + i, encodeLocation(rc, loc));
            if (MainPhase.assignedRunawayFlag == -1) MainPhase.assignedRunawayFlag = i;
        }
    }

    public static MapLocation getRunawayFlagLocation(RobotController rc) throws GameActionException {
        if (MainPhase.assignedRunawayFlag != -1) {
            int flagLoc = rc.readSharedArray(10 + MainPhase.assignedRunawayFlag);
            if (flagLoc != 0) return decodeLocation(rc, flagLoc);
        }

        for (int i=0; i<3; i++) {
            int flagLoc = rc.readSharedArray(10 + i);
            if (flagLoc != 0) return decodeLocation(rc, flagLoc);
            if (MainPhase.assignedRunawayFlag == -1) MainPhase.assignedRunawayFlag = i;
        }

        return null;
    }
}
