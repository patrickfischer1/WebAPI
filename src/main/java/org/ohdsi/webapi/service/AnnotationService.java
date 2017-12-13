/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.service;

import static java.lang.Character.toLowerCase;
import java.sql.ResultSet;
import java.util.Collection;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.ohdsi.circe.helper.ResourceHelper;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.annotation.Annotation;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * REST Web Service
 *
 * @author alondhe2
 */
@Path("/annotation/")
@Component
public class AnnotationService extends AbstractDaoService
{    
    private final RowMapper<Annotation> idRowMapper = (final ResultSet resultSet, final int arg1) -> {
        final Annotation annotation = new Annotation();
        annotation.metadataId = resultSet.getInt("metadataId");
        return annotation;
    };
    
    
    private final RowMapper<Annotation> rowMapper = (final ResultSet resultSet, final int arg1) -> {
        final Annotation annotation = new Annotation();
        
        annotation.metadataId = resultSet.getInt("metadata_id");
        annotation.metadataConceptId = resultSet.getInt("metadata_concept_id");
        annotation.metadataTypeConceptId = resultSet.getString("metadata_type_concept_id");
        annotation.metadataName = resultSet.getString("metadata_name");
        annotation.valueAsConceptId = resultSet.getInt("value_as_concept_id");
        annotation.valueAsString = resultSet.getString("value_as_string");
        annotation.valueAsValue = resultSet.getFloat("value_as_value");
        annotation.metadataDate = resultSet.getString("metadata_date");
        annotation.metadataDateTime = resultSet.getString("metadata_datetime");
        
        return annotation;
    };
    
    @GET
    @Path("{sourceKey}/get/{typeConceptId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Annotation> GetAnnotationsByType(
            @PathParam("sourceKey") String sourceKey, 
            @PathParam("typeConceptId") String typeConceptId)
    {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        if (!checkTableExists(source)) {
            return null;
        }
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String sql = String.format("select * from @resultsDatabaseSchema.metadata where metadata_type_concept_id = %s;", typeConceptId);
              
        sql = SqlRender.renderSql(sql, new String[] {"resultsDatabaseSchema"}, new String[] {tableQualifier});
        sql = SqlTranslate.translateSql(sql, source.getSourceDialect());
        
        return getSourceJdbcTemplate(source).query(sql, this.rowMapper);
    }
   
    @GET
    @Path("{sourceKey}/get/conceptId/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Annotation> GetAnnotationByConceptId(
            @PathParam("sourceKey") String sourceKey, 
            @PathParam("id") Integer conceptId)
    {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        if (!checkTableExists(source)) {
            return null;
        }
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String sql = "select * from @resultsDatabaseSchema.metadata where metadata_concept_id = tolower(%s);";
        sql = String.format(sql, toLowerCase(conceptId));
        sql = SqlRender.renderSql(sql, new String[] {"resultsDatabaseSchema"}, new String[] {tableQualifier});
        sql = SqlTranslate.translateSql(sql, source.getSourceDialect());
        
        return getSourceJdbcTemplate(source).query(sql, this.rowMapper);
    }
    
    @GET
    @Path("{sourceKey}/get/typeConceptId/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Annotation> GetAnnotationByTypeConceptId(
            @PathParam("sourceKey") String sourceKey, 
            @PathParam("id") String typeConceptId)
    {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        if (!checkTableExists(source)) {
            return null;
        }
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String sql = "select * from @resultsDatabaseSchema.metadata where metadata_type_concept_id = '%s';";
        sql = String.format(sql, typeConceptId);
        sql = SqlRender.renderSql(sql, new String[] {"resultsDatabaseSchema"}, new String[] {tableQualifier});
        sql = SqlTranslate.translateSql(sql, source.getSourceDialect());
        
        return getSourceJdbcTemplate(source).query(sql, this.rowMapper);
    }
    

    @PUT
    @Path("{sourceKey}/save")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Integer SaveAnnotation(
            @PathParam("sourceKey") String sourceKey, 
            Annotation item) throws DataAccessException
    {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        if (!checkTableExists(source)) {
            return -1;
        }
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        
        String maxIdSql = SqlRender.renderSql("SELECT ROW_NUMBER() OVER (ORDER BY (SELECT 1)) + (SELECT MAX(metadata_id) from @resultsDatabaseSchema.metadata);", 
                new String[] { "resultsDatabaseSchema" }, new String[] { tableQualifier });
        maxIdSql = SqlTranslate.translateSql(maxIdSql, source.getSourceDialect());
        
        int newId = getSourceJdbcTemplate(source).queryForObject(maxIdSql, new Object[] { }, Integer.class ) + 1;
                
        String insertSql = String.format("insert into @resultsDatabaseSchema.metadata "
                + "(metadata_id, metadata_concept_id, metadata_type_concept_id, metadata_name, value_as_concept_id, "
                + "value_as_string, value_as_value, metadata_date) "
                + "values (%1d, %2d, '%3s', '%4s', %5d, '%6s', %7f, '%8s');", 
                newId, item.metadataConceptId, item.metadataTypeConceptId, item.metadataName, item.valueAsConceptId, item.valueAsString, 
                item.valueAsValue, item.metadataDate);
        insertSql = SqlRender.renderSql(insertSql, new String[] { "resultsDatabaseSchema" }, new String[] { tableQualifier });
        try {
            getSourceJdbcTemplate(source).execute(insertSql);
            return newId;
        }
        catch (DataAccessException e) {
            throw e;
        }
    }
    
    @PUT
    @Path("{sourceKey}/update/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    public Integer UpdateAnnotation(
            @PathParam("sourceKey") String sourceKey,
            @PathParam("id") Integer metadataId,
            Annotation item) throws DataAccessException
    {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        if (!checkTableExists(source)) {
            return -1;
        }
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
                
        String updateSql = String.format(
                "update @resultsDatabaseSchema.metadata "
                + "set metadata_concept_id = %1d, "
                        + "metadata_type_concept_id = '%2s', "
                        + "metadata_name = '%3s', "
                        + "value_as_concept_id = %4d, "
                        + "value_as_string = '%5s', "
                        + "value_as_value = %6f, "
                        + "metadata_date = '%7s' "
                        + "where metadata_id = %8d;", 
                item.metadataConceptId, item.metadataTypeConceptId, item.metadataName, item.valueAsConceptId, item.valueAsString, 
                item.valueAsValue, item.metadataDate, metadataId);
        updateSql = SqlRender.renderSql(updateSql, new String[] { "resultsDatabaseSchema" }, new String[] { tableQualifier });
        try {
            getSourceJdbcTemplate(source).execute(updateSql);
            return 1;
        }
        catch (DataAccessException e) {
            throw e;
        }
    }
    
    
    @GET
    @Path("{sourceKey}/delete/{metadataId}")
    public void DeleteAnnotation(
            @PathParam("sourceKey") String sourceKey, 
            @PathParam("metadataId") String metadataId) throws DataAccessException
    {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        if (!checkTableExists(source)) {
            return;
        }
        
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        
        String sql = String.format("delete from @resultsDatabaseSchema.metadata where metadata_id = %s;", metadataId);
        sql = SqlRender.renderSql(sql, new String[] { "resultsDatabaseSchema" }, new String[] { tableQualifier });
        sql = SqlTranslate.translateSql(sql, source.getSourceDialect());
        
        try
        {
            getSourceJdbcTemplate(source).execute(sql);
        }
        catch (DataAccessException e)
        {
            throw e;
        }
    }
    
    private Boolean checkTableExists(Source source) throws DataAccessException
    {
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        
        try {
            String sql = ResourceHelper.GetResourceAsString("/resources/annotations/sql/checkMetaTable.sql");
            sql = SqlRender.renderSql(sql, new String[] { "results_database_schema" }, new String[] { tableQualifier });
            sql = SqlTranslate.translateSql(sql, source.getSourceDialect());
            getSourceJdbcTemplate(source).execute(sql);
            return true;
        }
        catch (DataAccessException e) {
            return false;
        }
    }
}
