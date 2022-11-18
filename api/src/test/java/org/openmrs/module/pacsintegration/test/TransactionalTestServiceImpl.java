package org.openmrs.module.pacsintegration.test;

import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalTestServiceImpl implements TransactionalTestService {

    private EncounterService encounterService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Encounter saveEncounter(Encounter encounter) {
        return encounterService.saveEncounter(encounter);
    }

    public void setEncounterService(EncounterService encounterService) {
        this.encounterService = encounterService;
    }
}