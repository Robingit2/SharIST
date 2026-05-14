package com.project.sharist.`data`.local

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.project.sharist.domain.model.User
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class UserDao_Impl(
  __db: RoomDatabase,
) : UserDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfUser: EntityInsertAdapter<User>
  init {
    this.__db = __db
    this.__insertAdapterOfUser = object : EntityInsertAdapter<User>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `users` (`id`,`userRole`,`vehicleNumber`,`vehicleModel`,`email`,`address`,`fullName`,`phone`,`document`,`password`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: User) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindText(2, entity.userRole)
        statement.bindText(3, entity.vehicleNumber)
        statement.bindText(4, entity.vehicleModel)
        statement.bindText(5, entity.email)
        statement.bindText(6, entity.address)
        statement.bindText(7, entity.fullName)
        statement.bindText(8, entity.phone)
        statement.bindText(9, entity.document)
        statement.bindText(10, entity.password)
      }
    }
  }

  public override suspend fun insertUser(user: User): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfUser.insert(_connection, user)
  }

  public override suspend fun getUserByEmail(email: String): User? {
    val _sql: String = "SELECT * FROM users WHERE email = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, email)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfUserRole: Int = getColumnIndexOrThrow(_stmt, "userRole")
        val _columnIndexOfVehicleNumber: Int = getColumnIndexOrThrow(_stmt, "vehicleNumber")
        val _columnIndexOfVehicleModel: Int = getColumnIndexOrThrow(_stmt, "vehicleModel")
        val _columnIndexOfEmail: Int = getColumnIndexOrThrow(_stmt, "email")
        val _columnIndexOfAddress: Int = getColumnIndexOrThrow(_stmt, "address")
        val _columnIndexOfFullName: Int = getColumnIndexOrThrow(_stmt, "fullName")
        val _columnIndexOfPhone: Int = getColumnIndexOrThrow(_stmt, "phone")
        val _columnIndexOfDocument: Int = getColumnIndexOrThrow(_stmt, "document")
        val _columnIndexOfPassword: Int = getColumnIndexOrThrow(_stmt, "password")
        val _result: User?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpUserRole: String
          _tmpUserRole = _stmt.getText(_columnIndexOfUserRole)
          val _tmpVehicleNumber: String
          _tmpVehicleNumber = _stmt.getText(_columnIndexOfVehicleNumber)
          val _tmpVehicleModel: String
          _tmpVehicleModel = _stmt.getText(_columnIndexOfVehicleModel)
          val _tmpEmail: String
          _tmpEmail = _stmt.getText(_columnIndexOfEmail)
          val _tmpAddress: String
          _tmpAddress = _stmt.getText(_columnIndexOfAddress)
          val _tmpFullName: String
          _tmpFullName = _stmt.getText(_columnIndexOfFullName)
          val _tmpPhone: String
          _tmpPhone = _stmt.getText(_columnIndexOfPhone)
          val _tmpDocument: String
          _tmpDocument = _stmt.getText(_columnIndexOfDocument)
          val _tmpPassword: String
          _tmpPassword = _stmt.getText(_columnIndexOfPassword)
          _result = User(_tmpId,_tmpUserRole,_tmpVehicleNumber,_tmpVehicleModel,_tmpEmail,_tmpAddress,_tmpFullName,_tmpPhone,_tmpDocument,_tmpPassword)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
