package org.ohdsi.webapi.generationcache;

import org.ohdsi.webapi.source.Source;

public interface GenerationCacheProvider {

    boolean supports(CacheableGenerationType type);
    String getDesignHash(String design);
    Integer getNextResultIdentifier(Source source);
    String getResultsChecksum(Source source, Integer resultIdentifier);
    String getResultsSql(Integer resultIdentifier);
}
