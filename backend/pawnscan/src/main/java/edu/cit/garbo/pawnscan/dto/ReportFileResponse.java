package edu.cit.garbo.pawnscan.dto;

import edu.cit.garbo.pawnscan.entity.ReportFileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportFileResponse {

    private Long id;
    private String fileUrl;
    private ReportFileType fileType;
}