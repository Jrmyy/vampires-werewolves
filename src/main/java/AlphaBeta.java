import java.util.ArrayList;
import java.util.HashMap;

class AlphaBeta {

    private static final String[] MOVEMENT_TYPES = {"attack", "transform", "escape"};
    private static final int MAX_DEPTH = 3;

    /**
     * Calcule, pour une map donnée, la liste des mouvements en utilisant l'algorithme alpha beta
     * @param map
     * @return
     */
    protected static ArrayList<Result> getAlphaBetaMove(MapManager map) {
        // On initialise en lançant l'algo alpha beta max, avec une profondeur initialisée à 3 et une valeur
        // alphaBeta nulle
        Object moves = alphaBetaMax(MAX_DEPTH, map, null, true);
        if (moves instanceof ArrayList) {
            ArrayList<Result> movesArray = (ArrayList<Result>) moves;
            return movesArray;
        }
        return new ArrayList<>();
    }

    /**
     * Fonction heuristique pour un seul groupe de crétures
     * @param map
     * @param coord
     * @return
     */
    private static double localHeuristic(MapManager map, Coord coord) {
        int nbCreatures;
        int nbHumans;
        int distance;
        double proba;
        double nbConverted; // Nombre d'humain convertis espérés (avec prise en compte des pertes subies)
        // Facteurs à déterminer expérimentalement
        double a = 1; // Importance dans le score des créatures effectives dans chaque camp
        double b = 1; // Importance dans le score des humains pouvant être convertis
        double phi = 0.8; // Facteur de décroissance pour accorder moins d'importance aux humains éloignés
        double score = 0;
        // Score des créatures alliées sur la case
        nbCreatures = map.map[coord.x][coord.y].population;
        score += a * nbCreatures;
        for (Coord humanCoord : map.humanPositions) {
            // Score des humains à distance des créatures alliées
            nbHumans = map.map[humanCoord.x][humanCoord.y].population;
            distance = Math.max(Math.abs(coord.x - humanCoord.x), Math.abs(coord.y - humanCoord.y));
            if (nbCreatures >= nbHumans){
                nbConverted = nbHumans;
            } else {
                proba = nbCreatures / (2 * nbHumans);
                nbConverted = proba * (proba * nbHumans - (1 - proba) * nbCreatures) - (1 - proba) * nbCreatures;
            }
            score += b * Math.pow(phi, distance - 1) * nbConverted;
        }
        return score;
    }

    /**
     * Fonction heuristique pour évaluer une situation donnée de la carte
     * @param map
     * @return
     */
    private static double heuristic(MapManager map) {
        int nbCreatures;
        int nbOpponents;
        int distance;
        // Facteurs à déterminer expérimentalement
        double c = 1; // Importance dans le score des attaques entre les deux équipes
        double phi = 0.8; // Facteur de décroissance pour accorder moins d'importance aux ennemis éloignés
        double proba;
        double battleGain; // Différence des pertes ennemies par nos pertes
        double score = 0;
        // Fonction heuristique locale pour chaque case alliée
        for (Coord coord : map.positions) {
            score += AlphaBeta.localHeuristic(map, coord);
        }
        // Fonction heuristique locale pour chaque case ennemi
        for (Coord opponentCoord : map.opponentPositions) {
            score -= AlphaBeta.localHeuristic(map, opponentCoord);
        }
        // Fonction heuristique de rapport de force entre les cases alliés-ennemies
        for (Coord coord : map.positions) {
            nbCreatures = map.map[coord.x][coord.y].population;
            for (Coord opponentCoord : map.opponentPositions) {
                nbOpponents = map.map[opponentCoord.x][opponentCoord.y].population;
                distance = Math.max(Math.abs(coord.x - opponentCoord.x), Math.abs(coord.y - opponentCoord.y);
                if (nbCreatures >= 1.5 * nbOpponents) {
                    // Cas de victoire sûre
                    battleGain = nbOpponents;
                } else if (nbOpponents >= 1.5 * nbCreatures) {
                    // Cas de défaite sûre
                    battleGain = - nbCreatures;
                } else {
                    // Cas de bataille
                    if (nbCreatures > nbOpponents) {
                        proba = (nbCreatures / nbOpponents) - 0.5;
                    } else {
                        proba = nbCreatures / (2 * nbOpponents);
                    }
                    battleGain = proba * (nbOpponents - (1 - proba) * nbCreatures)
                            + (1 - proba) * (proba * nbOpponents - nbCreatures);
                }
                score += c * Math.pow(phi, distance - 1) * battleGain;
            }
        }
        return score;
    }

    /**
     * Calcule le max dans l'algorithme alpha beta
     * @param depth : la profondeur actuelle
     * @param map : la map actuelle
     * @param alphaBetaValue : la valeur alphaBeta (borne sup de l'interval initial)
     * @param returnMove : Boolean pour indiquer si l'on doit renvoyer les mouvements ou pas
     * @return
     */
    private static Object alphaBetaMax(int depth, MapManager map, Double alphaBetaValue, boolean returnMove) {
        // Si la profondeur est inférieure ou égale à 0, on calcule l'heuristique
        if (depth <= 0) {
            return heuristic(map);
        } else {
            // Dans le cas où on retourne les mouvements, on initialise la liste des retours
            ArrayList<Result> moves = new ArrayList<>();
            // On crée un nouvel interval initialisé avec null en borne inf et la valeur alphaBeta en borne sup
            AlphaBetaInterval interval = new AlphaBetaInterval(null, alphaBetaValue);
            // Pour chacune de nos positions, on récupère la liste de ses enfants en fonction des différentes stratégies
            HashMap<Coord, ArrayList<Coord>> allChildren = getChildren(map);
            // Pour chaque coordonnée de nos positions
            for (Coord position: allChildren.keySet()) {
                // On va parcourir la liste des enfants possibles
                ArrayList<Coord> children = allChildren.get(position);
                int i = 0;
                // Tant que le nombre d'enfants n'a pas été parcouru et que l'interval n'est pas rempli convenablement
                while (i < children.size() &&
                        (interval.getInf() == null || interval.getSup() == null || interval.getInf() < interval.getSup())
                        ) {
                    // Le child représente la coordonnée que l'on vise (et donc peut être pas la coordonnée du prochain
                    // tour si distance > 1
                    Coord child = children.get(i);
                    // Pour ce mouvement, on va créer une nouvelle carte, impliquée par le mouvement vers la position
                    // child
                    MapManager impliedMap = map.cloneWithMovement(position, child);
                    i++;
                    // La valeur inférieure est donc l'algo alpha beta min, avec la profondeur baissée de 1, la
                    // nouvelle carte et la borne inf de l'interval
                    Double infValue = alphaBetaMin(depth - 1, impliedMap, interval.getInf());

                    // Si on a une infValue
                    if (infValue != null) {
                        // On ajoute les mouvements si on le demande et que l'inf valeur est meilleure que l'inf de
                        // l'interval
                        if (returnMove && (interval.getInf() == null || infValue > interval.getInf())) {
                            moves.add(new Result(
                                    position,
                                    map.map[position.x][position.y].population,
                                    Utils.findNextMove(position, child)
                            ));
                            interval.setInf(infValue);
                        }
                    } else {
                        // Si l'infValue est nulle, on ajoute les mouvements que si on le demande et si la borne inf
                        // est nulle
                        if (returnMove && interval.getInf() == null) {
                            moves.add(new Result(
                                    position,
                                    map.map[position.x][position.y].population,
                                    Utils.findNextMove(position, child)
                            ));
                        }
                    }
                }
            }

            // Si on demande les mouvements, on les retourne
            if (returnMove) {
                return moves;
            }

            // Sinon on retourne la borne inférieure
            return interval.getInf();
        }
    }

    /**
     * Calcule le min pour l'alpha beta (donc pour le tour d'un adversaire)
     * @param depth: profondeur actuelle
     * @param map: état de la carte actuel
     * @param alphaBetaValue: valeur min de l'interval initial
     * @return
     */
    private static Double alphaBetaMin(int depth, MapManager map, Double alphaBetaValue) {
        // Si la profondeur est inférieure ou égale à 0, on calcule l'heuristique
        if (depth <= 0) {
            return heuristic(map);
        } else {
            // On crée un nouvel interval initialisé avec null en borne inf et la valeur alphaBeta en borne sup
            AlphaBetaInterval interval = new AlphaBetaInterval(alphaBetaValue, null);
            // Pour chacune des positions adverse, on récupère la liste de ses enfants en fonction des différentes stratégies
            HashMap<Coord, ArrayList<Coord>> allChildren = getChildrenForOpponent(map);
            // Pour chaque coordonnée adverse
            for (Coord oPosition: allChildren.keySet()) {
                // On va parcourir la liste des enfants possibles
                ArrayList<Coord> children = allChildren.get(oPosition);
                int i = 0;
                // Tant que le nombre d'enfants n'a pas été parcouru et que l'interval n'est pas rempli convenablement
                while (i < children.size() &&
                        (interval.getInf() == null || interval.getSup() == null || interval.getInf() < interval.getSup())
                        ) {
                    // Le child représente la coordonnée que l'on vise (et donc peut être pas la coordonnée du prochain
                    // tour si distance > 1
                    Coord child = children.get(i);
                    // Pour ce mouvement, on va créer une nouvelle carte, impliquée par le mouvement vers la position
                    // child
                    MapManager impliedMap = map.cloneWithMovement(oPosition, child);
                    i++;
                    // La valeur supérieure est donc l'algo alpha beta max, avec la profondeur baissée de 1, la
                    // nouvelle carte et la borne sup de l'interval, et pas de retour de mouvement
                    Object objectSupValue = alphaBetaMax(depth - 1, impliedMap, interval.getSup(), false);
                    if (objectSupValue instanceof Double) {
                        Double supValue = (Double) objectSupValue;
                        if (supValue < interval.getSup()) {
                            interval.setSup(supValue);
                        }
                    }
                }
            }
            // On retourne la borne
            return interval.getSup();
        }
    }

    /**
     * Pour chacune de nos coordonnées sur la carte et chacune des stratégies possibles, on retourne les meilleurs
     * déplacements à faire
     * @param map
     * @return
     */
    private static HashMap<Coord, ArrayList<Coord>> getChildren(MapManager map) {
        HashMap<Coord, ArrayList<Coord>> possibleMoves = new HashMap<>();
        for (Coord position: map.positions) {
            possibleMoves.put(position, new ArrayList<>());
            for (String movement: MOVEMENT_TYPES) {
                possibleMoves.get(position).addAll(AlphaBeta.findBestMoveForStrategy(movement, position, map));
            }
        }
        return possibleMoves;
    }

    /**
     * Retourne la liste de tous les meilleurs coups possibles pour l'adversaire
     * @param map
     * @return
     */
    private static HashMap<Coord, ArrayList<Coord>> getChildrenForOpponent(MapManager map) {
        // On change de côté on se fait passer pour l'ennemi
        MapManager flippedMap = map.flip();
        // On retourne sa liste de meilleurs coups avec la carte échangée
        return getChildren(flippedMap);
    }

    /**
     * Retourne, pour une stratégie donnée et une position sur la carte, une liste de taille MAX_DEPTH au maximum des
     * meilleurs coups à faire
     * @param movement
     * @param position
     * @param map
     * @return
     */
    private static ArrayList<Coord> findBestMoveForStrategy(String movement, Coord position, MapManager map) {
        ArrayList<Coord> moves = new ArrayList<>();
        int maxDistance = -1;
        switch (movement) {
            case "attack":
                // Dans le cas d'une attaque, le meilleur coup sera d'attaquer le groupe le plus proche avec le plus
                // grand nombre d'adversaires mais dont leur nombre <= 1.5*la taille de notre groupe
                for (Coord opp: map.opponentPositions) {
                    Cell cell = map.map[opp.x][opp.y];
                    if (cell.population <= 1.5 * map.map[position.x][position.y].population) {
                        // On ajoute que si elle est plus proche que la plus lointaine des solutions si leur nombre
                        // dépasse MAX_DEPTH
                        maxDistance = AlphaBeta.addOrNotMovePosition(position, MAX_DEPTH, moves, maxDistance, opp);
                    }
                }
                return moves;
            case "transform":
                // Dans le cas d'une transformation, on va essayer de récupérer le plus grand nombre d'humains le plus
                // proche avec comme contrainte que le nombre d'humain doit être inférieur ou égal à la population de
                // notre position
                for (Coord human: map.humanPositions) {
                    Cell cell = map.map[human.x][human.y];
                    if (cell.population <= map.map[position.x][position.y].population) {
                        // On ajoute que si elle est plus proche que la plus lointaine des solutions si leur nombre
                        // dépasse MAX_DEPTH
                        maxDistance = addOrNotMovePosition(position, MAX_DEPTH, moves, maxDistance, human);
                    }
                }
                return moves;
            case "escape":
                // To do
            default: return new ArrayList<>();
        }

    }

    /**
     * Décide si on doit ajouter ou non le mouvement à la liste des résultats
     * @param position
     * @param maxResult
     * @param moves
     * @param maxDistance
     * @param opp
     * @return
     */
    private static int addOrNotMovePosition(Coord position, int maxResult, ArrayList<Coord> moves, int maxDistance, Coord opp) {
        // Si la taille maximal est atteinte
        if (moves.size() == maxResult) {
            int newMaxDistance = -1;
            // Si la distance de la position que l'on veut ajouter est plus petite que la distance max on va chercher
            // l'élément qui répond à cette distance, le supprimer et mettre l'élément à la place
            if (Utils.minDistance(position, opp) < maxDistance) {
                ArrayList<Coord> toRemove = new ArrayList<>();
                for (Coord moveCoord: moves) {
                    if (Utils.minDistance(moveCoord, position) == maxDistance) {
                        toRemove.add(moveCoord);
                    } else {
                        newMaxDistance = Math.max(newMaxDistance, Utils.minDistance(position, moveCoord));
                    }
                }
                moves.removeAll(toRemove);
                // On ajoute l'élément que l'on veut
                moves.add(opp);
                maxDistance = newMaxDistance;
            }
        } else {
            // Sinon on l'ajoute directement en changeant la distance max
            maxDistance = Math.max(maxDistance, Utils.minDistance(position, opp));
            moves.add(opp);
        }
        return maxDistance;
    }

}
