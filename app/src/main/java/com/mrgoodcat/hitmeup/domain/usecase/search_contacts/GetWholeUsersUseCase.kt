package com.mrgoodcat.hitmeup.domain.usecase.search_contacts

import com.mrgoodcat.hitmeup.data.model.FriendLocalModel
import com.mrgoodcat.hitmeup.domain.repository.DbRepository
import com.mrgoodcat.hitmeup.domain.repository.FirebaseUsersApi
import javax.inject.Inject


class GetWholeUsersUseCase @Inject constructor(
    private val firebaseUsersApi: FirebaseUsersApi,
    private val dbRepository: DbRepository
) {
    suspend fun execute(query: String): List<FriendLocalModel> {
        val globalSearchResult = firebaseUsersApi.globalsSearch(query)
        val userProfile = dbRepository.getUserProfile() ?: return globalSearchResult

        val filter = globalSearchResult.filter { currentItem ->
            (currentItem.user_id != userProfile.user_id) && currentItem.userDeleted.not()
        }

        return filter
    }
}