package edu.cit.garbo.pawnscan.features.reports.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import edu.cit.garbo.pawnscan.shared.validation.SerialNumberValidator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportUpsertRequest {

    @NotBlank(message = "Serial number is required")
    @Size(max = 255, message = "Serial number must not exceed 255 characters")
    @Pattern(
            regexp = SerialNumberValidator.ALLOWED_CHARACTERS_PATTERN,
            message = SerialNumberValidator.ALLOWED_CHARACTERS_MESSAGE
    )
    @JsonAlias({"serial_number", "serialNumber"})
    private String serialNumber;

    @NotBlank(message = "Item model is required")
    @Size(max = 255, message = "Item model must not exceed 255 characters")
    @JsonAlias({"item_model", "itemModel"})
    private String itemModel;

    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private List<MultipartFile> files;
}







