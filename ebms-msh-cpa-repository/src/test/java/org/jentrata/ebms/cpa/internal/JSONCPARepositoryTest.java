package org.jentrata.ebms.cpa.internal;

import com.google.common.collect.ImmutableMap;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.jentrata.ebms.cpa.pmode.PayloadService;
import org.jentrata.ebms.cpa.pmode.Security;
import org.jentrata.ebms.cpa.pmode.Signature;
import org.jentrata.ebms.cpa.pmode.UsernameToken;
import org.jentrata.ebms.utils.EbmsUtils;
import org.junit.Test;
import org.w3c.dom.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
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
    public void testAgreementWithSecurity() throws IOException {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithSecurity.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        assertThat(repository.getActivePartnerAgreements().get(0).hasSecurityToken(),is(true));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().getSendReceiptReplyPattern(),is(Security.ReplyPatternType.Response));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().isSendReceipt(),is(false));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().isSendReceiptNonRepudiation(),is(true));

        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().getSecurityToken(), instanceOf(UsernameToken.class));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().getSecurityToken().getTokenType(),equalTo(UsernameToken.class.getName()));
        UsernameToken usernameToken = (UsernameToken) repository.getActivePartnerAgreements().get(0).getSecurity().getSecurityToken();
        assertThat(usernameToken.getUsername(),equalTo("jentrata"));
        assertThat(usernameToken.getPassword(),equalTo("verySecret"));
        assertThat(usernameToken.isDigest(),is(false));
        assertThat(usernameToken.isNonce(),is(false));
        assertThat(usernameToken.isCreated(),is(false));
    }

    @Test
    public void testAgreementWithMinimalSecurity() throws IOException {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithMinimalSecurity.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        assertThat(repository.getActivePartnerAgreements().get(0).hasSecurityToken(),is(true));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().getSendReceiptReplyPattern(),is(Security.ReplyPatternType.Callback));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().isSendReceipt(),is(true));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().isSendReceiptNonRepudiation(),is(false));

        UsernameToken usernameToken = (UsernameToken) repository.getActivePartnerAgreements().get(0).getSecurity().getSecurityToken();
        assertThat(usernameToken.getUsername(),nullValue());
        assertThat(usernameToken.getPassword(),nullValue());
        assertThat(usernameToken.isDigest(),is(true));
        assertThat(usernameToken.isNonce(),is(true));
        assertThat(usernameToken.isCreated(),is(true));
    }

    @Test
    public void testAgreementWithSignatureSecurity() throws IOException {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithSignatureSecurity.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        assertThat(repository.getActivePartnerAgreements().get(0).hasSecurityToken(),is(true));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().getSendReceiptReplyPattern(),is(Security.ReplyPatternType.Response));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().isSendReceipt(),is(false));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().isSendReceiptNonRepudiation(),is(true));

        UsernameToken usernameToken = (UsernameToken) repository.getActivePartnerAgreements().get(0).getSecurity().getSecurityToken();
        assertThat(usernameToken.getUsername(),equalTo("jentrata"));
        assertThat(usernameToken.getPassword(),equalTo("verySecret"));
        assertThat(usernameToken.isDigest(),is(false));
        assertThat(usernameToken.isNonce(),is(false));
        assertThat(usernameToken.isCreated(),is(false));

        Signature signature = repository.getActivePartnerAgreements().get(0).getSecurity().getSignature();
        assertThat(signature,notNullValue());
        assertThat(signature.isEncrypt(),is(true));
    }

    @Test
    public void testAgreementWithNoSecurity() throws IOException {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("singleAgreement.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        assertThat(repository.getActivePartnerAgreements().get(0).hasSecurityToken(),is(false));
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

    @Test
    public void testPayloadServiceWithDefaults() throws Exception {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("singleAgreement.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        assertThat(repository.getActivePartnerAgreements().get(0).getPayloadService(),notNullValue());
        assertThat(repository.getActivePartnerAgreements().get(0).getPayloadService().getPayloadId(),nullValue());
        assertThat(repository.getActivePartnerAgreements().get(0).getPayloadService().getCompressionType(), equalTo(PayloadService.CompressionType.NONE));

    }

    @Test
    public void testPayloadServiceWithGZipCompression() throws Exception {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithGzipCompression.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        assertThat(repository.getActivePartnerAgreements().get(0).getPayloadService(),notNullValue());
        assertThat(repository.getActivePartnerAgreements().get(0).getPayloadService().getPayloadId(),nullValue());
        assertThat(repository.getActivePartnerAgreements().get(0).getPayloadService().getCompressionType(), equalTo(PayloadService.CompressionType.GZIP));

    }

    @Test
    public void testPayloadServiceWithPayloadId() throws Exception {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithPayloadId.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        assertThat(repository.getActivePartnerAgreements().get(0).getPayloadService(),notNullValue());
        assertThat(repository.getActivePartnerAgreements().get(0).getPayloadService().getPayloadId(),equalTo("attachment1234@jentrata.org"));
        assertThat(repository.getActivePartnerAgreements().get(0).getPayloadService().getCompressionType(), equalTo(PayloadService.CompressionType.NONE));

    }

    @Test
    public void testFindByMessageWithDefaultMatches() throws Exception {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithSecurity.json"));
        repository.init();

        Document ebmsMessage = EbmsUtils.toXML(EbmsUtils.toStringFromClasspath("sample-ebms-user-message.xml"));

        PartnerAgreement agreement1 = repository.findByMessage(ebmsMessage,EbmsConstants.EBMS_V3);
        assertThat(agreement1,notNullValue());
        assertThat(agreement1.getCpaId(),equalTo("testCPAId1"));
    }

    @Test
    public void testFindByMessageWithServiceIdentifier() throws Exception {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithIdentifier.json"));
        repository.init();

        Document ebmsMessage = EbmsUtils.toXML(EbmsUtils.toStringFromClasspath("sample-ebms-user-message.xml"));

        PartnerAgreement agreement1 = repository.findByMessage(ebmsMessage,EbmsConstants.EBMS_V3);
        assertThat(agreement1,notNullValue());
        assertThat(agreement1.getCpaId(),equalTo("testCPAId2"));
    }

    protected static File fileFromClasspath(String filename) {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
        return file;
    }
}
