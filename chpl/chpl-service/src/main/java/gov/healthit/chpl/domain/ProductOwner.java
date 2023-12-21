package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import gov.healthit.chpl.util.LocalDateDeserializer;
import gov.healthit.chpl.util.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOwner implements Serializable {
    private static final long serialVersionUID = 5678373560374145870L;

    @Schema(description = "Product owner internal ID")
    private Long id;

    @Schema(description = "Developer that either owns or used to own a given product.")
    private Developer developer;

    @Schema(description = "")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate transferDay;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((developer == null) ? 0 : developer.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((transferDay == null) ? 0 : transferDay.hashCode());
        return result;
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
        ProductOwner other = (ProductOwner) obj;
        if (developer == null) {
            if (other.developer != null) {
                return false;
            }
        } else if (!developer.getId().equals(other.developer.getId())) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (transferDay == null) {
            if (other.transferDay != null) {
                return false;
            }
        } else if (!transferDay.equals(other.transferDay)) {
            return false;
        }
        return true;
    }

}
