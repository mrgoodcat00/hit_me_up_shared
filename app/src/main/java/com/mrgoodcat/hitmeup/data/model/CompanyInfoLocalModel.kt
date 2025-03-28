package com.mrgoodcat.hitmeup.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "company_info")
data class CompanyInfoLocalModel(
    @PrimaryKey
    @ColumnInfo(name = "company_id") val company_id: String = "",
    @ColumnInfo(name = "company_name") val company_name: String = "",
    @ColumnInfo(name = "company_namespace") val company_namespace: String = "",
    @ColumnInfo(name = "company_owner_name") val company_owner_name: String = "",
    @ColumnInfo(name = "company_owner_email") val company_owner_email: String = "",
    @ColumnInfo(name = "company_owner_id") val company_owner_id: String = "",
    @ColumnInfo(name = "company_auto_create_new_users") val company_auto_create_new_users: Boolean = true
) : Parcelable