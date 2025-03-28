package com.mrgoodcat.hitmeup.domain.usecase.contacts

import androidx.paging.PagingData
import com.mrgoodcat.hitmeup.data.model.FriendLocalModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class GetAllUsersUseCase @Inject constructor(private val dbRepository: DbRepository) {
    fun execute(): Flow<PagingData<FriendLocalModel>> {
        return dbRepository.getFriends()
    }
}