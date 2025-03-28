package com.mrgoodcat.hitmeup.domain.usecase.chats

import androidx.paging.PagingData
import com.mrgoodcat.hitmeup.data.model.ChatLocalModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class GetChatListUseCase @Inject constructor(private val dbRepository: DbRepository) {
    fun execute(): Flow<PagingData<ChatLocalModel>> {
        return dbRepository.getChats()
    }
}