package tech.harmonysoft.oss.sql.dsl.filter

abstract class AbstractFilterModificationVisitor : FilterVisitor<Filter?> {

    override fun visit(filter: Filter.And): Filter? {
        return filter.filters.mapNotNull {
            it.visit(this)
        }.takeIf { it.isNotEmpty() }?.let {
            if (it.size == 1) {
                it.first()
            } else {
                filter.copy(filters = it)
            }
        }
    }

    override fun visit(filter: Filter.Or): Filter? {
        return filter.filters.mapNotNull {
            it.visit(this)
        }.takeIf { it.isNotEmpty() }?.let {
            if (it.size == 1) {
                it.first()
            } else {
                filter.copy(filters = it)
            }
        }
    }

    override fun visit(filter: Filter.Not): Filter? {
        return filter.filter.visit(this)?.let {
            Filter.Not(it)
        }
    }
}