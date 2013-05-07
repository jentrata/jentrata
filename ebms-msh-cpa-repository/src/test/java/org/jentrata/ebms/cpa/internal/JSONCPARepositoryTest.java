package org.jentrata.ebms.cpa.internal;

import com.google.common.collect.ImmutableMap;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Unit test for org.jentrata.ebms.cpa.internal.JSONCPARepository
 *
 * @author aaronwalker
 */
public class JSONCPARepositoryTest {

    @Test
    public void testLoadEmptyFile() throws IOException {

        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("empty.json"));
        repository.init();
        assertThat(repository.getActivePartnerAgreements().size(),equalTo(0));
    }

    @Test
    public void testLoadNonExistentFile() throws IOException {

        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(new File("nonexistent.json"));
        repository.init();
        assertThat(repository.getActivePartnerAgreements().size(),equalTo(0));
    }

    @Test
    public void testLoadNullFile() throws IOException {
        JSONCPARepository repository = new JSONCPARepository();
        repository.init();
        assertThat(repository.getActivePartnerAgreements().size(),equalTo(0));
    }

    @Test
    public void testLoadSingleAgreement() throws IOException {

        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("singleAgreement.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements().size(),equalTo(1));
        assertThat(repository.getActivePartnerAgreements().get(0).getCpaId(),equalTo("testCPAId1"));
        assertThat(repository.getActivePartnerAgreements().get(0).getTransportReceiverEndpoint(),equalTo("http://example.jentrata.org"));
        assertThat(repository.getActivePartnerAgreements().get(0).isActive(),is(true));
    }

    @Test
    public void testLoadMultipleAgreements() throws IOException {

        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("multipleAgreements.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements().size(),equalTo(2));

        assertThat(repository.getActivePartnerAgreements().get(0).getCpaId(),equalTo("testCPAId1"));
        assertThat(repository.getActivePartnerAgreements().get(0).getTransportReceiverEndpoint(),equalTo("http://example.jentrata.org"));
        assertThat(repository.getActivePartnerAgreements().get(0).isActive(),is(true));

        assertThat(repository.getActivePartnerAgreements().get(1).getCpaId(),equalTo("testCPAId2"));
        assertThat(repository.getActivePartnerAgreements().get(1).getTransportReceiverEndpoint(),equalTo("http://example2.jentrata.org"));
        assertThat(repository.getActivePartnerAgreements().get(1).isActive(),is(true));
    }

    @Test
    public void testLoadMultipleAgreementsWithOneActive() throws IOException {

        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("multipleAgreementsOneActive.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements().size(),equalTo(1));

        assertThat(repository.getActivePartnerAgreements().get(0).getCpaId(),equalTo("testCPAId2"));
        assertThat(repository.getActivePartnerAgreements().get(0).getTransportReceiverEndpoint(),equalTo("http://example2.jentrata.org"));
        assertThat(repository.getActivePartnerAgreements().get(0).isActive(),is(true));
    }

    @Test
    public void testGetAllAgreements() throws IOException {

        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("multipleAgreementsOneActive.json"));
        repository.init();

        List<PartnerAgreement> partnerAgreements = repository.getPartnerAgreements();
        assertThat(partnerAgreements.size(),equalTo(2));
        assertThat(partnerAgreements.get(0).getCpaId(),equalTo("testCPAId1"));
        assertThat(partnerAgreements.get(0).getTransportReceiverEndpoint(),equalTo("http://example.jentrata.org"));
        assertThat(partnerAgreements.get(0).isActive(),is(false));

        assertThat(partnerAgreements.get(1).getCpaId(),equalTo("testCPAId2"));
        assertThat(partnerAgreements.get(1).getTransportReceiverEndpoint(),equalTo("http://example2.jentrata.org"));
        assertThat(partnerAgreements.get(1).isActive(),is(true));
    }

    @Test
    public void testFindAgreementByServiceAndAction() throws IOException {

        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithServices.json"));
        repository.init();

        PartnerAgreement agreement1 = repository.findByServiceAndAction("service1", "action1");
        assertThat(agreement1,notNullValue());
        assertThat(agreement1.getCpaId(),equalTo("testCPAId1"));

        PartnerAgreement agreement2 = repository.findByServiceAndAction("service2", "action2");
        assertThat(agreement2,notNullValue());
        assertThat(agreement2.getCpaId(),equalTo("testCPAId1"));

    }

    @Test
    public void testIsValidPartnerAgreement() throws IOException {

        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithServices.json"));
        repository.init();

        Map<String,Object> fields = new ImmutableMap.Builder<String, Object>()
                .put(EbmsConstants.MESSAGE_SERVICE, "service1")
                .put(EbmsConstants.MESSAGE_ACTION,"action1")
                .build();
        assertThat(repository.isValidPartnerAgreement(fields),is(true));

        Map<String,Object> fields2 = new ImmutableMap.Builder<String, Object>()
                .put(EbmsConstants.MESSAGE_SERVICE, "service2")
                .put(EbmsConstants.MESSAGE_ACTION,"action1")
                .build();
        assertThat(repository.isValidPartnerAgreement(fields2),is(false));


    }

    protected static File fileFromClasspath(String filename) {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
        return file;
    }
}
