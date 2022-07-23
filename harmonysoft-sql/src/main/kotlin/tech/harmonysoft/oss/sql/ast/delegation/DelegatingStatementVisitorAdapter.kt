package tech.harmonysoft.oss.sql.ast.delegation

import net.sf.jsqlparser.statement.*
import net.sf.jsqlparser.statement.alter.Alter
import net.sf.jsqlparser.statement.alter.AlterSession
import net.sf.jsqlparser.statement.alter.AlterSystemStatement
import net.sf.jsqlparser.statement.alter.RenameTableStatement
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence
import net.sf.jsqlparser.statement.comment.Comment
import net.sf.jsqlparser.statement.create.index.CreateIndex
import net.sf.jsqlparser.statement.create.schema.CreateSchema
import net.sf.jsqlparser.statement.create.sequence.CreateSequence
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym
import net.sf.jsqlparser.statement.create.table.CreateTable
import net.sf.jsqlparser.statement.create.view.AlterView
import net.sf.jsqlparser.statement.create.view.CreateView
import net.sf.jsqlparser.statement.delete.Delete
import net.sf.jsqlparser.statement.drop.Drop
import net.sf.jsqlparser.statement.execute.Execute
import net.sf.jsqlparser.statement.grant.Grant
import net.sf.jsqlparser.statement.insert.Insert
import net.sf.jsqlparser.statement.merge.Merge
import net.sf.jsqlparser.statement.replace.Replace
import net.sf.jsqlparser.statement.select.Select
import net.sf.jsqlparser.statement.show.ShowTablesStatement
import net.sf.jsqlparser.statement.truncate.Truncate
import net.sf.jsqlparser.statement.update.Update
import net.sf.jsqlparser.statement.upsert.Upsert
import net.sf.jsqlparser.statement.values.ValuesStatement

abstract class DelegatingStatementVisitorAdapter : StatementVisitorAdapter() {
    
    abstract fun handle(statement: Statement)

    override fun visit(commit: Commit) {
        handle(commit)
    }

    override fun visit(select: Select) {
        handle(select)
    }

    override fun visit(delete: Delete) {
        handle(delete)
    }

    override fun visit(update: Update) {
        handle(update)
    }

    override fun visit(insert: Insert) {
        handle(insert)
    }

    override fun visit(replace: Replace) {
        handle(replace)
    }

    override fun visit(drop: Drop) {
        handle(drop)
    }

    override fun visit(truncate: Truncate) {
        handle(truncate)
    }

    override fun visit(createIndex: CreateIndex) {
        handle(createIndex)
    }

    override fun visit(createTable: CreateTable) {
        handle(createTable)
    }

    override fun visit(createView: CreateView) {
        handle(createView)
    }

    override fun visit(alter: Alter) {
        handle(alter)
    }

    override fun visit(stmts: Statements) {
        for (statement in stmts.statements) {
            handle(statement)
        }
    }

    override fun visit(execute: Execute) {
        handle(execute)
    }

    override fun visit(set: SetStatement) {
        handle(set)
    }

    override fun visit(merge: Merge) {
        handle(merge)
    }

    override fun visit(alterView: AlterView) {
        handle(alterView)
    }

    override fun visit(upsert: Upsert) {
        handle(upsert)
    }

    override fun visit(use: UseStatement) {
        handle(use)
    }

    override fun visit(comment: Comment) {
        handle(comment)
    }

    override fun visit(aThis: CreateSchema) {
        handle(aThis)
    }

    override fun visit(reset: ResetStatement) {
        handle(reset)
    }

    override fun visit(block: Block) {
        handle(block)
    }

    override fun visit(values: ValuesStatement) {
        handle(values)
    }

    override fun visit(describe: DescribeStatement) {
        handle(describe)
    }

    override fun visit(aThis: ExplainStatement) {
        handle(aThis)
    }

    override fun visit(aThis: ShowStatement) {
        handle(aThis)
    }

    override fun visit(set: ShowColumnsStatement) {
        handle(set)
    }

    override fun visit(showTables: ShowTablesStatement) {
        handle(showTables)
    }

    override fun visit(aThis: DeclareStatement) {
        handle(aThis)
    }

    override fun visit(grant: Grant) {
        handle(grant)
    }

    override fun visit(createSequence: CreateSequence) {
        handle(createSequence)
    }

    override fun visit(alterSequence: AlterSequence) {
        handle(alterSequence)
    }

    override fun visit(createFunctionalStatement: CreateFunctionalStatement) {
        handle(createFunctionalStatement)
    }

    override fun visit(createSynonym: CreateSynonym) {
        handle(createSynonym)
    }

    override fun visit(savepointStatement: SavepointStatement) {
        handle(savepointStatement)
    }

    override fun visit(rollbackStatement: RollbackStatement) {
        handle(rollbackStatement)
    }

    override fun visit(alterSession: AlterSession) {
        handle(alterSession)
    }

    override fun visit(ifElseStatement: IfElseStatement) {
        handle(ifElseStatement)
    }

    override fun visit(renameTableStatement: RenameTableStatement) {
        handle(renameTableStatement)
    }

    override fun visit(purgeStatement: PurgeStatement) {
        handle(purgeStatement)
    }

    override fun visit(alterSystemStatement: AlterSystemStatement) {
        handle(alterSystemStatement)
    }
}