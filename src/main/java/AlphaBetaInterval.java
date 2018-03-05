/**
 * Interval de l'algo alpha beta. C'est juste un tuple mais comme ils sont immutables en Java, bah faut faire le sien ...
 */
class AlphaBetaInterval {


    private Double inf;
    private Double sup;

    AlphaBetaInterval(Double inf, Double sup) {
        this.inf = inf;
        this.sup = sup;
    }

    public Double getInf() {
        return inf;
    }

    public void setInf(Double inf) {
        this.inf = inf;
    }

    public Double getSup() {
        return sup;
    }

    public void setSup(Double sup) {
        this.sup = sup;
    }
}
