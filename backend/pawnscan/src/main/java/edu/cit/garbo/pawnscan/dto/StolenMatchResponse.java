package edu.cit.garbo.pawnscan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StolenMatchResponse {

    private String searchedSerial;
    private LocalDateTime timestamp;
    private Long matchedReportId;
    private String itemModel;
    private String description;
    private LocalDateTime dateReported;
    private String victimName;
    private String victimEmail;
    private String victimPhoneNumber;
    private String evidenceFileUrl;
    private String evidenceFileType;
}
