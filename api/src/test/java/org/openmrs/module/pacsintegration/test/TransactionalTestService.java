package org.openmrs.module.pacsintegration.test;

import org.openmrs.Encounter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface TransactionalTestService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Encounter saveEncounter(Encounter encounter);
}
