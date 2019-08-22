package org.ohdsi.webapi.generationcache;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.ohdsi.webapi.Constants.Params.GENERATION_ID;
import static org.ohdsi.webapi.Constants.Params.RESULTS_DATABASE_SCHEMA;

@Component
public class CohortGenerationCacheProvider extends AbstractDaoService implements GenerationCacheProvider {

    private static final String CACHE_VALIDATION_TIME = "Generation cache for resultIdentifier = {} has been validated in {} milliseconds";

    private static final String COHORT_CHECKSUM_SQL_PATH = "/resources/generationcache/cohort/resultsChecksum.sql";
    private static final String NEXT_ID_SQL_PATH = "/resources/generationcache/cohort/nextResultIdentifier.sql";
    private static final String COHORT_RESULTS_SQL = ResourceHelper.GetResourceAsString("/resources/generationcache/cohort/results.sql");
    private static final String CLEANUP_SQL = ResourceHelper.GetResourceAsString("/resources/generationcache/cohort/cleanup.sql");

    @Override
    public boolean supports(CacheableGenerationType type) {

        return Objects.equals(type, CacheableGenerationType.COHORT);
    }

    @Override
    public String getDesignHash(String design) {

        CohortDefinitionDetails cohortDetails = new CohortDefinitionDetails();
        cohortDetails.setExpression(design);
        return cohortDetails.calculateHashCode().toString();
    }

    @Override
    public Integer getNextResultIdentifier(Source source) {

        PreparedStatementRenderer psr = new PreparedStatementRenderer(
            source,
            NEXT_ID_SQL_PATH,
            "@" + RESULTS_DATABASE_SCHEMA,
            SourceUtils.getResultsQualifier(source)
        );
        return getSourceJdbcTemplate(source).queryForObject(psr.getSql(), psr.getOrderedParams(), Integer.class);
    }

    @Override
    public String getResultsChecksum(Source source, Integer resultIdentifier) {

        long startTime = System.currentTimeMillis();
        PreparedStatementRenderer psr = new PreparedStatementRenderer(
            source,
            COHORT_CHECKSUM_SQL_PATH,
            "@" + RESULTS_DATABASE_SCHEMA,
            SourceUtils.getResultsQualifier(source),
            GENERATION_ID,
            resultIdentifier,
            SessionUtils.sessionId()
        );
        String checksum = getSourceJdbcTemplate(source).queryForObject(psr.getSql(), psr.getOrderedParams(), String.class);
        log.info(CACHE_VALIDATION_TIME, resultIdentifier, System.currentTimeMillis() - startTime);
        return checksum;
    }

    @Override
    public String getResultsSql(Integer resultIdentifier) {

        return SqlRender.renderSql(
                COHORT_RESULTS_SQL,
                new String[]{GENERATION_ID},
                new String[]{resultIdentifier.toString()}
        );
    }

    @Override
    public void remove(GenerationCache generationCache) {

        Source source = generationCache.getSource();
        String sql = SqlRender.renderSql(
            CLEANUP_SQL,
            new String[] {RESULTS_DATABASE_SCHEMA, GENERATION_ID},
            new String[] {SourceUtils.getResultsQualifier(source), generationCache.getResultIdentifier().toString()}
        );
        sql = SqlTranslate.translateSql(sql, source.getSourceDialect());
        getSourceJdbcTemplate(source).batchUpdate(SqlSplit.splitSql(sql));
    }
}
