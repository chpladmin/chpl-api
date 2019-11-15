package gov.healthit.chpl.activity;

import gov.healthit.chpl.domain.activity.ActivityMetadata;
import gov.healthit.chpl.dto.ActivityDTO;
import org.springframework.stereotype.Component;

@Component("apiKeyManagementReportActivityMetadataBuilder")
public class ApiKeyManagementReportActivityMetadataBuilder extends ActivityMetadataBuilder{

  public ApiKeyManagementReportActivityMetadataBuilder() {
    super();
  }

  @Override
  protected void addConceptSpecificMetadata(ActivityDTO dto, ActivityMetadata metadata) {

  }
}
