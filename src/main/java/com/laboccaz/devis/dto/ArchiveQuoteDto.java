package com.laboccaz.devis.dto;

import com.laboccaz.devis.entity.ArchiveReason;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArchiveQuoteDto {

    /**
     * Motif prédéfini (obligatoire sauf si archiveReasonCustom est renseigné).
     * Valeurs : AFFAIRE_CONCLUE, CLIENT_NON_INTERESSE, PAS_DE_BUDGET,
     * CONCURRENT_CHOISI, OFFRE_NON_ADAPTEE, DECISION_REPORTEE,
     * PROSPECT_PERDU, PROBLEME_TECHNIQUE, DOSSIER_INCOMPLET,
     * ERREUR_CIBLAGE, DOUBLON, PAS_OFFRE_ADAPTEE,
     * ORIENTE_CONFRERE, DELAI_DEPASSE, EN_LIGNE, AUTRE
     */
    private ArchiveReason archiveReason;

    /**
     * Motif libre — obligatoire si archiveReason == AUTRE.
     * Optionnel pour les autres motifs (précisions supplémentaires).
     */
    private String archiveReasonCustom;
}