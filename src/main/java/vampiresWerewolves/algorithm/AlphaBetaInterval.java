package algorithm;

/**
 * Interval de l'algo alpha beta. C'est juste un tuple mais comme ils sont immutables en Java, bah faut faire le sien ...
 */
class AlphaBetaInterval {


    private Double alpha;
    private Double beta;

    public AlphaBetaInterval(Double alpha, Double beta) {
        this.alpha = alpha;
        this.beta = beta;
    }

    public Double getAlpha() {
        return alpha;
    }

    public void setAlpha(Double alpha) {
        this.alpha = alpha;
    }

    public Double getBeta() {
        return beta;
    }

    public void setBeta(Double beta) {
        this.beta = beta;
    }
}
