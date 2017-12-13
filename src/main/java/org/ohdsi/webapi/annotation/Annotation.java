/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author alondhe2
 */
public class Annotation
{
    @JsonProperty("metadata_id")
    public Integer metadataId;
    
    @JsonProperty("metadata_concept_id")
    public Integer metadataConceptId;
    
    @JsonProperty("metadata_type_concept_id")
    public String metadataTypeConceptId;
    
    @JsonProperty("metadata_name")
    public String metadataName;
    
    @JsonProperty("value_as_concept_id")
    public Integer valueAsConceptId;
    
    @JsonProperty("value_as_string")
    public String valueAsString;
    
    @JsonProperty("value_as_value")
    public Float valueAsValue;
    
    @JsonProperty("metadata_date")
    public String metadataDate;   
    
    @JsonProperty("metadata_datetime")
    public String metadataDateTime;
}
