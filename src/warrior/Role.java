package warrior;

public enum Role {
    ATTACKER,
    DEFENDER,
    HEALER,
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