# Rapport projet d'intelligence artificielle 

<p align="justify">Ce projet a pour but de créer une Intelligence Artificielle permettant de jouer au jeu "Vampires vs Loup-Garous".</p>

### Table of content: 
+ **[1. Prérequis](#prerequisites)**
+ **[2. La structure du code](#code_structure)**
+ **[3. L'implémentation de l'algorithme MinMax](#alg_implementation)**
  + **[3.1. Le déroulé général](#general_behavior)**
  + **[3.2. La création des alternatives/branches](#branches)**
  + **[3.3. Séparation et rassemblement](#split_merge)**
  + **[3.4. L'heuristique](#heuristic)**
+ **[4. Idées d'amélioration](#next_steps)**

### 1. Prérequis  <a name="prerequisites"></a>

<p align="justify">Afin de pouvoir lancer l'IA, il vous suffit d'avoir, sur la machine, Java 8.</p>

### 2. La structure du code <a name="code_structure"></a>

<p align="justify">Le code est construit autour de 5 packages:

- **utils :** Ce package n'est composé que d'une seule case, la classe `Utils`, une classe statique fournissant quelques fonctions utilisables par d'autres packages. On y retrouve notamment des fonctions pour calculer le prochain mouvement, supprimer des duplicats, ...
- **tcp :** Ce package contient le client TCP implémenté dans le but de communiquer avec le serveur. Son rôle sera donc de comprendre les trames envoyées par le serveur et d'envoyer des trames compréhensibles par le serveur avec nos mouvements pendant la partie.
- **main :** Ce package contient la classe `Project` qui est lancée par le fichier d'exécution, il s'agit du point d'entrée du programme. Il va écouter en permanence le client et assurer de transmettre les bonnes informations aux classes qui implémentent la logique métier.
- **board :** Ce package donne une représentation en objet du jeu. Il contient donc 4 classes : 
  - `Board`: Cette classe est une représentation du plateau de jeu, et contient la liste des positions de chacune des espèces, ainsi que le contenu de chacune des cellules de la carte.
  - `Position`: Cette classe représente une position sur la carte, et est donc donnée par deux coordonnées x et y.
  - `Player`: Définie par une race, cette classe permet de définir les deux joueurs et de savoir qui est le joueur actif lors du déroulé de l'algorithme MinMax.
  - `Cell`: Cette classe représente le contenu d'une cellule de la carte. Elle est donc définie par une race qui la peuple et par une population.
- **algorithm :** Ce package contient toute l'intelligence de notre programme, l'algorithme qui est exécuté à chacun de nos tours et visant à fournir le meilleur déplacement possible:
  - `Result`: Cette classe est une représentation d'un résultat de mouvement, et se constitue donc d'une position source, d'une destination, et d'un nombre d'unités déplacées.
  - `Node`: La classe `Node` est un noeud de l'arbre créé par l'algorithme MinMax et se constitue donc d'une carte associée et également du nombre d'humains mangés par nous et l'adversaire depuis le début de l'algorithme jusqu'à la carte créée.
  - `MinMax`: C'est ici que l'algorithme est déroulé, et cette classe contient la logique et l'heuristique. </p>
  
### 3. L'implémentation de l'algorithme MinMax <a name="alg_implementation"></a>

#### 3.1. Le déroulé général <a name="general_behavior"></a>

<p align="justify">A chacun de notre tour, le programme réagit de la manière suivante:

1. A chaque tour, on récupère la trame d'update de la carte et on appelle alors la méthode `fillOrUpdate` de `Board` **(Board L. 54)**. Cette méthode est appelée au début de la partie pour créer toute la carte mais également à chaque tour afin de garder la carte dans la même état que le serveur.
2. Une fois la carte mise à jour, on va envoyer nos mouvements. Pour se faire, la classe `Board` va instantier une instance de `MinMax` avec comme racine de l'arbre la carte courante, avant notre tour de jeu. **(Board, L. 299)**
3. On va alors appeler la méthode `algorithm` de `MinMax` à une profondeur 3. **(Board L. 300)**
4. Cette fonction va simplement appeler `minMax` et retourner l'attribut de classe `bestMoves`, représentant les mouvements à envoyer au serveur. **(MinMax L. 31)**
</p>

#### 3.2. La création des alternatives/branches <a name="branches"></a>

#### 3.3. Séparation et rassemblement <a name="split_merge"></a>

Dans certains scénarios, il peut être utile de séparer les alliés en plusieurs groupes. Les groupes d'alliés ainsi formés doivent respecter certains critères : ils doivent ainsi tous avoir une taille leur permettant de gagner au moins un combat sur la carte. Concrètement, cette condition signifique que chaque groupe d'alliés formé doit comprendre au moins min(Nombre minimum d'humain, 1,5 * Nombre minimum d'ennemis) alliés.


#### 3.4. L'heuristique <a name="heuristic"></a>

<p align="justify">Le calcul de l'heuristique est fait de manière globale: cela veut dire que l'on va évaluer l'état d'une carte, avec nos positions, celles de l'ennemis ainsi que les humains. L'idée ici n'est pas de calculer un score pour chaque déplacement et de sommer le tout. Ici, le but est de prendre en compte les positions des alliés entre eux, avec tous les mouvements possibles, afin d'être le plus fidèle possible à ce qui pourrait se passer. Le calcul de l'heuristique repose sur la formule suivante:</p>

<figure>
  <p align="center">
    <img src="./report/heuristic_full.png"/>
    <br>Fig1. - Formule utilisée pour évaluer une carte.
  </p>
</figure>

**Calcul de la fonction d'évaluation des humains:**

Si on se concentre maintenant sur le calcul de la fonction d'évaluation des humains, on la représente de la manière suivante:

<figure>
  <p align="center">
    <img src="./report/heuristic_humans.png"/>
    <br>Fig2. - Formule utilisée pour évaluer les humains sur une carte.
  </p>
</figure>

Avec dans cette équation, `est_plus_proche`, définie par : 

<figure>
  <p align="center">
    <img src="./report/is_nearest.png"/>
    <br>Fig3. - Décomposition de la fonction est_plus_proche
  </p>
</figure>

Ainsi, en pratique, le raisonnement est le suivant: pour chaque groupe d'humains, on va chercher l'ennemi le plus proche dont la population dépasse la population d'humains sur la case. On fait la même chose pour les alliés. Si la distance minimum est celle de l'ennemi, ou que les distances sont égales mais que c'est à nous de jouer, alors on ajoute la valeur du ratio ![human ratio](./report/human_ratio.png "Nombre d'humains / distance minimale"). Dans le cas contraire, le ratio est retiré du score. **(MinMax L. 88 - L. 115)**

**Calcul de la fonction d'évaluation des opposants:**

Intéressons nous désormais à la fonction d'évaluation des opposants sur la carte. Elle se décompose sous la forme suivante

<figure>
  <p align="center">
    <img src="./report/heuristic_humans.png"/>
    <br>Fig4. - Formule utilisée pour évaluer les ennemis sur une carte.
  </p>
</figure>

Où la fonction `score` est déterminée de la manière suivante:

<figure>
  <p align="center">
    <img src="./report/score_ennemi_allie.png"/>
    <br>Fig5. - Score entre un allié et son plus proche opposant
  </p>
</figure>

Ici, le raisonnement est implicite à la formule : pour chaque allié, on va trouver l'ennemi le plus proche, et calculer le score en fonction des populations de chacun des deux groupes **(MinMax L. 117 - L. 141)**.