databaseChangeLog = {

    changeSet(author: "marko (generated)", id: "1482245814394-1") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "script_id", tableName: "job")
    }
    changeSet(author: "marko (generated)", id: "1482245814394-2") {
        sql('''
            UPDATE job SET script_id = NULL WHERE job.deleted = 1;
        ''')
    }

    changeSet(author: "marko (generated)", id: "1483441134102-6") {
        dropColumn(columnName: "valid", tableName: "location")
    }

    changeSet(author: "marko (generated)", id: "1483463294023-1") {
        createTable(tableName: "archived_script") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(primaryKey: "true", primaryKeyName: "archived_scriptPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "archive_tag", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "label", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "navigation_script", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "script_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "marko (generated)", id: "1483463294023-2") {
        addForeignKeyConstraint(baseColumnNames: "script_id", baseTableName: "archived_script", constraintName: "FK_qp572xq18h8ccjemkqgacq0x1", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "script")
    }

}
