package tech.harmonysoft.oss.sql.dsl.constraint

interface ConstraintVisitor<T> {

    fun visit(constraint: Constraint.Binary): T

    fun visit(constraint: Constraint.In): T

    fun visit(constraint: Constraint.NotIn): T

    fun visit(constraint: Constraint.IsNull): T

    fun visit(constraint: Constraint.IsNotNull): T

    fun visit(constraint: Constraint.Between): T

    companion object {

        fun removeValue(value: Any): ConstraintVisitor<Constraint?> {
            return object : ConstraintVisitor<Constraint?> {

                override fun visit(constraint: Constraint.Binary): Constraint? {
                    return constraint.takeIf { it.target != value }
                }

                override fun visit(constraint: Constraint.In): Constraint? {
                    return constraint.targetValues.filter {
                        it != value
                    }.takeIf { it.isNotEmpty() }?.let {
                        constraint.copy(targetValues = it)
                    }
                }

                override fun visit(constraint: Constraint.NotIn): Constraint? {
                    return constraint.targetValues.filter {
                        it != value
                    }.takeIf { it.isNotEmpty() }?.let {
                        constraint.copy(targetValues = it)
                    }
                }

                override fun visit(constraint: Constraint.IsNull): Constraint {
                    return constraint
                }

                override fun visit(constraint: Constraint.IsNotNull): Constraint {
                    return constraint
                }

                override fun visit(constraint: Constraint.Between): Constraint {
                    return constraint
                }
            }
        }
    }
}