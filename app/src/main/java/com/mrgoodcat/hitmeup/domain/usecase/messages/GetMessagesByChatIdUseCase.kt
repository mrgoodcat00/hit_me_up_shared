package com.mrgoodcat.hitmeup.domain.usecase.messages

import androidx.paging.PagingData
import com.mrgoodcat.hitmeup.data.model.MessageLocalModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

open class GetMessagesByChatIdUseCase @Inject constructor(private val dbRepository: DbRepository) {
    suspend fun execute(chatId: String): Flow<PagingData<MessageLocalModel>> {
        return dbRepository.getMessages(chatId)
    }
}