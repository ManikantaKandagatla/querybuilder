package com.rzp.querybuilder.util;

import com.healthmarketscience.sqlbuilder.*;
import com.healthmarketscience.sqlbuilder.custom.postgresql.PgLimitClause;
import com.healthmarketscience.sqlbuilder.dbspec.Column;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbColumn;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSchema;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbSpec;
import com.healthmarketscience.sqlbuilder.dbspec.basic.DbTable;
import com.rzp.querybuilder.model.DataType;
import com.rzp.querybuilder.model.FieldPath;
import com.rzp.querybuilder.model.query.*;
import org.apache.commons.lang.StringUtils;

import java.util.*;

import static com.rzp.querybuilder.model.query.JoinType.*;

public class SQLTransformer {

    private final SelectQuery selectQuery;
    private final QueryMaster queryMaster;
    private final Map<String, String> tableAliasMap;
    private final Set<String> uniqueJoins;
    private Integer aliasNumber;
    private final Map<String, DbTable> tableObjMap;
    private final DbSchema schema;
    private boolean fromTableIsSet;
    private Integer fieldAliasIndex;


    public SQLTransformer(QueryMaster queryMaster) {
        this.selectQuery = new SelectQuery();
        this.queryMaster = queryMaster;
        this.aliasNumber = 1;
        this.fieldAliasIndex = 1;
        this.tableAliasMap = new HashMap<>();
        this.tableObjMap = new HashMap<>();
        this.uniqueJoins = new HashSet<>();
        DbSpec dbSpec = new DbSpec();
        this.schema = dbSpec.addDefaultSchema();
    }


    public SelectQuery build() {
        try {
            if (Objects.nonNull(this.queryMaster.getSelect())) {
                processSelectFields(this.queryMaster.getSelect());
            }
            if (Objects.nonNull(this.queryMaster.getFilterConfig())) {
                processFilterConfig(this.queryMaster.getFilterConfig());
            }
            if (Objects.nonNull(this.queryMaster.getGroupBy())) {
                processGroupBy(this.queryMaster.getGroupBy());
            }
            if (Objects.nonNull(this.queryMaster.getOrderBy())) {
                processOrderBy(this.queryMaster.getOrderBy());
            }
            if (Objects.nonNull(this.queryMaster.getHaving())) {
                processHaving(this.queryMaster.getHaving());
            }

            setLimit(this.queryMaster.getLimit());
            setOffset(this.queryMaster.getOffset());
            this.selectQuery.validate();
        } catch (Exception e) {
            throw e;
        }
        return this.selectQuery;
    }

    private void processSelectFields(Select select) {
        List<BaseField> fields = select.getFields();
        for (BaseField field : fields) {
            SqlObject sqlFld = processField(field);
            String fieldAlias = StringUtils.isBlank(field.getFieldAlias()) ? getNewFieldAlias() : field.getFieldAlias();
            this.selectQuery.addCustomColumns(new Object[]{new CustomSql(sqlFld.toString() + " AS " + fieldAlias)});
        }
    }

    private void processFilterConfig(FilterConfig filterConfig) {
        List<FilterCriteria> filters = filterConfig.getFilters();
        for (FilterCriteria filter : filters) {
            Condition condition = processCondition(filter);
            this.selectQuery.addCondition(condition);
        }
    }

    private void processGroupBy(GroupBy groupBy) {
        List<BaseField> groupByFields = groupBy.getFields();
        for(BaseField groupByField: groupByFields) {
            SqlObject fld = processField(groupByField);
            this.selectQuery.addCustomGroupings(new Object[]{fld});
        }
    }

    private void processOrderBy(OrderBy orderBy) {
        List<BaseField> fields = orderBy.getFields();
        for(BaseField field : fields) {
            SqlObject fld = processField(field);
            OrderObject.Dir direction = SortOrder.ASC.equals(field.getSortOrder()) ? OrderObject.Dir.ASCENDING : OrderObject.Dir.DESCENDING;
            OrderObject orderObject = new OrderObject(direction, fld);
            this.selectQuery.addCustomOrderings(new Object[]{orderObject});
        }
    }

    private void processHaving(Having having) {
        List<FilterCriteria> filters = having.getFilters();
        for(FilterCriteria filter : filters) {
            Condition condition = processCondition(filter);
            this.selectQuery.addHaving(condition);
        }
    }

    private void setLimit(int limit) {
        if (limit != 0) {
            this.selectQuery.addCustomization(new PgLimitClause(this.queryMaster.getLimit()));
        }
    }

    private void setOffset(int offset) {
        if (offset != 0) {
            this.selectQuery.setOffset(this.queryMaster.getOffset());
        }
    }

    private Condition processCondition(FilterCriteria filterCondition) {
        BaseField filterField = filterCondition.getFilterField();
        SqlObject lhs = processField(filterField);
        List<Object> filterValues = filterCondition.getFilterValue().getValue();
        String op = filterCondition.getOperator().getSqlOperator();
        Object filterValue;
        if(DataType.INTEGER.equals(filterField.getDataType())) {
             filterValue = Integer.valueOf(filterValues.get(0).toString());
        } else {
             filterValue = filterValues.get(0).toString();
        }
        Condition condition = new BinaryCondition(op, lhs, filterValue);
        return condition;
    }

    private SqlObject processField(BaseField field) {
        FieldType fieldType = Objects.isNull(field.getFieldType()) ? FieldType.BASE : field.getFieldType();
        SqlObject sqlObject = null;
        switch (fieldType) {
            case BASE:
                sqlObject =  processBaseField(field);
                break;
            case MEASURE:
                sqlObject = processMeasure(field);
                break;
        }
        return sqlObject;
    }

    private FunctionCall processMeasure(BaseField measure) {
        SqlObject fld = processBaseField(measure);
        String alias = StringUtils.isBlank(measure.getFieldAlias())? getNewFieldAlias() : measure.getFieldAlias();
        measure.setFieldAlias(alias);
        FunctionCall functionCall = new FunctionCall(new CustomSql(measure.getAggregateFunction()));
        if (StringUtils.isBlank(measure.getFieldDbName())) {
            functionCall.addCustomParams(new Object[]{"*"});
        } else {
            functionCall.addCustomParams(new Object[]{fld});
        }
        return functionCall;
    }

    private SqlObject processBaseField(BaseField field) {
        DbTable table = null;
        String selectColumn = null;
        if (field.isHasLookup()) {
            table = processJoinField(field);
        } else {
            table = getTableFromField(field);
        }
        field.setTableAlas(table.getAlias());
        Column column = addColumn(table, field);
        selectColumn = table.getAlias() + "." + column.getColumnNameSQL();
        return new CustomSql(selectColumn);
    }

    private DbTable processFieldPaths(List<FieldPath> fieldPaths) {
        DbTable lookupTable = null;
        String key = "";
        JoinInfo joinInfo;
        Iterator<FieldPath> fpIterator = fieldPaths.iterator();
        while (fpIterator.hasNext()) {
            FieldPath fieldPath = fpIterator.next();
            String lookupName = this.getLookupName(fieldPath);
            key = StringUtils.isEmpty(key) ? lookupName : lookupName + "." + key;
            joinInfo = getJoinInfoFromFieldPath(fieldPath, lookupTable, key);
            String uniqueKey = joinInfo.fromTable.getAlias() + joinInfo.fromTableField + joinInfo.toTable.getAlias();
            if (!this.uniqueJoins.contains(uniqueKey)) {
                this.uniqueJoins.add(uniqueKey);
                com.healthmarketscience.sqlbuilder.SelectQuery.JoinType joinType = this.getHMSJoinTypeFromJoinType(fieldPath.getJoinType());
                this.selectQuery.addJoin(joinType, joinInfo.fromTable, joinInfo.toTable, new BinaryCondition(BinaryCondition.Op.EQUAL_TO, joinInfo.fromTableField, joinInfo.toTableField));

            }
            lookupTable = joinInfo.toTable;
        }
        return lookupTable;
    }

    private DbTable processJoinField(BaseField joinField) {
        FieldPath fieldPath = joinField.getFieldPath();
        List<FieldPath> fieldPaths = new ArrayList();
        List<JoinType> joinTypes = new ArrayList();
        this.fillInFieldPaths(fieldPath, fieldPaths, joinTypes);
        return this.processFieldPaths(fieldPaths);
    }

    private JoinInfo getJoinInfoFromFieldPath(FieldPath fieldPath, DbTable parentTable, String tableKey) {
        DbTable rightTable = Objects.isNull(parentTable) ? getTableFromField(fieldPath.getRight()) : parentTable;
        DbTable leftTable = getTableForLeftFieldInPath(fieldPath, tableKey);
        String lhsDBName = fieldPath.getLeft().getFieldDbName();
        String rhsDBName = fieldPath.getRight().getFieldDbName();
        DbColumn leftColumn = getColumnFromTable(leftTable, lhsDBName);
        DbColumn rightColumn = getColumnFromTable(rightTable, rhsDBName);
        com.healthmarketscience.sqlbuilder.SelectQuery.JoinType joinType = this.getHMSJoinTypeFromJoinType(fieldPath.getJoinType());
        return new JoinInfo(rightTable, leftTable, rightColumn, leftColumn, joinType);
    }

    private void fillInFieldPaths(FieldPath fieldPath, List<FieldPath> fieldPaths, List<JoinType> joinTypes) {
        fieldPaths.add(fieldPath);
        joinTypes.add(fieldPath.getJoinType() == null ? LEFT_JOIN : fieldPath.getJoinType());
        if (Objects.nonNull(fieldPath.getFieldPath())) {
            this.fillInFieldPaths(fieldPath.getFieldPath(), fieldPaths, joinTypes);
        }
    }

    private DbTable getTableForLeftFieldInPath(FieldPath fieldPath, String tableKey) {
        BaseField field = fieldPath.getLeft();
        BaseField joinField = fieldPath.getRight();
        String tableName = field.getObjectDbName();
        String baseTableName = joinField.getObjectDbName();
        String key = baseTableName + ":" + tableKey;

        DbTable table = this.tableObjMap.get(key);
        if (Objects.isNull(table)) {
            table = this.addTableForField(key, tableName);
        }
        return table;
    }

    private com.healthmarketscience.sqlbuilder.SelectQuery.JoinType getHMSJoinTypeFromJoinType(JoinType joinType) {
        com.healthmarketscience.sqlbuilder.SelectQuery.JoinType hmsJoinType = null;
        switch (joinType) {
            case RIGHT_JOIN:
                hmsJoinType = com.healthmarketscience.sqlbuilder.SelectQuery.JoinType.RIGHT_OUTER;
                break;
            case INNER_JOIN:
                hmsJoinType = com.healthmarketscience.sqlbuilder.SelectQuery.JoinType.INNER;
                break;
            case FULL_OUTER_JOIN:
                hmsJoinType = com.healthmarketscience.sqlbuilder.SelectQuery.JoinType.FULL_OUTER;
                break;
            case LEFT_JOIN:
            default:
                hmsJoinType = com.healthmarketscience.sqlbuilder.SelectQuery.JoinType.LEFT_OUTER;
        }

        return hmsJoinType;
    }

    private DbColumn getColumnFromTable(DbTable table, String fieldName) {
        DbColumn column = table.findColumn(fieldName);
        if (Objects.isNull(column)) {
            column = table.addColumn(fieldName);
        }
        return column;
    }

    private DbTable getTableFromField(BaseField field) {
        String tableName = field.getObjectDbName();
        DbTable table = this.tableObjMap.get(tableName);
        if (Objects.isNull(table)) {
            table = this.addTableForField(tableName);
            if (!this.fromTableIsSet && field.getFieldPath() == null) {
                this.selectQuery.addFromTable(table);
                this.fromTableIsSet = true;
            }
        }
        field.setTableAlas(table.getAlias());
        return table;
    }

    private DbTable addTableForField(String tableDBName) {
        return addTableForField(tableDBName, tableDBName);
    }

    private String getLookupName(FieldPath fieldPath) {
        return StringUtils.isNotBlank(fieldPath.getLookupName()) ? fieldPath.getLookupName() : fieldPath.getLookupId();
    }

    private DbTable addTableForField(String key, String tableDBName) {
        DbTable table;
        String tableAlias = this.getNewTableAlias();
        table = new DbTable(this.schema, tableDBName, tableAlias);
        this.tableObjMap.put(key, table);
        return table;
    }

    private String getNewTableAlias() {
        return "t" + (this.tableObjMap.size() + 1);
    }

    private Column addColumn(DbTable table, BaseField field) {
        String dbName = field.getFieldDbName();
        DbColumn column = table.findColumn(dbName);
        if (Objects.isNull(column)) {
            column = table.addColumn(dbName);
        }

        return column;
    }

    private String getNewFieldAlias() {
        String alias = "f" + this.fieldAliasIndex++;
        return alias;
    }

    private static final class JoinInfo {
        public DbTable fromTable;
        public DbTable toTable;
        public DbColumn fromTableField;
        public DbColumn toTableField;
        public com.healthmarketscience.sqlbuilder.SelectQuery.JoinType joinType;

        public JoinInfo(DbTable leftObj, DbTable rightObj, DbColumn leftFld, DbColumn rightFld, com.healthmarketscience.sqlbuilder.SelectQuery.JoinType joinType) {
            this.fromTable = leftObj;
            this.toTable = rightObj;
            this.fromTableField = leftFld;
            this.toTableField = rightFld;
            this.joinType = joinType;
        }

    }
}