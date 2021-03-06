databaseChangeLog = {

    // nightlyDatabaseCleanups can be de-/activated by REST-Call
    changeSet(author: "bka (generated)", id: "1452546683118-1") {
        addColumn(tableName: "api_key") {
            column(name: "allowed_for_nightly_database_cleanup_activation", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-1") {
        createTable(tableName: "csi_system") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "csi_systemPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "label", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-2") {
        createTable(tableName: "csi_system_job_group_weight") {
            column(name: "csi_system_job_group_weights_id", type: "bigint")

            column(name: "job_group_weight_id", type: "bigint")

            column(name: "job_group_weights_idx", type: "integer")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-3") {
        createTable(tableName: "job_group_weight") {
            column(autoIncrement: "true", name: "id", type: "bigint") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "job_group_weiPK")
            }

            column(name: "version", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "job_group_id", type: "bigint") {
                constraints(nullable: "false")
            }

            column(name: "weight", type: "double precision") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-6") {
        createIndex(indexName: "label_uniq_1454506330929", tableName: "csi_system", unique: "true") {
            column(name: "label")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-7") {
        createIndex(indexName: "FK588975446326A28C", tableName: "csi_system_job_group_weight") {
            column(name: "job_group_weight_id")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-8") {
        createIndex(indexName: "FK5044557A48E56BA7", tableName: "job_group_weight") {
            column(name: "job_group_id")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-4") {
        addForeignKeyConstraint(baseColumnNames: "job_group_weight_id", baseTableName: "csi_system_job_group_weight", constraintName: "FK588975446326A28C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group_weight", referencesUniqueColumn: "false")
    }

    changeSet(author: "marcus (generated)", id: "1454506331291-5") {
        addForeignKeyConstraint(baseColumnNames: "job_group_id", baseTableName: "job_group_weight", constraintName: "FK5044557A48E56BA7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "job_group", referencesUniqueColumn: "false")
    }

    // [BEGIN] changes for custom event result dashboard /////////////////////////////////////////////////////////////////////
    changeSet(author: "bwo (generated)", id: "1453899727017-1") {
        addColumn(tableName: "userspecific_event_result_dashboard") {
            column(name: "chart_title", type: "varchar(255)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-2") {
        addColumn(tableName: "userspecific_event_result_dashboard") {
            column(name: "chart_width", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-3") {
        addColumn(tableName: "userspecific_event_result_dashboard") {
            column(name: "chart_height", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-4") {
        addColumn(tableName: "userspecific_event_result_dashboard") {
            column(name: "load_time_maximum", type: "varchar(255)") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-5") {
        addColumn(tableName: "userspecific_event_result_dashboard") {
            column(name: "load_time_minimum", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-6") {
        addColumn(tableName: "userspecific_event_result_dashboard") {
            column(name: "show_data_labels", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-7") {
        addColumn(tableName: "userspecific_event_result_dashboard") {
            column(name: "show_data_markers", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-8") {
        modifyDataType(columnName: "weight", newDataType: "double precision", tableName: "page")
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-9") {
        dropNotNullConstraint(columnDataType: "double precision", columnName: "weight", tableName: "page")
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-10") {
        preConditions (onFail: 'MARK_RAN'){
            indexExists(indexName: 'full_hour_uniq_1447946152077')
        }
        dropIndex(indexName: "full_hour_uniq_1447946152077", tableName: "hour_of_day")
    }

    changeSet(author: "bwo (generated)", id: "1453899727017-11") {
        preConditions (onFail: 'MARK_RAN'){
            indexExists(indexName: 'full_hour')
        }
        dropIndex(indexName: "full_hour", tableName: "hour_of_day")
    }

    // [END] changes for custom event result dashboard /////////////////////////////////////////////////////////////////////

    // [BEGIN] changes for custom csi dashboard /////////////////////////////////////////////////////////////////////

    changeSet(author: "bwo (generated)", id: "1454590842460-1") {
        addColumn(tableName: "userspecific_csi_dashboard") {
            column(name: "chart_height", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1454590842460-2") {
        addColumn(tableName: "userspecific_csi_dashboard") {
            column(name: "chart_title", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1454590842460-3") {
        addColumn(tableName: "userspecific_csi_dashboard") {
            column(name: "chart_width", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1454590842460-4") {
        addColumn(tableName: "userspecific_csi_dashboard") {
            column(name: "load_time_maximum", type: "varchar(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1454590842460-5") {
        addColumn(tableName: "userspecific_csi_dashboard") {
            column(name: "load_time_minimum", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1454590842460-6") {
        addColumn(tableName: "userspecific_csi_dashboard") {
            column(name: "show_data_labels", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "bwo (generated)", id: "1454590842460-7") {
        addColumn(tableName: "userspecific_csi_dashboard") {
            column(name: "show_data_markers", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    // [END] changes for custom csi dashboard /////////////////////////////////////////////////////////////////////

    // add initial chart width to OsmConfiguration
    changeSet(author: "bwo (generated)", id: "1454675900207-1") {
        addColumn(tableName: "osm_configuration") {
            column(defaultValue: "1000", name: "initial_chart_width_in_pixels", type: "integer") {
                constraints(nullable: "false")
            }
        }
    }

//    ### add csiSystem to CsiAggregation
    changeSet(author: "marcus (generated)", id: "1454929626682-1") {
        addColumn(tableName: "measured_value") {
            column(name: "csi_system_id", type: "bigint")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454929626682-3") {
        createIndex(indexName: "FKCC54EB8B8A7ADFB", tableName: "measured_value") {
            column(name: "csi_system_id")
        }
    }

    changeSet(author: "marcus (generated)", id: "1454929626682-2") {
        addForeignKeyConstraint(baseColumnNames: "csi_system_id", baseTableName: "measured_value", constraintName: "FKCC54EB8B8A7ADFB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "csi_system", referencesUniqueColumn: "false")
    }
//    ### end add csiSystem to CsiAggregation
    // START [IT-728] add csByVisuallyComplete
    changeSet(author: "bka", id: "1455031326389-1") {
        renameTable(oldTableName: "measured_value", newTableName: "csi_aggregation")
    }

    changeSet(author: "birger (generated)", id: "1455031326389-2") {
        createTable(tableName: "csi_aggregation_event_result") {
            column(name: "csi_aggregation_underlying_event_results_by_visually_complete_id", type: "bigint")

            column(name: "event_result_id", type: "bigint")
        }
    }

    changeSet(author: "birger (generated)", id: "1455031326389-3") {
        addColumn(tableName: "event_result") {
            column(name: "cs_by_wpt_visually_complete_in_percent", type: "double precision")
        }
    }

    changeSet(author: "birger (generated)", id: "1455031326389-4") {
        addColumn(tableName: "csi_aggregation") {
            column(name: "cs_by_wpt_visually_complete_in_percent", type: "double precision")
        }
    }

    changeSet(author: "birger (generated)", id: "1455031326389-5") {
        addForeignKeyConstraint(baseColumnNames: "event_result_id", baseTableName: "csi_aggregation_event_result", constraintName: "FK4579D789CA94DFB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "event_result", referencesUniqueColumn: "false")
    }

    changeSet(author: "birger (generated)", id: "1455031326389-6") {
        addForeignKeyConstraint(baseColumnNames: "csi_aggregation_underlying_event_results_by_visually_complete_id", baseTableName: "csi_aggregation_event_result", constraintName: "FK4579D78978BC1A0C", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "csi_aggregation", referencesUniqueColumn: "false")
    }

    changeSet(author: "bka", id: "1455031326389-9") {
        renameColumn(tableName: "event_result", oldColumnName: "customer_satisfaction_in_percent", newColumnName: "cs_by_wpt_doc_complete_in_percent", columnDataType: "double precision")
    }

    changeSet(author: "bka", id: "1455031326389-10") {
        renameColumn(tableName: "csi_aggregation", oldColumnName: "value", newColumnName: "cs_by_wpt_doc_complete_in_percent", columnDataType: "double precision")
    }

    changeSet(author: "bka", id: "1455031326389-11") {
        renameColumn(tableName: "csi_aggregation", oldColumnName: "result_ids", newColumnName: "underlying_event_results_by_wpt_doc_complete", columnDataType: "longtext")
    }
    changeSet(author: "bka", id: "1455031326389-12") {
        addNotNullConstraint(columnDataType: "longtext", columnName: "underlying_event_results_by_wpt_doc_complete", tableName: "csi_aggregation")
    }
    // END [IT-728] add csByVisuallyComplete
    // START [IT-885] rename *MeasuredValue* to *CsiAggregation*
    changeSet(author: "bka", id: "1455031326389-13") {
        renameTable(oldTableName: "measured_value_interval", newTableName: "csi_aggregation_interval")
    }
    changeSet(author: "bka", id: "1455031326389-14") {
        renameTable(oldTableName: "measured_value_update_event", newTableName: "csi_aggregation_update_event")
    }
    changeSet(author: "bka", id: "1455031326389-15") {
        renameColumn(tableName: "csi_aggregation_update_event", oldColumnName: "measured_value_id", newColumnName: "csi_aggregation_id", columnDataType: "bigint")
    }
    changeSet(author: "bka", id: "1455031326389-16") {
        dropIndex(indexName: "measuredValueId_idx", tableName: "csi_aggregation_update_event")
    }
    changeSet(author: "bka", id: "1455031326389-17") {
        createIndex(indexName: "csiAggregationId_idx", tableName: "csi_aggregation_update_event") {
            column(name: "csi_aggregation_id")
        }
    }
    changeSet(author: "bka", id: "1455031326389-18") {
        addNotNullConstraint(columnDataType: "bigint", columnName: "csi_aggregation_id", tableName: "csi_aggregation_update_event")
    }
    // END [IT-885] rename *MeasuredValue* to *CsiAggregation*

    // START create bidirectional connection CsiSystem <-> JobGroupWeight
    changeSet(author: "birger (generated)", id: "1455561393941-1") {
        addColumn(tableName: "job_group_weight") {
            column(name: "csi_system_id", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "birger (generated)", id: "1455561393941-2") {
        dropForeignKeyConstraint(baseTableName: "csi_system_job_group_weight", constraintName: "FK588975446326A28C")
    }

    changeSet(author: "birger (generated)", id: "1455561393941-4") {
        createIndex(indexName: "FK5044557AB8A7ADFB", tableName: "job_group_weight") {
            column(name: "csi_system_id")
        }
    }

    changeSet(author: "birger (generated)", id: "1455561393941-5") {
        dropTable(tableName: "csi_system_job_group_weight")
    }

    changeSet(author: "birger (generated)", id: "1455561393941-3") {
        addForeignKeyConstraint(baseColumnNames: "csi_system_id", baseTableName: "job_group_weight", constraintName: "FK5044557AB8A7ADFB", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "csi_system", referencesUniqueColumn: "false")
    }
    // END create bidirectional connection CsiSystem <-> JobGroupWeight

    // BEGIN [IT-879] delete columns *.idx
    changeSet(author: "marcus (generated)", id: "1455551294234-2") {
        createIndex(indexName: "FKED13118372634BEF", tableName: "script_page") {
            column(name: "script_tested_pages_id")
        }
    }

    changeSet(author: "marcus (generated)", id: "1455551294234-3") {
        dropColumn(columnName: "tested_pages_idx", tableName: "script_page")
    }

    changeSet(author: "marcus (generated)", id: "1455551294234-1") {
        addForeignKeyConstraint(baseColumnNames: "script_tested_pages_id", baseTableName: "script_page", constraintName: "FKED13118372634BEF", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "script", referencesUniqueColumn: "false")
    }

    changeSet(author: "marcus (generated)", id: "1455697784147-4") {
        createIndex(indexName: "FKA5D3DA68CFECF0", tableName: "csi_configuration_browser_connectivity_weight") {
            column(name: "csi_configuration_browser_connectivity_weights_id")
        }
    }

    changeSet(author: "marcus (generated)", id: "1455697784147-5") {
        createIndex(indexName: "FKCDAE2599F1CB472F", tableName: "csi_configuration_page_weight") {
            column(name: "csi_configuration_page_weights_id")
        }
    }

    changeSet(author: "marcus (generated)", id: "1455697784147-6") {
        createIndex(indexName: "FK7F5A89C070CB3FD6", tableName: "csi_configuration_time_to_cs_mapping") {
            column(name: "csi_configuration_time_to_cs_mappings_id")
        }
    }

    changeSet(author: "marcus (generated)", id: "1455697784147-7") {
        dropColumn(columnName: "browser_connectivity_weights_idx", tableName: "csi_configuration_browser_connectivity_weight")
    }

    changeSet(author: "marcus (generated)", id: "1455697784147-8") {
        dropColumn(columnName: "page_weights_idx", tableName: "csi_configuration_page_weight")
    }

    changeSet(author: "marcus (generated)", id: "1455697784147-9") {
        dropColumn(columnName: "time_to_cs_mappings_idx", tableName: "csi_configuration_time_to_cs_mapping")
    }

    changeSet(author: "marcus (generated)", id: "1455697784147-1") {
        addForeignKeyConstraint(baseColumnNames: "csi_configuration_browser_connectivity_weights_id", baseTableName: "csi_configuration_browser_connectivity_weight", constraintName: "FKA5D3DA68CFECF0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "csi_configuration", referencesUniqueColumn: "false")
    }

    changeSet(author: "marcus (generated)", id: "1455697784147-2") {
        addForeignKeyConstraint(baseColumnNames: "csi_configuration_page_weights_id", baseTableName: "csi_configuration_page_weight", constraintName: "FKCDAE2599F1CB472F", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "csi_configuration", referencesUniqueColumn: "false")
    }

    changeSet(author: "marcus (generated)", id: "1455697784147-3") {
        addForeignKeyConstraint(baseColumnNames: "csi_configuration_time_to_cs_mappings_id", baseTableName: "csi_configuration_time_to_cs_mapping", constraintName: "FK7F5A89C070CB3FD6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "csi_configuration", referencesUniqueColumn: "false")
    }
    // END [IT-879] delete columns *.idx
    changeSet(author: "bwo", id: "1455722758000-1") {
        comment {'https://jira.grails.org/browse/GPDATABASEMIGRATION-16'}
        renameColumn(tableName: "user", oldColumnName: "password", newColumnName: "passwd", columnDataType: "varchar(255)")
    }
}