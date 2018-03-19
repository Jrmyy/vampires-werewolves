# Rapport projet d'intelligence artificielle 

Ce projet a pour but de créer une Intelligence Artificielle permettant de jouer au jeu "Vampires vs Loup-Garous".

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

Afin de pouvoir lancer l'IA, il vous suffit d'avoir, sur la machine, Java 8.

### 2. La structure du code <a name="code_structure"></a>

Le code est construit autour de 5 packages:

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
  - `MinMax`: C'est ici que l'algorithme est déroulé, et cette classe contient la logique et l'heuristique. 
  
### 3. L'implémentation de l'algorithme MinMax <a name="alg_implementation"></a>

#### 3.1. Le déroulé général <a name="general_behavior"></a>

A chacun de notre tour, le programme réagit de la manière suivante:

1. A chaque tour, on récupère la trame d'update de la carte et on appelle alors la méthode `fillOrUpdate` de `Board` **(Board L. 54)**. Cette méthode est appelée au début de la partie pour créer toute la carte mais également à chaque tour afin de garder la carte dans la même état que le serveur.
2. Une fois la carte mise à jour, on va envoyer nos mouvements. Pour se faire, la classe `Board` va instantier une instance de `MinMax` avec comme racine de l'arbre la carte courante, avant notre tour de jeu. **(Board, L. 299)**
3. On va alors appeler la méthode `algorithm` de `MinMax` à une profondeur 3. **(Board L. 300)**
4. Cette fonction va simplement appeler `minMax` et retourner l'attribut de classe `bestMoves`, représentant les mouvements à envoyer au serveur. **(MinMax L. 31)**
5. 

#### 3.2. La création des alternatives/branches <a name="branches"></a>

#### 3.3. Séparation et rassemblement <a name="split_merge"></a>

#### 3.4. L'heuristique <a name="heuristic"></a>

Le calcul de l'heuristique est fait de manière globale: cela veut dire que l'on va évaluer l'état d'une carte, avec nos positions, celles de l'ennemis ainsi que les humains. L'idée ici n'est pas de calculer un score pour chaque déplacement et de sommer le tout. Ici, le but est de prendre en compte les positions des alliés entre eux, avec tous les mouvements possibles, afin d'être le plus fidèle possible à ce qui pourrait se passer. Le calcul de l'heuristique repose sur la formule suivante:

<figure>
  <p align="center">
    <img src="./report/heuristic_full.png"/>
  </p>
  <p align="center">
    <figcaption>Fig1. - Formule utilisée pour évaluer une carte.</figcaption>
  </p>
</figure>
