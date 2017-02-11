package icurves.decomposition;

/**
 * Type of the decomposition strategy.
 */
public enum DecompositionStrategyType {
    ALPHABETICAL("Decompose in alphabetic order"),
    REVERSE_ALPHABETICAL("Decompose in reverse alphabetic order"),
    INNERMOST("Decompose using fewest-zone contours first"),
    PIERCED_FIRST("Decompose using piercing curves first");

    private String uiName;

    /**
     * @return UI-friendly name
     */
    public String getUiName() {
        return uiName;
    }

    DecompositionStrategyType(String uiName) {
        this.uiName = uiName;
    }
}
