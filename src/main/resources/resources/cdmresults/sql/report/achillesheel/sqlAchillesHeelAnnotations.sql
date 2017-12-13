SELECT
    A.analysis_id               AS attribute_name,
    A.ACHILLES_HEEL_warning     AS attribute_value,
    B.value_as_value            AS attribute_status,   
    B.value_as_string           AS attribute_comments
FROM @results_database_schema.ACHILLES_HEEL_results A
left join @results_database_schema.metadata B on A.achilles_heel_warning = B.metadata_name
    and lower(B.metadata_type_concept_id) = 'heel'
ORDER BY CASE WHEN LEFT(A.ACHILLES_HEEL_warning, 5) = 'Error' THEN 1 ELSE 2 END , A.analysis_id
;