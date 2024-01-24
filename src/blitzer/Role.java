package blitzer;

public enum Role {
    ATTACKER,
    DEFENDER,
    UNSET;

    public String toString() {
        switch (this.ordinal()) {
        case 0:
            return "attacker";
        case 1:
            return "defender";
        case 3: 
        }

        return "unset";
    }
};