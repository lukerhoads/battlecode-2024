package blitzer;

import battlecode.common.*;

public class Contract {
    public MapLocation loc;
    public TrapType trap;

    public Contract(MapLocation location, TrapType trapType) {
        loc = location;
        trap = trapType;
    }
}
