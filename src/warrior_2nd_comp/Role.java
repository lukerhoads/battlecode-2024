package warrior_2nd_comp;

public enum Role {
    ATTACKER,
    DEFENDER,
    CRUMBS,
    UNSET;

    public String toString() {
        switch (this.ordinal()) {
        case 0:
            return "attacker";
        case 1:
            return "defender";
        case 2:
            return "healer";
        case 3: 
        }

        return "unset";
    }
};