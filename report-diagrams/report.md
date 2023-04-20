# Rapport Intermédiaire - Application Informatique (12X015)

#### *Noah Munz - 18 Avril 2023*

<!-- vim-markdown-toc GFM -->

* [Identification des besoins](#identification-des-besoins)
        * [Qui est la cliente ?](#qui-est-la-cliente-)
        * [Qui seront les utilisateurs du logiciel ?](#qui-seront-les-utilisateurs-du-logiciel-)
        * [Quels sont les problèmes rencontrés ?](#quels-sont-les-problèmes-rencontrés-)
        * [Quel est l’objectif principal du logiciel, en quoi ce logiciel résoudra les problèmes ?](#quel-est-lobjectif-principal-du-logiciel-en-quoi-ce-logiciel-résoudra-les-problèmes-)
        * [Quelles sont les solutions existantes et en quoi la solution que vous proposez est différente ?](#quelles-sont-les-solutions-existantes-et-en-quoi-la-solution-que-vous-proposez-est-différente-)
        * [sous quelle licence le code sera-t-il livré ?](#sous-quelle-licence-le-code-sera-t-il-livré-)
    * [Liste des besoins](#liste-des-besoins)
        * [explicités par la cliente](#explicités-par-la-cliente)
        * [mis en avant par le developpeur](#mis-en-avant-par-le-developpeur)
* [Développement](#développement)
    * [Conception](#conception)
        * [Scénario d'utilisation](#scénario-dutilisation)
        * [Méthodes de conception & diagrammes](#méthodes-de-conception--diagrammes)
    * [Implémentation](#implémentation)
    * [Tests et évaluation](#tests-et-évaluation)
* [Organisation](#organisation)
* [Formation](#formation)
* [Feedback](#feedback)
* [Annexes](#annexes)

<!-- vim-markdown-toc -->

## Identification des besoins

#### Qui est la cliente ?

Anne-Isabelle Giuntini, administration et secrétariat des étudiants de Battelle (Batiment A).

#### Qui seront les utilisateurs du logiciel ?

Principalement la cliente elle même.

#### Quels sont les problèmes rencontrés ?

<!-- TODO: -->

#### Quel est l’objectif principal du logiciel, en quoi ce logiciel résoudra les problèmes ?

es problèmes de la cliente ont commencé du au fait que le principal support utilisé
par l'université pour décrire un cours et son contenu était :

1. Confus et épars (problème d'UI/UX dans l'affichage des informations)
2. Présents à plusieurs endroits du site de l'université sans avoir de cohérence
ni de _contenu_ (i.e. données concrètes) ni de _forme_ (i.e. séléction et mise en forme)
3. Les informations n'étaient pas affichées dans leur integralité
4. La modification de ces données était longue et fastidieuse à entreprendre à entreprendre


Sa solution (temporaire) était donc de reporter manuellement ces informations dans documents
word dont les pdfs étaient accessibles par les étudiants.

La création de ces pdfs est donc evidemment fastidieuse, répétitive et sujette à erreur.

C'est la qu'entre en jeux la solution developpé: l'automatisation de ces fiches descriptives.  
(Voir exemple ci-dessous)

![image](res\readme-example.png)



L'object du logiciel est de générer automatiquement des PDFs de fiches descriptives de cours
de 1-2 pages qui contiennent cette compression et ce résumé



#### Quelles sont les solutions existantes et en quoi la solution que vous proposez est différente ?

Il n'existe pas de solution à proprement parler pour ce problème spécifique.
Pas automatisé tout du moins.
Ce qui est la principale différence de la solution apporté (en outre de
l'esthétisme du nouveau format)

#### sous quelle licence le code sera-t-il livré ?

Sous la license MIT.
Pour plus d'information voir [https://choosealicense.com/licenses/mit](https://choosealicense.com/licenses/mit/)

### Liste des besoins

#### explicités par la cliente

*   PDF contenant un descriptif de cours
*   Information condensé dans un format pratique et concis (1 page A4)
*   Généré automatiquement

#### mis en avant par le developpeur

*   Génération de descriptifs "à la vollée" ("batch generation")
    par plan d'étude (rentrer un nom du type "BA-Inf" pour générer les descriptifs des cours du bachelor en sciences informatiques)

*   Realisation d'un installeur windows (.msi) pour simplifier le déploiement et éviter
    l'execution de scripts powershell qui sont, certes très pratiques, mais désactivés / bloqués
    par défaut sur la plupart des machines.

*   Implémentation d'une interface en rust qui utilise directement l'api Windows
    (voir [winsafe](https://docs.rs/winsafe/latest/winsafe/) et [winapi](https://docs.rs/winapi/latest/winapi/))
    pour lancer de manière "safe" les différents programmes externes ([pandoc](https://pandoc.org/) et [wkhtmltopdf](https://wkhtmltopdf.org/)) en parallèle afin de

    1.  Encore eviter l'utilisation de scripts powershell dont l'execution pourrait se faire bloquer sur la machine de la cliente.
    2.  Permettre un traitement "en masse" de fichiers markdown, effficient et optimisé grâce à la
        librairie de traitement de donnése en parallèle de rust, [rayon](https://docs.rs/rayon/latest/rayon/).

***

<br/>

## Développement

### Conception

#### Scénario d'utilisation

Pour donner un exemple d'utilisation sous forme de scénario, prenons en considération
le changement de batiment du cours de base de données qui a eu lieu il y a un ou deux ans.

Le cours de base de donnée était originellement, il y a un ou deux ans, donné à Uni-Bastions (>30min de trajet en tram du batiment informatique).
Cependant, au moment de l'écriture de ce rapport, il est toujours marqué dans la base de donnée de l'université que se cours se déroule à Bastion.

Ce qui pose le problème évident d'avoir l'intégralité des 2èmes des années du département d'informatique, se déplacer à l'opposé de la où ils ont réellement cours pour aller dans une salle qui est surement attribué à un autre cours/enseignant.

Ce problème n'aurait pas eu lieu si les descriptifs du cours présent à différents
endroits du site de l'université avaient pu régulièrment se mettre à jour.

#### Méthodes de conception & diagrammes

*




***

<br/>

### Implémentation

*

### Tests et évaluation

*

***

<br/>

## Organisation

*

***

<br/>

## Formation

*

***

<br/>

## Feedback

*

## Annexes

*
