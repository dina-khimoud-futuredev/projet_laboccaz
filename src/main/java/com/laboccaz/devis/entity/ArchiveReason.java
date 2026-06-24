package com.laboccaz.devis.entity;

public enum ArchiveReason {
    AFFAIRE_CONCLUE("Affaire conclue"),
    CLIENT_NON_INTERESSE("Client non intéressé / refus de poursuivre"),
    PAS_DE_BUDGET("Pas de budget / financement refusé"),
    CONCURRENT_CHOISI("Concurrent choisi"),
    OFFRE_NON_ADAPTEE("Offre non adaptée / prix trop élevé"),
    DECISION_REPORTEE("Décision reportée / projet gelé"),
    PROSPECT_PERDU("Pas de réponse du contact (prospect perdu)"),
    PROBLEME_TECHNIQUE("Problème technique ou logistique"),
    DOSSIER_INCOMPLET("Dossier incomplet / pièces manquantes"),
    ERREUR_CIBLAGE("Erreur de ciblage / hors périmètre"),
    DOUBLON("Doublon / fusion avec un autre dossier"),
    PAS_OFFRE_ADAPTEE("Pas d'offre adaptée de notre part"),
    ORIENTE_CONFRERE("Orienté vers un confrère"),
    DELAI_DEPASSE("Délai dépassé"),
    EN_LIGNE("En ligne"),
    AUTRE("Autre (motif libre)");

    private final String label;

    ArchiveReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}