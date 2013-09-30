package org.jentrata.ebms.cpa.internal;

import com.google.common.collect.ImmutableMap;
import org.hamcrest.Matchers;
import org.jentrata.ebms.EbmsConstants;
import org.jentrata.ebms.cpa.PartnerAgreement;
import org.jentrata.ebms.cpa.ValidationPredicate;
import org.jentrata.ebms.cpa.pmode.PayloadService;
import org.jentrata.ebms.cpa.pmode.ReceptionAwareness;
import org.jentrata.ebms.cpa.pmode.Security;
import org.jentrata.ebms.cpa.pmode.SecurityToken;
import org.jentrata.ebms.cpa.pmode.Signature;
import org.jentrata.ebms.cpa.pmode.UsernameToken;
import org.jentrata.ebms.cpa.validation.XPathConstantPredicate;
import org.jentrata.ebms.cpa.validation.XPathPredicate;
import org.jentrata.ebms.cpa.validation.XPathRegexPredicate;
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
        assertThat(repository.getActivePartnerAgreements().get(0).getProtocol().getAddress(),equalTo("http://example.jentrata.org"));
        assertThat(repository.getActivePartnerAgreements().get(0).isActive(),is(true));
    }

    @Test
    public void testLoadMultipleAgreements() throws IOException {

        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("multipleAgreements.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements().size(),equalTo(2));

        assertThat(repository.getActivePartnerAgreements().get(0).getCpaId(),equalTo("testCPAId1"));
        assertThat(repository.getActivePartnerAgreements().get(0).getProtocol().getAddress(),equalTo("http://example.jentrata.org"));
        assertThat(repository.getActivePartnerAgreements().get(0).isActive(),is(true));

        assertThat(repository.getActivePartnerAgreements().get(1).getCpaId(),equalTo("testCPAId2"));
        assertThat(repository.getActivePartnerAgreements().get(1).getProtocol().getAddress(),equalTo("http://example2.jentrata.org"));
        assertThat(repository.getActivePartnerAgreements().get(1).isActive(),is(true));
    }

    @Test
    public void testLoadMultipleAgreementsWithOneActive() throws IOException {

        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("multipleAgreementsOneActive.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements().size(),equalTo(1));

        assertThat(repository.getActivePartnerAgreements().get(0).getCpaId(),equalTo("testCPAId2"));
        assertThat(repository.getActivePartnerAgreements().get(0).getProtocol().getAddress(),equalTo("http://example2.jentrata.org"));
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
        assertThat(partnerAgreements.get(0).getProtocol().getAddress(),equalTo("http://example.jentrata.org"));
        assertThat(partnerAgreements.get(0).isActive(),is(false));

        assertThat(partnerAgreements.get(1).getCpaId(),equalTo("testCPAId2"));
        assertThat(partnerAgreements.get(1).getProtocol().getAddress(),equalTo("http://example2.jentrata.org"));
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

        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().getSendReceiptReplyPattern(),is(Security.ReplyPatternType.Response));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().isSendReceipt(),is(false));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().isSendReceiptNonRepudiation(),is(true));

    }

    @Test
    public void testAgreementWithMinimalSecurity() throws IOException {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithMinimalSecurity.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().getSendReceiptReplyPattern(),is(Security.ReplyPatternType.Callback));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().isSendReceipt(),is(true));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().isSendReceiptNonRepudiation(),is(false));

    }

    @Test
    public void testAgreementWithSignatureSecurity() throws IOException {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithSignatureSecurity.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().getSendReceiptReplyPattern(),is(Security.ReplyPatternType.Response));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().isSendReceipt(),is(false));
        assertThat(repository.getActivePartnerAgreements().get(0).getSecurity().isSendReceiptNonRepudiation(),is(true));

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
        assertThat(repository.getActivePartnerAgreements().get(0).hasInitiatorSecurityToken(),is(false));
        assertThat(repository.getActivePartnerAgreements().get(0).hasResponderSecurityToken(),is(false));
    }

    @Test
    public void testIsValidPartnerAgreement() throws IOException {

        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithServices.json"));
        repository.init();

        Map<String,Object> fields = new ImmutableMap.Builder<String, Object>()
                .put(EbmsConstants.CPA, repository.getActivePartnerAgreements().get(0))
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
        assertThat(repository.getActivePartnerAgreements().get(0).getPayloadProfile(null),notNullValue());
        assertThat(repository.getActivePartnerAgreements().get(0).getPayloadProfile(null).getPayloadId(),notNullValue());
        assertThat(repository.getActivePartnerAgreements().get(0).getPayloadProfile(null).getCompressionType(), equalTo(PayloadService.CompressionType.NONE));

    }

    @Test
    public void testPayloadServiceWithGZipCompression() throws Exception {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithGzipCompression.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        assertThat(repository.getActivePartnerAgreements().get(0).getBusinessInfo().getPayloadProfile().get(0),notNullValue());
        assertThat(repository.getActivePartnerAgreements().get(0).getBusinessInfo().getPayloadProfile().get(0).getPayloadId(),nullValue());
        assertThat(repository.getActivePartnerAgreements().get(0).getBusinessInfo().getPayloadProfile().get(0).getCompressionType(), equalTo(PayloadService.CompressionType.GZIP));

    }

    @Test
    public void testPayloadServiceWithPayloadId() throws Exception {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithPayloadId.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        assertThat(repository.getActivePartnerAgreements().get(0).getBusinessInfo().getPayloadProfile().get(0),notNullValue());
        assertThat(repository.getActivePartnerAgreements().get(0).getBusinessInfo().getPayloadProfile().get(0).getPayloadId(),equalTo("attachment1234@jentrata.org"));
        assertThat(repository.getActivePartnerAgreements().get(0).getBusinessInfo().getPayloadProfile().get(0).getCompressionType(), equalTo(PayloadService.CompressionType.NONE));

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
        assertThat(agreement1.getAgreementRef(), equalTo("test123"));
    }

    @Test
    public void testDefaultReceptionAwareness() throws Exception {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithReceptionAwareness.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        PartnerAgreement agreement = repository.getActivePartnerAgreements().get(0);
        assertThat(agreement.getReceptionAwareness(),notNullValue());
        assertThat(agreement.getReceptionAwareness().isDuplicateDetectionEnabled(),is(false));
        assertThat(agreement.getReceptionAwareness().isRetryEnabled(), is(false));

    }

    @Test
    public void testCustomReceptionAwareness() throws Exception {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithCustomReceptionAwareness.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        ReceptionAwareness receptionAwareness = repository.getActivePartnerAgreements().get(0).getReceptionAwareness();
        assertThat(receptionAwareness,notNullValue());
        assertThat(receptionAwareness.isDuplicateDetectionEnabled(),is(true));
        assertThat(receptionAwareness.isRetryEnabled(), is(true));
        assertThat((Integer) receptionAwareness.getRetry().get("maxretries"), equalTo(10));
        assertThat((Integer) receptionAwareness.getRetry().get("period"), equalTo(3000));
        assertThat((String) receptionAwareness.getDuplicateDetection().get("checkwindow"), equalTo("7D"));

    }

    @Test
    public void testXpathValidations() throws Exception {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithXpathValidations.json"));
        repository.init();
        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        List<ValidationPredicate> validations = repository.getActivePartnerAgreements().get(0).getService("service1","action1").getValidations();
        assertThat(validations,hasSize(1));
        assertThat(validations.get(0),instanceOf(XPathPredicate.class));
        XPathPredicate predicate = (XPathPredicate) validations.get(0);
        assertThat(predicate.getName(), equalTo("AgreementRef"));
        assertThat(predicate.getExpression(), equalTo("//eb3:AgreementRef[text()='http://agreement1234']"));
    }

    @Test
    public void testMultipleXpathValidations() throws Exception {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithMultipleXpathValidations.json"));
        repository.init();
        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        List<ValidationPredicate> validations = repository.getActivePartnerAgreements().get(0).getService("service1","action1").getValidations();
        assertThat(validations,hasSize(3));
        assertThat(validations.get(0),instanceOf(XPathPredicate.class));
        XPathPredicate predicate = (XPathPredicate) validations.get(0);
        assertThat(predicate.getName(), Matchers.equalTo("AgreementRef"));
        assertThat(predicate.getExpression(), equalTo("//eb3:AgreementRef[text()='http://agreement1234']"));

        XPathConstantPredicate constantPredicate = (XPathConstantPredicate) validations.get(1);
        assertThat(constantPredicate.getName(), Matchers.equalTo("ConversationId"));
        assertThat(constantPredicate.getExpression(), equalTo("//eb3:ConversationId/text()"));
        assertThat(constantPredicate.getValue(), equalTo("test"));

        XPathRegexPredicate regexPredicate = (XPathRegexPredicate) validations.get(2);
        assertThat(regexPredicate.getName(), Matchers.equalTo("MessageId"));
        assertThat(regexPredicate.getExpression(), equalTo("//eb3:MessageId/text()"));
        assertThat(regexPredicate.getRegex(), equalTo("^\\d{10}$"));
    }

    @Test
    public void testInitiatorAndResponder() throws Exception {
        JSONCPARepository repository = new JSONCPARepository();
        repository.setCpaJsonFile(fileFromClasspath("agreementWithInitiatorAndResponder.json"));
        repository.init();

        assertThat(repository.getActivePartnerAgreements(),hasSize(1));
        PartnerAgreement agreement = repository.getActivePartnerAgreements().get(0);
        assertThat(agreement.getInitiator(),notNullValue());
        assertThat(agreement.getInitiator().getPartyId(), equalTo("1234567890"));
        assertThat(agreement.getInitiator().getPartyIdType(), equalTo("urn:oasis:names:tc:ebcore:partyid-type:iso6523:0088"));
        UsernameToken authorization = (UsernameToken) agreement.getInitiator().getAuthorization();
        assertThat(authorization.getTokenType(), containsString("UsernameToken"));
        assertThat(authorization.getUsername(), equalTo("jentrata"));
        assertThat(authorization.getPassword(), equalTo("gocDv4SEXRDxNjucDDfo7I7ACTc="));

        assertThat(agreement.getResponder(),notNullValue());
        assertThat(agreement.getResponder().getPartyId(), equalTo("0987654321"));
        assertThat(agreement.getResponder().getPartyIdType(), equalTo("urn:oasis:names:tc:ebcore:partyid-type:iso6523:0088"));
        authorization = (UsernameToken) agreement.getResponder().getAuthorization();
        assertThat(authorization.getTokenType(), containsString("UsernameToken"));
        assertThat(authorization.getUsername(), equalTo("other"));
        assertThat(authorization.getPassword(), equalTo("otherpassword"));


    }

    protected static File fileFromClasspath(String filename) {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource(filename).getFile());
        return file;
    }
}
