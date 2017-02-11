package icurves.recomposition;

public enum RecompositionStrategyType {
    NESTED("Recompose using zero-piercing (nesting)"),
    SINGLY_PIERCED("Recompose using single piercings"),
    DOUBLY_PIERCED("Recompose using double piercings"),
    DOUBLY_PIERCED_EXTRA_ZONES("Recompose using dp with extra zones");

    private String uiName;

    public String getUiName() {
        return uiName;
    }

    RecompositionStrategyType(String uiName) {
        this.uiName = uiName;
    }
}
