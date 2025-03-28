package com.mrgoodcat.hitmeup.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Deprecated("Left it because it still present in Firebase DB")
@Entity(tableName = "company_info")
data class CompanyInfoModel(
    @PrimaryKey
    @ColumnInfo(name = "company_id") val company_id: String = "",
    @ColumnInfo(name = "company_name") val company_name: String = "",
    @ColumnInfo(name = "company_namespace") val company_namespace: String = "",
    @ColumnInfo(name = "company_owner_name") val company_owner_name: String = "",
    @ColumnInfo(name = "company_owner_email") val company_owner_email: String = "",
    @ColumnInfo(name = "company_owner_id") val company_owner_id: String = "",
    @ColumnInfo(name = "company_auto_create_new_users") val company_auto_create_new_users: Boolean = false
)