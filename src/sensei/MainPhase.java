package sensei;

import battlecode.common.*;
import blitzer.Utilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainPhase {
    public static String mode;
    public static RobotInfo closestOpponent;
    public static int retreatThreshold;
    public static int assignedRunawayFlag = -1;
    public static RobotInfo targetOpponent;

    public static boolean isInBetween(RobotController rc, MapLocation loc2, MapLocation loc3) {
      MapLocation loc1 = rc.getLocation();
      return loc2.distanceSquaredTo(loc3) < loc1.distanceSquaredTo(loc3) && loc1.distanceSquaredTo(loc2) < loc1.distanceSquaredTo(loc3);
    }

    public static void attack(RobotController rc, RobotInfo enemy) {
      
    }

    public static MapLocation closestBroadcastFlagLocation(RobotController rc) {
      MapLocation[] broadcastFlags = rc.senseBroadcastFlagLocations();
      if (broadcastFlags.length > 0) {
        MapLocation closestBroadcastFlag = broadcastFlags[0];
        int minFlagDist = RobotPlayer.reflectedSpawnLocation.distanceSquaredTo(closestBroadcastFlag);
        for (int i=broadcastFlags.length; --i>0;) {
          int dist = RobotPlayer.reflectedSpawnLocation.distanceSquaredTo(broadcastFlags[i]);
          if (dist < minFlagDist) {
            minFlagDist = dist;
            closestBroadcastFlag = broadcastFlags[i];
          }
        }

        return closestBroadcastFlag;
      }
      
      return RobotPlayer.reflectedSpawnLocation;
    }

    public static int getRobotAttractiveness(RobotController rc, RobotInfo ri) {
      return ri.getHealth() + rc.getLocation().distanceSquaredTo(ri.getLocation());
    }

    public static void play(RobotController rc) throws GameActionException {
      FlagInfo[] nearbyAllyFlags = rc.senseNearbyFlags(5, rc.getTeam().opponent());
      FlagInfo[] nearbyOpponentFlags = rc.senseNearbyFlags(-1, rc.getTeam().opponent());
      FlagInfo closestOpponentFlag = null;
      if (nearbyOpponentFlags.length > 0) {
        closestOpponentFlag = nearbyOpponentFlags[0];
        int minFlagDist = rc.getLocation().distanceSquaredTo(closestOpponentFlag.getLocation());
        for (int i=nearbyOpponentFlags.length; --i>0;) {
          int dist = rc.getLocation().distanceSquaredTo(nearbyOpponentFlags[i].getLocation());
          if (dist < minFlagDist) {
            minFlagDist = dist;
            closestOpponentFlag = nearbyOpponentFlags[i];
          }
        }
      }
      RobotInfo[] nearbyAllyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
      for (int i=nearbyAllyRobots.length; --i>=0;) {
        if (nearbyAllyRobots[i].getHealth() < 1000) {
          if (rc.canHeal(nearbyAllyRobots[i].getLocation())) rc.heal(nearbyAllyRobots[i].getLocation());
        }
      }

      RobotInfo[] nearbyOpponentRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
      RobotInfo closestOpponentRobot = null;
      if (nearbyOpponentRobots.length > 0) {
        closestOpponentRobot = nearbyOpponentRobots[0];
        int minOpponentDist = rc.getLocation().distanceSquaredTo(closestOpponentRobot.getLocation());
        for (int i=nearbyOpponentRobots.length; --i>0;) {
          int dist = rc.getLocation().distanceSquaredTo(nearbyOpponentRobots[i].getLocation());
          if (dist < minOpponentDist) {
            minOpponentDist = dist;
            closestOpponentRobot = nearbyOpponentRobots[i];
          }
        }
      }

      RobotInfo targetOpponentRobot = null;
      if (nearbyOpponentRobots.length > 0) {
        targetOpponentRobot = nearbyOpponentRobots[0];
        int minOpponentScore = getRobotAttractiveness(rc, targetOpponentRobot);
        for (int i=nearbyOpponentRobots.length; --i>0;) {
          int dist = getRobotAttractiveness(rc, nearbyOpponentRobots[i]);
          if (dist < minOpponentScore) {
            minOpponentScore = dist;
            targetOpponentRobot = nearbyOpponentRobots[i];
          }
        }
      }
      
      MapLocation currentLocation = rc.getLocation();
      MapLocation[] broadcastLocations = rc.senseBroadcastFlagLocations();
      int health = rc.getHealth();
      int avgMapSize = (rc.getMapWidth() + rc.getMapHeight()) / 2;
      int retreatThreshold = 800;
      if (avgMapSize < 45) retreatThreshold = 600;
      if (avgMapSize < 30) retreatThreshold = 400;
      MapLocation destination;
      int partnerAttackThreshold = nearbyOpponentRobots.length;

      // Pickup flag if can sense
      if (nearbyOpponentFlags != null && nearbyOpponentFlags.length > 0 && closestOpponentFlag != null && !closestOpponentFlag.isPickedUp()) {
        if (rc.canPickupFlag(closestOpponentFlag.getLocation())) {
          rc.pickupFlag(closestOpponentFlag.getLocation());
          return;
        }

        Pathfinder.moveTowards(rc, closestOpponentFlag.getLocation());
        destination = closestOpponentFlag.getLocation();
        return;
      }

      // MapLocation runawayFlagLocation = Communication.getRunawayFlagLocation(rc);
      // if (runawayFlagLocation != null) {
      //   // Swarm without blocking
      //   Pathfinder.moveTowards(rc, runawayFlagLocation);
      // }

      if (rc.hasFlag()) {
        MapLocation closestSpawn = Utilities.getClosestSpawnLocation(rc);
        Pathfinder.moveTowards(rc, closestSpawn);
        destination = closestSpawn;
        // Broadcast that flag has been picked up so swarm
        Communication.broadcastFlagRunawayLocation(rc, rc.getLocation());
        return;
      }

      if (rc.readSharedArray(38) >= 3) {
        Pathfinder.moveTowards(rc, RobotPlayer.spawnLocation);
      }

      // Optimization down here
      if (broadcastLocations.length > 0) {
        Pathfinder.moveTowards(rc, broadcastLocations[0]);
        destination = broadcastLocations[0];
      } else {
        Pathfinder.moveTowards(rc, RobotPlayer.reflectedSpawnLocation);
        destination = RobotPlayer.reflectedSpawnLocation;
      }

      // if no enemy is in way
      if (closestOpponentRobot != null && isInBetween(rc, closestOpponentRobot.getLocation(), destination) && rc.getHealth() > retreatThreshold && nearbyAllyRobots.length > partnerAttackThreshold) {
        // Engage 
        // Heuristic minimization here (no natural pathfinding)

        boolean attacked = false;
        // Attacking/building/healing
        if (rc.canAttack(closestOpponentRobot.getLocation())) {
          rc.attack(closestOpponentRobot.getLocation());
          attacked = true;
          // If already in attack radius don't retreat
          // If can get out of attack radius retreat
        }

        // Prioritize getting near opponent but not anyone else
        double minScore = (attacked ? -1 : 1) * closestOpponentRobot.getLocation().distanceSquaredTo(currentLocation);
        for (int i=nearbyOpponentRobots.length; --i>=0;) {
          if (nearbyOpponentRobots[i].getID() != closestOpponentRobot.getID()) {
            minScore -= nearbyOpponentRobots[i].getLocation().distanceSquaredTo(currentLocation);
          }
        }
        int minIndex = -1;
        for (int i=RobotPlayer.directions.length-1; --i>=0;) {
          MapLocation newLoc = currentLocation.add(RobotPlayer.directions[i]);
          double newLocScore = (attacked ? -1 : 1) * closestOpponentRobot.getLocation().distanceSquaredTo(newLoc);
          for (int j=nearbyOpponentRobots.length; --j>=0;) {
            if (nearbyOpponentRobots[j].getID() != closestOpponentRobot.getID()) {
              minScore -= nearbyOpponentRobots[j].getLocation().distanceSquaredTo(currentLocation);
            }
          }
          if (newLocScore < minScore) {
            minScore = newLocScore;
            minIndex = i;
          }
        } 

        if (minIndex != -1 && rc.canMove(RobotPlayer.directions[minIndex])) rc.move(RobotPlayer.directions[minIndex]);
      } else {
        Pathfinder.moveTowards(rc, destination);
      }

      
      // if enemy is in way but not enough backup
      // Skirt around enemy
      // else engage if sufficient conditions met
      // else retreat if sufficient conditions met

      // if (closestOpponentFlag != null) {
      //   if (rc.canPickupFlag(closestOpponentFlag.getLocation())) rc.pickupFlag(closestOpponentFlag.getLocation());
      //   if (closestOpponentRobot != null) {
      //     if (minFlagDist < minOpponentDist) {
      //       Pathfinder.moveTowards(rc, closestOpponentFlag.getLocation());
      //     } else {
      //       // Attack
      //       attack(rc, closestOpponentRobot);
      //     }
      //   }
      // }

      // if (nearbyOpponentFlags.length == 0 && nearbyOpponentRobots.length == 0) {
      //   MapLocation closestPotentialFlag = closestBroadcastFlagLocation(rc);
      //   Pathfinder.moveTowards(rc, closestPotentialFlag);
      // }
      // System.out.println(RobotPlayer.reflectedSpawnLocation.toString());
      
       // Dig
       // Build traps
       // follow A* (iteratively)
       // at the start of each turn:
     //   RobotInfo[] enemiesSeen = rc.senseNearbyRobots(20, rc.getTeam().opponent());
     //   for (RobotInfo enemySeen : enemiesSeen) {
     //        int distanceToEnemy = rc.getLocation().distanceSquaredTo(enemySeen.getLocation());
     //   if (distanceToEnemy <= 4) {
     //    //attack 
     //   }
     //   else if (distanceToEnemy <= 6) // maybe workshop this distance
     //   // post up
     //   }

     // Move towards enemy
     // - should only happen if we have enough backup
     // Move towards flag
     // - if no enemies in way, move closer to flag
     // Retreat to healer
     // - if health below a certain threshold
     // Attack enemy
     // - if enemy blocks way to goal
     // Move around enemy
     // Build trap
       
       // when an enemy is sensed, stay just outside of attack range and attack if enemy moves forward
       // broadcast ('posted up condition' and number of enemies seen)
       // when ally arrives, share info, ally either 'post up' or swarm attack
       // wait until enough allies also posting up where we outnumber enemy
       // both move in at once to trade and gain ground
       // runner follows behind


       // all robots have same role
       // post up on seeing enemy
       // outnumber enemy
       // heuristic function 
       // when robot is close enough to flag / when robot can pick up flag, pick up
       // if flag picked up, beeline to closest ally flag location

       // Check if another robot has picked up a flag.
     //   if (rc.readSharedArray(10 + RobotPlayer.assignedEnemyFlag) != 0) {
     //        MapLocation dest = Communication.decodeLocation(rc, rc.readSharedArray(10 + RobotPlayer.assignedEnemyFlag));
     //        Pathfinder.moveTowards(rc, dest);
     //   } else if (rc.hasFlag()) {
     //        MapLocation destination = blitzer.Utilities.getClosestSpawnLocation(rc);
     //        if (rc.canDropFlag(destination)) {
     //            rc.dropFlag(destination);
     //            rc.writeSharedArray(10 + RobotPlayer.assignedEnemyFlag, 0);
     //        }
     //        Pathfinder.moveTowards(rc, destination);
     //        // Move towards spawn location, broadcasting location
     //        rc.writeSharedArray(10 + RobotPlayer.assignedEnemyFlag, Communication.encodeLocation(rc, rc.getLocation()));
     //   }
       
       // if ally robot has picked up flag, swarm to protect
    }
}