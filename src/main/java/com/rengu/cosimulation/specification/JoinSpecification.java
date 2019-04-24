package com.rengu.cosimulation.specification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.util.List;

/**
 * Author: XYmar
 * Date: 2019/4/18 15:53
 */
public class JoinSpecification implements Specification<Object> {
    private static Logger logger = LoggerFactory.getLogger(JoinSpecification.class);
    List<String> leftJoinFetchTables;
    List<String> innnerJoinFetchTables;
    List<String> rightJoinFetchTables;

    public JoinSpecification() {
    }

    @Override
    public Predicate toPredicate(Root<Object> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
        //because this piece of code may be run twice for pagination,
        //first time 'count' , second time 'select',
        //So, if this is called by 'count', don't join fetch tables.
        if (isCountCriteriaQuery(cq))
            return null;

        join(root, leftJoinFetchTables, JoinType.LEFT);
        join(root, innnerJoinFetchTables, JoinType.INNER);
        join(root, rightJoinFetchTables, JoinType.RIGHT);
        ((CriteriaQuery<Object>) cq).select(root);
        return null;
    }

    /*
        For Issue:
        when run repository.findAll(specs,page)
        The method toPredicate(...) upon will return a Predicate for Count(TableName) number of rows.
        In hibernate query, we cannot do "select count(table_1) from table_1 left fetch join table_2 where ..."
        Resolution:
        In this scenario, CriteriaQuery<?> is CriteriaQuery<Long>, because return type is Long.
        we don't fetch other tables where generating query for "count";
     */
    private boolean isCountCriteriaQuery(CriteriaQuery<?> cq) {
        return cq.getResultType().toString().contains("java.lang.Long");
    }


    private void join(Root<Object> root, List<String> joinFetchTables, JoinType type) {
        if (joinFetchTables != null && (joinFetchTables.size() > 0)) {
            logger.debug("{} join root {} with table {}",
                    type.name(), root.getJavaType(), joinFetchTables);
            for (String table : joinFetchTables) {
                if (table != null)
                    root.fetch(table, type);
            }
        }
    }

    public JoinSpecification setLeftJoinFetchTables(List<String> leftJoinFetchTables) {
        this.leftJoinFetchTables = leftJoinFetchTables;
        return this;
    }

    public JoinSpecification setInnerJoinFetchTables(List<String> innerJoinFetchTables) {
        this.innnerJoinFetchTables = innerJoinFetchTables;
        return this;
    }

    public JoinSpecification setRightJoinFetchTables(List<String> rightJoinFetchTables) {
        this.rightJoinFetchTables = rightJoinFetchTables;
        return this;
    }
}
