package org.elinker.core.rest;

import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.exception.BadRequestException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.elinker.core.common.FremeActionMode.MODE_CLASSIFY;
import static org.elinker.core.common.FremeActionMode.MODE_LINK;
import static org.elinker.core.common.FremeActionMode.MODE_SPOT;
import static org.elinker.core.common.Symbols.COMMA;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class FremeRequest {

    @NotNull
    private String language;

    @NotNull
    private String dataset;

    @NotNull
    private String mode;

    private String acceptHeader;

    private String contentTypeHeader;

    private Integer numLinksParam = 1;

    private String enrichementType;

    private String domain;

    private String types;

    private String datasetKey;

    private String postBody;

    private String linkingMethod;

    private Map<String, String> allParams;

    private String plainText;

    private String prefix;

    private String nifVersion;


    public String outputFormat() {
        return allParams.getOrDefault("outformat", RDFConstants.TURTLE );
    }


    public List<String> datasets() {
        return Arrays.asList(dataset.split(COMMA));
    }

    private List<String> rMode() {
        List<String> rMode = new ArrayList<>();

        if (rMode.isEmpty() || rMode.contains("all")) {
            rMode.clear();
            rMode.add(MODE_SPOT);
            rMode.add(MODE_CLASSIFY);
            rMode.add(MODE_LINK);
        }

        if (rMode.contains(MODE_CLASSIFY) && !rMode.contains(MODE_SPOT)) {
            throw new BadRequestException(
                    "Unsupported mode combination: classification must be performed in combination with spotting.");
        }

        return rMode;
    }

    public Boolean isSpotLinkClassify() {
        return isSpotClassify() && isLink();
    }

    public Boolean isSpotClassify() {
        return isSpot() && isClassify();
    }

    public Boolean isSpotLink() {
        return isSpot() && isLink();
    }

    public Boolean isClassify() {
        List<String> rMode = rMode();
        return rMode.contains(MODE_CLASSIFY);
    }

    public Boolean isSpot() {
        List<String> rMode = rMode();
        return rMode.contains(MODE_SPOT);
    }

    public Boolean isLink() {
        List<String> rMode = rMode();
        return rMode.contains(MODE_LINK);
    }

}