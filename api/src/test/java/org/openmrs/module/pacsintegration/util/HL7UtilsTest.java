/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.pacsintegration.util;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v23.message.ACK;
import ca.uhn.hl7v2.model.v23.message.ORU_R01;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.Calendar;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class HL7UtilsTest {

    private Parser parser = new PipeParser();

    @Test
    public void shouldGenerateMessageHeader() throws HL7Exception {

        Calendar cal = Calendar.getInstance() ;
        cal.set(2013, Calendar.FEBRUARY, 28);
        cal.set(Calendar.HOUR_OF_DAY, 22);
        cal.set(Calendar.MINUTE, 25);
        cal.set(Calendar.SECOND,10);

        ORU_R01 message = new ORU_R01();

        HL7Utils.populateMessageHeader(message.getMSH(), cal.getTime(), "ORU", "R01", "openmrs_mirebalais");
        assertThat(parser.encode(message), is("MSH|^~\\&||openmrs_mirebalais|||20130228222510||ORU^R01||P|2.3\r"));
    }

    @Test
    public void shouldGenerateACK() throws HL7Exception {
        ACK ack = HL7Utils.generateACK("123", "openmrs_mirebalais");
        assertThat(parser.encode(ack), is(new IsExpectedACKMessage()));
    }

    @Test
    public void shouldGenerateErrorACK() throws HL7Exception {
        ACK ack = HL7Utils.generateErrorACK("123", "openmrs_mirebalais", "Something went wrong");
        assertThat(parser.encode(ack), is(new IsExpectedErrorACKMessage()));
    }

    public class IsExpectedACKMessage implements ArgumentMatcher<String> {

        @Override
        public boolean matches(String hl7Message) {
            assertThat(hl7Message, startsWith("MSH|^~\\&||openmrs_mirebalais|||"));
            // TODO: test that a valid date is passed
            assertThat(hl7Message, containsString("||ACK||P|2.3\r"));
            assertThat(hl7Message, containsString("MSA|AA|123\r"));

            return true;
        }
    }

    public class IsExpectedErrorACKMessage implements ArgumentMatcher<String> {

        @Override
        public boolean matches(String hl7Message) {
            assertThat(hl7Message, startsWith("MSH|^~\\&||openmrs_mirebalais|||"));
            // TODO: test that a valid date is passed
            assertThat(hl7Message, containsString("||ACK||P|2.3\r"));
            assertThat(hl7Message, containsString("MSA|AR|123|Something went wrong\r"));

            return true;
        }
    }
}
