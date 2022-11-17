package org.openmrs.module.pacsintegration.test;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.api.EncounterService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class TransactionalTestServiceImpl implements TransactionalTestService {

    private EncounterService encounterService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Encounter saveEncounter(Encounter encounter) {
        EncounterType encounterType = encounterService.getEncounterType(encounter.getEncounterType().getEncounterTypeId());
        encounter.setEncounterType(encounterType);
        return encounterService.saveEncounter(encounter);
    }

    public void setEncounterService(EncounterService encounterService) {
        this.encounterService = encounterService;
    }
}
