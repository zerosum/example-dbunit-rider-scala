import com.github.database.rider.core.api.dataset.JSONDataSet
import com.typesafe.scalalogging.LazyLogging
import org.dbunit.database.IDatabaseConnection
import org.dbunit.dataset.filter.DefaultColumnFilter
import org.dbunit.dataset.{ColumnFilterTable, IDataSet, ITable}
import org.dbunit.operation.DatabaseOperation
import org.dbunit.{JdbcDatabaseTester, Assertion => dbAssertion}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import sbt.io.IO

import java.io.File

class DatabaseTest extends AnyFunSpec with should.Matchers with BeforeAndAfterAll with LazyLogging {
  import implicits._

  describe("Sample test-cases with DBUnit and Database Rider") {
    it("case1: json as datasets works well!") {
      lazy val givenDataSet    = new JSONDataSet(new File("src/test/resources/fixtures/given.json"))
      lazy val expectedDataSet = new JSONDataSet(new File("src/test/resources/fixtures/expected.json"))

      withFixture(givenDataSet) { conn =>
        val actual = conn
          .createQueryTable("tweet", "select * from tweet")
          .excludeColumns("date")

        val expected = expectedDataSet.getTable("tweet")

        dbAssertion.assertEquals(expected, actual)
      }
    }
    it(
      "case2: check that case1's fixtures has been cleaned up. There is no unique-constraint-violation on using same given datasets."
    ) {
      lazy val givenDataSet = new JSONDataSet(new File("src/test/resources/fixtures/given.json"))
      withFixture(givenDataSet) { conn => }
    }
  }

  private lazy val container   = new PostgreSQLContainer("postgres:13.1")
  private lazy val logConsumer = new Slf4jLogConsumer(logger.underlying).withMdc("container", "postgres")

  private lazy val driverClassName = container.getDriverClassName
  private lazy val jdbcUrl         = container.getJdbcUrl
  private lazy val username        = container.getUsername
  private lazy val password        = container.getPassword

  private lazy val dbTester = new JdbcDatabaseTester(driverClassName, jdbcUrl, username, password, "test")

  private def setupSchema(dbTester: JdbcDatabaseTester): Unit = {
    lazy val initDdl = IO.read(new File("src/test/resources/fixtures/setup.ddl"))

    val iDbConn = dbTester.getConnection
    val conn    = iDbConn.getConnection

    val statement = conn.createStatement()
    statement.executeUpdate(initDdl)

    conn.close()
    iDbConn.close()
  }

  private def withFixture(dataSet: IDataSet)(testCode: IDatabaseConnection => Unit): Unit = {
    lazy val conn = dbTester.getConnection

    dbTester.setDataSet(dataSet)

    try {
      dbTester.setSetUpOperation(DatabaseOperation.INSERT)
      dbTester.onSetup()

      testCode(conn)
    } catch {
      case ex: Throwable => fail(ex.getMessage, ex)
    } finally {
      dbTester.setSetUpOperation(DatabaseOperation.DELETE)
      dbTester.onSetup()
    }
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()

    container.start()
    container.followOutput(logConsumer)

    setupSchema(dbTester)
  }

  override protected def afterAll(): Unit = {

    container.stop()

    super.afterAll()
  }

  object implicits {
    implicit class ITableExpander(iTable: ITable) {
      def excludeColumns(excludes: String*): ITable = {
        val columnFilter = excludes.toList.foldLeft(new DefaultColumnFilter) { (filter, name) =>
          filter.excludeColumn(name)
          filter
        }
        new ColumnFilterTable(iTable, columnFilter)
      }
    }
  }
}
