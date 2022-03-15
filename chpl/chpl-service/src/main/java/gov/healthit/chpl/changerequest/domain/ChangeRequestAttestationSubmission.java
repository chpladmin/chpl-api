package gov.healthit.chpl.changerequest.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.AttestationSubmittedResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangeRequestAttestationSubmission implements Serializable, ChangeRequestDetails {
    private static final long serialVersionUID = 2150025150434933303L;

    private Long id;
    private String signature;
    private AttestationPeriod attestationPeriod;
    @Singular
    private List<AttestationSubmittedResponse> attestationResponses;
    private String signatureEmail;

    public static ChangeRequestAttestationSubmission cast(Object obj) {
        if (obj instanceof ChangeRequestAttestationSubmission) {
            return (ChangeRequestAttestationSubmission) obj;
        } else {
            throw new RuntimeException("Could not cast object as type ChangeRequestAttestationSubmission");
        }
    }

    @Override
    public boolean matches(Object obj) {
        if (obj == null || !(obj instanceof ChangeRequestAttestationSubmission)) {
            return false;
        }
        //not able to use generated "equals" method here because if the attestation responses
        //submitted have the same answers but a different ordering, that is not considered equal
        //by the generated equals method.
        ChangeRequestAttestationSubmission other = (ChangeRequestAttestationSubmission) obj;
        return Objects.equals(this.id, other.id)
                && Objects.equals(this.signature, other.signature)
                && Objects.equals(this.attestationPeriod, other.attestationPeriod)
                && Objects.equals(this.signatureEmail, other.signatureEmail)
                && isEqualIgnoringOrder(this.attestationResponses, other.attestationResponses);
    }

    private boolean isEqualIgnoringOrder(List<AttestationSubmittedResponse> list1, List<AttestationSubmittedResponse> list2) {
        if ((CollectionUtils.isEmpty(list1) && !CollectionUtils.isEmpty(list2))
                || (!CollectionUtils.isEmpty(list1) && CollectionUtils.isEmpty(list2))) {
            return false;
        } else if (!CollectionUtils.isEmpty(list1) && !CollectionUtils.isEmpty(list2)) {
            return subtractListsOfAttestationResponses(list1, list2).size() == 0
                    && subtractListsOfAttestationResponses(list2, list1).size() == 0;
        }

        //both lists are empty
        return true;
    }

    private List<AttestationSubmittedResponse> subtractListsOfAttestationResponses(List<AttestationSubmittedResponse> listA, List<AttestationSubmittedResponse> listB) {
        Predicate<AttestationSubmittedResponse> notInListB = responseFromA -> !listB.stream()
                .anyMatch(responseFromB -> responseFromA.equals(responseFromB));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ChangeRequestAttestationSubmission other = (ChangeRequestAttestationSubmission) obj;

        return Objects.equals(attestationPeriod, other.attestationPeriod)
                && CollectionUtils.isEqualCollection(attestationResponses, other.getAttestationResponses())
                && Objects.equals(id, other.id)
                && Objects.equals(signature, other.signature)
                && Objects.equals(signatureEmail, other.signatureEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attestationPeriod, attestationResponses, id, signature, signatureEmail);
    }


}
