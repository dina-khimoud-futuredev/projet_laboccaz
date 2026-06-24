package com.laboccaz.devis.entity;

public enum RefusalReason {

    PAS_DE_BUDGET("Pas de budget / financement refusé"),
    CONCURRENT_CHOISI("Concurrent choisi"),
    OFFRE_NON_ADAPTEE("Offre non adaptée / prix trop élevé"),
    DECISION_REPORTEE("Décision reportée / projet gelé"),
    DOUBLON("Doublon / fusion avec un autre dossier"),

    PAS_DISPONIBLE("Produit non disponible"),
    PRIX_TROP_ELEVE("Prix trop élevé"),
    CLIENT_NON_INTERESSE("Client non intéressé"),
    HORS_CIBLE("Demande hors cible"),
    AUTRE("Autre");

    private final String label;

    RefusalReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}