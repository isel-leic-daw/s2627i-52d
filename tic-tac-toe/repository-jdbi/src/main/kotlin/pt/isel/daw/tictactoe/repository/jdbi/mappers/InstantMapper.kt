package pt.isel.daw.tictactoe.repository.jdbi.mappers

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.time.Instant

class InstantMapper : ColumnMapper<Instant> {
    @Throws(SQLException::class)
    override fun map(
        rs: ResultSet,
        columnNumber: Int,
        ctx: StatementContext,
    ): Instant? {
        val epochSeconds = rs.getLong(columnNumber)
        return if (rs.wasNull()) null else Instant.fromEpochSeconds(epochSeconds)
    }
}
