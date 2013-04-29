package org.jentrata.ebms.cpa.internal;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.is;
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

    protected static File fileFromClasspath(String filename) {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
        return file;
    }
}
