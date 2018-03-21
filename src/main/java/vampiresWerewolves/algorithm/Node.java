package algorithm;

import board.Board;
import board.Cell;
import board.Position;
import utils.Utils;

import java.io.IOException;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Représente un noeud de l'arbre créé lors de l'exécution de l'algorithme.
 */
public class Node {

    public static Logger logger = Node.createLogger();

    // Carte associée au noeud, ayant subi des transformation par rapport à la carte d'origine
    private Board board;

    // Liste des mouvements alliés faits depuis le noeud racine jusqu'à ce noeud
    private ArrayList<ArrayList<Result>> allyMoves;

    // Nombre d'humains mangés par des alliés par tous les mouvements effectués depuis le noeud racine
    private int humansEaten = 0;

    // Nombre d'humains mangés par des adversaires par tous les mouvements effectués depuis le noeud racine
    private int humansEatenByOpponent = 0;

    // Type de stratégie étudiée pour la création des branches
    private static final String[] MOVEMENT_TYPES = {"transform"};

    // Cache de l'heuristique : permet d'éviter de calculer des branches identiques à d'autres précédemment étudiées
    private static HashMap<Integer, Double> heuristicCache = new HashMap<>();

    Node(Board board) {
        this.board = board;
        this.allyMoves = new ArrayList<>();
    }

    private Node(Board board, ArrayList<ArrayList<Result>> allyMoves, int humansEaten, int humansEatenByOpponent) {
        this.board = board;
        this.allyMoves = allyMoves;
        this.humansEaten = humansEaten;
        this.humansEatenByOpponent = humansEatenByOpponent;
    }

    /**
     * Fonction heuristique pour évaluer une situation donnée d'un noeud
     * @return
     */
    public double heuristic() {

        Board map = this.getBoard();

        // Si la carte a déjà été calculée précédemment, on renvoie directement le score déjà calculé
        if (heuristicCache.keySet().contains(map.hashCode())) {
            double score = heuristicCache.get(map.hashCode());
            logger.info("Already in cache, returning result : " + score + " for move " + this.allyMoves.get(0));
            return score;
        }

        // Score initial : 2 * nombre d'alliés - nombre d'ennemis
        double score = 2 * map.alliesPopulation() - map.opponentsPopulation();

        for (Position human : map.getHumans()) {
            // humanPop = nombre d'humains présents sur la case
            int humanPop = map.getCells()[human.getX()][human.getY()].getPopulation();

            // On calcule la distance des alliés les plus proches à cette case
            double minDistAlly = Double.POSITIVE_INFINITY;
            for (Position ally: map.getAllies()) {
                int allyPop = map.getCells()[ally.getX()][ally.getY()].getPopulation();
                double temp = Utils.minDistance(ally, human);
                if (temp < minDistAlly && allyPop >= humanPop) {
                    minDistAlly = temp;
                }
            }

            // On calcule la distance des ennemis les plus proches à cette case
            double minDistOpponent = Double.POSITIVE_INFINITY;
            for (Position opp: map.getOpponents()) {
                int OpponentPop = map.getCells()[opp.getX()][opp.getY()].getPopulation();
                double temp = Utils.minDistance(opp, human);
                if (temp < minDistAlly && OpponentPop >= humanPop) {
                    minDistOpponent = temp;
                }
            }

            // Si :
            // - les alliés sont plus proches que les ennemis
            // - alliés et ennemis sont à distance égale mais c'est à nous de jouer
            // Alors on ajoute au score le ratio : nombre d'humains / distance minimum des alliés
            // Sinon on retire au score le ratio : nombre d'humains / distance minimum des ennemis
            if ((minDistAlly < minDistOpponent) || (minDistAlly == minDistOpponent && map.getCurrentPlayer().equals(map.getUs()))) {
                score += (double) humanPop / minDistAlly;
            } else {
                score -= (double) humanPop / minDistOpponent;
            }

        }


        // On détermine pour chaque allié l'ennemi le plus proche
        for (Position ally: map.getAllies()) {
            double minDistance = Double.POSITIVE_INFINITY;
            Position opponent = null;
            for (Position opp: map.getOpponents()) {
                double temp = Utils.minDistance(ally, opp);
                if (temp < minDistance) {
                    minDistance = temp;
                    opponent = opp;
                }
            }

            // Si :
            // - la distance est inférieure à 2
            // - la distance est de 1 mais c'est à nous de jouer
            // Alors :
            // - si on est certains de gagner, on ajoute au score le ratio nombre d'ennemis / distance min
            // - si on est certains de perdre, on retire au score le ratio nombre d'alliés / distance min
            // - sinon on calcule le score selon les lois de probabilités d'une bataille
            if (minDistance <= 2 || (minDistance == 1 && map.getCurrentPlayer().equals(map.getUs()))) {
                int allyPop = map.getCells()[ally.getX()][ally.getY()].getPopulation();
                int opponentPop = map.getCells()[opponent.getX()][opponent.getY()].getPopulation();
                if (allyPop > 1.5 * opponentPop) {
                    score += opponentPop / minDistance;
                } else if (1.5 * allyPop < opponentPop) {
                    score -= allyPop / Math.max(1, minDistance);
                } else {
                    double p = allyPop / (2 * opponentPop);
                    score += Math.pow(p, 2) * allyPop / Math.max(1, minDistance)
                            - Math.pow(1 - p, 2) * opponentPop / Math.max(1, minDistance);
                }
            }
        }

        // On ajoute au score 2 fois le nombre d'humains mangés par des alliés depuis la source de cette branche
        score += 2 * this.getHumansEaten();

        // On retire au score 2 fois le nombre d'humains mangés par des ennemis depuis la source de cette branche
        score -= 2 * this.getHumansEatenByOpponent();

        logger.info("Heuristic for move " + this.getAllyMoves().get(0) + " is " + score);

        // On ajoute le score de la carte donnée dans le cache
        heuristicCache.put(map.hashCode(), score);

        // On retourne le score
        return score;

    }

    /**
     * Fonction qui, pour un noeud, créé tous les enfants possibles
     * @return
     */
    public ArrayList<Node> createAlternatives() {
        ArrayList<Node> alternatives = new ArrayList<>();
        HashMap<Position, ArrayList<Result>> goalMoves = new HashMap<>();

        if (board.getCurrentPlayer().equals(board.getUs())) {
            // Si c'est à nous de jouer
            for (Position ally: board.getAllies()) {
                // Pour chaque position alliée, on récupère le nombre d'unités sur la position
                int allyPop = board.getCells()[ally.getX()][ally.getY()].getPopulation();
                ArrayList<Result> allMoves = new ArrayList<>();
                // Pour chacune des stratégies, on cherche les 3 meilleurs coups à jouer
                for (String strategy: MOVEMENT_TYPES) {

                    if (strategy.equals("transform")) {
                        // En stratégie 'transform', on va chercher les 3 meilleurs coups d'attaque d'humains
                        ArrayList<Result> earlyMoves = this.findBestMoveForStrategy(strategy, ally);
                        ArrayList<Result> earlyMovesSplit = new ArrayList<>();
                        for (Result eMove: earlyMoves) {
                            // Pour chaque mouvement, on cherche quelle est le nombre d'alliés min nécessaire pour l'effectuer
                            int minPopToSplit = board.getCells()[eMove.getDestination().getX()][eMove.getDestination().getY()]
                                    .getPopulation();
                            // Si ce nombre min est telle que :
                            // Nombre d'alliés sur la case - nombre min est supérieur au plus petit nombre d'humains sur une case
                            // ou à 1.5 fois le plus petit nombre d'ennemis sur une case
                            // Alors on ajoute aux mouvements possibles un mouvement de split du nombre d'alliés minimum,
                            // calculé précédemment, pour pouvoir effectuer le déplacement
                            if (minPopToSplit > Math.min(board.getMinHumanPop(), 1.5 * board.getMinOppPop())
                                    && (allyPop - minPopToSplit) > Math.min(board.getMinHumanPop(), 1.5 * board.getMinOppPop())) {
                                earlyMovesSplit.add(new Result(eMove.getSource(), minPopToSplit, eMove.getDestination()));
                            }
                        }
                        // on liste tous les mouvements possibles
                        allMoves.addAll(earlyMovesSplit);
                        allMoves.addAll(earlyMoves);

                    } else {
                        // Si ce n'est pas un mouvement de transformation, pour le moment pas besoin de spliter
                        // On ajoute juste les mouvements potentiels à la liste des mouvements
                        allMoves.addAll(this.findBestMoveForStrategy(strategy, ally));
                    }
                }
                // On ajoute au dictionnaire la clé de l'allié considéré et tous ses mouvements possibles en valeur
                goalMoves.put(ally, Result.dropDuplicates(allMoves));
                logger.info("We want to reach : " + goalMoves.get(ally) + " from " + ally);
            }

            // On liste toutes les combinaisons possibles des mouvements d'alliés
            List<ArrayList<Result>> allAlliesCombinationsMoves = computeAllMoves(goalMoves);

            logger.info("Moves computed for ally are " + allAlliesCombinationsMoves);

            for (ArrayList<Result> goalCombinaition: allAlliesCombinationsMoves) {
                // une goalCombination est un tableau de résultats des sources vers les cibles (et non nécessairement
                // vers le véritable move qui va être fait, les cibles n'étant pas nécessairement adjacentes aux sources)
                ArrayList<Result> realMoves = new ArrayList<>();
                int humansEaten = this.humansEaten;

                // remplit realMoves avec les véritables mouvements à faire des sources pour atteindre les cibles, et
                // ajoute également le nombre d'humains mangés par les alliés
                humansEaten = computeRealMovesMade(goalCombinaition, realMoves, humansEaten);

                // TODO: retirer cette ligne si le serveur est bien mis à jour
                if(!isSafeMoves(realMoves)) {
                    continue;
                }

                // Création d'une nouvelle carte avec les mouvements réels réalisés
                Board impliedBoard = board.simulateMoves(realMoves);
                // On crée les nouveaux mouvements alliés comme étant égaux aux anciens + ceux qu'on vient de faire
                ArrayList<ArrayList<Result>> newAllyMoves = new ArrayList<>(this.allyMoves);
                newAllyMoves.add(realMoves);
                // On ajoute le nouveau noeud à la liste des alternatives
                alternatives.add(new Node(impliedBoard, newAllyMoves, humansEaten, this.humansEatenByOpponent));
            }

        } else {
            // Si c'est à l'adversaire de jouer
            for (Position opp: board.getOpponents()) {
                // On peuple le dictionnaire des goalMoves avec comme clé la position des ennemis et comme valeurs leurs
                // mouvement possibles
                ArrayList<Result> allMoves = new ArrayList<>();
                for (String strategy: MOVEMENT_TYPES) {
                    allMoves.addAll(this.findBestMoveForStrategy(strategy, opp));
                }
                goalMoves.put(opp, Result.dropDuplicates(allMoves));
                logger.info("Enemy wants to reach : " + goalMoves.get(opp) + " from " + opp);
            }

            // On liste toutes les combinaisons possibles des mouvements d'ennemis
            List<ArrayList<Result>> allEnemiesCombinationsMoves = computeAllMoves(goalMoves);

           logger.info("Moves computed for opp are " + allEnemiesCombinationsMoves);

            for (ArrayList<Result> goalCombination : allEnemiesCombinationsMoves) {
                // une goalCombination est un tableau de résultats des sources vers les cibles (et non nécessairement
                // vers le véritable move qui va être fait, les cibles n'étant pas nécessairement adjacentes aux sources)
                ArrayList<Result> realMoves = new ArrayList<>();
                int humansEaten = this.humansEatenByOpponent;

                // remplit realMoves avec les véritables mouvements à faire des sources pour atteindre les cibles, et
                // ajoute également le nombre d'humains mangés par l'adversaire
                humansEaten = computeRealMovesMade(goalCombination, realMoves, humansEaten);

                // TODO: retirer cette ligne si le serveur est bien mis à jour
                if(!isSafeMoves(realMoves)) {
                    continue;
                }

                // Création d'une nouvelle carte avec les mouvements réels réalisés
                Board impliedBoard = board.simulateMoves(realMoves);
                // On ajoute le nouveau noeud à la liste des alternatives
                alternatives.add(new Node(impliedBoard, this.allyMoves, this.humansEatenByOpponent, humansEaten));
            }
        }

        // On retourne les différentes alternatives possibles
        return alternatives;
    }

    private int computeRealMovesMade(ArrayList<Result> resultedMoves, ArrayList<Result> realMoves, int humansEaten) {
        // Pour chaque mouvement planifié, on calcule les coordonnées de la cellule adjacente sur laquelle on doit se déplacer
        for (Result res: resultedMoves) {
            // On détermine le meilleur chemin possible (par exemple passant par d'autres points d'intérêts) vers la cible
            Position nextMoveFromGoal = Utils.findNextMove(board, res.getSource(), res.getDestination(), res.getItemsMoved());
            Cell nextMoveCell = board.getCells()[nextMoveFromGoal.getX()][nextMoveFromGoal.getY()];
            // Si la cellule choisie contient un nombre non nul d'humains, on l'ajoute à humansEaten
            if (nextMoveCell.getPopulation() > 0 && nextMoveCell.getKind().equals("humans")) {
                humansEaten += nextMoveCell.getPopulation();
            }

            // On ajoute le véritable move à la liste des mouvements réels
            Result realMove = new Result(res.getSource(), res.getItemsMoved(), nextMoveFromGoal);
            realMoves.add(realMove);
        }
        return humansEaten;
    }

    /**
     * Vérifie si une liste de mouvements de troupes est correct
     *      - Toutes les troupes visent des objectifs différents
     *      - Les troupes ne font pas de mouvements circulaires (ils vont tous se rejoindre l'un l'autre)
     * @param combination
     * @return
     */
    private static boolean isValidCombination(ArrayList<Result> combination) {
        // On supprime les combinaisons d'éléments qui vont au même endroit
        ArrayList<Position> goals = new ArrayList<>();
        for (Result move: combination) {
            // Si la destination n'existe pas, on l'ajoute à la liste des destinations
            if (!goals.contains(move.getDestination())) {
                goals.add(move.getDestination());
            } else {
                // Sinon ça veut dire que deux éléments vont au même endroit, et on retourne faux
                return false;
            }
        }

        // Si tous les éléments vont à des endroits différents, on vérifie que la combinaison n'est pas un cycle
        return !Result.isCircular(combination);
    }

    /**
     * A partir d'un dictionnaire du type {(1,1) : [(2,2), (2,5), (3,6): [(4,4), (3,7)]} retourner
     * [[(1,1) -> (2,2), (3,6) -> (4,4)], [(1,1) -> (2,5), (3,6) -> (4,4)], [(1,1) -> (2,2), (3,6) -> (3,7)], [(1,1) -> (2,5), (3,6) -> (3,7)]
     * @param goalMoves
     * @return
     */
    private List<ArrayList<Result>> computeAllMoves(HashMap<Position, ArrayList<Result>> goalMoves) {
        List<ArrayList<Result>> allMoves = new ArrayList<>();
        int solutions = 1;

        for(ArrayList<Result> results: goalMoves.values()) {
            solutions *= results.size();
        }

        for(int i = 0; i < solutions; i++) {
            ArrayList<Result> combination = new ArrayList<>();
            int j = 1;
            for(ArrayList<Result> res : goalMoves.values()) {
                combination.add(res.get((i/j)%res.size()));
                j *= res.size();
            }
            if (Node.isValidCombination(combination)) {
                allMoves.add(combination);
            }
        }

        return allMoves;
    }

    /**
     * Fonction à n'utiliser que si le serveur n'a pas été mis à jour pour la présentation.
     * Retire d'une liste de coups possibles ceux pour lesquels une case de départ est la même qu'une case d'arrivée.
     */
    private boolean isSafeMoves(ArrayList<Result> allMoves) {
        for (Result simpleMove1: allMoves) {
            for (Result simpleMove2: allMoves) {
                // Si un déplacement se termine là où un autre commence, on supprime ce déplacement
                if (simpleMove1.getSource() == simpleMove2.getDestination()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Retourne les 3 meilleurs destinations pour une position donnée en fonction de la stratégie à réaliser
     * @param movement
     * @param position
     * @return
     */
    private ArrayList<Result> findBestMoveForStrategy(String movement, Position position) {
        int population = board.getCells()[position.getX()][position.getY()].getPopulation();
        ArrayList<Position> keptPositions = new ArrayList<>();
        double minRatio = Double.POSITIVE_INFINITY;
        switch (movement) {
            case "attack":
                // En late game, il va falloir attaquer les ennemis
                Double minOpponentPop = Double.POSITIVE_INFINITY;
                Position minOpponent = null;
                for (Position opp: board.getOpponents()) {
                    int oppPop = board.getCells()[opp.getX()][opp.getY()].getPopulation();
                    if (oppPop < minOpponentPop) {
                        minOpponentPop = (double) oppPop;
                        minOpponent = opp;
                    }
                    // Si on peut battre l'adversaire à coup sûr
                    if (population > 1.5 * oppPop) {
                        double ratio = (double) oppPop / (double) Utils.minDistance(opp, position);
                        // On ajoute l'élément si la liste ne contient pas déjà 3 élément et si le ratio dépasse le ratio minimal
                        minRatio = addOrNotToKeptPositions(position, keptPositions, minRatio, opp, ratio);
                    }
                }
                if (keptPositions.size() == 0 && minOpponentPop > population) {
                    // Si on ne peut pas tuer à coup sûr des ennemis et que notre taille est plus petite que la plus
                    // petite de l'ennemi, on tente le tout pour le tout et on attaque
                    keptPositions.add(minOpponent);
                }
            case "transform":
                // En début de partie, le but sera de grossir le plus possible et donc de manger le plus d'humains
                // possibles, donc de réaliser des transformations
                for (Position human: board.getHumans()) {
                    int humanPop = board.getCells()[human.getX()][human.getY()].getPopulation();
                    // Si on peut battre l'humain à coup sûr
                    if (population >= humanPop) {
                        double ratio = (double) humanPop / (double) Utils.minDistance(human, position);
                        // On ajoute l'élément si la liste ne contient pas déjà 3 élément et si le ratio dépasse le ratio minimal
                        minRatio = addOrNotToKeptPositions(position, keptPositions, minRatio, human, ratio);
                    }
                }
            case "unify":
                // Un groupe peu chercher à rejoindre un autre groupe de la carte. On cherche simplement à rejoindre le
                // groupe le plus proche.
                for (Position allies: board.getAllies()) {
                    // Si la case alliée visée n'est pas la case actuelle
                    if (!position.equals(allies)) {
                        double ratio = 1.0 / (double) Utils.minDistance(allies, position);
                        // On ajoute l'élément si la liste ne contient pas déjà 3 élément et si le ratio dépasse le ratio minimal
                        minRatio = addOrNotToKeptPositions(position, keptPositions, minRatio, allies, ratio);
                    }
                }
        }

        // Pour chacune position gardée, on crée un nouveau résultat, de la position étudiée vers la position gardée,
        // avec la population de la cellule étudiée
        ArrayList<Result> moves = new ArrayList<>();
        for (Position kept: keptPositions) {
            moves.add(new Result(position, population, kept));
        }
        return moves;
    }

    /**
     * Choisit si oui ou non on doit ajouter l'élément à liste. On l'ajoute si la taille est <= 3 et sinon on l'ajoute
     * que si le ratio est meilleur que le ratio minimal de la liste (on récrée ensuite le ratio minimal de la liste
     * s'il y a insertion)
     * @param position
     * @param keptPositions
     * @param minRatio
     * @param other
     * @param ratio
     * @return
     */
    private double addOrNotToKeptPositions(Position position, ArrayList<Position> keptPositions, double minRatio,
                                           Position other, double ratio) {

        if (keptPositions.size() < 3) {

            keptPositions.add(other);
            minRatio = Math.min(minRatio, ratio);

        } else if (ratio > minRatio) {

            ArrayList<Position> removedPositions = new ArrayList<>();
            double newMinRatio = Double.POSITIVE_INFINITY;
            for (Position pos: keptPositions) {
                int posPop = board.getCells()[pos.getX()][pos.getY()].getPopulation();
                double posRatio = (double) posPop / (double) Utils.minDistance(pos, position);
                if (posRatio == minRatio) {
                    removedPositions.add(pos);
                } else {
                    newMinRatio = Math.min(newMinRatio, posRatio);
                }
            }
            keptPositions.removeAll(removedPositions);
            keptPositions.add(other);
            minRatio = Math.min(newMinRatio, ratio);

        }

        return minRatio;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public ArrayList<ArrayList<Result>> getAllyMoves() {
        return allyMoves;
    }

    private int getHumansEaten() {
        return humansEaten;
    }

    private int getHumansEatenByOpponent() {
        return humansEatenByOpponent;
    }

    private static Logger createLogger() {
        Logger log = Logger.getLogger("my logger");
        Handler fh;
        try {
            fh = new FileHandler("logs/myLog.log");
            fh.setFormatter(new SimpleFormatter());
            log.addHandler(fh);
            log.setUseParentHandlers(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return log;
    }
}
